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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;

/** Tests for {@link ExpectFailure} not used as JUnit's TestRule. */
@RunWith(JUnit4.class)
public class ExpectFailureNonRuleTest {

  @Test
  public void testExpect_userThrowExceptionInSubject_shouldPropagate() throws Exception {
    final List<Failure> reportedFailure = Lists.newArrayList();
    RunNotifier runNotifier = new RunNotifier();
    runNotifier.addListener(
        new RunListener() {
          @Override
          public void testFailure(Failure failure) throws Exception {
            reportedFailure.add(failure);
          }
        });

    Runner runner = new JUnit4(ExpectFailureThrowInSubject.class);
    runner.run(runNotifier);

    assertThat(reportedFailure).hasSize(2);
    assertThat(reportedFailure.get(0).getException())
        .hasMessageThat()
        .contains("Throw deliberately");
    assertThat(reportedFailure.get(1).getException())
        .hasMessageThat()
        .contains("ExpectFailure.whenTesting() invoked, but no failure was caught.");
  }

  @Test
  public void testExpect_userThrowExceptionAfterSubject_shouldPropagate() throws Exception {
    final List<Failure> reportedFailure = Lists.newArrayList();
    RunNotifier runNotifier = new RunNotifier();
    runNotifier.addListener(
        new RunListener() {
          @Override
          public void testFailure(Failure failure) throws Exception {
            reportedFailure.add(failure);
          }
        });

    Runner runner = new JUnit4(ExpectFailureThrowAfterSubject.class);
    runner.run(runNotifier);

    assertThat(reportedFailure).hasSize(2);
    assertThat(reportedFailure.get(0).getException())
        .hasMessageThat()
        .contains("Throw deliberately");
    assertThat(reportedFailure.get(1).getException())
        .hasMessageThat()
        .contains("ExpectFailure.whenTesting() invoked, but no failure was caught.");
  }

  /**
   * A test supporting test class which will fail because method in a subject will throw exception.
   */
  public static class ExpectFailureThrowInSubject {

    final ExpectFailure expectFailure = new ExpectFailure();

    @Before
    public void setupExpectFailure() {
      expectFailure.enterRuleContext();
    }

    @After
    public void ensureFailureCaught() {
      expectFailure.ensureFailureCaught();
      expectFailure.leaveRuleContext();
    }

    @Test
    public void testExpect_throwInSubject_shouldPropagate() {
      expectFailure.whenTesting().that(throwingMethod()).isEqualTo(4);
    }
  }

  /**
   * A test supporting test class which will fail because method after a subject will throw
   * exception.
   */
  public static class ExpectFailureThrowAfterSubject {

    final ExpectFailure expectFailure = new ExpectFailure();

    @Before
    public void setupExpectFailure() {
      expectFailure.enterRuleContext();
    }

    @After
    public void ensureFailureCaught() {
      expectFailure.ensureFailureCaught();
      expectFailure.leaveRuleContext();
    }

    @Test
    public void testExpect_throwInSubject_shouldPropagate() {
      expectFailure.whenTesting().that(4).isEqualTo(4); // No failure being caught
      long unused = throwingMethod();
    }
  }

  private static long throwingMethod() {
    throw new RuntimeException("Throw deliberately");
  }
}
