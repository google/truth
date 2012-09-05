/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
 * Copyright (c) 2012 Google, Inc.
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
package org.truth0.subjects;

import static org.truth0.subjects.SubjectUtils.accumulate;
import static org.truth0.subjects.SubjectUtils.countOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.truth0.FailureStrategy;

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
   * Attests that a Collection is empty or fails.
   */
  @Override public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  public Has<T, C> has() {
    return new Has<T, C>() {
      @Override public void item(T item) {
        if (!getSubject().contains(item)) {
          fail("has item", item);
        }
      }

      @Override public void anyOf(T first) {
        anyFrom(accumulate(first));
      }
      @Override public void anyOf(T first, T second, T ... rest) {
        anyFrom(accumulate(first, second, rest));
      }
      @Override public void anyFrom(Collection<T> col) {
        for (Object item : col) {
          if (getSubject().contains(item)) {
            return;
          }
        }
        fail("contains", col);
      }

      @Override public Ordered allOf(T first) {
        return allFrom(accumulate(first));
      }
      @Override public Ordered allOf(T first, T second, T ... rest) {
        return allFrom(accumulate(first, second, rest));
      }
      @Override public Ordered allFrom(final Collection<T> required) {
        Collection<T> toRemove = new ArrayList<T>(required);
        // remove each item in the subject, as many times as it occurs in the subject.
        for (Object item : getSubject()) {
          toRemove.remove(item);
        }
        if (!toRemove.isEmpty()) {
          // Try and make a useful message when dealing with duplicates.
          Set<T> missing = new HashSet<T>(toRemove);
          Object[] params = new Object[missing.size()];
          int n = 0;
          for (T item : missing) {
            int count = countOf(item, toRemove);
            params[n++] = (count > 1) ? count + " copies of " + item : item;
          }
          failWithBadResults("has all of", required, "is missing", Arrays.asList(params));
        }

        return new Ordered() {
          @Override public void inOrder() {
            Iterator<T> actualItems = getSubject().iterator();
            for (Object expected : required) {
              if (!actualItems.hasNext()) {
                fail("has all in order", required);
              } else {
                Object actual = actualItems.next();
                if (actual == expected || actual != null && actual.equals(expected)) {
                  continue;
                } else {
                  fail("has all in order", required);
                }
              }
            }
            if (actualItems.hasNext()) {
              fail("has all in order", required);
            }
          }
        };
      }

      /*@Override*/ public void exactly(T first) {
        exactlyAs(accumulate(first));
      }
      /*@Override*/ public void exactly(T first, T second, T ... rest) {
        exactlyAs(accumulate(first, second, rest));
      }
      /*@Override*/ public void exactlyAs(Collection<T> col) {
        throw new UnsupportedOperationException("Not yet implemented.");
      }
    };
  }

  public interface Has<E, C extends Collection<E>> {
    /**
     * Attests that a Collection contains at least the item
     */
    void item(E item);

    /**
     * Attests that a Collection contains at least one of the provided objects
     * or fails.
     */
    void anyOf(E first);

    /**
     * Attests that a Collection contains at least one of the provided objects
     * or fails.
     */
    void anyOf(E first, E second, E... rest);

    /**
     * Attests that a Collection contains at least one of the objects contained
     * in the provided collection or fails.
     */
    void anyFrom(Collection<E> expected);

    /**
     * Attests that a Collection contains at least all of the provided objects
     * or fails, coping with duplicates in both the Collection and the
     * parameters.
     */
    Ordered allOf(E first);

    /**
     * Attests that a Collection contains at least all of the provided objects
     * or fails, coping with duplicates in both the Collection and the
     * parameters.
     */
    Ordered allOf(E first, E second, E... rest);

    /**
     * Attests that a Collection contains at least all of the objects contained
     * in the provided collection or fails, coping with duplicates in both
     * the Collection and the parameters.
     */
    Ordered allFrom(Collection<E> expected);

    /**
     * Attests that a Collection contains at all of the provided objects and
     * only these objects or fails. This copes with duplicates in both the
     * Collection and the parameters.
     */
    //void exactly(E first);

    /**
     * Attests that a Collection contains at all of the provided objects and
     * only these objects or fails. This copes with duplicates in both the
     * Collection and the parameters.
     */
    //void exactly(E first, E second, E... rest);

    /**
     * Attests that a Collection contains at all of the objects contained in the
     * provided collection and only these objects or fails. This copes with
     * duplicates in both the Collection and the parameters.
     */
    //void exactlyAs(Collection<E> expected);
  }
}
