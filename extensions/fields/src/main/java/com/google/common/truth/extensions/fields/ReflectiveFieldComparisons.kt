/*
 * License Copyright 2019 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.common.truth.extensions.fields

import java.lang.IllegalArgumentException
import java.lang.reflect.Field
import java.util.ArrayDeque

/**
 * Does a field-by-field comparison of two objects, invoking workhorse lambdas on specific
 * mismatches.  This can result in different outcomes - e.g. comparator-style -1, 0, 1 output, or
 * a matching predicate, or an accumulation of mismatches as Truth errors.
 */
internal data class FieldComparer<R>(
    val scanner: FieldScanner,
    val configuration: Configuration = Configuration.DEFAULT,
    private val mismatch: (name: String, actual: Any?, expected: Any?) -> Iterable<R>,
    private val typeMismatch: (name: String, actual: Any?, expected: Any?) -> Iterable<R>
) {

    data class Configuration(val deep: Boolean, val respectEquals: Boolean) {
        init {
            check(deep || !respectEquals) {
                "When deep is false respectEquals cannot be true."
            }
        }

        companion object {
            val DEFAULT = Configuration(deep = false, respectEquals = false)
        }
    }

    private fun compareLeaf(path: ComparisonPath, actual: Any?, expected: Any?): Iterable<R> =
        if (actual != expected) mismatch(path.fieldName(), actual, expected) else emptyList()

    /**
     * Performs a field-by-field comparison between [act] and [exp] invoking the lambdas
     * [mismatch] and [typeMismatch] on unequal fields or types.
     *
     * > Note: Does not cross over collections or maps (uses their .equals method).  Also ignores
     * > other [equals] methods unless to honor them. (see [Configuration])
     */
    fun compare(act: Any?, exp: Any?): Iterable<R> =
    // Sadly, the === and null-or checks must be duplicated here, where there is no field, as well
    // as in compare(path, act, exp, configuration) since in this method we have no field, whereas
        // in compare(path, act, exp, configuration) a path exists.
        when {
            act === exp -> emptyList() // Short circuit reference (or null) equality
            act == null || exp == null -> mismatch("", act, exp) // no field here.
            else -> compareBranch(ComparisonPath(Pair(act, exp)), act, exp, configuration)
        }

    private fun compareBranch(
        path: ComparisonPath,
        act: Any?,
        exp: Any?,
        configuration: Configuration
    ): Iterable<R> {
        // Shorter names chosen here, to allow more consistent visualization in the decision table
        return when {
            act === exp -> emptyList() // Short circuit reference (or null) equality
            act == null || exp == null -> compareLeaf(path, act, exp)
            act unlike exp -> typeMismatch(path.fieldName(), act, exp)
            else -> when (act) {
                is String,
                is Number,
                is Boolean,
                is Char,
                is Enum<*> -> compareLeaf(path, act, exp)
                is Array<*> -> compareLeaf(path, act.asList(), (exp as Array<*>).asList())
                is BooleanArray -> compareLeaf(path, act.asList(), (exp as BooleanArray).asList())
                is CharArray -> compareLeaf(path, act.asList(), (exp as CharArray).asList())
                is DoubleArray -> compareLeaf(path, act.asList(), (exp as DoubleArray).asList())
                is FloatArray -> compareLeaf(path, act.asList(), (exp as FloatArray).asList())
                is IntArray -> compareLeaf(path, act.asList(), (exp as IntArray).asList())
                is LongArray -> compareLeaf(path, act.asList(), (exp as LongArray).asList())
                is ShortArray -> compareLeaf(path, act.asList(), (exp as ShortArray).asList())
                is Collection<*> -> compareLeaf(path, act, exp as Collection<*>)
                else -> {
                    if (configuration.respectEquals && act.javaClass.hasDeclaredEquals()) {
                        return compareLeaf(path, act, exp)
                    }

                    val fields = scanner.on(act.javaClass)
                    fields.flatMap {
                        val compareData =
                            CompareData(field = it, actual = it.get(act), expected = it.get(exp))
                        if (path.seen(compareData)) emptyList()
                        else {
                            path.inContext(compareData) { data ->
                                if (!configuration.deep) compareLeaf(
                                    path,
                                    data.actual,
                                    data.expected
                                )
                                else compareBranch(path, data.actual, data.expected, configuration)
                            }
                        }
                    }
                }
            }
        }
    }
}

private class CompareData(val field: Field, val actual: Any?, val expected: Any?) {
    override fun toString() = "CompareData(field=${field.name}, actual=$actual, expected=$expected"
    override fun equals(other: Any?) =
        other is CompareData && field == other.field && actual === other.actual &&
                expected === other.expected
}

private class ComparisonPath(
    val root: Pair<Any, Any>,
    private val stack: ArrayDeque<CompareData> = ArrayDeque()
) {
    fun <R> inContext(e: CompareData, work: (e: CompareData) -> R): R {
        stack.push(e)
        return work(e).also { stack.pop() }
    }

    override fun toString() =
        "CyclePath(root=${root.first.javaClass.simpleName}, stack=${stack.map { it.field.name }}"

    fun fieldName() =
        "${rootType().simpleName}.${stack.reversed().joinToString(".") { it.field.name }}"

    fun seen(compareData: CompareData) =
        !stack.isEmpty() &&
                root.first === compareData.actual &&
                root.first === compareData.expected ||
                stack.contains(compareData)

    fun rootType(): Class<*> =
        if (stack.isEmpty()) root.first.javaClass else stack.peekLast().field.declaringClass
}

/** Scans a class for all declared fields, based optionally on a whitelist and exclusion list */
internal data class FieldScanner internal constructor(
    val whitelisted: Set<String> = setOf(),
    val exclusions: Set<String> = setOf()
) {
    /**
     * Scans [clazz] for declared fields, optionally filtering with a whitelist and exclusion list,
     * throwing an [IllegalArgumentException] when a non-existent field is specified in the
     * inclusion list.
     */
    fun on(clazz: Class<Any>?)
        = declaredFieldsFromHierarchyOf(clazz)
            .toList()
            .also { fields ->
                if (whitelisted.isNotEmpty()) {
                    val extras = whitelisted.subtract(fields.map { it.name })
                    if (extras.isNotEmpty()) {
                        throw IllegalArgumentException(
                            "Comparing type: ${clazz?.name} using non-existent fields: $extras")
                    }
                }
            }
            .onEach { it.isAccessible = true }
            .filter { if (whitelisted.isEmpty()) true else whitelisted.contains(it.name) }
            .filter { if (exclusions.isEmpty()) true else !exclusions.contains(it.name) }

    /** Returns the full sequence of declared fields from the type hierarchy of [clazz] */
    private fun declaredFieldsFromHierarchyOf(clazz: Class<*>?): Sequence<Field> {
        return if (clazz == null) sequenceOf()
        else sequenceOf(*clazz.declaredFields) + declaredFieldsFromHierarchyOf(clazz.superclass)
    }
}

internal fun Class<*>.hasDeclaredEquals(): Boolean {
    if (this == Any::class.java) return false
    val hasEqualsMethodError = this.declaredMethods.any { method ->
        method.name == "equals" && method.parameterTypes.let {
            it.size == 1 && it[0] == Any::class.java
        }
    }
    return hasEqualsMethodError || superclass.hasDeclaredEquals()
}
