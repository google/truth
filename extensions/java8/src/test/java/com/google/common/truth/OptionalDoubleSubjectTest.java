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

import static com.google.common.truth.OptionalDoubleSubject.optionalDoubles;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.fail;

import java.util.OptionalDouble;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link OptionalDouble} Subjects.
 *
 * @author Ben Douglass
 */
@RunWith(JUnit4.class)
public class OptionalDoubleSubjectTest {
  @Test
  public void namedOptionalDouble() {
    OptionalDouble optional = OptionalDouble.of(1337.0);
    try {
      validateThat(optional).named("name").hasValue(42.0);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that name (<OptionalDouble[1337.0]>) has value <42.0>");
      return;
    }
  }

  @Test
  public void failOnNullSubject() {
    try {
      OptionalDouble nullOptional = null;
      validateThat(nullOptional).isEmpty();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <null> is empty");
      return;
    }
  }

  @Test
  public void isPresent() {
    assertThat(OptionalDouble.of(1337.0)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    try {
      validateThat(OptionalDouble.empty()).isPresent();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
      return;
    }
  }

  @Test
  public void isPresentFailingWithNamed() {
    try {
      validateThat(OptionalDouble.empty()).named("name").isPresent();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that \"name\" is present");
      return;
    }
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalDouble.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    try {
      validateThat(OptionalDouble.of(1337.0)).isEmpty();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalDouble[1337.0]> is empty");
      return;
    }
  }

  @Test
  public void hasValue() {
    assertThat(OptionalDouble.of(1337.0)).hasValue(1337.0);
  }

  @Test
  public void hasValue_FailingWithEmpty() {
    try {
      validateThat(OptionalDouble.empty()).hasValue(1337.0);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalDouble.empty> has value <1337.0>");
      return;
    }
  }

  @Test
  public void hasValue_FailingWithWrongValue() {
    try {
      validateThat(OptionalDouble.of(1337.0)).hasValue(42.0);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalDouble[1337.0]> has value <42.0>");
      return;
    }
  }

  @Test
  public void hasValueThat_FailingWithEmpty() {
    try {
      validateThat(OptionalDouble.empty()).hasValueThat();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
    }
  }

  @Test
  public void hasValueThat_FailingWithEmptyRespectsFailureStrategy() {
    CountingFailureStrategy strategy = new CountingFailureStrategy();
    TestVerb verb = new TestVerb(strategy);

    verb.about(optionalDoubles()).that(OptionalDouble.empty()).hasValueThat().isGreaterThan(42.0);
    assertThat(strategy.getFailureCount()).isEqualTo(1);

    verb.about(optionalDoubles()).that(OptionalDouble.empty()).hasValueThat().isAtMost(42.0);
    assertThat(strategy.getFailureCount()).isEqualTo(2);

    verb.about(optionalDoubles()).that(OptionalDouble.of(42.0)).hasValueThat().isLessThan(30.0);
    assertThat(strategy.getFailureCount()).isEqualTo(3);
  }

  @Test
  public void hasValueThat_FailingWithComparison() {
    try {
      validateThat(OptionalDouble.of(1337.0)).hasValueThat().isLessThan(42.0);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <1337.0> is less than <42.0>");
    }
  }

  @Test
  public void hasValueThat_SuccessWithComparison() {
    validateThat(OptionalDouble.of(1337.0)).hasValueThat().isGreaterThan(42.0);
  }

  @Test
  public void assumption() {
    try {
      assume().about(optionalDoubles()).that(OptionalDouble.empty()).isPresent();
      fail("Should have thrown");
    } catch (AssumptionViolatedException expected) {
    }
  }

  private static OptionalDoubleSubject validateThat(OptionalDouble that) {
    return validate().about(optionalDoubles()).that(that);
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
