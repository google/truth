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
import com.google.common.truth.extensions.fields.ReflectiveFieldSubject.Companion.fields
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.LinkedList
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class ReflectiveFieldSubjectTest {

    data class TestData(val a: String?, val b: String?)

    @Test
    fun comparingAllFields_null_actual_isEqualTo_null_expected_success() {
        VERIFY.about(fields()).that(null).comparingAllFields().isEqualTo(null)
    }

    @Test
    fun comparingAllFields_null_actual_isEqualTo_notnull_expected_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(null)
                .comparingAllFields()
                .isEqualTo(TestData("a", "a"))
        }
        assertThat(error).hasMessageThat().contains("expected           : TestData(a=a, b=a)")
        assertThat(error).hasMessageThat().contains("but was            : null")
    }

    @Test
    fun comparingAllFields_notnull_actual_isEqualTo_null_expected_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData("a", "a"))
                .comparingAllFields()
                .isEqualTo(null)
        }
        assertThat(error).hasMessageThat().contains("expected           : null")
        assertThat(error).hasMessageThat().contains("but was            : TestData(a=a, b=a)")
    }

    @Test
    fun comparingAllFields_where_fields_are_null_actual_isEqualTo_null_expected_success() {
        VERIFY.about(fields()).that(TestData(null, "b"))
            .comparingAllFields()
            .isEqualTo(TestData(null, "b"))
    }

    @Test
    fun comparingAllFields_where_fields_are_null_actual_isEqualTo_notnull_expected_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData(null, "b"))
                .comparingAllFields()
                .isEqualTo(TestData("a", "a"))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields : [a, b]")
        assertThat(error).hasMessageThat()
            .contains("TestData.a            : expected: <a> but was: <null>")
    }

    @Test
    fun comparingAllFields_where_fields_are_notnull_actual_isEqualTo_null_expected_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData("a", "a"))
                .comparingAllFields()
                .isEqualTo(TestData(null, "a"))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields : [a, b]")
        assertThat(error).hasMessageThat()
            .contains("TestData.a            : expected: <null> but was: <a>")
    }

    @Test
    fun comparingAllFields_isEqualTo_success() {
        VERIFY.about(fields())
            .that(TestData("a", "b"))
            .comparingAllFields()
            .isEqualTo(TestData("a", "b"))
    }

    @Test
    fun comparingAllFields_isEqualTo_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData("a", "b"))
                .comparingAllFields()
                .isEqualTo(TestData("a", "a"))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields : [a, b]")
        assertThat(error).hasMessageThat()
            .contains("TestData.b            : expected: <a> but was: <b>")
    }

    @Test
    fun comparingFields_isEqualTo_success() {
        VERIFY.about(fields())
            .that(TestData("a", "b"))
            .comparingFields("a")
            .isEqualTo(TestData("a", "a"))
    }

    @Test
    fun comparingFields_isEqualTo_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData("a", "b"))
                .comparingFields("b")
                .isEqualTo(TestData("a", "a"))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields : [b]")
        assertThat(error).hasMessageThat()
            .contains("TestData.b            : expected: <a> but was: <b>")
    }

    @Test
    fun comparingFields_non_existant_field_failure() {
        val error = assertFailsWith<IllegalArgumentException> {
            VERIFY.about(fields())
                .that(TestData("a", "b"))
                .comparingFields("nonexistent_field")
                .isEqualTo(TestData("a", "a"))
        }
        assertThat(error).hasMessageThat().contains("Comparing type")
        assertThat(error).hasMessageThat().contains("\$TestData using non-existent fields")
        assertThat(error).hasMessageThat().contains("[nonexistent_field]")
   }

    @Test
    fun comparingAllFieldsExcept_isEqualTo_success() {
        VERIFY.about(fields())
            .that(TestData("a", "b"))
            .comparingAllFields()
            .except("b")
            .isEqualTo(TestData("a", "a"))
    }

    @Test
    fun comparingAllFieldsExcept_isEqualTo_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData("a", "b"))
                .comparingAllFields()
                .except("a")
                .isEqualTo(TestData("a", "a"))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields : [b]")
        assertThat(error).hasMessageThat()
            .contains("TestData.b            : expected: <a> but was: <b>")
    }

    @Test
    fun comparingAllFields_isNotEqualTo_success() {
        VERIFY.about(fields())
            .that(TestData("a", "b"))
            .comparingAllFields()
            .isNotEqualTo(TestData("a", "a"))
    }

    @Test
    fun comparingAllFields_isNotEqualTo_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData("a", "b"))
                .comparingAllFields()
                .isNotEqualTo(TestData("a", "b"))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields: [a, b]")
        assertThat(error).hasMessageThat().contains("expected not to be   : TestData(a=a, b=b)")
        assertThat(error).hasMessageThat().contains("but was              : TestData(a=a, b=b)")
    }

    @Test
    fun comparingFields_isNotEqualTo_success() {
        VERIFY.about(fields())
            .that(TestData("a", "b"))
            .comparingFields("b")
            .isNotEqualTo(TestData("a", "a"))
    }

    @Test
    fun comparingFields_isNotEqualTo_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData("a", "b"))
                .comparingFields("b")
                .isNotEqualTo(TestData("a", "b"))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields: [b]")
        assertThat(error).hasMessageThat().contains("expected not to be   : TestData(a=a, b=b)")
        assertThat(error).hasMessageThat().contains("but was              : TestData(a=a, b=b)")
    }

    @Test
    fun comparingFieldsExcept_isNotEqualTo_success() {
        VERIFY.about(fields())
            .that(TestData("a", "b"))
            .comparingAllFields()
            .except("a")
            .isNotEqualTo(TestData("a", "a"))
    }

    @Test
    fun comparingFieldsExcept_isNotEqualTo_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(TestData("a", "b"))
                .comparingAllFields()
                .except("b")
                .isNotEqualTo(TestData("a", "a"))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields: [a]")
        assertThat(error).hasMessageThat().contains("expected not to be   : TestData(a=a, b=a)")
        assertThat(error).hasMessageThat().contains("but was              : TestData(a=a, b=b)")
    }

    abstract class A(protected val a: String)
    class B(a: String, val b: String) : A(a) {
        override fun toString() = "B(a=$a, b=$b)"
        override fun equals(other: Any?) =
            a::class == b::class && a == (other as B).a && b == other.b

        override fun hashCode() =
            0 // For testing, this is never used as a key in a hashtable in production.
    }

    @Test
    fun comparingAllFields_isEqualTo_inherited_types_success() {
        VERIFY.about(fields())
            .that(B("a", "b"))
            .comparingAllFields()
            .isEqualTo(B("a", "b"))
    }

    @Test
    fun comparingAllFields_isEqualTo_inherited_types_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(B("a", "b"))
                .comparingAllFields()
                .isEqualTo(B("q", "a"))
        }

        assertThat(error).hasMessageThat().contains("Compared using fields : [b, a]")
        assertThat(error).hasMessageThat()
            .contains("B.b                   : expected: <a> but was: <b>")
        assertThat(error).hasMessageThat()
            .contains("A.a                   : expected: <q> but was: <a>")
    }

    data class Foo(val b: B)
    data class Bar(val foo: Foo, val q: String)
    data class Blah(val bar: Bar)

    @Test
    fun comparingAllFields_recursively_isEqualTo_success() {
        VERIFY.about(fields())
            .that(Blah(Bar(Foo(B("some_A", "some_B")), "q")))
            .comparingAllFields()
            .recursively()
            .isEqualTo(Blah(Bar(Foo(B("some_A", "some_B")), "q")))
    }

    @Test
    fun comparingAllFields_recursively_isEqualTo_failure() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(Blah(Bar(Foo(B("A", "B")), "q")))
                .comparingAllFields()
                .recursively(respectDeclaredEquals = false)
                .isEqualTo(Blah(Bar(Foo(B("NotA", "B")), "a")))
        }
        assertThat(error).hasMessageThat()
            .contains("Blah.bar.foo.b.a : expected: <NotA> but was: <A>")
        assertThat(error).hasMessageThat().contains("Blah.bar.q       : expected: <a> but was: <q>")
    }

    class Cycle(var a: TestA? = null, val data: String) {
        override fun equals(that: Any?) = that is Cycle && data == that.data && a === that.a
        override fun hashCode() = 1000003 xor data.hashCode()
        override fun toString() = "Cycle(data=$data, a=TestA@${a.hashCode()}"
    }

    data class TestA(val b: TestB)
    data class TestB(val c: Cycle)

    @Test
    fun comparingAllFields_recursively_WithCycles_simple() {
        val actual = TestA(TestB(Cycle(data = "foo")))
        val expected = TestA(TestB(Cycle(data = "foo")))

        // make the cycles
        actual.b.c.a = actual
        expected.b.c.a = expected

        VERIFY.about(fields())
            .that(actual)
            .comparingAllFields()
            .recursively(respectDeclaredEquals = false)
            .isEqualTo(expected)
    }

    @Test
    fun comparingAllFields_recursively_WithCycles_cross_cycle() {
        val actual = TestA(TestB(Cycle(data = "foo")))
        val expected = TestA(TestB(Cycle(data = "foo")))

        // Make a cross-cycle
        actual.b.c.a = expected
        expected.b.c.a = actual

        // This technically succeeds, because the cross cycle makes both identical, just starting
        // at different parts in the cycle.  But since they're the same type, and they loop around,
        // they're the same, field-by-field.
        VERIFY.about(fields())
            .that(actual)
            .comparingAllFields()
            .recursively(respectDeclaredEquals = false)
            .isEqualTo(expected)
    }

    @Test
    fun comparingAllFields_recursively_WithCycles_cross_cycle_different_start() {
        val actual = TestA(TestB(Cycle(data = "foo")))
        val expected = TestB(Cycle(data = "foo"))
        val expectedA = TestA(expected)

        // Make a cross-cycle
        actual.b.c.a = expectedA
        expected.c.a = actual

        // This fails, despite being the same object as the above, because of the different types
        // under consideration.  The internal cycle has implications on hashcode/equals/tostring,
        // thought, so these are manually tweaked to avoid infinite cycles, as Truth relies on
        // having valid (non-infinitely recursing) implementations of these.
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(actual)
                .comparingAllFields()
                .recursively()
                .isEqualTo(expected)
        }
        // Trimmed off the hashcode from the error message.
        assertThat(error).hasMessageThat()
            .contains("expected           : TestB(c=Cycle(data=foo, a=TestA@")
        assertThat(error).hasMessageThat()
            .contains("but was            : TestA(b=TestB(c=Cycle(data=foo, a=TestA@")
    }

    sealed class State {
        abstract val abstractField: String
        data class StateA(val state: Int, override val abstractField: String) : State()
        data class StateB(val state: Int, override val abstractField: String) : State()
    }

    /*
     * For now, we don't support inter-type comparisons by field, which unfortunately rules out
     * certain use of abstract properties (because the field in A is not the same as the field in
     * B).
     *
     * This could be fixed for kotlin properties with kotlin reflection using properties as a first
     * class item, or it could be fixed by getting the get<property>() accessor and preferring that.
     * For now, however, it's out of scope. This will catch type inconsistencies.
     */
    @Test
    fun comparingAllFields_isEqualTo_abstract_properties_error() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(State.StateA(1, "bar"))
                .comparingAllFields()
                .isEqualTo(State.StateB(1, "foo"))
        }
        assertThat(error).hasMessageThat()
            .contains("expected           : StateB(state=1, abstractField=foo)")
        assertThat(error).hasMessageThat()
            .contains("but was            : StateA(state=1, abstractField=bar)")
    }

    data class Stateful(val state: State)

    @Test
    fun comparingAllFields_isEqualTo_abstract_properties_error_deep() {
        val error = assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(Stateful(State.StateA(1, "bar")))
                .comparingAllFields()
                .isEqualTo(Stateful(State.StateB(1, "foo")))
        }
        assertThat(error).hasMessageThat().contains("Compared using fields : [state]")
        assertThat(error).hasMessageThat()
            .contains(
                "Stateful.state        : expected: <StateB(state=1, abstractField=foo)> " +
                        "but was: <StateA(state=1, abstractField=bar)>"
            )
    }

    @Test
    fun comparingAllFields_recursively_isEqualTo_collection() {
        class Holder(@Suppress("unused") val collection: Collection<String>)

        val holder = Holder(LinkedList())

        // Different list types are equal.
        VERIFY.about(fields())
            .that(Holder(ArrayList()))
            .comparingAllFields()
            .recursively()
            .isEqualTo(holder)

        // List with one element is unequal.
        assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(Holder(LinkedList<String>().apply { add("1 element") }))
                .comparingAllFields()
                .recursively()
                .isEqualTo(holder)
        }.let { assertThat(it).hasMessageThat() }.also {
            it.contains("Some fields failed to match:")
            it.contains("expected: <[]> but was: <[1 element]>")
        }

        // Set is unequal.
        assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(Holder(emptySet()))
                .comparingAllFields()
                .recursively()
                .isEqualTo(holder)
        }.let { assertThat(it).hasMessageThat() }.also {
            it.contains("Some fields failed to match:")
            it.contains(
                "expected: <[]> (class: class java.util.LinkedList) but was: <[]> " +
                        "(class: class kotlin.collections.EmptySet)"
            )
        }
    }

    @Suppress("unused")
    @Test
    fun comparingAllFields_recursively_respects_declared_equals_method() {
        data class InnerWithEquals(val string: String) {
            override fun equals(other: Any?): Boolean = true
            override fun hashCode(): Int = 0
        }

        class HolderWithEquals(val inner: InnerWithEquals)

        class InnerWithoutEquals(val string: String)
        class HolderWithoutEquals(val inner: InnerWithoutEquals)

        // Uses the custom equals() method and is always equal
        VERIFY.about(fields())
            .that(HolderWithEquals(InnerWithEquals("hello")))
            .comparingAllFields()
            .recursively()
            .isEqualTo(HolderWithEquals(InnerWithEquals("world")))

        // List with one element is unequal.
        assertFailsWith<VerificationError> {
            VERIFY.about(fields())
                .that(HolderWithoutEquals(InnerWithoutEquals("hello")))
                .comparingAllFields()
                .recursively()
                .isEqualTo(HolderWithoutEquals(InnerWithoutEquals("world")))
        }.let { assertThat(it).hasMessageThat() }
            .also {
                it.contains("expected: <world> but was: <hello>")
            }
    }
}
