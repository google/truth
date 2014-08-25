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

import com.google.common.truth.PrimitiveBooleanArraySubject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PrimitiveBooleanArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveBooleanArraySubjectTest {

  @Test public void isEqualTo() {
    ASSERT.that(array(true, false, true)).isEqualTo(array(true, false, true));
  }

  @Test public void isEqualTo_Same() {
    boolean[] same = array(true, false, true);
    ASSERT.that(same).isEqualTo(same);
  }

  @Test public void asList() {
    ASSERT.that(array(true, true, false)).asList().has().allOf(true, false);
  }

  @Test public void isEqualTo_Fail_UnequalOrdering() {
    try {
      ASSERT.that(array(true, false, true)).isEqualTo(array(false, true, true));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is(
          "Not true that <(boolean[]) [true, false, true]> is equal to <[false, true, true]>");
    }
  }

  @Test public void isEqualTo_Fail_NotAnArray() {
    try {
      ASSERT.that(array(true, false, true)).isEqualTo(new Object());
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .contains("Incompatible types compared. expected: Object, actual: boolean[]");
    }
  }

  @Test public void isNotEqualTo_SameLengths() {
    ASSERT.that(array(true, false)).isNotEqualTo(array(true, true));
  }

  @Test public void isNotEqualTo_DifferentLengths() {
    ASSERT.that(array(true, false)).isNotEqualTo(array(true, false, true));
  }

  @Test public void isNotEqualTo_DifferentTypes() {
    ASSERT.that(array(true, false)).isNotEqualTo(new Object());
  }

  @Test public void isNotEqualTo_FailEquals() {
    try {
      ASSERT.that(array(true, false)).isNotEqualTo(array(true, false));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("<(boolean[]) [true, false]> unexpectedly equal to [true, false].");
    }
  }

  @Test public void isNotEqualTo_FailSame() {
    try {
      boolean[] same = array(true, false);
      ASSERT.that(same).isNotEqualTo(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("<(boolean[]) [true, false]> unexpectedly equal to [true, false].");
    }
  }

  private static boolean[] array(boolean... ts) {
    return ts;
  }
}
