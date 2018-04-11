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

import com.google.common.annotations.GwtIncompatible;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Longs;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PrimitiveDoubleArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveDoubleArraySubjectTest extends BaseSubjectTestCase {
  private static final double DEFAULT_TOLERANCE = 0.000005d;

  private static final double OVER_2POINT2 = 2.2000000000000006d;
  private static final double TOLERABLE_2 = 2.0000049999999994d;
  private static final double TOLERABLE_2POINT2 = 2.2000049999999995d;
  private static final double INTOLERABLE_2POINT2 = 2.2000050000000004d;
  private static final double TOLERABLE_3POINT3 = 3.300004999999999d;
  private static final double INTOLERABLE_3POINT3 = 3.300005d;
  private static final double UNDER_MIN_OF_LONG = -9.223372036854778E18d;

  @Test
  @GwtIncompatible("Math.nextAfter")
  public void testDoubleConstants_matchNextAfter() {
    assertThat(nextAfter(2.0 + DEFAULT_TOLERANCE, NEGATIVE_INFINITY)).isEqualTo(TOLERABLE_2);
    assertThat(nextAfter(2.2 + DEFAULT_TOLERANCE, NEGATIVE_INFINITY)).isEqualTo(TOLERABLE_2POINT2);
    assertThat(nextAfter(2.2 + DEFAULT_TOLERANCE, POSITIVE_INFINITY))
        .isEqualTo(INTOLERABLE_2POINT2);
    assertThat(nextAfter(2.2, POSITIVE_INFINITY)).isEqualTo(OVER_2POINT2);
    assertThat(nextAfter(3.3 + DEFAULT_TOLERANCE, NEGATIVE_INFINITY)).isEqualTo(TOLERABLE_3POINT3);
    assertThat(nextAfter(3.3 + DEFAULT_TOLERANCE, POSITIVE_INFINITY))
        .isEqualTo(INTOLERABLE_3POINT3);
    assertThat(nextAfter((double) Long.MIN_VALUE, NEGATIVE_INFINITY)).isEqualTo(UNDER_MIN_OF_LONG);
  }

  @Test
  public void isEqualTo_WithoutToleranceParameter_Success() {
    assertThat(array(2.2d, 5.4d, POSITIVE_INFINITY, NEGATIVE_INFINITY, 0.0, -0.0))
        .isEqualTo(array(2.2d, 5.4d, POSITIVE_INFINITY, NEGATIVE_INFINITY, 0.0, -0.0));
  }

  @Test
  @GwtIncompatible("gwt Arrays.equals(double[], double[])")
  public void isEqualTo_WithoutToleranceParameter_NaN_Success() {
    assertThat(array(2.2d, 5.4d, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN, 0.0, -0.0))
        .isEqualTo(array(2.2d, 5.4d, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN, 0.0, -0.0));
  }

  @Test
  public void isEqualTo_WithoutToleranceParameter_Fail_NotEqual() {
    expectFailureWhenTestingThat(array(2.2d)).isEqualTo(array(OVER_2POINT2));
    assertFailureValue("expected", "[2.2000000000000006]");
    assertFailureValue("but was", "[2.2]");
    assertFailureValue("differs at index", "[0]");
  }

  @Test
  public void isEqualTo_WithoutToleranceParameter_Fail_DifferentOrder() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d)).isEqualTo(array(3.3d, 2.2d));
  }

  @Test
  public void isEqualTo_WithoutToleranceParameter_Fail_Longer() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d)).isEqualTo(array(2.2d, 3.3d, 4.4d));
    assertFailureKeys("expected", "but was", "wrong length", "expected", "but was");
    assertFailureValueIndexed("expected", 1, "3");
    assertFailureValueIndexed("but was", 1, "2");
  }

  @Test
  public void isEqualTo_WithoutToleranceParameter_Fail_Shorter() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d)).isEqualTo(array(2.2d));
  }

  @Test
  public void isEqualTo_WithoutToleranceParameter_Fail_PlusMinusZero() {
    expectFailureWhenTestingThat(array(0.0d)).isEqualTo(array(-0.0d));
    assertFailureValue("expected", "[-0.0]");
    assertFailureValue("but was", "[0.0]");
  }

  @Test
  public void isEqualTo_WithoutToleranceParameter_Fail_NotAnArray() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d, 4.4d)).isEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_WithoutToleranceParameter_FailEquals() {
    expectFailureWhenTestingThat(array(2.2d, 5.4d, POSITIVE_INFINITY, NEGATIVE_INFINITY))
        .isNotEqualTo(array(2.2d, 5.4d, POSITIVE_INFINITY, NEGATIVE_INFINITY));
  }

  @Test
  public void isNotEqualTo_WithoutToleranceParameter_NaN_plusZero_FailEquals() {
    expectFailureWhenTestingThat(
            array(2.2d, 5.4d, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN, 0.0, -0.0))
        .isNotEqualTo(array(2.2d, 5.4d, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN, 0.0, -0.0));
  }

  @Test
  public void isNotEqualTo_WithoutToleranceParameter_Success_NotEqual() {
    assertThat(array(2.2d)).isNotEqualTo(array(OVER_2POINT2));
  }

  @Test
  public void isNotEqualTo_WithoutToleranceParameter_Success_DifferentOrder() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(3.3d, 2.2d));
  }

  @Test
  public void isNotEqualTo_WithoutToleranceParameter_Success_Longer() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d, 3.3d, 4.4d));
  }

  @Test
  public void isNotEqualTo_WithoutToleranceParameter_Success_Shorter() {
    assertThat(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d));
  }

  @Test
  public void isNotEqualTo_WithoutToleranceParameter_Success_PlusMinusZero() {
    assertThat(array(0.0d)).isNotEqualTo(array(-0.0d));
  }

  @Test
  public void isNotEqualTo_WithoutToleranceParameter_Success_NotAnArray() {
    assertThat(array(2.2d, 3.3d, 4.4d)).isNotEqualTo(new Object());
  }

  @Test
  public void hasValuesWithinOf() {
    assertThat(array(2.2d, 5.4d)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2d, 5.4d);
  }

  @Test
  public void hasValuesWithinOf_ApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2d, TOLERABLE_3POINT3);
  }

  @Test
  public void hasValuesWithinOf_FailNotQuiteApproximatelyEquals() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .of(2.2d, INTOLERABLE_3POINT3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, "
                + INTOLERABLE_3POINT3
                + "]>. It differs at indexes <[1]>");
  }

  @Test
  public void hasValuesWithinOf_Fail_DifferentOrder() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .of(3.3d, 2.2d);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[3.3, 2.2]>."
                + " It differs at indexes <[0, 1]>");
  }

  @Test
  public void hasValuesWithinOf_Fail_Longer() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .of(2.2d, 3.3d, 1.1d);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, 3.3, 1.1]>."
                + " Expected length <3> but got <2>");
  }

  @Test
  public void hasValuesWithinOf_Fail_Shorter() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2d);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[2.2]>."
                + " Expected length <1> but got <2>");
  }

  @Test
  public void hasValuesWithinOf_Fail_Infinity() {
    expectFailureWhenTestingThat(array(2.2d, POSITIVE_INFINITY))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .of(2.2d, POSITIVE_INFINITY);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, Infinity]> has values within "
                + DEFAULT_TOLERANCE
                + " of"
                + " <[2.2, Infinity]>. It differs at indexes <[1]>");
  }

  @Test
  public void hasValuesWithinOf_Fail_SameInfinity() {
    double[] same = array(2.2d, POSITIVE_INFINITY);
    expectFailureWhenTestingThat(same).hasValuesWithin(DEFAULT_TOLERANCE).of(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, Infinity]> has values within "
                + DEFAULT_TOLERANCE
                + " of"
                + " <[2.2, Infinity]>. It differs at indexes <[1]>");
  }

  @Test
  public void hasValuesWithinOf_Fail_OneInfinity() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .of(2.2d, POSITIVE_INFINITY);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, Infinity]>."
                + " It differs at indexes <[1]>");
  }

  @Test
  public void hasValuesWithinOf_Fail_LongerOneInfinity() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .of(POSITIVE_INFINITY);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[Infinity]>."
                + " Expected length <1> but got <2>");
  }

  @Test
  public void hasValuesWithinOf_Fail_NaN() {
    expectFailureWhenTestingThat(array(NaN)).hasValuesWithin(DEFAULT_TOLERANCE).of(NaN);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[NaN]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[NaN]>."
                + " It differs at indexes <[0]>");
  }

  @Test
  public void hasValuesWithinOf_NullSubject() {
    double[] nullArray = null;
    try {
      assertThat(nullArray).hasValuesWithin(DEFAULT_TOLERANCE).of(3.3d, 2.2d);
      fail("Expected NullPointerException to be thrown");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOf_NullObbject() {
    double[] nullArray = null;
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesWithin(DEFAULT_TOLERANCE).of(nullArray);
      fail("Expected NullPointerException to be thrown");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOf_NegativeTolerance() {
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesWithin(-0.001d).of(3.3d, 2.2d);
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("tolerance (-0.001) cannot be negative");
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
        .ofElementsIn(Doubles.asList(2.2d, TOLERABLE_3POINT3));
  }

  @Test
  public void hasValuesWithinOfElementsIn_FailNotQuiteApproximatelyEquals() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, INTOLERABLE_3POINT3));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, "
                + INTOLERABLE_3POINT3
                + "]>. It differs at indexes <[1]>");
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_DifferentOrder() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(3.3d, 2.2d));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[3.3, 2.2]>."
                + " It differs at indexes <[0, 1]>");
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Longer() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, 3.3d, 1.1d));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, 3.3, 1.1]>."
                + " Expected length <3> but got <2>");
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Shorter() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[2.2]>."
                + " Expected length <1> but got <2>");
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Infinity() {
    expectFailureWhenTestingThat(array(2.2d, POSITIVE_INFINITY))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, POSITIVE_INFINITY));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, Infinity]> has values within "
                + DEFAULT_TOLERANCE
                + " of"
                + " <[2.2, Infinity]>. It differs at indexes <[1]>");
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_OneInfinity() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, POSITIVE_INFINITY));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, Infinity]>."
                + " It differs at indexes <[1]>");
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_LongerOneInfinity() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(POSITIVE_INFINITY));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[Infinity]>."
                + " Expected length <1> but got <2>");
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_NaN() {
    expectFailureWhenTestingThat(array(NaN))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(NaN));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[NaN]> has values within "
                + DEFAULT_TOLERANCE
                + " of <[NaN]>."
                + " It differs at indexes <[0]>");
  }

  @Test
  public void hasValuesWithinOfElementsIn_NullSubject() {
    double[] nullArray = null;
    try {
      assertThat(nullArray)
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(3.3d, 2.2d));
      fail("Expected NullPointerException to be thrown");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_NullObject() {
    Iterable<Number> nullIterable = null;
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesWithin(DEFAULT_TOLERANCE).ofElementsIn(nullIterable);
      fail("Expected NullPointerException to be thrown");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_NegativeTolerance() {
    try {
      assertThat(array(3.3d, 2.2d))
          .hasValuesWithin(-0.001d)
          .ofElementsIn(Doubles.asList(3.3d, 2.2d));
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("tolerance (-0.001) cannot be negative");
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
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .of(2.2d, 3.3d);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values not within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, 3.3]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_FailApproximatelyEquals() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .of(2.2d, TOLERABLE_3POINT3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values not within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, "
                + TOLERABLE_3POINT3
                + "]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NotQuiteApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .of(2.2d, INTOLERABLE_3POINT3);
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_FailSame() {
    double[] same = array(2.2d, 3.3d);
    expectFailureWhenTestingThat(same).hasValuesNotWithin(DEFAULT_TOLERANCE).of(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values not within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, 3.3]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_Infinity() {
    expectFailureWhenTestingThat(array(2.2d, POSITIVE_INFINITY))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .of(2.2d, POSITIVE_INFINITY);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, Infinity]> has values not within "
                + DEFAULT_TOLERANCE
                + " of"
                + " <[2.2, Infinity]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_SameInfinity() {
    double[] same = array(2.2d, POSITIVE_INFINITY);
    expectFailureWhenTestingThat(same).hasValuesNotWithin(DEFAULT_TOLERANCE).of(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, Infinity]> has values not within "
                + DEFAULT_TOLERANCE
                + " of"
                + " <[2.2, Infinity]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_OneInfinity() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .of(2.2d, POSITIVE_INFINITY);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values not within "
                + DEFAULT_TOLERANCE
                + " of"
                + " <[2.2, Infinity]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_LongerOneInfinity() {
    assertThat(array(2.2d, 3.3d)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(POSITIVE_INFINITY);
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_NaN() {
    expectFailureWhenTestingThat(array(NaN)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(NaN);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[NaN]> has values not within " + DEFAULT_TOLERANCE + " of <[NaN]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NullSubject() {
    double[] nullArray = null;
    try {
      assertThat(nullArray).hasValuesNotWithin(DEFAULT_TOLERANCE).of(3.3d, 2.2d);
      fail("Expected NullPointerException to be thrown");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NullObject() {
    double[] nullArray = null;
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(nullArray);
      fail("Expected NullPointerException to be thrown");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NegativeTolerance() {
    try {
      assertThat(array(3.3d, 2.2d)).hasValuesNotWithin(-0.001d).of(3.3d, 2.2d);
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("tolerance (-0.001) cannot be negative");
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
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, 3.3d));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values not within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, 3.3]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_FailApproximatelyEquals() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, TOLERABLE_3POINT3));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values not within "
                + DEFAULT_TOLERANCE
                + " of <[2.2, "
                + TOLERABLE_3POINT3
                + "]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NotQuiteApproximatelyEquals() {
    assertThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, INTOLERABLE_3POINT3));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_Fail_Infinity() {
    expectFailureWhenTestingThat(array(2.2d, POSITIVE_INFINITY))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, POSITIVE_INFINITY));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, Infinity]> has values not within "
                + DEFAULT_TOLERANCE
                + " of"
                + " <[2.2, Infinity]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_OneInfinity() {
    expectFailureWhenTestingThat(array(2.2d, 3.3d))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, POSITIVE_INFINITY));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[2.2, 3.3]> has values not within "
                + DEFAULT_TOLERANCE
                + " of"
                + " <[2.2, Infinity]>");
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
    expectFailureWhenTestingThat(array(NaN))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(NaN));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[NaN]> has values not within " + DEFAULT_TOLERANCE + " of <[NaN]>");
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NullSubject() {
    double[] nullArray = null;
    try {
      assertThat(nullArray)
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Doubles.asList(3.3d, 2.2d));
      fail("Expected NullPointerException to be thrown");
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
      fail("Expected NullPointerException to be thrown");
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
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  public void usingTolerance_contains_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3)).usingTolerance(DEFAULT_TOLERANCE).contains(2.2);
  }

  @Test
  public void usingTolerance_contains_successWithExpectedLong() {
    assertThat(array(1.0, TOLERABLE_2, 3.0)).usingTolerance(DEFAULT_TOLERANCE).contains(2L);
  }

  @Test
  public void usingTolerance_contains_failure() {
    expectFailureWhenTestingThat(array(1.1, INTOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2.2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, "
                + INTOLERABLE_2POINT2
                + ", 3.3]> contains at least one element that is a finite number within "
                + DEFAULT_TOLERANCE
                + " of <2.2>");
  }

  @Test
  public void usingTolerance_contains_failureWithInfinity() {
    expectFailureWhenTestingThat(array(1.1, POSITIVE_INFINITY, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(POSITIVE_INFINITY);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, Infinity, 3.3]> "
                + "contains at least one element that is "
                + "a finite number within "
                + DEFAULT_TOLERANCE
                + " of <Infinity>");
  }

  @Test
  public void usingTolerance_contains_failureWithNaN() {
    expectFailureWhenTestingThat(array(1.1, NaN, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(NaN);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, NaN, 3.3]> "
                + "contains at least one element that is "
                + "a finite number within "
                + DEFAULT_TOLERANCE
                + " of <NaN>");
  }

  @Test
  public void usingTolerance_contains_successWithNegativeZero() {
    assertThat(array(1.1, -0.0, 3.3)).usingTolerance(0.0).contains(0.0);
  }

  @Test
  public void usingTolerance_contains_otherTypes() {
    // Expected value is Float
    assertThat(array(1.0, 2.0 + 0.5 * DEFAULT_TOLERANCE, 3.0))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2.0f);
    // Expected value is Integer
    assertThat(array(1.0, 2.0 + 0.5 * DEFAULT_TOLERANCE, 3.0))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2);
    // Expected value is Integer.MAX_VALUE
    assertThat(array(1.0, Integer.MAX_VALUE + 0.5 * DEFAULT_TOLERANCE, 3.0))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(Integer.MAX_VALUE);
    // Expected value is Long
    assertThat(array(1.0, 2.0 + 0.5 * DEFAULT_TOLERANCE, 3.0))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2L);
    // Expected value is Long.MIN_VALUE. This is -1*2^63, which has an exact double representation.
    // For the actual value we use the next value down, which is is 2^11 smaller (because the
    // resolution of doubles with absolute values between 2^63 and 2^64 is 2^11). So we'll make the
    // assertion with a tolerance of 2^12.
    assertThat(array(1.0, UNDER_MIN_OF_LONG, 3.0)).usingTolerance(1 << 12).contains(Long.MIN_VALUE);
    // Expected value is BigInteger
    assertThat(array(1.0, 2.0 + 0.5 * DEFAULT_TOLERANCE, 3.0))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(BigInteger.valueOf(2));
    // Expected value is BigDecimal
    assertThat(array(1.0, 2.0 + 0.5 * DEFAULT_TOLERANCE, 3.0))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(BigDecimal.valueOf(2.0));
  }

  @Test
  public void usingTolerance_contains_nullExpected() {
    try {
      assertThat(array(1.1, 2.2, 3.3)).usingTolerance(DEFAULT_TOLERANCE).contains(null);
      fail("Expected NullPointerException to be thrown but wasn't");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void usingTolerance_contains_negativeTolerance() {
    try {
      assertThat(array(1.1, 2.2, 3.3)).usingTolerance(-1.1 * DEFAULT_TOLERANCE).contains(2.0f);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("tolerance (" + -1.1 * DEFAULT_TOLERANCE + ") cannot be negative");
    }
  }

  @Test
  public void usingTolerance_containsAllOf_primitiveDoubleArray_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAllOf(array(2.2, 1.1));
  }

  @Test
  public void usingTolerance_containsAllOf_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAllOf(array(2.2, 99.99));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, "
                + TOLERABLE_2POINT2
                + ", 3.3]> contains at least one element that is a finite number "
                + "within "
                + DEFAULT_TOLERANCE
                + " of each element of <[2.2, 99.99]>. It is missing an element that is a finite "
                + "number within "
                + DEFAULT_TOLERANCE
                + " of <99.99>");
  }

  @Test
  public void usingTolerance_containsAllOf_primitiveDoubleArray_inOrder_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAllOf(array(1.1, 2.2))
        .inOrder();
  }

  @Test
  public void usingTolerance_containsAllOf_primitiveDoubleArray_inOrder_failure() {
    expectFailureWhenTestingThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAllOf(array(2.2, 1.1))
        .inOrder();
    assertFailureKeys(
        "value of",
        "required elements were all found, but order was wrong",
        "comparing contents by testing that each element is a finite number within "
            + DEFAULT_TOLERANCE
            + " of an expected value",
        "expected order for required elements",
        "but was");
    assertFailureValue("expected order for required elements", "[2.2, 1.1]");
  }

  @Test
  public void usingTolerance_containsAnyOf_primitiveDoubleArray_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAnyOf(array(99.99, 2.2));
  }

  @Test
  public void usingTolerance_containsAnyOf_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAnyOf(array(99.99, 999.999));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, "
                + TOLERABLE_2POINT2
                + ", 3.3]> contains at least one element that is a finite number within "
                + DEFAULT_TOLERANCE
                + " of any element in <[99.99, 999.999]>");
  }

  @Test
  public void usingTolerance_containsExactly_primitiveDoubleArray_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsExactly(array(2.2, 1.1, 3.3));
  }

  @Test
  public void usingTolerance_containsExactly_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsExactly(array(2.2, 1.1));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, "
                + TOLERABLE_2POINT2
                + ", 3.3]> contains exactly one element that is a finite number within "
                + DEFAULT_TOLERANCE
                + " of each element of <[2.2, 1.1]>. It has unexpected elements <[3.3]>");
  }

  @Test
  public void usingTolerance_containsExactly_primitiveDoubleArray_inOrder_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsExactly(array(1.1, 2.2, 3.3))
        .inOrder();
  }

  @Test
  public void usingTolerance_containsExactly_primitiveDoubleArray_inOrder_failure() {
    expectFailureWhenTestingThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsExactly(array(2.2, 1.1, 3.3))
        .inOrder();
    assertFailureKeys(
        "value of",
        "contents match, but order was wrong",
        "comparing contents by testing that each element is a finite number within "
            + DEFAULT_TOLERANCE
            + " of an expected value",
        "expected",
        "but was");
    assertFailureValue("expected", "[2.2, 1.1, 3.3]");
  }

  @Test
  public void usingTolerance_containsNoneOf_primitiveDoubleArray_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsNoneOf(array(99.99, 999.999));
  }

  @Test
  public void usingTolerance_containsNoneOf_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsNoneOf(array(99.99, 2.2));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, "
                + TOLERABLE_2POINT2
                + ", 3.3]> contains no element that is a finite number within "
                + DEFAULT_TOLERANCE
                + " of any element in <[99.99, 2.2]>. It contains <["
                + TOLERABLE_2POINT2
                + " which corresponds to 2.2]>");
  }

  @Test
  public void usingExactEquality_contains_success() {
    assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().contains(2.2);
  }

  @Test
  public void usingExactEquality_contains_failure() {
    expectFailureWhenTestingThat(array(1.1, OVER_2POINT2, 3.3)).usingExactEquality().contains(2.2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, "
                + OVER_2POINT2
                + ", 3.3]> contains at least one element that is exactly equal to <2.2>");
  }

  @Test
  public void usingExactEquality_contains_otherTypes() {
    // Expected value is Float
    assertThat(array(1.0, 2.0, 3.0)).usingExactEquality().contains(2.0f);
    // Expected value is Integer
    assertThat(array(1.0, 2.0, 3.0)).usingExactEquality().contains(2);
    assertThat(array(1.0, Integer.MAX_VALUE, 3.0)).usingExactEquality().contains(Integer.MAX_VALUE);
    // Expected value is Long - supported up to +/- 2^53
    assertThat(array(1.0, 2.0, 3.0)).usingExactEquality().contains(2L);
    assertThat(array(1.0, 1L << 53, 3.0)).usingExactEquality().contains(1L << 53);
    try {
      assertThat(array(1.0, 2.0, 3.0)).usingExactEquality().contains((1L << 53) + 1L);
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Expected value 9007199254740993 in assertion using exact double equality was a long "
                  + "with an absolute value greater than 2^52 which has no exact double "
                  + "representation");
    }
    // Expected value is BigInteger - not supported
    try {
      assertThat(array(1.0, 2.0, 3.0)).usingExactEquality().contains(BigInteger.valueOf(2));
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Expected value in assertion using exact double equality was of unsupported type "
                  + BigInteger.class
                  + " (it may not have an exact double representation)");
    }
    // Expected value is BigDecimal - not supported
    try {
      assertThat(array(1.0, 2.0, 3.0)).usingExactEquality().contains(BigDecimal.valueOf(2.0));
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Expected value in assertion using exact double equality was of unsupported type "
                  + BigDecimal.class
                  + " (it may not have an exact double representation)");
    }
  }

  @Test
  public void usingExactEquality_contains_successWithInfinity() {
    assertThat(array(1.1, POSITIVE_INFINITY, 3.3)).usingExactEquality().contains(POSITIVE_INFINITY);
  }

  @Test
  public void usingExactEquality_contains_successWithNaN() {
    assertThat(array(1.1, NaN, 3.3)).usingExactEquality().contains(NaN);
  }

  @Test
  public void usingExactEquality_contains_failureWithNegativeZero() {
    expectFailureWhenTestingThat(array(1.1, -0.0, 3.3)).usingExactEquality().contains(0.0);
    /*
     * TODO(cpovirk): Find a way to print "0.0" rather than 0 in the error, even under GWT. One
     * easy(?) hack would be to make UsingCorrespondence use Platform.doubleToString() when
     * applicable. Or maybe Correspondence implementations should be able to provide custom string
     * conversions, similar to how we plan to let them render their own diffs.
     */
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, -0.0, 3.3]> "
                + "contains at least one element that is "
                + "exactly equal to <"
                + 0.0
                + ">");
  }

  @Test
  public void usingExactEquality_contains_nullExpected() {
    try {
      assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().contains(null);
      fail("Expected NullPointerException to be thrown but wasn't");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void usingExactEquality_containsAllOf_primitiveDoubleArray_success() {
    assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().containsAllOf(array(2.2, 1.1));
  }

  @Test
  public void usingExactEquality_containsAllOf_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsAllOf(array(2.2, 99.99));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, 2.2, 3.3]> "
                + "contains at least one element that is exactly equal "
                + "to each element of <[2.2, 99.99]>. It is missing an element that is exactly "
                + "equal to <99.99>");
  }

  @Test
  public void usingExactEquality_containsAllOf_primitiveDoubleArray_inOrder_success() {
    assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().containsAllOf(array(1.1, 2.2)).inOrder();
  }

  @Test
  public void usingExactEquality_containsAllOf_primitiveDoubleArray_inOrder_failure() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsAllOf(array(2.2, 1.1))
        .inOrder();
    assertFailureKeys(
        "value of",
        "required elements were all found, but order was wrong",
        "comparing contents by testing that each element is exactly equal to an expected value",
        "expected order for required elements",
        "but was");
    assertFailureValue("expected order for required elements", "[2.2, 1.1]");
  }

  @Test
  public void usingExactEquality_containsAnyOf_primitiveDoubleArray_success() {
    assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().containsAnyOf(array(99.99, 2.2));
  }

  @Test
  public void usingExactEquality_containsAnyOf_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsAnyOf(array(99.99, 999.999));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, 2.2, 3.3]> "
                + "contains at least one element that is exactly equal "
                + "to any element in <[99.99, 999.999]>");
  }

  @Test
  public void usingExactEquality_containsExactly_primitiveDoubleArray_success() {
    assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().containsExactly(array(2.2, 1.1, 3.3));
  }

  @Test
  public void usingExactEquality_containsExactly_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsExactly(array(2.2, 1.1));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, 2.2, 3.3]> "
                + "contains exactly one element that is exactly equal "
                + "to each element of <[2.2, 1.1]>. It has unexpected elements <[3.3]>");
  }

  @Test
  public void usingExactEquality_containsExactly_primitiveDoubleArray_inOrder_success() {
    assertThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsExactly(array(1.1, 2.2, 3.3))
        .inOrder();
  }

  @Test
  public void usingExactEquality_containsExactly_primitiveDoubleArray_inOrder_failure() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsExactly(array(2.2, 1.1, 3.3))
        .inOrder();
    assertFailureKeys(
        "value of",
        "contents match, but order was wrong",
        "comparing contents by testing that each element is exactly equal to an expected value",
        "expected",
        "but was");
    assertFailureValue("expected", "[2.2, 1.1, 3.3]");
  }

  @Test
  public void usingExactEquality_containsNoneOf_primitiveDoubleArray_success() {
    assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().containsNoneOf(array(99.99, 999.999));
  }

  @Test
  public void usingExactEquality_containsNoneOf_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsNoneOf(array(99.99, 2.2));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "value of: array.asList()\nNot true that <[1.1, 2.2, 3.3]> "
                + "contains no element that is exactly equal to any "
                + "element in <[99.99, 2.2]>. It contains <[2.2 which corresponds to 2.2]>");
  }

  @Test
  public void smallDifferenceInLongRepresentation() {
    expectFailureWhenTestingThat(array(-4.4501477170144023E-308))
        .isEqualTo(array(-4.450147717014402E-308));
  }

  @Test
  public void noCommas() {
    // Maybe we should include commas, but we don't yet, so make sure we don't under GWT, either.
    expectFailureWhenTestingThat(array(10000.0)).isEqualTo(array(20000.0));
    assertFailureValue("expected", "[20000.0]");
    assertFailureValue("but was", "[10000.0]");
  }

  private static double[] array(double... primitives) {
    return primitives;
  }

  private PrimitiveDoubleArraySubject expectFailureWhenTestingThat(double[] actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
