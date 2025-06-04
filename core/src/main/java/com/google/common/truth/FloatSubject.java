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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Fact.numericFact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.MathUtil.equalWithinTolerance;
import static com.google.common.truth.MathUtil.notEqualWithinTolerance;
import static java.lang.Float.NaN;
import static java.lang.Float.floatToIntBits;

import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Float} values.
 *
 * @author Kurt Alfred Kluever
 */
public final class FloatSubject extends ComparableSubject<Float> {
  private static final int NEG_ZERO_BITS = floatToIntBits(-0.0f);

  private final @Nullable Float actual;
  private final DoubleSubject asDouble;

  private FloatSubject(FailureMetadata metadata, @Nullable Float actual) {
    super(metadata, actual);
    this.actual = actual;
    this.asDouble = substituteCheck().that(actual == null ? null : Double.valueOf(actual));
  }

  @FunctionalInterface
  interface FloatComparisonLogic {
    void apply(float expected);
  }

  /**
   * A partially specified check about an approximate relationship to a {@code float} value using a
   * tolerance.
   */
  public static final class TolerantFloatComparison {
    private final float tolerance;
    private final FloatComparisonLogic comparisonLogic;

    // Prevent subclassing outside of this class
    private TolerantFloatComparison(float tolerance, FloatComparisonLogic logic) {
      this.tolerance = tolerance; // Though not used directly here, it's part of the context
      this.comparisonLogic = logic;
    }

