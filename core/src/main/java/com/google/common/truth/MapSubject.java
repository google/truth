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
import static com.google.common.truth.SubjectUtils.countDuplicatesAndAddTypeInfo;
import static com.google.common.truth.SubjectUtils.hasMatchingToStringPair;
import static com.google.common.truth.SubjectUtils.objectToTypeName;
import static com.google.common.truth.SubjectUtils.retainMatchingToString;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.LinkedHashSet;
import java.util.List;
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
// Can't be final since SortedMapSubject extends it
public class MapSubject extends Subject<MapSubject, Map<?, ?>> {
  MapSubject(FailureMetadata metadata, @Nullable Map<?, ?> map) {
    super(metadata, map);
  }

  /** Fails if the subject is not equal to the given object. */
  @Override
  public void isEqualTo(@Nullable Object other) {
    if (!Objects.equal(actual(), other)) {
      if (other instanceof Map) {
        boolean mapEquals = containsExactlyEntriesInAnyOrder((Map<?, ?>) other, "is equal to");
        if (mapEquals) {
          failWithRawMessage(
              "Not true that %s is equal to <%s>. It is equal according to the contract of "
                  + "Map.equals(Object), but this implementation returned false",
              actualAsString(), other);
        }
      } else {
        fail("is equal to", other);
      }
    }
  }

  /** Fails if the map is not empty. */
  public void isEmpty() {
    if (!actual().isEmpty()) {
      fail("is empty");
    }
  }

  /** Fails if the map is empty. */
  public void isNotEmpty() {
    if (actual().isEmpty()) {
      fail("is not empty");
    }
  }

  /** Fails if the map does not have the given size. */
  public void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize (%s) must be >= 0", expectedSize);
    int actualSize = actual().size();
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /** Fails if the map does not contain the given key. */
  public void containsKey(@Nullable Object key) {
    if (!actual().containsKey(key)) {
      List<Object> keyList = Lists.newArrayList(key);
      if (hasMatchingToStringPair(actual().keySet(), keyList)) {
        failWithRawMessage(
            "Not true that %s contains key <%s (%s)>. However, it does contain keys <%s>.",
            actualAsString(),
            key,
            objectToTypeName(key),
            countDuplicatesAndAddTypeInfo(
                retainMatchingToString(actual().keySet(), keyList /* itemsToCheck */)));
      } else {
        fail("contains key", key);
      }
    }
  }

