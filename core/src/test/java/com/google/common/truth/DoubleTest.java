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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nullable;

/**
 * Tests for Double Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class DoubleTest {
  @Test
  public void isWithinOf() {
    assertThat(2.0).isWithin(0.00001).of(2.0);
    assertThat(2.0).isWithin(1000.0).of(2.0);
    assertThat(2.0).isWithin(1.00001).of(3.0);
  }

  @Test
  public void isNotWithinOf() {
    assertThat(2.0).isNotWithin(0.1).of(2.5);
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
      assertThat(iae).hasMessage("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  private static void isNotWithinNegativeToleranceThrowsIAE(
      double actual, double tolerance, double expected) {
    try {
      assertThat(actual).isNotWithin(tolerance).of(expected);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessage("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  @Test
  public void nanTolerances() {
    try {
      assertThat(1.0).isWithin(Double.NaN).of(1.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessage("tolerance cannot be NaN");
    }
    try {
      assertThat(1.0).isNotWithin(Double.NaN).of(2.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessage("tolerance cannot be NaN");
    }
  }

  @Test
  public void infiniteTolerances() {
    try {
      assertThat(1.0).isWithin(Double.POSITIVE_INFINITY).of(1.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessage("tolerance cannot be POSITIVE_INFINITY");
    }
    try {
      assertThat(1.0).isNotWithin(Double.POSITIVE_INFINITY).of(2.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessage("tolerance cannot be POSITIVE_INFINITY");
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
  public void isWithinOfNaN() {
    assertThatNaNFailsWithin(0.00001, Double.NaN);
    assertThatNaNFailsWithin(0.00001, 0.0);
    assertThatNaNFailsWithin(0.00001, +0.0);
    assertThatNaNFailsWithin(0.00001, -0.0);
  }

  @Test
  public void isNotWithinOfNaN() {
    assertThatNaNFailsNotWithin(0.00001, Double.NaN);
    assertThatNaNFailsNotWithin(0.00001, 0.0);
    assertThatNaNFailsNotWithin(0.00001, +0.0);
    assertThatNaNFailsNotWithin(0.00001, -0.0);
    assertThatNaNFailsNotWithin(0.00001, 1.0);
    assertThatNaNFailsNotWithin(0.00001, +1.0);
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
  }

  private static void assertThatNaNFailsWithin(double tolerance, double expected) {
    try {
      assertThat(Double.NaN).isWithin(tolerance).of(expected);
    } catch (AssertionError assertionError) {
      assertThat(assertionError.getMessage()).contains("NaN");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }

  private static void assertThatNaNFailsNotWithin(double tolerance, double expected) {
    try {
      assertThat(Double.NaN).isNotWithin(tolerance).of(expected);
    } catch (AssertionError assertionError) {
      assertThat(assertionError.getMessage()).contains("NaN");
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
      assertThat(value).isPositiveInfinity();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessage(
              "Not true that <" + value + "> is equal to <" + Double.POSITIVE_INFINITY + ">");
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
      assertThat(value).isNegativeInfinity();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessage(
              "Not true that <" + value + "> is equal to <" + Double.NEGATIVE_INFINITY + ">");
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
      assertThat(value).isNaN();
    } catch (AssertionError assertionError) {
      assertThat(assertionError)
          .hasMessage("Not true that <" + value + "> is equal to <" + Double.NaN + ">");
      return;
    }
    fail("Expected AssertionError to be thrown but wasn't");
  }
}
