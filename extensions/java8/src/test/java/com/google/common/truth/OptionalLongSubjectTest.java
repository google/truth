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
import static com.google.common.truth.OptionalLongSubject.optionalLongs;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import java.util.OptionalLong;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link OptionalLong} Subjects.
 *
 * @author Ben Douglass
 */
@RunWith(JUnit4.class)
public class OptionalLongSubjectTest {

  @Test
  public void failOnNullSubject() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isEmpty());
    assertThat(expected).factKeys().containsExactly("expected empty optional", "but was").inOrder();
  }

  @Test
  public void isPresent() {
    assertThat(OptionalLong.of(1337L)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalLong.empty()).isPresent());
    assertThat(expected).factKeys().containsExactly("expected to be present");
  }

  @Test
  public void isPresentFailingWithNamed() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalLong.empty()).named("name").isPresent());
    assertThat(expected).factKeys().contains("name");
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalLong.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalLong.of(1337L)).isEmpty());
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
    assertThat(OptionalLong.of(1337L)).hasValue(1337L);
  }

  @Test
  public void hasValue_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalLong.empty()).hasValue(1337L));
    assertThat(expected)
        .factKeys()
        .containsExactly("expected to have value", "but was absent")
        .inOrder();
    assertThat(expected).factValue("expected to have value").isEqualTo("1337");
  }

  @Test
  public void hasValue_FailingWithWrongValue() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalLong.of(1337L)).hasValue(42L));
    assertThat(expected).factValue("value of").isEqualTo("optionalLong.getAsLong()");
  }

  @Test
  public void hasValueThat_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(
            whenTesting -> {
              LongSubject unused = whenTesting.that(OptionalLong.empty()).hasValueThat();
            });
    assertThat(expected).factKeys().containsExactly("expected to be present");
  }

  @Test
  public void hasValueThat_FailingWithComparison() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalLong.of(1337L)).hasValueThat().isLessThan(42L));
    // TODO(cpovirk): Assert that "value of" is present once we set it:
    // assertThat(expected).fieldValue("value of").isEqualTo("optionalLong.getAsLong()");
  }

  @Test
  public void hasValueThat_SuccessWithComparison() {
    assertThat(OptionalLong.of(1337L)).hasValueThat().isGreaterThan(42L);
  }

  private static AssertionError expectFailure(
      ExpectFailure.SimpleSubjectBuilderCallback<OptionalLongSubject, OptionalLong>
          assertionCallback) {
    return ExpectFailure.expectFailureAbout(optionalLongs(), assertionCallback);
  }
}
