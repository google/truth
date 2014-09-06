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

import org.junit.Ignore;
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

  @Test public void toStringsAreIdentical() {
    IntWrapper wrapper = new IntWrapper();
    wrapper.wrapped = 5;
    try {
      assertThat(5).isEqualTo(wrapper);
      fail("Should have thrown.");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage()).isEqualTo(
          "Not true that <5> (java.lang.Integer) "
          + "is equal to <5> (com.google.common.truth.SubjectTest.IntWrapper)");
    }
  }

  private static class IntWrapper {
    int wrapped;
    @Override
    public String toString() {
      return Integer.toString(wrapped);
    }
  }

  @Test public void identityOfNulls() {
    Object o = null;
    assertThat(o).isEqualTo(null);
  }

  @Test public void identityOfNullsFailure() {
    Object o = null;
    try {
      assertThat(o).isEqualTo("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <null> is equal to <a>");
    }
  }

  @Test public void identityOfObject() {
    Object a = new Object();
    Object b = a;
    assertThat(a).isEqualTo(b);
  }

  @Test public void identityOfObjectFailure() {
    Object a = new Object() { @Override public String toString() { return "Object 1"; } };
    Object b = new Object() { @Override public String toString() { return "Object 2"; } };
    try {
      assertThat(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <Object 1> is equal to <Object 2>");
    }
  }

  // Ignore this until we fix identity
  @Ignore @Test public void identityOfObjectFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('b').toString();
    try {
      assertThat(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <null> is <a>");
    }
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
      assertThat(e.getMessage()).isEqualTo("Not true that <" + o.toString() + "> is null");
    }
  }

  @Test public void stringIsNullFail() {
    try {
      assertThat("foo").isNull();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <\"foo\"> is null");
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
      assertThat(e.getMessage()).isEqualTo("Not true that the subject is a non-null reference");
    }
  }

  @Test public void equalityOfNulls() {
    Object o = null;
    assertThat(o).isEqualTo(null);
  }

  @Test public void equalityOfNullsFailure() {
    Object o = null;
    try {
      assertThat(o).isEqualTo("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <null> is equal to <a>");
    }
  }

  @Test public void equalityOfObjectBasedOnIdentity() {
    Object a = new Object();
    Object b = a;
    assertThat(a).isEqualTo(b);
  }

  @Test public void equalityOfObjectFailure() {
    Object a = new Object() { @Override public String toString() { return "Object 1"; } };
    Object b = new Object() { @Override public String toString() { return "Object 2"; } };
    try {
      assertThat(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <Object 1> is equal to <Object 2>");
    }
  }

  @Test public void equalityOfComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('b').toString();
    assertThat(a).isEqualTo(b);
  }

  @Test public void equalityOfComparableObjectsFailure() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('a').toString();
    try {
      assertThat(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <ab> is equal to <aa>");
    }
  }

  @Test public void inequalityOfNulls() {
    Object o = null;
    assertThat(o).isNotEqualTo("a");
  }

  @Test public void inequalityOfNullsFailure() {
    Object o = null;
    try {
      assertThat(o).isNotEqualTo(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <null> is not equal to <null>");
    }
  }

  @Test public void inequalityOfObjectBasedOnIdentity() {
    Object a = new Object();
    Object b = new Object();
    assertThat(a).isNotEqualTo(b);
  }

  @Test public void inequalityOfObjectFailure() {
    Object a = new Object() { @Override public String toString() { return "Object 1"; } };
    Object b = a;
    try {
      assertThat(a).isNotEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <Object 1> is not equal to <Object 1>");
    }
  }

  @Test public void inequalityOfComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('a').toString();
    assertThat(a).isNotEqualTo(b);
  }

  @Test public void inequalityOfComparableObjectsFailure() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('b').toString();
    try {
      assertThat(a).isNotEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <ab> is not equal to <ab>");
    }
  }

  @Test public void isInstanceOf() {
    assertThat("a").isA(String.class);
    assertThat("a").isInstanceOf(String.class);
    // Reverse
    assertThat(String.class).isAssignableFrom("a".getClass());
  }

  @Test public void isAFail() {
    try {
      assertThat(4.5).isA(Long.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <4.5> is an instance of <java.lang.Long>."
          + " It is an instance of <java.lang.Double>");
    }
  }

  @Test public void isInstanceOfFail() {
    try {
      assertThat(4.5).isInstanceOf(Long.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <4.5> is an instance of <java.lang.Long>."
          + " It is an instance of <java.lang.Double>");
    }
  }

  @Test public void isNotInstanceOf() {
    assertThat("a").isNotA(Long.class);
    assertThat("a").isNotInstanceOf(Long.class);
  }

  @Test public void isNotAFail() {
    try {
      assertThat(5).isNotA(Number.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .isEqualTo("<5> expected not to be an instance of java.lang.Number, but was.");
    }
  }

  @Test public void isNotInstanceOfFail() {
    try {
      assertThat(5).isNotInstanceOf(Number.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .isEqualTo("<5> expected not to be an instance of java.lang.Number, but was.");
    }
  }
}
