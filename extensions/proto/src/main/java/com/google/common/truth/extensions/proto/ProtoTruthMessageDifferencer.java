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

  /**
   * Whether or not a sub-message tree should be ignored.
   *
   * <p>This enables {@link FieldScopeLogic}s and the {@code ProtoTruthMessageDifferencer} to work
   * together on traversing a message, instead of either class doing redundant work. The need for
   * {@code NONRECURSIVE} arises from sub-messages. For example:
   *
   * <p><code>
   *   message Foo {
   *     optional Bar bar = 1;
   *   }
   *
   *   message Bar {
   *     optional Baz baz = 1;
   *   }
   *
   *   message Baz {
   *     optional string name = 1;
   *     optional int64 id = 2;
   *   }
   * </code>
   *
   * <p>A {@link FieldScopeLogic} which ignores everything except 'Baz.name', when asked if
   * 'Foo.bar' should be ignored, cannot know whether it should be ignored or not without scanning
   * all of 'Foo.bar' for Baz submessages, and whether they have the name field set. We could scan
   * the entire message to make this decision, but the message differencer will be scanning anyway
   * if we choose not to ignore it, which creates redundant work. {@code NONRECURSIVEMAYBE} is the
   * solution to this problem: The logic defers the decision back to the message differencer, which
   * proceeds with the complete scan of 'Foo.bar', and ignores the entire submessage if and only if
   * nothing in 'Foo.bar' was determined to be un-ignorable.
   */
  enum ShouldIgnore {
    /** This field should be ignored, but children might not be ignorable. */
    YES_NONRECURSIVE(true, false),
    /** This field and all its children should be ignored. */
    YES_RECURSIVE(true, true),
    /** This field should not be ignored, but children might be ignorable. */
    NO_NONRECURSIVE(false, false),
    /** This field and all its children should not be ignored. */
    NO_RECURSIVE(false, true);

    public static ShouldIgnore of(boolean shouldIgnore, boolean recursive) {
      if (shouldIgnore) {
        return recursive ? YES_RECURSIVE : YES_NONRECURSIVE;
      } else {
        return recursive ? NO_RECURSIVE : NO_NONRECURSIVE;
      }
    }

    private final boolean shouldIgnore;
    private final boolean recursive;

    ShouldIgnore(boolean shouldIgnore, boolean recursive) {
      this.shouldIgnore = shouldIgnore;
      this.recursive = recursive;
    }

    /** Whether this field should be ignored or not. */
    boolean shouldIgnore() {
      return shouldIgnore;
    }

    /**
     * Whether this field's sub-children should also be unilaterally ignored or not-ignored,
     * conditional on {@link #shouldIgnore()}.
     */
    boolean recursive() {
      return recursive;
    }

    boolean shouldIgnoreNonRecursive() {
      return this == YES_NONRECURSIVE;
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
          FieldDescriptorOrUnknown.fromFieldDescriptor(fieldDescriptor);
      ShouldIgnore shouldIgnore =
          fieldScopeLogic.shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown);
      if (shouldIgnore == ShouldIgnore.YES_RECURSIVE) {
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
                    shouldIgnore.shouldIgnoreNonRecursive(),
                    fieldDescriptor,
                    fieldScopeLogic.subLogic(rootDescriptor, fieldDescriptorOrUnknown)));
          } else if (config.ignoreExtraRepeatedFieldElements() && !expectedList.isEmpty()) {
            builder.addRepeatedField(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldExpectingSubsequence(
                    actualList,
                    expectedList,
                    shouldIgnore.shouldIgnoreNonRecursive(),
                    fieldDescriptor,
                    fieldScopeLogic.subLogic(rootDescriptor, fieldDescriptorOrUnknown)));
          } else {
            builder.addAllSingularFields(
                fieldDescriptor.getNumber(),
                compareRepeatedFieldByIndices(
                    actualList,
                    expectedList,
                    shouldIgnore.shouldIgnoreNonRecursive(),
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
                shouldIgnore.shouldIgnoreNonRecursive(),
                fieldDescriptor,
                name(fieldDescriptor),
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
      FieldScopeLogic mapFieldScopeLogic) {
    FieldDescriptor keyFieldDescriptor = mapFieldDescriptor.getMessageType().findFieldByNumber(1);
    FieldDescriptor valueFieldDescriptor = mapFieldDescriptor.getMessageType().findFieldByNumber(2);
    FieldDescriptorOrUnknown valueFieldDescriptorOrUnknown =
        FieldDescriptorOrUnknown.fromFieldDescriptor(valueFieldDescriptor);
    FieldScopeLogic valueFieldScopeLogic =
        mapFieldScopeLogic.subLogic(rootDescriptor, valueFieldDescriptorOrUnknown);

    // We never ignore the key, no matter what the logic dictates.
    ShouldIgnore shouldIgnoreValue =
        valueFieldScopeLogic.shouldIgnore(rootDescriptor, valueFieldDescriptorOrUnknown);
    if (shouldIgnoreValue == ShouldIgnore.YES_RECURSIVE) {
      return ImmutableList.of(SingularField.ignored(name(mapFieldDescriptor)));
    }

    ImmutableList.Builder<SingularField> builder =
        ImmutableList.builderWithExpectedSize(keyOrder.size());
    for (Object key : keyOrder) {
      @NullableDecl Object actualValue = actualMap.get(key);
      @NullableDecl Object expectedValue = expectedMap.get(key);
      if (config.ignoreExtraRepeatedFieldElements()
          && !expectedMap.isEmpty()
          && expectedValue == null) {
        builder.add(
            SingularField.ignored(indexedName(mapFieldDescriptor, key, keyFieldDescriptor)));
      } else {
        builder.add(
            compareSingularValue(
                actualValue,
                expectedValue,
                /*defaultValue=*/ null,
                shouldIgnoreValue.shouldIgnoreNonRecursive(),
                valueFieldDescriptor,
                indexedName(mapFieldDescriptor, key, keyFieldDescriptor),
                valueFieldScopeLogic));
      }
    }

    return builder.build();
  }

  private RepeatedField compareRepeatedFieldIgnoringOrder(
      List<?> actualList,
      List<?> expectedList,
      boolean shouldIgnoreNonRecursive,
      FieldDescriptor fieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
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
                actual, expected, shouldIgnoreNonRecursive, fieldDescriptor, i, j, fieldScopeLogic);
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
      if (config.ignoreExtraRepeatedFieldElements() && !expectedList.isEmpty()) {
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
                shouldIgnoreNonRecursive,
                fieldDescriptor,
                i,
                /*expectedFieldIndex=*/ null,
                fieldScopeLogic));
      }
    }
    for (int j : unmatchedExpected) {
      builder.addPairResult(
          compareRepeatedFieldElementPair(
              /*actual=*/ null,
              expectedList.get(j),
              shouldIgnoreNonRecursive,
              fieldDescriptor,
              /*actualFieldIndex=*/ null,
              j,
              fieldScopeLogic));
    }

    return builder.build();
  }

  private RepeatedField compareRepeatedFieldExpectingSubsequence(
      List<?> actualList,
      List<?> expectedList,
      boolean shouldIgnoreNonRecursive,
      FieldDescriptor fieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
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
              shouldIgnoreNonRecursive,
              fieldDescriptor,
              fieldScopeLogic);

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
                shouldIgnoreNonRecursive,
                fieldDescriptor,
                fieldScopeLogic);
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
      boolean shouldIgnoreNonRecursive,
      FieldDescriptor fieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
    Iterator<Integer> actualIndexIter = actualIndices.iterator();
    while (actualIndexIter.hasNext()) {
      int actualIndex = actualIndexIter.next();
      RepeatedField.PairResult pairResult =
          compareRepeatedFieldElementPair(
              actualValues.get(actualIndex),
              expectedValue,
              shouldIgnoreNonRecursive,
              fieldDescriptor,
              actualIndex,
              expectedIndex,
              fieldScopeLogic);
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
      boolean shouldIgnoreNonRecursive,
      FieldDescriptor fieldDescriptor,
      @NullableDecl Integer actualFieldIndex,
      @NullableDecl Integer expectedFieldIndex,
      FieldScopeLogic fieldScopeLogic) {
    SingularField comparison =
        compareSingularValue(
            actual,
            expected,
            /*defaultValue=*/ null,
            shouldIgnoreNonRecursive,
            fieldDescriptor,
            "<no field path>",
            fieldScopeLogic);

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
   * fieldDescriptor}. Uses {@code shouldIgnoreNonRecursive}, {@code parentFieldPath}, and {@code
   * fieldScopeLogic} to compare the messages.
   *
   * @return A list in index order, containing the diff results for each message.
   */
  private List<SingularField> compareRepeatedFieldByIndices(
      List<?> actualList,
      List<?> expectedList,
      boolean shouldIgnoreNonRecursive,
      FieldDescriptor fieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
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
              shouldIgnoreNonRecursive,
              fieldDescriptor,
              indexedName(fieldDescriptor, i),
              fieldScopeLogic));
    }

    return builder.build();
  }

  private SingularField compareSingularValue(
      @NullableDecl Object actual,
      @NullableDecl Object expected,
      @NullableDecl Object defaultValue,
      boolean shouldIgnoreNonRecursive,
      FieldDescriptor fieldDescriptor,
      String fieldName,
      FieldScopeLogic fieldScopeLogic) {
    if (fieldDescriptor.getJavaType() == JavaType.MESSAGE) {
      return compareSingularMessage(
          (Message) actual,
          (Message) expected,
          (Message) defaultValue,
          shouldIgnoreNonRecursive,
          fieldDescriptor,
          fieldName,
          fieldScopeLogic);
    } else if (shouldIgnoreNonRecursive) {
      return SingularField.ignored(fieldName);
    } else {
      return compareSingularPrimitive(actual, expected, defaultValue, fieldDescriptor, fieldName);
    }
  }

  // Replaces 'input' with 'defaultValue' iff input is null and we're ignoring field absence.
  // Otherwise, just returns the input.
  private <T> T orIfIgnoringFieldAbsence(@NullableDecl T input, @NullableDecl T defaultValue) {
    return (input == null && config.ignoreFieldAbsence()) ? defaultValue : input;
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
      boolean shouldIgnoreNonRecursive,
      FieldDescriptor fieldDescriptor,
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
    @NullableDecl DiffResult breakdown = null;
    if (result.build() == Result.MATCHED || shouldIgnoreNonRecursive) {
      actual = orDefaultForType(actual, expected);
      expected = orDefaultForType(expected, actual);

      breakdown = diffMessages(actual, expected, fieldScopeLogic);
      if (breakdown.isIgnored() && shouldIgnoreNonRecursive) {
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
      String fieldName) {
    Result.Builder result = Result.builder();

    // Use the default if it's set and we're ignoring field absence.
    actual = orIfIgnoringFieldAbsence(actual, defaultValue);
    expected = orIfIgnoringFieldAbsence(expected, defaultValue);

    // If actual or expected is missing here, we know our result.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    if (actual != null && expected != null) {
      if (actual instanceof Double) {
        result.markModifiedIf(!doublesEqual((double) actual, (double) expected));
      } else if (actual instanceof Float) {
        result.markModifiedIf(!floatsEqual((float) actual, (float) expected));
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

  private boolean doublesEqual(double x, double y) {
    if (config.doubleCorrespondence().isPresent()) {
      return config.doubleCorrespondence().get().compare(x, y);
    } else {
      return Double.compare(x, y) == 0;
    }
  }

  private boolean floatsEqual(float x, float y) {
    if (config.floatCorrespondence().isPresent()) {
      return config.floatCorrespondence().get().compare(x, y);
    } else {
      return Float.compare(x, y) == 0;
    }
  }

  private UnknownFieldSetDiff diffUnknowns(
      UnknownFieldSet actual, UnknownFieldSet expected, FieldScopeLogic fieldScopeLogic) {
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
        ShouldIgnore shouldIgnore =
            fieldScopeLogic.shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown);
        if (shouldIgnore == ShouldIgnore.YES_NONRECURSIVE) {
          builder.addSingularField(
              fieldNumber, SingularField.ignored(name(unknownFieldDescriptor)));
          continue;
        }

        builder.addAllSingularFields(
            fieldNumber,
            compareUnknownFieldList(
                actualValues,
                expectedValues,
                shouldIgnore.shouldIgnoreNonRecursive(),
                unknownFieldDescriptor,
                fieldScopeLogic.subLogic(rootDescriptor, fieldDescriptorOrUnknown)));
      }
    }

    return builder.build();
  }

  private List<SingularField> compareUnknownFieldList(
      List<?> actualValues,
      List<?> expectedValues,
      boolean shouldIgnoreNonRecursive,
      UnknownFieldDescriptor unknownFieldDescriptor,
      FieldScopeLogic fieldScopeLogic) {
    int maxSize = Math.max(actualValues.size(), expectedValues.size());
    ImmutableList.Builder<SingularField> builder = ImmutableList.builderWithExpectedSize(maxSize);
    for (int i = 0; i < maxSize; i++) {
      @NullableDecl Object actual = actualValues.size() > i ? actualValues.get(i) : null;
      @NullableDecl Object expected = expectedValues.size() > i ? expectedValues.get(i) : null;
      builder.add(
          compareUnknownFieldValue(
              actual,
              expected,
              shouldIgnoreNonRecursive,
              unknownFieldDescriptor,
              indexedName(unknownFieldDescriptor, i),
              fieldScopeLogic));
    }

    return builder.build();
  }

  private SingularField compareUnknownFieldValue(
      @NullableDecl Object actual,
      @NullableDecl Object expected,
      boolean shouldIgnoreNonRecursive,
      UnknownFieldDescriptor unknownFieldDescriptor,
      String fieldName,
      FieldScopeLogic fieldScopeLogic) {
    if (unknownFieldDescriptor.type() == UnknownFieldDescriptor.Type.GROUP) {
      return compareUnknownFieldSet(
          (UnknownFieldSet) actual,
          (UnknownFieldSet) expected,
          shouldIgnoreNonRecursive,
          unknownFieldDescriptor,
          fieldName,
          fieldScopeLogic);
    } else {
      checkState(
          !shouldIgnoreNonRecursive, "shouldIgnoreNonRecursive is not a valid for primitives.");
      return compareUnknownPrimitive(actual, expected, unknownFieldDescriptor, fieldName);
    }
  }

  private SingularField compareUnknownFieldSet(
      @NullableDecl UnknownFieldSet actual,
      @NullableDecl UnknownFieldSet expected,
      boolean shouldIgnoreNonRecursive,
      UnknownFieldDescriptor unknownFieldDescriptor,
      String fieldName,
      FieldScopeLogic fieldScopeLogic) {
    Result.Builder result = Result.builder();

    // If actual or expected is missing, we know the result as long as it's not ignored.
    result.markRemovedIf(actual == null);
    result.markAddedIf(expected == null);

    // Perform the detailed breakdown only if necessary.
    @NullableDecl UnknownFieldSetDiff unknownsBreakdown = null;
    if (result.build() == Result.MATCHED || shouldIgnoreNonRecursive) {
      actual = firstNonNull(actual, UnknownFieldSet.getDefaultInstance());
      expected = firstNonNull(expected, UnknownFieldSet.getDefaultInstance());

      unknownsBreakdown = diffUnknowns(actual, expected, fieldScopeLogic);
      if (unknownsBreakdown.isIgnored() && shouldIgnoreNonRecursive) {
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
