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

import com.google.common.truth.Correspondence;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.MapSubject;
import com.google.common.truth.Ordered;
import com.google.common.truth.Subject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Truth subject for maps with protocol buffers for values.
 *
 * <p>{@code ProtoTruth.assertThat(actual).containsExactlyEntriesIn(expected)} performs the same
 * assertion as {@code Truth.assertThat(actual).containsExactlyEntriesIn(expected)}. By default, the
 * assertions are strict with respect to repeated field order, missing fields, etc. This behavior
 * can be changed with the configuration methods on this subject, e.g. {@code
 * ProtoTruth.assertThat(actual).ignoringRepeatedFieldOrder().containsExactly(expected)}.
 *
 * <p>Equality tests, and other methods, may yield slightly different behavior for versions 2 and 3
 * of Protocol Buffers. If testing protos of multiple versions, make sure you understand the
 * behaviors of default and unknown fields so you don't under or over test.
 */
public class MapWithProtoValuesSubject<
        S extends MapWithProtoValuesSubject<S, K, M, C>, K, M extends Message, C extends Map<K, M>>
    extends Subject<S, C> {

  private final FluentEqualityConfig config;

  /** Default implementation of {@link MapWithProtoValuesSubject}. */
  public static final class MapWithMessageValuesSubject<K, M extends Message>
      extends MapWithProtoValuesSubject<MapWithMessageValuesSubject<K, M>, K, M, Map<K, M>> {
    // See IterableOfProtosSubject.IterableOfMessagesSubject for why this class is exposed.

    MapWithMessageValuesSubject(FailureStrategy failureStrategy, @Nullable Map<K, M> map) {
      super(failureStrategy, map);
    }

    private MapWithMessageValuesSubject(
        FailureStrategy failureStrategy, FluentEqualityConfig config, @Nullable Map<K, M> map) {
      super(failureStrategy, config, map);
    }
  }

  protected MapWithProtoValuesSubject(FailureStrategy failureStrategy, @Nullable C map) {
    this(failureStrategy, FluentEqualityConfig.defaultInstance(), map);
  }

  MapWithProtoValuesSubject(
      FailureStrategy failureStrategy, FluentEqualityConfig config, @Nullable C map) {
    super(failureStrategy, map);
    this.config = config;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // MapSubject methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private final MapSubject delegate() {
    MapSubject delegate = check().that(actual());
    if (internalCustomName() != null) {
      delegate = delegate.named(internalCustomName());
    }
    return delegate;
  }

  /** Fails if the subject is not equal to the given object. */
  @Override
  public void isEqualTo(@Nullable Object other) {
    delegate().isEqualTo(other);
  }

  /** Fails if the map is not empty. */
  public void isEmpty() {
    delegate().isEmpty();
  }

  /** Fails if the map is empty. */
  public void isNotEmpty() {
    delegate().isNotEmpty();
  }

  /** Fails if the map does not have the given size. */
  public void hasSize(int expectedSize) {
    delegate().hasSize(expectedSize);
  }

  /** Fails if the map does not contain the given key. */
  public void containsKey(@Nullable Object key) {
    delegate().containsKey(key);
  }

  /** Fails if the map contains the given key. */
  public void doesNotContainKey(@Nullable Object key) {
    delegate().doesNotContainKey(key);
  }

  /** Fails if the map does not contain the given entry. */
  public void containsEntry(@Nullable Object key, @Nullable Object value) {
    delegate().containsEntry(key, value);
  }

  /** Fails if the map contains the given entry. */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    delegate().doesNotContainEntry(key, value);
  }

  /** Fails if the map is not empty. */
  @CanIgnoreReturnValue
  public Ordered containsExactly() {
    return delegate().containsExactly();
  }

  /**
   * Fails if the map does not contain exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   */
  @CanIgnoreReturnValue
  public Ordered containsExactly(@Nullable Object k0, @Nullable Object v0, Object... rest) {
    return delegate().containsExactly(k0, v0, rest);
  }

  /** Fails if the map does not contain exactly the given set of entries in the given map. */
  @CanIgnoreReturnValue
  public Ordered containsExactlyEntriesIn(Map<?, ?> expectedMap) {
    return delegate().containsExactlyEntriesIn(expectedMap);
  }

  /**
   * Starts a method chain for a check in which the actual values (i.e. the values of the {@link
   * Map} under test) are compared to expected values using the given {@link Correspondence}. The
   * actual values must be of type {@code A}, the expected values must be of type {@code E}. The
   * check is actually executed by continuing the method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualMap)
   *   .comparingValuesUsing(correspondence)
   *   .containsEntry(expectedKey, expectedValue);
   * }</pre>
   *
   * where {@code actualMap} is a {@code Map<?, A>} (or, more generally, a {@code Map<?, ? extends
   * A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and {@code expectedValue} is an
   * {@code E}.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual value that is not of type {@code A} or an expected value that is not of
   * type {@code E}.
   *
   * <p>Note that the {@code MapWithProtoValuesSubject} is designed to save you from having to write
   * your own {@link Correspondence}. The configuration methods, such as {@link
   * #ignoringRepeatedFieldOrder()} will construct a {@link Correspondence} under the hood which
   * performs protobuf comparisons with {@link #ignoringRepeatedFieldOrder()}.
   */
  public <A, E> MapSubject.UsingCorrespondence<A, E> comparingValuesUsing(
      Correspondence<A, E> correspondence) {
    return delegate().comparingValuesUsing(correspondence);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // MapWithProtoValuesFluentAssertion Configuration
  //////////////////////////////////////////////////////////////////////////////////////////////////

  MapWithProtoValuesFluentAssertion<M> usingConfig(FluentEqualityConfig newConfig) {
    MapWithMessageValuesSubject<K, M> newSubject =
        new MapWithMessageValuesSubject<K, M>(failureStrategy, newConfig, actual());
    if (internalCustomName() != null) {
      newSubject = newSubject.named(internalCustomName());
    }
    return new MapWithProtoValuesFluentAssertionImpl<M>(newSubject);
  }

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
  public MapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceForValues() {
    return usingConfig(config.ignoringFieldAbsence());
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
  public MapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderForValues() {
    return usingConfig(config.ignoringRepeatedFieldOrder());
  }

  /**
   * Limits the comparison of Protocol buffers to the defined {@link FieldScope}.
   *
   * <p>This method is additive and has well-defined ordering semantics. If the invoking {@link
   * ProtoFluentAssertion} is already scoped to a {@link FieldScope} {@code X}, and this method is
   * invoked with {@link FieldScope} {@code Y}, the resultant {@link ProtoFluentAssertion} is
   * constrained to the intersection of {@link FieldScope}s {@code X} and {@code Y}.
   *
   * <p>By default, {@link MapWithProtoValuesFluentAssertion} is constrained to {@link
   * FieldScopes#all()}, that is, no fields are excluded from comparison.
   */
  public MapWithProtoValuesFluentAssertion<M> withPartialScopeForValues(FieldScope fieldScope) {
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
  public MapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
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
  public MapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
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
  public MapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
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
  public MapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
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
  public MapWithProtoValuesFluentAssertion<M> ignoringFieldScopeForValues(FieldScope fieldScope) {
    return usingConfig(config.ignoringFieldScope(checkNotNull(fieldScope, "fieldScope")));
  }

  /**
   * If set, in the event of a comparison failure, the error message printed will list only those
   * specific fields that did not match between the actual and expected values. Useful for very
   * large protocol buffers.
   *
   * <p>This a purely cosmetic setting, and it has no effect on the behavior of the test.
   */
  public MapWithProtoValuesFluentAssertion<M> reportingMismatchesOnlyForValues() {
    return usingConfig(config.reportingMismatchesOnly());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // UsingCorrespondence Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private MapSubject.UsingCorrespondence<M, M> usingCorrespondence() {
    return comparingValuesUsing(
        config.<M>toCorrespondence(FieldScopeUtil.getSingleDescriptor(actual().values())));
  }

  // The UsingCorrespondence methods have conflicting erasure with default MapSubject methods,
  // so we can't implement them both on the same class, but we want to define both so
  // MapWithProtoValuesSubjects are interchangeable with MapSubjects when no configuration is
  // specified. So, we implement a dumb, private delegator to return instead.
  private static final class MapWithProtoValuesFluentAssertionImpl<M extends Message>
      implements MapWithProtoValuesFluentAssertion<M> {
    private final MapWithProtoValuesSubject<?, ?, M, ?> subject;

    MapWithProtoValuesFluentAssertionImpl(MapWithProtoValuesSubject<?, ?, M, ?> subject) {
      this.subject = subject;
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceForValues() {
      return subject.ignoringFieldAbsenceForValues();
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderForValues() {
      return subject.ignoringRepeatedFieldOrderForValues();
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> withPartialScopeForValues(FieldScope fieldScope) {
      return subject.withPartialScopeForValues(fieldScope);
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
        int firstFieldNumber, int... rest) {
      return subject.ignoringFieldsForValues(firstFieldNumber, rest);
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> ignoringFieldsForValues(
        Iterable<Integer> fieldNumbers) {
      return subject.ignoringFieldsForValues(fieldNumbers);
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
        FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
      return subject.ignoringFieldDescriptorsForValues(firstFieldDescriptor, rest);
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> ignoringFieldDescriptorsForValues(
        Iterable<FieldDescriptor> fieldDescriptors) {
      return subject.ignoringFieldDescriptorsForValues(fieldDescriptors);
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> ignoringFieldScopeForValues(FieldScope fieldScope) {
      return subject.ignoringFieldScopeForValues(fieldScope);
    }

    @Override
    public MapWithProtoValuesFluentAssertion<M> reportingMismatchesOnlyForValues() {
      return subject.reportingMismatchesOnlyForValues();
    }

    @Override
    public void containsEntry(@Nullable Object expectedKey, @Nullable M expectedValue) {
      usingCorrespondence().containsEntry(expectedKey, expectedValue);
    }

    @Override
    public void doesNotContainEntry(@Nullable Object excludedKey, @Nullable M excludedValue) {
      usingCorrespondence().doesNotContainEntry(excludedKey, excludedValue);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactly(@Nullable Object k0, @Nullable M v0, Object... rest) {
      return usingCorrespondence().containsExactly(k0, v0, rest);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactlyEntriesIn(Map<?, ? extends M> expectedMap) {
      return usingCorrespondence().containsExactlyEntriesIn(expectedMap);
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

    private final MapSubject.UsingCorrespondence<M, M> usingCorrespondence() {
      return subject.usingCorrespondence();
    }
  }
}
