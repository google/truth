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
package org.truth0.subjects;

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.truth0.Expect;

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;
import static org.truth0.Truth.ASSUME;
import static org.truth0.subjects.LongSubject.LONG;

/**
 * Tests for Long Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class LongTest {
  @Rule public final Expect EXPECT = Expect.create();

  @Test public void simpleEquality() {
    ASSERT.that(4l).isEqualTo((byte)4);
    ASSERT.that(4l).isEqualTo((short)4);
    ASSERT.that(4l).isEqualTo(4);
    ASSERT.that(4l).isEqualTo(4l);
  }

  @Test public void longIsLong() {
    ASSERT.that(4l).is(4);
  }

  @Test public void simpleInequality() {
    ASSERT.that(4l).isNotEqualTo((byte)5);
    ASSERT.that(4l).isNotEqualTo((short)5);
    ASSERT.that(4l).isNotEqualTo(5);
    ASSERT.that(4l).isNotEqualTo(5l);
  }

  @Test public void equalityFail() {
    try {
      ASSERT.that(4l).isEqualTo(5l);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).contains("Not true that <4> is equal to <5>");
    }
  }

  @Test public void inequalityFail() {
    try {
      ASSERT.that(4l).isNotEqualTo(4l);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).contains("Not true that <4> is not equal to <4>");
    }
  }

  @Test public void additionAssumptionFail() {
    try {
      ASSUME.that(4l).isEqualTo(5l);
      fail("Should have thrown");
    } catch (AssumptionViolatedException ignored) {}
  }

  @Test public void greaterThan() {
    ASSERT.that(2l).isGreaterThan(1l);
  }

  @Test public void nullGreaterThanNFails() {
    try {
      ASSERT.that((Long)null).isGreaterThan(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is greater than <3>");
    }
  }

  @Test public void greaterThanFailure() {
    try {
      ASSERT.that(2l).isGreaterThan(2l);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <2> is greater than <2>");
    }
  }

  @Test public void greaterThanOrEqual() {
    ASSERT.that(2l).isGreaterThanOrEqual(1l);
    ASSERT.that(2l).isGreaterThanOrEqual(2l);
  }

  @Test public void nullGreaterThanOrEqualNFails() {
    try {
      ASSERT.that((Long)null).isGreaterThanOrEqual(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is greater than or equal to <3>");
    }
  }

  @Test public void greaterThanOrEqualFailure() {
    try {
      ASSERT.that(2l).isGreaterThanOrEqual(3l);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <2> is greater than or equal to <3>");
    }
  }

  @Test public void lessThan() {
    ASSERT.that(1l).isLessThan(2l);
  }

  @Test public void nullLessThanNFails() {
    try {
      ASSERT.that((Long)null).isLessThan(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is less than <3>");
    }
  }

  @Test public void lessThanFailure() {
    try {
      ASSERT.that(2l).isLessThan(2l);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <2> is less than <2>");
    }
  }

  @Test public void lessThanOrEqual() {
    ASSERT.that(1l).isLessThanOrEqual(2l);
    ASSERT.that(2l).isLessThanOrEqual(2l);
  }

  @Test public void nullLessThanOrEqualNFails() {
    try {
      ASSERT.that((Long)null).isLessThanOrEqual(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is less than or equal to <3>");
    }
  }

  @Test public void lessThanOrEqualFailure() {
    try {
      ASSERT.that(3l).isLessThanOrEqual(2l);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <3> is less than or equal to <2>");
    }
  }

  @Test public void inclusiveRangeContainment() {
    EXPECT.that(2l).isInclusivelyInRange(2l, 4l);
    EXPECT.that(3l).isInclusivelyInRange(2l, 4l);
    EXPECT.that(4l).isInclusivelyInRange(2l, 4l);
  }

  @Test public void nullInclusiveRangeContainmentFailure() {
    try {
      ASSERT.that((Long)null).isInclusivelyInRange(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is inclusively in range <2> <4>");
    }
  }

  @Test public void inclusiveRangeContainmentFailure() {
    try {
      ASSERT.that(1l).isInclusivelyInRange(2l, 4l);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <1> is inclusively in range <2> <4>");
    }
    try {
      ASSERT.that(5l).isInclusivelyInRange(2l, 4l);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is inclusively in range <2> <4>");
    }
  }

  @Test public void inclusiveRangeContainmentInversionError() {
    try {
      ASSERT.that(Long.MAX_VALUE).isInclusivelyInRange(4l, 2l);
      fail("Should have thrown");
    } catch (IllegalArgumentException ignored) {}
  }

  @Test public void exclusiveRangeContainment() {
    EXPECT.that(3l).isBetween(2l, 5l);
    EXPECT.that(4l).isBetween(2l, 5l);
  }

  @Test public void nullExclusiveRangeContainmentFailure() {
    try {
      ASSERT.that((Long)null).isBetween(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is in between <2> <4>");
    }
  }

  @Test public void exclusiveRangeContainmentFailure() {
    try {
      ASSERT.that(5l).isBetween(2l, 5l);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is in between <2> <5>");
    }
  }

  @Test public void exclusiveRangeContainmentInversionError() {
    try {
      ASSERT.that(Long.MAX_VALUE).isBetween(5l, 2l);
      fail("Should have thrown");
    } catch (IllegalArgumentException ignored) {}
  }

  @Test public void equalityOfNulls() {
    ASSERT.that((Long)null).isEqualTo((Long)null);
  }

  @Test public void equalityOfNullsFail() {
    try {
      ASSERT.that((Long)null).isEqualTo(5l);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is equal to <5>");
    }
    try {
      ASSERT.that(5l).isEqualTo((Long)null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is equal to <null>");
    }
  }

  @Test public void inequalityOfNulls() {
    ASSERT.that((Long)null).isNotEqualTo(4l);
    ASSERT.that(4l).isNotEqualTo((Long)null);
  }

  @Test public void inequalityOfNullsFail() {
    try {
      ASSERT.that((Long)null).isNotEqualTo((Long)null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is not equal to <null>");
    }
  }

  @Test public void subjectFactory() {
    ASSERT.that(LONG.getSubjectClass()).is(LongSubject.class);
  }
}
