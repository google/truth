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

import static com.google.common.truth.TestCorrespondences.CASE_INSENSITIVE_EQUALITY;
import static com.google.common.truth.TestCorrespondences.INT_DIFF_FORMATTER;
import static com.google.common.truth.TestCorrespondences.STRING_PARSES_TO_INTEGER_CORRESPONDENCE;
import static com.google.common.truth.TestCorrespondences.WITHIN_10_OF;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Map} subjects.
 *
 * @author Christian Gruber
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class MapSubjectTest extends BaseSubjectTestCase {

  @Test
  public void containsExactlyWithNullKey() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, "value");

    assertThat(actual).containsExactly(null, "value");
    assertThat(actual).containsExactly(null, "value").inOrder();
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();
  }

  @Test
  public void containsExactlyWithNullValue() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put("key", null);

    assertThat(actual).containsExactly("key", null);
    assertThat(actual).containsExactly("key", null).inOrder();
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();
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

    expectFailureWhenTestingThat(actual).containsExactly();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void containsExactlyEntriesInEmpty_fails() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1);

    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(ImmutableMap.of());
    assertFailureKeys("expected to be empty", "but was");
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

    try {
      assertThat(actual).containsExactly("jan", 1, "jan", 2, "jan", 3);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Duplicate keys ([jan x 3]) cannot be passed to containsExactly().");
    }
  }

  @Test
  public void containsExactlyMultipleDuplicateKeys() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    try {
      assertThat(actual).containsExactly("jan", 1, "jan", 1, "feb", 2, "feb", 2);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Duplicate keys ([jan x 2, feb x 2]) cannot be passed to containsExactly().");
    }
  }

  @Test
  public void containsExactlyExtraKey() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    expectFailureWhenTestingThat(actual).containsExactly("feb", 2, "jan", 1);
    assertFailureKeys(
        "unexpected keys", "for key", "unexpected value", "---", "expected", "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("unexpected value", "3");
    assertFailureValue("expected", "{feb=2, jan=1}");
    assertFailureValue("but was", "{jan=1, feb=2, march=3}");
  }

  @Test
  public void containsExactlyExtraKeyInOrder() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    expectFailureWhenTestingThat(actual).containsExactly("feb", 2, "jan", 1).inOrder();
    assertFailureKeys(
        "unexpected keys", "for key", "unexpected value", "---", "expected", "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("unexpected value", "3");
  }

  @Test
  public void containsExactlyMissingKey() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2);
    expectFailureWhenTestingThat(actual).containsExactly("jan", 1, "march", 3, "feb", 2);
    assertFailureKeys("missing keys", "for key", "expected value", "---", "expected", "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("expected value", "3");
  }

  @Test
  public void containsExactlyWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    expectFailureWhenTestingThat(actual).containsExactly("jan", 1, "march", 33, "feb", 2);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("expected value", "33");
    assertFailureValue("but got value", "3");
  }

  @Test
  public void containsExactlyWrongValueWithNull() {
    // Test for https://github.com/google/truth/issues/468
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    expectFailureWhenTestingThat(actual).containsExactly("jan", 1, "march", null, "feb", 2);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("expected value", "null");
    assertFailureValue("but got value", "3");
  }

  @Test
  public void containsExactlyExtraKeyAndMissingKey() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "march", 3);
    expectFailureWhenTestingThat(actual).containsExactly("jan", 1, "feb", 2);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed("for key", 0, "feb");
    assertFailureValue("expected value", "2");
    assertFailureValueIndexed("for key", 1, "march");
    assertFailureValue("unexpected value", "3");
  }

  @Test
  public void containsExactlyExtraKeyAndWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    expectFailureWhenTestingThat(actual).containsExactly("jan", 1, "march", 33);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "march");
    assertFailureValue("expected value", "33");
    assertFailureValue("but got value", "3");
    assertFailureValueIndexed("for key", 1, "feb");
    assertFailureValue("unexpected value", "2");
  }

  @Test
  public void containsExactlyMissingKeyAndWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "march", 3);
    expectFailureWhenTestingThat(actual).containsExactly("jan", 1, "march", 33, "feb", 2);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "march");
    assertFailureValueIndexed("expected value", 0, "33");
    assertFailureValue("but got value", "3");
    assertFailureValueIndexed("for key", 1, "feb");
    assertFailureValueIndexed("expected value", 1, "2");
  }

  @Test
  public void containsExactlyExtraKeyAndMissingKeyAndWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "march", 3);
    expectFailureWhenTestingThat(actual).containsExactly("march", 33, "feb", 2);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "march");
    assertFailureValueIndexed("expected value", 0, "33");
    assertFailureValue("but got value", "3");
    assertFailureValueIndexed("for key", 1, "feb");
    assertFailureValueIndexed("expected value", 1, "2");
    assertFailureValueIndexed("for key", 2, "jan");
    assertFailureValue("unexpected value", "1");
  }

  @Test
  public void containsExactlyNotInOrder() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();

    assertThat(actual).containsExactly("jan", 1, "march", 3, "feb", 2);
    expectFailureWhenTestingThat(actual).containsExactly("jan", 1, "march", 3, "feb", 2).inOrder();
    assertFailureKeys("entries match, but order was wrong", "expected", "but was");
    assertFailureValue("expected", "{jan=1, march=3, feb=2}");
    assertFailureValue("but was", "{jan=1, feb=2, march=3}");
  }

  @Test
  @SuppressWarnings("ShouldHaveEvenArgs")
  public void containsExactlyBadNumberOfArgs() {
    ImmutableMap<String, Integer> actual =
        ImmutableMap.of("jan", 1, "feb", 2, "march", 3, "april", 4, "may", 5);
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();

    try {
      assertThat(actual)
          .containsExactly("jan", 1, "feb", 2, "march", 3, "april", 4, "may", 5, "june", 6, "july");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "There must be an equal number of key/value pairs "
                  + "(i.e., the number of key/value parameters (13) must be even).");
    }
  }

  @Test
  public void containsExactlyWrongValue_sameToStringForValues() {
    expectFailureWhenTestingThat(ImmutableMap.of("jan", 1L, "feb", 2L))
        .containsExactly("jan", 1, "feb", 2);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "jan");
    assertFailureValueIndexed("expected value", 0, "1 (java.lang.Integer)");
    assertFailureValueIndexed("but got value", 0, "1 (java.lang.Long)");
    assertFailureValueIndexed("for key", 1, "feb");
    assertFailureValueIndexed("expected value", 1, "2 (java.lang.Integer)");
    assertFailureValueIndexed("but got value", 1, "2 (java.lang.Long)");
  }

  @Test
  public void containsExactlyWrongValue_sameToStringForKeys() {
    expectFailureWhenTestingThat(ImmutableMap.of(1L, "jan", 1, "feb"))
        .containsExactly(1, "jan", 1L, "feb");
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "1 (java.lang.Integer)");
    assertFailureValueIndexed("expected value", 0, "jan");
    assertFailureValueIndexed("but got value", 0, "feb");
    assertFailureValueIndexed("for key", 1, "1 (java.lang.Long)");
    assertFailureValueIndexed("expected value", 1, "feb");
    assertFailureValueIndexed("but got value", 1, "jan");
  }

  @Test
  public void containsExactlyExtraKeyAndMissingKey_failsWithSameToStringForKeys() {
    expectFailureWhenTestingThat(ImmutableMap.of(1L, "jan", 2, "feb"))
        .containsExactly(1, "jan", 2, "feb");
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed("for key", 0, "1 (java.lang.Integer)");
    assertFailureValue("expected value", "jan");
    assertFailureValueIndexed("for key", 1, "1 (java.lang.Long)");
    assertFailureValue("unexpected value", "jan");
  }

  @Test
  public void containsAtLeastWithNullKey() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, "value");
    actual.put("unexpectedKey", "unexpectedValue");
    Map<String, String> expected = Maps.newHashMap();
    expected.put(null, "value");

    assertThat(actual).containsAtLeast(null, "value");
    assertThat(actual).containsAtLeast(null, "value").inOrder();
    assertThat(actual).containsAtLeastEntriesIn(expected);
    assertThat(actual).containsAtLeastEntriesIn(expected).inOrder();
  }

  @Test
  public void containsAtLeastWithNullValue() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put("key", null);
    actual.put("unexpectedKey", "unexpectedValue");
    Map<String, String> expected = Maps.newHashMap();
    expected.put("key", null);

    assertThat(actual).containsAtLeast("key", null);
    assertThat(actual).containsAtLeast("key", null).inOrder();
    assertThat(actual).containsAtLeastEntriesIn(expected);
    assertThat(actual).containsAtLeastEntriesIn(expected).inOrder();
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

    try {
      assertThat(actual).containsAtLeast("jan", 1, "jan", 2, "jan", 3);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Duplicate keys ([jan x 3]) cannot be passed to containsAtLeast().");
    }
  }

  @Test
  public void containsAtLeastMultipleDuplicateKeys() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    try {
      assertThat(actual).containsAtLeast("jan", 1, "jan", 1, "feb", 2, "feb", 2);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Duplicate keys ([jan x 2, feb x 2]) cannot be passed to containsAtLeast().");
    }
  }

  @Test
  public void containsAtLeastMissingKey() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2);
    expectFailureWhenTestingThat(actual).containsAtLeast("jan", 1, "march", 3);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("expected value", "3");
    assertFailureValue("expected to contain at least", "{jan=1, march=3}");
  }

  @Test
  public void containsAtLeastWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    expectFailureWhenTestingThat(actual).containsAtLeast("jan", 1, "march", 33);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("expected value", "33");
    assertFailureValue("but got value", "3");
  }

  @Test
  public void containsAtLeastWrongValueWithNull() {
    // Test for https://github.com/google/truth/issues/468
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    expectFailureWhenTestingThat(actual).containsAtLeast("jan", 1, "march", null);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("expected value", "null");
    assertFailureValue("but got value", "3");
  }

  @Test
  public void containsAtLeastExtraKeyAndMissingKeyAndWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "march", 3);
    expectFailureWhenTestingThat(actual).containsAtLeast("march", 33, "feb", 2);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "march");
    assertFailureValueIndexed("expected value", 0, "33");
    assertFailureValue("but got value", "3");
    assertFailureValueIndexed("for key", 1, "feb");
    assertFailureValueIndexed("expected value", 1, "2");
  }

  @Test
  public void containsAtLeastNotInOrder() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    assertThat(actual).containsAtLeast("march", 3, "feb", 2);
    expectFailureWhenTestingThat(actual).containsAtLeast("march", 3, "feb", 2).inOrder();
    assertFailureKeys(
        "required entries were all found, but order was wrong",
        "expected to contain at least",
        "but was");
    assertFailureValue("expected to contain at least", "{march=3, feb=2}");
    assertFailureValue("but was", "{jan=1, feb=2, march=3}");
  }

  @Test
  @SuppressWarnings("ShouldHaveEvenArgs")
  public void containsAtLeastBadNumberOfArgs() {
    ImmutableMap<String, Integer> actual =
        ImmutableMap.of("jan", 1, "feb", 2, "march", 3, "april", 4, "may", 5);

    try {
      assertThat(actual)
          .containsAtLeast("jan", 1, "feb", 2, "march", 3, "april", 4, "may", 5, "june", 6, "july");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "There must be an equal number of key/value pairs "
                  + "(i.e., the number of key/value parameters (13) must be even).");
    }
  }

  @Test
  public void containsAtLeastWrongValue_sameToStringForValues() {
    expectFailureWhenTestingThat(ImmutableMap.of("jan", 1L, "feb", 2L, "mar", 3L))
        .containsAtLeast("jan", 1, "feb", 2);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "jan");
    assertFailureValueIndexed("expected value", 0, "1 (java.lang.Integer)");
    assertFailureValueIndexed("but got value", 0, "1 (java.lang.Long)");
    assertFailureValueIndexed("for key", 1, "feb");
    assertFailureValueIndexed("expected value", 1, "2 (java.lang.Integer)");
    assertFailureValueIndexed("but got value", 1, "2 (java.lang.Long)");
  }

  @Test
  public void containsAtLeastWrongValue_sameToStringForKeys() {
    expectFailureWhenTestingThat(ImmutableMap.of(1L, "jan", 1, "feb"))
        .containsAtLeast(1, "jan", 1L, "feb");
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "1 (java.lang.Integer)");
    assertFailureValueIndexed("expected value", 0, "jan");
    assertFailureValueIndexed("but got value", 0, "feb");
    assertFailureValueIndexed("for key", 1, "1 (java.lang.Long)");
    assertFailureValueIndexed("expected value", 1, "feb");
    assertFailureValueIndexed("but got value", 1, "jan");
  }

  @Test
  public void containsAtLeastExtraKeyAndMissingKey_failsWithSameToStringForKeys() {
    expectFailureWhenTestingThat(ImmutableMap.of(1L, "jan", 2, "feb"))
        .containsAtLeast(1, "jan", 2, "feb");
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue("for key", "1 (java.lang.Integer)");
    assertFailureValue("expected value", "jan");
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

    expectFailureWhenTestingThat(actual).isEqualTo(expectedMap);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "march");
    assertFailureValueIndexed("expected value", 0, "5");
    assertFailureValue("but got value", "3");
    assertFailureValueIndexed("for key", 1, "april");
    assertFailureValueIndexed("expected value", 1, "4");
    assertFailureValueIndexed("for key", 2, "feb");
    assertFailureValue("unexpected value", "2");
    assertFailureValue("expected", "{jan=1, april=4, march=5}");
    assertFailureValue("but was", "{jan=1, feb=2, march=3}");
  }

  @Test
  public void isEqualToFailureDiffering() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 4);

    expectFailureWhenTestingThat(actual).isEqualTo(expectedMap);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed("for key", 0, "march");
    assertFailureValue("expected value", "4");
    assertFailureValue("but got value", "3");
  }

  @Test
  public void isEqualToFailureExtra() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2);

    expectFailureWhenTestingThat(actual).isEqualTo(expectedMap);
    assertFailureKeys(
        "unexpected keys", "for key", "unexpected value", "---", "expected", "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("unexpected value", "3");
  }

  @Test
  public void isEqualToFailureMissing() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    expectFailureWhenTestingThat(actual).isEqualTo(expectedMap);
    assertFailureKeys("missing keys", "for key", "expected value", "---", "expected", "but was");
    assertFailureValue("for key", "march");
    assertFailureValue("expected value", "3");
  }

  @Test
  public void isEqualToFailureExtraAndMissing() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "mar", 3);

    expectFailureWhenTestingThat(actual).isEqualTo(expectedMap);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed("for key", 0, "mar");
    assertFailureValue("expected value", "3");
    assertFailureValueIndexed("for key", 1, "march");
    assertFailureValue("unexpected value", "3");
  }

  @Test
  public void isEqualToFailureDiffering_sameToString() {
    ImmutableMap<String, Number> actual =
        ImmutableMap.<String, Number>of("jan", 1, "feb", 2, "march", 3L);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    expectFailureWhenTestingThat(actual).isEqualTo(expectedMap);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed("for key", 0, "march");
    assertFailureValue("expected value", "3 (java.lang.Integer)");
    assertFailureValue("but got value", "3 (java.lang.Long)");
  }

  @Test
  public void isEqualToNonMap() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    expectFailureWhenTestingThat(actual).isEqualTo("something else");
    assertFailureKeys("expected", "but was");
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
    expectFailureWhenTestingThat(actual).isEqualTo(expected);
    // The exact message generated is unspecified.
  }

  @Test
  public void isEqualToActualNullOtherMap() {
    expectFailureWhenTestingThat(null).isEqualTo(ImmutableMap.of());
  }

  @Test
  public void isEqualToActualMapOtherNull() {
    expectFailureWhenTestingThat(ImmutableMap.of()).isEqualTo(null);
  }

  @Test
  public void isNotEqualTo() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> unexpected = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    expectFailureWhenTestingThat(actual).isNotEqualTo(unexpected);
  }

  @Test
  public void isEmpty() {
    ImmutableMap<String, String> actual = ImmutableMap.of();
    assertThat(actual).isEmpty();
  }

  @Test
  public void isEmptyWithFailure() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of(1, 5);
    expectFailureWhenTestingThat(actual).isEmpty();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void isNotEmpty() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of(1, 5);
    assertThat(actual).isNotEmpty();
  }

  @Test
  public void isNotEmptyWithFailure() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of();
    expectFailureWhenTestingThat(actual).isNotEmpty();
    assertFailureKeys("expected not to be empty");
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
    try {
      assertThat(ImmutableMap.of(1, 2)).hasSize(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void containsKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    assertThat(actual).containsKey("kurt");
  }

  @Test
  public void containsKeyFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    expectFailureWhenTestingThat(actual).containsKey("greg");
    assertFailureKeys("value of", "expected to contain", "but was", "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected to contain", "greg");
    assertFailureValue("but was", "[kurt]");
  }

  @Test
  public void containsKeyNullFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    expectFailureWhenTestingThat(actual).containsKey(null);
    assertFailureKeys("value of", "expected to contain", "but was", "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected to contain", "null");
    assertFailureValue("but was", "[kurt]");
  }

  @Test
  public void containsKey_failsWithSameToString() {
    expectFailureWhenTestingThat(ImmutableMap.of(1L, "value1", 2L, "value2", "1", "value3"))
        .containsKey(1);
    assertFailureKeys(
        "value of",
        "expected to contain",
        "an instance of",
        "but did not",
        "though it did contain",
        "full contents",
        "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected to contain", "1");
  }

  @Test
  public void containsKey_failsWithNullStringAndNull() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put("null", "value1");

    expectFailureWhenTestingThat(actual).containsKey(null);
    assertFailureKeys(
        "value of",
        "expected to contain",
        "an instance of",
        "but did not",
        "though it did contain",
        "full contents",
        "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected to contain", "null");
  }

  @Test
  public void containsNullKey() {
    Map<String, String> actual = Maps.newHashMap();
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
    expectFailureWhenTestingThat(actual).doesNotContainKey("kurt");
    assertFailureKeys("value of", "expected not to contain", "but was", "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected not to contain", "kurt");
    assertFailureValue("but was", "[kurt]");
  }

  @Test
  public void doesNotContainNullKey() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, "null");
    expectFailureWhenTestingThat(actual).doesNotContainKey(null);
    assertFailureKeys("value of", "expected not to contain", "but was", "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected not to contain", "null");
    assertFailureValue("but was", "[null]");
  }

  @Test
  public void containsEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    assertThat(actual).containsEntry("kurt", "kluever");
  }

  @Test
  public void containsEntryFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    expectFailureWhenTestingThat(actual).containsEntry("greg", "kick");
    assertFailureKeys("expected to contain entry", "but was");
    assertFailureValue("expected to contain entry", "greg=kick");
    assertFailureValue("but was", "{kurt=kluever}");
  }

  @Test
  public void containsEntry_failsWithSameToStringOfKey() {
    expectFailureWhenTestingThat(ImmutableMap.of(1L, "value1", 2L, "value2"))
        .containsEntry(1, "value1");
    assertFailureKeys(
        "expected to contain entry",
        "an instance of",
        "but did not",
        "though it did contain keys",
        "full contents");
    assertFailureValue("an instance of", "Map.Entry<java.lang.Integer, java.lang.String>");
    assertFailureValue("though it did contain keys", "[1] (java.lang.Long)");
  }

  @Test
  public void containsEntry_failsWithSameToStringOfValue() {
    // Does not contain the correct key, but does contain a value which matches by toString.
    expectFailureWhenTestingThat(ImmutableMap.of(1, "null")).containsEntry(2, null);
    assertFailureKeys(
        "expected to contain entry",
        "an instance of",
        "but did not",
        "though it did contain values",
        "full contents");
    assertFailureValue("an instance of", "Map.Entry<java.lang.Integer, null type>");
    assertFailureValue("though it did contain values", "[null] (java.lang.String)");
  }

  @Test
  public void containsNullKeyAndValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    expectFailureWhenTestingThat(actual).containsEntry(null, null);
    assertFailureKeys("expected to contain entry", "but was");
    assertFailureValue("expected to contain entry", "null=null");
    assertFailureValue("but was", "{kurt=kluever}");
  }

  @Test
  public void containsNullEntry() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, null);
    assertThat(actual).containsEntry(null, null);
  }

  @Test
  public void containsNullEntryValue() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, null);
    expectFailureWhenTestingThat(actual).containsEntry("kurt", null);
    assertFailureKeys(
        "expected to contain entry",
        "but did not",
        "though it did contain keys with that value",
        "full contents");
    assertFailureValue("expected to contain entry", "kurt=null");
    assertFailureValue("though it did contain keys with that value", "[null]");
  }

  private static final String KEY_IS_PRESENT_WITH_DIFFERENT_VALUE =
      "key is present but with a different value";

  @Test
  public void containsNullEntryKey() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, null);
    expectFailureWhenTestingThat(actual).containsEntry(null, "kluever");
    assertFailureValue("value of", "map.get(null)");
    assertFailureValue("expected", "kluever");
    assertFailureValue("but was", "null");
    assertFailureValue("map was", "{null=null}");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains(KEY_IS_PRESENT_WITH_DIFFERENT_VALUE);
  }

  @Test
  public void containsExactly_bothExactAndToStringKeyMatches_showsExactKeyMatch() {
    ImmutableMap<Number, String> actual = ImmutableMap.of(1, "actual int", 1L, "actual long");
    expectFailureWhenTestingThat(actual).containsEntry(1L, "expected long");
    // should show the exact key match, 1="actual int", not the toString key match, 1L="actual long"
    assertFailureKeys("value of", "expected", "but was", "map was");
    assertFailureValue("value of", "map.get(1)");
    assertFailureValue("expected", "expected long");
    assertFailureValue("but was", "actual long");
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
    expectFailureWhenTestingThat(actual).doesNotContainEntry("kurt", "kluever");
    assertFailureKeys("value of", "expected not to contain", "but was");
    assertFailureValue("value of", "map.entrySet()");
    assertFailureValue("expected not to contain", "kurt=kluever");
    assertFailureValue("but was", "[kurt=kluever]");
  }

  @Test
  public void doesNotContainNullEntry() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, null);
    assertThat(actual).doesNotContainEntry("kurt", null);
    assertThat(actual).doesNotContainEntry(null, "kluever");
  }

  @Test
  public void doesNotContainNullEntryFailure() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, null);
    expectFailureWhenTestingThat(actual).doesNotContainEntry(null, null);
    assertFailureKeys("value of", "expected not to contain", "but was");
    assertFailureValue("value of", "map.entrySet()");
    assertFailureValue("expected not to contain", "null=null");
    assertFailureValue("but was", "[null=null]");
  }

  @Test
  public void failMapContainsKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    expectFailureWhenTestingThat(actual).containsKey("b");
    assertFailureKeys("value of", "expected to contain", "but was", "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected to contain", "b");
    assertFailureValue("but was", "[a]");
  }

  @Test
  public void failMapContainsKeyWithNull() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    expectFailureWhenTestingThat(actual).containsKey(null);
    assertFailureKeys("value of", "expected to contain", "but was", "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected to contain", "null");
    assertFailureValue("but was", "[a]");
  }

  @Test
  public void failMapLacksKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    expectFailureWhenTestingThat(actual).doesNotContainKey("a");
    assertFailureKeys("value of", "expected not to contain", "but was", "map was");
    assertFailureValue("value of", "map.keySet()");
    assertFailureValue("expected not to contain", "a");
    assertFailureValue("but was", "[a]");
  }

  @Test
  public void containsKeyWithValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    assertThat(actual).containsEntry("a", "A");
  }

  @Test
  public void containsKeyWithNullValueNullExpected() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put("a", null);
    assertThat(actual).containsEntry("a", null);
  }

  @Test
  public void failMapContainsKeyWithValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    expectFailureWhenTestingThat(actual).containsEntry("a", "a");
    assertFailureValue("value of", "map.get(a)");
    assertFailureValue("expected", "a");
    assertFailureValue("but was", "A");
    assertFailureValue("map was", "{a=A}");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .doesNotContain(KEY_IS_PRESENT_WITH_DIFFERENT_VALUE);
  }

  @Test
  public void failMapContainsKeyWithNullValuePresentExpected() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put("a", null);
    expectFailureWhenTestingThat(actual).containsEntry("a", "A");
    assertFailureValue("value of", "map.get(a)");
    assertFailureValue("expected", "A");
    assertFailureValue("but was", "null");
    assertFailureValue("map was", "{a=null}");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains(KEY_IS_PRESENT_WITH_DIFFERENT_VALUE);
  }

  @Test
  public void failMapContainsKeyWithPresentValueNullExpected() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    expectFailureWhenTestingThat(actual).containsEntry("a", null);
    assertFailureValue("value of", "map.get(a)");
    assertFailureValue("expected", "null");
    assertFailureValue("but was", "A");
    assertFailureValue("map was", "{a=A}");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains(KEY_IS_PRESENT_WITH_DIFFERENT_VALUE);
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("def", 123);
    assertFailureKeys("for key", "expected value", "testing whether", "but got value", "full map");
    assertFailureValue("for key", "def");
    assertFailureValue("expected value", "123");
    assertFailureValue("testing whether", "actual value parses to expected value");
    assertFailureValue("but got value", "+456");
    assertFailureValue("full map", "{abc=+123, def=+456}");
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsWrongKeyHasExpectedValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("xyz", 456);
    assertFailureKeys(
        "for key",
        "expected value",
        "testing whether",
        "but was missing",
        "other keys with matching values",
        "full map");
    assertFailureValue("other keys with matching values", "[def]");
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsMissingExpectedKeyAndValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("xyz", 321);
    assertFailureKeys(
        "for key", "expected value", "testing whether", "but was missing", "full map");
  }

  @Test
  public void comparingValuesUsing_containsEntry_diffExpectedKeyHasWrongValue() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("abc", 35, "def", 71);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(WITHIN_10_OF)
        .containsEntry("def", 60);
    assertFailureKeys(
        "for key", "expected value", "testing whether", "but got value", "diff", "full map");
    assertFailureValue("for key", "def");
    assertFailureValue("expected value", "60");
    assertFailureValue("but got value", "71");
    assertFailureValue("diff", "11");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesFormatDiffExceptions() {
    Map<String, Integer> actual = new LinkedHashMap<>();
    actual.put("abc", 35);
    actual.put("def", null);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(WITHIN_10_OF)
        .containsEntry("def", 60);
    assertFailureKeys(
        "for key",
        "expected value",
        "testing whether",
        "but got value",
        "full map",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception",
        "additionally, one or more exceptions were thrown while formatting diffs",
        "first exception");
    assertThatFailure()
        .factValue("first exception", 0)
        .startsWith(
            "compare(null, 60) threw"
                + " com.google.common.truth.TestCorrespondences$NullPointerExceptionFromWithin10Of");
    assertThatFailure()
        .factValue("first exception", 1)
        .startsWith("formatDiff(null, 60) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesExceptions_expectedKeyHasWrongValue() {
    Map<Integer, String> actual = new LinkedHashMap<>();
    actual.put(1, "one");
    actual.put(2, null);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsEntry(2, "TWO");
    // The test fails because the expected key has a null value which causes compare() to throw.
    // We should report that the key has the wrong value, and also that we saw an exception.
    assertFailureKeys(
        "for key",
        "expected value",
        "testing whether",
        "but got value",
        "full map",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesExceptions_wrongKeyHasExpectedValue() {
    Map<Integer, String> actual = new LinkedHashMap<>();
    actual.put(1, null);
    actual.put(2, "three");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsEntry(3, "THREE");
    // The test fails and does not contain the expected key, but does contain the expected value for
    // a different key. No reasonable implementation would find this value in the second entry
    // without hitting the exception from trying the first entry (which has a null value), so we
    // should report the exception as well.
    assertFailureKeys(
        "for key",
        "expected value",
        "testing whether",
        "but was missing",
        "other keys with matching values",
        "full map",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThatFailure()
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("def", 456);
    assertFailureKeys("expected not to contain", "testing whether", "but contained", "full map");
    assertFailureValue("expected not to contain", "def=456");
    assertFailureValue("but contained", "def=+456");
    assertFailureValue("full map", "{abc=+123, def=+456}");
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_handlesException() {
    Map<Integer, String> actual = new LinkedHashMap<>();
    actual.put(1, "one");
    actual.put(2, null);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .doesNotContainEntry(2, "TWO");
    // This test would pass if compare(null, "TWO") returned false. But it actually throws, so the
    // test must fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing values",
        "first exception",
        "expected not to contain",
        "testing whether",
        "found no match (but failing because of exception)",
        "full map");
    assertThatFailure()
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 456);
    assertFailureKeys(
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("for key", "abc");
    assertFailureValue("unexpected value", "123");
    assertFailureValue("expected", "{def=456}");
    assertFailureValue("testing whether", "actual value parses to expected value");
    assertFailureValue("but was", "{abc=123, def=456}");
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsMissingEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 456, "xyz", 999, "abc", 123);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("for key", "xyz");
    assertFailureValue("expected value", "999");
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsWrongKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 456, "cab", 123);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "cab");
    assertFailureValue("expected value", "123");
    assertFailureValueIndexed("for key", 1, "abc");
    assertFailureValue("unexpected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsWrongValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 456, "abc", 321);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("for key", "abc");
    assertFailureValue("expected value", "321");
    assertFailureValue("but got value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactly_handlesExceptions() {
    Map<Integer, String> actual = new LinkedHashMap<>();
    actual.put(1, "one");
    actual.put(2, null);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsExactly(1, "ONE", 2, "TWO");
    assertFailureKeys(
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
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsExactly_inOrder_failsOutOfOrder() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 456, "abc", 123)
        .inOrder();
    assertFailureKeys(
        "entries match, but order was wrong", "expected", "testing whether", "but was");
    assertFailureValue("expected", "{def=456, abc=123}");
    assertFailureValue("but was", "{abc=123, def=456}");
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongValueTypeInActual() {
    ImmutableMap<String, Object> actual = ImmutableMap.<String, Object>of("abc", "123", "def", 456);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 456, "abc", 123);
    assertFailureKeys(
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
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(456, 456) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongValueTypeInExpected() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 456, "abc", 123L);
    assertFailureKeys(
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
    assertThatFailure()
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
        "unexpected keys",
        "for key",
        "unexpected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("for key", "abc");
    assertFailureValue("unexpected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsMissingEntry() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "xyz", 999, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("for key", "xyz");
    assertFailureValue("expected value", "999");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsWrongKey() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "cab", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "cab");
    assertFailureValue("expected value", "123");
    assertFailureValueIndexed("for key", 1, "abc");
    assertFailureValue("unexpected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 321);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("for key", "abc");
    assertFailureValue("expected value", "321");
    assertFailureValue("but got value", "123");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_diffMissingAndExtraAndWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 30, "def", 60, "ghi", 90);
    ImmutableMap<String, Integer> actual = ImmutableMap.of("abc", 35, "fed", 60, "ghi", 101);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(WITHIN_10_OF)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "ghi");
    assertFailureValueIndexed("expected value", 0, "90");
    assertFailureValue("but got value", "101");
    assertFailureValue("diff", "11");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_handlesFormatDiffExceptions() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 30, "def", 60, "ghi", 90);
    Map<String, Integer> actual = new LinkedHashMap<>();
    actual.put("abc", 35);
    actual.put("def", null);
    actual.put("ghi", 95);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(WITHIN_10_OF)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
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
    assertThatFailure()
        .factValue("first exception", 0)
        .startsWith(
            "compare(null, 60) threw"
                + " com.google.common.truth.TestCorrespondences$NullPointerExceptionFromWithin10Of");
    assertThatFailure()
        .factValue("first exception", 1)
        .startsWith("formatDiff(null, 60) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_inOrder_failsOutOfOrder() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected)
        .inOrder();
    assertFailureKeys(
        "entries match, but order was wrong", "expected", "testing whether", "but was");
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_wrongValueTypeInActual() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 123);
    ImmutableMap<String, Object> actual = ImmutableMap.<String, Object>of("abc", "123", "def", 456);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
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
    assertThatFailure()
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 456, "xyz", 999, "abc", 123);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("for key", "xyz");
    assertFailureValue("expected value", "999");
    assertFailureValue("expected to contain at least", "{def=456, xyz=999, abc=123}");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_failsWrongKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 456, "cab", 123);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("for key", "cab");
    assertFailureValue("expected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_failsWrongValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("abc", 321);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("for key", "abc");
    assertFailureValue("expected value", "321");
    assertFailureValue("but got value", "123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_handlesExceptions() {
    Map<Integer, String> actual = new LinkedHashMap<>();
    actual.put(1, "one");
    actual.put(2, null);
    actual.put(3, "three");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsAtLeast(1, "ONE", 2, "TWO");
    assertFailureKeys(
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
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_inOrder_failsOutOfOrder() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 456, "abc", 123)
        .inOrder();
    assertFailureKeys(
        "required entries were all found, but order was wrong",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("expected to contain at least", "{def=456, abc=123}");
    assertFailureValue("but was", "{abc=123, def=456, ghi=789}");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_wrongValueTypeInExpectedActual() {
    ImmutableMap<String, Object> actual = ImmutableMap.<String, Object>of("abc", "123", "def", 456);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 456);
    assertFailureKeys(
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
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(456, 456) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_wrongValueTypeInUnexpectedActual_success() {
    ImmutableMap<String, Object> actual = ImmutableMap.<String, Object>of("abc", "123", "def", 456);
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("abc", 123);
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_wrongValueTypeInExpected() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 456, "abc", 123L);
    assertFailureKeys(
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
    assertThatFailure()
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("for key", "xyz");
    assertFailureValue("expected value", "999");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_failsWrongKey() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "cab", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
        "missing keys",
        "for key",
        "expected value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("for key", "cab");
    assertFailureValue("expected value", "123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_failsWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 321);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("for key", "abc");
    assertFailureValue("expected value", "321");
    assertFailureValue("but got value", "123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_diffMissingAndWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 30, "def", 60, "ghi", 90);
    ImmutableMap<String, Integer> actual = ImmutableMap.of("abc", 35, "fed", 60, "ghi", 101);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(WITHIN_10_OF)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
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
    assertFailureValueIndexed("for key", 0, "ghi");
    assertFailureValueIndexed("expected value", 0, "90");
    assertFailureValue("but got value", "101");
    assertFailureValue("diff", "11");
    assertFailureValueIndexed("for key", 1, "def");
    assertFailureValueIndexed("expected value", 1, "60");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_handlesFormatDiffExceptions() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 30, "def", 60, "ghi", 90);
    Map<String, Integer> actual = new LinkedHashMap<>();
    actual.put("abc", 35);
    actual.put("def", null);
    actual.put("ghi", 95);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(WITHIN_10_OF)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
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
    assertThatFailure()
        .factValue("first exception", 0)
        .startsWith(
            "compare(null, 60) threw"
                + " com.google.common.truth.TestCorrespondences$NullPointerExceptionFromWithin10Of");
    assertThatFailure()
        .factValue("first exception", 1)
        .startsWith("formatDiff(null, 60) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_inOrder_failsOutOfOrder() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("ghi", 789, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456", "ghi", "789");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected)
        .inOrder();
    assertFailureKeys(
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
    ImmutableMap<String, Object> actual = ImmutableMap.<String, Object>of("abc", "123", "def", 456);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
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
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(456, 456) threw java.lang.ClassCastException");
  }

  @Test
  public void
      comparingValuesUsing_containsAtLeastEntriesIn_wrongValueTypeInUnexpectedActual_success() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("abc", 123);
    ImmutableMap<String, Object> actual = ImmutableMap.<String, Object>of("abc", "123", "def", 456);
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
    expectFailure
        .whenTesting()
        .that(actual)
        .formattingDiffsUsing(INT_DIFF_FORMATTER)
        .containsExactly("abc", 100, "def", 200, "ghi", 300);
    assertFailureKeys(
        "keys with wrong values",
        "for key",
        "expected value",
        "but got value",
        "diff",
        "---",
        "expected",
        "but was");
    assertFailureValue("expected value", "200");
    assertFailureValue("but got value", "201");
    assertFailureValue("diff", "1");
  }

  private MapSubject expectFailureWhenTestingThat(Map<?, ?> actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
