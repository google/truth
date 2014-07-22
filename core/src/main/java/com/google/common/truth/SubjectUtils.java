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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Utility methods used in Subject<T> implementors.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
final class SubjectUtils {

  static <T> Iterable<T> accumulate(T only) {
    return new ArrayList<T>(Collections.singleton(only));
  }

  static <T> Iterable<T> accumulate(T first, T ... rest) {
    // rest should never be deliberately null, so assume that the caller passed null
    // in the third position but intended it to be the third element in the array of values.
    // Javac makes the opposite inference, so handle that here.
    List<T> items = new ArrayList<T>(1 + ((rest == null) ? 1 : rest.length));
    items.add(first);
    if (rest == null) {
      items.add(null);
    } else {
      items.addAll(Arrays.asList(rest));
    }
    return items;
  }

  static <T> Iterable<T> accumulate(T first, T second, T ... rest) {
    // rest should never be deliberately null, so assume that the caller passed null
    // in the third position but intended it to be the third element in the array of values.
    // Javac makes the opposite inference, so handle that here.
    List<T> items = new ArrayList<T>(2 + ((rest == null) ? 1 : rest.length));
    items.add(first);
    items.add(second);
    if (rest == null) {
      items.add(null);
    } else {
      items.addAll(Arrays.asList(rest));
    }
    return items;
  }

  static <T> int countOf(T t, Iterable<T> items) {
    int count = 0;
    for (T item : items) {
      if (t == null ? (item == null) : t.equals(item)) {
        count++;
      }
    }
    return count;
  }

  static <T> List<Object> countDuplicates(Collection<T> items) {
    Set<T> itemSet = new HashSet<T>(items);
    Object[] params = new Object[itemSet.size()];
    int n = 0;
    for (T item : itemSet) {
      int count = countOf(item, items);
      params[n++] = (count > 1) ? item + " [" + count + " copies]" : item;
    }
    return Arrays.asList(params);
  }

}
