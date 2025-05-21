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
import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Multisets.immutableEntry;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Utility methods used in {@code Subject} implementors.
 *
 * @author Christian Gruber
 * @author Jens Nyman
 */
final class SubjectUtils {
  private SubjectUtils() {}

  static final String HUMAN_UNDERSTANDABLE_EMPTY_STRING = "\"\" (empty String)";

  static <T extends @Nullable Object> List<T> accumulate(T first, T second, T @Nullable ... rest) {
    // rest should never be deliberately null, so assume that the caller passed null
    // in the third position but intended it to be the third element in the array of values.
    // Javac makes the opposite inference, so handle that here.
    List<T> items = new ArrayList<>(2 + ((rest == null) ? 1 : rest.length));
    items.add(first);
    items.add(second);
    if (rest == null) {
      items.add((T) null);
    } else {
      items.addAll(asList(rest));
    }
    return items;
  }

  static String countDuplicates(Iterable<?> items) {
    /*
     * TODO(cpovirk): Remove brackets after migrating all callers to the new message format. But
     * will that look OK when we put the result next to a homogeneous type name? If not, maybe move
     * the homogeneous type name to a separate Fact?
     */
    return countDuplicatesToMultiset(items).toStringWithBrackets();
  }

  static String entryString(Multiset.Entry<?> entry) {
    int count = entry.getCount();
    String item = String.valueOf(entry.getElement());
    return (count > 1) ? item + " [" + count + " copies]" : item;
  }

  private static <T extends @Nullable Object> NonHashingMultiset<T> countDuplicatesToMultiset(
      Iterable<T> items) {
    // We use avoid hashing in case the elements don't have a proper
    // .hashCode() method (e.g., MessageSet from old versions of protobuf).
    NonHashingMultiset<T> multiset = new NonHashingMultiset<>();
    for (T item : items) {
      multiset.add(item);
    }
    return multiset;
  }

  /**
   * Makes a String representation of {@code items} with collapsed duplicates and additional class
   * info.
   *
   * <p>Example: {@code countDuplicatesAndAddTypeInfo([1, 2, 2, 3]) == "[1, 2 [3 copies]]
   * (java.lang.Integer)"} and {@code countDuplicatesAndAddTypeInfo([1, 2L]) == "[1
   * (java.lang.Integer), 2 (java.lang.Long)]"}.
   */
  static String countDuplicatesAndAddTypeInfo(Iterable<?> itemsIterable) {
    Collection<?> items = iterableToCollection(itemsIterable);
    String homogeneousTypeName = getHomogeneousTypeName(items);

    return homogeneousTypeName != null
        ? lenientFormat("%s (%s)", countDuplicates(items), homogeneousTypeName)
        : countDuplicates(addTypeInfoToEveryItem(items));
  }

  /**
   * Similar to {@link #countDuplicatesAndAddTypeInfo} and {@link #countDuplicates} but (a) only
   * adds type info if requested and (b) returns a richer object containing the data.
   */
  static DuplicateGroupedAndTyped countDuplicatesAndMaybeAddTypeInfoReturnObject(
      Iterable<?> itemsIterable, boolean addTypeInfo) {
    if (addTypeInfo) {
      Collection<?> items = iterableToCollection(itemsIterable);
      String homogeneousTypeName = getHomogeneousTypeName(items);

      NonHashingMultiset<?> valuesWithCountsAndMaybeTypes =
          homogeneousTypeName != null
              ? countDuplicatesToMultiset(items)
              : countDuplicatesToMultiset(addTypeInfoToEveryItem(items));
      return new DuplicateGroupedAndTyped(valuesWithCountsAndMaybeTypes, homogeneousTypeName);
    } else {
      return new DuplicateGroupedAndTyped(
          countDuplicatesToMultiset(itemsIterable), /* homogeneousTypeToDisplay= */ null);
    }
  }

  private static final class NonHashingMultiset<E extends @Nullable Object> {
    /*
     * This ought to be static, but the generics are easier when I can refer to <E>. We still want
     * an Entry<?> so that entrySet() can return Iterable<Entry<?>> instead of Iterable<Entry<E>>.
     * That way, it can be returned directly from DuplicateGroupedAndTyped.entrySet() without our
     * having to generalize *its* return type to Iterable<? extends Entry<?>>.
     */
    private Multiset.Entry<?> unwrapKey(Multiset.Entry<Wrapper<E>> input) {
      return immutableEntry(input.getElement().get(), input.getCount());
    }

