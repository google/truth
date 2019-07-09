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

import com.google.common.truth.Fact
import com.google.common.truth.Fact.fact
import com.google.common.truth.Fact.simpleFact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.google.common.truth.extensions.fields.FieldComparer.Configuration
import java.lang.reflect.Field
import kotlin.DeprecationLevel.ERROR

/**
 * A set of propositions for the [Truth] framework to test things about a class based on its fields,
 * using a reflective to specify or exclude fields under consideration.
 */
class ReflectiveFieldSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: Any?
) : Subject(failureMetadata, actual) {
    companion object {
        @JvmStatic
        fun fields(): Factory<ReflectiveFieldSubject, Any?> = Factory(::ReflectiveFieldSubject)
    }

    @Deprecated(
        message = "Do not use isEqualTo() directly on this subject",
        replaceWith = ReplaceWith("comparingAllFields().isEqualTo(expected)"),
        level = ERROR
    )
    override fun isEqualTo(expected: Any?): Unit =
        throw UnsupportedOperationException("Should use field-comparing methods before isEqualTo()")

    @Deprecated(
        message = "Do not use isEqualTo() directly on this subject",
        replaceWith = ReplaceWith("comparingAllFields().isNotEqualTo(unexpected)"),
        level = ERROR
    )
    override fun isNotEqualTo(unexpected: Any?): Unit =
        throw UnsupportedOperationException(
            "Should use field-comparing methods before isNotEqualTo()"
        )

    fun comparingFields(vararg fields: String): FieldFilteringTerm =
        FieldComparingTerm(FieldScanner(setOf(*fields)), Configuration.DEFAULT)

    fun comparingAllFields(): AllFieldsTerm =
        FieldComparingTerm(FieldScanner(), Configuration.DEFAULT)

    /**
     * The terminal term in the fluent call chain, containing the actual test methods, configured
     * by earlier calls in the chain.
     */
    interface TerminalTerm {
        fun isEqualTo(expected: Any?)
        fun isNotEqualTo(unexpected: Any?)
    }

    /**
     * A fluent chain term which can present in the case of "all fields" or "some fields" cases,
     * which optionally configures field exclusion, to reduce the set of fields under consideration.
     */
    interface FieldFilteringTerm : TerminalTerm {
        /** Configures the equality test to exclude the supplied fields from consideration. */
        fun except(vararg exclusions: String): TerminalTerm
    }

    /**
     * A fluent chain term which is present when all fields (as opposed to a whitelisted set) are
     * under consideration for field-by-field equality.  It offers further configuration of the
     * equality consideration by either excluding fields by name, or configuring a recursive
     * (deepFieldByField) equality.
     */
    interface AllFieldsTerm : FieldFilteringTerm, TerminalTerm {
        /**
         * Configures the equality test to exclude the supplied fields from consideration.
         *
         * If [respectDeclaredEquals] is true and the class contains its own `equals()` method
         * (meaning an `equals()` method other than the one from Any), then the method uses the
         * `equals()` for comparing the object instead of the fields individually.
         */
        fun recursively(respectDeclaredEquals: Boolean): TerminalTerm

        /**
         * Configures the equality test to exclude the supplied fields from consideration.
         *
         * If the class contains its own `equals()` method (meaning an `equals()` method other than
         * the one from Any), then the method uses the `equals()` for comparing the object instead
         * of the fields individually.
         */
        // Note, in Kotlin only you should use a default parameter, but this doesn't work well with
        // Java. Neither @JmvOverloads nor @JvmName are working in interfaces.
        fun recursively() = recursively(true)
    }

    private inner class FieldComparingTerm private constructor(
        private val comparer: FieldComparer<Fact>
    ) : AllFieldsTerm, FieldFilteringTerm, TerminalTerm {
        constructor(scanner: FieldScanner, configuration: Configuration) :
                this(FieldComparer(scanner, configuration, ::mismatchFact, ::typeMismatchFact))

        override fun except(vararg exclusions: String): FieldComparingTerm =
            FieldComparingTerm(
                comparer = comparer.copy(
                    scanner = comparer.scanner.copy(exclusions = setOf(*exclusions))
                )
            )

        override fun recursively(respectDeclaredEquals: Boolean): FieldComparingTerm =
            FieldComparingTerm(
                comparer.copy(
                    configuration = Configuration(
                        deep = true,
                        respectEquals = respectDeclaredEquals
                    )
                )
            )

        override fun isEqualTo(expected: Any?) {
            when {
                actual === expected -> {
                }
                actual == null || expected == null -> check("isEqualTo()").that(actual).isEqualTo(
                    expected
                )
                actual unlike expected -> check("isEqualTo()").that(actual).isEqualTo(expected)
                else -> {
                    val facts = comparer.compare(actual, expected).toList()
                    if (facts.isNotEmpty()) {
                        if (comparer.configuration.deep) {
                            failWithoutActual(
                                simpleFact("Some fields failed to match:"),
                                *facts.toTypedArray()
                            )
                        } else {
                            failWithoutActual(
                                fact(
                                    "Compared using fields",
                                    comparer.scanner.on(actual.javaClass).map(Field::getName)
                                ),
                                *(listOf(fact("resulted in mismatches", "")) + facts).toTypedArray()
                            )
                        }
                    }
                }
            }
        }

        override fun isNotEqualTo(unexpected: Any?) {
            when {
                actual === unexpected -> failWithoutActual(fact("expected not to be", unexpected))
                // Short circuit when one is null and the other isn't.
                actual == null || unexpected == null -> return
                else -> {
                    val facts = comparer.compare(actual, unexpected).toList()
                    if (facts.isEmpty()) {
                        if (comparer.configuration.deep) failWithActual(
                            fact(
                                "expected not to be",
                                unexpected
                            )
                        )
                        else {
                            failWithActual(
                                fact(
                                    "Compared using fields",
                                    comparer.scanner.on(actual.javaClass).map(Field::getName)
                                ),
                                fact("expected not to be", unexpected)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun mismatchFact(field: String, actual: Any?, expected: Any?) =
    if (actual is Collection<*> && expected is Collection<*> && actual.size == expected.size) {
        fact(
            "$field ", "expected: <$expected> (class: ${expected::class.java}) " +
                    "but was: <$actual> (class: ${actual::class.java})"
        ).iterable()
    } else {
        fact("$field ", "expected: <$expected> but was: <$actual>").iterable()
    }

private fun typeMismatchFact(field: String, actual: Any?, expected: Any?) =
    if (actual is Collection<*> && expected is Collection<*> && actual.size == expected.size) {
        fact(
            "$field ", "expected: <$expected> (class: ${expected::class.java}) " +
                    "but was: <$actual> (class: ${actual::class.java})"
        ).iterable()
    } else {
        fact("$field ", "expected: <$expected> but was: <$actual>").iterable()
    }

private fun Fact?.iterable(): Iterable<Fact> = if (this == null) emptyList() else listOf(this)
