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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.extensions.proto.MessageDifferencer.ReportType;
import com.google.common.truth.extensions.proto.MessageDifferencer.SpecificField;
import com.google.common.truth.extensions.proto.MessageDifferencer.StreamReporter;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Truth subject for the full version of Protocol Buffers.
 *
 * <p>{@code ProtoTruth.assertThat(actual).isEqualTo(expected)} performs the same assertion as
 * {@code Truth.assertThat(actual).isEqualTo(expected)}, but with a better failure message. By
 * default, the assertions are strict with respect to repeated field order, missing fields, etc.
 * This behavior can be changed with the configuration methods on this subject, e.g. {@code
 * ProtoTruth.assertThat(actual).ignoringRepeatedFieldOrder().isEqualTo(expected)}.
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

  protected ProtoSubject(FailureMetadata failureMetadata, @Nullable M message) {
    this(failureMetadata, FluentEqualityConfig.defaultInstance(), message);
  }

  /**
   * @deprecated Switch your {@code Subject} from accepting {@link FailureStrategy} (and exposing a
   *     {@link SubjectFactory}) to accepting a {@link FailureMetadata} (and exposing a {@link
   *     Subject.Factory}), at which point you'll call the {@code FailureMetadata} overload of this
   *     constructor instead.
   */
  @Deprecated
  protected ProtoSubject(FailureStrategy failureStrategy, @Nullable M message) {
    super(failureStrategy, message);
    this.config = FluentEqualityConfig.defaultInstance();
  }

  ProtoSubject(FailureMetadata failureMetadata, FluentEqualityConfig config, @Nullable M message) {
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

  @Override
  public void isEqualTo(@Nullable Object expected) {
    if (getSubject() == null
        || expected == null
        || getSubject().getClass() != expected.getClass()) {
      super.isEqualTo(expected);
    } else {
      Reporter reporter = new Reporter();
      if (!makeDifferencer().compare((Message) expected, getSubject(), reporter)) {
        reporter.failEqual((Message) expected);
      }
    }
  }

  /**
   * Same as {@link #isEqualTo(Object)}, except it returns true on success and false on failure
   * without throwing any exceptions.
   */
  boolean testIsEqualTo(@Nullable Object expected) {
    if (getSubject() == null || expected == null) {
      return getSubject() == expected; // Only true if both null.
    } else if (getSubject().getClass() != expected.getClass()) {
      return false;
    } else {
      return makeDifferencer().compare((Message) expected, getSubject(), null);
    }
  }

  @Override
  public void isNotEqualTo(@Nullable Object expected) {
    if (getSubject() == null
        || expected == null
        || getSubject().getClass() != expected.getClass()) {
      super.isNotEqualTo(expected);
    } else {
      Reporter reporter = new Reporter();
      if (makeDifferencer().compare((Message) expected, getSubject(), reporter)) {
        reporter.failNotEqual((Message) expected);
      }
    }
  }

  @Override
  public void hasAllRequiredFields() {
    if (!getSubject().isInitialized()) {
      failWithRawMessage(
          "Not true that %s has all required fields set. Missing: %s",
          getTrimmedDisplaySubject(), getSubject().findInitializationErrors());
    }
  }

  private MessageDifferencer makeDifferencer() {
    return config.toMessageDifferencer(getSubject().getDescriptorForType());
  }

  /**
   * {@link MessageDifferencer.Reporter} implementation for reporting the results of {@link
   * #isEqualTo(Object)}.
   */
  private class Reporter implements MessageDifferencer.Reporter {
    private final List<ReporterRecord> records = new ArrayList<ReporterRecord>();
    private boolean anyFailures = false;
    private boolean anyNotices = false;

    @Override
    public void report(
        ReportType type, Message message1, Message message2, ImmutableList<SpecificField> path) {
      ReporterRecord record = ReporterRecord.of(type, message1, message2, path);
      anyFailures |= record.isFailure();
      anyNotices |= !record.isFailure();
      records.add(record);
    }

    void failEqual(Message expected) {
      StringBuilder rawMessage = new StringBuilder();
      rawMessage.append("Not true that ");
      if (ProtoSubject.this.internalCustomName() != null) {
        rawMessage.append(ProtoSubject.this.internalCustomName()).append(" compares equal. ");
      } else {
        rawMessage.append("messages compare equal. ");
      }

      StreamReporter streamReporter = new StreamReporter(rawMessage);
      if (anyFailures) {
        rawMessage.append("Differences were found:\n");
        for (ReporterRecord record : records) {
          if (record.isFailure()) {
            streamReporter.report(
                record.type(), record.message1(), record.message2(), record.path());
          }
        }

        if (anyNotices && !ProtoSubject.this.config.reportMismatchesOnly()) {
          // Append the full report.
          rawMessage.append("\nFull diff:\n");
          for (ReporterRecord record : records) {
            streamReporter.report(
                record.type(), record.message1(), record.message2(), record.path());
          }
        }
      } else {
        rawMessage.append("No differences were reported.");
        if (!ProtoSubject.this.config.reportMismatchesOnly()) {
          if (anyNotices) {
            rawMessage.append("\nFull diff:\n");
            for (ReporterRecord record : records) {
              streamReporter.report(
                  record.type(), record.message1(), record.message2(), record.path());
            }
          } else {
            // Shouldn't really happen, but it's better to print something than nothing if it does.
            rawMessage.append("\nActual:\n");
            rawMessage.append(TextFormat.printToString(getSubject()));
            rawMessage.append("Expected:\n");
            rawMessage.append(TextFormat.printToString(expected));
          }
        }
      }

      ProtoSubject.this.failWithRawMessage(rawMessage.toString());
    }

    void failNotEqual(Message expected) {
      StringBuilder rawMessage = new StringBuilder();
      rawMessage.append("Not true that ");
      if (ProtoSubject.this.internalCustomName() != null) {
        rawMessage.append(ProtoSubject.this.internalCustomName()).append(" compares not equal. ");
      } else {
        rawMessage.append("messages compare not equal. ");
      }

      if (!records.isEmpty() && !ProtoSubject.this.config.reportMismatchesOnly()) {
        rawMessage.append("Only ignorable differences were found:\n");
        StreamReporter streamReporter = new StreamReporter(rawMessage);
        for (ReporterRecord record : records) {
          streamReporter.report(record.type(), record.message1(), record.message2(), record.path());
        }
      } else {
        rawMessage.append("No differences were found.");
        if (!ProtoSubject.this.config.reportMismatchesOnly()) {
          rawMessage.append("\nActual:\n");
          rawMessage.append(TextFormat.printToString(getSubject()));
          rawMessage.append("Expected:\n");
          rawMessage.append(TextFormat.printToString(expected));
        }
      }

      ProtoSubject.this.failWithRawMessage(rawMessage.toString());
    }
  }

  @AutoValue
  abstract static class ReporterRecord {
    abstract ReportType type();

    abstract Message message1();

    abstract Message message2();

    abstract ImmutableList<SpecificField> path();

    // Whether this ReporterRecord indicates an actionable message difference.
    final boolean isFailure() {
      return type() == ReportType.ADDED
          || type() == ReportType.DELETED
          || type() == ReportType.MODIFIED;
    }

    static ReporterRecord of(
        ReportType type, Message message1, Message message2, ImmutableList<SpecificField> path) {
      return new AutoValue_ProtoSubject_ReporterRecord(type, message1, message2, path);
    }
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

    MessageSubject(FailureMetadata failureMetadata, @Nullable Message message) {
      super(failureMetadata, message);
    }

    private MessageSubject(
        FailureMetadata failureMetadata, FluentEqualityConfig config, @Nullable Message message) {
      super(failureMetadata, config, message);
    }
  }
}
