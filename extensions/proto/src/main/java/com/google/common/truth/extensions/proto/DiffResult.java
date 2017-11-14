/*
 * Copyright (c) 2017 Google, Inc.
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

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.truth.extensions.proto.RecursableDiffEntity.WithResultCode.Result;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.ForOverride;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import java.util.Set;

/**
 * Structural summary of the difference between two messages.
 *
 * <p>A {@code DiffResult} has singular fields, repeated fields, and unknowns, each with their own
 * class. The inner classes may also contain their own {@code DiffResult}s for submessages.
 *
 * <p>These classes form a recursive, hierarchical relationship. Much of the common recursion logic
 * across all the classes is in {@link RecursableDiffEntity}.
 */
@AutoValue
abstract class DiffResult extends RecursableDiffEntity.WithoutResultCode {
  /**
   * Structural summary of the difference between two singular (non-repeated) fields.
   *
   * <p>It is possible for the result to be {@code ADDED} or {@code REMOVED}, even if {@code
   * actual()} and {@code expected()} are identical. This occurs if the config does not have {@link
   * FluentEqualityConfig#ignoringFieldAbsence()} enabled, one message had the field explicitly set
   * to the default value, and the other did not.
   */
  @AutoValue
  abstract static class SingularField extends RecursableDiffEntity.WithResultCode {
    /** The human-readable name of the field compared. */
    abstract String fieldName();

    /** The field under test. */
    abstract Optional<Object> actual();

    /** The expected value of said field. */
    abstract Optional<Object> expected();

    /**
     * The detailed breakdown of the comparison, only present if both objects are set on this
     * instance and they are messages.
     *
     * <p>This does not necessarily mean the messages were set on the input protos.
     */
    abstract Optional<DiffResult> breakdown();

    /**
     * The detailed breakdown of the comparison, only present if both objects are set and they are
     * {@link UnknownFieldSet}s.
     *
     * <p>This will only ever be set inside a parent {@link UnknownFieldSetDiff}. The top {@link
     * UnknownFieldSetDiff} is set on the {@link DiffResult}, not here.
     */
    abstract Optional<UnknownFieldSetDiff> unknownsBreakdown();

    @Memoized
    @Override
    Iterable<? extends RecursableDiffEntity> childEntities() {
      return ImmutableList.copyOf(
          Iterables.concat(breakdown().asSet(), unknownsBreakdown().asSet()));
    }

    static SingularField ignored(String fieldName) {
      return newBuilder().setFieldName(fieldName).setResult(Result.IGNORED).build();
    }

    static Builder newBuilder() {
      return new AutoValue_DiffResult_SingularField.Builder();
    }

    /** Builder for {@link SingularField}. */
    @CanIgnoreReturnValue
    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder setResult(Result result);

      abstract Builder setFieldName(String fieldName);

      abstract Builder setActual(Object actual);

      abstract Builder setExpected(Object expected);

      abstract Builder setBreakdown(DiffResult breakdown);

      abstract Builder setUnknownsBreakdown(UnknownFieldSetDiff unknownsBreakdown);

