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
import static com.google.common.truth.MathUtil.equalWithinTolerance;
import static com.google.common.truth.MathUtil.notEqualWithinTolerance;
import static java.lang.Double.doubleToLongBits;

import javax.annotation.Nullable;

/**
 * Propositions for {@link Double} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class DoubleSubject extends ComparableSubject<DoubleSubject, Double> {
  private static final long NEG_ZERO_BITS = doubleToLongBits(-0.0);

  DoubleSubject(FailureStrategy failureStrategy, @Nullable Double actual) {
    super(failureStrategy, actual);
  }

  /**
   * A partially specified proposition about an approximate relationship to a {@code double} subject
   * using a tolerance.
   */
  public abstract static class TolerantDoubleComparison {

    // Prevent subclassing outside of this class
    private TolerantDoubleComparison() {}

    /**
     * Fails if the subject was expected to be within the tolerance of the given value but was not
     * <i>or</i> if it was expected <i>not</i> to be within the tolerance but was. The subject and
     * tolerance are specified earlier in the fluent call chain.
     */
    public abstract void of(double expectedDouble);

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on TolerantDoubleComparison. If
     *     you meant to compare doubles, use {@link #of(double)} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object o) {
      throw new UnsupportedOperationException(
          "If you meant to compare doubles, use .of(double) instead.");
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#hashCode()} is not supported on TolerantDoubleComparison
     */
    @Deprecated
    @Override
    public int hashCode() {
      throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
    }
  }

  /**
   * Prepares for a check that the subject is a finite number within the given tolerance of an
   * expected value that will be provided in the next call in the fluent chain.
   *
   * <p>The check will fail if either the subject or the object is {@link Double#POSITIVE_INFINITY},
   * {@link Double#NEGATIVE_INFINITY}, or {@link Double#NaN}. To check for those values, use {@link
   * #isPositiveInfinity}, {@link #isNegativeInfinity}, {@link #isNaN}, or (with more generality)
   * {@link #isEqualTo}.
   *
   * <p>The check will pass if both values are zero, even if one is {@code 0.0} and the other is
   * {@code -0.0}. Use {@code #isEqualTo} to assert that a value is exactly {@code 0.0} or that it
   * is exactly {@code -0.0}.
   *
   * <p>You can use a tolerance of {@code 0.0} to assert the exact equality of finite doubles, but
   * often {@link #isEqualTo} is preferable (note the different behaviours around non-finite values
   * and {@code -0.0}). See the documentation on {@link #isEqualTo} for advice on when exact
   * equality assertions are appropriate.
   *
   * @param tolerance an inclusive upper bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not {@link
   *     Double#NaN}, {@link Double#POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  public TolerantDoubleComparison isWithin(final double tolerance) {
    return new TolerantDoubleComparison() {
      @Override
      public void of(double expected) {
        Double actual = actual();
        checkNotNull(
            actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
        checkTolerance(tolerance);

        if (!equalWithinTolerance(actual, expected, tolerance)) {
          failWithRawMessage(
              "%s and <%s> should have been finite values within <%s> of each other",
              actualAsString(), expected, tolerance);
        }
      }
    };
  }

  /**
   * Prepares for a check that the subject is a finite number not within the given tolerance of an
   * expected value that will be provided in the next call in the fluent chain.
   *
   * <p>The check will fail if either the subject or the object is {@link Double#POSITIVE_INFINITY},
   * {@link Double#NEGATIVE_INFINITY}, or {@link Double#NaN}. See {@link #isFinite}, {@link
   * #isNotNaN}, or {@link #isNotEqualTo} for checks with other behaviours.
   *
   * <p>The check will fail if both values are zero, even if one is {@code 0.0} and the other is
   * {@code -0.0}. Use {@code #isNotEqualTo} for a test which fails for a value of exactly zero with
   * one sign but passes for zero with the opposite sign.
   *
   * <p>You can use a tolerance of {@code 0.0} to assert the exact non-equality of finite doubles,
   * but sometimes {@link #isNotEqualTo} is preferable (note the different behaviours around
   * non-finite values and {@code -0.0}).
   *
   * @param tolerance an exclusive lower bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not {@code
   *     Double.NaN}, {@code Double.POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  public TolerantDoubleComparison isNotWithin(final double tolerance) {
    return new TolerantDoubleComparison() {
      @Override
      public void of(double expected) {
        Double actual = actual();
        checkNotNull(
            actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
        checkTolerance(tolerance);

        if (!notEqualWithinTolerance(actual, expected, tolerance)) {
          failWithRawMessage(
              "%s and <%s> should have been finite values not within <%s> of each other",
              actualAsString(), expected, tolerance);
        }
      }
    };
  }

  /**
   * Asserts that the subject is exactly equal to the given value, with equality defined as by
   * {@code Double#equals}. This method is <i>not</i> recommended when the code under test is doing
   * any kind of arithmetic: use {@link #isWithin} with a suitable tolerance in that case. (Remember
   * that the exact result of floating point arithmetic is sensitive to apparently trivial changes
   * such as replacing {@code (a + b) + c} with {@code a + (b + c)}, and that unless {@code
   * strictfp} is in force even the result of {@code (a + b) + c} is sensitive to the JVM's choice
   * of precision for the intermediate result.) This method is recommended when the code under test
   * is specified as either copying a value without modification from its input or returning a
   * well-defined literal or constant value.
   *
   * <p><b>Note:</b> The assertion {@code isEqualTo(0.0)} fails for an input of {@code -0.0}, and
   * vice versa. For an assertion that passes for either {@code 0.0} or {@code -0.0}, use {@link
   * #isZero}.
   */
  public final void isEqualTo(@Nullable Double other) {
    super.isEqualTo(other);
  }

  /**
   * Asserts that the subject is not exactly equal to the given value, with equality defined as by
   * {@code Double#equals}. See {@link #isEqualTo} for advice on when exact equality is recommended.
   * Use {@link #isNotWithin} for an assertion with a tolerance.
   *
   * <p><b>Note:</b> The assertion {@code isNotEqualTo(0.0)} passes for {@code -0.0}, and vice
   * versa. For an assertion that fails for either {@code 0.0} or {@code -0.0}, use {@link
   * #isNonZero}.
   */
  public final void isNotEqualTo(@Nullable Double other) {
    super.isNotEqualTo(other);
  }

  /**
   * @deprecated Use {@link #isWithin} or {@link #isEqualTo} instead (see documentation for advice).
   */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(Double other) {
    super.isEquivalentAccordingToCompareTo(other);
  }

  /**
   * Ensures that the given tolerance is a non-negative finite value, i.e. not {@code Double.NaN},
   * {@code Double.POSITIVE_INFINITY}, or negative, including {@code -0.0}.
   */
  static void checkTolerance(double tolerance) {
    checkArgument(!Double.isNaN(tolerance), "tolerance cannot be NaN");
    checkArgument(tolerance >= 0.0, "tolerance (%s) cannot be negative", tolerance);
    checkArgument(
        doubleToLongBits(tolerance) != NEG_ZERO_BITS,
        "tolerance (%s) cannot be negative",
        tolerance);
    checkArgument(tolerance != Double.POSITIVE_INFINITY, "tolerance cannot be POSITIVE_INFINITY");
  }

  /** Asserts that the subject is zero (i.e. it is either {@code 0.0} or {@code -0.0}). */
  public final void isZero() {
    if (actual() == null || actual().doubleValue() != 0.0) {
      fail("is zero");
    }
  }

  /**
   * Asserts that the subject is a non-null value other than zero (i.e. it is not {@code 0.0},
   * {@code -0.0} or {@code null}).
   */
  public final void isNonZero() {
    if (actual() == null || actual().doubleValue() == 0.0) {
      fail("is non-zero");
    }
  }

  /** Asserts that the subject is {@link Double#POSITIVE_INFINITY}. */
  public final void isPositiveInfinity() {
    isEqualTo(Double.POSITIVE_INFINITY);
  }

  /** Asserts that the subject is {@link Double#NEGATIVE_INFINITY}. */
  public final void isNegativeInfinity() {
    isEqualTo(Double.NEGATIVE_INFINITY);
  }

  /** Asserts that the subject is {@link Double#NaN}. */
  public final void isNaN() {
    if (actual() == null || !actual().isNaN()) {
      fail("is NaN");
    }
  }

  /**
   * Asserts that the subject is finite, i.e. not {@link Double#POSITIVE_INFINITY}, {@link
   * Double#NEGATIVE_INFINITY}, or {@link Double#NaN}.
   */
  public final void isFinite() {
    if (actual() == null || actual().isNaN() || actual().isInfinite()) {
      failWithRawMessage("%s should have been finite", actualAsString());
    }
  }

  /**
   * Asserts that the subject is a non-null value other than {@link Double#NaN} (but it may be
   * {@link Double#POSITIVE_INFINITY} or {@link Double#NEGATIVE_INFINITY}).
   */
  public final void isNotNaN() {
    if (actual() == null || actual().isNaN()) {
      failWithRawMessage("%s should not have been NaN", actualAsString());
    }
  }
}
