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

import com.google.common.truth.Ordered;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import javax.annotation.Nullable;

/**
 * Fluent API to perform detailed, customizable comparison of iterables of protocol buffers. The
 * same comparison rules are applied to all pairs of protocol buffers which get compared.
 *
 * <p>Methods may be chained in any order, but the chain should terminate with a method that doesn't
 * return an IterableOfProtosFluentAssertion, such as {@link #containsExactly}, or {@link
 * #containsAnyIn}.
 *
 * <p>The state of an {@code IterableOfProtosFluentAssertion} object after each method is called is
 * left undefined. Users should not retain references to {@code IterableOfProtosFluentAssertion}
 * instances.
 */
public interface IterableOfProtosFluentAssertion<M extends Message> {

  /**
   * Specifies that the 'has' bit of individual fields should be ignored when comparing for
   * equality.
   *
   * <p>For version 2 Protocol Buffers, this setting determines whether two protos with the same
   * value for a primitive field compare equal if one explicitly sets the value, and the other
   * merely implicitly uses the schema-defined default. This setting also determines whether unknown
   * fields should be considered in the comparison. By {@code ignoringFieldAbsence()}, unknown
   * fields are ignored, and value-equal fields as specified above are considered equal.
   *
   * <p>For version 3 Protocol Buffers, this setting has no effect. Primitive fields set to their
   * default value are indistinguishable from unset fields in proto 3. Proto 3 also eliminates
   * unknown fields, so this setting has no effect there either.
   */
  IterableOfProtosFluentAssertion<M> ignoringFieldAbsence();

  /**
   * Specifies that the ordering of repeated fields, at all levels, should be ignored when comparing
   * for equality.
   *
   * <p>This setting applies to all repeated fields recursively, but it does not ignore structure.
   * For example, with {@link #ignoringRepeatedFieldOrder()}, a repeated {@code int32} field {@code
   * bar}, set inside a repeated message field {@code foo}, the following protos will all compare
   * equal:
   *
   * <pre>{@code
   * message1: {
   *   foo: {
   *     bar: 1
   *     bar: 2
   *   }
   *   foo: {
   *     bar: 3
   *     bar: 4
   *   }
   * }
   *
   * message2: {
   *   foo: {
   *     bar: 2
   *     bar: 1
   *   }
   *   foo: {
   *     bar: 4
   *     bar: 3
   *   }
   * }
   *
   * message3: {
   *   foo: {
   *     bar: 4
   *     bar: 3
   *   }
   *   foo: {
   *     bar: 2
   *     bar: 1
   *   }
   * }
   * }</pre>
   *
   * <p>However, the following message will compare equal to none of these:
   *
   * <pre>{@code
   * message4: {
   *   foo: {
   *     bar: 1
   *     bar: 3
   *   }
   *   foo: {
   *     bar: 2
   *     bar: 4
   *   }
   * }
   * }</pre>
   *
   * <p>This setting does not apply to map fields, for which field order is always ignored. The
   * serialization order of map fields is undefined, and it may change from runtime to runtime.
   */
  IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrder();

  /**
   * Limits the comparison of Protocol buffers to the defined {@link FieldScope}.
   *
   * <p>This method is additive and has well-defined ordering semantics. If the invoking {@link
   * IterableOfProtosFluentAssertion} is already scoped to a {@link FieldScope} {@code X}, and this
   * method is invoked with {@link FieldScope} {@code Y}, the resultant {@link
   * IterableOfProtosFluentAssertion} is constrained to the intersection of {@link FieldScope}s
   * {@code X} and {@code Y}.
   *
   * <p>By default, {@link IterableOfProtosFluentAssertion} is constrained to {@link
   * FieldScopes#all()}, that is, no fields are excluded from comparison.
   */
  IterableOfProtosFluentAssertion<M> withPartialScope(FieldScope fieldScope);

