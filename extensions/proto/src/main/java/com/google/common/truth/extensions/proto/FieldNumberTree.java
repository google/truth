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

import com.google.common.collect.Maps;
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

  // Modified only during [factory] construction, never changed afterwards.
  private final Map<SubScopeId, FieldNumberTree> children = Maps.newHashMap();

  /** Returns whether this {@code FieldNumberTree} has no children. */
  boolean isEmpty() {
    return children.isEmpty();
  }

  /**
   * Returns the {@code FieldNumberTree} corresponding to this sub-field.
   *
   * <p>{@code empty()} if there is none.
   */
  FieldNumberTree child(SubScopeId subScopeId) {
    FieldNumberTree child = children.get(subScopeId);
    return child == null ? EMPTY : child;
  }

  /** Returns whether this tree has a child for this node. */
  boolean hasChild(SubScopeId subScopeId) {
    return children.containsKey(subScopeId);
  }

  static FieldNumberTree fromMessage(Message message) {
    FieldNumberTree tree = new FieldNumberTree();

    // Known fields.
    Map<FieldDescriptor, Object> knownFieldValues = message.getAllFields();
    for (FieldDescriptor field : knownFieldValues.keySet()) {
      SubScopeId subScopeId = SubScopeId.of(field);
      FieldNumberTree childTree = new FieldNumberTree();
      tree.children.put(subScopeId, childTree);

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

  static FieldNumberTree fromMessages(Iterable<? extends Message> messages) {
    FieldNumberTree tree = new FieldNumberTree();
    for (Message message : messages) {
      if (message != null) {
        tree.merge(fromMessage(message));
      }
    }
    return tree;
  }

  private static FieldNumberTree fromUnknownFieldSet(UnknownFieldSet unknownFieldSet) {
    FieldNumberTree tree = new FieldNumberTree();
    for (int fieldNumber : unknownFieldSet.asMap().keySet()) {
      UnknownFieldSet.Field unknownField = unknownFieldSet.asMap().get(fieldNumber);
      for (UnknownFieldDescriptor unknownFieldDescriptor :
          UnknownFieldDescriptor.descriptors(fieldNumber, unknownField)) {
        SubScopeId subScopeId = SubScopeId.of(unknownFieldDescriptor);
        FieldNumberTree childTree = new FieldNumberTree();
        tree.children.put(subScopeId, childTree);

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
    for (SubScopeId subScopeId : other.children.keySet()) {
      FieldNumberTree value = other.children.get(subScopeId);
      if (!this.children.containsKey(subScopeId)) {
        this.children.put(subScopeId, value);
      } else {
        this.children.get(subScopeId).merge(value);
      }
    }
  }
}
