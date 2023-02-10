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

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.truth.Correspondence;
import com.google.common.truth.extensions.proto.DiffResult.RepeatedField;
import com.google.common.truth.extensions.proto.DiffResult.SingularField;
import com.google.common.truth.extensions.proto.DiffResult.UnknownFieldSetDiff;
import com.google.common.truth.extensions.proto.RecursableDiffEntity.WithResultCode.Result;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.UnknownFieldSet;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Tool to differentiate two messages with the same {@link Descriptor}, subject to the rules set out
 * in a {@link FluentEqualityConfig}.
 *
 * <p>A {@code ProtoTruthMessageDifferencer} is immutable and thread-safe. Its outputs, however,
 * have caching behaviors and are not thread-safe.
 */
final class ProtoTruthMessageDifferencer {
  private final FluentEqualityConfig rootConfig;
  private final Descriptor rootDescriptor;
  private final TextFormat.Printer protoPrinter;

  private ProtoTruthMessageDifferencer(FluentEqualityConfig rootConfig, Descriptor descriptor) {
    rootConfig.validate(descriptor, FieldDescriptorValidator.ALLOW_ALL);

    this.rootConfig = rootConfig;
    this.rootDescriptor = descriptor;
    this.protoPrinter = TextFormat.printer().usingTypeRegistry(rootConfig.useTypeRegistry());
  }

  /** Create a new {@link ProtoTruthMessageDifferencer} for the given config and descriptor. */
  static ProtoTruthMessageDifferencer create(
      FluentEqualityConfig rootConfig, Descriptor descriptor) {
    return new ProtoTruthMessageDifferencer(rootConfig, descriptor);
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

    return diffMessages(actual, expected, rootConfig);
  }

  private DiffResult diffMessages(Message actual, Message expected, FluentEqualityConfig config) {
    if (actual.getDescriptorForType().equals(Any.getDescriptor())) {
      return diffAnyMessages(actual, expected, config);
    }
    DiffResult.Builder builder = DiffResult.newBuilder().setActual(actual).setExpected(expected);

    // Compare known fields.
    Map<FieldDescriptor, Object> actualFields = actual.getAllFields();
    Map<FieldDescriptor, Object> expectedFields = expected.getAllFields();
    for (FieldDescriptor fieldDescriptor :
        Sets.union(actualFields.keySet(), expectedFields.keySet())) {
      // Check if we should ignore this field.  If the result is nonrecursive, proceed anyway, but
      // the field will be considered ignored in the final diff report if no sub-fields get compared
      // (i.e., the sub-DiffResult winds up empty). This allows us support FieldScopeLogic
      // disjunctions without repeating recursive work.
      SubScopeId subScopeId = SubScopeId.of(fieldDescriptor);
      FieldScopeResult shouldCompare =
          config.compareFieldsScope().policyFor(rootDescriptor, subScopeId);
      if (shouldCompare == FieldScopeResult.EXCLUDED_RECURSIVELY) {
        builder.addSingularField(
            fieldDescriptor.getNumber(), SingularField.ignored(name(fieldDescriptor)));
        continue;
      }

      if (fieldDescriptor.isRepeated()) {
        if (fieldDescriptor.isMapField()) {
          Map<Object, Object> actualMap = toProtoMap(actualFields.get(fieldDescriptor));
          Map<Object, Object> expectedMap = toProtoMap(expectedFields.get(fieldDescriptor));

          ImmutableSet<Object> actualAndExpectedKeys =
              Sets.union(actualMap.keySet(), expectedMap.keySet()).immutableCopy();
          builder.addAllSingularFields(
              fieldDescriptor.getNumber(),
              compareMapFieldsByKey(
                  actualMap,
                  expectedMap,
                  actualAndExpectedKeys,
                  fieldDescriptor,
                  config.subScope(rootDescriptor, subScopeId)));
        } else {
          List<?> actualList = toProtoList(actualFields.get(fieldDescriptor));
          List<?> expectedList = toProtoList(expectedFields.get(fieldDescriptor));

          boolean ignoreRepeatedFieldOrder =
              config.ignoreRepeatedFieldOrderScope().contains(rootDescriptor, subScopeId);
          boolean ignoreExtraRepeatedFieldElements =
              config.ignoreExtraRepeatedFieldElementsScope().contains(rootDescriptor, subScopeId);
          if (ignoreRepeatedFieldOrder) {
            builder.addRepeatedField(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldIgnoringOrder(
                    actualList,
                    expectedList,
                    shouldCompare == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                    fieldDescriptor,
                    ignoreExtraRepeatedFieldElements,
                    config.subScope(rootDescriptor, subScopeId)));
          } else if (ignoreExtraRepeatedFieldElements && !expectedList.isEmpty()) {
            builder.addRepeatedField(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldExpectingSubsequence(
                    actualList,
                    expectedList,
                    shouldCompare == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                    fieldDescriptor,
                    config.subScope(rootDescriptor, subScopeId)));
          } else {
            builder.addAllSingularFields(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldByIndices(
                    actualList,
                    expectedList,
                    shouldCompare == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                    fieldDescriptor,
                    config.subScope(rootDescriptor, subScopeId)));
          }
        }
      } else {
        builder.addSingularField(
            fieldDescriptor.getNumber(),
            compareSingularValue(
                actualFields.get(fieldDescriptor),
                expectedFields.get(fieldDescriptor),
                actual.getDefaultInstanceForType().getField(fieldDescriptor),
                shouldCompare == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                fieldDescriptor,
                name(fieldDescriptor),
                config.subScope(rootDescriptor, subScopeId)));
      }
    }

    // Compare unknown fields.
    if (!config.ignoreFieldAbsenceScope().isAll()) {
      UnknownFieldSetDiff diff =
          diffUnknowns(actual.getUnknownFields(), expected.getUnknownFields(), config);
      builder.setUnknownFields(diff);
    }

    return builder.build();
  }

