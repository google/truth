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

import com.google.common.base.Optional;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * An immutable, abstract representation of a set of specific field paths. See {@link FieldScopes}
 * for entry points to obtain a {@code FieldScope} object.
 *
 * <p>A {@code FieldScope} is similar in concept to a {@code FieldMask}, which is an explicitly
 * enumerated set of specific field paths. A FieldScope is more general, allowing for the
 * description of arbitrary classes of specific field paths to be included or excluded from its
 * definition. For example, given a large protocol buffer with many field definitions, and a single
 * string field named 'x', it is arduous to specify "All fields except 'x'" as a {@code FieldMask}.
 * With a {@code FieldScope}, it is simply {@code
 * FieldScopes.ignoringFields(MyMessage.X_FIELD_NUMBER)}.
 *
 * <p>All inclusion and exclusion operations on message-type fields are recursive, but may be
 * overridden by subsequent operations. In this way, a complex {@code FieldScope} such as:
 *
 * <pre>{@code
 * FieldScopes.ignoringFields(A.B_FIELD_NUMBER)
 *     .allowingFieldDescriptors(B.getDescriptor().findFieldByName("flag"))
 * }</pre>
 *
 * ...will match all fields on A, except fields on the message type B, but including B's flag field.
 * Thus, two messages of type A will compare equal even if their sub messages of type B are
 * completely different, so long as the 'flag' fields for each B matches. Because of this, method
 * ordering matters. Generally, exclusions should come after inclusions.
 *
 * <p>{@code FieldScope}s are not designed to be compact or efficient, trading flexibility of use
 * for runtime efficiency, generally composing themselves as recursive structures. For this reason,
 * it is not recommended to use {@code FieldScope} in production code. Prefer to use proper {@code
 * FieldMask}s, directly in production code.
 */
public abstract class FieldScope {

  /**
   * Returns a {@code FieldScope} equivalent to this one, minus all fields defined by the given
   * field numbers.
   *
   * <p>Validation of the field numbers is performed when the {@code FieldScope} is invoked
   * (typically by {@link ProtoFluentAssertion#isEqualTo}). A runtime exception will occur if bad
   * field numbers are encountered.
   *
   * <p>The field numbers are ignored recursively on this type. That is, if {@code YourMessage}
   * contains another {@code YourMessage} somewhere within its subtree, a {@code FieldScope
   * ignoringFields(X)} will ignore field number {@code X} for all submessages of type {@code
   * YourMessage}, as well as for the top-level message.
   */
  public abstract FieldScope ignoringFields(int firstFieldNumber, int... rest);

  /**
   * Returns a {@code FieldScope} equivalent to this one, minus all fields defined by the given
   * field numbers.
   *
   * <p>Validation of the field numbers is performed when the {@code FieldScope} is invoked
   * (typically by {@link ProtoFluentAssertion#isEqualTo}). A runtime exception will occur if bad
   * field numbers are encountered.
   *
   * <p>The field numbers are ignored recursively on this type. That is, if {@code YourMessage}
   * contains another {@code YourMessage} somewhere within its subtree, a {@code FieldScope
   * ignoringFields(X)} will ignore field number {@code X} for all submessages of type {@code
   * YourMessage}, as well as for the top-level message.
   */
  public abstract FieldScope ignoringFields(Iterable<Integer> fieldNumbers);

