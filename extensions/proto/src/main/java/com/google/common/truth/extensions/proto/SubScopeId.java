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

import com.google.auto.value.AutoOneOf;
import com.google.protobuf.Descriptors.FieldDescriptor;

@AutoOneOf(SubScopeId.Kind.class)
abstract class SubScopeId {
  enum Kind {
    FIELD_DESCRIPTOR,
    UNKNOWN_FIELD_DESCRIPTOR;
  }

  abstract Kind kind();

  abstract FieldDescriptor fieldDescriptor();

  abstract UnknownFieldDescriptor unknownFieldDescriptor();

  /** Returns a short, human-readable version of this identifier. */
  final String shortName() {
    switch (kind()) {
      case FIELD_DESCRIPTOR:
        return fieldDescriptor().isExtension()
            ? "[" + fieldDescriptor() + "]"
            : fieldDescriptor().getName();
      case UNKNOWN_FIELD_DESCRIPTOR:
        return String.valueOf(unknownFieldDescriptor().fieldNumber());
    }
    throw new AssertionError(kind());
  }

  static SubScopeId of(FieldDescriptor fieldDescriptor) {
    return AutoOneOf_SubScopeId.fieldDescriptor(fieldDescriptor);
  }

  static SubScopeId of(UnknownFieldDescriptor unknownFieldDescriptor) {
    return AutoOneOf_SubScopeId.unknownFieldDescriptor(unknownFieldDescriptor);
  }
}
