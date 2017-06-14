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

import com.google.common.collect.Multimap;
import com.google.common.truth.Correspondence;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.MultimapSubject;
import com.google.common.truth.Ordered;
import com.google.common.truth.Subject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import javax.annotation.Nullable;

/**
 * Truth subject for {@link Multimap}s with protocol buffers for values.
 *
 * <p>{@code ProtoTruth.assertThat(actual).containsExactlyEntriesIn(expected)} performs the same
 * assertion as {@code Truth.assertThat(actual).containsExactlyEntriesIn(expected)}. By default, the
 * assertions are strict with respect to repeated field order, missing fields, etc. This behavior
 * can be changed with the configuration methods on this subject, e.g. {@code
 * ProtoTruth.assertThat(actual).ignoringRepeatedFieldOrder().containsExactlyEntriesIn(expected)}.
 *
 * <p>Equality tests, and other methods, may yield slightly different behavior for versions 2 and 3
 * of Protocol Buffers. If testing protos of multiple versions, make sure you understand the
 * behaviors of default and unknown fields so you don't under or over test.
 */
public class MultimapWithProtoValuesSubject<
        S extends MultimapWithProtoValuesSubject<S, K, M, C>,
        K,
        M extends Message,
        C extends Multimap<K, M>>
    extends Subject<S, C> {

  private final FluentEqualityConfig config;

  /** Default implementation of {@link MultimapWithProtoValuesSubject}. */
  public static class MultimapWithMessageValuesSubject<K, M extends Message>
      extends MultimapWithProtoValuesSubject<
          MultimapWithMessageValuesSubject<K, M>, K, M, Multimap<K, M>> {
    // See IterableOfProtosSubject.IterableOfMessagesSubject for why this class is exposed.

    MultimapWithMessageValuesSubject(
        FailureStrategy failureStrategy, @Nullable Multimap<K, M> multimap) {
      super(failureStrategy, multimap);
    }

    private MultimapWithMessageValuesSubject(
        FailureStrategy failureStrategy,
        FluentEqualityConfig config,
        @Nullable Multimap<K, M> multimap) {
      super(failureStrategy, config, multimap);
    }
  }

  protected MultimapWithProtoValuesSubject(FailureStrategy failureStrategy, @Nullable C multimap) {
    this(failureStrategy, FluentEqualityConfig.defaultInstance(), multimap);
  }

  MultimapWithProtoValuesSubject(
      FailureStrategy failureStrategy, FluentEqualityConfig config, @Nullable C multimap) {
    super(failureStrategy, multimap);
    this.config = config;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // MultimapSubject methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private final MultimapSubject delegate() {
    MultimapSubject delegate = check().that(actual());
    if (internalCustomName() != null) {
      delegate = delegate.named(internalCustomName());
    }
    return delegate;
  }

  /** Fails if the multimap is not empty. */
  public void isEmpty() {
    delegate().isEmpty();
  }

  /** Fails if the multimap is empty. */
  public void isNotEmpty() {
    delegate().isNotEmpty();
  }

  /** Fails if the multimap does not have the given size. */
  public void hasSize(int expectedSize) {
    delegate().hasSize(expectedSize);
  }

  /** Fails if the multimap does not contain the given key. */
  public void containsKey(@Nullable Object key) {
    delegate().containsKey(key);
  }

  /** Fails if the multimap contains the given key. */
  public void doesNotContainKey(@Nullable Object key) {
    delegate().doesNotContainKey(key);
  }

  /** Fails if the multimap does not contain the given entry. */
  public void containsEntry(@Nullable Object key, @Nullable Object value) {
    delegate().containsEntry(key, value);
  }

  /** Fails if the multimap contains the given entry. */
  public void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    delegate().doesNotContainEntry(key, value);
  }

  private static class IterableValuesForKey<M extends Message>
      extends IterableOfProtosSubject<IterableValuesForKey<M>, M, Iterable<M>> {
    @Nullable private final Object key;
    private final String stringRepresentation;

    @SuppressWarnings({"unchecked"})
    IterableValuesForKey(
        FailureStrategy failureStrategy,
        MultimapWithProtoValuesSubject<?, ?, M, ?> multimapSubject,
        @Nullable Object key) {
      super(failureStrategy, ((Multimap<Object, M>) multimapSubject.actual()).get(key));
      this.key = key;
      this.stringRepresentation = multimapSubject.actualAsString();
    }

    @Override
    protected String actualCustomStringRepresentation() {
      return "Values for key <" + key + "> (<" + actual() + ">) in " + stringRepresentation;
    }
  }

  /**
   * Returns a context-aware {@link Subject} for making assertions about the values for the given
   * key within the {@link Multimap}.
   *
   * <p>This method performs no checks on its own and cannot cause test failures. Subsequent
   * assertions must be chained onto this method call to test properties of the {@link Multimap}.
   */
  public IterableOfProtosSubject<?, M, Iterable<M>> valuesForKey(@Nullable Object key) {
    return new IterableValuesForKey<M>(failureStrategy, this, key);
  }

  /**
   * Fails if the {@link Multimap} does not contain precisely the same entries as the argument
   * {@link Multimap}.
   *
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify that
   * the two multimaps iterate fully in the same order. That is, their key sets iterate in the same
   * order, and the value collections for each key iterate in the same order.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactlyEntriesIn(Multimap<?, ?> expectedMultimap) {
    return delegate().containsExactlyEntriesIn(expectedMultimap);
  }

  /**
   * Fails if the multimap does not contain exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   */
  @CanIgnoreReturnValue
  public Ordered containsExactly(@Nullable Object k0, @Nullable Object v0, Object... rest) {
    return delegate().containsExactly(k0, v0, rest);
  }

  /**
   * Starts a method chain for a test proposition in which the actual values (i.e. the values of the
   * {@link Multimap} under test) are compared to expected values using the given {@link
   * Correspondence}. The actual values must be of type {@code A} and the expected values must be of
   * type {@code E}. The proposition is actually executed by continuing the method chain. For
   * example:
   *
   * <pre>{@code
   * assertThat(actualMultimap)
   *   .comparingValuesUsing(correspondence)
   *   .containsEntry(expectedKey, expectedValue);
   * }</pre>
   *
   * where {@code actualMultimap} is a {@code Multimap<?, A>} (or, more generally, a {@code
   * Multimap<?, ? extends A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and
   * {@code expectedValue} is an {@code E}.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual value that is not of type {@code A}.
   */
  public <A, E> MultimapSubject.UsingCorrespondence<A, E> comparingValuesUsing(
      Correspondence<A, E> correspondence) {
    return delegate().comparingValuesUsing(correspondence);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // MultimapWithProtoValuesFluentAssertion Configuration
  //////////////////////////////////////////////////////////////////////////////////////////////////

  MultimapWithProtoValuesFluentAssertion<M> usingConfig(FluentEqualityConfig newConfig) {
    MultimapWithMessageValuesSubject<K, M> newSubject =
        new MultimapWithMessageValuesSubject<K, M>(failureStrategy, newConfig, actual());
    if (internalCustomName() != null) {
      newSubject = newSubject.named(internalCustomName());
    }
    return new MultimapWithProtoValuesFluentAssertionImpl<M>(newSubject);
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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceForValues() {
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
  public MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderForValues() {
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

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // UsingCorrespondence Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private MultimapSubject.UsingCorrespondence<M, M> usingCorrespondence() {
    return comparingValuesUsing(
        config.<M>toCorrespondence(FieldScopeUtil.getSingleDescriptor(actual().values())));
  }

  // The UsingCorrespondence methods have conflicting erasure with default MapSubject methods,
  // so we can't implement them both on the same class, but we want to define both so
  // MultimapWithProtoValuesSubjects are interchangeable with MapSubjects when no configuration is
  // specified. So, we implement a dumb, private delegator to return instead.
  private static final class MultimapWithProtoValuesFluentAssertionImpl<M extends Message>
      implements MultimapWithProtoValuesFluentAssertion<M> {
    private final MultimapWithProtoValuesSubject<?, ?, M, ?> subject;

    MultimapWithProtoValuesFluentAssertionImpl(MultimapWithProtoValuesSubject<?, ?, M, ?> subject) {
      this.subject = subject;
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringFieldAbsenceForValues() {
      return subject.ignoringFieldAbsenceForValues();
    }

    @Override
    public MultimapWithProtoValuesFluentAssertion<M> ignoringRepeatedFieldOrderForValues() {
      return subject.ignoringRepeatedFieldOrderForValues();
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
    public void containsEntry(@Nullable Object expectedKey, @Nullable M expectedValue) {
      usingCorrespondence().containsEntry(expectedKey, expectedValue);
    }

    @Override
    public void doesNotContainEntry(@Nullable Object excludedKey, @Nullable M excludedValue) {
      usingCorrespondence().doesNotContainEntry(excludedKey, excludedValue);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactlyEntriesIn(Multimap<?, ? extends M> expectedMap) {
      return usingCorrespondence().containsExactlyEntriesIn(expectedMap);
    }

    @Override
    @CanIgnoreReturnValue
    public Ordered containsExactly(@Nullable Object k0, @Nullable M v0, Object... rest) {
      return usingCorrespondence().containsExactly(k0, v0, rest);
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

    private final MultimapSubject.UsingCorrespondence<M, M> usingCorrespondence() {
      return subject.usingCorrespondence();
    }
  }
}
