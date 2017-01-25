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
import static com.google.common.truth.Correspondence.tolerance;
import static com.google.common.truth.FloatSubject.checkTolerance;
import static com.google.common.truth.MathUtil.equalWithinTolerance;
import static com.google.common.truth.MathUtil.notEqualWithinTolerance;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Floats;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A Subject to handle testing propositions for {@code float[]}.
 *
 * <p>Note: this class deprecates some common methods because the operation of equality and
 * comparison on floating point numbers requires additional specification. Alternative equality
 * tests are provided.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public final class PrimitiveFloatArraySubject
    extends AbstractArraySubject<PrimitiveFloatArraySubject, float[]> {
  PrimitiveFloatArraySubject(FailureStrategy failureStrategy, @Nullable float[] o) {
    super(failureStrategy, o);
  }

  @Override
  protected String underlyingType() {
    return "float";
  }

  @Override
  protected List<Float> listRepresentation() {
    return Floats.asList(actual());
  }

  /**
   * This form is unsafe for floating point types, and will throw an {@link
   * UnsupportedOperationException}.
   *
   * @deprecated use either {@code
   *     usingTolerance(someTolerance).containsExactly(someValues).inOrder()} or {@code
   *     usingExactEquality().containsExactly(someValues).inOrder()}
   */
  @Deprecated
  @Override
  public void isEqualTo(Object expected) {
    throw new UnsupportedOperationException(
        "Comparing raw equality of floats is often unsafe. Use either "
            + "usingTolerance(someTolerance).containsExactly(someValues).inOrder() to compare with"
            + "a tolerance or usingExactEquality().containsExactly(someValues).inOrder() if you"
            + "really want exact equality instead.");
  }

  /**
   * A proposition that the provided float[] is an array of the same length and type, and contains
   * elements such that each element in {@code expected} is equal to each element in the subject,
   * and in the same position.
   *
   * <p>Behaviour for non-finite values ({@link Float#POSITIVE_INFINITY POSITIVE_INFINITY}, {@link
   * Float#NEGATIVE_INFINITY NEGATIVE_INFINITY}, and {@link Float#NaN NaN}) is as follows: If the
   * subject and the object of the assertion are the same array, the test will pass. If not
   * (including if one is a clone of the other) then non-finite values are considered not equal so
   * the any non-finite value in either argument will cause the test to fail.
   *
   * @deprecated use {@code usingTolerance(someTolerance).containsExactly(someValues).inOrder()},
   *     noting the different behaviour for non-finite values
   */
  @Deprecated
  public void isEqualTo(Object expected, float tolerance) {
    float[] actual = actual();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      float[] expectedArray = (float[]) expected;
      if (expectedArray.length != actual.length) {
        failWithRawMessage(
            "Arrays are of different lengths. expected: %s, actual %s",
            Floats.asList(expectedArray), Floats.asList(actual));
      }
      List<Integer> unequalIndices = new ArrayList<Integer>();
      for (int i = 0; i < expectedArray.length; i++) {
        if (!equalWithinTolerance(actual[i], expectedArray[i], tolerance)) {
          unequalIndices.add(i);
        }
      }

      if (!unequalIndices.isEmpty()) {
        fail("is equal to", Floats.asList(expectedArray));
      }
    } catch (ClassCastException e) {
      failWithBadType(expected);
    }
  }

  /**
   * This form is unsafe for floating point types, and will throw an {@link
   * UnsupportedOperationException}.
   *
   * @deprecated If you really want this, convert the array to a list, possibly using {@link
   *     Floats#asList}, and do the assertion on that, e.g. {@code
   *     assertThat(asList(actualFloatArray)).isNotEqualTo(asList(expectedFloatArray));}.
   */
  @Deprecated
  @Override
  public void isNotEqualTo(Object expected) {
    throw new UnsupportedOperationException(
        "Comparing raw equality of floats is unsafe, "
            + "use isNotEqualTo(float[] array, float tolerance) instead.");
  }

  /**
   * A proposition that the provided float[] is not an array of the same length or type, or has at
   * least one element that does not pass an equality test within the given tolerance.
   *
   * <p>Behaviour for non-finite values ({@link Float#POSITIVE_INFINITY POSITIVE_INFINITY}, {@link
   * Float#NEGATIVE_INFINITY NEGATIVE_INFINITY}, and {@link Float#NaN NaN}) is as follows: If the
   * subject and the object of the assertion are the same array, the test will fail. If not
   * (including if one is a clone of the other) then non-finite values are considered not equal so
   * the any non-finite value in either argument will cause the test to pass.
   *
   * @deprecated Write a for loop over the values looking for mismatches (see this implementation
   *     for an example)
   */
  @Deprecated
  public void isNotEqualTo(Object expectedArray, float tolerance) {
    float[] actual = actual();
    try {
      float[] expected = (float[]) expectedArray;
      if (actual == expected) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", actualAsString(), Floats.asList(expected));
      }
      if (expected.length != actual.length) {
        return; // Unequal-lengthed arrays are not equal.
      }
      List<Integer> unequalIndices = new ArrayList<Integer>();
      for (int i = 0; i < expected.length; i++) {
        if (!equalWithinTolerance(actual[i], expected[i], tolerance)) {
          unequalIndices.add(i);
        }
      }
      if (unequalIndices.isEmpty()) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", actualAsString(), Floats.asList(expected));
      }
    } catch (ClassCastException ignored) {
      // Unequal since they are of different types.
    }
  }

  /**
   * A partially specified proposition about an approximate relationship to a {@code float[]}
   * subject using a tolerance.
   */
  public abstract static class TolerantPrimitiveFloatArrayComparison {

    // Prevent subclassing outside of this class
    private TolerantPrimitiveFloatArrayComparison() {}

    /**
     * Fails if the values in the subject were expected to be within the tolerance of the given
     * values but were not <i>or</i> if they were expected <i>not</i> to be within the tolerance but
     * were. The subject and tolerance are specified earlier in the fluent call chain.
     */
    public void of(float... expected) {
      ofElementsIn(Floats.asList(expected));
    }

    /**
     * Fails if the values in the subject were expected to be within the tolerance of the given
     * values but were not <i>or</i> if they were expected <i>not</i> to be within the tolerance but
     * were. The subject and tolerance are specified earlier in the fluent call chain. The values
     * will be cast to floats if necessary, which might lose precision.
     */
    public abstract void ofElementsIn(Iterable<? extends Number> expected);

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on
     *     TolerantPrimitiveFloatArrayComparison. If you meant to compare float arrays, use {@link
     *     #of} or {@link #ofElementsIn} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object o) {
      throw new UnsupportedOperationException(
          "If you meant to compare float arrays, use .of() or .ofElementsIn() instead.");
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#hashCode()} is not supported on
     *     TolerantPrimitiveFloatArrayComparison
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
   * {@code tolerance} of each other, that is {@code
   * assertThat(actual[i]).isWithin(tolerance).of(expected[i])} passes for all {@code i} (see the
   * {@link FloatSubject#isWithin isWithin} assertion for floats).
   *
   * <p>The check will fail if any value in either the subject array or the object array is {@link
   * Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, or {@link Float#NaN}.
   *
   * @param tolerance an inclusive upper bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not {@link
   *     Float#NaN}, {@link Float#POSITIVE_INFINITY}, or negative, including {@code -0.0f}
   */
  public TolerantPrimitiveFloatArrayComparison hasValuesWithin(final float tolerance) {
    return new TolerantPrimitiveFloatArrayComparison() {

      @Override
      public void ofElementsIn(Iterable<? extends Number> expected) {
        checkTolerance(tolerance);
        float[] actual = checkNotNull(actual());
        List<Integer> mismatches = new ArrayList<Integer>();
        int expectedCount = 0;
        for (Number expectedValue : expected) {
          // if expected is longer than actual, we can skip the excess values: this case is covered
          // by the length check below
          if (expectedCount < actual.length
              && !equalWithinTolerance(
                  actual[expectedCount], expectedValue.floatValue(), tolerance)) {
            mismatches.add(expectedCount);
          }
          expectedCount++;
        }
        if (actual.length != expectedCount) {
          failWithRawMessage(
              "Not true that %s has values within %s of <%s>. Expected length <%s> but got <%s>",
              actualAsString(),
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
   * in each array are finite values not within {@code tolerance} of each other, that is {@code
   * assertThat(actual[i]).isNotWithin(tolerance).of(expected[i])} passes for at least one {@code i}
   * (see the {@link FloatSubject#isNotWithin isNotWithin} assertion for floats).
   *
   * <p>In the case (b), a pair of subject and object values will not cause the test to pass if
   * either of them is {@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, or {@link
   * Float#NaN}.
   *
   * @param tolerance an exclusive lower bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not {@code
   *     Float.NaN}, {@code Float.POSITIVE_INFINITY}, or negative, including {@code -0.0f}
   * @deprecated Write a for loop over the values looking for mismatches (see this implementation
   *     for an example)
   */
  @Deprecated
  public TolerantPrimitiveFloatArrayComparison hasValuesNotWithin(
      final float tolerance) {
    return new TolerantPrimitiveFloatArrayComparison() {

      @Override
      public void ofElementsIn(Iterable<? extends Number> expected) {
        checkTolerance(tolerance);
        float[] actual = checkNotNull(actual());
        int expectedCount = 0;
        for (Number expectedValue : expected) {
          // if expected is longer than actual, we can skip the excess values: this case is covered
          // by the length check below
          if (expectedCount < actual.length
              && notEqualWithinTolerance(
                  actual[expectedCount], expectedValue.floatValue(), tolerance)) {
            return;
          }
          expectedCount++;
        }
        // By the method contract, the assertion passes if the lengths are different. This is so
        // that hasValuesNotWithin behaves like isNotEqualTo with a tolerance (and different
        // handling of non-finite values).
        if (actual.length == expectedCount) {
          fail("has values not within " + tolerance + " of", Iterables.toString(expected));
        }
      }
    };
  }

  /**
   * Starts a method chain for a test proposition in which the actual values (i.e. the elements of
   * the array under test) are compared to expected elements using a {@link Correspondence} which
   * considers values to correspond if they are finite values within {@code tolerance} of each
   * other. The proposition is actually executed by continuing the method chain. For example:
   * <pre>   {@code
   * assertThat(actualFloatArray).usingTolerance(1.0e-5f).contains(3.14159f);}</pre>
   *
   * <ul>
   * <li>It does not consider values to correspond if either value is infinite or NaN.
   * <li>It considers {@code -0.0f} to be within any tolerance of {@code 0.0f}.
   * <li>The expected values provided later in the chain will be {@link Number} instances which will
   *     be converted to doubles, which may result in a loss of precision for some numeric types.
   * <li>The subsequent methods in the chain may throw a {@link NullPointerException} if any
   *     expected {@link Number} instance is null.
   * </ul>
   *
   * @param tolerance an inclusive upper bound on the difference between the double values of the
   *     actual and expected numbers, which must be a non-negative finite value, i.e. not {@link
   *     Float#NaN}, {@link Float#POSITIVE_INFINITY}, or negative, including {@code -0.0f}
   */
  public IterableSubject.UsingCorrespondence<Number, Number> usingTolerance(float tolerance) {
    return new IterableSubject(failureStrategy, listRepresentation())
        .comparingElementsUsing(tolerance(tolerance));
  }

  private static final Correspondence<Float, Number> EXACT_EQUALITY_CORRESPONDENCE =
      new Correspondence<Float, Number>() {

        @Override
        public boolean compare(Float actual, Number expected) {
          return actual.equals(checkedToFloat(expected));
        }

        @Override
        public String toString() {
          return "is exactly equal to";
        }
      };

  private static float checkedToFloat(Number expected) {
    checkNotNull(expected);
    checkArgument(
        expected instanceof Float || expected instanceof Integer || expected instanceof Long,
        "Expected value in assertion using exact double equality was of unsupported type %s "
            + "(it may not have an exact double representation)",
        expected.getClass());
    if (expected instanceof Integer) {
      checkArgument(
          Math.abs((Integer) expected) <= 1 << 24,
          "Expected value %s in assertion using exact float equality was an int with an absolute "
              + "value greater than 2^24 which has no exact float representation",
          expected);
    }
    if (expected instanceof Long) {
      checkArgument(
          Math.abs((Long) expected) <= 1L << 24,
          "Expected value %s in assertion using exact float equality was a long with an absolute "
              + "value greater than 2^24 which has no exact float representation",
          expected);
    }
    return expected.floatValue();
  }

  /**
   * Starts a method chain for a test proposition in which the actual values (i.e. the elements of
   * the array under test) are compared to expected elements using a {@link Correspondence} which
   * considers values to correspond if they are exactly equal, with equality defined by {@link
   * Float#equals}. This method is <i>not</i> recommended when the code under test is doing any
   * kind of arithmetic: use {@link #usingTolerance} with a suitable tolerance in that case.
   * (Remember that the exact result of floating point arithmetic is sensitive to apparently trivial
   * changes such as replacing {@code (a + b) + c} with {@code a + (b + c)}, and that unless {@code
   * strictfp} is in force even the result of {@code (a + b) + c} is sensitive to the JVM's choice
   * of precision for the intermediate result.) This method is recommended when the code under test
   * is specified as either copying a value without modification from its input or returning a
   * well-defined literal or constant value. The proposition is actually executed by continuing the
   * method chain. For example:
   * <pre>   {@code
   * assertThat(actualFloatArray).usingExactEquality().contains(3.14159f);}</pre>
   *
   * <p>For convenience, some subsequent methods accept expected values as {@link Number} instances.
   * These numbers must be either of type {@link Float}, {@link Integer}, or {@link Long}, and if
   * they are {@link Integer} or {@link Long} then their absolute values must not exceed 2^24 which
   * is 16,777,216. (This restriction ensures that the expected values have exact {@link Float}
   * representations: using exact equality makes no sense if they do not.)
   *
   * <ul>
   *   <li>It considers {@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, and
   *       {@link Float#NaN} to be equal to themselves.
   *   <li>It does <i>not</i> consider {@code -0.0f} to be equal to {@code 0.0f}.
   *   <li>The subsequent methods in the chain may throw a {@link NullPointerException} if any
   *       expected {@link Float} instance is null.
   * </ul>
   */
  public IterableSubject.UsingCorrespondence<Float, Number> usingExactEquality() {
    return new IterableSubject(failureStrategy, listRepresentation())
        .comparingElementsUsing(EXACT_EQUALITY_CORRESPONDENCE);
  }
}
