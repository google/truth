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
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for generic Subject behaviour.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class SubjectTest {
  private static final Object OBJECT_1 = new Object() {
    @Override
    public String toString() {
      return "Object 1";
    }
  };
  private static final Object OBJECT_2 = new Object() {
    @Override
    public String toString() {
      return "Object 2";
    }
  };

  @Test public void toStringsAreIdentical() {
    IntWrapper wrapper = new IntWrapper();
    wrapper.wrapped = 5;
    try {
      assertThat(5).isEqualTo(wrapper);
      fail("Should have thrown.");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage(
          "Not true that <5> (java.lang.Integer) "
          + "is equal to <5> (com.google.common.truth.SubjectTest$IntWrapper)");
    }
  }

  private static class IntWrapper {
    int wrapped;
    @Override
    public String toString() {
      return Integer.toString(wrapped);
    }
  }

  @Test public void isSameAsWithNulls() {
    Object o = null;
    assertThat(o).isSameAs(null);
  }

  @Test public void isSameAsFailureWithNulls() {
    Object o = null;
    try {
      assertThat(o).isSameAs("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is the same instance as <a>");
    }
  }

  @Test public void isSameAsWithSameObject() {
    Object a = new Object();
    Object b = a;
    assertThat(a).isSameAs(b);
  }

  @Test public void isSameAsFailureWithObjects() {
    Object a = OBJECT_1;
    Object b = OBJECT_2;
    try {
      assertThat(a).isSameAs(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "Not true that <Object 1> is the same instance as <Object 2>");
    }
  }

  @Test public void isSameAsFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append("b").toString();
    try {
      assertThat(a).isSameAs(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <ab> is the same instance as <ab>");
    }
  }

  @Test public void isSameAsFailureWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    try {
      assertThat(a).isSameAs(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <true> (java.lang.String) is the same"
          + " instance as <true> (java.lang.Boolean)");
    }
  }

  @Test public void isNotSameAsWithNulls() {
    Object o = null;
    assertThat(o).isNotSameAs("a");
  }

  @Test public void isNotSameAsFailureWithNulls() {
    Object o = null;
    try {
      assertThat(o).isNotSameAs(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "Not true that <null> is not the same instance as <null>");
    }
  }

  @Test public void isNotSameAsWithObjects() {
    Object a = new Object();
    Object b = new Object();
    assertThat(a).isNotSameAs(b);
  }

  @Test public void isNotSameAsFailureWithSameObject() {
    Object a = OBJECT_1;
    Object b = a;
    try {
      assertThat(a).isNotSameAs(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "Not true that <Object 1> is not the same instance as <Object 1>");
    }
  }

  @Test public void isNotSameAsWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append("b").toString();
    assertThat(a).isNotSameAs(b);
  }

  @Test public void isNotSameAsWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    assertThat(a).isNotSameAs(b);
  }

  @Test public void isNull() {
    Object o = null;
    assertThat(o).isNull();
  }

  @Test public void isNullFail() {
    Object o = new Object();
    try {
      assertThat(o).isNull();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <" + o.toString() + "> is null");
    }
  }

  @Test public void stringIsNullFail() {
    try {
      assertThat("foo").isNull();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <\"foo\"> is null");
    }
  }

  @Test public void isNotNull() {
    Object o = new Object();
    assertThat(o).isNotNull();
  }

  @Test public void isNotNullFail() {
    Object o = null;
    try {
      assertThat(o).isNotNull();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that the subject is a non-null reference");
    }
  }

  @Test public void isEqualToWithNulls() {
    Object o = null;
    assertThat(o).isEqualTo(null);
  }

  @Test public void isEqualToFailureWithNulls() {
    Object o = null;
    try {
      assertThat(o).isEqualTo("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is equal to <a>");
    }
  }

  @Test public void isEqualToWithSameObject() {
    Object a = new Object();
    Object b = a;
    assertThat(a).isEqualTo(b);
  }

  @Test public void isEqualToFailureWithObjects() {
    Object a = OBJECT_1;
    Object b = OBJECT_2;
    try {
      assertThat(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <Object 1> is equal to <Object 2>");
    }
  }

  @Test public void isEqualToWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append("b").toString();
    assertThat(a).isEqualTo(b);
  }

  @Test public void isEqualToFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append("a").toString();
    try {
      assertThat(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <ab> is equal to <aa>");
    }
  }

  @Test public void isEqualToFailureWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    try {
      assertThat(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <true> (java.lang.String) is equal to"
          + " <true> (java.lang.Boolean)");
    }
  }

  @Test public void isNotEqualToWithNulls() {
    Object o = null;
    assertThat(o).isNotEqualTo("a");
  }

  @Test public void isNotEqualToFailureWithNulls() {
    Object o = null;
    try {
      assertThat(o).isNotEqualTo(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is not equal to <null>");
    }
  }

  @Test public void isNotEqualToWithObjects() {
    Object a = new Object();
    Object b = new Object();
    assertThat(a).isNotEqualTo(b);
  }

  @Test public void isNotEqualToFailureWithObjects() {
    Object o = null;
    try {
      assertThat(o).isNotEqualTo(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <null> is not equal to <null>");
    }
  }

  @Test public void isNotEqualToFailureWithSameObject() {
    Object a = OBJECT_1;
    Object b = a;
    try {
      assertThat(a).isNotEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <Object 1> is not equal to <Object 1>");
    }
  }

  @Test public void isNotEqualToWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append("a").toString();
    assertThat(a).isNotEqualTo(b);
  }

  @Test public void isNotEqualToFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append("b").toString();
    try {
      assertThat(a).isNotEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <ab> is not equal to <ab>");
    }
  }

  @Test public void isNotEqualToWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    assertThat(a).isNotEqualTo(b);
  }

  @Test public void isInstanceOf() {
    assertThat("a").isInstanceOf(String.class);
  }

  @Test public void isInstanceOfFail() {
    try {
      assertThat(4.5).isInstanceOf(Long.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <4.5> is an instance of <java.lang.Long>."
          + " It is an instance of <java.lang.Double>");
    }
  }

  @Test public void isNotInstanceOf() {
    assertThat("a").isNotInstanceOf(Long.class);
  }

  @Test public void isNotInstanceOfFail() {
    try {
      assertThat(5).isNotInstanceOf(Number.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "<5> expected not to be an instance of java.lang.Number, but was.");
    }
  }

  @Test public void throwableHasInitedCause() {
    NullPointerException cause = new NullPointerException();
    String msg = "foo";
    try {
      Truth.THROW_ASSERTION_ERROR.fail(msg, cause);
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage(msg);
      assertThat(expected.getCause()).isSameAs(cause);
    }
  }

  @Test public void equalsThrowsUSOE() {
    try {
      assertThat(5).equals(5);
    } catch (UnsupportedOperationException expected) {
      assertThat(expected).hasMessage(
          "If you meant to test object equality, use .isEqualTo(other) instead.");
      return;
    }
    fail("Should have thrown.");
  }

  @Test public void hashCodeThrowsUSOE() {
    try {
      assertThat(5).hashCode();
    } catch (UnsupportedOperationException expected) {
      assertThat(expected).hasMessage("Subject.hashCode() is not supported.");
      return;
    }
    fail("Should have thrown.");
  }
}
