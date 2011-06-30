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

  @Test public void identityOfNulls() {
    Object o = null;
    ASSERT.that(o).is(null);
  }

  @Test public void identityOfNullsFailure() {
    Object o = null;
    try {
      ASSERT.that(o).is("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <null> is <a>");
    }
  }

  @Test public void identityOfObject() {
    Object a = new Object();
    Object b = a;
    ASSERT.that(a).is(b);
  }

  @Test public void identityOfObjectFailure() {
    Object a = new Object() { @Override public String toString() { return "Object 1"; } };
    Object b = new Object() { @Override public String toString() { return "Object 2"; } };
    try {
      ASSERT.that(a).is(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <Object 1> is <Object 2>");
    }
  }

  // Ignore this until we fix identity
  @Ignore @Test public void identityOfObjectFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('b').toString();
    try {
      ASSERT.that(a).is(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <null> is <a>");
    }
  }

  @Test public void isNull() {
    Object o = null;
    ASSERT.that(o).isNull();
  }

  @Test public void isNullFail() {
    Object o = new Object();
    try {
      ASSERT.that(o).isNull();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that the subject is null");
    }
  }

  @Test public void isNotNull() {
    Object o = new Object();
    ASSERT.that(o).isNotNull();
  }

  @Test public void isNotNullFail() {
    Object o = null;
    try {
      ASSERT.that(o).isNotNull();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that the subject is not null");
    }
  }

  @Test public void equalityOfNulls() {
    Object o = null;
    ASSERT.that(o).isEqualTo(null);
  }

  @Test public void equalityOfNullsFailure() {
    Object o = null;
    try {
      ASSERT.that(o).isEqualTo("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <null> is equal to <a>");
    }
  }

  @Test public void equalityOfObjectBasedOnIdentity() {
    Object a = new Object();
    Object b = a;
    ASSERT.that(a).isEqualTo(b);
  }

  @Test public void equalityOfObjectFailure() {
    Object a = new Object() { @Override public String toString() { return "Object 1"; } };
    Object b = new Object() { @Override public String toString() { return "Object 2"; } };
    try {
      ASSERT.that(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <Object 1> is equal to <Object 2>");
    }
  }

  @Test public void equalityOfComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('b').toString();
    ASSERT.that(a).isEqualTo(b);
  }

  @Test public void equalityOfComparableObjectsFailure() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('a').toString();
    try {
      ASSERT.that(a).isEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <ab> is equal to <aa>");
    }
  }

  @Test public void inequalityOfNulls() {
    Object o = null;
    ASSERT.that(o).isNotEqualTo("a");
  }

  @Test public void inequalityOfNullsFailure() {
    Object o = null;
    try {
      ASSERT.that(o).isNotEqualTo(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <null> is not equal to <null>");
    }
  }

  @Test public void inequalityOfObjectBasedOnIdentity() {
    Object a = new Object();
    Object b = new Object();
    ASSERT.that(a).isNotEqualTo(b);
  }

  @Test public void inequalityOfObjectFailure() {
    Object a = new Object() { @Override public String toString() { return "Object 1"; } };
    Object b = a;
    try {
      ASSERT.that(a).isNotEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <Object 1> is not equal to <Object 1>");
    }
  }

  @Test public void inequalityOfComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('a').toString();
    ASSERT.that(a).isNotEqualTo(b);
  }

  @Test public void inequalityOfComparableObjectsFailure() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('b').toString();
    try {
      ASSERT.that(a).isNotEqualTo(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <ab> is not equal to <ab>");
    }
  }

  @Test public void isA() {
    ASSERT.that("a").isA(String.class);
  }

  @Test public void isAFail() {
    try {
      ASSERT.that("a").isA(Long.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <a> is a <java.lang.Long>");
    }
  }

  @Test public void isNotA() {
    ASSERT.that("a").isNotA(Long.class);
  }

  @Test public void isNotAFail() {
    try {
      ASSERT.that("a").isNotA(String.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo("Not true that <a> is not a <java.lang.String>");
    }
  }

}
