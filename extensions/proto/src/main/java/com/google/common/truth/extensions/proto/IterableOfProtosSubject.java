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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Ordered;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.Arrays;
import java.util.Comparator;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Truth subject for the iterables of protocol buffers.
 *
 * <p>{@code ProtoTruth.assertThat(actual).containsExactly(expected)} performs the same assertion as
 * {@code Truth.assertThat(actual).containsExactly(expected)}. By default, the assertions are strict
 * with respect to repeated field order, missing fields, etc. This behavior can be changed with the
 * configuration methods on this subject, e.g. {@code
 * ProtoTruth.assertThat(actual).ignoringRepeatedFieldOrder().containsExactlyEntriesIn(expected)}.
 *
 * <p>Floating-point fields are compared using exact equality, which is <a
 * href="http://google.github.io/truth/floating_point">probably not what you want</a> if the values
 * are the results of some arithmetic. Support for approximate equality may be added in a later
 * version.
 *
 * <p>Equality tests, and other methods, may yield slightly different behavior for versions 2 and 3
 * of Protocol Buffers. If testing protos of multiple versions, make sure you understand the
 * behaviors of default and unknown fields so you don't under or over test.
 *
 * @param <S> <b>deprecated -</b> the self-type, allowing {@code this}-returning methods to avoid
 *     needing subclassing. <i>This type parameter will be removed, as the method that needs it is
 *     being removed. You can prepare for this change by editing your class to refer to raw {@code
 *     IterableOfProtosSubject} today and then, after the removal, editing it to refer to {@code
 *     IterableOfProtosSubject<M>} (with a single type parameter).</i>
 * @param <M> the type of the messages in the {@code Iterable}
 * @param <C> <b>deprecated -</b> the type of the {@code Iterable} being tested by this {@code
 *     Subject}. <i>This type parameter will be removed, as the method that needs it is being
 *     removed. You can prepare for this change by editing your class to refer to raw {@code
 *     IterableOfProtosSubject} today and then, after the removal, editing it to refer to {@code
 *     IterableOfProtosSubject<M>} (with a single type parameter).</i>
 */
