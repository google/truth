/*
 * Copyright (c) 2014 Google, Inc.
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
import static com.google.common.truth.SubjectUtils.HUMAN_UNDERSTANDABLE_EMPTY_STRING;
import static com.google.common.truth.SubjectUtils.countDuplicatesAndAddTypeInfo;
import static com.google.common.truth.SubjectUtils.hasMatchingToStringPair;
import static com.google.common.truth.SubjectUtils.objectToTypeName;
import static com.google.common.truth.SubjectUtils.retainMatchingToString;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Propositions for {@link Multimap} subjects.
 *
 * @author Daniel Ploch
 * @author Kurt Alfred Kluever
 */
// Not final since SetMultimapSubject and ListMultimapSubject extends this
public class MultimapSubject extends Subject<MultimapSubject, Multimap<?, ?>> {

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED =
      new Ordered() {
        @Override
        public void inOrder() {}
      };

  MultimapSubject(FailureMetadata metadata, @Nullable Multimap<?, ?> multimap) {
    super(metadata, multimap);
  }

  /** Fails if the multimap is not empty. */
  public void isEmpty() {
    if (!actual().isEmpty()) {
      fail("is empty");
    }
  }

  /** Fails if the multimap is empty. */
  public void isNotEmpty() {
    if (actual().isEmpty()) {
      fail("is not empty");
    }
  }

  /** Fails if the multimap does not have the given size. */
  public void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = actual().size();
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /** Fails if the multimap does not contain the given key. */
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

