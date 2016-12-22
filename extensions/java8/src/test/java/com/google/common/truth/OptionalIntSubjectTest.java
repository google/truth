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

import static com.google.common.truth.OptionalIntSubject.optionalInts;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.fail;

import java.util.OptionalInt;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link OptionalInt} Subjects.
 *
 * @author Ben Douglass
 */
@RunWith(JUnit4.class)
public class OptionalIntSubjectTest {
  @Test
  public void namedOptionalInt() {
    OptionalInt optional = OptionalInt.of(1337);
    try {
      validateThat(optional).named("name").hasValue(42);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessage("Not true that name (<OptionalInt[1337]>) has value <42>");
      return;
    }
  }

  @Test
  public void failOnNullSubject() {
    try {
      OptionalInt nullOptional = null;
      validateThat(nullOptional).isEmpty();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessage("Not true that <null> is empty");
      return;
    }
  }

  @Test
  public void isPresent() {
    assertThat(OptionalInt.of(1337)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    try {
      validateThat(OptionalInt.empty()).isPresent();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessage("Not true that the subject is present");
      return;
    }
  }

  @Test
  public void isPresentFailingWithNamed() {
    try {
      validateThat(OptionalInt.empty()).named("name").isPresent();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessage("Not true that \"name\" is present");
      return;
    }
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalInt.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    try {
      validateThat(OptionalInt.of(1337)).isEmpty();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessage("Not true that <OptionalInt[1337]> is empty");
      return;
    }
  }

  @Test
  public void hasValue() {
    assertThat(OptionalInt.of(1337)).hasValue(1337);
  }

  @Test
  public void hasValue_FailingWithEmpty() {
    try {
      validateThat(OptionalInt.empty()).hasValue(1337);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessage("Not true that <OptionalInt.empty> has value <1337>");
      return;
    }
  }

  @Test
  public void hasValue_FailingWithWrongValue() {
    try {
      validateThat(OptionalInt.of(1337)).hasValue(42);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessage("Not true that <OptionalInt[1337]> has value <42>");
      return;
    }
  }

  @Test
  public void hasValueThat_FailingWithEmpty() {
    try {
      validateThat(OptionalInt.empty()).hasValueThat();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessage("Not true that the subject is present");
    }
  }

  @Test
  public void hasValueThat_FailingWithEmptyRespectsFailureStrategy() {
    CountingFailureStrategy strategy = new CountingFailureStrategy();
    TestVerb verb = new TestVerb(strategy);

    verb.about(optionalInts()).that(OptionalInt.empty()).hasValueThat().isGreaterThan(42);
    assertThat(strategy.getFailureCount()).isEqualTo(1);

    verb.about(optionalInts()).that(OptionalInt.empty()).hasValueThat().isAtMost(42);
    assertThat(strategy.getFailureCount()).isEqualTo(2);

    verb.about(optionalInts()).that(OptionalInt.of(42)).hasValueThat().isLessThan(30);
    assertThat(strategy.getFailureCount()).isEqualTo(3);
  }

  @Test
  public void hasValueThat_FailingWithComparison() {
    try {
      validateThat(OptionalInt.of(1337)).hasValueThat().isLessThan(42);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessage("Not true that <1337> is less than <42>");
    }
  }

  @Test
  public void hasValueThat_SuccessWithComparison() {
    validateThat(OptionalInt.of(1337)).hasValueThat().isGreaterThan(42);
  }

  @Test
  public void assumption() {
    try {
      assume().about(optionalInts()).that(OptionalInt.empty()).isPresent();
      fail("Should have thrown");
    } catch (AssumptionViolatedException expected) {
    }
  }

  private static OptionalIntSubject validateThat(OptionalInt that) {
    return validate().about(optionalInts()).that(that);
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

  /** A failure strategy that increments a counter rather than throwing. */
  private static class CountingFailureStrategy extends FailureStrategy {
    private int failureCount = 0;

    @Override
    public void fail(String message, Throwable cause) {
      failureCount++;
    }

    public int getFailureCount() {
      return failureCount;
    }
  }
}
