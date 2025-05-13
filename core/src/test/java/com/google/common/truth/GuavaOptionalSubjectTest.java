/*
 * Copyright (c) 2014 Google, Inc.
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

import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Guava {@link Optional} Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class GuavaOptionalSubjectTest {

  @Test
  public void isPresent() {
    assertThat(Optional.of("foo")).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(Optional.absent()).isPresent());
    assertFailureKeys(e, "expected to be present");
  }

  @Test
  public void isPresentFailingNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Optional<?>) null).isPresent());
    assertFailureKeys(e, "expected present optional", "but was");
  }

  @Test
  public void isAbsent() {
    assertThat(Optional.absent()).isAbsent();
  }

  @Test
  public void isAbsentFailing() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(Optional.of("foo")).isAbsent());
    assertFailureKeys(e, "expected to be absent", "but was present with value");
    assertFailureValue(e, "but was present with value", "foo");
  }

  @Test
  public void isAbsentFailingNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Optional<?>) null).isAbsent());
    assertFailureKeys(e, "expected absent optional", "but was");
  }

  @Test
  public void hasValue() {
    assertThat(Optional.of("foo")).hasValue("foo");
  }

  @Test
  public void hasValue_failingWithAbsent() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(Optional.absent()).hasValue("foo"));
    assertFailureKeys(e, "expected to have value", "but was absent");
    assertFailureValue(e, "expected to have value", "foo");
  }

  @Test
  public void hasValue_npeWithNullParameter() {
    NullPointerException expected =
        assertThrows(
            NullPointerException.class, () -> assertThat(Optional.of("foo")).hasValue(null));
    assertThat(expected).hasMessageThat().contains("Optional");
  }

  @Test
  public void hasValue_failingWithWrongValue() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(Optional.of("foo")).hasValue("boo"));
    assertFailureValue(e, "value of", "optional.get()");
  }
}
