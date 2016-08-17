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

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
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
  MultimapSubject(FailureStrategy failureStrategy, @Nullable Multimap<?, ?> multimap) {
    super(failureStrategy, multimap);
  }

  @Override
  public MultimapSubject named(String format, Object... args) {
    super.named(format, args);
    return this;
  }

  /** Fails if the multimap is not empty. */
  public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  /** Fails if the multimap is empty. */
  public void isNotEmpty() {
    if (getSubject().isEmpty()) {
      fail("is not empty");
    }
  }

  /** Fails if the multimap does not have the given size. */
  public void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = getSubject().size();
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /** Fails if the multimap does not contain the given key. */
  public void containsKey(@Nullable Object key) {
    if (!getSubject().containsKey(key)) {
      fail("contains key", key);
    }
  }

  /** Fails if the multimap contains the given key. */
  public void doesNotContainKey(@Nullable Object key) {
    if (getSubject().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /** Fails if the multimap does not contain the given entry. */
  public void containsEntry(@Nullable Object key, @Nullable Object value) {
    // TODO(kak): Can we share any of this logic w/ MapSubject.containsEntry()?
    if (!getSubject().containsEntry(key, value)) {
      Entry<Object, Object> entry = Maps.immutableEntry(key, value);
      if (getSubject().containsKey(key)) {
        failWithRawMessage(
            "Not true that %s contains entry <%s>. However, it has a mapping from <%s> to <%s>",
            getDisplaySubject(), entry, key, getSubject().asMap().get(key));
      }
      if (getSubject().containsValue(value)) {
        Set<Object> keys = new LinkedHashSet<Object>();
        for (Entry<?, ?> actualEntry : getSubject().entries()) {
          if (Objects.equal(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithRawMessage(
            "Not true that %s contains entry <%s>. "
                + "However, the following keys are mapped to <%s>: %s",
            getDisplaySubject(), entry, value, keys);
      }
      fail("contains entry", Maps.immutableEntry(key, value));
    }
  }

  /** Fails if the multimap contains the given entry. */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    if (getSubject().containsEntry(key, value)) {
      fail("does not contain entry", Maps.immutableEntry(key, value));
    }
  }

  /**
   * Returns a context-aware Subject for making assertions about the values for the given key within
   * the Multimap.
   *
   * <p>This method performs no checks on its own and cannot cause test failures. Subsequent
   * assertions must be chained onto this method call to test properties of the Multimap.
   */
  public IterableSubject valuesForKey(@Nullable Object key) {
    return new IterableValuesForKey(failureStrategy, this, key);
  }

  @Override
  public void isEqualTo(@Nullable Object other) {
    if (!Objects.equal(getSubject(), other)) {
      if ((getSubject() instanceof ListMultimap && other instanceof SetMultimap)
          || (getSubject() instanceof SetMultimap && other instanceof ListMultimap)) {
        String mapType1 = (getSubject() instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
        String mapType2 = (other instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
        failWithRawMessage(
            "Not true that %s %s is equal to %s <%s>. "
                + "A %s cannot equal a %s if either is non-empty.",
            mapType1, getDisplaySubject(), mapType2, other, mapType1, mapType2);
      } else {
        if (getSubject() instanceof ListMultimap) {
          // If we're comparing ListMultimaps, check for order
          containsExactlyEntriesIn((Multimap<?, ?>) other).inOrder();
        } else if (getSubject() instanceof SetMultimap) {
          // If we're comparing SetMultimaps, don't check for order
          containsExactlyEntriesIn((Multimap<?, ?>) other);
        }
        // This statement should generally never be reached because one of the two
        // containsExactlyEntriesIn calls above should throw an exception. It'll only be reached if
        // we're looking at a non-ListMultimap and non-SetMultimap
        // (e.g., a custom Multimap implementation).
        fail("is equal to", other);
      }
    }
  }

  /**
   * Fails if the Multimap does not contain precisely the same entries as the argument Multimap.
   *
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify that
   * the two Multimaps iterate fully in the same order. That is, their key sets iterate in the same
   * order, and the value collections for each key iterate in the same order.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactlyEntriesIn(Multimap<?, ?> expectedMultimap) {
    checkNotNull(expectedMultimap, "expectedMultimap");
    return containsExactly("contains exactly", expectedMultimap);
  }

  /** @deprecated Use {@link #containsExactlyEntriesIn} instead. */
  @Deprecated
  @CanIgnoreReturnValue
  public Ordered containsExactly(Multimap<?, ?> expectedMultimap) {
    return containsExactlyEntriesIn(expectedMultimap);
  }

  private Ordered containsExactly(String failVerb, Multimap<?, ?> expectedMultimap) {
    Multimap<?, ?> missing = difference(expectedMultimap, getSubject());
    Multimap<?, ?> extra = difference(getSubject(), expectedMultimap);

    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the subject but not enough times. Similarly for unexpected extra items.
    if (!missing.isEmpty()) {
      if (!extra.isEmpty()) {
        failWithRawMessage(
            "Not true that %s %s <%s>. It is missing <%s> and has unexpected items <%s>",
            getDisplaySubject(),
            failVerb,
            expectedMultimap,
            countDuplicatesMultimap(missing),
            countDuplicatesMultimap(extra));
      } else {
        failWithBadResults(
            failVerb, expectedMultimap, "is missing", countDuplicatesMultimap(missing));
      }
    } else if (!extra.isEmpty()) {
      failWithBadResults(
          failVerb, expectedMultimap, "has unexpected items", countDuplicatesMultimap(extra));
    }

    return new MultimapInOrder(expectedMultimap);
  }

  private class IterableValuesForKey extends IterableSubject {
    @Nullable private final Object key;
    @Nullable private final String display;

    @SuppressWarnings({"unchecked"})
    IterableValuesForKey(
        FailureStrategy failureStrategy, MultimapSubject multimapSubject, @Nullable Object key) {
      super(failureStrategy, ((Multimap<Object, Object>) multimapSubject.getSubject()).get(key));
      this.key = key;
      this.display = multimapSubject.getDisplaySubject();
    }

    @Override
    protected String getDisplaySubject() {
      String innerDisplaySubject =
          "<Values for key <" + key + "> (<" + getSubject() + ">) in " + display + ">";

      if (internalCustomName() != null) {
        return internalCustomName() + " (" + innerDisplaySubject + ")";
      } else {
        return innerDisplaySubject;
      }
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
          Lists.newArrayList(getSubject().keySet())
              .equals(Lists.newArrayList(expectedMultimap.keySet()));

      LinkedHashSet<Object> keysWithValuesOutOfOrder = Sets.newLinkedHashSet();
      LinkedHashSet<Object> allKeys = Sets.newLinkedHashSet();
      allKeys.addAll(getSubject().keySet());
      allKeys.addAll(expectedMultimap.keySet());
      for (Object key : allKeys) {
        List<?> actualVals = Lists.newArrayList(get(getSubject(), key));
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
              getDisplaySubject(), expectedMultimap, keysWithValuesOutOfOrder);
        } else {
          failWithRawMessage(
              "Not true that %s contains exactly <%s> in order. The keys are not in order",
              getDisplaySubject(), expectedMultimap);
        }
      } else if (!keysWithValuesOutOfOrder.isEmpty()) {
        failWithRawMessage(
            "Not true that %s contains exactly <%s> in order. "
                + "The values for keys <%s> are not in order",
            getDisplaySubject(), expectedMultimap, keysWithValuesOutOfOrder);
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

  private static Multimap<?, ?> difference(Multimap<?, ?> minuend, Multimap<?, ?> subtrahend) {
    LinkedListMultimap<Object, Object> difference = LinkedListMultimap.create();
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
   * Starts a method chain for a test proposition in which the actual values (i.e. the values of the
   * {@link Multimap} under test) are compared to expected values using the given {@link
   * Correspondence}. The actual values must be of type {@code A}, the expected values must be of
   * type {@code E}. The proposition is actually executed by continuing the method chain. For
   * example:<pre>   {@code
   *   assertThat(actualMultimap)
   *     .comparingValuesUsing(correspondence)
   *     .containsEntry(expectedKey, expectedValue);}</pre>
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
      if (getSubject().containsKey(expectedKey)) {
        // Found matching key.
        Collection<A> actualValues = getCastSubject().asMap().get(expectedKey);
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
            getDisplaySubject(), expectedKey, correspondence, expectedValue, actualValues);
      } else {
        // Did not find matching key.
        Set<Object> keys = new LinkedHashSet<Object>();
        for (Entry<?, A> actualEntry : getCastSubject().entries()) {
          if (correspondence.compare(actualEntry.getValue(), expectedValue)) {
            keys.add(actualEntry.getKey());
          }
        }
        if (!keys.isEmpty()) {
          // Found matching values with non-matching keys.
          failWithRawMessage(
              "Not true that %s contains at least one entry with key <%s> and a value that %s <%s>."
                  + " However, the following keys are mapped to such values: <%s>",
              getDisplaySubject(), expectedKey, correspondence, expectedValue, keys);
        } else {
          // Did not find matching key or value.
          failWithRawMessage(
              "Not true that %s contains at least one entry with key <%s> and a value that %s <%s>",
              getDisplaySubject(), expectedKey, correspondence, expectedValue);
        }
      }
    }

    /**
     * Fails if the multimap contains an entry with the given key and a value that corresponds to
     * the given value.
     */
    public void doesNotContainEntry(@Nullable Object excludedKey, @Nullable E excludedValue) {
      if (getSubject().containsKey(excludedKey)) {
        Collection<A> actualValues = getCastSubject().asMap().get(excludedKey);
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
              getDisplaySubject(), excludedKey, correspondence, excludedValue, matchingValues);
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
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private Ordered containsExactlyEntriesIn(Multimap<?, ? extends E> expectedMultimap) {
      throw new UnsupportedOperationException();
    }

    /**
     * Returns a context-aware object for making assertions about the values for the given key
     * within the multimap, using the associated correspondence to compare the values.
     *
     * <p>This method performs no checks on its own and cannot cause test failures. Subsequent
     * assertions must be chained onto this method call to test properties of the multimap.
     */
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private IterableSubject.UsingCorrespondence<A, E> valuesForKey(@Nullable Object key) {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Multimap<?, A> getCastSubject() {
      return (Multimap<?, A>) getSubject();
    }
  }
}
