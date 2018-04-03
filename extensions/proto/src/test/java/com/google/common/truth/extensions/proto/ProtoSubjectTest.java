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

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import java.util.Collection;
import java.util.Map;
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
  public void testDifferentClasses() throws InvalidProtocolBufferException {
    Message message = parse("o_int: 3");
    DynamicMessage dynamicMessage =
        DynamicMessage.parseFrom(message.getDescriptorForType(), message.toByteString());

    expectThat(message).isEqualTo(dynamicMessage);
    expectThat(dynamicMessage).isEqualTo(message);
  }

  @Test
  public void testFullDiffOnlyWhenRelevant() {
    // There are no matches, so 'Full diff' should not be printed.
    expectFailureWhenTesting().that(parse("o_int: 3")).isEqualTo(parse("o_int: 4"));
    expectThatFailure().hasMessageThat().doesNotContain("Full diff");

    // r_string is matched, so the 'Full diff' contains extra information.
    expectFailureWhenTesting()
        .that(parse("o_int: 3 r_string: 'abc'"))
        .isEqualTo(parse("o_int: 4 r_string: 'abc'"));
    expectThatFailure().hasMessageThat().contains("Full diff");
  }

  @Test
  public void testIgnoringFieldAbsence() {
    Message message = parse("o_int: 3");
    Message diffMessage = parse("o_int: 3 o_enum: DEFAULT");

    // Make sure the implementation is reflexive.
    if (isProto3()) {
      expectThat(diffMessage).isEqualTo(message);
      expectThat(message).isEqualTo(diffMessage);
    } else {
      expectThat(diffMessage).isNotEqualTo(message);
      expectThat(message).isNotEqualTo(diffMessage);
    }
    expectThat(diffMessage).ignoringFieldAbsence().isEqualTo(message);
    expectThat(message).ignoringFieldAbsence().isEqualTo(diffMessage);

    if (!isProto3()) {
      Message customDefaultMessage = parse("o_int: 3");
      Message diffCustomDefaultMessage = parse("o_int: 3 o_long_defaults_to_42: 42");

      expectThat(diffCustomDefaultMessage).isNotEqualTo(customDefaultMessage);
      expectThat(diffCustomDefaultMessage).ignoringFieldAbsence().isEqualTo(customDefaultMessage);
      expectThat(customDefaultMessage).isNotEqualTo(diffCustomDefaultMessage);
      expectThat(customDefaultMessage).ignoringFieldAbsence().isEqualTo(diffCustomDefaultMessage);
    }

    if (!isProto3()) {
      expectFailureWhenTesting().that(diffMessage).isEqualTo(message);
      expectIsEqualToFailed();
      expectThatFailure().hasMessageThat().contains("added: o_enum: DEFAULT");
    }

    expectFailureWhenTesting().that(diffMessage).ignoringFieldAbsence().isNotEqualTo(message);
    expectIsNotEqualToFailed();
    expectThatFailure().hasMessageThat().contains("matched: o_int: 3");
    if (!isProto3()) {
      // Proto 3 doesn't cover the field at all when it's not set.
      expectThatFailure().hasMessageThat().contains("matched: o_enum: DEFAULT");
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

    expectFailureWhenTesting().that(diffMessage).isEqualTo(message);
    expectIsEqualToFailed();
    expectThatFailure().hasMessageThat().contains("added: 93[0]: 42");
    expectThatFailure().hasMessageThat().contains("deleted: 99[0]: 42");

    expectFailureWhenTesting().that(diffMessage).ignoringFieldAbsence().isNotEqualTo(message);
    expectIsNotEqualToFailed();
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

    expectFailureWhenTesting().that(eqMessage).isEqualTo(message);
    expectIsEqualToFailed();
    expectThatFailure().hasMessageThat().contains("modified: r_string[0]: \"foo\" -> \"bar\"");
    expectThatFailure().hasMessageThat().contains("modified: r_string[1]: \"bar\" -> \"foo\"");

    expectFailureWhenTesting().that(eqMessage).ignoringRepeatedFieldOrder().isNotEqualTo(message);
    expectIsNotEqualToFailed();
    expectThatFailure().hasMessageThat().contains("moved: r_string[0] -> r_string[1]: \"foo\"");
    expectThatFailure().hasMessageThat().contains("moved: r_string[1] -> r_string[0]: \"bar\"");

    expectFailureWhenTesting().that(diffMessage).ignoringRepeatedFieldOrder().isEqualTo(message);
    expectIsEqualToFailed();
    expectThatFailure().hasMessageThat().contains("matched: r_string[0]: \"foo\"");
    expectThatFailure().hasMessageThat().contains("moved: r_string[1] -> r_string[2]: \"bar\"");
    expectThatFailure().hasMessageThat().contains("added: r_string[1]: \"foo\"");
  }

  @Test
  public void testDoubleTolerance() {
    Message message = parse("o_double: 1.0");
    Message diffMessage = parse("o_double: 1.1");

    expectThat(diffMessage).isNotEqualTo(message);
    expectThat(diffMessage).usingDoubleTolerance(0.2).isEqualTo(message);
    expectThat(diffMessage).usingDoubleTolerance(0.05).isNotEqualTo(message);
    expectThat(diffMessage).usingFloatTolerance(0.2f).isNotEqualTo(message);

  }

  @Test
  public void testFloatTolerance() {
    Message message = parse("o_float: 1.0");
    Message diffMessage = parse("o_float: 1.1");

    expectThat(diffMessage).isNotEqualTo(message);
    expectThat(diffMessage).usingFloatTolerance(0.2f).isEqualTo(message);
    expectThat(diffMessage).usingFloatTolerance(0.05f).isNotEqualTo(message);
    expectThat(diffMessage).usingDoubleTolerance(0.2).isNotEqualTo(message);

  }

  @Test
  public void testComparingExpectedFieldsOnly() {
    Message message = parse("o_int: 3 r_string: 'foo'");
    Message narrowMessage = parse("o_int: 3");

    expectThat(message).comparingExpectedFieldsOnly().isEqualTo(narrowMessage);
    expectThat(narrowMessage).comparingExpectedFieldsOnly().isNotEqualTo(message);

    expectFailureWhenTesting()
        .that(message)
        .comparingExpectedFieldsOnly()
        .isNotEqualTo(narrowMessage);
    expectThatFailure().hasMessageThat().contains("ignored: r_string");
  }

  @Test
  public void testIgnoringExtraRepeatedFieldElements_respectingOrder() {
    Message message = parse("r_string: 'foo' r_string: 'bar'");
    Message eqMessage = parse("r_string: 'foo' r_string: 'foobar' r_string: 'bar'");
    Message diffMessage = parse("r_string: 'bar' r_string: 'foobar' r_string: 'foo'");

    expectThat(eqMessage).ignoringExtraRepeatedFieldElements().isEqualTo(message);
    expectThat(diffMessage).ignoringExtraRepeatedFieldElements().isNotEqualTo(message);

    expectFailureWhenTesting()
        .that(eqMessage)
        .ignoringExtraRepeatedFieldElements()
        .isNotEqualTo(message);
    expectThatFailure()
        .hasMessageThat()
        .contains("ignored: r_string[?] -> r_string[1]: \"foobar\"");

    expectFailureWhenTesting()
        .that(diffMessage)
        .ignoringExtraRepeatedFieldElements()
        .isEqualTo(message);
    expectThatFailure()
        .hasMessageThat()
        .contains("out_of_order: r_string[1] -> r_string[0]: \"bar\"");
    expectThatFailure().hasMessageThat().contains("moved: r_string[0] -> r_string[2]: \"foo\"");
  }

  @Test
  public void testIgnoringExtraRepeatedFieldElements_ignoringOrder() {
    Message message = parse("r_string: 'foo' r_string: 'bar'");
    Message eqMessage = parse("r_string: 'baz' r_string: 'bar' r_string: 'qux' r_string: 'foo'");
    Message diffMessage = parse("r_string: 'abc' r_string: 'foo' r_string: 'xyz'");

    expectThat(eqMessage)
        .ignoringExtraRepeatedFieldElements()
        .ignoringRepeatedFieldOrder()
        .isEqualTo(message);
    expectThat(diffMessage)
        .ignoringExtraRepeatedFieldElements()
        .ignoringRepeatedFieldOrder()
        .isNotEqualTo(message);

    expectFailureWhenTesting()
        .that(diffMessage)
        .ignoringExtraRepeatedFieldElements()
        .ignoringRepeatedFieldOrder()
        .isEqualTo(message);
    expectThatFailure().hasMessageThat().contains("moved: r_string[0] -> r_string[1]: \"foo\"");
    expectThatFailure().hasMessageThat().contains("deleted: r_string[1]: \"bar\"");
  }

  @Test
  public void testIgnoringExtraRepeatedFieldElements_empty() {
    Message message = parse("o_int: 2");
    Message diffMessage = parse("o_int: 2 r_string: 'error'");

    expectThat(diffMessage).ignoringExtraRepeatedFieldElements().isNotEqualTo(message);
    expectThat(diffMessage)
        .ignoringExtraRepeatedFieldElements()
        .ignoringRepeatedFieldOrder()
        .isNotEqualTo(message);

    expectThat(diffMessage)
        .comparingExpectedFieldsOnly()
        .ignoringExtraRepeatedFieldElements()
        .isEqualTo(message);
    expectThat(diffMessage)
        .comparingExpectedFieldsOnly()
        .ignoringExtraRepeatedFieldElements()
        .ignoringRepeatedFieldOrder()
        .isEqualTo(message);
  }

  // Utility which fills a proto map field, based on the java.util.Map.
  private Message makeProtoMap(Map<String, Integer> map) {
    StringBuilder textProto = new StringBuilder();
    for (String key : map.keySet()) {
      int value = map.get(key);
      textProto
          .append("test_message_map { key: '")
          .append(key)
          .append("' value { o_int: ")
          .append(value)
          .append(" } } ");
    }
    return parse(textProto.toString());
  }

  @Test
  public void testIgnoringExtraRepeatedFieldElements_map() {
    Message message = makeProtoMap(ImmutableMap.of("foo", 2, "bar", 3));
    Message eqMessage = makeProtoMap(ImmutableMap.of("bar", 3, "qux", 4, "foo", 2));
    Message diffMessage = makeProtoMap(ImmutableMap.of("quz", 5, "foo", 2));
    Message emptyMessage = parse("");

    expectThat(eqMessage).ignoringExtraRepeatedFieldElements().isEqualTo(message);
    expectThat(eqMessage)
        .ignoringRepeatedFieldOrder()
        .ignoringExtraRepeatedFieldElements()
        .isEqualTo(message);
    expectThat(diffMessage).ignoringExtraRepeatedFieldElements().isNotEqualTo(message);
    expectThat(diffMessage)
        .ignoringExtraRepeatedFieldElements()
        .ignoringRepeatedFieldOrder()
        .isNotEqualTo(message);

    expectThat(message).ignoringExtraRepeatedFieldElements().isNotEqualTo(emptyMessage);

    expectFailureWhenTesting()
        .that(diffMessage)
        .ignoringExtraRepeatedFieldElements()
        .isEqualTo(message);
    expectThatFailure().hasMessageThat().contains("matched: test_message_map[\"foo\"].o_int: 2");
    expectThatFailure().hasMessageThat().contains("ignored: test_message_map[\"quz\"]");
    expectThatFailure().hasMessageThat().contains("deleted: test_message_map[\"bar\"]");
  }

  @Test
  public void testReportingMismatchesOnly_isEqualTo() {
    Message message = parse("r_string: \"foo\" r_string: \"bar\"");
    Message diffMessage = parse("r_string: \"foo\" r_string: \"not_bar\"");

    expectFailureWhenTesting().that(diffMessage).isEqualTo(message);
    expectIsEqualToFailed();
    expectThatFailure().hasMessageThat().contains("foo");
    expectThatFailure().hasMessageThat().contains("bar");
    expectThatFailure().hasMessageThat().contains("not_bar");

    expectFailureWhenTesting().that(diffMessage).reportingMismatchesOnly().isEqualTo(message);
    expectIsEqualToFailed();
    expectThatFailure().hasMessageThat().doesNotContain("foo");
    expectThatFailure().hasMessageThat().contains("bar");
    expectThatFailure().hasMessageThat().contains("not_bar");
  }

  @Test
  public void testReportingMismatchesOnly_isNotEqualTo() {
    Message message = parse("o_int: 33 r_string: \"foo\" r_string: \"bar\"");
    Message diffMessage = parse("o_int: 33 r_string: \"bar\" r_string: \"foo\"");

    expectFailureWhenTesting().that(diffMessage).ignoringRepeatedFieldOrder().isNotEqualTo(message);
    expectIsNotEqualToFailed();
    expectThatFailure().hasMessageThat().contains("33");
    expectThatFailure().hasMessageThat().contains("foo");
    expectThatFailure().hasMessageThat().contains("bar");

    expectFailureWhenTesting()
        .that(diffMessage)
        .ignoringRepeatedFieldOrder()
        .reportingMismatchesOnly()
        .isNotEqualTo(message);
    expectIsNotEqualToFailed();
    expectThatFailure().hasMessageThat().doesNotContain("33");
    expectThatFailure().hasMessageThat().doesNotContain("foo");
    expectThatFailure().hasMessageThat().doesNotContain("bar");
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

    expectFailureWhenTesting()
        .that(parsePartial("o_required_string_message: {}"))
        .hasAllRequiredFields();
    expectFailureMatches(
        "Not true that <.*> has all required fields set\\.\\s*Missing: \\[.*\\].*");
    expectThatFailure().hasMessageThat().contains("[o_required_string_message.required_string]");

    expectFailureWhenTesting()
        .that(parsePartial("r_required_string_message: {} r_required_string_message: {}"))
        .hasAllRequiredFields();
    expectFailureMatches(
        "Not true that <.*> has all required fields set\\.\\s*Missing: \\[.*\\].*");
    expectThatFailure().hasMessageThat().contains("r_required_string_message[0].required_string");
    expectThatFailure().hasMessageThat().contains("r_required_string_message[1].required_string");
  }
}