      abstract SingularField build();
    }
  }

  /**
   * Structural summary of the difference between two repeated fields.
   *
   * <p>This is only present if the user specified {@code ignoringRepeatedFieldOrder()}. Otherwise,
   * the repeated elements are compared as singular fields, and there are no 'move' semantics.
   */
  @AutoValue
  abstract static class RepeatedField extends RecursableDiffEntity.WithoutResultCode {

    /**
     * Structural summary of the difference between two elements in two corresponding repeated
     * fields, in the context of a {@link RepeatedField} diff.
     *
     * <p>The field indexes will only be present if the corresponding object is also present. If an
     * object is absent, the PairResult represents an extra/missing element in the repeated field.
     * If both are present but the indexes differ, it represents a 'move'.
     */
    @AutoValue
    abstract static class PairResult extends RecursableDiffEntity.WithResultCode {
      /** The index of the element in the {@code actual()} list that was matched. */
      abstract Optional<Integer> actualFieldIndex();

      /** The index of the element in the {@code expected()} list that was matched. */
      abstract Optional<Integer> expectedFieldIndex();

      /** The element in the {@code actual()} list that was matched. */
      abstract Optional<Object> actual();

      /** The element in the {@code expected()} list that was matched. */
      abstract Optional<Object> expected();

      /**
       * A detailed breakdown of the comparison between the messages. Present iff {@code actual()}
       * and {@code expected()} are {@link Message}s.
       */
      abstract Optional<DiffResult> breakdown();

      @Memoized
      @Override
      Iterable<? extends RecursableDiffEntity> childEntities() {
        return breakdown().asSet();
      }

      static Builder newBuilder() {
        return new AutoValue_DiffResult_RepeatedField_PairResult.Builder();
      }

      @CanIgnoreReturnValue
      @AutoValue.Builder
      abstract static class Builder {
        abstract Builder setResult(Result result);

        abstract Builder setActualFieldIndex(int actualFieldIndex);

        abstract Builder setExpectedFieldIndex(int expectedFieldIndex);

        abstract Builder setActual(Object actual);

        abstract Builder setExpected(Object expected);

        abstract Builder setBreakdown(DiffResult breakdown);

        abstract PairResult build();
      }
    }

    /** The human-readable name of the field compared. */
    abstract String fieldName();

    /** The elements under test. */
    abstract ImmutableList<Object> actual();

    /** The elements expected. */
    abstract ImmutableList<Object> expected();

    // TODO(user,peteg): Also provide a minimum-edit-distance pairing between unmatched elements,
    // and the diff report between them.

    /** Pairs of elements which were diffed against each other. */
    abstract ImmutableList<PairResult> pairResults();

    @Memoized
    @Override
    Iterable<? extends RecursableDiffEntity> childEntities() {
      return pairResults();
    }

    static Builder newBuilder() {
      return new AutoValue_DiffResult_RepeatedField.Builder();
    }

    @CanIgnoreReturnValue
    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder setFieldName(String fieldName);

      abstract Builder setActual(Iterable<?> actual);

      abstract Builder setExpected(Iterable<?> expected);

      @ForOverride
      abstract ImmutableList.Builder<PairResult> pairResultsBuilder();

      final Builder addPairResult(PairResult pairResult) {
        pairResultsBuilder().add(pairResult);
        return this;
      }

      abstract RepeatedField build();
    }
  }

  /** Structural summary of the difference between two unknown field sets. */
  @AutoValue
  abstract static class UnknownFieldSetDiff extends RecursableDiffEntity.WithoutResultCode {
    /** The {@link UnknownFieldSet} being tested. */
    abstract Optional<UnknownFieldSet> actual();

    /** The {@link UnknownFieldSet} expected. */
    abstract Optional<UnknownFieldSet> expected();

    /**
     * A list of top-level singular field comparison results.
     *
     * <p>All unknown fields are treated as repeated and with {@code ignoringRepeatedFieldOrder()}
     * off, because we don't know their nature. If they're optional, only last element matters, but
     * if they're repeated, all elements matter.
     */
    abstract ImmutableListMultimap<Integer, SingularField> singularFields();

    @Memoized
    @Override
    Iterable<? extends RecursableDiffEntity> childEntities() {
      return singularFields().values();
    }

    static Builder newBuilder() {
      return new AutoValue_DiffResult_UnknownFieldSetDiff.Builder();
    }

    @CanIgnoreReturnValue
    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder setActual(UnknownFieldSet actual);

      abstract Builder setExpected(UnknownFieldSet expected);

      @ForOverride
      abstract ImmutableListMultimap.Builder<Integer, SingularField> singularFieldsBuilder();

      final Builder addSingularField(int fieldNumber, SingularField singularField) {
        singularFieldsBuilder().put(fieldNumber, singularField);
        return this;
      }

      final Builder addAllSingularFields(int fieldNumber, Iterable<SingularField> singularFields) {
        singularFieldsBuilder().putAll(fieldNumber, singularFields);
        return this;
      }

      abstract UnknownFieldSetDiff build();
    }
  }

  /** The {@link Message} being tested. */
  abstract Message actual();

  /** The {@link Message} expected. */
  abstract Message expected();

  /** A list of top-level singular field comparison results grouped by field number. */
  abstract ImmutableListMultimap<Integer, SingularField> singularFields();

  /**
   * A list of top-level repeated field comparison results grouped by field number.
   *
   * <p>This is only populated if {@link FluentEqualityConfig#ignoreRepeatedFieldOrder()} is set.
   * Otherwise, repeated fields are compared strictly in index order, as singular fields.
   */
  abstract ImmutableListMultimap<Integer, RepeatedField> repeatedFields();

  /**
   * The result of comparing the message's {@link UnknownFieldSet}s. Not present if unknown fields
   * were not compared.
   */
  abstract Optional<UnknownFieldSetDiff> unknownFields();

  @Memoized
  @Override
  Iterable<? extends RecursableDiffEntity> childEntities() {
    // Assemble the diffs in field number order so it most closely matches the schema.
    ImmutableList.Builder<RecursableDiffEntity> builder =
        ImmutableList.builderWithExpectedSize(
            singularFields().size() + repeatedFields().size() + unknownFields().asSet().size());
    Set<Integer> fieldNumbers = Sets.union(singularFields().keySet(), repeatedFields().keySet());
    for (int fieldNumber : Ordering.natural().sortedCopy(fieldNumbers)) {
      builder.addAll(singularFields().get(fieldNumber));
      builder.addAll(repeatedFields().get(fieldNumber));
    }
    builder.addAll(unknownFields().asSet());
    return builder.build();
  }

  static Builder newBuilder() {
    return new AutoValue_DiffResult.Builder();
  }

  @CanIgnoreReturnValue
  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setActual(Message actual);

    abstract Builder setExpected(Message expected);

    @ForOverride
    abstract ImmutableListMultimap.Builder<Integer, SingularField> singularFieldsBuilder();

    final Builder addSingularField(int fieldNumber, SingularField singularField) {
      singularFieldsBuilder().put(fieldNumber, singularField);
      return this;
    }

    final Builder addAllSingularFields(int fieldNumber, Iterable<SingularField> singularFields) {
      singularFieldsBuilder().putAll(fieldNumber, singularFields);
      return this;
    }

    @ForOverride
    abstract ImmutableListMultimap.Builder<Integer, RepeatedField> repeatedFieldsBuilder();

    final Builder addRepeatedField(int fieldNumber, RepeatedField repeatedField) {
      repeatedFieldsBuilder().put(fieldNumber, repeatedField);
      return this;
    }

    abstract Builder setUnknownFields(UnknownFieldSetDiff unknownFields);

    abstract DiffResult build();
  }
}
