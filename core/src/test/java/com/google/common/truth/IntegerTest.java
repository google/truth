/*
 * Copyright (c) 2011 Google, Inc.
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
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.fail;

import com.google.common.truth.Expect;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Integer Subjects.
 *
 * @author David Saff
 * @author Christian Gruber
 */
@RunWith(JUnit4.class)
public class IntegerTest {
  @Rule public final Expect EXPECT = Expect.create();

  @Test
  public void simpleEquality() {
    assertThat(2 + 2).isEqualTo(4);
  }

  @Test
  public void equalityWithLongs() {
    int x = 0;
    assertThat(x).isEqualTo(0L);
    try {
      assertThat(x).isNotEqualTo(0L);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <0> is not equal to <0>");
    }
  }

  @Test
  public void intIsInt() {
    assertThat(4).isEqualTo(4);
  }

  @Test
  public void simpleInequality() {
    assertThat(2 + 2).isNotEqualTo(5);
  }

  @Test
  public void equalityFail() {
    try {
      assertThat(2 + 2).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage()).contains("Not true that <4> is equal to <5>");
    }
  }

  @Test
  public void inequalityFail() {
    try {
      assertThat(2 + 2).isNotEqualTo(4);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage()).contains("Not true that <4> is not equal to <4>");
    }
  }

  @Test
  public void additionAssumptionFail() {
    try {
      assume().that(2 + 2).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssumptionViolatedException expected) {
    }
  }

  @Test
  public void equalityOfNulls() {
    assertThat((Integer) null).isEqualTo((Long) null);
  }

  @Test
  public void equalityOfNullsFail() {
    try {
      assertThat((Long) null).isEqualTo(5);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that <null> is equal to <5>");
    }
    try {
      assertThat(5).isEqualTo((Integer) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that <5> is equal to <null>");
    }
  }

  @Test
  public void inequalityOfNulls() {
    assertThat(4).isNotEqualTo((Long) null);
    assertThat(4).isNotEqualTo((Integer) null);
    assertThat(4).isNotEqualTo((Object) null);
    assertThat(4L).isNotEqualTo((Long) null);
    assertThat(4L).isNotEqualTo((Integer) null);
    assertThat(4L).isNotEqualTo((Object) null);
    assertThat((Long) null).isNotEqualTo(4);
    assertThat((Long) null).isNotEqualTo(4L);
    assertThat((Integer) null).isNotEqualTo(4);
    assertThat((Integer) null).isNotEqualTo(4L);

    assertThat((Integer) null).isEqualTo((Integer) null);
    assertThat((Integer) null).isEqualTo((Long) null);
    assertThat((Integer) null).isEqualTo((Object) null);

    assertThat((Long) null).isEqualTo((Integer) null);
    assertThat((Long) null).isEqualTo((Long) null);
    assertThat((Long) null).isEqualTo((Object) null);
  }

  @Test
  public void inequalityOfNullsFail() {
    try {
      assertThat((Long) null).isNotEqualTo((Integer) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is not equal to <null>");
    }
    try {
      assertThat((Long) null).isNotEqualTo((Long) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is not equal to <null>");
    }
    try {
      assertThat((Integer) null).isNotEqualTo((Integer) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is not equal to <null>");
    }
    try {
      assertThat((Integer) null).isNotEqualTo((Long) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is not equal to <null>");
    }
    try {
      assertThat((Integer) null).isNotEqualTo((Object) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is not equal to <null>");
    }
    try {
      assertThat((Long) null).isNotEqualTo((Object) null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is not equal to <null>");
    }
  }

  @Test
  public void primitives() {
    Assert.assertEquals(4, 4L);
    Assert.assertEquals(4L, 4);
    assertThat(4 == 4L).isTrue();
    assertThat(4L == 4).isTrue();
    assertThat(4).isEqualTo(4L);
    assertThat(4L).isEqualTo(4);
  }

  @Test
  public void boxedPrimitives() {
    // Java says boxed primitives are not .equals().
    // Check the boolean expression with JUnit and Truth:
    Assert.assertFalse(new Integer(4).equals(new Long(4L)));
    Assert.assertFalse(new Long(4L).equals(new Integer(4)));
    assertThat(new Integer(4).equals(new Long(4L))).isFalse();
    assertThat(new Long(4L).equals(new Integer(4))).isFalse();

    // JUnit says boxed primitives are not .equals()
    try {
      Assert.assertEquals(new Integer(4), new Long(4L)); // this throws!
      fail();
    } catch (AssertionError expected) {
    }
    try {
      Assert.assertEquals(new Long(4L), new Integer(4)); // this throws!
      fail();
    } catch (AssertionError expected) {
    }

    // But boxed primitives are equal to "similar" unboxed primitives
    assertThat(new Integer(4)).isEqualTo(4L);
    assertThat(new Long(4L)).isEqualTo(4);
  }

  @Test
  public void newInteger_isNotEqualTo_Long() {
    try {
      assertThat(new Integer(4)).isNotEqualTo(new Long(4L));
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <4> is not equal to <4>");
      return;
    }
    fail("Expected an AssertionError to be thrown but wasn't");
  }

  @Test
  public void newLong_isNotEqualTo_Integer() {
    try {
      assertThat(new Long(4L)).isNotEqualTo(new Integer(4));
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <4> is not equal to <4>");
      return;
    }
    fail("Expected an AssertionError to be thrown but wasn't");
  }

  @Test
  public void mixedBoxedAndUnboxedPrimitives() {
    // Java says boxed primitives are not .equals() to primitives.
    Assert.assertFalse(new Integer(4).equals(4L));
    Assert.assertFalse(new Long(4L).equals(4));
    assertThat(new Integer(4).equals(4L)).isFalse();
    assertThat(new Long(4L).equals(4)).isFalse();

    // JUnit won't even let you do this comparison (compile error!)
    // "reference to assertEquals is ambiguous"
    // Assert.assertEquals(new Integer(4), 4L);
    // Assert.assertEquals(4L, new Integer(4));

    assertThat(new Integer(4)).isEqualTo(4L);
    assertThat(new Long(4L)).isEqualTo(4);
  }

  @Test
  public void int_isNotEqualTo_Long() {
    try {
      assertThat(4).isNotEqualTo(new Long(4L));
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <4> is not equal to <4>");
      return;
    }
    fail("Expected an AssertionError to be thrown but wasn't");
  }

  @Test
  public void long_isNotEqualTo_Integer() {
    try {
      assertThat(4L).isNotEqualTo(new Integer(4));
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <4> is not equal to <4>");
      return;
    }
    fail("Expected an AssertionError to be thrown but wasn't");
  }

  @Test
  public void isEqualTo_biggerThanIntegerMaxValue() {
    try {
      assertThat(4).isEqualTo(new Long(Integer.MAX_VALUE) + 1L);
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <4> is equal to <2147483648>");
      return;
    }
    fail("Expected an AssertionError to be thrown but wasn't");
  }

  @Test
  public void isNotEqualTo_biggerThanIntegerMaxValue() {
    assertThat(4).isNotEqualTo(new Long(Integer.MAX_VALUE) + 1L);
  }

  @Test
  public void isEqualTo_lessThanIntegerMinValue() {
    try {
      assertThat(4).isEqualTo(new Long(Integer.MIN_VALUE) - 1L);
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <4> is equal to <-2147483649>");
      return;
    }
    fail("Expected an AssertionError to be thrown but wasn't");
  }

  @Test
  public void isNotEqualTo_lessThanIntegerMinValue() {
    assertThat(4).isNotEqualTo(new Long(Integer.MIN_VALUE) - 1L);
  }
}
