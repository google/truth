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
import static com.google.common.truth.FailureAssertions.assertFailureValueIndexed;
import static com.google.common.truth.TestCorrespondences.CASE_INSENSITIVE_EQUALITY;
import static com.google.common.truth.TestCorrespondences.INT_DIFF_FORMATTER;
import static com.google.common.truth.TestCorrespondences.STRING_PARSES_TO_INTEGER_CORRESPONDENCE;
import static com.google.common.truth.TestCorrespondences.WITHIN_10_OF;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Map} subjects.
 */
@RunWith(JUnit4.class)
// We intentionally test mismatches.
// TODO(cpovirk): Maybe suppress at a finer scope.
@SuppressWarnings("TruthIncompatibleType")
public class MapSubjectTest {

  @Test
  public void containsExactlyWithNullKey() {
    Map<@Nullable String, String> actual = new HashMap<>();
    actual.put(null, "value");

    assertThat(actual).containsExactly(null, "value");
    assertThat(actual).containsExactly(null, "value").inOrder();
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();
  }

  @Test
  public void containsExactlyWithNullValue() {
    Map<String, @Nullable String> actual = new HashMap<>();
    actual.put("key", null);

    assertThat(actual).containsExactly("key", null);
    assertThat(actual).containsExactly("key", null).inOrder();
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();
  }