  /** Fails if the multimap contains the given key. */
  public void doesNotContainKey(@Nullable Object key) {
    if (actual().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /** Fails if the multimap does not contain the given entry. */
  public void containsEntry(@Nullable Object key, @Nullable Object value) {
    // TODO(kak): Can we share any of this logic w/ MapSubject.containsEntry()?
    if (!actual().containsEntry(key, value)) {
      Entry<Object, Object> entry = Maps.immutableEntry(key, value);
      List<Entry<Object, Object>> entryList = ImmutableList.of(entry);
      if (hasMatchingToStringPair(actual().entries(), entryList)) {
        failWithRawMessage(
            "Not true that %s contains entry <%s (%s)>. However, it does contain entries <%s>",
            actualAsString(),
            entry,
            objectToTypeName(entry),
            countDuplicatesAndAddTypeInfo(
                retainMatchingToString(actual().entries(), entryList /* itemsToCheck */)));
      } else if (actual().containsKey(key)) {
        failWithRawMessage(
            "Not true that %s contains entry <%s>. However, it has a mapping from <%s> to <%s>",
            actualAsString(), entry, key, actual().asMap().get(key));
      } else if (actual().containsValue(value)) {
        Set<Object> keys = new LinkedHashSet<Object>();
        for (Entry<?, ?> actualEntry : actual().entries()) {
          if (Objects.equal(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithRawMessage(
            "Not true that %s contains entry <%s>. "
                + "However, the following keys are mapped to <%s>: %s",
            actualAsString(), entry, value, keys);
      } else {
        fail("contains entry", Maps.immutableEntry(key, value));
      }
    }
  }

  /** Fails if the multimap contains the given entry. */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    if (actual().containsEntry(key, value)) {
      fail("does not contain entry", Maps.immutableEntry(key, value));
    }
  }

  /**
   * Returns a context-aware {@link Subject} for making assertions about the values for the given
   * key within the {@link Multimap}.
   *
   * <p>This method performs no checks on its own and cannot cause test failures. Subsequent
   * assertions must be chained onto this method call to test properties of the {@link Multimap}.
   */
  public IterableSubject valuesForKey(@Nullable Object key) {
    return new IterableValuesForKey(metadata, this, key);
  }

  @Override
  public void isEqualTo(@Nullable Object other) {
    if (!Objects.equal(actual(), other)) {
      if ((actual() instanceof ListMultimap && other instanceof SetMultimap)
          || (actual() instanceof SetMultimap && other instanceof ListMultimap)) {
        String mapType1 = (actual() instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
        String mapType2 = (other instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
        failWithRawMessage(
            "Not true that %s %s is equal to %s <%s>. "
                + "A %s cannot equal a %s if either is non-empty.",
            mapType1, actualAsString(), mapType2, other, mapType1, mapType2);
      } else {
        if (actual() instanceof ListMultimap) {
          // If we're comparing ListMultimaps, check for order
          containsExactlyEntriesIn((Multimap<?, ?>) other).inOrder();
          return;
        } else if (actual() instanceof SetMultimap) {
          // If we're comparing SetMultimaps, don't check for order
          containsExactlyEntriesIn((Multimap<?, ?>) other);
          return;
        }
        fail("is equal to", other);
      }
    }
  }

  /**
   * Fails if the {@link Multimap} does not contain precisely the same entries as the argument
   * {@link Multimap}.
   *
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify that
   * the two multimaps iterate fully in the same order. That is, their key sets iterate in the same
   * order, and the value collections for each key iterate in the same order.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactlyEntriesIn(Multimap<?, ?> expectedMultimap) {
    checkNotNull(expectedMultimap, "expectedMultimap");
    ListMultimap<?, ?> missing = difference(expectedMultimap, actual());
    ListMultimap<?, ?> extra = difference(actual(), expectedMultimap);

    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the subject but not enough times. Similarly for unexpected extra items.
    if (!missing.isEmpty()) {
      if (!extra.isEmpty()) {
        boolean addTypeInfo = hasMatchingToStringPair(missing.entries(), extra.entries());
        failWithRawMessage(
            "Not true that %s contains exactly <%s>. "
                + "It is missing <%s> and has unexpected items <%s>",
            actualAsString(),
            annotateEmptyStringsMultimap(expectedMultimap),
            // Note: The usage of countDuplicatesAndAddTypeInfo() below causes entries no longer to
            // be grouped by key in the 'missing' and 'unexpected items' parts of the message (we
            // still show the actual and expected multimaps in the standard format).
            addTypeInfo
                ? countDuplicatesAndAddTypeInfo(annotateEmptyStringsMultimap(missing).entries())
                : countDuplicatesMultimap(annotateEmptyStringsMultimap(missing)),
            addTypeInfo
                ? countDuplicatesAndAddTypeInfo(annotateEmptyStringsMultimap(extra).entries())
                : countDuplicatesMultimap(annotateEmptyStringsMultimap(extra)));
        return ALREADY_FAILED;
      } else {
        failWithBadResults(
            "contains exactly",
            annotateEmptyStringsMultimap(expectedMultimap),
            "is missing",
            countDuplicatesMultimap(annotateEmptyStringsMultimap(missing)));
        return ALREADY_FAILED;
      }
    } else if (!extra.isEmpty()) {
      failWithBadResults(
          "contains exactly",
          annotateEmptyStringsMultimap(expectedMultimap),
          "has unexpected items",
          countDuplicatesMultimap(annotateEmptyStringsMultimap(extra)));
      return ALREADY_FAILED;
    }

    return new MultimapInOrder(expectedMultimap);
  }

  /** Fails if the multimap is not empty. */
  @CanIgnoreReturnValue
  public Ordered containsExactly() {
    return check().that(actual().entries()).containsExactly();
  }

  /**
   * Fails if the multimap does not contain exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   */
  @CanIgnoreReturnValue
  public Ordered containsExactly(@Nullable Object k0, @Nullable Object v0, Object... rest) {
    return containsExactlyEntriesIn(accumulateMultimap(k0, v0, rest));
  }

  private static Multimap<Object, Object> accumulateMultimap(
      @Nullable Object k0, @Nullable Object v0, Object... rest) {
    checkArgument(
        rest.length % 2 == 0,
        "There must be an equal number of key/value pairs "
            + "(i.e., the number of key/value parameters (%s) must be even).",
        rest.length + 2);

    LinkedListMultimap<Object, Object> expectedMultimap = LinkedListMultimap.create();
    expectedMultimap.put(k0, v0);
    for (int i = 0; i < rest.length; i += 2) {
      expectedMultimap.put(rest[i], rest[i + 1]);
    }
    return expectedMultimap;
  }

  /** @deprecated Use {@link #containsExactlyEntriesIn} instead. */
  @Deprecated
  @CanIgnoreReturnValue
  public Ordered containsExactly(Multimap<?, ?> expectedMultimap) {
    return containsExactlyEntriesIn(expectedMultimap);
  }

  private static class IterableValuesForKey extends IterableSubject {
    @Nullable private final Object key;
    private final String stringRepresentation;

    @SuppressWarnings({"unchecked"})
    IterableValuesForKey(
        FailureMetadata metadata, MultimapSubject multimapSubject, @Nullable Object key) {
      super(metadata, ((Multimap<Object, Object>) multimapSubject.actual()).get(key));
      this.key = key;
      this.stringRepresentation = multimapSubject.actualAsString();
    }

    @Override
    protected String actualCustomStringRepresentation() {
      return "Values for key <" + key + "> (<" + actual() + ">) in " + stringRepresentation;
    }
  }

  private static class IterableEntries extends IterableSubject {
    private final String stringRepresentation;

    IterableEntries(FailureMetadata metadata, MultimapSubject multimapSubject) {
      super(metadata, multimapSubject.actual().entries());
      // We want to use the multimap's toString() instead of the iterable of entries' toString():
      this.stringRepresentation = multimapSubject.actual().toString();
      // If the multimap subject is named() then this should be, too:
      if (multimapSubject.internalCustomName() != null) {
        named(multimapSubject.internalCustomName());
      }
    }

    @Override
    protected String actualCustomStringRepresentation() {
      return stringRepresentation;
    }
  }

  private class MultimapInOrder implements Ordered {
    private final Multimap<?, ?> expectedMultimap;

    MultimapInOrder(Multimap<?, ?> expectedMultimap) {
      this.expectedMultimap = expectedMultimap;
    }

    @Override
    public void inOrder() {
      boolean keysInOrder =
          Lists.newArrayList(actual().keySet())
              .equals(Lists.newArrayList(expectedMultimap.keySet()));

      LinkedHashSet<Object> keysWithValuesOutOfOrder = Sets.newLinkedHashSet();
      LinkedHashSet<Object> allKeys = Sets.newLinkedHashSet();
      allKeys.addAll(actual().keySet());
      allKeys.addAll(expectedMultimap.keySet());
      for (Object key : allKeys) {
        List<?> actualVals = Lists.newArrayList(get(actual(), key));
        List<?> expectedVals = Lists.newArrayList(get(expectedMultimap, key));
        if (!actualVals.equals(expectedVals)) {
          keysWithValuesOutOfOrder.add(key);
        }
      }

      if (!keysInOrder) {
        if (!keysWithValuesOutOfOrder.isEmpty()) {
          failWithRawMessage(
              "Not true that %s contains exactly <%s> in order. The keys are not in order, "
                  + "and the values for keys <%s> are not in order either",
              actualAsString(), expectedMultimap, keysWithValuesOutOfOrder);
        } else {
          failWithRawMessage(
              "Not true that %s contains exactly <%s> in order. The keys are not in order",
              actualAsString(), expectedMultimap);
        }
      } else if (!keysWithValuesOutOfOrder.isEmpty()) {
        failWithRawMessage(
            "Not true that %s contains exactly <%s> in order. "
                + "The values for keys <%s> are not in order",
            actualAsString(), expectedMultimap, keysWithValuesOutOfOrder);
      }
    }
  }

  private static <K, V> Collection<V> get(Multimap<K, V> multimap, @Nullable Object key) {
    if (multimap.containsKey(key)) {
      return multimap.asMap().get(key);
    } else {
      return Collections.emptyList();
    }
  }

  private static ListMultimap<?, ?> difference(Multimap<?, ?> minuend, Multimap<?, ?> subtrahend) {
    ListMultimap<Object, Object> difference = LinkedListMultimap.create();
    for (Object key : minuend.keySet()) {
      List<?> valDifference =
          difference(
              Lists.newArrayList(get(minuend, key)), Lists.newArrayList(get(subtrahend, key)));
      difference.putAll(key, valDifference);
    }
    return difference;
  }

  private static List<?> difference(List<?> minuend, List<?> subtrahend) {
    LinkedHashMultiset<Object> remaining = LinkedHashMultiset.<Object>create(subtrahend);
    List<Object> difference = Lists.newArrayList();
    for (Object elem : minuend) {
      if (!remaining.remove(elem)) {
        difference.add(elem);
      }
    }
    return difference;
  }

  private static <K, V> String countDuplicatesMultimap(Multimap<K, V> multimap) {
    List<String> entries = new ArrayList<String>();
    for (K key : multimap.keySet()) {
      entries.add(key + "=" + SubjectUtils.countDuplicates(multimap.get(key)));
    }

    StringBuilder sb = new StringBuilder();
    sb.append("{");
    Joiner.on(", ").appendTo(sb, entries);
    sb.append("}");
    return sb.toString();
  }

  /**
   * Returns a multimap with all empty strings (as keys or values) replaced by a non-empty human
   * understandable indicator for an empty string.
   *
   * <p>Returns the given multimap if it contains no empty strings.
   */
  private static Multimap<?, ?> annotateEmptyStringsMultimap(Multimap<?, ?> multimap) {
    if (multimap.containsKey("") || multimap.containsValue("")) {
      ListMultimap<Object, Object> annotatedMultimap = LinkedListMultimap.create();
      for (Entry<?, ?> entry : multimap.entries()) {
        Object key = "".equals(entry.getKey()) ? HUMAN_UNDERSTANDABLE_EMPTY_STRING : entry.getKey();
        Object value =
            "".equals(entry.getValue()) ? HUMAN_UNDERSTANDABLE_EMPTY_STRING : entry.getValue();
        annotatedMultimap.put(key, value);
      }
      return annotatedMultimap;
    } else {
      return multimap;
    }
  }

  /**
   * Starts a method chain for a test proposition in which the actual values (i.e. the values of the
   * {@link Multimap} under test) are compared to expected values using the given {@link
   * Correspondence}. The actual values must be of type {@code A}, and the expected values must be
   * of type {@code E}. The proposition is actually executed by continuing the method chain. For
   * example:
   *
   * <pre>{@code
   * assertThat(actualMultimap)
   *   .comparingValuesUsing(correspondence)
   *   .containsEntry(expectedKey, expectedValue);
   * }</pre>
   *
   * where {@code actualMultimap} is a {@code Multimap<?, A>} (or, more generally, a {@code
   * Multimap<?, ? extends A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and
   * {@code expectedValue} is an {@code E}.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual value that is not of type {@code A}.
   */
  public <A, E> UsingCorrespondence<A, E> comparingValuesUsing(
      Correspondence<A, E> correspondence) {
    return new UsingCorrespondence<A, E>(correspondence);
  }

  /**
   * A partially specified proposition in which the actual values (i.e. the values of the {@link
   * Multimap} under test) are compared to expected values using a {@link Correspondence}. The
   * expected values are of type {@code E}. Call methods on this object to actually execute the
   * proposition.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   */
  public final class UsingCorrespondence<A, E> {

    private final Correspondence<A, E> correspondence;

    private UsingCorrespondence(Correspondence<A, E> correspondence) {
      this.correspondence = checkNotNull(correspondence);
    }

    /**
     * Fails if the multimap does not contain an entry with the given key and a value that
     * corresponds to the given value.
     */
    public void containsEntry(@Nullable Object expectedKey, @Nullable E expectedValue) {
      if (actual().containsKey(expectedKey)) {
        // Found matching key.
        Collection<A> actualValues = getCastActual().asMap().get(expectedKey);
        for (A actualValue : actualValues) {
          if (correspondence.compare(actualValue, expectedValue)) {
            // Found matching key and value. Test passes!
            return;
          }
        }
        // Found matching key with non-matching values.
        failWithRawMessage(
            "Not true that %s contains at least one entry with key <%s> and a value that %s <%s>. "
                + "However, it has a mapping from that key to <%s>",
            actualAsString(), expectedKey, correspondence, expectedValue, actualValues);
      } else {
        // Did not find matching key.
        Set<Object> keys = new LinkedHashSet<Object>();
        for (Entry<?, A> actualEntry : getCastActual().entries()) {
          if (correspondence.compare(actualEntry.getValue(), expectedValue)) {
            keys.add(actualEntry.getKey());
          }
        }
        if (!keys.isEmpty()) {
          // Found matching values with non-matching keys.
          failWithRawMessage(
              "Not true that %s contains at least one entry with key <%s> and a value that %s <%s>."
                  + " However, the following keys are mapped to such values: <%s>",
              actualAsString(), expectedKey, correspondence, expectedValue, keys);
        } else {
          // Did not find matching key or value.
          failWithRawMessage(
              "Not true that %s contains at least one entry with key <%s> and a value that %s <%s>",
              actualAsString(), expectedKey, correspondence, expectedValue);
        }
      }
    }

    /**
     * Fails if the multimap contains an entry with the given key and a value that corresponds to
     * the given value.
     */
    public void doesNotContainEntry(@Nullable Object excludedKey, @Nullable E excludedValue) {
      if (actual().containsKey(excludedKey)) {
        Collection<A> actualValues = getCastActual().asMap().get(excludedKey);
        List<A> matchingValues = new ArrayList<A>();
        for (A actualValue : actualValues) {
          if (correspondence.compare(actualValue, excludedValue)) {
            matchingValues.add(actualValue);
          }
        }
        if (!matchingValues.isEmpty()) {
          failWithRawMessage(
              "Not true that %s did not contain an entry with key <%s> and a value that %s <%s>. "
                  + "It maps that key to the following such values: <%s>",
              actualAsString(), excludedKey, correspondence, excludedValue, matchingValues);
        }
      }
    }

    /**
     * Fails if the map does not contain exactly the keys in the given multimap, mapping to values
     * that correspond to the values of the given multimap.
     *
     * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify
     * that the two Multimaps iterate fully in the same order. That is, their key sets iterate in
     * the same order, and the corresponding value collections for each key iterate in the same
     * order.
     */
    @CanIgnoreReturnValue
    public <K, V extends E> Ordered containsExactlyEntriesIn(Multimap<K, V> expectedMultimap) {
      // Note: The non-fuzzy MultimapSubject.containsExactlyEntriesIn has a custom implementation
      // and produces somewhat better failure messages simply asserting about the iterables of
      // entries would: it formats the expected values as  k=[v1, v2] rather than k=v1, k=v2; and in
      // the case where inOrder() fails it says the keys and/or the values for some keys are out of
      // order. We don't bother with that here. It would be nice, but it would be a lot of added
      // complexity for little gain.
      return new IterableEntries(metadata, MultimapSubject.this)
          .comparingElementsUsing(new MapSubject.EntryCorrespondence<K, A, V>(correspondence))
          .containsExactlyElementsIn(expectedMultimap.entries());
    }

    /**
     * Fails if the multimap does not contain exactly the given set of key/value pairs.
     *
     * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
     * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
     */
    @CanIgnoreReturnValue
    public <K, V extends E> Ordered containsExactly(
        @Nullable Object k0, @Nullable Object v0, Object... rest) {
      @SuppressWarnings("unchecked")
      Multimap<K, V> expectedMultimap = (Multimap<K, V>) accumulateMultimap(k0, v0, rest);
      return containsExactlyEntriesIn(expectedMultimap);
    }

    /** Fails if the multimap is not empty. */
    @CanIgnoreReturnValue
    public <K, V extends E> Ordered containsExactly() {
      return MultimapSubject.this.containsExactly();
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Multimap<?, A> getCastActual() {
      return (Multimap<?, A>) actual();
    }
  }
}
