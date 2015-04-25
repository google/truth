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
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

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
        failWithRawMessage("Not true that %s is equal to <%s>. " + errorMsg,
            getDisplaySubject(), other);
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
    Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (!getSubject().entrySet().contains(entry)) {
      fail("contains entry", entry);
    }
  }

  /**
   * Fails if the map contains the given entry.
   */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (getSubject().entrySet().contains(entry)) {
      fail("does not contain entry", entry);
    }
  }
}
