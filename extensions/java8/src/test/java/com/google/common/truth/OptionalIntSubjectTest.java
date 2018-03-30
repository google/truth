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

import static com.google.common.truth.ExpectFailure.assertThat;
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
  public void failOnNullSubject() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isEmpty());
    assertThat(expected).factKeys().containsExactly("expected empty optional", "but was").inOrder();
  }

  @Test
  public void isPresent() {
    assertThat(OptionalInt.of(1337)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.empty()).isPresent());
    assertThat(expected).factKeys().containsExactly("expected to be present");
  }

  @Test
  public void isPresentFailingWithNamed() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalInt.empty()).named("name").isPresent());
    assertThat(expected).factKeys().contains("name");
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalInt.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.of(1337)).isEmpty());
    assertThat(expected).factKeys().contains("expected to be empty");
    assertThat(expected).factValue("but was present with value").isEqualTo("1337");
  }

  @Test
  public void isEmptyFailingNull() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isEmpty());
    assertThat(expected).factKeys().containsExactly("expected empty optional", "but was").inOrder();
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
        .factKeys()
        .containsExactly("expected to have value", "but was absent")
        .inOrder();
    assertThat(expected).factValue("expected to have value").isEqualTo("1337");
  }

  @Test
  public void hasValue_FailingWithWrongValue() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.of(1337)).hasValue(42));
    assertThat(expected).factValue("value of").isEqualTo("optionalInt.getAsInt()");
  }

  @Test
  public void hasValueThat_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(
            whenTesting -> {
              IntegerSubject unused = whenTesting.that(OptionalInt.empty()).hasValueThat();
            });
    assertThat(expected).factKeys().containsExactly("expected to be present");
  }

  @Test
  public void hasValueThat_FailingWithComparison() {
    AssertionError unused =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalInt.of(1337)).hasValueThat().isLessThan(42));
    // TODO(cpovirk): Assert that "value of" is present once we set it:
    // assertThat(expected).fieldValue("value of").isEqualTo("optionalInt.getAsInt()");
  }

  @Test
  public void hasValueThat_SuccessWithComparison() {
    assertThat(OptionalInt.of(1337)).hasValueThat().isGreaterThan(42);
  }

  private static AssertionError expectFailure(
      ExpectFailure.SimpleSubjectBuilderCallback<OptionalIntSubject, OptionalInt>
          assertionCallback) {
    return ExpectFailure.expectFailureAbout(optionalInts(), assertionCallback);
  }
}
