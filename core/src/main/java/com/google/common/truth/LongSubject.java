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
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.MathUtil.equalWithinTolerance;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Propositions for {@code long} subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
@NullMarked
public class LongSubject extends ComparableSubject<Long> {

  private final @Nullable Long actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected LongSubject(FailureMetadata metadata, @Nullable Long actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /**
   * A partially specified check about an approximate relationship to a {@code long} subject using a
   * tolerance.
   *
   * @since 1.2
   */
  public abstract static class TolerantLongComparison {

    // Prevent subclassing outside of this class
    private TolerantLongComparison() {}

    /**
     * Fails if the subject was expected to be within the tolerance of the given value but was not
     * <i>or</i> if it was expected <i>not</i> to be within the tolerance but was. The subject and
     * tolerance are specified earlier in the fluent call chain.
     */
    public abstract void of(long expectedLong);

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on TolerantLongComparison. If you
     *     meant to compare longs, use {@link #of(long)} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object o) {
      throw new UnsupportedOperationException(
          "If you meant to compare longs, use .of(long) instead.");
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#hashCode()} is not supported on TolerantLongComparison
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
  public TolerantLongComparison isWithin(long tolerance) {
    return new TolerantLongComparison() {
      @Override
      public void of(long expected) {
        Long actual = LongSubject.this.actual;
        checkNotNull(
            actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
        checkTolerance(tolerance);

        if (!equalWithinTolerance(actual, expected, tolerance)) {
          failWithoutActual(
              fact("expected", Long.toString(expected)),
              butWas(),
              fact("outside tolerance", Long.toString(tolerance)));
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
  public TolerantLongComparison isNotWithin(long tolerance) {
    return new TolerantLongComparison() {
      @Override
      public void of(long expected) {
        Long actual = LongSubject.this.actual;
        checkNotNull(
            actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
        checkTolerance(tolerance);

        if (equalWithinTolerance(actual, expected, tolerance)) {
          failWithoutActual(
              fact("expected not to be", Long.toString(expected)),
              butWas(),
              fact("within tolerance", Long.toString(tolerance)));
        }
      }
    };
  }

  /**
   * @deprecated Use {@link #isEqualTo} instead. Long comparison is consistent with equality.
   */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(@Nullable Long other) {
    super.isEquivalentAccordingToCompareTo(other);
  }

  /** Ensures that the given tolerance is a non-negative value. */
  private static void checkTolerance(long tolerance) {
    checkArgument(tolerance >= 0, "tolerance (%s) cannot be negative", tolerance);
  }

  /**
   * Checks that the subject is greater than {@code other}.
   *
   * <p>To check that the subject is greater than <i>or equal to</i> {@code other}, use {@link
   * #isAtLeast}.
   */
  public final void isGreaterThan(int other) {
    isGreaterThan((long) other);
  }

  /**
   * Checks that the subject is less than {@code other}.
   *
   * <p>To check that the subject is less than <i>or equal to</i> {@code other}, use {@link
   * #isAtMost} .
   */
  public final void isLessThan(int other) {
    isLessThan((long) other);
  }

  /**
   * Checks that the subject is less than or equal to {@code other}.
   *
   * <p>To check that the subject is <i>strictly</i> less than {@code other}, use {@link
   * #isLessThan}.
   */
  public final void isAtMost(int other) {
    isAtMost((long) other);
  }

  /**
   * Checks that the subject is greater than or equal to {@code other}.
   *
   * <p>To check that the subject is <i>strictly</i> greater than {@code other}, use {@link
   * #isGreaterThan}.
   */
  public final void isAtLeast(int other) {
    isAtLeast((long) other);
  }
}
