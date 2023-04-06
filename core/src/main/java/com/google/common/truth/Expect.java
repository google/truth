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
import static com.google.common.base.Strings.padStart;
import static com.google.common.base.Strings.repeat;
import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.common.truth.Expect.TestPhase.AFTER;
import static com.google.common.truth.Expect.TestPhase.BEFORE;
import static com.google.common.truth.Expect.TestPhase.DURING;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Throwables;
import com.google.common.truth.Truth.SimpleAssertionError;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} that batches up all failures encountered during a test, and reports them all
 * together at the end (similar to {@link ErrorCollector}). It is also useful for making assertions
 * from other threads or from within callbacks whose exceptions would be swallowed or logged, rather
 * than propagated out to fail the test. (<a href="https://joel-costigliola.github.io/assertj/"
 * target="_top">AssertJ</a> has a similar feature called "soft assertions"; however, soft
 * assertions are not safe for concurrent use.)
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
 * <p>{@code Expect} may be used concurrently from multiple threads. However, multithreaded tests
 * still require care:
 *
 * <ul>
 *   <li>{@code Expect} has no way of knowing when all your other test threads are done. It simply
 *       checks for failures when the main thread finishes executing the test method. Thus, you must
 *       ensure that any background threads complete their assertions before then, or your test may
 *       ignore their results.
 *   <li>Assertion failures are not the only exceptions that may occur in other threads. For maximum
 *       safety, multithreaded tests should check for such exceptions regardless of whether they use
 *       {@code Expect}. (Typically, this means calling {@code get()} on any {@code Future} returned
 *       by a method like {@code executor.submit(...)}. It might also include checking for
 *       unexpected log messages
 *       or reading metrics that count failures.) If your tests already check for exceptions from a
 *       thread, then that will any cover exception from plain {@code assertThat}.
 * </ul>
 *
 * <p>To record failures for the purpose of testing that an assertion fails when it should, see
 * {@link ExpectFailure}.
 *
 * <p>For more on this class, see <a href="https://truth.dev/expect">the documentation page</a>.
 */
@GwtIncompatible("JUnit4")
@J2ktIncompatible
public final class Expect extends StandardSubjectBuilder implements TestRule {

  private static final class ExpectationGatherer implements FailureStrategy {
    @GuardedBy("this")
    private final List<AssertionError> failures = new ArrayList<>();

    @GuardedBy("this")
    private TestPhase inRuleContext = BEFORE;

    ExpectationGatherer() {}

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
      if (failures.isEmpty()) {
        return "No expectation failed.";
      }
      int numFailures = failures.size();
      StringBuilder message =
          new StringBuilder()
              .append(numFailures)
              .append(numFailures > 1 ? " expectations" : " expectation")
              .append(" failed:\n");
      int countLength = String.valueOf(failures.size() + 1).length();
      int count = 0;
      for (AssertionError failure : failures) {
        count++;
        message.append("  ");
        message.append(padStart(String.valueOf(count), countLength, ' '));
        message.append(". ");
        if (count == 1) {
          appendIndented(countLength, message, getStackTraceAsString(failure));
        } else {
          appendIndented(
              countLength,
              message,
              printSubsequentFailure(failures.get(0).getStackTrace(), failure));
        }
        message.append("\n");
      }

      return message.toString();
    }

    // String.repeat is not available under Java 8 and old versions of Android.
    @SuppressWarnings({"StringsRepeat", "InlineMeInliner"})
    private static void appendIndented(int countLength, StringBuilder builder, String toAppend) {
      int indent = countLength + 4; // "  " and ". "
      builder.append(toAppend.replace("\n", "\n" + repeat(" ", indent)));
    }

    private String printSubsequentFailure(
        StackTraceElement[] baseTraceFrames, AssertionError toPrint) {
      Exception e = new RuntimeException("__EXCEPTION_MARKER__", toPrint);
      e.setStackTrace(baseTraceFrames);
      String s = Throwables.getStackTraceAsString(e);
      // Force single line reluctant matching
      return s.replaceFirst("(?s)^.*?__EXCEPTION_MARKER__.*?Caused by:\\s+", "");
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
        throw SimpleAssertionError.createWithNoStack(this.toString());
      }
    }

    @GuardedBy("this")
    private void doLeaveRuleContext(Throwable caught) throws Throwable {
      if (hasFailures()) {
        String message =
            caught instanceof AssumptionViolatedException
                ? "Also, after those failures, an assumption was violated:"
                : "Also, after those failures, an exception was thrown:";
        record(SimpleAssertionError.createWithNoStack(message, caught));
        throw SimpleAssertionError.createWithNoStack(this.toString());
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

  /** Creates a new instance. */
  public static Expect create() {
    return new Expect(new ExpectationGatherer());
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
  public Statement apply(Statement base, Description description) {
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