    private final Multiset<Wrapper<E>> contents = LinkedHashMultiset.create();

    void add(E element) {
      contents.add(EQUALITY_WITHOUT_USING_HASH_CODE.wrap(element));
    }

    int totalCopies() {
      return contents.size();
    }

    boolean isEmpty() {
      return contents.isEmpty();
    }

    Iterable<Multiset.Entry<?>> entrySet() {
      return transform(contents.entrySet(), this::unwrapKey);
    }

    String toStringWithBrackets() {
      List<String> parts = new ArrayList<>();
      for (Multiset.Entry<?> entry : entrySet()) {
        parts.add(entryString(entry));
      }
      return parts.toString();
    }

    @Override
    public String toString() {
      String withBrackets = toStringWithBrackets();
      return withBrackets.substring(1, withBrackets.length() - 1);
    }

    private static final Equivalence<Object> EQUALITY_WITHOUT_USING_HASH_CODE =
        new Equivalence<Object>() {
          @Override
          protected boolean doEquivalent(Object a, Object b) {
            return Objects.equals(a, b);
          }

          @Override
          protected int doHash(Object o) {
            return 0; // slow but hopefully not much worse than what we get with a flat list
          }
        };
  }

  /**
   * Missing or unexpected values from a collection assertion, with equal objects grouped together
   * and, in some cases, type information added. If the type information is present, it is either
   * present in {@code homogeneousTypeToDisplay} (if all objects have the same type) or appended to
   * each individual element (if some elements have different types).
   *
   * <p>This allows collection assertions to the type information on a separate line from the
   * elements and even to output different elements on different lines.
   */
  static final class DuplicateGroupedAndTyped {
    final NonHashingMultiset<?> valuesAndMaybeTypes;
    final @Nullable String homogeneousTypeToDisplay;

    DuplicateGroupedAndTyped(
        NonHashingMultiset<?> valuesAndMaybeTypes, @Nullable String homogeneousTypeToDisplay) {
      this.valuesAndMaybeTypes = valuesAndMaybeTypes;
      this.homogeneousTypeToDisplay = homogeneousTypeToDisplay;
    }

    int totalCopies() {
      return valuesAndMaybeTypes.totalCopies();
    }

    boolean isEmpty() {
      return valuesAndMaybeTypes.isEmpty();
    }

    Iterable<Multiset.Entry<?>> entrySet() {
      return valuesAndMaybeTypes.entrySet();
    }

    @Override
    public String toString() {
      return homogeneousTypeToDisplay != null
          ? valuesAndMaybeTypes + " (" + homogeneousTypeToDisplay + ")"
          : valuesAndMaybeTypes.toString();
    }
  }

  /**
   * Returns a new collection containing all elements in {@code items} for which there exists at
   * least one element in {@code itemsToCheck} that has the same {@code toString()} value without
   * being equal.
   *
   * <p>Example: {@code retainMatchingToString([1L, 2L, 2L], [2, 3]) == [2L, 2L]}
   */
  static List<@Nullable Object> retainMatchingToString(
      Iterable<?> items, Iterable<?> itemsToCheck) {
    ListMultimap<String, @Nullable Object> stringValueToItemsToCheck = ArrayListMultimap.create();
    for (Object itemToCheck : itemsToCheck) {
      stringValueToItemsToCheck.put(String.valueOf(itemToCheck), itemToCheck);
    }

    List<@Nullable Object> result = new ArrayList<>();
    for (Object item : items) {
      for (Object itemToCheck : stringValueToItemsToCheck.get(String.valueOf(item))) {
        if (!Objects.equals(itemToCheck, item)) {
          result.add(item);
          break;
        }
      }
    }
    return result;
  }

  /**
   * Returns true if there is a pair of an item from {@code items1} and one in {@code items2} that
   * has the same {@code toString()} value without being equal.
   *
   * <p>Example: {@code hasMatchingToStringPair([1L, 2L], [1]) == true}
   */
  static boolean hasMatchingToStringPair(Iterable<?> items1, Iterable<?> items2) {
    if (isEmpty(items1) || isEmpty(items2)) {
      return false; // Bail early to avoid calling hashCode() on the elements unnecessarily.
    }
    return !retainMatchingToString(items1, items2).isEmpty();
  }

