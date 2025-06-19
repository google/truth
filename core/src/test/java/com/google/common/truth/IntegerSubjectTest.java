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

import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.Fact.formatNumericValue;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Integer Subjects.
 *
 * @author David Saff
 * @author Christian Gruber
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class IntegerSubjectTest {

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void simpleEquality() {
    assertThat(4).isEqualTo(4);
  }

  @Test
  public void simpleInequality() {
    assertThat(4).isNotEqualTo(5);
  }

  @Test
  public void equalityWithLongs() {
    assertThat(0).isEqualTo(0L);
    expectFailure(whenTesting -> whenTesting.that(0).isNotEqualTo(0L));
  }

  @Test
  public void equalityFail() {
    expectFailure(whenTesting -> whenTesting.that(4).isEqualTo(5));
  }

  @SuppressWarnings("SelfAssertion")
  @Test
  public void inequalityFail() {
    expectFailure(whenTesting -> whenTesting.that(4).isNotEqualTo(4));
  }

  @Test
  public void equalityOfNulls() {
    assertThat((Integer) null).isEqualTo(null);
  }

  @Test
  public void equalityOfNullsFail_nullActual() {
    expectFailure(whenTesting -> whenTesting.that((Integer) null).isEqualTo(5));
  }

  @Test
  public void equalityOfNullsFail_nullExpected() {
    expectFailure(whenTesting -> whenTesting.that(5).isEqualTo(null));
  }

  @Test
  public void inequalityOfNulls() {
    assertThat(4).isNotEqualTo(null);
    assertThat((Integer) null).isNotEqualTo(4);
  }

  @Test
  public void inequalityOfNullsFail() {
    expectFailure(whenTesting -> whenTesting.that((Integer) null).isNotEqualTo(null));
  }

  @Test
  public void overflowOnPrimitives() {
    assertThat(Long.MIN_VALUE).isNotEqualTo(Integer.MIN_VALUE);
    assertThat(Long.MAX_VALUE).isNotEqualTo(Integer.MAX_VALUE);

    assertThat(Integer.MIN_VALUE).isNotEqualTo(Long.MIN_VALUE);
    assertThat(Integer.MAX_VALUE).isNotEqualTo(Long.MAX_VALUE);

    assertThat(Integer.MIN_VALUE).isEqualTo((long) Integer.MIN_VALUE);
    assertThat(Integer.MAX_VALUE).isEqualTo((long) Integer.MAX_VALUE);
  }

  @Test
  public void overflowOnPrimitives_shouldBeEqualAfterCast_min() {
    expectFailure(
        whenTesting -> whenTesting.that(Integer.MIN_VALUE).isNotEqualTo((long) Integer.MIN_VALUE));
  }

  @Test
  public void overflowOnPrimitives_shouldBeEqualAfterCast_max() {
    expectFailure(
        whenTesting -> whenTesting.that(Integer.MAX_VALUE).isNotEqualTo((long) Integer.MAX_VALUE));
  }

  @Test
  public void overflowBetweenIntegerAndLong_shouldBeDifferent_min() {
    expectFailure(whenTesting -> whenTesting.that(Integer.MIN_VALUE).isEqualTo(Long.MIN_VALUE));
  }

  @Test
  public void overflowBetweenIntegerAndLong_shouldBeDifferent_max() {
    expectFailure(whenTesting -> whenTesting.that(Integer.MAX_VALUE).isEqualTo(Long.MAX_VALUE));
  }

  @Test
  public void isWithinOf() {
    assertThat(20000).isWithin(0).of(20000);
    assertThat(20000).isWithin(1).of(20000);
    assertThat(20000).isWithin(10000).of(20000);
    assertThat(20000).isWithin(10000).of(30000);
    assertThat(Integer.MIN_VALUE).isWithin(1).of(Integer.MIN_VALUE + 1);
    assertThat(Integer.MAX_VALUE).isWithin(1).of(Integer.MAX_VALUE - 1);
    assertThat(Integer.MAX_VALUE / 2).isWithin(Integer.MAX_VALUE).of(-Integer.MAX_VALUE / 2);
    assertThat(-Integer.MAX_VALUE / 2).isWithin(Integer.MAX_VALUE).of(Integer.MAX_VALUE / 2);

    assertThatIsWithinFails(20000, 9999, 30000);
    assertThatIsWithinFails(20000, 10000, 30001);
    assertThatIsWithinFails(Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
    assertThatIsWithinFails(Integer.MAX_VALUE, 0, Integer.MIN_VALUE);
    assertThatIsWithinFails(Integer.MIN_VALUE, 1, Integer.MIN_VALUE + 2);
    assertThatIsWithinFails(Integer.MAX_VALUE, 1, Integer.MAX_VALUE - 2);
    // Don't fall for rollover
    assertThatIsWithinFails(Integer.MIN_VALUE, 1, Integer.MAX_VALUE);
    assertThatIsWithinFails(Integer.MAX_VALUE, 1, Integer.MIN_VALUE);
  }

  private static void assertThatIsWithinFails(int actual, int tolerance, int expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertThat(e).factKeys().containsExactly("expected", "but was", "outside tolerance").inOrder();
    assertThat(e).factValue("expected").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("but was").isEqualTo(formatNumericValue(actual));
    assertThat(e).factValue("outside tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  @Test
  public void isNotWithinOf() {
    assertThatIsNotWithinFails(20000, 0, 20000);
    assertThatIsNotWithinFails(20000, 1, 20000);
    assertThatIsNotWithinFails(20000, 10000, 20000);
    assertThatIsNotWithinFails(20000, 10000, 30000);
    assertThatIsNotWithinFails(Integer.MIN_VALUE, 1, Integer.MIN_VALUE + 1);
    assertThatIsNotWithinFails(Integer.MAX_VALUE, 1, Integer.MAX_VALUE - 1);
    assertThatIsNotWithinFails(Integer.MAX_VALUE / 2, Integer.MAX_VALUE, -Integer.MAX_VALUE / 2);
    assertThatIsNotWithinFails(-Integer.MAX_VALUE / 2, Integer.MAX_VALUE, Integer.MAX_VALUE / 2);

    assertThat(20000).isNotWithin(9999).of(30000);
    assertThat(20000).isNotWithin(10000).of(30001);
    assertThat(Integer.MIN_VALUE).isNotWithin(0).of(Integer.MAX_VALUE);
    assertThat(Integer.MAX_VALUE).isNotWithin(0).of(Integer.MIN_VALUE);
    assertThat(Integer.MIN_VALUE).isNotWithin(1).of(Integer.MIN_VALUE + 2);
    assertThat(Integer.MAX_VALUE).isNotWithin(1).of(Integer.MAX_VALUE - 2);
    // Don't fall for rollover
    assertThat(Integer.MIN_VALUE).isNotWithin(1).of(Integer.MAX_VALUE);
    assertThat(Integer.MAX_VALUE).isNotWithin(1).of(Integer.MIN_VALUE);
  }

  private static void assertThatIsNotWithinFails(int actual, int tolerance, int expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertThat(e).factValue("expected not to be").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("within tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  @Test
  public void isWithinNegativeTolerance() {
    isWithinNegativeToleranceFails(0, -10, 0);
    isWithinNegativeToleranceFails(0, -10, 0);
    isNotWithinNegativeToleranceFails(0, -10, 0);
    isNotWithinNegativeToleranceFails(0, -10, 0);
  }

  private static void isWithinNegativeToleranceFails(int actual, int tolerance, int expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertFailureKeys(
        e,
        "could not perform approximate-equality check because tolerance was negative",
        "expected",
        "was",
        "tolerance");
  }

  private static void isNotWithinNegativeToleranceFails(int actual, int tolerance, int expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertFailureKeys(
        e,
        "could not perform approximate-equality check because tolerance was negative",
        "expected",
        "was",
        "tolerance");
  }
}
