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
 * Tests for Long Subjects.
 */
@RunWith(JUnit4.class)
public class LongSubjectTest {

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void simpleEquality() {
    assertThat(4L).isEqualTo(4L);
  }

  @Test
  public void simpleInequality() {
    assertThat(4L).isNotEqualTo(5L);
  }

  @Test
  public void equalityWithInts() {
    assertThat(0L).isEqualTo(0);
    expectFailure(whenTesting -> whenTesting.that(0L).isNotEqualTo(0));
  }

  @Test
  public void equalityFail() {
    expectFailure(whenTesting -> whenTesting.that(4L).isEqualTo(5L));
  }

  @SuppressWarnings("SelfAssertion")
  @Test
  public void inequalityFail() {
    expectFailure(whenTesting -> whenTesting.that(4L).isNotEqualTo(4L));
  }

  @Test
  public void equalityOfNulls() {
    assertThat((Long) null).isEqualTo(null);
  }

  @Test
  public void equalityOfNullsFail_nullActual() {
    expectFailure(whenTesting -> whenTesting.that((Long) null).isEqualTo(5L));
  }

  @Test
  public void equalityOfNullsFail_nullExpected() {
    expectFailure(whenTesting -> whenTesting.that(5L).isEqualTo(null));
  }

  @Test
  public void inequalityOfNulls() {
    assertThat(4L).isNotEqualTo(null);
    assertThat((Integer) null).isNotEqualTo(4L);
  }

  @Test
  public void inequalityOfNullsFail() {
    expectFailure(whenTesting -> whenTesting.that((Long) null).isNotEqualTo(null));
  }

  @SuppressWarnings("SelfAssertion")
  @Test
  public void numericTypeWithSameValue_shouldBeEqual_long_long() {
    expectFailure(whenTesting -> whenTesting.that(42L).isNotEqualTo(42L));
  }

  @Test
  public void numericTypeWithSameValue_shouldBeEqual_long_int() {
    expectFailure(whenTesting -> whenTesting.that(42L).isNotEqualTo(42));
  }

  @Test
  public void isGreaterThan_int_strictly() {
    expectFailure(whenTesting -> whenTesting.that(2L).isGreaterThan(3));
  }

  @Test
  public void isGreaterThan_int() {
    expectFailure(whenTesting -> whenTesting.that(2L).isGreaterThan(2));
    assertThat(2L).isGreaterThan(1);
  }

  @Test
  public void isLessThan_int_strictly() {
    expectFailure(whenTesting -> whenTesting.that(2L).isLessThan(1));
  }

  @Test
  public void isLessThan_int() {
    expectFailure(whenTesting -> whenTesting.that(2L).isLessThan(2));
    assertThat(2L).isLessThan(3);
  }

  @Test
  public void isAtLeast_int() {
    expectFailure(whenTesting -> whenTesting.that(2L).isAtLeast(3));
    assertThat(2L).isAtLeast(2);
    assertThat(2L).isAtLeast(1);
  }

  @Test
  public void isAtMost_int() {
    expectFailure(whenTesting -> whenTesting.that(2L).isAtMost(1));
    assertThat(2L).isAtMost(2);
    assertThat(2L).isAtMost(3);
  }

  @Test
  public void isWithinOf() {
    assertThat(20000L).isWithin(0L).of(20000L);
    assertThat(20000L).isWithin(1L).of(20000L);
    assertThat(20000L).isWithin(10000L).of(20000L);
    assertThat(20000L).isWithin(10000L).of(30000L);
    assertThat(Long.MIN_VALUE).isWithin(1L).of(Long.MIN_VALUE + 1);
    assertThat(Long.MAX_VALUE).isWithin(1L).of(Long.MAX_VALUE - 1);
    assertThat(Long.MAX_VALUE / 2).isWithin(Long.MAX_VALUE).of(-Long.MAX_VALUE / 2);
    assertThat(-Long.MAX_VALUE / 2).isWithin(Long.MAX_VALUE).of(Long.MAX_VALUE / 2);

    assertThatIsWithinFails(20000L, 9999L, 30000L);
    assertThatIsWithinFails(20000L, 10000L, 30001L);
    assertThatIsWithinFails(Long.MIN_VALUE, 0L, Long.MAX_VALUE);
    assertThatIsWithinFails(Long.MAX_VALUE, 0L, Long.MIN_VALUE);
    assertThatIsWithinFails(Long.MIN_VALUE, 1L, Long.MIN_VALUE + 2);
    assertThatIsWithinFails(Long.MAX_VALUE, 1L, Long.MAX_VALUE - 2);
    // Don't fall for rollover
    assertThatIsWithinFails(Long.MIN_VALUE, 1L, Long.MAX_VALUE);
    assertThatIsWithinFails(Long.MAX_VALUE, 1L, Long.MIN_VALUE);
  }

