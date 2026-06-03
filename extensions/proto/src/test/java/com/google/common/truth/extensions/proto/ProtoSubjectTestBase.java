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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.TruthFailureSubject.truthFailures;
import static com.google.common.truth.extensions.proto.ProtoTruth.protos;
import static com.google.protobuf.ExtensionRegistry.getEmptyRegistry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.truth.Expect;
import com.google.common.truth.ExpectFailure;
import com.google.common.truth.Subject;
import com.google.common.truth.TruthFailureSubject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.UnknownFieldSet;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.junit.Rule;

/** Base class for testing {@link ProtoSubject} methods. */
public class ProtoSubjectTestBase {

  // Type information for subclasses.
  enum TestType {
    IMMUTABLE_PROTO2(TestMessage2.getDefaultInstance()),
    PROTO3(TestMessage3.getDefaultInstance());

    private final Message defaultInstance;

    TestType(Message defaultInstance) {
      this.defaultInstance = defaultInstance;
    }

    Message defaultInstance() {
      return defaultInstance;
    }

    boolean isProto3() {
      return this == PROTO3;
    }
  }

  private static final TypeRegistry typeRegistry =
      TypeRegistry.newBuilder()
          .add(TestMessage3.getDescriptor())
          .add(TestMessage2.getDescriptor())
          .build();

  private static final TextFormat.Parser PARSER =
      TextFormat.Parser.newBuilder()
          .setSingularOverwritePolicy(
              TextFormat.Parser.SingularOverwritePolicy.FORBID_SINGULAR_OVERWRITES)
          .setTypeRegistry(typeRegistry)
          .build();

  private static final ExtensionRegistry extensionRegistry = getEmptyRegistry();

  // For Parameterized testing.
  static Collection<Object[]> parameters() {
    ImmutableList.Builder<Object[]> builder = ImmutableList.builder();
    for (TestType testType : TestType.values()) {
      builder.add(new Object[] {testType});
    }
    return builder.build();
  }

  @Rule public final Expect expect = Expect.create();

  /**
   * A functional interface for {@link #expectFailure} to invoke and capture failures.
   *
   * <p>TODO(cpovirk): Replace this with {@code CustomSubjectBuilderCallback} once we introduce such
   * a type.
   */
  interface ProtoSubjectBuilderCallback {
    void invokeAssertion(ProtoSubjectBuilder whenTesting);
  }

  @CanIgnoreReturnValue
  static AssertionError expectFailure(ProtoSubjectBuilderCallback assertionCallback) {
    return ExpectFailure.expectFailure(
        whenTesting -> assertionCallback.invokeAssertion(whenTesting.about(protos())));
  }

  final TruthFailureSubject expectThatFailure(AssertionError failure) {
    return expect.about(truthFailures()).that(failure);
  }

  private final Message defaultInstance;
  private final boolean isProto3;

  ProtoSubjectTestBase(TestType testType) {
    this.defaultInstance = testType.defaultInstance();
    this.isProto3 = testType.isProto3();
  }

  final Message fromUnknownFields(UnknownFieldSet unknownFieldSet)
      throws InvalidProtocolBufferException {
    return defaultInstance.getParserForType().parseFrom(unknownFieldSet.toByteArray());
  }

  final String fullMessageName() {
    return defaultInstance.getDescriptorForType().getFullName();
  }

  final FieldDescriptor getFieldDescriptor(String fieldName) {
    FieldDescriptor fieldDescriptor =
        defaultInstance.getDescriptorForType().findFieldByName(fieldName);
    checkArgument(fieldDescriptor != null, "No field named %s.", fieldName);
    return fieldDescriptor;
  }

  final int getFieldNumber(String fieldName) {
    return getFieldDescriptor(fieldName).getNumber();
  }

  final TypeRegistry getTypeRegistry() {
    return typeRegistry;
  }

  final ExtensionRegistry getExtensionRegistry() {
    return extensionRegistry;
  }

  final Message clone(Message in) {
    return in.toBuilder().build();
  }

