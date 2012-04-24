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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.util.GwtCompatible;

@GwtCompatible
public class CollectionSubject<S extends CollectionSubject<S, T, C>, T, C extends Collection<T>> extends IterableSubject<S, T, C> {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T, C extends Collection<T>> CollectionSubject<? extends CollectionSubject<?, T, C>, T, C> create(
      FailureStrategy failureStrategy, Collection<T> list) {
    return new CollectionSubject(failureStrategy, list);
  }

  // TODO: Arguably this should even be package private
  protected CollectionSubject(FailureStrategy failureStrategy, C list) {
    super(failureStrategy, list);
  }

  /**
   * Attests that a Collection contains the provided object or fails.
   */
  @Override public And<S> contains(Object item) {
    if (!getSubject().contains(item)) {
      fail("contains", item);
    }
    return nextChain();
  }

  @Override public And<S> isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("isEmpty");
    }
    return nextChain();
  }

  /**
   * Attests that a Collection contains at least one of the provided
   * objects or fails.
   */
  public And<S> containsAnyOf(Object ... items) {
    Collection<?> collection = getSubject();
    for (Object item : items) {
      if (collection.contains(item)) {
        return nextChain();
      }
    }
    fail("contains", (Object[])items);
    return nextChain();
  }

  /**
   * Attests that a Collection contains all of the provided objects or fails.
   * This copes with duplicates in both the Collection and the parameters.
   */
  public And<S> containsAllOf(Object ... items) {
    Collection<?> collection = getSubject();
    // Arrays.asList() does not support remove() so we need a mutable copy.
    List<Object> required = new ArrayList<Object>(Arrays.asList(items));
    for (Object item : collection) {
      required.remove(item);
    }
    if (!required.isEmpty()) {
      // Try and make a useful message when dealing with duplicates.
      Set<Object> missing = new HashSet<Object>(required);
      Object[] params = new Object[missing.size()];
      int n = 0;
      for (Object item : missing) {
        int count = countOf(item, items);
        params[n++] = (count > 1) ? count + " copies of " + item : item;
      }
      fail("contains", params);
    }
    return nextChain();
  }

  private static int countOf(Object t, Object... items) {
    int count = 0;
    for (Object item : items) {
      if (t == null ? (item == null) : t.equals(item)) {
        count++;
      }
    }
    return count;
  }
}
