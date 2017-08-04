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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ForwardingSortedSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import javax.annotation.Nullable;

/**
 * Tests for {@link SortedSet} and {@link NavigableSet} objects. This class supports assertions
 * based on {@code NavigableSet}'s API even if the subject only implements {@code SortedSet}.
 */
// TODO(diamondm): identify use-cases for assertions on other NavigableSet methods, such as
// ceiling(), and propose an API to support such assertions.
public final class SortedSetSubject extends IterableSubject {
  private final NavigableSet<?> actualAsNavigableSet;

  SortedSetSubject(FailureStrategy failureStrategy, SortedSet<?> set) {
    super(failureStrategy, set);
    actualAsNavigableSet = set == null ? null : SortedSetAsNavigableSet.wrapIfNecessary(set);
  }

  @Override
  public SortedSetSubject named(String format, Object... args) {
    super.named(format, args);
    return this;
  }

  /** Fails if the subject does not have the given first element. */
  public void hasFirstElement(@Nullable Object element) {
    if (actualAsNavigableSet().isEmpty()) {
      fail("has first element", element);
      return;
    }

    if (!Objects.equal(actualAsNavigableSet().first(), element)) {
      if (actualAsNavigableSet().contains(element)) {
        failWithRawMessage(
            "Not true that %s has first element <%s>. "
                + "It does contain this element, but the first element is <%s>",
            actualAsString(), element, actualAsNavigableSet().first());
        return;
      }
      failWithRawMessage(
          "Not true that %s has first element <%s>. "
              + "It does not contain this element, and the first element is <%s>",
          actualAsString(), element, actualAsNavigableSet().first());
    }
  }

  /** Fails if the subject does not have the given last element. */
  public void hasLastElement(@Nullable Object element) {
    if (actualAsNavigableSet().isEmpty()) {
      fail("has last element", element);
      return;
    }

    if (!Objects.equal(actualAsNavigableSet().last(), element)) {
      if (actualAsNavigableSet().contains(element)) {
        failWithRawMessage(
            "Not true that %s has last element <%s>. "
                + "It does contain this element, but the last element is <%s>",
            actualAsString(), element, actualAsNavigableSet().last());
        return;
      }
      failWithRawMessage(
          "Not true that %s has last element <%s>. "
              + "It does not contain this element, and the last element is <%s>",
          actualAsString(), element, actualAsNavigableSet().last());
    }
  }

  /**
   * Provides access to the actual value via {@link NavigableSet}'s API. This may or may be the same
   * object as returned by {@link #actual}, therefore you should avoid identity (e.g. {@code ==}) or
   * type (e.g. {@code instanceof}) assertions on this object.
   */
  private NavigableSet<?> actualAsNavigableSet() {
    return actualAsNavigableSet;
  }

  /**
   * A view into a {@link SortedSet} as a {@link NavigableSet}, enabling Truth to support assertions
   * on {@code NavigableSet}'s API even if the user only has a {@code SortedSet}. For now only the
   * functionality needed for the existing assertions has been implemented. Reference {@link
   * com.google.common.collect.ForwardingNavigableSet}'s behavior when implementing additional
   * methods.
   *
   * <p>TODO(diamondm): consider moving this to com.google.common.collect if it's ever fully
   * implemented.
   */
  private static class SortedSetAsNavigableSet<E> extends ForwardingSortedSet<E>
      implements NavigableSet<E> {
    private final SortedSet<E> delegate;

    static <E> NavigableSet<E> wrapIfNecessary(SortedSet<E> set) {
      if (set instanceof NavigableSet) {
        return (NavigableSet<E>) set;
      }
      return new SortedSetAsNavigableSet<E>(set);
    }

    SortedSetAsNavigableSet(SortedSet<E> delegate) {
      this.delegate = checkNotNull(delegate);
    }

    @Override
    protected SortedSet<E> delegate() {
      return delegate;
    }

    @Override
    public E ceiling(E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> descendingIterator() {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> descendingSet() {
      throw new UnsupportedOperationException();
    }

    @Override
    public E floor(E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
      throw new UnsupportedOperationException();
    }

    @Override
    public E higher(E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public E lower(E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public E pollFirst() {
      throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> subSet(
        E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
      throw new UnsupportedOperationException();
    }
  }
}
