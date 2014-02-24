/*
 * Copyright (c) 2013 Google, Inc.
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

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Multimap;

import org.truth0.FailureStrategy;
import org.truth0.subjects.CollectionSubject.Has;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A subject with propositions for {@link Multimap}
 * @author Christian Gruber (cgruber@google.com)
 */
@GwtCompatible
public class MultimapSubject<S extends MultimapSubject<S, K, V, M>, K, V, M extends Multimap<K, V>>
    extends Subject<S, M> {

  public MultimapSubject(FailureStrategy failureStrategy, M map) {
    super(failureStrategy, map);
  }

  /**
   * Attests that the subject holds no key/value pairs, or fails.
   */
  public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  /**
   * Attests that the subject holds at least one key/value pair, or fails.
   */
  public void isNotEmpty() {
    if (getSubject().isEmpty()) {
      fail("is not empty");
    }
  }

  /**
   * A part of the fluent chain which returns a CollectionSubject wrapping the value
   * collection for the provided key.
   */
  public CollectionSubject<?, V, Collection<V>> valuesForKey(final K key) {
    return CollectionSubject.create(failureStrategy, getSubject().get(key));
  }

  /**
   * Attests that the subject contains the provided key or fails.
   */
  public void hasKey(final K key) {
    if (!getSubject().containsKey(key)) {
      fail("has key", key);
    }
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

  public Has<Map.Entry<K, V>, Collection<Map.Entry<K, V>>> has() {
    return CollectionSubject.create(failureStrategy, getSubject().entries()).has();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <K, V, M extends Multimap<K, V>>
      MultimapSubject<? extends MultimapSubject<?, K, V, M>, K, V, M> create(
          FailureStrategy failureStrategy, Multimap<K, V> map) {
    return new MultimapSubject(failureStrategy, map);
  }

  public static <K,V> Entry<K,V> entry(final K key, final V value) {
    return new Entry<K, V>() {
      @Override public K getKey() {
        return key;
      }

      @Override public V getValue() {
        return value;
      }

      @Override public V setValue(V arg0) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
