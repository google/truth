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

import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

/**
 * Tests for Map Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
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
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is empty");
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
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is not empty");
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

  @Test public void mapHasValue() {
    assertThat(map(String.class, Object.class, "a", "A")).hasValue("A");
  }

  @Test public void mapHasValueWithNull() {
    assertThat(map(String.class, Object.class, "a", null)).hasValue(null);
  }

  @Test public void failMapHasValue() {
    try {
      assertThat(map(String.class, Object.class, "a", "A")).hasValue("B");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has value");
    }
  }

  @Test public void mapLacksValue() {
    assertThat(map(String.class, Object.class, "a", "A")).lacksValue("B");
  }

  @Test public void failMapLacksValue() {
    try {
      assertThat(map(String.class, Object.class, "a", "A")).lacksValue("A");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("lacks value");
    }
  }

  @SuppressWarnings("unchecked") // Want this to blow up if wrong.
  public static <K, V> Map<K, V> map(Class<K> keyClass, Class<V> valueClass, Object ... keyval) {
    Map<K, V> map = Maps.newHashMap();
    if (keyval.length % 2 != 0)
      throw new IllegalArgumentException("Wrong number of key/value pairs.");
    for (int i = 0; i < keyval.length ; i = i + 2) {
      map.put(keyClass.cast(keyval[i]), valueClass.cast(keyval[i + 1]));
    }
    return map;
  }
}
