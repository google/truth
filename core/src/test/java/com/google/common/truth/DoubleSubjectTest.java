/*
 * Copyright (c) 2014 Google, Inc.
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
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

import com.google.common.annotations.GwtIncompatible;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Double Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class DoubleSubjectTest {

  private static final double NEARLY_MAX = 1.7976931348623155E308;
  private static final double NEGATIVE_NEARLY_MAX = -1.7976931348623155E308;
  private static final double OVER_MIN = 9.9E-324;
  private static final double UNDER_NEGATIVE_MIN = -9.9E-324;
  private static final double GOLDEN = 1.23;
  private static final double OVER_GOLDEN = 1.2300000000000002;

  @Test
  @GwtIncompatible("Math.nextAfter")
  public void doubleConstants_matchNextAfter() {
    assertThat(Math.nextAfter(Double.MIN_VALUE, 1.0)).isEqualTo(OVER_MIN);
    assertThat(Math.nextAfter(1.23, POSITIVE_INFINITY)).isEqualTo(OVER_GOLDEN);
    assertThat(Math.nextAfter(Double.MAX_VALUE, 0.0)).isEqualTo(NEARLY_MAX);
    assertThat(Math.nextAfter(-1.0 * Double.MAX_VALUE, 0.0)).isEqualTo(NEGATIVE_NEARLY_MAX);
    assertThat(Math.nextAfter(-1.0 * Double.MIN_VALUE, -1.0)).isEqualTo(UNDER_NEGATIVE_MIN);
  }

  @Test
  public void j2clCornerCaseZero() {
    // GWT considers -0.0 to be equal to 0.0. But we've added a special workaround inside Truth.
    assertThatIsEqualToFails(-0.0, 0.0);
  }

  @Test
  @GwtIncompatible("GWT behavior difference")
  public void j2clCornerCaseDoubleVsFloat() {
    // Under GWT, 1.23f.toString() is different than 1.23d.toString(), so the message omits types.
    // TODO(b/35377736): Consider making Truth add the types manually.
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(1.23).isEqualTo(1.23f));
    assertFailureKeys(e, "expected", "an instance of", "but was", "an instance of");
  }

  @Test
  public void isWithinOf() {
    assertThat(2.0).isWithin(0.0).of(2.0);
    assertThat(2.0).isWithin(0.00001).of(2.0);
    assertThat(2.0).isWithin(1000.0).of(2.0);
    assertThat(2.0).isWithin(1.00001).of(3.0);
    assertThatIsWithinFails(2.0, 0.99999, 3.0);
    assertThatIsWithinFails(2.0, 1000.0, 1003.0);
    assertThatIsWithinFailsForNonFiniteExpected(2.0, 1000.0, POSITIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteExpected(2.0, 1000.0, NaN);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 1000.0, 2.0);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 1000.0, 2.0);
  }

  private static void assertThatIsWithinFails(double actual, double tolerance, double expected) {
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertThat(failure)
        .factKeys()
        .containsExactly("expected", "but was", "outside tolerance")
        .inOrder();
    assertThat(failure).factValue("expected").isEqualTo(formatNumericValue(expected));
    assertThat(failure).factValue("but was").isEqualTo(formatNumericValue(actual));
    assertThat(failure).factValue("outside tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  private static void assertThatIsWithinFailsForNonFiniteExpected(
      double actual, double tolerance, double expected) {
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertThat(failure)
        .factKeys()
        .containsExactly(
            "could not perform approximate-equality check because expected value is not finite",
            "expected",
            "was",
            "tolerance")
        .inOrder();
    assertThat(failure).factValue("expected").isEqualTo(formatNumericValue(expected));
    assertThat(failure).factValue("was").isEqualTo(formatNumericValue(actual));
    assertThat(failure).factValue("tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  private static void assertThatIsWithinFailsForNonFiniteActual(
      double actual, double tolerance, double expected) {
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertThat(failure)
        .factKeys()
        .containsExactly("expected a finite value near", "but was", "tolerance")
        .inOrder();
    assertThat(failure)
        .factValue("expected a finite value near")
        .isEqualTo(formatNumericValue(expected));
    assertThat(failure).factValue("but was").isEqualTo(formatNumericValue(actual));
    assertThat(failure).factValue("tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  @Test
  public void isNotWithinOf() {
    assertThatIsNotWithinFails(2.0, 0.0, 2.0);
    assertThatIsNotWithinFails(2.0, 0.00001, 2.0);
    assertThatIsNotWithinFails(2.0, 1000.0, 2.0);
    assertThatIsNotWithinFails(2.0, 1.00001, 3.0);
    assertThat(2.0).isNotWithin(0.99999).of(3.0);
    assertThat(2.0).isNotWithin(1000.0).of(1003.0);
    assertThatIsNotWithinFailsForNonFiniteExpected(2.0, 0.0, POSITIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteExpected(2.0, 0.0, NaN);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 1000.0, 2.0);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 1000.0, 2.0);
  }

  private static void assertThatIsNotWithinFails(double actual, double tolerance, double expected) {
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertThat(failure).factValue("expected not to be").isEqualTo(formatNumericValue(expected));
    assertThat(failure).factValue("within tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  private static void assertThatIsNotWithinFailsForNonFiniteExpected(
      double actual, double tolerance, double expected) {
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertThat(failure)
        .factKeys()
        .containsExactly(
            "could not perform approximate-equality check because expected value is not finite",
            "expected not to be",
            "was",
            "tolerance");
    assertThat(failure).factValue("expected not to be").isEqualTo(formatNumericValue(expected));
    assertThat(failure).factValue("was").isEqualTo(formatNumericValue(actual));
    assertThat(failure).factValue("tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  private static void assertThatIsNotWithinFailsForNonFiniteActual(
      double actual, double tolerance, double expected) {
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertThat(failure)
        .factValue("expected a finite value that is not near")
        .isEqualTo(formatNumericValue(expected));
    assertThat(failure).factValue("tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  @Test
  public void negativeTolerances() {
    isWithinNegativeToleranceThrows(-0.5);
    isNotWithinNegativeToleranceThrows(-0.5);

    // You know what's worse than zero? Negative zero.

    isWithinNegativeToleranceThrows(-0.0);
    isNotWithinNegativeToleranceThrows(-0.0);
  }

  private static void isWithinNegativeToleranceThrows(double tolerance) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(5.0).isWithin(tolerance).of(5.0));
    assertFailureKeys(
        e,
        "could not perform approximate-equality check because tolerance is negative",
        "expected",
        "was",
        "tolerance");
  }

  private static void isNotWithinNegativeToleranceThrows(double tolerance) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(5.0).isNotWithin(tolerance).of(5.0));
    assertFailureKeys(
        e,
        "could not perform approximate-equality check because tolerance is negative",
        "expected not to be",
        "was",
        "tolerance");
  }

  @Test
  public void nanTolerances() {
    {
      AssertionError e = expectFailure(whenTesting -> whenTesting.that(1.0).isWithin(NaN).of(1.0));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance is not finite",
          "expected",
          "was",
          "tolerance");
    }
    {
      AssertionError e =
          expectFailure(whenTesting -> whenTesting.that(1.0).isNotWithin(NaN).of(1.0));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance is not finite",
          "expected not to be",
          "was",
          "tolerance");
    }
  }

  @Test
  public void positiveInfinityTolerances() {
    {
      AssertionError e =
          expectFailure(whenTesting -> whenTesting.that(1.0).isWithin(POSITIVE_INFINITY).of(1.0));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance is not finite",
          "expected",
          "was",
          "tolerance");
    }
    {
      AssertionError e =
          expectFailure(
              whenTesting -> whenTesting.that(1.0).isNotWithin(POSITIVE_INFINITY).of(1.0));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance is not finite",
          "expected not to be",
          "was",
          "tolerance");
    }
  }

  @SuppressWarnings("FloatingPointAssertionWithinEpsilon") // test of a bogus call
  @Test
  public void negativeInfinityTolerances() {
    {
      AssertionError e =
          expectFailure(whenTesting -> whenTesting.that(1.0).isWithin(NEGATIVE_INFINITY).of(1.0));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance is not finite",
          "expected",
          "was",
          "tolerance");
    }
    {
      AssertionError e =
          expectFailure(
              whenTesting -> whenTesting.that(1.0).isNotWithin(NEGATIVE_INFINITY).of(1.0));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance is not finite",
          "expected not to be",
          "was",
          "tolerance");
    }
  }

  @Test
  public void isWithinOfZero() {
    assertThat(+0.0).isWithin(0.00001).of(+0.0);
    assertThat(+0.0).isWithin(0.00001).of(-0.0);
    assertThat(-0.0).isWithin(0.00001).of(+0.0);
    assertThat(-0.0).isWithin(0.00001).of(-0.0);

    assertThat(+0.0).isWithin(0.0).of(+0.0);
    assertThat(+0.0).isWithin(0.0).of(-0.0);
    assertThat(-0.0).isWithin(0.0).of(+0.0);
    assertThat(-0.0).isWithin(0.0).of(-0.0);
  }

  @Test
  public void isNotWithinOfZero() {
    assertThat(+0.0).isNotWithin(0.00001).of(+1.0);
    assertThat(+0.0).isNotWithin(0.00001).of(-1.0);
    assertThat(-0.0).isNotWithin(0.00001).of(+1.0);
    assertThat(-0.0).isNotWithin(0.00001).of(-1.0);

    assertThat(+1.0).isNotWithin(0.00001).of(+0.0);
    assertThat(+1.0).isNotWithin(0.00001).of(-0.0);
    assertThat(-1.0).isNotWithin(0.00001).of(+0.0);
    assertThat(-1.0).isNotWithin(0.00001).of(-0.0);

    assertThat(+1.0).isNotWithin(0.0).of(+0.0);
    assertThat(+1.0).isNotWithin(0.0).of(-0.0);
    assertThat(-1.0).isNotWithin(0.0).of(+0.0);
    assertThat(-1.0).isNotWithin(0.0).of(-0.0);

    assertThatIsNotWithinFails(-0.0, 0.0, 0.0);
  }

  @Test
  public void isWithinZeroTolerance() {
    double max = Double.MAX_VALUE;
    assertThat(max).isWithin(0.0).of(max);
    assertThat(NEARLY_MAX).isWithin(0.0).of(NEARLY_MAX);
    assertThatIsWithinFails(max, 0.0, NEARLY_MAX);
    assertThatIsWithinFails(NEARLY_MAX, 0.0, max);

    double negativeMax = -1.0 * Double.MAX_VALUE;
    assertThat(negativeMax).isWithin(0.0).of(negativeMax);
    assertThat(NEGATIVE_NEARLY_MAX).isWithin(0.0).of(NEGATIVE_NEARLY_MAX);
    assertThatIsWithinFails(negativeMax, 0.0, NEGATIVE_NEARLY_MAX);
    assertThatIsWithinFails(NEGATIVE_NEARLY_MAX, 0.0, negativeMax);

    double min = Double.MIN_VALUE;
    assertThat(min).isWithin(0.0).of(min);
    assertThat(OVER_MIN).isWithin(0.0).of(OVER_MIN);
    assertThatIsWithinFails(min, 0.0, OVER_MIN);
    assertThatIsWithinFails(OVER_MIN, 0.0, min);

    double negativeMin = -1.0 * Double.MIN_VALUE;
    assertThat(negativeMin).isWithin(0.0).of(negativeMin);
    assertThat(UNDER_NEGATIVE_MIN).isWithin(0.0).of(UNDER_NEGATIVE_MIN);
    assertThatIsWithinFails(negativeMin, 0.0, UNDER_NEGATIVE_MIN);
    assertThatIsWithinFails(UNDER_NEGATIVE_MIN, 0.0, negativeMin);
  }

  @Test
  public void isNotWithinZeroTolerance() {
    double max = Double.MAX_VALUE;
    assertThatIsNotWithinFails(max, 0.0, max);
    assertThatIsNotWithinFails(NEARLY_MAX, 0.0, NEARLY_MAX);
    assertThat(max).isNotWithin(0.0).of(NEARLY_MAX);
    assertThat(NEARLY_MAX).isNotWithin(0.0).of(max);

    double min = Double.MIN_VALUE;
    assertThatIsNotWithinFails(min, 0.0, min);
    assertThatIsNotWithinFails(OVER_MIN, 0.0, OVER_MIN);
    assertThat(min).isNotWithin(0.0).of(OVER_MIN);
    assertThat(OVER_MIN).isNotWithin(0.0).of(min);
  }

  @Test
  public void isWithinNonFinite() {
    assertThatIsWithinFailsForNonFiniteExpected(NaN, 0.00001, NaN);
    assertThatIsWithinFailsForNonFiniteExpected(NaN, 0.00001, POSITIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteExpected(NaN, 0.00001, NEGATIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 0.00001, +0.0);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 0.00001, -0.0);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 0.00001, +1.0);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 0.00001, -0.0);
    assertThatIsWithinFailsForNonFiniteExpected(POSITIVE_INFINITY, 0.00001, POSITIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteExpected(POSITIVE_INFINITY, 0.00001, NEGATIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001, +0.0);
    assertThatIsWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001, -0.0);
    assertThatIsWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001, +1.0);
    assertThatIsWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001, -0.0);
    assertThatIsWithinFailsForNonFiniteExpected(NEGATIVE_INFINITY, 0.00001, NEGATIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001, +0.0);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001, -0.0);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001, +1.0);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001, -0.0);
    assertThatIsWithinFailsForNonFiniteExpected(+1.0, 0.00001, NaN);
    assertThatIsWithinFailsForNonFiniteExpected(+1.0, 0.00001, POSITIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteExpected(+1.0, 0.00001, NEGATIVE_INFINITY);
  }

  @Test
  public void isNotWithinNonFinite() {
    assertThatIsNotWithinFailsForNonFiniteExpected(NaN, 0.00001, NaN);
    assertThatIsNotWithinFailsForNonFiniteExpected(NaN, 0.00001, POSITIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteExpected(NaN, 0.00001, NEGATIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 0.00001, +0.0);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 0.00001, -0.0);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 0.00001, +1.0);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 0.00001, -0.0);
    assertThatIsNotWithinFailsForNonFiniteExpected(POSITIVE_INFINITY, 0.00001, POSITIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteExpected(POSITIVE_INFINITY, 0.00001, NEGATIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001, +0.0);
    assertThatIsNotWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001, -0.0);
    assertThatIsNotWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001, +1.0);
    assertThatIsNotWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001, -0.0);
    assertThatIsNotWithinFailsForNonFiniteExpected(NEGATIVE_INFINITY, 0.00001, NEGATIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001, +0.0);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001, -0.0);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001, +1.0);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001, -0.0);
    assertThatIsNotWithinFailsForNonFiniteExpected(+1.0, 0.00001, NaN);
    assertThatIsNotWithinFailsForNonFiniteExpected(+1.0, 0.00001, POSITIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteExpected(+1.0, 0.00001, NEGATIVE_INFINITY);
  }

  @SuppressWarnings({"TruthSelfEquals", "PositiveInfinity", "NaN"})
  @Test
  public void isEqualTo() {
    assertThat(1.23).isEqualTo(1.23);
    assertThatIsEqualToFails(GOLDEN, OVER_GOLDEN);
    assertThat(POSITIVE_INFINITY).isEqualTo(POSITIVE_INFINITY);
    assertThat(NaN).isEqualTo(NaN);
    assertThat((Double) null).isEqualTo(null);
    assertThat(1.0).isEqualTo(1);
  }

  private static void assertThatIsEqualToFails(double actual, double expected) {
    expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expected));
  }

  @Test
  public void isNotEqualTo() {
    assertThatIsNotEqualToFails(1.23);
    assertThat(GOLDEN).isNotEqualTo(OVER_GOLDEN);
    assertThatIsNotEqualToFails(POSITIVE_INFINITY);
    assertThatIsNotEqualToFails(NaN);
    assertThat(-0.0).isNotEqualTo(0.0);
    assertThatIsNotEqualToFails(null);
    assertThat(1.23).isNotEqualTo(1.23f);
    assertThat(1.0).isNotEqualTo(2);
  }

  @SuppressWarnings("SelfAssertion")
  private static void assertThatIsNotEqualToFails(@Nullable Double value) {
    expectFailure(whenTesting -> whenTesting.that(value).isNotEqualTo(value));
  }

  @Test
  public void isZero() {
    assertThat(0.0).isZero();
    assertThat(-0.0).isZero();
    assertThatIsZeroFails(Double.MIN_VALUE);
    assertThatIsZeroFails(-1.23);
    assertThatIsZeroFails(POSITIVE_INFINITY);
    assertThatIsZeroFails(NaN);
    assertThatIsZeroFails(null);
  }

  private static void assertThatIsZeroFails(@Nullable Double value) {
    AssertionError failure = expectFailure(whenTesting -> whenTesting.that(value).isZero());
    assertThat(failure).factKeys().containsExactly("expected zero", "but was").inOrder();
  }

  @Test
  public void isNonZero() {
    assertThatIsNonZeroFails(0.0, "expected not to be zero");
    assertThatIsNonZeroFails(-0.0, "expected not to be zero");
    assertThat(Double.MIN_VALUE).isNonZero();
    assertThat(-1.23).isNonZero();
    assertThat(POSITIVE_INFINITY).isNonZero();
    assertThat(NaN).isNonZero();
    assertThatIsNonZeroFails(null, "expected a double other than zero");
  }

  private static void assertThatIsNonZeroFails(@Nullable Double value, String factKey) {
    AssertionError failure = expectFailure(whenTesting -> whenTesting.that(value).isNonZero());
    assertThat(failure).factKeys().containsExactly(factKey, "but was").inOrder();
  }

  @Test
  public void isPositiveInfinity() {
    assertThat(POSITIVE_INFINITY).isPositiveInfinity();
    assertThatIsPositiveInfinityFails(1.23);
    assertThatIsPositiveInfinityFails(NEGATIVE_INFINITY);
    assertThatIsPositiveInfinityFails(NaN);
    assertThatIsPositiveInfinityFails(null);
  }

  private static void assertThatIsPositiveInfinityFails(@Nullable Double value) {
    expectFailure(whenTesting -> whenTesting.that(value).isPositiveInfinity());
  }

  @Test
  public void isNegativeInfinity() {
    assertThat(NEGATIVE_INFINITY).isNegativeInfinity();
    assertThatIsNegativeInfinityFails(1.23);
    assertThatIsNegativeInfinityFails(POSITIVE_INFINITY);
    assertThatIsNegativeInfinityFails(NaN);
    assertThatIsNegativeInfinityFails(null);
  }

  private static void assertThatIsNegativeInfinityFails(@Nullable Double value) {
    expectFailure(whenTesting -> whenTesting.that(value).isNegativeInfinity());
  }

  @Test
  public void isNaN() {
    assertThat(NaN).isNaN();
    assertThatIsNaNFails(1.23);
    assertThatIsNaNFails(POSITIVE_INFINITY);
    assertThatIsNaNFails(NEGATIVE_INFINITY);
    assertThatIsNaNFails(null);
  }

  private static void assertThatIsNaNFails(@Nullable Double value) {
    expectFailure(whenTesting -> whenTesting.that(value).isNaN());
  }

  @Test
  public void isFinite() {
    assertThat(1.23).isFinite();
    assertThat(Double.MAX_VALUE).isFinite();
    assertThat(-1.0 * Double.MIN_VALUE).isFinite();
    assertThatIsFiniteFails(POSITIVE_INFINITY);
    assertThatIsFiniteFails(NEGATIVE_INFINITY);
    assertThatIsFiniteFails(NaN);
    assertThatIsFiniteFails(null);
  }

  private static void assertThatIsFiniteFails(@Nullable Double value) {
    AssertionError failure = expectFailure(whenTesting -> whenTesting.that(value).isFinite());
    assertThat(failure).factKeys().containsExactly("expected to be finite", "but was").inOrder();
  }

  @Test
  public void isNotNaN() {
    assertThat(1.23).isNotNaN();
    assertThat(Double.MAX_VALUE).isNotNaN();
    assertThat(-1.0 * Double.MIN_VALUE).isNotNaN();
    assertThat(POSITIVE_INFINITY).isNotNaN();
    assertThat(NEGATIVE_INFINITY).isNotNaN();
  }

  @Test
  public void isNotNaNIsNaN() {
    expectFailure(whenTesting -> whenTesting.that(NaN).isNotNaN());
  }

  @Test
  public void isNotNaNIsNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Double) null).isNotNaN());
    assertFailureKeys(e, "expected a double other than NaN", "but was");
  }

  @Test
  public void isGreaterThan_int_strictly() {
    expectFailure(whenTesting -> whenTesting.that(2.0).isGreaterThan(3));
  }

  @Test
  public void isGreaterThan_int() {
    expectFailure(whenTesting -> whenTesting.that(2.0).isGreaterThan(2));
    assertThat(2.0).isGreaterThan(1);
  }

  @Test
  public void isLessThan_int_strictly() {
    expectFailure(whenTesting -> whenTesting.that(2.0).isLessThan(1));
  }

  @Test
  public void isLessThan_int() {
    expectFailure(whenTesting -> whenTesting.that(2.0).isLessThan(2));
    assertThat(2.0).isLessThan(3);
  }

  @Test
  public void isAtLeast_int() {
    expectFailure(whenTesting -> whenTesting.that(2.0).isAtLeast(3));
    assertThat(2.0).isAtLeast(2);
    assertThat(2.0).isAtLeast(1);
  }

  @Test
  public void isAtMost_int() {
    expectFailure(whenTesting -> whenTesting.that(2.0).isAtMost(1));
    assertThat(2.0).isAtMost(2);
    assertThat(2.0).isAtMost(3);
  }
}
