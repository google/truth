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

import com.google.common.truth.PrimitiveCharArraySubject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PrimitiveCharArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveCharArraySubjectTest {

  @Test public void isEqualTo() {
    ASSERT.that(array('a', 'q')).isEqualTo(array('a', 'q'));
  }

  @Test public void isEqualTo_Same() {
    char[] same = array('a', 'q');
    ASSERT.that(same).isEqualTo(same);
  }

  @Test public void asList() {
    ASSERT.that(array('a', 'q', 'z')).asList().has().allOf('a', 'z');
  }

  @Test public void isEqualTo_Fail_UnequalOrdering() {
    try {
      ASSERT.that(array('a', 'q')).isEqualTo(array('q', 'a'));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is("Not true that <(char[]) [a, q]> is equal to <[q, a]>");
    }
  }

  @Test public void isEqualTo_Fail_NotAnArray() {
    try {
      ASSERT.that(array('a', 'q')).isEqualTo(new int[] {});
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .contains("Incompatible types compared. expected: int[], actual: char[]");
    }
  }

  @Test public void isNotEqualTo_SameLengths() {
    ASSERT.that(array('a', 'q')).isNotEqualTo(array('q', 'a'));
  }

  @Test public void isNotEqualTo_DifferentLengths() {
    ASSERT.that(array('a', 'q')).isNotEqualTo(array('q', 'a', 'b'));
  }

  @Test public void isNotEqualTo_DifferentTypes() {
    ASSERT.that(array('a', 'q')).isNotEqualTo(new Object());
  }

  @Test public void isNotEqualTo_FailEquals() {
    try {
      ASSERT.that(array('a', 'q')).isNotEqualTo(array('a', 'q'));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("<(char[]) [a, q]> unexpectedly equal to [a, q].");
    }
  }

  @Test public void isNotEqualTo_FailSame() {
    try {
      char[] same = array('a', 'q');
      ASSERT.that(same).isNotEqualTo(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("<(char[]) [a, q]> unexpectedly equal to [a, q].");
    }
  }

  private static char[] array(char... ts) {
    return ts;
  }
}