  private DiffResult diffAnyMessages(
      Message actual, Message expected, FluentEqualityConfig config) {
    DiffResult.Builder builder = DiffResult.newBuilder().setActual(actual).setExpected(expected);

    // Compare the TypeUrl fields.
    FieldScopeResult shouldCompareTypeUrl =
        config.compareFieldsScope().policyFor(rootDescriptor, AnyUtils.typeUrlSubScopeId());
    SingularField typeUrlDiffResult;
    if (!shouldCompareTypeUrl.included()) {
      typeUrlDiffResult = SingularField.ignored(name(AnyUtils.typeUrlFieldDescriptor()));
    } else {
      typeUrlDiffResult =
          compareSingularPrimitive(
              actual.getField(AnyUtils.typeUrlFieldDescriptor()),
              expected.getField(AnyUtils.typeUrlFieldDescriptor()),
              /* defaultValue= */ "",
              AnyUtils.typeUrlFieldDescriptor(),
              name(AnyUtils.typeUrlFieldDescriptor()),
              config.subScope(rootDescriptor, AnyUtils.typeUrlSubScopeId()));
    }
    builder.addSingularField(Any.TYPE_URL_FIELD_NUMBER, typeUrlDiffResult);

    // Try to unpack the value fields using the TypeRegister and url from the type_url field. If
    // that does not work then we revert to the original behaviour compare the bytes strings.
    FieldScopeResult shouldCompareValue =
        config.compareFieldsScope().policyFor(rootDescriptor, AnyUtils.valueSubScopeId());
    SingularField valueDiffResult;
    if (shouldCompareValue == FieldScopeResult.EXCLUDED_RECURSIVELY) {
      valueDiffResult = SingularField.ignored(name(AnyUtils.valueFieldDescriptor()));
    } else {
      Optional<Message> unpackedActual = AnyUtils.unpack(actual, config);
      Optional<Message> unpackedExpected = AnyUtils.unpack(expected, config);
      if (unpackedActual.isPresent()
          && unpackedExpected.isPresent()
          && descriptorsMatch(unpackedActual.get(), unpackedExpected.get())) {
        Message defaultMessage = unpackedActual.get().getDefaultInstanceForType();
        valueDiffResult =
            compareSingularMessage(
                unpackedActual.get(),
                unpackedExpected.get(),
                defaultMessage,
                shouldCompareValue == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                AnyUtils.valueFieldDescriptor(),
                name(AnyUtils.valueFieldDescriptor()),
                config.subScope(rootDescriptor, AnyUtils.valueSubScopeId()));
      } else {
        valueDiffResult =
            compareSingularValue(
                actual.getField(AnyUtils.valueFieldDescriptor()),
                expected.getField(AnyUtils.valueFieldDescriptor()),
                AnyUtils.valueFieldDescriptor().getDefaultValue(),
                shouldCompareValue == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                AnyUtils.valueFieldDescriptor(),
                name(AnyUtils.valueFieldDescriptor()),
                config.subScope(rootDescriptor, AnyUtils.valueSubScopeId()));
      }
    }
    builder.addSingularField(Any.VALUE_FIELD_NUMBER, valueDiffResult);

    // Compare unknown fields.
    if (!config.ignoreFieldAbsenceScope().isAll()) {
      UnknownFieldSetDiff diff =
          diffUnknowns(actual.getUnknownFields(), expected.getUnknownFields(), config);
      builder.setUnknownFields(diff);
    }

    return builder.build();
  }

