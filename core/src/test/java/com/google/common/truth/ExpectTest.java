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
// We use ExpectedException so that we can test our code that runs after the test method completes.
@SuppressWarnings({"ExpectedExceptionChecker", "deprecation"})
public class ExpectTest {
  private final Expect oopsNotARule = Expect.create();

  private final Expect expect = Expect.create();
  private final ExpectedException thrown = ExpectedException.none();

  private final TestRule postTestWait =
      (base, description) ->
          new Statement() {
            @Override
            public void evaluate() throws Throwable {
              base.evaluate();
              testMethodComplete.countDown();
              taskToAwait.get();
            }
          };

  private final CountDownLatch testMethodComplete = new CountDownLatch(1);

  /**
   * A task that the main thread will await, to be provided by tests that do work in other threads.
   */
  private Future<?> taskToAwait = immediateFuture(null);

  @Rule
  public final TestRule wrapper =
      (statement, description) -> {
        statement = expect.apply(statement, description);
        statement = postTestWait.apply(statement, description);
        statement = thrown.apply(statement, description);
        return statement;
      };

  @Test
  public void expectTrue() {
    expect.that(4).isEqualTo(4);
  }

  @Test
  public void singleExpectationFails() {
    thrown.expectMessage("1 expectation failed:");
    thrown.expectMessage("1. x");
    expect.withMessage("x").fail();
  }

  @Test
  public void expectFail() {
    thrown.expectMessage("3 expectations failed:");
    thrown.expectMessage("1. x");
    thrown.expectMessage("2. y");
    thrown.expectMessage("3. z");
    expect.withMessage("x").fail();
    expect.withMessage("y").fail();
    expect.withMessage("z").fail();
  }

  @Test
  public void expectFail10Aligned() {
    thrown.expectMessage("10 expectations failed:");
    thrown.expectMessage(" 1. x");
    thrown.expectMessage("10. x");
    for (int i = 0; i < 10; i++) {
      expect.withMessage("x").fail();
    }
  }

  @Test
  public void expectFail10WrappedAligned() {
    thrown.expectMessage("10 expectations failed:");
    thrown.expectMessage(" 1. abc\n      xyz");
    thrown.expectMessage("10. abc\n      xyz");
    for (int i = 0; i < 10; i++) {
      expect.withMessage("abc\nxyz").fail();
    }
  }

  @Test
  public void expectFailWithExceptionNoMessage() {
    thrown.expectMessage("3 expectations failed:");
    thrown.expectMessage("1. x");
    thrown.expectMessage("2. y");
    thrown.expectMessage("3. Also, after those failures, an exception was thrown:");
    expect.withMessage("x").fail();
    expect.withMessage("y").fail();
    throw new IllegalStateException();
  }

  @Test
  public void expectFailWithExceptionWithMessage() {
    thrown.expectMessage("3 expectations failed:");
    thrown.expectMessage("1. x");
    thrown.expectMessage("2. y");
    thrown.expectMessage("3. Also, after those failures, an exception was thrown:");
    expect.withMessage("x").fail();
    expect.withMessage("y").fail();
    throw new IllegalStateException("testing");
  }

  @Test
  public void expectFailWithExceptionBeforeExpectFailures() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("testing");
    throwException();
    expect.withMessage("x").fail();
    expect.withMessage("y").fail();
  }

  private void throwException() {
    throw new IllegalStateException("testing");
  }

  @Test
  public void expectFailWithFailuresBeforeAssume() {
    thrown.expectMessage("3 expectations failed:");
    thrown.expectMessage("1. x");
    thrown.expectMessage("2. y");
    thrown.expectMessage("3. Also, after those failures, an assumption was violated:");
    expect.withMessage("x").fail();
    expect.withMessage("y").fail();
    assume().withMessage("testing").fail();
  }

  @Test
  public void expectSuccessWithFailuresAfterAssume() {
    assume().withMessage("testing").fail();
    expect.withMessage("x").fail();
    expect.withMessage("y").fail();
  }

  @Test
  public void warnWhenExpectIsNotRule() {
    String message = "assertion made on Expect instance, but it's not enabled as a @Rule.";
    thrown.expectMessage(message);
    oopsNotARule.that(true).isEqualTo(true);
  }

  @Test
  public void bash() throws Exception {
    Runnable task = () -> expect.that(3).isEqualTo(4);
    List<Future<?>> results = new ArrayList<>();
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
            () -> {
              awaitUninterruptibly(testMethodComplete);
              try {
                expect.that(3);
                fail();
              } catch (IllegalStateException expected) {
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
    IntegerSubject expectThat3 = expect.that(3);
    taskToAwait =
        executor.submit(
            () -> {
              awaitUninterruptibly(testMethodComplete);
              try {
                expectThat3.isEqualTo(4);
                fail();
              } catch (IllegalStateException expected) {
                assertThat(expected).hasCauseThat().isInstanceOf(AssertionError.class);
              }
            });
    executor.shutdown();
  }
}
