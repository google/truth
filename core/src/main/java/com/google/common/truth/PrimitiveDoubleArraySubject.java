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


import com.google.common.primitives.Doubles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Subject to handle testing propositions for {@code double[]}.
 *
 * Note: this class deprecates some common methods because the operation of equality and
 * comparison on floating point numbers requires additional specification.  Alternatives
 * equality tests are provided.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class PrimitiveDoubleArraySubject
    extends AbstractArraySubject<PrimitiveDoubleArraySubject, double[]> {
  public PrimitiveDoubleArraySubject(FailureStrategy failureStrategy, double[] o) {
    super(failureStrategy, o);
  }

  @Override protected String underlyingType() {
    return "double";
  }

  @Override protected List<Double> listRepresentation() {
    return Doubles.asList(getSubject());
  }

  /**
   * This form is unsafe for double-precision floating point types, and will throw an
   * {@link UnsupportedOperationException}.
   *
   * @deprecated use {@link #isEqualTo(Object, double)}
   */
  @Deprecated
  @Override public void isEqualTo(Object expected) {
    throw new UnsupportedOperationException("Comparing raw equality of doubles is unsafe, "
    		+ "use isEqualTo(double[] array, double tolerance) instead.");
  }

  /**
   * A proposition that the provided double[] is an array of the same length and type, and
   * contains elements such that each element in {@code expected} is equal to each element
   * in the subject, and in the same position.
   */
  public void isEqualTo(Object expected, double tolerance) {
    double[] actual = getSubject();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      double[] expectedArray = (double[]) expected;
      if (expectedArray.length != actual.length) {
        failWithRawMessage("Arrays are of different lengths."
        		+ "expected: %s, actual %s", Arrays.asList(expectedArray), Arrays.asList(actual));
      }
      List<Integer> unequalIndices = new ArrayList<Integer>();
      for (int i = 0; i < expectedArray.length; i++) {
        if (!MathUtil.equals(actual[i], expectedArray[i], tolerance)) {
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
   * @deprecated use {@link #isNotEqualTo(Object, double)}
   */
  @Deprecated
  @Override public void isNotEqualTo(Object expected) {
    throw new UnsupportedOperationException("Comparing raw equality of floats is unsafe, "
        + "use isNotEqualTo(double[] array, float tolerance) instead.");
  }

  /**
   * A proposition that the provided double[] is not an array of the same length or type, or
   * has at least one element that does not pass an equality test within the given tolerance.
   */
  public void isNotEqualTo(Object expectedArray, double tolerance) {
    double[] actual = getSubject();
    try {
      double[] expected = (double[]) expectedArray;
      if (actual == expected) {
        failWithRawMessage("%s unexpectedly equal to %s.",
            getDisplaySubject(),  Doubles.asList(expected));
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
        failWithRawMessage("%s unexpectedly equal to %s.",
            getDisplaySubject(),  Doubles.asList(expected));
      }
    } catch (ClassCastException ignored) {} // Unequal since they are of different types.
  }

  // TODO(cgruber): Extend to a List<Float> type that handles specialized float equality
  //     including tolerances. But diable this for now, since it will nearly always be
  //     incorrect to simply treat a list of floats and do normal set operations that are
  //     based on bare comparisons.
  @SuppressWarnings("unused")
  private ListSubject<?, Double, List<Double>> asList() {
    return ListSubject.create(failureStrategy, listRepresentation());
  }

}
