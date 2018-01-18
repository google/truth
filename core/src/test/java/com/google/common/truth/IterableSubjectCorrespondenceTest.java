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

import static com.google.common.collect.Collections2.permutations;
import static com.google.common.truth.Correspondence.tolerance;
import static com.google.common.truth.TestCorrespondences.STRING_PARSES_TO_INTEGER_CORRESPONDENCE;
import static com.google.common.truth.TestCorrespondences.WITHIN_10_OF;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link IterableSubject} APIs that use {@link Correspondence}.
 *
 * @author Pete Gillin
 */
@RunWith(JUnit4.class)
public class IterableSubjectCorrespondenceTest extends BaseSubjectTestCase {

  @Test
  public void comparingElementsUsing_contains_success() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(456);
  }

  @Test
  public void comparingElementsUsing_contains_failure() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(2345);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[not a number, +123, +456, +789]> contains at least one element that"
                + " parses to <2345>");
  }

  @Test
  public void comparingElementsUsing_contains_null() {
    List<String> actual = Arrays.asList("+123", null, "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(null);
  }

  @Test
  public void comparingElementsUsing_wrongTypeInActual() {
    ImmutableList<?> actual = ImmutableList.of("valid", 123);
    IterableSubject.UsingCorrespondence<String, Integer> intermediate =
        assertThat(actual).comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE);
    try {
      intermediate.contains(456);
      fail("Expected ClassCastException as actual Iterable contains a non-String");
    } catch (ClassCastException expected) {
    }
  }

  @Test
  public void comparingElementsUsing_doesNotContain_success() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContain(2345);
  }

  @Test
  public void comparingElementsUsing_doesNotContains_failure() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContain(456);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "<[not a number, +123, +456, +789]> should not have contained an element that "
                + "parses to <456>. It contained the following such elements: <[+456]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_inOrder_success() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_successOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_successNonGreedy() {
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
  public void comparingElementsUsing_containsExactlyElementsIn_failsMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "0x40", "0x80");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+64, +128, 0x40, 0x80]> contains exactly one element that "
                + "parses to each element of <[64, 128, 256, 128]>. "
                + "It is missing an element that parses to <256>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_inOrder_passesWhenBothEmpty() {
    ImmutableList<Integer> expected = ImmutableList.of();
    ImmutableList<String> actual = ImmutableList.of();
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsExpectedIsEmpty() {
    ImmutableList<Integer> expected = ImmutableList.of();
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "0x40", "0x80");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[+64, +128, 0x40, 0x80]> is empty");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMultipleMissingCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+64", "0x40", "0x40");
    // Actual list has candidate matches for 64 only, and is missing 128, 256, and the other 128.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+64, +64, 0x40, 0x40]> contains exactly one element that "
                + "parses to each element of <[64, 128, 256, 128]>. "
                + "It is missing an element that parses to each of <[128, 256, 128]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsOrderedMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 512);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256");
    // Actual list has in-order candidate matches for 64, 128, and 256, but is missing 512.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+64, +128, +256]> contains exactly one element that "
                + "parses to each element of <[64, 128, 256, 512]>. "
                + "It is missing an element that parses to <512>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "cheese");
    // Actual list has candidate matches for all the expected, but has extra cheese.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+64, +128, +256, cheese]> contains exactly one element that "
                + "parses to each element of <[64, 128, 256, 128]>. "
                + "It has unexpected elements <[cheese]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsOrderedExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80", "cheese");
    // Actual list has in-order candidate matches for all the expected, but has extra cheese.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+64, +128, +256, 0x80, cheese]> contains exactly one element that "
                + "parses to each element of <[64, 128, 256, 128]>. "
                + "It has unexpected elements <[cheese]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMissingAndExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "jalapenos", "cheese");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256 and has
    // extra jalapenos and cheese.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+64, +128, jalapenos, cheese]> contains exactly one element that "
                + "parses to each element of <[64, 128, 256, 128]>. "
                + "It is missing an element that parses to <256> "
                + "and has unexpected elements <[jalapenos, cheese]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_diffOneMissingAndExtraCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(30, 60, 90);
    ImmutableList<Integer> actual = ImmutableList.of(101, 65, 35);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(WITHIN_10_OF)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[101, 65, 35]> contains exactly one element that is within 10 of "
                + "each element of <[30, 60, 90]>. It is missing an element that is within 10 of "
                + "<90> and has unexpected elements <[101 (diff: 11)]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMissingElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, +256]> contains exactly one element that parses "
                + "to each element of <[64, 128, 256, 128]>. It contains at least one element "
                + "that matches each expected element, and every element it contains matches at "
                + "least one expected element, but there was no 1:1 mapping between all the "
                + "actual and expected elements. Using the most complete 1:1 mapping (or one "
                + "such mapping, if there is a tie), it is missing an element that parses to "
                + "<128>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsExtraElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x80", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    String expectedPreamble =
        "Not true that <[+128, +64, +256, 0x80, 0x40]> contains exactly one element that parses "
            + "to each element of <[64, 128, 256, 128]>. It contains at least one element "
            + "that matches each expected element, and every element it contains matches at "
            + "least one expected element, but there was no 1:1 mapping between all the "
            + "actual and expected elements. Using the most complete 1:1 mapping (or one "
            + "such mapping, if there is a tie), it has unexpected elements ";
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isAnyOf(expectedPreamble + "<[0x40]>", expectedPreamble + "<[+64]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMissingAndExtraInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
    String expectedPreamble =
        "Not true that <[+128, +64, +256, 0x40]> contains exactly one element that parses "
            + "to each element of <[64, 128, 256, 128]>. It contains at least one element "
            + "that matches each expected element, and every element it contains matches at "
            + "least one expected element, but there was no 1:1 mapping between all the "
            + "actual and expected elements. Using the most complete 1:1 mapping (or one "
            + "such mapping, if there is a tie), it is missing an element that parses to "
            + "<128> and has unexpected elements ";
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isAnyOf(expectedPreamble + "<[0x40]>", expectedPreamble + "<[+64]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_diffOneMissingAndExtraInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(30, 30, 60);
    ImmutableList<Integer> actual = ImmutableList.of(25, 55, 65);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(WITHIN_10_OF)
        .containsExactlyElementsIn(expected);
    String expectedPreamble =
        "Not true that <[25, 55, 65]> contains exactly one element that is within 10 of "
            + "each element of <[30, 30, 60]>. It contains at least one element that matches each "
            + "expected element, and every element it contains matches at least one expected "
            + "element, but there was no 1:1 mapping between all the actual and expected elements. "
            + "Using the most complete 1:1 mapping (or one such mapping, if there is a tie), it is "
            + "missing an element that is within 10 of <30> and has unexpected elements ";
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isAnyOf(expectedPreamble + "<[55 (diff: 25)]>", expectedPreamble + "<[65 (diff: 35)]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_inOrder_failsOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected)
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, 0x80, +256]> contains, in order, exactly one element "
                + "that parses to each element of <[64, 128, 256, 128]>");
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_null() {
    List<Integer> expected = Arrays.asList(128, null);
    List<String> actual = Arrays.asList(null, "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_array() {
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
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+64, +128, 0x40, 0x80]> contains exactly one element that "
                + "parses to each element of <[64, 128, 256, 128]>. "
                + "It is missing an element that parses to <256>");
  }

  @Test
  public void comparingElementsUsing_containsExactly_inOrder_success() {
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(64, 128, 256, 128)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsExactly_successOutOfOrder() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(64, 128, 256, 128);
  }

  @Test
  public void comparingElementsUsing_containsExactly_failsMissingAndExtraInOneToOne() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(64, 128, 256, 128);
    String expectedPreamble =
        "Not true that <[+128, +64, +256, 0x40]> contains exactly one element that parses "
            + "to each element of <[64, 128, 256, 128]>. It contains at least one element "
            + "that matches each expected element, and every element it contains matches at "
            + "least one expected element, but there was no 1:1 mapping between all the "
            + "actual and expected elements. Using the most complete 1:1 mapping (or one "
            + "such mapping, if there is a tie), it is missing an element that parses to "
            + "<128> and has unexpected elements ";
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isAnyOf(expectedPreamble + "<[0x40]>", expectedPreamble + "<[+64]>");
  }

  @Test
  public void comparingElementsUsing_containsExactly_nullValueInArray() {
    List<String> actual = Arrays.asList(null, "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(128, null);
  }

  @Test
  public void comparingElementsUsing_containsExactly_nullArray() {
    // Truth is tolerant of this erroneous varargs call.
    List<String> actual = Arrays.asList((String) null);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly((Integer[]) null)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsAllIn_inOrder_success() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "+256", "0x80", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsAllIn_successOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsAllIn_successNonGreedy() {
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
      assertThat(permutedActual).comparingElementsUsing(tolerance(0.1)).containsAllIn(expected);
    }
  }

  @Test
  public void comparingElementsUsing_containsAllIn_failsMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "0x40", "0x80", "fum");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[fee, +64, +128, fi, fo, 0x40, 0x80, fum]> contains at least one "
                + "element that parses to each element of <[64, 128, 256, 128]>. "
                + "It is missing an element that parses to <256>");
  }

  @Test
  public void comparingElementsUsing_containsAllIn_failsMultipleMissingCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+64", "fi", "fo", "0x40", "0x40", "fum");
    // Actual list has candidate matches for 64 only, and is missing 128, 256, and the other 128.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[fee, +64, +64, fi, fo, 0x40, 0x40, fum]> contains at least one "
                + "element that parses to each element of <[64, 128, 256, 128]>. "
                + "It is missing an element that parses to each of <[128, 256, 128]>");
  }

  @Test
  public void comparingElementsUsing_containsAllIn_failsOrderedMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 512);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "fi", "fo", "+128", "+256", "fum");
    // Actual list has in-order candidate matches for 64, 128, and 256, but is missing 512.
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[fee, +64, fi, fo, +128, +256, fum]> contains at least one "
                + "element that parses to each element of <[64, 128, 256, 512]>. "
                + "It is missing an element that parses to <512>");
  }

  @Test
  public void comparingElementsUsing_containsAllIn_failsMissingElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "fi", "fo", "+64", "+256", "fum");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[fee, +128, fi, fo, +64, +256, fum]> contains at least one element "
                + "that parses to each element of <[64, 128, 256, 128]>. It contains at least "
                + "one element that matches each expected element, but there was no 1:1 mapping "
                + "between all the expected elements and any subset of the actual elements. "
                + "Using the most complete 1:1 mapping (or one such mapping, if there is a tie), "
                + "it is missing an element that parses to <128>");
  }

  @Test
  public void comparingElementsUsing_containsAllIn_inOrder_failsOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected)
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[fee, +128, +64, fi, fo, 0x80, +256, fum]> contains, in order, "
                + "at least one element that parses to each element of <[64, 128, 256, 128]>");
  }

  @Test
  public void comparingElementsUsing_containsAllIn_null() {
    List<Integer> expected = Arrays.asList(128, null);
    List<String> actual = Arrays.asList(null, "fee", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsAllIn_array() {
    Integer[] expected = new Integer[] {64, 128, 256, 128};
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);

    actual = ImmutableList.of("fee", "+64", "+128", "fi", "fo", "0x40", "0x80", "fum");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[fee, +64, +128, fi, fo, 0x40, 0x80, fum]> contains at least one "
                + "element that parses to each element of <[64, 128, 256, 128]>. "
                + "It is missing an element that parses to <256>");
  }

  @Test
  public void comparingElementsUsing_containsAllOf_inOrder_success() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "+256", "0x80", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllOf(64, 128, 256, 128)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsAllOf_successOutOfOrder() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllOf(64, 128, 256, 128);
  }

  @Test
  public void comparingElementsUsing_containsAllOf_failsMissingElementInOneToOne() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "fi", "fo", "+64", "+256", "fum");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllOf(64, 128, 256, 128);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[fee, +128, fi, fo, +64, +256, fum]> contains at least one element "
                + "that parses to each element of <[64, 128, 256, 128]>. It contains at least "
                + "one element that matches each expected element, but there was no 1:1 mapping "
                + "between all the expected elements and any subset of the actual elements. "
                + "Using the most complete 1:1 mapping (or one such mapping, if there is a tie), "
                + "it is missing an element that parses to <128>");
  }

  @Test
  public void comparingElementsUsing_containsAllOf_nullValueInArray() {
    List<String> actual = Arrays.asList(null, "fee", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllOf(128, null);
  }

  @Test
  public void comparingElementsUsing_containsAnyOf_success() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyOf(255, 256, 257);
  }

  @Test
  public void comparingElementsUsing_containsAnyOf_failure() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyOf(255, 256, 257);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, This is not the string you're looking for, 0x40]> "
                + "contains at least one element that parses to any of <[255, 256, 257]>");
  }

  @Test
  public void comparingElementsUsing_containsAnyOf_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyOf(255, null, 257);
  }

  @Test
  public void comparingElementsUsing_containsAnyIn_success() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    ImmutableList<Integer> expected = ImmutableList.of(255, 256, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsAnyIn_failure() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    ImmutableList<Integer> expected = ImmutableList.of(255, 256, 257);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, This is not the string you're looking for, 0x40]> "
                + "contains at least one element that parses to any element in "
                + "<[255, 256, 257]>");
  }

  @Test
  public void comparingElementsUsing_containsAnyIn_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    List<Integer> expected = asList(255, null, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsAnyIn_array() {
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
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, +256, 0x40]> "
                + "contains at least one element that parses to any element in "
                + "<[511, 512, 513]>");
  }

  @Test
  public void comparingElementsUsing_containsNoneOf_success() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(255, 256, 257);
  }

  @Test
  public void comparingElementsUsing_containsNoneOf_failure() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(255, 256, 257);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, +256, 0x40]> contains no element that parses to any of "
                + "<[255, 256, 257]>. It contains <[+256 which corresponds to 256]>");
  }

  @Test
  public void comparingElementsUsing_containsNoneOf_multipleFailures() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(64, 128);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, +256, 0x40]> contains no element that parses to any of "
                + "<[64, 128]>. It contains <[[+64, 0x40] which all correspond to 64, "
                + "+128 which corresponds to 128]>");
  }

  @Test
  public void comparingElementsUsing_containsNoneOf_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(255, null, 257);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, null, 0x40]> contains no element that parses to any of "
                + "<[255, null, 257]>. It contains <[null which corresponds to null]>");
  }

  @Test
  public void comparingElementsUsing_containsNoneIn_success() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    ImmutableList<Integer> excluded = ImmutableList.of(255, 256, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);
  }

  @Test
  public void comparingElementsUsing_containsNoneIn_failure() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    ImmutableList<Integer> excluded = ImmutableList.of(255, 256, 257);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, +256, 0x40]> contains no element that parses to "
                + "any element in <[255, 256, 257]>. It contains "
                + "<[+256 which corresponds to 256]>");
  }

  @Test
  public void comparingElementsUsing_containsNoneIn_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    List<Integer> excluded = asList(255, null, 257);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, null, 0x40]> contains no element that parses to "
                + "any element in <[255, null, 257]>. It contains "
                + "<[null which corresponds to null]>");
  }

  @Test
  public void comparingElementsUsing_containsNoneIn_array() {
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
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[+128, +64, This is not the string you're looking for, 0x40]> "
                + "contains no element that parses to any element in <[127, 128, 129]>. "
                + "It contains <[+128 which corresponds to 128]>");
  }
}
