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
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.fail;

import java.util.Optional;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
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
    try {
      validateThat(optional).named("name").hasValue("expected");
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that name (<Optional[actual]>) has value <expected>");
      return;
    }
  }

  @Test
  public void failOnNullSubject() {
    try {
      Optional<String> nullOptional = null;
      validateThat(nullOptional).isEmpty();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <null> is empty");
      return;
    }
  }

  @Test
  public void isPresent() {
    assertThat(Optional.of("foo")).isPresent();
  }

  @Test
  public void isPresentFailing() {
    try {
      validateThat(Optional.empty()).isPresent();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
      return;
    }
  }

  @Test
  public void isPresentFailingWithNamed() {
    try {
      validateThat(Optional.empty()).named("name").isPresent();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that \"name\" is present");
      return;
    }
  }

  @Test
  public void isEmpty() {
    assertThat(Optional.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    try {
      validateThat(Optional.of("foo")).isEmpty();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <Optional[foo]> is empty");
      return;
    }
  }

  @Test
  public void hasValue() {
    assertThat(Optional.of("foo")).hasValue("foo");
  }

  @Test
  public void hasValue_FailingWithEmpty() {
    try {
      validateThat(Optional.empty()).hasValue("foo");
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <Optional.empty> has value <foo>");
      return;
    }
  }

  @Test
  public void hasValue_FailingWithNullParameter() {
    try {
      validateThat(Optional.of("foo")).hasValue(null);
      fail("Should have thrown");
    } catch (NullPointerException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Optional cannot have a null value.");
      return;
    }
  }

  @Test
  public void hasValue_FailingWithWrongValueForString() {
    try {
      validateThat(Optional.of("foo")).hasValue("boo");
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <Optional[foo]> has value <boo>");
      return;
    }
  }

  @Test
  public void hasValue_FailingWithWrongValueForOther() {
    try {
      validateThat(Optional.of(5)).hasValue(10);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <Optional[5]> has value <10>");
      return;
    }
  }

  @Test
  public void hasValue_Named_Failing() {
    try {
      assertThat(Optional.of("foo")).named("bar").hasValue("boo");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that bar (<Optional[foo]>) has value <boo>");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void hasValue_Named_FailingWithSameToStrings() {
    try {
      assertThat(Optional.of(10)).named("bar").hasValue("10");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that bar (<Optional[10]>) (class java.lang.Integer) "
                  + "has value <10> (class java.lang.String)");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void assumption() {
    try {
      assume().about(optionals()).that(Optional.empty()).isPresent();
      fail("Should have thrown");
    } catch (AssumptionViolatedException expected) {
    }
  }

  private static OptionalSubject validateThat(Optional<?> that) {
    return validate().about(optionals()).that(that);
  }

  private static TestVerb validate() {
    return new TestVerb(
        new FailureStrategy() {
          @Override
          public void fail(String message, Throwable cause) {
            throw new ValidationException(message, cause);
          }
        });
  }

  private static class ValidationException extends RuntimeException {
    private ValidationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
