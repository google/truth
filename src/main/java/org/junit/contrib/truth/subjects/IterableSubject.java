/*
 * Copyright (C) 2011 Google, Inc.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.contrib.truth.FailureStrategy;

/**
 * @author Kevin Bourrillion
 */
public class IterableSubject<S extends IterableSubject<S, T, C>, T, C extends Iterable<T>> extends Subject<S, C> {

  @SuppressWarnings("unchecked")
  public static <T, C extends Iterable<T>> IterableSubject<? extends IterableSubject<?, T, C>, T, C> create(
      FailureStrategy failureStrategy, Iterable<T> list) {
    return new IterableSubject(failureStrategy, list);
  }

  // TODO: Arguably this should even be package private
  protected IterableSubject(FailureStrategy failureStrategy, C list) {
    super(failureStrategy, list);
  }

  public And<S> contains(T item) {
    for (T t : getSubject()) {
      if (item.equals(t)) {
        return nextChain();
      }
    }
    fail("contains", item);
    throw new AssertionError();
  }

  /**
   * Attests that a Collection contains the provided object or fails.
   */
  public And<S> isEmpty() {
    if (getSubject().iterator().hasNext()) {
      fail("isEmpty");
    }
    return nextChain();
  }

  public And<S> hasContentsInOrder(T... expected) {
    // TODO(kevinb): prettier error message
    List<T> target = new ArrayList<T>();
    for (T t : getSubject()) {
      target.add(t);
    }
    check().that(target).isEqualTo(Arrays.asList(expected));
    return nextChain();
  }

  public And<S> hasContentsAnyOrder(T... expected) {
    check().that(createFakeMultiset(getSubject()))
        .isEqualTo(createFakeMultiset(Arrays.asList(expected)));
    return nextChain();
  }

  private static <T> Map<T, Integer> createFakeMultiset(Iterable<T> iterable) {
    Map<T, Integer> map = new HashMap<T, Integer>();
    for (T t : iterable) {
      Integer count = map.get(t);
      map.put(t, (count == null) ? 1 : count + 1);
    }
    return map;
  }
}
