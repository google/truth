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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

/**
 * Tests for Map Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class MapTest {
  @Test
  public void mapIsEqualToPass() {
    ImmutableMap<String, Integer> actualMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "mar", 3);

    assertThat(actualMap).isEqualTo(actualMap);
  }

  @Test
  public void mapIsEqualToFailureExtraMissingAndDiffering() {
    ImmutableMap<String, Integer> actualMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "april", 4, "march", 5);

    try {
      assertThat(actualMap).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <{jan=1, feb=2, march=3}> is equal to <{jan=1, april=4, march=5}>. "
                  + "The subject is missing the following entries: {april=4} and "
                  + "has the following extra entries: {feb=2} and "
                  + "has the following different entries: {march=(5, 3)}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void mapIsEqualToFailureDiffering() {
    ImmutableMap<String, Integer> actualMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 4);

    try {
      assertThat(actualMap).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <{jan=1, feb=2, march=3}> is equal to <{jan=1, feb=2, march=4}>. "
                  + "The subject has the following different entries: {march=(4, 3)}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void namedMapIsEqualToFailureDiffering() {
    ImmutableMap<String, Integer> actualMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 4);

    try {
      assertThat(actualMap).named("foo").isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that foo (<{jan=1, feb=2, march=3}>) is equal to <{jan=1, feb=2, march=4}>."
                  + " The subject has the following different entries: {march=(4, 3)}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void mapIsEqualToFailureExtra() {
    ImmutableMap<String, Integer> actualMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2);

    try {
      assertThat(actualMap).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <{jan=1, feb=2, march=3}> is equal to <{jan=1, feb=2}>. "
                  + "The subject has the following extra entries: {march=3}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void mapIsEqualToFailureMissing() {
    ImmutableMap<String, Integer> actualMap = ImmutableMap.of("jan", 1, "feb", 2);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    try {
      assertThat(actualMap).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <{jan=1, feb=2}> is equal to <{jan=1, feb=2, march=3}>. "
                  + "The subject is missing the following entries: {march=3}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void mapIsEqualToFailureExtraAndMissing() {
    ImmutableMap<String, Integer> actualMap = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);
    ImmutableMap<String, Integer> expectedMap = ImmutableMap.of("jan", 1, "feb", 2, "mar", 3);

    try {
      assertThat(actualMap).isEqualTo(expectedMap);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <{jan=1, feb=2, march=3}> is equal to <{jan=1, feb=2, mar=3}>. "
                  + "The subject is missing the following entries: {mar=3} "
                  + "and has the following extra entries: {march=3}");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void mapIsNotEqualTo() {
    ImmutableMap<String, Integer> map = ImmutableMap.of("jan", 1, "feb", 2, "march", 3);

    try {
      assertThat(map).isNotEqualTo(map);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <{jan=1, feb=2, march=3}> is not equal to <{jan=1, feb=2, march=3}>");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void mapIsEmpty() {
    ImmutableMap<String, String> map = ImmutableMap.of();
    assertThat(map).isEmpty();
  }

  @Test
  public void mapIsEmptyWithFailure() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.of(1, 5);
    try {
      assertThat(map).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{1=5}> is empty");
    }
  }

  @Test
  public void mapIsNotEmpty() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.of(1, 5);
    assertThat(map).isNotEmpty();
  }

  @Test
  public void mapIsNotEmptyWithFailure() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.of();
    try {
      assertThat(map).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{}> is not empty");
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
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).containsKey("kurt");
  }

  @Test
  public void containsKeyFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).containsKey("greg");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{kurt=kluever}> contains key <greg>");
    }
  }

  @Test
  public void containsKeyNullFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).containsKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{kurt=kluever}> contains key <null>");
    }
  }

  @Test
  public void containsNullKey() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, "null");
    assertThat(map).containsKey(null);
  }

  @Test
  public void doesNotContainKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).doesNotContainKey("greg");
    assertThat(map).doesNotContainKey(null);
  }

  @Test
  public void doesNotContainKeyFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).doesNotContainKey("kurt");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{kurt=kluever}> does not contain key <kurt>");
    }
  }

  @Test
  public void doesNotContainNullKey() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, "null");
    try {
      assertThat(map).doesNotContainKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{null=null}> does not contain key <null>");
    }
  }

  @Test
  public void containsEntry() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).containsEntry("kurt", "kluever");
  }

  @Test
  public void containsEntryFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).containsEntry("greg", "kick");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{kurt=kluever}> contains entry <greg=kick>");
    }
  }

  @Test
  public void containsNullKeyAndValue() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).containsEntry(null, null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{kurt=kluever}> contains entry <null=null>");
    }
  }

  @Test
  public void containsNullEntry() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    assertThat(map).containsEntry(null, null);
  }

  @Test
  public void containsNullEntryValue() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    try {
      assertThat(map).containsEntry("kurt", null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{null=null}> contains entry <kurt=null>");
    }
  }

  @Test
  public void containsNullEntryKey() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    try {
      assertThat(map).containsEntry(null, "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{null=null}> contains entry <null=kluever>");
    }
  }

  @Test
  public void doesNotContainEntry() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).doesNotContainEntry("greg", "kick");
    assertThat(map).doesNotContainEntry(null, null);
    assertThat(map).doesNotContainEntry("kurt", null);
    assertThat(map).doesNotContainEntry(null, "kluever");
  }

  @Test
  public void doesNotContainEntryFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).doesNotContainEntry("kurt", "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <{kurt=kluever}> does not contain entry <kurt=kluever>");
    }
  }

  @Test
  public void doesNotContainNullEntry() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    assertThat(map).doesNotContainEntry("kurt", null);
    assertThat(map).doesNotContainEntry(null, "kluever");
  }

  @Test
  public void doesNotContainNullEntryFailure() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    try {
      assertThat(map).doesNotContainEntry(null, null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{null=null}> does not contain entry <null=null>");
    }
  }

  @Test
  public void mapContainsKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    assertThat(map).containsKey("a");
  }

  @Test
  public void failMapContainsKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).containsKey("b");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{a=A}> contains key <b>");
    }
  }

  @Test
  public void failMapContainsKeyWithNull() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).containsKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{a=A}> contains key <null>");
    }
  }

  @Test
  public void mapLacksKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    assertThat(map).doesNotContainKey("b");
  }

  @Test
  public void failMapLacksKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).doesNotContainKey("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{a=A}> does not contain key <a>");
    }
  }

  @Test
  public void mapContainsKeyWithValue() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    assertThat(map).containsEntry("a", "A");
  }

  @Test
  public void mapContainsKeyWithNullValueNullExpected() {
    Map<String, String> map = Maps.newHashMap();
    map.put("a", null);
    assertThat(map).containsEntry("a", null);
  }

  @Test
  public void failMapContainsKeyWithValue() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).containsEntry("a", "a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{a=A}> contains entry <a=a>");
    }
  }

  @Test
  public void failMapContainsKeyWithNullValuePresentExpected() {
    Map<String, String> map = Maps.newHashMap();
    map.put("a", null);
    try {
      assertThat(map).containsEntry("a", "A");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{a=null}> contains entry <a=A>");
    }
  }

  @Test
  public void failMapContainsKeyWithPresentValueNullExpected() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).containsEntry("a", null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{a=A}> contains entry <a=null>");
    }
  }
}
