/*
 * Copyright (c) 2019 Google, Inc.
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

import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static org.junit.Assert.assertThrows;
import static org.junit.runner.Description.createTestDescription;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.Statement;

/** Tests for {@link ActualValueInference}. */
@GwtIncompatible // Inference doesn't work under GWT.
@RunWith(JUnit4.class)
/*
 * We declare a single `failure` variable in each method, and many methods assign to it multiple
 * times. We declare it without initializing it so that every assignment to it can look the same as
 * every other (rather than having an initial combined initialization+assignment that looks slightly
 * different.
 */
@SuppressWarnings("InitializeInline")
public final class ActualValueInferenceTest {
  @Test
  public void simple() {
    AssertionError failure;

    failure = expectFailure(whenTesting -> whenTesting.that(staticNoArg()).isEqualTo("b"));
    assertThat(failure).factValue("value of").isEqualTo("staticNoArg()");

    failure = expectFailure(whenTesting -> whenTesting.that(instanceNoArg()).isEqualTo("b"));
    assertThat(failure).factValue("value of").isEqualTo("instanceNoArg()");

    failure = expectFailure(whenTesting -> whenTesting.that(staticOneArg(0)).isEqualTo("b"));
    assertThat(failure).factValue("value of").isEqualTo("staticOneArg(...)");

    failure = expectFailure(whenTesting -> whenTesting.that(instanceOneArg(0)).isEqualTo("b"));
    assertThat(failure).factValue("value of").isEqualTo("instanceOneArg(...)");

    failure =
        expectFailure(
            whenTesting ->
                whenTesting.that(new ActualValueInferenceTest().instanceOneArg(0)).isEqualTo("b"));
    assertThat(failure).factValue("value of").isEqualTo("instanceOneArg(...)");
  }

  @Test
  public void autoBox() {
    AssertionError failure;

    failure = expectFailure(whenTesting -> whenTesting.that(someByte()).isEqualTo(1));
    assertThat(failure).factValue("value of").isEqualTo("someByte()");

    failure = expectFailure(whenTesting -> whenTesting.that(someShort()).isEqualTo(1));
    assertThat(failure).factValue("value of").isEqualTo("someShort()");

    failure = expectFailure(whenTesting -> whenTesting.that(someInt()).isEqualTo(1));
    assertThat(failure).factValue("value of").isEqualTo("someInt()");

    failure = expectFailure(whenTesting -> whenTesting.that(someLong()).isEqualTo(1));
    assertThat(failure).factValue("value of").isEqualTo("someLong()");

    failure = expectFailure(whenTesting -> whenTesting.that(someFloat()).isEqualTo(1));
    assertThat(failure).factValue("value of").isEqualTo("someFloat()");

    failure = expectFailure(whenTesting -> whenTesting.that(someDouble()).isEqualTo(1));
    assertThat(failure).factValue("value of").isEqualTo("someDouble()");

    failure = expectFailure(whenTesting -> whenTesting.that(someBoolean()).isEqualTo(true));
    assertThat(failure).factValue("value of").isEqualTo("someBoolean()");

    failure = expectFailure(whenTesting -> whenTesting.that(someChar()).isEqualTo(1));
    assertThat(failure).factValue("value of").isEqualTo("someChar()");
  }

  @Test
  public void otherValueOfOverloads() {
    AssertionError failure;

    failure =
        expectFailure(
            whenTesting -> whenTesting.that(Integer.valueOf(someNumberString())).isEqualTo(1));
    assertThat(failure).factKeys().doesNotContain("value of");

    failure =
        expectFailure(
            whenTesting -> whenTesting.that(Integer.valueOf(someNumberString(), 16)).isEqualTo(1));
    assertThat(failure).factKeys().doesNotContain("value of");
  }

  @Test
  public void variable() {
    AssertionError failure;

    failure =
        expectFailure(
            whenTesting -> {
              String s = staticNoArg();
              whenTesting.that(s).isEqualTo("b");
            });
    assertThat(failure).factValue("value of").isEqualTo("staticNoArg()");
  }

  @Test
  public void chaining() {
    AssertionError failure;

    failure =
        expectFailure(
            whenTesting -> whenTesting.that(makeException()).hasMessageThat().isEqualTo("b"));
    assertThat(failure).factValue("value of").isEqualTo("makeException().getMessage()");
  }

  @Test
  public void multipleOnOneLine() {
    AssertionError failure;

    failure =
        expectFailure(whenTesting -> whenTesting.that(oneTwoThree()).containsExactly(1).inOrder());
    assertThat(failure).factValue("value of").isEqualTo("oneTwoThree()");

    failure =
        expectFailure(
            whenTesting -> whenTesting.that(oneTwoThree()).containsExactly(1, 3, 2).inOrder());
    assertThat(failure).factValue("value of").isEqualTo("oneTwoThree()");
  }

  @Test
  public void boringNames() {
    AssertionError failure;

    failure =
        expectFailure(whenTesting -> whenTesting.that(ImmutableList.of(1, 2)).containsExactly(1));
    assertThat(failure).factKeys().doesNotContain("value of");
  }

  @Test
  public void loop() {
    AssertionError failure;

    failure =
        expectFailure(
            whenTesting -> {
              for (int i = 0; i < 1; i++) {
                whenTesting.that(staticNoArg()).isEqualTo("b");
              }
            });
    /*
     * It would be nice for inference to work on this simple loop, but loops can be much more
     * complex, so for now, we're conservative.
     */
    assertThat(failure).factKeys().doesNotContain("value of");
  }

  @Test
  public void tryCatch() {
    AssertionError failure;

    failure =
        expectFailure(
            whenTesting -> {
              String s;
              try {
                s = staticNoArg();
              } catch (RuntimeException e) {
                s = instanceNoArg();
              }
              whenTesting.that(s).isEqualTo("b");
            });
    assertThat(failure).factKeys().doesNotContain("value of");
  }

  @Test
  public void expect() {
    Expect expect = Expect.create();
    Statement testMethod =
        new Statement() {
          @Override
          public void evaluate() {
            expect.that(staticNoArg()).isEqualTo("b");
          }
        };
    Statement wrapped = expect.apply(testMethod, createTestDescription("MyTest", "myMethod"));
    AssertionError failure = assertThrows(AssertionError.class, wrapped::evaluate);
    /*
     * We can't use factValue here because Expect throws a plain wrapper AssertionError, not the
     * original ErrorWithFacts. We could in theory change that someday, perhaps as part of a
     * followup to https://github.com/google/truth/issues/543, but it seems unlikely.
     */
    assertThat(failure).hasMessageThat().contains("staticNoArg()");
  }

  static String staticNoArg() {
    return "a";
  }

  String instanceNoArg() {
    return "a";
  }

  static String staticOneArg(Object o) {
    return "a";
  }

  String instanceOneArg(Object o) {
    return "a";
  }

  ImmutableList<Integer> oneTwoThree() {
    return ImmutableList.of(1, 2, 3);
  }

  Exception makeException() {
    return new Exception("a");
  }

  byte someByte() {
    return 0;
  }

  short someShort() {
    return 0;
  }

  int someInt() {
    return 0;
  }

  long someLong() {
    return 0;
  }

  float someFloat() {
    return 0;
  }

  double someDouble() {
    return 0;
  }

  boolean someBoolean() {
    return false;
  }

  char someChar() {
    return 0;
  }

  String someNumberString() {
    return "0";
  }
}
