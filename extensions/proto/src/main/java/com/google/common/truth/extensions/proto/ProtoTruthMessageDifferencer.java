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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.auto.value.AutoValue;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.truth.extensions.proto.DiffResult.RepeatedField;
import com.google.common.truth.extensions.proto.DiffResult.SingularField;
import com.google.common.truth.extensions.proto.DiffResult.UnknownFieldSetDiff;
import com.google.common.truth.extensions.proto.RecursableDiffEntity.WithResultCode.Result;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Tool to differentiate two messages with the same {@link Descriptor}, subject to the rules set out
 * in a {@link FluentEqualityConfig}.
 *
 * <p>A {@code ProtoTruthMessageDifferencer} is immutable and thread-safe. Its outputs, however,
 * have caching behaviors and are not thread-safe.
 */
final class ProtoTruthMessageDifferencer {

  /** Whether or not a sub-message tree should be ignored. */
  enum ShouldIgnore {
    /** Indicates that this field (and all its children) should be completely ignored. */
    YES,

    /**
     * Indicates that this field is a {@code Message} which may or may not contain sub-fields that
     * should not be ignored. The {@code Message} should be ignored iff no descendant (that is,
     * child, child-of-child, etc.) reports {@code SubResult.NO}.
     */
    MAYBE,

    /**
     * Indicates that this field should definitely not be ignored. However, its children might still
     * be ignorable.
     */
    NO;

    /**
     * Returns whether we should maybe ignore for this case, or we should definitely not ignore.
     *
     * <p>Useful as a simplification where YES is not expected, since it's illegal to call this
     * method on YES.
     */
    boolean shouldMaybeIgnore() {
      switch (this) {
        case YES:
          throw new IllegalStateException("Should not have ShouldIgnore.YES here.");
        case MAYBE:
          return true;
        case NO:
          return false;
      }
      throw new AssertionError("Impossible: " + this);
    }
  }

  @AutoValue
  abstract static class UnknownFieldDescriptor {

    enum Type {
      VARINT {
        @Override
        public List<?> getValues(UnknownFieldSet.Field field) {
          return field.getVarintList();
        }
      },
      FIXED32 {
        @Override
        public List<?> getValues(UnknownFieldSet.Field field) {
          return field.getFixed32List();
        }
      },
      FIXED64 {
        @Override
        public List<?> getValues(UnknownFieldSet.Field field) {
          return field.getFixed64List();
        }
      },
      LENGTH_DELIMITED {
        @Override
        public List<?> getValues(UnknownFieldSet.Field field) {
          return field.getLengthDelimitedList();
        }
      },
      GROUP {
        @Override
        public List<?> getValues(UnknownFieldSet.Field field) {
          return field.getGroupList();
        }
      };

      private static final ImmutableList<Type> TYPES = ImmutableList.copyOf(values());

      static ImmutableList<Type> all() {
        return TYPES;
      }

      /** Returns the corresponding values from the given field. */
      abstract List<?> getValues(UnknownFieldSet.Field field);
    }

    abstract int fieldNumber();

    abstract Type type();

    static UnknownFieldDescriptor create(int fieldNumber, Type type) {
      return new AutoValue_ProtoTruthMessageDifferencer_UnknownFieldDescriptor(fieldNumber, type);
    }

    static ImmutableList<UnknownFieldDescriptor> descriptors(
        int fieldNumber, UnknownFieldSet.Field field) {
      ImmutableList.Builder<UnknownFieldDescriptor> builder = ImmutableList.builder();
      for (Type type : Type.all()) {
        if (!type.getValues(field).isEmpty()) {
          builder.add(create(fieldNumber, type));
        }
      }
      return builder.build();
    }
  }

  @AutoValue
  abstract static class FieldDescriptorOrUnknown {
    abstract Optional<FieldDescriptor> fieldDescriptor();

    abstract Optional<UnknownFieldDescriptor> unknownFieldDescriptor();

    static FieldDescriptorOrUnknown fieldDescriptor(FieldDescriptor fieldDescriptor) {
      return new AutoValue_ProtoTruthMessageDifferencer_FieldDescriptorOrUnknown(
          Optional.of(fieldDescriptor), Optional.<UnknownFieldDescriptor>absent());
    }

    static FieldDescriptorOrUnknown unknown(UnknownFieldDescriptor unknownFieldDescriptor) {
      return new AutoValue_ProtoTruthMessageDifferencer_FieldDescriptorOrUnknown(
          Optional.<FieldDescriptor>absent(), Optional.of(unknownFieldDescriptor));
    }
  }

