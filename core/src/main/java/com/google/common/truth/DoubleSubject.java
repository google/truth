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
     * <i>or</i> if it was expected <i>not</i> to be within the tolerance but was. The expectation,
     * subject, and tolerance are all specified earlier in the fluent call chain.
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
   * Prepares for a check that the subject is within the given tolerance of an expected value
   * that will be provided in the next call in the fluent chain.
   */
  @CheckReturnValue
  public TolerantDoubleComparison isWithin(final double tolerance) {
    return new TolerantDoubleComparison() {
      @Override
      public void of(double expected) {
        Double actual = getSubject();
        checkNotNull(actual, "actual value cannot be null. tolerance=%s expected=%s",
            tolerance, expected);
        checkTolerance(tolerance);

        if (!MathUtil.equals(actual, expected, tolerance)) {
          failWithRawMessage(
              "%s should have been within %s of %s", actual, tolerance, expected);
        }
      }
    };
  }

  /**
   * Prepares for a check that the subject is not within the given tolerance of an expected value
   * that will be provided in the next call in the fluent chain.
   */
  @CheckReturnValue
  public TolerantDoubleComparison isNotWithin(final double tolerance) {
    return new TolerantDoubleComparison() {
      @Override
      public void of(double expected) {
        Double actual = getSubject();
        checkNotNull(actual, "actual value cannot be null. tolerance=%s expected=%s",
            tolerance, expected);
        checkTolerance(tolerance);

        if (!MathUtil.notEquals(actual, expected, tolerance)) {
          failWithRawMessage(
              "%s should not have been within %s of %s", actual, tolerance, expected);
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
  public final void comparesEqualTo(Double other) {
    super.comparesEqualTo(other);
  }

  /**
   * Ensures that the given tolerance is not {@code Double.NaN} or negative, including {@code -0.0}.
   */
  private static void checkTolerance(double tolerance) {
    checkArgument(!Double.isNaN(tolerance), "tolerance (%s) cannot be NaN", tolerance);
    checkArgument(tolerance >= 0.0, "tolerance (%s) cannot be negative", tolerance);
    checkArgument(doubleToLongBits(tolerance) != NEG_ZERO_BITS,
        "tolerance (%s) cannot be negative", tolerance);
  }
}
