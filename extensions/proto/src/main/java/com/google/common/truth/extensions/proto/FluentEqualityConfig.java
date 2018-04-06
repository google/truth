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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.extensions.proto.FieldScopeUtil.join;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Correspondence;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.errorprone.annotations.CheckReturnValue;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * A specification for a {@link MessageDifferencer} for comparing two individual protobufs.
 *
 * <p>Can be used to compare lists, maps, and multimaps of protos as well by conversion to a {@link
 * Correspondence}.
 */
@AutoValue
abstract class FluentEqualityConfig {

  private static final FluentEqualityConfig DEFAULT_INSTANCE =
      new AutoValue_FluentEqualityConfig.Builder()
          .setIgnoreFieldAbsence(false)
          .setIgnoreRepeatedFieldOrder(false)
          .setIgnoreExtraRepeatedFieldElements(false)
          .setCompareExpectedFieldsOnly(false)
          .setFieldScopeLogic(FieldScopeLogic.all())
          .setReportMismatchesOnly(false)
          .setUsingCorrespondenceStringFunction(Functions.constant(""))
          .build();

  static FluentEqualityConfig defaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private final LoadingCache<Descriptor, ProtoTruthMessageDifferencer> messageDifferencers =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<Descriptor, ProtoTruthMessageDifferencer>() {
                @Override
                public ProtoTruthMessageDifferencer load(Descriptor descriptor) {
                  return ProtoTruthMessageDifferencer.create(FluentEqualityConfig.this, descriptor);
                }
              });

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Storage of AbstractProtoFluentEquals configuration data.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  abstract boolean ignoreFieldAbsence();

  abstract boolean ignoreRepeatedFieldOrder();

  abstract boolean ignoreExtraRepeatedFieldElements();

  abstract Optional<Correspondence<Number, Number>> doubleCorrespondence();

  abstract Optional<Correspondence<Number, Number>> floatCorrespondence();

  abstract boolean compareExpectedFieldsOnly();

  // The full list of non-null Messages in the 'expected' part of the assertion.  When set, the
  // FieldScopeLogic should be narrowed appropriately if 'compareExpectedFieldsOnly()' is true.
  //
  // This field will be absent while the assertion is being composed, but *must* be set before
  // passed to a message differencer.  We check this to ensure no assertion path forgets to pass
  // along the expected protos.
  abstract Optional<ImmutableList<Message>> expectedMessages();

  abstract FieldScopeLogic fieldScopeLogic();

  abstract boolean reportMismatchesOnly();

  // For pretty-printing, does not affect behavior.
  abstract Function<? super Optional<Descriptor>, String> usingCorrespondenceStringFunction();

  final String usingCorrespondenceString(Optional<Descriptor> descriptor) {
    return usingCorrespondenceStringFunction().apply(descriptor);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Mutators of FluentEqualityConfig configuration data.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  final FluentEqualityConfig ignoringFieldAbsence() {
    return toBuilder()
        .setIgnoreFieldAbsence(true)
        .addUsingCorrespondenceString(".ignoringFieldAbsence()")
        .build();
  }

  final FluentEqualityConfig ignoringRepeatedFieldOrder() {
    return toBuilder()
        .setIgnoreRepeatedFieldOrder(true)
        .addUsingCorrespondenceString(".ignoringRepeatedFieldOrder()")
        .build();
  }

  final FluentEqualityConfig ignoringExtraRepeatedFieldElements() {
    return toBuilder()
        .setIgnoreExtraRepeatedFieldElements(true)
        .addUsingCorrespondenceString(".ignoringExtraRepeatedFieldElements()")
        .build();
  }

  final FluentEqualityConfig usingDoubleTolerance(double tolerance) {
    return toBuilder()
        .setDoubleCorrespondence(Correspondence.tolerance(tolerance))
        .addUsingCorrespondenceString(".usingDoubleTolerance(" + tolerance + ")")
        .build();
  }

  final FluentEqualityConfig usingFloatTolerance(float tolerance) {
    return toBuilder()
        .setFloatCorrespondence(Correspondence.tolerance(tolerance))
        .addUsingCorrespondenceString(".usingFloatTolerance(" + tolerance + ")")
        .build();
  }

  final FluentEqualityConfig comparingExpectedFieldsOnly() {
    return toBuilder()
        .setCompareExpectedFieldsOnly(true)
        .addUsingCorrespondenceString(".comparingExpectedFieldsOnly()")
        .build();
  }

  final FluentEqualityConfig withExpectedMessages(Iterable<? extends Message> messages) {
    ImmutableList.Builder<Message> listBuilder = ImmutableList.builder();
    for (Message message : messages) {
      if (message != null) {
        listBuilder.add(message);
      }
    }
    Builder builder = toBuilder().setExpectedMessages(listBuilder.build());
    if (compareExpectedFieldsOnly()) {
      builder.setFieldScopeLogic(
          FieldScopeLogic.and(fieldScopeLogic(), FieldScopes.fromSetFields(messages).logic()));
    }
    return builder.build();
  }

  final FluentEqualityConfig withPartialScope(FieldScope partialScope) {
    return toBuilder()
        .setFieldScopeLogic(FieldScopeLogic.and(fieldScopeLogic(), partialScope.logic()))
        .addUsingCorrespondenceFieldScopeString(".withPartialScope(%s)", partialScope)
        .build();
  }

  final FluentEqualityConfig ignoringFields(Iterable<Integer> fieldNumbers) {
    return toBuilder()
        .setFieldScopeLogic(fieldScopeLogic().ignoringFields(fieldNumbers))
        .addUsingCorrespondenceFieldNumbersString(".ignoringFields(%s)", fieldNumbers)
        .build();
  }

  final FluentEqualityConfig ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    return toBuilder()
        .setFieldScopeLogic(fieldScopeLogic().ignoringFieldDescriptors(fieldDescriptors))
        .addUsingCorrespondenceFieldDescriptorsString(
            ".ignoringFieldDescriptors(%s)", fieldDescriptors)
        .build();
  }

  final FluentEqualityConfig ignoringFieldScope(FieldScope fieldScope) {
    return toBuilder()
        .setFieldScopeLogic(
            FieldScopeLogic.and(fieldScopeLogic(), FieldScopeLogic.not(fieldScope.logic())))
        .addUsingCorrespondenceFieldScopeString(".ignoringFieldScope(%s)", fieldScope)
        .build();
  }

  final FluentEqualityConfig reportingMismatchesOnly() {
    return toBuilder()
        .setReportMismatchesOnly(true)
        .addUsingCorrespondenceString(".reportingMismatchesOnly()")
        .build();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Converters into comparison utilities.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  final ProtoTruthMessageDifferencer toMessageDifferencer(Descriptor descriptor) {
    checkState(expectedMessages().isPresent(), "expectedMessages() not set");
    return messageDifferencers.getUnchecked(descriptor);
  }

  final <M extends Message> Correspondence<M, M> toCorrespondence(
      final Optional<Descriptor> optDescriptor) {
    checkState(expectedMessages().isPresent(), "expectedMessages() not set");
    return new Correspondence<M, M>() {
      @Override
      public final boolean compare(@NullableDecl M actual, @NullableDecl M expected) {
        return ProtoTruth.assertThat(actual)
            .usingConfig(FluentEqualityConfig.this)
            .testIsEqualTo(expected);
      }

      @Override
      public final String formatDiff(@NullableDecl M actual, @NullableDecl M expected) {
        if (actual == null || expected == null) {
          return "";
        }

        return FluentEqualityConfig.this
            .toMessageDifferencer(actual.getDescriptorForType())
            .diffMessages(actual, expected)
            .printToString(FluentEqualityConfig.this.reportMismatchesOnly());
      }

      @Override
      public final String toString() {
        return "is equivalent according to assertThat(proto)"
            + usingCorrespondenceString(optDescriptor)
            + ".isEqualTo(target) to";
      }
    };
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Builder methods.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  abstract Builder toBuilder();

  @CanIgnoreReturnValue
  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setIgnoreFieldAbsence(boolean ignoringFieldAbsence);

    abstract Builder setIgnoreRepeatedFieldOrder(boolean ignoringRepeatedFieldOrder);

    abstract Builder setIgnoreExtraRepeatedFieldElements(boolean ignoreExtraRepeatedFieldElements);

    abstract Builder setDoubleCorrespondence(Correspondence<Number, Number> doubleCorrespondence);

    abstract Builder setFloatCorrespondence(Correspondence<Number, Number> floatCorrespondence);

    abstract Builder setCompareExpectedFieldsOnly(boolean compare);

    abstract Builder setExpectedMessages(ImmutableList<Message> messages);

    abstract Builder setFieldScopeLogic(FieldScopeLogic fieldScopeLogic);

    abstract Builder setReportMismatchesOnly(boolean reportMismatchesOnly);

    @CheckReturnValue
    abstract Function<? super Optional<Descriptor>, String> usingCorrespondenceStringFunction();

    abstract Builder setUsingCorrespondenceStringFunction(
        Function<? super Optional<Descriptor>, String> usingCorrespondenceStringFunction);

    abstract FluentEqualityConfig build();

    // Lazy formatting methods.
    // These allow us to print raw integer field numbers with meaningful names.

    final Builder addUsingCorrespondenceString(String string) {
      return setUsingCorrespondenceStringFunction(
          FieldScopeUtil.concat(usingCorrespondenceStringFunction(), Functions.constant(string)));
    }

    final Builder addUsingCorrespondenceFieldNumbersString(
        String fmt, Iterable<Integer> fieldNumbers) {
      return setUsingCorrespondenceStringFunction(
          FieldScopeUtil.concat(
              usingCorrespondenceStringFunction(),
              FieldScopeUtil.fieldNumbersFunction(fmt, fieldNumbers)));
    }

    final Builder addUsingCorrespondenceFieldDescriptorsString(
        String fmt, Iterable<FieldDescriptor> fieldDescriptors) {
      return setUsingCorrespondenceStringFunction(
          FieldScopeUtil.concat(
              usingCorrespondenceStringFunction(),
              Functions.constant(String.format(fmt, join(fieldDescriptors)))));
    }

    final Builder addUsingCorrespondenceFieldScopeString(String fmt, FieldScope fieldScope) {
      return setUsingCorrespondenceStringFunction(
          FieldScopeUtil.concat(
              usingCorrespondenceStringFunction(),
              FieldScopeUtil.fieldScopeFunction(fmt, fieldScope)));
    }
  }
}
