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

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.truth.Expect.ExpectationGatherer;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Tests (and effectively sample code) for the Expect 
 * verb (implemented as a rule)
 * 
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class ExpectFailureTest {
  @Rule public Expect EXPECT = new FailingExpect(new ExpectationGatherer());


  @Test public void expectFailNotEquals() {
    EXPECT.that(4).isNotEqualTo(4);
  }

  @Test public void expectFailStringContains() {
    EXPECT.that("abc").contains("x").and().contains("y").and().contains("z");
  }
  
  @Test public void expectFailContainsAllOf() {
    EXPECT.that(Arrays.asList("a", "b", "c")).containsAllOf("a", "c", "d");
  }
  
  @Test public void expectFailContainsAnyOf() {
    EXPECT.that(Arrays.asList("a", "b", "c")).containsAnyOf("z", "q");
  }
  
  public static class FailingExpect extends Expect {
    protected FailingExpect(ExpectationGatherer gatherer) {
      super(gatherer);
    }

    @Override public Statement apply(Statement base, FrameworkMethod method, Object target) {
      final Statement s = super.apply(base, method, target);
      return new Statement() {

        @Override public void evaluate() throws Throwable {
          try {
            s.evaluate();
          } catch (AssertionError e) {
            return; //ignore - we're happy that it threw.
          }
          throw new AssertionError("Should have thrown error with caught assertion failures..");
        }        
      };
    }
  }
}
