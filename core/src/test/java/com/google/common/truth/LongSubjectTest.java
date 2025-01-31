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
import static com.google.common.truth.Fact.formatNumericValue;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.truth.ExpectFailure.SimpleSubjectBuilderCallback;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.junit.Test;
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
public class LongSubjectTest extends BaseSubjectTestCase {

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
    expectFailureWhenTestingThat(0L).isNotEqualTo(0);
  }

  @Test
  public void equalityFail() {
    expectFailureWhenTestingThat(4L).isEqualTo(5L);
  }

  @Test
  public void inequalityFail() {
    expectFailureWhenTestingThat(4L).isNotEqualTo(4L);
  }

  @Test
  public void equalityOfNulls() {
    assertThat((Long) null).isEqualTo(null);
  }

  @Test
  public void equalityOfNullsFail_nullActual() {
    expectFailureWhenTestingThat(null).isEqualTo(5L);
  }

  @Test
  public void equalityOfNullsFail_nullExpected() {
    expectFailureWhenTestingThat(5L).isEqualTo(null);
  }

  @Test
  public void inequalityOfNulls() {
    assertThat(4L).isNotEqualTo(null);
    assertThat((Integer) null).isNotEqualTo(4L);
  }

  @Test
  public void inequalityOfNullsFail() {
    expectFailureWhenTestingThat(null).isNotEqualTo(null);
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_long_long() {
    expectFailureWhenTestingThat(42L).isNotEqualTo(42L);
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_long_int() {
    expectFailureWhenTestingThat(42L).isNotEqualTo(42);
  }

  @Test
  public void isGreaterThan_int_strictly() {
    expectFailureWhenTestingThat(2L).isGreaterThan(3);
  }

  @Test
  public void isGreaterThan_int() {
    expectFailureWhenTestingThat(2L).isGreaterThan(2);
    assertThat(2L).isGreaterThan(1);
  }

  @Test
  public void isLessThan_int_strictly() {
    expectFailureWhenTestingThat(2L).isLessThan(1);
  }

  @Test
  public void isLessThan_int() {
    expectFailureWhenTestingThat(2L).isLessThan(2);
    assertThat(2L).isLessThan(3);
  }

  @Test
  public void isAtLeast_int() {
    expectFailureWhenTestingThat(2L).isAtLeast(3);
    assertThat(2L).isAtLeast(2);
    assertThat(2L).isAtLeast(1);
  }

  @Test
  public void isAtMost_int() {
    expectFailureWhenTestingThat(2L).isAtMost(1);
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
    ExpectFailure.SimpleSubjectBuilderCallback<LongSubject, Long> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<LongSubject, Long>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<LongSubject, Long> expect) {
            expect.that(actual).isWithin(tolerance).of(expected);
          }
        };
    AssertionError failure = expectFailure(callback);
    assertThat(failure)
        .factKeys()
        .containsExactly("expected", "but was", "outside tolerance")
        .inOrder();
    assertThat(failure).factValue("expected").isEqualTo(formatNumericValue(expected));
    assertThat(failure).factValue("but was").isEqualTo(formatNumericValue(actual));
    assertThat(failure).factValue("outside tolerance").isEqualTo(formatNumericValue(tolerance));
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
    ExpectFailure.SimpleSubjectBuilderCallback<LongSubject, Long> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<LongSubject, Long>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<LongSubject, Long> expect) {
            expect.that(actual).isNotWithin(tolerance).of(expected);
          }
        };
    AssertionError failure = expectFailure(callback);
    assertThat(failure).factValue("expected not to be").isEqualTo(formatNumericValue(expected));
    assertThat(failure).factValue("within tolerance").isEqualTo(formatNumericValue(tolerance));
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
    isWithinNegativeToleranceThrowsIAE(0L, -10, 5);
    isWithinNegativeToleranceThrowsIAE(0L, -10, 20);
    isNotWithinNegativeToleranceThrowsIAE(0L, -10, 5);
    isNotWithinNegativeToleranceThrowsIAE(0L, -10, 20);
  }

  private static void isWithinNegativeToleranceThrowsIAE(
      long actual, long tolerance, long expected) {
    try {
      assertThat(actual).isWithin(tolerance).of(expected);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae)
          .hasMessageThat()
          .isEqualTo("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  private static void isNotWithinNegativeToleranceThrowsIAE(
      long actual, long tolerance, long expected) {
    try {
      assertThat(actual).isNotWithin(tolerance).of(expected);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae)
          .hasMessageThat()
          .isEqualTo("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  private static final Subject.Factory<LongSubject, Long> LONG_SUBJECT_FACTORY =
      new Subject.Factory<LongSubject, Long>() {
        @Override
        public LongSubject createSubject(FailureMetadata metadata, Long that) {
          return new LongSubject(metadata, that);
        }
      };

  @CanIgnoreReturnValue
  private static AssertionError expectFailure(
      SimpleSubjectBuilderCallback<LongSubject, Long> callback) {
    return ExpectFailure.expectFailureAbout(LONG_SUBJECT_FACTORY, callback);
  }

  private LongSubject expectFailureWhenTestingThat(Long actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
