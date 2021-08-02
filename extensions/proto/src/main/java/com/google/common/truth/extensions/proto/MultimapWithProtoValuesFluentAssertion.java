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

import com.google.common.collect.Multimap;
import com.google.common.truth.Ordered;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.TypeRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Fluent API to perform detailed, customizable comparison of {@link Multimap}s containing protocol
 * buffers as values. The same comparison rules are applied to all pairs of protocol buffers which
 * get compared.
 *
 * <p>The <b>keys</b> of these maps are treated as ordinary objects, and keys which happen to be
 * protocol buffers are not given special treatment. They are compared with {@link Object#equals}
 * and {@link Object#hashCode()} as documented by the {@link Multimap} interface.
 *
 * <p>Methods may be chained in any order, but the chain should terminate with a method that doesn't
 * return a {@code MultimapWithProtoValuesFluentAssertion}, such as {@link
 * #containsExactlyEntriesIn} or {@link #containsEntry}.
 *
 * <p>The state of a {@code MultimapWithProtoValuesFluentAssertion} object after each method is
 * called is left undefined. Users should not retain references to {@code
 * MultimapWithProtoValuesFluentAssertion} instances.
 */
public interface MultimapWithProtoValuesFluentAssertion<M extends Message> {

  /**
   * Specifies that the 'has' bit of individual fields should be ignored when comparing for
   * equality.
   *
   * <p>For version 2 Protocol Buffers, this setting determines whether two protos with the same
   * value for a field compare equal if one explicitly sets the value, and the other merely
   * implicitly uses the schema-defined default. This setting also determines whether unknown fields
   * should be considered in the comparison. By {@code ignoringFieldAbsence()}, unknown fields are
   * ignored, and value-equal fields as specified above are considered equal.
   *
   * <p>For version 3 Protocol Buffers, this setting does not affect primitive fields, because their
   * default value is indistinguishable from unset.
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceForValues();

  /**
   * Specifies that the 'has' bit of these explicitly specified top-level field numbers should be
   * ignored when comparing for equality. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if they are to be ignored as well.
   *
   * <p>Use {@link #ignoringFieldAbsenceForValues()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsenceForValues() for details
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldsForValues(
      int firstFieldNumber, int... rest);

  /**
   * Specifies that the 'has' bit of these explicitly specified top-level field numbers should be
   * ignored when comparing for equality. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if they are to be ignored as well.
   *
   * <p>Use {@link #ignoringFieldAbsenceForValues()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsenceForValues() for details
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldsForValues(
      Iterable<Integer> fieldNumbers);

  /**
   * Specifies that the 'has' bit of these explicitly specified field descriptors should be ignored
   * when comparing for equality. Sub-fields must be specified explicitly if they are to be ignored
   * as well.
   *
   * <p>Use {@link #ignoringFieldAbsenceForValues()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsenceForValues() for details
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldDescriptorsForValues(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest);

  /**
   * Specifies that the 'has' bit of these explicitly specified field descriptors should be ignored
   * when comparing for equality. Sub-fields must be specified explicitly if they are to be ignored
   * as well.
   *
   * <p>Use {@link #ignoringFieldAbsenceForValues()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsenceForValues() for details
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldDescriptorsForValues(
      Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Specifies that the ordering of repeated fields, at all levels, should be ignored when comparing
   * for equality.
   *
   * <p>This setting applies to all repeated fields recursively, but it does not ignore structure.
   * For example, with {@link #ignoringRepeatedFieldOrderForValues()}, a repeated {@code int32}
   * field {@code bar}, set inside a repeated message field {@code foo}, the following protos will
   * all compare equal:
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
  MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderForValues();

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified top-level field
   * numbers should be ignored when comparing for equality. Sub-fields must be specified explicitly
   * (via {@link FieldDescriptor}) if their orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrderForValues()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrderForValues() for details.
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldsForValues(
      int firstFieldNumber, int... rest);

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified top-level field
   * numbers should be ignored when comparing for equality. Sub-fields must be specified explicitly
   * (via {@link FieldDescriptor}) if their orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrderForValues()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrderForValues() for details.
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldsForValues(
      Iterable<Integer> fieldNumbers);

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified field descriptors
   * should be ignored when comparing for equality. Sub-fields must be specified explicitly if their
   * orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrderForValues()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrderForValues() for details.
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldDescriptorsForValues(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest);

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified field descriptors
   * should be ignored when comparing for equality. Sub-fields must be specified explicitly if their
   * orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrderForValues()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrderForValues() for details.
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldDescriptorsForValues(
      Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Specifies that, for all repeated and map fields, any elements in the 'actual' proto which are
   * not found in the 'expected' proto are ignored, with the exception of fields in the expected
   * proto which are empty. To ignore empty repeated fields as well, use {@link
   * #comparingExpectedFieldsOnlyForValues}.
   *
   * <p>This rule is applied independently from {@link #ignoringRepeatedFieldOrderForValues}. If
   * ignoring repeated field order AND extra repeated field elements, all that is tested is that the
   * expected elements comprise a subset of the actual elements. If not ignoring repeated field
   * order, but still ignoring extra repeated field elements, the actual elements must contain a
   * subsequence that matches the expected elements for the test to pass. (The subsequence rule does
   * not apply to Map fields, which are always compared by key.)
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringExtraRepeatedFieldElementsForValues();

  /**
   * Specifies that extra repeated field elements for these explicitly specified top-level field
   * numbers should be ignored. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if their extra elements are to be ignored as well.
   *
   * <p>Use {@link #ignoringExtraRepeatedFieldElementsForValues()} instead to ignore these for all
   * fields.
   *
   * @see #ignoringExtraRepeatedFieldElementsForValues() for details.
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFieldsForValues(
      int firstFieldNumber, int... rest);

  /**
   * Specifies that extra repeated field elements for these explicitly specified top-level field
   * numbers should be ignored. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if their extra elements are to be ignored as well.
   *
   * <p>Use {@link #ignoringExtraRepeatedFieldElementsForValues()} instead to ignore these for all
   * fields.
   *
   * @see #ignoringExtraRepeatedFieldElementsForValues() for details.
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFieldsForValues(
      Iterable<Integer> fieldNumbers);

  /**
   * Specifies that extra repeated field elements for these explicitly specified field descriptors
   * should be ignored. Sub-fields must be specified explicitly if their extra elements are to be
   * ignored as well.
   *
   * <p>Use {@link #ignoringExtraRepeatedFieldElementsForValues()} instead to ignore these for all
   * fields.
   *
   * @see #ignoringExtraRepeatedFieldElementsForValues() for details.
   */
  MultimapWithProtoValuesFluentAssertion<M>
      ignoringExtraRepeatedFieldElementsOfFieldDescriptorsForValues(
          FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest);

  /**
   * Specifies that extra repeated field elements for these explicitly specified field descriptors
   * should be ignored. Sub-fields must be specified explicitly if their extra elements are to be
   * ignored as well.
   *
   * <p>Use {@link #ignoringExtraRepeatedFieldElementsForValues()} instead to ignore these for all
   * fields.
   *
   * @see #ignoringExtraRepeatedFieldElementsForValues() for details.
   */
  MultimapWithProtoValuesFluentAssertion<M>
      ignoringExtraRepeatedFieldElementsOfFieldDescriptorsForValues(
          Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Compares double fields as equal if they are both finite and their absolute difference is less
   * than or equal to {@code tolerance}.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForValues(double tolerance);

  /**
   * Compares double fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldsForValues(
      double tolerance, int firstFieldNumber, int... rest);

  /**
   * Compares double fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldsForValues(
      double tolerance, Iterable<Integer> fieldNumbers);

  /**
   * Compares double fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldDescriptorsForValues(
      double tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest);

  /**
   * Compares double fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldDescriptorsForValues(
      double tolerance, Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Compares float fields as equal if they are both finite and their absolute difference is less
   * than or equal to {@code tolerance}.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForValues(float tolerance);

  /**
   * Compares float fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldsForValues(
      float tolerance, int firstFieldNumber, int... rest);

  /**
   * Compares float fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldsForValues(
      float tolerance, Iterable<Integer> fieldNumbers);

  /**
   * Compares float fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldDescriptorsForValues(
      float tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest);

  /**
   * Compares float fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldDescriptorsForValues(
      float tolerance, Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Limits the comparison of Protocol buffers to the fields set in the expected proto(s). When
   * multiple protos are specified, the comparison is limited to the union of set fields in all the
   * expected protos.
   *
   * <p>The "expected proto(s)" are those passed to the method at the end of the call chain, such as
   * {@link #containsEntry} or {@link #containsExactlyEntriesIn}.
   *
   * <p>Fields not set in the expected proto(s) are ignored. In particular, proto3 fields which have
   * their default values are ignored, as these are indistinguishable from unset fields. If you want
   * to assert that a proto3 message has certain fields with default values, you cannot use this
   * method.
   */
  MultimapWithProtoValuesFluentAssertion<M> comparingExpectedFieldsOnlyForValues();

  /**
   * Limits the comparison of Protocol buffers to the defined {@link FieldScope}.
   *
   * <p>This method is additive and has well-defined ordering semantics. If the invoking {@link
   * MultimapWithProtoValuesFluentAssertion} is already scoped to a {@link FieldScope} {@code X},
   * and this method is invoked with {@link FieldScope} {@code Y}, the resultant {@link
   * MultimapWithProtoValuesFluentAssertion} is constrained to the intersection of {@link
   * FieldScope}s {@code X} and {@code Y}.
   *
   * <p>By default, {@link MultimapWithProtoValuesFluentAssertion} is constrained to {@link
   * FieldScopes#all()}, that is, no fields are excluded from comparison.
   */
  MultimapWithProtoValuesFluentAssertion<M> withPartialScopeForValues(FieldScope fieldScope);

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
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
      int firstFieldNumber, int... rest);

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
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(Iterable<Integer> fieldNumbers);

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
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
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
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
      Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Excludes all specific field paths under the argument {@link FieldScope} from the comparison.
   *
   * <p>This method is additive and has well-defined ordering semantics. If the invoking {@link
   * MultimapWithProtoValuesFluentAssertion} is already scoped to a {@link FieldScope} {@code X},
   * and this method is invoked with {@link FieldScope} {@code Y}, the resultant {@link
   * MultimapWithProtoValuesFluentAssertion} is constrained to the subtraction of {@code X - Y}.
   *
   * <p>By default, {@link MultimapWithProtoValuesFluentAssertion} is constrained to {@link
   * FieldScopes#all()}, that is, no fields are excluded from comparison.
   */
  MultimapWithProtoValuesFluentAssertion<M> ignoringFieldScopeForValues(FieldScope fieldScope);

  /**
   * If set, in the event of a comparison failure, the error message printed will list only those
   * specific fields that did not match between the actual and expected values. Useful for very
   * large protocol buffers.
   *
   * <p>This a purely cosmetic setting, and it has no effect on the behavior of the test.
   */
  MultimapWithProtoValuesFluentAssertion<M> reportingMismatchesOnlyForValues();

  /**
   * Specifies the {@link TypeRegistry} and {@link ExtensionRegistry} to use for {@link
   * com.google.protobuf.Any Any} messages.
   *
   * <p>To compare the value of an {@code Any} message, ProtoTruth looks in the given type registry
   * for a descriptor for the message's type URL:
   *
   * <ul>
   *   <li>If ProtoTruth finds a descriptor, it unpacks the value and compares it against the
   *       expected value, respecting any configuration methods used for the assertion.
   *   <li>If ProtoTruth does not find a descriptor (or if the value can't be deserialized with the
   *       descriptor), it compares the raw, serialized bytes of the expected and actual values.
   * </ul>
   *
   * <p>When ProtoTruth unpacks a value, it is parsing a serialized proto. That proto may contain
   * extensions. To look up those extensions, ProtoTruth uses the provided {@link
   * ExtensionRegistry}.
   *
   * @since 1.1
   */
  MultimapWithProtoValuesFluentAssertion<M> unpackingAnyUsingForValues(
      TypeRegistry typeRegistry, ExtensionRegistry extensionRegistry);

  /**
   * Fails if the multimap does not contain an entry with the given key and a value that corresponds
   * to the given value.
   */
  void containsEntry(@Nullable Object expectedKey, @Nullable M expectedValue);

  /**
   * Fails if the multimap contains an entry with the given key and a value that corresponds to the
   * given value.
   */
  void doesNotContainEntry(@Nullable Object excludedKey, @Nullable M excludedValue);

  /**
   * Fails if the map does not contain exactly the keys in the given multimap, mapping to values
   * that correspond to the values of the given multimap.
   *
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify that
   * the two Multimaps iterate fully in the same order. That is, their key sets iterate in the same
   * order, and the corresponding value collections for each key iterate in the same order.
   */
  @CanIgnoreReturnValue
  Ordered containsExactlyEntriesIn(Multimap<?, ? extends M> expectedMultimap);

  /** Fails if the multimap is not empty. */
  @CanIgnoreReturnValue
  public Ordered containsExactly();

  /**
   * Fails if the multimap does not contain exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   */
  @CanIgnoreReturnValue
  public Ordered containsExactly(@Nullable Object k0, @Nullable M v0, @Nullable Object... rest);

  /**
   * @deprecated Do not call {@code equals()} on a {@code MultimapWithProtoValuesFluentAssertion}.
   * @see com.google.common.truth.Subject#equals(Object)
   */
  @Override
  @Deprecated
  boolean equals(Object o);

  /**
   * @deprecated {@code MultimapWithProtoValuesFluentAssertion} does not support {@code hashCode()}.
   * @see com.google.common.truth.Subject#hashCode()
   */
  @Override
  @Deprecated
  int hashCode();
}
