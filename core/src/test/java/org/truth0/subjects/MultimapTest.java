/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0.subjects;

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Map Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class MultimapTest {

  @Test public void mapIsEmpty() {
    ASSERT.that(multimap(String.class, String.class)).isEmpty();
  }

  @Test public void mapIsEmptyWithFailure() {
    try {
      ASSERT.that(multimap(Integer.class, Integer.class, 1, 5)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is empty");
    }
  }

  @Test public void mapIsNotEmpty() {
    ASSERT.that(multimap(Integer.class, Integer.class, 1, 5)).isNotEmpty();
  }

  @Test public void mapIsNotEmptyWithFailure() {
    try {
      ASSERT.that(multimap(String.class, String.class)).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is not empty");
    }
  }

  @Test public void mapHasKey() {
    ASSERT.that(multimap(String.class, Object.class, "a", "A")).hasKey("a");
  }

  @Test public void failMapHasKey() {
    try {
      ASSERT.that(multimap(String.class, Object.class, "a", "A")).hasKey("b");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has key");
    }
  }

  @Test public void failMapHasKeyWithNull() {
    try {
      ASSERT.that(multimap(String.class, Object.class, "a", "A")).hasKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has key <null>");
    }
  }

  @Test public void mapLacksKey() {
    ASSERT.that(multimap(String.class, Object.class, "a", "A")).lacksKey("b");
  }

  @Test public void failMapLacksKey() {
    try {
      ASSERT.that(multimap(String.class, Object.class, "a", "A")).lacksKey("a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("lacks key");
    }
  }

  @Test public void mapHasKeyWithValue() {
    ASSERT.that(multimap(String.class, Object.class, "a", String.class))
        .whoseValuesForKey("a").has().exactly(String.class);
  }

  @Test public void mapHasKeyWithNullValueNullExpected() {
    ASSERT.that(multimap(String.class, Object.class, "a", null))
        .whoseValuesForKey("a").has().exactly(null);
  }

  @Test public void failMapHasKeyWithValue() {
    try {
      ASSERT.that(multimap(String.class, Object.class, "a", String.class))
          .whoseValuesForKey("a").has().exactly(Integer.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has key/value pair");
    }
  }

  @Test public void failMapHasKeyWithNullValuePresentExpected() {
    try {
      ASSERT.that(multimap(String.class, Object.class, "a", null))
          .whoseValuesForKey("a").has().exactly(Integer.class);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has key/value pair");
    }
  }

  @Test public void failMapHasKeyWithPresentValueNullExpected() {
    try {
      ASSERT.that(multimap(String.class, Object.class, "a", String.class))
          .whoseValuesForKey("a").has().exactly(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has key/value pair");
    }
  }

  @Test public void mapHasValue() {
    ASSERT.that(multimap(String.class, Object.class, "a", "A")).hasValue("A");
  }

  @Test public void mapHasValueWithNull() {
    ASSERT.that(multimap(String.class, Object.class, "a", null)).hasValue(null);
  }

  @Test public void failMapHasValue() {
    try {
      ASSERT.that(multimap(String.class, Object.class, "a", "A")).hasValue("B");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has value");
    }
  }

  @Test public void mapLacksValue() {
    ASSERT.that(multimap(String.class, Object.class, "a", "A")).lacksValue("B");
  }

  @Test public void failMapLacksValue() {
    try {
      ASSERT.that(multimap(String.class, Object.class, "a", "A")).lacksValue("A");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("lacks value");
    }
  }


  @SuppressWarnings("unchecked") // Want this to blow up if wrong.
  public static <K, V> Multimap<K, V> multimap(Class<K> keyClass, Class<V> valueClass,
      Object ... keyval) {
    Multimap<K, V> map = HashMultimap.create();
    if (keyval.length % 2 != 0)
      throw new IllegalArgumentException("Wrong number of key/value pairs.");
    for (int i = 0; i < keyval.length ; i = i + 2) {
      map.put(keyClass.cast(keyval[i]), valueClass.cast(keyval[i + 1]));
    }
    return map;
  }
}