  /**
   * Returns a {@code FieldScope} equivalent to this one, minus all fields matching the given {@link
   * FieldDescriptor}s.
   *
   * <p>The {@link FieldDescriptor}s are not validated, as that would require scanning the entire
   * protobuf schema recursively from this message type. If a {@link FieldDescriptor} is provided
   * which refers to a field that is not part of this message, or any possible recursive
   * submessages, it is silently ignored.
   *
   * <p>The field descriptors are also ignored recursively on the message type. That is, if {@code
   * FooMessage.field_bar} is ignored, {@code field_bar} will be ignored for all submessages of the
   * parent type of type {@code FooMessage}.
   */
  public abstract FieldScope ignoringFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest);

  /**
   * Returns a {@code FieldScope} equivalent to this one, minus all fields defined by the given
   * field numbers.
   *
   * <p>The {@link FieldDescriptor}s are not validated, as that would require scanning the entire
   * protobuf schema recursively from this message type. If a {@link FieldDescriptor} is provided
   * which refers to a field that is not part of this message, or any possible recursive
   * submessages, it is silently ignored.
   *
   * <p>The field descriptors are also ignored recursively on the message type. That is, if {@code
   * FooMessage.field_bar} is ignored, {@code field_bar} will be ignored for all submessages of the
   * parent type of type {@code FooMessage}.
   */
  public abstract FieldScope ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors);

  /**
   * Returns a {@code FieldScope} equivalent to this one, plus all fields defined by the given field
   * numbers.
   *
   * <p>Validation of the field numbers is performed when the {@code FieldScope} is invoked
   * (typically by {@link ProtoFluentAssertion#isEqualTo}). A runtime exception will occur if bad
   * field numbers are encountered.
   *
   * <p>The field numbers are included recursively on this type. That is, if {@code YourMessage}
   * contains another {@code YourMessage} somewhere within its subtree, a {@code FieldScope
   * allowingFields(X)} will include field number {@code X} for all submessages of type {@code
   * YourMessage}, as well as for the top-level message.
   */
  public abstract FieldScope allowingFields(int firstFieldNumber, int... rest);

  /**
   * Returns a {@code FieldScope} equivalent to this one, plus all fields defined by the given field
   * numbers.
   *
   * <p>Validation of the field numbers is performed when the {@code FieldScope} is invoked
   * (typically by {@link ProtoFluentAssertion#isEqualTo}). A runtime exception will occur if bad
   * field numbers are encountered.
   *
   * <p>The field numbers are included recursively on this type. That is, if {@code YourMessage}
   * contains another {@code YourMessage} somewhere within its subtree, a {@code FieldScope
   * allowingFields(X)} will include field number {@code X} for all submessages of type {@code
   * YourMessage}, as well as for the top-level message.
   */
  public abstract FieldScope allowingFields(Iterable<Integer> fieldNumbers);

  /**
   * Returns a {@code FieldScope} equivalent to this one, plus all fields matching the given {@link
   * FieldDescriptor}s.
   *
   * <p>The {@link FieldDescriptor}s are not validated, as that would require scanning the entire
   * protobuf schema from this message type. If a {@link FieldDescriptor} is provided which refers
   * to a field that is not part of this message, or any possible recursive submessages, it is
   * silently ignored.
   *
   * <p>The field descriptors are also included recursively on the message type. That is, if {@code
   * FooMessage.field_bar} is included, {@code field_bar} will be included for all submessages of
   * the parent type of type {@code FooMessage}.
   */
  public abstract FieldScope allowingFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest);

  /**
   * Returns a {@code FieldScope} equivalent to this one, plus all fields matching the given {@link
   * FieldDescriptor}s.
   *
   * <p>The {@link FieldDescriptor}s are not validated, as that would require scanning the entire
   * protobuf schema from this message type. If a {@link FieldDescriptor} is provided which refers
   * to a field that is not part of this message, or any possible recursive submessages, it is
   * silently ignored.
   *
   * <p>The field descriptors are also included recursively on the message type. That is, if {@code
   * FooMessage.field_bar} is included, {@code field_bar} will be included for all submessages of
   * the parent type of type {@code FooMessage}.
   */
  public abstract FieldScope allowingFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors);

  // package-protected: Should not be implemented outside the package.
  FieldScope() {}

  /** Returns the underlying logical implementation of the {@link FieldScope}. */
  abstract FieldScopeLogic logic();

  /**
   * Returns a human-readable representation of this {@link FieldScope}, detailing its construction.
   *
   * <p>For use in {@link com.google.common.truth.Correspondence#toString()} for clarity.
   *
   * @param descriptor a unique message {@link Descriptor} that applies to all non-null arguments,
   *     if present. Used to pretty-print raw field numbers.
   */
  abstract String usingCorrespondenceString(Optional<Descriptor> descriptor);
}
