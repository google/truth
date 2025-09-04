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
import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.truth.Platform.expectFailureWarningIfWeb;
import static com.google.common.truth.Platform.getStackTraceAsString;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.TruthFailureSubject.truthFailures;

import com.google.common.annotations.GwtIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jspecify.annotations.Nullable;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A utility for testing that assertions against a custom {@link Subject} fail when they should,
 * plus a utility to assert about parts of the resulting failure messages.
 *
 * <p>Usage:
 *
 * <pre>{@code
 *   AssertionError e =
 *       expectFailure(whenTesting -> whenTesting.that(cancelButton).isVisible());
 *   assertThat(e).factKeys().containsExactly("expected to be visible");
 *
 * ...
 *
 * private static AssertionError expectFailure(
 *     SimpleSubjectBuilderCallback<UiElementSubject, UiElement> assertionCallback) {
 *   return expectFailureAbout(uiElements(), assertionCallback);
 * }
 * }</pre>
 *
 * {@link ExpectFailure} also supports a legacy approach, which we no longer recommend now that all
 * Truth users can use lambdas. That approach is based on the JUnit {@code @Rule} system:
 *
 * <pre>
 * {@code @Rule public final ExpectFailure expectFailure = new ExpectFailure();}
 *
 * {@code ...
 *
 *     expectFailure.whenTesting().about(uiElements()).that(cancelButton).isVisible();
 *     assertThat(expectFailure.getFailure()).factKeys().containsExactly("expected to be visible");
 * }</pre>
 *
 * <p>{@code ExpectFailure} is similar to JUnit's {@code assertThrows} (<a
 * href="https://junit.org/junit4/javadoc/latest/org/junit/Assert.html#assertThrows%28java.lang.Class,%20org.junit.function.ThrowingRunnable%29">JUnit
 * 4</a>, <a
 * href="https://docs.junit.org/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html#assertThrows(java.lang.Class,org.junit.jupiter.api.function.Executable)">JUnit
 * 5</a>). We recommend it over {@code assertThrows} when you're testing a Truth subject because:
 *
 * <ul>
 *   <li>It performs additional checks:
 *       <ul>
 *         <li>It checks that the assertion you're testing uses the supplied {@link
 *             FailureStrategy}.
 *         <li>It checks that the assertion you're testing calls {@link FailureStrategy#fail} only
 *             once.
 *       </ul>
 *   <li>It instructs Truth to generate failure messages <i>without</i> adding lines like "value of:
 *       foo()" for {@code assertThat(foo())....} calls that it detects in the test bytecode. Truth
 *       doesn't provide guarantees for when such lines will be generated, so tests become more
 *       resilient without them.
 * </ul>
 */
public final class ExpectFailure implements Platform.JUnitTestRule {
  private boolean inRuleContext = false;
  private boolean failureExpected = false;
  private @Nullable AssertionError failure = null;

  /**
   * Creates a new instance for use as a {@code @Rule}. See the class documentation for details, and
   * consider using {@linkplain #expectFailure the lambda version} instead.
   */
  public ExpectFailure() {}

  /**
   * Legacy method that returns a subject builder that expects the chained assertion to fail, and
   * makes the failure available via {@link #getFailure}.
   *
   * <p>An instance of {@link ExpectFailure} supports only one {@code whenTesting} call per test
   * method. The static {@link #expectFailure} method, by contrast, does not have this limitation.
   */
  public StandardSubjectBuilder whenTesting() {
    checkState(inRuleContext, "ExpectFailure must be used as a JUnit @Rule");
    if (failure != null) {
      throw AssertionErrorWithFacts.createWithoutFacts(
          "ExpectFailure already captured a failure", failure);
    }
    if (failureExpected) {
      throw new AssertionError(
          "ExpectFailure.whenTesting() called previously, but did not capture a failure.");
    }
    failureExpected = true;
    return StandardSubjectBuilder.forCustomFailureStrategy(
        (ExpectFailureFailureStrategy) this::captureFailure);
  }