  private final FluentEqualityConfig config;
  private final Descriptor rootDescriptor;

  private ProtoTruthMessageDifferencer(FluentEqualityConfig config, Descriptor descriptor) {
    config.fieldScopeLogic().validate(descriptor);

    this.config = config;
    this.rootDescriptor = descriptor;
  }

  /** Create a new {@link ProtoTruthMessageDifferencer} for the given config and descriptor. */
  static ProtoTruthMessageDifferencer create(FluentEqualityConfig config, Descriptor descriptor) {
    return new ProtoTruthMessageDifferencer(config, descriptor);
  }

  /** Compare the two non-null messages, and return a detailed comparison report. */
  DiffResult diffMessages(Message actual, Message expected) {
    checkNotNull(actual);
    checkNotNull(expected);
    checkArgument(
        actual.getDescriptorForType() == expected.getDescriptorForType(),
        "The actual [%s] and expected [%s] message descriptors do not match.",
        actual.getDescriptorForType(),
        expected.getDescriptorForType());

    return diffMessages(actual, expected, config.fieldScopeLogic());
  }

  private DiffResult diffMessages(
      Message actual, Message expected, FieldScopeLogic fieldScopeLogic) {
    DiffResult.Builder builder = DiffResult.newBuilder().setActual(actual).setExpected(expected);

    // Compare known fields.
    Map<FieldDescriptor, Object> actualFields = actual.getAllFields();
    Map<FieldDescriptor, Object> expectedFields = expected.getAllFields();
    for (FieldDescriptor fieldDescriptor :
        Sets.union(actualFields.keySet(), expectedFields.keySet())) {
      // Check if we should ignore this field.  If ShouldIgnore.MAYBE, proceed anyway, but the field
      // will be considered ignored in the final diff report if no sub-fields get compared (i.e.,
      // the sub-DiffResult winds up empty). This allows us support FieldScopeLogic disjunctions
      // without repeating recursive work.
      FieldDescriptorOrUnknown fieldDescriptorOrUnknown =
          FieldDescriptorOrUnknown.fieldDescriptor(fieldDescriptor);
      ShouldIgnore shouldIgnore =
          fieldScopeLogic.shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown);
      if (shouldIgnore == ShouldIgnore.YES) {
        builder.addSingularField(
            fieldDescriptor.getNumber(), SingularField.ignored(name(fieldDescriptor)));
        continue;
      }

      if (fieldDescriptor.isRepeated()) {
        if (fieldDescriptor.isMapField()) {
          Map<Object, Object> actualMap = toProtoMap(actualFields.get(fieldDescriptor));
          Map<Object, Object> expectedMap = toProtoMap(expectedFields.get(fieldDescriptor));

          ImmutableSet<Object> keyOrder =
              Sets.union(actualMap.keySet(), expectedMap.keySet()).immutableCopy();
          builder.addAllSingularFields(
              fieldDescriptor.getNumber(),
              compareMapFieldsByKey(
                  actualMap, expectedMap, keyOrder, fieldDescriptor, fieldScopeLogic));
        } else {
          List<?> actualList = toProtoList(actualFields.get(fieldDescriptor));
          List<?> expectedList = toProtoList(expectedFields.get(fieldDescriptor));

          if (config.ignoreRepeatedFieldOrder()) {
            builder.addRepeatedField(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldIgnoringOrder(
                    actualList,
                    expectedList,
                    shouldIgnore.shouldMaybeIgnore(),
                    fieldDescriptor,
                    fieldScopeLogic.subLogic(rootDescriptor, fieldDescriptorOrUnknown)));
          } else {
            builder.addAllSingularFields(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldByIndices(
                    actualList,
                    expectedList,
                    shouldIgnore.shouldMaybeIgnore(),
                    fieldDescriptor,
                    fieldScopeLogic.subLogic(rootDescriptor, fieldDescriptorOrUnknown)));
          }
        }
      } else {
        builder.addSingularField(
            fieldDescriptor.getNumber(),
            compareSingularValue(
                actualFields.get(fieldDescriptor),
                expectedFields.get(fieldDescriptor),
                actual.getDefaultInstanceForType().getField(fieldDescriptor),
                shouldIgnore.shouldMaybeIgnore(),
                name(fieldDescriptor),
                fieldDescriptor,
                fieldScopeLogic.subLogic(rootDescriptor, fieldDescriptorOrUnknown)));
      }
    }

