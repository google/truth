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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.DoubleSubject.checkTolerance;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * A Subject to handle testing propositions for {@code double[]}.
 *
 * <p>Note: this class deprecates some common methods because the operation of equality and
 * comparison on floating point numbers requires additional specification.  Alternative
 * equality tests are provided.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@CheckReturnValue
public class PrimitiveDoubleArraySubject
    extends AbstractArraySubject<PrimitiveDoubleArraySubject, double[]> {
  PrimitiveDoubleArraySubject(FailureStrategy failureStrategy, @Nullable double[] o) {
    super(failureStrategy, o);
  }

  @Override
  protected String underlyingType() {
    return "double";
  }

  @Override
  protected List<Double> listRepresentation() {
    return Doubles.asList(getSubject());
  }

  /**
   * This form is unsafe for double-precision floating point types, and will throw an
   * {@link UnsupportedOperationException}.
   *
   * @deprecated use {@link #isWithin(double)}
   */
  @Deprecated
  @Override
  public void isEqualTo(Object expected) {
    throw new UnsupportedOperationException(
        "Comparing raw equality of doubles is unsafe, "
            + "use isEqualTo(double[] array, double tolerance) instead.");
  }

  /**
   * A proposition that the provided double[] is an array of the same length and type, and
   * contains elements such that each element in {@code expected} is equal to each element
   * in the subject, and in the same position.
   *
   * <p>Behaviour for non-finite values ({@link Double#POSITIVE_INFINITY POSITIVE_INFINITY},
   * {@link Double#NEGATIVE_INFINITY NEGATIVE_INFINITY}, and {@link Double#NaN NaN}) is as follows:
   * If the subject and the object of the assertion are the same array the test will pass. If not
   * (including if one is a clone of the other) then non-finite values are considered not equal so
   * the any non-finite value in either argument will cause the test to fail.
   *
   * @deprecated use {@link #hasValuesWithin(double)}, noting the different behaviour for non-finite
   *     values
   */
  @Deprecated
  public void isEqualTo(Object expected, double tolerance) {
    double[] actual = getSubject();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      double[] expectedArray = (double[]) expected;
      if (expectedArray.length != actual.length) {
        failWithRawMessage(
            "Arrays are of different lengths. expected: %s, actual %s",
            Doubles.asList(expectedArray),
            Doubles.asList(actual));
      }
      List<Integer> unequalIndices = new ArrayList<Integer>();
      for (int i = 0; i < expectedArray.length; i++) {
        if (!MathUtil.isEquals(actual[i], expectedArray[i], tolerance)) {
          unequalIndices.add(i);
        }
      }

      if (!unequalIndices.isEmpty()) {
        fail("is equal to", Doubles.asList(expectedArray));
      }
    } catch (ClassCastException e) {
      failWithBadType(expected);
    }
  }

  /**
   * This form is unsafe for double-precision floating point types, and will throw an
   * {@link UnsupportedOperationException}.
   *
   * @deprecated use {@link #isNotWithin(double)}
   */
  @Deprecated
  @Override
  public void isNotEqualTo(Object expected) {
    throw new UnsupportedOperationException(
        "Comparing raw equality of doubles is unsafe, "
            + "use isNotEqualTo(double[] array, double tolerance) instead.");
  }

  /**
   * A proposition that the provided double[] is not an array of the same length or type, or
   * has at least one element that does not pass an equality test within the given tolerance.
   *
   * <p>Behaviour for non-finite values ({@link Double#POSITIVE_INFINITY POSITIVE_INFINITY},
   * {@link Double#NEGATIVE_INFINITY NEGATIVE_INFINITY}, and {@link Double#NaN NaN}) is as follows:
   * If the subject and the object of the assertion are the same array the test will fail. If not
   * (including if one is a clone of the other) then non-finite values are considered not equal so
   * the any non-finite value in either argument will cause the test to pass.
   *
   * @deprecated use {@link #hasValuesNotWithin(double)}, noting the different behaviour for
   *     non-finite values
   */
  @Deprecated
  public void isNotEqualTo(Object expectedArray, double tolerance) {
    double[] actual = getSubject();
    try {
      double[] expected = (double[]) expectedArray;
      if (actual == expected) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", getDisplaySubject(), Doubles.asList(expected));
      }
      if (expected.length != actual.length) {
        return; // Unequal-lengthed arrays are not equal.
      }
      List<Integer> unequalIndices = new ArrayList<Integer>();
      for (int i = 0; i < expected.length; i++) {
        if (!MathUtil.isEquals(actual[i], expected[i], tolerance)) {
          unequalIndices.add(i);
        }
      }
      if (unequalIndices.isEmpty()) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", getDisplaySubject(), Doubles.asList(expected));
      }
    } catch (ClassCastException ignored) {
      // Unequal since they are of different types.
    }
  }

  /**
   * A partially specified proposition about an approximate relationship to a {@code double[]}
   * subject using a tolerance.
   */
  public abstract class TolerantPrimitiveDoubleArrayComparison {

    // Prevent subclassing outside of this class
    private TolerantPrimitiveDoubleArrayComparison() {}

    /**
     * Fails if the values in the subject were expected to be within the tolerance of the given
     * values but were not <i>or</i> if they were expected <i>not</i> to be within the tolerance but
     * were. The subject and tolerance are specified earlier in the fluent call chain.
     */
    public void of(double... expected) {
      ofElementsIn(Doubles.asList(expected));
    }

    /**
     * Fails if the values in the subject were expected to be within the tolerance of the given
     * values but were not <i>or</i> if they were expected <i>not</i> to be within the tolerance but
     * were. The subject and tolerance are specified earlier in the fluent call chain. The values
     * will be cast to doubles if necessary, which might lose precision.
     */
    public abstract void ofElementsIn(Iterable<? extends Number> expected);

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on
     *     TolerantPrimitiveDoubleArrayComparison. If you meant to compare double arrays, use
     *     {@link #of} or {@link #ofElementsIn} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object o) {
      throw new UnsupportedOperationException(
          "If you meant to compare double arrays, use .of() or .ofElementsIn() instead.");
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#hashCode()} is not supported on
     *     TolerantPrimitiveDoubleArrayComparison
     */
    @Deprecated
    @Override
    public int hashCode() {
      throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
    }
  }

  /**
   * Prepares for a check that the subject and object are arrays both (a) of the same length, and
   * (b) where the values at all corresponding positions in each array are finite values within
   * {@code tolerance} of each other, that is
   * {@code assertThat(actual[i]).isWithin(tolerance).of(expected[i])} passes for all {@code i}
   * (see the {@link DoubleSubject#isWithin isWithin} assertion for doubles).
   *
   * <p>The check will fail if any value in either the subject array or the object array is
   * {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, or {@link Double#NaN}.
   *
   * @param tolerance an inclusive upper bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not
   *     {@link Double#NaN}, {@link Double#POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  public TolerantPrimitiveDoubleArrayComparison hasValuesWithin(final double tolerance) {
    return new TolerantPrimitiveDoubleArrayComparison() {

      @Override
      public void ofElementsIn(Iterable<? extends Number> expected) {
        checkTolerance(tolerance);
        double[] actual = checkNotNull(getSubject());
        List<Integer> mismatches = new ArrayList<Integer>();
        int expectedCount = 0;
        for (Number expectedValue : expected) {
          // if expected is longer than actual, we can skip the excess values: this case is covered
          // by the length check below
          if (expectedCount < actual.length
              && !MathUtil.isEquals(actual[expectedCount], expectedValue.doubleValue(), tolerance)) {
            mismatches.add(expectedCount);
          }
          expectedCount++;
        }
        if (actual.length != expectedCount) {
          failWithRawMessage(
              "Not true that %s has values within %s of <%s>. Expected length <%s> but got <%s>",
              getDisplaySubject(),
              tolerance,
              Iterables.toString(expected),
              expectedCount,
              actual.length);
        }
        if (!mismatches.isEmpty()) {
          failWithBadResults(
              "has values within " + tolerance + " of",
              Iterables.toString(expected),
              "differs at indexes",
              mismatches);
        }
      }
    };
  }

  /**
   * Prepares for a check that the subject and object are arrays either (a) of the different
   * lengths, or (b) of the same length but where the values at at least one corresponding position
   * in each array are finite values not within {@code tolerance} of each other, that is
   * {@code assertThat(actual[i]).isNotWithin(tolerance).of(expected[i])} passes for at least one
   * {@code i} (see the {@link DoubleSubject#isNotWithin isNotWithin} assertion for doubles).
   *
   * <p>In the case (b), a pair of subject and object values will not cause the test to pass if
   * either of them is {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, or
   * {@link Double#NaN}.
   *
   * @param tolerance an exclusive lower bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not
   *     {@code Double.NaN}, {@code Double.POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  public TolerantPrimitiveDoubleArrayComparison hasValuesNotWithin(final double tolerance) {
    return new TolerantPrimitiveDoubleArrayComparison() {

      @Override
      public void ofElementsIn(Iterable<? extends Number> expected) {
        checkTolerance(tolerance);
        double[] actual = checkNotNull(getSubject());
        int expectedCount = 0;
        for (Number expectedValue : expected) {
          // if expected is longer than actual, we can skip the excess values: this case is covered
          // by the length check below
          if (expectedCount < actual.length
              && MathUtil.notEquals(
                  actual[expectedCount], expectedValue.doubleValue(), tolerance)) {
            return;
          }
          expectedCount++;
        }
        // By the method contract, the assertion passes if the lengths are different. This is so
        // that isNotWithin behaves like isNotEqualTo with a tolerance (and different handling of
        // non-finite values).
        if (actual.length == expectedCount) {
          fail("has values not within " + tolerance + " of", Iterables.toString(expected));
        }
      }
    };
  }

  // TODO(b/25905290): Find a way to safely expose this. But disable it for now, since it will
  // nearly always be incorrect to simply treat a list of floats and do normal set operations that
  // are based on bare comparisons.
  @SuppressWarnings("unused")
  public IterableSubject asList() {
    return new IterableSubject(failureStrategy, listRepresentation());
  }
}
