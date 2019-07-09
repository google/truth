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
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ReflectiveFieldComparisonsTest {

    private open class Foo
    private open class Bar : Foo() {
        override fun equals(other: Any?) = true
    }

    private class Baz : Bar()
    private class Quf : Foo()

    @Test
    fun hasDeclaredEquals() {
        assertThat(Foo::class.java.hasDeclaredEquals()).isFalse()
        assertThat(Bar::class.java.hasDeclaredEquals()).isTrue()
        assertThat(Baz::class.java.hasDeclaredEquals()).isTrue()
        assertThat(Quf::class.java.hasDeclaredEquals()).isFalse()
    }
}
