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
package com.google.common.truth.extensions.fields;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.google.common.truth.extensions.fields.ReflectiveFieldCorrespondence.flatFieldByField;
import static com.google.common.truth.extensions.fields.TruthSubjectTesting.VERIFY;
import static java.util.Arrays.asList;

/**
 * A subset of the other tests, rewritten in Java, both to prove out the java interop, and to show
 * usage in Java.
 */
@RunWith(JUnit4.class)
public class ReflectiveFieldJavaUsageTest {
    // Can't use an auto-value to test this as .equals will hijack the field-by field. In general
    // auto-value classes don't need this sort of assertion.
    private class Foo {
        private final String a;
        private final String b;

        Foo(String a, String b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            String stringRep = super.toString().split("@")[1];
            return "TestData@" + stringRep + "(a=" + a + ", b=" + b + ")";
        }
    }

    @Test
    public void correspondence_with_all_fields_success() {
        List<Foo> actual = asList(
            new Foo("foo", "bar"),
            new Foo("baz", "blah"),
            new Foo("qu", "arr"));
        List<Foo> expected = asList(
            new Foo("baz", "blah"),
            new Foo("foo", "bar"), // intentionally out of order
            new Foo("qu", "arr"));

        VERIFY.that(actual)
            .comparingElementsUsing(flatFieldByField())
            .containsExactlyElementsIn(expected);
    }

}
