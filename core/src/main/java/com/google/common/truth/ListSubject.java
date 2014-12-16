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

import java.util.List;

/**
 * Propositions for {@link List} subjects.
 *
 * @author Christian Gruber
 */
// TODO(user): Remove this class entirely.
public class ListSubject<S extends ListSubject<S, T, C>, T, C extends List<T>>
    extends IterableSubject<S, T, C> {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <T, C extends List<T>> ListSubject<? extends ListSubject<?, T, C>, T, C> create(
      FailureStrategy failureStrategy, List<T> list) {
    return new ListSubject(failureStrategy, list);
  }

  ListSubject(FailureStrategy failureStrategy, C list) {
    super(failureStrategy, list);
  }

  /**
   * Fails if the list does not contain the specified sequence.
   */
  public void containsSequence(List<?> sequence) {
    if (sequence.isEmpty()) {
      return;
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
        return;
      }
      list = list.subList(first + 1, list.size());
    }
    fail("contains sequence", sequence);
  }
}
