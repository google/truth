/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.truth.OptionalDoubleSubject.optionalDoubles;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import java.util.OptionalDouble;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link OptionalDouble} Subjects.
 *
 * @author Ben Douglass
 */
@RunWith(JUnit4.class)
public class OptionalDoubleSubjectTest {
  @Test
  public void namedOptionalDouble() {
    OptionalDouble optional = OptionalDouble.of(1337.0);
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(optional).named("name").hasValue(42.0));
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that name (<OptionalDouble[1337.0]>) has value <42.0>");
  }

  @Test
  public void failOnNullSubject() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isEmpty());
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <null> is empty");
  }

  @Test
  public void isPresent() {
    assertThat(OptionalDouble.of(1337.0)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalDouble.empty()).isPresent());
      assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
  }

  @Test
  public void isPresentFailingWithNamed() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalDouble.empty()).named("name").isPresent());
      assertThat(expected).hasMessageThat().isEqualTo("Not true that \"name\" is present");
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalDouble.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalDouble.of(1337.0)).isEmpty());
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalDouble[1337.0]> is empty");
  }

  @Test
  public void hasValue() {
    assertThat(OptionalDouble.of(1337.0)).hasValue(1337.0);
  }

  @Test
  public void hasValue_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalDouble.empty()).hasValue(1337.0));
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalDouble.empty> has value <1337.0>");
  }

  @Test
  public void hasValue_FailingWithWrongValue() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalDouble.of(1337.0)).hasValue(42.0));
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalDouble[1337.0]> has value <42.0>");
  }

  @Test
  public void hasValueThat_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(
            whenTesting -> {
              DoubleSubject ignored = whenTesting.that(OptionalDouble.empty()).hasValueThat();
            });
      assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
  }

  @Test
  public void hasValueThat_FailingWithComparison() {
    AssertionError expected =
        expectFailure(
            whenTesting ->
                whenTesting.that(OptionalDouble.of(1337.0)).hasValueThat().isLessThan(42.0));
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <1337.0> is less than <42.0>");
  }

  @Test
  public void hasValueThat_SuccessWithComparison() {
    assertThat(OptionalDouble.of(1337.0)).hasValueThat().isGreaterThan(42.0);
  }

  private static AssertionError expectFailure(
      ExpectFailure.DelegatedAssertionCallback<OptionalDoubleSubject, OptionalDouble>
          assertionCallback) {
    return ExpectFailure.expectFailureAbout(optionalDoubles(), assertionCallback);
  }
}
