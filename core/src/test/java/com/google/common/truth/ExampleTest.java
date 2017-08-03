/*
 * Copyright (c) 2011 Google, Inc.
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
package com.google.common.truth;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static com.google.common.truth.delegation.FooSubject.foo;

import com.google.common.collect.Range;
import com.google.common.truth.delegation.Foo;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ExampleTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void stringContains() {
    assertThat("abc").contains("c");
  }

  @Test
  public void listHasElements() {
    // single item
    assertThat(Arrays.asList(1, 2, 3)).contains(1);

    // at least these items
    assertThat(Arrays.asList(1, 2, 3)).containsAllOf(1, 2);

    // at least one of these items
    assertThat(Arrays.asList(1, 2, 3)).containsAnyOf(1, 5);
  }

  @Test
  public void equalityFail() {
    int x = 2 + 2;
    expectFailure.whenTesting().that(x).isEqualTo(5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is equal to <5>");
  }

  @DataPoints public static int[] ints = {-1, 0, 1, 2};

  @SuppressWarnings("IdentityBinaryExpression")
  @Theory
  public void divideBySelf(int x) {
    assume().that(x).isNotEqualTo(0);
    assertThat(x / x).isEqualTo(1);
  }

  @Rule public final Expect EXPECT = Expect.create();

  @Test
  public void expectRange() {
    int x = 4;
    EXPECT.that(x).isNotNull();
    EXPECT.that(x).isIn(Range.open(3, 5));
    EXPECT.that(x).isEqualTo(4);
  }

  @Test
  public void customTypeCompares() {
    assertAbout(foo()).that(new Foo(5)).matches(new Foo(2 + 3));
  }
}
