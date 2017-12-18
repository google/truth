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
import static com.google.common.truth.TruthJUnit.assume;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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

  private final Expect expect = Expect.create();
  private final ExpectedException thrown = ExpectedException.none();

  private final TestRule postTestWait =
      new TestRule() {
        @Override
        public Statement apply(final Statement base, Description description) {
          return new Statement() {
            @Override
            public void evaluate() throws Throwable {
              base.evaluate();
              testMethodComplete.countDown();
              taskToAwait.get();
            }
          };
        }
      };

  private final CountDownLatch testMethodComplete = new CountDownLatch(1);

  /**
   * A task that the main thread will await, to be provided by tests that do work in other threads.
   */
  private Future<?> taskToAwait = immediateFuture(null);

  @Rule
  public final TestRule wrapper =
      new TestRule() {
        @Override
        public Statement apply(Statement statement, Description description) {
          statement = expect.apply(statement, description);
          statement = postTestWait.apply(statement, description);
          statement = thrown.apply(statement, description);
          return statement;
        }
      };

  @Test
  public void expectTrue() {
    expect.that(4).isEqualTo(4);
  }

  @Test
  public void singleExpectationFails() {
    thrown.expectMessage("1 expectation failed:");
    thrown.expectMessage("1. Not true that <\"abc\"> contains <\"x\">");
    expect.that("abc").contains("x");
  }

  @Test
  public void expectFail() {
    thrown.expectMessage("3 expectations failed:");
    thrown.expectMessage("1. Not true that <\"abc\"> contains <\"x\">");
    thrown.expectMessage("2. Not true that <\"abc\"> contains <\"y\">");
    thrown.expectMessage("3. Not true that <\"abc\"> contains <\"z\">");
    expect.that("abc").contains("x");
    expect.that("abc").contains("y");
    expect.that("abc").contains("z");
  }

  @Test
  public void expectFailWithExceptionNoMessage() {
    thrown.expectMessage("3 expectations failed:");
    thrown.expectMessage("1. Not true that <\"abc\"> contains <\"x\">");
    thrown.expectMessage("2. Not true that <\"abc\"> contains <\"y\">");
    thrown.expectMessage(
        "3. Failures occurred before an exception was thrown while the test was running: "
            + "java.lang.IllegalStateException");
    expect.that("abc").contains("x");
    expect.that("abc").contains("y");
    throw new IllegalStateException();
  }

  @Test
  public void expectFailWithExceptionWithMessage() {
    thrown.expectMessage("3 expectations failed:");
    thrown.expectMessage("1. Not true that <\"abc\"> contains <\"x\">");
    thrown.expectMessage("2. Not true that <\"abc\"> contains <\"y\">");
    thrown.expectMessage(
        "3. Failures occurred before an exception was thrown while the test was running: "
            + "java.lang.IllegalStateException: testing");
    expect.that("abc").contains("x");
    expect.that("abc").contains("y");
    throw new IllegalStateException("testing");
  }

  @Test
  public void expectFailWithExceptionBeforeExpectFailures() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("testing");
    throwException();
    expect.that("abc").contains("x");
    expect.that("abc").contains("y");
  }

  private void throwException() {
    throw new IllegalStateException("testing");
  }

  @Test
  public void expectFailWithFailuresBeforeAssume() {
    thrown.expectMessage("3 expectations failed:");
    thrown.expectMessage("1. Not true that <\"abc\"> contains <\"x\">");
    thrown.expectMessage("2. Not true that <\"abc\"> contains <\"y\">");
    thrown.expectMessage(
        "3. Failures occurred before an assumption was violated: "
            + "com.google.common.truth.TruthJUnit$ThrowableAssumptionViolatedException: testing");
    expect.that("abc").contains("x");
    expect.that("abc").contains("y");
    assume().fail("testing");
  }

  @Test
  public void expectSuccessWithFailuresAfterAssume() {
    assume().fail("testing");
    expect.that("abc").contains("x");
    expect.that("abc").contains("y");
  }

  @Test
  public void warnWhenExpectIsNotRule() {
    String message = "assertion made on Expect instance, but it's not enabled as a @Rule.";
    thrown.expectMessage(message);
    oopsNotARule.that(true).isEqualTo(true);
  }

  @Test
  public void bash() throws Exception {
    Runnable task =
        new Runnable() {
          @Override
          public void run() {
            expect.that(3).isEqualTo(4);
          }
        };
    List<Future<?>> results = new ArrayList<Future<?>>();
    ExecutorService executor = newFixedThreadPool(10);
    for (int i = 0; i < 1000; i++) {
      results.add(executor.submit(task));
    }
    executor.shutdown();
    for (Future<?> result : results) {
      result.get();
    }
    thrown.expectMessage("1000 expectations failed:");
  }

  @Test
  public void failWhenCallingThatAfterTest() {
    ExecutorService executor = newSingleThreadExecutor();
    taskToAwait =
        executor.submit(
            new Runnable() {
              @Override
              public void run() {
                awaitUninterruptibly(testMethodComplete);
                try {
                  expect.that(3);
                  fail();
                } catch (IllegalStateException expected) {
                }
              }
            });
    executor.shutdown();
  }

  @Test
  public void failWhenCallingFailingAssertionMethodAfterTest() {
    ExecutorService executor = newSingleThreadExecutor();
    /*
     * We wouldn't expect people to do this exactly. The point is that, if someone were to call
     * expect.that(3).isEqualTo(4), we would always either fail the test or throw an
     * IllegalStateException, not record a "failure" that we never read.
     */
    final IntegerSubject expectThat3 = expect.that(3);
    taskToAwait =
        executor.submit(
            new Runnable() {
              @Override
              public void run() {
                awaitUninterruptibly(testMethodComplete);
                try {
                  expectThat3.isEqualTo(4);
                  fail();
                } catch (IllegalStateException expected) {
                  assertThat(expected).hasCauseThat().isInstanceOf(AssertionError.class);
                }
              }
            });
    executor.shutdown();
  }
}
