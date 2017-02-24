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

import static com.google.common.truth.extensions.proto.FieldScopeUtil.join;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.truth.Correspondence;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import javax.annotation.Nullable;

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
          .setReportMismatchesOnly(false)
          .setFieldScopeLogic(FieldScopeLogic.all())
          .setUsingCorrespondenceStringFunction(Functions.constant(""))
          .build();

  static FluentEqualityConfig defaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private final LoadingCache<Descriptor, MessageDifferencer> messageDifferencers =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<Descriptor, MessageDifferencer>() {
                @Override
                public MessageDifferencer load(Descriptor descriptor) {
                  return makeMessageDifferencer(descriptor);
                }
              });

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Storage of AbstractProtoFluentEquals configuration data.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  abstract boolean ignoreFieldAbsence();

  abstract boolean ignoreRepeatedFieldOrder();

  abstract boolean reportMismatchesOnly();

  abstract FieldScopeLogic fieldScopeLogic();

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

  final FluentEqualityConfig reportingMismatchesOnly() {
    return toBuilder()
        .setReportMismatchesOnly(true)
        .addUsingCorrespondenceString(".reportingMismatchesOnly()")
        .build();
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

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Converters into comparison utilities.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private MessageDifferencer makeMessageDifferencer(Descriptor descriptor) {
    return MessageDifferencer.newBuilder()
        .setMessageFieldComparison(
            ignoreFieldAbsence()
                ? MessageDifferencer.MessageFieldComparison.EQUIVALENT
                : MessageDifferencer.MessageFieldComparison.EQUAL)
        .setRepeatedFieldComparison(
            ignoreRepeatedFieldOrder()
                ? MessageDifferencer.RepeatedFieldComparison.AS_SET
                : MessageDifferencer.RepeatedFieldComparison.AS_LIST)
        .setReportMatches(!reportMismatchesOnly())
        .addIgnoreCriteria(fieldScopeLogic().toIgnoreCriteria(descriptor))
        .build();
  }

  final MessageDifferencer toMessageDifferencer(Descriptor descriptor) {
    return messageDifferencers.getUnchecked(descriptor);
  }

  final <M extends Message> Correspondence<M, M> toCorrespondence(
      final Optional<Descriptor> optDescriptor) {
    return new Correspondence<M, M>() {
      @Override
      public final boolean compare(@Nullable M actual, @Nullable M expected) {
        return ProtoTruth.assertThat(actual)
            .usingConfig(FluentEqualityConfig.this)
            .testIsEqualTo(expected);
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

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setIgnoreFieldAbsence(boolean ignoringFieldAbsence);

    abstract Builder setIgnoreRepeatedFieldOrder(boolean ignoringRepeatedFieldOrder);

    abstract Builder setReportMismatchesOnly(boolean reportMismatchesOnly);

    abstract Builder setFieldScopeLogic(FieldScopeLogic fieldScopeLogic);

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
