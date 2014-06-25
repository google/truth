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

/**
 * Tests for an integer Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
abstract public class AnIntegerTest {
  @Rule
  public final Expect EXPECT = Expect.create();

  @Test
  public void simpleEquality() {
    assertThat(4).isEqualTo((byte) 4);
    assertThat(4).isEqualTo((short) 4);
    assertThat(4).isEqualTo(4);
    assertThat(4).isEqualTo(4l);
  }

  @Test
  public void isSelf() {
    assertThat(4).is(4);
  }

  @Test
  public void simpleInequality() {
    assertThat(4).isNotEqualTo((byte) 5);
    assertThat(4).isNotEqualTo((short) 5);
    assertThat(4).isNotEqualTo(5);
    assertThat(4).isNotEqualTo(5l);
  }

  @Test
  public void equalityFail() {
    try {
      assertThat(4).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).contains("Not true that <4> is equal to <5>");
    }
  }

  @Test
  public void inequalityFail() {
    try {
      assertThat(4).isNotEqualTo(4);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).contains("Not true that <4> is not equal to <4>");
    }
  }

  @Test
  public void additionAssumptionFail() {
    try {
      ASSUME.that(4).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssumptionViolatedException ignored) {
    }
  }

  @Test
  public void greaterThan() {
    assertThat(2).isGreaterThan(1);
  }

  @Test
  public void nullGreaterThanNFails() {
    try {
      assertThat(null).isGreaterThan(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is greater than <3>");
    }
  }

  @Test
  public void greaterThanFailure() {
    try {
      assertThat(2).isGreaterThan(2);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <2> is greater than <2>");
    }
  }

  @Test
  public void greaterThanOrEqual() {
    assertThat(2).isGreaterThanOrEqual(1);
    assertThat(2).isGreaterThanOrEqual(2);
  }

  @Test
  public void nullGreaterThanOrEqualNFails() {
    try {
      assertThat(null).isGreaterThanOrEqual(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is greater than or equal to <3>");
    }
  }

  @Test
  public void greaterThanOrEqualFailure() {
    try {
      assertThat(2).isGreaterThanOrEqual(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <2> is greater than or equal to <3>");
    }
  }

  @Test
  public void lessThan() {
    assertThat(1).isLessThan(2);
  }

  @Test
  public void nullLessThanNFails() {
    try {
      assertThat(null).isLessThan(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is less than <3>");
    }
  }

  @Test
  public void lessThanFailure() {
    try {
      assertThat(2).isLessThan(2);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <2> is less than <2>");
    }
  }

  @Test
  public void lessThanOrEqual() {
    assertThat(1).isLessThanOrEqual(2);
    assertThat(2).isLessThanOrEqual(2);
  }

  @Test
  public void nullLessThanOrEqualNFails() {
    try {
      assertThat(null).isLessThanOrEqual(3);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is less than or equal to <3>");
    }
  }

  @Test
  public void lessThanOrEqualFailure() {
    try {
      assertThat(3).isLessThanOrEqual(2);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <3> is less than or equal to <2>");
    }
  }

  @Test
  public void inclusiveRangeContainment() {
    EXPECT.that(2).isInclusivelyInRange(2, 4);
    EXPECT.that(3).isInclusivelyInRange(2, 4);
    EXPECT.that(4).isInclusivelyInRange(2, 4);
  }

  @Test
  public void nullInclusiveRangeContainmentFailure() {
    try {
      assertThat(null).isInclusivelyInRange(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is inclusively in range <2> <4>");
    }
  }

  @Test
  public void inclusiveRangeContainmentFailure() {
    try {
      assertThat(1).isInclusivelyInRange(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <1> is inclusively in range <2> <4>");
    }
    try {
      assertThat(5).isInclusivelyInRange(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is inclusively in range <2> <4>");
    }
  }

  @Test
  public void inclusiveRangeContainmentInversionError() {
    try {
      assertThat(3).isInclusivelyInRange(4, 2);
      fail("Should have thrown");
    } catch (IllegalArgumentException ignored) {
    }
  }

  @Test
  public void exclusiveRangeContainment() {
    EXPECT.that(3).isBetween(2, 5);
    EXPECT.that(4).isBetween(2, 5);
  }

  @Test
  public void nullExclusiveRangeContainmentFailure() {
    try {
      assertThat(null).isBetween(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is in between <2> <4>");
    }
  }

  @Test
  public void exclusiveRangeContainmentFailure() {
    try {
      assertThat(5).isBetween(2, 5);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is in between <2> <5>");
    }
  }

  @Test
  public void exclusiveRangeContainmentInversionError() {
    try {
      assertThat(3).isBetween(5, 2);
      fail("Should have thrown");
    } catch (IllegalArgumentException ignored) {
    }
  }

  @Test
  public void equalityOfNulls() {
    assertThat(null).isEqualTo((Long) null);
  }

  @Test
  public void equalityOfNullsFail() {
    try {
      assertThat(null).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is equal to <5>");
    }
    try {
      assertThat(5).isEqualTo((Long) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <5> is equal to <null>");
    }
  }

  @Test
  public void inequalityOfNulls() {
    assertThat(null).isNotEqualTo(4);
    assertThat(4).isNotEqualTo((Long) null);
  }

  @Test
  public void inequalityOfNullsFail() {
    try {
      assertThat(null).isNotEqualTo((Long) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that <null> is not equal to <null>");
    }
  }

  @Test
  public void subjectFactory() {
    ASSERT.that(getSubjectFactory().getSubjectClass()).is(assertThat(0).getClass());
  }

  abstract protected AnIntegerSubject assertThat(long target);

  abstract protected AnIntegerSubject assertThat(Long target);

  abstract protected SubjectFactory getSubjectFactory();
}
