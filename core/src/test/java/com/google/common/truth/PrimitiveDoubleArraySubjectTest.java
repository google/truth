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

import static com.google.common.truth.Truth.ASSERT;

import com.google.common.truth.PrimitiveDoubleArraySubject;

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
  private static final float DEFAULT_TOLERANCE = 0.000005f;

  @SuppressWarnings("deprecation")
  @Test public void isEqualTo_Default_Fail() {
    try {
      ASSERT.that(array(2.2d, 5.4d)).isEqualTo(array(2.2d, 5.4d));
      throw new Error("Expected to throw.");
    } catch (UnsupportedOperationException expected) {}
  }

  @Test public void isEqualTo() {
    ASSERT.that(array(2.2d, 5.4d)).isEqualTo(array(2.2d, 5.4d), DEFAULT_TOLERANCE);
  }

  @Test public void isEqualTo_Fail_UnequalOrdering() {
    try {
      ASSERT.that(array(2.2d, 3.3d)).isEqualTo(array(3.3d, 2.2d), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is("Not true that <(double[]) [2.2, 3.3]> is equal to <[3.3, 2.2]>");
    }
  }

  @Test public void isEqualTo_Fail_NotAnArray() {
    try {
      ASSERT.that(array(2.2d, 3.3d, 4.4d)).isEqualTo(new Object(), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .contains("Incompatible types compared. expected: Object, actual: double[]");
    }
  }

  @SuppressWarnings("deprecation")
  @Test public void isNotEqualTo_Default_Fail() {
    try {
      ASSERT.that(array(2.2d, 5.4d)).isNotEqualTo(array(5.4d, 2.2d));
      throw new Error("Expected to throw.");
    } catch (UnsupportedOperationException expected) {}
  }

  @Test public void isNotEqualTo_SameLengths() {
    ASSERT.that(array(2.2d, 3.3d)).isNotEqualTo(array(3.3d, 2.2d), DEFAULT_TOLERANCE);
  }

  @Test public void isNotEqualTo_DifferentLengths() {
    ASSERT.that(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d, 3.3d, 1.1d), DEFAULT_TOLERANCE);
  }

  @Test public void isNotEqualTo_DifferentTypes() {
    ASSERT.that(array(2.2d, 3.3d)).isNotEqualTo(new Object(), DEFAULT_TOLERANCE);
  }

  @Test public void isNotEqualTo_FailEquals() {
    try {
      ASSERT.that(array(2.2d, 3.3d)).isNotEqualTo(array(2.2d, 3.3d), DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("<(double[]) [2.2, 3.3]> unexpectedly equal to [2.2, 3.3].");
    }
  }

  @Test public void isNotEqualTo_FailSame() {
    try {
      double[] same = array(2.2d, 3.3d);
      ASSERT.that(same).isNotEqualTo(same, DEFAULT_TOLERANCE);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("<(double[]) [2.2, 3.3]> unexpectedly equal to [2.2, 3.3].");
    }
  }

  private static double[] array(double... primitives) {
    return primitives;
  }
}
