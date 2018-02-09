/*
 * Copyright (c) 2018 Google, Inc.
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
import com.google.common.collect.ImmutableList;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.WireFormat;
import java.util.List;

/** Convenience class encapsulating type information for unknown fields. */
@AutoValue
abstract class UnknownFieldDescriptor {

  enum Type {
    VARINT(WireFormat.WIRETYPE_VARINT) {
      @Override
      public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getVarintList();
      }
    },
    FIXED32(WireFormat.WIRETYPE_FIXED32) {
      @Override
      public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getFixed32List();
      }
    },
    FIXED64(WireFormat.WIRETYPE_FIXED64) {
      @Override
      public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getFixed64List();
      }
    },
    LENGTH_DELIMITED(WireFormat.WIRETYPE_LENGTH_DELIMITED) {
      @Override
      public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getLengthDelimitedList();
      }
    },
    GROUP(WireFormat.WIRETYPE_START_GROUP) {
      @Override
      public List<?> getValues(UnknownFieldSet.Field field) {
        return field.getGroupList();
      }
    };

    private static final ImmutableList<Type> TYPES = ImmutableList.copyOf(values());

    static ImmutableList<Type> all() {
      return TYPES;
    }

    private final int wireType;

    Type(int wireType) {
      this.wireType = wireType;
    }

    /** Returns the corresponding values from the given field. */
    abstract List<?> getValues(UnknownFieldSet.Field field);

    /** Returns the {@link WireFormat} constant for this field type. */
    final int wireType() {
      return wireType;
    }
  }

  static UnknownFieldDescriptor create(int fieldNumber, Type type) {
    return new AutoValue_UnknownFieldDescriptor(fieldNumber, type);
  }

  abstract int fieldNumber();

  abstract Type type();

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
