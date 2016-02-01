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

import javax.annotation.Nullable;

/**
 * Propositions for {@link Multimap} subjects.
 *
 * @author Daniel Ploch
 * @author Kurt Alfred Kluever
 */
public class MultimapSubject extends Subject<MultimapSubject, Multimap<?, ?>> {
  MultimapSubject(FailureStrategy failureStrategy, @Nullable Multimap<?, ?> multimap) {
    super(failureStrategy, multimap);
  }

  /**
   * Renames the subject so that this name appears in the error messages in place of string
   * representations of the subject.
   */
  @Override
  public MultimapSubject named(String name) {
    super.named(name);
    return this;
  }

  /**
   * Fails if the multimap is not empty.
   */
  public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  /**
   * Fails if the multimap is empty.
   */
  public void isNotEmpty() {
    if (getSubject().isEmpty()) {
      fail("is not empty");
    }
  }

  /**
   * Fails if the multimap does not have the given size.
   */
  public void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = getSubject().size();
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /**
   * Fails if the multimap does not contain the given key.
   */
  public void containsKey(@Nullable Object key) {
    if (!getSubject().containsKey(key)) {
      fail("contains key", key);
    }
  }

  /**
   * Fails if the multimap contains the given key.
   */
  public void doesNotContainKey(@Nullable Object key) {
    if (getSubject().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /**
   * Fails if the multimap does not contain the given entry.
   */
  public void containsEntry(@Nullable Object key, @Nullable Object value) {
    if (!getSubject().containsEntry(key, value)) {
      fail("contains entry", Maps.immutableEntry(key, value));
    }
  }

  /**
   * Fails if the multimap contains the given entry.
   */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    if (getSubject().containsEntry(key, value)) {
      fail("does not contain entry", Maps.immutableEntry(key, value));
    }
  }

  /**
   * Returns a context-aware Subject for making assertions about the values for the given key
   * within the Multimap.
   *
   * <p>This method performs no checks on its own and cannot cause test failures.  Subsequent
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
            mapType1,
            getDisplaySubject(),
            mapType2,
            other,
            mapType1,
            mapType2);
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
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify
   * that the two Multimaps iterate fully in the same order.  That is, their key sets iterate
   * in the same order, and the value collections for each key iterate in the same order.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactlyEntriesIn(Multimap<?, ?> expectedMultimap) {
    checkNotNull(expectedMultimap, "expectedMultimap");
    return containsExactly("contains exactly", expectedMultimap);
  }

  /**
   * @deprecated Use {@link #containsExactlyEntriesIn} instead.
   */
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
              getDisplaySubject(),
              expectedMultimap,
              keysWithValuesOutOfOrder);
        } else {
          failWithRawMessage(
              "Not true that %s contains exactly <%s> in order. The keys are not in order",
              getDisplaySubject(),
              expectedMultimap);
        }
      } else if (!keysWithValuesOutOfOrder.isEmpty()) {
        failWithRawMessage(
            "Not true that %s contains exactly <%s> in order. "
                + "The values for keys <%s> are not in order",
            getDisplaySubject(),
            expectedMultimap,
            keysWithValuesOutOfOrder);
      }
    }
  }

  private static <K, V> Collection<V> get(Multimap<K, V> multimap, @Nullable Object key) {
    if (multimap.containsKey(key)) {
      return multimap.get((K) key);
    } else {
      return Collections.emptyList();
    }
  }

  private static Multimap<?, ?> difference(Multimap<?, ?> minuend, Multimap<?, ?> subtrahend) {
    LinkedListMultimap<Object, Object> difference = LinkedListMultimap.create();
    for (Object key : minuend.keySet()) {
      List<?> valDifference = difference(
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
}
