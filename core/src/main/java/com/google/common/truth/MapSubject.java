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

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

/**
 * Propositions for {@link Map} subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
public class MapSubject<S extends MapSubject<S, K, V, M>, K, V, M extends Map<K, V>>
    extends Subject<S, M> {

  public MapSubject(FailureStrategy failureStrategy, M map) {
    super(failureStrategy, map);
  }

  /**
   * Attests that the map contains no entries, or fails.
   */
  public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  /**
   * Attests that the map contains one or more entries, or fails.
   */
  public void isNotEmpty() {
    if (getSubject().isEmpty()) {
      fail("is not empty");
    }
  }

  /**
   * Attests that the map contains the given key.
   */
  public void containsKey(Object key) {
    if (!getSubject().containsKey(key)) {
      fail("contains key", key);
    }
  }

  /**
   * Attests that the map does not contain the given key.
   */
  public void doesNotContainKey(Object key) {
    if (getSubject().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /**
   * Attests that the map contains the given entry.
   */
  public void containsEntry(Object key, Object value) {
    if (!getSubject().entrySet().contains(Maps.immutableEntry(key, value))) {
      fail("contains entry", key, value);
    }
  }

  /**
   * Attests that the map does not contain the given entry.
   */
  public void doesNotContainEntry(Object key, Object value) {
    if (getSubject().entrySet().contains(Maps.immutableEntry(key, value))) {
      fail("does not contain entry", key, value);
    }
  }

  // TODO(user): Get rid of everything below this line.

  /**
   * Attests that the map contains the given key or fails.
   *
   * @deprecated Use {@link #containsKey(Object)} instead.
   */
  @Deprecated
  public WithValue<V> hasKey(final K key) {
    if (!getSubject().containsKey(key)) {
      fail("has key", key);
    }
    return new WithValue<V>() {
      @Override public void withValue(V expected) {
        V actual = getSubject().get(key);
        if ((actual == null && expected != null) ||
            !(actual == expected || actual.equals(expected))) {
          fail("has key/value pair", Arrays.asList(key, expected),
              "actually has key/value pair", Arrays.asList(key, actual));
        }
      }
    };
  }

  /**
   * Attests that the map does not contain the given key or fails.
   *
   * @deprecated Use {@link #doesNotContainKey(Object)} instead.
   */
  @Deprecated
  public void lacksKey(K key) {
    if (getSubject().containsKey(key)) {
      fail("lacks key", key);
    }
  }

  /**
   * Interface for fluent chaining of value-checking.
   *
   * @deprecated Use {@link MapSubject#containsEntry(Object, Object)} instead.
   */
  @Deprecated
  public interface WithValue<V> {
    /**
     * Attests that the map contains the given value for the given key.
     *
     * @deprecated Use {@link MapSubject#containsEntry(Object, Object)} instead.
     */
    @Deprecated
    void withValue(V value);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <K, V, M extends Map<K, V>> MapSubject<? extends MapSubject<?, K, V, M>, K, V, M> create(
      FailureStrategy failureStrategy, Map<K, V> map) {
    return new MapSubject(failureStrategy, map);
  }

}
