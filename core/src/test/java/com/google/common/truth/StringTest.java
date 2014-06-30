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

import static org.truth0.Truth.ASSERT;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.regex.Pattern;

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
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <\"abc\"> contains <\"d\">");
    }
  }

  @Test public void stringDoesNotContain() {
    ASSERT.that("abc").doesNotContain("d");
  }

  @Test public void stringDoesNotContainFail() {
    try {
      ASSERT.that("abc").doesNotContain("b");
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("<\"abc\"> unexpectedly contains <\"b\">");
      return;
    }
  }

  @Test public void stringEquality() {
    ASSERT.that("abc").is("abc");
    ASSERT.that("abc").isEqualTo("abc");
  }

  @Test public void stringEqualityToNull() {
    try {
      ASSERT.that("abc").is(null);
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <\"abc\"> is null");
    }
  }

  @Test public void stringEqualityFail() {
    try {
      ASSERT.that("abc").is("abd");
      throw new Error("Expected to fail.");
    } catch (ComparisonFailure expected) {
      ASSERT.that(expected.getMessage())
          .contains("expected:<ab[d]> but was:<ab[c]>");
    }
  }

  @Test public void stringStartsWith() {
    ASSERT.that("abc").startsWith("ab");
  }

  @Test public void stringStartsWithFail() {
    try {
      ASSERT.that("abc").startsWith("bc");
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <\"abc\"> starts with <\"bc\">");
    }
  }

  @Test public void stringEndsWith() {
    ASSERT.that("abc").endsWith("bc");
  }

  @Test public void stringEndsWithFail() {
    try {
      ASSERT.that("abc").endsWith("ab");
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <\"abc\"> ends with <\"ab\">");
    }
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
    ASSERT.that((String)null).is(null);
    ASSERT.that((String)null).isEqualTo(null);
    try {
      ASSERT.that((String)null).contains(null);
      ASSERT.fail("Expected to throw");
    } catch (IllegalArgumentException expected) {}
    try {
      ASSERT.that((String)null).doesNotContain(null);
      ASSERT.fail("Expected to throw");
    } catch (IllegalArgumentException expected) {}
    try {
      ASSERT.that((String)null).startsWith(null);
      ASSERT.fail("Expected to throw");
    } catch (IllegalArgumentException expected) {}
    try {
      ASSERT.that((String)null).endsWith(null);
      ASSERT.fail("Expected to throw");
    } catch (IllegalArgumentException expected) {}
  }

  @Test public void stringNullContains() {
    try {
      ASSERT.that((String)null).contains("a");
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that null reference contains <\"a\">");
    }
  }

  @Test public void stringNullStartsWith() {
    try {
      ASSERT.that((String)null).startsWith("a");
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that null reference starts with <\"a\">");
    }
  }

  @Test public void stringNullEndsWith() {
    try {
      ASSERT.that((String)null).endsWith("a");
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that null reference ends with <\"a\">");
    }
  }

  @Test public void stringMatchesString() {
    ASSERT.that("abcaaadev").matches(".*aaa.*");
  }

  @Test public void stringMatchesStringWithFail() {
    try {
      ASSERT.that("abcaqadev").matches(".*aaa.*");
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <\"abcaqadev\"> matches <.*aaa.*>");
    }
  }

  @Test public void stringMatchesPattern() {
    ASSERT.that("abcaqadev").doesNotMatch(Pattern.compile(".*aaa.*"));
  }

  @Test public void stringMatchesPatternWithFail() {
    try {
      ASSERT.that("abcaaadev").doesNotMatch(Pattern.compile(".*aaa.*"));
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that <\"abcaaadev\"> fails to match <.*aaa.*>");
    }
  }
}
