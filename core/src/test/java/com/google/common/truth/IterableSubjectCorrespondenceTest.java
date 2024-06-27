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

import static com.google.common.base.Functions.identity;
import static com.google.common.collect.Collections2.permutations;
import static com.google.common.truth.Correspondence.equality;
import static com.google.common.truth.Correspondence.tolerance;
import static com.google.common.truth.TestCorrespondences.CASE_INSENSITIVE_EQUALITY;
import static com.google.common.truth.TestCorrespondences.CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE;
import static com.google.common.truth.TestCorrespondences.NULL_SAFE_RECORD_ID;
import static com.google.common.truth.TestCorrespondences.PARSED_RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10;
import static com.google.common.truth.TestCorrespondences.PARSED_RECORD_ID;
import static com.google.common.truth.TestCorrespondences.RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10;
import static com.google.common.truth.TestCorrespondences.RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10_NO_DIFF;
import static com.google.common.truth.TestCorrespondences.RECORD_DIFF_FORMATTER;
import static com.google.common.truth.TestCorrespondences.RECORD_ID;
import static com.google.common.truth.TestCorrespondences.STRING_PARSES_TO_INTEGER_CORRESPONDENCE;
import static com.google.common.truth.TestCorrespondences.WITHIN_10_OF;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.TestCorrespondences.MyRecord;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link IterableSubject} APIs that use {@link Correspondence}.
 *
 * <p>Note: Most of the assertions here call {@code assertThat(someIterable)} to get an {@link
 * IterableSubject}, and then call {@code comparingElementsUsing(someCorrespondence)} on that to get
 * an {@link IterableSubject.UsingCorrespondence}. The test method names omit the {@code
 * comparingElementsUsing_} prefix for brevity.
 *
 * @author Pete Gillin
 */
@RunWith(JUnit4.class)
public class IterableSubjectCorrespondenceTest extends BaseSubjectTestCase {

