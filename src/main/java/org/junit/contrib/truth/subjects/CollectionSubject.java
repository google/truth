/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.junit.contrib.truth.subjects;

import org.junit.contrib.truth.FailureStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionSubject<T, C extends Collection<T>> extends Subject<C> {

  public static <T, C extends Collection<T>> CollectionSubject<T, C> create(
      FailureStrategy failureStrategy, C list) {
    return new CollectionSubject<T, C>(failureStrategy, list);
  }

  CollectionSubject(FailureStrategy failureStrategy, C list) {
    super(failureStrategy, list);
  }

  /**
   * Attests that a Collection contains the provided object or fails.
   */
  public CollectionSubject<T,C> contains(T item) {
    if (!getSubject().contains(item)) {
      fail("contains", item);
    }
    return this;
  }

  /**
   * Attests that a Collection contains at least one of the provided
   * objects or fails.
   */
  public CollectionSubject<T,C> containsAnyOf(T ... items) {
    Collection<T> collection = getSubject();
    for (T item : items) {
      if (collection.contains(item)) {
        return this;
      }
    }
    fail("contains", (Object[])items);
    return this;
  }

  /**
   * Attests that a Collection contains all of the provided objects or fails.
   * This copes with duplicates in both the Collection and the parameters.
   */
  public CollectionSubject<T,C> containsAllOf(T ... items) {
    Collection<T> collection = getSubject();
    // Arrays.asList() does not support remove() so we need a mutable copy.
    List<T> required = new ArrayList<T>(Arrays.asList(items));
    for (T item : collection) {
      required.remove(item);
    }
    if (!required.isEmpty()) {
      // Try and make a useful message when dealing with duplicates.
      Set<T> missing = new HashSet<T>(required);
      Object[] params = new Object[missing.size()];
      int n = 0;
      for (T item : missing) {
        int count = countOf(item, items);
        params[n++] = (count > 1) ? count + " copies of " + item : item;
      }
      fail("contains", params);
    }
    return this;
  }

  private static <T> int countOf(T t, T... items) {
    int count = 0;
    for (T item : items) {
      if (t == null ? (item == null) : t.equals(item)) {
        count++;
      }
    }
    return count;
  }
}
