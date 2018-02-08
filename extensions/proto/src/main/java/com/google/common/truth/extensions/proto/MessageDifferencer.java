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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.truth.extensions.proto.MessageDifferencer.FieldComparator.ComparisonResult;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.WireFormat;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Static methods and classes for comparing Protocol Messages.
 * Port of C++ version in {@code //net/proto2/util/public/message_differencer.h}
 *
 * @author chrisn@google.com (Chris Nokleberg)
 */
@Immutable final class MessageDifferencer {

  /**
   * MapKeyComparator is used to determine if two elements have the same key
   * when comparing elements of a repeated field as a map.
   */
  public interface MapKeyComparator {
    /**
     * Decides whether the given messages match with respect to the keys of the
     * map entries they represent.
     *
     * @param parentFields the stack of SpecificFields corresponding to the proto
     * path to the given messages.
     */
    public boolean isMatch(
        MessageDifferencer messageDifferencer,
        Message message1,
        Message message2,
        List<SpecificField> parentFields);
  }

  private static class ProtoMapKeyComparator implements MapKeyComparator {
    @Override public boolean isMatch(
        MessageDifferencer messageDifferencer, Message message1, Message message2,
        List<SpecificField> parentFields) {
      FieldDescriptor keyField = message1.getDescriptorForType().findFieldByName("key");
      return messageDifferencer.compareFieldValueUsingParentFields(
          message1, message2,
          // -1 indices because there is no way to declare a map key as repeated.
          keyField, -1, -1,
          null, parentFields);
    }
  }
  private static final ProtoMapKeyComparator PROTO_MAP_KEY_COMPARATOR = new ProtoMapKeyComparator();

  /**
   * When comparing a repeated field as map, MultipleFieldMapKeyComparator can be used to specify
   * multiple fields as key for key comparison. Two elements of a repeated field will be regarded as
   * having the same key iff they have the same value for every specified key field.
   * Note that you can also specify only one field as key.
   */
  private static class MultipleFieldsMapKeyComparator implements MapKeyComparator {
    private final List<FieldDescriptor> keyFields;

    public MultipleFieldsMapKeyComparator(List<FieldDescriptor> key) {
      this.keyFields = key;
    }

    public MultipleFieldsMapKeyComparator(FieldDescriptor fieldDescriptor) {
      keyFields = new LinkedList<>();
      keyFields.add(fieldDescriptor);
    }

    @Override
    public boolean isMatch(
        MessageDifferencer messageDifferencer, Message message1, Message message2,
        List<SpecificField> parentFields) {
      for (int i = 0; i < keyFields.size(); ++i) {
        FieldDescriptor field = keyFields.get(i);
        if (field.isRepeated()) {
          if (!messageDifferencer.compareRepeatedField(
                  message1, message2, field, null, parentFields)) {
            return false;
          }
        } else {
          if (!messageDifferencer.compareFieldValueUsingParentFields(
                  message1, message2, field, -1, -1, null, parentFields)) {
            return false;
          }
        }
      }
      return true;
    }
  }

  /** Creates a new builder. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Builder object for {@link MessageDifferencer}. */
  public static final class Builder {
    private final Set<FieldDescriptor> setFields = Sets.newHashSet();
    private final Set<FieldDescriptor> ignoreFields = Sets.newHashSet();
    private final Map<FieldDescriptor, MapKeyComparator> mapKeyComparatorMap = Maps.newHashMap();
    private MessageFieldComparison messageFieldComparison = MessageFieldComparison.EQUAL;
    private Scope scope = Scope.FULL;
    private FloatComparison floatComparison = FloatComparison.EXACT;
    private RepeatedFieldComparison repeatedFieldComparison = RepeatedFieldComparison.AS_LIST;
    private boolean reportMatches;
    private FieldComparator fieldComparator;
    private final List<IgnoreCriteria> ignoreCriterias = Lists.newArrayList();

    private Builder() {}

    /**
     * The elements of the given repeated field will be treated as a set for
     * diffing purposes, so different orderings of the same elements will be
     * considered equal.  Elements which are present on both sides of the
     * comparison but which have changed position will be reported with {@link
     * ReportType#MOVED}.  Elements which only exist on one side or the other
     * are reported with {@link ReportType#ADDED} and {@link ReportType#DELETED}
     * regardless of their positions.  {@link ReportType#MODIFIED} is never used
     * for this repeated field.  If the only differences between the compared
     * messages is that some fields have been moved, then {@link #compare} will
     * return true.
     *
     * <p>If the scope of comparison is set to {@link Scope#PARTIAL}, extra
     * values added to repeated fields of the second message will not cause
     * {@link #compare} to return false.
     *
     * @throws IllegalArgumentException if the field is not repeated or is
     *     is already being as a map for comparison
     */
    public Builder treatAsSet(FieldDescriptor field) {
      Preconditions.checkArgument(field.isRepeated(), "Field must be repeated: %s",
          field.getFullName());
      Preconditions.checkArgument(!mapKeyComparatorMap.containsKey(field),
          "Cannot treat this repeated field as both Map and Set for comparison: %s",
          field.getFullName());
      setFields.add(field);
      return this;
    }

    /**
     * The elements of the given repeated field will be treated as a map for
     * diffing purposes, with {@code key} being the map key.  Thus, elements
     * with the same key will be compared even if they do not appear at the same
     * index.  Differences are reported similarly to {@link #treatAsSet}, except
     * that {@link ReportType#MODIFIED} is used to report elements with the same
     * key but different values.  Note that if an element is both moved and
     * modified, only {@link ReportType#MODIFIED} will be used.  As with {@link
     * #treatAsSet}, if the only differences between the compared messages is
     * that some fields have been moved, then {@link #compare} will return true.
     *
     * @throws IllegalArgumentException if the field is not repeated, is not a
     *     message, is already being as a set for comparison, or is not a
     *     containing type of the key
     */
    public Builder treatAsMap(FieldDescriptor field, FieldDescriptor key) {
      Preconditions.checkArgument(field.isRepeated(), "Field must be repeated: %s",
          field.getFullName());
      Preconditions.checkArgument(field.getJavaType() == JavaType.MESSAGE,
          "Field has to be message type: %s", field.getFullName());
      Preconditions.checkArgument(key.getContainingType().equals(field.getMessageType()),
          "%s must be a direct subfield within the repeated field: %s",
          key.getFullName(), field.getFullName());
      Preconditions.checkArgument(!setFields.contains(field),
          "Cannot treat this repeated field as both Map and Set for comparison: %s",
          key.getFullName());
      MultipleFieldsMapKeyComparator keyComparator = new MultipleFieldsMapKeyComparator(key);
      mapKeyComparatorMap.put(field, keyComparator);
      return this;
    }

    public Builder treatAsMapWithMultipleFieldsAsKey(FieldDescriptor field,
        List<FieldDescriptor> keyFields) {
      Preconditions.checkArgument(field.isRepeated(),
          "Field must be repeated " + field.getFullName());
      Preconditions.checkArgument(JavaType.MESSAGE.equals(field.getJavaType()),
          "Field has to be message type.  Field name is: " + field.getFullName());
      for (int i = 0; i < keyFields.size(); ++i) {
        FieldDescriptor key = keyFields.get(i);
        Preconditions.checkArgument(
            key.getContainingType().equals(field.getMessageType()),
            key.getFullName() + " must be a direct subfield within the repeated field: "
            + field.getFullName());
      }
      Preconditions.checkArgument(!setFields.contains(field),
          "Cannot treat this repeated field as both Map and Set for comparison.");
      MapKeyComparator keyComparator = new MultipleFieldsMapKeyComparator(keyFields);
      mapKeyComparatorMap.put(field, keyComparator);
      return this;
    }

    public Builder treatAsMapUsingKeyComparator(
        FieldDescriptor field, MapKeyComparator keyComparator) {
      Preconditions.checkArgument(field.isRepeated(),
          "Field must be repeated " + field.getFullName());
      Preconditions.checkArgument(JavaType.MESSAGE.equals(field.getJavaType()),
          "Field has to be message type.  Field name is: " + field.getFullName());
      Preconditions.checkArgument(!setFields.contains(field),
          "Cannot treat this repeated field as both Map and Set for comparison.");
      mapKeyComparatorMap.put(field, keyComparator);
      return this;
    }

    /**
     * Indicates that any field with the given descriptor should be
     * ignored for the purposes of comparing two messages. This applies
     * to fields nested in the message structure as well as top level
     * ones. When the MessageDifferencer encounters an ignored field,
     * it is reported with {@link ReportType#IGNORED}.
     *
     * <p>The only place where the field's 'ignored' status is not applied is when
     * it is being used as a key in a field passed to TreatAsMap or is one of
     * the fields passed to TreatAsMapWithMultipleFieldsAsKey.
     * In this case it is compared in key matching but after that it's ignored
     * in value comparison.
     */
    public Builder ignoreField(FieldDescriptor field) {
      ignoreFields.add(field);
      return this;
    }

    public Builder addIgnoreCriteria(IgnoreCriteria criterion) {
      this.ignoreCriterias.add(criterion);
      return this;
    }

    /**
     * Sets the type of comparison that is used by the differencer when
     * determining how to compare fields in messages.
     */
    public Builder setMessageFieldComparison(MessageFieldComparison comparison) {
      messageFieldComparison = comparison;
      return this;
    }

    /**
     * Tells the differencer whether or not to report matches. Defaults to
     * false.
     */
    public Builder setReportMatches(boolean reportMatches) {
      this.reportMatches = reportMatches;
      return this;
    }

    /**
     * Sets the scope of the comparison that is used by the differencer when
     * determining which fields to compare between the messages. Defaults to
     * {@link Scope#FULL}.
     */
    public Builder setScope(Scope scope) {
      this.scope = scope;
      return this;
    }

    /**
     * Sets the type of comparison that is used by the differencer when
     * comparing float (and double) fields in messages. Defaults to {@link
     * FloatComparison#EXACT}.
     *
     * <p>If you use {@link Builder#setFieldComparator(FieldComparator)},
     * this operation will be ignored
     */
    public Builder setFloatComparison(FloatComparison comparison) {
      floatComparison = Preconditions.checkNotNull(
          comparison, "FloatComparison should not be null.");
      return this;
    }

    /**
     * Sets the {@link FieldComparator} used to determine differences between protocol
     * buffer fields. By default it's set to a {@link DefaultFieldComparator} instance.
     * Note that this method must be called before Compare for the comparator to
     * be used.
     */
    public Builder setFieldComparator(FieldComparator fieldComparator) {
      this.fieldComparator = fieldComparator;
      return this;
    }

    /**
     * Sets the type of comparison for repeated field that is used by this
     * differencer when compare repeated fields in messages. Defaults to
     * {@link RepeatedFieldComparison#AS_LIST}.
     */
    public Builder setRepeatedFieldComparison(RepeatedFieldComparison comparison) {
      repeatedFieldComparison = comparison;
      return this;
    }

    IgnoreCriteria getMergedIgnoreCriteria() {
      if (!ignoreFields.isEmpty()) {
        IgnoreCriteria criterion = ignoringFields(ImmutableSet.copyOf(ignoreFields));
        return mergeCriteria(Iterables.concat(ignoreCriterias, Collections.singleton(criterion)));
      } else {
        return mergeCriteria(ignoreCriterias);
      }
    }

    /** Creates a new immutable differencer instance from this builder. */
    public MessageDifferencer build() {
      return new MessageDifferencer(this);
    }
  }

  private final ImmutableSet<FieldDescriptor> setFields;
  private final IgnoreCriteria ignoreCriteria;
  private final ImmutableMap<FieldDescriptor, MapKeyComparator> mapKeyComparatorMap;
  private final MessageFieldComparison messageFieldComparison;
  private final Scope scope;
  private final FloatComparison floatComparison;
  private final RepeatedFieldComparison repeatedFieldComparison;
  private final boolean reportMatches;
  private final FieldComparator fieldComparator;

  private MessageDifferencer(Builder builder) {
    setFields = ImmutableSet.copyOf(builder.setFields);
    ignoreCriteria = builder.getMergedIgnoreCriteria();
    mapKeyComparatorMap = ImmutableMap.copyOf(builder.mapKeyComparatorMap);
    messageFieldComparison = builder.messageFieldComparison;
    scope = builder.scope;
    floatComparison = builder.floatComparison;
    repeatedFieldComparison = builder.repeatedFieldComparison;
    reportMatches = builder.reportMatches;
    fieldComparator = builder.fieldComparator == null ?
        new DefaultFieldComparator(floatComparison) : builder.fieldComparator;
  }

  /**
   * Determines whether the supplied messages are equal. Equality is defined as
   * all fields within the two messages being set to the same value. Primitive
   * fields and strings are compared by value while embedded messages/groups are
   * compared as if via a recursive call.
   *
   * @throws IllegalArgumentException if the messages have different descriptors
   */
  public static boolean equals(Message message1, Message message2) {
    return newBuilder().build().compare(message1, message2);
  }

  /**
   * Determines whether the supplied messages are equivalent. Equivalency is
   * defined as all fields within the two messages having the same value. This
   * differs from the {@link #equals(Message, Message)} method above in that
   * fields with default values are considered set to said value
   * automatically. This method also ignores unknown fields.
   *
   * @throws IllegalArgumentException if the messages have different descriptors
   */
  public static boolean equivalent(Message message1, Message message2) {
    return newBuilder()
        .setMessageFieldComparison(MessageFieldComparison.EQUIVALENT)
        .build()
        .compare(message1, message2);
  }

  /**
   * Determines whether the supplied messages are approximately equal.
   * Approximate equality is defined as all fields within the two messages being
   * approximately equal. Primitive (non-float) fields and strings are compared
   * by value, floats are compared using an equivalent of C++ {@code
   * MathUtil::AlmostEquals} and embedded messages/groups are compared as if via
   * a recursive call.
   *
   * @throws IllegalArgumentException if the messages have different descriptors
   */
  public static boolean approximatelyEquals(Message message1, Message message2) {
    return newBuilder()
        .setFloatComparison(FloatComparison.APPROXIMATE)
        .build()
        .compare(message1, message2);
  }

  /**
   * Determines whether the supplied messages are approximately equivalent.
   * Approximate equivalency is defined as all fields within the two messages
   * being approximately equivalent. As in {@link #approximatelyEquals},
   * primitive (non-float) fields and strings are compared by value, floats are
   * compared using an equivalent of C++ {@code MathUtil::AlmostEquals} and
   * embedded messages/groups are compared as if via a recursive call. However,
   * fields with default values are considered set to said value, as per {@link
   * #equivalent}.
   *
   * @throws IllegalArgumentException if the messages have different descriptors
   */
  public static boolean approximatelyEquivalent(Message message1, Message message2) {
    return newBuilder()
        .setMessageFieldComparison(MessageFieldComparison.EQUIVALENT)
        .setFloatComparison(FloatComparison.APPROXIMATE)
        .build()
        .compare(message1, message2);
  }

  /**
   * IgnoreCriteria are registered with addIgnoreCriteria. For each compared field isIgnored is
   * called on each criterion until one returns true or all return false. isIgnored is called
   * for fields where at least one side has a value.
   */
  public interface IgnoreCriteria {

    /**
     * Should this field be ignored during the comparison.
     *
     * @param message1 the message containing the field being compared
     * @param message2 the message containing the field being compared
     * @param fieldDescriptor the field being compared (null for unknown fields).
     *        More details about unknown field is available in the last entry of fieldPath.
     * @param fieldPath an unmodifiable view of the path from the root message to this field
     * @return whether this field should be ignored in the comparison.
     */
    boolean isIgnored(Message message1, Message message2,
        @Nullable FieldDescriptor fieldDescriptor, List<SpecificField> fieldPath);
  }

  private static IgnoreCriteria ignoringFields(
      final ImmutableCollection<FieldDescriptor> fieldDescriptors) {
    return new IgnoreCriteria() {
      @Override
      public boolean isIgnored(Message message1, Message message2,
          FieldDescriptor fieldDescriptor, List<SpecificField> fieldPath) {
        return fieldDescriptors.contains(fieldDescriptor);
      }
    };
  }

  static IgnoreCriteria mergeCriteria(final Iterable<IgnoreCriteria> criteria) {
    return new IgnoreCriteria() {
      @Override
      public boolean isIgnored(Message message1, Message message2,
          FieldDescriptor fieldDescriptor,
          List<SpecificField> fieldPath) {
        for (IgnoreCriteria criterion : criteria) {
          if (criterion.isIgnored(message1, message2, fieldDescriptor, fieldPath)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  /** Identifies an individual field in a message instance. */
  @AutoValue
  @Immutable
  public abstract static class SpecificField {

    private static SpecificField forField(FieldDescriptor field) {
      Preconditions.checkNotNull(field);
      return new AutoValue_MessageDifferencer_SpecificField(field, null, -1, -1);
    }

    private static SpecificField forRepeatedField(FieldDescriptor field, int index) {
      Preconditions.checkNotNull(field);
      Preconditions.checkArgument(index >= 0);
      return new AutoValue_MessageDifferencer_SpecificField(field, null, index, index);
    }

    private static SpecificField forRepeatedField(FieldDescriptor field, int index, int newIndex) {
      Preconditions.checkNotNull(field);
      Preconditions.checkArgument(index >= 0);
      Preconditions.checkArgument(newIndex >= 0);
      return new AutoValue_MessageDifferencer_SpecificField(field, null, index, newIndex);
    }

    private static SpecificField forUnknownDescriptor(UnknownDescriptor unknown, int index) {
      Preconditions.checkNotNull(unknown);
      return new AutoValue_MessageDifferencer_SpecificField(null, unknown, index, index);
    }

    /** Returns the descriptor for known fields, or null for unknown fields. */
    @Nullable
    public abstract FieldDescriptor getField();

    /** Returns the descriptor for unknown fields, or null for known fields. */
    @Nullable
    public abstract UnknownDescriptor getUnknown();

    /**
     * Returns the field index. If this a repeated field, this is the index
     * within it.  For unknown fields, this is the index of the field among all
     * unknown fields of the same field number and type. For other fields,
     * returns -1.
     */
    public abstract int getIndex();

    /**
     * Returns the new field index. If this field is a repeated field which is
     * being treated as a map or a set, this indicates the position to which the
     * element has been moved. This only applies to {@link ReportType#MOVED},
     * and (in the case of {@link Builder#treatAsMap}) {@link
     * ReportType#MODIFIED}.
     */
    public abstract int getNewIndex();
  }

  /** Unknown field information. */
  @AutoValue
  @Immutable
  public abstract static class UnknownDescriptor {

    private static UnknownDescriptor create(int fieldNumber, UnknownFieldType fieldType) {
      return new AutoValue_MessageDifferencer_UnknownDescriptor(fieldNumber, fieldType);
    }

    /** Returns the field number. */
    public abstract int getFieldNumber();

    /** Returns the field type. */
    public abstract UnknownFieldType getFieldType();
  }

  /**
   * Interface for comparing protocol buffer fields.
   * Regular users should consider using {@link DefaultFieldComparator}
   * rather than this interface.
   * Currently, this does not support comparing unknown fields.
   */
  public interface FieldComparator {
    /**
     * Comparison result for {@link FieldComparator#compare}
     */
    public enum ComparisonResult {
      /**
       * Compared fields are equal. In case of comparing submessages,
       * user should not recursively compare their contents.
       */
      SAME,

      /**
       * Compared fields are different. In case of comparing submessages,
       * user should not recursively compare their contents.
       */
      DIFFERENT,

      /**
       * Compared submessages need to be compared recursively.
       * FieldComparator does not specify the semantics of recursive comparison.
       * This value should not be returned for simple values.
       */
      RECURSE;

      /**
       * Return {@link ComparisonResult} from a boolean value.
       *
       * @return {@link ComparisonResult#SAME} if result is true,
       *     {@link ComparisonResult#DIFFERENT} if result is false.
       */
      public static ComparisonResult of(boolean result) {
        return result ? SAME : DIFFERENT;
      }
    }

    /**
     * Compares the values of a field in two protocol buffer messages.
     *
     * @param message1 the first message.
     * @param message2 the second message.
     * @param field field descriptor of the field where need to be compared.
     * @param index1 the index of first message. In case the given FieldDescriptor
     *     points to a repeated field, the indices need to be valid. Otherwise
     *     they should be ignored.
     * @param index2 the index of second message. In case the given FieldDescriptor
     *     points to a repeated field, the indices need to be valid. Otherwise
     *     they should be ignored.
     * @param parentFields an immutable list of fields that was taken to find
     *     the current field (not include current field).
     * @return Returns SAME or DIFFERENT for simple values, and SAME, DIFFERENT
     *     or RECURSE for submessages. Returning RECURSE for fields not being
     *     submessages is illegal.
     */
    ComparisonResult compare(Message message1, Message message2,
        FieldDescriptor field, int index1, int index2, ImmutableList<SpecificField> parentFields);
  }

  /**
   * Interface by which callers can receive information about each difference.
   */
  public interface Reporter {
    /**
     * Reports information about a specific field.
     *
     * @param type the type of difference
     * @param message1 the first message
     * @param message2 the second message
     * @param fieldPath an immutable list of fields that was taken to find
     *     the current field. For example, for a field found in an embedded
     *     message, the list will contain two field descriptors. The first will
     *     be the field of the embedded message itself and the second will be
     *     the actual field in the embedded message that was
     *     added/deleted/modified.
     */
    void report(ReportType type, Message message1, Message message2,
        ImmutableList<SpecificField> fieldPath);
  }

  /** The type of the reported difference. */
  public enum ReportType {
    /** A field has been added to {@code message2}. */
    ADDED,

    /** A field has been deleted in {@code message2}. */
    DELETED,

    IGNORED,

    /** A field has been modified. */
    MODIFIED,

    /**
     * A repeated field has been moved to another location.  This only applies
     * when using {@link Builder#treatAsSet} or {@link Builder#treatAsMap}.
     * Also note that for any given field, {@link #MODIFIED} and {@link #MOVED}
     * are mutually exclusive. If a field has been both moved and modified,
     * then only {@link #MODIFIED} will be used.
     */
    MOVED,

    /**
     * Reports that two fields match. Useful for doing side-by-side diffs.  This
     * is mutually exclusive with {@link #MODIFIED} and {@link #MOVED}.  Matches
     * must be enabled using {@link Builder#setReportMatches}.
     */
    MATCHED
  }

  /**
   * The type of comparison that is used by the differencer when determining
   * how to compare fields in messages.
   */
  public enum MessageFieldComparison {
    /**
     * Fields must be present in both messages for the messages to be considered
     * the same.
     */
    EQUAL,

    /**
     * Fields with default values are considered set for comparison purposes
     * even if not explicitly set in the messages themselves. Unknown fields
     * are ignored.
     */
    EQUIVALENT
  }

  /** Which fields to consider when comparing messages. */
  public enum Scope {
    /** All fields of both messages are considered in the comparison. */
    FULL,

    /**
     * Only fields present in the first message are considered; fields set only
     * in the second message will be skipped during comparison.
     */
    PARTIAL
  }

  /** How float and double fields in messages are compared. */
  public enum FloatComparison {
    /** Floats and doubles are compared exactly. */
    EXACT,

    /**
     * Floats and doubles are compared using an equivalent of C++ {@code
     * MathUtil::AlmostEqual}.
     */
    APPROXIMATE
  }

  /** How to compare repeated fields. */
  public enum RepeatedFieldComparison {
    /**
     * Repeated fields are compared in order. Differing values at the same
     * index are reported using ReportModified(). If the repeated fields have
     * different numbers of elements, the unpaired elements are reported using
     * {@link ReportType#ADDED} or {@link ReportType#DELETED}.
     */
    AS_LIST,

    /**
     * Treat all the repeated fields as sets by default. See {@link
     * Builder#treatAsSet}.
     */
    AS_SET
  }

  /** The wire type of unknown fields. */
  public enum UnknownFieldType {
    /** Varint. */
    VARINT(WireFormat.WIRETYPE_VARINT) {
      @Override public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getVarintList();
      }
    },

    /** Fixed32. */
    FIXED32(WireFormat.WIRETYPE_FIXED32) {
      @Override public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getFixed32List();
      }
    },

    /** Fixed64. */
    FIXED64(WireFormat.WIRETYPE_FIXED64) {
      @Override public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getFixed64List();
      }
    },

    /** Length delimited. */
    LENGTH_DELIMITED(WireFormat.WIRETYPE_LENGTH_DELIMITED) {
      @Override public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getLengthDelimitedList();
      }
    },

    /** Group. */
    GROUP(WireFormat.WIRETYPE_START_GROUP) {
      @Override public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getGroupList();
      }
    };

    final int wireFormat;

    UnknownFieldType(int wireFormat) {
      this.wireFormat = wireFormat;
    }

    /** Returns the wire format for this unknown field type. */
    public int getWireFormat() {
      return wireFormat;
    }

    // TODO(chrisn): Genericize UnknownFieldType based on value type?
    /** Returns the corresponding values from the given field. */
    public abstract List<?> getValues(UnknownFieldSet.Field field);
  }

  /**
   * Compares the two specified messages, returning true if they are the same.
   *
   * @throws IllegalArgumentException if the messages have different descriptors
   */
  public boolean compare(Message message1, Message message2) {
    return compare(message1, message2, null);
  }

  /**
   * Compares the two specified messages, returning true if they are the same.
   * Reports differences to the reporter if it is non-null.
   *
   * @throws IllegalArgumentException if the messages have different descriptors
   */
  public boolean compare(Message message1, Message message2, @Nullable Reporter reporter) {
    List<SpecificField> stack = Lists.newArrayList();
    return compare(message1, message2, reporter, stack);
  }

  private boolean compare(
      Message message1, Message message2, @Nullable Reporter reporter, List<SpecificField> stack) {
    checkSameDescriptor(message1, message2);
    if (message1 == message2 && (reporter == null || !reportMatches)) {
      return true;
    }
    boolean unknownCompareResult = true;
    if (!compareUnknownFields(message1, message2, reporter, stack)) {
      if (reporter == null) {
        return false;
      }
      unknownCompareResult = false;
    }
    Set<FieldDescriptor> message1Fields = message1.getAllFields().keySet();
    Set<FieldDescriptor> message2Fields = message2.getAllFields().keySet();
    return compareRequestedFields(
            message1, message2, message1Fields, message2Fields, reporter, stack)
        && unknownCompareResult;
  }

  /**
   * Same as above, except comparing only the given sets of field descriptors,
   * using only the given message fields.
   *
   * @throws IllegalArgumentException if the messages have different descriptors
   */
  public boolean compareWithFields(Message message1, Message message2,
      Set<FieldDescriptor> message1Fields, Set<FieldDescriptor> message2Fields) {
    return compareWithFields(message1, message2, message1Fields, message2Fields, null);
  }

  /**
   * Compares the two specified messages, returning true if they are the same,
   * using only the given message fields. Reports differences to the reporter if
   * it is non-null.
   *
   * @throws IllegalArgumentException if the messages have different descriptors
   */
  public boolean compareWithFields(Message message1, Message message2,
      Set<FieldDescriptor> message1Fields, Set<FieldDescriptor> message2Fields,
      @Nullable Reporter reporter) {
    checkSameDescriptor(message1, message2);
    // Ensure fields are sorted.
    message1Fields = ImmutableSet.copyOf(Ordering.natural().sortedCopy(message1Fields));
    message2Fields = ImmutableSet.copyOf(Ordering.natural().sortedCopy(message2Fields));
    List<SpecificField> stack = Lists.newArrayList();
    return compareRequestedFields(message1, message2, message1Fields, message2Fields, reporter,
        stack);
  }

  private void checkSameDescriptor(Message message1, Message message2) {
    Preconditions.checkArgument(message1.getDescriptorForType()
            .equals(message2.getDescriptorForType()),
        "Comparison between two messages with different descriptors: %s and %s",
        message1.getClass(), message2.getClass());
  }

  private boolean compareUnknownFields(Message message1, Message message2,
      @Nullable Reporter reporter, List<SpecificField> stack) {
    UnknownFieldSet unknownFieldSet1 = message1.getUnknownFields();
    UnknownFieldSet unknownFieldSet2 = message2.getUnknownFields();
    return compareUnknownFields(message1, message2, unknownFieldSet1, unknownFieldSet2,
        reporter, stack);
  }

  private boolean compareUnknownFields(Message message1, Message message2,
      UnknownFieldSet unknownFieldSet1, UnknownFieldSet unknownFieldSet2,
      @Nullable Reporter reporter, List<SpecificField> stack) {
    if (messageFieldComparison == MessageFieldComparison.EQUIVALENT) {
      return true;
    }
    boolean identical = unknownFieldSet1.equals(unknownFieldSet2);
    if (identical && (reporter == null || !reportMatches)) {
      return true;
    }
    Set<Integer> numbers1 = unknownFieldSet1.asMap().keySet();
    Set<Integer> numbers2 = unknownFieldSet2.asMap().keySet();
    if (numbers1.isEmpty() && numbers2.isEmpty()) {
      return true;
    }

    boolean match = true;
    // Use TreeSet to visit the fields in tag order.
    for (Integer number : Sets.newTreeSet(Sets.union(numbers1, numbers2))) {
      for (UnknownFieldType fieldType : UnknownFieldType.values()) {
        List<?> values1 = fieldType.getValues(unknownFieldSet1.getField(number));
        List<?> values2 = fieldType.getValues(unknownFieldSet2.getField(number));
        if (values1.equals(values2)) {
          continue;
        }
        if (values1.isEmpty()) {
          if (scope == Scope.PARTIAL) {
            continue;
          }
        }
        UnknownDescriptor unknownDesc = UnknownDescriptor.create(number, fieldType);
        for (int i = 0, count = Math.max(values1.size(), values2.size()); i < count; i++) {
          Object value1 = (i < values1.size()) ? values1.get(i) : null;
          Object value2 = (i < values2.size()) ? values2.get(i) : null;

          ReportType reportType = ReportType.MATCHED;
          SpecificField unknownField = SpecificField.forUnknownDescriptor(unknownDesc, i);
          if (ignoreCriteria.isIgnored(message1, message2, null, immutable(stack, unknownField))) {
            if (reporter == null || !reportMatches) {
              continue;
            }
            reportType = ReportType.IGNORED;
          } else if (value1 == null) {
            reportType = ReportType.ADDED;
            match = false;
          } else if (value2 == null) {
            reportType = ReportType.DELETED;
            match = false;
          } else if (fieldType == UnknownFieldType.GROUP) {
            stack.add(unknownField);
            if (!compareUnknownFields(
                message1,
                message2,
                (UnknownFieldSet) value1,
                (UnknownFieldSet) value2,
                reporter,
                stack)) {
              reportType = ReportType.MODIFIED;
              match = false;
            }
            pop(stack);
          } else if (!Objects.equals(value1, value2)) {
            reportType = ReportType.MODIFIED;
            match = false;
          }

          if (reporter != null) {
            if (reportType != ReportType.MATCHED || reportMatches) {
              reporter.report(reportType, message1, message2, immutable(stack, unknownField));
            }
          } else if (!match) {
            return false;
          }
        }
      }
    }
    return match;
  }

  private boolean compareRequestedFields(Message message1, Message message2,
      Set<FieldDescriptor> message1Fields, Set<FieldDescriptor> message2Fields,
      @Nullable Reporter reporter, List<SpecificField> stack) {
    if (scope == Scope.FULL) {
      if (messageFieldComparison == MessageFieldComparison.EQUIVALENT) {
        // We need to merge the field lists of both messages (i.e.
        // we are merely checking for a difference in field values,
        // rather than the addition or deletion of fields).
        Set<FieldDescriptor> fieldsUnion = Sets.union(message1Fields, message2Fields);
        return compareWithFieldsInternal(message1, message2, fieldsUnion, fieldsUnion, reporter,
            stack);
      } else {
        // Simple equality comparison, use the unaltered field lists.
        return compareWithFieldsInternal(message1, message2, message1Fields, message2Fields,
            reporter, stack);
      }
    } else {
      if (messageFieldComparison == MessageFieldComparison.EQUIVALENT) {
        // We use the list of fields for message1 for both messages when
        // comparing.  This way, extra fields in message2 are ignored,
        // and missing fields in message2 use their default value.
        return compareWithFieldsInternal(message1, message2, message1Fields, message1Fields,
            reporter, stack);
      } else {
        // We need to consider the full list of fields for message1
        // but only the intersection for message2.  This way, any fields
        // only present in message2 will be ignored, but any fields only
        // present in message1 will be marked as a difference.
        Set<FieldDescriptor> fieldsIntersection = Sets.intersection(message1Fields, message2Fields);
        return compareWithFieldsInternal(message1, message2, message1Fields, fieldsIntersection,
            reporter, stack);
      }
    }
  }

  private static final Set<FieldDescriptor> SENTINEL = Collections.singleton(null);

  private boolean compareWithFieldsInternal(Message message1, Message message2,
      Set<FieldDescriptor> message1Fields, Set<FieldDescriptor> message2Fields,
      @Nullable Reporter reporter, List<SpecificField> stack) {

    boolean isDifferent = false;
    Iterator<FieldDescriptor> it1 = Iterables.concat(message1Fields, SENTINEL).iterator();
    Iterator<FieldDescriptor> it2 = Iterables.concat(message2Fields, SENTINEL).iterator();

    // Loop while there are any fields in either message.
    FieldDescriptor field1 = it1.next();
    FieldDescriptor field2 = it2.next();
    while (field1 != null || field2 != null) {
      // Check for differences in the field itself.
      if (fieldBefore(field1, field2)) {
        // Field 1 is not in the field list for message 2.
        if (ignoreCriteria.isIgnored(
            message1, message2, field1, Collections.unmodifiableList(stack))) {
          // We are ignoring field1. Report the ignore and move on to the next field in message1.
          if (reporter != null) {
            report(ReportType.IGNORED, message1, message2, field1, message1, reporter, stack);
          }
          field1 = it1.next();
          continue;
        }
        if (reporter == null) {
          return false;
        } else {
          report(ReportType.DELETED, message1, message2, field1, message1, reporter, stack);
          isDifferent = true;
        }
        field1 = it1.next();
        continue;
      } else if (fieldBefore(field2, field1)) {
        // Field 2 is not in the field list for message 1.
        if (ignoreCriteria.isIgnored(
            message1, message2, field2, Collections.unmodifiableList(stack))) {
          // We are ignoring field2. Report the ignore and move on to the next field in message2.
          if (reporter != null) {
            report(ReportType.IGNORED, message1, message2, field2, message2, reporter, stack);
          }
          field2 = it2.next();
          continue;
        }
        if (reporter == null) {
          return false;
        } else {
          report(ReportType.ADDED, message1, message2, field2, message2, reporter, stack);
          isDifferent = true;
        }
        field2 = it2.next();
        continue;
      }

      // By this point, field1 and field2 are guaranteed to point to the same
      // field, so we can now compare the values.
      boolean fieldDifferent = false;
      if (ignoreCriteria.isIgnored(
          message1, message2, field1, Collections.unmodifiableList(stack))) {
        if (reporter != null) {
          report(ReportType.IGNORED, message1, message2, field2, message2, reporter, stack);
        }
      } else if (field1.isRepeated()) {
        fieldDifferent = !compareRepeatedField(message1, message2, field1, reporter, stack);
        if (fieldDifferent) {
          if (reporter == null) {
            return false;
          }
          isDifferent = true;
        }
      } else {
        SpecificField specificField = SpecificField.forField(field1);
        fieldDifferent = !compareFieldValueUsingParentFields(
            message1, message2, field1, -1, -1, reporter, stack);
        // If we have found differences, either report them or terminate if
        // no reporter is present.
        if (fieldDifferent) {
          if (reporter == null) {
            return false;
          }
          reporter.report(ReportType.MODIFIED, message1, message2, immutable(stack, specificField));
          // If the field was at any point found to be different, mark to
          // return this difference once the loop has completed.
          isDifferent = true;
        } else if (reportMatches && reporter != null) {
          reporter.report(ReportType.MATCHED, message1, message2, immutable(stack, specificField));
        }
      }
      field1 = it1.next();
      field2 = it2.next();
    }
    return !isDifferent;
  }

  boolean compareFieldValueUsingParentFields(Message message1, Message message2,
      FieldDescriptor field, int index1, int index2,
      @Nullable Reporter reporter, List<SpecificField> stack) {
    ComparisonResult result = fieldComparator.compare(message1, message2, field,
        index1, index2, ImmutableList.copyOf(stack));
    if (result == ComparisonResult.RECURSE) {
      Preconditions.checkArgument(field.getJavaType() == JavaType.MESSAGE,
            "FieldComparator should not return RECURSE for fields not being submessages!");
      // Get the nested messages and compare them using one of the
      // methods.
      Message nextMessage1 = field.isRepeated()
          ? (Message) message1.getRepeatedField(field, index1)
          : (Message) message1.getField(field);
      Message nextMessage2 = field.isRepeated()
          ? (Message) message2.getRepeatedField(field, index2)
          : (Message) message2.getField(field);

      stack.add(field.isRepeated()
          ? SpecificField.forRepeatedField(field, index1, index2)
          : SpecificField.forField(field));
      boolean isSame = compare(nextMessage1, nextMessage2, reporter, stack);
      pop(stack);
      return isSame;
    }

    return result == ComparisonResult.SAME;
  }

  private void report(ReportType reportType, Message message1, Message message2,
      FieldDescriptor field, Message first, Reporter reporter, List<SpecificField> stack) {
    if (field.isRepeated()) {
      int count = first.getRepeatedFieldCount(field);
      for (int i = 0; i < count; i++) {
        reporter.report(reportType, message1, message2,
            immutable(stack, SpecificField.forRepeatedField(field, i)));
      }
    } else {
      reporter.report(reportType, message1, message2,
          immutable(stack, SpecificField.forField(field)));
    }
  }

  private boolean fieldBefore(FieldDescriptor field1, FieldDescriptor field2) {
    if (field1 == null) {
      return false;
    }
    if (field2 == null) {
      return true;
    }
    return field1.getNumber() < field2.getNumber();
  }

  boolean compareRepeatedField(Message message1, Message message2,
      FieldDescriptor repeatedField, @Nullable Reporter reporter,
      List<SpecificField> stack) {
    int count1 = message1.getRepeatedFieldCount(repeatedField);
    int count2 = message2.getRepeatedFieldCount(repeatedField);
    boolean treatedAsSubset = isTreatedAsSubset(repeatedField);

    // If the field is not treated as subset and no detailed reports is needed,
    // we do a quick check on the number of the elements to avoid unnecessary
    // comparison.
    if (count1 != count2 && reporter == null && !treatedAsSubset) {
      return false;
    }

    // These two arrays are used for store the index of the correspondent
    // element in peer repeated field.
    int[] matchList1 = new int[count1];
    int[] matchList2 = new int[count2];

    // Try to match indices of the repeated fields. Return false if match fails
    // and there's no detailed report needed.
    if (!matchRepeatedFieldIndices(message1, message2, repeatedField, matchList1, matchList2, stack)
        && reporter == null) {
      return false;
    }

    boolean fieldDifferent = false;
    // At this point, we have already matched pairs of fields (with the reporting
    // to be done later). Now to check if the paired elements are different.
    for (int i = 0; i < count1; i++) {
      if (matchList1[i] == -1) {
        continue;
      }
      int newIndex = matchList1[i];
      SpecificField specificField = SpecificField.forRepeatedField(repeatedField, i, newIndex);
      boolean result = compareFieldValueUsingParentFields(
          message1, message2, repeatedField, i, newIndex, reporter, stack);

      // If we have found differences, either report them or terminate if
      // no reporter is present. Note that ReportModified, ReportMoved, and
      // ReportMatched are all mutually exclusive.
      if (!result) {
        if (reporter == null) {
          return false;
        }
        fieldDifferent = true;
      }

      if (reporter == null) {
        continue;
      }

      ReportType reportType = null;
      if (!result) {
        reportType = ReportType.MODIFIED;
      } else if (i != newIndex) {
        reportType = ReportType.MOVED;
      } else if (reportMatches) {
        reportType = ReportType.MATCHED;
      }
      if (reportType != null) {
        reporter.report(reportType, message1, message2, immutable(stack, specificField));
      }
    }

    // Report any remaining additions or deletions.
    for (int i = 0; i < count2; i++) {
      if (matchList2[i] != -1) {
        continue;
      }
      if (!treatedAsSubset) {
        fieldDifferent = true;
      }
      if (reporter != null) {
        reporter.report(ReportType.ADDED, message1, message2,
            immutable(stack, SpecificField.forRepeatedField(repeatedField, i)));
      }
    }

    for (int i = 0; i < count1; i++) {
      if (matchList1[i] != -1) {
        continue;
      }
      // We would have exited earlier if reporter was null.
      reporter.report(ReportType.DELETED, message1, message2,
          immutable(stack, SpecificField.forRepeatedField(repeatedField, i)));
      fieldDifferent = true;
    }
    return !fieldDifferent;
  }

  private boolean matchRepeatedFieldIndices(Message message1, Message message2,
      FieldDescriptor repeatedField, int[] matchList1, int[] matchList2,
      List<SpecificField> stack) {
    MapKeyComparator keyComparator = mapKeyComparatorMap.get(repeatedField);
    if (repeatedField.isMapField() && keyComparator == null) {
      keyComparator = PROTO_MAP_KEY_COMPARATOR;
    }
    int count1 = matchList1.length;
    int count2 = matchList2.length;
    Arrays.fill(matchList1, -1);
    Arrays.fill(matchList2, -1);

    boolean success = true;
    // Find potential match if this is a special repeated field.
    if (keyComparator != null || isTreatedAsSet(repeatedField)) {
      for (int i = 0; i < count1; i++) {
        // Indicates any matched elements for this repeated field.
        boolean match = false;
        int newIndex = i;
        for (int j = 0; j < count2; j++) {
          if (matchList2[j] != -1) {
            continue;
          }
          newIndex = j;
          match = isMatch(repeatedField, keyComparator, message1, message2, i, j, stack);
          if (match) {
            matchList1[i] = newIndex;
            matchList2[newIndex] = i;
            break;
          }
        }
        success = success && match;
      }
    } else {
      // If this field should be treated as list, just label the match_list.
      for (int i = 0; i < count1 && i < count2; i++) {
        matchList1[i] = matchList2[i] = i;
      }
    }
    return success;
  }

  private boolean isMatch(FieldDescriptor repeatedField, @Nullable MapKeyComparator keyComparator,
      Message message1, Message message2, int index1, int index2, List<SpecificField> stack) {
    boolean isSame;

    if (keyComparator == null) {
      return compareFieldValueUsingParentFields(
          message1, message2, repeatedField, index1, index2, null, stack);
    } else {
      Message m1 = (Message) message1.getRepeatedField(repeatedField, index1);
      Message m2 = (Message) message2.getRepeatedField(repeatedField, index2);
      stack.add(SpecificField.forRepeatedField(repeatedField, index1, index2));
      isSame = keyComparator.isMatch(this, m1, m2, stack);
    }
    pop(stack);

    return isSame;
  }

  private boolean isTreatedAsSubset(FieldDescriptor field) {
    return isTreatedAsSet(field) && scope == Scope.PARTIAL;
  }

  private boolean isTreatedAsSet(FieldDescriptor field) {
    if (repeatedFieldComparison == RepeatedFieldComparison.AS_SET) {
      return true;
    }
    return setFields.contains(field);
  }

  // Returns an immutable list copy of the stack with an extra element appended.
  private static <T> ImmutableList<T> immutable(Iterable<T> stack, T extraElement) {
    return ImmutableList.<T>builder().addAll(stack).add(extraElement).build();
  }

  // Pops the last result off of a list.
  private static void pop(List<?> stack) {
    stack.remove(stack.size() - 1);
  }

  /**
   * A message difference reporter that writes a textual description of the
   * differences to a character stream.
   */
  public static final class StreamReporter implements Reporter {
    private final Appendable output;
    private final boolean reportModifiedAggregates;

    /** Equivalent to {@code new StreamReporter(output, false)}. */
    public StreamReporter(Appendable output) {
      this(output, false);
    }

    /**
     * Creates a new reporter.
     *
     * @param output where to write the output to
     * @param reportModifiedAggregates when set to true, the stream reporter will
     *     also output aggregates nodes (i.e. messages and groups) whose subfields
     *     have been modified. When false, will only report the individual
     *     subfields. Defaults to false.
     */
    public StreamReporter(Appendable output, boolean reportModifiedAggregates) {
      this.output = Preconditions.checkNotNull(output);
      this.reportModifiedAggregates = reportModifiedAggregates;
    }

    /** I/O exceptions that occur during reporting are wrapped by this type. */
    public static final class StreamException extends RuntimeException {
      private StreamException(IOException e) {
        super(e);
      }
    }

    @Override public void report(ReportType type, Message message1, Message message2,
        ImmutableList<SpecificField> fieldPath) {
      try {
        if (type == ReportType.MODIFIED && !reportModifiedAggregates) {
          SpecificField specificField = Iterables.getLast(fieldPath);
          if (specificField.getField() == null) {
            if (specificField.getUnknown().getFieldType() == UnknownFieldType.GROUP) {
              // Any changes to the subfields have already been printed.
              return;
            }
          } else if (specificField.getField().getJavaType() == JavaType.MESSAGE) {
            // Any changes to the subfields have already been printed.
            return;
          }
        }
        output.append(type.name().toLowerCase()).append(": ");
        switch (type) {
          case ADDED:
            appendPath(fieldPath, false);
            output.append(": ");
            appendValue(message2, fieldPath, false);
            break;
          case DELETED:
            appendPath(fieldPath, true);
            output.append(": ");
            appendValue(message1, fieldPath, true);
            break;
          case IGNORED:
            appendPath(fieldPath, false);
            break;
          case MOVED:
            appendPath(fieldPath, true);
            output.append(" -> ");
            appendPath(fieldPath, false);
            output.append(" : ");
            appendValue(message1, fieldPath, true);
            break;
          case MODIFIED:
            appendPath(fieldPath, true);
            if (checkPathChanged(fieldPath)) {
              output.append(" -> ");
              appendPath(fieldPath, false);
            }
            output.append(": ");
            appendValue(message1, fieldPath, true);
            output.append(" -> ");
            appendValue(message2, fieldPath, false);
            break;
          case MATCHED:
            appendPath(fieldPath, true);
            if (checkPathChanged(fieldPath)) {
              output.append(" -> ");
              appendPath(fieldPath, false);
            }
            output.append(" : ");
            appendValue(message1, fieldPath, true);
            break;
        }
        output.append("\n");
      } catch (IOException e) {
        throw new StreamException(e);
      }
    }

    private boolean checkPathChanged(ImmutableList<SpecificField> fieldPath) {
      for (SpecificField specificField : fieldPath) {
        if (specificField.getIndex() != specificField.getNewIndex()) {
          return true;
        }
      }
      return false;
    }

    private void appendPath(ImmutableList<SpecificField> fieldPath, boolean leftSide)
        throws IOException {
      for (Iterator<SpecificField> it = fieldPath.iterator(); it.hasNext();) {
        SpecificField specificField = it.next();
        FieldDescriptor field = specificField.getField();
        if (field != null) {
          if (field.isExtension()) {
            output.append("(").append(field.getFullName()).append(")");
          } else {
            output.append(field.getName());
          }
        } else {
          output.append(String.valueOf(specificField.getUnknown().getFieldNumber()));
        }
        if (leftSide && specificField.getIndex() >= 0) {
          output.append("[").append(String.valueOf(specificField.getIndex())).append("]");
        }
        if (!leftSide && specificField.getNewIndex() >= 0) {
          output.append("[").append(String.valueOf(specificField.getNewIndex())).append("]");
        }
        if (it.hasNext()) {
          output.append(".");
        }
      }
    }

    private void appendValue(Message message, ImmutableList<SpecificField> fieldPath,
        boolean leftSide) throws IOException {
      SpecificField specificField = Iterables.getLast(fieldPath);
      FieldDescriptor field = specificField.getField();
      if (field != null) {
        int index = leftSide ? specificField.getIndex() : specificField.getNewIndex();
        Object value = field.isRepeated()
              ? message.getRepeatedField(field, index)
              : message.getField(field);
        if (field.getJavaType() == JavaType.MESSAGE) {
          output.append(wrapDebugString(TextFormat.shortDebugString((Message) value)));
        } else {
          TextFormat.printFieldValue(field, value, output);
        }
      } else {
        UnknownFieldSet unknownFields = message.getUnknownFields();
        UnknownFieldSet.Field unknownField = null;
        UnknownDescriptor unknownDescriptor = null;
        for (SpecificField node : fieldPath) {
          unknownDescriptor = node.getUnknown();
          if (unknownDescriptor != null) {
            unknownField = unknownFields.getField(unknownDescriptor.getFieldNumber());
            if (unknownDescriptor.getFieldType() == UnknownFieldType.GROUP) {
              unknownFields = unknownField.getGroupList().get(node.getIndex());
            }
          }
        }
        UnknownFieldType unknownType = unknownDescriptor.getFieldType();
        Object value = unknownType.getValues(unknownField).get(specificField.getIndex());
        int wireFormat = unknownType.getWireFormat();
        if (wireFormat == WireFormat.WIRETYPE_START_GROUP) {
          output.append(wrapDebugString(TextFormat.shortDebugString((UnknownFieldSet) value)));
        } else {
          TextFormat.printUnknownFieldValue(wireFormat, value, output);
        }
      }
    }
  }

  // Wraps a message debug string in curly braces.
  private static String wrapDebugString(String debugString) {
    return debugString.isEmpty() ? "{ }" : "{ " + debugString + " }";
  }

  /**
   * Basic implementation of FieldComparator.
   */
  @Immutable public static final class DefaultFieldComparator implements FieldComparator {
    private final FloatComparison floatComparison;

    public DefaultFieldComparator(FloatComparison floatComparison) {
      this.floatComparison = Preconditions.checkNotNull(floatComparison);
    }

    /** Port of C++ MathUtil::AlmostEquals, with STD_ERR of 1e-5f * 32. */
    @VisibleForTesting static boolean almostEquals(float x, float y) {
      return almostEquals(x, y, 1e-5f * 32);
    }

    /** Port of C++ MathUtil::AlmostEquals, with STD_ERR of 1e-9d * 32. */
    @VisibleForTesting static boolean almostEquals(double x, double y) {
      return almostEquals(x, y, 1e-9d * 32);
    }

    private static boolean almostEquals(double x, double y, double stdErr) {
      if (x == y) {
        return true;
      }
      // It's convenient in many ways to treat NaN as equal to NaN - it's also
      // what the exact comparison does, by virtue of using Double.equals instead
      // of ==.
      if (Double.isNaN(x) && Double.isNaN(y)) {
        return true;
      }
      if (Double.isInfinite(x) || Double.isInfinite(y)) {
        return false;
      }
      if (Math.abs(x) <= stdErr && Math.abs(y) <= stdErr) {
        return true;
      }
      double absDiff = (x > y) ? x - y : y - x;
      return absDiff <= Math.max(stdErr, stdErr * Math.max(Math.abs(x), Math.abs(y)));
    }

    @Override
    public ComparisonResult compare(Message message1, Message message2, FieldDescriptor field,
        int index1, int index2, ImmutableList<SpecificField> parentFields) {
      Object value1 = field.isRepeated()
          ? message1.getRepeatedField(field, index1)
          : message1.getField(field);
      Object value2 = field.isRepeated()
          ? message2.getRepeatedField(field, index2)
          : message2.getField(field);

      switch (field.getJavaType()) {
        case MESSAGE:
          return ComparisonResult.RECURSE;
        case INT:
        case LONG:
        case BOOLEAN:
        case STRING:
        case BYTE_STRING:
        case ENUM:
          return ComparisonResult.of(value1.equals(value2));
        case FLOAT:
          if (floatComparison == FloatComparison.EXACT) {
            return ComparisonResult.of(value1.equals(value2));
          } else {
            return ComparisonResult.of(almostEquals(
                ((Number) value1).floatValue(),
                ((Number) value2).floatValue()));
          }
        case DOUBLE:
          if (floatComparison == FloatComparison.EXACT) {
            return ComparisonResult.of(value1.equals(value2));
          } else {
            return ComparisonResult.of(almostEquals(
                ((Number) value1).doubleValue(),
                ((Number) value2).doubleValue()));
          }
        default:
          throw new IllegalArgumentException("Bad field type " + field.getJavaType());
      }
    }
  }
}
