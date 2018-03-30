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
import static com.google.common.truth.OptionalSubject.optionals;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.Assert.fail;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link Optional} Subject.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class OptionalSubjectTest {

  @Test
  public void isPresent() {
    assertThat(Optional.of("foo")).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.empty()).isPresent());
    assertThat(expected).factKeys().containsExactly("expected to be present");
  }

  @Test
  public void isPresentFailing_named() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.empty()).named("name").isPresent());
    assertThat(expected).factKeys().contains("name");
  }

  @Test
  public void isPresentFailingNull() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isPresent());
    assertThat(expected)
        .factKeys()
        .containsExactly("expected present optional", "but was")
        .inOrder();
  }

  @Test
  public void isEmpty() {
    assertThat(Optional.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.of("foo")).isEmpty());
    assertThat(expected).factKeys().contains("expected to be empty");
    assertThat(expected).factValue("but was present with value").isEqualTo("foo");
  }

  @Test
  public void isEmptyFailingNull() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isEmpty());
    assertThat(expected).factKeys().containsExactly("expected empty optional", "but was").inOrder();
  }

  @Test
  public void hasValue() {
    assertThat(Optional.of("foo")).hasValue("foo");
  }

  @Test
  public void hasValue_failingWithEmpty() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.empty()).hasValue("foo"));
    assertThat(expected)
        .factKeys()
        .containsExactly("expected to have value", "but was absent")
        .inOrder();
    assertThat(expected).factValue("expected to have value").isEqualTo("foo");
  }

  @Test
  public void hasValue_npeWithNullParameter() {
    try {
      assertThat(Optional.of("foo")).hasValue(null);
      fail("Expected NPE");
    } catch (NullPointerException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Optional cannot have a null value.");
    }
  }

  @Test
  public void hasValue_failingWithWrongValue() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.of("foo")).hasValue("boo"));
    assertThat(expected).factValue("value of").isEqualTo("optional.get()");
  }

  @Test
  public void hasValue_failingWithWrongValue_named() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(Optional.of("foo")).named("bar").hasValue("boo"));
    assertThat(expected).factValue("value of").isEqualTo("bar.get()");
  }

  private static AssertionError expectFailure(
      ExpectFailure.SimpleSubjectBuilderCallback<OptionalSubject, Optional<?>> assertionCallback) {
    return ExpectFailure.expectFailureAbout(optionals(), assertionCallback);
  }
}
