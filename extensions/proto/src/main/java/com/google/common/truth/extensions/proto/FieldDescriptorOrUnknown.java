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
import com.google.common.base.Optional;
import com.google.protobuf.Descriptors.FieldDescriptor;

/** An 'Either' of a {@link FieldDescriptor} or {@link UnknownFieldDescriptor}. */
@AutoValue
abstract class FieldDescriptorOrUnknown {
  static FieldDescriptorOrUnknown fromFieldDescriptor(FieldDescriptor fieldDescriptor) {
    return new AutoValue_FieldDescriptorOrUnknown(
        Optional.of(fieldDescriptor), Optional.<UnknownFieldDescriptor>absent());
  }

  static FieldDescriptorOrUnknown fromUnknown(UnknownFieldDescriptor unknownFieldDescriptor) {
    return new AutoValue_FieldDescriptorOrUnknown(
        Optional.<FieldDescriptor>absent(), Optional.of(unknownFieldDescriptor));
  }

  abstract Optional<FieldDescriptor> fieldDescriptor();

  abstract Optional<UnknownFieldDescriptor> unknownFieldDescriptor();

  /** Returns a short, human-readable version of this identifier. */
  final String shortName() {
    if (fieldDescriptor().isPresent()) {
      return fieldDescriptor().get().isExtension()
          ? "[" + fieldDescriptor().get() + "]"
          : fieldDescriptor().get().getName();
    } else {
      return String.valueOf(unknownFieldDescriptor().get().fieldNumber());
    }
  }
}
