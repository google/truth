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

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.fields.ReflectiveFieldCorrespondence.deepFieldByField
import com.google.common.truth.extensions.fields.ReflectiveFieldCorrespondence.flatFieldByField
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class ReflectiveFieldCorrespondenceTest {

    /**
     * A supertype for test classes that will contain fields reflectively scanned, but which may
     * have odd equals (to force certain test cases).  This supertype gives each a string
     * representation and a default (dumb, but correct) hashCode() implementation.
     */
    internal abstract class TestClass {
        override fun hashCode() =
            0 // For testing, this is never used as a key in a hashtable in production.

        protected val stringRep
            get() = "${javaClass.simpleName}@${super.toString().split("@")[1]}"
    }

    // Can't use a data class to test this as .equals will hijack the field-by field. In general
    // kotlin data classes don't need this sort of assertion.
    private class Foo(val a: String, val b: String) : TestClass() {
        override fun toString() = "TestData@$stringRep(a=$a, b=$b)"
    }

    @Test
    fun verify_test_class_fails_with_regular_iterablesubject() {
        val actual =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))

        val error = assertFailsWith<VerificationError> {
            VERIFY.that(actual).containsExactlyElementsIn(expected)
        }
        assertThat(error).hasMessageThat().contains("missing (3)")
        assertThat(error).hasMessageThat().contains("unexpected (3)")
    }

    @Test
    fun correspondence_with_all_fields_success() {
        val actual =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))

        VERIFY.that(actual).comparingElementsUsing(flatFieldByField())
            .containsExactlyElementsIn(expected)
    }

    @Test
    fun correspondence_with_all_fields_failure() {
        val actual =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("bar", "bar"), Foo("blah", "blah"), Foo("qu", "arr"))

        val error = assertFailsWith<VerificationError> {
            VERIFY.that(actual).comparingElementsUsing(flatFieldByField())
                .containsExactlyElementsIn(expected)
        }
        assertThat(error).hasMessageThat()
            .contains("contains exactly one element that has fields which match those of")
        assertThat(error).hasMessageThat()
            .contains("It is missing an element that has fields which match those of")
    }

    @Test
    fun correspondence_with_whitelisted_fields_success() {
        val actual =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("foo", "foo"), Foo("baz", "blah"), Foo("qu", "arr"))
        val fieldByField = flatFieldByField(whitelisted = setOf("a"))

        VERIFY.that(actual).comparingElementsUsing(fieldByField).containsExactlyElementsIn(expected)
    }

    @Test
    fun correspondence_with_whitelisted_fields_failure() {
        val actual =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("bar", "bar"), Foo("blah", "blah"), Foo("qu", "arr"))
        val fieldByField = flatFieldByField(whitelisted = setOf("a"))

        val error = assertFailsWith<VerificationError> {
            VERIFY.that(actual).comparingElementsUsing(fieldByField)
                .containsExactlyElementsIn(expected)
        }
        assertThat(error).hasMessageThat()
            .contains("contains exactly one element that has fields [a] which match those of")
        assertThat(error).hasMessageThat()
            .contains("It is missing an element that has fields [a] which match those of")
    }

    @Test
    fun correspondence_with_excluded_fields_success() {
        val actual =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("bar", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val fieldByField = flatFieldByField(exclude = setOf("a"))

        VERIFY.that(actual).comparingElementsUsing(fieldByField).containsExactlyElementsIn(expected)
    }

    @Test
    fun correspondence_with_excluded_fields_failure() {
        val actual =
            listOf(Foo("foo", "foo"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val fieldByField = flatFieldByField(exclude = setOf("a"))

        val error = assertFailsWith<VerificationError> {
            VERIFY.that(actual).comparingElementsUsing(fieldByField)
                .containsExactlyElementsIn(expected)
        }
        assertThat(error).hasMessageThat()
            .contains("contains exactly one element that has fields")
        assertThat(error).hasMessageThat()
            .contains("(excluding [a]) which match those of")
        assertThat(error).hasMessageThat()
            .contains("It is missing an element that has fields (excluding [a]) which match those")
    }

    @Test
    fun correspondence_with_whitelisted_and_excluded_fields_success() {
        val actual =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("bar", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val fieldByField = flatFieldByField(whitelisted = setOf("a", "b"), exclude = setOf("a"))

        VERIFY.that(actual).comparingElementsUsing(fieldByField).containsExactlyElementsIn(expected)
    }

    @Test
    fun correspondence_with_whitelisted_and_excluded_fields_failure() {
        val actual =
            listOf(Foo("foo", "foo"), Foo("baz", "blah"), Foo("qu", "arr"))
        val expected =
            listOf(Foo("foo", "bar"), Foo("baz", "blah"), Foo("qu", "arr"))
        val fieldByField = flatFieldByField(whitelisted = setOf("a", "b"), exclude = setOf("a"))

        val error = assertFailsWith<VerificationError> {
            VERIFY.that(actual).comparingElementsUsing(fieldByField)
                .containsExactlyElementsIn(expected)
        }
        assertThat(error).hasMessageThat()
            .contains("contains exactly one element that has fields [b] which match those of")
        assertThat(error).hasMessageThat()
            .contains("It is missing an element that has fields [b] which match those of")
    }

    private class A(val data: String) : TestClass() {
        override fun toString() = "$stringRep(data=$data)"
    }

    private class B(val data: String, val a: A) : TestClass() {
        override fun toString() = "$stringRep(data=$data, a=$a)"
    }

    @Test
    fun verify_nested_test_classes_fails_with_regular_iterablesubject() {
        val actual = listOf(
            B("foo", A("bar")),
            B("baz", A("blah")),
            B("qu", A("arr"))
        )
        val expected = listOf(
            B("foo", A("bar")),
            B("baz", A("blah")),
            B("qu", A("arr"))
        )

        val error = assertFailsWith<VerificationError> {
            VERIFY.that(actual).containsExactlyElementsIn(expected)
        }
        assertThat(error).hasMessageThat().contains("missing (3)")
        assertThat(error).hasMessageThat().contains("unexpected (3)")
    }

    @Test
    fun correspondence_with_recursively_matched_fields_success() {
        val actual = listOf(
            B("foo", A("bar")),
            B("baz", A("blah")),
            B("qu", A("arr"))
        )
        val expected = listOf(
            B("foo", A("bar")),
            B("baz", A("blah")),
            B("qu", A("arr"))
        )

        VERIFY.that(actual).comparingElementsUsing(deepFieldByField())
            .containsExactlyElementsIn(expected)
    }

    @Test
    fun correspondence_with_recursively_matched_fields_failure() {
        val actual = listOf(
            B("foo", A("bar")),
            B("baz", A("blah")),
            B("qu", A("arr"))
        )
        val expected = listOf(
            B("foo", A("foo")),
            B("baz", A("blah")),
            B("qu", A("arr"))
        )

        val error = assertFailsWith<VerificationError> {
            VERIFY.that(actual).comparingElementsUsing(deepFieldByField())
                .containsExactlyElementsIn(expected)
        }
        assertThat(error).hasMessageThat()
            .contains("contains exactly one element that has fields which (deeply) match those of")
        assertThat(error).hasMessageThat()
            .contains("It is missing an element that has fields which (deeply) match those of")
    }

    private class C(val data: String, val ignored: String) : TestClass() {
        override fun toString() = "$stringRep(data=$data, ignored=$ignored)"
        override fun equals(other: Any?) = other is C && data == other.data
    }

    private class D(val data: String, val c: C) : TestClass() {
        override fun toString() = "$stringRep(data=$data, c=$c)"
    }

    @Test
    fun correspondence_with_recursively_matched_fields_respecting_equals() {
        val actual = listOf(
            D("foo", C("bar", "foo")),
            D("baz", C("blah", "baz"))
        )
        val expected = listOf(
            D("foo", C("bar", "bar")),
            D("baz", C("blah", "baz"))
        )

        VERIFY.that(actual)
            .comparingElementsUsing(deepFieldByField(true))
            .containsExactlyElementsIn(expected)
        assertFailsWith<VerificationError> {
            VERIFY.that(actual)
                .comparingElementsUsing(deepFieldByField(false))
                .containsExactlyElementsIn(expected)
        }
    }
}
