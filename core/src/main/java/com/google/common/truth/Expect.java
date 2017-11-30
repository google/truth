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
import static com.google.common.truth.Platform.comparisonFailure;
import static com.google.common.truth.StackTraceCleaner.cleanStackTrace;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.truth.Truth.AssertionErrorWithCause;
import java.util.ArrayList;
import java.util.List;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} that batches up all failures encountered during a test, and reports them all
 * together at the end.
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
 * <p>To record failures for the purpose of testing that an assertion fails when it should, see
 * {@link ExpectFailure}.
 */
@GwtIncompatible("JUnit4")
public final class Expect extends StandardSubjectBuilder implements TestRule {

  private static final class ExpectationGatherer extends AbstractFailureStrategy {
    private final List<AssertionError> failures = new ArrayList<AssertionError>();
    private final boolean showStackTrace;

    ExpectationGatherer(boolean showStackTrace) {
      this.showStackTrace = showStackTrace;
    }

    @Override
    public void failComparing(
        String message, CharSequence expected, CharSequence actual, Throwable cause) {
      cleanAndRecord(comparisonFailure(message, expected.toString(), actual.toString(), cause));
    }

    @Override
    public void fail(String message, Throwable cause) {
      cleanAndRecord(new AssertionErrorWithCause(message, cause));
    }

    void cleanAndRecord(AssertionError failure) {
      cleanStackTrace(failure);
      failures.add(failure);
    }

    List<AssertionError> getFailures() {
      return failures;
    }

    @Override
    public String toString() {
      List<AssertionError> failures = getFailures();
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
  }

  private final ExpectationGatherer gatherer;
  private boolean inRuleContext = false;

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
    return !gatherer.getFailures().isEmpty();
  }

  @Override
  void checkStatePreconditions() {
    checkState(
        inRuleContext, "assertion made on Expect instance, but it's not enabled as a @Rule.");
  }

  @Override
  public Statement apply(final Statement base, Description description) {
    checkNotNull(base);
    checkNotNull(description);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        inRuleContext = true;
        try {
          base.evaluate();
        } catch (Throwable t) {
          if (!gatherer.getFailures().isEmpty()) {
            String message =
                t instanceof AssumptionViolatedException
                    ? "Failures occurred before an assumption was violated"
                    : "Failures occurred before an exception was thrown while the test was running";
            gatherer.cleanAndRecord(new AssertionErrorWithCause(message + ": " + t, t));
          } else {
            throw t;
          }
        } finally {
          inRuleContext = false;
        }
        if (!gatherer.getFailures().isEmpty()) {
          throw new AssertionError(gatherer.toString());
        }
      }
    };
  }
}
