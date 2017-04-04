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

import static com.google.common.truth.IterableSubjectTest.STRING_PARSES_TO_INTEGER_CORRESPONDENCE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
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
public class MapSubjectTest {
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

    assertThat(actual).containsExactly("jan", 1, "feb", 2, "march", 3);
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
  public void containsExactlyMissingEntry() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();

    try {
      assertThat(actual).containsExactly("jan", 1, "feb", 2);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[jan=1, feb=2, march=3]> contains exactly <[jan=1, feb=2]>. "
                  + "It has unexpected items <[march=3]>");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void containsExactlyMissingEntryInOrder() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();

    try {
      assertThat(actual).containsExactly("feb", 2, "jan", 1).inOrder();
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[jan=1, feb=2, march=3]> contains exactly <[feb=2, jan=1]>. "
                  + "It has unexpected items <[march=3]>");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void containsExactlyNotInOrder() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    assertThat(actual).containsExactlyEntriesIn(actual);
    assertThat(actual).containsExactlyEntriesIn(actual).inOrder();

    assertThat(actual).containsExactly("jan", 1, "march", 3, "feb", 2);
    try {
      assertThat(actual).containsExactly("jan", 1, "march", 3, "feb", 2).inOrder();
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[jan=1, feb=2, march=3]> contains exactly these elements in order "
                  + "<[jan=1, march=3, feb=2]>");
      return;
    }
    fail("Should have thrown.");
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
  public void isEqualToPass() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    assertThat(actual).isEqualTo(expectedMap);
  }

  @Test
  public void isEqualToFailureExtraMissingAndDiffering() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "april", 4, "march", 5);

    try {
      assertThat(actual).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{jan=1, feb=2, march=3}> is equal to <{jan=1, april=4, march=5}>. "
                  + "The subject is missing the following entries: {april=4} and "
                  + "has the following extra entries: {feb=2} and "
                  + "has the following different entries: {march=(5, 3)}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void isEqualToFailureDiffering() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 4);

    try {
      assertThat(actual).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{jan=1, feb=2, march=3}> is equal to <{jan=1, feb=2, march=4}>. "
                  + "The subject has the following different entries: {march=(4, 3)}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void namedMapIsEqualToFailureDiffering() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 4);

    try {
      assertThat(actual).named("foo").isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that foo (<{jan=1, feb=2, march=3}>) is equal to <{jan=1, feb=2, march=4}>."
                  + " The subject has the following different entries: {march=(4, 3)}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void isEqualToFailureExtra() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2);

    try {
      assertThat(actual).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{jan=1, feb=2, march=3}> is equal to <{jan=1, feb=2}>. "
                  + "The subject has the following extra entries: {march=3}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void isEqualToFailureMissing() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    try {
      assertThat(actual).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{jan=1, feb=2}> is equal to <{jan=1, feb=2, march=3}>. "
                  + "The subject is missing the following entries: {march=3}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void isEqualToFailureExtraAndMissing() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "mar", 3);

    try {
      assertThat(actual).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{jan=1, feb=2, march=3}> is equal to <{jan=1, feb=2, mar=3}>. "
                  + "The subject is missing the following entries: {mar=3} "
                  + "and has the following extra entries: {march=3}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void isNotEqualTo() {
    ImmutableMap<String, Integer> actual = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> unexpected = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    try {
      assertThat(actual).isNotEqualTo(unexpected);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{jan=1, feb=2, march=3}> is not equal to <{jan=1, feb=2, march=3}>");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void isEmpty() {
    ImmutableMap<String, String> actual = ImmutableMap.of();
    assertThat(actual).isEmpty();
  }

  @Test
  public void isEmptyWithFailure() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of(1, 5);
    try {
      assertThat(actual).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("Not true that <{1=5}> is empty");
    }
  }

  @Test
  public void isNotEmpty() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of(1, 5);
    assertThat(actual).isNotEmpty();
  }

  @Test
  public void isNotEmptyWithFailure() {
    ImmutableMap<Integer, Integer> actual = ImmutableMap.of();
    try {
      assertThat(actual).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("Not true that <{}> is not empty");
    }
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
    try {
      assertThat(actual).containsKey("greg");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <{kurt=kluever}> contains key <greg>");
    }
  }

  @Test
  public void containsKeyNullFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(actual).containsKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <{kurt=kluever}> contains key <null>");
    }
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
    try {
      assertThat(actual).doesNotContainKey("kurt");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <{kurt=kluever}> does not contain key <kurt>");
    }
  }

  @Test
  public void doesNotContainNullKey() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, "null");
    try {
      assertThat(actual).doesNotContainKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <{null=null}> does not contain key <null>");
    }
  }

  @Test
  public void containsEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    assertThat(actual).containsEntry("kurt", "kluever");
  }

  @Test
  public void containsEntryFailure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(actual).containsEntry("greg", "kick");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <{kurt=kluever}> contains entry <greg=kick>");
    }
  }

  @Test
  public void containsNullKeyAndValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(actual).containsEntry(null, null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <{kurt=kluever}> contains entry <null=null>");
    }
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
    try {
      assertThat(actual).containsEntry("kurt", null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{null=null}> contains entry <kurt=null>. "
                  + "However, the following keys are mapped to <null>: [null]");
    }
  }

  @Test
  public void containsNullEntryKey() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put(null, null);
    try {
      assertThat(actual).containsEntry(null, "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{null=null}> contains entry <null=kluever>. "
                  + "However, it has a mapping from <null> to <null>");
    }
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
    try {
      assertThat(actual).doesNotContainEntry("kurt", "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <{kurt=kluever}> does not contain entry <kurt=kluever>");
    }
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
    try {
      assertThat(actual).doesNotContainEntry(null, null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <{null=null}> does not contain entry <null=null>");
    }
  }

  @Test
  public void failMapContainsKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    try {
      assertThat(actual).containsKey("b");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("Not true that <{a=A}> contains key <b>");
    }
  }

  @Test
  public void failMapContainsKeyWithNull() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    try {
      assertThat(actual).containsKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("Not true that <{a=A}> contains key <null>");
    }
  }

  @Test
  public void failMapLacksKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    try {
      assertThat(actual).doesNotContainKey("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("Not true that <{a=A}> does not contain key <a>");
    }
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
    try {
      assertThat(actual).containsEntry("a", "a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{a=A}> contains entry <a=a>. "
                  + "However, it has a mapping from <a> to <A>");
    }
  }

  @Test
  public void failMapContainsKeyWithNullValuePresentExpected() {
    Map<String, String> actual = Maps.newHashMap();
    actual.put("a", null);
    try {
      assertThat(actual).containsEntry("a", "A");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{a=null}> contains entry <a=A>. "
                  + "However, it has a mapping from <a> to <null>");
    }
  }

  @Test
  public void failMapContainsKeyWithPresentValueNullExpected() {
    ImmutableMap<String, String> actual = ImmutableMap.of("a", "A");
    try {
      assertThat(actual).containsEntry("a", null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{a=A}> contains entry <a=null>. "
                  + "However, it has a mapping from <a> to <A>");
    }
  }

  @Test
  public void comparingValuesUsing_containsEntry_success() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("def", 456);
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsExpectedKeyHasWrongValues() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsEntry("def", 123);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{abc=+123, def=+456}> contains an entry with "
                  + "key <def> and a value that parses to <123>. "
                  + "However, it has a mapping from that key to <+456>");
    }
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsWrongKeyHasExpectedValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsEntry("xyz", 456);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{abc=+123, def=+456}> contains an entry with "
                  + "key <xyz> and a value that parses to <456>. "
                  + "However, the following keys are mapped to such values: <[def]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsMissingExpectedKeyAndValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsEntry("xyz", 321);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{abc=+123, def=+456}> contains an entry with "
                  + "key <xyz> and a value that parses to <321>");
    }
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
  public void comparingValuesUsing_doesNotContainEntry_failsMissingExcludedKeyAndValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("xyz", 321);
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_failure() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "+123", "def", "+456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .doesNotContainEntry("def", 456);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <{abc=+123, def=+456}> does not contain an entry with "
                  + "key <def> and a value that parses to <456>. It maps that key to <+456>");
    }
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
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactly("def", 456);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains exactly one element that has a key "
                  + "that is equal to and a value that parses to the key and value of each "
                  + "element of <[def=456]>. It has unexpected elements <[abc=123]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsMissingEntry() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactly("def", 456, "xyz", 999, "abc", 123);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains exactly one element that has a key "
                  + "that is equal to and a value that parses to the key and value of each "
                  + "element of <[def=456, xyz=999, abc=123]>. It is missing an element that has a "
                  + "key that is equal to and a value that parses to the key and value of "
                  + "<xyz=999>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsWrongKey() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactly("def", 456, "cab", 123);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains exactly one element that has a key "
                  + "that is equal to and a value that parses to the key and value of each "
                  + "element of <[def=456, cab=123]>. It is missing an element that has a "
                  + "key that is equal to and a value that parses to the key and value of "
                  + "<cab=123> and has unexpected elements <[abc=123]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsWrongValue() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactly("def", 456, "abc", 321);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains exactly one element that has a key "
                  + "that is equal to and a value that parses to the key and value of each "
                  + "element of <[def=456, abc=321]>. It is missing an element that has a "
                  + "key that is equal to and a value that parses to the key and value of "
                  + "<abc=321> and has unexpected elements <[abc=123]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactly_inOrder_failsOutOfOrder() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactly("def", 456, "abc", 123)
          .inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains, in order, exactly one element that has"
                  + " a key that is equal to and a value that parses to the key and value of each"
                  + " element of <[def=456, abc=123]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongValueTypeInActual() {
    ImmutableMap<String, Object> actual = ImmutableMap.<String, Object>of("abc", "123", "def", 456);
    MapSubject.UsingCorrespondence<String, Integer> intermediate =
        assertThat(actual).comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE);
    try {
      intermediate.containsExactly("def", 456, "abc", 123);
      fail("Should have thrown.");
    } catch (ClassCastException expected) {
    }
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongValueTypeInExpected() {
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    MapSubject.UsingCorrespondence<String, Integer> intermediate =
        assertThat(actual).comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE);
    try {
      intermediate.containsExactly("def", 456, "abc", 123L);
      fail("Should have thrown.");
    } catch (ClassCastException expected) {
    }
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
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyEntriesIn(expected);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains exactly one element that has a key "
                  + "that is equal to and a value that parses to the key and value of each "
                  + "element of <[def=456]>. It has unexpected elements <[abc=123]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsMissingEntry() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "xyz", 999, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyEntriesIn(expected);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains exactly one element that has a key "
                  + "that is equal to and a value that parses to the key and value of each "
                  + "element of <[def=456, xyz=999, abc=123]>. It is missing an element that has a "
                  + "key that is equal to and a value that parses to the key and value of "
                  + "<xyz=999>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsWrongKey() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "cab", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyEntriesIn(expected);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains exactly one element that has a key "
                  + "that is equal to and a value that parses to the key and value of each "
                  + "element of <[def=456, cab=123]>. It is missing an element that has a "
                  + "key that is equal to and a value that parses to the key and value of "
                  + "<cab=123> and has unexpected elements <[abc=123]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsWrongValue() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 321);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyEntriesIn(expected);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains exactly one element that has a key "
                  + "that is equal to and a value that parses to the key and value of each "
                  + "element of <[def=456, abc=321]>. It is missing an element that has a "
                  + "key that is equal to and a value that parses to the key and value of "
                  + "<abc=321> and has unexpected elements <[abc=123]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_inOrder_failsOutOfOrder() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 123);
    ImmutableMap<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyEntriesIn(expected)
          .inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[abc=123, def=456]> contains, in order, exactly one element that has"
                  + " a key that is equal to and a value that parses to the key and value of each"
                  + " element of <[def=456, abc=123]>");
    }
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_wrongValueTypeInActual() {
    ImmutableMap<String, Integer> expected = ImmutableMap.of("def", 456, "abc", 123);
    ImmutableMap<String, Object> actual = ImmutableMap.<String, Object>of("abc", "123", "def", 456);
    MapSubject.UsingCorrespondence<String, Integer> intermediate =
        assertThat(actual).comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE);
    try {
      intermediate.containsExactlyEntriesIn(expected);
      fail("Should have thrown.");
    } catch (ClassCastException e) {
      // expected
    }
  }
}
