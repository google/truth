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
import com.google.common.base.Verify;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Correspondence;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * A specification for a {@link ProtoTruthMessageDifferencer} for comparing two individual
 * protobufs.
 *
 * <p>Can be used to compare lists, maps, and multimaps of protos as well by conversion to a {@link
 * Correspondence}.
 */
@AutoValue
abstract class FluentEqualityConfig implements FieldScopeLogicContainer<FluentEqualityConfig> {

  private static final FluentEqualityConfig DEFAULT_INSTANCE =
      new AutoValue_FluentEqualityConfig.Builder()
          .setIgnoreFieldAbsenceScope(FieldScopeLogic.none())
          .setIgnoreRepeatedFieldOrderScope(FieldScopeLogic.none())
          .setIgnoreExtraRepeatedFieldElementsScope(FieldScopeLogic.none())
          .setDoubleCorrespondenceMap(FieldScopeLogicMap.<Correspondence<Number, Number>>empty())
          .setFloatCorrespondenceMap(FieldScopeLogicMap.<Correspondence<Number, Number>>empty())
          .setCompareExpectedFieldsOnly(false)
          .setCompareFieldsScope(FieldScopeLogic.all())
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

  abstract FieldScopeLogic ignoreFieldAbsenceScope();

  abstract FieldScopeLogic ignoreRepeatedFieldOrderScope();

  abstract FieldScopeLogic ignoreExtraRepeatedFieldElementsScope();

  abstract FieldScopeLogicMap<Correspondence<Number, Number>> doubleCorrespondenceMap();

  abstract FieldScopeLogicMap<Correspondence<Number, Number>> floatCorrespondenceMap();

  abstract boolean compareExpectedFieldsOnly();

  // The full list of non-null Messages in the 'expected' part of the assertion.  When set, the
  // FieldScopeLogic should be narrowed appropriately if 'compareExpectedFieldsOnly()' is true.
  //
  // This field will be absent while the assertion is being composed, but *must* be set before
  // passed to a message differencer.  We check this to ensure no assertion path forgets to pass
  // along the expected protos.
  abstract Optional<ImmutableList<Message>> expectedMessages();

