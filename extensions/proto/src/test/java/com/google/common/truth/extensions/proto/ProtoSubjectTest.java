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

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Unit tests for {@link ProtoSubject}. */
@RunWith(Parameterized.class)
public class ProtoSubjectTest extends ProtoSubjectTestBase {

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return ProtoSubjectTestBase.parameters();
  }

  public ProtoSubjectTest(TestType testType) {
    super(testType);
  }

  @Test
  public void testIgnoringFieldAbsence() {
    Message message = parse("o_int: 3");
    Message diffMessage = parse("o_int: 3 o_enum: DEFAULT");

    if (isProto3()) {
      expectThat(diffMessage).isEqualTo(message);
    } else {
      expectThat(diffMessage).isNotEqualTo(message);
    }
    expectThat(diffMessage).ignoringFieldAbsence().isEqualTo(message);

    if (!isProto3()) {
      Message customDefaultMessage = parse("o_int: 3");
      Message diffCustomDefaultMessage = parse("o_int: 3 o_long_defaults_to_42: 42");

      expectThat(diffCustomDefaultMessage).isNotEqualTo(customDefaultMessage);
      expectThat(diffCustomDefaultMessage).ignoringFieldAbsence().isEqualTo(customDefaultMessage);
    }

    if (!isProto3()) {
      try {
        assertThat(diffMessage).isEqualTo(message);
        expectedFailure();
      } catch (AssertionError e) {
        expectIsEqualToFailed(e);
        expectSubstr(e, "added: o_enum: DEFAULT");
      }
    }

    try {
      assertThat(diffMessage).ignoringFieldAbsence().isNotEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsNotEqualToFailed(e);
      expectSubstr(e, "matched: o_int : 3");
      if (!isProto3()) {
        // Proto 3 doesn't cover the field at all when it's not set.
        expectSubstr(e, "matched: o_enum : DEFAULT");
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testUnknownFields() throws InvalidProtocolBufferException {
    if (isProto3()) {
      // Proto 3 doesn't support unknown fields.
      return;
    }

    Message message =
        fromUnknownFields(
            UnknownFieldSet.newBuilder()
                .addField(99, UnknownFieldSet.Field.newBuilder().addVarint(42).build())
                .build());
    Message diffMessage =
        fromUnknownFields(
            UnknownFieldSet.newBuilder()
                .addField(93, UnknownFieldSet.Field.newBuilder().addVarint(42).build())
                .build());

    expectThat(diffMessage).isNotEqualTo(message);
    expectThat(diffMessage).ignoringFieldAbsence().isEqualTo(message);

    try {
      assertThat(diffMessage).isEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsEqualToFailed(e);
      expectSubstr(e, "added: 93[0]: 42");
      expectSubstr(e, "deleted: 99[0]: 42");
    }

    try {
      assertThat(diffMessage).ignoringFieldAbsence().isNotEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsNotEqualToFailed(e);
    }
  }

  @Test
  public void testRepeatedFieldOrder() {
    Message message = parse("r_string: \"foo\" r_string: \"bar\"");
    Message eqMessage = parse("r_string: \"bar\" r_string: \"foo\"");
    Message diffMessage = parse("r_string: \"foo\" r_string: \"foo\" r_string: \"bar\"");

    expectThat(message).isEqualTo(message.toBuilder().build());
    expectThat(message).ignoringRepeatedFieldOrder().isEqualTo(message.toBuilder().build());
    expectThat(diffMessage).isNotEqualTo(message);
    expectThat(diffMessage).ignoringRepeatedFieldOrder().isNotEqualTo(message);
    expectThat(eqMessage).isNotEqualTo(message);
    expectThat(eqMessage).ignoringRepeatedFieldOrder().isEqualTo(message);

    Message nestedMessage =
        parse(
            "r_test_message: { o_int: 33 r_string: \"foo\" r_string: \"bar\" } "
                + "r_test_message: { o_int: 44 r_string: \"baz\" r_string: \"qux\" } ");
    Message diffNestedMessage =
        parse(
            "r_test_message: { o_int: 33 r_string: \"qux\" r_string: \"baz\" } "
                + "r_test_message: { o_int: 44 r_string: \"bar\" r_string: \"foo\" } ");
    Message eqNestedMessage =
        parse(
            "r_test_message: { o_int: 44 r_string: \"qux\" r_string: \"baz\" } "
                + "r_test_message: { o_int: 33 r_string: \"bar\" r_string: \"foo\" } ");

    expectThat(nestedMessage).isEqualTo(nestedMessage.toBuilder().build());
    expectThat(nestedMessage)
        .ignoringRepeatedFieldOrder()
        .isEqualTo(nestedMessage.toBuilder().build());
    expectThat(diffNestedMessage).isNotEqualTo(nestedMessage);
    expectThat(diffNestedMessage).ignoringRepeatedFieldOrder().isNotEqualTo(nestedMessage);
    expectThat(eqNestedMessage).isNotEqualTo(nestedMessage);
    expectThat(eqNestedMessage).ignoringRepeatedFieldOrder().isEqualTo(nestedMessage);

    try {
      assertThat(eqMessage).isEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsEqualToFailed(e);
      expectSubstr(e, "modified: r_string[0]: \"foo\" -> \"bar\"");
      expectSubstr(e, "modified: r_string[1]: \"bar\" -> \"foo\"");
    }

    try {
      assertThat(eqMessage).ignoringRepeatedFieldOrder().isNotEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsNotEqualToFailed(e);
      expectSubstr(e, "moved: r_string[0] -> r_string[1] : \"foo\"");
      expectSubstr(e, "moved: r_string[1] -> r_string[0] : \"bar\"");
    }

    try {
      assertThat(diffMessage).ignoringRepeatedFieldOrder().isEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsEqualToFailed(e);
      expectSubstr(e, "matched: r_string[0] : \"foo\"");
      expectSubstr(e, "moved: r_string[1] -> r_string[2] : \"bar\"");
      expectSubstr(e, "added: r_string[1]: \"foo\"");
    }
  }

  @Test
  public void testReportingMismatchesOnly_isEqualTo() {
    Message message = parse("r_string: \"foo\" r_string: \"bar\"");
    Message diffMessage = parse("r_string: \"foo\" r_string: \"not_bar\"");

    try {
      assertThat(diffMessage).isEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsEqualToFailed(e);
      expectSubstr(e, "foo");
      expectSubstr(e, "bar");
      expectSubstr(e, "not_bar");
    }

    try {
      assertThat(diffMessage).reportingMismatchesOnly().isEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsEqualToFailed(e);
      expectNoSubstr(e, "foo");
      expectSubstr(e, "bar");
      expectSubstr(e, "not_bar");
    }
  }

  @Test
  public void testReportingMismatchesOnly_isNotEqualTo() {
    Message message = parse("o_int: 33 r_string: \"foo\" r_string: \"bar\"");
    Message diffMessage = parse("o_int: 33 r_string: \"bar\" r_string: \"foo\"");

    try {
      assertThat(diffMessage).ignoringRepeatedFieldOrder().isNotEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsNotEqualToFailed(e);
      expectSubstr(e, "33");
      expectSubstr(e, "foo");
      expectSubstr(e, "bar");
    }

    try {
      assertThat(diffMessage)
          .ignoringRepeatedFieldOrder()
          .reportingMismatchesOnly()
          .isNotEqualTo(message);
      expectedFailure();
    } catch (AssertionError e) {
      expectIsNotEqualToFailed(e);
      expectNoSubstr(e, "33");
      expectNoSubstr(e, "foo");
      expectNoSubstr(e, "bar");
    }
  }

  @Test
  public void testHasAllRequiredFields() {
    // Proto 3 doesn't have required fields.
    if (isProto3()) {
      return;
    }

    expectThat(parsePartial("")).hasAllRequiredFields();
    expectThat(parsePartial("o_required_string_message: { required_string: \"foo\" }"))
        .hasAllRequiredFields();

    try {
      assertThat(parsePartial("o_required_string_message: {}")).hasAllRequiredFields();
      expectedFailure();
    } catch (AssertionError e) {
      expectRegex(e, "Not true that <.*> has all required fields set\\.\\s*Missing: \\[.*\\].*");
      expectSubstr(e, "[o_required_string_message.required_string]");
    }

    try {
      assertThat(parsePartial("r_required_string_message: {} r_required_string_message: {}"))
          .hasAllRequiredFields();
      expectedFailure();
    } catch (AssertionError e) {
      expectRegex(e, "Not true that <.*> has all required fields set\\.\\s*Missing: \\[.*\\].*");
      expectSubstr(e, "r_required_string_message[0].required_string");
      expectSubstr(e, "r_required_string_message[1].required_string");
    }
  }
}
