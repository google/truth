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
  
  private Object o = null;

  @Test public void identityOfNulls() {
    ASSERT.that(o).is(null);
  }

  @Test public void identityOfNullsFailure() {
    try {
      ASSERT.that(o).is("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is("Not true that <null> is <a>");
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
      ASSERT.that(e.getMessage()).is("Not true that <Object 1> is <Object 2>");
    }
  }
  
  // Ignore this until we fix identity
  @Ignore @Test public void identityOfObjectFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder().append("a").append('b').toString();
    try {
      ASSERT.that((Object)a).is(b);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is("Not true that <null> is <a>");
    }
  }

  @Test public void isNull() {
    ASSERT.that(o).isNull();
  }

  @Test public void isNullFail() {
    o = new Object();
    try {
      ASSERT.that(o).isNull();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is("Not true that the subject is null");
    }
  }
  
  @Test public void isNotNull() {
    o = new Object();
    ASSERT.that(o).isNotNull();
  }

  @Test public void isNotNullFail() {
    try {
      ASSERT.that(o).isNotNull();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is("Not true that the subject is not null");
    }
  }
}
