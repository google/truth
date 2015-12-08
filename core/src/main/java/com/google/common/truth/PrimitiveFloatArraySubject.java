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
import static com.google.common.truth.FloatSubject.checkTolerance;

import com.google.common.primitives.Floats;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * A Subject to handle testing propositions for {@code float[]}.
 *
 * <p>Note: this class deprecates some common methods because the operation of equality and
 * comparison on floating point numbers requires additional specification.  Alternative
 * equality tests are provided.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@CheckReturnValue
public class PrimitiveFloatArraySubject
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
    return Floats.asList(getSubject());
  }

  /**
   * This form is unsafe for floating point types, and will throw an
   * {@link UnsupportedOperationException}.
   *
   * @deprecated use {@link #isWithin(float)}
   */
  @Deprecated
  @Override
  public void isEqualTo(Object expected) {
    throw new UnsupportedOperationException(
        "Comparing raw equality of floats is unsafe, "
            + "use isEqualTo(float[] array, float tolerance) instead.");
  }

  /**
   * A proposition that the provided float[] is an array of the same length and type, and
   * contains elements such that each element in {@code expected} is equal to each element
   * in the subject, and in the same position.
   *
   * <p>Behaviour for non-finite values ({@link Float#POSITIVE_INFINITY POSITIVE_INFINITY},
   * {@link Float#NEGATIVE_INFINITY NEGATIVE_INFINITY}, and {@link Float#NaN NaN}) is as follows:
   * If the subject and the object of the assertion are the same array the test will pass. If not
   * (including if one is a clone of the other) then non-finite values are considered not equal so
   * the any non-finite value in either argument will cause the test to fail.
   *
   * @deprecated use {@link #hasValuesWithin(float)}, noting the different behaviour for non-finite
   *     values
   */
  @Deprecated
  public void isEqualTo(Object expected, float tolerance) {
    float[] actual = getSubject();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      float[] expectedArray = (float[]) expected;
      if (expectedArray.length != actual.length) {
        failWithRawMessage(
            "Arrays are of different lengths. expected: %s, actual %s",
            Floats.asList(expectedArray),
            Floats.asList(actual));
      }
      List<Integer> unequalIndices = new ArrayList<Integer>();
      for (int i = 0; i < expectedArray.length; i++) {
        if (!MathUtil.equals(actual[i], expectedArray[i], tolerance)) {
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
   * This form is unsafe for floating point types, and will throw an
   * {@link UnsupportedOperationException}.
   *
   * @deprecated use {@link #isNotWithin(float)}
   */
  @Deprecated
  @Override
  public void isNotEqualTo(Object expected) {
    throw new UnsupportedOperationException(
        "Comparing raw equality of floats is unsafe, "
            + "use isNotEqualTo(float[] array, float tolerance) instead.");
  }

  /**
   * A proposition that the provided float[] is not an array of the same length or type, or
   * has at least one element that does not pass an equality test within the given tolerance.
   *
   * <p>Behaviour for non-finite values ({@link Float#POSITIVE_INFINITY POSITIVE_INFINITY},
   * {@link Float#NEGATIVE_INFINITY NEGATIVE_INFINITY}, and {@link Float#NaN NaN}) is as follows:
   * If the subject and the object of the assertion are the same array the test will fail. If not
   * (including if one is a clone of the other) then non-finite values are considered not equal so
   * the any non-finite value in either argument will cause the test to pass.
   *
   * @deprecated use {@link #hasValuesNotWithin(float)}, noting the different behaviour for
   *     non-finite values
   */
  @Deprecated
  public void isNotEqualTo(Object expectedArray, float tolerance) {
    float[] actual = getSubject();
    try {
      float[] expected = (float[]) expectedArray;
      if (actual == expected) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", getDisplaySubject(), Floats.asList(expected));
      }
      if (expected.length != actual.length) {
        return; // Unequal-lengthed arrays are not equal.
      }
      List<Integer> unequalIndices = new ArrayList<Integer>();
      for (int i = 0; i < expected.length; i++) {
        if (!MathUtil.equals(actual[i], expected[i], tolerance)) {
          unequalIndices.add(i);
        }
      }
      if (unequalIndices.isEmpty()) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", getDisplaySubject(), Floats.asList(expected));
      }
    } catch (ClassCastException ignored) {
      // Unequal since they are of different types.
    }
  }

  /**
   * A partially specified proposition about an approximate relationship to a {@code float[]}
   * subject using a tolerance.
   */
  public abstract class TolerantPrimitiveFloatArrayComparison {

    // Prevent subclassing outside of this class
    private TolerantPrimitiveFloatArrayComparison() {}

    /**
     * Fails if the values in the subject wer expected to be within the tolerance of the given
     * values but were not <i>or</i> if they wer expected <i>not</i> to be within the tolerance but
     * were. The expectation, subject, and tolerance are all specified earlier in the fluent call
     * chain.
     */
    public abstract void of(float[] expectedFloats);

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on
     *     TolerantPrimitiveFloatArrayComparison. If you meant to compare float arrays, use
     *     {@link #of(float[])} instead.
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object o) {
      throw new UnsupportedOperationException(
          "If you meant to compare floats, use .of(float[]) instead.");
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
   * {@code tolerance} of each other, that is
   * {@code assertThat(actual[i]).isWithin(tolerance).of(expected[i])} passes for all {@code i}
   * (see the {@link FloatSubject#isWithin isWithin} assertion for floats).
   *
   * <p>The check will fail if any value in either the subject array or the object array is
   * {@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, or {@link Float#NaN}.
   *
   * @param tolerance an inclusive upper bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not
   *     {@link Float#NaN}, {@link Float#POSITIVE_INFINITY}, or negative, including {@code -0.0f}
   */
  public TolerantPrimitiveFloatArrayComparison hasValuesWithin(final float tolerance) {
    return new TolerantPrimitiveFloatArrayComparison() {

      @Override
      public void of(float[] expected) {
        checkTolerance(tolerance);
        float[] actual = checkNotNull(getSubject());
        if (actual.length != expected.length) {
          failWithRawMessage(
              "Not true that %s has values within %s of <%s>. Expected length <%s> but got <%s>",
              getDisplaySubject(),
              tolerance,
              Floats.asList(expected),
              expected.length,
              actual.length);
        }
        List<Integer> mismatches = new ArrayList<Integer>();
        for (int i = 0; i < expected.length; i++) {
          if (!MathUtil.equals(actual[i], expected[i], tolerance)) {
            mismatches.add(i);
          }
        }
        if (!mismatches.isEmpty()) {
          failWithBadResults(
              "has values within " + tolerance + " of",
              Floats.asList(expected),
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
   * {@code i} (see the {@link FloatSubject#isNotWithin isNotWithin} assertion for floats).
   *
   * <p>In the case (b), a pair of subject and object values will not cause the test to pass if
   * either of them is {@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, or
   * {@link Float#NaN}.
   *
   * @param tolerance an exclusive lower bound on the difference between the subject and object
   *     allowed by the check, which must be a non-negative finite value, i.e. not
   *     {@code Float.NaN}, {@code Float.POSITIVE_INFINITY}, or negative, including {@code -0.0f}
   */
  public TolerantPrimitiveFloatArrayComparison hasValuesNotWithin(final float tolerance) {
    return new TolerantPrimitiveFloatArrayComparison() {

      @Override
      public void of(float[] expected) {
        checkTolerance(tolerance);
        float[] actual = checkNotNull(getSubject());
        if (expected.length != actual.length) {
          // By the method contract, the assertion passes if the lengths are different. This is so
          // that isNotWithin behaves like isNotEqualTo with a tolerance (and different handling of
          // non-finite values).
          return;
        }
        boolean pass = false;
        for (int i = 0; i < expected.length; i++) {
          if (MathUtil.notEquals(actual[i], expected[i], tolerance)) {
            pass = true;
            break;
          }
        }
        if (!pass) {
          fail("has values not within " + tolerance + " of", Floats.asList(expected));
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
