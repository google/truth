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

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import javax.annotation.Nullable;

/**
 * Fluent API to perform detailed, customizable comparison of Protocol buffers.
 *
 * <p>Methods may be chained in any order, but the chain should terminate with {@link
 * #isEqualTo(Object)} or {@link #isNotEqualTo(Object)}.
 *
 * <p>The state of a {@code ProtoFluentAssertion} object after each method is called is left
 * undefined. Users should not retain references to {@code ProtoFluentAssertion} instances.
 */
public interface ProtoFluentAssertion {

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
  ProtoFluentAssertion ignoringFieldAbsence();

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
  ProtoFluentAssertion ignoringRepeatedFieldOrder();

  /**
   * Limits the comparison of Protocol buffers to the defined {@link FieldScope}.
   *
   * <p>This method is additive and has well-defined ordering semantics. If the invoking {@link
   * ProtoFluentAssertion} is already scoped to a {@link FieldScope} {@code X}, and this method is
   * invoked with {@link FieldScope} {@code Y}, the resultant {@link ProtoFluentAssertion} is
   * constrained to the intersection of {@link FieldScope}s {@code X} and {@code Y}.
   *
   * <p>By default, {@link ProtoFluentAssertion} is constrained to {@link FieldScopes#all()}, that
   * is, no fields are excluded from comparison.
   */
  ProtoFluentAssertion withPartialScope(FieldScope fieldScope);

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
  ProtoFluentAssertion ignoringFields(int firstFieldNumber, int... rest);

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
  ProtoFluentAssertion ignoringFields(Iterable<Integer> fieldNumbers);

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
  ProtoFluentAssertion ignoringFieldDescriptors(
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
  ProtoFluentAssertion ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Excludes all specific field paths under the argument {@link FieldScope} from the comparison.
   *
   * <p>This method is additive and has well-defined ordering semantics. If the invoking {@link
   * ProtoFluentAssertion} is already scoped to a {@link FieldScope} {@code X}, and this method is
   * invoked with {@link FieldScope} {@code Y}, the resultant {@link ProtoFluentAssertion} is
   * constrained to the subtraction of {@code X - Y}.
   *
   * <p>By default, {@link ProtoFluentAssertion} is constrained to {@link FieldScopes#all()}, that
   * is, no fields are excluded from comparison.
   */
  ProtoFluentAssertion ignoringFieldScope(FieldScope fieldScope);

  /**
   * If set, in the event of a comparison failure, the error message printed will list only those
   * specific fields that did not match between the actual and expected values. Useful for very
   * large protocol buffers.
   *
   * <p>This a purely cosmetic setting, and it has no effect on the behavior of the test.
   */
  ProtoFluentAssertion reportingMismatchesOnly();

  /**
   * Compares the subject of the assertion to {@code expected}, using all of the rules specified by
   * earlier operations. If no settings are changed, this invokes the default {@code equals}
   * implementation of the subject {@link Message}.
   */
  void isEqualTo(@Nullable Object expected);

  /**
   * Compares the subject of the assertion to {@code expected}, expecting a difference, using all of
   * the rules specified by earlier operations. If no settings are changed, this invokes the default
   * {@code equals} implementation of the subject {@link Message}.
   */
  void isNotEqualTo(@Nullable Object expected);

  /**
   * @deprecated Do not call {@code equals()} on a {@code ProtoFluentAssertion}. Use {@link
   *     #isEqualTo(Object)} instead.
   * @see com.google.common.truth.Subject#equals(Object)
   */
  @Override
  @Deprecated
  boolean equals(Object o);

  /**
   * @deprecated {@code ProtoFluentAssertion} does not support {@code hashCode()}. Use {@link
   *     #isEqualTo(Object)} for testing.
   * @see com.google.common.truth.Subject#hashCode()
   */
  @Override
  @Deprecated
  int hashCode();
}
