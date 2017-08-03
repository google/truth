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
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Long Subjects.
 *
 * @author David Saff
 * @author Christian Gruber
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class LongSubjectTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void simpleEquality() {
    assertThat(2L + 2).isEqualTo(4L);
  }

  @Test
  public void longIsLong() {
    assertThat(4L).isEqualTo(4L);
  }

  @Test
  public void simpleInequality() {
    assertThat(2L + 2).isNotEqualTo(5L);
  }

  @Test
  public void equalityWithInts() {
    long x = 0;

    assertThat(x).isEqualTo(0);

    expectFailure.whenTesting().that(x).isNotEqualTo(0);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <0> is not equal to <0>");
  }

  @Test
  public void equalityFail() {
    expectFailure.whenTesting().that(2L + 2).isEqualTo(5L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <4> is equal to <5>");
  }

  @Test
  public void inequalityFail() {
    expectFailure.whenTesting().that(2L + 2).isNotEqualTo(4L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <4> is not equal to <4>");
  }

  @Test
  public void additionAssumptionFail() {
    try {
      assume().that(2L + 2).isEqualTo(5L);
      fail("Should have thrown");
    } catch (AssumptionViolatedException expected) {
    }
  }

  @Test
  public void equalityOfNulls() {
    assertThat((Long) null).isEqualTo((Long) null);
  }

  @Test
  public void equalityOfNullsFail_nullActual() {
    expectFailure.whenTesting().that((Long) null).isEqualTo(5L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is equal to <5>");
  }

  @Test
  public void equalityOfNullsFail_nullExpected() {
    expectFailure.whenTesting().that(5L).isEqualTo((Long) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <5> is equal to <null>");
  }

  @Test
  public void inequalityOfNulls() {
    assertThat((Long) null).isNotEqualTo(4L);
    assertThat(4L).isNotEqualTo((Long) null);
  }

  @Test
  public void inequalityOfNullsFail() {
    expectFailure.whenTesting().that((Long) null).isNotEqualTo((Long) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not equal to <null>");
  }
}