  @Test
  // test of a mistaken call
  @SuppressWarnings({"EqualsIncompatibleType", "DoNotCall", "deprecation"})
  public void equalsThrowsUSOE() {
    try {
      boolean unused =
          assertThat(ImmutableList.of(42.0))
              .comparingElementsUsing(tolerance(10e-5))
              .equals(ImmutableList.of(0.0));
    } catch (UnsupportedOperationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "UsingCorrespondence.equals() is not supported. Did you mean to call"
                  + " containsExactlyElementsIn(expected) instead of equals(expected)?");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  @SuppressWarnings({"DoNotCall", "deprecation"}) // test of a mistaken call
  public void hashCodeThrowsUSOE() {
    try {
      int unused =
          assertThat(ImmutableList.of(42.0)).comparingElementsUsing(tolerance(10e-5)).hashCode();
    } catch (UnsupportedOperationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("UsingCorrespondence.hashCode() is not supported.");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void contains_success() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(456);
  }

  @Test
  public void contains_failure() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(2345);
    assertFailureKeys("expected to contain", "testing whether", "but was");
    assertFailureValue("expected to contain", "2345");
    assertFailureValue("testing whether", "actual element parses to expected element");
    assertFailureValue("but was", "[not a number, +123, +456, +789]");
  }

  @Test
  public void contains_handlesExceptions() {
    // CASE_INSENSITIVE_EQUALITY.compare throws on the null actual element.
    List<String> actual = asList("abc", null, "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .contains("DEF");
    // We fail with the more helpful failure message about the missing value, not the NPE.
    assertFailureKeys(
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, DEF) threw java.lang.NullPointerException");
  }

  @Test
  public void contains_handlesExceptions_alwaysFails() {
    List<String> actual = asList("abc", null, "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .contains("GHI");
    // The actual list does contain the required match. However, no reasonable implementation would
    // find that mapping without hitting the null along the way, and that throws NPE, so we are
    // contractually required to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing elements",
        "first exception",
        "expected to contain",
        "testing whether",
        "found match (but failing because of exception)",
        "full contents");
    assertFailureValue("found match (but failing because of exception)", "ghi");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, GHI) threw java.lang.NullPointerException");
  }

  @Test
  public void displayingDiffsPairedBy_1arg_contains() {
    MyRecord expected = MyRecord.create(2, 200);
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 211),
            MyRecord.create(4, 400),
            MyRecord.create(2, 189),
            MyRecord.createWithoutId(999));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .contains(expected);
    assertFailureKeys(
        "expected to contain",
        "testing whether",
        "but did not",
        "though it did contain elements with correct key (2)",
        "#1",
        "diff",
        "#2",
        "diff",
        "---",
        "full contents");
    assertFailureValue("#1", "2/211");
    assertFailureValueIndexed("diff", 0, "score:11");
    assertFailureValue("#2", "2/189");
    assertFailureValueIndexed("diff", 1, "score:-11");
  }

  @Test
  public void displayingDiffsPairedBy_1arg_contains_noDiff() {
    MyRecord expected = MyRecord.create(2, 200);
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 211),
            MyRecord.create(4, 400),
            MyRecord.create(2, 189),
            MyRecord.createWithoutId(999));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10_NO_DIFF)
        .displayingDiffsPairedBy(RECORD_ID)
        .contains(expected);
    assertFailureKeys(
        "expected to contain",
        "testing whether",
        "but did not",
        "though it did contain elements with correct key (2)",
        "---",
        "full contents");
    assertFailureValue("though it did contain elements with correct key (2)", "[2/211, 2/189]");
  }

  @Test
  public void displayingDiffsPairedBy_1arg_contains_handlesActualKeyerExceptions() {
    MyRecord expected = MyRecord.create(0, 999);
    List<MyRecord> actual = asList(MyRecord.create(1, 100), null, MyRecord.create(4, 400));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .contains(expected);
    assertFailureKeys(
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while keying elements for pairing",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("actualKeyFunction.apply(null) threw java.lang.NullPointerException");
  }

  @Test
  public void displayingDiffsPairedBy_1arg_contains_handlesExpectedKeyerExceptions() {
    List<MyRecord> actual =
        asList(MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.create(4, 400));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .contains(null);
    assertFailureKeys(
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while keying elements for pairing",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("expectedKeyFunction.apply(null) threw java.lang.NullPointerException");
  }

  @Test
  public void displayingDiffsPairedBy_1arg_contains_handlesFormatDiffExceptions() {
    MyRecord expected = MyRecord.create(0, 999);
    List<MyRecord> actual = asList(MyRecord.create(1, 100), null, MyRecord.create(4, 400));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(NULL_SAFE_RECORD_ID)
        .contains(expected);
    assertFailureKeys(
        "expected to contain",
        "testing whether",
        "but did not",
        "though it did contain elements with correct key (1)",
        "---",
        "full contents",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("formatDiff(null, 0/999) threw java.lang.NullPointerException");
  }

  @Test
  public void contains_null() {
    List<String> actual = Arrays.asList("+123", null, "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(null);
  }

  @Test
  public void wrongTypeInActual() {
    ImmutableList<?> actual = ImmutableList.of("valid", 123);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(456);
    assertFailureKeys(
        "expected to contain",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(123, 456) threw java.lang.ClassCastException");
  }

  @Test
  public void doesNotContain_success() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContain(2345);
  }

  @Test
  public void doesNotContains_failure() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContain(456);
    assertFailureKeys(
        "expected not to contain", "testing whether", "but contained", "full contents");
    assertFailureValue("expected not to contain", "456");
    assertFailureValue("but contained", "[+456]");
  }

  @Test
  public void doesNotContain_handlesExceptions() {
    // CASE_INSENSITIVE_EQUALITY.compare throws on the null actual element.
    List<String> actual = asList("abc", null, "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .doesNotContain("GHI");
    // We fail with the more helpful failure message about the unexpected value, not the NPE.
    assertFailureKeys(
        "expected not to contain",
        "testing whether",
        "but contained",
        "full contents",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, GHI) threw java.lang.NullPointerException");
  }

  @Test
  public void doesNotContain_handlesExceptions_alwaysFails() {
    List<String> actual = asList("abc", null, "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .doesNotContain("DEF");
    // The actual list does not contain the forbidden match. However, we cannot establish that
    // without hitting the null along the way, and that throws NPE, so we are contractually required
    // to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing elements",
        "first exception",
        "expected not to contain",
        "testing whether",
        "found no match (but failing because of exception)",
        "full contents");
    assertFailureValue("expected not to contain", "DEF");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, DEF) threw java.lang.NullPointerException");
    assertFailureValue("expected not to contain", "DEF");
  }

  @Test
  public void containsExactlyElementsIn_inOrder_success() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected)
        .inOrder();
  }

  @Test
  public void containsExactlyElementsIn_successOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
  }

  @Test
  public void containsExactlyElementsIn_outOfOrderDoesNotStringify() {
    CountsToStringCalls o = new CountsToStringCalls();
    List<Object> actual = asList(o, 1);
    List<Object> expected = asList(1, o);
    assertThat(actual).comparingElementsUsing(equality()).containsExactlyElementsIn(expected);
    assertThat(o.calls).isEqualTo(0);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(equality())
        .containsExactlyElementsIn(expected)
        .inOrder();
    assertThat(o.calls).isGreaterThan(0);
  }

  @Test
  public void containsExactlyElementsIn_successNonGreedy() {
    // (We use doubles with approximate equality for this test, because we can't illustrate this
    // case with the string parsing correspondence used in the other tests, because one string
    // won't parse to more than one integer.)
    ImmutableList<Double> expected = ImmutableList.of(1.0, 1.1, 1.2);
    ImmutableList<Double> actual = ImmutableList.of(1.05, 1.15, 0.95);
    // The comparingElementsUsing test with a tolerance of 0.1 should succeed by pairing 1.0 with
    // 0.95, 1.1 with 1.05, and 1.2 with 1.15. A left-to-right greedy implementation would fail as
    // it would pair 1.0 with 1.05 and 1.1 with 1.15, and fail to pair 1.2 with 0.95. Check that the
    // implementation is truly non-greedy by testing all permutations.
    for (List<Double> permutedActual : permutations(actual)) {
      assertThat(permutedActual)
          .comparingElementsUsing(tolerance(0.1))
          .containsExactlyElementsIn(expected);
    }
  }

  @Test
  public void containsExactlyElementsIn_failsMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "0x40", "0x80");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys("missing (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (1)", "256");
    assertFailureValue("expected", "[64, 128, 256, 128]");
    assertFailureValue("testing whether", "actual element parses to expected element");
    assertFailureValue("but was", "[+64, +128, 0x40, 0x80]");
  }

  @Test
  public void containsExactlyElementsIn_inOrder_passesWhenBothEmpty() {
    ImmutableList<Integer> expected = ImmutableList.of();
    ImmutableList<String> actual = ImmutableList.of();
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected)
        .inOrder();
  }

  @Test
  public void containsExactlyElementsIn_failsExpectedIsEmpty() {
    ImmutableList<Integer> expected = ImmutableList.of();
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "0x40", "0x80");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void containsExactlyElementsIn_failsMultipleMissingCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+64", "0x40", "0x40");
    // Actual list has candidate matches for 64 only, and is missing 128, 256, and the other 128.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys("missing (3)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (3)", "128 [2 copies], 256");
  }

  @Test
  public void containsExactlyElementsIn_failsOrderedMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 512);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256");
    // Actual list has in-order candidate matches for 64, 128, and 256, but is missing 512.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys("missing (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (1)", "512");
  }

  @Test
  public void containsExactlyElementsIn_failsExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "cheese");
    // Actual list has candidate matches for all the expected, but has extra cheese.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys("unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("unexpected (1)", "cheese");
  }

  @Test
  public void containsExactlyElementsIn_failsOrderedExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80", "cheese");
    // Actual list has in-order candidate matches for all the expected, but has extra cheese.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys("unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("unexpected (1)", "cheese");
  }

  @Test
  public void containsExactlyElementsIn_failsMissingAndExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "jalapenos", "cheese");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256 and has
    // extra jalapenos and cheese.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "missing (1)", "unexpected (2)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (1)", "256");
    assertFailureValue("unexpected (2)", "[jalapenos, cheese]");
  }

  @Test
  public void containsExactlyElementsIn_failsMissingAndExtraNull() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    List<String> actual = asList("+64", "+128", "0x80", null);
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256 and has
    // extra null. (N.B. This tests a previous regression from calling extra.toString().)
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "missing (1)", "unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (1)", "256");
    assertFailureValue("unexpected (1)", "[null]");
  }

  @Test
  public void containsExactlyElementsIn_failsNullMissingAndExtra() {
    List<Integer> expected = asList(64, 128, null, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "0x80", "cheese");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing null and has
    // extra cheese.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "missing (1)", "unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (1)", "null");
    assertFailureValue("unexpected (1)", "[cheese]");
  }

  @Test
  public void containsExactlyElementsIn_handlesExceptions() {
    ImmutableList<String> expected = ImmutableList.of("ABC", "DEF", "GHI", "JKL");
    // CASE_INSENSITIVE_EQUALITY.compare throws on the null actual element.
    List<String> actual = asList(null, "xyz", "abc", "def");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .containsExactlyElementsIn(expected);
    // We fail with the more helpful failure message about the mis-matched values, not the NPE.
    assertFailureKeys(
        "missing (2)",
        "unexpected (2)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("missing (2)", "GHI, JKL");
    assertFailureValue("unexpected (2)", "null, xyz");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ABC) threw java.lang.NullPointerException");
  }

  @Test
  public void containsExactlyElementsIn_handlesExceptions_alwaysFails() {
    List<String> expected = asList("ABC", "DEF", "GHI", null);
    List<String> actual = asList(null, "def", "ghi", "abc");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE)
        .containsExactlyElementsIn(expected);
    // CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE.compare(null, null) returns true, so there is a
    // mapping between actual and expected elements where they all correspond. However, no
    // reasonable implementation would find that mapping without hitting the (null, "ABC") case
    // along the way, and that throws NPE, so we are contractually required to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing elements",
        "first exception",
        "expected",
        "testing whether",
        "found all expected elements (but failing because of exception)",
        "full contents");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ABC) threw java.lang.NullPointerException");
  }

  @Test
  public void containsExactlyElementsIn_diffOneMissingSomeExtraCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(30, 60, 90);
    ImmutableList<Integer> actual = ImmutableList.of(101, 65, 35, 190);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(WITHIN_10_OF)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "missing (1)",
        "unexpected (2)",
        "#1",
        "diff",
        "#2",
        "diff",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "90");
    assertFailureValue("#1", "101");
    assertFailureValueIndexed("diff", 0, "11");
    assertFailureValue("#2", "190");
    assertFailureValueIndexed("diff", 1, "100");
  }

  @Test
  public void displayingDiffsPairedBy_1arg_containsExactlyElementsIn() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(3, 300),
            MyRecord.createWithoutId(900));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 211),
            MyRecord.create(4, 400),
            MyRecord.createWithoutId(999));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "for key",
        "missing",
        "unexpected (1)",
        "#1",
        "diff",
        "---",
        "elements without matching keys:",
        "missing (2)",
        "unexpected (2)",
        "---",
        "expected",
        "testing whether",
        "but was");
    // the key=2 values:
    assertFailureValue("for key", "2");
    assertFailureValue("missing", "2/200");
    assertFailureValue("#1", "2/211");
    assertFailureValue("diff", "score:11");
    // the values without matching keys:
    assertFailureValue("missing (2)", "3/300, none/900");
    assertFailureValue("unexpected (2)", "4/400, none/999");
  }

  @Test
  public void displayingDiffsPairedBy_2arg_containsExactlyElementsIn() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(3, 300),
            MyRecord.createWithoutId(900));
    ImmutableList<String> actual = ImmutableList.of("1/100", "2/211", "4/400", "none/999");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(PARSED_RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(PARSED_RECORD_ID, RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "for key",
        "missing",
        "unexpected (1)",
        "#1",
        "diff",
        "---",
        "elements without matching keys:",
        "missing (2)",
        "unexpected (2)",
        "---",
        "expected",
        "testing whether",
        "but was");
    // the key=2 values:
    assertFailureValue("for key", "2");
    assertFailureValue("missing", "2/200");
    assertFailureValue("#1", "2/211");
    assertFailureValue("diff", "score:11");
    // the values without matching keys:
    assertFailureValue("missing (2)", "3/300, none/900");
    assertFailureValue("unexpected (2)", "4/400, none/999");
  }

  @Test
  public void displayingDiffsPairedBy_containsExactlyElementsIn_onlyKeyed() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(3, 300),
            MyRecord.createWithoutId(999));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 211),
            MyRecord.create(3, 303),
            MyRecord.createWithoutId(999));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "for key",
        "missing",
        "unexpected (1)",
        "#1",
        "diff",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("for key", "2");
    assertFailureValue("missing", "2/200");
    assertFailureValue("#1", "2/211");
    assertFailureValue("diff", "score:11");
  }

  @Test
  public void displayingDiffsPairedBy_containsExactlyElementsIn_noKeyed() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(3, 300),
            MyRecord.createWithoutId(900));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 201),
            MyRecord.create(4, 400),
            MyRecord.createWithoutId(999));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "elements without matching keys:",
        "missing (2)",
        "unexpected (2)",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (2)", "3/300, none/900");
    assertFailureValue("unexpected (2)", "4/400, none/999");
  }

  @Test
  public void displayingDiffsPairedBy_containsExactlyElementsIn_noDiffs() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(3, 300),
            MyRecord.createWithoutId(999));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 211),
            MyRecord.create(3, 303),
            MyRecord.createWithoutId(999));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10_NO_DIFF)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "for key", "missing", "unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing", "2/200");
    assertFailureValue("unexpected (1)", "[2/211]");
  }

  @Test
  public void displayingDiffsPairedBy_containsExactlyElementsIn_passing() {
    // The contract on displayingDiffsPairedBy requires that it should not affect whether the test
    // passes or fails. This test asserts that a test which would pass on the basis of its
    // correspondence still passes even if the user specifies a key function such that none of the
    // elements match by key. (We advise against assertions where key function equality is stricter
    // than correspondence, but we should still do the thing we promised we'd do in that case.)
    ImmutableList<Double> expected = ImmutableList.of(1.0, 1.1, 1.2);
    ImmutableList<Double> actual = ImmutableList.of(1.05, 1.15, 0.95);
    assertThat(actual)
        .comparingElementsUsing(tolerance(0.1))
        .displayingDiffsPairedBy(identity())
        .containsExactlyElementsIn(expected);
  }

  @Test
  public void displayingDiffsPairedBy_containsExactlyElementsIn_notUnique() {
    // The missing elements here are not uniquely keyed by the key function, so the key function
    // should be ignored, but a warning about this should be appended to the failure message.
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(3, 300),
            MyRecord.create(3, 301),
            MyRecord.createWithoutId(900));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 211),
            MyRecord.create(4, 400),
            MyRecord.createWithoutId(999));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "missing (4)",
        "unexpected (3)",
        "---",
        "a key function which does not uniquely key the expected elements was provided and has"
            + " consequently been ignored",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (4)", "2/200, 3/300, 3/301, none/900");
    assertFailureValue("unexpected (3)", "2/211, 4/400, none/999");
  }

  @Test
  public void displayingDiffsPairedBy_containsExactlyElementsIn_handlesActualKeyerExceptions() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.create(4, 400));
    List<MyRecord> actual = asList(MyRecord.create(1, 101), MyRecord.create(2, 211), null);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "for key",
        "missing",
        "unexpected (1)",
        "#1",
        "diff",
        "---",
        "elements without matching keys:",
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while keying elements for pairing",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("actualKeyFunction.apply(null) threw java.lang.NullPointerException");
  }

  @Test
  public void displayingDiffsPairedBy_containsExactlyElementsIn_handlesExpectedKeyerExceptions() {
    List<MyRecord> expected = asList(MyRecord.create(1, 100), MyRecord.create(2, 200), null);
    List<MyRecord> actual =
        asList(MyRecord.create(1, 101), MyRecord.create(2, 211), MyRecord.create(4, 400));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "for key",
        "missing",
        "unexpected (1)",
        "#1",
        "diff",
        "---",
        "elements without matching keys:",
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while keying elements for pairing",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("expectedKeyFunction.apply(null) threw java.lang.NullPointerException");
  }

  @Test
  public void displayingDiffsPairedBy_containsExactlyElementsIn_handlesFormatDiffExceptions() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.create(0, 999));
    List<MyRecord> actual = asList(MyRecord.create(1, 101), MyRecord.create(2, 211), null);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(NULL_SAFE_RECORD_ID)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "for key",
        "missing",
        "unexpected (1)",
        "#1",
        "diff",
        "---",
        "for key",
        "missing",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("formatDiff(null, 0/999) threw java.lang.NullPointerException");
  }

  @Test
  public void containsExactlyElementsIn_failsMissingElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and the actual elements,"
            + " each actual element matches as least one expected element, and vice versa, but"
            + " there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "128");
  }

  @Test
  public void containsExactlyElementsIn_failsExtraElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x80", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and the actual elements,"
            + " each actual element matches as least one expected element, and vice versa, but"
            + " there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertThatFailure().factValue("unexpected (1)").isAnyOf("+64", "0x40");
  }

  @Test
  public void containsExactlyElementsIn_failsMissingAndExtraInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and the actual elements,"
            + " each actual element matches as least one expected element, and vice versa, but"
            + " there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "128");
    assertThatFailure().factValue("unexpected (1)").isAnyOf("[+64]", "[0x40]");
  }

  @Test
  public void containsExactlyElementsIn_diffOneMissingAndExtraInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(30, 30, 60);
    ImmutableList<Integer> actual = ImmutableList.of(25, 55, 65);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(WITHIN_10_OF)
        .containsExactlyElementsIn(expected);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and the actual elements,"
            + " each actual element matches as least one expected element, and vice versa, but"
            + " there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "unexpected (1)",
        "#1",
        "diff",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "30");
    assertThatFailure().factValue("#1").isAnyOf("55", "65");
    assertThatFailure().factValue("diff").isAnyOf("25", "35");
  }

  @Test
  public void containsExactlyElementsIn_inOrder_failsOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected)
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong", "expected", "testing whether", "but was");
    assertFailureValue("expected", "[64, 128, 256, 128]");
  }

  @Test
  public void containsExactlyElementsIn_null() {
    List<Integer> expected = Arrays.asList(128, null);
    List<String> actual = Arrays.asList(null, "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
  }

  @Test
  public void containsExactlyElementsIn_array() {
    Integer[] expected = new Integer[] {64, 128, 256, 128};
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);

    actual = ImmutableList.of("+64", "+128", "0x40", "0x80");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertFailureKeys("missing (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (1)", "256");
  }

  @Test
  public void containsExactly_inOrder_success() {
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(64, 128, 256, 128)
        .inOrder();
  }

  @Test
  public void containsExactly_successOutOfOrder() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(64, 128, 256, 128);
  }

  @Test
  public void containsExactly_failsMissingAndExtraInOneToOne() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(64, 128, 256, 128);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and the actual elements,"
            + " each actual element matches as least one expected element, and vice versa, but"
            + " there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "128");
    assertThatFailure().factValue("unexpected (1)").isAnyOf("[+64]", "[0x40]");
  }

  @Test
  public void containsExactly_nullValueInArray() {
    List<String> actual = Arrays.asList(null, "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(128, null);
  }

  @Test
  public void containsExactly_nullArray() {
    // Truth is tolerant of this erroneous varargs call.
    List<String> actual = Arrays.asList((String) null);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly((Integer[]) null)
        .inOrder();
  }

  @Test
  public void containsAtLeastElementsIn() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "+256", "0x80", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected)
        .inOrder();
  }

  @Test
  public void containsAtLeastElementsIn_inOrder_success() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "+256", "0x80", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected)
        .inOrder();
  }

  @Test
  public void containsAtLeastElementsIn_successOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected);
  }

  @Test
  public void containsAtLeastElementsIn_outOfOrderDoesNotStringify() {
    CountsToStringCalls o = new CountsToStringCalls();
    List<Object> actual = asList(o, 1);
    List<Object> expected = asList(1, o);
    assertThat(actual).comparingElementsUsing(equality()).containsAtLeastElementsIn(expected);
    assertThat(o.calls).isEqualTo(0);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(equality())
        .containsAtLeastElementsIn(expected)
        .inOrder();
    assertThat(o.calls).isGreaterThan(0);
  }

  @Test
  public void containsAtLeastElementsIn_successNonGreedy() {
    // (We use doubles with approximate equality for this test, because we can't illustrate this
    // case with the string parsing correspondence used in the other tests, because one string
    // won't parse to more than one integer.)
    ImmutableList<Double> expected = ImmutableList.of(1.0, 1.1, 1.2);
    ImmutableList<Double> actual = ImmutableList.of(99.999, 1.05, 99.999, 1.15, 0.95, 99.999);
    // The comparingElementsUsing test with a tolerance of 0.1 should succeed by pairing 1.0 with
    // 0.95, 1.1 with 1.05, and 1.2 with 1.15. A left-to-right greedy implementation would fail as
    // it would pair 1.0 with 1.05 and 1.1 with 1.15, and fail to pair 1.2 with 0.95. Check that the
    // implementation is truly non-greedy by testing all permutations.
    for (List<Double> permutedActual : permutations(actual)) {
      assertThat(permutedActual)
          .comparingElementsUsing(tolerance(0.1))
          .containsAtLeastElementsIn(expected);
    }
  }

  @Test
  public void containsAtLeastElementsIn_failsMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "0x40", "0x80", "fum");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected);
    assertFailureKeys(
        "missing (1)", "---", "expected to contain at least", "testing whether", "but was");
    assertFailureValue("missing (1)", "256");
    assertFailureValue("expected to contain at least", "[64, 128, 256, 128]");
    assertFailureValue("testing whether", "actual element parses to expected element");
    assertFailureValue("but was", "[fee, +64, +128, fi, fo, 0x40, 0x80, fum]");
  }

  @Test
  public void containsAtLeastElementsIn_handlesExceptions() {
    ImmutableList<String> expected = ImmutableList.of("ABC", "DEF", "GHI");
    // CASE_INSENSITIVE_EQUALITY.compare throws on the null actual element.
    List<String> actual = asList(null, "xyz", "abc", "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .containsAtLeastElementsIn(expected);
    // We fail with the more helpful failure message about the mis-matched values, not the NPE.
    assertFailureKeys(
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ABC) threw java.lang.NullPointerException");
  }

  @Test
  public void containsAtLeastElementsIn_handlesExceptions_alwaysFails() {
    List<String> expected = asList("ABC", "DEF", null);
    List<String> actual = asList(null, "def", "ghi", "abc");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE)
        .containsAtLeastElementsIn(expected);
    // CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE.compare(null, null) returns true, so there is a
    // mapping between actual and expected elements which includes all the expected. However, no
    // reasonable implementation would find that mapping without hitting the (null, "ABC") case
    // along the way, and that throws NPE, so we are contractually required to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing elements",
        "first exception",
        "expected to contain at least",
        "testing whether",
        "found all expected elements (but failing because of exception)",
        "full contents");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ABC) threw java.lang.NullPointerException");
  }

  @Test
  public void displayingElementsPairedBy_containsAtLeastElementsIn() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.createWithoutId(999));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 101),
            MyRecord.create(2, 211),
            MyRecord.create(2, 222),
            MyRecord.create(3, 303),
            MyRecord.createWithoutId(888));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsAtLeastElementsIn(expected);
    assertFailureKeys(
        "for key",
        "missing",
        "did contain elements with that key (2)",
        "#1",
        "diff",
        "#2",
        "diff",
        "---",
        "elements without matching keys:",
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    // values at key 2:
    assertFailureValue("for key", "2");
    assertFailureValue("missing", "2/200");
    assertFailureValue("#1", "2/211");
    assertFailureValueIndexed("diff", 0, "score:11");
    assertFailureValue("#2", "2/222");
    assertFailureValueIndexed("diff", 1, "score:22");
    // values without matching keys:
    assertFailureValue("missing (1)", "none/999");
  }

  @Test
  public void displayingElementsPairedBy_containsAtLeastElementsIn_notUnique() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(2, 201),
            MyRecord.createWithoutId(999));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(1, 101), MyRecord.create(3, 303), MyRecord.createWithoutId(999));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsAtLeastElementsIn(expected);
    assertFailureKeys(
        "missing (2)",
        "---",
        "a key function which does not uniquely key the expected elements was provided and has"
            + " consequently been ignored",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("missing (2)", "2/200, 2/201");
  }

  @Test
  public void displayingElementsPairedBy_containsAtLeastElementsIn_handlesFormatDiffExceptions() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.create(0, 999));
    List<MyRecord> actual =
        asList(MyRecord.create(1, 101), MyRecord.create(2, 211), MyRecord.create(3, 303), null);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(NULL_SAFE_RECORD_ID)
        .containsAtLeastElementsIn(expected);
    assertFailureKeys(
        "for key",
        "missing",
        "did contain elements with that key (1)",
        "#1",
        "diff",
        "---",
        "for key",
        "missing",
        "did contain elements with that key (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertFailureValueIndexed("for key", 0, "2");
    assertFailureValueIndexed("missing", 0, "2/200");
    assertFailureValue("#1", "2/211");
    assertFailureValue("diff", "score:11");
    assertFailureValueIndexed("for key", 1, "0");
    assertFailureValueIndexed("missing", 1, "0/999");
    assertFailureValueIndexed("did contain elements with that key (1)", 1, "[null]");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("formatDiff(null, 0/999) threw java.lang.NullPointerException");
  }

  @Test
  public void containsAtLeastElementsIn_failsMultipleMissingCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+64", "fi", "fo", "0x40", "0x40", "fum");
    // Actual list has candidate matches for 64 only, and is missing 128, 256, and the other 128.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected);
    assertFailureKeys(
        "missing (3)", "---", "expected to contain at least", "testing whether", "but was");
    assertFailureValue("missing (3)", "128 [2 copies], 256");
  }

  @Test
  public void containsAtLeastElementsIn_failsOrderedMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 512);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "fi", "fo", "+128", "+256", "fum");
    // Actual list has in-order candidate matches for 64, 128, and 256, but is missing 512.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected);
    assertFailureKeys(
        "missing (1)", "---", "expected to contain at least", "testing whether", "but was");
    assertFailureValue("missing (1)", "512");
  }

  @Test
  public void containsAtLeastElementsIn_failsMissingElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "fi", "fo", "+64", "+256", "fum");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and a subset of the actual"
            + " elements, each actual element matches as least one expected element, and vice"
            + " versa, but there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "128");
  }

  @Test
  public void containsAtLeastElementsIn_inOrder_failsOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected)
        .inOrder();
    assertFailureKeys(
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "testing whether",
        "but was");
    assertFailureValue("expected order for required elements", "[64, 128, 256, 128]");
  }

  @Test
  public void containsAtLeastElementsIn_null() {
    List<Integer> expected = Arrays.asList(128, null);
    List<String> actual = Arrays.asList(null, "fee", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected);
  }

  @Test
  public void containsAtLeastElementsIn_array() {
    Integer[] expected = new Integer[] {64, 128, 256, 128};
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected);

    actual = ImmutableList.of("fee", "+64", "+128", "fi", "fo", "0x40", "0x80", "fum");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastElementsIn(expected);
    assertFailureKeys(
        "missing (1)", "---", "expected to contain at least", "testing whether", "but was");
    assertFailureValue("missing (1)", "256");
  }

  @Test
  public void containsAtLeast() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "+256", "0x80", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast(64, 128, 256, 128)
        .inOrder();
  }

  @Test
  public void containsAtLeast_inOrder_success() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "+256", "0x80", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast(64, 128, 256, 128)
        .inOrder();
  }

  @Test
  public void containsAtLeast_successOutOfOrder() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast(64, 128, 256, 128);
  }

  @Test
  public void containsAtLeast_failsMissingElementInOneToOne() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "fi", "fo", "+64", "+256", "fum");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast(64, 128, 256, 128);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and a subset of the actual"
            + " elements, each actual element matches as least one expected element, and vice"
            + " versa, but there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "128");
  }

  @Test
  public void containsAtLeast_nullValueInArray() {
    List<String> actual = Arrays.asList(null, "fee", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast(128, null);
  }

  @Test
  public void containsAnyOf_success() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyOf(255, 256, 257);
  }

  @Test
  public void containsAnyOf_failure() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyOf(255, 256, 257);
    assertFailureKeys("expected to contain any of", "testing whether", "but was");
    assertFailureValue("expected to contain any of", "[255, 256, 257]");
    assertFailureValue("testing whether", "actual element parses to expected element");
    assertFailureValue("but was", "[+128, +64, This is not the string you're looking for, 0x40]");
  }

  @Test
  public void containsAnyOf_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyOf(255, null, 257);
  }

  @Test
  public void containsAnyOf_handlesExceptions() {
    // CASE_INSENSITIVE_EQUALITY.compare throws on the null actual element.
    List<String> actual = asList("abc", null, "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .containsAnyOf("DEF", "FED");
    // We fail with the more helpful failure message about missing the expected values, not the NPE.
    assertFailureKeys(
        "expected to contain any of",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, DEF) threw java.lang.NullPointerException");
  }

  @Test
  public void containsAnyOf_handlesExceptions_alwaysFails() {
    List<String> actual = asList("abc", null, "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .containsAnyOf("GHI", "XYZ");
    // The actual list does contain the required match. However, no reasonable implementation would
    // find that mapping without hitting the null along the way, and that throws NPE, so we are
    // contractually required to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing elements",
        "first exception",
        "expected to contain any of",
        "testing whether",
        "found match (but failing because of exception)",
        "full contents");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, GHI) threw java.lang.NullPointerException");
  }

  @Test
  public void containsAnyIn_success() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    ImmutableList<Integer> expected = ImmutableList.of(255, 256, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
  }

  @Test
  public void containsAnyIn_failure() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    ImmutableList<Integer> expected = ImmutableList.of(255, 256, 257);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
    assertFailureKeys("expected to contain any of", "testing whether", "but was");
    assertFailureValue("expected to contain any of", "[255, 256, 257]");
    assertFailureValue("testing whether", "actual element parses to expected element");
    assertFailureValue("but was", "[+128, +64, This is not the string you're looking for, 0x40]");
  }

  @Test
  public void displayingDiffsPairedBy_containsAnyIn_withKeyMatches() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(3, 300),
            MyRecord.createWithoutId(999));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(3, 311),
            MyRecord.create(2, 211),
            MyRecord.create(2, 222),
            MyRecord.create(4, 404),
            MyRecord.createWithoutId(888));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsAnyIn(expected);
    assertFailureKeys(
        "expected to contain any of",
        "testing whether",
        "but was",
        "for key",
        "expected any of",
        "but got (2)",
        "#1",
        "diff",
        "#2",
        "diff",
        "---",
        "for key",
        "expected any of",
        "but got (1)",
        "#1",
        "diff",
        "---");
    // at key 2:
    assertFailureValueIndexed("for key", 0, "2");
    assertFailureValueIndexed("expected any of", 0, "2/200");
    assertFailureValueIndexed("#1", 0, "2/211");
    assertFailureValueIndexed("diff", 0, "score:11");
    assertFailureValue("#2", "2/222");
    assertFailureValueIndexed("diff", 1, "score:22");
    // at key 3:
    assertFailureValueIndexed("for key", 1, "3");
    assertFailureValueIndexed("expected any of", 1, "3/300");
    assertFailureValueIndexed("#1", 1, "3/311");
    assertFailureValueIndexed("diff", 2, "score:11");
  }

  @Test
  public void displayingDiffsPairedBy_containsAnyIn_withoutKeyMatches() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.createWithoutId(999));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(3, 300), MyRecord.create(4, 411), MyRecord.createWithoutId(888));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsAnyIn(expected);
    assertFailureKeys(
        "expected to contain any of",
        "testing whether",
        "but was",
        "it does not contain any matches by key, either");
  }

  @Test
  public void displayingDiffsPairedBy_containsAnyIn_notUnique() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(
            MyRecord.create(1, 100),
            MyRecord.create(2, 200),
            MyRecord.create(2, 250),
            MyRecord.createWithoutId(999));
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(3, 300), MyRecord.create(2, 211), MyRecord.createWithoutId(888));
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsAnyIn(expected);
    assertFailureKeys(
        "expected to contain any of",
        "testing whether",
        "but was",
        "a key function which does not uniquely key the expected elements was provided and has"
            + " consequently been ignored");
  }

  @Test
  public void displayingDiffsPairedBy_containsAnyIn_handlesFormatDiffExceptions() {
    ImmutableList<MyRecord> expected =
        ImmutableList.of(MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.create(0, 999));
    List<MyRecord> actual = asList(MyRecord.create(3, 311), MyRecord.create(4, 404), null);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
        .displayingDiffsPairedBy(NULL_SAFE_RECORD_ID)
        .containsAnyIn(expected);
    assertFailureKeys(
        "expected to contain any of",
        "testing whether",
        "but was",
        "for key",
        "expected any of",
        "but got (1)",
        "---",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("formatDiff(null, 0/999) threw java.lang.NullPointerException");
  }

  @Test
  public void containsAnyIn_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    List<Integer> expected = asList(255, null, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
  }

  @Test
  public void containsAnyIn_array() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    Integer[] expected = new Integer[] {255, 256, 257};
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);

    expected = new Integer[] {511, 512, 513};
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
    assertFailureKeys("expected to contain any of", "testing whether", "but was");
  }

  @Test
  public void containsNoneOf_success() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(255, 256, 257);
  }

  @Test
  public void containsNoneOf_failure() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(255, 256, 257);
    assertFailureKeys(
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "full contents");
    assertFailureValue("expected not to contain any of", "[255, 256, 257]");
    assertFailureValue("testing whether", "actual element parses to expected element");
    assertFailureValue("but contained", "[+256]");
    assertFailureValue("corresponding to", "256");
    assertFailureValue("full contents", "[+128, +64, +256, 0x40]");
  }

  @Test
  public void containsNoneOf_multipleFailures() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(64, 128);
    assertFailureKeys(
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "but contained",
        "corresponding to",
        "---",
        "full contents");
    assertFailureValueIndexed("but contained", 0, "[+64, 0x40]");
    assertFailureValueIndexed("corresponding to", 0, "64");
    assertFailureValueIndexed("but contained", 1, "[+128]");
    assertFailureValueIndexed("corresponding to", 1, "128");
  }

  @Test
  public void containsNoneOf_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(255, null, 257);
    assertFailureKeys(
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "full contents");
    assertFailureValue("but contained", "[null]");
    assertFailureValue("corresponding to", "null");
  }

  @Test
  public void containsNoneOf_handlesExceptions() {
    // CASE_INSENSITIVE_EQUALITY.compare throws on the null actual element.
    List<String> actual = asList("abc", null, "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .containsNoneOf("GHI", "XYZ");
    // We fail with the more helpful failure message about the unexpected value, not the NPE.
    assertFailureKeys(
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "full contents",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, GHI) threw java.lang.NullPointerException");
  }

  @Test
  public void containsNoneOf_handlesExceptions_alwaysFails() {
    List<String> actual = asList("abc", null, "ghi");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
        .containsNoneOf("DEF", "XYZ");
    // The actual list does not contain the forbidden matcesh. However, we cannot establish that
    // without hitting the null along the way, and that throws NPE, so we are contractually required
    // to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing elements",
        "first exception",
        "expected not to contain any of",
        "testing whether",
        "found no matches (but failing because of exception)",
        "full contents");
    assertFailureValue("expected not to contain any of", "[DEF, XYZ]");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, DEF) threw java.lang.NullPointerException");
  }

  @Test
  public void containsNoneIn_success() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    ImmutableList<Integer> excluded = ImmutableList.of(255, 256, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);
  }

  @Test
  public void containsNoneIn_failure() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    ImmutableList<Integer> excluded = ImmutableList.of(255, 256, 257);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);
    assertFailureKeys(
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "full contents");
    assertFailureValue("but contained", "[+256]");
    assertFailureValue("corresponding to", "256");
  }

  @Test
  public void containsNoneIn_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    List<Integer> excluded = asList(255, null, 257);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);
    assertFailureKeys(
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "full contents");
    assertFailureValue("but contained", "[null]");
    assertFailureValue("corresponding to", "null");
  }

  @Test
  public void containsNoneIn_array() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    Integer[] excluded = new Integer[] {255, 256, 257};
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);

    excluded = new Integer[] {127, 128, 129};
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);
    assertFailureKeys(
        "expected not to contain any of",
        "testing whether",
        "but contained",
        "corresponding to",
        "---",
        "full contents");
    assertFailureValue("but contained", "[+128]");
    assertFailureValue("corresponding to", "128");
  }

  @Test
  public void formattingDiffsUsing_success() {
    ImmutableList<MyRecord> actual =
        ImmutableList.of(MyRecord.create(3, 300), MyRecord.create(2, 200), MyRecord.create(1, 100));
    assertThat(actual)
        .formattingDiffsUsing(RECORD_DIFF_FORMATTER)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactly(MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.create(3, 300));
  }

  @Test
  public void formattingDiffsUsing_failure() {
    ImmutableList<MyRecord> actual =
        ImmutableList.of(
            MyRecord.create(3, 300),
            MyRecord.create(2, 201),
            MyRecord.create(1, 100),
            MyRecord.create(2, 199));
    expectFailure
        .whenTesting()
        .that(actual)
        .formattingDiffsUsing(RECORD_DIFF_FORMATTER)
        .displayingDiffsPairedBy(RECORD_ID)
        .containsExactly(MyRecord.create(1, 100), MyRecord.create(2, 200), MyRecord.create(3, 300));
    assertFailureKeys(
        "for key",
        "missing",
        "unexpected (2)",
        "#1",
        "diff",
        "#2",
        "diff",
        "---",
        "expected",
        "but was");
    assertFailureValue("missing", "2/200");
    assertFailureValue("#1", "2/201");
    assertFailureValueIndexed("diff", 0, "score:1");
    assertFailureValue("#2", "2/199");
    assertFailureValueIndexed("diff", 1, "score:-1");
  }

  private static final class CountsToStringCalls {
    int calls;

    @Override
    public String toString() {
      calls++;
      return super.toString();
    }
  }
}
