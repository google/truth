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
  public void namedOptional() {
    Optional<String> optional = Optional.of("actual");
    try {
      assertThat(optional).named("name").hasValue("expected");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage("Not true that name (<Optional.of(actual)>) has value <expected>");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void failOnNullSubject() {
    try {
      Optional<String> nullOptional = null;
      assertThat(nullOptional).isAbsent();
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <null> is absent");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void isPresent() {
    assertThat(Optional.of("foo")).isPresent();
  }

  @Test
  public void isPresentFailing() {
    try {
      assertThat(Optional.absent()).isPresent();
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that the subject is present");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void isPresentFailingWithNamed() {
    try {
      assertThat(Optional.absent()).named("name").isPresent();
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that \"name\" is present");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void isAbsent() {
    assertThat(Optional.absent()).isAbsent();
  }

  @Test
  public void isAbsentFailing() {
    try {
      assertThat(Optional.of("foo")).isAbsent();
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <Optional.of(foo)> is absent");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void hasValue() {
    assertThat(Optional.of("foo")).hasValue("foo");
  }

  @Test
  public void hasValue_FailingWithAbsent() {
    try {
      assertThat(Optional.absent()).hasValue("foo");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <Optional.absent()> has value <foo>");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void hasValue_FailingWithNullParameter() {
    try {
      assertThat(Optional.of("foo")).hasValue(null);
    } catch (NullPointerException expected) {
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void hasValue_FailingWithWrongValueForString() {
    try {
      assertThat(Optional.of("foo")).hasValue("boo");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <Optional.of(foo)> has value <boo>");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void hasValue_FailingWithWrongValueForOther() {
    try {
      assertThat(Optional.of(5)).hasValue(10);
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <Optional.of(5)> has value <10>");
      return;
    }
    fail("Should have thrown");
  }
}
