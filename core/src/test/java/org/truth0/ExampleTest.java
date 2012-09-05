/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.truth0;

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;
import static org.truth0.Truth.ASSUME;
import static org.truth0.delegatetest.FooSubject.FOO;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.truth0.delegatetest.Foo;

@RunWith(Theories.class)
public class ExampleTest {
  @Test public void stringContains() {
    ASSERT.that("abc").contains("c");
  }

  @Test public void listHasElements() {
    // single item
    ASSERT.that(Arrays.asList(1, 2, 3)).has().item(1);

    // at least these items
    ASSERT.that(Arrays.asList(1, 2, 3)).has().allOf(1, 2);

    // at least one of these items
    ASSERT.that(Arrays.asList(1, 2, 3)).has().anyOf(1, 5);
  }

  @Test public void equalityFail() {
    int x = 2 + 2;
    try {
      ASSERT.that(x).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).contains("Not true that <4> is equal to <5>");
    }
  }

  @DataPoints public static int[] ints = { -1, 0, 1, 2 };

  @Theory public void divideBySelf(int x) {
    ASSUME.that(x).isNotEqualTo(0);
    ASSERT.that(x / x).isEqualTo(1);
  }

  @Rule public final Expect EXPECT = Expect.create();

  @Test public void expectRange() {
    int x = 4;
    EXPECT.that(x).isNotNull();
    EXPECT.that(x).isBetween(3, 5);
    EXPECT.that(x).isEqualTo(4);
  }

  @Test public void customTypeCompares() {
    ASSERT.about(FOO).that(new Foo(5)).matches(new Foo(2 + 3));
  }
}