  /**
   * Marker interface that we check for so that we can automatically disable "value of" lines during
   * {@link ExpectFailure} assertions.
   */
  interface ExpectFailureFailureStrategy extends FailureStrategy {}

  /**
   * Enters rule context to be ready to capture failures.
   *
   * <p>This should be used only from framework code. This normally means from the {@link #apply}
   * method below, but our tests call it directly under J2CL.
   */
  void enterRuleContext() {
    this.inRuleContext = true;
  }

  /** Leaves rule context and verify if a failure has been caught if it's expected. */
  void leaveRuleContext() {
    this.inRuleContext = false;
  }

  /**
   * Ensures a failure is caught if it's expected (i.e., {@link #whenTesting} is called) and throws
   * error if not.
   */
  void ensureFailureCaught() {
    if (failureExpected && failure == null) {
      throw new AssertionError(
          "ExpectFailure.whenTesting() invoked, but no failure was caught."
              + expectFailureWarningIfWeb());
    }
  }

  /** Legacy method that returns the failure captured by {@link #whenTesting}, if one occurred. */
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
          lenientFormat(
              "ExpectFailure.whenTesting() caught multiple failures:\n\n%s\n\n%s\n",
              getStackTraceAsString(failure), getStackTraceAsString(captured)));
    }
    failure = captured;
  }

  /**
   * Captures and returns the failure produced by the assertion in the provided callback, similar to
   * {@code assertThrows()}:
   *
   * <p>{@code AssertionError e = expectFailure(whenTesting ->
   * whenTesting.that(4).isNotEqualTo(4));}
   */
  @CanIgnoreReturnValue
  public static AssertionError expectFailure(StandardSubjectBuilderCallback assertionCallback) {
    ExpectFailure expectFailure = new ExpectFailure();
    expectFailure.enterRuleContext(); // safe since this instance doesn't leave this method
    assertionCallback.invokeAssertion(expectFailure.whenTesting());
    return expectFailure.getFailure();
  }

  /**
   * Captures and returns the failure produced by the assertion in the provided callback, similar to
   * {@code assertThrows()}:
   *
   * <p>{@code AssertionError e = expectFailureAbout(myTypes(), whenTesting ->
   * whenTesting.that(myType).hasProperty());}
   */
  @CanIgnoreReturnValue
  public static <S extends Subject, A> AssertionError expectFailureAbout(
      Subject.Factory<S, A> factory, SimpleSubjectBuilderCallback<S, A> assertionCallback) {
    return expectFailure(
        whenTesting -> assertionCallback.invokeAssertion(whenTesting.about(factory)));
  }

  /**
   * Creates a subject for asserting about the given {@link AssertionError}, usually one produced by
   * Truth.
   */
  public static TruthFailureSubject assertThat(@Nullable AssertionError actual) {
    return assertAbout(truthFailures()).that(actual);
  }

  @Override
  @GwtIncompatible("org.junit.rules.TestRule")
  @J2ktIncompatible
  public Statement apply(Statement base, Description description) {
    checkNotNull(base);
    checkNotNull(description);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        enterRuleContext();
        try {
          base.evaluate();
        } finally {
          leaveRuleContext();
        }
        ensureFailureCaught();
      }
    };
  }

  /**
   * A functional interface for {@link #expectFailure expectFailure()} to invoke and capture
   * failures.
   *
   * <p>Users should pass a lambda to {@code .expectFailure()} rather than directly implement this
   * interface.
   */
  public interface StandardSubjectBuilderCallback {
    void invokeAssertion(StandardSubjectBuilder whenTesting);
  }

  /**
   * A functional interface for {@link #expectFailureAbout expectFailureAbout()} to invoke and
   * capture failures.
   *
   * <p>Users should pass a lambda to {@code .expectFailureAbout()} rather than directly implement
   * this interface.
   */
  public interface SimpleSubjectBuilderCallback<S extends Subject, A> {
    void invokeAssertion(SimpleSubjectBuilder<S, A> whenTesting);
  }
}