  static String objectToTypeName(@Nullable Object item) {
    // TODO(cpovirk): Merge this with the code in Subject.failEqualityCheck().
    if (item == null) {
      // The name "null type" comes from the interface javax.lang.model.type.NullType.
      return "null type";
    } else if (item instanceof Map.Entry) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) item;
      // Fix for interesting bug when entry.getValue() returns itself b/170390717
      String valueTypeName =
          entry.getValue() == entry ? "Map.Entry" : objectToTypeName(entry.getValue());

      return lenientFormat("Map.Entry<%s, %s>", objectToTypeName(entry.getKey()), valueTypeName);
    } else {
      return item.getClass().getName();
    }
  }

  /**
   * Returns the name of the single type of all given items or {@code null} if no such type exists.
   */
  private static @Nullable String getHomogeneousTypeName(Iterable<?> items) {
    String homogeneousTypeName = null;
    for (Object item : items) {
      if (item == null) {
        /*
         * TODO(cpovirk): Why? We could have multiple nulls, which would be homogeneous. More
         * likely, we could have exactly one null, which is still homogeneous. Arguably it's weird
         * to call a single element "homogeneous" at all, but that's not specific to null.
         */
        return null;
      } else if (homogeneousTypeName == null) {
        // This is the first item
        homogeneousTypeName = objectToTypeName(item);
      } else if (!objectToTypeName(item).equals(homogeneousTypeName)) {
        // items is a heterogeneous collection
        return null;
      }
    }
    return homogeneousTypeName;
  }

  private static List<String> addTypeInfoToEveryItem(Iterable<?> items) {
    List<String> itemsWithTypeInfo = new ArrayList<>();
    for (Object item : items) {
      itemsWithTypeInfo.add(lenientFormat("%s (%s)", item, objectToTypeName(item)));
    }
    return itemsWithTypeInfo;
  }

  static <T extends @Nullable Object> Collection<T> iterableToCollection(
      @Nullable Iterable<T> iterable) {
    // TODO(cpovirk): For null inputs, produce a better exception message (ideally in callers).
    checkNotNull(iterable);
    if (iterable instanceof Collection) {
      // Should be safe to assume that any Iterable implementing Collection isn't a one-shot
      // iterable, right? I sure hope so.
      return (Collection<T>) iterable;
    } else {
      return Lists.newArrayList(iterable);
    }
  }

  static <T extends @Nullable Object> List<T> iterableToList(Iterable<T> iterable) {
    if (iterable instanceof List) {
      return (List<T>) iterable;
    } else {
      return Lists.newArrayList(iterable);
    }
  }

  /**
   * Returns an iterable with all empty strings replaced by a non-empty human understandable
   * indicator for an empty string.
   *
   * <p>Returns the given iterable if it contains no empty strings.
   */
  static <T extends @Nullable Object> Iterable<T> annotateEmptyStrings(Iterable<T> items) {
    if (Iterables.contains(items, "")) {
      List<T> annotatedItems = new ArrayList<>();
      for (T item : items) {
        if (Objects.equals(item, "")) {
          // This is a safe cast because know that at least one instance of T (this item) is a
          // String.
          @SuppressWarnings("unchecked")
          T newItem = (T) HUMAN_UNDERSTANDABLE_EMPTY_STRING;
          annotatedItems.add(newItem);
        } else {
          annotatedItems.add(item);
        }
      }
      return annotatedItems;
    } else {
      return items;
    }
  }

  @SafeVarargs
  static <E> ImmutableList<E> concat(Iterable<? extends E>... inputs) {
    return ImmutableList.copyOf(Iterables.concat(inputs));
  }

  static <E> ImmutableList<E> append(E[] array, E object) {
    return new ImmutableList.Builder<E>().add(array).add(object).build();
  }

  static <E> ImmutableList<E> append(ImmutableList<? extends E> list, E object) {
    return new ImmutableList.Builder<E>().addAll(list).add(object).build();
  }

  static <E> ImmutableList<E> sandwich(E first, E[] array, E last) {
    return new ImmutableList.Builder<E>().add(first).add(array).add(last).build();
  }

  @SuppressWarnings("nullness") // TODO: b/316358623 - Remove suppression after fixing checker
  static <E extends @Nullable Object> List<E> asList(E... a) {
    return Arrays.asList(a);
  }
}
