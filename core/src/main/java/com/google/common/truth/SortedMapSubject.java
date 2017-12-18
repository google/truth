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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ForwardingSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nullable;

/**
 * Tests for {@link SortedMap} and {@link NavigableMap} objects. This class supports assertions
 * based on {@code NavigableMap}'s API even if the subject only implements {@code SortedMap}.
 */
// TODO(diamondm): identify use-cases for assertions on other NavigableMap methods, such as
// ceilingKey(), and propose an API to support such assertions.
public final class SortedMapSubject extends MapSubject {
  private final NavigableMap<?, ?> actualAsNavigableMap;

  SortedMapSubject(FailureMetadata metadata, SortedMap<?, ?> map) {
    super(metadata, map);
    actualAsNavigableMap = map == null ? null : SortedMapAsNavigableMap.wrapIfNecessary(map);
  }

  @Override
  public SortedMapSubject named(String format, Object... args) {
    super.named(format, args);
    return this;
  }

  /** Fails if the map's first key is not equal to the given key. */
  public void hasFirstKey(@Nullable Object key) {
    if (actualAsNavigableMap().isEmpty()) {
      fail("has first key", key);
      return;
    }

    if (!Objects.equal(actualAsNavigableMap().firstKey(), key)) {
      if (actualAsNavigableMap().containsKey(key)) {
        failWithRawMessage(
            "Not true that %s has first key <%s>. "
                + "It does contain this key, but the first key is <%s>",
            actualAsString(), key, actualAsNavigableMap().firstKey());
        return;
      }
      failWithRawMessage(
          "Not true that %s has first key <%s>. "
              + "It does not contain this key, and the first key is <%s>",
          actualAsString(), key, actualAsNavigableMap().firstKey());
    }
  }

