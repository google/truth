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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

/** Helper methods for working with Any protos. */
class AnyUtils {
  private static final FieldDescriptor TYPE_URL_FIELD_DESCRIPTOR =
      Any.getDescriptor().findFieldByNumber(Any.TYPE_URL_FIELD_NUMBER);

  static FieldDescriptor typeUrlFieldDescriptor() {
    return TYPE_URL_FIELD_DESCRIPTOR;
  }

  private static final FieldDescriptorOrUnknown TYPE_URL_FIELD_DESCRIPTOR_OR_UNKNOWN =
      FieldDescriptorOrUnknown.of(TYPE_URL_FIELD_DESCRIPTOR);

  static FieldDescriptorOrUnknown typeUrlFieldDescriptorOrUnknown() {
    return TYPE_URL_FIELD_DESCRIPTOR_OR_UNKNOWN;
  }

  private static final FieldDescriptor VALUE_FIELD_DESCRIPTOR =
      Any.getDescriptor().findFieldByNumber(Any.VALUE_FIELD_NUMBER);

  static FieldDescriptor valueFieldDescriptor() {
    return VALUE_FIELD_DESCRIPTOR;
  }

  private static final FieldDescriptorOrUnknown VALUE_FIELD_DESCRIPTOR_OR_UNKNOWN =
      FieldDescriptorOrUnknown.of(VALUE_FIELD_DESCRIPTOR);

  static FieldDescriptorOrUnknown valueFieldDescriptorOrUnknown() {
    return VALUE_FIELD_DESCRIPTOR_OR_UNKNOWN;
  }

  /** Unpack an `Any` proto using the TypeRegistry and ExtensionRegistry on `config`. */
  static Optional<Message> unpack(Message any, FluentEqualityConfig config) {
    Preconditions.checkArgument(
        any.getDescriptorForType().equals(Any.getDescriptor()),
        "Expected type google.protobuf.Any, but was: %s",
        any.getDescriptorForType().getFullName());

    String typeUrl = (String) any.getField(typeUrlFieldDescriptor());
    ByteString value = (ByteString) any.getField(valueFieldDescriptor());

    try {
      Descriptor descriptor = config.useTypeRegistry().getDescriptorForTypeUrl(typeUrl);
      if (descriptor == null) {
        return Optional.absent();
      }

      Message defaultMessage =
          DynamicMessage.parseFrom(descriptor, value, config.useExtensionRegistry());
      return Optional.of(defaultMessage);
    } catch (InvalidProtocolBufferException e) {
      return Optional.absent();
    }
  }

  private AnyUtils() {}
}
