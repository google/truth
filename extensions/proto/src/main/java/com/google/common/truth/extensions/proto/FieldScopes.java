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

import static com.google.common.collect.Lists.asList;
import static com.google.common.truth.extensions.proto.FieldScopeUtil.asList;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

/** Factory class for {@link FieldScope} instances. */
public final class FieldScopes {
  /**
   * Returns a {@link FieldScope} which is constrained to precisely those specific field paths that
   * are explicitly set in the message. Note that, for version 3 protobufs, such a {@link
   * FieldScope} will omit fields in the provided message which are set to default values.
   *
   * <p>This can be used limit the scope of a comparison to a complex set of fields in a very brief
   * statement. Often, {@code message} is the expected half of a comparison about to be performed.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * Foo actual = Foo.newBuilder().setBar(3).setBaz(4).build();
   * Foo expected = Foo.newBuilder().setBar(3).setBaz(5).build();
   * // Fails, because actual.getBaz() != expected.getBaz().
   * assertThat(actual).isEqualTo(expected);
   *
   * Foo scope = Foo.newBuilder().setBar(2).build();
   * // Succeeds, because only the field 'bar' is compared.
   * assertThat(actual).withPartialScope(FieldScopes.fromSetFields(scope)).isEqualTo(expected);
   *
   * }</pre>
   *
   * <p>The returned {@link FieldScope} does not respect repeated field indices nor map keys. For
   * example, if the provided message sets different field values for different elements of a
   * repeated field, like so:
   *
   * <pre>{@code
   * sub_message: {
   *   foo: "foo"
   * }
   * sub_message: {
   *   bar: "bar"
   * }
   * }</pre>
   *
   * <p>The {@link FieldScope} will contain {@code sub_message.foo} and {@code sub_message.bar} for
   * *all* repeated {@code sub_messages}, including those beyond index 1.
   */
  // TODO(user): Figure out a way to improve this without reinventing MessageDifferencer.
  // Alternatively, gather evidence to show that the existing behavior is fine/preferable.
  // Alternatively II, add Scope.PARTIAL support to ProtoFluentEquals, but with a different name and
  // explicit documentation that it may cause issues with Proto 3.
  public static FieldScope fromSetFields(Message message) {
    return FieldScopeImpl.createFromSetFields(message);
  }

  /**
   * Creates a {@link FieldScope} covering the fields set in every message in the provided list of
   * messages, with the same semantics as in {@link #fromSetFields(Message)}.
   *
   * <p>This can be thought of as the union of the {@link FieldScope}s for each individual message,
   * or the {@link FieldScope} for the merge of all the messages. These are equivalent.
   */
  public static FieldScope fromSetFields(
      Message firstMessage, Message secondMessage, Message... rest) {
    return fromSetFields(asList(firstMessage, secondMessage, rest));
  }

  /**
   * Creates a {@link FieldScope} covering the fields set in every message in the provided list of
   * messages, with the same semantics as in {@link #fromSetFields(Message)}.
   *
   * <p>This can be thought of as the union of the {@link FieldScope}s for each individual message,
   * or the {@link FieldScope} for the merge of all the messages. These are equivalent.
   */
  public static FieldScope fromSetFields(Iterable<? extends Message> messages) {
    return FieldScopeImpl.createFromSetFields(messages);
  }

  /**
   * Returns a {@link FieldScope} which matches everything except the provided field numbers for the
   * top level message type.
   *
   * <p>The field numbers are ignored recursively on this type. That is, if {@code YourMessage}
   * contains another {@code YourMessage} somewhere within its subtree, field number {@code X} will
   * be ignored for all submessages of type {@code YourMessage}, as well as for the top-level
   * message.
   *
   * @see FieldScope#ignoringFields(int, int...)
   */
  public static FieldScope ignoringFields(int firstFieldNumber, int... rest) {
    return FieldScopeImpl.createIgnoringFields(asList(firstFieldNumber, rest));
  }

  /**
   * Returns a {@link FieldScope} which matches everything except the provided field numbers for the
   * top level message type.
   *
   * <p>The field numbers are ignored recursively on this type. That is, if {@code YourMessage}
   * contains another {@code YourMessage} somewhere within its subtree, field number {@code X} will
   * be ignored for all submessages of type {@code YourMessage}, as well as for the top-level
   * message.
   *
   * @see FieldScope#ignoringFields(Iterable)
   */
  public static FieldScope ignoringFields(Iterable<Integer> fieldNumbers) {
    return FieldScopeImpl.createIgnoringFields(fieldNumbers);
  }

  /**
   * Returns a {@link FieldScope} which matches everything except the provided field descriptors for
   * the message.
   *
   * @see FieldScope#ignoringFieldDescriptors(FieldDescriptor, FieldDescriptor...)
   */
  public static FieldScope ignoringFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return FieldScopeImpl.createIgnoringFieldDescriptors(asList(firstFieldDescriptor, rest));
  }

  /**
   * Returns a {@link FieldScope} which matches everything except the provided field descriptors for
   * the message.
   *
   * @see FieldScope#ignoringFieldDescriptors(Iterable)
   */
  public static FieldScope ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    return FieldScopeImpl.createIgnoringFieldDescriptors(fieldDescriptors);
  }

  /**
   * Returns a {@link FieldScope} which matches nothing except the provided field numbers for the
   * top level message type.
   *
   * @see FieldScope#allowingFields(int, int...)
   */
  public static FieldScope allowingFields(int firstFieldNumber, int... rest) {
    return FieldScopeImpl.createAllowingFields(asList(firstFieldNumber, rest));
  }

  /**
   * Returns a {@link FieldScope} which matches nothing except the provided field numbers for the
   * top level message type.
   *
   * @see FieldScope#allowingFields(Iterable)
   */
  public static FieldScope allowingFields(Iterable<Integer> fieldNumbers) {
    return FieldScopeImpl.createAllowingFields(fieldNumbers);
  }

  /**
   * Returns a {@link FieldScope} which matches nothing except the provided field descriptors for
   * the message.
   *
   * @see FieldScope#allowingFieldDescriptors(FieldDescriptor, FieldDescriptor...)
   */
  public static FieldScope allowingFieldDescriptors(
      FieldDescriptor firstFieldDescriptor, FieldDescriptor... rest) {
    return FieldScopeImpl.createAllowingFieldDescriptors(asList(firstFieldDescriptor, rest));
  }

  /**
   * Returns a {@link FieldScope} which matches nothing except the provided field descriptors for
   * the message.
   *
   * @see FieldScope#allowingFieldDescriptors(Iterable)
   */
  public static FieldScope allowingFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    return FieldScopeImpl.createAllowingFieldDescriptors(fieldDescriptors);
  }

  /**
   * Returns a {@link FieldScope} which matches all fields without exception. Generally not needed,
   * since the other factory functions will build on top of this for you.
   */
  public static FieldScope all() {
    return FieldScopeImpl.all();
  }

  /**
   * Returns a {@link FieldScope} which matches no fields. A comparison made using this scope alone
   * will always trivially pass. Generally not needed, since the other factory functions will build
   * on top of this for you.
   */
  public static FieldScope none() {
    return FieldScopeImpl.none();
  }

  private FieldScopes() {}
}
