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

import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Kevin Bourrillion
 */
public class IterableSubject<S extends IterableSubject<S, T, C>, T, C extends Iterable<T>> extends Subject<S, C> {

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
}