  private static boolean descriptorsMatch(Message actual, Message expected) {
    return actual.getDescriptorForType().equals(expected.getDescriptorForType());
  }

  // Helper which takes a proto map in List<Message> form, and converts it to a Map<Object, Object>
  // by extracting the keys and values from the generated map-entry submessages.  Returns an empty
  // map if null is passed in.
  private static ImmutableMap<Object, Object> toProtoMap(@Nullable Object container) {
    if (container == null) {
      return ImmutableMap.of();
    }
    List<?> entryMessages = (List<?>) container;

    // Can't use an ImmutableMap.Builder because proto wire format could have multiple entries with
    // the same key. Documented behaviour is to use the last seen entry.
    Map<Object, Object> retVal = Maps.newHashMap();
    for (Object entry : entryMessages) {
      Message message = (Message) entry;
      retVal.put(valueAtFieldNumber(message, 1), valueAtFieldNumber(message, 2));
    }
    return ImmutableMap.copyOf(retVal);
  }

  private static Object valueAtFieldNumber(Message message, int fieldNumber) {
    FieldDescriptor field = message.getDescriptorForType().findFieldByNumber(fieldNumber);
    Object value = message.getAllFields().get(field);
    return value != null ? value : field.getDefaultValue();
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
      Set<Object> actualAndExpectedKeys,
      FieldDescriptor mapFieldDescriptor,
      FluentEqualityConfig mapConfig) {
    FieldDescriptor keyFieldDescriptor = mapFieldDescriptor.getMessageType().findFieldByNumber(1);
    FieldDescriptor valueFieldDescriptor = mapFieldDescriptor.getMessageType().findFieldByNumber(2);
    SubScopeId valueSubScopeId = SubScopeId.of(valueFieldDescriptor);

    // We never ignore the key, no matter what the logic dictates.
    FieldScopeResult compareValues =
        mapConfig.compareFieldsScope().policyFor(rootDescriptor, valueSubScopeId);
    if (compareValues == FieldScopeResult.EXCLUDED_RECURSIVELY) {
      return ImmutableList.of(SingularField.ignored(name(mapFieldDescriptor)));
    }

    boolean ignoreExtraRepeatedFieldElements =
        mapConfig
            .ignoreExtraRepeatedFieldElementsScope()
            .contains(rootDescriptor, SubScopeId.of(mapFieldDescriptor));

    FluentEqualityConfig valuesConfig = mapConfig.subScope(rootDescriptor, valueSubScopeId);

    ImmutableList.Builder<SingularField> builder =
        ImmutableList.builderWithExpectedSize(actualAndExpectedKeys.size());
    for (Object key : actualAndExpectedKeys) {
      @Nullable Object actualValue = actualMap.get(key);
      @Nullable Object expectedValue = expectedMap.get(key);
      if (ignoreExtraRepeatedFieldElements && !expectedMap.isEmpty() && expectedValue == null) {
        builder.add(
            SingularField.ignored(indexedName(mapFieldDescriptor, key, keyFieldDescriptor)));
      } else {
        builder.add(
            compareSingularValue(
                actualValue,
                expectedValue,
                /*defaultValue=*/ null,
                compareValues == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                valueFieldDescriptor,
                indexedName(mapFieldDescriptor, key, keyFieldDescriptor),
                valuesConfig));
      }
    }

    return builder.build();
  }

