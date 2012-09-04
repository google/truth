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

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for String Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class StringTest {

  @Test public void stringContains() {
    ASSERT.that("abc").contains("c");
  }

  @Test public void stringContainsFail() {
    try {
      ASSERT.that("abc").contains("d");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <abc> contains <d>");
      return;
    }
    fail("Should have thrown");
  }

  @Test public void stringEquality() {
    ASSERT.that("abc").isEqualTo("abc");
  }

  @Test public void stringEqualityFail() {
    try {
      ASSERT.that("abc").isEqualTo("abd");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <abc> is equal to <abd>");
      return;
    }
    fail("Should have thrown");
  }

  @Test public void stringStartsWith() {
    ASSERT.that("abc").startsWith("ab");
  }

  @Test public void stringStartsWithFail() {
    try {
      ASSERT.that("abc").startsWith("bc");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <abc> starts with <bc>");
      return;
    }
    fail("Should have thrown");
  }

  @Test public void stringEndsWith() {
    ASSERT.that("abc").endsWith("bc");
  }

  @Test public void stringEndsWithFail() {
    try {
      ASSERT.that("abc").endsWith("ab");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <abc> ends with <ab>");
      return;
    }
    fail("Should have thrown");
  }

  @Test public void emptyStringTests() {
    ASSERT.that("").contains("");
    ASSERT.that("").startsWith("");
    ASSERT.that("").endsWith("");
    ASSERT.that("a").contains("");
    ASSERT.that("a").startsWith("");
    ASSERT.that("a").endsWith("");
  }

  @Test public void stringNullNullTests() {
    ASSERT.that((String)null).contains(null);
    ASSERT.that((String)null).startsWith(null);
    ASSERT.that((String)null).endsWith(null);
  }

  @Test public void stringNullContains() {
    try {
      ASSERT.that((String)null).contains("a");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <null> contains <a>");
      return;
    }
    fail("Should have thrown");
  }

  @Test public void stringNullStartsWith() {
    try {
      ASSERT.that((String)null).startsWith("a");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <null> starts with <a>");
      return;
    }
    fail("Should have thrown");
  }

  @Test public void stringNullEndsWith() {
    try {
      ASSERT.that((String)null).endsWith("a");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <null> ends with <a>");
      return;
    }
    fail("Should have thrown");
  }

}
