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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.Statement;

@RunWith(JUnit4.class)
public class ExpectWithStackTest {
  private final Expect expectWithTrace = Expect.create();

  @Rule public final TestRuleVerifier verifyAssertionError = new TestRuleVerifier(expectWithTrace);

  @Test
  public void testExpectTrace_simpleCase() {
    verifyAssertionError.setErrorVerifier(
        new ErrorVerifier() {
          @Override
          public void verify(AssertionError expected) {
            assertThat(expected.getStackTrace()).hasLength(0);
            assertThat(expected).hasMessageThat().startsWith("3 expectations failed:");
          }
        });

    expectWithTrace.that(true).isFalse();
    expectWithTrace.that("Hello").isNull();
    expectWithTrace.that(1).isEqualTo(2);
  }

  @Test
  public void testExpectTrace_loop() {
    verifyAssertionError.setErrorVerifier(
        new ErrorVerifier() {
          @Override
          public void verify(AssertionError expected) {
            assertThat(expected.getStackTrace()).hasLength(0);
            assertThat(expected).hasMessageThat().startsWith("4 expectations failed:");
            assertWithMessage("test method name should only show up once with following omitted")
                .that(expected.getMessage().split("testExpectTrace_loop"))
                .hasLength(2);
          }
        });

    for (int i = 0; i < 4; i++) {
      expectWithTrace.that(true).isFalse();
    }
  }

  @Test
  public void testExpectTrace_callerException() {
    verifyAssertionError.setErrorVerifier(
        new ErrorVerifier() {
          @Override
          public void verify(AssertionError expected) {
            assertThat(expected.getStackTrace()).hasLength(0);
            assertThat(expected).hasMessageThat().startsWith("2 expectations failed:");
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
        new ErrorVerifier() {
          @Override
          public void verify(AssertionError expected) {
            assertWithMessage("Should throw exception as it is if only caller exception")
                .that(expected.getStackTrace().length)
                .isAtLeast(2);
          }
        });

    expectWithTrace
        .that(alwaysFailWithCause(getFirstException("First", getSecondException("Second", null))))
        .isEqualTo(5);
  }

  private static long alwaysFailWithCause(Throwable throwable) {
    throw new AssertionError("Always fail", throwable);
  }

  private static Exception getFirstException(String message, Throwable cause) {
    if (cause != null) {
      return new RuntimeException(message, cause);
    } else {
      return new RuntimeException(message);
    }
  }

  private static Exception getSecondException(String message, Throwable cause) {
    if (cause != null) {
      return new RuntimeException(message, cause);
    } else {
      return new RuntimeException(message);
    }
  }

  private static final class TestRuleVerifier implements TestRule {
    private final TestRule ruleToVerify;
    private ErrorVerifier errorVerifier = error -> {};

    TestRuleVerifier(TestRule ruleToVerify) {
      this.ruleToVerify = ruleToVerify;
    }

    void setErrorVerifier(ErrorVerifier verifier) {
      this.errorVerifier = verifier;
    }

    @Override
    public Statement apply(Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          try {
            ruleToVerify.apply(base, description).evaluate();
          } catch (AssertionError caught) {
            errorVerifier.verify(caught);
          }
        }
      };
    }
  }

  interface ErrorVerifier {
    void verify(AssertionError error);
  }
}
