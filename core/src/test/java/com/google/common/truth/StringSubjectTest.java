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
  public void isEmpty() {
    assertThat("").isEmpty();
  }

  @Test
  public void isEmptyFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").isEmpty());
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void isEmptyFailNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((String) null).isEmpty());
    assertFailureKeys(e, "expected an empty string", "but was");
  }

  @Test
  public void isNotEmpty() {
    assertThat("abc").isNotEmpty();
  }

  @Test
  public void isNotEmptyFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("").isNotEmpty());
    assertFailureKeys(e, "expected not to be empty");
  }

  @Test
  public void isNotEmptyFailNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((String) null).isNotEmpty());
    assertFailureKeys(e, "expected a non-empty string", "but was");
  }

  @Test
  public void contains() {
    assertThat("abc").contains("c");
  }

  @Test
  public void containsCharSeq() {
    CharSequence charSeq = new StringBuilder("c");
    assertThat("abc").contains(charSeq);
  }

  @Test
  public void containsFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").contains("d"));
    assertFailureValue(e, "expected to contain", "d");
  }

  @Test
  public void doesNotContain() {
    assertThat("abc").doesNotContain("d");
  }

  @Test
  public void doesNotContainCharSequence() {
    CharSequence charSeq = new StringBuilder("d");
    assertThat("abc").doesNotContain(charSeq);
  }

  @Test
  public void doesNotContainFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").doesNotContain("b"));
    assertFailureValue(e, "expected not to contain", "b");
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void equality() {
    assertThat("abc").isEqualTo("abc");
  }

  @Test
  public void equalityToNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").isEqualTo(null));
    assertThat(e).isNotInstanceOf(ComparisonFailureWithFacts.class);
  }

  @Test
  public void equalityToEmpty() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").isEqualTo(""));
    assertFailureKeys(e, "expected an empty string", "but was");
  }

  @Test
  public void equalityEmptyToNonEmpty() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("").isEqualTo("abc"));
    assertFailureKeys(e, "expected", "but was an empty string");
  }

  @Test
  public void equalityFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").isEqualTo("ABC"));
    assertThat(e).isInstanceOf(ComparisonFailureWithFacts.class);
  }

  @Test
  public void startsWith() {
    assertThat("abc").startsWith("ab");
  }

  @Test
  public void startsWithFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("abc").startsWith("bc"));
    assertFailureValue(e, "expected to start with", "bc");
  }

  @Test
  public void endsWith() {
    assertThat("abc").endsWith("bc");
  }

  @Test
  public void endsWithFail() {
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
  public void matchesString() {
    assertThat("abcaaadev").matches(".*aaa.*");
  }

  @Test
  public void matchesStringWithFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abcaqadev").matches(".*aaa.*"));
    assertFailureValue(e, "expected to match", ".*aaa.*");
  }

  @Test
  public void matchesStringFailNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).matches(".*aaa.*"));
    assertFailureValue(e, "expected a string that matches", ".*aaa.*");
  }

  @Test
  public void matchesStringLiteralFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("$abc").matches("$abc"));
    assertFailureValue(e, "expected to match", "$abc");
    assertFailureValue(e, "but was", "$abc");
    assertThat(e)
        .factKeys()
        .contains("Looks like you want to use .isEqualTo() for an exact equality assertion.");
  }

  @Test
  public void matchesStringLiteralFailButContainsMatchSuccess() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("aba").matches("[b]"));
    assertFailureValue(e, "expected to match", "[b]");
    assertFailureValue(e, "but was", "aba");
    assertThat(e).factKeys().contains("Did you mean to call containsMatch() instead of match()?");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void matchesPattern() {
    assertThat("abcaaadev").matches(Pattern.compile(".*aaa.*"));
  }

  @Test
  @GwtIncompatible("Pattern")
  public void matchesPatternWithFail() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that("abcaqadev").matches(Pattern.compile(".*aaa.*")));
    assertFailureValue(e, "expected to match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void matchesPatternFailNull() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).matches(Pattern.compile(".*aaa.*")));
    assertFailureValue(e, "expected a string that matches", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void matchesPatternLiteralFail() {
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
  public void matchesPatternLiteralFailButContainsMatchSuccess() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("aba").matches(Pattern.compile("[b]")));
    assertFailureValue(e, "expected to match", "[b]");
    assertFailureValue(e, "but was", "aba");
    assertThat(e).factKeys().contains("Did you mean to call containsMatch() instead of match()?");
  }

  @Test
  public void doesNotMatchString() {
    assertThat("abcaqadev").doesNotMatch(".*aaa.*");
  }

  @Test
  public void doesNotMatchStringWithFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abcaaadev").doesNotMatch(".*aaa.*"));
    assertFailureValue(e, "expected not to match", ".*aaa.*");
  }

  @Test
  public void doesNotMatchStringFailNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).doesNotMatch(".*aaa.*"));
    assertFailureValue(e, "expected a string that does not match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void doesNotMatchPattern() {
    assertThat("abcaqadev").doesNotMatch(Pattern.compile(".*aaa.*"));
  }

  @Test
  @GwtIncompatible("Pattern")
  public void doesNotMatchPatternWithFail() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that("abcaaadev").doesNotMatch(Pattern.compile(".*aaa.*")));
    assertFailureValue(e, "expected not to match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void doesNotMatchPatternFailNull() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that((String) null).doesNotMatch(Pattern.compile(".*aaa.*")));
    assertFailureValue(e, "expected a string that does not match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void containsMatchStringUsesFind() {
    assertThat("aba").containsMatch("[b]");
    assertThat("aba").containsMatch(Pattern.compile("[b]"));
  }

  @Test
  public void containsMatchString() {
    assertThat("aba").containsMatch(".*b.*");

    AssertionError e = expectFailure(whenTesting -> whenTesting.that("aaa").containsMatch(".*b.*"));
    assertFailureValue(e, "expected to contain a match for", ".*b.*");
  }

  @Test
  public void containsMatchStringFailNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).containsMatch(".*b.*"));
    assertFailureValue(e, "expected a string that contains a match for", ".*b.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void containsMatchPattern() {
    assertThat("aba").containsMatch(Pattern.compile(".*b.*"));

    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that("aaa").containsMatch(Pattern.compile(".*b.*")));
    assertFailureValue(e, "expected to contain a match for", ".*b.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void containsMatchPatternFailNull() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).containsMatch(Pattern.compile(".*b.*")));
    assertFailureValue(e, "expected a string that contains a match for", ".*b.*");
  }

  @Test
  public void doesNotContainMatchString() {
    assertThat("aaa").doesNotContainMatch(".*b.*");

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("aba").doesNotContainMatch(".*b.*"));
    assertFailureValue(e, "expected not to contain a match for", ".*b.*");
  }

  @Test
  public void doesNotContainMatchStringUsesFind() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("aba").doesNotContainMatch("[b]"));
    assertFailureValue(e, "expected not to contain a match for", "[b]");
  }

  @Test
  public void doesNotContainMatchStringUsesFindFailNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).doesNotContainMatch("[b]"));
    assertFailureValue(e, "expected a string that does not contain a match for", "[b]");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void doesNotContainMatchPattern() {
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
  public void doesNotContainMatchPatternFailNull() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that((String) null).doesNotContainMatch(Pattern.compile(".b.")));
    assertFailureValue(e, "expected a string that does not contain a match for", ".b.");
  }

  @Test
  public void equalityIgnoringCase() {
    assertThat("café").ignoringCase().isEqualTo("CAFÉ");
  }

  @Test
  public void equalityIgnoringCaseWithNullSubject() {
    assertThat((String) null).ignoringCase().isEqualTo(null);
  }

  @Test
  public void equalityIgnoringCaseFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().isEqualTo("abd"));

    assertFailureValue(e, "expected", "abd");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void equalityIgnoringCaseFailWithNullSubject() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).ignoringCase().isEqualTo("abc"));

    assertFailureValue(e, "expected a string that is equal to", "abc");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void equalityIgnoringCaseFailWithNullExpectedString() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().isEqualTo(null));

    assertFailureValue(e, "expected", "null (null reference)");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void inequalityIgnoringCase() {
    assertThat("café").ignoringCase().isNotEqualTo("AFÉ");
  }

  @Test
  public void inequalityIgnoringCaseWithNullSubject() {
    assertThat((String) null).ignoringCase().isNotEqualTo("abc");
  }

  @Test
  public void inequalityIgnoringCaseWithNullExpectedString() {
    assertThat("abc").ignoringCase().isNotEqualTo(null);
  }

  @Test
  public void inequalityIgnoringCaseFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("café").ignoringCase().isNotEqualTo("CAFÉ"));

    assertFailureValue(e, "expected not to be", "CAFÉ");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void inequalityIgnoringCaseFailWithNullSubject() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((String) null).ignoringCase().isNotEqualTo(null));

    assertFailureValue(e, "expected a string that is not equal to", "null (null reference)");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void containsIgnoringCase() {
    assertThat("äbc").ignoringCase().contains("Ä");
  }

  @Test
  public void containsIgnoringCaseEmptyString() {
    assertThat("abc").ignoringCase().contains("");
  }

  @Test
  public void containsIgnoringCaseWithWord() {
    assertThat("abcdé").ignoringCase().contains("CdÉ");
  }

  @Test
  public void containsIgnoringCaseWholeWord() {
    assertThat("abcde").ignoringCase().contains("ABCde");
  }

  @Test
  public void containsIgnoringCaseCharSeq() {
    CharSequence charSeq = new StringBuilder("C");
    assertThat("abc").ignoringCase().contains(charSeq);
  }

  @Test
  public void containsIgnoringCaseFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().contains("d"));

    assertFailureValue(e, "expected to contain", "d");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void containsIgnoringCaseFailBecauseTooLarge() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().contains("abcc"));

    assertFailureValue(e, "expected to contain", "abcc");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void containsIgnoringCaseFailBecauseNullSubject() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((String) null).ignoringCase().contains("d"));

    assertFailureValue(e, "expected a string that contains", "d");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void doesNotContainIgnoringCase() {
    assertThat("äbc").ignoringCase().doesNotContain("Äc");
  }

  @Test
  public void doesNotContainIgnoringCaseCharSeq() {
    CharSequence charSeq = new StringBuilder("cb");
    assertThat("abc").ignoringCase().doesNotContain(charSeq);
  }

  @Test
  public void doesNotContainIgnoringCaseFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("äbc").ignoringCase().doesNotContain("Äb"));

    assertFailureValue(e, "expected not to contain", "Äb");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void doesNotContainIgnoringCaseFailWithEmptyString() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("abc").ignoringCase().doesNotContain(""));

    assertFailureValue(e, "expected not to contain", "");
    assertThat(e).factKeys().contains("(case is ignored)");
  }

  @Test
  public void doesNotContainIgnoringCaseFailBecauseNullSubject() {
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
