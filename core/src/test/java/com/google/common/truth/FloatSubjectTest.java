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
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.NaN;
import static java.lang.Float.POSITIVE_INFINITY;

import com.google.common.annotations.GwtIncompatible;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Float Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class FloatSubjectTest {
  private static final float NEARLY_MAX = 3.4028233E38f;
  private static final float NEGATIVE_NEARLY_MAX = -3.4028233E38f;
  private static final float JUST_OVER_MIN = 2.8E-45f;
  private static final float JUST_UNDER_NEGATIVE_MIN = -2.8E-45f;
  private static final float GOLDEN = 1.23f;
  private static final float JUST_OVER_GOLDEN = 1.2300001f;

  @Test
  @GwtIncompatible("Math.nextAfter")
  public void floatConstants_matchNextAfter() {
    assertThat(Math.nextAfter(Float.MAX_VALUE, 0.0f)).isEqualTo(NEARLY_MAX);
    assertThat(Math.nextAfter(-1.0f * Float.MAX_VALUE, 0.0f)).isEqualTo(NEGATIVE_NEARLY_MAX);
    assertThat(Math.nextAfter(Float.MIN_VALUE, 1.0f)).isEqualTo(JUST_OVER_MIN);
    assertThat(Math.nextAfter(-1.0f * Float.MIN_VALUE, -1.0f)).isEqualTo(JUST_UNDER_NEGATIVE_MIN);
    assertThat(1.23f).isEqualTo(GOLDEN);
    assertThat(Math.nextAfter(1.23f, POSITIVE_INFINITY)).isEqualTo(JUST_OVER_GOLDEN);
  }

  @Test
  public void j2clCornerCaseZero() {
    // GWT considers -0.0 to be equal to 0.0. But we've added a special workaround inside Truth.
    assertThatIsEqualToFails(-0.0f, 0.0f);
  }

  @Test
  @GwtIncompatible("GWT behavior difference")
  public void j2clCornerCaseDoubleVsFloat() {
    // Under GWT, 1.23f.toString() is different than 1.23d.toString(), so the message omits types.
    // TODO(b/35377736): Consider making Truth add the types manually.
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(1.23f).isEqualTo(1.23));
    assertFailureKeys(e, "expected", "an instance of", "but was", "an instance of");
  }

  @Test
  public void isWithinOf() {
    assertThat(2.0f).isWithin(0.0f).of(2.0f);
    assertThat(2.0f).isWithin(0.00001f).of(2.0f);
    assertThat(2.0f).isWithin(1000.0f).of(2.0f);
    assertThat(2.0f).isWithin(1.00001f).of(3.0f);
    assertThatIsWithinFails(2.0f, 0.99999f, 3.0f);
    assertThatIsWithinFails(2.0f, 1000.0f, 1003.0f);
    assertThatIsWithinFailsForNonFiniteExpected(2.0f, 1000.0f, POSITIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteExpected(2.0f, 1000.0f, NaN);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 1000.0f, 2.0f);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 1000.0f, 2.0f);
  }

  private static void assertThatIsWithinFails(float actual, float tolerance, float expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertThat(e).factKeys().containsExactly("expected", "but was", "outside tolerance").inOrder();
    assertThat(e).factValue("expected").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("but was").isEqualTo(formatNumericValue(actual));
    assertThat(e).factValue("outside tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  private static void assertThatIsWithinFailsForNonFiniteExpected(
      float actual, float tolerance, float expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertThat(e)
        .factKeys()
        .containsExactly(
            "could not perform approximate-equality check because expected value was not finite",
            "expected",
            "was",
            "tolerance")
        .inOrder();
    assertThat(e).factValue("expected").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("was").isEqualTo(formatNumericValue(actual));
    assertThat(e).factValue("tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  private static void assertThatIsWithinFailsForNonFiniteActual(
      float actual, float tolerance, float expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isWithin(tolerance).of(expected));
    assertThat(e)
        .factKeys()
        .containsExactly("expected a finite value near", "but was", "tolerance")
        .inOrder();
    assertThat(e).factValue("expected a finite value near").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("but was").isEqualTo(formatNumericValue(actual));
    assertThat(e).factValue("tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  @Test
  public void isNotWithinOf() {
    assertThatIsNotWithinFails(2.0f, 0.0f, 2.0f);
    assertThatIsNotWithinFails(2.0f, 0.00001f, 2.0f);
    assertThatIsNotWithinFails(2.0f, 1000.0f, 2.0f);
    assertThatIsNotWithinFails(2.0f, 1.00001f, 3.0f);
    assertThat(2.0f).isNotWithin(0.99999f).of(3.0f);
    assertThat(2.0f).isNotWithin(1000.0f).of(1003.0f);
    assertThatIsNotWithinFailsForNonFiniteExpected(2.0f, 0.0f, POSITIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteExpected(2.0f, 0.0f, NaN);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 1000.0f, 2.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 1000.0f, 2.0f);
  }

  private static void assertThatIsNotWithinFails(float actual, float tolerance, float expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertThat(e).factValue("expected not to be").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("within tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  private static void assertThatIsNotWithinFailsForNonFiniteExpected(
      float actual, float tolerance, float expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertThat(e)
        .factKeys()
        .containsExactly(
            "could not perform approximate-equality check because expected value was not finite",
            "expected not to be",
            "was",
            "tolerance");
    assertThat(e).factValue("expected not to be").isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("was").isEqualTo(formatNumericValue(actual));
    assertThat(e).factValue("tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  private static void assertThatIsNotWithinFailsForNonFiniteActual(
      float actual, float tolerance, float expected) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotWithin(tolerance).of(expected));
    assertThat(e)
        .factValue("expected a finite value that is not near")
        .isEqualTo(formatNumericValue(expected));
    assertThat(e).factValue("tolerance").isEqualTo(formatNumericValue(tolerance));
  }

  @Test
  public void negativeTolerances() {
    isWithinNegativeToleranceThrows(-0.5f);
    isNotWithinNegativeToleranceThrows(-0.5f);

    // You know what's worse than zero? Negative zero.

    isWithinNegativeToleranceThrows(-0.0f);
    isNotWithinNegativeToleranceThrows(-0.0f);
  }

  private static void isWithinNegativeToleranceThrows(float tolerance) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(5.0f).isWithin(tolerance).of(5.0f));
    assertFailureKeys(
        e,
        "could not perform approximate-equality check because tolerance was negative",
        "expected",
        "was",
        "tolerance");
  }

  private static void isNotWithinNegativeToleranceThrows(float tolerance) {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(5.0f).isNotWithin(tolerance).of(5.0f));
    assertFailureKeys(
        e,
        "could not perform approximate-equality check because tolerance was negative",
        "expected not to be",
        "was",
        "tolerance");
  }

  @Test
  public void nanTolerances() {
    {
      AssertionError e =
          expectFailure(whenTesting -> whenTesting.that(1.0f).isWithin(NaN).of(1.0f));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance was not finite",
          "expected",
          "was",
          "tolerance");
    }
    {
      AssertionError e =
          expectFailure(whenTesting -> whenTesting.that(1.0f).isNotWithin(NaN).of(1.0f));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance was not finite",
          "expected not to be",
          "was",
          "tolerance");
    }
  }

  @Test
  public void positiveInfinityTolerances() {
    {
      AssertionError e =
          expectFailure(whenTesting -> whenTesting.that(1.0f).isWithin(POSITIVE_INFINITY).of(1.0f));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance was not finite",
          "expected",
          "was",
          "tolerance");
    }
    {
      AssertionError e =
          expectFailure(
              whenTesting -> whenTesting.that(1.0f).isNotWithin(POSITIVE_INFINITY).of(1.0f));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance was not finite",
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
          expectFailure(whenTesting -> whenTesting.that(1.0f).isWithin(NEGATIVE_INFINITY).of(1.0f));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance was not finite",
          "expected",
          "was",
          "tolerance");
    }
    {
      AssertionError e =
          expectFailure(
              whenTesting -> whenTesting.that(1.0f).isNotWithin(NEGATIVE_INFINITY).of(1.0f));
      assertFailureKeys(
          e,
          "could not perform approximate-equality check because tolerance was not finite",
          "expected not to be",
          "was",
          "tolerance");
    }
  }

  @Test
  public void isWithinOfZero() {
    assertThat(+0.0f).isWithin(0.00001f).of(+0.0f);
    assertThat(+0.0f).isWithin(0.00001f).of(-0.0f);
    assertThat(-0.0f).isWithin(0.00001f).of(+0.0f);
    assertThat(-0.0f).isWithin(0.00001f).of(-0.0f);

    assertThat(+0.0f).isWithin(0.0f).of(+0.0f);
    assertThat(+0.0f).isWithin(0.0f).of(-0.0f);
    assertThat(-0.0f).isWithin(0.0f).of(+0.0f);
    assertThat(-0.0f).isWithin(0.0f).of(-0.0f);
  }

  @Test
  public void isNotWithinOfZero() {
    assertThat(+0.0f).isNotWithin(0.00001f).of(+1.0f);
    assertThat(+0.0f).isNotWithin(0.00001f).of(-1.0f);
    assertThat(-0.0f).isNotWithin(0.00001f).of(+1.0f);
    assertThat(-0.0f).isNotWithin(0.00001f).of(-1.0f);

    assertThat(+1.0f).isNotWithin(0.00001f).of(+0.0f);
    assertThat(+1.0f).isNotWithin(0.00001f).of(-0.0f);
    assertThat(-1.0f).isNotWithin(0.00001f).of(+0.0f);
    assertThat(-1.0f).isNotWithin(0.00001f).of(-0.0f);

    assertThat(+1.0f).isNotWithin(0.0f).of(+0.0f);
    assertThat(+1.0f).isNotWithin(0.0f).of(-0.0f);
    assertThat(-1.0f).isNotWithin(0.0f).of(+0.0f);
    assertThat(-1.0f).isNotWithin(0.0f).of(-0.0f);

    assertThatIsNotWithinFails(-0.0f, 0.0f, 0.0f);
  }

  @Test
  public void isWithinZeroTolerance() {
    float max = Float.MAX_VALUE;
    assertThat(max).isWithin(0.0f).of(max);
    assertThat(NEARLY_MAX).isWithin(0.0f).of(NEARLY_MAX);
    assertThatIsWithinFails(max, 0.0f, NEARLY_MAX);
    assertThatIsWithinFails(NEARLY_MAX, 0.0f, max);

    float negativeMax = -1.0f * Float.MAX_VALUE;
    assertThat(negativeMax).isWithin(0.0f).of(negativeMax);
    assertThat(NEGATIVE_NEARLY_MAX).isWithin(0.0f).of(NEGATIVE_NEARLY_MAX);
    assertThatIsWithinFails(negativeMax, 0.0f, NEGATIVE_NEARLY_MAX);
    assertThatIsWithinFails(NEGATIVE_NEARLY_MAX, 0.0f, negativeMax);

    float min = Float.MIN_VALUE;
    assertThat(min).isWithin(0.0f).of(min);
    assertThat(JUST_OVER_MIN).isWithin(0.0f).of(JUST_OVER_MIN);
    assertThatIsWithinFails(min, 0.0f, JUST_OVER_MIN);
    assertThatIsWithinFails(JUST_OVER_MIN, 0.0f, min);

    float negativeMin = -1.0f * Float.MIN_VALUE;
    assertThat(negativeMin).isWithin(0.0f).of(negativeMin);
    assertThat(JUST_UNDER_NEGATIVE_MIN).isWithin(0.0f).of(JUST_UNDER_NEGATIVE_MIN);
    assertThatIsWithinFails(negativeMin, 0.0f, JUST_UNDER_NEGATIVE_MIN);
    assertThatIsWithinFails(JUST_UNDER_NEGATIVE_MIN, 0.0f, negativeMin);
  }

  @Test
  public void isNotWithinZeroTolerance() {
    float max = Float.MAX_VALUE;
    assertThatIsNotWithinFails(max, 0.0f, max);
    assertThatIsNotWithinFails(NEARLY_MAX, 0.0f, NEARLY_MAX);
    assertThat(max).isNotWithin(0.0f).of(NEARLY_MAX);
    assertThat(NEARLY_MAX).isNotWithin(0.0f).of(max);

    float min = Float.MIN_VALUE;
    assertThatIsNotWithinFails(min, 0.0f, min);
    assertThatIsNotWithinFails(JUST_OVER_MIN, 0.0f, JUST_OVER_MIN);
    assertThat(min).isNotWithin(0.0f).of(JUST_OVER_MIN);
    assertThat(JUST_OVER_MIN).isNotWithin(0.0f).of(min);
  }

  @Test
  public void isWithinNonFinite() {
    assertThatIsWithinFailsForNonFiniteExpected(NaN, 0.00001f, NaN);
    assertThatIsWithinFailsForNonFiniteExpected(NaN, 0.00001f, POSITIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteExpected(NaN, 0.00001f, NEGATIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 0.00001f, +0.0f);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 0.00001f, -0.0f);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 0.00001f, +1.0f);
    assertThatIsWithinFailsForNonFiniteActual(NaN, 0.00001f, -0.0f);
    assertThatIsWithinFailsForNonFiniteExpected(POSITIVE_INFINITY, 0.00001f, POSITIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteExpected(POSITIVE_INFINITY, 0.00001f, NEGATIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001f, +0.0f);
    assertThatIsWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001f, +1.0f);
    assertThatIsWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsWithinFailsForNonFiniteExpected(NEGATIVE_INFINITY, 0.00001f, NEGATIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001f, +0.0f);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001f, +1.0f);
    assertThatIsWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsWithinFailsForNonFiniteExpected(+1.0f, 0.00001f, NaN);
    assertThatIsWithinFailsForNonFiniteExpected(+1.0f, 0.00001f, POSITIVE_INFINITY);
    assertThatIsWithinFailsForNonFiniteExpected(+1.0f, 0.00001f, NEGATIVE_INFINITY);
  }

  @Test
  public void isNotWithinNonFinite() {
    assertThatIsNotWithinFailsForNonFiniteExpected(NaN, 0.00001f, NaN);
    assertThatIsNotWithinFailsForNonFiniteExpected(NaN, 0.00001f, POSITIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteExpected(NaN, 0.00001f, NEGATIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 0.00001f, +0.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 0.00001f, -0.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 0.00001f, +1.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(NaN, 0.00001f, -0.0f);
    assertThatIsNotWithinFailsForNonFiniteExpected(POSITIVE_INFINITY, 0.00001f, POSITIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteExpected(POSITIVE_INFINITY, 0.00001f, NEGATIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001f, +0.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001f, +1.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(POSITIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsNotWithinFailsForNonFiniteExpected(NEGATIVE_INFINITY, 0.00001f, NEGATIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001f, +0.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001f, +1.0f);
    assertThatIsNotWithinFailsForNonFiniteActual(NEGATIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsNotWithinFailsForNonFiniteExpected(+1.0f, 0.00001f, NaN);
    assertThatIsNotWithinFailsForNonFiniteExpected(+1.0f, 0.00001f, POSITIVE_INFINITY);
    assertThatIsNotWithinFailsForNonFiniteExpected(+1.0f, 0.00001f, NEGATIVE_INFINITY);
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo() {
    assertThat(GOLDEN).isEqualTo(GOLDEN);
    assertThatIsEqualToFails(GOLDEN, JUST_OVER_GOLDEN);
    assertThat(POSITIVE_INFINITY).isEqualTo(POSITIVE_INFINITY);
    assertThat(NaN).isEqualTo(NaN);
    assertThat((Float) null).isEqualTo(null);
    assertThat(1.0f).isEqualTo(1);
  }

  private static void assertThatIsEqualToFails(float actual, float expected) {
    expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expected));
  }

  @Test
  public void isNotEqualTo() {
    assertThatIsNotEqualToFails(GOLDEN);
    assertThat(GOLDEN).isNotEqualTo(JUST_OVER_GOLDEN);
    assertThatIsNotEqualToFails(POSITIVE_INFINITY);
    assertThatIsNotEqualToFails(NaN);
    assertThat(-0.0f).isNotEqualTo(0.0f);
    assertThatIsNotEqualToFails(null);
    assertThat(1.23f).isNotEqualTo(1.23);
    assertThat(1.0f).isNotEqualTo(2);
  }

  @SuppressWarnings("SelfAssertion")
  private static void assertThatIsNotEqualToFails(@Nullable Float value) {
    expectFailure(whenTesting -> whenTesting.that(value).isNotEqualTo(value));
  }

  @Test
  public void isZero() {
    assertThat(0.0f).isZero();
    assertThat(-0.0f).isZero();
    assertThatIsZeroFails(Float.MIN_VALUE);
    assertThatIsZeroFails(-1.23f);
    assertThatIsZeroFails(POSITIVE_INFINITY);
    assertThatIsZeroFails(NaN);
    assertThatIsZeroFails(null);
  }

  private static void assertThatIsZeroFails(@Nullable Float value) {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(value).isZero());
    assertThat(e).factKeys().containsExactly("expected zero", "but was").inOrder();
  }

  @Test
  public void isNonZero() {
    assertThatIsNonZeroFails(0.0f, "expected not to be zero");
    assertThatIsNonZeroFails(-0.0f, "expected not to be zero");
    assertThat(Float.MIN_VALUE).isNonZero();
    assertThat(-1.23f).isNonZero();
    assertThat(POSITIVE_INFINITY).isNonZero();
    assertThat(NaN).isNonZero();
    assertThatIsNonZeroFails(null, "expected a float other than zero");
  }

  private static void assertThatIsNonZeroFails(@Nullable Float value, String factKey) {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(value).isNonZero());
    assertThat(e).factKeys().containsExactly(factKey, "but was").inOrder();
  }

  @Test
  public void isPositiveInfinity() {
    assertThat(POSITIVE_INFINITY).isPositiveInfinity();
    assertThatIsPositiveInfinityFails(1.23f);
    assertThatIsPositiveInfinityFails(NEGATIVE_INFINITY);
    assertThatIsPositiveInfinityFails(NaN);
    assertThatIsPositiveInfinityFails(null);
  }

  private static void assertThatIsPositiveInfinityFails(@Nullable Float value) {
    expectFailure(whenTesting -> whenTesting.that(value).isPositiveInfinity());
  }

  @Test
  public void isNegativeInfinity() {
    assertThat(NEGATIVE_INFINITY).isNegativeInfinity();
    assertThatIsNegativeInfinityFails(1.23f);
    assertThatIsNegativeInfinityFails(POSITIVE_INFINITY);
    assertThatIsNegativeInfinityFails(NaN);
    assertThatIsNegativeInfinityFails(null);
  }

  private static void assertThatIsNegativeInfinityFails(@Nullable Float value) {
    expectFailure(whenTesting -> whenTesting.that(value).isNegativeInfinity());
  }

  @Test
  public void isNaN() {
    assertThat(NaN).isNaN();
    assertThatIsNaNFails(1.23f);
    assertThatIsNaNFails(POSITIVE_INFINITY);
    assertThatIsNaNFails(NEGATIVE_INFINITY);
    assertThatIsNaNFails(null);
  }

  private static void assertThatIsNaNFails(@Nullable Float value) {
    expectFailure(whenTesting -> whenTesting.that(value).isNaN());
  }

  @Test
  public void isFinite() {
    assertThat(1.23f).isFinite();
    assertThat(Float.MAX_VALUE).isFinite();
    assertThat(-1.0 * Float.MIN_VALUE).isFinite();
    assertThatIsFiniteFails(POSITIVE_INFINITY);
    assertThatIsFiniteFails(NEGATIVE_INFINITY);
    assertThatIsFiniteFails(NaN);
    assertThatIsFiniteFails(null);
  }

  private static void assertThatIsFiniteFails(@Nullable Float value) {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(value).isFinite());
    assertThat(e).factKeys().containsExactly("expected to be finite", "but was").inOrder();
  }

  @Test
  public void isNotNaN() {
    assertThat(1.23f).isNotNaN();
    assertThat(Float.MAX_VALUE).isNotNaN();
    assertThat(-1.0 * Float.MIN_VALUE).isNotNaN();
    assertThat(POSITIVE_INFINITY).isNotNaN();
    assertThat(NEGATIVE_INFINITY).isNotNaN();
  }

  @Test
  public void isNotNaNIsNaN() {
    expectFailure(whenTesting -> whenTesting.that(NaN).isNotNaN());
  }

  @Test
  public void isNotNaNIsNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Float) null).isNotNaN());
    assertFailureKeys(e, "expected a float other than NaN", "but was");
  }

  @Test
  public void isGreaterThan_int_strictly() {
    expectFailure(whenTesting -> whenTesting.that(2.0f).isGreaterThan(3));
  }

  @Test
  public void isGreaterThan_int() {
    expectFailure(whenTesting -> whenTesting.that(2.0f).isGreaterThan(2));
    assertThat(2.0f).isGreaterThan(1);
    assertThat(0x1.0p30f).isGreaterThan((1 << 30) - 1);
  }

  @Test
  public void isLessThan_int_strictly() {
    expectFailure(whenTesting -> whenTesting.that(2.0f).isLessThan(1));
  }

  @Test
  public void isLessThan_int() {
    expectFailure(whenTesting -> whenTesting.that(2.0f).isLessThan(2));
    assertThat(2.0f).isLessThan(3);
    assertThat(0x1.0p30f).isLessThan((1 << 30) + 1);
  }

  @Test
  public void isAtLeast_int() {
    expectFailure(whenTesting -> whenTesting.that(2.0f).isAtLeast(3));
    assertThat(2.0f).isAtLeast(2);
    assertThat(2.0f).isAtLeast(1);
  }

  @Test
  public void isAtLeast_int_withNoExactFloatRepresentation() {
    expectFailure(whenTesting -> whenTesting.that(0x1.0p30f).isAtLeast((1 << 30) + 1));
  }

  @Test
  public void isAtMost_int() {
    expectFailure(whenTesting -> whenTesting.that(2.0f).isAtMost(1));
    assertThat(2.0f).isAtMost(2);
    assertThat(2.0f).isAtMost(3);
  }

  @Test
  public void isAtMost_int_withNoExactFloatRepresentation() {
    expectFailure(whenTesting -> whenTesting.that(0x1.0p30f).isAtMost((1 << 30) - 1));
  }
}
