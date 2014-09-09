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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.truth.SubjectUtils.accumulate;
import static com.google.common.truth.SubjectUtils.countDuplicates;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * @author Kevin Bourrillion
 */
public class IterableSubject<S extends IterableSubject<S, T, C>, T, C extends Iterable<T>>
    extends Subject<S, C> {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T, C extends Iterable<T>> IterableSubject<? extends IterableSubject<?, T, C>, T, C>
      create(FailureStrategy failureStrategy, Iterable<T> list) {
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
    if (!Iterables.isEmpty(getSubject())) {
      fail("is empty");
    }
  }

  /**
   * Attests that the subject holds one or more objects, or fails
   */
  public void isNotEmpty() {
    if (Iterables.isEmpty(getSubject())) {
      fail("is not empty");
    }
  }

  /**
   * Asserts that an Iterable has a specific size.
   */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = Iterables.size(getSubject());
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /**
   * Asserts that the items are supplied in the order given by the iterable. If
   * the iterable under test and/or the {@code expectedItems} do not provide
   * iteration order guarantees (say, {@link Set}{@code <?>}s), this method may provide
   * unexpected results.  Consider using {@link #isEqualTo(Object)} in such cases, or using
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
   * @deprecated use {@link #iteratesAs(Object...)}
   */
  @Deprecated
  public void iteratesOverSequence(Object... expectedItems) {
    iteratesAs(expectedItems);
  }

  /**
   * Asserts that the items are supplied in the order given by the iterable. If
   * the iterable under test does not provide iteration order guarantees (say,
   * a {@link Set}{@code <?>}), this method is not suitable for asserting that order.
   * Consider using {@link #isEqualTo(Object)}
   */
  public void iteratesAs(Object... expectedItems) {
    iteratesAs(Arrays.asList(expectedItems));
  }

  /**
   * Attests (with a side-effect failure) that the subject contains the
   * supplied item.
   */
  public void contains(@Nullable Object element) {
    if (!Iterables.contains(getSubject(), element)) {
      failWithRawMessage("<%s> unexpectedly does not contain <%s>", getSubject(), element);
    }
  }

  /**
   * Attests (with a side-effect failure) that the subject does not contain
   * the supplied item.
   */
  public void doesNotContain(@Nullable Object element) {
    if (Iterables.contains(getSubject(), element)) {
      failWithRawMessage("<%s> unexpectedly contains <%s>", getSubject(), element);
    }
  }

  /**
   * Attests that the subject contains at least one of the provided objects
   * or fails.
   */
  public void containsAnyOf(@Nullable Object first, @Nullable Object second, Object... rest) {
    contains("contains any of", accumulate(first, second, rest));
  }

  /**
   * Attests that a Collection contains at least one of the objects contained
   * in the provided collection or fails.
   */
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

  /**
   * Attests that the subject contains at least all of the provided objects
   * or fails, potentially permitting duplicates in both the subject and the
   * parameters (if the subject even can have duplicates).
   *
   * <p>Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
  public Ordered containsAllOf(@Nullable Object first, @Nullable Object second, Object... rest) {
    return containsAll("contains all of", accumulate(first, second, rest));
  }

  /**
   * Attests that the subject contains at least all of the provided objects
   * or fails, potentially permitting duplicates in both the subject and the
   * parameters (if the subject even can have duplicates).
   *
   * <p>Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
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

  /**
   * Attests that a subject contains all of the provided objects and
   * only these objects or fails, potentially permitting duplicates
   * in both the subject and the parameters (if the subject even can
   * have duplicates).
   *
   * <p>Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
  public Ordered containsOnlyElements(
      @Nullable Object first, @Nullable Object second, Object... rest) {
    return containsExactly("contains only", accumulate(first, second, rest));
  }

  /**
   * Attests that a subject contains all of the provided objects and
   * only these objects or fails, potentially permitting duplicates
   * in both the subject and the parameters (if the subject even can
   * have duplicates).
   *
   * <p>Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
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

  /**
   * Attests that a subject contains none of the provided objects
   * or fails, eliding duplicates.
   */
  public void containsNoneOf(@Nullable Object first, @Nullable Object second, Object... rest) {
    containsNone("contains none of", accumulate(first, second, rest));
  }

  /**
   * Attests that a Collection contains none of the objects contained
   * in the provided collection or fails, eliding duplicates.
   */
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
