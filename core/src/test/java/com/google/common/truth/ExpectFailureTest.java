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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ExpectFailure} */
@RunWith(JUnit4.class)
public class ExpectFailureTest {
  private final ExpectFailure expectFailure = new ExpectFailure();

  @Before
  public void setupExpectFailure() {
    expectFailure.enterRuleContext();
  }

  @Test
  public void expectFail_notEquals() {
    expectFailure.whenTesting().that(4).isNotEqualTo(4);
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("<4> is not equal to <4>");
  }

  @Test
  public void expectFail_stringContains() {
    expectFailure.whenTesting().that("abc").contains("x");
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("contains <\"x\">");
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
  public void expectFail_passesIfUnused() {
    assertThat(4).isEqualTo(4);
  }

  @Test
  public void expectFail_failsOnSuccess() {
    expectFailure.whenTesting().that(4).isEqualTo(4);
    try {
      @SuppressWarnings("unused")
      AssertionError unused = expectFailure.getFailure();
      throw new Error("Expected to fail");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessageThat().contains("ExpectFailure did not capture a failure.");
    }
  }

  @Test
  public void expectFail_failsOnMultipleFailures() {
    try {
      expectFailure.whenTesting().about(BadSubject.badSubject()).that(5).isEqualTo(4);
      throw new Error("Expected to fail");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessageThat().contains("caught multiple failures");
      assertThat(expected).hasMessageThat().contains("<4> is equal to <5>");
      assertThat(expected).hasMessageThat().contains("<5> is equal to <4>");
    }
  }

  @Test
  public void expectFail_failsOnMultiplewhenTestings() {
    try {
      expectFailure.whenTesting().that(4).isEqualTo(4);
      StandardSubjectBuilder unused = expectFailure.whenTesting();
      throw new Error("Expected to fail");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .contains(
              "ExpectFailure.whenTesting() called previously, but did not capture a failure.");
    }
  }

  @Test
  public void expectFail_failsOnMultiplewhenTestings_thatFail() {
    expectFailure.whenTesting().that(5).isEqualTo(4);
    try {
      StandardSubjectBuilder unused = expectFailure.whenTesting();
      throw new Error("Expected to fail");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessageThat().contains("ExpectFailure already captured a failure");
    }
  }

  @Test
  public void expectFail_failsAfterTest() {
    try {
      expectFailure.whenTesting().that(4).isEqualTo(4);
      expectFailure.ensureFailureCaught();
      throw new Error("Expected to fail");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .contains("ExpectFailure.whenTesting() invoked, but no failure was caught.");
    }
  }

  @Test
  public void expectFail_whenTestingWithoutInContext_shouldFail() {
    ExpectFailure expectFailure = new ExpectFailure();
    try {
      expectFailure.whenTesting().that(4).isEqualTo(4);
      throw new Error("Expected to fail");
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessageThat().contains("ExpectFailure must be used as a JUnit @Rule");
    }
  }

  private static Subject.Factory<StringSubject, String> strings() {
    return new Subject.Factory<StringSubject, String>() {
      @Override
      public StringSubject createSubject(FailureMetadata fm, String that) {
        return new StringSubject(fm, that);
      }
    };
  }

  private static class BadSubject extends Subject<BadSubject, Integer> {
    BadSubject(FailureMetadata failureMetadat, Integer actual) {
      super(failureMetadat, actual);
    }

    @Override
    public void isEqualTo(Object expected) {
      if (!actual().equals(expected)) {
        failWithRawMessage("expected <%s> is equal to <%s>", actual(), expected);
        failWithRawMessage("expected <%s> is equal to <%s>", expected, actual());
      }
    }

    private static Subject.Factory<BadSubject, Integer> badSubject() {
      return new Subject.Factory<BadSubject, Integer>() {
        @Override
        public BadSubject createSubject(FailureMetadata fm, Integer that) {
          return new BadSubject(fm, that);
        }
      };
    }
  }
}