  private static void assertThatIsWithinFails(long actual, long tolerance, long expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertThat(e).factKeys().containsExactly("expected", "but was", "outside tolerance").inOrder();
    assertThat(e).factValue("expected").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("but was").isEqualTo(formatNumericValue(actual));
    assertThat(e).factValue("outside tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  @Test
  public void isNotWithinOf() {
    assertThatIsNotWithinFails(20000L, 0L, 20000L);
    assertThatIsNotWithinFails(20000L, 1L, 20000L);
    assertThatIsNotWithinFails(20000L, 10000L, 20000L);
    assertThatIsNotWithinFails(20000L, 10000L, 30000L);
    assertThatIsNotWithinFails(Long.MIN_VALUE, 1L, Long.MIN_VALUE + 1);
    assertThatIsNotWithinFails(Long.MAX_VALUE, 1L, Long.MAX_VALUE - 1);
    assertThatIsNotWithinFails(Long.MAX_VALUE / 2, Long.MAX_VALUE, -Long.MAX_VALUE / 2);
    assertThatIsNotWithinFails(-Long.MAX_VALUE / 2, Long.MAX_VALUE, Long.MAX_VALUE / 2);

    assertThat(20000L).isNotWithin(9999L).of(30000L);
    assertThat(20000L).isNotWithin(10000L).of(30001L);
    assertThat(Long.MIN_VALUE).isNotWithin(0L).of(Long.MAX_VALUE);
    assertThat(Long.MAX_VALUE).isNotWithin(0L).of(Long.MIN_VALUE);
    assertThat(Long.MIN_VALUE).isNotWithin(1L).of(Long.MIN_VALUE + 2);
    assertThat(Long.MAX_VALUE).isNotWithin(1L).of(Long.MAX_VALUE - 2);
    // Don't fall for rollover
    assertThat(Long.MIN_VALUE).isNotWithin(1L).of(Long.MAX_VALUE);
    assertThat(Long.MAX_VALUE).isNotWithin(1L).of(Long.MIN_VALUE);
  }

  private static void assertThatIsNotWithinFails(long actual, long tolerance, long expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertThat(e).factValue("expected not to be").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("within tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  @Test
  public void isWithinIntegers() {
    assertThat(20000L).isWithin(0).of(20000);
    assertThat(20000L).isWithin(1).of(20000);
    assertThat(20000L).isWithin(10000).of(20000);
    assertThat(20000L).isWithin(10000).of(30000);

    assertThat(20000L).isNotWithin(0).of(200000);
    assertThat(20000L).isNotWithin(1).of(200000);
    assertThat(20000L).isNotWithin(10000).of(200000);
    assertThat(20000L).isNotWithin(10000).of(300000);
  }

  @Test
  public void isWithinNegativeTolerance() {
    isWithinNegativeToleranceThrows(0L, -10, 0L);
    isWithinNegativeToleranceThrows(0L, -10, 0L);
    isNotWithinNegativeToleranceThrows(0L, -10, 0L);
    isNotWithinNegativeToleranceThrows(0L, -10, 0L);
  }

  private static void isWithinNegativeToleranceThrows(long actual, long tolerance, long expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertFailureKeys(
        e,
        "could not perform approximate-equality check because tolerance was negative",
        "expected",
        "was",
        "tolerance");
  }

  private static void isNotWithinNegativeToleranceThrows(
      long actual, long tolerance, long expected) {
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
