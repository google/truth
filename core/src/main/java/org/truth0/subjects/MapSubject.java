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

import java.util.Map;

import org.truth0.FailureStrategy;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableList;

/**
 * @author Christian Gruber (cgruber@israfil.net)
 */
@GwtCompatible
public class MapSubject<S extends MapSubject<S, K, V, M>, K, V, M extends Map<K, V>> extends Subject<S, M> {

  public MapSubject(FailureStrategy failureStrategy, M map) {
    super(failureStrategy, map);
  }

  /**
   * Attests that the subject holds no objects, or fails.
   */
  public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  /**
   * Attests that the subject holds one or more objects, or fails
   */
  public void isNotEmpty() {
    if (getSubject().isEmpty()) {
      fail("is not empty");
    }
  }

  /**
   * Attests that the subject contains the provided key or fails.
   */
  public WithValue<V> hasKey(final K key) {
    if (!getSubject().containsKey(key)) {
      fail("has key", key);
    }
    return new WithValue<V>() {
      @Override public void withValue(V expected) {
        V actual = getSubject().get(key);
        if ((actual == null && key != null) ||
            !actual.equals(expected)) {
          fail("has key/value pair", ImmutableList.of(key, expected),
              "actually has key/value pair", ImmutableList.of(key, actual));
        }
      }
    };
  }

  public void lacksKey(K key) {
    if (getSubject().containsKey(key)) {
      fail("lacks key", key);
    }
  }

  public void hasValue(V key) {
    if (!getSubject().containsValue(key)) {
      fail("has value", key);
    }
  }

  public void lacksValue(V key) {
    if (getSubject().containsValue(key)) {
      fail("lacks value", key);
    }
  }

  public interface WithValue<V> {
    void withValue(V value);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <K, V, M extends Map<K, V>> MapSubject<? extends MapSubject<?, K, V, M>, K, V, M> create(
      FailureStrategy failureStrategy, Map<K, V> map) {
    return new MapSubject(failureStrategy, map);
  }

}
