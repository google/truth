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
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistry;
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
  public void testDifferentDynamicDescriptors() throws InvalidProtocolBufferException {
    // Only test once.
    if (!isProto3()) {
      return;
    }

    DynamicMessage message1 =
        DynamicMessage.parseFrom(
            TestMessage2.getDescriptor(),
            TestMessage2.newBuilder().setOInt(43).build().toByteString());
    DynamicMessage message2 =
        DynamicMessage.parseFrom(
            TestMessage3.getDescriptor(),
            TestMessage3.newBuilder().setOInt(43).build().toByteString());

    expectFailureWhenTesting().that(message1).isEqualTo(message2);
    expectThatFailure().hasMessageThat().contains("different descriptors");
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

  @Test
  public void testIgnoringFieldAbsence_anyMessage() {
    Message message = parse("o_int: 3");
    Message diffMessage = parse("o_int: 3 o_any_message: {}");

    expectThat(diffMessage).ignoringFieldAbsence().isEqualTo(message);
    expectThat(message).ignoringFieldAbsence().isEqualTo(diffMessage);

    expectFailureWhenTesting().that(diffMessage).isEqualTo(message);
    expectIsEqualToFailed();
    expectThatFailure().hasMessageThat().contains("added: o_any_message:");

    expectFailureWhenTesting().that(diffMessage).ignoringFieldAbsence().isNotEqualTo(message);
    expectIsNotEqualToFailed();
    expectThatFailure().hasMessageThat().contains("matched: o_int: 3");
    expectThatFailure().hasMessageThat().contains("matched: o_any_message");
  }

  @Test
  public void testIgnoringFieldAbsence_scoped() {
    Message message = parse("o_sub_test_message: { o_test_message: {} }");
    Message emptyMessage = parse("");
    Message partialMessage = parse("o_sub_test_message: {}");

    // All three are equal if we ignore field absence entirely.
    expectThat(emptyMessage).ignoringFieldAbsence().isEqualTo(message);
    expectThat(partialMessage).ignoringFieldAbsence().isEqualTo(message);

    // If we ignore only o_sub_test_message.o_test_message, only the partial message is equal.
    FieldDescriptor subTestMessageField = getFieldDescriptor("o_sub_test_message");
    FieldDescriptor subTestMessageTestMessageField =
        checkNotNull(subTestMessageField.getMessageType().findFieldByName("o_test_message"));
    expectThat(partialMessage)
        .ignoringFieldAbsenceOfFieldDescriptors(subTestMessageTestMessageField)
        .isEqualTo(message);
    expectFailureWhenTesting()
        .that(emptyMessage)
        .ignoringFieldAbsenceOfFieldDescriptors(subTestMessageTestMessageField)
        .isEqualTo(message);
    expectIsEqualToFailed();
    expectThatFailure().hasMessageThat().contains("deleted: o_sub_test_message");

    // But, we can ignore both.
    expectThat(partialMessage)
        .ignoringFieldAbsenceOfFieldDescriptors(subTestMessageField)
        .ignoringFieldAbsenceOfFieldDescriptors(subTestMessageTestMessageField)
        .isEqualTo(message);
    expectThat(partialMessage)
        .ignoringFieldAbsenceOfFieldDescriptors(subTestMessageField, subTestMessageTestMessageField)
        .isEqualTo(message);
    expectThat(emptyMessage)
        .ignoringFieldAbsenceOfFieldDescriptors(subTestMessageField)
        .ignoringFieldAbsenceOfFieldDescriptors(subTestMessageTestMessageField)
        .isEqualTo(message);
    expectThat(emptyMessage)
        .ignoringFieldAbsenceOfFieldDescriptors(subTestMessageField, subTestMessageTestMessageField)
        .isEqualTo(message);

    try {
      expectThat(message)
          .ignoringFieldAbsenceOfFieldDescriptors(getFieldDescriptor("r_string"))
          .isEqualTo(message);
      fail("Expected failure.");
    } catch (Exception e) {
      assertThat(e).hasMessageThat().contains("r_string");
      assertThat(e).hasMessageThat().contains("repeated fields cannot be absent");
    }

    if (isProto3()) {
      try {
        expectThat(message)
            .ignoringFieldAbsenceOfFieldDescriptors(getFieldDescriptor("o_double"))
            .isEqualTo(message);
        fail("Expected failure.");
      } catch (Exception e) {
        assertThat(e).hasMessageThat().contains("o_double");
        assertThat(e).hasMessageThat().contains("is a field without presence");
      }
    } else {
      expectThat(message)
          .ignoringFieldAbsenceOfFieldDescriptors(getFieldDescriptor("o_double"))
          .isEqualTo(message);
    }
  }

  @Test
  public void testUnknownFields() throws InvalidProtocolBufferException {
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

    expectThat(message).isEqualTo(clone(message));
    expectThat(message).ignoringRepeatedFieldOrder().isEqualTo(clone(message));
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

    expectThat(nestedMessage).isEqualTo(clone(nestedMessage));
    expectThat(nestedMessage).ignoringRepeatedFieldOrder().isEqualTo(clone(nestedMessage));
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
  public void testRepeatedFieldOrder_scoped() {
    Message message =
        parse("r_string: 'a' r_string: 'b' o_sub_test_message: { r_string: 'c' r_string: 'd' }");
    Message diffSubMessage =
        parse("r_string: 'a' r_string: 'b' o_sub_test_message: { r_string: 'd' r_string: 'c' }");
    Message diffAll =
        parse("r_string: 'b' r_string: 'a' o_sub_test_message: { r_string: 'd' r_string: 'c' }");

    FieldDescriptor rootMessageRepeatedfield = getFieldDescriptor("r_string");
    FieldDescriptor subMessageRepeatedField =
        checkNotNull(
            getFieldDescriptor("o_sub_test_message").getMessageType().findFieldByName("r_string"));

    // Ignoring all repeated field order tests pass.
    expectThat(diffSubMessage).ignoringRepeatedFieldOrder().isEqualTo(message);
    expectThat(diffAll).ignoringRepeatedFieldOrder().isEqualTo(message);

    // Ignoring only some results in failures.
    //
    // TODO(user): Whether we check failure message substrings or not is currently ad-hoc on a
    // per-test basis, and not especially maintainable.  We should make the tests consistent
    // according to some reasonable rule in this regard.
    expectFailureWhenTesting()
        .that(diffSubMessage)
        .ignoringRepeatedFieldOrderOfFieldDescriptors(rootMessageRepeatedfield)
        .isEqualTo(message);
    expectIsEqualToFailed();
    expectThatFailure()
        .hasMessageThat()
        .contains("modified: o_sub_test_message.r_string[0]: \"c\" -> \"d\"");
    expectThat(diffSubMessage)
        .ignoringRepeatedFieldOrderOfFieldDescriptors(subMessageRepeatedField)
        .isEqualTo(message);

    expectThat(diffAll)
        .ignoringRepeatedFieldOrderOfFieldDescriptors(rootMessageRepeatedfield)
        .isNotEqualTo(message);
    expectThat(diffAll)
        .ignoringRepeatedFieldOrderOfFieldDescriptors(subMessageRepeatedField)
        .isNotEqualTo(message);
    expectThat(diffAll)
        .ignoringRepeatedFieldOrderOfFieldDescriptors(
            rootMessageRepeatedfield, subMessageRepeatedField)
        .isEqualTo(message);

    try {
      expectThat(message)
          .ignoringRepeatedFieldOrderOfFields(getFieldNumber("o_int"))
          .isEqualTo(message);
      fail("Expected failure.");
    } catch (Exception e) {
      assertThat(e).hasMessageThat().contains("o_int");
      assertThat(e).hasMessageThat().contains("is not a repeated field");
    }
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
  public void testDoubleTolerance_defaultValue() {
    Message message = parse("o_double: 0.0");
    Message defaultInstance = parse("");
    Message diffMessage = parse("o_double: 0.01");

    expectThat(diffMessage).isNotEqualTo(message);

    if (isProto3()) {
      // The default value is ignored and treated as unset.
      // We treat is as equivalent to being set to 0.0 because there is no distinction in Proto 3.
      expectThat(message).isEqualTo(defaultInstance);
      expectThat(diffMessage).usingDoubleTolerance(0.1).isEqualTo(message);
      expectThat(diffMessage).usingDoubleTolerance(0.1).ignoringFieldAbsence().isEqualTo(message);
    } else {
      // The default value can be set or unset, so we respect it.
      expectThat(message).isNotEqualTo(defaultInstance);

      expectThat(diffMessage).usingDoubleTolerance(0.1).isEqualTo(message);
      expectThat(diffMessage).usingDoubleTolerance(0.1).isNotEqualTo(defaultInstance);
      expectThat(diffMessage).usingDoubleTolerance(0.1).ignoringFieldAbsence().isEqualTo(message);
      expectThat(diffMessage)
          .usingDoubleTolerance(0.1)
          .ignoringFieldAbsence()
          .isEqualTo(defaultInstance);

      // The same logic applies for a specified default; not available in Proto 3.
      Message message2 = parse("o_double_defaults_to_42: 42.0");
      Message diffMessage2 = parse("o_double_defaults_to_42: 42.01");

      expectThat(diffMessage2).usingDoubleTolerance(0.1).isEqualTo(message2);
      expectThat(diffMessage2).usingDoubleTolerance(0.1).isNotEqualTo(defaultInstance);
      expectThat(diffMessage2).usingDoubleTolerance(0.1).ignoringFieldAbsence().isEqualTo(message2);
      expectThat(diffMessage2)
          .usingDoubleTolerance(0.1)
          .ignoringFieldAbsence()
          .isEqualTo(defaultInstance);
    }
  }

  @Test
  public void testDoubleTolerance_scoped() {
    Message message = parse("o_double: 1.0 o_double2: 1.0");
    Message diffMessage = parse("o_double: 1.1 o_double2: 1.5");

    int doubleFieldNumber = getFieldNumber("o_double");
    int double2FieldNumber = getFieldNumber("o_double2");

    expectThat(diffMessage).usingDoubleTolerance(0.6).isEqualTo(message);
    expectThat(diffMessage).usingDoubleTolerance(0.2).isNotEqualTo(message);

    // usingDoubleTolerance*() statements override all previous statements.
    expectThat(diffMessage)
        .usingDoubleTolerance(0.2)
        .usingDoubleToleranceForFields(0.6, double2FieldNumber)
        .isEqualTo(message);
    expectThat(diffMessage)
        .usingDoubleToleranceForFields(0.6, doubleFieldNumber, double2FieldNumber)
        .usingDoubleToleranceForFields(0.2, doubleFieldNumber)
        .isEqualTo(message);

    expectThat(diffMessage)
        .usingDoubleTolerance(0.2)
        .usingDoubleToleranceForFields(0.6, doubleFieldNumber)
        .isNotEqualTo(message);
    expectThat(diffMessage)
        .usingDoubleToleranceForFields(0.6, double2FieldNumber)
        .usingDoubleTolerance(0.2)
        .isNotEqualTo(message);
    expectThat(diffMessage)
        .usingDoubleToleranceForFields(0.6, doubleFieldNumber, double2FieldNumber)
        .usingDoubleToleranceForFields(0.2, double2FieldNumber)
        .isNotEqualTo(message);

    try {
      expectThat(message)
          .usingDoubleToleranceForFields(3.14159, getFieldNumber("o_int"))
          .isEqualTo(message);
      fail("Expected failure.");
    } catch (Exception e) {
      assertThat(e).hasMessageThat().contains("o_int");
      assertThat(e).hasMessageThat().contains("is not a double field");
    }
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
  public void testFloatTolerance_defaultValue() {
    Message message = parse("o_float: 0.0");
    Message defaultInstance = parse("");
    Message diffMessage = parse("o_float: 0.01");

    expectThat(diffMessage).isNotEqualTo(message);

    if (isProto3()) {
      // The default value is ignored and treated as unset.
      // We treat is as equivalent to being set to 0.0f because there is no distinction in Proto 3.
      expectThat(message).isEqualTo(defaultInstance);
      expectThat(diffMessage).usingFloatTolerance(0.1f).isEqualTo(message);
      expectThat(diffMessage).usingFloatTolerance(0.1f).ignoringFieldAbsence().isEqualTo(message);
    } else {
      // The default value can be set or unset, so we respect it.
      expectThat(message).isNotEqualTo(defaultInstance);

      expectThat(diffMessage).usingFloatTolerance(0.1f).isEqualTo(message);
      expectThat(diffMessage).usingFloatTolerance(0.1f).isNotEqualTo(defaultInstance);
      expectThat(diffMessage).usingFloatTolerance(0.1f).ignoringFieldAbsence().isEqualTo(message);
      expectThat(diffMessage)
          .usingFloatTolerance(0.1f)
          .ignoringFieldAbsence()
          .isEqualTo(defaultInstance);

      // The same logic applies for a specified default; not available in Proto 3.
      Message message2 = parse("o_float_defaults_to_42: 42.0");
      Message diffMessage2 = parse("o_float_defaults_to_42: 42.01");

      expectThat(diffMessage2).usingFloatTolerance(0.1f).isEqualTo(message2);
      expectThat(diffMessage2).usingFloatTolerance(0.1f).isNotEqualTo(defaultInstance);
      expectThat(diffMessage2).usingFloatTolerance(0.1f).ignoringFieldAbsence().isEqualTo(message2);
      expectThat(diffMessage2)
          .usingFloatTolerance(0.1f)
          .ignoringFieldAbsence()
          .isEqualTo(defaultInstance);
    }
  }

  @Test
  public void testFloatTolerance_scoped() {
    Message message = parse("o_float: 1.0 o_float2: 1.0");
    Message diffMessage = parse("o_float: 1.1 o_float2: 1.5");

    int floatFieldNumber = getFieldNumber("o_float");
    int float2FieldNumber = getFieldNumber("o_float2");

    expectThat(diffMessage).usingFloatTolerance(0.6f).isEqualTo(message);
    expectThat(diffMessage).usingFloatTolerance(0.2f).isNotEqualTo(message);

    // usingFloatTolerance*() statements override all previous statements.
    expectThat(diffMessage)
        .usingFloatTolerance(0.2f)
        .usingFloatToleranceForFields(0.6f, float2FieldNumber)
        .isEqualTo(message);
    expectThat(diffMessage)
        .usingFloatTolerance(0.2f)
        .usingFloatToleranceForFields(0.6f, floatFieldNumber)
        .isNotEqualTo(message);
    expectThat(diffMessage)
        .usingFloatToleranceForFields(0.6f, float2FieldNumber)
        .usingFloatTolerance(0.2f)
        .isNotEqualTo(message);
    expectThat(diffMessage)
        .usingFloatToleranceForFields(0.6f, floatFieldNumber, float2FieldNumber)
        .usingFloatToleranceForFields(0.2f, floatFieldNumber)
        .isEqualTo(message);
    expectThat(diffMessage)
        .usingFloatToleranceForFields(0.6f, floatFieldNumber, float2FieldNumber)
        .usingFloatToleranceForFields(0.2f, float2FieldNumber)
        .isNotEqualTo(message);

    try {
      expectThat(message)
          .usingFloatToleranceForFields(3.1416f, getFieldNumber("o_int"))
          .isEqualTo(message);
      fail("Expected failure.");
    } catch (Exception e) {
      assertThat(e).hasMessageThat().contains("o_int");
      assertThat(e).hasMessageThat().contains("is not a float field");
    }
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

  @Test
  public void testIgnoringExtraRepeatedFieldElements_scoped() {
    Message message = parse("r_string: 'a' o_sub_test_message: { r_string: 'c' }");
    Message diffMessage =
        parse("r_string: 'a' o_sub_test_message: { r_string: 'b' r_string: 'c' }");

    FieldDescriptor rootRepeatedField = getFieldDescriptor("r_string");
    FieldDescriptor subMessageRepeatedField =
        checkNotNull(
            getFieldDescriptor("o_sub_test_message").getMessageType().findFieldByName("r_string"));

    expectThat(diffMessage).ignoringExtraRepeatedFieldElements().isEqualTo(message);
    expectThat(diffMessage)
        .ignoringExtraRepeatedFieldElementsOfFieldDescriptors(rootRepeatedField)
        .isNotEqualTo(message);
    expectThat(diffMessage)
        .ignoringExtraRepeatedFieldElementsOfFieldDescriptors(subMessageRepeatedField)
        .isEqualTo(message);
    expectThat(diffMessage)
        .ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
            rootRepeatedField, subMessageRepeatedField)
        .isEqualTo(message);

    try {
      expectThat(message)
          .ignoringExtraRepeatedFieldElementsOfFields(getFieldNumber("o_int"))
          .isEqualTo(message);
      fail("Expected failure.");
    } catch (Exception e) {
      assertThat(e).hasMessageThat().contains("o_int");
      assertThat(e).hasMessageThat().contains("it cannot contain extra elements");
    }
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
    expectThatFailure()
        .factKeys()
        .containsExactly(
            "expected to have all required fields set", "but was missing", "proto was");
    expectThatFailure()
        .factValue("but was missing")
        .isEqualTo("[o_required_string_message.required_string]");

    expectFailureWhenTesting()
        .that(parsePartial("r_required_string_message: {} r_required_string_message: {}"))
        .hasAllRequiredFields();
    expectThatFailure()
        .factKeys()
        .containsExactly(
            "expected to have all required fields set", "but was missing", "proto was");
    expectThatFailure()
        .factValue("but was missing")
        .contains("r_required_string_message[0].required_string");
    expectThatFailure()
        .factValue("but was missing")
        .contains("r_required_string_message[1].required_string");
  }

  @Test
  public void testAnyMessage_notEqual_diffPrintsExpandedAny() {
    String typeUrl =
        isProto3()
            ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
            : "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";
    Message msgWithAny =
        parse("" + "o_int: 42 " + "o_any_message: { [" + typeUrl + "]: {r_string: \"foo\"} }");
    Message msgWithoutAny = parse("o_int: 42");

    expectFailureWhenTesting()
        .that(msgWithAny)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isEqualTo(msgWithoutAny);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "added: o_any_message: \n" + "[" + typeUrl + "] {\n" + "  r_string: \"foo\"\n" + "}\n");

    expectFailureWhenTesting()
        .that(msgWithoutAny)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isEqualTo(msgWithAny);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "deleted: o_any_message: \n"
                + "["
                + typeUrl
                + "] {\n"
                + "  r_string: \"foo\"\n"
                + "}\n");
  }

  @Test
  public void testRepeatedAnyMessage_notEqual_diffPrintsExpandedAny() {
    String typeUrl =
        isProto3()
            ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
            : "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";
    String fooSubMessage = "{ [" + typeUrl + "]: {r_string: \"foo\"} }";
    String barSubMessage = "{ [" + typeUrl + "]: {r_string: \"bar\"} }";
    String bazSubMessage = "{ [" + typeUrl + "]: {r_string: \"baz\"} }";
    Message msgWithFooBar =
        parse(
            ""
                + "o_int: 42 "
                + "r_any_message: "
                + fooSubMessage
                + "r_any_message: "
                + barSubMessage);
    Message msgWithBazFoo =
        parse(
            ""
                + "o_int: 42 "
                + "r_any_message: "
                + bazSubMessage
                + "r_any_message: "
                + fooSubMessage);

    expectFailureWhenTesting()
        .that(msgWithFooBar)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .ignoringRepeatedFieldOrder()
        .isEqualTo(msgWithBazFoo);

    expectThatFailure()
        .hasMessageThat()
        .contains(
            ""
                + "moved: r_any_message[1] -> r_any_message[0]:\n"
                + "added: r_any_message[1]: \n"
                + "["
                + typeUrl
                + "] {\n"
                + "  r_string: \"bar\"\n"
                + "}\n"
                + "deleted: r_any_message[0]: \n"
                + "["
                + typeUrl
                + "] {\n"
                + "  r_string: \"baz\"\n"
                + "}");
  }

  @Test
  public void testAnyMessagesWithDifferentTypes() {
    String typeUrl =
        isProto3()
            ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
            : "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";
    String diffTypeUrl =
        isProto3()
            ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubSubTestMessage3"
            : "type.googleapis.com/com.google.common.truth.extensions.proto.SubSubTestMessage2";

    Message message = parse("o_any_message: { [" + typeUrl + "]: {r_string: \"foo\"} }");
    Message diffMessage = parse("o_any_message: { [" + diffTypeUrl + "]: {r_string: \"bar\"} }");

    expectThat(message)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isNotEqualTo(diffMessage);

    expectFailureWhenTesting()
        .that(message)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isEqualTo(diffMessage);
    expectThatFailure().hasMessageThat().contains("modified: o_any_message.type_url");
    expectThatFailure()
        .hasMessageThat()
        .containsMatch("modified: o_any_message.value:.*bar.*->.*foo.*");
  }

  @Test
  public void testAnyMessageCompareWithEmptyAnyMessage() {
    String typeUrl =
        isProto3()
            ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
            : "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";

    Message messageWithAny = parse("o_any_message: { [" + typeUrl + "]: {o_int: 1} }");
    Message messageWithEmptyAny = parse("o_any_message: { }");

    expectThat(messageWithAny)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isNotEqualTo(messageWithEmptyAny);
    expectThat(messageWithEmptyAny)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isNotEqualTo(messageWithAny);

    expectFailureWhenTesting()
        .that(messageWithAny)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isEqualTo(messageWithEmptyAny);
    expectThatFailure().hasMessageThat().contains("modified: o_any_message.type_url");
    expectThatFailure().hasMessageThat().contains("modified: o_any_message.value");

    expectFailureWhenTesting()
        .that(messageWithEmptyAny)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isEqualTo(messageWithAny);
    expectThatFailure().hasMessageThat().contains("modified: o_any_message.type_url");
    expectThatFailure().hasMessageThat().contains("modified: o_any_message.value");
  }

  @Test
  public void testAnyMessageComparedWithDynamicMessage() throws InvalidProtocolBufferException {
    String typeUrl =
        isProto3()
            ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
            : "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";

    Message messageWithAny = parse("o_any_message: { [" + typeUrl + "]: {o_int: 1} }");
    FieldDescriptor fieldDescriptor = getFieldDescriptor("o_any_message");
    Any message = (Any) messageWithAny.getField(fieldDescriptor);
    DynamicMessage dynamicMessage =
        DynamicMessage.parseFrom(
            Any.getDescriptor(), message.toByteString(), ExtensionRegistry.getEmptyRegistry());

    expectThat(dynamicMessage)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isEqualTo(message);
    expectThat(message)
        .unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
        .isEqualTo(dynamicMessage);
  }

  @Test
  public void testMapWithDefaultKeysAndValues() throws InvalidProtocolBufferException {
    Descriptor descriptor = getFieldDescriptor("o_int").getContainingType();
    String defaultString = "";
    int defaultInt32 = 0;
    Message message = makeProtoMap(ImmutableMap.of(defaultString, 1, "foo", defaultInt32));
    Message dynamicMessage =
        DynamicMessage.parseFrom(
            descriptor, message.toByteString(), ExtensionRegistry.getEmptyRegistry());
    expectThat(message).isEqualTo(dynamicMessage);
  }
}
