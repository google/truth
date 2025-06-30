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

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ExpectFailure}. */
@RunWith(JUnit4.class)
public class ExpectFailureTest {
  private final ExpectFailure expectFailure = new ExpectFailure();

  @Before
  public void setupExpectFailure() {
    expectFailure.enterRuleContext();
  }

  @Test
  public void expectFail() {
    expectFailure.whenTesting().withMessage("abc").fail();
    assertThat(expectFailure.getFailure()).hasMessageThat().isEqualTo("abc");
  }

  @Test
  public void expectFail_withCause() {
    expectFailure.whenTesting().that(new NullPointerException()).isNull();
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("NullPointerException");
    assertThat(expectFailure.getFailure()).hasCauseThat().isInstanceOf(NullPointerException.class);
  }

  @Test
  public void expectFail_about() {
    expectFailure.whenTesting().about(strings()).that("foo").isEqualTo("bar");
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("foo");
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void expectFail_passesIfUnused() {
    assertThat(4).isEqualTo(4);
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void expectFail_failsOnSuccess() {
    expectFailure.whenTesting().that(4).isEqualTo(4);
    AssertionError e = assertThrows(AssertionError.class, () -> expectFailure.getFailure());
    assertThat(e).hasMessageThat().contains("ExpectFailure did not capture a failure.");
  }

  @Test
  public void expectFail_failsOnMultipleFailures() {
    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> expectFailure.whenTesting().about(badSubject()).that(5).isEqualTo(4));
    assertThat(e).hasMessageThat().contains("caught multiple failures");
    assertThat(e).hasMessageThat().contains("4!=5");
    assertThat(e).hasMessageThat().contains("5!=4");
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void expectFail_failsOnMultipleWhenTestings() {
    expectFailure.whenTesting().that(4).isEqualTo(4);
    AssertionError e = assertThrows(AssertionError.class, () -> expectFailure.whenTesting());
    assertThat(e)
        .hasMessageThat()
        .contains("ExpectFailure.whenTesting() called previously, but did not capture a failure.");
  }

  @Test
  public void expectFail_failsOnMultipleWhenTestings_thatFail() {
    expectFailure.whenTesting().that(5).isEqualTo(4);
    AssertionError e = assertThrows(AssertionError.class, () -> expectFailure.whenTesting());
    assertThat(e).hasMessageThat().contains("ExpectFailure already captured a failure");
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void expectFail_failsAfterTest() {
    expectFailure.whenTesting().that(4).isEqualTo(4);
    AssertionError e =
        assertThrows(AssertionError.class, () -> expectFailure.ensureFailureCaught());
    assertThat(e)
        .hasMessageThat()
        .contains("ExpectFailure.whenTesting() invoked, but no failure was caught.");
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void expectFail_whenTestingWithoutInContext_shouldFail() {
    ExpectFailure expectFailure = new ExpectFailure();
    IllegalStateException expected =
        assertThrows(
            IllegalStateException.class, () -> expectFailure.whenTesting().that(4).isEqualTo(4));
    assertThat(expected).hasMessageThat().contains("ExpectFailure must be used as a JUnit @Rule");
  }

  private static Subject.Factory<StringSubject, String> strings() {
    return StringSubject::new;
  }

  private static class BadSubject extends Subject {
    private final @Nullable Integer actual;

    BadSubject(FailureMetadata metadata, @Nullable Integer actual) {
      super(metadata, actual);
      this.actual = actual;
    }

    @Override
    public void isEqualTo(@Nullable Object expected) {
      if (!Objects.equals(actual, expected)) {
        failWithoutActual(simpleFact(actual + "!=" + expected));
        failWithoutActual(simpleFact(expected + "!=" + actual));
      }
    }
  }

  private static Subject.Factory<BadSubject, Integer> badSubject() {
    return BadSubject::new;
  }
}
