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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

/** Factory class for {@link FieldScope} instances. */
public class FieldScopes {
  private FieldScopes() {}

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
   * Foo scope = Foo.newBuilder().setBar(2);
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
  public static <M extends Message> FieldScope<M> fromSetFields(M message) {
    return FieldScopeImpl.partialScope(checkNotNull(message));
  }

  /**
   * Returns a {@link FieldScope} which matches all fields without exception. {@link ProtoSubject}
   * uses this scope by default, so this is generally not needed unless you need to also ignore
   * certain fields.
   */
  public static <M extends Message> FieldScope<M> all() {
    return FieldScopeImpl.all();
  }

  /**
   * Returns a {@link FieldScope} which matches no fields. A comparison made using this scope will
   * always trivially pass, so generally an {@code allowing} call is expected after calling this
   * method.
   */
  public static <M extends Message> FieldScope<M> none() {
    return FieldScopeImpl.none();
  }

  /**
   * Returns a {@link FieldScope} which matches everything except the provided field numbers for the
   * top level message type.
   *
   * @see FieldScope#ignoringFields
   */
  public static <M extends Message> FieldScope<M> ignoringFields(int... fieldNumbers) {
    return FieldScopes.<M>all().ignoringFields(fieldNumbers);
  }

  /**
   * Returns a {@link FieldScope} which matches everything except the provided field descriptors for
   * the message.
   *
   * @see FieldScope#ignoringFieldDescriptors
   */
  public static <M extends Message> FieldScope<M> ignoringFieldDescriptors(
      FieldDescriptor... fieldDescriptors) {
    return FieldScopes.<M>all().ignoringFieldDescriptors(fieldDescriptors);
  }

  /**
   * Returns a {@link FieldScope} which matches nothing except the provided field numbers for the
   * top level message type.
   *
   * @see FieldScope#allowingFields
   */
  public static <M extends Message> FieldScope<M> allowingFields(int... fieldNumbers) {
    return FieldScopes.<M>none().allowingFields(fieldNumbers);
  }

  /**
   * Returns a {@link FieldScope} which matches nothing except the provided field descriptors for
   * the message.
   *
   * @see FieldScope#allowingFieldDescriptors
   */
  public static <M extends Message> FieldScope<M> allowingFieldDescriptors(
      FieldDescriptor... fieldDescriptors) {
    return FieldScopes.<M>none().allowingFieldDescriptors(fieldDescriptors);
  }
}
