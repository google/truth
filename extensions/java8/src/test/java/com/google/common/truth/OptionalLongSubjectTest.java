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
  public void namedOptionalLong() {
    OptionalLong optional = OptionalLong.of(1337L);
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(optional).named("name").hasValue(42L));
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that name (<OptionalLong[1337]>) has value <42>");
  }

  @Test
  public void failOnNullSubject() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isEmpty());
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <null> is empty");
  }

  @Test
  public void isPresent() {
    assertThat(OptionalLong.of(1337L)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalLong.empty()).isPresent());
      assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
  }

  @Test
  public void isPresentFailingWithNamed() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalLong.empty()).named("name").isPresent());
      assertThat(expected).hasMessageThat().isEqualTo("Not true that \"name\" is present");
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalLong.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalLong.of(1337L)).isEmpty());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that <OptionalLong[1337]> is empty");
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
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalLong.empty> has value <1337>");
  }

  @Test
  public void hasValue_FailingWithWrongValue() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(OptionalLong.of(1337L)).hasValue(42L));
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalLong[1337]> has value <42>");
  }

  @Test
  public void hasValueThat_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(
            whenTesting -> {
              LongSubject unused = whenTesting.that(OptionalLong.empty()).hasValueThat();
            });
    assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
  }

  @Test
  public void hasValueThat_FailingWithComparison() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(OptionalLong.of(1337L)).hasValueThat().isLessThan(42L));
    assertThat(expected).hasMessageThat().isEqualTo("Not true that <1337> is less than <42>");
  }

  @Test
  public void hasValueThat_SuccessWithComparison() {
    assertThat(OptionalLong.of(1337L)).hasValueThat().isGreaterThan(42L);
  }

  private static AssertionError expectFailure(
      ExpectFailure.DelegatedAssertionCallback<OptionalLongSubject, OptionalLong>
          assertionCallback) {
    return ExpectFailure.expectFailureAbout(optionalLongs(), assertionCallback);
  }
}
