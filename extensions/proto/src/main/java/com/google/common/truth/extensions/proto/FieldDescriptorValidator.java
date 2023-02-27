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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;

/** Various validators, to ensure that explicit comparison settings made by the user make sense. */
enum FieldDescriptorValidator {
  ALLOW_ALL() {
    @Override
    void validate(FieldDescriptor fieldDescriptor) {}
  },
  IS_FIELD_WITH_ABSENCE() {
    @Override
    void validate(FieldDescriptor fieldDescriptor) {
      checkArgument(
          !fieldDescriptor.isRepeated(),
          "%s is a repeated field; repeated fields cannot be absent, only empty",
          fieldDescriptor);

      checkArgument(
          fieldDescriptor.hasPresence(),
          "%s is a field without presence; it cannot be absent",
          fieldDescriptor);
    }
  },
  IS_FIELD_WITH_ORDER() {
    @Override
    void validate(FieldDescriptor fieldDescriptor) {
      checkArgument(
          !fieldDescriptor.isMapField(), "%s is a map field; it has no order", fieldDescriptor);
      checkArgument(
          fieldDescriptor.isRepeated(),
          "%s is not a repeated field; it has no order",
          fieldDescriptor);
    }
  },
  IS_FIELD_WITH_EXTRA_ELEMENTS() {
    @Override
    void validate(FieldDescriptor fieldDescriptor) {
      checkArgument(
          fieldDescriptor.isRepeated(),
          "%s is not a repeated field or a map field; it cannot contain extra elements",
          fieldDescriptor);
    }
  },
  IS_DOUBLE_FIELD() {
    @Override
    void validate(FieldDescriptor fieldDescriptor) {
      checkArgument(
          fieldDescriptor.getJavaType() == JavaType.DOUBLE,
          "%s is not a double field",
          fieldDescriptor);
    }
  },
  IS_FLOAT_FIELD() {
    @Override
    void validate(FieldDescriptor fieldDescriptor) {
      checkArgument(
          fieldDescriptor.getJavaType() == JavaType.FLOAT,
          "%s is not a float field",
          fieldDescriptor);
    }
  };

  /** Validates the given {@link FieldDescriptor} according to this instance's rules. */
  abstract void validate(FieldDescriptor fieldDescriptor);
}
