/*
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
package com.google.common.truth;

import static com.google.common.truth.SubjectUtils.accumulate;

import java.util.Collection;

import javax.annotation.CheckReturnValue;

public class CollectionSubject<S extends CollectionSubject<S, T, C>, T, C extends Collection<T>>
    extends IterableSubject<S, T, C> {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T, C extends Collection<T>> CollectionSubject<? extends CollectionSubject<?, T, C>, T, C> create(
      FailureStrategy failureStrategy, Collection<T> collection) {
    return new CollectionSubject(failureStrategy, collection);
  }

  // TODO: Arguably this should even be package private
  protected CollectionSubject(FailureStrategy failureStrategy, C collection) {
    super(failureStrategy, collection);
  }

  /**
   * @tobedeprecated {@code assertThat(foo).has().somePredicate()} has
   *     been replaced with direct methods such as
   *     {@link IterableSubject#containsAllOf(Object, Object, Object...)},
   *     {@link IterableSubject#contains(Object)}, etc.
   */
  @CheckReturnValue
  // TODO(user): @Deprecated
  public Has<T, C> has() {
    return new Has<T, C>() {
      @Override public void item(T item) {
        CollectionSubject.this.contains(item);
      }

      @Override public void anyOf(T first) {
        anyFrom(accumulate(first));
      }

      @Override public final void anyOf(T first, T second, T ... rest) {
        anyFrom(accumulate(first, second, rest));
      }

      @Override public void anyFrom(Iterable<T> col) {
        CollectionSubject.this.containsAnyIn(col);
      }

      @Override public Ordered allOf(T first) {
        return allFrom(accumulate(first));
      }

      @Override public final Ordered allOf(T first, T second, T ... rest) {
        return allFrom(accumulate(first, second, rest));
      }

      @Override public Ordered allFrom(Iterable<T> required) {
        return CollectionSubject.this.containsAllIn(required);
      }

      @Override public Ordered exactly(T first) {
        return exactlyAs(accumulate(first));
      }

      @Override public final Ordered exactly(T first, T second, T ... rest) {
        return exactlyAs(accumulate(first, second, rest));
      }

      @Override public Ordered exactlyAs(Iterable<T> required) {
        return CollectionSubject.this.containsOnlyElementsIn(required);
      }

      @Override public void noneOf(T first) {
        noneFrom(accumulate(first));
      }

      @Override public final void noneOf(T first, T second, T ... rest) {
        noneFrom(accumulate(first, second, rest));
      }

      @Override public void noneFrom(Iterable<T> excluded) {
        CollectionSubject.this.containsNoneIn(excluded);
      }
    };
  }

  // TODO(user): @Deprecated
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
    void anyFrom(Iterable<E> expected);

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
    Ordered allFrom(Iterable<E> expected);

    /**
     * Attests that a Collection contains at all of the provided objects and
     * only these objects or fails. This copes with duplicates in both the
     * Collection and the parameters. It makes no attestation about order
     * unless {@code inOrder()} is explicitly called.
     */
    Ordered exactly(E first);

    /**
     * Attests that a Collection contains at all of the provided objects and
     * only these objects or fails. This copes with duplicates in both the
     * Collection and the parameters. It makes no attestation about order
     * unless {@code inOrder()} is explicitly called.
     */
    Ordered exactly(E first, E second, E... rest);

    /**
     * Attests that a Collection contains at all of the objects contained in the
     * provided collection and only these objects or fails. This copes with
     * duplicates in both the Collection and the parameters. It makes no
     * attestation about order unless {@code inOrder()} is explicitly called.
     */
    Ordered exactlyAs(Iterable<E> expected);

    /**
     * Attests that a Collection contains none of the provided objects
     * or fails, coping with duplicates in both the Collection and the
     * parameters.
     */
    void noneOf(E first);

    /**
     * Attests that a Collection contains none of the provided objects
     * or fails, coping with duplicates in both the Collection and the
     * parameters.
     */
    void noneOf(E first, E second, E... rest);

    /**
     * Attests that a Collection contains at none of the objects contained
     * in the provided collection or fails, coping with duplicates in both
     * the Collection and the parameters.
     */
    void noneFrom(Iterable<E> expected);
  }
}