  @Test
  public void containsExactlyRejectsNullActual() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((Map<?, ?>) null).containsExactly("key", "value"));
    assertFailureKeys(e, "expected a map that contains exactly", "but was");
  }

  @Test
  public void containsExactlyEntriesInRejectsNullExpected() {
    ImmutableMap<String, String> map = ImmutableMap.of("key", "value");

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(map).containsExactlyEntriesIn(null));
    assertFailureKeys(
        e, "could not perform containment check because expected map was null", "actual contents");
  }

  @Test
  public void containsExactlyEmpty() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of();

    assertThat(actual).containsExactly();
    assertThat(actual).containsExactly().inOrder();
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();
  }

  @Test
  public void containsExactlyEmpty_fails() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1);

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).containsExactly());
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void containsExactlyEntriesInEmpty_fails() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1);

    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).containsExactlyEntriesIn(ImmutableMap.of()));
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void containsExactlyOneEntry() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1);

    assertThat(actual).containsExactly("jan", 1);
    assertThat(actual).containsExactly("jan", 1).inOrder();
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();
  }

  @Test
  public void containsExactlyMultipleEntries() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    assertThat(actual).containsExactly("march", 3, "jan", 1, "feb", 2);
    assertThat(actual).containsExactly("jan", 1, "feb", 2, "march", 3).inOrder();
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();
  }

  @Test
  public void containsExactlyDuplicateKeys() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    IllegalArgumentException expected =
        assertThrows(
            IllegalArgumentException.class,
            () -> assertThat(actual).containsExactly("jan", 1, "jan", 2, "jan", 3));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Duplicate keys ([jan x 3]) cannot be passed to containsExactly().");
  }

  @Test
  public void containsExactlyMultipleDuplicateKeys() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    IllegalArgumentException expected =
        assertThrows(
            IllegalArgumentException.class,
            () -> assertThat(actual).containsExactly("jan", 1, "jan", 1, "feb", 2, "feb", 2));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Duplicate keys ([jan x 2, feb x 2]) cannot be passed to containsExactly().");
  }

  @Test
  public void containsExactlyExtraKey() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsExactly("feb", 2, "jan", 1));
    assertFailureKeys(
        e, "unexpected keys", "for key", "unexpected value", "---", "expected", "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "unexpected value", "3");
    assertFailureValue(e, "expected", "{feb=2, jan=1}");
    assertFailureValue(e, "but was", "{jan=1, feb=2, march=3}");
  }

  @Test
  public void containsExactlyExtraKeyInOrder() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).containsExactly("feb", 2, "jan", 1).inOrder());
    assertFailureKeys(
        e, "unexpected keys", "for key", "unexpected value", "---", "expected", "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "unexpected value", "3");
  }

  @Test
  public void containsExactlyMissingKey() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(actual).containsExactly("jan", 1, "march", 3, "feb", 2));
    assertFailureKeys(e, "missing keys", "for key", "expected value", "---", "expected", "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "expected value", "3");
  }

  @Test
  public void containsExactlyWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(actual).containsExactly("jan", 1, "march", 33, "feb", 2));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "expected value", "33");
    assertFailureValue(e, "but got value", "3");
  }

  @Test
  public void containsExactlyWrongValueWithNull() {
    // Test for https://github.com/google/truth/issues/468
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(actual).containsExactly("jan", 1, "march", null, "feb", 2));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "expected value", "null");
    assertFailureValue(e, "but got value", "3");
  }

  @Test
  public void containsExactlyExtraKeyAndMissingKey() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "march", 3);
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsExactly("jan", 1, "feb", 2));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "feb");
    assertFailureValue(e, "expected value", "2");
    assertFailureValueIndexed(e, "for key", 1, "march");
    assertFailureValue(e, "unexpected value", "3");
  }

  @Test
  public void containsExactlyExtraKeyAndWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).containsExactly("jan", 1, "march", 33));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "march");
    assertFailureValue(e, "expected value", "33");
    assertFailureValue(e, "but got value", "3");
    assertFailureValueIndexed(e, "for key", 1, "feb");
    assertFailureValue(e, "unexpected value", "2");
  }

  @Test
  public void containsExactlyMissingKeyAndWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(actual).containsExactly("jan", 1, "march", 33, "feb", 2));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "march");
    assertFailureValueIndexed(e, "expected value", 0, "33");
    assertFailureValue(e, "but got value", "3");
    assertFailureValueIndexed(e, "for key", 1, "feb");
    assertFailureValueIndexed(e, "expected value", 1, "2");
  }

  @Test
  public void containsExactlyExtraKeyAndMissingKeyAndWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).containsExactly("march", 33, "feb", 2));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "march");
    assertFailureValueIndexed(e, "expected value", 0, "33");
    assertFailureValue(e, "but got value", "3");
    assertFailureValueIndexed(e, "for key", 1, "feb");
    assertFailureValueIndexed(e, "expected value", 1, "2");
    assertFailureValueIndexed(e, "for key", 2, "jan");
    assertFailureValue(e, "unexpected value", "1");
  }

  @Test
  public void containsExactlyNotInOrder() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();

    assertThat(actual).containsExactly("jan", 1, "march", 3, "feb", 2);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(actual).containsExactly("jan", 1, "march", 3, "feb", 2).inOrder());
    assertFailureKeys(e, "entries match, but order was wrong", "expected", "but was");
    assertFailureValue(e, "expected", "{jan=1, march=3, feb=2}");
    assertFailureValue(e, "but was", "{jan=1, feb=2, march=3}");
  }

  @Test
  @SuppressWarnings("ShouldHaveEvenArgs")
  public void containsExactlyBadNumberOfArgs() {
    ImmutableMap<String, Integer> actual =
        ImmutableMap.of("jan", 1, "feb", 2, "march", 3, "april", 4, "may", 5);
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();

    IllegalArgumentException expected =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                assertThat(actual)
                    .containsExactly(
                        "jan", 1, "feb", 2, "march", 3, "april", 4, "may", 5, "june", 6, "july"));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo(
            "There must be an equal number of key/value pairs "
                + "(i.e., the number of key/value parameters (13) must be even).");
  }

  @Test
  public void containsExactlyWrongValue_sameToStringForValues() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableMap.of("jan", 1L, "feb", 2L))
                    .containsExactly("jan", 1, "feb", 2));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "jan");
    assertFailureValueIndexed(e, "expected value", 0, "1 (Integer)");
    assertFailureValueIndexed(e, "but got value", 0, "1 (Long)");
    assertFailureValueIndexed(e, "for key", 1, "feb");
    assertFailureValueIndexed(e, "expected value", 1, "2 (Integer)");
    assertFailureValueIndexed(e, "but got value", 1, "2 (Long)");
  }

  @Test
  public void containsExactlyWrongValue_sameToStringForKeys() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableMap.of(1L, "jan", 1, "feb"))
                    .containsExactly(1, "jan", 1L, "feb"));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "1 (Integer)");
    assertFailureValueIndexed(e, "expected value", 0, "jan");
    assertFailureValueIndexed(e, "but got value", 0, "feb");
    assertFailureValueIndexed(e, "for key", 1, "1 (Long)");
    assertFailureValueIndexed(e, "expected value", 1, "feb");
    assertFailureValueIndexed(e, "but got value", 1, "jan");
  }

  @Test
  public void containsExactlyExtraKeyAndMissingKey_failsWithSameToStringForKeys() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableMap.of(1L, "jan", 2, "feb"))
                    .containsExactly(1, "jan", 2, "feb"));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "1 (Integer)");
    assertFailureValue(e, "expected value", "jan");
    assertFailureValueIndexed(e, "for key", 1, "1 (Long)");
    assertFailureValue(e, "unexpected value", "jan");
  }

  @Test
  public void containsAtLeastWithNullKey() {
    Map<@Nullable String, String> actual = new HashMap<>();
    actual.put(null, "value");
    actual.put("unexpectedKey", "unexpectedValue");
    Map<@Nullable String, String> expected = new HashMap<>();
    expected.put(null, "value");

    assertThat(actual).containsAtLeast(null, "value");
    assertThat(actual).containsAtLeast(null, "value").inOrder();
    assertThat(actual).containsAtLeastEntriesIn(expected);
    assertThat(actual).containsAtLeastEntriesIn(expected).inOrder();
  }

  @Test
  public void containsAtLeastWithNullValue() {
    Map<String, @Nullable String> actual = new HashMap<>();
    actual.put("key", null);
    actual.put("unexpectedKey", "unexpectedValue");
    Map<String, @Nullable String> expected = new HashMap<>();
    expected.put("key", null);

    assertThat(actual).containsAtLeast("key", null);
    assertThat(actual).containsAtLeast("key", null).inOrder();
    assertThat(actual).containsAtLeastEntriesIn(expected);
    assertThat(actual).containsAtLeastEntriesIn(expected).inOrder();
  }

  @Test
  public void containsAtLeastRejectsNullActual() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((Map<?, ?>) null).containsAtLeast("key", "value"));
    assertFailureKeys(e, "expected a map that contains at least", "but was");
  }

  @Test
  public void containsAtLeastEntriesInRejectsNullExpected() {
    ImmutableMap<String, String> map = ImmutableMap.of("key", "value");

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(map).containsAtLeastEntriesIn(null));
    assertFailureKeys(
        e, "could not perform containment check because expected map was null", "actual contents");
  }

  @Test
  public void containsAtLeastEmpty() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("key", 1);

    assertThat(actual).containsAtLeastEntriesIn(ImmutableMap.of());
    assertThat(actual).containsAtLeastEntriesIn(ImmutableMap.of()).inOrder();
  }

  @Test
  public void containsAtLeastOneEntry() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1);

    assertThat(actual).containsAtLeast("jan", 1);
    assertThat(actual).containsAtLeast("jan", 1).inOrder();
    assertThat(actual).containsAtLeastEntriesIn(actual);
    assertThat(actual).containsAtLeastEntriesIn(actual).inOrder();
  }

  @Test
  public void containsAtLeastMultipleEntries() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "mar", 3, "apr", 4);

    assertThat(actual).containsAtLeast("apr", 4, "jan", 1, "feb", 2);
    assertThat(actual).containsAtLeast("jan", 1, "feb", 2, "apr", 4).inOrder();
    assertThat(actual).containsAtLeastEntriesIn(ImmutableMap.of("apr", 4, "jan", 1, "feb", 2));
    assertThat(actual).containsAtLeastEntriesIn(actual).inOrder();
  }

  @Test
  public void containsAtLeastDuplicateKeys() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    IllegalArgumentException expected =
        assertThrows(
            IllegalArgumentException.class,
            () -> assertThat(actual).containsAtLeast("jan", 1, "jan", 2, "jan", 3));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Duplicate keys ([jan x 3]) cannot be passed to containsAtLeast().");
  }

  @Test
  public void containsAtLeastMultipleDuplicateKeys() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    IllegalArgumentException expected =
        assertThrows(
            IllegalArgumentException.class,
            () -> assertThat(actual).containsAtLeast("jan", 1, "jan", 1, "feb", 2, "feb", 2));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Duplicate keys ([jan x 2, feb x 2]) cannot be passed to containsAtLeast().");
  }

  @Test
  public void containsAtLeastMissingKey() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2);
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).containsAtLeast("jan", 1, "march", 3));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "expected value", "3");
    assertFailureValue(e, "expected to contain at least", "{jan=1, march=3}");
  }

  @Test
  public void containsAtLeastWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).containsAtLeast("jan", 1, "march", 33));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "expected value", "33");
    assertFailureValue(e, "but got value", "3");
  }

  @Test
  public void containsAtLeastWrongValueWithNull() {
    // Test for https://github.com/google/truth/issues/468
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).containsAtLeast("jan", 1, "march", null));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "expected value", "null");
    assertFailureValue(e, "but got value", "3");
  }

  @Test
  public void containsAtLeastExtraKeyAndMissingKeyAndWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "march", 3);
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).containsAtLeast("march", 33, "feb", 2));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "march");
    assertFailureValueIndexed(e, "expected value", 0, "33");
    assertFailureValue(e, "but got value", "3");
    assertFailureValueIndexed(e, "for key", 1, "feb");
    assertFailureValueIndexed(e, "expected value", 1, "2");
  }

  @Test
  public void containsAtLeastNotInOrder() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    assertThat(actual).containsAtLeast("march", 3, "feb", 2);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(actual).containsAtLeast("march", 3, "feb", 2).inOrder());
    assertFailureKeys(
        e,
        "required entries were all found, but order was wrong",
        "expected to contain at least",
        "but was");
    assertFailureValue(e, "expected to contain at least", "{march=3, feb=2}");
    assertFailureValue(e, "but was", "{jan=1, feb=2, march=3}");
  }

  @Test
  @SuppressWarnings("ShouldHaveEvenArgs")
  public void containsAtLeastBadNumberOfArgs() {
    ImmutableMap<String, Integer> actual =
        ImmutableMap.of("jan", 1, "feb", 2, "march", 3, "april", 4, "may", 5);

    IllegalArgumentException expected =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                assertThat(actual)
                    .containsAtLeast(
                        "jan", 1, "feb", 2, "march", 3, "april", 4, "may", 5, "june", 6, "july"));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo(
            "There must be an equal number of key/value pairs "
                + "(i.e., the number of key/value parameters (13) must be even).");
  }

  @Test
  public void containsAtLeastWrongValue_sameToStringForValues() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableMap.of("jan", 1L, "feb", 2L, "mar", 3L))
                    .containsAtLeast("jan", 1, "feb", 2));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "jan");
    assertFailureValueIndexed(e, "expected value", 0, "1 (Integer)");
    assertFailureValueIndexed(e, "but got value", 0, "1 (Long)");
    assertFailureValueIndexed(e, "for key", 1, "feb");
    assertFailureValueIndexed(e, "expected value", 1, "2 (Integer)");
    assertFailureValueIndexed(e, "but got value", 1, "2 (Long)");
  }

  @Test
  public void containsAtLeastWrongValue_sameToStringForKeys() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableMap.of(1L, "jan", 1, "feb"))
                    .containsAtLeast(1, "jan", 1L, "feb"));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "1 (Integer)");
    assertFailureValueIndexed(e, "expected value", 0, "jan");
    assertFailureValueIndexed(e, "but got value", 0, "feb");
    assertFailureValueIndexed(e, "for key", 1, "1 (Long)");
    assertFailureValueIndexed(e, "expected value", 1, "feb");
    assertFailureValueIndexed(e, "but got value", 1, "jan");
  }

  @Test
  public void containsAtLeastExtraKeyAndMissingKey_failsWithSameToStringForKeys() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableMap.of(1L, "jan", 2, "feb"))
                    .containsAtLeast(1, "jan", 2, "feb"));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue(e, "for key", "1 (Integer)");
    assertFailureValue(e, "expected value", "jan");
  }

  @Test
  public void isEqualToPass() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    assertThat(actual).isEqualTo(expectedMap);
  }

  @Test
  public void isEqualToFailureExtraMissingAndDiffering() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "april", 4, "march", 5);

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expectedMap));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "march");
    assertFailureValueIndexed(e, "expected value", 0, "5");
    assertFailureValue(e, "but got value", "3");
    assertFailureValueIndexed(e, "for key", 1, "april");
    assertFailureValueIndexed(e, "expected value", 1, "4");
    assertFailureValueIndexed(e, "for key", 2, "feb");
    assertFailureValue(e, "unexpected value", "2");
    assertFailureValue(e, "expected", "{jan=1, april=4, march=5}");
    assertFailureValue(e, "but was", "{jan=1, feb=2, march=3}");
  }

  @Test
  public void isEqualToFailureDiffering() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 4);

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expectedMap));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "march");
    assertFailureValue(e, "expected value", "4");
    assertFailureValue(e, "but got value", "3");
  }

  @Test
  public void isEqualToFailureExtra() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2);

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expectedMap));
    assertFailureKeys(
        e, "unexpected keys", "for key", "unexpected value", "---", "expected", "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "unexpected value", "3");
  }

  @Test
  public void isEqualToFailureMissing() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expectedMap));
    assertFailureKeys(e, "missing keys", "for key", "expected value", "---", "expected", "but was");
    assertFailureValue(e, "for key", "march");
    assertFailureValue(e, "expected value", "3");
  }

  @Test
  public void isEqualToFailureExtraAndMissing() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "mar", 3);

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expectedMap));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "mar");
    assertFailureValue(e, "expected value", "3");
    assertFailureValueIndexed(e, "for key", 1, "march");
    assertFailureValue(e, "unexpected value", "3");
  }

  @Test
  public void isEqualToFailureDiffering_sameToString() {
    ImmutableMap<String, Number> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3L);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expectedMap));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "march");
    assertFailureValue(e, "expected value", "3 (Integer)");
    assertFailureValue(e, "but got value", "3 (Long)");
  }

  @Test
  public void isEqualToNonMap() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo("something else"));
    assertFailureKeys(e, "expected", "but was");
  }

  @Test
  public void isEqualToNotConsistentWithEquals() {
    TreeMap<String, Integer> actual = new TreeMap<>(CASE_INSENSITIVE_ORDER);
    TreeMap<String, Integer> expected = new TreeMap<>(CASE_INSENSITIVE_ORDER);
    actual.put("one", 1);
    expected.put("ONE", 1);
    /*
     * Our contract doesn't guarantee that the following test will pass. It *currently* does,
     * though, and if we change that behavior, we want this test to let us know.
     */
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void isEqualToNotConsistentWithEquals_failure() {
    TreeMap<String, Integer> actual = new TreeMap<>(CASE_INSENSITIVE_ORDER);
    TreeMap<String, Integer> expected = new TreeMap<>(CASE_INSENSITIVE_ORDER);
    actual.put("one", 1);
    expected.put("ONE", 1);
    actual.put("two", 2);
    expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expected));
    // The exact message generated is unspecified.
  }

  @Test
  public void isEqualToActualNullOtherMap() {
    expectFailure(whenTesting -> whenTesting.that((Map<?, ?>) null).isEqualTo(ImmutableMap.of()));
  }

  @Test
  public void isEqualToActualMapOtherNull() {
    expectFailure(whenTesting -> whenTesting.that(ImmutableMap.of()).isEqualTo(null));
  }

  @Test
  public void isNotEqualTo() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> unexpected = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    expectFailure(whenTesting -> whenTesting.that(actual).isNotEqualTo(unexpected));
  }

  @Test
  public void isEmpty() {
    ImmutableMap<String, String> actual = ImmutableMap.of();
    assertThat(actual).isEmpty();
  }

  @Test
  public void isEmptyWithFailure() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of(1, 5);
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).isEmpty());
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void isEmptyOnNullMap() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Map<?, ?>) null).isEmpty());
    assertFailureKeys(e, "expected an empty map", "but was");
  }

  @Test
  public void isNotEmpty() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of(1, 5);
    assertThat(actual).isNotEmpty();
  }

  @Test
  public void isNotEmptyWithFailure() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of();
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).isNotEmpty());
    assertFailureKeys(e, "expected not to be empty");
  }

  @Test
  public void isNotEmptyOnNullMap() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Map<?, ?>) null).isNotEmpty());
    assertFailureKeys(e, "expected a nonempty map", "but was");
  }

  @Test
  public void hasSize() {
    assertThat(ImmutableMap.of(1, 2, 3, 4)).hasSize(2);
  }

  @Test
  public void hasSizeZero() {
    assertThat(ImmutableMap.of()).hasSize(0);
  }

  @Test
  public void hasSizeNegative() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(ImmutableMap.of(1, 2)).hasSize(-1));
    assertFailureKeys(
        e,
        "expected a map with a negative size, but that is impossible",
        "expected size",
        "actual size",
        "actual contents");
    assertFailureValue(e, "expected size", "-1");
    assertFailureValue(e, "actual size", "1");
    assertFailureValue(e, "actual contents", "{1=2}");
  }

  @Test
  public void hasSizeOnNullMap() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Map<?, ?>) null).hasSize(1));
    assertFailureKeys(e, "expected a map with size", "but was");
  }

  @Test
  public void containsKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    assertThat(actual).containsKey("kurt");
  }

  @Test
  public void containsKeyFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).containsKey("greg"));
    assertFailureKeys(e, "value of", "expected to contain", "but was", "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected to contain", "greg");
    assertFailureValue(e, "but was", "[kurt]");
  }

  @Test
  public void containsKeyNullFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).containsKey(null));
    assertFailureKeys(e, "value of", "expected to contain", "but was", "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected to contain", "null");
    assertFailureValue(e, "but was", "[kurt]");
  }

  @Test
  public void containsKeyOnNullMap() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Map<?, ?>) null).containsKey("greg"));
    assertFailureKeys(e, "expected a map that contains key", "but was");
  }

  @Test
  public void containsKey_failsWithSameToString() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableMap.of(1L, "value1", 2L, "value2", "1", "value3"))
                    .containsKey(1));
    assertFailureKeys(
        e,
        "value of",
        "expected to contain",
        "an instance of",
        "but did not",
        "though it did contain",
        "full contents",
        "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected to contain", "1");
  }

  @Test
  public void containsKey_failsWithNullStringAndNull() {
    Map<String, String> actual = new HashMap<>();
    actual.put("null", "value1");

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).containsKey(null));
    assertFailureKeys(
        e,
        "value of",
        "expected to contain",
        "an instance of",
        "but did not",
        "though it did contain",
        "full contents",
        "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected to contain", "null");
  }

  @Test
  public void containsNullKey() {
    Map<@Nullable String, String> actual = new HashMap<>();
    actual.put(null, "null");
    assertThat(actual).containsKey(null);
  }

  @Test
  public void doesNotContainKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    assertThat(actual).doesNotContainKey("greg");
    assertThat(actual).doesNotContainKey(null);
  }

  @Test
  public void doesNotContainKeyFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).doesNotContainKey("kurt"));
    assertFailureKeys(e, "value of", "expected not to contain", "but was", "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected not to contain", "kurt");
    assertFailureValue(e, "but was", "[kurt]");
  }

  @Test
  public void doesNotContainKeyOnNullMap() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Map<?, ?>) null).doesNotContainKey("greg"));
    assertFailureKeys(e, "expected a map that does not contain key", "but was");
  }

  @Test
  public void doesNotContainNullKey() {
    Map<@Nullable String, String> actual = new HashMap<>();
    actual.put(null, "null");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).doesNotContainKey(null));
    assertFailureKeys(e, "value of", "expected not to contain", "but was", "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected not to contain", "null");
    assertFailureValue(e, "but was", "[null]");
  }

  @Test
  public void containsEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    assertThat(actual).containsEntry("kurt", "kluever");
  }

  @Test
  /*
   * This test is all about reference equality, so we do want a new String instance.
   *
   * (Alternatively, we could perform the test with a type other than String. That would still have
   * been enough to demonstrate that the test failed before my change to MapSubject. However, it
   * would have demonstrated only a *very* terrible failure message, as opposed to the *extremely*
   * terrible failure message that a user reported.)
   */
  @SuppressWarnings("StringCopy")
  public void containsEntryInIdentityHashMapWithNonIdenticalValue() {
    IdentityHashMap<String, String> actual = new IdentityHashMap<>();
    actual.put("kurt", "kluever");
    assertThat(actual).containsEntry("kurt", new String("kluever"));
  }

  @Test
  public void containsEntryFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsEntry("greg", "kick"));
    assertFailureKeys(e, "expected to contain entry", "but was");
    assertFailureValue(e, "expected to contain entry", "greg=kick");
    assertFailureValue(e, "but was", "{kurt=kluever}");
  }

  @Test
  public void containsEntryOnNullMap() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((Map<?, ?>) null).containsEntry("greg", "kick"));
    assertFailureKeys(e, "expected a map that contains entry", "but was");
  }

  @Test
  public void containsEntry_failsWithSameToStringOfKey() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(ImmutableMap.of(1L, "value1", 2L, "value2"))
                    .containsEntry(1, "value1"));
    assertFailureKeys(
        e,
        "expected to contain entry",
        "an instance of",
        "but did not",
        "though it did contain keys",
        "full contents");
    assertFailureValue(e, "an instance of", "Map.Entry<Integer, String>");
    assertFailureValue(e, "though it did contain keys", "[1] (Long)");
  }

  @Test
  public void containsEntry_failsWithSameToStringOfValue() {
    // Does not contain the correct key, but does contain a value which matches by toString.
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(ImmutableMap.of(1, "null")).containsEntry(2, null));
    assertFailureKeys(
        e,
        "expected to contain entry",
        "an instance of",
        "but did not",
        "though it did contain values",
        "full contents");
    assertFailureValue(e, "an instance of", "Map.Entry<Integer, null type>");
    assertFailureValue(e, "though it did contain values", "[null] (String)");
  }

  @Test
  public void containsNullKeyAndValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsEntry(null, null));
    assertFailureKeys(e, "expected to contain entry", "but was");
    assertFailureValue(e, "expected to contain entry", "null=null");
    assertFailureValue(e, "but was", "{kurt=kluever}");
  }

  @Test
  public void containsNullEntry() {
    Map<@Nullable String, @Nullable String> actual = new HashMap<>();
    actual.put(null, null);
    assertThat(actual).containsEntry(null, null);
  }

  @Test
  public void containsNullEntryValue() {
    Map<@Nullable String, @Nullable String> actual = new HashMap<>();
    actual.put(null, null);
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsEntry("kurt", null));
    assertFailureKeys(
        e,
        "expected to contain entry",
        "but did not",
        "though it did contain keys with that value",
        "full contents");
    assertFailureValue(e, "expected to contain entry", "kurt=null");
    assertFailureValue(e, "though it did contain keys with that value", "[null]");
  }

  private static final String KEY_IS_PRESENT_WITH_DIFFERENT_VALUE =
      "key is present but with a different value";

  @Test
  public void containsNullEntryKey() {
    Map<@Nullable String, @Nullable String> actual = new HashMap<>();
    actual.put(null, null);
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsEntry(null, "kluever"));
    assertFailureValue(e, "value of", "map.get(null)");
    assertFailureValue(e, "expected", "kluever");
    assertFailureValue(e, "but was", "null");
    assertFailureValue(e, "map was", "{null=null}");
    assertThat(e).hasMessageThat().contains(KEY_IS_PRESENT_WITH_DIFFERENT_VALUE);
  }

  @Test
  public void containsExactly_bothExactAndToStringKeyMatches_showsExactKeyMatch() {
    ImmutableMap<Number, String> actual = ImmutableMap.of(1, "actual int", 1L, "actual long");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsEntry(1L, "expected long"));
    // should show the exact key match, 1="actual int", not the toString key match, 1L="actual long"
    assertFailureKeys(e, "value of", "expected", "but was", "map was");
    assertFailureValue(e, "value of", "map.get(1)");
    assertFailureValue(e, "expected", "expected long");
    assertFailureValue(e, "but was", "actual long");
  }

  @Test
  public void doesNotContainEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    assertThat(actual).doesNotContainEntry("greg", "kick");
    assertThat(actual).doesNotContainEntry(null, null);
    assertThat(actual).doesNotContainEntry("kurt", null);
    assertThat(actual).doesNotContainEntry(null, "kluever");
  }

  @Test
  public void doesNotContainEntryFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).doesNotContainEntry("kurt", "kluever"));
    assertFailureKeys(e, "value of", "expected not to contain", "but was");
    assertFailureValue(e, "value of", "map.entrySet()");
    assertFailureValue(e, "expected not to contain", "kurt=kluever");
    assertFailureValue(e, "but was", "[kurt=kluever]");
  }

  @Test
  public void doesNotContainEntryOnNullMap() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that((Map<?, ?>) null).doesNotContainEntry("kurt", "kluever"));
    assertFailureKeys(e, "expected a map that does not contain entry", "but was");
  }

  @Test
  public void doesNotContainNullEntry() {
    Map<@Nullable String, @Nullable String> actual = new HashMap<>();
    actual.put(null, null);
    assertThat(actual).doesNotContainEntry("kurt", null);
    assertThat(actual).doesNotContainEntry(null, "kluever");
  }

  @Test
  public void doesNotContainNullEntryFailure() {
    Map<@Nullable String, @Nullable String> actual = new HashMap<>();
    actual.put(null, null);
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).doesNotContainEntry(null, null));
    assertFailureKeys(e, "value of", "expected not to contain", "but was");
    assertFailureValue(e, "value of", "map.entrySet()");
    assertFailureValue(e, "expected not to contain", "null=null");
    assertFailureValue(e, "but was", "[null=null]");
  }

  @Test
  public void failMapContainsKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).containsKey("b"));
    assertFailureKeys(e, "value of", "expected to contain", "but was", "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected to contain", "b");
    assertFailureValue(e, "but was", "[a]");
  }

  @Test
  public void failMapContainsKeyWithNull() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).containsKey(null));
    assertFailureKeys(e, "value of", "expected to contain", "but was", "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected to contain", "null");
    assertFailureValue(e, "but was", "[a]");
  }

  @Test
  public void failMapLacksKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).doesNotContainKey("a"));
    assertFailureKeys(e, "value of", "expected not to contain", "but was", "map was");
    assertFailureValue(e, "value of", "map.keySet()");
    assertFailureValue(e, "expected not to contain", "a");
    assertFailureValue(e, "but was", "[a]");
  }

  @Test
  public void containsKeyWithValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    assertThat(actual).containsEntry("a", "A");
  }

  @Test
  public void containsKeyWithNullValueNullExpected() {
    Map<String, @Nullable String> actual = new HashMap<>();
    actual.put("a", null);
    assertThat(actual).containsEntry("a", null);
  }

  @Test
  public void failMapContainsKeyWithValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsEntry("a", "a"));
    assertFailureValue(e, "value of", "map.get(a)");
    assertFailureValue(e, "expected", "a");
    assertFailureValue(e, "but was", "A");
    assertFailureValue(e, "map was", "{a=A}");
    assertThat(e).hasMessageThat().doesNotContain(KEY_IS_PRESENT_WITH_DIFFERENT_VALUE);
  }

  @Test
  public void failMapContainsKeyWithNullValuePresentExpected() {
    Map<String, @Nullable String> actual = new HashMap<>();
    actual.put("a", null);
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsEntry("a", "A"));
    assertFailureValue(e, "value of", "map.get(a)");
    assertFailureValue(e, "expected", "A");
    assertFailureValue(e, "but was", "null");
    assertFailureValue(e, "map was", "{a=null}");
    assertThat(e).hasMessageThat().contains(KEY_IS_PRESENT_WITH_DIFFERENT_VALUE);
  }

  @Test
  public void failMapContainsKeyWithPresentValueNullExpected() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).containsEntry("a", null));
    assertFailureValue(e, "value of", "map.get(a)");
    assertFailureValue(e, "expected", "null");
    assertFailureValue(e, "but was", "A");
    assertFailureValue(e, "map was", "{a=A}");
    assertThat(e).hasMessageThat().contains(KEY_IS_PRESENT_WITH_DIFFERENT_VALUE);
  }

  @Test
  public void comparingValuesUsing_containsEntry_success() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("def", 456);
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsExpectedKeyHasWrongValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsEntry("def", 123));
    assertFailureKeys(
        e, "for key", "expected value", "testing whether", "but got value", "full map");
    assertFailureValue(e, "for key", "def");
    assertFailureValue(e, "expected value", "123");
    assertFailureValue(e, "testing whether", "actual value parses to expected value");
    assertFailureValue(e, "but got value", "+456");
    assertFailureValue(e, "full map", "{abc=+123, def=+456}");
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsWrongKeyHasExpectedValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsEntry("xyz", 456));
    assertFailureKeys(
        e,
        "for key",
        "expected value",
        "testing whether",
        "but was missing",
        "other keys with matching values",
        "full map");
    assertFailureValue(e, "other keys with matching values", "[def]");
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsMissingExpectedKeyAndValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsEntry("xyz", 321));
    assertFailureKeys(
        e, "for key", "expected value", "testing whether", "but was missing", "full map");
  }

  @Test
  public void comparingValuesUsing_containsEntry_diffExpectedKeyHasWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("abc", 35, "def", 71);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(WITHIN_10_OF)
                    .containsEntry("def", 60));
    assertFailureKeys(
        e, "for key", "expected value", "testing whether", "but got value", "diff", "full map");
    assertFailureValue(e, "for key", "def");
    assertFailureValue(e, "expected value", "60");
    assertFailureValue(e, "but got value", "71");
    assertFailureValue(e, "diff", "11");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesFormatDiffExceptions() {
    Map<String, @Nullable Integer> actual = new LinkedHashMap<>();
    actual.put("abc", 35);
    actual.put("def", null);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(WITHIN_10_OF)
                    .containsEntry("def", 60));
    assertFailureKeys(
        e,
        "for key",
        "expected value",
        "testing whether",
        "but got value",
        "full map",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertThat(e)
        .factValue("first exception", 0)
        .startsWith(
            "compare(null, 60) threw"
                + " com.google.common.truth.TestCorrespondences$NullPointerExceptionFromWithin10Of");
    assertThat(e)
        .factValue("first exception", 1)
        .startsWith("formatDiff(null, 60) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesExceptions_expectedKeyHasWrongValue() {
    Map<Integer, @Nullable String> actual = new LinkedHashMap<>();
    actual.put(1, "one");
    actual.put(2, null);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
                    .containsEntry(2, "TWO"));
    // The test fails because the expected key has a null value which causes compare() to throw.
    // We should report that the key has the wrong value, and also that we saw an exception.
    assertFailureKeys(
        e,
        "for key",
        "expected value",
        "testing whether",
        "but got value",
        "full map",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(null, TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesExceptions_wrongKeyHasExpectedValue() {
    Map<Integer, @Nullable String> actual = new LinkedHashMap<>();
    actual.put(1, null);
    actual.put(2, "three");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
                    .containsEntry(3, "THREE"));
    // The test fails and does not contain the expected key, but does contain the expected value for
    // a different key. No reasonable implementation would find this value in the second entry
    // without hitting the exception from trying the first entry (which has a null value), so we
    // should report the exception as well.
    assertFailureKeys(
        e,
        "for key",
        "expected value",
        "testing whether",
        "but was missing",
        "other keys with matching values",
        "full map",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(null, THREE) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_successExcludedKeyHasWrongValues() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("def", 123);
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_successWrongKeyHasExcludedValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("xyz", 456);
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_successMissingExcludedKeyAndValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("xyz", 321);
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_failure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .doesNotContainEntry("def", 456));
    assertFailureKeys(e, "expected not to contain", "testing whether", "but contained", "full map");
    assertFailureValue(e, "expected not to contain", "def=456");
    assertFailureValue(e, "but contained", "def=+456");
    assertFailureValue(e, "full map", "{abc=+123, def=+456}");
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_handlesException() {
    Map<Integer, @Nullable String> actual = new LinkedHashMap<>();
    actual.put(1, "one");
    actual.put(2, null);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
                    .doesNotContainEntry(2, "TWO"));
    // This test would pass if compare(null, "TWO") returned false. But it actually throws, so the
    // test must fail.
    assertFailureKeys(
        e,
        "one or more exceptions were thrown while comparing values",
        "first exception",
        "expected not to contain",
        "testing whether",
        "found no match (but failing because of exception)",
        "full map");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(null, TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsExactly_success() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 456, "abc", 123);
  }

  @Test
  public void comparingValuesUsing_containsExactly_inOrder_success() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("abc", 123, "def", 456)
        .inOrder();
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsExtraEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactly("def", 456));
    assertFailureKeys(
        e,
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "abc");
    assertFailureValue(e, "unexpected value", "123");
    assertFailureValue(e, "expected", "{def=456}");
    assertFailureValue(e, "testing whether", "actual value parses to expected value");
    assertFailureValue(e, "but was", "{abc=123, def=456}");
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsMissingEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactly("def", 456, "xyz", 999, "abc", 123));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "xyz");
    assertFailureValue(e, "expected value", "999");
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsWrongKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactly("def", 456, "cab", 123));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "cab");
    assertFailureValue(e, "expected value", "123");
    assertFailureValueIndexed(e, "for key", 1, "abc");
    assertFailureValue(e, "unexpected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsWrongValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactly("def", 456, "abc", 321));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "abc");
    assertFailureValue(e, "expected value", "321");
    assertFailureValue(e, "but got value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactly_handlesExceptions() {
    Map<Integer, @Nullable String> actual = new LinkedHashMap<>();
    actual.put(1, "one");
    actual.put(2, null);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
                    .containsExactly(1, "ONE", 2, "TWO"));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(null, TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsExactly_inOrder_failsOutOfOrder() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactly("def", 456, "abc", 123)
                    .inOrder());
    assertFailureKeys(
        e, "entries match, but order was wrong", "expected", "testing whether", "but was");
    assertFailureValue(e, "expected", "{def=456, abc=123}");
    assertFailureValue(e, "but was", "{abc=123, def=456}");
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongValueTypeInActual() {
    ImmutableMap<String, Object> actual = ImmutableMap.of("abc", "123", "def", 456);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactly("def", 456, "abc", 123));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(456, 456) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongValueTypeInExpected() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactly("def", 456, "abc", 123L));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(123, 123) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_success() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_inOrder_success() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 123, "def", 456);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected)
        .inOrder();
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsExtraEntry() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactlyEntriesIn(expected));
    assertFailureKeys(
        e,
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "abc");
    assertFailureValue(e, "unexpected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsMissingEntry() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "xyz", 999, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactlyEntriesIn(expected));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "xyz");
    assertFailureValue(e, "expected value", "999");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsWrongKey() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "cab", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactlyEntriesIn(expected));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "cab");
    assertFailureValue(e, "expected value", "123");
    assertFailureValueIndexed(e, "for key", 1, "abc");
    assertFailureValue(e, "unexpected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 321);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactlyEntriesIn(expected));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "abc");
    assertFailureValue(e, "expected value", "321");
    assertFailureValue(e, "but got value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_diffMissingAndExtraAndWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 30, "def", 60, "ghi", 90);
    ImmutableMap<String, Integer> actual = ImmutableMap.of("abc", 35, "fed", 60, "ghi", 101);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(WITHIN_10_OF)
                    .containsExactlyEntriesIn(expected));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "diff",
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "ghi");
    assertFailureValueIndexed(e, "expected value", 0, "90");
    assertFailureValue(e, "but got value", "101");
    assertFailureValue(e, "diff", "11");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_handlesFormatDiffExceptions() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 30, "def", 60, "ghi", 90);
    Map<String, @Nullable Integer> actual = new LinkedHashMap<>();
    actual.put("abc", 35);
    actual.put("def", null);
    actual.put("ghi", 95);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(WITHIN_10_OF)
                    .containsExactlyEntriesIn(expected));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertThat(e)
        .factValue("first exception", 0)
        .startsWith(
            "compare(null, 60) threw"
                + " com.google.common.truth.TestCorrespondences$NullPointerExceptionFromWithin10Of");
    assertThat(e)
        .factValue("first exception", 1)
        .startsWith("formatDiff(null, 60) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_inOrder_failsOutOfOrder() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactlyEntriesIn(expected)
                    .inOrder());
    assertFailureKeys(
        e, "entries match, but order was wrong", "expected", "testing whether", "but was");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_empty() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of();
    ImmutableMap<String, String> actual = ImmutableMap.of();
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsEmpty() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of();
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactlyEntriesIn(expected));
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_wrongValueTypeInActual() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 123);
    ImmutableMap<String, Object> actual = ImmutableMap.of("abc", "123", "def", 456);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsExactlyEntriesIn(expected));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(456, 456) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_success() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 456, "abc", 123);
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_inOrder_success() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "ghi", "789", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("abc", 123, "def", 456)
        .inOrder();
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_failsMissingEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeast("def", 456, "xyz", 999, "abc", 123));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "xyz");
    assertFailureValue(e, "expected value", "999");
    assertFailureValue(e, "expected to contain at least", "{def=456, xyz=999, abc=123}");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_failsWrongKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeast("def", 456, "cab", 123));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "cab");
    assertFailureValue(e, "expected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_failsWrongValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeast("abc", 321));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "abc");
    assertFailureValue(e, "expected value", "321");
    assertFailureValue(e, "but got value", "123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_handlesExceptions() {
    Map<Integer, @Nullable String> actual = new LinkedHashMap<>();
    actual.put(1, "one");
    actual.put(2, null);
    actual.put(3, "three");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
                    .containsAtLeast(1, "ONE", 2, "TWO"));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(null, TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_inOrder_failsOutOfOrder() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeast("def", 456, "abc", 123)
                    .inOrder());
    assertFailureKeys(
        e,
        "required entries were all found, but order was wrong",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue(e, "expected to contain at least", "{def=456, abc=123}");
    assertFailureValue(e, "but was", "{abc=123, def=456, ghi=789}");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_wrongValueTypeInExpectedActual() {
    ImmutableMap<String, Object> actual = ImmutableMap.of("abc", "123", "def", 456);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeast("def", 456));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(456, 456) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_wrongValueTypeInUnexpectedActual_success() {
    ImmutableMap<String, Object> actual = ImmutableMap.of("abc", "123", "def", 456);
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("abc", 123);
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_wrongValueTypeInExpected() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeast("def", 456, "abc", 123L));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(123, 123) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_success() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_inOrder_success() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 123, "ghi", 789);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected)
        .inOrder();
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_failsMissingEntry() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "xyz", 999, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeastEntriesIn(expected));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "xyz");
    assertFailureValue(e, "expected value", "999");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_failsWrongKey() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "cab", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeastEntriesIn(expected));
    assertFailureKeys(
        e,
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "cab");
    assertFailureValue(e, "expected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_failsWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 321);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeastEntriesIn(expected));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue(e, "for key", "abc");
    assertFailureValue(e, "expected value", "321");
    assertFailureValue(e, "but got value", "123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_diffMissingAndWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 30, "def", 60, "ghi", 90);
    ImmutableMap<String, Integer> actual = ImmutableMap.of("abc", 35, "fed", 60, "ghi", 101);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(WITHIN_10_OF)
                    .containsAtLeastEntriesIn(expected));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "diff",
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValueIndexed(e, "for key", 0, "ghi");
    assertFailureValueIndexed(e, "expected value", 0, "90");
    assertFailureValue(e, "but got value", "101");
    assertFailureValue(e, "diff", "11");
    assertFailureValueIndexed(e, "for key", 1, "def");
    assertFailureValueIndexed(e, "expected value", 1, "60");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_handlesFormatDiffExceptions() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 30, "def", 60, "ghi", 90);
    Map<String, @Nullable Integer> actual = new LinkedHashMap<>();
    actual.put("abc", 35);
    actual.put("def", null);
    actual.put("ghi", 95);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(WITHIN_10_OF)
                    .containsAtLeastEntriesIn(expected));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertThat(e)
        .factValue("first exception", 0)
        .startsWith(
            "compare(null, 60) threw"
                + " com.google.common.truth.TestCorrespondences$NullPointerExceptionFromWithin10Of");
    assertThat(e)
        .factValue("first exception", 1)
        .startsWith("formatDiff(null, 60) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_inOrder_failsOutOfOrder() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("ghi", 789, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeastEntriesIn(expected)
                    .inOrder());
    assertFailureKeys(
        e,
        "required entries were all found, but order was wrong",
        "expected to contain at least",
        "testing whether",
        "but was");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_empty() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of();
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_wrongValueTypeInExpectedActual() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456);
    ImmutableMap<String, Object> actual = ImmutableMap.of("abc", "123", "def", 456);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
                    .containsAtLeastEntriesIn(expected));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThat(e)
        .factValue("first exception")
        .startsWith("compare(456, 456) threw java.lang.ClassCastException");
  }

  @Test
  public void
      comparingValuesUsing_containsAtLeastEntriesIn_wrongValueTypeInUnexpectedActual_success() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 123);
    ImmutableMap<String, Object> actual = ImmutableMap.of("abc", "123", "def", 456);
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
  }

  @Test
  public void formattingDiffsUsing_success() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("ghi", 300, "def", 200, "abc", 100);
    assertThat(actual)
        .formattingDiffsUsing(INT_DIFF_FORMATTER)
        .containsExactly("abc", 100, "def", 200, "ghi", 300);
  }

  @Test
  public void formattingDiffsUsing_failure() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("ghi", 300, "def", 201, "abc", 100);
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .formattingDiffsUsing(INT_DIFF_FORMATTER)
                    .containsExactly("abc", 100, "def", 200, "ghi", 300));
    assertFailureKeys(
        e,
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "diff",
        "---",
        "expected",
        "but was");
    assertFailureValue(e, "expected value", "200");
    assertFailureValue(e, "but got value", "201");
    assertFailureValue(e, "diff", "1");
  }
}