  Message parse(String textProto) {
    try {
      Message.Builder builder = defaultInstance.toBuilder();
      PARSER.merge(textProto, builder);
      return builder.build();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  final Message parsePartial(String textProto) {
    try {
      Message.Builder builder = defaultInstance.toBuilder();
      PARSER.merge(textProto, builder);
      return builder.buildPartial();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  final boolean isProto3() {
    return isProto3;
  }

  /**
   * Some tests don't vary across the different proto types, and should only be run once.
   *
   * <p>This method returns true for exactly one {@link TestType}, and false for all the others, and
   * so can be used to ensure tests are only run once.
   */
  final boolean testIsRunOnce() {
    return isProto3;
  }

  final ProtoSubject expectThat(@Nullable Message message) {
    return expect.about(protos()).that(message);
  }

  final <M extends Message> IterableOfProtosSubject<M> expectThat(Iterable<M> messages) {
    return expect.about(protos()).that(messages);
  }

  final <M extends Message> MapWithProtoValuesSubject<M> expectThat(Map<?, M> map) {
    return expect.about(protos()).that(map);
  }

  final <M extends Message> MultimapWithProtoValuesSubject<M> expectThat(Multimap<?, M> multimap) {
    return expect.about(protos()).that(multimap);
  }

  final ProtoSubject expectThatWithMessage(String msg, @Nullable Message message) {
    return expect.withMessage(msg).about(protos()).that(message);
  }

  final void expectIsEqualToFailed(AssertionError failure) {
    expectFailureMatches(
        failure,
        "Not true that messages compare equal\\.\\s*"
            + "(Differences were found:\\n.*|No differences were reported\\..*)");
  }

  final void expectIsNotEqualToFailed(AssertionError failure) {
    expectFailureMatches(
        failure,
        "Not true that messages compare not equal\\.\\s*"
            + "(Only ignorable differences were found:\\n.*|"
            + "No differences were found\\..*)");
  }

  /**
   * Expects the {@link ExpectFailure} failure message to match the provided regex, using {@code
   * Pattern.DOTALL} to match newlines.
   */
  final void expectFailureMatches(AssertionError failure, String regex) {
    expectThatFailure(failure).hasMessageThat().matches(Pattern.compile(regex, Pattern.DOTALL));
  }

  static <T> ImmutableList<T> listOf(T... elements) {
    return ImmutableList.copyOf(elements);
  }

  static <T> T[] arrayOf(T... elements) {
    return elements;
  }

  @SuppressWarnings("unchecked")
  static <K, V> ImmutableMap<K, V> mapOf(K k0, V v0, Object... rest) {
    Preconditions.checkArgument(rest.length % 2 == 0, "Uneven args: %s", rest.length);

    ImmutableMap.Builder<K, V> builder = new ImmutableMap.Builder<>();
    builder.put(k0, v0);
    for (int i = 0; i < rest.length; i += 2) {
      builder.put((K) rest[i], (V) rest[i + 1]);
    }
    return builder.buildOrThrow();
  }

  @SuppressWarnings("unchecked")
  static <K, V> ImmutableMultimap<K, V> multimapOf(K k0, V v0, Object... rest) {
    Preconditions.checkArgument(rest.length % 2 == 0, "Uneven args: %s", rest.length);

    ImmutableMultimap.Builder<K, V> builder = new ImmutableMultimap.Builder<>();
    builder.put(k0, v0);
    for (int i = 0; i < rest.length; i += 2) {
      builder.put((K) rest[i], (V) rest[i + 1]);
    }
    return builder.build();
  }

  final void checkMethodNamesEndWithForValues(
      Class<?> clazz, Class<? extends Subject> pseudoSuperclass) {
    // Don't run this test twice.
    if (!testIsRunOnce()) {
      return;
    }

    Set<String> diff = Sets.difference(getMethodNames(clazz), getMethodNames(pseudoSuperclass));
    assertWithMessage("Found no methods to test. Bug in test?").that(diff).isNotEmpty();
    for (String methodName : diff) {
      assertThat(methodName).endsWith("ForValues");
    }
  }

  private static ImmutableSet<String> getMethodNames(Class<?> clazz) {
    ImmutableSet.Builder<String> names = ImmutableSet.builder();
    for (Method method : clazz.getMethods()) {
      names.add(method.getName());
    }
    return names.build();
  }
}
