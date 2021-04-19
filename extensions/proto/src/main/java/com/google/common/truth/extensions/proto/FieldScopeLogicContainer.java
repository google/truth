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

import com.google.protobuf.Descriptors.Descriptor;

/**
 * Critical methods for anything which is, or contains a {@link FieldScopeLogic}.
 *
 * <p>All such containers must support invoking {@code subScope} for scoping to sub-messages, and
 * all must support validation of integer field numbers.
 */
interface FieldScopeLogicContainer<T extends FieldScopeLogicContainer<T>> {

  /** Returns the analog of {@link FieldScopeLogic#subScope} for this container. */
  T subScope(Descriptor rootDescriptor, SubScopeId subScopeId);

  /** Validates explicitly specified fields for this container. */
  void validate(Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator);
}
