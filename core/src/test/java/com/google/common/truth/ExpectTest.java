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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.Statement;

/**
 * Tests (and effectively sample code) for the Expect verb (implemented as a rule)
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class ExpectTest {
  private final Expect oopsNotARule = Expect.create();

  private final Expect EXPECT = Expect.create();
  private final ExpectedException thrown = ExpectedException.none();

  @Rule
  public final TestRule wrapper =
      new TestRule() {
        @Override
        public Statement apply(Statement base, Description description) {
          Statement expected = EXPECT.apply(base, description);
          return thrown.apply(expected, description);
        }
      };

  @Test
  public void expectTrue() {
    EXPECT.that(4).isEqualTo(4);
  }

  @Test
  public void expectFail() {
    thrown.expectMessage("All failed expectations:");
    thrown.expectMessage("1. Not true that <\"abc\"> contains <\"x\">");
    thrown.expectMessage("2. Not true that <\"abc\"> contains <\"y\">");
    thrown.expectMessage("3. Not true that <\"abc\"> contains <\"z\">");
    EXPECT.that("abc").contains("x");
    EXPECT.that("abc").contains("y");
    EXPECT.that("abc").contains("z");
  }

  @Test
  public void warnWhenExpectIsNotRule() {
    String message = "assertion made on Expect instance, but it's not enabled as a @Rule.";
    thrown.expectMessage(message);
    oopsNotARule.that(true).isEqualTo(true);
  }
}