  abstract FieldScopeLogic compareFieldsScope();

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
        .setIgnoreFieldAbsenceScope(FieldScopeLogic.all())
        .addUsingCorrespondenceString(".ignoringFieldAbsence()")
        .build();
  }

  final FluentEqualityConfig ignoringFieldAbsenceOfFields(Iterable<Integer> fieldNumbers) {
    return toBuilder()
        .setIgnoreFieldAbsenceScope(
            ignoreFieldAbsenceScope().allowingFieldsNonRecursive(fieldNumbers))
        .addUsingCorrespondenceFieldNumbersString(".ignoringFieldAbsenceOf(%s)", fieldNumbers)
        .build();
  }

  final FluentEqualityConfig ignoringFieldAbsenceOfFieldDescriptors(
      Iterable<FieldDescriptor> fieldDescriptors) {
    return toBuilder()
        .setIgnoreFieldAbsenceScope(
            ignoreFieldAbsenceScope().allowingFieldDescriptorsNonRecursive(fieldDescriptors))
        .addUsingCorrespondenceFieldDescriptorsString(
            ".ignoringFieldAbsenceOf(%s)", fieldDescriptors)
        .build();
  }

  final FluentEqualityConfig ignoringRepeatedFieldOrder() {
    return toBuilder()
        .setIgnoreRepeatedFieldOrderScope(FieldScopeLogic.all())
        .addUsingCorrespondenceString(".ignoringRepeatedFieldOrder()")
        .build();
  }

  final FluentEqualityConfig ignoringRepeatedFieldOrderOfFields(Iterable<Integer> fieldNumbers) {
    return toBuilder()
        .setIgnoreRepeatedFieldOrderScope(
            ignoreRepeatedFieldOrderScope().allowingFieldsNonRecursive(fieldNumbers))
        .addUsingCorrespondenceFieldNumbersString(".ignoringRepeatedFieldOrderOf(%s)", fieldNumbers)
        .build();
  }

  final FluentEqualityConfig ignoringRepeatedFieldOrderOfFieldDescriptors(
      Iterable<FieldDescriptor> fieldDescriptors) {
    return toBuilder()
        .setIgnoreRepeatedFieldOrderScope(
            ignoreRepeatedFieldOrderScope().allowingFieldDescriptorsNonRecursive(fieldDescriptors))
        .addUsingCorrespondenceFieldDescriptorsString(
            ".ignoringRepeatedFieldOrderOf(%s)", fieldDescriptors)
        .build();
  }

  final FluentEqualityConfig ignoringExtraRepeatedFieldElements() {
    return toBuilder()
        .setIgnoreExtraRepeatedFieldElementsScope(FieldScopeLogic.all())
        .addUsingCorrespondenceString(".ignoringExtraRepeatedFieldElements()")
        .build();
  }

  final FluentEqualityConfig ignoringExtraRepeatedFieldElementsOfFields(
      Iterable<Integer> fieldNumbers) {
    return toBuilder()
        .setIgnoreExtraRepeatedFieldElementsScope(
            ignoreExtraRepeatedFieldElementsScope().allowingFieldsNonRecursive(fieldNumbers))
        .addUsingCorrespondenceFieldNumbersString(
            ".ignoringExtraRepeatedFieldElements(%s)", fieldNumbers)
        .build();
  }

  final FluentEqualityConfig ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
      Iterable<FieldDescriptor> fieldDescriptors) {
    return toBuilder()
        .setIgnoreExtraRepeatedFieldElementsScope(
            ignoreExtraRepeatedFieldElementsScope()
                .allowingFieldDescriptorsNonRecursive(fieldDescriptors))
        .addUsingCorrespondenceFieldDescriptorsString(
            ".ignoringExtraRepeatedFieldElements(%s)", fieldDescriptors)
        .build();
  }

  final FluentEqualityConfig usingDoubleTolerance(double tolerance) {
    return toBuilder()
        .setDoubleCorrespondenceMap(
            FieldScopeLogicMap.defaultValue(Correspondence.tolerance(tolerance)))
        .addUsingCorrespondenceString(".usingDoubleTolerance(" + tolerance + ")")
        .build();
  }

  final FluentEqualityConfig usingDoubleToleranceForFields(
      double tolerance, Iterable<Integer> fieldNumbers) {
    return toBuilder()
        .setDoubleCorrespondenceMap(
            doubleCorrespondenceMap()
                .with(
                    FieldScopeLogic.none().allowingFieldsNonRecursive(fieldNumbers),
                    Correspondence.tolerance(tolerance)))
        .addUsingCorrespondenceFieldNumbersString(
            ".usingDoubleTolerance(" + tolerance + ", %s)", fieldNumbers)
        .build();
  }

  final FluentEqualityConfig usingDoubleToleranceForFieldDescriptors(
      double tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
    return toBuilder()
        .setDoubleCorrespondenceMap(
            doubleCorrespondenceMap()
                .with(
                    FieldScopeLogic.none().allowingFieldDescriptorsNonRecursive(fieldDescriptors),
                    Correspondence.tolerance(tolerance)))
        .addUsingCorrespondenceFieldDescriptorsString(
            ".usingDoubleTolerance(" + tolerance + ", %s)", fieldDescriptors)
        .build();
  }

  final FluentEqualityConfig usingFloatTolerance(float tolerance) {
    return toBuilder()
        .setFloatCorrespondenceMap(
            FieldScopeLogicMap.defaultValue(Correspondence.tolerance(tolerance)))
        .addUsingCorrespondenceString(".usingFloatTolerance(" + tolerance + ")")
        .build();
  }

  final FluentEqualityConfig usingFloatToleranceForFields(
      float tolerance, Iterable<Integer> fieldNumbers) {
    return toBuilder()
        .setFloatCorrespondenceMap(
            floatCorrespondenceMap()
                .with(
                    FieldScopeLogic.none().allowingFieldsNonRecursive(fieldNumbers),
                    Correspondence.tolerance(tolerance)))
        .addUsingCorrespondenceFieldNumbersString(
            ".usingFloatTolerance(" + tolerance + ", %s)", fieldNumbers)
        .build();
  }

  final FluentEqualityConfig usingFloatToleranceForFieldDescriptors(
      float tolerance, Iterable<FieldDescriptor> fieldDescriptors) {
    return toBuilder()
        .setFloatCorrespondenceMap(
            floatCorrespondenceMap()
                .with(
                    FieldScopeLogic.none().allowingFieldDescriptorsNonRecursive(fieldDescriptors),
                    Correspondence.tolerance(tolerance)))
        .addUsingCorrespondenceFieldDescriptorsString(
            ".usingFloatTolerance(" + tolerance + ", %s)", fieldDescriptors)
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
      builder.setCompareFieldsScope(
          FieldScopeLogic.and(compareFieldsScope(), FieldScopes.fromSetFields(messages).logic()));
    }
    return builder.build();
  }

  final FluentEqualityConfig withPartialScope(FieldScope partialScope) {
    return toBuilder()
        .setCompareFieldsScope(FieldScopeLogic.and(compareFieldsScope(), partialScope.logic()))
        .addUsingCorrespondenceFieldScopeString(".withPartialScope(%s)", partialScope)
        .build();
  }

  final FluentEqualityConfig ignoringFields(Iterable<Integer> fieldNumbers) {
    return toBuilder()
        .setCompareFieldsScope(compareFieldsScope().ignoringFields(fieldNumbers))
        .addUsingCorrespondenceFieldNumbersString(".ignoringFields(%s)", fieldNumbers)
        .build();
  }

  final FluentEqualityConfig ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    return toBuilder()
        .setCompareFieldsScope(compareFieldsScope().ignoringFieldDescriptors(fieldDescriptors))
        .addUsingCorrespondenceFieldDescriptorsString(
            ".ignoringFieldDescriptors(%s)", fieldDescriptors)
        .build();
  }

  final FluentEqualityConfig ignoringFieldScope(FieldScope fieldScope) {
    return toBuilder()
        .setCompareFieldsScope(
            FieldScopeLogic.and(compareFieldsScope(), FieldScopeLogic.not(fieldScope.logic())))
        .addUsingCorrespondenceFieldScopeString(".ignoringFieldScope(%s)", fieldScope)
        .build();
  }

  final FluentEqualityConfig reportingMismatchesOnly() {
    return toBuilder()
        .setReportMismatchesOnly(true)
        .addUsingCorrespondenceString(".reportingMismatchesOnly()")
        .build();
  }

  @Override
  public final FluentEqualityConfig subScope(
      Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
    return toBuilder()
        .setIgnoreFieldAbsenceScope(
            ignoreFieldAbsenceScope().subScope(rootDescriptor, fieldDescriptorOrUnknown))
        .setIgnoreRepeatedFieldOrderScope(
            ignoreRepeatedFieldOrderScope().subScope(rootDescriptor, fieldDescriptorOrUnknown))
        .setIgnoreExtraRepeatedFieldElementsScope(
            ignoreExtraRepeatedFieldElementsScope()
                .subScope(rootDescriptor, fieldDescriptorOrUnknown))
        .setDoubleCorrespondenceMap(
            doubleCorrespondenceMap().subScope(rootDescriptor, fieldDescriptorOrUnknown))
        .setFloatCorrespondenceMap(
            floatCorrespondenceMap().subScope(rootDescriptor, fieldDescriptorOrUnknown))
        .setCompareFieldsScope(
            compareFieldsScope().subScope(rootDescriptor, fieldDescriptorOrUnknown))
        .build();
  }

  @Override
  public final void validate(
      Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator) {
    // FluentEqualityConfig should never be validated other than as a root entity.
    Verify.verify(fieldDescriptorValidator == FieldDescriptorValidator.ALLOW_ALL);

    ignoreFieldAbsenceScope()
        .validate(rootDescriptor, FieldDescriptorValidator.IS_FIELD_WITH_ABSENCE);
    ignoreRepeatedFieldOrderScope()
        .validate(rootDescriptor, FieldDescriptorValidator.IS_FIELD_WITH_ORDER);
    ignoreExtraRepeatedFieldElementsScope()
        .validate(rootDescriptor, FieldDescriptorValidator.IS_FIELD_WITH_EXTRA_ELEMENTS);
    doubleCorrespondenceMap().validate(rootDescriptor, FieldDescriptorValidator.IS_DOUBLE_FIELD);
    floatCorrespondenceMap().validate(rootDescriptor, FieldDescriptorValidator.IS_FLOAT_FIELD);
    compareFieldsScope().validate(rootDescriptor, FieldDescriptorValidator.ALLOW_ALL);
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
    return Correspondence.from(
            // If we were allowed lambdas, this would be:
            // (M a, M e) ->
            //     ProtoTruth.assertThat(a).usingConfig(FluentEqualityConfig.this).testIsEqualTo(e),
            new Correspondence.BinaryPredicate<M, M>() {
              @Override
              public boolean apply(@NullableDecl M actual, @NullableDecl M expected) {
                return ProtoTruth.assertThat(actual)
                    .usingConfig(FluentEqualityConfig.this)
                    .testIsEqualTo(expected);
              }
            },
            "is equivalent according to assertThat(proto)"
                + usingCorrespondenceString(optDescriptor)
                + ".isEqualTo(target) to")
        .formattingDiffsUsing(
            // If we were allowed method references, this would be this::formatDiff.
            new Correspondence.DiffFormatter<M, M>() {
              @Override
              public String formatDiff(@NullableDecl M actual, @NullableDecl M expected) {
                return FluentEqualityConfig.this.formatDiff(actual, expected);
              }
            });
  }

  private <M extends Message> String formatDiff(@NullableDecl M actual, @NullableDecl M expected) {
    if (actual == null || expected == null) {
      return "";
    }

    return toMessageDifferencer(actual.getDescriptorForType())
        .diffMessages(actual, expected)
        .printToString(reportMismatchesOnly());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Builder methods.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  abstract Builder toBuilder();

  @CanIgnoreReturnValue
  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setIgnoreFieldAbsenceScope(FieldScopeLogic fieldScopeLogic);

    abstract Builder setIgnoreRepeatedFieldOrderScope(FieldScopeLogic fieldScopeLogic);

    abstract Builder setIgnoreExtraRepeatedFieldElementsScope(FieldScopeLogic fieldScopeLogic);

    abstract Builder setDoubleCorrespondenceMap(
        FieldScopeLogicMap<Correspondence<Number, Number>> doubleCorrespondenceMap);

    abstract Builder setFloatCorrespondenceMap(
        FieldScopeLogicMap<Correspondence<Number, Number>> floatCorrespondenceMap);

    abstract Builder setCompareExpectedFieldsOnly(boolean compare);

    abstract Builder setExpectedMessages(ImmutableList<Message> messages);

    abstract Builder setCompareFieldsScope(FieldScopeLogic fieldScopeLogic);

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
