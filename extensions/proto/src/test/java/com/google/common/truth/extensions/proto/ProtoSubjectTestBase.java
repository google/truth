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

import com.google.auto.value.AutoValue;
import com.google.common.truth.Expect;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.junit.Rule;

/**
 * Base class for testing {@link ProtoSubject} methods. Generics are required for using FieldScopes,
 * so we must provide a generic base class instead of using raw Parameterized elements.
 */
public class ProtoSubjectTestBase<M extends Message> {

  // Type information for subclasses.
  @AutoValue
  abstract static class TestType<M extends Message> {
    abstract M defaultInstance();

    abstract boolean isProto3();
  }

  protected static final TestType<TestMessage2> PROTO2 =
      new AutoValue_ProtoSubjectTestBase_TestType<>(TestMessage2.getDefaultInstance(), false);
  protected static final TestType<TestMessage3> PROTO3 =
      new AutoValue_ProtoSubjectTestBase_TestType<>(TestMessage3.getDefaultInstance(), true);

  private static final TextFormat.Parser PARSER =
      TextFormat.Parser.newBuilder()
          .setAllowUnknownFields(false)
          .setSingularOverwritePolicy(
              TextFormat.Parser.SingularOverwritePolicy.FORBID_SINGULAR_OVERWRITES)
          .build();

  @Rule public final Expect expect = Expect.create();

  private final M defaultInstance;
  private final boolean isProto3;

  protected ProtoSubjectTestBase(TestType<M> testType) {
    this.defaultInstance = testType.defaultInstance();
    this.isProto3 = testType.isProto3();
  }

  protected final Message.Builder newBuilder() {
    return defaultInstance.toBuilder();
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

  @SuppressWarnings("unchecked")
  protected final M parse(String textProto) {
    try {
      Message.Builder builder = defaultInstance.toBuilder();
      PARSER.merge(textProto, builder);
      return (M) builder.build();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  protected final M parsePartial(String textProto) {
    try {
      Message.Builder builder = defaultInstance.toBuilder();
      PARSER.merge(textProto, builder);
      return (M) builder.buildPartial();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  protected final boolean isProto3() {
    return isProto3;
  }

  protected ProtoSubject<? extends ProtoSubject<?, M>, M> expectThat(@Nullable M message) {
    return expect.about(ProtoSubject.<M>protos()).that(message);
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
  protected void expectRegex(Throwable t, String regex) {
    expect
        .withFailureMessage(String.format("Expected <%s> to match '%s'.", regex, t.getMessage()))
        .that(Pattern.compile(regex, Pattern.DOTALL).matcher(t.getMessage()).matches())
        .isTrue();
  }

  protected void expectNoRegex(Throwable t, String regex) {
    expect
        .withFailureMessage(String.format("Expected <%s> to match '%s'.", regex, t.getMessage()))
        .that(Pattern.compile(regex, Pattern.DOTALL).matcher(t.getMessage()).matches())
        .isFalse();
  }

  protected void expectSubstr(Throwable t, String substr) {
    expect.that(t.getMessage()).contains(substr);
  }

  protected void expectNoSubstr(Throwable t, String substr) {
    expect.that(t.getMessage()).doesNotContain(substr);
  }
}
