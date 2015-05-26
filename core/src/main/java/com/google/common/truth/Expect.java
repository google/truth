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

import static com.google.common.truth.StringUtil.messageFor;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtIncompatible;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@GwtIncompatible("JUnit4")
public class Expect extends TestVerb implements TestRule {
  public static class ExpectationGatherer extends FailureStrategy {
    private List<ExpectationFailure> messages = new ArrayList<ExpectationFailure>();

    @Override
    public void fail(String message) {
      fail(message, new Throwable(message));
    }

    @Override
    public void failComparing(String message, CharSequence expected, CharSequence actual) {
      String errorMessage = messageFor(message, expected, actual);
      fail(errorMessage, new Throwable(errorMessage));
    }

    @Override
    public void fail(String message, Throwable cause) {
      messages.add(ExpectationFailure.create(message, cause));
    }

    public List<ExpectationFailure> getMessages() {
      return messages;
    }
  }

  @AutoValue
  static abstract class ExpectationFailure {
    static ExpectationFailure create(String message, Throwable cause) {
      return new AutoValue_Expect_ExpectationFailure(message, cause);
    }

    static ExpectationFailure create(String message) {
      return new AutoValue_Expect_ExpectationFailure(message, null);
    }

    ExpectationFailure() {}

    abstract String message();

    abstract @Nullable Throwable cause();
  }

  private final ExpectationGatherer gatherer;
  private final boolean showStackTrace;
  private boolean inRuleContext = false;

  public static Expect create() {
    return new Expect(new ExpectationGatherer());
  }

  public static Expect createAndEnableStackTrace() {
    return new Expect(new ExpectationGatherer(), true /* showStackTrace */);
  }

  Expect(ExpectationGatherer gatherer) {
    this(gatherer, false /* showStackTrace */);
  }

  Expect(ExpectationGatherer gatherer, boolean showStackTrace) {
    super(gatherer);
    this.gatherer = gatherer;
    this.showStackTrace = showStackTrace;
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

  // TODO(cgruber): Make this override TestRule when 4.9 is released.
  @Override
  public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        inRuleContext = true;
        base.evaluate();
        inRuleContext = false;
        Throwable earliestCause = null;
        if (!gatherer.getMessages().isEmpty()) {
          StringBuilder message = new StringBuilder("All failed expectations:\n");
          int count = 0;
          for (ExpectationFailure failure : gatherer.getMessages()) {
            if (earliestCause == null && failure.cause() != null) {
              earliestCause = failure.cause();
            }
            message
                .append("  ")
                .append((count++) + 1)
                .append(". ")
                .append(failure.message())
                .append("\n");
            if (showStackTrace && failure.cause() != null) {
              // Append stack trace to the failure message
              StringWriter stackTraceWriter = new StringWriter();
              failure.cause().printStackTrace(new PrintWriter(stackTraceWriter));
              message.append(stackTraceWriter + "\n");
            }
          }
          AssertionError error = new AssertionError(message.toString());
          error.initCause(earliestCause);
          throw error;
        }
      }
    };
  }
}
