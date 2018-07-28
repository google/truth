/*
 * Copyright (c) 2018 Google, Inc.
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
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.Statement;

@RunWith(JUnit4.class)
public class ExpectWithStackTest {
  private final Expect expectWithTrace = Expect.createAndEnableStackTrace();

  @Rule public final TestRuleVerifier verifyAssertionError = new TestRuleVerifier(expectWithTrace);

  @Test
  public void testExpectTrace_simpleCase() {
    verifyAssertionError.setErrorVerifier(
        new Predicate<AssertionError>() {
          @Override
          public boolean apply(AssertionError expected) {
            assertThat(expected.getStackTrace()).hasLength(0);
            assertThat(expected).hasMessageThat().startsWith("3 expectations failed:");
            return true;
          }
        });

    expectWithTrace.that(true).isFalse();
    expectWithTrace.that("Hello").isNull();
    expectWithTrace.that(1).isEqualTo(2);
  }

  @Test
  public void testExpectTrace_loop() {
    verifyAssertionError.setErrorVerifier(
        new Predicate<AssertionError>() {
          @Override
          public boolean apply(AssertionError expected) {
            assertThat(expected.getStackTrace()).hasLength(0);
            assertThat(expected).hasMessageThat().startsWith("4 expectations failed:");
            assertWithMessage("test method name should only show up once with following omitted")
                .that(expected.getMessage().split("testExpectTrace_loop"))
                .hasLength(2);
            return true;
          }
        });

    for (int i = 0; i < 4; i++) {
      expectWithTrace.that(true).isFalse();
    }
  }

  @Test
  public void testExpectTrace_callerException() {
    verifyAssertionError.setErrorVerifier(
        new Predicate<AssertionError>() {
          @Override
          public boolean apply(AssertionError expected) {
            assertThat(expected.getStackTrace()).hasLength(0);
            assertThat(expected).hasMessageThat().startsWith("2 expectations failed:");
            return true;
          }
        });

    expectWithTrace.that(true).isFalse();
    expectWithTrace
        .that(alwaysFailWithCause(getFirstException("First", getSecondException("Second", null))))
        .isEqualTo(5);
  }

  @Test
  public void testExpectTrace_onlyCallerException() {
    verifyAssertionError.setErrorVerifier(
        new Predicate<AssertionError>() {
          @Override
          public boolean apply(AssertionError expected) {
            assertWithMessage("Should throw exception as it is if only caller exception")
                .that(expected.getStackTrace().length)
                .isAtLeast(2);
            return true;
          }
        });

    expectWithTrace
        .that(alwaysFailWithCause(getFirstException("First", getSecondException("Second", null))))
        .isEqualTo(5);
  }

  private static long alwaysFailWithCause(Throwable throwable) {
    throw new AssertionError("Always fail", throwable);
  }

  private static Exception getFirstException(String messsage, Throwable cause) {
    if (cause != null) {
      return new RuntimeException(messsage, cause);
    } else {
      return new RuntimeException(messsage);
    }
  }

  private static Exception getSecondException(String messsage, Throwable cause) {
    if (cause != null) {
      return new RuntimeException(messsage, cause);
    } else {
      return new RuntimeException(messsage);
    }
  }

  private static class TestRuleVerifier implements TestRule {
    protected Predicate<AssertionError> errorVerifier = Predicates.alwaysFalse();

    private final TestRule ruleToVerify;

    public TestRuleVerifier(TestRule ruleToVerify) {
      this.ruleToVerify = ruleToVerify;
    }

    public void setErrorVerifier(Predicate<AssertionError> verifier) {
      this.errorVerifier = verifier;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          try {
            ruleToVerify.apply(base, description).evaluate();
          } catch (AssertionError caught) {
            if (!errorVerifier.apply(caught)) {
              throw new AssertionError("Caught error doesn't meet expectation", caught);
            }
          }
        }
      };
    }
  }
}
