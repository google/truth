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
import static org.junit.Assert.fail;

import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Double Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class DoubleSubjectTest {
  @Test
  public void isWithinOf() {
    assertThat(2.0).isWithin(0.0).of(2.0);
    assertThat(2.0).isWithin(0.00001).of(2.0);
    assertThat(2.0).isWithin(1000.0).of(2.0);
    assertThat(2.0).isWithin(1.00001).of(3.0);
    assertThatIsWithinFails(2.0, 0.99999, 3.0);
    assertThatIsWithinFails(2.0, 1000.0, 1003.0);
    assertThatIsWithinFails(2.0, 1000.0, Double.POSITIVE_INFINITY);
    assertThatIsWithinFails(2.0, 1000.0, Double.NaN);
    assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 1000.0, 2.0);
    assertThatIsWithinFails(Double.NaN, 1000.0, 2.0);
  }

  private static void assertThatIsWithinFails(double actual, double tolerance, double expected) {
    try {
      assertThat(actual).named("testValue").isWithin(tolerance).of(expected);
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo(
              String.format(
                  "testValue (<%s>) and <%s> should have been finite values within"
                      + " <%s> of each other",
                  actual, expected, tolerance));
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  @Test
  public void isNotWithinOf() {
    assertThatIsNotWithinFails(2.0, 0.0, 2.0);
    assertThatIsNotWithinFails(2.0, 0.00001, 2.0);
    assertThatIsNotWithinFails(2.0, 1000.0, 2.0);
    assertThatIsNotWithinFails(2.0, 1.00001, 3.0);
    assertThat(2.0).isNotWithin(0.99999).of(3.0);
    assertThat(2.0).isNotWithin(1000.0).of(1003.0);
    assertThatIsNotWithinFails(2.0, 0.0, Double.POSITIVE_INFINITY);
    assertThatIsNotWithinFails(2.0, 0.0, Double.NaN);
    assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 1000.0, 2.0);
    assertThatIsNotWithinFails(Double.NaN, 1000.0, 2.0);
  }

  private static void assertThatIsNotWithinFails(double actual, double tolerance, double expected) {
    try {
      assertThat(actual).named("testValue").isNotWithin(tolerance).of(expected);
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo(
              String.format(
                  "testValue (<%s>) and <%s> should have been finite values not within"
                      + " <%s> of each other",
                  actual, expected, tolerance));
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  @Test
  public void negativeTolerances() {
    isWithinNegativeToleranceThrowsIAE(5.0, -0.5, 4.9);
    isWithinNegativeToleranceThrowsIAE(5.0, -0.5, 4.0);

    isNotWithinNegativeToleranceThrowsIAE(5.0, -0.5, 4.9);
    isNotWithinNegativeToleranceThrowsIAE(5.0, -0.5, 4.0);

    isWithinNegativeToleranceThrowsIAE(+0.0, -0.00001, +0.0);
    isWithinNegativeToleranceThrowsIAE(+0.0, -0.00001, -0.0);
    isWithinNegativeToleranceThrowsIAE(-0.0, -0.00001, +0.0);
    isWithinNegativeToleranceThrowsIAE(-0.0, -0.00001, -0.0);

    isNotWithinNegativeToleranceThrowsIAE(+0.0, -0.00001, +1.0);
    isNotWithinNegativeToleranceThrowsIAE(+0.0, -0.00001, -1.0);
    isNotWithinNegativeToleranceThrowsIAE(-0.0, -0.00001, +1.0);
    isNotWithinNegativeToleranceThrowsIAE(-0.0, -0.00001, -1.0);

    isNotWithinNegativeToleranceThrowsIAE(+1.0, -0.00001, +0.0);
    isNotWithinNegativeToleranceThrowsIAE(+1.0, -0.00001, -0.0);
    isNotWithinNegativeToleranceThrowsIAE(-1.0, -0.00001, +0.0);
    isNotWithinNegativeToleranceThrowsIAE(-1.0, -0.00001, -0.0);

    // You know what's worse than zero? Negative zero.

    isWithinNegativeToleranceThrowsIAE(+0.0, -0.0, +0.0);
    isWithinNegativeToleranceThrowsIAE(+0.0, -0.0, -0.0);
    isWithinNegativeToleranceThrowsIAE(-0.0, -0.0, +0.0);
    isWithinNegativeToleranceThrowsIAE(-0.0, -0.0, -0.0);

    isNotWithinNegativeToleranceThrowsIAE(+1.0, -0.0, +0.0);
    isNotWithinNegativeToleranceThrowsIAE(+1.0, -0.0, -0.0);
    isNotWithinNegativeToleranceThrowsIAE(-1.0, -0.0, +0.0);
    isNotWithinNegativeToleranceThrowsIAE(-1.0, -0.0, -0.0);
  }

  private static void isWithinNegativeToleranceThrowsIAE(
      double actual, double tolerance, double expected) {
    try {
      assertThat(actual).isWithin(tolerance).of(expected);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae)
          .hasMessageThat()
          .isEqualTo("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  private static void isNotWithinNegativeToleranceThrowsIAE(
      double actual, double tolerance, double expected) {
    try {
      assertThat(actual).isNotWithin(tolerance).of(expected);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae)
          .hasMessageThat()
          .isEqualTo("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  @Test
  public void nanTolerances() {
    try {
      assertThat(1.0).isWithin(Double.NaN).of(1.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be NaN");
    }
    try {
      assertThat(1.0).isNotWithin(Double.NaN).of(2.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be NaN");
    }
  }

  @Test
  public void infiniteTolerances() {
    try {
      assertThat(1.0).isWithin(Double.POSITIVE_INFINITY).of(1.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be POSITIVE_INFINITY");
    }
    try {
      assertThat(1.0).isNotWithin(Double.POSITIVE_INFINITY).of(2.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be POSITIVE_INFINITY");
    }
  }

  @Test
  public void isWithinOfZero() {
    assertThat(+0.0).isWithin(0.00001).of(+0.0);
    assertThat(+0.0).isWithin(0.00001).of(-0.0);
    assertThat(-0.0).isWithin(0.00001).of(+0.0);
    assertThat(-0.0).isWithin(0.00001).of(-0.0);

    assertThat(+0.0).isWithin(0.0).of(+0.0);
    assertThat(+0.0).isWithin(0.0).of(-0.0);
    assertThat(-0.0).isWithin(0.0).of(+0.0);
    assertThat(-0.0).isWithin(0.0).of(-0.0);
  }

  @Test
  public void isNotWithinOfZero() {
    assertThat(+0.0).isNotWithin(0.00001).of(+1.0);
    assertThat(+0.0).isNotWithin(0.00001).of(-1.0);
    assertThat(-0.0).isNotWithin(0.00001).of(+1.0);
    assertThat(-0.0).isNotWithin(0.00001).of(-1.0);

    assertThat(+1.0).isNotWithin(0.00001).of(+0.0);
    assertThat(+1.0).isNotWithin(0.00001).of(-0.0);
    assertThat(-1.0).isNotWithin(0.00001).of(+0.0);
    assertThat(-1.0).isNotWithin(0.00001).of(-0.0);

    assertThat(+1.0).isNotWithin(0.0).of(+0.0);
    assertThat(+1.0).isNotWithin(0.0).of(-0.0);
    assertThat(-1.0).isNotWithin(0.0).of(+0.0);
    assertThat(-1.0).isNotWithin(0.0).of(-0.0);

    assertThatIsNotWithinFails(-0.0, 0.0, 0.0);
  }

  @Test
  public void isWithinZeroTolerance() {
    double max = Double.MAX_VALUE;
    double nearlyMax = Math.nextAfter(Double.MAX_VALUE, 0.0);
    assertThat(max).isWithin(0.0).of(max);
    assertThat(nearlyMax).isWithin(0.0).of(nearlyMax);
    assertThatIsWithinFails(max, 0.0, nearlyMax);
    assertThatIsWithinFails(nearlyMax, 0.0, max);

    double negativeMax = -1.0 * Double.MAX_VALUE;
    double negativeNearlyMax = Math.nextAfter(-1.0 * Double.MAX_VALUE, 0.0);
    assertThat(negativeMax).isWithin(0.0).of(negativeMax);
    assertThat(negativeNearlyMax).isWithin(0.0).of(negativeNearlyMax);
    assertThatIsWithinFails(negativeMax, 0.0, negativeNearlyMax);
    assertThatIsWithinFails(negativeNearlyMax, 0.0, negativeMax);

    double min = Double.MIN_VALUE;
    double justOverMin = Math.nextAfter(Double.MIN_VALUE, 1.0);
    assertThat(min).isWithin(0.0).of(min);
    assertThat(justOverMin).isWithin(0.0).of(justOverMin);
    assertThatIsWithinFails(min, 0.0, justOverMin);
    assertThatIsWithinFails(justOverMin, 0.0, min);

    double negativeMin = -1.0 * Double.MIN_VALUE;
    double justUnderNegativeMin = Math.nextAfter(-1.0 * Double.MIN_VALUE, -1.0);
    assertThat(negativeMin).isWithin(0.0).of(negativeMin);
    assertThat(justUnderNegativeMin).isWithin(0.0).of(justUnderNegativeMin);
    assertThatIsWithinFails(negativeMin, 0.0, justUnderNegativeMin);
    assertThatIsWithinFails(justUnderNegativeMin, 0.0, negativeMin);
  }

  @Test
  public void isNotWithinZeroTolerance() {
    double max = Double.MAX_VALUE;
    double nearlyMax = Math.nextAfter(Double.MAX_VALUE, 0.0);
    assertThatIsNotWithinFails(max, 0.0, max);
    assertThatIsNotWithinFails(nearlyMax, 0.0, nearlyMax);
    assertThat(max).isNotWithin(0.0).of(nearlyMax);
    assertThat(nearlyMax).isNotWithin(0.0).of(max);

    double min = Double.MIN_VALUE;
    double justOverMin = Math.nextAfter(Double.MIN_VALUE, 1.0);
    assertThatIsNotWithinFails(min, 0.0, min);
    assertThatIsNotWithinFails(justOverMin, 0.0, justOverMin);
    assertThat(min).isNotWithin(0.0).of(justOverMin);
    assertThat(justOverMin).isNotWithin(0.0).of(min);
  }

  @Test
  public void isWithinNonFinite() {
    assertThatIsWithinFails(Double.NaN, 0.00001, Double.NaN);
    assertThatIsWithinFails(Double.NaN, 0.00001, Double.POSITIVE_INFINITY);
    assertThatIsWithinFails(Double.NaN, 0.00001, Double.NEGATIVE_INFINITY);
    assertThatIsWithinFails(Double.NaN, 0.00001, +0.0);
    assertThatIsWithinFails(Double.NaN, 0.00001, -0.0);
    assertThatIsWithinFails(Double.NaN, 0.00001, +1.0);
    assertThatIsWithinFails(Double.NaN, 0.00001, -0.0);
    assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, Double.POSITIVE_INFINITY);
    assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, Double.NEGATIVE_INFINITY);
    assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, +0.0);
    assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, -0.0);
    assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, +1.0);
    assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, -0.0);
    assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, Double.NEGATIVE_INFINITY);
    assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, +0.0);
    assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, -0.0);
    assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, +1.0);
    assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, -0.0);
    assertThatIsWithinFails(+1.0, 0.00001, Double.NaN);
    assertThatIsWithinFails(+1.0, 0.00001, Double.POSITIVE_INFINITY);
    assertThatIsWithinFails(+1.0, 0.00001, Double.NEGATIVE_INFINITY);
  }

  @Test
  public void isNotWithinNonFinite() {
    assertThatIsNotWithinFails(Double.NaN, 0.00001, Double.NaN);
    assertThatIsNotWithinFails(Double.NaN, 0.00001, Double.POSITIVE_INFINITY);
    assertThatIsNotWithinFails(Double.NaN, 0.00001, Double.NEGATIVE_INFINITY);
    assertThatIsNotWithinFails(Double.NaN, 0.00001, +0.0);
    assertThatIsNotWithinFails(Double.NaN, 0.00001, -0.0);
    assertThatIsNotWithinFails(Double.NaN, 0.00001, +1.0);
    assertThatIsNotWithinFails(Double.NaN, 0.00001, -0.0);
    assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, Double.POSITIVE_INFINITY);
    assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, Double.NEGATIVE_INFINITY);
    assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, +0.0);
    assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, -0.0);
    assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, +1.0);
    assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, -0.0);
    assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, Double.NEGATIVE_INFINITY);
    assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, +0.0);
    assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, -0.0);
    assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, +1.0);
    assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, -0.0);
    assertThatIsNotWithinFails(+1.0, 0.00001, Double.NaN);
    assertThatIsNotWithinFails(+1.0, 0.00001, Double.POSITIVE_INFINITY);
    assertThatIsNotWithinFails(+1.0, 0.00001, Double.NEGATIVE_INFINITY);
  }

  @Test
  public void isEqualTo() {
    assertThat(1.23).isEqualTo(1.23);
    assertThatIsEqualToFails(1.23, Math.nextAfter(1.23, Double.POSITIVE_INFINITY));
    assertThat(Double.POSITIVE_INFINITY).isEqualTo(Double.POSITIVE_INFINITY);
    assertThat(Double.NaN).isEqualTo(Double.NaN);
    assertThatIsEqualToFails(-0.0, 0.0);
    assertThat((Double) null).isEqualTo(null);
    try {
      assertThat(1.23).isEqualTo(1.23f);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              String.format(
                  "Not true that <%s> (java.lang.Double) is equal to <%s> (java.lang.Float)",
                  1.23, 1.23f));
    }
  }

  private static void assertThatIsEqualToFails(double actual, double expected) {
    try {
      assertThat(actual).isEqualTo(expected);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(String.format("Not true that <%s> is equal to <%s>", actual, expected));
    }
  }

  @Test
  public void isNotEqualTo() {
    assertThatIsNotEqualToFails(1.23);
    assertThat(1.23).isNotEqualTo(Math.nextAfter(1.23, Double.POSITIVE_INFINITY));
    assertThatIsNotEqualToFails(Double.POSITIVE_INFINITY);
    assertThatIsNotEqualToFails(Double.NaN);
    assertThat(-0.0).isNotEqualTo(0.0);
    assertThatIsNotEqualToFails(null);
    assertThat(1.23).isNotEqualTo(1.23f);
  }

  private static void assertThatIsNotEqualToFails(@Nullable Double value) {
    try {
      assertThat(value).isNotEqualTo(value);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(String.format("Not true that <%s> is not equal to <%s>", value, value));
    }
  }

  @Test
  public void isZero() {
    assertThat(0.0).isZero();
    assertThat(-0.0).isZero();
    assertThatIsZeroFails(Double.MIN_VALUE);
    assertThatIsZeroFails(-1.23);
    assertThatIsZeroFails(Double.POSITIVE_INFINITY);
    assertThatIsZeroFails(Double.NaN);
    assertThatIsZeroFails(null);
  }

  private static void assertThatIsZeroFails(@Nullable Double value) {
    try {
      assertThat(value).named("testValue").isZero();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo("Not true that testValue (<" + value + ">) is zero");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  @Test
  public void isNonZero() {
    assertThatIsNonZeroFails(0.0);
    assertThatIsNonZeroFails(-0.0);
    assertThat(Double.MIN_VALUE).isNonZero();
    assertThat(-1.23).isNonZero();
    assertThat(Double.POSITIVE_INFINITY).isNonZero();
    assertThat(Double.NaN).isNonZero();
    assertThatIsNonZeroFails(null);
  }

  private static void assertThatIsNonZeroFails(@Nullable Double value) {
    try {
      assertThat(value).named("testValue").isNonZero();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo("Not true that testValue (<" + value + ">) is non-zero");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  @Test
  public void isPositiveInfinity() {
    assertThat(Double.POSITIVE_INFINITY).isPositiveInfinity();
    assertThatIsPositiveInfinityFails(1.23);
    assertThatIsPositiveInfinityFails(Double.NEGATIVE_INFINITY);
    assertThatIsPositiveInfinityFails(Double.NaN);
    assertThatIsPositiveInfinityFails(null);
  }

  private static void assertThatIsPositiveInfinityFails(@Nullable Double value) {
    try {
      assertThat(value).named("testValue").isPositiveInfinity();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo(
              "Not true that testValue (<"
                  + value
                  + ">) is equal to <"
                  + Double.POSITIVE_INFINITY
                  + ">");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  @Test
  public void isNegativeInfinity() {
    assertThat(Double.NEGATIVE_INFINITY).isNegativeInfinity();
    assertThatIsNegativeInfinityFails(1.23);
    assertThatIsNegativeInfinityFails(Double.POSITIVE_INFINITY);
    assertThatIsNegativeInfinityFails(Double.NaN);
    assertThatIsNegativeInfinityFails(null);
  }

  private static void assertThatIsNegativeInfinityFails(@Nullable Double value) {
    try {
      assertThat(value).named("testValue").isNegativeInfinity();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo(
              "Not true that testValue (<"
                  + value
                  + ">) is equal to <"
                  + Double.NEGATIVE_INFINITY
                  + ">");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  @Test
  public void isNaN() {
    assertThat(Double.NaN).isNaN();
    assertThatIsNaNFails(1.23);
    assertThatIsNaNFails(Double.POSITIVE_INFINITY);
    assertThatIsNaNFails(Double.NEGATIVE_INFINITY);
    assertThatIsNaNFails(null);
  }

  private static void assertThatIsNaNFails(@Nullable Double value) {
    try {
      assertThat(value).named("testValue").isNaN();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo("Not true that testValue (<" + value + ">) is NaN");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  @Test
  public void isFinite() {
    assertThat(1.23).isFinite();
    assertThat(Double.MAX_VALUE).isFinite();
    assertThat(-1.0 * Double.MIN_VALUE).isFinite();
    assertThatIsFiniteFails(Double.POSITIVE_INFINITY);
    assertThatIsFiniteFails(Double.NEGATIVE_INFINITY);
    assertThatIsFiniteFails(Double.NaN);
    assertThatIsFiniteFails(null);
  }

  private static void assertThatIsFiniteFails(@Nullable Double value) {
    try {
      assertThat(value).named("testValue").isFinite();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo("testValue (<" + value + ">) should have been finite");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  @Test
  public void isNotNaN() {
    assertThat(1.23).isNotNaN();
    assertThat(Double.MAX_VALUE).isNotNaN();
    assertThat(-1.0 * Double.MIN_VALUE).isNotNaN();
    assertThat(Double.POSITIVE_INFINITY).isNotNaN();
    assertThat(Double.NEGATIVE_INFINITY).isNotNaN();
    assertThatIsNotNaNFails(Double.NaN);
    assertThatIsNotNaNFails(null);
  }

  private static void assertThatIsNotNaNFails(@Nullable Double value) {
    try {
      assertThat(value).named("testValue").isNotNaN();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessageThat()
          .isEqualTo("testValue (<" + value + ">) should not have been NaN");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }
}
