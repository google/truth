/*
 * Copyright (c) 2011 Google, Inc.
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
import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.common.truth.Expect.TestPhase.AFTER;
import static com.google.common.truth.Expect.TestPhase.BEFORE;
import static com.google.common.truth.Expect.TestPhase.DURING;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.truth.Truth.AssertionErrorWithCause;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} that batches up all failures encountered during a test, and reports them all
 * together at the end (similar to {@link ErrorCollector}). It is also useful for making assertions
 * from other threads or from within callbacks whose exceptions would be swallowed or logged, rather
 * than propagated out to fail the test.
 *
 * <p>Usage:
 *
 * <pre>
 * {@code @Rule public final Expect expect = Expect.create();}
 *
 * {@code ...}
 *
 * {@code   expect.that(results).containsExactly(...);}
 * {@code   expect.that(errors).isEmpty();}
 * </pre>
 *
 * If both of the assertions above fail, the test will fail with an exception that contains
 * information about both.
 *
 * <p>{@code Expect} may be used concurrently from multiple threads. Note, however, that {@code
 * Expect} has no way of knowing when all your other test threads are done. It simply checks for
 * failures when the main thread finishes executing the test method. Thus, you must ensure that any
 * background threads complete their assertions before then, or your test may ignore their results.
 *
 * <p>To record failures for the purpose of testing that an assertion fails when it should, see
 * {@link ExpectFailure}.
 */
@GwtIncompatible("JUnit4")
public final class Expect extends StandardSubjectBuilder implements TestRule {

  private static final class ExpectationGatherer implements FailureStrategy {
    @GuardedBy("this")
    private final List<AssertionError> failures = new ArrayList<AssertionError>();

    @GuardedBy("this")
    private TestPhase inRuleContext = BEFORE;

    private final boolean showStackTrace;

    ExpectationGatherer(boolean showStackTrace) {
      this.showStackTrace = showStackTrace;
    }

    @Override
    public synchronized void fail(AssertionError failure) {
      record(failure);
    }

    synchronized void enterRuleContext() {
      checkState(inRuleContext == BEFORE);
      inRuleContext = DURING;
    }

    synchronized void leaveRuleContext(@Nullable Throwable caught) throws Throwable {
      try {
        if (caught == null) {
          doLeaveRuleContext();
        } else {
          doLeaveRuleContext(caught);
        }
        /*
         * We'd like to check this even if an exception was thrown, but we don't want to override
         * the "real" failure. TODO(cpovirk): Maybe attach as a suppressed exception once we require
         * a newer version of Android.
         */
        checkState(inRuleContext == DURING);
      } finally {
        inRuleContext = AFTER;
      }
    }

    synchronized void checkInRuleContext() {
      doCheckInRuleContext(null);
    }

    synchronized boolean hasFailures() {
      return !failures.isEmpty();
    }

    @Override
    public synchronized String toString() {
      int numFailures = failures.size();
      StringBuilder message =
          new StringBuilder(
              numFailures + (numFailures > 1 ? " expectations" : " expectation") + " failed:\n");
      int count = 0;
      for (AssertionError failure : failures) {
        count++;
        message.append("  ");
        message.append(count);
        message.append(". ");
        message.append(showStackTrace ? getStackTraceAsString(failure) : failure.getMessage());
        message.append("\n");
      }

      return message.toString();
    }

    @GuardedBy("this")
    private void doCheckInRuleContext(@Nullable AssertionError failure) {
      switch (inRuleContext) {
        case BEFORE:
          throw new IllegalStateException(
              "assertion made on Expect instance, but it's not enabled as a @Rule.", failure);
        case DURING:
          return;
        case AFTER:
          throw new IllegalStateException(
              "assertion made on Expect instance, but its @Rule has already completed. Maybe "
                  + "you're making assertions from a background thread and not waiting for them to "
                  + "complete, or maybe you've shared an Expect instance across multiple tests? "
                  + "We're throwing this exception to warn you that your assertion would have been "
                  + "ignored. However, this exception might not cause any test to fail, or it "
                  + "might cause some subsequent test to fail rather than the test that caused the "
                  + "problem.",
              failure);
      }
      throw new AssertionError();
    }

    @GuardedBy("this")
    private void doLeaveRuleContext() {
      if (hasFailures()) {
        throw new AssertionError(this);
      }
    }

    @GuardedBy("this")
    private void doLeaveRuleContext(Throwable caught) throws Throwable {
      if (hasFailures()) {
        String message =
            caught instanceof AssumptionViolatedException
                ? "Failures occurred before an assumption was violated"
                : "Failures occurred before an exception was thrown while the test was running";
        record(new AssertionErrorWithCause(message + ": " + caught, caught));
        throw new AssertionError(this);
      } else {
        throw caught;
      }
    }

    @GuardedBy("this")
    private void record(AssertionError failure) {
      doCheckInRuleContext(failure);
      failures.add(failure);
    }
  }

  private final ExpectationGatherer gatherer;

  public static Expect create() {
    return new Expect(new ExpectationGatherer(false /* showStackTrace */));
  }

  public static Expect createAndEnableStackTrace() {
    return new Expect(new ExpectationGatherer(true /* showStackTrace */));
  }

  private Expect(ExpectationGatherer gatherer) {
    super(FailureMetadata.forFailureStrategy(gatherer));
    this.gatherer = checkNotNull(gatherer);
  }

  public boolean hasFailures() {
    return gatherer.hasFailures();
  }

  @Override
  void checkStatePreconditions() {
    gatherer.checkInRuleContext();
  }

  @Override
  public Statement apply(final Statement base, Description description) {
    checkNotNull(base);
    checkNotNull(description);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        gatherer.enterRuleContext();
        Throwable caught = null;
        try {
          base.evaluate();
        } catch (Throwable t) {
          caught = t;
        } finally {
          gatherer.leaveRuleContext(caught);
        }
      }
    };
  }

  enum TestPhase {
    BEFORE,
    DURING,
    AFTER;
  }
}
