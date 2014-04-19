/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0;

import static org.truth0.util.StringUtil.messageFor;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtIncompatible;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.truth0.FailureStrategy.ThrowableAssertionError;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@GwtIncompatible("JUnit4")
public class Expect extends TestVerb implements MethodRule {
  protected static class ExpectationGatherer extends
  FailureStrategy {
    List<ExpectationFailure> messages = new ArrayList<ExpectationFailure>();

    @Override public void fail(String message) {
      messages.add(ExpectationFailure.create(message));
    }

    @Override public void failComparing(
        String message, CharSequence expected, CharSequence actual) {
      messages.add(ExpectationFailure.create(messageFor(message, expected, actual)));
    }

    @Override public void fail(String message, Throwable cause) {
      messages.add(ExpectationFailure.create(message, cause));
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
  private boolean inRuleContext = false;

  public static Expect create() {
    return new Expect(new ExpectationGatherer());
  }

  Expect(ExpectationGatherer gatherer) {
    super(gatherer);
    this.gatherer = gatherer;
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
  @Override public Statement apply(final Statement base,
      FrameworkMethod method, Object target) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        inRuleContext = true;
        base.evaluate();
        inRuleContext = false;
        Throwable earliestCause = null;
        if (!gatherer.messages.isEmpty()) {
          StringBuilder message = new StringBuilder("All failed expectations:\n");
          int count = 0;
          for (ExpectationFailure failure : gatherer.messages) {
            if (earliestCause == null && failure.cause() != null) {
              earliestCause = failure.cause();
            }
            message.append("  ").append((count++) + 1).append(". ")
                   .append(failure.message()).append("\n");
          }
          throw new ThrowableAssertionError(message.toString(), earliestCause);
        }
      }
    };
  }
}
