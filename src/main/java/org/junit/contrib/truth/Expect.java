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
package org.junit.contrib.truth;

import java.util.ArrayList;
import java.util.List;

import org.junit.contrib.truth.util.GwtIncompatible;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

@GwtIncompatible("JUnit4")
@SuppressWarnings("deprecation") 
public class Expect extends TestVerb implements MethodRule {
  protected static class ExpectationGatherer implements FailureStrategy {
    List<String> messages = new ArrayList<String>();

    @Override public void fail(String message) {
      messages.add(message);
    }
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
        if (!gatherer.messages.isEmpty()) {
          String message = "All failed expectations:\n";
          for (int i = 0; i < gatherer.messages.size(); i++) {
            message += "  " + (i + 1) + ". " + gatherer.messages.get(i) + "\n";
          }
          throw new AssertionError(message);
        }
      }
    };
  }
}
