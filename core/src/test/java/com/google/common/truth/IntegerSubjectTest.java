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
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.truth.ExpectFailure.SimpleSubjectBuilderCallback;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
public class IntegerSubjectTest extends BaseSubjectTestCase {

  @Test
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
    expectFailureWhenTestingThat(0).isNotEqualTo(0L);
  }

  @Test
  public void equalityFail() {
    expectFailureWhenTestingThat(4).isEqualTo(5);
  }

  @Test
  public void inequalityFail() {
    expectFailureWhenTestingThat(4).isNotEqualTo(4);
  }

  @Test
  public void equalityOfNulls() {
    assertThat((Integer) null).isEqualTo(null);
  }

  @Test
  public void equalityOfNullsFail_nullActual() {
    expectFailureWhenTestingThat(null).isEqualTo(5);
  }

  @Test
  public void equalityOfNullsFail_nullExpected() {
    expectFailureWhenTestingThat(5).isEqualTo(null);
  }

  @Test
  public void inequalityOfNulls() {
    assertThat(4).isNotEqualTo(null);
    assertThat((Integer) null).isNotEqualTo(4);
  }

  @Test
  public void inequalityOfNullsFail() {
    expectFailureWhenTestingThat(null).isNotEqualTo(null);
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
    expectFailureWhenTestingThat(Integer.MIN_VALUE).isNotEqualTo((long) Integer.MIN_VALUE);
  }

  @Test
  public void overflowOnPrimitives_shouldBeEqualAfterCast_max() {
    expectFailureWhenTestingThat(Integer.MAX_VALUE).isNotEqualTo((long) Integer.MAX_VALUE);
  }

  @Test
  public void overflowBetweenIntegerAndLong_shouldBeDifferent_min() {
    expectFailureWhenTestingThat(Integer.MIN_VALUE).isEqualTo(Long.MIN_VALUE);
  }

  @Test
  public void overflowBetweenIntegerAndLong_shouldBeDifferent_max() {
    expectFailureWhenTestingThat(Integer.MAX_VALUE).isEqualTo(Long.MAX_VALUE);
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
    ExpectFailure.SimpleSubjectBuilderCallback<IntegerSubject, Integer> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<IntegerSubject, Integer>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<IntegerSubject, Integer> expect) {
            expect.that(actual).isWithin(tolerance).of(expected);
          }
        };
    AssertionError failure = expectFailure(callback);
    assertThat(failure)
        .factKeys()
        .containsExactly("expected", "but was", "outside tolerance")
        .inOrder();
    assertThat(failure).factValue("expected").isEqualTo(Integer.toString(expected));
    assertThat(failure).factValue("but was").isEqualTo(Integer.toString(actual));
    assertThat(failure).factValue("outside tolerance").isEqualTo(Integer.toString(tolerance));
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
    ExpectFailure.SimpleSubjectBuilderCallback<IntegerSubject, Integer> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<IntegerSubject, Integer>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<IntegerSubject, Integer> expect) {
            expect.that(actual).isNotWithin(tolerance).of(expected);
          }
        };
    AssertionError failure = expectFailure(callback);
    assertThat(failure).factValue("expected not to be").isEqualTo(Integer.toString(expected));
    assertThat(failure).factValue("within tolerance").isEqualTo(Integer.toString(tolerance));
  }

  @Test
  public void isWithinNegativeTolerance() {
    isWithinNegativeToleranceThrowsIAE(0, -10, 5);
    isWithinNegativeToleranceThrowsIAE(0, -10, 20);
    isNotWithinNegativeToleranceThrowsIAE(0, -10, 5);
    isNotWithinNegativeToleranceThrowsIAE(0, -10, 20);
  }

  private static void isWithinNegativeToleranceThrowsIAE(int actual, int tolerance, int expected) {
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
      int actual, int tolerance, int expected) {
    try {
      assertThat(actual).isNotWithin(tolerance).of(expected);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae)
          .hasMessageThat()
          .isEqualTo("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  private static final Subject.Factory<IntegerSubject, Integer> INTEGER_SUBJECT_FACTORY =
      new Subject.Factory<IntegerSubject, Integer>() {
        @Override
        public IntegerSubject createSubject(FailureMetadata metadata, Integer that) {
          return new IntegerSubject(metadata, that);
        }
      };

  @CanIgnoreReturnValue
  private static AssertionError expectFailure(
      SimpleSubjectBuilderCallback<IntegerSubject, Integer> callback) {
    return ExpectFailure.expectFailureAbout(INTEGER_SUBJECT_FACTORY, callback);
  }

  private IntegerSubject expectFailureWhenTestingThat(Integer actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