  /** Fails if the map's first key/value pair is not equal to the given entry. */
  public void hasFirstEntry(@Nullable Object key, @Nullable Object value) {
    Entry<Object, Object> expectedEntry = Maps.immutableEntry(key, value);
    if (actualAsNavigableMap().isEmpty()) {
      fail("has first entry", expectedEntry);
      return;
    }

    Entry<?, ?> actualFirstEntry = actualAsNavigableMap().firstEntry();
    if (!Objects.equal(actualFirstEntry, expectedEntry)) {
      Object actualFirstKey = actualFirstEntry.getKey();
      if (actualAsNavigableMap().entrySet().contains(expectedEntry)) {
        failWithRawMessage(
            "Not true that %s has first entry <%s>. "
                + "It does contain this entry, but the first entry is <%s>",
            actualAsString(), expectedEntry, actualFirstEntry);
      } else if (Objects.equal(actualFirstKey, key)) {
        failWithRawMessage(
            "Not true that %s has first entry <%s>, the first value is <%s>",
            actualAsString(), expectedEntry, actualFirstEntry.getValue());
      } else if (Objects.equal(actualFirstEntry.getValue(), value)) {
        failWithRawMessage(
            "Not true that %s has first entry <%s>, the first key is <%s>",
            actualAsString(), expectedEntry, actualFirstKey);
      } else if (actualAsNavigableMap().containsKey(key)) {
        failWithRawMessage(
            "Not true that %s has first entry <%s>. It does contain this key, "
                + "but the key is mapped to <%s>, and the first entry is <%s>",
            actualAsString(), expectedEntry, actualAsNavigableMap().get(key), actualFirstEntry);
      } else if (actualAsNavigableMap().containsValue(value)) {
        Set<Object> keys = new LinkedHashSet<>();
        for (Entry<?, ?> actualEntry : actualAsNavigableMap().entrySet()) {
          if (Objects.equal(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithRawMessage(
            "Not true that %s has first entry <%s>. It does contain this value, but the value is "
                + "mapped from the keys <%s>, and the first entry is <%s>",
            actualAsString(), expectedEntry, keys, actualFirstEntry);
      } else {
        failWithRawMessage(
            "Not true that %s has first entry <%s>. "
                + "It does not contain this entry, and the first entry is <%s>",
            actualAsString(), expectedEntry, actualFirstEntry);
      }
    }
  }

  /** Fails if the map's last key is not equal to the given key. */
  public void hasLastKey(@Nullable Object key) {
    if (actualAsNavigableMap().isEmpty()) {
      fail("has last key", key);
      return;
    }

    if (!Objects.equal(actualAsNavigableMap().lastKey(), key)) {
      if (actualAsNavigableMap().containsKey(key)) {
        failWithRawMessage(
            "Not true that %s has last key <%s>. "
                + "It does contain this key, but the last key is <%s>",
            actualAsString(), key, actualAsNavigableMap().lastKey());
        return;
      }
      failWithRawMessage(
          "Not true that %s has last key <%s>. "
              + "It does not contain this key, and the last key is <%s>",
          actualAsString(), key, actualAsNavigableMap().lastKey());
    }
  }

  /** Fails if the map's last key/value pair is not equal to the given entry. */
  public void hasLastEntry(@Nullable Object key, @Nullable Object value) {
    Entry<Object, Object> expectedEntry = Maps.immutableEntry(key, value);
    if (actualAsNavigableMap().isEmpty()) {
      fail("has last entry", expectedEntry);
      return;
    }

    Entry<?, ?> actualLastEntry = actualAsNavigableMap().lastEntry();
    if (!Objects.equal(actualLastEntry, expectedEntry)) {
      Object actualLastKey = actualLastEntry.getKey();
      if (actualAsNavigableMap().entrySet().contains(expectedEntry)) {
        failWithRawMessage(
            "Not true that %s has last entry <%s>. "
                + "It does contain this entry, but the last entry is <%s>",
            actualAsString(), expectedEntry, actualLastEntry);
      } else if (Objects.equal(actualLastKey, key)) {
        failWithRawMessage(
            "Not true that %s has last entry <%s>, the last value is <%s>",
            actualAsString(), expectedEntry, actualLastEntry.getValue());
      } else if (Objects.equal(actualLastEntry.getValue(), value)) {
        failWithRawMessage(
            "Not true that %s has last entry <%s>, the last key is <%s>",
            actualAsString(), expectedEntry, actualLastKey);
      } else if (actualAsNavigableMap().containsKey(key)) {
        failWithRawMessage(
            "Not true that %s has last entry <%s>. It does contain this key, "
                + "but the key is mapped to <%s>, and the last entry is <%s>",
            actualAsString(), expectedEntry, actualAsNavigableMap().get(key), actualLastEntry);
      } else if (actualAsNavigableMap().containsValue(value)) {
        Set<Object> keys = new LinkedHashSet<>();
        for (Entry<?, ?> actualEntry : actualAsNavigableMap().entrySet()) {
          if (Objects.equal(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithRawMessage(
            "Not true that %s has last entry <%s>. It does contain this value, but the value is "
                + "mapped from the keys <%s>, and the last entry is <%s>",
            actualAsString(), expectedEntry, keys, actualLastEntry);
      } else {
        failWithRawMessage(
            "Not true that %s has last entry <%s>. "
                + "It does not contain this entry, and the last entry is <%s>",
            actualAsString(), expectedEntry, actualLastEntry);
      }
    }
  }

  /**
   * Provides access to the actual value via {@link NavigableMap}'s API. This may or may be the same
   * object as returned by {@link #actual}, therefore you should avoid identity (e.g. {@code ==}) or
   * type (e.g. {@code instanceof}) assertions on this object.
   */
  private NavigableMap<?, ?> actualAsNavigableMap() {
    return actualAsNavigableMap;
  }

  /**
   * A view into a {@link SortedMap} as a {@link NavigableMap}, enabling Truth to support assertions
   * on {@code NavigableMap}'s API even if the user only has a {@code SortedMap}. For now only the
   * functionality needed for the existing assertions has been implemented. Reference {@link
   * com.google.common.collect.ForwardingNavigableMap}'s behavior when implementing additional
   * methods.
   *
   * <p>TODO(diamondm): consider moving this to com.google.common.collect if it's ever fully
   * implemented.
   */
  private static class SortedMapAsNavigableMap<K, V> extends ForwardingSortedMap<K, V>
      implements NavigableMap<K, V> {
    private final SortedMap<K, V> delegate;

    static <K, V> NavigableMap<K, V> wrapIfNecessary(SortedMap<K, V> map) {
      if (map instanceof NavigableMap) {
        return (NavigableMap<K, V>) map;
      }
      return new SortedMapAsNavigableMap<>(map);
    }

    SortedMapAsNavigableMap(SortedMap<K, V> delegate) {
      this.delegate = checkNotNull(delegate);
    }

    @Override
    protected SortedMap<K, V> delegate() {
      return delegate;
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public K ceilingKey(K key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Entry<K, V> firstEntry() {
      Iterator<Entry<K, V>> entryIterator = delegate().entrySet().iterator();
      return entryIterator.hasNext() ? entryIterator.next() : null;
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public K floorKey(K key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public K higherKey(K key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Entry<K, V> lastEntry() {
      try {
        K lastKey = delegate().lastKey();
        return Iterables.getOnlyElement(delegate().tailMap(lastKey).entrySet());
      } catch (NoSuchElementException e) {
        return null;
      }
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public K lowerKey(K key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
      // TODO(diamondm): can use (the to-be-implemented) SortedSetAsNavigableSet class
      throw new UnsupportedOperationException();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<K, V> subMap(
        K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      throw new UnsupportedOperationException();
    }
  }
}
