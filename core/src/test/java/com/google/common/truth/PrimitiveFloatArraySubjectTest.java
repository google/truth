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
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.NaN;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.nextAfter;
import static org.junit.Assert.fail;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Longs;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PrimitiveFloatArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveFloatArraySubjectTest {
  private static final float DEFAULT_TOLERANCE = 0.000005f;

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Default_Fail() {
    try {
      assertThat(array(2.2f, 5.4f)).isEqualTo(array(2.2f, 5.4f));
      throw new Error("Expected to throw.");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo() {
    assertThat(array(2.2f, 5.4f)).isEqualTo(array(2.2f, 5.4f), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_ApproximatelyEquals() {
    assertThat(array(2.2f, 3.3f))
        .isEqualTo(
            array(2.2f, nextAfter(3.3f + DEFAULT_TOLERANCE, NEGATIVE_INFINITY)), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_FailNotQuiteApproximatelyEquals() {
    float roughly3point3 = nextAfter(3.3f + DEFAULT_TOLERANCE, POSITIVE_INFINITY);
    try {
      assertThat(array(2.2f, 3.3f)).isEqualTo(array(2.2f, roughly3point3), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> is equal to <[2.2, " + roughly3point3 + "]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_DifferentOrder() {
    try {
      assertThat(array(2.2f, 3.3f)).isEqualTo(array(3.3f, 2.2f), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <(float[]) [2.2, 3.3]> is equal to <[3.3, 2.2]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_Longer() {
    try {
      assertThat(array(2.2f, 3.3f)).isEqualTo(array(2.2f, 3.3f, 1.1f), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Arrays are of different lengths. expected: [2.2, 3.3, 1.1], actual [2.2, 3.3]");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_Shorter() {
    try {
      assertThat(array(2.2f, 3.3f)).isEqualTo(array(2.2f), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Arrays are of different lengths. expected: [2.2], actual [2.2, 3.3]");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_NotAnArray() {
    try {
      assertThat(array(2.2f, 3.3f, 4.4f)).isEqualTo(new Object(), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .contains("Incompatible types compared. expected: Object, actual: float[]");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_Infinity() {
    try {
      assertThat(array(2.2f, POSITIVE_INFINITY))
          .isEqualTo(array(2.2f, POSITIVE_INFINITY), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <(float[]) [2.2, Infinity]> is equal to <[2.2, Infinity]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_SameInfinity() {
    float[] same = array(2.2f, POSITIVE_INFINITY);
    assertThat(same).isEqualTo(same, DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_OneInfinity() {
    try {
      assertThat(array(2.2f, 3.3f)).isEqualTo(array(2.2f, POSITIVE_INFINITY), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <(float[]) [2.2, 3.3]> is equal to <[2.2, Infinity]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_LongerOneInfinity() {
    try {
      assertThat(array(2.2f, 3.3f)).isEqualTo(array(POSITIVE_INFINITY), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Arrays are of different lengths. expected: [Infinity], actual [2.2, 3.3]");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isEqualTo_Fail_NaN() {
    try {
      assertThat(array(NaN)).isEqualTo(array(NaN), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <(float[]) [NaN]> is equal to <[NaN]>");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Default_Fail() {
    try {
      assertThat(array(2.2f, 5.4f)).isNotEqualTo(array(5.4f, 2.2f));
      throw new Error("Expected to throw.");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_DifferentOrder() {
    assertThat(array(2.2f, 3.3f)).isNotEqualTo(array(3.3f, 2.2f), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Longer() {
    assertThat(array(2.2f, 3.3f)).isNotEqualTo(array(2.2f, 3.3f, 1.1f), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Shorter() {
    assertThat(array(2.2f, 3.3f)).isNotEqualTo(array(2.2f), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_DifferentTypes() {
    assertThat(array(2.2f, 3.3f)).isNotEqualTo(new Object(), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_FailEquals() {
    try {
      assertThat(array(2.2f, 3.3f)).isNotEqualTo(array(2.2f, 3.3f), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("<(float[]) [2.2, 3.3]> unexpectedly equal to [2.2, 3.3].");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_FailApproximatelyEquals() {
    float roughly3point3 = nextAfter(3.3f + DEFAULT_TOLERANCE, NEGATIVE_INFINITY);
    try {
      assertThat(array(2.2f, 3.3f)).isNotEqualTo(array(2.2f, roughly3point3), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("<(float[]) [2.2, 3.3]> unexpectedly equal to [2.2, " + roughly3point3 + "].");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_NotQuiteApproximatelyEquals() {
    assertThat(array(2.2f, 3.3f))
        .isNotEqualTo(
            array(2.2f, nextAfter(3.3f + DEFAULT_TOLERANCE, POSITIVE_INFINITY)), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_FailSame() {
    try {
      float[] same = array(2.2f, 3.3f);
      assertThat(same).isNotEqualTo(same, DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("<(float[]) [2.2, 3.3]> unexpectedly equal to [2.2, 3.3].");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Fail_Infinity() {
    assertThat(array(2.2f, POSITIVE_INFINITY))
        .isNotEqualTo(array(2.2f, POSITIVE_INFINITY), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Fail_SameInfinity() {
    try {
      float[] same = array(2.2f, POSITIVE_INFINITY);
      assertThat(same).isNotEqualTo(same, DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("<(float[]) [2.2, Infinity]> unexpectedly equal to [2.2, Infinity].");
    }
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_OneInfinity() {
    assertThat(array(2.2f, 3.3f)).isNotEqualTo(array(2.2f, POSITIVE_INFINITY), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_LongerOneInfinity() {
    assertThat(array(2.2f, 3.3f)).isNotEqualTo(array(POSITIVE_INFINITY), DEFAULT_TOLERANCE);
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  public void isNotEqualTo_Fail_NaN() {
    assertThat(array(NaN)).isNotEqualTo(array(NaN), DEFAULT_TOLERANCE);
  }

  @Test
  public void hasValuesWithinOf() {
    assertThat(array(2.2f, 5.4f)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2f, 5.4f);
  }

  @Test
  public void hasValuesWithinOf_ApproximatelyEquals() {
    assertThat(array(2.2f, 3.3f))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .of(2.2f, nextAfter(3.3f + DEFAULT_TOLERANCE, NEGATIVE_INFINITY));
  }

  @Test
  public void hasValuesWithinOf_FailNotQuiteApproximatelyEquals() {
    float roughly3point3 = nextAfter(3.3f + DEFAULT_TOLERANCE, POSITIVE_INFINITY);
    try {
      assertThat(array(2.2f, 3.3f)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2f, roughly3point3);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, "
                  + roughly3point3
                  + "]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_DifferentOrder() {
    try {
      assertThat(array(2.2f, 3.3f)).hasValuesWithin(DEFAULT_TOLERANCE).of(3.3f, 2.2f);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[3.3, 2.2]>."
                  + " It differs at indexes <[0, 1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_Longer() {
    try {
      assertThat(array(2.2f, 3.3f)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2f, 3.3f, 1.1f);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, 3.3, 1.1]>."
                  + " Expected length <3> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_Shorter() {
    try {
      assertThat(array(2.2f, 3.3f)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2f);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2]>."
                  + " Expected length <1> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_Infinity() {
    try {
      assertThat(array(2.2f, POSITIVE_INFINITY))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .of(2.2f, POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, Infinity]> has values within 5.0E-6 of"
                  + " <[2.2, Infinity]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_SameInfinity() {
    float[] same = array(2.2f, POSITIVE_INFINITY);
    try {
      assertThat(same).hasValuesWithin(DEFAULT_TOLERANCE).of(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, Infinity]> has values within 5.0E-6 of"
                  + " <[2.2, Infinity]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_OneInfinity() {
    try {
      assertThat(array(2.2f, 3.3f)).hasValuesWithin(DEFAULT_TOLERANCE).of(2.2f, POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, Infinity]>."
                  + " It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOf_Fail_LongerOneInfinity() {
    try {
      assertThat(array(2.2f, 3.3f)).hasValuesWithin(DEFAULT_TOLERANCE).of(POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[Infinity]>."
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
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [NaN]> has values within 5.0E-6 of <[NaN]>."
                  + " It differs at indexes <[0]>");
    }
  }

  @Test
  public void hasValuesWithinOf_NullSubject() {
    float[] nullArray = null;
    try {
      assertThat(nullArray).hasValuesWithin(DEFAULT_TOLERANCE).of(3.3f, 2.2f);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOf_NullObbject() {
    float[] nullArray = null;
    try {
      assertThat(array(3.3f, 2.2f)).hasValuesWithin(DEFAULT_TOLERANCE).of(nullArray);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOf_NegativeTolerance() {
    try {
      assertThat(array(3.3f, 2.2f)).hasValuesWithin(-0.001f).of(3.3f, 2.2f);
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_floats() {
    assertThat(array(2.2f, 5.4f))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Floats.asList(2.2f, 5.4f));
  }

  @Test
  public void hasValuesWithinOfElementsIn_doubles() {
    assertThat(array(2.2f, 5.4f))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(2.2d, 5.4d));
  }

  @Test
  public void hasValuesWithinOfElementsIn_longs() {
    assertThat(array(2.0f, 5.0f))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Longs.asList(2L, 5L));
  }

  @Test
  public void hasValuesWithinOfElementsIn_ApproximatelyEquals() {
    assertThat(array(2.2f, 3.3f))
        .hasValuesWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Floats.asList(2.2f, nextAfter(3.3f + DEFAULT_TOLERANCE, NEGATIVE_INFINITY)));
  }

  @Test
  public void hasValuesWithinOfElementsIn_FailNotQuiteApproximatelyEquals() {
    float roughly3point3 = nextAfter(3.3f + DEFAULT_TOLERANCE, POSITIVE_INFINITY);
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f, roughly3point3));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, "
                  + roughly3point3
                  + "]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_DifferentOrder() {
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(3.3f, 2.2f));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[3.3, 2.2]>."
                  + " It differs at indexes <[0, 1]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Longer() {
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f, 3.3f, 1.1f));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, 3.3, 1.1]>."
                  + " Expected length <3> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Shorter() {
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2]>."
                  + " Expected length <1> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_Infinity() {
    try {
      assertThat(array(2.2f, POSITIVE_INFINITY))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f, POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, Infinity]> has values within 5.0E-6 of"
                  + " <[2.2, Infinity]>. It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_OneInfinity() {
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f, POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[2.2, Infinity]>."
                  + " It differs at indexes <[1]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_LongerOneInfinity() {
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values within 5.0E-6 of <[Infinity]>."
                  + " Expected length <1> but got <2>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_Fail_NaN() {
    try {
      assertThat(array(NaN)).hasValuesWithin(DEFAULT_TOLERANCE).ofElementsIn(Floats.asList(NaN));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [NaN]> has values within 5.0E-6 of <[NaN]>."
                  + " It differs at indexes <[0]>");
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_NullSubject() {
    float[] nullArray = null;
    try {
      assertThat(nullArray)
          .hasValuesWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(3.3f, 2.2f));
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_NullObject() {
    Iterable<Number> nullIterable = null;
    try {
      assertThat(array(3.3f, 2.2f)).hasValuesWithin(DEFAULT_TOLERANCE).ofElementsIn(nullIterable);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void hasValuesWithinOfElementsIn_NegativeTolerance() {
    try {
      assertThat(array(3.3f, 2.2f))
          .hasValuesWithin(-0.001f)
          .ofElementsIn(Floats.asList(3.3f, 2.2f));
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_DifferentOrder() {
    assertThat(array(2.2f, 3.3f)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(3.3f, 2.2f);
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_DifferentLengths() {
    assertThat(array(2.2f, 3.3f)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(2.2f, 3.3f, 1.1f);
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_FailEquals() {
    try {
      assertThat(array(2.2f, 3.3f)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(2.2f, 3.3f);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, 3.3]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_FailApproximatelyEquals() {
    float roughly3point3 = nextAfter(3.3f + DEFAULT_TOLERANCE, NEGATIVE_INFINITY);
    try {
      assertThat(array(2.2f, 3.3f)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(2.2f, roughly3point3);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, "
                  + roughly3point3
                  + "]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NotQuiteApproximatelyEquals() {
    assertThat(array(2.2f, 3.3f))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .of(2.2f, nextAfter(3.3f + DEFAULT_TOLERANCE, POSITIVE_INFINITY));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_FailSame() {
    try {
      float[] same = array(2.2f, 3.3f);
      assertThat(same).hasValuesNotWithin(DEFAULT_TOLERANCE).of(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, 3.3]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_Infinity() {
    try {
      assertThat(array(2.2f, POSITIVE_INFINITY))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .of(2.2f, POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, Infinity]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_SameInfinity() {
    try {
      float[] same = array(2.2f, POSITIVE_INFINITY);
      assertThat(same).hasValuesNotWithin(DEFAULT_TOLERANCE).of(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, Infinity]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_OneInfinity() {
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .of(2.2f, POSITIVE_INFINITY);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_LongerOneInfinity() {
    assertThat(array(2.2f, 3.3f)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(POSITIVE_INFINITY);
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_Fail_NaN() {
    try {
      assertThat(array(NaN)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(NaN);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <(float[]) [NaN]> has values not within 5.0E-6 of <[NaN]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NullSubject() {
    float[] nullArray = null;
    try {
      assertThat(nullArray).hasValuesNotWithin(DEFAULT_TOLERANCE).of(3.3f, 2.2f);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NullObject() {
    float[] nullArray = null;
    try {
      assertThat(array(3.3f, 2.2f)).hasValuesNotWithin(DEFAULT_TOLERANCE).of(nullArray);
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOf_NegativeTolerance() {
    try {
      assertThat(array(3.3f, 2.2f)).hasValuesNotWithin(-0.001f).of(3.3f, 2.2f);
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_DifferentOrderFloats() {
    assertThat(array(2.2f, 3.3f))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Floats.asList(3.3f, 2.2f));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_DifferentOrderDoubles() {
    assertThat(array(2.2f, 3.3f))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Doubles.asList(3.3d, 2.2d));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_DifferentOrderLongs() {
    assertThat(array(2f, 3f))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Longs.asList(3L, 2L));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_DifferentLengths() {
    assertThat(array(2.2f, 3.3f))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Floats.asList(2.2f, 3.3f, 1.1f));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_FailEquals() {
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f, 3.3f));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, 3.3]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_FailApproximatelyEquals() {
    float roughly3point3 = nextAfter(3.3f + DEFAULT_TOLERANCE, NEGATIVE_INFINITY);
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f, roughly3point3));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values not within 5.0E-6 of <[2.2, "
                  + roughly3point3
                  + "]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NotQuiteApproximatelyEquals() {
    assertThat(array(2.2f, 3.3f))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Floats.asList(2.2f, nextAfter(3.3f + DEFAULT_TOLERANCE, POSITIVE_INFINITY)));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_Fail_Infinity() {
    try {
      assertThat(array(2.2f, POSITIVE_INFINITY))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f, POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, Infinity]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_OneInfinity() {
    try {
      assertThat(array(2.2f, 3.3f))
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(2.2f, POSITIVE_INFINITY));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <(float[]) [2.2, 3.3]> has values not within 5.0E-6 of"
                  + " <[2.2, Infinity]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_LongerOneInfinity() {
    assertThat(array(2.2f, 3.3f))
        .hasValuesNotWithin(DEFAULT_TOLERANCE)
        .ofElementsIn(Floats.asList(POSITIVE_INFINITY));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_Fail_NaN() {
    try {
      assertThat(array(NaN)).hasValuesNotWithin(DEFAULT_TOLERANCE).ofElementsIn(Floats.asList(NaN));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <(float[]) [NaN]> has values not within 5.0E-6 of <[NaN]>");
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NullSubject() {
    float[] nullArray = null;
    try {
      assertThat(nullArray)
          .hasValuesNotWithin(DEFAULT_TOLERANCE)
          .ofElementsIn(Floats.asList(3.3f, 2.2f));
      throw new Error("Expected to throw.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hasValuesNotWithinOfElementsIn_NullObject() {
    Iterable<Number> nullIterable = null;
    try {
      assertThat(array(3.3f, 2.2f))
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
      assertThat(array(3.3f, 2.2f))
          .hasValuesNotWithin(-0.001f)
          .ofElementsIn(Floats.asList(3.3f, 2.2f));
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("tolerance (-0.001) cannot be negative");
    }
  }

  @Test
  public void usingTolerance_contains_success() {
    assertThat(array(1.0f, nextAfter(2.0f + DEFAULT_TOLERANCE, NEGATIVE_INFINITY), 3.0f))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2.0f);
  }

  @Test
  public void usingTolerance_contains_successWithExpectedLong() {
    assertThat(array(1.0f, nextAfter(2.0f + DEFAULT_TOLERANCE, NEGATIVE_INFINITY), 3.0f))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2L);
  }

  @Test
  public void usingTolerance_contains_failure() {
    float justOverTwoPlusTolerance = nextAfter(2.0f + DEFAULT_TOLERANCE, POSITIVE_INFINITY);
    try {
      assertThat(array(1.0f, justOverTwoPlusTolerance, 3.0f))
          .usingTolerance(DEFAULT_TOLERANCE)
          .contains(2.0f);
      fail("Expected AssertionError to be thrown but wasn't");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1.0, "
                  + justOverTwoPlusTolerance
                  + ", 3.0]> contains at least one element that is a finite number within "
                  + (double) DEFAULT_TOLERANCE
                  + " of <2.0>");
    }
  }

  @Test
  public void usingTolerance_contains_failureWithInfinity() {
    try {
      assertThat(array(1.0f, POSITIVE_INFINITY, 3.0f))
          .usingTolerance(DEFAULT_TOLERANCE)
          .contains(POSITIVE_INFINITY);
      fail("Expected AssertionError to be thrown but wasn't");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1.0, Infinity, 3.0]> contains at least one element that is "
                  + "a finite number within "
                  + (double) DEFAULT_TOLERANCE
                  + " of <Infinity>");
    }
  }

  @Test
  public void usingTolerance_contains_failureWithNaN() {
    try {
      assertThat(array(1.0f, NaN, 3.0f))
          .usingTolerance(DEFAULT_TOLERANCE)
          .contains(NaN);
      fail("Expected AssertionError to be thrown but wasn't");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1.0, NaN, 3.0]> contains at least one element that is "
                  + "a finite number within "
                  + (double) DEFAULT_TOLERANCE
                  + " of <NaN>");
    }
  }

  @Test
  public void usingTolerance_contains_successWithNegativeZero() {
    assertThat(array(1.0f, -0.0f, 3.0f))
        .usingTolerance(0.0f)
        .contains(0.0f);
  }

  @Test
  public void usingTolerance_contains_otherTypes() {
    // Expected value is Double
    assertThat(array(1.0f, 2.0f + 0.5f * DEFAULT_TOLERANCE, 3.0f))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2.0);
    // Expected value is Integer
    assertThat(array(1.0f, 2.0f + 0.5f * DEFAULT_TOLERANCE, 3.0f))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2);
    // Expected value is Integer.MIN_VALUE. This is -1*2^31, which has an exact float
    // representation. For the actual value we use the next value down, which is 2^8 smaller
    // (because the resolution of floats with absolute values between 2^31 and 2^32 is 2^8). So
    // we'll make the assertion with a tolerance of 2^9.
    assertThat(array(1.0f, Integer.MIN_VALUE + 0.5f * DEFAULT_TOLERANCE, 3.0f))
        .usingTolerance(1 << 9)
        .contains(Integer.MIN_VALUE);
    // Expected value is Long
    assertThat(array(1.0f, 2.0f + 0.5f * DEFAULT_TOLERANCE, 3.0f))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(2L);
    // Expected value is Long.MIN_VALUE. This is -1*2^63, which has an exact float representation.
    // For the actual value we use the next value down, which is is 2^40 smaller (because the
    // resolution of floats with absolute values between 2^63 and 2^64 is 2^40). So we'll make the
    // assertion with a tolerance of 2^41.
    assertThat(array(1.0f, nextAfter(Long.MIN_VALUE, NEGATIVE_INFINITY), 3.0f))
        .usingTolerance(1L << 41)
        .contains(Long.MIN_VALUE);
    // Expected value is BigInteger
    assertThat(array(1.0f, 2.0f + 0.5f * DEFAULT_TOLERANCE, 3.0f))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(BigInteger.valueOf(2));
    // Expected value is BigDecimal
    assertThat(array(1.0f, 2.0f + 0.5f * DEFAULT_TOLERANCE, 3.0f))
        .usingTolerance(DEFAULT_TOLERANCE)
        .contains(BigDecimal.valueOf(2.0));
  }

  @Test
  public void usingTolerance_contains_nullExpected() {
    try {
      assertThat(array(1.0f, 2.0f, 3.0f)).usingTolerance(DEFAULT_TOLERANCE).contains(null);
      fail("Expected NullPointerException to be thrown but wasn't");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void usingTolerance_contains_negativeTolerance() {
    try {
      assertThat(array(1.0f, 2.0f, 3.0f)).usingTolerance(-1.0f * DEFAULT_TOLERANCE).contains(2.0f);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("tolerance (" + -1.0 * DEFAULT_TOLERANCE + ") cannot be negative");
    }
  }

  @Test
  public void usingExactEquality_contains_success() {
    assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains(2.0f);
  }

  @Test
  public void usingExactEquality_contains_failure() {
    float justOverTwo = nextAfter(2.0f, POSITIVE_INFINITY);
    try {
      assertThat(array(1.0f, justOverTwo, 3.0f)).usingExactEquality().contains(2.0f);
      fail("Expected AssertionError to be thrown but wasn't");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1.0, "
                  + justOverTwo
                  + ", 3.0]> contains at least one element that is exactly equal to <2.0>");
    }
  }

  @Test
  public void usingExactEquality_contains_otherTypes() {
    // Expected value is Integer - supported up to +/- 2^24
    assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains(2);
    assertThat(array(1.0f, 1 << 24, 3.0f)).usingExactEquality().contains(1 << 24);
    try {
      assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains((1 << 24) + 1);
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Expected value 16777217 in assertion using exact float equality was an int with an "
                  + "absolute value greater than 2^24 which has no exact float representation");
    }
    // Expected value is Long - supported up to +/- 2^24
    assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains(2L);
    assertThat(array(1.0f, 1 << 24, 3.0f)).usingExactEquality().contains(1L << 24);
    try {
      assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains((1L << 24) + 1L);
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Expected value 16777217 in assertion using exact float equality was a long with an "
                  + "absolute value greater than 2^24 which has no exact float representation");
    }
    // Expected value is Double - not supported
    try {
      assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains(2.0);
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Expected value in assertion using exact float equality was a double, which is not "
                  + "supported as a double may not have an exact float representation");
    }
    // Expected value is BigInteger - not supported
    try {
      assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains(BigInteger.valueOf(2));
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Expected value in assertion using exact float equality was of unsupported type "
                  + BigInteger.class
                  + " (it may not have an exact float representation)");
    }
    // Expected value is BigDecimal - not supported
    try {
      assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains(BigDecimal.valueOf(2.0));
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
      .hasMessageThat()
      .isEqualTo(
          "Expected value in assertion using exact float equality was of unsupported type "
              + BigDecimal.class
              + " (it may not have an exact float representation)");
    }
  }

  @Test
  public void usingExactEquality_contains_successWithInfinity() {
    assertThat(array(1.0f, POSITIVE_INFINITY, 3.0f))
        .usingExactEquality()
        .contains(POSITIVE_INFINITY);
  }

  @Test
  public void usingExactEquality_contains_successWithNaN() {
    assertThat(array(1.0f, NaN, 3.0f)).usingExactEquality().contains(NaN);
  }

  @Test
  public void usingExactEquality_contains_failureWithNegativeZero() {
    try {
      assertThat(array(1.0f, -0.0f, 3.0f)).usingExactEquality().contains(0.0f);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1.0, -0.0, 3.0]> contains at least one element that is "
                  + "exactly equal to <0.0>");
    }
  }

  @Test
  public void usingExactEquality_contains_nullExpected() {
    try {
      assertThat(array(1.0f, 2.0f, 3.0f)).usingExactEquality().contains(null);
      fail("Expected NullPointerException to be thrown but wasn't");
    } catch (NullPointerException expected) {
    }
  }

  private static float[] array(float... primitives) {
    return primitives;
  }
}
