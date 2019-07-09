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

import com.google.common.truth.Correspondence
import com.google.common.truth.extensions.fields.FieldComparer.Configuration

/**
 * A [Correspondence] for use in Truth subjects, which compares based on a field-by-field comparison
 * strategy, defined in [FieldComparer]
 */
object ReflectiveFieldCorrespondence {

    /**
     * Makes a correspondence which deeply (recursively) scans field equality across a graph.
     *
     * By default, if the type has an explicitly declared .equals() method (other than
     * Any's/Object's default implementation) then short-circuit on that. Otherwise, do further
     * field comparison. This facility can be configured by the [respectDeclaredEquals] parameter.
     */
    @JvmOverloads
    @JvmStatic
    fun deepFieldByField(respectDeclaredEquals: Boolean = true): Correspondence<Any, Any> =
        reflectiveFieldCorrespondence(
            FieldComparer(
                FieldScanner(),
                Configuration(true, respectDeclaredEquals),
                ::mismatch,
                ::mismatch
            )
        )

    /**
     * Makes a correspondence which compares field-by-field (shallow comparison for each field),
     * where the fields may be unfiltered, or filtered via a whitelist or an exclusion list.
     * Exclusions (if any) are subtracted from the whitelist (if present), or from the full set of
     * available fields.
     */
    @JvmStatic
    @JvmOverloads
    fun flatFieldByField(whitelisted: Set<String> = setOf(), exclude: Set<String> = setOf()) =
        reflectiveFieldCorrespondence(
            FieldComparer(
                FieldScanner(whitelisted, exclude), Configuration.DEFAULT, ::mismatch, ::mismatch
            )
        )

    private fun reflectiveFieldCorrespondence(
        comparer: FieldComparer<Boolean>
    ): Correspondence<Any, Any> {
        return Correspondence.from({ actual, expected ->
            // This compare returns a list of mismatches, so if there are any, it didn't match,
            // so we need to invert.
            !comparer.compare(actual, expected).any { it }
        }, description(comparer))
    }

    private fun description(comparer: FieldComparer<Boolean>) = when {
        comparer.configuration.deep -> "has fields which (deeply) match those of"
        else -> "has ${fieldsAffected(comparer)} which match those of"
    }

    private fun fieldsAffected(comparer: FieldComparer<Boolean>): String {
        val (whitelisted, exclusions) = comparer.scanner
        return when {
            whitelisted.isEmpty() && exclusions.isEmpty() -> "fields"
            whitelisted.isEmpty() && exclusions.isNotEmpty() -> "fields (excluding $exclusions)"
            whitelisted.isNotEmpty() && exclusions.isEmpty() -> "fields $whitelisted"
            else -> "fields ${whitelisted - exclusions}"
        }
    }
}

/** Simply returns true on a mismatch, to allow the presence of mismatches to be noted */
@Suppress("UNUSED_PARAMETER")
private fun mismatch(
    name: String,
    actual: Any?,
    expected: Any?
): Iterable<Boolean> = listOf(true)
