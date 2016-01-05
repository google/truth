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
import static java.lang.Double.doubleToLongBits;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * Propositions for {@link Double} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class DoubleSubject extends ComparableSubject<DoubleSubject, Double> {
  private static final long NEG_ZERO_BITS = doubleToLongBits(-0.0);

  DoubleSubject(FailureStrategy failureStrategy, @Nullable Double subject) {
    super(failureStrategy, subject);
  }

  /**
   * A partially specified proposition about an approximate relationship to a {@code double}
   * subject using a tolerance.
   */
  public abstract class TolerantDoubleComparison {

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
     * @deprecated {@link Object#equals(Object)} is not supported on TolerantDoubleComparison
     *     If you meant to compare doubles, use {@link #of(double)} instead.
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
   * <p>You can use a tolerance of {@code 0.0} to assert the exact equality of finite doubles. This
   * is appropriate when the contract of the code under test guarantees this, e.g. by specifying
   * that a return value is copied unchanged from the input, by specifying an exact constant value
   * to be returned, or by specifying an exact sequence of arithmetic operations to be followed.
   *
   * <p>The check will fail if either the subject or the object is
   * {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, or {@link Double#NaN}. To
   * check for those values, use {@link #isPositiveInfinity}, {@link isNegativeInfinity}, or
   * {@link isNaN}.
   *
   * @param tolerance an inclusive upper bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not
   *     {@link Double#NaN}, {@link Double#POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  @CheckReturnValue
  public TolerantDoubleComparison isWithin(final double tolerance) {
    return new TolerantDoubleComparison() {
      @Override
      public void of(double expected) {
        Double actual = getSubject();
        checkNotNull(
            actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
        checkTolerance(tolerance);

        if (!MathUtil.equals(actual, expected, tolerance)) {
          failWithRawMessage(
              "%s and <%s> should have been finite values within <%s> of each other",
              getDisplaySubject(),
              expected,
              tolerance);
        }
      }
    };
  }

  /**
   * Prepares for a check that the subject is a finite number not within the given tolerance of an
   * expected value that will be provided in the next call in the fluent chain.
   *
   * <p>You can use a tolerance of {@code 0.0} to assert the exact inequality of finite doubles.
   *
   * <p>The check will fail if either the subject or the object is
   * {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, or {@link Double#NaN}.
   * See {@link #isFinite} and {@link #isNotNaN}, or use {@link #isIn} with a suitable
   * {@link com.google.common.collect.Range Range}.
   *
   * @param tolerance an exclusive lower bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not
   *     {@code Double.NaN}, {@code Double.POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  @CheckReturnValue
  public TolerantDoubleComparison isNotWithin(final double tolerance) {
    return new TolerantDoubleComparison() {
      @Override
      public void of(double expected) {
        Double actual = getSubject();
        checkNotNull(
            actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
        checkTolerance(tolerance);

        if (!MathUtil.notEquals(actual, expected, tolerance)) {
          failWithRawMessage(
              "%s and <%s> should have been finite values not within <%s> of each other",
              getDisplaySubject(),
              expected,
              tolerance);
        }
      }
    };
  }

  /**
   * @deprecated Use {@link #isWithin} instead. Double comparison should always have a tolerance.
   */
  @Deprecated
  public final void isEqualTo(@Nullable Double other) {
    super.isEqualTo(other);
  }

  /**
   * @deprecated Use {@link #isNotWithin} instead. Double comparison should always have a tolerance.
   */
  @Deprecated
  public final void isNotEqualTo(@Nullable Double other) {
    super.isNotEqualTo(other);
  }

  /**
   * @deprecated Use {@link #isWithin} instead. Double comparison should always have a tolerance.
   */
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

  /**
   * Asserts that the subject is {@link Double#POSITIVE_INFINITY}.
   */
  public final void isPositiveInfinity() {
    super.isEqualTo(Double.POSITIVE_INFINITY);
  }

  /**
   * Asserts that the subject is {@link Double#NEGATIVE_INFINITY}.
   */
  public final void isNegativeInfinity() {
    super.isEqualTo(Double.NEGATIVE_INFINITY);
  }

  /**
   * Asserts that the subject is {@link Double#NaN}.
   */
  public final void isNaN() {
    super.isEqualTo(Double.NaN);
  }

  /**
   * Asserts that the subject is finite, i.e. not {@link Double#POSITIVE_INFINITY},
   * {@link Double#NEGATIVE_INFINITY}, or {@link Double#NaN}.
   */
  public final void isFinite() {
    if (getSubject() == null || getSubject().isNaN() || getSubject().isInfinite()) {
      failWithRawMessage("%s should have been finite", getDisplaySubject());
    }
  }

  /**
   * Asserts that the subject is not {@link Double#NaN} (but it may be
   * {@link Double#POSITIVE_INFINITY} or {@link Double#NEGATIVE_INFINITY}).
   */
  public final void isNotNaN() {
    if (getSubject() == null || getSubject().isNaN()) {
      failWithRawMessage("%s should not have been NaN", getDisplaySubject());
    }
  }
}
