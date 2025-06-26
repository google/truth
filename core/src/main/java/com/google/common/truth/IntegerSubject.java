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
 * A subject for {@link Integer} values.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
public class IntegerSubject extends ComparableSubject<Integer> {
  private final @Nullable Integer actual;

  /**
   * The constructor is for use by subclasses only. If you want to create an instance of this class
   * itself, call {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected IntegerSubject(FailureMetadata metadata, @Nullable Integer actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /**
   * A partially specified check about an approximate relationship to a {@code int} actual value
   * using a tolerance.
   *
   * @since 1.2
   */
  public static final class TolerantIntegerComparison {
    private final IntegerComparer comparer;

    private TolerantIntegerComparison(IntegerComparer comparer) {
      this.comparer = comparer;
    }

    /**
     * Checks that the actual value is within the tolerance of the given value or <i>not</i> within
     * the tolerance of the given value, depending on the choice made earlier in the fluent call
     * chain. The actual value and tolerance are also specified earlier in the fluent call chain.
     */
    public void of(int other) {
      comparer.compareAgainst(other);
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on TolerantIntegerComparison. If
     *     you meant to compare ints, use {@link #of(int)} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object other) {
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

    static TolerantIntegerComparison comparing(IntegerComparer comparer) {
      return new TolerantIntegerComparison(comparer);
    }
  }

  private interface IntegerComparer {
    void compareAgainst(int other);
  }

  /**
   * Prepares for a check that the actual value is a number within the given tolerance of an
   * expected value that will be provided in the next call in the fluent chain.
   *
   * @param tolerance an inclusive upper bound on the difference between the actual value and
   *     expected value allowed by the check, which must be a non-negative value.
   * @since 1.2
   */
  public TolerantIntegerComparison isWithin(int tolerance) {
    return TolerantIntegerComparison.comparing(
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
  public TolerantIntegerComparison isNotWithin(int tolerance) {
    return TolerantIntegerComparison.comparing(
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
   * @deprecated Use {@link #isEqualTo} instead. Integer comparison is consistent with equality.
   */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(@Nullable Integer expected) {
    super.isEquivalentAccordingToCompareTo(expected);
  }

  static Factory<IntegerSubject, Integer> integers() {
    return IntegerSubject::new;
  }
}
