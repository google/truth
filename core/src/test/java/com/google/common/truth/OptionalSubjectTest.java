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
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.Truth.assertThat;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Optional} Subject.
 */
@RunWith(JUnit4.class)
public class OptionalSubjectTest {

  @Test
  public void isPresent() {
    assertThat(Optional.of("foo")).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(Optional.empty()).isPresent());
    assertThat(e).factKeys().containsExactly("expected to be present");
  }

  @Test
  public void isPresentFailingNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Optional<?>) null).isPresent());
    assertThat(e).factKeys().containsExactly("expected present optional", "but was").inOrder();
  }

  @Test
  public void isEmpty() {
    assertThat(Optional.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(Optional.of("foo")).isEmpty());
    assertThat(e).factKeys().contains("expected to be empty");
    assertThat(e).factValue("but was present with value").isEqualTo("foo");
  }

  @Test
  public void isEmptyFailingNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Optional<?>) null).isEmpty());
    assertThat(e).factKeys().containsExactly("expected empty optional", "but was").inOrder();
  }

  @Test
  public void hasValue() {
    assertThat(Optional.of("foo")).hasValue("foo");
  }

  @Test
  public void hasValue_failingWithEmpty() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(Optional.empty()).hasValue("foo"));
    assertThat(e).factKeys().containsExactly("expected to have value", "but was empty").inOrder();
    assertThat(e).factValue("expected to have value").isEqualTo("foo");
  }

  @Test
  public void hasValue_failingWithNullParameter() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(com.google.common.base.Optional.of("foo")).hasValue(null));
    assertFailureKeys(e, "expected an optional with a null value, but that is impossible", "was");
  }

  @Test
  public void hasValue_failingWithWrongValue() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(Optional.of("foo")).hasValue("boo"));
    assertThat(e).factValue("value of").isEqualTo("optional.get()");
  }
}
