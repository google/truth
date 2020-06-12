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
    assertFailureKeys("value of", "expected to contain", "testing whether", "but was");
    assertFailureValue("value of", "array.asList()");
    assertFailureValue("expected to contain", "2.2");
    assertFailureValue(
        "testing whether",
        "actual element is a finite number within " + DEFAULT_TOLERANCE + " of expected element");
    assertFailureValue("but was", "[1.1, " + INTOLERABLE_2POINT2 + ", 3.3]");
  }

  @Test
  public void usingTolerance_contains_failureWithInfinity() {
    expectFailureWhenTestingThat(array(1.1, POSITIVE_INFINITY, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(POSITIVE_INFINITY);
    assertFailureKeys("value of", "expected to contain", "testing whether", "but was");
    assertFailureValue("expected to contain", "Infinity");
    assertFailureValue("but was", "[1.1, Infinity, 3.3]");
  }

  @Test
  public void usingTolerance_contains_failureWithNaN() {
    expectFailureWhenTestingThat(array(1.1, NaN, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(NaN);
    assertFailureKeys("value of", "expected to contain", "testing whether", "but was");
    assertFailureValue("expected to contain", "NaN");
    assertFailureValue("but was", "[1.1, NaN, 3.3]");
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
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(null);
    assertFailureKeys(
        "value of",
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(1.1, null) threw java.lang.NullPointerException");
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
  public void usingTolerance_containsAtLeast_primitiveDoubleArray_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAtLeast(array(2.2, 1.1));
  }

  @Test
  public void usingTolerance_containsAtLeast_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAtLeast(array(2.2, 99.99));
    assertFailureKeys(
        "value of",
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "99.99");
  }

  @Test
  public void usingTolerance_containsAtLeast_primitiveDoubleArray_inOrder_success() {
    assertThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAtLeast(array(1.1, 2.2))
        .inOrder();
  }

  @Test
  public void usingTolerance_containsAtLeast_primitiveDoubleArray_inOrder_failure() {
    expectFailureWhenTestingThat(array(1.1, TOLERABLE_2POINT2, 3.3))
        .usingTolerance(DEFAULT_TOLERANCE)
        .containsAtLeast(array(2.2, 1.1))
        .inOrder();
    assertFailureKeys(
        "value of",
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "testing whether",
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
    assertFailureKeys("value of", "expected to contain any of", "testing whether", "but was");
    assertFailureValue("expected to contain any of", "[99.99, 999.999]");
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
    assertFailureKeys(
        "value of", "unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("unexpected (1)", "3.3");
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
        "expected",
        "testing whether",
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
    assertFailureKeys(
        "value of",
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "full contents");
    assertFailureValue("expected not to contain any of", "[99.99, 2.2]");
    assertFailureValue("but contained", "[" + TOLERABLE_2POINT2 + "]");
    assertFailureValue("corresponding to", "2.2");
  }

  @Test
  public void usingExactEquality_contains_success() {
    assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().contains(2.2);
  }

  @Test
  public void usingExactEquality_contains_failure() {
    expectFailureWhenTestingThat(array(1.1, OVER_2POINT2, 3.3)).usingExactEquality().contains(2.2);
    assertFailureKeys("value of", "expected to contain", "testing whether", "but was");
    assertFailureValue("expected to contain", "2.2");
    assertFailureValue("testing whether", "actual element is exactly equal to expected element");
    assertFailureValue("but was", "[1.1, " + OVER_2POINT2 + ", 3.3]");
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
  }

  @Test
  public void usingExactEquality_contains_otherTypes_longOutOfRange() {
    long expected = (1L << 53) + 1L;
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3)).usingExactEquality().contains(expected);
    assertFailureKeys(
        "value of",
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("expected to contain", Long.toString(expected));
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(1.1, " + expected + ") threw java.lang.IllegalArgumentException");
    assertThatFailure()
        .factValue("first exception")
        .contains(
            "Expected value "
                + expected
                + " in assertion using exact double equality was a long with an absolute value "
                + "greater than 2^52 which has no exact double representation");
  }

  @Test
  public void usingExactEquality_contains_otherTypes_bigIntegerNotSupported() {
    BigInteger expected = BigInteger.valueOf(2);
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3)).usingExactEquality().contains(expected);
    assertFailureKeys(
        "value of",
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("expected to contain", "2");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(1.1, " + expected + ") threw java.lang.IllegalArgumentException");
    assertThatFailure()
        .factValue("first exception")
        .contains(
            "Expected value in assertion using exact double equality was of unsupported type "
                + BigInteger.class
                + " (it may not have an exact double representation)");
  }

  @Test
  public void usingExactEquality_contains_otherTypes_bigDecimalNotSupported() {
    BigDecimal expected = BigDecimal.valueOf(2.0);
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3)).usingExactEquality().contains(expected);
    assertFailureKeys(
        "value of",
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("expected to contain", expected.toString());
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(1.1, " + expected + ") threw java.lang.IllegalArgumentException");
    assertThatFailure()
        .factValue("first exception")
        .contains(
            "Expected value in assertion using exact double equality was of unsupported type "
                + BigDecimal.class
                + " (it may not have an exact double representation)");
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
    assertFailureKeys("value of", "expected to contain", "testing whether", "but was");
    /*
     * TODO(cpovirk): Find a way to print "0.0" rather than 0 in the error, even under GWT. One
     * easy(?) hack would be to make UsingCorrespondence use Platform.doubleToString() when
     * applicable. Or maybe Correspondence implementations should be able to provide custom string
     * conversions, similar to how we plan to let them render their own diffs.
     */
    assertFailureValue("expected to contain", Double.toString(0.0));
  }

  @Test
  public void usingExactEquality_contains_nullExpected() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3)).usingExactEquality().contains(null);
    assertFailureKeys(
        "value of",
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("expected to contain", "null");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(1.1, null) threw java.lang.NullPointerException");
  }

  @Test
  public void usingExactEquality_containsAtLeast_primitiveDoubleArray_success() {
    assertThat(array(1.1, 2.2, 3.3)).usingExactEquality().containsAtLeast(array(2.2, 1.1));
  }

  @Test
  public void usingExactEquality_containsAtLeast_primitiveDoubleArray_failure() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsAtLeast(array(2.2, 99.99));
    assertFailureKeys(
        "value of",
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "99.99");
  }

  @Test
  public void usingExactEquality_containsAtLeast_primitiveDoubleArray_inOrder_success() {
    assertThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsAtLeast(array(1.1, 2.2))
        .inOrder();
  }

  @Test
  public void usingExactEquality_containsAtLeast_primitiveDoubleArray_inOrder_failure() {
    expectFailureWhenTestingThat(array(1.1, 2.2, 3.3))
        .usingExactEquality()
        .containsAtLeast(array(2.2, 1.1))
        .inOrder();
    assertFailureKeys(
        "value of",
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "testing whether",
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
    assertFailureKeys("value of", "expected to contain any of", "testing whether", "but was");
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
    assertFailureKeys(
        "value of", "unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("unexpected (1)", "3.3");
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
        "expected",
        "testing whether",
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
    assertFailureKeys(
        "value of",
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "full contents");
    assertFailureValue("expected not to contain any of", "[99.99, 2.2]");
    assertFailureValue("but contained", "[2.2]");
    assertFailureValue("corresponding to", "2.2");
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
