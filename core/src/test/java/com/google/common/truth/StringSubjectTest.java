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

import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.annotations.GwtIncompatible;
import java.util.regex.Pattern;
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
public class StringSubjectTest {

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
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("kurt").hasLength(5));
    assertFailureValue(e, "value of", "string.length()");
  }

  @Test
  public void hasLengthNegative() {
    assertThrows(IllegalArgumentException.class, () -> assertThat("kurt").hasLength(-1));
  }

  @Test
  public void hasLengthNullString() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((String) null).hasLength(5));
    assertFailureKeys(e, "expected a string with length", "but was");
  }

  @Test
  public void stringIsEmpty() {
    assertThat("").isEmpty();
  }

  @Test
  public void stringIsEmptyFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").isEmpty());
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void stringIsEmptyFailNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((String) null).isEmpty());
    assertFailureKeys(e, "expected an empty string", "but was");
  }

  @Test
  public void stringIsNotEmpty() {
    assertThat("abc").isNotEmpty();
  }

  @Test
  public void stringIsNotEmptyFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("").isNotEmpty());
    assertFailureKeys(e, "expected not to be empty");
  }

  @Test
  public void stringIsNotEmptyFailNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((String) null).isNotEmpty());
    assertFailureKeys(e, "expected a non-empty string", "but was");
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
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").contains("d"));
    assertFailureValue(e, "expected to contain", "d");
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
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").doesNotContain("b"));
    assertFailureValue(e, "expected not to contain", "b");
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void stringEquality() {
    assertThat("abc").isEqualTo("abc");
  }

  @Test
  public void stringEqualityToNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").isEqualTo(null));
    assertThat(e).isNotInstanceOf(ComparisonFailureWithFacts.class);
  }

  @Test
  public void stringEqualityToEmpty() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").isEqualTo(""));
    assertFailureKeys(e, "expected an empty string", "but was");
  }

  @Test
  public void stringEqualityEmptyToNonEmpty() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("").isEqualTo("abc"));
    assertFailureKeys(e, "expected", "but was an empty string");
  }

  @Test
  public void stringEqualityFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").isEqualTo("ABC"));
    assertThat(e).isInstanceOf(ComparisonFailureWithFacts.class);
  }

  @Test
  public void stringStartsWith() {
    assertThat("abc").startsWith("ab");
  }

  @Test
  public void stringStartsWithFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").startsWith("bc"));
    assertFailureValue(e, "expected to start with", "bc");
  }

  @Test
  public void stringEndsWith() {
    assertThat("abc").endsWith("bc");
  }

  @Test
  public void stringEndsWithFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").endsWith("ab"));
    assertFailureValue(e, "expected to end with", "ab");
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
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abcaqadev").matches(".*aaa.*"));
    assertFailureValue(e, "expected to match", ".*aaa.*");
  }

  @Test
  public void stringMatchesStringFailNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).matches(".*aaa.*"));
    assertFailureValue(e, "expected a string that matches", ".*aaa.*");
  }

  @Test
  public void stringMatchesStringLiteralFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("$abc").matches("$abc"));
    assertFailureValue(e, "expected to match", "$abc");
    assertFailureValue(e, "but was", "$abc");
    assertThat(e)
        .factKeys()
        .contains("Looks like you want to use .isEqualTo() for an exact equality assertion.");
  }

  @Test
  public void stringMatchesStringLiteralFailButContainsMatchSuccess() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("aba").matches("[b]"));
    assertFailureValue(e, "expected to match", "[b]");
    assertFailureValue(e, "but was", "aba");
    assertThat(e).factKeys().contains("Did you mean to call containsMatch() instead of match()?");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPattern() {
    assertThat("abcaaadev").matches(Pattern.compile(".*aaa.*"));
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPatternWithFail() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that("abcaqadev").matches(Pattern.compile(".*aaa.*")));
    assertFailureValue(e, "expected to match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPatternFailNull() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).matches(Pattern.compile(".*aaa.*")));
    assertFailureValue(e, "expected a string that matches", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPatternLiteralFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("$abc").matches(Pattern.compile("$abc")));
    assertFailureValue(e, "expected to match", "$abc");
    assertFailureValue(e, "but was", "$abc");
    assertThat(e)
        .factKeys()
        .contains(
            "If you want an exact equality assertion you can escape your regex with"
                + " Pattern.quote().");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPatternLiteralFailButContainsMatchSuccess() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("aba").matches(Pattern.compile("[b]")));
    assertFailureValue(e, "expected to match", "[b]");
    assertFailureValue(e, "but was", "aba");
    assertThat(e).factKeys().contains("Did you mean to call containsMatch() instead of match()?");
  }

  @Test
  public void stringDoesNotMatchString() {
    assertThat("abcaqadev").doesNotMatch(".*aaa.*");
  }

  @Test
  public void stringDoesNotMatchStringWithFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abcaaadev").doesNotMatch(".*aaa.*"));
    assertFailureValue(e, "expected not to match", ".*aaa.*");
  }

  @Test
  public void stringDoesNotMatchStringFailNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).doesNotMatch(".*aaa.*"));
    assertFailureValue(e, "expected a string that does not match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotMatchPattern() {
    assertThat("abcaqadev").doesNotMatch(Pattern.compile(".*aaa.*"));
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotMatchPatternWithFail() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that("abcaaadev").doesNotMatch(Pattern.compile(".*aaa.*")));
    assertFailureValue(e, "expected not to match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotMatchPatternFailNull() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that((String) null).doesNotMatch(Pattern.compile(".*aaa.*")));
    assertFailureValue(e, "expected a string that does not match", ".*aaa.*");
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

    AssertionError e = expectFailure(whenTesting -> whenTesting.that("aaa").containsMatch(".*b.*"));
    assertFailureValue(e, "expected to contain a match for", ".*b.*");
  }

  @Test
  public void stringContainsMatchStringFailNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).containsMatch(".*b.*"));
    assertFailureValue(e, "expected a string that contains a match for", ".*b.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringContainsMatchPattern() {
    assertThat("aba").containsMatch(Pattern.compile(".*b.*"));

    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that("aaa").containsMatch(Pattern.compile(".*b.*")));
    assertFailureValue(e, "expected to contain a match for", ".*b.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringContainsMatchPatternFailNull() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).containsMatch(Pattern.compile(".*b.*")));
    assertFailureValue(e, "expected a string that contains a match for", ".*b.*");
  }

  @Test
  public void stringDoesNotContainMatchString() {
    assertThat("aaa").doesNotContainMatch(".*b.*");

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("aba").doesNotContainMatch(".*b.*"));
    assertFailureValue(e, "expected not to contain a match for", ".*b.*");
  }

  @Test
  public void stringDoesNotContainMatchStringUsesFind() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("aba").doesNotContainMatch("[b]"));
    assertFailureValue(e, "expected not to contain a match for", "[b]");
  }

  @Test
  public void stringDoesNotContainMatchStringUsesFindFailNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).doesNotContainMatch("[b]"));
    assertFailureValue(e, "expected a string that does not contain a match for", "[b]");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotContainMatchPattern() {
    assertThat("zzaaazz").doesNotContainMatch(Pattern.compile(".b."));

    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that("zzabazz").doesNotContainMatch(Pattern.compile(".b.")));
    assertFailureValue(e, "expected not to contain a match for", ".b.");
    assertFailureValue(e, "but contained", "aba");
    assertFailureValue(e, "full string", "zzabazz");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotContainMatchPatternFailNull() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that((String) null).doesNotContainMatch(Pattern.compile(".b.")));
    assertFailureValue(e, "expected a string that does not contain a match for", ".b.");
  }

  @Test
  public void stringEqualityIgnoringCase() {
    assertThat("café").ignoringCase().isEqualTo("CAFÉ");
  }

  @Test
  public void stringEqualityIgnoringCaseWithNullSubject() {
    assertThat((String) null).ignoringCase().isEqualTo(null);
  }

  @Test
  public void stringEqualityIgnoringCaseFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().isEqualTo("abd"));

    assertFailureValue(e, "expected", "abd");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringEqualityIgnoringCaseFailWithNullSubject() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).ignoringCase().isEqualTo("abc"));

    assertFailureValue(e, "expected a string that is equal to", "abc");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringEqualityIgnoringCaseFailWithNullExpectedString() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().isEqualTo(null));

    assertFailureValue(e, "expected", "null (null reference)");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringInequalityIgnoringCase() {
    assertThat("café").ignoringCase().isNotEqualTo("AFÉ");
  }

  @Test
  public void stringInequalityIgnoringCaseWithNullSubject() {
    assertThat((String) null).ignoringCase().isNotEqualTo("abc");
  }

  @Test
  public void stringInequalityIgnoringCaseWithNullExpectedString() {
    assertThat("abc").ignoringCase().isNotEqualTo(null);
  }

  @Test
  public void stringInequalityIgnoringCaseFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("café").ignoringCase().isNotEqualTo("CAFÉ"));

    assertFailureValue(e, "expected not to be", "CAFÉ");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringInequalityIgnoringCaseFailWithNullSubject() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).ignoringCase().isNotEqualTo(null));

    assertFailureValue(e, "expected a string that is not equal to", "null (null reference)");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringContainsIgnoringCase() {
    assertThat("äbc").ignoringCase().contains("Ä");
  }

  @Test
  public void stringContainsIgnoringCaseEmptyString() {
    assertThat("abc").ignoringCase().contains("");
  }

  @Test
  public void stringContainsIgnoringCaseWithWord() {
    assertThat("abcdé").ignoringCase().contains("CdÉ");
  }

  @Test
  public void stringContainsIgnoringCaseWholeWord() {
    assertThat("abcde").ignoringCase().contains("ABCde");
  }

  @Test
  public void stringContainsIgnoringCaseCharSeq() {
    CharSequence charSeq = new StringBuilder("C");
    assertThat("abc").ignoringCase().contains(charSeq);
  }

  @Test
  public void stringContainsIgnoringCaseFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().contains("d"));

    assertFailureValue(e, "expected to contain", "d");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringContainsIgnoringCaseFailBecauseTooLarge() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().contains("abcc"));

    assertFailureValue(e, "expected to contain", "abcc");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringContainsIgnoringCaseFailBecauseNullSubject() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).ignoringCase().contains("d"));

    assertFailureValue(e, "expected a string that contains", "d");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringDoesNotContainIgnoringCase() {
    assertThat("äbc").ignoringCase().doesNotContain("Äc");
  }

  @Test
  public void stringDoesNotContainIgnoringCaseCharSeq() {
    CharSequence charSeq = new StringBuilder("cb");
    assertThat("abc").ignoringCase().doesNotContain(charSeq);
  }

  @Test
  public void stringDoesNotContainIgnoringCaseFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("äbc").ignoringCase().doesNotContain("Äb"));

    assertFailureValue(e, "expected not to contain", "Äb");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringDoesNotContainIgnoringCaseFailWithEmptyString() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().doesNotContain(""));

    assertFailureValue(e, "expected not to contain", "");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringDoesNotContainIgnoringCaseFailBecauseNullSubject() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).ignoringCase().doesNotContain("d"));

    assertFailureValue(e, "expected a string that does not contain", "d");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void trailingWhitespaceInActual() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("foo\n").isEqualTo("foo"));
    assertFailureKeys(e, "expected", "but contained extra trailing whitespace");
    assertFailureValue(e, "but contained extra trailing whitespace", "\\n");
  }

  @Test
  public void trailingWhitespaceInExpected() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("foo").isEqualTo("foo "));
    assertFailureKeys(e, "expected", "but was missing trailing whitespace");
    assertFailureValue(e, "but was missing trailing whitespace", "␣");
  }

  @Test
  public void trailingWhitespaceInBoth() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("foo \n").isEqualTo("foo\u00a0"));
    assertFailureKeys(e, "expected", "with trailing whitespace", "but trailing whitespace was");
    assertFailureValue(e, "with trailing whitespace", "\\u00a0");
    assertFailureValue(e, "but trailing whitespace was", "␣\\n");
  }

  @Test
  public void trailingWhitespaceVsEmptyString() {
    /*
     * The code has special cases for both trailing whitespace and an empty string. Make sure that
     * it specifically reports the trailing whitespace. (It might be nice to *also* report the empty
     * string specially, but that's less important.)
     */
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("\t").isEqualTo(""));
    assertFailureKeys(e, "expected", "but contained extra trailing whitespace");
    assertFailureValue(e, "but contained extra trailing whitespace", "\\t");
  }
}
