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

import static com.google.common.truth.Fact.numericFact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.MathUtil.equalWithinTolerance;

import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Long} values.
 */
public class LongSubject extends ComparableSubject<Long> {

  private final @Nullable Long actual;

  /**
   * The constructor is for use by subclasses only. If you want to create an instance of this class
   * itself, call {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected LongSubject(FailureMetadata metadata, @Nullable Long actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /**
   * A partially specified check about an approximate relationship to a {@code long} value using a
   * tolerance.
   *
   * @since 1.2
   */
  public static final class TolerantLongComparison {
    private final LongComparer comparer;

    private TolerantLongComparison(LongComparer comparer) {
      this.comparer = comparer;
    }

    /**
     * Checks that the actual value is within the tolerance of the given value or <i>not</i> within
     * the tolerance of the given value, depending on the choice made earlier in the fluent call
     * chain. The actual value and tolerance are also specified earlier in the fluent call chain.
     */
    public void of(long other) {
      comparer.compareAgainst(other);
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on TolerantLongComparison. If you
     *     meant to compare longs, use {@link #of(long)} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object other) {
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

    static TolerantLongComparison comparing(LongComparer comparer) {
      return new TolerantLongComparison(comparer);
    }
  }

  private interface LongComparer {
    void compareAgainst(long other);
  }

  /**
   * Prepares for a check that the actual value is a number within the given tolerance of an
   * expected value that will be provided in the next call in the fluent chain.
   *
   * @param tolerance an inclusive upper bound on the difference between the actual value and
   *     expected value allowed by the check, which must be a non-negative value.
   * @since 1.2
   */
  public TolerantLongComparison isWithin(long tolerance) {
    return TolerantLongComparison.comparing(
        other -> {
          if (tolerance < 0) {
            failWithoutActual(
                simpleFact(
                    "could not perform approximate-equality check because tolerance was negative"),
                numericFact("expected", other),
                numericFact("was", actual),
                numericFact("tolerance", tolerance));
          } else if (actual == null) {
            failWithoutActual(
                numericFact("expected a value near", other),
                numericFact("but was", actual),
                numericFact("tolerance", tolerance));
          } else if (!equalWithinTolerance(actual, other, tolerance)) {
            failWithoutActual(
                numericFact("expected", other),
                numericFact("but was", actual),
                numericFact("outside tolerance", tolerance));
          }
        });
  }

  /**
   * Prepares for a check that the actual value is a number not within the given tolerance of an
   * expected value that will be provided in the next call in the fluent chain.
   *
   * @param tolerance an exclusive lower bound on the difference between the actual value and
   *     expected value allowed by the check, which must be a non-negative value.
   * @since 1.2
   */
  public TolerantLongComparison isNotWithin(long tolerance) {
    return TolerantLongComparison.comparing(
        other -> {
          if (tolerance < 0) {
            failWithoutActual(
                simpleFact(
                    "could not perform approximate-equality check because tolerance was negative"),
                numericFact("expected", other),
                numericFact("was", actual),
                numericFact("tolerance", tolerance));
          } else if (actual == null) {
            failWithoutActual(
                numericFact("expected a value that is not near", other),
                numericFact("but was", actual),
                numericFact("tolerance", tolerance));
          } else if (equalWithinTolerance(actual, other, tolerance)) {
            failWithoutActual(
                numericFact("expected not to be", other),
                numericFact("but was", actual),
                numericFact("within tolerance", tolerance));
          }
        });
  }

  /**
   * @deprecated Use {@link #isEqualTo} instead. Long comparison is consistent with equality.
   */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(@Nullable Long expected) {
    super.isEquivalentAccordingToCompareTo(expected);
  }

  /**
   * Checks that the actual value is greater than {@code other}.
   *
   * <p>To check that the actual value is greater than <i>or equal to</i> {@code other}, use {@link
   * #isAtLeast}.
   */
  public final void isGreaterThan(int other) {
    isGreaterThan((long) other);
  }

  /**
   * Checks that the actual value is less than {@code other}.
   *
   * <p>To check that the actual value is less than <i>or equal to</i> {@code other}, use {@link
   * #isAtMost} .
   */
  public final void isLessThan(int other) {
    isLessThan((long) other);
  }

  /**
   * Checks that the actual value is less than or equal to {@code other}.
   *
   * <p>To check that the actual value is <i>strictly</i> less than {@code other}, use {@link
   * #isLessThan}.
   */
  public final void isAtMost(int other) {
    isAtMost((long) other);
  }

  /**
   * Checks that the actual value is greater than or equal to {@code other}.
   *
   * <p>To check that the actual value is <i>strictly</i> greater than {@code other}, use {@link
   * #isGreaterThan}.
   */
  public final void isAtLeast(int other) {
    isAtLeast((long) other);
  }

  static Factory<LongSubject, Long> longs() {
    return LongSubject::new;
  }
}
