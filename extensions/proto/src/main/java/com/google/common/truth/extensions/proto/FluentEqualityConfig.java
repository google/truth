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

import com.google.auto.value.AutoValue;
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
          .setFieldScope(FieldScopes.all())
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

  // Storage of AbstractProtoFluentEquals configuration data.

  abstract boolean ignoreFieldAbsence();

  abstract boolean ignoreRepeatedFieldOrder();

  abstract boolean reportMismatchesOnly();

  abstract FieldScope fieldScope();

  // Mutators of AbstractProtoFluentEquals configuration data.

  final FluentEqualityConfig ignoringFieldAbsence() {
    return toBuilder().setIgnoreFieldAbsence(true).build();
  }

  final FluentEqualityConfig ignoringRepeatedFieldOrder() {
    return toBuilder().setIgnoreRepeatedFieldOrder(true).build();
  }

  final FluentEqualityConfig reportingMismatchesOnly() {
    return toBuilder().setReportMismatchesOnly(true).build();
  }

  final FluentEqualityConfig withPartialScope(FieldScope partialScope) {
    return toBuilder().setFieldScope(FieldScopeImpl.and(fieldScope(), partialScope)).build();
  }

  final FluentEqualityConfig ignoringFields(int... fieldNumbers) {
    return toBuilder().setFieldScope(fieldScope().ignoringFields(fieldNumbers)).build();
  }

  final FluentEqualityConfig ignoringFieldDescriptors(FieldDescriptor... fieldDescriptors) {
    return toBuilder()
        .setFieldScope(fieldScope().ignoringFieldDescriptors(fieldDescriptors))
        .build();
  }

  final FluentEqualityConfig ignoringFieldScope(FieldScope fieldScope) {
    return toBuilder()
        .setFieldScope(FieldScopeImpl.and(fieldScope(), FieldScopeImpl.not(fieldScope)))
        .build();
  }

  // Converters into comparison utilities.

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
        .addIgnoreCriteria(fieldScope().toIgnoreCriteria(descriptor))
        .build();
  }

  final MessageDifferencer toMessageDifferencer(Descriptor descriptor) {
    return messageDifferencers.getUnchecked(descriptor);
  }

  final Correspondence<Message, Message> toCorrespondence() {
    return new Correspondence<Message, Message>() {
      @Override
      public final boolean compare(@Nullable Message actual, @Nullable Message expected) {
        return ProtoTruth.assertThat(actual)
            .usingConfig(FluentEqualityConfig.this)
            .testIsEqualTo(expected);
      }

      @Override
      public final String toString() {
        // TODO(user): Provide good error messaging.
        throw new UnsupportedOperationException("Not ready yet.");
      }
    };
  }

  // Builder methods.

  abstract Builder toBuilder();

  @AutoValue.Builder
  interface Builder {
    Builder setIgnoreFieldAbsence(boolean ignoringFieldAbsence);

    Builder setIgnoreRepeatedFieldOrder(boolean ignoringRepeatedFieldOrder);

    Builder setReportMismatchesOnly(boolean reportMismatchesOnly);

    Builder setFieldScope(FieldScope fieldScope);

    FluentEqualityConfig build();
  }
}
