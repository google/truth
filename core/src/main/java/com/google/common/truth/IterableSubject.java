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

import static com.google.common.truth.SubjectUtils.accumulate;
import static com.google.common.truth.SubjectUtils.countDuplicates;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Kevin Bourrillion
 */
public class IterableSubject<S extends IterableSubject<S, T, C>, T, C extends Iterable<T>>
    extends Subject<S, C> implements Contains {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T, C extends Iterable<T>> IterableSubject<? extends IterableSubject<?, T, C>, T, C> create(
      FailureStrategy failureStrategy, Iterable<T> list) {
    return new IterableSubject(failureStrategy, list);
  }

  // TODO: Arguably this should even be package private
  protected IterableSubject(FailureStrategy failureStrategy, C list) {
    super(failureStrategy, list);
  }

  /**
   * Attests that the subject holds no more objects, or fails.
   */
  public void isEmpty() {
    if (getSubject().iterator().hasNext()) {
      fail("is empty");
    }
  }

  /**
   * Attests that the subject holds one or more objects, or fails
   */
  public void isNotEmpty() {
    if (!getSubject().iterator().hasNext()) {
      fail("is not empty");
    }
  }

  /**
   * Asserts that the items are supplied in the order given by the iterable. If
   * the iterable under test and/or the {@code expectedItems} do not provide
   * iteration order guarantees (say, {@link Set<T>}s), this method may provide
   * unexpected results.  Consider using {@link #is(T)} in such cases, or using
   * collections and iterables that provide strong order guarantees.
   */
  public void iteratesAs(Iterable<?> expectedItems) {
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
  }

  /**
   * @deprecated use {@link #iteratesAs(T...)}
   */
  @Deprecated
  public void iteratesOverSequence(Object... expectedItems) {
    iteratesAs(expectedItems);
  }

  /**
   * Asserts that the items are supplied in the order given by the iterable. If
   * the iterable under test does not provide iteration order guarantees (say,
   * a {@link Set<T>}), this method is not suitable for asserting that order.
   * Consider using {@link #is(T)}
   */
  public void iteratesAs(Object... expectedItems) {
    iteratesAs(Arrays.asList(expectedItems));
  }

  @Override
  public void contains(Object element) {
    if (!Iterables.contains(getSubject(), element)) {
      fail("contains", element);
    }
  }

  @Override
  public void doesNotContain(Object element) {
    if (Iterables.contains(getSubject(), element)) {
      fail("does not contain", element);
    }
  }

  @Override
  public void containsAnyOf(Object first, Object... rest) {
    contains("contains any of", accumulate(first, rest));
  }

  @Override
  public void containsAnyIn(Iterable<?> expected) {
    contains("contains any element in", expected);
  }

  private void contains(String failVerb, Iterable<?> expected) {
    for (Object item : expected) {
      if (Iterables.contains(getSubject(), item)) {
        return;
      }
    }
    fail(failVerb, expected);
  }

  @Override
  public Ordered containsAllOf(Object first, Object... rest) {
    return containsAll("contains all of", accumulate(first, rest));
  }

  @Override
  public Ordered containsAllIn(Iterable<?> expected) {
    return containsAll("contains all elements in", expected);
  }

  private Ordered containsAll(String failVerb, Iterable<?> expected) {
    Collection<?> toRemove = Lists.newArrayList(expected);
    // remove each item in the subject, as many times as it occurs in the subject.
    for (Object item : getSubject()) {
      toRemove.remove(item);
    }
    if (!toRemove.isEmpty()) {
      failWithBadResults(failVerb, expected, "is missing", countDuplicates(toRemove));
    }
    return new InOrder("contains all elements in order", expected);
  }

  @Override
  public Ordered containsOnlyElements(Object first, Object... rest) {
    return containsExactly("contains only", accumulate(first, rest));
  }

  @Override
  public Ordered containsOnlyElementsIn(Iterable<?> expected) {
    return containsExactly("contains only the elements in", expected);
  }

  private Ordered containsExactly(String failVerb, Iterable<?> required) {
    Collection<?> toRemove = Lists.newArrayList(required);
    Collection<Object> extra = new ArrayList<Object>();
    // remove each item in the subject, as many times as it occurs in the subject.
    for (Object item : getSubject()) {
      if (!toRemove.remove(item)) {
        extra.add(item);
      }
    }
    if (!toRemove.isEmpty()) {
      failWithBadResults(failVerb, required, "is missing", countDuplicates(toRemove));
    }
    if (!extra.isEmpty()) {
      failWithBadResults(failVerb, required, "has unexpected items", countDuplicates(extra));
    }
    return new InOrder("contains only these elements in order", required);
  }

  @Override
  public void containsNoneOf(Object first, Object... rest) {
    containsNone("contains none of", accumulate(first, rest));
  }

  @Override
  public void containsNoneIn(Iterable<?> excluded) {
    containsNone("contains no elements in", excluded);
  }

  private void containsNone(String failVerb, Iterable<?> excluded) {
    Collection<Object> present = new ArrayList<Object>();
    for (Object item : Sets.newHashSet(excluded)) {
      if (Iterables.contains(getSubject(), item)) {
        present.add(item);
      }
    }
    if (!present.isEmpty()) {
      failWithBadResults(failVerb, excluded, "contains", present);
    }
  }

  private class InOrder implements Ordered {
    private final String check;
    private final Iterable<?> required;

    InOrder(String check, Iterable<?> required) {
      this.check = check;
      this.required = required;
    }

    @Override public void inOrder() {
      Iterator<T> actualItems = getSubject().iterator();
      for (Object expected : required) {
        if (!actualItems.hasNext()) {
          fail(check, required);
        } else {
          Object actual = actualItems.next();
          if (actual == expected || actual != null && actual.equals(expected)) {
            continue;
          } else {
            fail(check, required);
          }
        }
      }
      if (actualItems.hasNext()) {
        fail(check, required);
      }
    }
  }
}
