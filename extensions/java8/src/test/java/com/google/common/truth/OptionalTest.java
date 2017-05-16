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

import static com.google.common.truth.OptionalSubject.optionals;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.Assert.fail;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link Optional} Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class OptionalTest {
  @Test
  public void namedOptional() {
    Optional<String> optional = Optional.of("actual");
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(optional).named("name").hasValue("expected"));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Not true that name (<Optional[actual]>) has value <expected>");
  }

  @Test
  public void failOnNullSubject() {
    AssertionError expected = expectFailure(whenTesting -> whenTesting.that(null).isEmpty());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that <null> is empty");
  }

  @Test
  public void isPresent() {
    assertThat(Optional.of("foo")).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.empty()).isPresent());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
  }

  @Test
  public void isPresentFailingWithNamed() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.empty()).named("name").isPresent());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that \"name\" is present");
  }

  @Test
  public void isEmpty() {
    assertThat(Optional.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.of("foo")).isEmpty());
    assertThat(expected).hasMessageThat().isEqualTo("Not true that <Optional[foo]> is empty");
  }

  @Test
  public void hasValue() {
    assertThat(Optional.of("foo")).hasValue("foo");
  }

  @Test
  public void hasValue_FailingWithEmpty() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.empty()).hasValue("foo"));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Not true that <Optional.empty> has value <foo>");
  }

  @Test
  public void hasValue_NPEWithNullParameter() {
    try {
      assertThat(Optional.of("foo")).hasValue(null);
      fail("Expected NPE");
    } catch (NullPointerException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Optional cannot have a null value.");
    }
  }

  @Test
  public void hasValue_FailingWithWrongValueForString() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.of("foo")).hasValue("boo"));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Not true that <Optional[foo]> has value <boo>");
  }

  @Test
  public void hasValue_FailingWithWrongValueForOther() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.of(5)).hasValue(10));
    assertThat(expected).hasMessageThat().isEqualTo("Not true that <Optional[5]> has value <10>");
  }

  @Test
  public void hasValue_Named_Failing() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(Optional.of("foo")).named("bar").hasValue("boo"));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Not true that bar (<Optional[foo]>) has value <boo>");
  }

  @Test
  public void hasValue_Named_FailingWithSameToStrings() {
    AssertionError expected =
        expectFailure(whenTesting -> whenTesting.that(Optional.of(10)).named("bar").hasValue("10"));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo(
            "Not true that bar (<Optional[10]>) (class java.lang.Integer) "
                + "has value <10> (class java.lang.String)");
  }

  private static AssertionError expectFailure(
      ExpectFailure.DelegatedAssertionCallback<OptionalSubject, Optional<?>> assertionCallback) {
    return ExpectFailure.expectFailureAbout(optionals(), assertionCallback);
  }
}
