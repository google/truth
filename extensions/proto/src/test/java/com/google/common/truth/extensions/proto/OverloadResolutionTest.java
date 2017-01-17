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

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static com.google.common.truth.extensions.proto.ProtoTruth.protos;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test to ensure that Truth.assertThat and ProtoTruth.assertThat can coexist while statically
 * imported. The tests themselves are simple and dumb, as what's really being tested here is whether
 * or not this file compiles.
 */
@RunWith(JUnit4.class)
public class OverloadResolutionTest extends ProtoSubjectTestBase {
  public OverloadResolutionTest() {
    // We don't bother testing Proto3 because it's immaterial to this test, and we want to ensure
    // that using Iterable<MyMessage> works, not just Iterable<Message>.
    super(PROTO2);
  }

  @Override
  protected TestMessage2 parse(String string) {
    return (TestMessage2) super.parse(string);
  }

  @Test
  public void testObjectOverloads_testMessages_normalMethods() {
    TestMessage2 message = parse("r_string: \"foo\" r_string: \"bar\"");
    TestMessage2 eqMessage = parse("r_string: \"foo\" r_string: \"bar\"");
    TestMessage2 diffMessage = parse("r_string: \"bar\" r_string: \"foo\"");
    Object object = message;
    Object eqObject = eqMessage;
    Object diffObject = diffMessage;

    assertThat(message).isSameAs(object);
    assertThat(message).isNotSameAs(eqMessage);
    assertThat(message).isEqualTo(eqMessage);
    assertThat(message).isNotEqualTo(diffMessage);
    assertThat(message).isEqualTo(eqObject);
    assertThat(message).isNotEqualTo(diffObject);
  }

  @Test
  public void testObjectOverloads_testMessages_specializedMethods() {
    TestMessage2 message = parse("r_string: \"foo\" r_string: \"bar\"");
    TestMessage2 diffMessage = parse("r_string: \"bar\" r_string: \"foo\"");

    assertThat(message).ignoringRepeatedFieldOrder().isEqualTo(diffMessage);
  }

  @Test
  public void testObjectOverloads_objects_actuallyMessages() {
    TestMessage2 message = parse("r_string: \"foo\" r_string: \"bar\"");
    TestMessage2 eqMessage = parse("r_string: \"foo\" r_string: \"bar\"");
    TestMessage2 diffMessage = parse("r_string: \"bar\" r_string: \"foo\"");
    Object object = message;
    Object eqObject = eqMessage;
    Object diffObject = diffMessage;

    assertThat(object).isSameAs(message);
    assertThat(object).isNotSameAs(eqObject);
    assertThat(object).isEqualTo(eqObject);
    assertThat(object).isNotEqualTo(diffObject);
    assertThat(object).isEqualTo(eqMessage);
    assertThat(object).isNotEqualTo(diffMessage);
  }

  @Test
  public void testObjectOverloads_objects_actuallyNotMessages() {
    TestMessage2 message = parse("r_string: \"foo\" r_string: \"bar\"");
    Object altObject = 1111;
    Object eqAltObject = (1 + 10 + 100 + 1000);

    assertThat(altObject).isEqualTo(eqAltObject);
    assertThat(altObject).isNotEqualTo(message);
  }

  @Test
  public void testIterableOverloads_assertAbout() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");

    assertAbout(protos()).that(listOf(message1, message2)).contains(eqMessage2);
  }

  @Test
  public void testIterableOverloads_testMessages_normalMethods() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 message3 = parse("o_int: 3 r_string: \"baz\"");
    TestMessage2 message4 = parse("o_int: 4 r_string: \"qux\"");
    Object object1 = message1;
    Object object2 = message2;
    ImmutableSet<TestMessage2> actualMessages = ImmutableSet.of(message1, message2);

    assertThat(actualMessages).containsExactly(message1, message2).inOrder();
    assertThat(actualMessages).containsExactly(object1, object2).inOrder();
    assertThat(actualMessages).hasSize(2);
    assertThat(actualMessages).containsAnyOf(message3, eqMessage2, message4);
  }

  @Test
  public void testIterableOverloads_testMessages_specializedMethods() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 message3 = parse("o_int: 3 r_string: \"baz\"");
    TestMessage2 message4 = parse("o_int: 4 r_string: \"qux\"");
    ImmutableSet<TestMessage2> actualMessages = ImmutableSet.of(message1, message2);

    assertThat(actualMessages)
        .ignoringFields(getFieldNumber("o_int"), getFieldNumber("r_string"))
        .containsExactly(message3, message4)
        .inOrder();
  }

  @Test
  public void testIterableOverloads_objects_actuallyMessages() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 message3 = parse("o_int: 3 r_string: \"baz\"");
    TestMessage2 message4 = parse("o_int: 4 r_string: \"qux\"");
    Object object1 = message1;
    Object object2 = message2;
    ImmutableList<Object> actualObjects = ImmutableList.of(object1, object2);

    assertThat(actualObjects).containsExactly(message1, message2).inOrder();
    assertThat(actualObjects).hasSize(2);
    assertThat(actualObjects).containsAnyOf(message3, eqMessage2, message4);
  }

  @Test
  public void testIterableOverloads_objects_actuallyNotMessages() {
    TestMessage2 message1 = TestMessage2.newBuilder().setOInt(1).addRString("foo").build();
    TestMessage2 message2 = TestMessage2.newBuilder().setOInt(2).addRString("bar").build();
    ImmutableList<Object> altActualObjects = ImmutableList.of("Foo!", 42);

    assertThat(altActualObjects).containsExactly(21 * 2, "Foo! Bar!".substring(0, 4));
    assertThat(altActualObjects).containsNoneOf(message1, message2);
  }
}
