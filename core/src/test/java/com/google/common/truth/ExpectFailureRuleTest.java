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

import com.google.common.annotations.GwtIncompatible;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ExpectFailure} used as JUnit {@link Rule).*/
@RunWith(JUnit4.class)
@GwtIncompatible("org.junit.Rule")
public class ExpectFailureRuleTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void expectFail_captureFailureAsExpected() {
    expectFailure.whenTesting().that(4).isNotEqualTo(4);
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("<4> is not equal to <4>");
  }

  @Test
  public void expectFail_passesIfUnused() {
    assertThat(4).isEqualTo(4);
  }

  @Test
  public void expectFail_failsAfterTest() {
    expectFailure.whenTesting().that(4).isEqualTo(4);
    thrown.expectMessage("ExpectFailure.whenTesting() invoked, but no failure was caught.");
  }

  @Test
  public void expectFail_throwInSubject_shouldPropagateOriginalException() {
    thrown.expectMessage("Throwing deliberately");
    expectFailure.whenTesting().that(throwingMethod()).isEqualTo(2);
  }

  @Test
  public void expectFail_throwAfterSubject_shouldPropagateOriginalException() {
    expectFailure.whenTesting().that(2).isEqualTo(2);
    thrown.expectMessage("Throwing deliberately");
    throwingMethod();
  }

  private static long throwingMethod() {
    throw new RuntimeException("Throwing deliberately");
  }
}
