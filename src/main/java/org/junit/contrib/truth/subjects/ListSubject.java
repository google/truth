/*
 * Copyright (c) 2011 David Beaumont
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

import java.util.Comparator;
import java.util.List;

import org.junit.contrib.truth.FailureStrategy;

public class ListSubject<S extends ListSubject<S, T, C>, T, C extends List<T>>
    extends CollectionSubject<S, T, C> {

  @SuppressWarnings("unchecked")
  public static <T, C extends List<T>> ListSubject<? extends ListSubject<?, T, C>, T, C> create(
      FailureStrategy failureStrategy, List<T> list) {
    return new ListSubject(failureStrategy, list);
  }

  protected ListSubject(FailureStrategy failureStrategy, C list) {
    super(failureStrategy, list);
  }

  /**
   * Attests that a List contains the specified sequence.
   */
  public And<S> containsSequence(List<?> sequence) {
    if (sequence.isEmpty()) {
      return nextChain();
    }
    List<?> list = getSubject();
    while (true) {
      int first = list.indexOf(sequence.get(0));
      if (first < 0) {
        break;    // Not found
      }
      int last = first + sequence.size();
      if (last > list.size()) {
        break;    // Not enough room left
      }
      if (sequence.equals(list.subList(first, last))) {
        return nextChain();
      }
      list = list.subList(first + 1, list.size());
    }
    fail("contains sequence", sequence);
    return nextChain();
  }

  /**
   * Attests that a List is strictly ordered according to the natural ordering of its elements.
   * Null elements are not permitted.
   * 
   * @throws ClassCastException if any pair of elements is not mutually Comparable.
   * @throws NullPointerException if any element is null.
   */
  public And<S> isOrdered() {
    return pairwiseCheck(new PairwiseChecker<T>() {
      @SuppressWarnings("unchecked")
      @Override public void check(T prev, T next) {
        if (((Comparable<T>) prev).compareTo(next) >= 0) {
          fail("is strictly ordered", prev, next);
        }
      }
    });
  }

  /**
   * Attests that a List is partially ordered according to the natural ordering of its elements.
   * Null elements are not permitted.
   * 
   * @throws ClassCastException if any pair of elements is not mutually Comparable.
   * @throws NullPointerException if any element is null.
   */
  public And<S> isPartiallyOrdered() {
    return pairwiseCheck(new PairwiseChecker<T>() {
      @SuppressWarnings("unchecked")
      @Override public void check(T prev, T next) {
        if (((Comparable<T>) prev).compareTo(next) > 0) {
          fail("is partially ordered", prev, next);
        }
      }
    });
  }

  /**
   * Attests that a List is strictly ordered according to the given comparator.
   * Null elements are not permitted.
   * 
   * @throws ClassCastException if any pair of elements is not mutually Comparable.
   * @throws NullPointerException if any element is null.
   */
  public And<S> isOrdered(final Comparator<T> comparator) {
    return pairwiseCheck(new PairwiseChecker<T>() {
      @Override public void check(T prev, T next) {
        if (comparator.compare(prev, next) >= 0) {
          fail("is strictly ordered", prev, next);
        }
      }
    });
  }

  /**
   * Attests that a List is partially ordered according to the given comparator.
   * Null elements are not permitted.
   * 
   * @throws ClassCastException if any pair of elements is not mutually Comparable.
   * @throws NullPointerException if any element is null.
   */
  public And<S> isPartiallyOrdered(final Comparator<T> comparator) {
    return pairwiseCheck(new PairwiseChecker<T>() {
      @Override public void check(T prev, T next) {
        if (comparator.compare(prev, next) > 0) {
          fail("is partially ordered", prev, next);
        }
      }
    });
  }

  private And<S> pairwiseCheck(PairwiseChecker<T> checker) {
    List<T> list = getSubject();
    if (list.size() > 1) {
      T prev = list.get(0);
      for (int n = 1; n < list.size(); n++) {
        T next = list.get(n);
        checker.check(prev, next);
        prev = next;
      }
    }
    return nextChain();
  }

  private interface PairwiseChecker<T> {
    void check(T prev, T next);
  }
}
