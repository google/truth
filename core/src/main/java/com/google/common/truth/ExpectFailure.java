/*
 * Copyright (c) 2017 Google, Inc.
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Throwables;
import javax.annotation.Nullable;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * An assertion strategy that expects the given assertion to fail, and captures the failure rather
 * than throwing it. It should be used as a JUnit {@code @Rule}, so that unexpectedly-passing
 * assertions will cause your test to fail. In Java 8 you can use {@link #expectFailure} to instead
 * capture failures directly.
 *
 * <p>By design this class expects exactly one failure per instance, meaning you should create
 * separate tests for each failure case you intend to assert on, rather than trying to capture
 * multiple failures in a single test. With {@code expectFailure()} you can safely capture multiple
 * failures in the same test method, but creating separate tests is still encouraged.
 *
 * <p><b>Note:</b> this class is not intended for general use; it exists primarily to write tests of
 * {@link Subject} implementations and other Truth internals. If you are writing your own custom
 * subject you are encouraged to use this class rather than define a custom {@link FailureStrategy}
 * or directly catch {@link AssertionError}. Use {@code
 * expectFailure.whenTesting().about(subjectFactory()).that(...)} to make assertions about custom
 * subjects.
 */
public class ExpectFailure implements TestRule {
  private final FailureStrategy strategy =
      new AbstractFailureStrategy() {
        @Override
        public void fail(String message, Throwable cause) {
          try {
            Truth.THROW_ASSERTION_ERROR.fail(message, cause);
          } catch (AssertionError e) {
            captureFailure(e);
            return;
          }
          throw new AssertionError("fail() unexpectedly succeeded.");
        }

        @Override
        public void failComparing(
            String message, CharSequence expected, CharSequence actual, Throwable cause) {
          try {
            Truth.THROW_ASSERTION_ERROR.failComparing(message, expected, actual, cause);
          } catch (AssertionError e) {
            captureFailure(e);
            return;
          }
          throw new AssertionError("fail() unexpectedly succeeded.");
        }
      };

  private boolean inRuleContext = false;
  private boolean failureExpected = false;
  private @Nullable AssertionError failure = null;

  /**
   * Returns a test verb that expects the chained assertion to fail, and makes the failure available
   * via {@link #getFailure}.
   */
  public TestVerb whenTesting() {
    checkState(inRuleContext, "ExpectFailure must be used as a JUnit @Rule");
    if (failure != null) {
      AssertionError error = new AssertionError("ExpectFailure already captured a failure");
      error.initCause(failure);
      throw error;
    }
    if (failureExpected) {
      throw new AssertionError(
          "ExpectFailure.whenTesting() called previously, but did not capture a failure.");
    }
    failureExpected = true;
    return new TestVerb(strategy);
  }

  /** Returns the captured failure, if one occurred. */
  public AssertionError getFailure() {
    if (failure == null) {
      throw new AssertionError("ExpectFailure did not capture a failure.");
    }
    return failure;
  }

  /**
   * Captures the provided failure, or throws an {@link AssertionError} if a failure had previously
   * been captured.
   */
  private void captureFailure(AssertionError captured) {
    if (failure != null) {
      // TODO(diamondm) is it worthwhile to add the failures as suppressed exceptions?
      throw new AssertionError(
          String.format(
              "ExpectFailure.whenTesting() caught multiple failures:\n\n%s\n\n%s\n",
              Throwables.getStackTraceAsString(failure),
              Throwables.getStackTraceAsString(captured)));
    }
    failure = captured;
  }

  /**
   * Static alternative that directly returns the triggered failure. This is intended to be used in
   * Java 8 tests similar to {@code expectThrows()}:
   *
   * <p>{@code AssertionError failure = expectFailure(whenTesting ->
   * whenTesting.that(4).isNotEqualTo(4));}
   */
  public static AssertionError expectFailure(AssertionCallback assertionCallback) {
    ExpectFailure expectFailure = new ExpectFailure();
    expectFailure.inRuleContext = true; // safe since this instance doesn't leave this method
    assertionCallback.invokeAssertion(expectFailure.whenTesting());
    return expectFailure.getFailure();
  }

  /**
   * Static alternative that directly returns the triggered failure. This is intended to be used in
   * Java 8 tests similar to {@code expectThrows()}:
   *
   * <p>{@code AssertionError failure = expectFailureAbout(myTypes(), whenTesting ->
   * whenTesting.that(myType).hasProperty());}
   */
  public static <S extends Subject<S, D>, D> AssertionError expectFailureAbout(
      final SubjectFactory<S, D> factory,
      final DelegatedAssertionCallback<S, D> assertionCallback) {
    // whenTesting -> assertionCallback.invokeAssertion(whenTesting.about(factory))
    return expectFailure(
        new AssertionCallback() {
          @Override
          public void invokeAssertion(TestVerb whenTesting) {
            assertionCallback.invokeAssertion(whenTesting.about(factory));
          }
        });
  }

  @Override
  public Statement apply(final Statement base, Description description) {
    checkNotNull(base);
    checkNotNull(description);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        inRuleContext = true;
        base.evaluate();
        inRuleContext = false;
        if (failureExpected && failure == null) {
          throw new AssertionError(
              "ExpectFailure.whenTesting() invoked, but no failure was caught.");
        }
      }
    };
  }

  /**
   * A "functional interface" for {@link #expectFailure} to invoke and capture failures.
   *
   * <p>Java 8 users should pass a lambda to {@code .expectFailure()} rather than directly implement
   * this interface. Java 7 users can define an {@code @Rule ExpectFailure} instance instead,
   * however if you prefer the {@code .expectFailure()} pattern you can use this interface to pass
   * in an anonymous class.
   */
  public interface AssertionCallback {
    void invokeAssertion(TestVerb expect);
  }

  /**
   * A "functional interface" for {@link #expectFailureAbout} to invoke and capture failures.
   *
   * <p>Java 8 users should pass a lambda to {@code .expectFailureAbout()} rather than directly
   * implement this interface. Java 7 users can define an {@code @Rule ExpectFailure} instance
   * instead, however if you prefer the {@code .expectFailureAbout()} pattern you can use this
   * interface to pass in an anonymous class.
   */
  public interface DelegatedAssertionCallback<S extends Subject<S, D>, D> {
    void invokeAssertion(AbstractVerb.DelegatedVerb<S, D> expect);
  }
}