  /** Fails if the map contains the given key. */
  public void doesNotContainKey(@Nullable Object key) {
    if (actual().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /** Fails if the map does not contain the given entry. */
  public void containsEntry(@Nullable Object key, @Nullable Object value) {
    Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (!actual().entrySet().contains(entry)) {
      List<Object> keyList = Lists.newArrayList(key);
      List<Object> valueList = Lists.newArrayList(value);
      if (hasMatchingToStringPair(actual().keySet(), keyList)) {
        failWithRawMessage(
            "Not true that %s contains entry <%s (%s)>. However, it does contain keys <%s>.",
            actualAsString(),
            entry,
            objectToTypeName(entry),
            countDuplicatesAndAddTypeInfo(
                retainMatchingToString(actual().keySet(), keyList /* itemsToCheck */)));
      } else if (hasMatchingToStringPair(actual().values(), valueList)) {
        failWithRawMessage(
            "Not true that %s contains entry <%s (%s)>. However, it does contain values <%s>.",
            actualAsString(),
            entry,
            objectToTypeName(entry),
            countDuplicatesAndAddTypeInfo(
                retainMatchingToString(actual().values(), valueList /* itemsToCheck */)));
      } else if (actual().containsKey(key)) {
        failWithRawMessage(
            "Not true that %s contains entry <%s>. However, it has a mapping from <%s> to <%s>",
            actualAsString(), entry, key, actual().get(key));
      } else if (actual().containsValue(value)) {
        Set<Object> keys = new LinkedHashSet<Object>();
        for (Entry<?, ?> actualEntry : actual().entrySet()) {
          if (Objects.equal(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithRawMessage(
            "Not true that %s contains entry <%s>. "
                + "However, the following keys are mapped to <%s>: %s",
            actualAsString(), entry, value, keys);
      } else {
        fail("contains entry", entry);
      }
    }
  }

  /** Fails if the map contains the given entry. */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (actual().entrySet().contains(entry)) {
      fail("does not contain entry", entry);
    }
  }

  /** Fails if the map is not empty. */
  @CanIgnoreReturnValue
  public Ordered containsExactly() {
    return containsExactlyEntriesIn(ImmutableMap.of());
  }

  /**
   * Fails if the map does not contain exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   *
   * <p>The arguments must not contain duplicate keys.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactly(@Nullable Object k0, @Nullable Object v0, Object... rest) {
    return containsExactlyEntriesIn(accumulateMap(k0, v0, rest));
  }

  private static Map<Object, Object> accumulateMap(
      @Nullable Object k0, @Nullable Object v0, Object... rest) {
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
    return expectedMap;
  }

  /** Fails if the map does not contain exactly the given set of entries in the given map. */
  @CanIgnoreReturnValue
  public Ordered containsExactlyEntriesIn(Map<?, ?> expectedMap) {
    boolean containsAnyOrder = containsExactlyEntriesInAnyOrder(expectedMap, "contains exactly");
    if (containsAnyOrder) {
      return new MapInOrder(expectedMap);
    } else {
      return ALREADY_FAILED;
    }
  }

  @CanIgnoreReturnValue
  private boolean containsExactlyEntriesInAnyOrder(Map<?, ?> expectedMap, String failVerb) {
    MapDifference<?, ?> diff = Maps.difference(expectedMap, actual());
    if (diff.areEqual()) {
      return true;
    }
    boolean includeKeyTypes = includeKeyTypes(diff);
    Map<?, ?> missing = diff.entriesOnlyOnLeft();
    Map<?, ?> unexpected = diff.entriesOnlyOnRight();
    Map<?, ? extends ValueDifference<?>> wrongValues = diff.entriesDiffering();
    StringBuilder errorMsg = new StringBuilder();
    if (!missing.isEmpty()) {
      errorMsg
          .append("is missing keys for the following entries: ")
          .append(includeKeyTypes ? addKeyTypes(missing) : missing);
    }
    if (!unexpected.isEmpty()) {
      if (errorMsg.length() > 0) {
        errorMsg.append(" and ");
      }
      errorMsg
          .append("has the following entries with unexpected keys: ")
          .append(includeKeyTypes ? addKeyTypes(unexpected) : unexpected);
    }
    if (!wrongValues.isEmpty()) {
      if (errorMsg.length() > 0) {
        errorMsg.append(" and ");
      }
      Map<?, String> wrongValuesFormatted =
          Maps.transformValues(wrongValues, VALUE_DIFFERENCE_FORMATTER);
      errorMsg
          .append("has the following entries with matching keys but different values: ")
          .append(includeKeyTypes ? addKeyTypes(wrongValuesFormatted) : wrongValuesFormatted);
    }
    failWithRawMessage(
        "Not true that %s %s <%s>. It %s", actualAsString(), failVerb, expectedMap, errorMsg);
    return false;
  }

  private static boolean includeKeyTypes(MapDifference<?, ?> diff) {
    // We will annotate all the keys in the diff with their types if any of the keys involved have
    // the same toString() without being equal.
    Set<Object> keys = Sets.newHashSet();
    keys.addAll(diff.entriesOnlyOnLeft().keySet());
    keys.addAll(diff.entriesOnlyOnRight().keySet());
    keys.addAll(diff.entriesDiffering().keySet());
    return hasMatchingToStringPair(keys, keys);
  }

  private static final Map<Object, Object> addKeyTypes(Map<?, ?> in) {
    Map<Object, Object> out = Maps.newLinkedHashMap();
    for (Map.Entry<?, ?> entry : in.entrySet()) {
      out.put(new TypedToStringWrapper(entry.getKey()), entry.getValue());
    }
    return out;
  }

  private static final Function<ValueDifference<?>, String> VALUE_DIFFERENCE_FORMATTER =
      new Function<ValueDifference<?>, String>() {
        @Override
        public String apply(ValueDifference<?> diff) {
          Object left = diff.leftValue();
          Object right = diff.rightValue();
          boolean includeTypes = left.toString().equals(right.toString());
          return StringUtil.format(
              "(expected %s but got %s)",
              includeTypes ? new TypedToStringWrapper(left) : left,
              includeTypes ? new TypedToStringWrapper(right) : right);
        }
      };

  private static class TypedToStringWrapper {

    private final Object delegate;

    TypedToStringWrapper(Object delegate) {
      this.delegate = delegate;
    }

    @Override
    public boolean equals(Object other) {
      return Objects.equal(delegate, other);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(delegate);
    }

    @Override
    public String toString() {
      return StringUtil.format("%s (%s)", delegate, objectToTypeName(delegate));
    }
  }

  private class MapInOrder implements Ordered {

    private final Map<?, ?> expectedMap;

    MapInOrder(Map<?, ?> expectedMap) {
      this.expectedMap = expectedMap;
    }

    @Override
    public void inOrder() {
      List<?> expectedKeyOrder = Lists.newArrayList(expectedMap.keySet());
      List<?> actualKeyOrder = Lists.newArrayList(actual().keySet());
      if (!actualKeyOrder.equals(expectedKeyOrder)) {
        failWithRawMessage(
            "Not true that %s contains exactly these entries in order <%s>",
            actualAsString(), expectedMap);
      }
    }
  }

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED =
      new Ordered() {
        @Override
        public void inOrder() {}
      };

  /**
   * Starts a method chain for a check in which the actual values (i.e. the values of the {@link
   * Map} under test) are compared to expected values using the given {@link Correspondence}. The
   * actual values must be of type {@code A}, the expected values must be of type {@code E}. The
   * check is actually executed by continuing the method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualMap)
   *   .comparingValuesUsing(correspondence)
   *   .containsEntry(expectedKey, expectedValue);
   * }</pre>
   *
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
   * A partially specified check in which the actual values (i.e. the values of the {@link Map}
   * under test) are compared to expected values using a {@link Correspondence}. The expected values
   * are of type {@code E}. Call methods on this object to actually execute the check.
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
      if (actual().containsKey(expectedKey)) {
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
            actualAsString(), expectedKey, correspondence, expectedValue, actualValue);
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
              actualAsString(), expectedKey, correspondence, expectedValue, keys);
        } else {
          // Did not find matching key or value.
          failWithRawMessage(
              "Not true that %s contains an entry with key <%s> and a value that %s <%s>",
              actualAsString(), expectedKey, correspondence, expectedValue);
        }
      }
    }

    /**
     * Fails if the map contains an entry with the given key and a value that corresponds to the
     * given value.
     */
    public void doesNotContainEntry(@Nullable Object excludedKey, @Nullable E excludedValue) {
      if (actual().containsKey(excludedKey)) {
        A actualValue = getCastSubject().get(excludedKey);
        if (correspondence.compare(actualValue, excludedValue)) {
          failWithRawMessage(
              "Not true that %s does not contain an entry with key <%s> and a value that %s <%s>. "
                  + "It maps that key to <%s>",
              actualAsString(), excludedKey, correspondence, excludedValue, actualValue);
        }
      }
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
    // For bonus points, checking that the even-numbered values are of type E would be sweet.
    @CanIgnoreReturnValue
    public Ordered containsExactly(@Nullable Object k0, @Nullable E v0, Object... rest) {
      @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
      Map<Object, E> expectedMap = (Map<Object, E>) accumulateMap(k0, v0, rest);
      return containsExactlyEntriesIn(expectedMap);
    }

    /**
     * Fails if the map does not contain exactly the keys in the given map, mapping to values that
     * correspond to the values of the given map.
     */
    @CanIgnoreReturnValue
    public <K, V extends E> Ordered containsExactlyEntriesIn(Map<K, V> expectedMap) {
      // TODO(b/69728070): Show missing keys, unexpected keys, and matching keys with wrong values,
      // like we do for the vanilla containsExactlyEntriesIn.
      return check()
          .that(actual().entrySet())
          .comparingElementsUsing(new EntryCorrespondence<K, A, V>(correspondence))
          .containsExactlyElementsIn(expectedMap.entrySet());
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Map<?, A> getCastSubject() {
      return (Map<?, A>) actual();
    }
  }

  static final class EntryCorrespondence<K, A, E>
      extends Correspondence<Map.Entry<K, A>, Map.Entry<K, E>> {

    private final Correspondence<A, ? super E> valueCorrespondence;

    EntryCorrespondence(Correspondence<A, ? super E> valueCorrespondence) {
      this.valueCorrespondence = valueCorrespondence;
    }

    @Override
    public boolean compare(Entry<K, A> actual, Entry<K, E> expected) {
      return actual.getKey().equals(expected.getKey())
          && valueCorrespondence.compare(actual.getValue(), expected.getValue());
    }

    @Override
    public String toString() {
      return StringUtil.format(
          "has a key that is equal to and a value that %s the key and value of",
          valueCorrespondence);
    }
  }
}
