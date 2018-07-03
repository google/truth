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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.truth.Correspondence;
import com.google.common.truth.extensions.proto.DiffResult.RepeatedField;
import com.google.common.truth.extensions.proto.DiffResult.SingularField;
import com.google.common.truth.extensions.proto.DiffResult.UnknownFieldSetDiff;
import com.google.common.truth.extensions.proto.RecursableDiffEntity.WithResultCode.Result;
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
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

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

  private ProtoTruthMessageDifferencer(FluentEqualityConfig rootConfig, Descriptor descriptor) {
    rootConfig.validate(descriptor);

    this.rootConfig = rootConfig;
    this.rootDescriptor = descriptor;
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
      FieldDescriptorOrUnknown fieldDescriptorOrUnknown =
          FieldDescriptorOrUnknown.fromFieldDescriptor(fieldDescriptor);
      FieldScopeResult shouldCompare =
          config.compareFieldsScope().policyFor(rootDescriptor, fieldDescriptorOrUnknown);
      if (shouldCompare == FieldScopeResult.EXCLUDED_RECURSIVELY) {
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
                  actualMap,
                  expectedMap,
                  keyOrder,
                  fieldDescriptor,
                  config.subScope(rootDescriptor, fieldDescriptorOrUnknown)));
        } else {
          List<?> actualList = toProtoList(actualFields.get(fieldDescriptor));
          List<?> expectedList = toProtoList(expectedFields.get(fieldDescriptor));

          boolean ignoreRepeatedFieldOrder =
              config
                  .ignoreRepeatedFieldOrderScope()
                  .contains(rootDescriptor, fieldDescriptorOrUnknown);
          boolean ignoreExtraRepeatedFieldElements =
              config
                  .ignoreExtraRepeatedFieldElementsScope()
                  .contains(rootDescriptor, fieldDescriptorOrUnknown);
          if (ignoreRepeatedFieldOrder) {
            builder.addRepeatedField(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldIgnoringOrder(
                    actualList,
                    expectedList,
                    shouldCompare == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                    fieldDescriptor,
                    ignoreExtraRepeatedFieldElements,
                    config.subScope(rootDescriptor, fieldDescriptorOrUnknown)));
          } else if (ignoreExtraRepeatedFieldElements && !expectedList.isEmpty()) {
            builder.addRepeatedField(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldExpectingSubsequence(
                    actualList,
                    expectedList,
                    shouldCompare == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                    fieldDescriptor,
                    config.subScope(rootDescriptor, fieldDescriptorOrUnknown)));
          } else {
            builder.addAllSingularFields(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldByIndices(
                    actualList,
                    expectedList,
                    shouldCompare == FieldScopeResult.EXCLUDED_NONRECURSIVELY,
                    fieldDescriptor,
                    config.subScope(rootDescriptor, fieldDescriptorOrUnknown)));
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
                config.subScope(rootDescriptor, fieldDescriptorOrUnknown)));
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

  // Helper which takes a proto map in List<Message> form, and converts it to a Map<Object, Object>
  // by extracting the keys and values from the generated map-entry submessages.  Returns an empty
  // map if null is passed in.
  private static Map<Object, Object> toProtoMap(@NullableDecl Object container) {
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
  private static List<?> toProtoList(@NullableDecl Object container) {
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
      FluentEqualityConfig mapConfig) {
    FieldDescriptor keyFieldDescriptor = mapFieldDescriptor.getMessageType().findFieldByNumber(1);
    FieldDescriptor valueFieldDescriptor = mapFieldDescriptor.getMessageType().findFieldByNumber(2);
    FieldDescriptorOrUnknown valueFieldDescriptorOrUnknown =
        FieldDescriptorOrUnknown.fromFieldDescriptor(valueFieldDescriptor);

    // We never ignore the key, no matter what the logic dictates.
    FieldScopeResult compareValues =
        mapConfig.compareFieldsScope().policyFor(rootDescriptor, valueFieldDescriptorOrUnknown);
    if (compareValues == FieldScopeResult.EXCLUDED_RECURSIVELY) {
      return ImmutableList.of(SingularField.ignored(name(mapFieldDescriptor)));
    }

    boolean ignoreExtraRepeatedFieldElements =
        mapConfig
            .ignoreExtraRepeatedFieldElementsScope()
            .contains(
                rootDescriptor, FieldDescriptorOrUnknown.fromFieldDescriptor(mapFieldDescriptor));

    FluentEqualityConfig valuesConfig =
        mapConfig.subScope(rootDescriptor, valueFieldDescriptorOrUnknown);

    ImmutableList.Builder<SingularField> builder =
        ImmutableList.builderWithExpectedSize(keyOrder.size());
    for (Object key : keyOrder) {
      @NullableDecl Object actualValue = actualMap.get(key);
      @NullableDecl Object expectedValue = expectedMap.get(key);
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
    // This mostly replicates the algorithm used by IterableSubject.containsAll().inOrder(), but
    // with some tweaks for fuzzy equality and structured output.
    Deque<Integer> actualIndices = new ArrayDeque<>();
    for (int i = 0; i < actualList.size(); i++) {
      actualIndices.addLast(i);
    }
    Deque<Integer> actualNotInOrder = new ArrayDeque<>();

    for (int expectedIndex = 0; expectedIndex < expectedList.size(); expectedIndex++) {
      Object expected = expectedList.get(expectedIndex);

      // Find the first actual element which matches.
      @NullableDecl
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
              .build());
    }

    return builder.build();
  }

  // Given a list of values, a list of indexes into that list, and an expected value, find the first
  // actual value that compares equal to the expected value, and return the PairResult for it.
  // Also removes the index for the matching value from actualIndicies.
  //
  // If there is no match, returns null.
  @NullableDecl
  private RepeatedField.PairResult findMatchingPairResult(
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
      @NullableDecl Object actual,
      @NullableDecl Object expected,
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      @NullableDecl Integer actualFieldIndex,
      @NullableDecl Integer expectedFieldIndex,
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
            .setFieldDescriptor(fieldDescriptor);
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
      @NullableDecl Object actual = actualList.size() > i ? actualList.get(i) : null;
      @NullableDecl Object expected = expectedList.size() > i ? expectedList.get(i) : null;
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
      @NullableDecl Object actual,
      @NullableDecl Object expected,
      @NullableDecl Object defaultValue,
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
      @NullableDecl T input, @NullableDecl T defaultValue, boolean ignoreFieldAbsence) {
    return (input == null && ignoreFieldAbsence) ? defaultValue : input;
  }

  // Returns 'input' if it's non-null, otherwise the default instance of 'other'.
  // Requires at least one parameter is non-null.
  private static Message orDefaultForType(
      @NullableDecl Message input, @NullableDecl Message other) {
    return (input != null) ? input : other.getDefaultInstanceForType();
  }

  private SingularField compareSingularMessage(
      @NullableDecl Message actual,
      @NullableDecl Message expected,
      @NullableDecl Message defaultValue,
      boolean excludeNonRecursive,
      FieldDescriptor fieldDescriptor,
      String fieldName,
      FluentEqualityConfig config) {
    Result.Builder result = Result.builder();

    // Use the default if it's set and we're ignoring field absence.
    boolean ignoreFieldAbsence =
        config
            .ignoreFieldAbsenceScope()
            .contains(
                rootDescriptor, FieldDescriptorOrUnknown.fromFieldDescriptor(fieldDescriptor));
    actual = orIfIgnoringFieldAbsence(actual, defaultValue, ignoreFieldAbsence);
    expected = orIfIgnoringFieldAbsence(expected, defaultValue, ignoreFieldAbsence);

    // If actual or expected is missing here, we know our result so long as it's not ignored.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    // Perform the detailed breakdown only if necessary.
    @NullableDecl DiffResult breakdown = null;
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
            .setFieldDescriptorOrUnknown(
                FieldDescriptorOrUnknown.fromFieldDescriptor(fieldDescriptor))
            .setFieldName(fieldName)
            .setResult(result.build());
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
      @NullableDecl Object actual,
      @NullableDecl Object expected,
      @NullableDecl Object defaultValue,
      FieldDescriptor fieldDescriptor,
      String fieldName,
      FluentEqualityConfig config) {
    Result.Builder result = Result.builder();

    // Use the default if it's set and we're ignoring field absence.
    FieldDescriptorOrUnknown fieldDescriptorOrUnknown =
        FieldDescriptorOrUnknown.fromFieldDescriptor(fieldDescriptor);
    boolean ignoreFieldAbsence =
        config.ignoreFieldAbsenceScope().contains(rootDescriptor, fieldDescriptorOrUnknown);
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
                config.doubleCorrespondenceMap().get(rootDescriptor, fieldDescriptorOrUnknown)
                ));
      } else if (actual instanceof Float) {
        result.markModifiedIf(
            !floatsEqual(
                (float) actual,
                (float) expected,
                config.floatCorrespondenceMap().get(rootDescriptor, fieldDescriptorOrUnknown)
                ));
      } else {
        result.markModifiedIf(!Objects.equal(actual, expected));
      }
    }

    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder()
            .setFieldDescriptorOrUnknown(
                FieldDescriptorOrUnknown.fromFieldDescriptor(fieldDescriptor))
            .setFieldName(fieldName)
            .setResult(result.build());
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
      @NullableDecl UnknownFieldSet.Field actualField = actualFields.get(fieldNumber);
      @NullableDecl UnknownFieldSet.Field expectedField = expectedFields.get(fieldNumber);
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
            FieldDescriptorOrUnknown.fromUnknown(unknownFieldDescriptor);
        FieldScopeResult compareFields =
            config.compareFieldsScope().policyFor(rootDescriptor, fieldDescriptorOrUnknown);
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
                config.subScope(rootDescriptor, fieldDescriptorOrUnknown)));
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
      @NullableDecl Object actual = actualValues.size() > i ? actualValues.get(i) : null;
      @NullableDecl Object expected = expectedValues.size() > i ? expectedValues.get(i) : null;
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
      @NullableDecl Object actual,
      @NullableDecl Object expected,
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
      @NullableDecl UnknownFieldSet actual,
      @NullableDecl UnknownFieldSet expected,
      boolean excludeNonRecursive,
      UnknownFieldDescriptor unknownFieldDescriptor,
      String fieldName,
      FluentEqualityConfig config) {
    Result.Builder result = Result.builder();

    // If actual or expected is missing, we know the result as long as it's not ignored.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    // Perform the detailed breakdown only if necessary.
    @NullableDecl UnknownFieldSetDiff unknownsBreakdown = null;
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
            .setFieldDescriptorOrUnknown(
                FieldDescriptorOrUnknown.fromUnknown(unknownFieldDescriptor))
            .setFieldName(fieldName)
            .setResult(result.build());
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
      @NullableDecl Object actual,
      @NullableDecl Object expected,
      UnknownFieldDescriptor unknownFieldDescriptor,
      String fieldName) {
    Result.Builder result = Result.builder();

    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);
    result.markModifiedIf(!Objects.equal(actual, expected));

    SingularField.Builder singularFieldBuilder =
        SingularField.newBuilder()
            .setFieldDescriptorOrUnknown(
                FieldDescriptorOrUnknown.fromUnknown(unknownFieldDescriptor))
            .setFieldName(fieldName)
            .setResult(result.build());
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
