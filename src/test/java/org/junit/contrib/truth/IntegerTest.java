/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.junit.contrib.truth;

import static org.junit.Assert.fail;
import static org.junit.contrib.truth.Truth.ASSERT;
import static org.junit.contrib.truth.Truth.ASSUME;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.truth.Expect;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Integer Subjects.
 * 
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IntegerTest {
  @Rule public Expect EXPECT = Expect.create();

  @Test public void simpleEquality() {
    ASSERT.that(2 + 2).isEqualTo(4).and().isBetween(3, 5);
  }
  
  @Test public void simpleInequality() {
    ASSERT.that(2 + 2).isNotEqualTo(5);
  }

  @Test public void equalityFail() {
    try {
      ASSERT.that(2 + 2).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).contains("Not true that <4> is equal to <5>");
    }
  }
  
  @Test public void inequalityFail() {
    try {
      ASSERT.that(2 + 2).isNotEqualTo(4);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).contains("Not true that <4> is not equal to <4>");
    }
  }

  @Test public void additionAssumptionFail() {
    try {
      ASSUME.that(2 + 2).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssumptionViolatedException expected) {}
  }

  @Test public void inclusiveRangeContainment() {
    EXPECT.that(2).isInclusivelyInRange(2, 4);
    EXPECT.that(3).isInclusivelyInRange(2, 4);
    EXPECT.that(4).isInclusivelyInRange(2, 4);
  }

  @Test public void inclusiveRangeContainmentFailure() {
    try {
      ASSERT.that(1).isInclusivelyInRange(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <1> is inclusively in range <2> <4>");
    }
    try {
      ASSERT.that(5).isInclusivelyInRange(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is inclusively in range <2> <4>");
    }
  }

  @Test public void inclusiveRangeContainmentInversionError() {
    try {
      ASSERT.that(Integer.MAX_VALUE).isInclusivelyInRange(4, 2);
      fail("Should have thrown");
    } catch (IllegalArgumentException e) {}
  }

  @Test public void exclusiveRangeContainment() {
    EXPECT.that(3).isBetween(2, 5);
    EXPECT.that(4).isBetween(2, 5);
  }

  @Test public void exclusiveRangeContainmentFailure() {
    try {
      ASSERT.that(5).isBetween(2, 5);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is in between <2> <5>");
    }
  }

  @Test public void exclusiveRangeContainmentInversionError() {
    try {
      ASSERT.that(Integer.MAX_VALUE).isBetween(5, 2);
      fail("Should have thrown");
    } catch (IllegalArgumentException e) {}
  }

  @Test public void equalityOfNulls() {
    ASSERT.that((Integer)null).isEqualTo((Long)null);
  }
  
  @Test public void equalityOfNullsFail() {
    try {
      ASSERT.that((Long)null).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is equal to <5>");
    }
    try {
      ASSERT.that(5).isEqualTo((Integer)null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is equal to <null>");
    }
  }

  @Test public void inequalityOfNulls() {
    ASSERT.that((Long)null).isNotEqualTo(4);
    ASSERT.that(4).isNotEqualTo((Long)null);
  }

  @Test public void inequalityOfNullsFail() {
    try {
      ASSERT.that((Long)null).isNotEqualTo((Integer)null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is not equal to <null>");
    }
  }
  
}
