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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Fact.numericFact;
import static com.google.common.truth.MathUtil.equalWithinTolerance;

import org.jspecify.annotations.Nullable;

/**
 * Propositions for {@link Integer} subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
public class IntegerSubject extends ComparableSubject<Integer> {
  private final @Nullable Integer actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected IntegerSubject(FailureMetadata metadata, @Nullable Integer actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /**
   * A partially specified check about an approximate relationship to a {@code int} subject using a
   * tolerance.
   *
   * @since 1.2
   */
  public abstract static class TolerantIntegerComparison {

    // Prevent subclassing outside of this class
    private TolerantIntegerComparison() {}

    /**
     * Fails if the subject was expected to be within the tolerance of the given value but was not
     * <i>or</i> if it was expected <i>not</i> to be within the tolerance but was. The subject and
     * tolerance are specified earlier in the fluent call chain.
     */
    public abstract void of(int expected);

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on TolerantIntegerComparison. If
     *     you meant to compare ints, use {@link #of(int)} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object o) {
      throw new UnsupportedOperationException(
          "If you meant to compare ints, use .of(int) instead.");
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#hashCode()} is not supported on TolerantIntegerComparison
     */
    @Deprecated
    @Override
    public int hashCode() {
      throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
    }
  }

  /**
   * Prepares for a check that the subject is a number within the given tolerance of an expected
   * value that will be provided in the next call in the fluent chain.
   *
   * @param tolerance an inclusive upper bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative value.
   * @since 1.2
   */
  public TolerantIntegerComparison isWithin(int tolerance) {
    return new TolerantIntegerComparison() {
      @Override
      public void of(int expected) {
        Integer actual = IntegerSubject.this.actual;
        checkNotNull(
            actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
        checkTolerance(tolerance);

        if (!equalWithinTolerance(actual, expected, tolerance)) {
          failWithoutActual(
              numericFact("expected", expected),
              numericFact("but was", actual),
              numericFact("outside tolerance", tolerance));
        }
      }
    };
  }

  /**
   * Prepares for a check that the subject is a number not within the given tolerance of an expected
   * value that will be provided in the next call in the fluent chain.
   *
   * @param tolerance an exclusive lower bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative value.
   * @since 1.2
   */
  public TolerantIntegerComparison isNotWithin(int tolerance) {
    return new TolerantIntegerComparison() {
      @Override
      public void of(int expected) {
        Integer actual = IntegerSubject.this.actual;
        checkNotNull(
            actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
        checkTolerance(tolerance);

        if (equalWithinTolerance(actual, expected, tolerance)) {
          failWithoutActual(
              numericFact("expected not to be", expected),
              numericFact("but was", actual),
              numericFact("within tolerance", tolerance));
        }
      }
    };
  }

  /**
   * @deprecated Use {@link #isEqualTo} instead. Integer comparison is consistent with equality.
   */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(@Nullable Integer other) {
    super.isEquivalentAccordingToCompareTo(other);
  }

  /** Ensures that the given tolerance is a non-negative value. */
  private static void checkTolerance(int tolerance) {
    checkArgument(tolerance >= 0, "tolerance (%s) cannot be negative", tolerance);
  }
}
