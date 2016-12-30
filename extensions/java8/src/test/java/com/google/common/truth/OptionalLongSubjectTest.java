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

import static com.google.common.truth.OptionalLongSubject.optionalLongs;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.fail;

import java.util.OptionalLong;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link OptionalLong} Subjects.
 *
 * @author Ben Douglass
 */
@RunWith(JUnit4.class)
public class OptionalLongSubjectTest {
  @Test
  public void namedOptionalLong() {
    OptionalLong optional = OptionalLong.of(1337L);
    try {
      validateThat(optional).named("name").hasValue(42L);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that name (<OptionalLong[1337]>) has value <42>");
      return;
    }
  }

  @Test
  public void failOnNullSubject() {
    try {
      OptionalLong nullOptional = null;
      validateThat(nullOptional).isEmpty();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <null> is empty");
      return;
    }
  }

  @Test
  public void isPresent() {
    assertThat(OptionalLong.of(1337L)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    try {
      validateThat(OptionalLong.empty()).isPresent();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
      return;
    }
  }

  @Test
  public void isPresentFailingWithNamed() {
    try {
      validateThat(OptionalLong.empty()).named("name").isPresent();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that \"name\" is present");
      return;
    }
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalLong.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    try {
      validateThat(OptionalLong.of(1337L)).isEmpty();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalLong[1337]> is empty");
      return;
    }
  }

  @Test
  public void hasValue() {
    assertThat(OptionalLong.of(1337L)).hasValue(1337L);
  }

  @Test
  public void hasValue_FailingWithEmpty() {
    try {
      validateThat(OptionalLong.empty()).hasValue(1337L);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalLong.empty> has value <1337>");
      return;
    }
  }

  @Test
  public void hasValue_FailingWithWrongValue() {
    try {
      validateThat(OptionalLong.of(1337L)).hasValue(42L);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <OptionalLong[1337]> has value <42>");
      return;
    }
  }

  @Test
  public void hasValueThat_FailingWithEmpty() {
    try {
      validateThat(OptionalLong.empty()).hasValueThat();
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that the subject is present");
    }
  }

  @Test
  public void hasValueThat_FailingWithEmptyRespectsFailureStrategy() {
    CountingFailureStrategy strategy = new CountingFailureStrategy();
    TestVerb verb = new TestVerb(strategy);

    verb.about(optionalLongs()).that(OptionalLong.empty()).hasValueThat().isGreaterThan(42L);
    assertThat(strategy.getFailureCount()).isEqualTo(1);

    verb.about(optionalLongs()).that(OptionalLong.empty()).hasValueThat().isAtMost(42L);
    assertThat(strategy.getFailureCount()).isEqualTo(2);

    verb.about(optionalLongs()).that(OptionalLong.of(42L)).hasValueThat().isLessThan(30L);
    assertThat(strategy.getFailureCount()).isEqualTo(3);
  }

  @Test
  public void hasValueThat_FailingWithComparison() {
    try {
      validateThat(OptionalLong.of(1337L)).hasValueThat().isLessThan(42L);
      fail("Should have thrown");
    } catch (ValidationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Not true that <1337> is less than <42>");
    }
  }

  @Test
  public void hasValueThat_SuccessWithComparison() {
    validateThat(OptionalLong.of(1337L)).hasValueThat().isGreaterThan(42L);
  }

  @Test
  public void assumption() {
    try {
      assume().about(optionalLongs()).that(OptionalLong.empty()).isPresent();
      fail("Should have thrown");
    } catch (AssumptionViolatedException expected) {
    }
  }

  private static OptionalLongSubject validateThat(OptionalLong that) {
    return validate().about(optionalLongs()).that(that);
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
