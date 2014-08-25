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
  static <T, C extends Collection<T>> CollectionSubject<? extends CollectionSubject<?, T, C>, T, C>
      create(FailureStrategy failureStrategy, Collection<T> collection) {
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
    return new Has<T, C>();
  }

  // TODO(user): @Deprecated
  public class Has<T, C extends Collection<T>> {
    /**
     * Attests that a Collection contains at least the item
     *
     * @deprecated Use {@link IterableSubject#contains(Object)} instead.
     */
    @Deprecated
    public void item(T item) {
      CollectionSubject.this.contains(item);
    }

    /**
     * Attests that a Collection contains the provided object or fails.
     *
     * @deprecated Use {@link IterableSubject#contains(Object)} instead.
     */
    @Deprecated
    public void anyOf(T first) {
      CollectionSubject.this.contains(first);
    }

    /**
     * Attests that a Collection contains at least one of the provided objects
     * or fails.
     *
     * @deprecated Use {@link IterableSubject#containsAnyOf(Object, Object, Object...)} instead.
     */
    @Deprecated
    public void anyOf(T first, T second, T ... rest) {
      CollectionSubject.this.containsAnyOf(first, second, rest);
    }

    /**
     * Attests that a Collection contains at least one of the objects contained
     * in the provided collection or fails.
     *
     * @deprecated Use {@link IterableSubject#containsAnyIn(Iterable<?>)} instead.
     */
    @Deprecated
    public void anyFrom(Iterable<T> col) {
      CollectionSubject.this.containsAnyIn(col);
    }

    /**
     * Attests that a Collection contains at least all of the provided objects
     * or fails, coping with duplicates in both the Collection and the
     * parameters.
     *
     * @deprecated Use {@link IterableSubject#contains(Object)} instead.
     */
    @Deprecated
    public Ordered allOf(T first) {
      // We can't inline contains(first) because of the (bogus) return type!
      return allFrom(accumulate(first));
    }

    /**
     * Attests that a Collection contains at least all of the provided objects
     * or fails, coping with duplicates in both the Collection and the
     * parameters.
     *
     * @deprecated Use {@link IterableSubject#containsAllOf(Object, Object, Object...)} instead.
     */
    @Deprecated
    public Ordered allOf(T first, T second, T ... rest) {
      return CollectionSubject.this.containsAllOf(first, second, rest);
    }

    /**
     * Attests that a Collection contains at least all of the objects contained
     * in the provided collection or fails, coping with duplicates in both
     * the Collection and the parameters.
     *
     * @deprecated Use {@link IterableSubject#containsAllIn(Iterable<?>)} instead.
     */
    @Deprecated
    public Ordered allFrom(Iterable<T> required) {
      return CollectionSubject.this.containsAllIn(required);
    }

    /**
     * Attests that a Collection contains at all of the provided objects and
     * only these objects or fails. This copes with duplicates in both the
     * Collection and the parameters. It makes no attestation about order
     * unless {@code inOrder()} is explicitly called.
     */
    // TODO(user): Deprecate this once the replacement method is added!
    // @deprecated Use {@link IterableSubject#containsExactly(Object)} instead.
    // @Deprecated
    public Ordered exactly(T first) {
      // We can't inline containsExactly(first) because of the (bogus) return type!
      return exactlyAs(accumulate(first));
    }

    /**
     * Attests that a Collection contains at all of the provided objects and
     * only these objects or fails. This copes with duplicates in both the
     * Collection and the parameters. It makes no attestation about order
     * unless {@code inOrder()} is explicitly called.
     */
    // TODO(user): Deprecate this once the replacement method is added!
    // @deprecated Use {@link IterableSubject#containsExactly(Object, Object, Object...)} instead.
    // @Deprecated
    public Ordered exactly(T first, T second, T ... rest) {
      return CollectionSubject.this.containsOnlyElements(first, second, rest);
    }

    /**
     * Attests that a Collection contains at all of the objects contained in the
     * provided collection and only these objects or fails. This copes with
     * duplicates in both the Collection and the parameters. It makes no
     * attestation about order unless {@code inOrder()} is explicitly called.
     */
    // TODO(user): Deprecate this once the replacement method is added!
    // @deprecated Use {@link IterableSubject#containsExactlyIn(Iterable<?>)} instead.
    // @Deprecated
    public Ordered exactlyAs(Iterable<T> required) {
      return CollectionSubject.this.containsOnlyElementsIn(required);
    }

    /**
     * Attests that a Collection contains none of the provided objects
     * or fails, coping with duplicates in both the Collection and the
     * parameters.
     *
     * @deprecated Use {@link IterableSubject#doesNotContain(Object)} instead.
     */
    @Deprecated
    public void noneOf(T first) {
      CollectionSubject.this.doesNotContain(first);
    }

    /**
     * Attests that a Collection contains none of the provided objects
     * or fails, coping with duplicates in both the Collection and the
     * parameters.
     *
     * @deprecated Use {@link IterableSubject#containsNoneOf(Object, Object, Object...)} instead.
     */
    @Deprecated
    public void noneOf(T first, T second, T ... rest) {
      CollectionSubject.this.containsNoneOf(first, second, rest);
    }

    /**
     * Attests that a Collection contains at none of the objects contained
     * in the provided collection or fails, coping with duplicates in both
     * the Collection and the parameters.
     *
     * @deprecated Use {@link IterableSubject#containsNoneIn(Iterable<?>)} instead.
     */
    @Deprecated
    public void noneFrom(Iterable<T> excluded) {
      CollectionSubject.this.containsNoneIn(excluded);
    }
  }
}
