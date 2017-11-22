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
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.truth.extensions.proto.MessageDifferencer.SpecificField;
import com.google.common.truth.extensions.proto.ProtoTruthMessageDifferencer.FieldDescriptorOrUnknown;
import com.google.common.truth.extensions.proto.ProtoTruthMessageDifferencer.UnknownFieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import java.util.List;
import java.util.Map;

/**
 * Tree representation of all set field numbers in a message, merging across repeated elements.
 *
 * <p>Sub messages are represented by child {@link FieldNumberTree} objects.
 *
 * @see FieldScopeImpl#partialScope
 */
final class FieldNumberTree {

  private static final FieldNumberTree EMPTY = new FieldNumberTree();

  /** A {@code FieldNumberTree} with no children. */
  static FieldNumberTree empty() {
    return EMPTY;
  }

  @AutoValue
  abstract static class Key {
    abstract Optional<Integer> knownFieldNumber();

    abstract Optional<UnknownFieldDescriptor> unknownFieldDescriptor();

    static Key fromFieldDescriptorOrUnknown(FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      return fieldDescriptorOrUnknown.fieldDescriptor().isPresent()
          ? known(fieldDescriptorOrUnknown.fieldDescriptor().get().getNumber())
          : unknown(fieldDescriptorOrUnknown.unknownFieldDescriptor().get());
    }

    static Key known(int fieldNumber) {
      return new AutoValue_FieldNumberTree_Key(
          Optional.of(fieldNumber), Optional.<UnknownFieldDescriptor>absent());
    }

    static Key unknown(UnknownFieldDescriptor unknownFieldDescriptor) {
      return new AutoValue_FieldNumberTree_Key(
          Optional.<Integer>absent(), Optional.of(unknownFieldDescriptor));
    }
  }

  // Modified only during [factory] construction, never changed afterwards.
  private final Map<Key, FieldNumberTree> children = Maps.newHashMap();

  /** Returns whether this {@code FieldNumberTree} has no children. */
  boolean isEmpty() {
    return children.isEmpty();
  }

  /**
   * Returns the {@code FieldNumberTree} corresponding to this sub-field.
   *
   * <p>{@code empty()} if there is none.
   */
  FieldNumberTree child(FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
    FieldNumberTree child =
        children.get(Key.fromFieldDescriptorOrUnknown(fieldDescriptorOrUnknown));
    return child == null ? EMPTY : child;
  }

  /** Returns whether this tree has a child for this node. */
  boolean hasChild(FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
    return children.containsKey(Key.fromFieldDescriptorOrUnknown(fieldDescriptorOrUnknown));
  }

  static FieldNumberTree fromMessage(Message message) {
    FieldNumberTree tree = new FieldNumberTree();

    // Known fields.
    Map<FieldDescriptor, Object> knownFieldValues = message.getAllFields();
    for (FieldDescriptor field : knownFieldValues.keySet()) {
      Key key = Key.known(field.getNumber());
      FieldNumberTree childTree = new FieldNumberTree();
      tree.children.put(key, childTree);

      Object fieldValue = knownFieldValues.get(field);
      if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
        if (field.isRepeated()) {
          List<?> valueList = (List<?>) fieldValue;
          for (Object value : valueList) {
            childTree.merge(fromMessage((Message) value));
          }
        } else {
          childTree.merge(fromMessage((Message) fieldValue));
        }
      }
    }

    // Unknown fields.
    tree.merge(fromUnknownFieldSet(message.getUnknownFields()));

    return tree;
  }

  private static FieldNumberTree fromUnknownFieldSet(UnknownFieldSet unknownFieldSet) {
    FieldNumberTree tree = new FieldNumberTree();
    for (int fieldNumber : unknownFieldSet.asMap().keySet()) {
      UnknownFieldSet.Field unknownField = unknownFieldSet.asMap().get(fieldNumber);
      for (UnknownFieldDescriptor unknownFieldDescriptor :
          UnknownFieldDescriptor.descriptors(fieldNumber, unknownField)) {
        Key key = Key.unknown(unknownFieldDescriptor);
        FieldNumberTree childTree = new FieldNumberTree();
        tree.children.put(key, childTree);

        if (unknownFieldDescriptor.type() == UnknownFieldDescriptor.Type.GROUP) {
          for (Object group : unknownFieldDescriptor.type().getValues(unknownField)) {
            childTree.merge(fromUnknownFieldSet((UnknownFieldSet) group));
          }
        }
      }
    }

    return tree;
  }

  /** Adds the other tree onto this one. May destroy {@code other} in the process. */
  private void merge(FieldNumberTree other) {
    for (Key key : other.children.keySet()) {
      FieldNumberTree value = other.children.get(key);
      if (!this.children.containsKey(key)) {
        this.children.put(key, value);
      } else {
        this.children.get(key).merge(value);
      }
    }
  }

  /**
   * Whether the field path described by {@code fieldPath} + {@code fieldDescriptor} was set by any
   * matching instance on the message used to instantiate this {@code FieldNumberTree}.
   */
  boolean matches(List<SpecificField> fieldPath, Optional<FieldDescriptor> fieldDescriptor) {
    return matchesInternal(0, fieldPath, fieldDescriptor);
  }

  private boolean matchesInternal(
      int fieldPathIndex,
      List<SpecificField> fieldPath,
      Optional<FieldDescriptor> fieldDescriptor) {
    if (fieldPathIndex < fieldPath.size()) {
      SpecificField field = fieldPath.get(fieldPathIndex);
      Key key;
      if (field.getField() != null) {
        key = Key.known(field.getField().getNumber());
      } else {
        UnknownFieldDescriptor.Type type =
            UnknownFieldDescriptor.Type.all().get(field.getUnknown().getFieldType().ordinal());
        key = Key.unknown(UnknownFieldDescriptor.create(field.getUnknown().getFieldNumber(), type));
      }

      FieldNumberTree child = children.get(key);
      return child != null && child.matchesInternal(fieldPathIndex + 1, fieldPath, fieldDescriptor);
    } else {
      return !fieldDescriptor.isPresent()
          || children.containsKey(Key.known(fieldDescriptor.get().getNumber()));
    }
  }
}
