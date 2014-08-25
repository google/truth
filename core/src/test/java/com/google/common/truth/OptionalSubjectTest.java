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

import static com.google.common.truth.Truth.ASSERT;
import static org.junit.Assert.fail;

import com.google.common.base.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Optional} Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class OptionalSubjectTest {

  @Test public void failOnNullSubject() {
    try {
      Optional<String> nullOptional = null;
      ASSERT.that(nullOptional).isAbsent();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .isEqualTo("Not true that \"Optional<T>\" is a non-null reference");
    }
  }

  @Test public void isPresent() {
    ASSERT.that(Optional.of("foo")).isPresent();
  }

  @Test public void isPresentFailing() {
    try {
      ASSERT.that(Optional.absent()).isPresent();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).isEqualTo("Not true that the subject is present");
    }
  }

  @Test public void isAbsent() {
    ASSERT.that(Optional.absent()).isAbsent();
  }

  @Test public void isAbsentFailing() {
    try {
      ASSERT.that(Optional.of("foo")).isAbsent();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).isEqualTo("Not true that the subject is absent");
    }
  }

  @Test public void hasValue() {
    ASSERT.that(Optional.of("foo")).hasValue("foo");
  }

  @Test public void hasValue_FailingWithAbsent() {
    try {
      ASSERT.that(Optional.absent()).hasValue("foo");
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .isEqualTo("Not true that <Optional.absent()> has value <foo>");
    }
  }

  @Test public void hasValue_FailingWithNullParameter() {
    try {
      ASSERT.that(Optional.of("foo")).hasValue(null);
      fail("Should have thrown");
    } catch (NullPointerException expected) {}
  }

  @Test public void hasValue_FailingWithWrongValueForString() {
    try {
      ASSERT.that(Optional.of("foo")).hasValue("boo");
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .isEqualTo("Optional<String> has an incorrect value. expected:<[b]oo> but was:<[f]oo>");
    }
  }

  @Test public void hasValue_FailingWithWrongValueForOther() {
    try {
      ASSERT.that(Optional.of(5)).hasValue(10);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .isEqualTo("Not true that <Optional.of(5)> has value <10>");
    }
  }
}
