/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * Utility methods used in Subject<T> implementors.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
final class SubjectUtils {

  static <T> List<T> accumulate(T first, T second, T ... rest) {
    List<T> items = new LinkedList<T>();
    items.add(first);
    items.add(second);
    items.addAll(Arrays.asList(rest));
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

}
