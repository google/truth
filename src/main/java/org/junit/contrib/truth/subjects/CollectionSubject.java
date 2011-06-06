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
package org.junit.contrib.truth.subjects;

import java.util.Collection;

import org.junit.contrib.truth.FailureStrategy;

public class CollectionSubject<T> extends Subject<Collection<T>> {
  public CollectionSubject(FailureStrategy failureStrategy, Collection<T> list) {
    super(failureStrategy, list);
  }

  /**
   * Attests that a Collection contains the provided object or fails.
   */
  public CollectionSubject<T> contains(T item) {
    if (!getSubject().contains(item)) {
      fail("contains", item);
    }
    return this;
  }
  
  /**
   * Attests that a Collection contains at least one of the provided 
   * objects or fails.
   */
  public CollectionSubject<T> containsAnyOf(T ... items) {
    Collection<T> collection = getSubject();
    for (T item : items) {
      if (collection.contains(item)) {
        return this;
      }
    }
    fail("contains", (Object[])items);
    return this;
  }
  
  /**
   * Attests that a Collection contains every provided object or fails.
   */
  public CollectionSubject<T> containsAllOf(T ... items) {
    Collection<T> collection = getSubject();
    for (T item : items) {
      if (!collection.contains(item)) {
        fail("contains", item);
      }
    }
    return this;
  }
}
