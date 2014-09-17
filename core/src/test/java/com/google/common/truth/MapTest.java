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

  @Test public void mapIsEmpty() {
    ImmutableMap<String, String> map = ImmutableMap.of();
    assertThat(map).isEmpty();
  }

  @Test public void mapIsEmptyWithFailure() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.of(1, 5);
    try {
      assertThat(map).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <{1=5}> is empty");
    }
  }

  @Test public void mapIsNotEmpty() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.of(1, 5);
    assertThat(map).isNotEmpty();
  }

  @Test public void mapIsNotEmptyWithFailure() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.of();
    try {
      assertThat(map).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <{}> is not empty");
    }
  }

  @Test public void hasSize() {
    assertThat(ImmutableMap.of(1, 2, 3, 4)).hasSize(2);
  }

  @Test public void hasSizeZero() {
    assertThat(ImmutableMap.of()).hasSize(0);
  }

  @Test public void hasSizeNegative() {
    try {
      assertThat(ImmutableMap.of(1, 2)).hasSize(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test public void containsKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).containsKey("kurt");
  }

  @Test public void containsKeyFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).containsKey("greg");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <{kurt=kluever}> contains key <greg>");
    }
  }

  @Test public void containsKeyNullFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).containsKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <{kurt=kluever}> contains key <null>");
    }
  }

  @Test public void containsNullKey() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, "null");
    assertThat(map).containsKey(null);
  }

  @Test public void doesNotContainKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).doesNotContainKey("greg");
    assertThat(map).doesNotContainKey(null);
  }

  @Test public void doesNotContainKeyFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).doesNotContainKey("kurt");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <{kurt=kluever}> does not contain key <kurt>");
    }
  }

  @Test public void doesNotContainNullKey() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, "null");
    try {
      assertThat(map).doesNotContainKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <{null=null}> does not contain key <null>");
    }
  }

  @Test public void containsEntry() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).containsEntry("kurt", "kluever");
  }

  @Test public void containsEntryFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).containsEntry("greg", "kick");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <{kurt=kluever}> contains entry <greg=kick>");
    }
  }

  @Test public void containsNullKeyAndValue() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).containsEntry(null, null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <{kurt=kluever}> contains entry <null=null>");
    }
  }

  @Test public void containsNullEntry() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    assertThat(map).containsEntry(null, null);
  }

  @Test public void containsNullEntryValue() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    try {
      assertThat(map).containsEntry("kurt", null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <{null=null}> contains entry <kurt=null>");
    }
  }

  @Test public void containsNullEntryKey() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    try {
      assertThat(map).containsEntry(null, "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <{null=null}> contains entry <null=kluever>");
    }
  }

  @Test public void doesNotContainEntry() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).doesNotContainEntry("greg", "kick");
    assertThat(map).doesNotContainEntry(null, null);
    assertThat(map).doesNotContainEntry("kurt", null);
    assertThat(map).doesNotContainEntry(null, "kluever");
  }

  @Test public void doesNotContainEntryFailure() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    try {
      assertThat(map).doesNotContainEntry("kurt", "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <{kurt=kluever}> does not contain entry <kurt=kluever>");
    }
  }

  @Test public void doesNotContainNullEntry() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    assertThat(map).doesNotContainEntry("kurt", null);
    assertThat(map).doesNotContainEntry(null, "kluever");
  }

  @Test public void doesNotContainNullEntryFailure() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    try {
      assertThat(map).doesNotContainEntry(null, null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <{null=null}> does not contain entry <null=null>");
    }
  }

  @Test public void mapHasKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    assertThat(map).hasKey("a");
  }

  @Test public void failMapHasKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).hasKey("b");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key");
    }
  }

  @Test public void failMapHasKeyWithNull() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).hasKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key <null>");
    }
  }

  @Test public void mapLacksKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    assertThat(map).lacksKey("b");
  }

  @Test public void failMapLacksKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).lacksKey("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("lacks key");
    }
  }

  @Test public void mapHasKeyWithValue() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    assertThat(map).hasKey("a").withValue("A");
  }

  @Test public void mapHasKeyWithNullValueNullExpected() {
    Map<String, String> map = Maps.newHashMap();
    map.put("a", null);
    assertThat(map).hasKey("a").withValue(null);
  }

  @Test public void failMapHasKeyWithValue() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).hasKey("a").withValue("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key/value pair");
    }
  }

  @Test public void failMapHasKeyWithNullValuePresentExpected() {
    Map<String, String> map = Maps.newHashMap();
    map.put("a", null);
    try {
      assertThat(map).hasKey("a").withValue("A");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key/value pair");
    }
  }

  @Test public void failMapHasKeyWithPresentValueNullExpected() {
    ImmutableMap<String, String> map = ImmutableMap.of("a", "A");
    try {
      assertThat(map).hasKey("a").withValue(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key/value pair");
    }
  }
}