    /**
     * Checks that the actual value is within the tolerance of the given value or <i>not</i> within
     * the tolerance of the given value, depending on the choice made earlier in the fluent call
     * chain. The actual value and tolerance are also specified earlier in the fluent call chain.
     */
    public void of(float expected) {
      comparisonLogic.apply(expected);
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on TolerantFloatComparison. If you
     *     meant to compare floats, use {@link #of(float)} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object o) {
      throw new UnsupportedOperationException(
          "If you meant to compare floats, use .of(float) instead.");
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#hashCode()} is not supported on TolerantFloatComparison
     */
    @Deprecated
    @Override
    public int hashCode() {
      throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
    }
  }

  /**
   * Prepares for a check that the actual value is a finite number within the given tolerance of an
   * expected value that will be provided in the next call in the fluent chain.
   *
   * <p>The check will fail if either the actual value or the expected value is {@link
   * Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, or {@link Float#NaN}. To check for
   * those values, use {@link #isPositiveInfinity}, {@link #isNegativeInfinity}, {@link #isNaN}, or
   * (with more generality) {@link #isEqualTo}.
   *
   * <p>The check will pass if both values are zero, even if one is {@code 0.0f} and the other is
   * {@code -0.0f}. Use {@link #isEqualTo} to assert that a value is exactly {@code 0.0f} or that it
   * is exactly {@code -0.0f}.
   *
   * <p>You can use a tolerance of {@code 0.0f} to assert the exact equality of finite floats, but
   * often {@link #isEqualTo} is preferable (note the different behaviours around non-finite values
   * and {@code -0.0f}). See the documentation on {@link #isEqualTo} for advice on when exact
   * equality assertions are appropriate.
   *
   * @param tolerance an inclusive upper bound on the difference between the actual value and
   *     expected value allowed by the check, which must be a non-negative finite value, i.e. not
   *     {@link Float#NaN}, {@link Float#POSITIVE_INFINITY}, or negative, including {@code -0.0f}
   */
  public TolerantFloatComparison isWithin(float tolerance) {
    return TolerantFloatComparison.create(
        tolerance,
        expected -> {
          Float actual = FloatSubject.this.actual;
          checkNotNull(
              actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
          checkTolerance(tolerance);

          if (!equalWithinTolerance(actual, expected, tolerance)) {
            failWithoutActual(
                numericFact("expected", expected),
                numericFact("but was", actual),
                numericFact("outside tolerance", tolerance));
          }
        });
  }

  /**
   * Prepares for a check that the actual value is a finite number not within the given tolerance of
   * an expected value that will be provided in the next call in the fluent chain.
   *
   * <p>The check will fail if either the actual value or the expected value is {@link
   * Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, or {@link Float#NaN}. See {@link
   * #isFinite}, {@link #isNotNaN}, or {@link #isNotEqualTo} for checks with other behaviours.
   *
   * <p>The check will fail if both values are zero, even if one is {@code 0.0f} and the other is
   * {@code -0.0f}. Use {@link #isNotEqualTo} for a test which fails for a value of exactly zero
   * with one sign but passes for zero with the opposite sign.
   *
   * <p>You can use a tolerance of {@code 0.0f} to assert the exact non-equality of finite floats,
   * but sometimes {@link #isNotEqualTo} is preferable (note the different behaviours around
   * non-finite values and {@code -0.0f}).
   *
   * @param tolerance an exclusive lower bound on the difference between the actual value and
   *     expected value allowed by the check, which must be a non-negative finite value, i.e. not
   *     {@code Float.NaN}, {@code Float.POSITIVE_INFINITY}, or negative, including {@code -0.0f}
   */
  public TolerantFloatComparison isNotWithin(float tolerance) {
    return TolerantFloatComparison.create(
        tolerance,
        expected -> {
          Float actual = FloatSubject.this.actual;
          checkNotNull(
              actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
          checkTolerance(tolerance);

          if (!notEqualWithinTolerance(actual, expected, tolerance)) {
            failWithoutActual(
                numericFact("expected not to be", expected),
                numericFact("but was", actual),
                numericFact("within tolerance", tolerance));
          }
        });
  }

  /**
   * Asserts that the actual value is exactly equal to the given value, with equality defined as by
   * {@code Float#equals}. This method is <i>not</i> recommended when the code under test is doing
   * any kind of arithmetic: use {@link #isWithin} with a suitable tolerance in that case. (Remember
   * that the exact result of floating point arithmetic is sensitive to apparently trivial changes
   * such as replacing {@code (a + b) + c} with {@code a + (b + c)}, and that unless {@code
   * strictfp} is in force even the result of {@code (a + b) + c} is sensitive to the JVM's choice
   * of precision for the intermediate result.) This method is recommended when the code under test
   * is specified as either copying a value without modification from its input or returning a
   * well-defined literal or constant value.
   *
   * <p><b>Note:</b> The assertion {@code isEqualTo(0.0f)} fails for an input of {@code -0.0f}, and
   * vice versa. For an assertion that passes for either {@code 0.0f} or {@code -0.0f}, use {@link
   * #isZero}.
   */
  @Override
  public void isEqualTo(@Nullable Object other) {
    super.isEqualTo(other);
  }

  /**
   * Asserts that the actual value is not exactly equal to the given value, with equality defined as
   * by {@code Float#equals}. See {@link #isEqualTo} for advice on when exact equality is
   * recommended. Use {@link #isNotWithin} for an assertion with a tolerance.
   *
   * <p><b>Note:</b> The assertion {@code isNotEqualTo(0.0f)} passes for {@code -0.0f}, and vice
   * versa. For an assertion that fails for either {@code 0.0f} or {@code -0.0f}, use {@link
   * #isNonZero}.
   */
  @Override
  public void isNotEqualTo(@Nullable Object other) {
    super.isNotEqualTo(other);
  }

  /**
   * @deprecated Use {@link #isWithin} or {@link #isEqualTo} instead (see documentation for advice).
   */
  @Override
  @Deprecated
  public void isEquivalentAccordingToCompareTo(@Nullable Float other) {
    super.isEquivalentAccordingToCompareTo(other);
  }

  /**
   * Ensures that the given tolerance is a non-negative finite value, i.e. not {@code Float.NaN},
   * {@code Float.POSITIVE_INFINITY}, or negative, including {@code -0.0f}.
   */
  static void checkTolerance(float tolerance) {
    checkArgument(!Float.isNaN(tolerance), "tolerance cannot be NaN");
    checkArgument(tolerance >= 0.0f, "tolerance (%s) cannot be negative", tolerance);
    checkArgument(
        floatToIntBits(tolerance) != NEG_ZERO_BITS, "tolerance (%s) cannot be negative", tolerance);
    checkArgument(tolerance != Float.POSITIVE_INFINITY, "tolerance cannot be POSITIVE_INFINITY");
  }

  /** Asserts that the actual value is zero (i.e. it is either {@code 0.0f} or {@code -0.0f}). */
  public void isZero() {
    if (actual == null || actual != 0.0f) {
      failWithActual(simpleFact("expected zero"));
    }
  }

  /**
   * Asserts that the actual value is a non-null value other than zero (i.e. it is not {@code 0.0f},
   * {@code -0.0f} or {@code null}).
   */
  public void isNonZero() {
    if (actual == null) {
      failWithActual(simpleFact("expected a float other than zero"));
    } else if (actual == 0.0f) {
      failWithActual(simpleFact("expected not to be zero"));
    }
  }

  /** Asserts that the actual value is {@link Float#POSITIVE_INFINITY}. */
  public void isPositiveInfinity() {
    isEqualTo(Float.POSITIVE_INFINITY);
  }

  /** Asserts that the actual value is {@link Float#NEGATIVE_INFINITY}. */
  public void isNegativeInfinity() {
    isEqualTo(Float.NEGATIVE_INFINITY);
  }

  /** Asserts that the actual value is {@link Float#NaN}. */
  public void isNaN() {
    isEqualTo(NaN);
  }

  /**
   * Asserts that the actual value is finite, i.e. not {@link Float#POSITIVE_INFINITY}, {@link
   * Float#NEGATIVE_INFINITY}, or {@link Float#NaN}.
   */
  public void isFinite() {
    if (actual == null || actual.isNaN() || actual.isInfinite()) {
      failWithActual(simpleFact("expected to be finite"));
    }
  }

  /**
   * Asserts that the actual value is a non-null value other than {@link Float#NaN} (but it may be
   * {@link Float#POSITIVE_INFINITY} or {@link Float#NEGATIVE_INFINITY}).
   */
  public void isNotNaN() {
    if (actual == null) {
      failWithActual(simpleFact("expected a float other than NaN"));
    } else {
      isNotEqualTo(NaN);
    }
  }

  /**
   * Checks that the actual value is greater than {@code other}.
   *
   * <p>To check that the actual value is greater than <i>or equal to</i> {@code other}, use {@link
   * #isAtLeast}.
   */
  public void isGreaterThan(int other) {
    asDouble.isGreaterThan(other);
  }

  /**
   * Checks that the actual value is less than {@code other}.
   *
   * <p>To check that the actual value is less than <i>or equal to</i> {@code other}, use {@link
   * #isAtMost} .
   */
  public void isLessThan(int other) {
    asDouble.isLessThan(other);
  }

  /**
   * Checks that the actual value is less than or equal to {@code other}.
   *
   * <p>To check that the actual value is <i>strictly</i> less than {@code other}, use {@link
   * #isLessThan}.
   */
  public void isAtMost(int other) {
    asDouble.isAtMost(other);
  }

  /**
   * Checks that the actual value is greater than or equal to {@code other}.
   *
   * <p>To check that the actual value is <i>strictly</i> greater than {@code other}, use {@link
   * #isGreaterThan}.
   */
  public void isAtLeast(int other) {
    asDouble.isAtLeast(other);
  }

  static Factory<FloatSubject, Float> floats() {
    return FloatSubject::new;
  }

  // Factory method for TolerantFloatComparison
  static TolerantFloatComparison create(float tolerance, FloatComparisonLogic logic) {
    return new TolerantFloatComparison(tolerance, logic);
  }
}
