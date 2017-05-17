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

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility methods used in {@code Subject<T>} implementors.
 *
 * @author Christian Gruber
 * @author Jens Nyman
 */
final class SubjectUtils {
  private SubjectUtils() {}

  static <T> List<T> accumulate(T first, T second, T... rest) {
    // rest should never be deliberately null, so assume that the caller passed null
    // in the third position but intended it to be the third element in the array of values.
    // Javac makes the opposite inference, so handle that here.
    List<T> items = new ArrayList<T>(2 + ((rest == null) ? 1 : rest.length));
    items.add(first);
    items.add(second);
    if (rest == null) {
      items.add(null);
    } else {
      items.addAll(Arrays.asList(rest));
    }
    return items;
  }

  static <T> int countOf(T t, Iterable<T> items) {
    int count = 0;
    for (T item : items) {
      if (t == null ? (item == null) : t.equals(item)) {
        count++;
      }
    }
    return count;
  }

  static <T> List<Object> countDuplicates(Collection<T> items) {
    // We use a List to de-dupe instead of a Set in case the elements don't have a proper
    // .hashCode() method (e.g., MessageSet from old versions of protobuf).
    List<T> itemSet = new ArrayList<T>();
    for (T item : items) {
      if (!itemSet.contains(item)) {
        itemSet.add(item);
      }
    }
    Object[] params = new Object[itemSet.size()];
    int n = 0;
    for (T item : itemSet) {
      int count = countOf(item, items);
      params[n++] = (count > 1) ? item + " [" + count + " copies]" : item;
    }
    return Arrays.asList(params);
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
    Optional<String> homogeneousTypeName = getHomogeneousTypeName(items);

    return homogeneousTypeName.isPresent()
        ? StringUtil.format("%s (%s)", countDuplicates(items), homogeneousTypeName.get())
        : countDuplicates(addTypeInfoToEveryItem(items)).toString();
  }

  /**
   * Makes a String representation of {@code items} with additional class info.
   *
   * <p>Example: {@code iterableToStringWithTypeInfo([1, 2]) == "[1, 2] (java.lang.Integer)"} and
   * {@code iterableToStringWithTypeInfo([1, 2L]) == "[1 (java.lang.Integer), 2 (java.lang.Long)]"}.
   */
  static String iterableToStringWithTypeInfo(Iterable<?> itemsIterable) {
    Collection<?> items = iterableToCollection(itemsIterable);
    Optional<String> homogeneousTypeName = getHomogeneousTypeName(items);

    if (homogeneousTypeName.isPresent()) {
      return StringUtil.format("%s (%s)", items, homogeneousTypeName.get());
    } else {
      return addTypeInfoToEveryItem(items).toString();
    }
  }

  /**
   * Returns a new collection containing all elements in {@code items} for which there exists at
   * least one element in {@code itemsToCheck} that has the same {@code toString()} value without
   * being equal.
   *
   * <p>Example: {@code retainMatchingToString([1L, 2L, 2L], [2, 3]) == [2L, 2L]}
   */
  static List<Object> retainMatchingToString(Iterable<?> items, Iterable<?> itemsToCheck) {
    SetMultimap<String, Object> stringValueToItemsToCheck =
        MultimapBuilder.hashKeys().hashSetValues().build();
    for (Object itemToCheck : itemsToCheck) {
      stringValueToItemsToCheck.put(String.valueOf(itemToCheck), itemToCheck);
    }

    List<Object> result = Lists.newArrayList();
    for (Object item : items) {
      for (Object itemToCheck : stringValueToItemsToCheck.get(String.valueOf(item))) {
        if (!Objects.equal(itemToCheck, item)) {
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
    return !retainMatchingToString(items1, items2).isEmpty();
  }

  static String objectToTypeName(Object item) {
    if (item == null) {
      // The name "null type" comes from the interface javax.lang.model.type.NullType.
      return "null type";
    } else if (item instanceof Map.Entry) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) item;
      return StringUtil.format(
          "Map.Entry<%s, %s>",
          objectToTypeName(entry.getKey()), objectToTypeName(entry.getValue()));
    } else {
      return item.getClass().getName();
    }
  }

  /**
   * Returns the name of the single type of all given items or {@link Optional#absent()} if no such
   * type exists.
   */
  private static Optional<String> getHomogeneousTypeName(Iterable<?> items) {
    Optional<String> homogeneousTypeName = Optional.absent();
    for (Object item : items) {
      if (item == null) {
        return Optional.absent();
      } else if (!homogeneousTypeName.isPresent()) {
        // This is the first item
        homogeneousTypeName = Optional.of(objectToTypeName(item));
      } else if (!objectToTypeName(item).equals(homogeneousTypeName.get())) {
        // items is a heterogeneous collection
        return Optional.absent();
      }
    }
    return homogeneousTypeName;
  }

  private static List<String> addTypeInfoToEveryItem(Iterable<?> items) {
    List<String> itemsWithTypeInfo = Lists.newArrayList();
    for (Object item : items) {
      itemsWithTypeInfo.add(StringUtil.format("%s (%s)", item, objectToTypeName(item)));
    }
    return itemsWithTypeInfo;
  }

  static <T> Collection<T> iterableToCollection(Iterable<T> iterable) {
    if (iterable instanceof Collection) {
      // Should be safe to assume that any Iterable implementing Collection isn't a one-shot
      // iterable, right? I sure hope so.
      return (Collection<T>) iterable;
    } else {
      return Lists.newArrayList(iterable);
    }
  }

  static <T> List<T> iterableToList(Iterable<T> iterable) {
    if (iterable instanceof List) {
      return (List<T>) iterable;
    } else {
      return Lists.newArrayList(iterable);
    }
  }
}
