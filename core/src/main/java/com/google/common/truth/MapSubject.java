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

import com.google.common.base.Objects;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

/**
 * Propositions for {@link Map} subjects.
 *
 * @author Christian Gruber
 * @author Kurt Alfred Kluever
 */
public class MapSubject extends Subject<MapSubject, Map<?, ?>> {
  MapSubject(FailureStrategy failureStrategy, @Nullable Map<?, ?> map) {
    super(failureStrategy, map);
  }

  /**
   * Fails if the subject is not equal to the given object.
   */
  @Override
  public void isEqualTo(@Nullable Object other) {
    if (!Objects.equal(getSubject(), other)) {
      if (other instanceof Map) {
        MapDifference<?, ?> diff = Maps.difference((Map<?, ?>) other, (Map<?, ?>) getSubject());
        String errorMsg = "The subject";
        if (!diff.entriesOnlyOnLeft().isEmpty()) {
          errorMsg += " is missing the following entries: " + diff.entriesOnlyOnLeft();
          if (!diff.entriesOnlyOnRight().isEmpty() || !diff.entriesDiffering().isEmpty()) {
            errorMsg += " and";
          }
        }
        if (!diff.entriesOnlyOnRight().isEmpty()) {
          errorMsg += " has the following extra entries: " + diff.entriesOnlyOnRight();
          if (!diff.entriesDiffering().isEmpty()) {
            errorMsg += " and";
          }
        }
        if (!diff.entriesDiffering().isEmpty()) {
          errorMsg += " has the following different entries: " + diff.entriesDiffering();
        }
        failWithRawMessage(
            "Not true that %s is equal to <%s>. " + errorMsg, getDisplaySubject(), other);
      } else {
        fail("is equal to", other);
      }
    }
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
  public void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize (%s) must be >= 0", expectedSize);
    int actualSize = getSubject().size();
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /**
   * Fails if the map does not contain the given key.
   */
  public void containsKey(@Nullable Object key) {
    if (!getSubject().containsKey(key)) {
      fail("contains key", key);
    }
  }

  /**
   * Fails if the map contains the given key.
   */
  public void doesNotContainKey(@Nullable Object key) {
    if (getSubject().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /**
   * Fails if the map does not contain the given entry.
   */
  public void containsEntry(@Nullable Object key, @Nullable Object value) {
    if (!getSubject().containsKey(key)){
      fail("contains entry", Maps.immutableEntry(key, value));
    }
    Object actualValue = getSubject().get(key);
    if ((actualValue == null && value != null) ||
            (actualValue != null && !actualValue.equals(value))) {
      fail("contains entry", Maps.immutableEntry(key, value));
    }
  }

  /**
   * Fails if the map contains the given entry.
   */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    Object actualValue = getSubject().get(key);
    if (getSubject().containsKey(key) && (actualValue == value || (actualValue != null && actualValue.equals(value)))) {
      fail("does not contain entry", Maps.immutableEntry(key, value));
    }
  }

  /**
   * Fails if the map is not empty.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactly() {
    return check().that(getSubject().entrySet()).containsExactly();
  }

  /**
   * Fails if the map does not contain exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   */
  // TODO(b/25744307): Can we add an error-prone check that rest.length % 2 == 0?
  @CanIgnoreReturnValue
  public Ordered containsExactly(@Nullable Object k0, @Nullable Object v0, Object... rest) {
    checkArgument(
        rest.length % 2 == 0,
        "There must be an equal number of key/value pairs "
            + "(i.e., the number of key/value parameters (%s) must be even).",
        rest.length + 2);

    Map<Object, Object> expectedMap = Maps.newLinkedHashMap();
    expectedMap.put(k0, v0);
    Multiset<Object> keys = LinkedHashMultiset.create();
    keys.add(k0);
    for (int i = 0; i < rest.length; i += 2) {
      Object key = rest[i];
      expectedMap.put(key, rest[i + 1]);
      keys.add(key);
    }
    checkArgument(
        keys.size() == expectedMap.size(),
        "Duplicate keys (%s) cannot be passed to containsExactly().",
        keys);
    return containsExactlyEntriesIn(expectedMap);
  }

  /**
   * Fails if the map does not contain exactly the given set of entries in the given map.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactlyEntriesIn(Map<?, ?> expectedMap) {
    return check().that(getSubject().entrySet()).containsExactlyElementsIn(expectedMap.entrySet());
  }
}
