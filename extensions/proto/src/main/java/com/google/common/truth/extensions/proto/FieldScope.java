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

import com.google.common.truth.extensions.proto.MessageDifferencer.IgnoreCriteria;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * An immutable, abstract representation of a set of specific field paths. See {@link FieldScopes}
 * for entry points to obtain a {@code FieldScope} object.
 *
 * <p>A {@code FieldScope} is similar in concept to a {@link com.google.protobuf.FieldMask}, which
 * is an explicitly enumerated set of specific field paths. A FieldScope is more general, allowing
 * for the description of arbitrary classes of specific field paths to be included or excluded from
 * its definition. For example, given a large protocol buffer with many field definitions, and a
 * single string field named 'x', it is arduous to specify "All fields except 'x'" as a {@code
 * FieldMask}. With a {@code FieldScope}, it is simply {@code
 * FieldScopes.all().ignoringFields(MyMessage.X_FIELD_NUMBER)}.
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
 * <p>FieldScopes are not designed to be compact or efficient, trading flexibility of use for
 * runtime efficiency, generally composing themselves as recursive structures. For this reason, it
 * is not recommended to use FieldScopes in production code. Prefer to use {@link
 * MessageDifferencer}, and proper {@link FieldMask}s, directly in production code.
 *
 * @see com.google.protobuf.FieldMask
 * @see com.google.protobuf.util.FieldMaskUtil
 */
public abstract class FieldScope {

  /**
   * Returns a {@code FieldScope} equivalent to this one, minus all fields defined by the given
   * field numbers.
   *
   * <p>Validation of the field numbers is performed when the {@code FieldScope} is invoked
   * (typically by {@link ProtoFluentEquals#isEqualTo}). A runtime exception will occur if bad field
   * numbers are encountered.
   *
   * <p>The field numbers are ignored recursively on this type. That is, if {@code YourMessage}
   * contains another {@code YourMessage} somewhere within its subtree, a {@code FieldScope
   * ignoringFields(X)} will ignore field number {@code X} for all submessages of type {@code
   * YourMessage}, as well as for the top-level message.
   */
  public abstract FieldScope ignoringFields(int... fieldNumbers);

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
  public abstract FieldScope ignoringFieldDescriptors(FieldDescriptor... fieldDescriptors);

  /**
   * Returns a {@code FieldScope} equivalent to this one, plus all fields defined by the given field
   * numbers.
   *
   * <p>Validation of the field numbers is performed when the {@code FieldScope} is invoked
   * (typically by {@link ProtoFluentEquals#isEqualTo}). A runtime exception will occur if bad field
   * numbers are encountered.
   *
   * <p>The field numbers are included recursively on this type. That is, if {@code YourMessage}
   * contains another {@code YourMessage} somewhere within its subtree, a {@code
   * FieldScope<YourMessage> allowingFields(X)} will include field number {@code X} for all
   * submessages of type {@code YourMessage}, as well as for the top-level message.
   */
  public abstract FieldScope allowingFields(int... fieldNumbers);

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
  public abstract FieldScope allowingFieldDescriptors(FieldDescriptor... fieldDescriptors);

  // package-protected: Should not be implemented outside the package.
  FieldScope() {}

  /**
   * Convert this into an {@link IgnoreCriteria} for use with {@link MessageDifferencer}.
   *
   * <p>The returned object is only good for one {@link MessageDifferencer} invocation. The client
   * should request a new {@link IgnoreCriteria} for every {@link MessageDifferencer} call.
   *
   * @param descriptor Message Descriptor for M.
   */
  abstract IgnoreCriteria toIgnoreCriteria(Descriptor descriptor);
}
