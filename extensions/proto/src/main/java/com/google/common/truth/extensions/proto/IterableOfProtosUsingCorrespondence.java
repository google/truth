/*
 * Copyright (c) 2016 Google, Inc.
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
package com.google.common.truth.extensions.proto;

import com.google.common.base.Function;
import com.google.common.truth.Ordered;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Message;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Comparison methods, which enforce the rules set in prior calls to {@link
 * IterableOfProtosFluentAssertion}.
 */
public interface IterableOfProtosUsingCorrespondence<M extends Message> {

  /**
   * Specifies a way to pair up unexpected and missing elements in the message when an assertion
   * fails. For example:
   *
   * <pre>{@code
   * assertThat(actualFoos)
   *     .ignoringRepeatedFieldOrder()
   *     .ignoringFields(Foo.BAR_FIELD_NUMBER)
   *     .displayingDiffsPairedBy(Foo::getId)
   *     .containsExactlyElementsIn(expectedFoos);
   * }</pre>
   *
   * <p>On assertions where it makes sense to do so, the elements are paired as follows: they are
   * keyed by {@code keyFunction}, and if an unexpected element and a missing element have the same
   * non-null key then the they are paired up. (Elements with null keys are not paired.) The failure
   * message will show paired elements together, and a diff will be shown.
   *
   * <p>The expected elements given in the assertion should be uniquely keyed by {@code
   * keyFunction}. If multiple missing elements have the same key then the pairing will be skipped.
   *
   * <p>Useful key functions will have the property that key equality is less strict than the
   * already specified equality rules; i.e. given {@code actual} and {@code expected} values with
   * keys {@code actualKey} and {@code expectedKey}, if {@code actual} and {@code expected} compare
   * equal given the rest of the directives such as {@code ignoringRepeatedFieldOrder} and {@code
   * ignoringFields}, then it is guaranteed that {@code actualKey} is equal to {@code expectedKey},
   * but there are cases where {@code actualKey} is equal to {@code expectedKey} but the direct
   * comparison fails.
   *
   * <p>Note that calling this method makes no difference to whether a test passes or fails, it just
   * improves the message if it fails.
   */
  IterableOfProtosUsingCorrespondence<M> displayingDiffsPairedBy(
      Function<? super M, ?> keyFunction);

  /**
   * Checks that the subject contains at least one element that corresponds to the given expected
   * element.
   */
  void contains(@NullableDecl M expected);

  /** Checks that none of the actual elements correspond to the given element. */
  void doesNotContain(@NullableDecl M excluded);

  /**
   * Checks that subject contains exactly elements that correspond to the expected elements, i.e.
   * that there is a 1:1 mapping between the actual elements and the expected elements where each
   * pair of elements correspond.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   *
   * <p>To test that the iterable contains the same elements as an array, prefer {@link
   * #containsExactlyElementsIn(Message[])}. It makes clear that the given array is a list of
   * elements, not an element itself.
   */
  @CanIgnoreReturnValue
  Ordered containsExactly(@NullableDecl M... expected);

  /**
   * Checks that subject contains exactly elements that correspond to the expected elements, i.e.
   * that there is a 1:1 mapping between the actual elements and the expected elements where each
   * pair of elements correspond.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   */
  @CanIgnoreReturnValue
  Ordered containsExactlyElementsIn(Iterable<? extends M> expected);

  /**
   * Checks that subject contains exactly elements that correspond to the expected elements, i.e.
   * that there is a 1:1 mapping between the actual elements and the expected elements where each
   * pair of elements correspond.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   */
  @CanIgnoreReturnValue
  Ordered containsExactlyElementsIn(M[] expected);

  /**
   * Checks that the subject contains elements that corresponds to all of the expected elements,
   * i.e. that there is a 1:1 mapping between any subset of the actual elements and the expected
   * elements where each pair of elements correspond.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The elements must appear in the given order within the
   * subject, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  Ordered containsAllOf(@NullableDecl M first, @NullableDecl M second, @NullableDecl M... rest);

  /**
   * Checks that the subject contains elements that corresponds to all of the expected elements,
   * i.e. that there is a 1:1 mapping between any subset of the actual elements and the expected
   * elements where each pair of elements correspond.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The elements must appear in the given order within the
   * subject, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  Ordered containsAllIn(Iterable<? extends M> expected);

  /**
   * Checks that the subject contains elements that corresponds to all of the expected elements,
   * i.e. that there is a 1:1 mapping between any subset of the actual elements and the expected
   * elements where each pair of elements correspond.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The elements must appear in the given order within the
   * subject, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  Ordered containsAllIn(M[] expected);

  /**
   * Checks that the subject contains at least one element that corresponds to at least one of the
   * expected elements.
   */
  void containsAnyOf(@NullableDecl M first, @NullableDecl M second, @NullableDecl M... rest);

  /**
   * Checks that the subject contains at least one element that corresponds to at least one of the
   * expected elements.
   */
  void containsAnyIn(Iterable<? extends M> expected);

  /**
   * Checks that the subject contains at least one element that corresponds to at least one of the
   * expected elements.
   */
  void containsAnyIn(M[] expected);

  /**
   * Checks that the subject contains no elements that correspond to any of the given elements.
   * (Duplicates are irrelevant to this test, which fails if any of the subject elements correspond
   * to any of the given elements.)
   */
  void containsNoneOf(
      @NullableDecl M firstExcluded,
      @NullableDecl M secondExcluded,
      @NullableDecl M... restOfExcluded);

  /**
   * Checks that the subject contains no elements that correspond to any of the given elements.
   * (Duplicates are irrelevant to this test, which fails if any of the subject elements correspond
   * to any of the given elements.)
   */
  void containsNoneIn(Iterable<? extends M> excluded);

  /**
   * Checks that the subject contains no elements that correspond to any of the given elements.
   * (Duplicates are irrelevant to this test, which fails if any of the subject elements correspond
   * to any of the given elements.)
   */
  void containsNoneIn(M[] excluded);

}
