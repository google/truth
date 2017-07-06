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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.base.Optional;
import org.junit.Rule;
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
  @Rule public ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void namedOptional() {
    Optional<String> optional = Optional.of("actual");

    expectFailure.whenTesting().that(optional).named("name").hasValue("expected");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that name (<Optional.of(actual)>) has value <expected>");
  }

  @Test
  public void failOnNullSubject() {
      Optional<String> nullOptional = null;
    expectFailure.whenTesting().that(nullOptional).isAbsent();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is absent");
  }

  @Test
  public void isPresent() {
    assertThat(Optional.of("foo")).isPresent();
  }

  @Test
  public void isPresentFailing() {
    expectFailure.whenTesting().that(Optional.absent()).isPresent();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that the subject is present");
  }

  @Test
  public void isPresentFailingWithNamed() {
    expectFailure.whenTesting().that(Optional.absent()).named("name").isPresent();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that \"name\" is present");
  }

  @Test
  public void isAbsent() {
    assertThat(Optional.absent()).isAbsent();
  }

  @Test
  public void isAbsentFailing() {
    expectFailure.whenTesting().that(Optional.of("foo")).isAbsent();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <Optional.of(foo)> is absent");
  }

  @Test
  public void hasValue() {
    assertThat(Optional.of("foo")).hasValue("foo");
  }

  @Test
  public void hasValue_FailingWithAbsent() {
    expectFailure.whenTesting().that(Optional.absent()).hasValue("foo");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <Optional.absent()> has value <foo>");
  }

  @Test
  public void hasValue_ErrorWithNullParameter() {
    try {
      assertThat(Optional.of("foo")).hasValue(null);
    } catch (NullPointerException expected) {
      assertThat(expected).hasMessageThat().contains("Optional");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void hasValue_FailingWithWrongValueForString() {
    expectFailure.whenTesting().that(Optional.of("foo")).hasValue("boo");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <Optional.of(foo)> has value <boo>");
  }

  @Test
  public void hasValue_FailingWithWrongValueForOther() {
    expectFailure.whenTesting().that(Optional.of(5)).hasValue(10);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <Optional.of(5)> has value <10>");
  }

  @Test
  public void hasValue_FailingWithSameToStrings() {
    expectFailure.whenTesting().that(Optional.of(10)).hasValue("10");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <Optional.of(10)> (class java.lang.Integer) "
                + "has value <10> (class java.lang.String)");
  }

  @Test
  public void hasValue_Named_Failing() {
    expectFailure.whenTesting().that(Optional.of("foo")).named("bar").hasValue("boo");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that bar (<Optional.of(foo)>) has value <boo>");
  }

  @Test
  public void hasValue_Named_FailingWithSameToStrings() {
    expectFailure.whenTesting().that(Optional.of(10)).named("bar").hasValue("10");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that bar (<Optional.of(10)>) (class java.lang.Integer) "
                + "has value <10> (class java.lang.String)");
  }
}
