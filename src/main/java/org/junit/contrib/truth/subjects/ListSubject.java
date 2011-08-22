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

import org.junit.contrib.truth.FailureStrategy;

import java.util.Comparator;
import java.util.List;

public class ListSubject<T, C extends List<T>>
    extends CollectionSubject<T, C> {

  public static <T> ListSubject<T, List<T>> create(
      FailureStrategy failureStrategy, List<T> list) {
    return new ListSubject<T, List<T>>(failureStrategy, list);
  }

  protected ListSubject(FailureStrategy failureStrategy, C list) {
    super(failureStrategy, list);
  }

  /**
   * Attests that a List contains the specified sequence.
   */
  public ListSubject<T,C> containsSequence(List<T> sequence) {
    if (sequence.isEmpty()) {
      return this;
    }
    List<T> list = getSubject();
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
        return this;
      }
      list = list.subList(first + 1, list.size());
    }
    fail("contains sequence", sequence);
    return this;
  }

  /**
   * Attests that a List is strictly ordered according to the natural ordering of its elements.
   * Null elements are not permitted.
   * 
   * @throws ClassCastException if any pair of elements is not mutually Comparable.
   * @throws NullPointerException if any element is null.
   */
  public ListSubject<T,C> isOrdered() {
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
  public ListSubject<T,C> isPartiallyOrdered() {
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
  public ListSubject<T,C> isOrdered(final Comparator<T> comparator) {
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
  public ListSubject<T,C> isPartiallyOrdered(final Comparator<T> comparator) {
    return pairwiseCheck(new PairwiseChecker<T>() {
      @Override public void check(T prev, T next) {
        if (comparator.compare(prev, next) > 0) {
          fail("is partially ordered", prev, next);
        }
      }
    });
  }

  private ListSubject<T,C> pairwiseCheck(PairwiseChecker<T> checker) {
    List<T> list = getSubject();
    if (list.size() > 1) {
      T prev = list.get(0);
      for (int n = 1; n < list.size(); n++) {
        T next = list.get(n);
        checker.check(prev, next);
        prev = next;
      }
    }
    return this;
  }

  private interface PairwiseChecker<T> {
    void check(T prev, T next);
  }
}
