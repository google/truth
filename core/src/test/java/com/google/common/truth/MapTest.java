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
    assertThat(map(String.class, String.class)).isEmpty();
  }

  @Test public void mapIsEmptyWithFailure() {
    try {
      assertThat(map(Integer.class, Integer.class, 1, 5)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <{1=5}> is empty");
    }
  }

  @Test public void mapIsNotEmpty() {
    assertThat(map(Integer.class, Integer.class, 1, 5)).isNotEmpty();
  }

  @Test public void mapIsNotEmptyWithFailure() {
    try {
      assertThat(map(String.class, String.class)).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <{}> is not empty");
    }
  }

  @Test public void containsKey() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).containsKey("kurt");

    try {
      assertThat(map).containsKey("greg");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("contains key <greg>");
    }

    try {
      assertThat(map).containsKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("contains key <null>");
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

    try {
      assertThat(map).doesNotContainKey("kurt");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("does not contain key <kurt>");
    }
  }

  @Test public void doesNotContainNullKey() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, "null");

    try {
      assertThat(map).doesNotContainKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("does not contain key <null>");
    }
  }

  @Test public void containsEntry() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).containsEntry("kurt", "kluever");

    try {
      assertThat(map).containsEntry("greg", "kick");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("contains entry <greg> <kick>");
    }

    try {
      assertThat(map).containsEntry(null, null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("contains entry <null> <null>");
    }
  }

  @Test public void containsNullEntry() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    assertThat(map).containsEntry(null, null);

    try {
      assertThat(map).containsEntry("kurt", null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("contains entry <kurt> <null>");
    }

    try {
      assertThat(map).containsEntry(null, "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("contains entry <null> <kluever>");
    }
  }

  @Test public void doesNotContainEntry() {
    ImmutableMap<String, String> map = ImmutableMap.of("kurt", "kluever");
    assertThat(map).doesNotContainEntry("greg", "kick");
    assertThat(map).doesNotContainEntry(null, null);
    assertThat(map).doesNotContainEntry("kurt", null);
    assertThat(map).doesNotContainEntry(null, "kluever");

    try {
      assertThat(map).doesNotContainEntry("kurt", "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("does not contain entry <kurt> <kluever>");
    }
  }

  @Test public void doesNotContainNullEntry() {
    Map<String, String> map = Maps.newHashMap();
    map.put(null, null);
    assertThat(map).doesNotContainEntry("kurt", null);
    assertThat(map).doesNotContainEntry(null, "kluever");

    try {
      assertThat(map).doesNotContainEntry(null, null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("does not contain entry <null> <null>");
    }
  }

  @Test public void mapHasKey() {
    assertThat(map(String.class, Object.class, "a", "A")).hasKey("a");
  }

  @Test public void failMapHasKey() {
    try {
      assertThat(map(String.class, Object.class, "a", "A")).hasKey("b");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key");
    }
  }

  @Test public void failMapHasKeyWithNull() {
    try {
      assertThat(map(String.class, Object.class, "a", "A")).hasKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key <null>");
    }
  }

  @Test public void mapLacksKey() {
    assertThat(map(String.class, Object.class, "a", "A")).lacksKey("b");
  }

  @Test public void failMapLacksKey() {
    try {
      assertThat(map(String.class, Object.class, "a", "A")).lacksKey("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("lacks key");
    }
  }

  @Test public void mapHasKeyWithValue() {
    assertThat(map(String.class, Object.class, "a", String.class))
      .hasKey("a").withValue(String.class);
  }

  @Test public void mapHasKeyWithNullValueNullExpected() {
    assertThat(map(String.class, Object.class, "a", null))
    .hasKey("a").withValue(null);
  }

  @Test public void failMapHasKeyWithValue() {
    try {
      assertThat(map(String.class, Object.class, "a", String.class))
          .hasKey("a").withValue(Integer.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key/value pair");
    }
  }

  @Test public void failMapHasKeyWithNullValuePresentExpected() {
    try {
      assertThat(map(String.class, Object.class, "a", null))
          .hasKey("a").withValue(Integer.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key/value pair");
    }
  }

  @Test public void failMapHasKeyWithPresentValueNullExpected() {
    try {
      assertThat(map(String.class, Object.class, "a", String.class))
          .hasKey("a").withValue(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has key/value pair");
    }
  }

  @SuppressWarnings("unchecked") // Want this to blow up if wrong.
  private static <K, V> Map<K, V> map(Class<K> keyClass, Class<V> valueClass, Object ... keyval) {
    Map<K, V> map = Maps.newHashMap();
    if (keyval.length % 2 != 0) {
      throw new IllegalArgumentException("Wrong number of key/value pairs.");
    }
    for (int i = 0; i < keyval.length ; i = i + 2) {
      map.put(keyClass.cast(keyval[i]), valueClass.cast(keyval[i + 1]));
    }
    return map;
  }
}
