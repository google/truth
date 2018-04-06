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

import com.google.common.base.Objects;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.Arrays;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Truth subject for the full version of Protocol Buffers.
 *
 * <p>{@code ProtoTruth.assertThat(actual).isEqualTo(expected)} performs the same assertion as
 * {@code Truth.assertThat(actual).isEqualTo(expected)}, but with a better failure message. By
 * default, the assertions are strict with respect to repeated field order, missing fields, etc.
 * This behavior can be changed with the configuration methods on this subject, e.g. {@code
 * ProtoTruth.assertThat(actual).ignoringRepeatedFieldOrder().isEqualTo(expected)}.
 *
 * <p>Floating-point fields are compared using exact equality, which is <a
 * href="http://google.github.io/truth/floating_point">probably not what you want</a> if the values
 * are the results of some arithmetic. Support for approximate equality may be added in a later
 * version.
 *
 * <p>Equality tests, and other methods, may yield slightly different behavior for versions 2 and 3
 * of Protocol Buffers. If testing protos of multiple versions, make sure you understand the
 * behaviors of default and unknown fields so you don't under or over test.
 *
 * @param <S> subject class type.
 * @param <M> message type.
 */
public class ProtoSubject<S extends ProtoSubject<S, M>, M extends Message>
    extends LiteProtoSubject<S, M> implements ProtoFluentAssertion {

  // TODO(user): Type this if we solve the typing assertAbout() problem for
  // IterableOfProtosSubject and there is use for such typing.
  private final FluentEqualityConfig config;

  protected ProtoSubject(FailureMetadata failureMetadata, @NullableDecl M message) {
    this(failureMetadata, FluentEqualityConfig.defaultInstance(), message);
  }

  ProtoSubject(
      FailureMetadata failureMetadata, FluentEqualityConfig config, @NullableDecl M message) {
    super(failureMetadata, message);
    this.config = config;
  }

  ProtoSubject<?, Message> usingConfig(FluentEqualityConfig newConfig) {
    MessageSubject newSubject = check().about(MessageSubject.messages(newConfig)).that(actual());
    if (internalCustomName() != null) {
      newSubject = newSubject.named(internalCustomName());
    }
    return newSubject;
  }

  @Override
  public ProtoFluentAssertion ignoringFieldAbsence() {
    return usingConfig(config.ignoringFieldAbsence());
  }

  @Override
  public ProtoFluentAssertion ignoringRepeatedFieldOrder() {
    return usingConfig(config.ignoringRepeatedFieldOrder());
  }

  @Override
  public ProtoFluentAssertion ignoringExtraRepeatedFieldElements() {
    return usingConfig(config.ignoringExtraRepeatedFieldElements());
  }

  @Override
  public ProtoFluentAssertion usingDoubleTolerance(double tolerance) {
    return usingConfig(config.usingDoubleTolerance(tolerance));
  }

  @Override
  public ProtoFluentAssertion usingFloatTolerance(float tolerance) {
    return usingConfig(config.usingFloatTolerance(tolerance));
  }

  @Override
  public ProtoFluentAssertion comparingExpectedFieldsOnly() {
    return usingConfig(config.comparingExpectedFieldsOnly());
  }

  @Override
  public ProtoFluentAssertion withPartialScope(FieldScope fieldScope) {
    return usingConfig(config.withPartialScope(checkNotNull(fieldScope, "fieldScope")));
  }

  @Override
  public ProtoFluentAssertion ignoringFields(int firstFieldNumber, int... rest) {
    return ignoringFields(asList(firstFieldNumber, rest));
  }

  @Override
  public ProtoFluentAssertion ignoringFields(Iterable<Integer> fieldNumbers) {
    return usingConfig(config.ignoringFields(fieldNumbers));
  }

  @Override
  public ProtoFluentAssertion ignoringFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return ignoringFieldDescriptors(asList(firstFieldDescriptor, rest));
  }

  @Override
  public ProtoFluentAssertion ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    return usingConfig(config.ignoringFieldDescriptors(fieldDescriptors));
  }

  @Override
  public ProtoFluentAssertion ignoringFieldScope(FieldScope fieldScope) {
    return usingConfig(config.ignoringFieldScope(checkNotNull(fieldScope, "fieldScope")));
  }

  @Override
  public ProtoFluentAssertion reportingMismatchesOnly() {
    return usingConfig(config.reportingMismatchesOnly());
  }

  private static boolean notMessagesWithSameDescriptor(
      @NullableDecl Message actual, @NullableDecl Object expected) {
    if (actual != null && expected instanceof Message) {
      return actual.getDescriptorForType() != ((Message) expected).getDescriptorForType();
    }
    return true;
  }

  @Override
  public void isEqualTo(@NullableDecl Object expected) {
    if (notMessagesWithSameDescriptor(actual(), expected)) {
      super.isEqualTo(expected);
    } else {
      DiffResult diffResult =
          makeDifferencer((Message) expected).diffMessages(actual(), (Message) expected);
      if (!diffResult.isMatched()) {
        failWithRawMessage(
            failureMessage(/* expectedEqual = */ true)
                + "\n"
                + diffResult.printToString(config.reportMismatchesOnly()));
      }
    }
  }

  /**
   * Same as {@link #isEqualTo(Object)}, except it returns true on success and false on failure
   * without throwing any exceptions.
   */
  boolean testIsEqualTo(@NullableDecl Object expected) {
    if (notMessagesWithSameDescriptor(actual(), expected)) {
      return Objects.equal(actual(), expected);
    } else {
      return makeDifferencer((Message) expected)
          .diffMessages(actual(), (Message) expected)
          .isMatched();
    }
  }

  @Override
  public void isNotEqualTo(@NullableDecl Object expected) {
    if (notMessagesWithSameDescriptor(actual(), expected)) {
      super.isNotEqualTo(expected);
    } else {
      DiffResult diffResult =
          makeDifferencer((Message) expected).diffMessages(actual(), (Message) expected);
      if (diffResult.isMatched()) {
        failWithRawMessage(
            failureMessage(/* expectedEqual= */ false)
                + "\n"
                + diffResult.printToString(config.reportMismatchesOnly()));
      }
    }
  }

  @Override
  public void hasAllRequiredFields() {
    if (!actual().isInitialized()) {
      failWithRawMessage(
          "Not true that %s has all required fields set. Missing: %s",
          actualAsString(), actual().findInitializationErrors());
    }
  }

  private ProtoTruthMessageDifferencer makeDifferencer(Message expected) {
    return config
        .withExpectedMessages(Arrays.asList(expected))
        .toMessageDifferencer(actual().getDescriptorForType());
  }

  private String failureMessage(boolean expectedEqual) {
    StringBuilder rawMessage = new StringBuilder();
    rawMessage.append("Not true that ");
    if (internalCustomName() != null) {
      rawMessage
          .append(internalCustomName())
          .append(" compares ")
          .append(expectedEqual ? "" : "not ")
          .append("equal. ");
    } else {
      rawMessage.append("messages compare ").append(expectedEqual ? "" : "not ").append("equal. ");
    }

    return rawMessage.toString();
  }

  static final class MessageSubject extends ProtoSubject<MessageSubject, Message> {
    static Subject.Factory<MessageSubject, Message> messages(final FluentEqualityConfig config) {
      return new Subject.Factory<MessageSubject, Message>() {
        @Override
        public MessageSubject createSubject(FailureMetadata failureMetadata, Message actual) {
          return new MessageSubject(failureMetadata, config, actual);
        }
      };
    }

    MessageSubject(FailureMetadata failureMetadata, @NullableDecl Message message) {
      super(failureMetadata, message);
    }

    private MessageSubject(
        FailureMetadata failureMetadata,
        FluentEqualityConfig config,
        @NullableDecl Message message) {
      super(failureMetadata, config, message);
    }
  }
}
