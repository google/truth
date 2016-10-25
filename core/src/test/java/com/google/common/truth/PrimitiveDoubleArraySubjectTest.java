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

import static com.google.common.truth.Truth.assertThat;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.nextAfter;
import static org.junit.Assert.fail;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Longs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PrimitiveDoubleArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveDoubleArraySubjectTest {
  private static final double DEFAULT_TOLERANCE = 0.000005d;

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Default_Fail() {
    try {
      assertThat(array(2.2d, 5.4d)).isEqualTo(array(2.2d, 5.4d));
      throw new Error("Expected to throw.");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo() {
    assertThat(array(2.2d, 5.4d)).isEqualTo(array(2.2d, 5.4d), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_ApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d))
        .isEqualTo(
            array(2.2d, nextAfter(3.3d + DEFAULT_TOLERANCE, NEGATIVE_INFINITY)), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_FailNotQuiteApproximatelyEquals() {
    double roughly3point3 = nextAfter(3.3d + DEFAULT_TOLERANCE, POSITIVE_INFINITY);
    try {
      assertThat(array(2.2d, 3.3d)).isEqualTo(array(2.2d, roughly3point3), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> is equal to <[2.2, " + roughly3point3 + "]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_DifferentOrder() {
    try {
      assertThat(array(2.2d, 3.3d)).isEqualTo(array(3.3d, 2.2d), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <(double[]) [2.2, 3.3]> is equal to <[3.3, 2.2]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_Longer() {
    try {
      assertThat(array(2.2d, 3.3d)).isEqualTo(array(2.2d, 3.3d, 1.1d), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Arrays are of different lengths. expected: [2.2, 3.3, 1.1], actual [2.2, 3.3]");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_Shorter() {
    try {
      assertThat(array(2.2d, 3.3d)).isEqualTo(array(2.2d), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Arrays are of different lengths. expected: [2.2], actual [2.2, 3.3]");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_NotAnArray() {
    try {
      assertThat(array(2.2d, 3.3d, 4.4d)).isEqualTo(new Object(), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains("Incompatible types compared. expected: Object, actual: double[]");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_Infinity() {
    try {
      assertThat(array(2.2d, POSITIVE_INFINITY))
          .isEqualTo(array(2.2d, POSITIVE_INFINITY), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <(double[]) [2.2, Infinity]> is equal to <[2.2, Infinity]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_SameInfinity() {
    double[] same = array(2.2d, POSITIVE_INFINITY);
    assertThat(same).isEqualTo(same, DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_OneInfinity() {
    try {
      assertThat(array(2.2d, 3.3d)).isEqualTo(array(2.2d, POSITIVE_INFINITY), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <(double[]) [2.2, 3.3]> is equal to <[2.2, Infinity]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_LongerOneInfinity() {
    try {
      assertThat(array(2.2d, 3.3d)).isEqualTo(array(POSITIVE_INFINITY), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Arrays are of different lengths. expected: [Infinity], actual [2.2, 3.3]");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_NaN() {
    try {
      assertThat(array(NaN)).isEqualTo(array(NaN), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <(double[]) [NaN]> is equal to <[NaN]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Default_Fail() {
    try {
      assertThat(array(2.2d, 5.4d)).isNotEqualTo(array(5.4d, 2.2d));
      throw new Error("Expected to throw.");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_DifferentOrder() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(3.3d, 2.2d), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Longer() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d, 3.3d, 1.1d), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Shorter() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_DifferentTypes() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(new Object(), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_FailEquals() {
    try {
      assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d, 3.3d), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<(double[]) [2.2, 3.3]> unexpectedly equal to [2.2, 3.3].");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_FailApproximatelyEquals() {
    double roughly3point3 = nextAfter(3.3d + DEFAULT_TOLERANCE, NEGATIVE_INFINITY);
    try {
      assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d, roughly3point3), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "<(double[]) [2.2, 3.3]> unexpectedly equal to [2.2, " + roughly3point3 + "].");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_NotQuiteApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d))
        .isNotEqualTo(
            array(2.2d, nextAfter(3.3d + DEFAULT_TOLERANCE, POSITIVE_INFINITY)), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_FailSame() {
    try {
      double[] same = array(2.2d, 3.3d);
      assertThat(same).isNotEqualTo(same, DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<(double[]) [2.2, 3.3]> unexpectedly equal to [2.2, 3.3].");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Fail_Infinity() {
    assertThat(array(2.2d, POSITIVE_INFINITY))
        .isNotEqualTo(array(2.2d, POSITIVE_INFINITY), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Fail_SameInfinity() {
    try {
      double[] same = array(2.2d, POSITIVE_INFINITY);
      assertThat(same).isNotEqualTo(same, DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("<(double[]) [2.2, Infinity]> unexpectedly equal to [2.2, Infinity].");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_OneInfinity() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d, POSITIVE_INFINITY), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_LongerOneInfinity() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(POSITIVE_INFINITY), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Fail_NaN() {
    assertThat(array(NaN)).isNotEqualTo(array(NaN), DEFAULT_TOLERANCE);
  }

  @Test
  public void hasValuesWithinOf() {
    assertThat(array(2.2d, 5.4d)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2d, 5.4d);
  }

  @Test
  public void hasValuesWithinOf_ApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .of(2.2d, nextAfter(3.3d + DEFAULT_TOLERANCE, NEGATIVE_INFINITY));
  }

  @Test
  public void hasValuesWithinOf_FailNotQuiteApproximatelyEquals() {
    double roughly3point3 = nextAfter(3.3d + DEFAULT_TOLERANCE, POSITIVE_INFINITY);
    try {
      assertThat(array(2.2d, 3.3d)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2d, roughly3point3);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, "
                  + roughly3point3
                  + "]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_DifferentOrder() {
    try {
      assertThat(array(2.2d, 3.3d)).hasValuesWithin(DEFAULT_TOLERANCE).of(3.3d, 2.2d);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[3.3, 2.2]>."
                  + " It differs at indexes <[0, 1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_Longer() {
    try {
      assertThat(array(2.2d, 3.3d)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2d, 3.3d, 1.1d);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, 3.3, 1.1]>."
                  + " Expected length <3> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_Shorter() {
    try {
      assertThat(array(2.2d, 3.3d)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2d);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2]>."
                  + " Expected length <1> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_Infinity() {
    try {
      assertThat(array(2.2d, POSITIVE_INFINITY))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .of(2.2d, POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, Infinity]> has values within 5.0E-6 of"
                  + " <[2.2, Infinity]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_SameInfinity() {
    double[] same = array(2.2d, POSITIVE_INFINITY);
    try {
      assertThat(same).hasValuesWithin(DEFAULT_TOLERANCE).of(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, Infinity]> has values within 5.0E-6 of"
                  + " <[2.2, Infinity]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_OneInfinity() {
    try {
      assertThat(array(2.2d, 3.3d)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2d, POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, Infinity]>."
                  + " It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_LongerOneInfinity() {
    try {
      assertThat(array(2.2d, 3.3d)).hasValuesWithin(DEFAULT_TOLERANCE).of(POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[Infinity]>."
                  + " Expected length <1> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_NaN() {
    try {
      assertThat(array(NaN)).hasValuesWithin(DEFAULT_TOLERANCE).of(NaN);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [NaN]> has values within 5.0E-6 of <[NaN]>."
                  + " It differs at indexes <[0]>");
    }
  }

  @Test
  public void hasValuesWithinOf_NullSubject() {
    double[] nullArray = null;
    try {
      assertThat(nullArray).hasValuesWithin(DEFAULT_TOLERANCE).of(3.3d, 2.2d);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOf_NullObbject() {
    double[] nullArray = null;
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesWithin(DEFAULT_TOLERANCE).of(nullArray);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOf_NegativeTolerance() {
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesWithin(-0.001d).of(3.3d, 2.2d);
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_doubles() {
    assertThat(array(2.2d, 5.4d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, 5.4d));
  }

  @Test
  public void hasValuesWithinOfElementsIn_floats() {
    assertThat(array(2.2d, 5.4d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Floats.asList(2.2f, 5.4f));
  }

  @Test
  public void hasValuesWithinOfElementsIn_longs() {
    assertThat(array(2.0d, 5.0d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Longs.asList(2L, 5L));
  }

  @Test
  public void hasValuesWithinOfElementsIn_ApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, nextAfter(3.3d + DEFAULT_TOLERANCE, NEGATIVE_INFINITY)));
  }

  @Test
  public void hasValuesWithinOfElementsIn_FailNotQuiteApproximatelyEquals() {
    double roughly3point3 = nextAfter(3.3d + DEFAULT_TOLERANCE, POSITIVE_INFINITY);
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d, roughly3point3));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, "
                  + roughly3point3
                  + "]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_DifferentOrder() {
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(3.3d, 2.2d));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[3.3, 2.2]>."
                  + " It differs at indexes <[0, 1]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Longer() {
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d, 3.3d, 1.1d));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, 3.3, 1.1]>."
                  + " Expected length <3> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Shorter() {
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2]>."
                  + " Expected length <1> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Infinity() {
    try {
      assertThat(array(2.2d, POSITIVE_INFINITY))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d, POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, Infinity]> has values within 5.0E-6 of"
                  + " <[2.2, Infinity]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_OneInfinity() {
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d, POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, Infinity]>."
                  + " It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_LongerOneInfinity() {
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values within 5.0E-6 of <[Infinity]>."
                  + " Expected length <1> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_NaN() {
    try {
      assertThat(array(NaN)).hasValuesWithin(DEFAULT_TOLERANCE).ofElementsIn(Doubles.asList(NaN));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [NaN]> has values within 5.0E-6 of <[NaN]>."
                  + " It differs at indexes <[0]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_NullSubject() {
    double[] nullArray = null;
    try {
      assertThat(nullArray)
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(3.3d, 2.2d));
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_NullObject() {
    Iterable<Number> nullIterable = null;
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesWithin(DEFAULT_TOLERANCE).ofElementsIn(nullIterable);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_NegativeTolerance() {
    try {
      assertThat(array(3.3d, 2.2d))
          .hasValuesWithin(-0.001d)
          .ofElementsIn(Doubles.asList(3.3d, 2.2d));
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_DifferentOrder() {
    assertThat(array(2.2d, 3.3d)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(3.3d, 2.2d);
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_DifferentLengths() {
    assertThat(array(2.2d, 3.3d)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(2.2d, 3.3d, 1.1d);
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_FailEquals() {
    try {
      assertThat(array(2.2d, 3.3d)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(2.2d, 3.3d);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, 3.3]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_FailApproximatelyEquals() {
    double roughly3point3 = nextAfter(3.3d + DEFAULT_TOLERANCE, NEGATIVE_INFINITY);
    try {
      assertThat(array(2.2d, 3.3d)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(2.2d, roughly3point3);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, "
                  + roughly3point3
                  + "]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NotQuiteApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .of(2.2d, nextAfter(3.3d + DEFAULT_TOLERANCE, POSITIVE_INFINITY));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_FailSame() {
    try {
      double[] same = array(2.2d, 3.3d);
      assertThat(same).hasValuesNotWithin(DEFAULT_TOLERANCE).of(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, 3.3]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_Infinity() {
    try {
      assertThat(array(2.2d, POSITIVE_INFINITY))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .of(2.2d, POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, Infinity]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_SameInfinity() {
    try {
      double[] same = array(2.2d, POSITIVE_INFINITY);
      assertThat(same).hasValuesNotWithin(DEFAULT_TOLERANCE).of(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, Infinity]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_OneInfinity() {
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .of(2.2d, POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_LongerOneInfinity() {
    assertThat(array(2.2d, 3.3d)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(POSITIVE_INFINITY);
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_NaN() {
    try {
      assertThat(array(NaN)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(NaN);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <(double[]) [NaN]> has values not within 5.0E-6 of <[NaN]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NullSubject() {
    double[] nullArray = null;
    try {
      assertThat(nullArray).hasValuesNotWithin(DEFAULT_TOLERANCE).of(3.3d, 2.2d);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NullObject() {
    double[] nullArray = null;
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(nullArray);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NegativeTolerance() {
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesNotWithin(-0.001d).of(3.3d, 2.2d);
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_DifferentOrderDoubles() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(3.3d, 2.2d));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_DifferentOrderFloats() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Floats.asList(3.3f, 2.2f));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_DifferentOrderLongs() {
    assertThat(array(2d, 3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Longs.asList(3L, 2L));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_DifferentLengths() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, 3.3d, 1.1d));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_FailEquals() {
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d, 3.3d));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, 3.3]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_FailApproximatelyEquals() {
    double roughly3point3 = nextAfter(3.3d + DEFAULT_TOLERANCE, NEGATIVE_INFINITY);
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d, roughly3point3));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, "
                  + roughly3point3
                  + "]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NotQuiteApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, nextAfter(3.3d + DEFAULT_TOLERANCE, POSITIVE_INFINITY)));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_Fail_Infinity() {
    try {
      assertThat(array(2.2d, POSITIVE_INFINITY))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d, POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, Infinity]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_OneInfinity() {
    try {
      assertThat(array(2.2d, 3.3d))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(2.2d, POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(double[]) [2.2, 3.3]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_LongerOneInfinity() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(POSITIVE_INFINITY));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_Fail_NaN() {
    try {
      assertThat(array(NaN))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(NaN));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <(double[]) [NaN]> has values not within 5.0E-6 of <[NaN]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NullSubject() {
    double[] nullArray = null;
    try {
      assertThat(nullArray)
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(3.3d, 2.2d));
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NullObject() {
    Iterable<Number> nullIterable = null;
    try {
      assertThat(array(3.3d, 2.2d))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(nullIterable);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NegativeTolerance() {
    try {
      assertThat(array(3.3d, 2.2d))
          .hasValuesNotWithin(-0.001d)
          .ofElementsIn(Doubles.asList(3.3d, 2.2d));
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  public void usingTolerance_contains_success() {
    assertThat(array(1.0, nextAfter(2.0 + DEFAULT_TOLERANCE, NEGATIVE_INFINITY), 3.0))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2.0);
  }

  @Test
  public void usingTolerance_contains_successWithExpectedLong() {
    assertThat(array(1.0, nextAfter(2.0 + DEFAULT_TOLERANCE, NEGATIVE_INFINITY), 3.0))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2L);
  }

  @Test
  public void usingTolerance_contains_failure() {
    double justOverTwoPlusTolerance = nextAfter(2.0 + DEFAULT_TOLERANCE, POSITIVE_INFINITY);
    try {
      assertThat(array(1.0, justOverTwoPlusTolerance, 3.0))
          .usingTolerance(DEFAULT_TOLERANCE)
          .contains(2.0);
      fail("Expected AssertionError to be thrown but wasn't");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <[1.0, "
                  + justOverTwoPlusTolerance
                  + ", 3.0]> contains at least one element that is a finite number within "
                  + DEFAULT_TOLERANCE
                  + " of <2.0>");
    }
  }

  @Test
  public void usingTolerance_contains_failureWithInfinity() {
    try {
      assertThat(array(1.0, POSITIVE_INFINITY, 3.0))
          .usingTolerance(DEFAULT_TOLERANCE)
          .contains(POSITIVE_INFINITY);
      fail("Expected AssertionError to be thrown but wasn't");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <[1.0, Infinity, 3.0]> contains at least one element that is "
                  + "a finite number within "
                  + DEFAULT_TOLERANCE
                  + " of <Infinity>");
    }
  }

  @Test
  public void usingTolerance_contains_failureWithNaN() {
    try {
      assertThat(array(1.0, NaN, 3.0))
          .usingTolerance(DEFAULT_TOLERANCE)
          .contains(NaN);
      fail("Expected AssertionError to be thrown but wasn't");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <[1.0, NaN, 3.0]> contains at least one element that is "
                  + "a finite number within "
                  + DEFAULT_TOLERANCE
                  + " of <NaN>");
    }
  }

  @Test
  public void usingTolerance_contains_successWithNegativeZero() {
    assertThat(array(1.0, -0.0, 3.0))
        .usingTolerance(0.0)
        .contains(0.0);
  }

  @Test
  public void usingTolerance_contains_nullExpected() {
    try {
      assertThat(array(1.0, 2.0, 3.0)).usingTolerance(DEFAULT_TOLERANCE).contains(null);
      fail("Expected NullPointerException to be thrown but wasn't");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void usingTolerance_contains_negativeTolerance() {
    try {
      assertThat(array(1.0, 2.0, 3.0)).usingTolerance(-1.0 * DEFAULT_TOLERANCE).contains(2.0f);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessage("tolerance (" + -1.0 * DEFAULT_TOLERANCE + ") cannot be negative");
    }
  }

  @Test
  public void usingExactEquality_contains_success() {
    assertThat(array(1.0, 2.0, 3.0)).usingExactEquality().contains(2.0);
  }

  @Test
  public void usingExactEquality_contains_failure() {
    double justOverTwo = nextAfter(2.0, POSITIVE_INFINITY);
    try {
      assertThat(array(1.0, justOverTwo, 3.0)).usingExactEquality().contains(2.0);
      fail("Expected AssertionError to be thrown but wasn't");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <[1.0, "
                  + justOverTwo
                  + ", 3.0]> contains at least one element that is exactly equal to <2.0>");
    }
  }

  @Test
  public void usingExactEquality_contains_successWithInfinity() {
    assertThat(array(1.0, POSITIVE_INFINITY, 3.0)).usingExactEquality().contains(POSITIVE_INFINITY);
  }

  @Test
  public void usingExactEquality_contains_successWithNaN() {
    assertThat(array(1.0, NaN, 3.0)).usingExactEquality().contains(NaN);
  }

  @Test
  public void usingExactEquality_contains_failureWithNegativeZero() {
    try {
      assertThat(array(1.0, -0.0, 3.0)).usingExactEquality().contains(0.0);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <[1.0, -0.0, 3.0]> contains at least one element that is "
                  + "exactly equal to <0.0>");
    }
  }

  @Test
  public void usingExactEquality_contains_nullExpected() {
    try {
      assertThat(array(1.0, 2.0, 3.0)).usingExactEquality().contains(null);
      fail("Expected NullPointerException to be thrown but wasn't");
    } catch (NullPointerException expected) {
    }
  }

  private static double[] array(double... primitives) {
    return primitives;
  }
}
