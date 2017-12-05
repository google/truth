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

import com.google.common.annotations.GwtIncompatible;
import java.util.regex.Pattern;
import org.junit.ComparisonFailure;
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
public class StringSubjectTest extends BaseSubjectTestCase {

  @Test
  public void hasLength() {
    assertThat("kurt").hasLength(4);
  }

  @Test
  public void hasLengthZero() {
    assertThat("").hasLength(0);
  }

  @Test
  public void hasLengthFails() {
    expectFailure.whenTesting().that("kurt").hasLength(5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"kurt\"> has a length of 5. It is 4.");
  }

  @Test
  public void hasLengthNegative() {
    try {
      assertThat("kurt").hasLength(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void stringIsEmpty() {
    assertThat("").isEmpty();
  }

  @Test
  public void stringIsEmptyFail() {
    expectFailure.whenTesting().that("abc").isEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"abc\"> is empty");
  }

  @Test
  public void stringIsNotEmpty() {
    assertThat("abc").isNotEmpty();
  }

  @Test
  public void stringIsNotEmptyFail() {
    expectFailure.whenTesting().that("").isNotEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"\"> is not empty");
  }

  @Test
  public void stringContains() {
    assertThat("abc").contains("c");
  }

  @Test
  public void stringContainsCharSeq() {
    CharSequence charSeq = new StringBuilder("c");
    assertThat("abc").contains(charSeq);
  }

  @Test
  public void stringContainsFail() {
    expectFailure.whenTesting().that("abc").contains("d");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <\"abc\"> contains <\"d\">");
  }

  @Test
  public void stringDoesNotContain() {
    assertThat("abc").doesNotContain("d");
  }

  @Test
  public void stringDoesNotContainCharSequence() {
    CharSequence charSeq = new StringBuilder("d");
    assertThat("abc").doesNotContain(charSeq);
  }

  @Test
  public void stringDoesNotContainFail() {
    expectFailure.whenTesting().that("abc").doesNotContain("b");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("<\"abc\"> unexpectedly contains <\"b\">");
  }

  @Test
  public void stringEquality() {
    assertThat("abc").isEqualTo("abc");
    assertThat("abc").isEqualTo("abc");
  }

  @Test
  public void stringEqualityToNull() {
    expectFailure.whenTesting().that("abc").isEqualTo(null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <\"abc\"> is null");
  }

  @Test
  @GwtIncompatible("ComparisonFailure")
  public void stringEqualityFail() {
    try {
      assertThat("abc").isEqualTo("abd");
    } catch (ComparisonFailure expected) {
      assertThat(expected).hasMessageThat().isEqualTo("expected:<ab[d]> but was:<ab[c]>");
      // truth used to create a synthetic cause, make sure that isn't happening anymore.
      assertThat(expected).hasCauseThat().isNull();
    }
  }

  @Test
  @GwtIncompatible("ComparisonFailure")
  public void stringNamedEqualityFail() {
    try {
      assertThat("abc").named("foo").isEqualTo("abd");
    } catch (ComparisonFailure expected) {
      assertThat(expected).hasMessageThat().isEqualTo("\"foo\": expected:<ab[d]> but was:<ab[c]>");
    }
  }

  @Test
  public void stringNamedNullFail() {
    expectFailure.whenTesting().that((String) null).named("foo").isEqualTo("abd");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that foo (<null>) is equal to <\"abd\">");
  }

  @Test
  public void stringStartsWith() {
    assertThat("abc").startsWith("ab");
  }

  @Test
  public void stringStartsWithFail() {
    expectFailure.whenTesting().that("abc").startsWith("bc");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <\"abc\"> starts with <\"bc\">");
  }

  @Test
  public void stringEndsWith() {
    assertThat("abc").endsWith("bc");
  }

  @Test
  public void stringEndsWithFail() {
    expectFailure.whenTesting().that("abc").endsWith("ab");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <\"abc\"> ends with <\"ab\">");
  }

  @Test
  public void emptyStringTests() {
    assertThat("").contains("");
    assertThat("").startsWith("");
    assertThat("").endsWith("");
    assertThat("a").contains("");
    assertThat("a").startsWith("");
    assertThat("a").endsWith("");
  }

  @Test
  public void stringMatchesString() {
    assertThat("abcaaadev").matches(".*aaa.*");
  }

  @Test
  public void stringMatchesStringWithFail() {
    expectFailure.whenTesting().that("abcaqadev").matches(".*aaa.*");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"abcaqadev\"> matches <.*aaa.*>");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPattern() {
    assertThat("abcaqadev").doesNotMatch(Pattern.compile(".*aaa.*"));
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPatternWithFail() {
    expectFailure.whenTesting().that("abcaaadev").doesNotMatch(Pattern.compile(".*aaa.*"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"abcaaadev\"> fails to match <.*aaa.*>");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringContainsMatchStringUsesFind() {
    assertThat("aba").containsMatch("[b]");
    assertThat("aba").containsMatch(Pattern.compile("[b]"));
  }

  @Test
  public void stringContainsMatchString() {
    assertThat("aba").containsMatch(".*b.*");

    expectFailure.whenTesting().that("aaa").containsMatch(".*b.*");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<\"aaa\"> should have contained a match for <.*b.*>");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringContainsMatchPattern() {
    assertThat("aba").containsMatch(Pattern.compile(".*b.*"));

    expectFailure.whenTesting().that("aaa").containsMatch(Pattern.compile(".*b.*"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<\"aaa\"> should have contained a match for <.*b.*>");
  }

  @Test
  public void stringDoesNotContainMatchString() {
    assertThat("aaa").doesNotContainMatch(".*b.*");

    expectFailure.whenTesting().that("aba").doesNotContainMatch(".*b.*");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<\"aba\"> should not have contained a match for <.*b.*>");
  }

  @Test
  public void stringDoesNotContainMatchStringUsesFind() {
    expectFailure.whenTesting().that("aba").doesNotContainMatch("[b]");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<\"aba\"> should not have contained a match for <[b]>");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotContainMatchPattern() {
    assertThat("aaa").doesNotContainMatch(Pattern.compile(".*b.*"));

    expectFailure.whenTesting().that("aba").doesNotContainMatch(Pattern.compile(".*b.*"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<\"aba\"> should not have contained a match for <.*b.*>");
  }
}
