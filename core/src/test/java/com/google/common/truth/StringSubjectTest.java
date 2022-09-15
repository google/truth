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
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

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
    expectFailureWhenTestingThat("kurt").hasLength(5);
    assertFailureValue("value of", "string.length()");
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
    expectFailureWhenTestingThat("abc").isEmpty();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void stringIsEmptyFailNull() {
    expectFailureWhenTestingThat(null).isEmpty();
    assertFailureKeys("expected an empty string", "but was");
  }

  @Test
  public void stringIsNotEmpty() {
    assertThat("abc").isNotEmpty();
  }

  @Test
  public void stringIsNotEmptyFail() {
    expectFailureWhenTestingThat("").isNotEmpty();
    assertFailureKeys("expected not to be empty");
  }

  @Test
  public void stringIsNotEmptyFailNull() {
    expectFailureWhenTestingThat(null).isNotEmpty();
    assertFailureKeys("expected a non-empty string", "but was");
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
    expectFailureWhenTestingThat("abc").contains("d");
    assertFailureValue("expected to contain", "d");
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
    expectFailureWhenTestingThat("abc").doesNotContain("b");
    assertFailureValue("expected not to contain", "b");
  }

  @Test
  public void stringEquality() {
    assertThat("abc").isEqualTo("abc");
  }

  @Test
  public void stringEqualityToNull() {
    expectFailureWhenTestingThat("abc").isEqualTo(null);
    assertThat(expectFailure.getFailure()).isNotInstanceOf(ComparisonFailureWithFacts.class);
  }

  @Test
  public void stringEqualityToEmpty() {
    expectFailureWhenTestingThat("abc").isEqualTo("");
    assertFailureKeys("expected an empty string", "but was");
  }

  @Test
  public void stringEqualityEmptyToNonEmpty() {
    expectFailureWhenTestingThat("").isEqualTo("abc");
    assertFailureKeys("expected", "but was an empty string");
  }

  @Test
  public void stringEqualityFail() {
    expectFailureWhenTestingThat("abc").isEqualTo("ABC");
    assertThat(expectFailure.getFailure()).isInstanceOf(ComparisonFailureWithFacts.class);
  }

  @Test
  public void stringStartsWith() {
    assertThat("abc").startsWith("ab");
  }

  @Test
  public void stringStartsWithFail() {
    expectFailureWhenTestingThat("abc").startsWith("bc");
    assertFailureValue("expected to start with", "bc");
  }

  @Test
  public void stringEndsWith() {
    assertThat("abc").endsWith("bc");
  }

  @Test
  public void stringEndsWithFail() {
    expectFailureWhenTestingThat("abc").endsWith("ab");
    assertFailureValue("expected to end with", "ab");
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
    expectFailureWhenTestingThat("abcaqadev").matches(".*aaa.*");
    assertFailureValue("expected to match", ".*aaa.*");
  }

  @Test
  public void stringMatchesStringFailNull() {
    expectFailureWhenTestingThat(null).matches(".*aaa.*");
    assertFailureValue("expected a string that matches", ".*aaa.*");
  }

  @Test
  public void stringMatchesStringLiteralFail() {
    expectFailureWhenTestingThat("$abc").matches("$abc");
    assertFailureValue("expected to match", "$abc");
    assertFailureValue("but was", "$abc");
    assertThat(expectFailure.getFailure())
        .factKeys()
        .contains("Looks like you want to use .isEqualTo() for an exact equality assertion.");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPattern() {
    assertThat("abcaaadev").matches(Pattern.compile(".*aaa.*"));
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPatternWithFail() {
    expectFailureWhenTestingThat("abcaqadev").matches(Pattern.compile(".*aaa.*"));
    assertFailureValue("expected to match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPatternFailNull() {
    expectFailureWhenTestingThat(null).matches(Pattern.compile(".*aaa.*"));
    assertFailureValue("expected a string that matches", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringMatchesPatternLiteralFail() {
    expectFailureWhenTestingThat("$abc").matches(Pattern.compile("$abc"));
    assertFailureValue("expected to match", "$abc");
    assertFailureValue("but was", "$abc");
    assertThat(expectFailure.getFailure())
        .factKeys()
        .contains(
            "If you want an exact equality assertion you can escape your regex with"
                + " Pattern.quote().");
  }

  @Test
  public void stringDoesNotMatchString() {
    assertThat("abcaqadev").doesNotMatch(".*aaa.*");
  }

  @Test
  public void stringDoesNotMatchStringWithFail() {
    expectFailureWhenTestingThat("abcaaadev").doesNotMatch(".*aaa.*");
    assertFailureValue("expected not to match", ".*aaa.*");
  }

  @Test
  public void stringDoesNotMatchStringFailNull() {
    expectFailureWhenTestingThat(null).doesNotMatch(".*aaa.*");
    assertFailureValue("expected a string that does not match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotMatchPattern() {
    assertThat("abcaqadev").doesNotMatch(Pattern.compile(".*aaa.*"));
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotMatchPatternWithFail() {
    expectFailureWhenTestingThat("abcaaadev").doesNotMatch(Pattern.compile(".*aaa.*"));
    assertFailureValue("expected not to match", ".*aaa.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotMatchPatternFailNull() {
    expectFailureWhenTestingThat(null).doesNotMatch(Pattern.compile(".*aaa.*"));
    assertFailureValue("expected a string that does not match", ".*aaa.*");
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

    expectFailureWhenTestingThat("aaa").containsMatch(".*b.*");
    assertFailureValue("expected to contain a match for", ".*b.*");
  }

  @Test
  public void stringContainsMatchStringFailNull() {
    expectFailureWhenTestingThat(null).containsMatch(".*b.*");
    assertFailureValue("expected a string that contains a match for", ".*b.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringContainsMatchPattern() {
    assertThat("aba").containsMatch(Pattern.compile(".*b.*"));

    expectFailureWhenTestingThat("aaa").containsMatch(Pattern.compile(".*b.*"));
    assertFailureValue("expected to contain a match for", ".*b.*");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringContainsMatchPatternFailNull() {
    expectFailureWhenTestingThat(null).containsMatch(Pattern.compile(".*b.*"));
    assertFailureValue("expected a string that contains a match for", ".*b.*");
  }

  @Test
  public void stringDoesNotContainMatchString() {
    assertThat("aaa").doesNotContainMatch(".*b.*");

    expectFailureWhenTestingThat("aba").doesNotContainMatch(".*b.*");
    assertFailureValue("expected not to contain a match for", ".*b.*");
  }

  @Test
  public void stringDoesNotContainMatchStringUsesFind() {
    expectFailureWhenTestingThat("aba").doesNotContainMatch("[b]");
    assertFailureValue("expected not to contain a match for", "[b]");
  }

  @Test
  public void stringDoesNotContainMatchStringUsesFindFailNull() {
    expectFailureWhenTestingThat(null).doesNotContainMatch("[b]");
    assertFailureValue("expected a string that does not contain a match for", "[b]");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotContainMatchPattern() {
    assertThat("zzaaazz").doesNotContainMatch(Pattern.compile(".b."));

    expectFailureWhenTestingThat("zzabazz").doesNotContainMatch(Pattern.compile(".b."));
    assertFailureValue("expected not to contain a match for", ".b.");
    assertFailureValue("but contained", "aba");
    assertFailureValue("full string", "zzabazz");
  }

  @Test
  @GwtIncompatible("Pattern")
  public void stringDoesNotContainMatchPatternFailNull() {
    expectFailureWhenTestingThat(null).doesNotContainMatch(Pattern.compile(".b."));
    assertFailureValue("expected a string that does not contain a match for", ".b.");
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
    expectFailureWhenTestingThat("abc").ignoringCase().isEqualTo("abd");

    assertFailureValue("expected", "abd");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringEqualityIgnoringCaseFailWithNullSubject() {
    expectFailureWhenTestingThat((String) null).ignoringCase().isEqualTo("abc");

    assertFailureValue("expected a string that is equal to", "abc");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringEqualityIgnoringCaseFailWithNullExpectedString() {
    expectFailureWhenTestingThat("abc").ignoringCase().isEqualTo(null);

    assertFailureValue("expected", "null (null reference)");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
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
    expectFailureWhenTestingThat("café").ignoringCase().isNotEqualTo("CAFÉ");

    assertFailureValue("expected not to be", "CAFÉ");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringInequalityIgnoringCaseFailWithNullSubject() {
    expectFailureWhenTestingThat((String) null).ignoringCase().isNotEqualTo(null);

    assertFailureValue("expected a string that is not equal to", "null (null reference)");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
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
    expectFailureWhenTestingThat("abc").ignoringCase().contains("d");

    assertFailureValue("expected to contain", "d");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringContainsIgnoringCaseFailBecauseTooLarge() {
    expectFailureWhenTestingThat("abc").ignoringCase().contains("abcc");

    assertFailureValue("expected to contain", "abcc");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringContainsIgnoringCaseFailBecauseNullSubject() {
    expectFailureWhenTestingThat((String) null).ignoringCase().contains("d");

    assertFailureValue("expected a string that contains", "d");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
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
    expectFailureWhenTestingThat("äbc").ignoringCase().doesNotContain("Äb");

    assertFailureValue("expected not to contain", "Äb");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringDoesNotContainIgnoringCaseFailWithEmptyString() {
    expectFailureWhenTestingThat("abc").ignoringCase().doesNotContain("");

    assertFailureValue("expected not to contain", "");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
  }

  @Test
  public void stringDoesNotContainIgnoringCaseFailBecauseNullSubject() {
    expectFailureWhenTestingThat((String) null).ignoringCase().doesNotContain("d");

    assertFailureValue("expected a string that does not contain", "d");
    assertThat(expectFailure.getFailure()).factKeys().contains("(case is ignored)");
  }

  @Test
  public void trailingWhitespaceInActual() {
    expectFailureWhenTestingThat("foo\n").isEqualTo("foo");
    assertFailureKeys("expected", "but contained extra trailing whitespace");
    assertFailureValue("but contained extra trailing whitespace", "\\n");
  }

  @Test
  public void trailingWhitespaceInExpected() {
    expectFailureWhenTestingThat("foo").isEqualTo("foo ");
    assertFailureKeys("expected", "but was missing trailing whitespace");
    assertFailureValue("but was missing trailing whitespace", "␣");
  }

  @Test
  public void trailingWhitespaceInBoth() {
    expectFailureWhenTestingThat("foo \n").isEqualTo("foo\u00a0");
    assertFailureKeys("expected", "with trailing whitespace", "but trailing whitespace was");
    assertFailureValue("with trailing whitespace", "\\u00a0");
    assertFailureValue("but trailing whitespace was", "␣\\n");
  }

  @Test
  public void trailingWhitespaceVsEmptyString() {
    /*
     * The code has special cases for both trailing whitespace and an empty string. Make sure that
     * it specifically reports the trailing whitespace. (It might be nice to *also* report the empty
     * string specially, but that's less important.)
     */
    expectFailureWhenTestingThat("\t").isEqualTo("");
    assertFailureKeys("expected", "but contained extra trailing whitespace");
    assertFailureValue("but contained extra trailing whitespace", "\\t");
  }

  private StringSubject expectFailureWhenTestingThat(String actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
