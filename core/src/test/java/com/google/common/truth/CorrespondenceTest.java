/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.truth.Correspondence.equality;
import static com.google.common.truth.Correspondence.tolerance;
import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.TestCorrespondences.INT_DIFF_FORMATTER;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Correspondence}. */
@RunWith(JUnit4.class)
public final class CorrespondenceTest {
  // Tests of the abstract base class (just assert that equals and hashCode throw).

  private static final Correspondence<Object, Object> INSTANCE =
      Correspondence.from((a, e) -> false, "has example property");

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void equals_throws() {
    assertThrows(UnsupportedOperationException.class, () -> INSTANCE.equals(new Object()));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void hashCode_throws() {
    assertThrows(UnsupportedOperationException.class, () -> INSTANCE.hashCode());
  }

  // Tests of the 'from' factory method.

  private static final Correspondence<String, String> STRING_PREFIX_EQUALITY =
      Correspondence.from(String::startsWith, "starts with");

  @Test
  public void from_compare() {
    assertThat(STRING_PREFIX_EQUALITY.compare("foot", "foo")).isTrue();
    assertThat(STRING_PREFIX_EQUALITY.compare("foot", "foot")).isTrue();
    assertThat(STRING_PREFIX_EQUALITY.compare("foo", "foot")).isFalse();
  }

  @Test
  public void from_formatDiff() {
    assertThat(STRING_PREFIX_EQUALITY.formatDiff("foo", "foot")).isNull();
  }

  @Test
  public void from_toString() {
    assertThat(STRING_PREFIX_EQUALITY.toString()).isEqualTo("starts with");
  }

  @Test
  public void from_isEquality() {
    assertThat(STRING_PREFIX_EQUALITY.isEquality()).isFalse();
  }

  @Test
  public void from_viaIterableSubjectContainsExactly_success() {
    assertThat(ImmutableList.of("foot", "barn"))
        .comparingElementsUsing(STRING_PREFIX_EQUALITY)
        .containsExactly("foo", "bar");
  }

  @Test
  public void from_viaIterableSubjectContainsExactly_failure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableList.of("foot", "barn", "gallon"))
                    .comparingElementsUsing(STRING_PREFIX_EQUALITY)
                    .containsExactly("foo", "bar"));
    assertFailureKeys(e, "unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue(e, "unexpected (1)", "gallon");
    assertFailureValue(e, "testing whether", "actual element starts with expected element");
  }

  @Test
  public void from_viaIterableSubjectContainsExactly_null() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(asList("foot", "barn", null))
                    .comparingElementsUsing(STRING_PREFIX_EQUALITY)
                    .containsExactly("foo", "bar"));
    assertFailureKeys(
        e,
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue(e, "unexpected (1)", "null");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(null, foo) threw java.lang.NullPointerException");
  }

  // Tests of the 'transform' factory methods.

  private static final Correspondence<@Nullable String, Integer> LENGTHS =
      Correspondence.transforming(s -> requireNonNull(s).length(), "has a length of");

  private static final Correspondence<@Nullable String, @Nullable Integer> HYPHEN_INDEXES =
      Correspondence.transforming(
          str -> {
            requireNonNull(str);
            int index = str.indexOf('-');
            return (index >= 0) ? index : null;
          },
          "has a hyphen at an index of");

  @Test
  public void transforming_actual_compare() {
    assertThat(LENGTHS.compare("foo", 3)).isTrue();
    assertThat(LENGTHS.compare("foot", 4)).isTrue();
    assertThat(LENGTHS.compare("foo", 4)).isFalse();
  }

  @Test
  public void transforming_actual_compare_nullTransformedValues() {
    assertThat(HYPHEN_INDEXES.compare("mailing-list", null)).isFalse();
    assertThat(HYPHEN_INDEXES.compare("forum", 7)).isFalse();
    assertThat(HYPHEN_INDEXES.compare("forum", null)).isTrue();
  }

  @Test
  public void transforming_actual_compare_nullActualValue() {
    assertThrows(NullPointerException.class, () -> HYPHEN_INDEXES.compare(null, 7));
  }

  @Test
  public void transforming_actual_formatDiff() {
    assertThat(LENGTHS.formatDiff("foo", 4)).isNull();
  }

  @Test
  public void transforming_actual_toString() {
    assertThat(LENGTHS.toString()).isEqualTo("has a length of");
  }

  @Test
  public void transforming_actual_isEquality() {
    assertThat(LENGTHS.isEquality()).isFalse();
  }

  @Test
  public void transforming_actual_viaIterableSubjectContainsExactly_success() {
    assertThat(ImmutableList.of("feet", "barns", "gallons"))
        .comparingElementsUsing(LENGTHS)
        .containsExactly(4, 5, 7)
        .inOrder();
  }

  @Test
  public void transforming_actual_viaIterableSubjectContainsExactly_failure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableList.of("feet", "barns", "gallons"))
                    .comparingElementsUsing(LENGTHS)
                    .containsExactly(4, 5));
    assertFailureKeys(e, "unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue(e, "unexpected (1)", "gallons");
    assertFailureValue(e, "testing whether", "actual element has a length of expected element");
  }

  @Test
  public void transforming_actual_viaIterableSubjectContainsExactly_nullActual() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(asList("feet", "barns", null))
                    .comparingElementsUsing(LENGTHS)
                    .containsExactly(4, 5));
    assertFailureKeys(
        e,
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue(e, "unexpected (1)", "null");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(null, 4) threw java.lang.NullPointerException");
  }

  @Test
  public void transforming_actual_viaIterableSubjectContainsExactly_nullTransformed() {
    // "mailing-list" and "chat-room" have hyphens at index 7 and 4 respectively.
    // "forum" contains no hyphen so the Function in HYPHEN_INDEXES transforms it to null.
    assertThat(ImmutableList.of("mailing-list", "chat-room", "forum"))
        .comparingElementsUsing(HYPHEN_INDEXES)
        .containsExactly(7, 4, null)
        .inOrder();
  }

  private static final Correspondence<@Nullable String, @Nullable String> HYPHENS_MATCH_COLONS =
      Correspondence.transforming(
          str -> {
            int index = requireNonNull(str).indexOf('-');
            return (index >= 0) ? index : null;
          },
          str -> {
            int index = requireNonNull(str).indexOf(':');
            return (index >= 0) ? index : null;
          },
          "has a hyphen at the same index as the colon in");

  @Test
  public void transforming_both_compare() {
    assertThat(HYPHENS_MATCH_COLONS.compare("mailing-list", "abcdefg:hij")).isTrue();
    assertThat(HYPHENS_MATCH_COLONS.compare("chat-room", "abcd:efghij")).isTrue();
    assertThat(HYPHENS_MATCH_COLONS.compare("chat-room", "abcdefg:hij")).isFalse();
  }

  @Test
  public void transforming_both_compare_nullTransformedValue() {
    assertThat(HYPHENS_MATCH_COLONS.compare("mailing-list", "abcdefg-hij")).isFalse();
    assertThat(HYPHENS_MATCH_COLONS.compare("forum", "abcde:fghij")).isFalse();
    assertThat(HYPHENS_MATCH_COLONS.compare("forum", "abcde-fghij")).isTrue();
  }

  @Test
  public void transforming_both_compare_nullInputValues() {
    assertThrows(
        NullPointerException.class, () -> HYPHENS_MATCH_COLONS.compare(null, "abcde:fghij"));
    assertThrows(
        NullPointerException.class, () -> HYPHENS_MATCH_COLONS.compare("mailing-list", null));
  }

  @Test
  public void transforming_both_formatDiff() {
    assertThat(HYPHENS_MATCH_COLONS.formatDiff("chat-room", "abcdefg:hij")).isNull();
  }

  @Test
  public void transforming_both_toString() {
    assertThat(HYPHENS_MATCH_COLONS.toString())
        .isEqualTo("has a hyphen at the same index as the colon in");
  }

  @Test
  public void transforming_both_isEquality() {
    assertThat(HYPHENS_MATCH_COLONS.isEquality()).isFalse();
  }

  @Test
  public void transforming_both_viaIterableSubjectContainsExactly_success() {
    assertThat(ImmutableList.of("mailing-list", "chat-room", "web-app"))
        .comparingElementsUsing(HYPHENS_MATCH_COLONS)
        .containsExactly("abcdefg:hij", "abcd:efghij", "abc:defghij")
        .inOrder();
  }

  @Test
  public void transforming_both_viaIterableSubjectContainsExactly_failure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableList.of("mailing-list", "chat-room", "web-app"))
                    .comparingElementsUsing(HYPHENS_MATCH_COLONS)
                    .containsExactly("abcdefg:hij", "abcd:efghij"));
    assertFailureKeys(e, "unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue(e, "unexpected (1)", "web-app");
    assertFailureValue(
        e,
        "testing whether",
        "actual element has a hyphen at the same index as the colon in expected element");
  }

  @Test
  public void transforming_both_viaIterableSubjectContainsExactly_nullActual() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(asList("mailing-list", "chat-room", null))
                    .comparingElementsUsing(HYPHENS_MATCH_COLONS)
                    .containsExactly("abcdefg:hij", "abcd:efghij"));
    assertFailureKeys(
        e,
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue(e, "unexpected (1)", "null");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(null, abcdefg:hij) threw java.lang.NullPointerException");
  }

  @Test
  public void transforming_both_viaIterableSubjectContainsExactly_nullExpected() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableList.of("mailing-list", "chat-room"))
                    .comparingElementsUsing(HYPHENS_MATCH_COLONS)
                    .containsExactly("abcdefg:hij", "abcd:efghij", null));
    assertFailureKeys(
        e,
        "missing (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue(e, "missing (1)", "null");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(mailing-list, null) threw java.lang.NullPointerException");
  }

  @Test
  public void transforming_both_viaIterableSubjectContainsExactly_nullTransformed() {
    // The actual element "forum" contains no hyphen, and the expected element "abcde-fghij"
    // contains no colon, so they both transform to null, and so they correspond.
    assertThat(ImmutableList.of("mailing-list", "chat-room", "forum"))
        .comparingElementsUsing(HYPHENS_MATCH_COLONS)
        .containsExactly("abcdefg:hij", "abcd:efghij", "abcde-fghij")
        .inOrder();
  }

  // Tests of the 'tolerance' factory method. Includes both direct tests of the compare method and
  // indirect tests using it in a basic call chain.

  @Test
  public void tolerance_compare_doubles() {
    assertThat(tolerance(0.0).compare(2.0, 2.0)).isTrue();
    assertThat(tolerance(0.00001).compare(2.0, 2.0)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0, 2.0)).isTrue();
    assertThat(tolerance(1.00001).compare(2.0, 3.0)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0, 1003.0)).isFalse();
    assertThat(tolerance(1000.0).compare(2.0, Double.POSITIVE_INFINITY)).isFalse();
    assertThat(tolerance(1000.0).compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
        .isFalse();
    assertThat(tolerance(1000.0).compare(2.0, Double.NaN)).isFalse();
    assertThat(tolerance(1000.0).compare(Double.NaN, Double.NaN)).isFalse();
    assertThat(tolerance(0.0).compare(-0.0, 0.0)).isTrue();
  }

  @Test
  public void tolerance_compare_floats() {
    assertThat(tolerance(0.0).compare(2.0f, 2.0f)).isTrue();
    assertThat(tolerance(0.00001).compare(2.0f, 2.0f)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0f, 2.0f)).isTrue();
    assertThat(tolerance(1.00001).compare(2.0f, 3.0f)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0f, 1003.0f)).isFalse();
    assertThat(tolerance(1000.0).compare(2.0f, Float.POSITIVE_INFINITY)).isFalse();
    assertThat(tolerance(1000.0).compare(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY))
        .isFalse();
    assertThat(tolerance(1000.0).compare(2.0f, Float.NaN)).isFalse();
    assertThat(tolerance(1000.0).compare(Float.NaN, Float.NaN)).isFalse();
    assertThat(tolerance(0.0).compare(-0.0f, 0.0f)).isTrue();
  }

  @Test
  public void tolerance_compare_doublesVsInts() {
    assertThat(tolerance(0.0).compare(2.0, 2)).isTrue();
    assertThat(tolerance(0.00001).compare(2.0, 2)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0, 2)).isTrue();
    assertThat(tolerance(1.00001).compare(2.0, 3)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0, 1003)).isFalse();
  }

  @Test
  public void tolerance_compare_negativeTolerance() {
    IllegalArgumentException expected =
        assertThrows(IllegalArgumentException.class, () -> tolerance(-0.05).compare(1.0, 2.0));
    assertThat(expected).hasMessageThat().isEqualTo("tolerance (-0.05) cannot be negative");
  }

  @Test
  /*
   * TODO: b/136037484 - Make tolerance() return false instead of throwing? Declare it with
   * @Nullable types even though it throws?
   */
  @J2ktIncompatible
  public void tolerance_compare_null() {
    assertThrows(NullPointerException.class, () -> tolerance(0.05).compare(1.0, null));
    assertThrows(NullPointerException.class, () -> tolerance(0.05).compare(null, 2.0));
  }

  @Test
  public void tolerance_formatDiff() {
    assertThat(tolerance(0.01).formatDiff(1.0, 2.0)).isNull();
  }

  @Test
  public void tolerance_toString() {
    assertThat(tolerance(0.01).toString()).isEqualTo("is a finite number within 0.01 of");
  }

  @Test
  public void tolerance_isEquality() {
    assertThat(tolerance(0.01).isEquality()).isFalse();
    // This is close to equality, but not close enough (it calls numbers of different types equal):
    assertThat(tolerance(0.0).isEquality()).isFalse();
  }

  @Test
  public void tolerance_viaIterableSubjectContains_success() {
    assertThat(ImmutableList.of(1.02, 2.04, 3.08))
        .comparingElementsUsing(tolerance(0.05))
        .contains(2.0);
  }

  @Test
  public void tolerance_viaIterableSubjectContains_failure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableList.of(1.02, 2.04, 3.08))
                    .comparingElementsUsing(tolerance(0.05))
                    .contains(3.01));
    assertFailureKeys(e, "expected to contain", "testing whether", "but was");
    assertFailureValue(e, "expected to contain", "3.01");
    assertFailureValue(
        e, "testing whether", "actual element is a finite number within 0.05 of expected element");
    assertFailureValue(e, "but was", "[1.02, 2.04, 3.08]");
  }

  // Tests of the 'equality' factory method. Includes both direct tests of the compare method and
  // indirect tests using it in a basic call chain.

  @Test
  public void equality_compare() {
    Correspondence<@Nullable Object, @Nullable Object> equality = equality();
    assertThat(equality.compare("foo", "foo")).isTrue();
    assertThat(equality.compare("foo", "bar")).isFalse();
    assertThat(equality.compare(123, 123)).isTrue();
    assertThat(equality.compare(123, 123L)).isFalse();
    assertThat(equality.compare(null, null)).isTrue();
    assertThat(equality.compare(null, "bar")).isFalse();
  }

  @Test
  public void equality_formatDiff() {
    assertThat(equality().formatDiff("foo", "bar")).isNull();
  }

  @Test
  public void equality_toString() {
    assertThat(equality().toString()).isEqualTo("is equal to"); // meta!
  }

  @Test
  public void equality_isEquality() {
    assertThat(equality().isEquality()).isTrue();
  }

  @Test
  public void equality_viaIterableSubjectContains_success() {
    assertThat(ImmutableList.of(1.0, 2.0, 3.0)).comparingElementsUsing(equality()).contains(2.0);
  }

  @Test
  public void equality_viaIterableSubjectContains_failure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableList.of(1.01, 2.02, 3.03))
                    .comparingElementsUsing(equality())
                    .contains(2.0));
    // N.B. No "testing whether" fact:
    assertFailureKeys(e, "expected to contain", "but was");
  }

  // Tests of formattingDiffsUsing.

  private static final Correspondence<String, Integer> LENGTHS_WITH_DIFF =
      Correspondence.transforming(String::length, "has a length of")
          .formattingDiffsUsing((a, e) -> Integer.toString(a.length() - e));

  @Test
  public void formattingDiffsUsing_compare() {
    // The compare behaviour should be the same as the wrapped correspondence.
    assertThat(LENGTHS_WITH_DIFF.compare("foo", 3)).isTrue();
    assertThat(LENGTHS_WITH_DIFF.compare("foot", 4)).isTrue();
    assertThat(LENGTHS_WITH_DIFF.compare("foo", 4)).isFalse();
  }

  @Test
  public void formattingDiffsUsing_formatDiff() {
    assertThat(LENGTHS_WITH_DIFF.formatDiff("foo", 4)).isEqualTo("-1");
    assertThat(LENGTHS_WITH_DIFF.formatDiff("foot", 3)).isEqualTo("1");
  }

  @Test
  public void formattingDiffsUsing_toString() {
    // The toString behaviour should be the same as the wrapped correspondence.
    assertThat(LENGTHS_WITH_DIFF.toString()).isEqualTo("has a length of");
  }

  @Test
  public void formattingDiffsUsing_isEquality() {
    // The isEquality behaviour should be the same as the wrapped correspondence.
    assertThat(LENGTHS_WITH_DIFF.isEquality()).isFalse();
    Correspondence<Integer, Integer> equalityWithDiffFormatter =
        Correspondence.<Integer>equality().formattingDiffsUsing(INT_DIFF_FORMATTER);
    assertThat(equalityWithDiffFormatter.isEquality()).isTrue();
  }

  @Test
  public void formattingDiffsUsing_viaIterableSubjectContainsExactly_failure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableList.of("feet", "gallons"))
                    .comparingElementsUsing(LENGTHS_WITH_DIFF)
                    .containsExactly(4, 5));
    assertFailureKeys(
        e,
        "missing (1)",
        "unexpected (1)",
        "#1",
        "diff",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue(e, "missing (1)", "5");
    assertFailureValue(e, "#1", "gallons");
    assertFailureValue(e, "diff", "2");
  }

  @Test
  public void formattingDiffsUsing_viaIterableSubjectContainsExactly_nullActual() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(asList("feet", null))
                    .comparingElementsUsing(LENGTHS_WITH_DIFF)
                    .containsExactly(4, 5));
    assertFailureKeys(
        e,
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertFailureValue(e, "missing (1)", "5");
    assertFailureValue(e, "unexpected (1)", "[null]");
    assertThat(e)
        .factValue("first exception", 0)
        .startsWith("compare(null, 4) threw java.lang.NullPointerException");
    assertThat(e)
        .factValue("first exception", 1)
        .startsWith("formatDiff(null, 5) threw java.lang.NullPointerException");
  }
}
