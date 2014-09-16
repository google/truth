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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Propositions for {@link Map} subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
public class MapSubject<S extends MapSubject<S, K, V, M>, K, V, M extends Map<K, V>>
    extends Subject<S, M> {

  private MapSubject(FailureStrategy failureStrategy, M map) {
    super(failureStrategy, map);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <K, V, M extends Map<K, V>>
      MapSubject<? extends MapSubject<?, K, V, M>, K, V, M> create(
          FailureStrategy failureStrategy, Map<K, V> map) {
    return new MapSubject(failureStrategy, map);
  }

  /**
   * Fails if the map is not empty.
   */
  public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  /**
   * Fails if the map is empty.
   */
  public void isNotEmpty() {
    if (getSubject().isEmpty()) {
      fail("is not empty");
    }
  }

  /**
   * Fails if the map does not have the given size.
   */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = getSubject().size();
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /**
   * Fails if the map does not contain the given key.
   */
  public void containsKey(Object key) {
    if (!getSubject().containsKey(key)) {
      fail("contains key", key);
    }
  }

  /**
   * Fails if the map contains the given key.
   */
  public void doesNotContainKey(Object key) {
    if (getSubject().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /**
   * Fails if the map does not contain the given entry.
   */
  public void containsEntry(Object key, Object value) {
    Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (!getSubject().entrySet().contains(entry)) {
      fail("contains entry", entry);
    }
  }

  /**
   * Fails if the map contains the given entry.
   */
  public void doesNotContainEntry(Object key, Object value) {
    Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (getSubject().entrySet().contains(entry)) {
      fail("does not contain entry", entry);
    }
  }

  // TODO(user): Get rid of everything below this line.

  /**
   * Fails if the map does not contain the given key.
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
   * Fails if the map contains the given key.
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
     * Fails if the map does not contain the given value.
     *
     * @deprecated Use {@link MapSubject#containsEntry(Object, Object)} instead.
     */
    @Deprecated
    void withValue(V value);
  }
}
