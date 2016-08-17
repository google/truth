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
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Propositions for {@link Map} subjects.
 *
 * @author Christian Gruber
 * @author Kurt Alfred Kluever
 */
public final class MapSubject extends Subject<MapSubject, Map<?, ?>> {
  MapSubject(FailureStrategy failureStrategy, @Nullable Map<?, ?> map) {
    super(failureStrategy, map);
  }

  /** Fails if the subject is not equal to the given object. */
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

  /** Fails if the map is not empty. */
  public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  /** Fails if the map is empty. */
  public void isNotEmpty() {
    if (getSubject().isEmpty()) {
      fail("is not empty");
    }
  }

  /** Fails if the map does not have the given size. */
  public void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize (%s) must be >= 0", expectedSize);
    int actualSize = getSubject().size();
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /** Fails if the map does not contain the given key. */
  public void containsKey(@Nullable Object key) {
    if (!getSubject().containsKey(key)) {
      fail("contains key", key);
    }
  }

  /** Fails if the map contains the given key. */
  public void doesNotContainKey(@Nullable Object key) {
    if (getSubject().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /** Fails if the map does not contain the given entry. */
  public void containsEntry(@Nullable Object key, @Nullable Object value) {
    Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (!getSubject().entrySet().contains(entry)) {
      if (getSubject().containsKey(key)) {
        failWithRawMessage(
            "Not true that %s contains entry <%s>. However, it has a mapping from <%s> to <%s>",
            getDisplaySubject(), entry, key, getSubject().get(key));
      }
      if (getSubject().containsValue(value)) {
        Set<Object> keys = new LinkedHashSet<Object>();
        for (Entry<?, ?> actualEntry : getSubject().entrySet()) {
          if (Objects.equal(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithRawMessage(
            "Not true that %s contains entry <%s>. "
                + "However, the following keys are mapped to <%s>: %s",
            getDisplaySubject(), entry, value, keys);
      }
      fail("contains entry", entry);
    }
  }

  /** Fails if the map contains the given entry. */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (getSubject().entrySet().contains(entry)) {
      fail("does not contain entry", entry);
    }
  }

  /** Fails if the map is not empty. */
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

  /** Fails if the map does not contain exactly the given set of entries in the given map. */
  @CanIgnoreReturnValue
  public Ordered containsExactlyEntriesIn(Map<?, ?> expectedMap) {
    return check().that(getSubject().entrySet()).containsExactlyElementsIn(expectedMap.entrySet());
  }

  /**
   * Starts a method chain for a test proposition in which the actual values (i.e. the values of the
   * {@link Map} under test) are compared to expected values using the given {@link Correspondence}.
   * The actual values must be of type {@code A}, the expected values must be of type {@code E}. The
   * proposition is actually executed by continuing the method chain. For example:<pre>   {@code
   *   assertThat(actualMap)
   *     .comparingValuesUsing(correspondence)
   *     .containsEntry(expectedKey, expectedValue);}</pre>
   * where {@code actualMap} is a {@code Map<?, A>} (or, more generally, a {@code Map<?, ? extends
   * A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and {@code expectedValue} is an
   * {@code E}.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual value that is not of type {@code A} or an expected value that is not of
   * type {@code E}.
   */
  public <A, E> UsingCorrespondence<A, E> comparingValuesUsing(
      Correspondence<A, E> correspondence) {
    return new UsingCorrespondence<A, E>(correspondence);
  }

  /**
   * A partially specified proposition in which the actual values (i.e. the values of the {@link
   * Map} under test) are compared to expected values using a {@link Correspondence}. The expected
   * values are of type {@code E}. Call methods on this object to actually execute the proposition.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   */
  public final class UsingCorrespondence<A, E> {

    private final Correspondence<A, E> correspondence;

    private UsingCorrespondence(Correspondence<A, E> correspondence) {
      this.correspondence = checkNotNull(correspondence);
    }

    /**
     * Fails if the map does not contain an entry with the given key and a value that corresponds to
     * the given value.
     */
    public void containsEntry(@Nullable Object expectedKey, @Nullable E expectedValue) {
      if (getSubject().containsKey(expectedKey)) {
        // Found matching key.
        A actualValue = getCastSubject().get(expectedKey);
        if (correspondence.compare(actualValue, expectedValue)) {
          // Found matching key and value. Test passes!
          return;
        }
        // Found matching key with non-matching value.
        failWithRawMessage(
            "Not true that %s contains an entry with key <%s> and a value that %s <%s>. "
                + "However, it has a mapping from that key to <%s>",
            getDisplaySubject(), expectedKey, correspondence, expectedValue, actualValue);
      } else {
        // Did not find matching key.
        Set<Object> keys = new LinkedHashSet<Object>();
        for (Entry<?, A> actualEntry : getCastSubject().entrySet()) {
          if (correspondence.compare(actualEntry.getValue(), expectedValue)) {
            keys.add(actualEntry.getKey());
          }
        }
        if (!keys.isEmpty()) {
          // Found matching values with non-matching keys.
          failWithRawMessage(
              "Not true that %s contains an entry with key <%s> and a value that %s <%s>. "
                  + "However, the following keys are mapped to such values: <%s>",
              getDisplaySubject(), expectedKey, correspondence, expectedValue, keys);
        } else {
          // Did not find matching key or value.
          failWithRawMessage(
              "Not true that %s contains an entry with key <%s> and a value that %s <%s>",
              getDisplaySubject(), expectedKey, correspondence, expectedValue);
        }
      }
    }

    /**
     * Fails if the map contains an entry with the given key and a value that corresponds to the
     * given value.
     */
    public void doesNotContainEntry(@Nullable Object excludedKey, @Nullable E excludedValue) {
      if (getSubject().containsKey(excludedKey)) {
        A actualValue = getCastSubject().get(excludedKey);
        if (correspondence.compare(actualValue, excludedValue)) {
          failWithRawMessage(
              "Not true that %s does not contain an entry with key <%s> and a value that %s <%s>. "
                  + "It maps that key to <%s>",
              getDisplaySubject(), excludedKey, correspondence, excludedValue, actualValue);
        }
      }
    }

    /** Fails if the map is not empty. */
    @CanIgnoreReturnValue
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private Ordered containsExactly() {
      throw new UnsupportedOperationException();
    }

    /**
     * Fails if the map does not contain exactly the given set of keys mapping to values that
     * correspond to the given values.
     *
     * <p>The values must all be of type {@code E}, and a {@link ClassCastException} will be thrown
     * if any other type is encountered.
     *
     * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
     * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
     */
    // TODO(b/25744307): Can we add an error-prone check that rest.length % 2 == 0?
    @CanIgnoreReturnValue
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private Ordered containsExactly(@Nullable Object k0, @Nullable E v0, Object... rest) {
      throw new UnsupportedOperationException();
    }

    /**
     * Fails if the map does not contain exactly the keys in the given map, mapping to values that
     * correspond to the values of the given map.
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private Ordered containsExactlyEntriesIn(Map<?, ? extends E> expectedMap) {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Map<?, A> getCastSubject() {
      return (Map<?, A>) getSubject();
    }
  }
}
