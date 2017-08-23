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
import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.common.truth.StringUtil.messageFor;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

@GwtIncompatible("JUnit4")
public final class Expect extends TestVerb implements TestRule {
  /**
   * @deprecated To provide your own failure handling, use {@code new TestVerb(new
   *     AbstractFailureStrategy() { ... })} instead of {@code Expect.create(new
   *     ExpectationGatherer() { ... })}. Or, if you're testing that assertions on a custom {@code
   *     Subject} fail (using {@code ExpectationGatherer} to capture the failures), use {@link
   *     ExpectFailure}.
   */
  @Deprecated
  public static class ExpectationGatherer extends AbstractFailureStrategy {
    private final List<ExpectationFailure> messages = new ArrayList<ExpectationFailure>();
    private final boolean showStackTrace;

    public ExpectationGatherer() {
      this.showStackTrace = false;
    }

    public ExpectationGatherer(boolean showStackTrace) {
      this.showStackTrace = showStackTrace;
    }

    @Override
    public void failComparing(
        String message, CharSequence expected, CharSequence actual, Throwable cause) {
      fail(messageFor(message, expected, actual), cause);
    }

    @Override
    public void fail(String message, Throwable cause) {
      messages.add(
          new ExpectationFailure(
              message, cause != null ? new AssertionError(cause) : new AssertionError()));
    }

    // TODO(cpovirk): Rename this to "getFailures" or something.
    public List<ExpectationFailure> getMessages() {
      return messages;
    }

    @Override
    public String toString() {
      StringBuilder message = new StringBuilder("All failed expectations:\n");
      int count = 0;
      for (ExpectationFailure failure : getMessages()) {
        message.append("  ");
        message.append(++count);
        message.append(". ");
        message.append(failure.message());
        message.append("\n");
        if (showStackTrace) {
          message.append(getStackTraceAsString(stripTruthStackFrames(failure.cause())));
          message.append("\n");
        }
      }

      return message.toString();
    }
  }

  // TODO(cpovirk): Eliminate this in favor of just storing an AssertionError.
  private static final class ExpectationFailure {
    private final String message;
    private final Throwable cause;

    ExpectationFailure(String message, Throwable cause) {
      this.message = checkNotNull(message);
      this.cause = cause;
    }

    String message() {
      return message;
    }

    Throwable cause() {
      return cause;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (other instanceof ExpectationFailure) {
        ExpectationFailure that = (ExpectationFailure) other;
        return this.message.equals(that.message) && Objects.equal(this.cause, that.cause);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(message, cause);
    }
  }

  private final ExpectationGatherer gatherer;
  private boolean inRuleContext = false;

  public static Expect create() {
    return create(new ExpectationGatherer());
  }

  /**
   * @deprecated To provide your own failure handling, use {@code new TestVerb(new
   *     AbstractFailureStrategy() { ... })} instead of {@code Expect.create(new
   *     ExpectationGatherer() { ... })}.
   */
  @Deprecated
  public static Expect create(ExpectationGatherer gatherer) {
    return new Expect(gatherer);
  }

  public static Expect createAndEnableStackTrace() {
    return new Expect(new ExpectationGatherer(true /* showStackTrace */));
  }

  Expect(ExpectationGatherer gatherer) {
    super(gatherer);
    this.gatherer = checkNotNull(gatherer);
  }

  public boolean hasFailures() {
    return !gatherer.getMessages().isEmpty();
  }

  @Override
  protected FailureStrategy getFailureStrategy() {
    if (!inRuleContext) {
      String message = "assertion made on Expect instance, but it's not enabled as a @Rule.";
      throw new IllegalStateException(message);
    }
    return super.getFailureStrategy();
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
          if (!gatherer.getMessages().isEmpty()) {
            String message =
                t instanceof AssumptionViolatedException
                    ? "Failures occurred before an assumption was violated"
                    : "Failures occurred before an exception was thrown while the test was running";
            gatherer.fail(message + ": " + t, t);
          } else {
            throw t;
          }
        }
        inRuleContext = false;
        if (!gatherer.getMessages().isEmpty()) {
          throw new AssertionError(gatherer.toString());
        }
      }
    };
  }
}