  private RepeatedField compareRepeatedFieldIgnoringOrder(
      List<?> actualList,
      List<?> expectedList,
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      boolean ignoreExtraRepeatedFieldElements,
      FluentEqualityConfig config) {
    RepeatedField.Builder builder =
        RepeatedField.newBuilder()
            .setFieldDescriptor(fieldDescriptor)
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
                actual, expected, excludeNonRecursive, fieldDescriptor, i, j, config);
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
      if (ignoreExtraRepeatedFieldElements && !expectedList.isEmpty()) {
        builder.addPairResult(
            RepeatedField.PairResult.newBuilder()
                .setResult(Result.IGNORED)
                .setActual(actualList.get(i))
                .setActualFieldIndex(i)
                .setFieldDescriptor(fieldDescriptor)
                .setProtoPrinter(protoPrinter)
                .build());
      } else {
        builder.addPairResult(
            compareRepeatedFieldElementPair(
                actualList.get(i),
                /*expected=*/ null,
                excludeNonRecursive,
                fieldDescriptor,
                i,
                /*expectedFieldIndex=*/ null,
                config));
      }
    }
    for (int j : unmatchedExpected) {
      builder.addPairResult(
          compareRepeatedFieldElementPair(
              /*actual=*/ null,
              expectedList.get(j),
              excludeNonRecursive,
              fieldDescriptor,
              /*actualFieldIndex=*/ null,
              j,
              config));
    }

    return builder.build();
  }

  private RepeatedField compareRepeatedFieldExpectingSubsequence(
      List<?> actualList,
      List<?> expectedList,
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      FluentEqualityConfig config) {
    RepeatedField.Builder builder =
        RepeatedField.newBuilder()
            .setFieldDescriptor(fieldDescriptor)
            .setActual(actualList)
            .setExpected(expectedList);

    // Search for expectedList as a subsequence of actualList.
    //
    // This mostly replicates the algorithm used by IterableSubject.containsAtLeast().inOrder(), but
    // with some tweaks for fuzzy equality and structured output.
    Deque<Integer> actualIndices = new ArrayDeque<>();
    for (int i = 0; i < actualList.size(); i++) {
      actualIndices.addLast(i);
    }
    Deque<Integer> actualNotInOrder = new ArrayDeque<>();

    for (int expectedIndex = 0; expectedIndex < expectedList.size(); expectedIndex++) {
      Object expected = expectedList.get(expectedIndex);

      // Find the first actual element which matches.
      RepeatedField.PairResult matchingResult =
          findMatchingPairResult(
              actualIndices,
              actualList,
              expectedIndex,
              expected,
              excludeNonRecursive,
              fieldDescriptor,
              config);

      if (matchingResult != null) {
        // Move all prior elements to actualNotInOrder.
        while (!actualIndices.isEmpty()
            && actualIndices.getFirst() < matchingResult.actualFieldIndex().get()) {
          actualNotInOrder.add(actualIndices.removeFirst());
        }
        builder.addPairResult(matchingResult);
      } else {
        // Otherwise, see if a previous element matches, so we can improve the diff.
        matchingResult =
            findMatchingPairResult(
                actualNotInOrder,
                actualList,
                expectedIndex,
                expected,
                excludeNonRecursive,
                fieldDescriptor,
                config);
        if (matchingResult != null) {
          // Report an out-of-order match, which is treated as not-matched.
          matchingResult = matchingResult.toBuilder().setResult(Result.MOVED_OUT_OF_ORDER).build();
          builder.addPairResult(matchingResult);
        } else {
          // Report a missing expected element.
          builder.addPairResult(
              RepeatedField.PairResult.newBuilder()
                  .setResult(Result.REMOVED)
                  .setFieldDescriptor(fieldDescriptor)
                  .setExpected(expected)
                  .setExpectedFieldIndex(expectedIndex)
                  .setProtoPrinter(protoPrinter)
                  .build());
        }
      }
    }

    // Report any remaining not-in-order elements as ignored.
    for (int index : actualNotInOrder) {
      builder.addPairResult(
          RepeatedField.PairResult.newBuilder()
              .setResult(Result.IGNORED)
              .setFieldDescriptor(fieldDescriptor)
              .setActual(actualList.get(index))
              .setActualFieldIndex(index)
              .setProtoPrinter(protoPrinter)
              .build());
    }

    return builder.build();
  }

  // Given a list of values, a list of indexes into that list, and an expected value, find the first
  // actual value that compares equal to the expected value, and return the PairResult for it.
  // Also removes the index for the matching value from actualIndicies.
  //
  // If there is no match, returns null.
  private RepeatedField.@Nullable PairResult findMatchingPairResult(
      Deque<Integer> actualIndices,
      List<?> actualValues,
      int expectedIndex,
      Object expectedValue,
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      FluentEqualityConfig config) {
    Iterator<Integer> actualIndexIter = actualIndices.iterator();
    while (actualIndexIter.hasNext()) {
      int actualIndex = actualIndexIter.next();
      RepeatedField.PairResult pairResult =
          compareRepeatedFieldElementPair(
              actualValues.get(actualIndex),
              expectedValue,
              excludeNonRecursive,
              fieldDescriptor,
              actualIndex,
              expectedIndex,
              config);
      if (pairResult.isMatched()) {
        actualIndexIter.remove();
        return pairResult;
      }
    }

    return null;
  }

  private RepeatedField.PairResult compareRepeatedFieldElementPair(
      @Nullable Object actual,
      @Nullable Object expected,
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      @Nullable Integer actualFieldIndex,
      @Nullable Integer expectedFieldIndex,
      FluentEqualityConfig config) {
    SingularField comparison =
        compareSingularValue(
            actual,
            expected,
            /*defaultValue=*/ null,
            excludeNonRecursive,
            fieldDescriptor,
            "<no field path>",
            config);

    RepeatedField.PairResult.Builder pairResultBuilder =
        RepeatedField.PairResult.newBuilder()
            .setResult(comparison.result())
            .setFieldDescriptor(fieldDescriptor)
            .setProtoPrinter(protoPrinter);
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
   * fieldDescriptor}. Uses {@code excludeNonRecursive}, {@code parentFieldPath}, and {@code
   * fieldScopeLogic} to compare the messages.
   *
   * @return A list in index order, containing the diff results for each message.
   */
  private List<SingularField> compareRepeatedFieldByIndices(
      List<?> actualList,
      List<?> expectedList,
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      FluentEqualityConfig config) {
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
              excludeNonRecursive,
              fieldDescriptor,
              indexedName(fieldDescriptor, i),
              config));
    }

    return builder.build();
  }

  private SingularField compareSingularValue(
      @Nullable Object actual,
      @Nullable Object expected,
      @Nullable Object defaultValue,
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      String fieldName,
      FluentEqualityConfig config) {
    if (fieldDescriptor.getJavaType() == JavaType.MESSAGE) {
      return compareSingularMessage(
          (Message) actual,
          (Message) expected,
          (Message) defaultValue,
          excludeNonRecursive,
          fieldDescriptor,
          fieldName,
          config);
    } else if (excludeNonRecursive) {
      return SingularField.ignored(fieldName);
    } else {
      return compareSingularPrimitive(
          actual, expected, defaultValue, fieldDescriptor, fieldName, config);
    }
  }

  // Replaces 'input' with 'defaultValue' iff input is null and we're ignoring field absence.
  // Otherwise, just returns the input.
  private <T> T orIfIgnoringFieldAbsence(
      @Nullable T input, @Nullable T defaultValue, boolean ignoreFieldAbsence) {
    return (input == null && ignoreFieldAbsence) ? defaultValue : input;
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
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      String fieldName,
      FluentEqualityConfig config) {
    Result.Builder result = Result.builder();

    // Use the default if it's set and we're ignoring field absence.
    boolean ignoreFieldAbsence =
        config.ignoreFieldAbsenceScope().contains(rootDescriptor, SubScopeId.of(fieldDescriptor));
    actual = orIfIgnoringFieldAbsence(actual, defaultValue, ignoreFieldAbsence);
    expected = orIfIgnoringFieldAbsence(expected, defaultValue, ignoreFieldAbsence);

    // If actual or expected is missing here, we know our result so long as it's not ignored.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    // Perform the detailed breakdown only if necessary.
    @Nullable DiffResult breakdown = null;
    if (result.build() == Result.MATCHED || excludeNonRecursive) {
      actual = orDefaultForType(actual, expected);
      expected = orDefaultForType(expected, actual);

      breakdown = diffMessages(actual, expected, config);
      if (breakdown.isIgnored() && excludeNonRecursive) {
        // Ignore this field entirely, report nothing.
        return SingularField.ignored(fieldName);
      }

      result.markModifiedIf(!breakdown.isMatched());
    }

    // Report the full breakdown.
    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder()
            .setSubScopeId(SubScopeId.of(fieldDescriptor))
            .setFieldName(fieldName)
            .setResult(result.build())
            .setProtoPrinter(protoPrinter);
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
      FieldDescriptor fieldDescriptor,
      String fieldName,
      FluentEqualityConfig config) {
    Result.Builder result = Result.builder();

    // Use the default if it's set and we're ignoring field absence or if it's a field without
    // presence for which default is indistinguishable from unset.
    SubScopeId subScopeId = SubScopeId.of(fieldDescriptor);
    boolean hasPresence = fieldDescriptor.isRepeated() || fieldDescriptor.hasPresence();
    boolean ignoreFieldAbsence =
        !hasPresence || config.ignoreFieldAbsenceScope().contains(rootDescriptor, subScopeId);
    actual = orIfIgnoringFieldAbsence(actual, defaultValue, ignoreFieldAbsence);
    expected = orIfIgnoringFieldAbsence(expected, defaultValue, ignoreFieldAbsence);

    // If actual or expected is missing here, we know our result.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    if (actual != null && expected != null) {
      if (actual instanceof Double) {
        result.markModifiedIf(
            !doublesEqual(
                (double) actual,
                (double) expected,
                config.doubleCorrespondenceMap().get(rootDescriptor, subScopeId)
                ));
      } else if (actual instanceof Float) {
        result.markModifiedIf(
            !floatsEqual(
                (float) actual,
                (float) expected,
                config.floatCorrespondenceMap().get(rootDescriptor, subScopeId)
                ));
      } else {
        result.markModifiedIf(!Objects.equal(actual, expected));
      }
    }

    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder()
            .setSubScopeId(SubScopeId.of(fieldDescriptor))
            .setFieldName(fieldName)
            .setResult(result.build())
            .setProtoPrinter(protoPrinter);
    if (actual != null) {
      singularFieldBuilder.setActual(actual);
    }
    if (expected != null) {
      singularFieldBuilder.setExpected(expected);
    }
    return singularFieldBuilder.build();
  }

  private boolean doublesEqual(
      double x,
      double y,
      Optional<Correspondence<Number, Number>> correspondence
      ) {
    if (correspondence.isPresent()) {
      return correspondence.get().compare(x, y);
    } else {
      return Double.compare(x, y) == 0;
    }
  }

  private boolean floatsEqual(
      float x,
      float y,
      Optional<Correspondence<Number, Number>> correspondence
      ) {
    if (correspondence.isPresent()) {
      return correspondence.get().compare(x, y);
    } else {
      return Float.compare(x, y) == 0;
    }
  }

  private UnknownFieldSetDiff diffUnknowns(
      UnknownFieldSet actual, UnknownFieldSet expected, FluentEqualityConfig config) {
    UnknownFieldSetDiff.Builder builder = UnknownFieldSetDiff.newBuilder();

    Map<Integer, UnknownFieldSet.Field> actualFields = actual.asMap();
    Map<Integer, UnknownFieldSet.Field> expectedFields = expected.asMap();
    for (int fieldNumber : Sets.union(actualFields.keySet(), expectedFields.keySet())) {
      UnknownFieldSet.Field actualField = actualFields.get(fieldNumber);
      UnknownFieldSet.Field expectedField = expectedFields.get(fieldNumber);
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
        SubScopeId subScopeId = SubScopeId.of(unknownFieldDescriptor);
        FieldScopeResult compareFields =
            config.compareFieldsScope().policyFor(rootDescriptor, subScopeId);
        if (compareFields == FieldScopeResult.EXCLUDED_RECURSIVELY) {
          builder.addSingularField(
              fieldNumber, SingularField.ignored(name(unknownFieldDescriptor)));
          continue;
        }

        builder.addAllSingularFields(
            fieldNumber,
            compareUnknownFieldList(
                actualValues,
                expectedValues,
                compareFields == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                unknownFieldDescriptor,
                config.subScope(rootDescriptor, subScopeId)));
      }
    }

    return builder.build();
  }

  private List<SingularField> compareUnknownFieldList(
      List<?> actualValues,
      List<?> expectedValues,
      boolean excludeNonRecursive,
      UnknownFieldDescriptor unknownFieldDescriptor,
      FluentEqualityConfig config) {
    int maxSize = Math.max(actualValues.size(), expectedValues.size());
    ImmutableList.Builder<SingularField> builder = ImmutableList.builderWithExpectedSize(maxSize);
    for (int i = 0; i < maxSize; i++) {
      @Nullable Object actual = actualValues.size() > i ? actualValues.get(i) : null;
      @Nullable Object expected = expectedValues.size() > i ? expectedValues.get(i) : null;
      builder.add(
          compareUnknownFieldValue(
              actual,
              expected,
              excludeNonRecursive,
              unknownFieldDescriptor,
              indexedName(unknownFieldDescriptor, i),
              config));
    }

    return builder.build();
  }

  private SingularField compareUnknownFieldValue(
      @Nullable Object actual,
      @Nullable Object expected,
      boolean excludeNonRecursive,
      UnknownFieldDescriptor unknownFieldDescriptor,
      String fieldName,
      FluentEqualityConfig config) {
    if (unknownFieldDescriptor.type() == UnknownFieldDescriptor.Type.GROUP) {
      return compareUnknownFieldSet(
          (UnknownFieldSet) actual,
          (UnknownFieldSet) expected,
          excludeNonRecursive,
          unknownFieldDescriptor,
          fieldName,
          config);
    } else {
      checkState(!excludeNonRecursive, "excludeNonRecursive is not a valid for primitives.");
      return compareUnknownPrimitive(actual, expected, unknownFieldDescriptor, fieldName);
    }
  }

  private SingularField compareUnknownFieldSet(
      @Nullable UnknownFieldSet actual,
      @Nullable UnknownFieldSet expected,
      boolean excludeNonRecursive,
      UnknownFieldDescriptor unknownFieldDescriptor,
      String fieldName,
      FluentEqualityConfig config) {
    Result.Builder result = Result.builder();

    // If actual or expected is missing, we know the result as long as it's not ignored.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    // Perform the detailed breakdown only if necessary.
    @Nullable UnknownFieldSetDiff unknownsBreakdown = null;
    if (result.build() == Result.MATCHED || excludeNonRecursive) {
      actual = firstNonNull(actual, UnknownFieldSet.getDefaultInstance());
      expected = firstNonNull(expected, UnknownFieldSet.getDefaultInstance());

      unknownsBreakdown = diffUnknowns(actual, expected, config);
      if (unknownsBreakdown.isIgnored() && excludeNonRecursive) {
        // Ignore this field entirely, report nothing.
        return SingularField.ignored(fieldName);
      }
      result.markModifiedIf(!unknownsBreakdown.isMatched());
    }

    // Report the full breakdown.
    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder()
            .setSubScopeId(SubScopeId.of(unknownFieldDescriptor))
            .setFieldName(fieldName)
            .setResult(result.build())
            .setProtoPrinter(protoPrinter);
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
      @Nullable Object actual,
      @Nullable Object expected,
      UnknownFieldDescriptor unknownFieldDescriptor,
      String fieldName) {
    Result.Builder result = Result.builder();

    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);
    result.markModifiedIf(!Objects.equal(actual, expected));

    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder()
            .setSubScopeId(SubScopeId.of(unknownFieldDescriptor))
            .setFieldName(fieldName)
            .setResult(result.build())
            .setProtoPrinter(protoPrinter);
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

  private static String name(UnknownFieldDescriptor unknownFieldDescriptor) {
    return String.valueOf(unknownFieldDescriptor.fieldNumber());
  }

  private static String indexedName(
      FieldDescriptor fieldDescriptor, Object key, FieldDescriptor keyFieldDescriptor) {
    StringBuilder sb = new StringBuilder();
    try {
      TextFormat.printFieldValue(keyFieldDescriptor, key, sb);
    } catch (IOException impossible) {
      throw new AssertionError(impossible);
    }
    return name(fieldDescriptor) + "[" + sb + "]";
  }

  private static String indexedName(FieldDescriptor fieldDescriptor, int index) {
    return name(fieldDescriptor) + "[" + index + "]";
  }

  private static String indexedName(UnknownFieldDescriptor unknownFieldDescriptor, int index) {
    return name(unknownFieldDescriptor) + "[" + index + "]";
  }
}
