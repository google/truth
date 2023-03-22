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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.asList;
import static com.google.common.truth.extensions.proto.FieldScopeUtil.asList;
import static com.google.common.truth.extensions.proto.ProtoTruth.protos;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.MultimapSubject;
import com.google.common.truth.Ordered;
import com.google.common.truth.Subject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.TypeRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Truth subject for {@link Multimap}s with protocol buffers for values.
 *
 * <p>{@code ProtoTruth.assertThat(actual).containsExactlyEntriesIn(expected)} performs the same
 * assertion as {@code Truth.assertThat(actual).containsExactlyEntriesIn(expected)}. By default, the
 * assertions are strict with respect to repeated field order, missing fields, etc. This behavior
 * can be changed with the configuration methods on this subject, e.g. {@code
 * ProtoTruth.assertThat(actual).ignoringRepeatedFieldOrder().containsExactlyEntriesIn(expected)}.
 *
 * <p>By default, floating-point fields are compared using exact equality, which is <a
 * href="https://truth.dev/floating_point">probably not what you want</a> if the values are the
 * results of some arithmetic. To check for approximate equality, use {@link
 * #usingDoubleToleranceForValues}, {@link #usingFloatToleranceForValues}, and {@linkplain
 * #usingDoubleToleranceForFieldsForValues(double, int, int...) their per-field equivalents}.
 *
 * <p>Equality tests, and other methods, may yield slightly different behavior for versions 2 and 3
 * of Protocol Buffers. If testing protos of multiple versions, make sure you understand the
 * behaviors of default and unknown fields so you don't under or over test.
 *
 * @param <M> the type of the message values in the multimap
 */
public class MultimapWithProtoValuesSubject<M extends Message> extends MultimapSubject {

  /*
   * Storing a FailureMetadata instance in a Subject subclass is generally a bad practice. For an
   * explanation of why it works out OK here, see LiteProtoSubject.
   */
  private final FailureMetadata metadata;
  private final Multimap<?, M> actual;
  private final FluentEqualityConfig config;

  protected MultimapWithProtoValuesSubject(
      FailureMetadata failureMetadata, @Nullable Multimap<?, M> multimap) {
    this(failureMetadata, FluentEqualityConfig.defaultInstance(), multimap);
  }

  MultimapWithProtoValuesSubject(
      FailureMetadata failureMetadata,
      FluentEqualityConfig config,
      @Nullable Multimap<?, M> multimap) {
    super(failureMetadata, multimap);
    this.metadata = failureMetadata;
    this.actual = multimap;
    this.config = config;
  }

  /**
   * Returns a context-aware {@link Subject} for making assertions about the values for the given
   * key within the {@link Multimap}.
   *
   * <p>This method performs no checks on its own and cannot cause test failures. Subsequent
   * assertions must be chained onto this method call to test properties of the {@link Multimap}.
   */
  /*
   * This is mostly safe because we only read from the map. And if it produces NPE/CCE immediately,
   * that's no worse than many existing Collection implementations....
   */
  @SuppressWarnings("unchecked")
  @Override
  public IterableOfProtosSubject<M> valuesForKey(@Nullable Object key) {
    return check("valuesForKey(%s)", key)
        .about(protos())
        .that(((Multimap<Object, M>) actual).get(key));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // MultimapWithProtoValuesFluentAssertion Configuration
  //////////////////////////////////////////////////////////////////////////////////////////////////

  MultimapWithProtoValuesFluentAssertion<M> usingConfig(FluentEqualityConfig newConfig) {
    return new MultimapWithProtoValuesFluentAssertionImpl<>(
        new MultimapWithProtoValuesSubject<>(metadata, newConfig, actual));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceForValues() {
    return usingConfig(config.ignoringFieldAbsence());
  }

  /**
   * Specifies that the 'has' bit of these explicitly specified top-level field numbers should be
   * ignored when comparing for equality. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if they are to be ignored as well.
   *
   * <p>Use {@link #ignoringFieldAbsenceForValues()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsenceForValues() for details
   */
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldsForValues(
      int firstFieldNumber, int... rest) {
    return usingConfig(config.ignoringFieldAbsenceOfFields(asList(firstFieldNumber, rest)));
  }

  /**
   * Specifies that the 'has' bit of these explicitly specified top-level field numbers should be
   * ignored when comparing for equality. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if they are to be ignored as well.
   *
   * <p>Use {@link #ignoringFieldAbsenceForValues()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsenceForValues() for details
   */
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldsForValues(
      Iterable<Integer> fieldNumbers) {
    return usingConfig(config.ignoringFieldAbsenceOfFields(fieldNumbers));
  }

  /**
   * Specifies that the 'has' bit of these explicitly specified field descriptors should be ignored
   * when comparing for equality. Sub-fields must be specified explicitly if they are to be ignored
   * as well.
   *
   * <p>Use {@link #ignoringFieldAbsenceForValues()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsenceForValues() for details
   */
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldDescriptorsForValues(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return usingConfig(
        config.ignoringFieldAbsenceOfFieldDescriptors(asList(firstFieldDescriptor, rest)));
  }

  /**
   * Specifies that the 'has' bit of these explicitly specified field descriptors should be ignored
   * when comparing for equality. Sub-fields must be specified explicitly if they are to be ignored
   * as well.
   *
   * <p>Use {@link #ignoringFieldAbsenceForValues()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsenceForValues() for details
   */
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldDescriptorsForValues(
      Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.ignoringFieldAbsenceOfFieldDescriptors(fieldDescriptors));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderForValues() {
    return usingConfig(config.ignoringRepeatedFieldOrder());
  }

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified top-level field
   * numbers should be ignored when comparing for equality. Sub-fields must be specified explicitly
   * (via {@link FieldDescriptor}) if their orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrderForValues()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrderForValues() for details.
   */
  public MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldsForValues(
      int firstFieldNumber, int... rest) {
    return usingConfig(config.ignoringRepeatedFieldOrderOfFields(asList(firstFieldNumber, rest)));
  }

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified top-level field
   * numbers should be ignored when comparing for equality. Sub-fields must be specified explicitly
   * (via {@link FieldDescriptor}) if their orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrderForValues()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrderForValues() for details.
   */
  public MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldsForValues(
      Iterable<Integer> fieldNumbers) {
    return usingConfig(config.ignoringRepeatedFieldOrderOfFields(fieldNumbers));
  }

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified field descriptors
   * should be ignored when comparing for equality. Sub-fields must be specified explicitly if their
   * orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrderForValues()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrderForValues() for details.
   */
  public MultimapWithProtoValuesFluentAssertion<M>
      ignoringRepeatedFieldOrderOfFieldDescriptorsForValues(
          FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return usingConfig(
        config.ignoringRepeatedFieldOrderOfFieldDescriptors(asList(firstFieldDescriptor, rest)));
  }

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified field descriptors
   * should be ignored when comparing for equality. Sub-fields must be specified explicitly if their
   * orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrderForValues()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrderForValues() for details.
   */
  public MultimapWithProtoValuesFluentAssertion<M>
      ignoringRepeatedFieldOrderOfFieldDescriptorsForValues(
          Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.ignoringRepeatedFieldOrderOfFieldDescriptors(fieldDescriptors));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringExtraRepeatedFieldElementsForValues() {
    return usingConfig(config.ignoringExtraRepeatedFieldElements());
  }

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
  public MultimapWithProtoValuesFluentAssertion<M>
      ignoringExtraRepeatedFieldElementsOfFieldsForValues(int firstFieldNumber, int... rest) {
    return usingConfig(
        config.ignoringExtraRepeatedFieldElementsOfFields(asList(firstFieldNumber, rest)));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M>
      ignoringExtraRepeatedFieldElementsOfFieldsForValues(Iterable<Integer> fieldNumbers) {
    return usingConfig(config.ignoringExtraRepeatedFieldElementsOfFields(fieldNumbers));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M>
      ignoringExtraRepeatedFieldElementsOfFieldDescriptorsForValues(
          FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return usingConfig(
        config.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
            asList(firstFieldDescriptor, rest)));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M>
      ignoringExtraRepeatedFieldElementsOfFieldDescriptorsForValues(
          Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(
        config.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(fieldDescriptors));
  }

  /**
   * Compares double fields as equal if they are both finite and their absolute difference is less
   * than or equal to {@code tolerance}.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForValues(double tolerance) {
    return usingConfig(config.usingDoubleTolerance(tolerance));
  }

  /**
   * Compares double fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldsForValues(
      double tolerance, int firstFieldNumber, int... rest) {
    return usingConfig(
        config.usingDoubleToleranceForFields(tolerance, asList(firstFieldNumber, rest)));
  }

  /**
   * Compares double fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldsForValues(
      double tolerance, Iterable<Integer> fieldNumbers) {
    return usingConfig(config.usingDoubleToleranceForFields(tolerance, fieldNumbers));
  }

  /**
   * Compares double fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldDescriptorsForValues(
      double tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return usingConfig(
        config.usingDoubleToleranceForFieldDescriptors(
            tolerance, asList(firstFieldDescriptor, rest)));
  }

  /**
   * Compares double fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldDescriptorsForValues(
      double tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.usingDoubleToleranceForFieldDescriptors(tolerance, fieldDescriptors));
  }

  /**
   * Compares float fields as equal if they are both finite and their absolute difference is less
   * than or equal to {@code tolerance}.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForValues(float tolerance) {
    return usingConfig(config.usingFloatTolerance(tolerance));
  }

  /**
   * Compares float fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldsForValues(
      float tolerance, int firstFieldNumber, int... rest) {
    return usingConfig(
        config.usingFloatToleranceForFields(tolerance, asList(firstFieldNumber, rest)));
  }

  /**
   * Compares float fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldsForValues(
      float tolerance, Iterable<Integer> fieldNumbers) {
    return usingConfig(config.usingFloatToleranceForFields(tolerance, fieldNumbers));
  }

  /**
   * Compares float fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldDescriptorsForValues(
      float tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return usingConfig(
        config.usingFloatToleranceForFieldDescriptors(
            tolerance, asList(firstFieldDescriptor, rest)));
  }

  /**
   * Compares float fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldDescriptorsForValues(
      float tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.usingFloatToleranceForFieldDescriptors(tolerance, fieldDescriptors));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> comparingExpectedFieldsOnlyForValues() {
    return usingConfig(config.comparingExpectedFieldsOnly());
  }

  /**
   * Limits the comparison of Protocol buffers to the defined {@link FieldScope}.
   *
   * <p>This method is additive and has well-defined ordering semantics. If the invoking {@link
   * ProtoFluentAssertion} is already scoped to a {@link FieldScope} {@code X}, and this method is
   * invoked with {@link FieldScope} {@code Y}, the resultant {@link ProtoFluentAssertion} is
   * constrained to the intersection of {@link FieldScope}s {@code X} and {@code Y}.
   *
   * <p>By default, {@link MultimapWithProtoValuesFluentAssertion} is constrained to {@link
   * FieldScopes#all()}, that is, no fields are excluded from comparison.
   */
  public MultimapWithProtoValuesFluentAssertion<M> withPartialScopeForValues(
      FieldScope fieldScope) {
    return usingConfig(config.withPartialScope(checkNotNull(fieldScope, "fieldScope")));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
      int firstFieldNumber, int... rest) {
    return ignoringFieldsForValues(asList(firstFieldNumber, rest));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
      Iterable<Integer> fieldNumbers) {
    return usingConfig(config.ignoringFields(fieldNumbers));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return ignoringFieldDescriptorsForValues(asList(firstFieldDescriptor, rest));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
      Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.ignoringFieldDescriptors(fieldDescriptors));
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldScopeForValues(
      FieldScope fieldScope) {
    return usingConfig(config.ignoringFieldScope(checkNotNull(fieldScope, "fieldScope")));
  }

  /**
   * If set, in the event of a comparison failure, the error message printed will list only those
   * specific fields that did not match between the actual and expected values. Useful for very
   * large protocol buffers.
   *
   * <p>This a purely cosmetic setting, and it has no effect on the behavior of the test.
   */
  public MultimapWithProtoValuesFluentAssertion<M> reportingMismatchesOnlyForValues() {
    return usingConfig(config.reportingMismatchesOnly());
  }

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
  public MultimapWithProtoValuesFluentAssertion<M> unpackingAnyUsingForValues(
      TypeRegistry typeRegistry, ExtensionRegistry extensionRegistry) {
    return usingConfig(config.unpackingAnyUsing(typeRegistry, extensionRegistry));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // UsingCorrespondence Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private MultimapSubject.UsingCorrespondence<M, M> usingCorrespondence(
      Iterable<? extends M> expectedValues) {
    return comparingValuesUsing(
        config
            .withExpectedMessages(expectedValues)
            .<M>toCorrespondence(FieldScopeUtil.getSingleDescriptor(actual.values())));
  }

  // The UsingCorrespondence methods have conflicting erasure with default MapSubject methods,
  // so we can't implement them both on the same class, but we want to define both so
  // MultimapWithProtoValuesSubjects are interchangeable with MapSubjects when no configuration is
  // specified. So, we implement a dumb, private delegator to return instead.
  private static final class MultimapWithProtoValuesFluentAssertionImpl<M extends Message>
      implements MultimapWithProtoValuesFluentAssertion<M> {
    private final MultimapWithProtoValuesSubject<M> subject;

    MultimapWithProtoValuesFluentAssertionImpl(MultimapWithProtoValuesSubject<M> subject) {
      this.subject = subject;
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceForValues() {
      return subject.ignoringFieldAbsenceForValues();
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldsForValues(
        int firstFieldNumber, int... rest) {
      return subject.ignoringFieldAbsenceOfFieldsForValues(firstFieldNumber, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceOfFieldsForValues(
        Iterable<Integer> fieldNumbers) {
      return subject.ignoringFieldAbsenceOfFieldsForValues(fieldNumbers);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        ignoringFieldAbsenceOfFieldDescriptorsForValues(
            FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringFieldAbsenceOfFieldDescriptorsForValues(firstFieldDescriptor, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        ignoringFieldAbsenceOfFieldDescriptorsForValues(
            Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringFieldAbsenceOfFieldDescriptorsForValues(fieldDescriptors);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderForValues() {
      return subject.ignoringRepeatedFieldOrderForValues();
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldsForValues(
        int firstFieldNumber, int... rest) {
      return subject.ignoringRepeatedFieldOrderOfFieldsForValues(firstFieldNumber, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldsForValues(
        Iterable<Integer> fieldNumbers) {
      return subject.ignoringRepeatedFieldOrderOfFieldsForValues(fieldNumbers);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        ignoringRepeatedFieldOrderOfFieldDescriptorsForValues(
            FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringRepeatedFieldOrderOfFieldDescriptorsForValues(
          firstFieldDescriptor, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        ignoringRepeatedFieldOrderOfFieldDescriptorsForValues(
            Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringRepeatedFieldOrderOfFieldDescriptorsForValues(fieldDescriptors);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringExtraRepeatedFieldElementsForValues() {
      return subject.ignoringExtraRepeatedFieldElementsForValues();
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        ignoringExtraRepeatedFieldElementsOfFieldsForValues(int firstFieldNumber, int... rest) {
      return subject.ignoringExtraRepeatedFieldElementsOfFieldsForValues(firstFieldNumber, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        ignoringExtraRepeatedFieldElementsOfFieldsForValues(Iterable<Integer> fieldNumbers) {
      return subject.ignoringExtraRepeatedFieldElementsOfFieldsForValues(fieldNumbers);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        ignoringExtraRepeatedFieldElementsOfFieldDescriptorsForValues(
            FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringExtraRepeatedFieldElementsOfFieldDescriptorsForValues(
          firstFieldDescriptor, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        ignoringExtraRepeatedFieldElementsOfFieldDescriptorsForValues(
            Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringExtraRepeatedFieldElementsOfFieldDescriptorsForValues(
          fieldDescriptors);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForValues(
        double tolerance) {
      return subject.usingDoubleToleranceForValues(tolerance);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldsForValues(
        double tolerance, int firstFieldNumber, int... rest) {
      return subject.usingDoubleToleranceForFieldsForValues(tolerance, firstFieldNumber, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> usingDoubleToleranceForFieldsForValues(
        double tolerance, Iterable<Integer> fieldNumbers) {
      return subject.usingDoubleToleranceForFieldsForValues(tolerance, fieldNumbers);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        usingDoubleToleranceForFieldDescriptorsForValues(
            double tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.usingDoubleToleranceForFieldDescriptorsForValues(
          tolerance, firstFieldDescriptor, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        usingDoubleToleranceForFieldDescriptorsForValues(
            double tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.usingDoubleToleranceForFieldDescriptorsForValues(tolerance, fieldDescriptors);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForValues(float tolerance) {
      return subject.usingFloatToleranceForValues(tolerance);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldsForValues(
        float tolerance, int firstFieldNumber, int... rest) {
      return subject.usingFloatToleranceForFieldsForValues(tolerance, firstFieldNumber, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> usingFloatToleranceForFieldsForValues(
        float tolerance, Iterable<Integer> fieldNumbers) {
      return subject.usingFloatToleranceForFieldsForValues(tolerance, fieldNumbers);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        usingFloatToleranceForFieldDescriptorsForValues(
            float tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.usingFloatToleranceForFieldDescriptorsForValues(
          tolerance, firstFieldDescriptor, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M>
        usingFloatToleranceForFieldDescriptorsForValues(
            float tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.usingFloatToleranceForFieldDescriptorsForValues(tolerance, fieldDescriptors);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> comparingExpectedFieldsOnlyForValues() {
      return subject.comparingExpectedFieldsOnlyForValues();
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> withPartialScopeForValues(
        FieldScope fieldScope) {
      return subject.withPartialScopeForValues(fieldScope);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
        int firstFieldNumber, int... rest) {
      return subject.ignoringFieldsForValues(firstFieldNumber, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
        Iterable<Integer> fieldNumbers) {
      return subject.ignoringFieldsForValues(fieldNumbers);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringFieldDescriptorsForValues(firstFieldDescriptor, rest);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringFieldDescriptorsForValues(fieldDescriptors);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldScopeForValues(
        FieldScope fieldScope) {
      return subject.ignoringFieldScopeForValues(fieldScope);
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> reportingMismatchesOnlyForValues() {
      return subject.reportingMismatchesOnlyForValues();
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> unpackingAnyUsingForValues(
        TypeRegistry typeRegistry, ExtensionRegistry extensionRegistry) {
      return subject.unpackingAnyUsingForValues(typeRegistry, extensionRegistry);
    }

    @Override
    public void containsEntry(@Nullable Object expectedKey, @Nullable M expectedValue) {
      subject
          .usingCorrespondence(Arrays.asList(expectedValue))
          .containsEntry(expectedKey, expectedValue);
    }

    @Override
    public void doesNotContainEntry(@Nullable Object excludedKey, @Nullable M excludedValue) {
      subject
          .usingCorrespondence(Arrays.asList(excludedValue))
          .doesNotContainEntry(excludedKey, excludedValue);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactlyEntriesIn(Multimap<?, ? extends M> expectedMap) {
      return subject
          .usingCorrespondence(expectedMap.values())
          .containsExactlyEntriesIn(expectedMap);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactly() {
      return subject.usingCorrespondence(ImmutableList.of()).containsExactly();
    }

    @Override
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked") // ClassCastException is fine
    public Ordered containsExactly(@Nullable Object k0, @Nullable M v0, @Nullable Object... rest) {
      List<M> expectedValues = new ArrayList<>();
      expectedValues.add(v0);
      for (int i = 1; i < rest.length; i += 2) {
        expectedValues.add((M) rest[i]);
      }
      return subject.usingCorrespondence(expectedValues).containsExactly(k0, v0, rest);
    }

    /*
     * Calling this method is a mistake, so we delegate to a method whose implementation throws an
     * exception to explain the mistake.
     */
    @SuppressWarnings({"DoNotCall", "deprecation"})
    @Override
    @Deprecated
    public boolean equals(Object o) {
      return subject.equals(o);
    }

    // (see equals() just above)
    @SuppressWarnings({"DoNotCall", "deprecation"})
    @Override
    @Deprecated
    public int hashCode() {
      return subject.hashCode();
    }
  }
}