public class IterableOfProtosSubject<
        S extends IterableOfProtosSubject<S, M, C>, M extends Message, C extends Iterable<M>>
    extends IterableSubject {

  /*
   * Storing a FailureMetadata instance in a Subject subclass is generally a bad practice. For an
   * explanation of why it works out OK here, see LiteProtoSubject.
   */
  private final FailureMetadata metadata;
  private final C actual;
  private final FluentEqualityConfig config;

  /** Default implementation of {@link IterableOfProtosSubject}. */
  public static final class IterableOfMessagesSubject<M extends Message>
      extends IterableOfProtosSubject<IterableOfMessagesSubject<M>, M, Iterable<M>> {
    // Fun generics note! Theoretically we shouldn't have to expose the IterableOfMessagesSubject
    // type at all, but it seems that Java generics handling is either insufficiently intelligent,
    // or beyond my understanding. If we type the iterablesOfProtos() method with the return
    // signature:
    //   <M extends Message> IterableOfProtosSubject.Factory<?, M, Iterable<M>>
    //
    // Then this does not compile, due to type bound conflicts:
    //   Truth.assertAbout(IterableOfProtosSubject.<M>iterablesOfProtos())
    //       .that(listOfProtos)
    //       .contains(foo);
    //
    // But *this* compiles fine!:
    //   IterableOfProtosSubject.Factory<?, M, Iterable<M>> factory =
    //       IterableOfProtosSubject.<M>iterablesOfProtos()
    //   Truth.assertAbout(factory).that(listOfProtos).contains(foo);
    //
    // It seems that when the wild card is captured through inlining, as opposed to being lost and
    // generalized through reference assignment, javac stops being able to intuit that the '?' in
    // 'Factory<?, M, Iterable<M>>' does in fact satisfy the type bound of 'S' in
    // 'S extends Subject<S, T>', even though this is evident from the definition of Factory and
    // from IterableOfProtosSubject.
    //
    // The work around would be annoyingly verbose for users, so we expose IterableOfMessagesSubject
    // explicitly so that there are no wildcards to have conflicting bounds.

    IterableOfMessagesSubject(FailureMetadata failureMetadata, @NullableDecl Iterable<M> messages) {
      super(failureMetadata, messages);
    }

    private IterableOfMessagesSubject(
        FailureMetadata failureMetadata,
        FluentEqualityConfig config,
        @NullableDecl Iterable<M> messages) {
      super(failureMetadata, config, messages);
    }
  }

  protected IterableOfProtosSubject(FailureMetadata failureMetadata, @NullableDecl C messages) {
    this(failureMetadata, FluentEqualityConfig.defaultInstance(), messages);
  }

  IterableOfProtosSubject(
      FailureMetadata failureMetadata, FluentEqualityConfig config, @NullableDecl C messages) {
    super(failureMetadata, messages);
    this.metadata = failureMetadata;
    this.actual = messages;
    this.config = config;
  }

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
  public IterableOfProtosUsingCorrespondence<M> displayingDiffsPairedBy(
      Function<? super M, ?> keyFunction) {
    return usingCorrespondence().displayingDiffsPairedBy(keyFunction);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // ProtoFluentAssertion Configuration
  //////////////////////////////////////////////////////////////////////////////////////////////////

  IterableOfProtosFluentAssertion<M> usingConfig(FluentEqualityConfig newConfig) {
    IterableOfMessagesSubject<M> newSubject =
        new IterableOfMessagesSubject<>(metadata, newConfig, actual);
    if (internalCustomName() != null) {
      newSubject.named(internalCustomName());
    }

    return new IterableOfProtosFluentAssertionImpl<>(newSubject);
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
  public IterableOfProtosFluentAssertion<M> ignoringFieldAbsence() {
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
  public IterableOfProtosFluentAssertion<M> ignoringFieldAbsenceOfFields(
      int firstFieldNumber, int... rest) {
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
  public IterableOfProtosFluentAssertion<M> ignoringFieldAbsenceOfFields(
      Iterable<Integer> fieldNumbers) {
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
  public IterableOfProtosFluentAssertion<M> ignoringFieldAbsenceOfFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> ignoringFieldAbsenceOfFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrder() {
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
  public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrderOfFields(
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
  public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrderOfFields(
      Iterable<Integer> fieldNumbers) {
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
  public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElements() {
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
  public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFields(
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
  public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFields(
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
  public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
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
   * <p>Use {@link #ignoringExtraRepeatedFieldElements()} instead to ignore these for all fields.
   *
   * @see #ignoringExtraRepeatedFieldElements() for details.
   */
  public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> usingDoubleTolerance(double tolerance) {
    return usingConfig(config.usingDoubleTolerance(tolerance));
  }

  /**
   * Compares double fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public IterableOfProtosFluentAssertion<M> usingDoubleToleranceForFields(
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
  public IterableOfProtosFluentAssertion<M> usingDoubleToleranceForFields(
      double tolerance, Iterable<Integer> fieldNumbers) {
    return usingConfig(config.usingDoubleToleranceForFields(tolerance, fieldNumbers));
  }

  /**
   * Compares double fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public IterableOfProtosFluentAssertion<M> usingDoubleToleranceForFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> usingDoubleToleranceForFieldDescriptors(
      double tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.usingDoubleToleranceForFieldDescriptors(tolerance, fieldDescriptors));
  }

  /**
   * Compares float fields as equal if they are both finite and their absolute difference is less
   * than or equal to {@code tolerance}.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public IterableOfProtosFluentAssertion<M> usingFloatTolerance(float tolerance) {
    return usingConfig(config.usingFloatTolerance(tolerance));
  }

  /**
   * Compares float fields with these explicitly specified top-level field numbers using the
   * provided absolute tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public IterableOfProtosFluentAssertion<M> usingFloatToleranceForFields(
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
  public IterableOfProtosFluentAssertion<M> usingFloatToleranceForFields(
      float tolerance, Iterable<Integer> fieldNumbers) {
    return usingConfig(config.usingFloatToleranceForFields(tolerance, fieldNumbers));
  }

  /**
   * Compares float fields with these explicitly specified fields using the provided absolute
   * tolerance.
   *
   * @param tolerance A finite, non-negative tolerance.
   */
  public IterableOfProtosFluentAssertion<M> usingFloatToleranceForFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> usingFloatToleranceForFieldDescriptors(
      float tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.usingFloatToleranceForFieldDescriptors(tolerance, fieldDescriptors));
  }

  /**
   * Limits the comparison of Protocol buffers to the fields set in the expected proto(s). When
   * multiple protos are specified, the comparison is limited to the union of set fields in all the
   * expected protos.
   *
   * <p>The "expected proto(s)" are those passed to the method in {@link
   * IterableOfProtosUsingCorrespondence} at the end of the call-chain.
   *
   * <p>Fields not set in the expected proto(s) are ignored. In particular, proto3 fields which have
   * their default values are ignored, as these are indistinguishable from unset fields. If you want
   * to assert that a proto3 message has certain fields with default values, you cannot use this
   * method.
   */
  public IterableOfProtosFluentAssertion<M> comparingExpectedFieldsOnly() {
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
  public IterableOfProtosFluentAssertion<M> withPartialScope(FieldScope fieldScope) {
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
  public IterableOfProtosFluentAssertion<M> ignoringFields(int firstFieldNumber, int... rest) {
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
  public IterableOfProtosFluentAssertion<M> ignoringFields(Iterable<Integer> fieldNumbers) {
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
  public IterableOfProtosFluentAssertion<M> ignoringFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> ignoringFieldDescriptors(
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
  public IterableOfProtosFluentAssertion<M> ignoringFieldScope(FieldScope fieldScope) {
    return usingConfig(config.ignoringFieldScope(checkNotNull(fieldScope, "fieldScope")));
  }

  /**
   * If set, in the event of a comparison failure, the error message printed will list only those
   * specific fields that did not match between the actual and expected values. Useful for very
   * large protocol buffers.
   *
   * <p>This a purely cosmetic setting, and it has no effect on the behavior of the test.
   */
  public IterableOfProtosFluentAssertion<M> reportingMismatchesOnly() {
    return usingConfig(config.reportingMismatchesOnly());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Overrides for IterableSubject Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @deprecated Protos do not implement {@link Comparable}, so you must {@linkplain
   *     #isInStrictOrder(Comparator) supply a comparator}.
   * @throws ClassCastException always
   */
  @Override
  @Deprecated
  public final void isInStrictOrder() {
    throw new ClassCastException(
        "Protos do not implement Comparable, so you must supply a Comparator.");
  }

  /**
   * @deprecated Protos do not implement {@link Comparable}, so you must {@linkplain
   *     #isInOrder(Comparator) supply a comparator}.
   * @throws ClassCastException always
   */
  @Override
  @Deprecated
  public final void isInOrder() {
    throw new ClassCastException(
        "Protos do not implement Comparable, so you must supply a Comparator.");
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // UsingCorrespondence Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  // A forwarding implementation of IterableSubject.UsingCorrespondence which passes the expected
  // protos to FluentEqualityConfig before comparing.  This is required to support
  // displayingDiffsPairedBy(), since we can't pass the user to a vanilla
  // IterableSubject.UsingCorrespondence until we know what the expected messages are.
  private static class UsingCorrespondence<M extends Message>
      implements IterableOfProtosUsingCorrespondence<M> {
    private final IterableOfProtosSubject<?, M, ?> subject;
    @NullableDecl private final Function<? super M, ? extends Object> keyFunction;

    UsingCorrespondence(
        IterableOfProtosSubject<?, M, ?> subject,
        @NullableDecl Function<? super M, ? extends Object> keyFunction) {
      this.subject = checkNotNull(subject);
      this.keyFunction = keyFunction;
    }

    private IterableSubject.UsingCorrespondence<M, M> delegate(Iterable<? extends M> messages) {
      IterableSubject.UsingCorrespondence<M, M> usingCorrespondence =
          subject.comparingElementsUsing(
              subject
                  .config
                  .withExpectedMessages(messages)
                  .<M>toCorrespondence(FieldScopeUtil.getSingleDescriptor(subject.actual)));
      if (keyFunction != null) {
        usingCorrespondence = usingCorrespondence.displayingDiffsPairedBy(keyFunction);
      }
      return usingCorrespondence;
    }

    @Override
    public IterableOfProtosUsingCorrespondence<M> displayingDiffsPairedBy(
        Function<? super M, ?> keyFunction) {
      return new UsingCorrespondence<M>(subject, checkNotNull(keyFunction));
    }

    @Override
    public void contains(@NullableDecl M expected) {
      delegate(Arrays.asList(expected)).contains(expected);
    }

    @Override
    public void doesNotContain(@NullableDecl M excluded) {
      delegate(Arrays.asList(excluded)).doesNotContain(excluded);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactly(@NullableDecl M... expected) {
      return delegate(Arrays.asList(expected)).containsExactly(expected);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactlyElementsIn(Iterable<? extends M> expected) {
      return delegate(expected).containsExactlyElementsIn(expected);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactlyElementsIn(M[] expected) {
      return delegate(Arrays.asList(expected)).containsExactlyElementsIn(expected);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsAtLeast(
        @NullableDecl M first, @NullableDecl M second, @NullableDecl M... rest) {
      return delegate(Lists.asList(first, second, rest)).containsAtLeast(first, second, rest);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsAtLeastElementsIn(Iterable<? extends M> expected) {
      return delegate(expected).containsAtLeastElementsIn(expected);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsAtLeastElementsIn(M[] expected) {
      return delegate(Arrays.asList(expected)).containsAtLeastElementsIn(expected);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsAllOf(
        @NullableDecl M first, @NullableDecl M second, @NullableDecl M... rest) {
      return containsAtLeast(first, second, rest);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsAllIn(Iterable<? extends M> expected) {
      return containsAtLeastElementsIn(expected);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsAllIn(M[] expected) {
      return containsAtLeastElementsIn(expected);
    }

    @Override
    public void containsAnyOf(
        @NullableDecl M first, @NullableDecl M second, @NullableDecl M... rest) {
      delegate(Lists.asList(first, second, rest)).containsAnyOf(first, second, rest);
    }

    @Override
    public void containsAnyIn(Iterable<? extends M> expected) {
      delegate(expected).containsAnyIn(expected);
    }

    @Override
    public void containsAnyIn(M[] expected) {
      delegate(Arrays.asList(expected)).containsAnyIn(expected);
    }

    @Override
    public void containsNoneOf(
        @NullableDecl M firstExcluded,
        @NullableDecl M secondExcluded,
        @NullableDecl M... restOfExcluded) {
      delegate(Lists.asList(firstExcluded, secondExcluded, restOfExcluded))
          .containsNoneOf(firstExcluded, secondExcluded, restOfExcluded);
    }

    @Override
    public void containsNoneIn(Iterable<? extends M> excluded) {
      delegate(excluded).containsNoneIn(excluded);
    }

    @Override
    public void containsNoneIn(M[] excluded) {
      delegate(Arrays.asList(excluded)).containsNoneIn(excluded);
    }
  }

  private IterableOfProtosUsingCorrespondence<M> usingCorrespondence() {
    return new UsingCorrespondence<M>(this, /* keyFunction= */ null);
  }

  // The UsingCorrespondence methods have conflicting erasure with default IterableSubject methods,
  // so we can't implement them both on the same class, but we want to define both so
  // IterableOfProtosSubjects are interchangeable with IterableSubjects when no configuration is
  // specified. So, we implement a dumb, private delegator to return instead.
  private static final class IterableOfProtosFluentAssertionImpl<M extends Message>
      implements IterableOfProtosFluentAssertion<M> {
    private final IterableOfProtosSubject<?, M, ?> subject;

    IterableOfProtosFluentAssertionImpl(IterableOfProtosSubject<?, M, ?> subject) {
      this.subject = subject;
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFieldAbsence() {
      return subject.ignoringFieldAbsence();
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFieldAbsenceOfFields(
        int firstFieldNumber, int... rest) {
      return subject.ignoringFieldAbsenceOfFields(firstFieldNumber, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFieldAbsenceOfFields(
        Iterable<Integer> fieldNumbers) {
      return subject.ignoringFieldAbsenceOfFields(fieldNumbers);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFieldAbsenceOfFieldDescriptors(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringFieldAbsenceOfFieldDescriptors(firstFieldDescriptor, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFieldAbsenceOfFieldDescriptors(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringFieldAbsenceOfFieldDescriptors(fieldDescriptors);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrder() {
      return subject.ignoringRepeatedFieldOrder();
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrderOfFields(
        int firstFieldNumber, int... rest) {
      return subject.ignoringRepeatedFieldOrderOfFields(firstFieldNumber, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrderOfFields(
        Iterable<Integer> fieldNumbers) {
      return subject.ignoringRepeatedFieldOrderOfFields(fieldNumbers);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldDescriptors(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringRepeatedFieldOrderOfFieldDescriptors(firstFieldDescriptor, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringRepeatedFieldOrderOfFieldDescriptors(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringRepeatedFieldOrderOfFieldDescriptors(fieldDescriptors);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElements() {
      return subject.ignoringExtraRepeatedFieldElements();
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFields(
        int firstFieldNumber, int... rest) {
      return subject.ignoringExtraRepeatedFieldElementsOfFields(firstFieldNumber, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFields(
        Iterable<Integer> fieldNumbers) {
      return subject.ignoringExtraRepeatedFieldElementsOfFields(fieldNumbers);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
          firstFieldDescriptor, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(fieldDescriptors);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingDoubleTolerance(double tolerance) {
      return subject.usingDoubleTolerance(tolerance);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingDoubleToleranceForFields(
        double tolerance, int firstFieldNumber, int... rest) {
      return subject.usingDoubleToleranceForFields(tolerance, firstFieldNumber, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingDoubleToleranceForFields(
        double tolerance, Iterable<Integer> fieldNumbers) {
      return subject.usingDoubleToleranceForFields(tolerance, fieldNumbers);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingDoubleToleranceForFieldDescriptors(
        double tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.usingDoubleToleranceForFieldDescriptors(tolerance, firstFieldDescriptor, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingDoubleToleranceForFieldDescriptors(
        double tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.usingDoubleToleranceForFieldDescriptors(tolerance, fieldDescriptors);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingFloatTolerance(float tolerance) {
      return subject.usingFloatTolerance(tolerance);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingFloatToleranceForFields(
        float tolerance, int firstFieldNumber, int... rest) {
      return subject.usingFloatToleranceForFields(tolerance, firstFieldNumber, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingFloatToleranceForFields(
        float tolerance, Iterable<Integer> fieldNumbers) {
      return subject.usingFloatToleranceForFields(tolerance, fieldNumbers);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingFloatToleranceForFieldDescriptors(
        float tolerance, FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.usingFloatToleranceForFieldDescriptors(tolerance, firstFieldDescriptor, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> usingFloatToleranceForFieldDescriptors(
        float tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.usingFloatToleranceForFieldDescriptors(tolerance, fieldDescriptors);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> comparingExpectedFieldsOnly() {
      return subject.comparingExpectedFieldsOnly();
    }

    @Override
    public IterableOfProtosFluentAssertion<M> withPartialScope(FieldScope fieldScope) {
      return subject.withPartialScope(fieldScope);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFields(int firstFieldNumber, int... rest) {
      return subject.ignoringFields(firstFieldNumber, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFields(Iterable<Integer> fieldNumbers) {
      return subject.ignoringFields(fieldNumbers);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFieldDescriptors(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringFieldDescriptors(firstFieldDescriptor, rest);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFieldDescriptors(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringFieldDescriptors(fieldDescriptors);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> ignoringFieldScope(FieldScope fieldScope) {
      return subject.ignoringFieldScope(fieldScope);
    }

    @Override
    public IterableOfProtosFluentAssertion<M> reportingMismatchesOnly() {
      return subject.reportingMismatchesOnly();
    }

    @Override
    public IterableOfProtosUsingCorrespondence<M> displayingDiffsPairedBy(
        Function<? super M, ?> keyFunction) {
      return usingCorrespondence().displayingDiffsPairedBy(keyFunction);
    }

    @Override
    public void contains(@NullableDecl M expected) {
      usingCorrespondence().contains(expected);
    }

    @Override
    public void doesNotContain(@NullableDecl M excluded) {
      usingCorrespondence().doesNotContain(excluded);
    }

    @Override
    public Ordered containsExactly(@NullableDecl M... expected) {
      return usingCorrespondence().containsExactly(expected);
    }

    @Override
    public Ordered containsExactlyElementsIn(Iterable<? extends M> expected) {
      return usingCorrespondence().containsExactlyElementsIn(expected);
    }

    @Override
    public Ordered containsExactlyElementsIn(M[] expected) {
      return usingCorrespondence().containsExactlyElementsIn(expected);
    }

    @Override
    public Ordered containsAtLeast(
        @NullableDecl M first, @NullableDecl M second, @NullableDecl M... rest) {
      return usingCorrespondence().containsAtLeast(first, second, rest);
    }

    @Override
    public Ordered containsAtLeastElementsIn(Iterable<? extends M> expected) {
      return usingCorrespondence().containsAtLeastElementsIn(expected);
    }

    @Override
    public Ordered containsAtLeastElementsIn(M[] expected) {
      return usingCorrespondence().containsAtLeastElementsIn(expected);
    }

    @Override
    public Ordered containsAllOf(
        @NullableDecl M first, @NullableDecl M second, @NullableDecl M... rest) {
      return containsAtLeast(first, second, rest);
    }

    @Override
    public Ordered containsAllIn(Iterable<? extends M> expected) {
      return containsAtLeastElementsIn(expected);
    }

    @Override
    public Ordered containsAllIn(M[] expected) {
      return containsAtLeastElementsIn(expected);
    }

    @Override
    public void containsAnyOf(
        @NullableDecl M first, @NullableDecl M second, @NullableDecl M... rest) {
      usingCorrespondence().containsAnyOf(first, second, rest);
    }

    @Override
    public void containsAnyIn(Iterable<? extends M> expected) {
      usingCorrespondence().containsAnyIn(expected);
    }

    @Override
    public void containsAnyIn(M[] expected) {
      usingCorrespondence().containsAnyIn(expected);
    }

    @Override
    public void containsNoneOf(
        @NullableDecl M firstExcluded,
        @NullableDecl M secondExcluded,
        @NullableDecl M... restOfExcluded) {
      usingCorrespondence().containsNoneOf(firstExcluded, secondExcluded, restOfExcluded);
    }

    @Override
    public void containsNoneIn(Iterable<? extends M> excluded) {
      usingCorrespondence().containsNoneIn(excluded);
    }

    @Override
    public void containsNoneIn(M[] excluded) {
      usingCorrespondence().containsNoneIn(excluded);
    }

    @Override
    @Deprecated
    public boolean equals(Object o) {
      return subject.equals(o);
    }

    @Override
    @Deprecated
    public int hashCode() {
      return subject.hashCode();
    }

    private final IterableOfProtosUsingCorrespondence<M> usingCorrespondence() {
      return subject.usingCorrespondence();
    }
  }
}