  /**
   * Excludes the top-level message fields with the given tag numbers from the comparison.
   *
   * <p>This method adds on any previous {@link FieldScope} related settings, overriding previous
   * changes to ensure the specified fields are ignored recursively. All sub-fields of these field
   * numbers are ignored, and all sub-messages of type {@code M} will also have these field numbers
   * ignored.
   *
   * <p>If an invalid field number is supplied, the terminal comparison operation will throw a
   * runtime exception.
   */
  IterableOfProtosFluentAssertion<M> ignoringFields(int firstFieldNumber, int... rest);

  /**
   * Excludes the top-level message fields with the given tag numbers from the comparison.
   *
   * <p>This method adds on any previous {@link FieldScope} related settings, overriding previous
   * changes to ensure the specified fields are ignored recursively. All sub-fields of these field
   * numbers are ignored, and all sub-messages of type {@code M} will also have these field numbers
   * ignored.
   *
   * <p>If an invalid field number is supplied, the terminal comparison operation will throw a
   * runtime exception.
   */
  IterableOfProtosFluentAssertion<M> ignoringFields(Iterable<Integer> fieldNumbers);

  /**
   * Excludes all message fields matching the given {@link FieldDescriptor}s from the comparison.
   *
   * <p>This method adds on any previous {@link FieldScope} related settings, overriding previous
   * changes to ensure the specified fields are ignored recursively. All sub-fields of these field
   * descriptors are ignored, no matter where they occur in the tree.
   *
   * <p>If a field descriptor which does not, or cannot occur in the proto structure is supplied, it
   * is silently ignored.
   */
  IterableOfProtosFluentAssertion<M> ignoringFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest);

  /**
   * Excludes all message fields matching the given {@link FieldDescriptor}s from the comparison.
   *
   * <p>This method adds on any previous {@link FieldScope} related settings, overriding previous
   * changes to ensure the specified fields are ignored recursively. All sub-fields of these field
   * descriptors are ignored, no matter where they occur in the tree.
   *
   * <p>If a field descriptor which does not, or cannot occur in the proto structure is supplied, it
   * is silently ignored.
   */
  IterableOfProtosFluentAssertion<M> ignoringFieldDescriptors(
      Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Excludes all specific field paths under the argument {@link FieldScope} from the comparison.
   *
   * <p>This method is additive and has well-defined ordering semantics. If the invoking {@link
   * IterableOfProtosFluentAssertion} is already scoped to a {@link FieldScope} {@code X}, and this
   * method is invoked with {@link FieldScope} {@code Y}, the resultant {@link
   * IterableOfProtosFluentAssertion} is constrained to the subtraction of {@code X - Y}.
   *
   * <p>By default, {@link IterableOfProtosFluentAssertion} is constrained to {@link
   * FieldScopes#all()}, that is, no fields are excluded from comparison.
   */
  IterableOfProtosFluentAssertion<M> ignoringFieldScope(FieldScope fieldScope);

  /**
   * If set, in the event of a comparison failure, the error message printed will list only those
   * specific fields that did not match between the actual and expected values. Useful for very
   * large protocol buffers.
   *
   * <p>This a purely cosmetic setting, and it has no effect on the behavior of the test.
   */
  IterableOfProtosFluentAssertion<M> reportingMismatchesOnly();

  /**
   * Checks that the subject contains at least one element that corresponds to the given expected
   * element.
   */
  void contains(@Nullable M expected);

  /** Checks that none of the actual elements correspond to the given element. */
  void doesNotContain(@Nullable M excluded);

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
  Ordered containsExactly(@Nullable M... expected);

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
  Ordered containsAllOf(@Nullable M first, @Nullable M second, @Nullable M... rest);

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
  void containsAnyOf(@Nullable M first, @Nullable M second, @Nullable M... rest);

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
      @Nullable M firstExcluded, @Nullable M secondExcluded, @Nullable M... restOfExcluded);

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

  /**
   * @deprecated Do not call {@code equals()} on a {@code IterableOfProtosFluentAssertion}.
   * @see com.google.common.truth.Subject#equals(Object)
   */
  @Override
  @Deprecated
  boolean equals(Object o);

  /**
   * @deprecated {@code IterableOfProtosFluentAssertion} does not support {@code hashCode()}.
   * @see com.google.common.truth.Subject#hashCode()
   */
  @Override
  @Deprecated
  int hashCode();
}
