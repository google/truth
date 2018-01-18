/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.DoubleSubject.checkTolerance;

import javax.annotation.Nullable;

/**
 * Determines whether an instance of type {@code A} corresponds in some way to an instance of type
 * {@code E}. For example, the implementation returned by the {@link #tolerance(double)} factory
 * method implements approximate equality between numeric values, with values being said to
 * correspond if the difference between them is does not exceed some fixed tolerance. The instances
 * of type {@code A} are typically actual values from a collection returned by the code under test;
 * the instances of type {@code E} are typically expected values with which the actual values are
 * compared by the test.
 *
 * <p>The correspondence is required to be consistent: for any given values {@code actual} and
 * {@code expected}, multiple invocations of {@code compare(actual, expected)} must consistently
 * return {@code true} or consistently return {@code false} (provided that neither value is
 * modified). Although {@code A} and {@code E} will often be the same types, they are <i>not</i>
 * required to be the same, and even if they are it is <i>not</i> required that the correspondence
 * should have any of the other properties of an equivalence relation (reflexivity, symmetry, or
 * transitivity).
 *
 * <p>Subclasses may optionally override {@link #formatDiff}. This results in failure messages
 * including formatted diffs between expected and actual elements, where possible.
 *
 * <p>Instances of this are typically used via {@link IterableSubject#comparingElementsUsing},
 * {@link MapSubject#comparingValuesUsing}, or {@link MultimapSubject#comparingValuesUsing}.
 *
 * @author Pete Gillin
 */
public abstract class Correspondence<A, E> {

  /**
   * Returns a {@link Correspondence} between {@link Number} instances that considers instances to
   * correspond (i.e. {@link Correspondence#compare(Object, Object)} returns {@code true}) if the
   * double values of each instance (i.e. the result of calling {@link Number#doubleValue()} on
   * them) are finite values within {@code tolerance} of each other.
   *
   * <ul>
   *   <li>It does not consider instances to correspond if either value is infinite or NaN.
   *   <li>The conversion to double may result in a loss of precision for some numeric types.
   *   <li>The {@link Correspondence#compare(Object, Object)} method throws a {@link
   *       NullPointerException} if either {@link Number} instance is null.
   * </ul>
   *
   * @param tolerance an inclusive upper bound on the difference between the double values of the
   *     two {@link Number} instances, which must be a non-negative finite value, i.e. not {@link
   *     Double#NaN}, {@link Double#POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  public static Correspondence<Number, Number> tolerance(double tolerance) {
    return new TolerantNumericEquality(tolerance);
  }

  private static final class TolerantNumericEquality extends Correspondence<Number, Number> {

    private final double tolerance;

    private TolerantNumericEquality(double tolerance) {
      this.tolerance = tolerance;
    }

    @Override
    public boolean compare(Number actual, Number expected) {
      checkTolerance(tolerance);
      double actualDouble = checkNotNull(actual).doubleValue();
      double expectedDouble = checkNotNull(expected).doubleValue();
      return MathUtil.equalWithinTolerance(actualDouble, expectedDouble, tolerance);
    }

    @Override
    public String toString() {
      return "is a finite number within " + tolerance + " of";
    }
  }

  /**
   * Returns whether or not the {@code actual} value is said to correspond to the {@code expected}
   * value for the purposes of this test.
   */
  public abstract boolean compare(@Nullable A actual, @Nullable E expected);

  /**
   * Returns a {@link String} describing the difference between the {@code actual} and {@code
   * expected} values, if possible, or {@code null} if not.
   *
   * <p>The implementation on the {@link Correspondence} base class always returns {@code null}. To
   * enable diffing, subclasses should override this method.
   *
   * <p>N.B. Implementing this method is currently of limited value as not all the assertions which
   * could make use of this do so. However, instances that implement it now will get those
   * improvements for free when they are made. TODO(b/32960783): Implement the rest of the planned
   * changes.
   *
   * <p>Assertions should only invoke this with parameters for which {@link #compare} returns {@code
   * false}.
   */
  @Nullable
  public String formatDiff(@Nullable A actual, @Nullable E expected) {
    return null;
  }

  /**
   * Returns a description of the correspondence, suitable to fill the gap in a failure message of
   * the form {@code "<some actual element> is an element that ... <some expected element>"}. Note
   * that this is a fragment of a verb phrase which takes a singular subject.
   *
   * <p>Example 1: For a {@code Correspondence<String, Integer>} that tests whether the actual
   * string parses to the expected integer, this would return {@code "parses to"} to result in a
   * failure message of the form {@code "<some actual string> is an element that parses to <some
   * expected integer>"}.
   *
   * <p>Example 2: For the {@code Correspondence<Number, Number>} returns by {@link #tolerance} this
   * returns {@code "is a finite number within " + tolerance + " of"} to result in a failure message
   * of the form {@code "<some actual number> is an element that is a finite number within 0.0001 of
   * <some expected number>"}.
   */
  @Override
  public abstract String toString();

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#equals(Object)} is not supported. If you meant to compare objects
   *     using this {@link Correspondence}, use {@link #compare}.
   */
  @Deprecated
  @Override
  public final boolean equals(@Nullable Object o) {
    throw new UnsupportedOperationException(
        "Correspondence.equals(object) is not supported. If you meant to compare objects, use"
            + " .compare(actual, expected) instead.");
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#hashCode()} is not supported.
   */
  @Deprecated
  @Override
  public final int hashCode() {
    throw new UnsupportedOperationException("Correspondence.hashCode() is not supported.");
  }
}
