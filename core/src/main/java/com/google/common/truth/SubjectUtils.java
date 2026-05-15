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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Multisets.immutableEntry;
import static com.google.common.truth.NullnessCasts.uncheckedCastNullableTToT;
import static com.google.common.truth.Platform.stringValueForFailure;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.base.VerifyException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

/** Utility methods used in {@link Subject} implementors. */
final class SubjectUtils {
  private SubjectUtils() {}

  static final String HUMAN_UNDERSTANDABLE_EMPTY_STRING = "\"\" (empty String)";

  static <T extends @Nullable Object> List<T> accumulate(T first, T second, T @Nullable [] rest) {
    // rest should never be deliberately null, so assume that the caller passed null
    // in the third position but intended it to be the third element in the array of values.
    // Javac makes the opposite inference, so handle that here.
    List<T> items = new ArrayList<>(2 + ((rest == null) ? 1 : rest.length));
    items.add(first);
    items.add(second);
    if (rest == null) {
      /*
       * This cast is probably not actually safe as used in IterableSubject.UsingCorrespondence. But
       * that whole API is stuck being type-unsafe unless we re-generify IterableSubject:
       * b/145689657#comment1.
       */
      items.add(uncheckedCastNullableTToT(null));
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
    String item = stringValueForFailure(entry.getElement());
    return (count > 1) ? item + " [" + count + " copies]" : item;
  }

  private static <T extends @Nullable Object> NonHashingMultiset<T> countDuplicatesToMultiset(
      Iterable<T> items) {
    // We avoid hashing the elements in case they don't have a proper hashCode() implementation.
    // (The prototypical example is MessageSet from old versions of protobuf.)
    NonHashingMultiset<T> multiset = NonHashingMultiset.create();
    for (T item : items) {
      multiset.add(item);
    }
    return multiset;
  }

  /**
   * Makes a String representation of {@code itemsIterable} with collapsed duplicates and additional
   * class info.
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
   * Similar to {@link #countDuplicatesAndAddTypeInfo} and {@link #countDuplicates} but:
   *
   * <ul>
   *   <li>only adds type info if requested
   *   <li>returns a richer object containing the data
   * </ul>
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
      return DuplicateGroupedAndTyped.create(valuesWithCountsAndMaybeTypes, homogeneousTypeName);
    } else {
      return DuplicateGroupedAndTyped.create(
          countDuplicatesToMultiset(itemsIterable), /* homogeneousTypeToDisplay= */ null);
    }
  }

  private static final class NonHashingMultiset<E extends @Nullable Object> {
    private final Multiset<Wrapper<E>> contents = LinkedHashMultiset.create();

    private NonHashingMultiset() {}

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

    /*
     * This ought to be static, but the generics are easier when I can refer to <E>. We still want
     * an Entry<?> so that entrySet() can return Iterable<Entry<?>> instead of Iterable<Entry<E>>.
     * That way, it can be returned directly from DuplicateGroupedAndTyped.entrySet() without our
     * having to generalize *its* return type to Iterable<? extends Entry<?>>.
     */
    private Multiset.Entry<?> unwrapKey(Multiset.Entry<Wrapper<E>> input) {
      return immutableEntry(input.getElement().get(), input.getCount());
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

    static <E extends @Nullable Object> NonHashingMultiset<E> create() {
      return new NonHashingMultiset<>();
    }
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
    private final NonHashingMultiset<?> valuesAndMaybeTypes;
    private final @Nullable String homogeneousTypeToDisplay;

    private DuplicateGroupedAndTyped(
        NonHashingMultiset<?> valuesAndMaybeTypes, @Nullable String homogeneousTypeToDisplay) {
      this.valuesAndMaybeTypes = valuesAndMaybeTypes;
      this.homogeneousTypeToDisplay = homogeneousTypeToDisplay;
    }

    @Nullable String getHomogeneousTypeToDisplay() {
      return homogeneousTypeToDisplay;
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

    static DuplicateGroupedAndTyped create(
        NonHashingMultiset<?> valuesAndMaybeTypes, @Nullable String homogeneousTypeToDisplay) {
      return new DuplicateGroupedAndTyped(valuesAndMaybeTypes, homogeneousTypeToDisplay);
    }
  }

  /**
   * Returns a new collection containing all elements in {@code items} for which there exists at
   * least one element in {@code itemsToCheck} that has the same {@link String#valueOf(Object)}
   * value without being equal.
   *
   * <p>Example: {@code retainMatchingToString([1L, 2L, 2L], [2, 3]) == [2L, 2L]}
   */
  static List<@Nullable Object> retainMatchingToString(
      Iterable<?> items, Iterable<?> itemsToCheck) {
    ListMultimap<String, @Nullable Object> stringValueToItemsToCheck = ArrayListMultimap.create();
    for (Object itemToCheck : itemsToCheck) {
      stringValueToItemsToCheck.put(stringValueForFailure(itemToCheck), itemToCheck);
    }

    List<@Nullable Object> result = new ArrayList<>();
    for (Object item : items) {
      for (Object itemToCheck : stringValueToItemsToCheck.get(stringValueForFailure(item))) {
        // This approach avoids hashing the items themselves.
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
   * has the same {@link String#valueOf(Object)} value without being equal.
   *
   * <p>Example: {@code hasMatchingToStringPair([1L, 2L], [1]) == true}
   */
  static boolean hasMatchingToStringPair(Iterable<?> items1, Iterable<?> items2) {
    // Bail early for empty iterables to avoid calling hashCode() on the elements unnecessarily.
    return !isEmpty(items1)
        && !isEmpty(items2)
        && !retainMatchingToString(items1, items2).isEmpty();
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
      return longName(item.getClass());
    }
  }

  /**
   * Returns the name of the single type of all given items or {@code null} if no such type exists.
   */
  private static @Nullable String getHomogeneousTypeName(Iterable<?> items) {
    /*
     * TODO(cpovirk): If we remove the null case below, just collect all the type names to a Set and
     * return singleOrNull()?
     */
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

  static <T extends @Nullable Object> Collection<T> iterableToCollection(Iterable<T> iterable) {
    return iterable instanceof Collection
        // Should be safe to assume that any Iterable implementing Collection isn't a one-shot
        // iterable, right? I sure hope so.
        ? (Collection<T>) iterable
        : newArrayList(iterable);
  }

  static <T extends @Nullable Object> List<T> iterableToList(Iterable<T> iterable) {
    return iterable instanceof List ? (List<T>) iterable : newArrayList(iterable);
  }

  /**
   * Returns an {@link Iterable} with each empty {@link String} replaced by a non-empty human
   * understandable indicator for an empty {@link String}.
   *
   * @return a new {@link Iterable} with each empty {@link String} replaced or the given {@link
   *     Iterable} if it contains no empty {@link String}
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

  private static final ImmutableMap<Class<?>, String> CLASS_TO_NAME = makeClassToNameMap();

  /*
   * The LinkedHash* classes are typealiases for the Hash* classes under
   * Kotlin/Native, so they're equal at runtime there.
   */
  @SuppressWarnings("EqualsIncompatibleType")
  private static ImmutableMap<Class<?>, String> makeClassToNameMap() {
    ImmutableMap.Builder<Class<?>, String> b = ImmutableMap.builder();
    // Part 1:
    // entries from https://kotlinlang.org/docs/java-interop.html#mapped-types
    b.put(Byte.class, "Byte");
    b.put(Short.class, "Short");
    b.put(Integer.class, "Integer");
    b.put(Long.class, "Long");
    b.put(Character.class, "Character");
    b.put(Float.class, "Float");
    b.put(Double.class, "Double");
    b.put(Boolean.class, "Boolean");
    b.put(Object.class, "Object");
    b.put(Cloneable.class, "Cloneable");
    b.put(Comparable.class, "Comparable");
    b.put(Enum.class, "Enum");
    b.put(Annotation.class, "Annotation");
    b.put(CharSequence.class, "CharSequence");
    b.put(String.class, "String");
    b.put(Number.class, "Number");
    b.put(Throwable.class, "Throwable");
    b.put(Exception.class, "Exception");
    // TODO(cpovirk): What do we do about collections, with their Foo-MutableFoo split?

    // Part 2:
    // results for "public actual typealias", minus J2CL-incompatible CharacterCodingException
    b.put(Error.class, "Error");
    b.put(RuntimeException.class, "RuntimeException");
    b.put(IllegalArgumentException.class, "IllegalArgumentException");
    b.put(IllegalStateException.class, "IllegalStateException");
    b.put(IndexOutOfBoundsException.class, "IndexOutOfBoundsException");
    b.put(UnsupportedOperationException.class, "UnsupportedOperationException");
    b.put(ArithmeticException.class, "ArithmeticException");
    b.put(NumberFormatException.class, "NumberFormatException");
    b.put(NullPointerException.class, "NullPointerException");
    b.put(ClassCastException.class, "ClassCastException");
    b.put(AssertionError.class, "AssertionError");
    b.put(NoSuchElementException.class, "NoSuchElementException");
    b.put(ConcurrentModificationException.class, "ConcurrentModificationException");
    b.put(Comparator.class, "Comparator");
    b.put(AutoCloseable.class, "AutoCloseable");
    b.put(RandomAccess.class, "RandomAccess");
    b.put(ArrayList.class, "ArrayList");
    b.put(HashMap.class, "HashMap");
    //noinspection ConstantConditions
    if (!LinkedHashMap.class.equals(HashMap.class)) {
      b.put(LinkedHashMap.class, "LinkedHashMap");
    }
    b.put(HashSet.class, "HashSet");
    //noinspection ConstantConditions
    if (!LinkedHashSet.class.equals(HashSet.class)) {
      b.put(LinkedHashSet.class, "LinkedHashSet");
    }
    b.put(CancellationException.class, "CancellationException");
    b.put(Appendable.class, "Appendable");
    b.put(StringBuilder.class, "StringBuilder");

    // Part 3:
    // other types that commonly appear in instanceOf assertions
    b.put(TimeoutException.class, "TimeoutException");
    b.put(ExecutionException.class, "ExecutionException");
    b.put(InterruptedException.class, "InterruptedException");
    b.put(IOException.class, "IOException");
    b.put(VerifyException.class, "VerifyException");
    return b.buildOrThrow();
  }

  static String longName(Class<?> clazz) {
    String name = CLASS_TO_NAME.get(clazz);
    if (name != null) {
      return name;
    }
    Class<?> arrayComponentType = clazz.getComponentType();
    if (arrayComponentType != null) {
      return longName(arrayComponentType) + "[]";
    }
    return firstNonNull(clazz.getCanonicalName(), clazz.getName());
  }

  @SafeVarargs
  static <E> ImmutableList<E> concat(Iterable<? extends E>... inputs) {
    return ImmutableList.copyOf(Iterables.concat(inputs));
  }

  static <E> ImmutableList<E> append(E[] array, E e) {
    return ImmutableList.<E>builderWithExpectedSize(array.length + 1).add(array).add(e).build();
  }

  static <E> ImmutableList<E> append(ImmutableList<? extends E> list, E e) {
    return ImmutableList.<E>builderWithExpectedSize(list.size() + 1).addAll(list).add(e).build();
  }

  static <E> ImmutableList<E> sandwich(E first, E[] array, E last) {
    return ImmutableList.<E>builderWithExpectedSize(array.length + 2)
        .add(first)
        .add(array)
        .add(last)
        .build();
  }

  /**
   * Performs an unchecked conversion from a varargs array to a {@link List}, treating a null array
   * from {@code caller(null)} as a single null element.
   */
  static <E extends @Nullable Object> List<E> listifyNullableVarargs(
      @Nullable E @Nullable [] expected) {
    return expected == null ? asList((E) null) : asList(expected);
  }

  // TODO: b/316358623 - Inline this helper method after fixing our nullness checker to not need it.
  @SuppressWarnings("nullness") // the aforementioned checker bug
  static <E extends @Nullable Object> List<E> asList(E... a) {
    return Arrays.asList(a);
  }
}
