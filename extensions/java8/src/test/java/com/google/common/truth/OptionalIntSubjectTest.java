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

import static com.google.common.truth.OptionalIntSubject.optionalInts;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import java.util.OptionalInt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link OptionalInt} Subjects.
 *
 * @author Ben Douglass
 */
@RunWith(JUnit4.class)
public class OptionalIntSubjectTest {
  @Test
  public void namedOptionalInt() {
    OptionalInt optional = OptionalInt.of(1337);
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(optional).named("name").hasValue(42));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Not true that name (<OptionalInt[1337]>) has value <42>");
  }

  @Test
  public void failOnNullSubject() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isEmpty());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that <null> is empty");
  }

  @Test
  public void isPresent() {
    assertThat(OptionalInt.of(1337)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.empty()).isPresent());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
  }

  @Test
  public void isPresentFailingWithNamed() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalInt.empty()).named("name").isPresent());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that \"name\" is present");
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalInt.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.of(1337)).isEmpty());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that <OptionalInt[1337]> is empty");
  }

  @Test
  public void hasValue() {
    assertThat(OptionalInt.of(1337)).hasValue(1337);
  }

  @Test
  public void hasValue_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.empty()).hasValue(1337));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Not true that <OptionalInt.empty> has value <1337>");
  }

  @Test
  public void hasValue_FailingWithWrongValue() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.of(1337)).hasValue(42));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Not true that <OptionalInt[1337]> has value <42>");
  }

  @Test
  public void hasValueThat_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(
            whenTesting -> {
              IntegerSubject unused = whenTesting.that(OptionalInt.empty()).hasValueThat();
            });
    assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
  }

  @Test
  public void hasValueThat_FailingWithComparison() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalInt.of(1337)).hasValueThat().isLessThan(42));
    assertThat(expected).hasMessageThat().isEqualTo("Not true that <1337> is less than <42>");
  }

  @Test
  public void hasValueThat_SuccessWithComparison() {
    assertThat(OptionalInt.of(1337)).hasValueThat().isGreaterThan(42);
  }

  private static AssertionError expectFailure(
      ExpectFailure.DelegatedAssertionCallback<OptionalIntSubject, OptionalInt> assertionCallback) {
    return ExpectFailure.expectFailureAbout(optionalInts(), assertionCallback);
  }
}
