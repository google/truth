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
import static org.junit.Assert.fail;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.truth.Expect;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;
import com.google.protobuf.UnknownFieldSet;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.junit.Rule;

/** Base class for testing {@link ProtoSubject} methods. */
public class ProtoSubjectTestBase {

  // Type information for subclasses.
  static enum TestType {
    IMMUTABLE_PROTO2(TestMessage2.getDefaultInstance()),
    PROTO3(TestMessage3.getDefaultInstance());

    private final Message defaultInstance;

    TestType(Message defaultInstance) {
      this.defaultInstance = defaultInstance;
    }

    public Message defaultInstance() {
      return defaultInstance;
    }

    public boolean isProto3() {
      return this == PROTO3;
    }
  }

  private static final TextFormat.Parser PARSER =
      TextFormat.Parser.newBuilder()
          .setAllowUnknownFields(false)
          .setSingularOverwritePolicy(
              TextFormat.Parser.SingularOverwritePolicy.FORBID_SINGULAR_OVERWRITES)
          .build();

  // For Parameterized testing.
  protected static Collection<Object[]> parameters() {
    ImmutableList.Builder<Object[]> builder = ImmutableList.builder();
    for (TestType testType : TestType.values()) {
      builder.add(new Object[] {testType});
    }
    return builder.build();
  }

  @Rule public final Expect expect = Expect.create();

  private final Message defaultInstance;
  private final boolean isProto3;

  protected ProtoSubjectTestBase(TestType testType) {
    this.defaultInstance = testType.defaultInstance();
    this.isProto3 = testType.isProto3();
  }

  protected final Message fromUnknownFields(UnknownFieldSet unknownFieldSet)
      throws InvalidProtocolBufferException {
    return defaultInstance.getParserForType().parseFrom(unknownFieldSet.toByteArray());
  }

  protected final String fullMessageName() {
    return defaultInstance.getDescriptorForType().getFullName();
  }

  protected final FieldDescriptor getFieldDescriptor(String fieldName) {
    FieldDescriptor fieldDescriptor =
        defaultInstance.getDescriptorForType().findFieldByName(fieldName);
    checkArgument(fieldDescriptor != null, "No field named %s.", fieldName);
    return fieldDescriptor;
  }

  protected final int getFieldNumber(String fieldName) {
    return getFieldDescriptor(fieldName).getNumber();
  }

  protected Message parse(String textProto) {
    try {
      Message.Builder builder = defaultInstance.toBuilder();
      PARSER.merge(textProto, builder);
      return builder.build();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  protected final Message parsePartial(String textProto) {
    try {
      Message.Builder builder = defaultInstance.toBuilder();
      PARSER.merge(textProto, builder);
      return builder.buildPartial();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  protected final boolean isProto3() {
    return isProto3;
  }

  /**
   * Some tests don't vary across the different proto types, and should only be run once.
   *
   * <p>This method returns true for exactly one {@link TestType}, and false for all the others, and
   * so can be used to ensure tests are only run once.
   */
  protected final boolean testIsRunOnce() {
    return isProto3;
  }

  protected final ProtoSubject<?, Message> expectThat(@Nullable Message message) {
    return expect.about(ProtoTruth.protos()).that(message);
  }

  protected final <M extends Message> IterableOfProtosSubject<?, M, ?> expectThat(
      Iterable<M> messages) {
    return expect.about(ProtoTruth.protos()).that(messages);
  }

  protected final <M extends Message> MapWithProtoValuesSubject<?, ?, M, ?> expectThat(
      Map<?, M> map) {
    return expect.about(ProtoTruth.protos()).that(map);
  }

  protected final <M extends Message> MultimapWithProtoValuesSubject<?, ?, M, ?> expectThat(
      Multimap<?, M> multimap) {
    return expect.about(ProtoTruth.protos()).that(multimap);
  }

  protected final ProtoSubject<?, Message> expectThatWithMessage(
      String msg, @Nullable Message message) {
    return expect.withMessage(msg).about(ProtoTruth.protos()).that(message);
  }

  /**
   * Assert than an AssertionError was expected before we got to this line.
   *
   * <p>Intended for try-catch blocks, where the previous line is meant to throw an AssertionError
   * and it's a test failure if we reach the next line in the try block. The catch block should
   * inspect the message, possibly with 'expectFailureNotMissing' to ensure that the expected
   * AssertionError was thrown.
   */
  protected final void expectedFailure() {
    fail("Expected failure.");
  }

  protected final void expectFailureNotMissing(AssertionError expected) {
    expectNoSubstr(expected, "Expected failure.");
  }

  protected final void expectIsEqualToFailed(AssertionError e) {
    expectRegex(
        e,
        "Not true that messages compare equal\\.\\s*"
            + "(Differences were found:\\n.*|No differences were reported\\..*)");
  }

  protected final void expectIsNotEqualToFailed(AssertionError e) {
    expectRegex(
        e,
        "Not true that messages compare not equal\\.\\s*"
            + "(Only ignorable differences were found:\\n.*|"
            + "No differences were found\\..*)");
  }

  // TODO(cgruber): These probably belong in ThrowableSubject.
  protected final void expectRegex(Throwable t, String regex) {
    expect
        .withMessage(String.format("Expected <%s> to match '%s'.", regex, t.getMessage()))
        .that(Pattern.compile(regex, Pattern.DOTALL).matcher(t.getMessage()).matches())
        .isTrue();
  }

  protected final void expectNoRegex(Throwable t, String regex) {
    expect
        .withMessage(String.format("Expected <%s> to match '%s'.", regex, t.getMessage()))
        .that(Pattern.compile(regex, Pattern.DOTALL).matcher(t.getMessage()).matches())
        .isFalse();
  }

  protected final void expectSubstr(Throwable t, String substr) {
    expect.that(t.getMessage()).contains(substr);
  }

  protected final void expectNoSubstr(Throwable t, String substr) {
    expect.that(t.getMessage()).doesNotContain(substr);
  }

  protected static final <T> ImmutableList<T> listOf(T... elements) {
    return ImmutableList.copyOf(elements);
  }

  protected static final <T> T[] arrayOf(T... elements) {
    return elements;
  }

  @SuppressWarnings("unchecked")
  protected static final <K, V> ImmutableMap<K, V> mapOf(K k0, V v0, Object... rest) {
    Preconditions.checkArgument(rest.length % 2 == 0, "Uneven args: %s", rest.length);

    ImmutableMap.Builder<K, V> builder = new ImmutableMap.Builder<>();
    builder.put(k0, v0);
    for (int i = 0; i < rest.length; i += 2) {
      builder.put((K) rest[i], (V) rest[i + 1]);
    }
    return builder.build();
  }

  @SuppressWarnings("unchecked")
  protected static final <K, V> ImmutableMultimap<K, V> multimapOf(K k0, V v0, Object... rest) {
    Preconditions.checkArgument(rest.length % 2 == 0, "Uneven args: %s", rest.length);

    ImmutableMultimap.Builder<K, V> builder = new ImmutableMultimap.Builder<>();
    builder.put(k0, v0);
    for (int i = 0; i < rest.length; i += 2) {
      builder.put((K) rest[i], (V) rest[i + 1]);
    }
    return builder.build();
  }
}