    // Compare unknown fields.
    if (!config.ignoreFieldAbsence()) {
      UnknownFieldSetDiff diff =
          diffUnknowns(actual.getUnknownFields(), expected.getUnknownFields(), fieldScopeLogic);
      builder.setUnknownFields(diff);
    }

    return builder.build();
  }

  // Helper which takes a proto map in List<Message> form, and converts it to a Map<Object, Object>
  // by extracting the keys and values from the generated map-entry submessages.  Returns an empty
  // map if null is passed in.
  private static Map<Object, Object> toProtoMap(@Nullable Object container) {
    if (container == null) {
      return Collections.emptyMap();
    }
    List<?> entryMessages = (List<?>) container;

    Map<Object, Object> retVal = Maps.newHashMap();
    for (Object entry : entryMessages) {
      Message message = (Message) entry;
      Object key = message.getAllFields().get(message.getDescriptorForType().findFieldByNumber(1));
      Object value =
          message.getAllFields().get(message.getDescriptorForType().findFieldByNumber(2));
      retVal.put(key, value);
    }
    return retVal;
  }

  // Takes a List<Object> or null, and returns the casted list in the first case, an empty list in
  // the latter case.
  private static List<?> toProtoList(@Nullable Object container) {
    if (container == null) {
      return Collections.emptyList();
    }
    return (List<?>) container;
  }

  private List<SingularField> compareMapFieldsByKey(
      Map<Object, Object> actualMap,
      Map<Object, Object> expectedMap,
      Set<Object> keyOrder,
      FieldDescriptor mapFieldDescriptor,
      FieldScopeLogic mapFieldScopeLogic) {
    FieldDescriptor valueFieldDescriptor = mapFieldDescriptor.getMessageType().findFieldByNumber(2);
    FieldDescriptorOrUnknown valueFieldDescriptorOrUnknown =
        FieldDescriptorOrUnknown.fieldDescriptor(valueFieldDescriptor);
    FieldScopeLogic valueFieldScopeLogic =
        mapFieldScopeLogic.subLogic(rootDescriptor, valueFieldDescriptorOrUnknown);

    // We never ignore the key, no matter what the logic dictates.
    ShouldIgnore shouldIgnoreValue =
        valueFieldScopeLogic.shouldIgnore(rootDescriptor, valueFieldDescriptorOrUnknown);
    if (shouldIgnoreValue == ShouldIgnore.YES) {
      return ImmutableList.of(SingularField.ignored(name(mapFieldDescriptor)));
    }

    ImmutableList.Builder<SingularField> builder =
        ImmutableList.builderWithExpectedSize(keyOrder.size());
    for (Object key : keyOrder) {
      @Nullable Object actualValue = actualMap.get(key);
      @Nullable Object expectedValue = expectedMap.get(key);
      builder.add(
          compareSingularValue(
              actualValue,
              expectedValue,
              /*defaultValue=*/ null,
              shouldIgnoreValue.shouldMaybeIgnore(),
              indexedName(mapFieldDescriptor, key),
              valueFieldDescriptor,
              valueFieldScopeLogic));
    }

    return builder.build();
  }

  private RepeatedField compareRepeatedFieldIgnoringOrder(
      List<?> actualList,
      List<?> expectedList,
      boolean shouldMaybeIgnore,
      FieldDescriptor fieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
    RepeatedField.Builder builder =
        RepeatedField.newBuilder()
            .setFieldName(name(fieldDescriptor))
            .setActual(actualList)
            .setExpected(expectedList);

    // TODO(user): Use maximum bipartite matching here, instead of greedy matching.
    Set<Integer> unmatchedActual = setForRange(actualList.size());
    Set<Integer> unmatchedExpected = setForRange(expectedList.size());
    for (int i = 0; i < actualList.size(); i++) {
      Object actual = actualList.get(i);
      for (int j : unmatchedExpected) {
        Object expected = expectedList.get(j);
        RepeatedField.PairResult pairResult =
            compareRepeatedFieldElementPair(
                actual, expected, shouldMaybeIgnore, fieldDescriptor, i, j, fieldScopeLogic);
        if (pairResult.isMatched()) {
          // Found a match - remove both these elements from the candidate pools.
          builder.addPairResult(pairResult);
          unmatchedActual.remove(i);
          unmatchedExpected.remove(j);
          break;
        }
      }
    }

    // Record remaining unmatched elements.
    for (int i : unmatchedActual) {
      builder.addPairResult(
          compareRepeatedFieldElementPair(
              actualList.get(i),
              /*expected=*/ null,
              shouldMaybeIgnore,
              fieldDescriptor,
              i,
              /*expectedFieldIndex=*/ null,
              fieldScopeLogic));
    }
    for (int j : unmatchedExpected) {
      builder.addPairResult(
          compareRepeatedFieldElementPair(
              /*actual=*/ null,
              expectedList.get(j),
              shouldMaybeIgnore,
              fieldDescriptor,
              /*actualFieldIndex=*/ null,
              j,
              fieldScopeLogic));
    }

    return builder.build();
  }

  private RepeatedField.PairResult compareRepeatedFieldElementPair(
      @Nullable Object actual,
      @Nullable Object expected,
      boolean shouldMaybeIgnore,
      FieldDescriptor fieldDescriptor,
      @Nullable Integer actualFieldIndex,
      @Nullable Integer expectedFieldIndex,
      FieldScopeLogic fieldScopeLogic) {
    SingularField comparison =
        compareSingularValue(
            actual,
            expected,
            /*defaultValue=*/ null,
            shouldMaybeIgnore,
            "<no field path>",
            fieldDescriptor,
            fieldScopeLogic);

    RepeatedField.PairResult.Builder pairResultBuilder =
        RepeatedField.PairResult.newBuilder().setResult(comparison.result());
    if (actual != null) {
      pairResultBuilder.setActual(actual).setActualFieldIndex(actualFieldIndex);
    }
    if (expected != null) {
      pairResultBuilder.setExpected(expected).setExpectedFieldIndex(expectedFieldIndex);
    }
    if (comparison.breakdown().isPresent()) {
      pairResultBuilder.setBreakdown(comparison.breakdown().get());
    }
    return pairResultBuilder.build();
  }

  /** Returns a {@link LinkedHashSet} containing the integers in {@code [0, max)}, in order. */
  private static Set<Integer> setForRange(int max) {
    Set<Integer> set = Sets.newLinkedHashSet();
    for (int i = 0; i < max; i++) {
      set.add(i);
    }
    return set;
  }

  /**
   * Compares {@code actualList} and {@code expectedList}, two submessages corresponding to {@code
   * fieldDescriptor}. Uses {@code shouldMaybeIgnore}, {@code parentFieldPath}, and {@code
   * fieldScopeLogic} to compare the messages.
   *
   * @return A list in index order, containing the diff results for each message.
   */
  private List<SingularField> compareRepeatedFieldByIndices(
      List<?> actualList,
      List<?> expectedList,
      boolean shouldMaybeIgnore,
      FieldDescriptor fieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
    int maxSize = Math.max(actualList.size(), expectedList.size());
    ImmutableList.Builder<SingularField> builder = ImmutableList.builderWithExpectedSize(maxSize);
    for (int i = 0; i < maxSize; i++) {
      @Nullable Object actual = actualList.size() > i ? actualList.get(i) : null;
      @Nullable Object expected = expectedList.size() > i ? expectedList.get(i) : null;
      builder.add(
          compareSingularValue(
              actual,
              expected,
              /*defaultValue=*/ null,
              shouldMaybeIgnore,
              indexedName(fieldDescriptor, i),
              fieldDescriptor,
              fieldScopeLogic));
    }

    return builder.build();
  }

  private SingularField compareSingularValue(
      @Nullable Object actual,
      @Nullable Object expected,
      @Nullable Object defaultValue,
      boolean shouldMaybeIgnore,
      String fieldName,
      FieldDescriptor fieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
    if (fieldDescriptor.getJavaType() == JavaType.MESSAGE) {
      return compareSingularMessage(
          (Message) actual,
          (Message) expected,
          (Message) defaultValue,
          shouldMaybeIgnore,
          fieldName,
          fieldScopeLogic);
    } else {
      checkState(!shouldMaybeIgnore, "MAYBE is not a valid ShouldIgnore for primitives.");
      return compareSingularPrimitive(actual, expected, defaultValue, fieldName);
    }
  }

  // Replaces 'input' with 'defaultValue' iff input is null and we're ignoring field absence.
  // Otherwise, just returns the input.
  private <T> T orIfIgnoringFieldAbsence(@Nullable T input, @Nullable T defaultValue) {
    return (input == null && config.ignoreFieldAbsence()) ? defaultValue : input;
  }

  // Returns 'input' if it's non-null, otherwise the default instance of 'other'.
  // Requires at least one parameter is non-null.
  private static Message orDefaultForType(@Nullable Message input, @Nullable Message other) {
    return (input != null) ? input : other.getDefaultInstanceForType();
  }

  private SingularField compareSingularMessage(
      @Nullable Message actual,
      @Nullable Message expected,
      @Nullable Message defaultValue,
      boolean shouldMaybeIgnore,
      String fieldName,
      FieldScopeLogic fieldScopeLogic) {
    Result.Builder result = Result.builder();

    // Use the default if it's set and we're ignoring field absence.
    actual = orIfIgnoringFieldAbsence(actual, defaultValue);
    expected = orIfIgnoringFieldAbsence(expected, defaultValue);

    // If actual or expected is missing here, we know our result so long as it's not ignored.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    // Perform the detailed breakdown only if necessary.
    @Nullable DiffResult breakdown = null;
    if (result.build() == Result.MATCHED || shouldMaybeIgnore) {
      actual = orDefaultForType(actual, expected);
      expected = orDefaultForType(expected, actual);

      breakdown = diffMessages(actual, expected, fieldScopeLogic);
      if (breakdown.isIgnored() && shouldMaybeIgnore) {
        // Ignore this field entirely, report nothing.
        return SingularField.ignored(fieldName);
      }

      result.markModifiedIf(!breakdown.isMatched());
    }

    // Report the full breakdown.
    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder().setFieldName(fieldName).setResult(result.build());
    if (actual != null) {
      singularFieldBuilder.setActual(actual);
    }
    if (expected != null) {
      singularFieldBuilder.setExpected(expected);
    }
    if (breakdown != null) {
      singularFieldBuilder.setBreakdown(breakdown);
    }
    return singularFieldBuilder.build();
  }

  private SingularField compareSingularPrimitive(
      @Nullable Object actual,
      @Nullable Object expected,
      @Nullable Object defaultValue,
      String fieldName) {
    Result.Builder result = Result.builder();

    // Use the default if it's set and we're ignoring field absence.
    actual = orIfIgnoringFieldAbsence(actual, defaultValue);
    expected = orIfIgnoringFieldAbsence(expected, defaultValue);

    // If actual or expected is missing here, we know our result.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    // TODO(user): Implement approximate equality testing for floats/doubles.
    result.markModifiedIf(!Objects.equal(actual, expected));

    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder().setFieldName(fieldName).setResult(result.build());
    if (actual != null) {
      singularFieldBuilder.setActual(actual);
    }
    if (expected != null) {
      singularFieldBuilder.setExpected(expected);
    }
    return singularFieldBuilder.build();
  }

  private UnknownFieldSetDiff diffUnknowns(
      UnknownFieldSet actual, UnknownFieldSet expected, FieldScopeLogic fieldScopeLogic) {
    UnknownFieldSetDiff.Builder builder = UnknownFieldSetDiff.newBuilder();

    Map<Integer, UnknownFieldSet.Field> actualFields = actual.asMap();
    Map<Integer, UnknownFieldSet.Field> expectedFields = expected.asMap();
    for (int fieldNumber : Sets.union(actualFields.keySet(), expectedFields.keySet())) {
      @Nullable UnknownFieldSet.Field actualField = actualFields.get(fieldNumber);
      @Nullable UnknownFieldSet.Field expectedField = expectedFields.get(fieldNumber);
      for (UnknownFieldDescriptor.Type type : UnknownFieldDescriptor.Type.all()) {
        List<?> actualValues =
            actualField != null ? type.getValues(actualField) : Collections.emptyList();
        List<?> expectedValues =
            expectedField != null ? type.getValues(expectedField) : Collections.emptyList();
        if (actualValues.isEmpty() && expectedValues.isEmpty()) {
          continue;
        }

        UnknownFieldDescriptor unknownFieldDescriptor =
            UnknownFieldDescriptor.create(fieldNumber, type);
        FieldDescriptorOrUnknown fieldDescriptorOrUnknown =
            FieldDescriptorOrUnknown.unknown(unknownFieldDescriptor);
        ShouldIgnore shouldIgnore =
            fieldScopeLogic.shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown);
        if (shouldIgnore == ShouldIgnore.YES) {
          builder.addSingularField(
              fieldNumber, SingularField.ignored(name(unknownFieldDescriptor)));
          continue;
        }

        builder.addAllSingularFields(
            fieldNumber,
            compareUnknownFieldList(
                actualValues,
                expectedValues,
                shouldIgnore.shouldMaybeIgnore(),
                unknownFieldDescriptor,
                fieldScopeLogic.subLogic(rootDescriptor, fieldDescriptorOrUnknown)));
      }
    }

    return builder.build();
  }

  private List<SingularField> compareUnknownFieldList(
      List<?> actualValues,
      List<?> expectedValues,
      boolean shouldMaybeIgnore,
      UnknownFieldDescriptor unknownFieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
    int maxSize = Math.max(actualValues.size(), expectedValues.size());
    ImmutableList.Builder<SingularField> builder = ImmutableList.builderWithExpectedSize(maxSize);
    for (int i = 0; i < maxSize; i++) {
      @Nullable Object actual = actualValues.size() > i ? actualValues.get(i) : null;
      @Nullable Object expected = expectedValues.size() > i ? expectedValues.get(i) : null;
      builder.add(
          compareUnknownFieldValue(
              actual,
              expected,
              shouldMaybeIgnore,
              indexedName(unknownFieldDescriptor, i),
              unknownFieldDescriptor,
              fieldScopeLogic));
    }

    return builder.build();
  }

  private SingularField compareUnknownFieldValue(
      @Nullable Object actual,
      @Nullable Object expected,
      boolean shouldMaybeIgnore,
      String fieldName,
      UnknownFieldDescriptor unknownFieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
    if (unknownFieldDescriptor.type() == UnknownFieldDescriptor.Type.GROUP) {
      return compareUnknownFieldSet(
          (UnknownFieldSet) actual,
          (UnknownFieldSet) expected,
          shouldMaybeIgnore,
          fieldName,
          fieldScopeLogic);
    } else {
      checkState(!shouldMaybeIgnore, "MAYBE is not a valid ShouldIgnore for primitives.");
      return compareUnknownPrimitive(actual, expected, fieldName);
    }
  }

  private SingularField compareUnknownFieldSet(
      @Nullable UnknownFieldSet actual,
      @Nullable UnknownFieldSet expected,
      boolean shouldMaybeIgnore,
      String fieldName,
      FieldScopeLogic fieldScopeLogic) {
    Result.Builder result = Result.builder();

    // If actual or expected is missing, we know the result as long as it's not ignored.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    // Perform the detailed breakdown only if necessary.
    @Nullable UnknownFieldSetDiff unknownsBreakdown = null;
    if (result.build() == Result.MATCHED || shouldMaybeIgnore) {
      actual = firstNonNull(actual, UnknownFieldSet.getDefaultInstance());
      expected = firstNonNull(expected, UnknownFieldSet.getDefaultInstance());

      unknownsBreakdown = diffUnknowns(actual, expected, fieldScopeLogic);
      if (unknownsBreakdown.isIgnored() && shouldMaybeIgnore) {
        // Ignore this field entirely, report nothing.
        return SingularField.ignored(fieldName);
      }
      result.markModifiedIf(!unknownsBreakdown.isMatched());
    }

    // Report the full breakdown.
    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder().setFieldName(fieldName).setResult(result.build());
    if (actual != null) {
      singularFieldBuilder.setActual(actual);
    }
    if (expected != null) {
      singularFieldBuilder.setExpected(expected);
    }
    if (unknownsBreakdown != null) {
      singularFieldBuilder.setUnknownsBreakdown(unknownsBreakdown);
    }
    return singularFieldBuilder.build();
  }

  private SingularField compareUnknownPrimitive(
      @Nullable Object actual, @Nullable Object expected, String fieldName) {
    Result.Builder result = Result.builder();

    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);
    result.markModifiedIf(!Objects.equal(actual, expected));

    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder().setFieldName(fieldName).setResult(result.build());
    if (actual != null) {
      singularFieldBuilder.setActual(actual);
    }
    if (expected != null) {
      singularFieldBuilder.setExpected(expected);
    }
    return singularFieldBuilder.build();
  }

  private static String name(FieldDescriptor fieldDescriptor) {
    return fieldDescriptor.isExtension() ? "[" + fieldDescriptor + "]" : fieldDescriptor.getName();
  }

  private static String indexedName(FieldDescriptor fieldDescriptor, Object key) {
    return name(fieldDescriptor) + "[" + key + "]";
  }

  private static String name(UnknownFieldDescriptor unknownFieldDescriptor) {
    return String.valueOf(unknownFieldDescriptor.fieldNumber());
  }

  private static String indexedName(UnknownFieldDescriptor unknownFieldDescriptor, int index) {
    return name(unknownFieldDescriptor) + "[" + index + "]";
  }
}
