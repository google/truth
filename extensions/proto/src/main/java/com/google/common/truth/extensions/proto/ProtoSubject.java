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
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.extensions.proto.FieldScopeUtil.asList;

import com.google.common.base.Objects;
import com.google.common.truth.FailureMetadata;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.TypeRegistry;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Truth subject for the full version of Protocol Buffers.
 *
 * <p>{@code ProtoTruth.assertThat(actual).isEqualTo(expected)} performs the same assertion as
 * {@code Truth.assertThat(actual).isEqualTo(expected)}, but with a better failure message. By
 * default, the assertions are strict with respect to repeated field order, missing fields, etc.
 * This behavior can be changed with the configuration methods on this subject, e.g. {@code
 * ProtoTruth.assertThat(actual).ignoringRepeatedFieldOrder().isEqualTo(expected)}.
 *
 * <p>By default, floating-point fields are compared using exact equality, which is <a
 * href="https://truth.dev/floating_point">probably not what you want</a> if the values are the
 * results of some arithmetic. To check for approximate equality, use {@link #usingDoubleTolerance},
 * {@link #usingFloatTolerance}, and {@linkplain #usingDoubleToleranceForFields(double, int, int...)
 * their per-field equivalents}.
 *
 * <p>Equality tests, and other methods, may yield slightly different behavior for versions 2 and 3
 * of Protocol Buffers. If testing protos of multiple versions, make sure you understand the
 * behaviors of default and unknown fields so you don't under or over test.
 */
public class ProtoSubject extends LiteProtoSubject {

  /*
   * Storing a FailureMetadata instance in a Subject subclass is generally a bad practice. For an
   * explanation of why it works out OK here, see LiteProtoSubject.
   */
  private final FailureMetadata metadata;
  private final Message actual;
  private final FluentEqualityConfig config;

  protected ProtoSubject(FailureMetadata failureMetadata, @Nullable Message message) {
    this(failureMetadata, FluentEqualityConfig.defaultInstance(), message);
  }

  ProtoSubject(
      FailureMetadata failureMetadata, FluentEqualityConfig config, @Nullable Message message) {
    super(failureMetadata, message);
    this.metadata = failureMetadata;
    this.actual = message;
    this.config = config;
  }

  ProtoFluentAssertionImpl usingConfig(FluentEqualityConfig newConfig) {
    return new ProtoFluentAssertionImpl(new ProtoSubject(metadata, newConfig, actual));
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
  public ProtoFluentAssertion ignoringFieldAbsence() {
    return usingConfig(config.ignoringFieldAbsence());
  }

  /**
   * Specifies that the 'has' bit of these explicitly specified top-level field numbers should be
   * ignored when comparing for equality. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if they are to be ignored as well.
   *
   * <p>Use {@link #ignoringFieldAbsence()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsence() for details
   */
  public ProtoFluentAssertion ignoringFieldAbsenceOfFields(int firstFieldNumber, int... rest) {
    return usingConfig(config.ignoringFieldAbsenceOfFields(asList(firstFieldNumber, rest)));
  }

  /**
   * Specifies that the 'has' bit of these explicitly specified top-level field numbers should be
   * ignored when comparing for equality. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if they are to be ignored as well.
   *
   * <p>Use {@link #ignoringFieldAbsence()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsence() for details
   */
  public ProtoFluentAssertion ignoringFieldAbsenceOfFields(Iterable<Integer> fieldNumbers) {
    return usingConfig(config.ignoringFieldAbsenceOfFields(fieldNumbers));
  }

  /**
   * Specifies that the 'has' bit of these explicitly specified field descriptors should be ignored
   * when comparing for equality. Sub-fields must be specified explicitly if they are to be ignored
   * as well.
   *
   * <p>Use {@link #ignoringFieldAbsence()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsence() for details
   */
  public ProtoFluentAssertion ignoringFieldAbsenceOfFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return usingConfig(
        config.ignoringFieldAbsenceOfFieldDescriptors(asList(firstFieldDescriptor, rest)));
  }

  /**
   * Specifies that the 'has' bit of these explicitly specified field descriptors should be ignored
   * when comparing for equality. Sub-fields must be specified explicitly if they are to be ignored
   * as well.
   *
   * <p>Use {@link #ignoringFieldAbsence()} instead to ignore the 'has' bit for all fields.
   *
   * @see #ignoringFieldAbsence() for details
   */
  public ProtoFluentAssertion ignoringFieldAbsenceOfFieldDescriptors(
      Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.ignoringFieldAbsenceOfFieldDescriptors(fieldDescriptors));
  }

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
  public ProtoFluentAssertion ignoringRepeatedFieldOrder() {
    return usingConfig(config.ignoringRepeatedFieldOrder());
  }

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified top-level field
   * numbers should be ignored when comparing for equality. Sub-fields must be specified explicitly
   * (via {@link FieldDescriptor}) if their orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrder()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrder() for details.
   */
  public ProtoFluentAssertion ignoringRepeatedFieldOrderOfFields(
      int firstFieldNumber, int... rest) {
    return usingConfig(config.ignoringRepeatedFieldOrderOfFields(asList(firstFieldNumber, rest)));
  }

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified top-level field
   * numbers should be ignored when comparing for equality. Sub-fields must be specified explicitly
   * (via {@link FieldDescriptor}) if their orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrder()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrder() for details.
   */
  public ProtoFluentAssertion ignoringRepeatedFieldOrderOfFields(Iterable<Integer> fieldNumbers) {
    return usingConfig(config.ignoringRepeatedFieldOrderOfFields(fieldNumbers));
  }

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified field descriptors
   * should be ignored when comparing for equality. Sub-fields must be specified explicitly if their
   * orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrder()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrder() for details.
   */
  public ProtoFluentAssertion ignoringRepeatedFieldOrderOfFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return usingConfig(
        config.ignoringRepeatedFieldOrderOfFieldDescriptors(asList(firstFieldDescriptor, rest)));
  }

  /**
   * Specifies that the ordering of repeated fields for these explicitly specified field descriptors
   * should be ignored when comparing for equality. Sub-fields must be specified explicitly if their
   * orders are to be ignored as well.
   *
   * <p>Use {@link #ignoringRepeatedFieldOrder()} instead to ignore order for all fields.
   *
   * @see #ignoringRepeatedFieldOrder() for details.
   */
  public ProtoFluentAssertion ignoringRepeatedFieldOrderOfFieldDescriptors(
      Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.ignoringRepeatedFieldOrderOfFieldDescriptors(fieldDescriptors));
  }

  /**
   * Specifies that, for all repeated and map fields, any elements in the 'actual' proto which are
   * not found in the 'expected' proto are ignored, with the exception of fields in the expected
   * proto which are empty. To ignore empty repeated fields as well, use {@link
   * #comparingExpectedFieldsOnly}.
   *
   * <p>This rule is applied independently from {@link #ignoringRepeatedFieldOrder}. If ignoring
   * repeated field order AND extra repeated field elements, all that is tested is that the expected
   * elements comprise a subset of the actual elements. If not ignoring repeated field order, but
   * still ignoring extra repeated field elements, the actual elements must contain a subsequence
   * that matches the expected elements for the test to pass. (The subsequence rule does not apply
   * to Map fields, which are always compared by key.)
   */
  public ProtoFluentAssertion ignoringExtraRepeatedFieldElements() {
    return usingConfig(config.ignoringExtraRepeatedFieldElements());
  }

  /**
   * Specifies that extra repeated field elements for these explicitly specified top-level field
   * numbers should be ignored. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if their extra elements are to be ignored as well.
   *
   * <p>Use {@link #ignoringExtraRepeatedFieldElements()} instead to ignore these for all fields.
   *
   * @see #ignoringExtraRepeatedFieldElements() for details.
   */
  public ProtoFluentAssertion ignoringExtraRepeatedFieldElementsOfFields(
      int firstFieldNumber, int... rest) {
    return usingConfig(
        config.ignoringExtraRepeatedFieldElementsOfFields(asList(firstFieldNumber, rest)));
  }

  /**
   * Specifies that extra repeated field elements for these explicitly specified top-level field
   * numbers should be ignored. Sub-fields must be specified explicitly (via {@link
   * FieldDescriptor}) if their extra elements are to be ignored as well.
   *
   * <p>Use {@link #ignoringExtraRepeatedFieldElements()} instead to ignore these for all fields.
   *
   * @see #ignoringExtraRepeatedFieldElements() for details.
   */
  public ProtoFluentAssertion ignoringExtraRepeatedFieldElementsOfFields(
      Iterable<Integer> fieldNumbers) {
    return usingConfig(config.ignoringExtraRepeatedFieldElementsOfFields(fieldNumbers));
  }

  /**
   * Specifies that extra repeated field elements for these explicitly specified field descriptors
   * should be ignored. Sub-fields must be specified explicitly if their extra elements are to be
   * ignored as well.
   *
   * <p>Use {@link #ignoringExtraRepeatedFieldElements()} instead to ignore these for all fields.
   *
   * @see #ignoringExtraRepeatedFieldElements() for details.
   */
  public ProtoFluentAssertion ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
      FieldDescriptor first, FieldDescriptor... rest) {
    return usingConfig(
        config.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(asList(first, rest)));
  }

  /**
   * Specifies that extra repeated field elements for these explicitly specified field descriptors
   * should be ignored. Sub-fields must be specified explicitly if their extra elements are to be
   * ignored as well.
   *
   * <p>Use {@link #ignoringExtraRepeatedFieldElements()} instead to ignore these for all fields.
   *
   * @see #ignoringExtraRepeatedFieldElements() for details.
   */
  public ProtoFluentAssertion ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
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
  public ProtoFluentAssertion usingDoubleTolerance(double tolerance) {
    return usingConfig(config.usingDoubleTolerance(tolerance));
  }

  /**
   * Compares double fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public ProtoFluentAssertion usingDoubleToleranceForFields(
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
  public ProtoFluentAssertion usingDoubleToleranceForFields(
      double tolerance, Iterable<Integer> fieldNumbers) {
    return usingConfig(config.usingDoubleToleranceForFields(tolerance, fieldNumbers));
  }

  /**
   * Compares double fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public ProtoFluentAssertion usingDoubleToleranceForFieldDescriptors(
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
  public ProtoFluentAssertion usingDoubleToleranceForFieldDescriptors(
      double tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.usingDoubleToleranceForFieldDescriptors(tolerance, fieldDescriptors));
  }

  /**
   * Compares float fields as equal if they are both finite and their absolute difference is less
   * than or equal to {@code tolerance}.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public ProtoFluentAssertion usingFloatTolerance(float tolerance) {
    return usingConfig(config.usingFloatTolerance(tolerance));
  }

  /**
   * Compares float fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public ProtoFluentAssertion usingFloatToleranceForFields(
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
  public ProtoFluentAssertion usingFloatToleranceForFields(
      float tolerance, Iterable<Integer> fieldNumbers) {
    return usingConfig(config.usingFloatToleranceForFields(tolerance, fieldNumbers));
  }

  /**
   * Compares float fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public ProtoFluentAssertion usingFloatToleranceForFieldDescriptors(
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
  public ProtoFluentAssertion usingFloatToleranceForFieldDescriptors(
      float tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.usingFloatToleranceForFieldDescriptors(tolerance, fieldDescriptors));
  }

  /**
   * Limits the comparison of Protocol buffers to the fields set in the expected proto(s). When
   * multiple protos are specified, the comparison is limited to the union of set fields in all the
   * expected protos.
   *
   * <p>The "expected proto(s)" are those passed to the void method at the end of the {@code
   * ProtoFluentAssertion} call-chain: For example, {@link #isEqualTo(Message)}, or {@link
   * #isNotEqualTo(Message)}.
   *
   * <p>Fields not set in the expected proto(s) are ignored. In particular, proto3 fields which have
   * their default values are ignored, as these are indistinguishable from unset fields. If you want
   * to assert that a proto3 message has certain fields with default values, you cannot use this
   * method.
   */
  public ProtoFluentAssertion comparingExpectedFieldsOnly() {
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
   * <p>By default, {@link ProtoFluentAssertion} is constrained to {@link FieldScopes#all()}, that
   * is, no fields are excluded from comparison.
   */
  public ProtoFluentAssertion withPartialScope(FieldScope fieldScope) {
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
  public ProtoFluentAssertion ignoringFields(int firstFieldNumber, int... rest) {
    return ignoringFields(asList(firstFieldNumber, rest));
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
  public ProtoFluentAssertion ignoringFields(Iterable<Integer> fieldNumbers) {
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
  public ProtoFluentAssertion ignoringFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return ignoringFieldDescriptors(asList(firstFieldDescriptor, rest));
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
  public ProtoFluentAssertion ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
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
  public ProtoFluentAssertion ignoringFieldScope(FieldScope fieldScope) {
    return usingConfig(config.ignoringFieldScope(checkNotNull(fieldScope, "fieldScope")));
  }

  /**
   * If set, in the event of a comparison failure, the error message printed will list only those
   * specific fields that did not match between the actual and expected values. Useful for very
   * large protocol buffers.
   *
   * <p>This a purely cosmetic setting, and it has no effect on the behavior of the test.
   */
  public ProtoFluentAssertion reportingMismatchesOnly() {
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
  public ProtoFluentAssertion unpackingAnyUsing(
      TypeRegistry typeRegistry, ExtensionRegistry extensionRegistry) {
    return usingConfig(config.unpackingAnyUsing(typeRegistry, extensionRegistry));
  }

  private static boolean sameClassMessagesWithDifferentDescriptors(
      @Nullable Message actual, @Nullable Object expected) {
    if (actual == null
        || !(expected instanceof Message)
        || actual.getClass() != expected.getClass()) {
      return false;
    }

    return actual.getDescriptorForType() != ((Message) expected).getDescriptorForType();
  }

  private static boolean notMessagesWithSameDescriptor(
      @Nullable Message actual, @Nullable Object expected) {
    if (actual != null && expected instanceof Message) {
      return actual.getDescriptorForType() != ((Message) expected).getDescriptorForType();
    }
    return true;
  }

  @Override
  public void isEqualTo(@Nullable Object expected) {
    if (sameClassMessagesWithDifferentDescriptors(actual, expected)) {
      // This can happen with DynamicMessages, and it's very confusing if they both have the
      // same string.
      failWithoutActual(
          simpleFact("Not true that messages compare equal; they have different descriptors."),
          fact("expected", expected),
          fact("with descriptor", ((Message) expected).getDescriptorForType()),
          fact("but was", actual),
          fact("with descriptor", actual.getDescriptorForType()));
    } else if (notMessagesWithSameDescriptor(actual, expected)) {
      super.isEqualTo(expected);
    } else {
      DiffResult diffResult =
          makeDifferencer((Message) expected).diffMessages(actual, (Message) expected);
      if (!diffResult.isMatched()) {
        failWithoutActual(
            simpleFact(
                "Not true that messages compare equal.\n"
                    + diffResult.printToString(config.reportMismatchesOnly())));
      }
    }
  }

  @Override
  public void isNotEqualTo(@Nullable Object expected) {
    if (notMessagesWithSameDescriptor(actual, expected)) {
      super.isNotEqualTo(expected);
    } else {
      DiffResult diffResult =
          makeDifferencer((Message) expected).diffMessages(actual, (Message) expected);
      if (diffResult.isMatched()) {
        failWithoutActual(
            simpleFact(
                "Not true that messages compare not equal.\n"
                    + diffResult.printToString(config.reportMismatchesOnly())));
      }
    }
  }

  @Override
  public void hasAllRequiredFields() {
    if (!actual.isInitialized()) {
      failWithoutActual(
          simpleFact("expected to have all required fields set"),
          fact("but was missing", actual.findInitializationErrors()),
          fact("proto was", actualCustomStringRepresentationForProtoPackageMembersToCall()));
    }
  }

  private ProtoTruthMessageDifferencer makeDifferencer(Message expected) {
    return config
        .withExpectedMessages(Arrays.asList(expected))
        .toMessageDifferencer(actual.getDescriptorForType());
  }

  static final class ProtoFluentAssertionImpl implements ProtoFluentAssertion {
    private final ProtoSubject protoSubject;

    ProtoFluentAssertionImpl(ProtoSubject protoSubject) {
      this.protoSubject = protoSubject;
    }

    @Override
    public ProtoFluentAssertion ignoringFieldAbsence() {
      return protoSubject.ignoringFieldAbsence();
    }

    @Override
    public ProtoFluentAssertion ignoringFieldAbsenceOfFields(int firstFieldNumber, int... rest) {
      return protoSubject.ignoringFieldAbsenceOfFields(firstFieldNumber, rest);
    }

    @Override
    public ProtoFluentAssertion ignoringFieldAbsenceOfFields(Iterable<Integer> fieldNumbers) {
      return protoSubject.ignoringFieldAbsenceOfFields(fieldNumbers);
    }

    @Override
    public ProtoFluentAssertion ignoringFieldAbsenceOfFieldDescriptors(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return protoSubject.ignoringFieldAbsenceOfFieldDescriptors(firstFieldDescriptor, rest);
    }

    @Override
    public ProtoFluentAssertion ignoringFieldAbsenceOfFieldDescriptors(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return protoSubject.ignoringFieldAbsenceOfFieldDescriptors(fieldDescriptors);
    }

    @Override
    public ProtoFluentAssertion ignoringRepeatedFieldOrder() {
      return protoSubject.ignoringRepeatedFieldOrder();
    }

    @Override
    public ProtoFluentAssertion ignoringRepeatedFieldOrderOfFields(
        int firstFieldNumber, int... rest) {
      return protoSubject.ignoringRepeatedFieldOrderOfFields(firstFieldNumber, rest);
    }

    @Override
    public ProtoFluentAssertion ignoringRepeatedFieldOrderOfFields(Iterable<Integer> fieldNumbers) {
      return protoSubject.ignoringRepeatedFieldOrderOfFields(fieldNumbers);
    }

    @Override
    public ProtoFluentAssertion ignoringRepeatedFieldOrderOfFieldDescriptors(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return protoSubject.ignoringRepeatedFieldOrderOfFieldDescriptors(firstFieldDescriptor, rest);
    }

    @Override
    public ProtoFluentAssertion ignoringRepeatedFieldOrderOfFieldDescriptors(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return protoSubject.ignoringRepeatedFieldOrderOfFieldDescriptors(fieldDescriptors);
    }

    @Override
    public ProtoFluentAssertion ignoringExtraRepeatedFieldElements() {
      return protoSubject.ignoringExtraRepeatedFieldElements();
    }

    @Override
    public ProtoFluentAssertion ignoringExtraRepeatedFieldElementsOfFields(
        int firstFieldNumber, int... rest) {
      return protoSubject.ignoringExtraRepeatedFieldElementsOfFields(firstFieldNumber, rest);
    }

    @Override
    public ProtoFluentAssertion ignoringExtraRepeatedFieldElementsOfFields(
        Iterable<Integer> fieldNumbers) {
      return protoSubject.ignoringExtraRepeatedFieldElementsOfFields(fieldNumbers);
    }

    @Override
    public ProtoFluentAssertion ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return protoSubject.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
          firstFieldDescriptor, rest);
    }

    @Override
    public ProtoFluentAssertion ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return protoSubject.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(fieldDescriptors);
    }

    @Override
    public ProtoFluentAssertion usingDoubleTolerance(double tolerance) {
      return protoSubject.usingDoubleTolerance(tolerance);
    }

    @Override
    public ProtoFluentAssertion usingDoubleToleranceForFields(
        double tolerance, int firstFieldNumber, int... rest) {
      return protoSubject.usingDoubleToleranceForFields(tolerance, firstFieldNumber, rest);
    }

    @Override
    public ProtoFluentAssertion usingDoubleToleranceForFields(
        double tolerance, Iterable<Integer> fieldNumbers) {
      return protoSubject.usingDoubleToleranceForFields(tolerance, fieldNumbers);
    }

    @Override
    public ProtoFluentAssertion usingDoubleToleranceForFieldDescriptors(
        double tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return protoSubject.usingDoubleToleranceForFieldDescriptors(
          tolerance, firstFieldDescriptor, rest);
    }

    @Override
    public ProtoFluentAssertion usingDoubleToleranceForFieldDescriptors(
        double tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
      return protoSubject.usingDoubleToleranceForFieldDescriptors(tolerance, fieldDescriptors);
    }

    @Override
    public ProtoFluentAssertion usingFloatTolerance(float tolerance) {
      return protoSubject.usingFloatTolerance(tolerance);
    }

    @Override
    public ProtoFluentAssertion usingFloatToleranceForFields(
        float tolerance, int firstFieldNumber, int... rest) {
      return protoSubject.usingFloatToleranceForFields(tolerance, firstFieldNumber, rest);
    }

    @Override
    public ProtoFluentAssertion usingFloatToleranceForFields(
        float tolerance, Iterable<Integer> fieldNumbers) {
      return protoSubject.usingFloatToleranceForFields(tolerance, fieldNumbers);
    }

    @Override
    public ProtoFluentAssertion usingFloatToleranceForFieldDescriptors(
        float tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return protoSubject.usingFloatToleranceForFieldDescriptors(
          tolerance, firstFieldDescriptor, rest);
    }

    @Override
    public ProtoFluentAssertion usingFloatToleranceForFieldDescriptors(
        float tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
      return protoSubject.usingFloatToleranceForFieldDescriptors(tolerance, fieldDescriptors);
    }

    @Override
    public ProtoFluentAssertion comparingExpectedFieldsOnly() {
      return protoSubject.comparingExpectedFieldsOnly();
    }

    @Override
    public ProtoFluentAssertion withPartialScope(FieldScope fieldScope) {
      return protoSubject.withPartialScope(fieldScope);
    }

    @Override
    public ProtoFluentAssertion ignoringFields(int firstFieldNumber, int... rest) {
      return protoSubject.ignoringFields(firstFieldNumber, rest);
    }

    @Override
    public ProtoFluentAssertion ignoringFields(Iterable<Integer> fieldNumbers) {
      return protoSubject.ignoringFields(fieldNumbers);
    }

    @Override
    public ProtoFluentAssertion ignoringFieldDescriptors(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return protoSubject.ignoringFieldDescriptors(firstFieldDescriptor, rest);
    }

    @Override
    public ProtoFluentAssertion ignoringFieldDescriptors(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return protoSubject.ignoringFieldDescriptors(fieldDescriptors);
    }

    @Override
    public ProtoFluentAssertion ignoringFieldScope(FieldScope fieldScope) {
      return protoSubject.ignoringFieldScope(fieldScope);
    }

    @Override
    public ProtoFluentAssertion reportingMismatchesOnly() {
      return protoSubject.reportingMismatchesOnly();
    }

    @Override
    public ProtoFluentAssertion unpackingAnyUsing(
        TypeRegistry typeRegistry, ExtensionRegistry extensionRegistry) {
      return protoSubject.unpackingAnyUsing(typeRegistry, extensionRegistry);
    }

    @Override
    public void isEqualTo(@Nullable Message expected) {
      protoSubject.isEqualTo(expected);
    }

    @Override
    public void isNotEqualTo(@Nullable Message expected) {
      protoSubject.isNotEqualTo(expected);
    }

    /**
     * Same as {@link #isEqualTo(Message)}, except it returns true on success and false on failure
     * without throwing any exceptions.
     */
    boolean testIsEqualTo(@Nullable Message expected) {
      if (notMessagesWithSameDescriptor(protoSubject.actual, expected)) {
        return Objects.equal(protoSubject.actual, expected);
      } else {
        return protoSubject
            .makeDifferencer(expected)
            .diffMessages(protoSubject.actual, expected)
            .isMatched();
      }
    }
  }
}
