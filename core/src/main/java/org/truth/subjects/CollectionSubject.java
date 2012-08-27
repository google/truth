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
package org.truth.subjects;

import static org.truth.subjects.SubjectUtils.accumulate;
import static org.truth.subjects.SubjectUtils.countOf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.truth.FailureStrategy;

import com.google.common.annotations.GwtCompatible;

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

  /**
   * Attests that a Collection is empty or fails.
   */
  @Override public And<S> isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
    return nextChain();
  }

  /**
   * Attests that a Collection contains at least one of the provided
   * objects or fails.
   */
  public And<S> containsAnyOf(Object first, Object second, Object ... rest) {
    Collection<?> collection = getSubject();
    for (Object item : accumulate(first, second, rest)) {
      if (collection.contains(item)) {
        return nextChain();
      }
    }
    fail("contains", accumulate(first, second, rest));
    return nextChain();
  }


  /**
   * Attests that a Collection contains all of the provided objects or fails.
   * This copes with duplicates in both the Collection and the parameters.
   */
  public Ordered<S> contains(Object first, Object second, Object ... rest) {
    Collection<?> collection = getSubject();
    // Arrays.asList() does not support remove() so we need a mutable copy.
    List<Object> required = accumulate(first, second, rest);
    for (Object item : collection) {
      required.remove(item);
    }
    if (!required.isEmpty()) {
      // Try and make a useful message when dealing with duplicates.
      Set<Object> missing = new HashSet<Object>(required);
      Object[] params = new Object[missing.size()];
      int n = 0;
      for (Object item : missing) {
        int count = countOf(item, accumulate(first, second, rest));
        params[n++] = (count > 1) ? count + " copies of " + item : item;
      }
      fail("contains", params);
    }

    final List<?> expectedItems = accumulate(first, second, rest);
    return new Ordered<S>() {
      @Override public And<S> inOrder() {
        Iterator<T> actualItems = getSubject().iterator();
        for (Object expected : expectedItems) {
          if (!actualItems.hasNext()) {
            fail("iterates through", expectedItems);
          } else {
            Object actual = actualItems.next();
            if (actual == expected || actual != null && actual.equals(expected)) {
              continue;
            } else {
              fail("iterates through", expectedItems);
            }
          }
        }
        if (actualItems.hasNext()) {
          fail("iterates through", expectedItems);
        }
        return nextChain();
      }
      @Override public S and() {
        return nextChain().and();
      }
    };
  }

}
