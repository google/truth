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
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
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
    super(TestType.IMMUTABLE_PROTO2);
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

  @Test
  public void testMapOverloads_assertAbout() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");

    assertAbout(protos()).that(mapOf(1, message1, 2, message2)).containsEntry(2, eqMessage2);
  }

  @Test
  public void testMapOverloads_testMessages_normalMethods() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");
    Object object1 = message1;
    Object object2 = message2;
    ImmutableMap<Object, TestMessage2> actualMessages = mapOf(1, message1, 2, message2);

    assertThat(actualMessages).containsExactly(1, message1, 2, message2).inOrder();
    assertThat(actualMessages).containsExactly(1, object1, 2, object2).inOrder();
    assertThat(actualMessages).hasSize(2);
    assertThat(actualMessages).isEqualTo(mapOf(2, eqMessage2, 1, object1));
    assertThat(actualMessages).isNotEqualTo(mapOf(1, object2, 2, object1));
  }

  @Test
  public void testMapOverloads_testMessages_specializedMethods() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 message3 = parse("o_int: 3 r_string: \"baz\"");
    TestMessage2 message4 = parse("o_int: 4 r_string: \"qux\"");
    ImmutableMap<Integer, TestMessage2> actualMessages = mapOf(1, message1, 2, message2);

    assertThat(actualMessages)
        .ignoringFieldsForValues(getFieldNumber("o_int"), getFieldNumber("r_string"))
        .containsExactly(1, message3, 2, message4)
        .inOrder();
  }

  @Test
  public void testMapOverloads_objects_actuallyMessages() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");
    Object object1 = message1;
    Object object2 = message2;
    ImmutableMap<String, Object> actualObjects = mapOf("a", object1, "b", object2);

    assertThat(actualObjects).containsExactly("a", message1, "b", message2).inOrder();
    assertThat(actualObjects).hasSize(2);
    assertThat(actualObjects).containsEntry("b", eqMessage2);
  }

  @Test
  public void testMapOverloads_objects_actuallyNotMessages() {
    TestMessage2 message1 = TestMessage2.newBuilder().setOInt(1).addRString("foo").build();
    TestMessage2 message2 = TestMessage2.newBuilder().setOInt(2).addRString("bar").build();
    ImmutableMap<Object, Object> altActualObjects = mapOf("a", "Foo!", "b", 42);

    assertThat(altActualObjects).containsExactly("a", "Foo! Bar!".substring(0, 4), "b", 21 * 2);
    assertThat(altActualObjects).doesNotContainEntry("a", message1);
    assertThat(altActualObjects).doesNotContainEntry("b", message2);
  }

  @Test
  public void testMultimapOverloads_assertAbout() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");

    assertAbout(protos())
        .that(multimapOf(1, message1, 1, message2, 2, message1))
        .containsEntry(1, eqMessage2);
  }

  @Test
  public void testMultimapOverloads_assertAbout_listAndSet() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");
    ImmutableMultimap<Integer, TestMessage2> multimap =
        multimapOf(1, message1, 1, message2, 2, message1);
    ImmutableListMultimap<Integer, TestMessage2> listMultimap =
        ImmutableListMultimap.copyOf(multimap);
    ImmutableSetMultimap<Integer, TestMessage2> setMultimap = ImmutableSetMultimap.copyOf(multimap);

    assertAbout(protos())
        .that(multimap)
        .ignoringRepeatedFieldOrderForValues()
        .containsExactlyEntriesIn(listMultimap);
    assertAbout(protos())
        .that(listMultimap)
        .ignoringRepeatedFieldOrderForValues()
        .containsExactlyEntriesIn(setMultimap);
    assertAbout(protos())
        .that(setMultimap)
        .ignoringRepeatedFieldOrderForValues()
        .containsExactlyEntriesIn(multimap);
    assertAbout(protos()).that(listMultimap).isNotEqualTo(setMultimap);
  }

  @Test
  public void testMultimapOverloads_testMessages_normalMethods() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");
    Object object1 = message1;
    Object object2 = message2;
    ImmutableMultimap<Object, TestMessage2> actualMessages =
        multimapOf(1, message1, 1, message2, 2, message1);

    assertThat(actualMessages)
        .containsExactlyEntriesIn(multimapOf(1, message1, 1, eqMessage2, 2, message1))
        .inOrder();
    assertThat(actualMessages).hasSize(3);
    assertThat(actualMessages).isEqualTo(multimapOf(2, message1, 1, message1, 1, eqMessage2));
    assertThat(actualMessages).isNotEqualTo(multimapOf(2, object1, 1, object2, 1, object1));
  }

  @Test
  public void testMultimapOverloads_testMessages_normalMethods_listAndSet() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");
    Object object1 = message1;
    Object object2 = message2;
    ImmutableMultimap<Object, TestMessage2> multimap =
        multimapOf(1, message1, 1, message2, 2, message1);
    ImmutableListMultimap<Object, TestMessage2> listMultimap =
        ImmutableListMultimap.copyOf(multimap);
    ImmutableSetMultimap<Object, TestMessage2> setMultimap = ImmutableSetMultimap.copyOf(multimap);

    assertThat(multimap)
        .containsExactlyEntriesIn(multimapOf(1, message1, 1, eqMessage2, 2, message1))
        .inOrder();
    assertThat(listMultimap)
        .containsExactlyEntriesIn(multimapOf(1, message1, 1, eqMessage2, 2, message1))
        .inOrder();
    assertThat(setMultimap)
        .containsExactlyEntriesIn(multimapOf(1, message1, 1, eqMessage2, 2, message1))
        .inOrder();
    assertThat(multimap).hasSize(3);
    assertThat(listMultimap).hasSize(3);
    assertThat(setMultimap).hasSize(3);
  }

  @Test
  public void testMultimapOverloads_testMessages_specializedMethods() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 message3 = parse("o_int: 3 r_string: \"baz\"");
    TestMessage2 message4 = parse("o_int: 4 r_string: \"qux\"");
    ImmutableMultimap<Integer, TestMessage2> actualMessages =
        multimapOf(1, message1, 1, message2, 2, message1);

    assertThat(actualMessages)
        .ignoringFieldsForValues(getFieldNumber("o_int"), getFieldNumber("r_string"))
        .containsExactlyEntriesIn(multimapOf(1, message3, 1, message4, 2, message3))
        .inOrder();
    assertThat(actualMessages)
        .valuesForKey(1)
        .ignoringFields(getFieldNumber("o_int"), getFieldNumber("r_string"))
        .containsExactly(message3, message4)
        .inOrder();
  }

  @Test
  public void testMultimapOverloads_objects_actuallyMessages() {
    TestMessage2 message1 = parse("o_int: 1 r_string: \"foo\"");
    TestMessage2 message2 = parse("o_int: 2 r_string: \"bar\"");
    TestMessage2 eqMessage2 = parse("o_int: 2 r_string: \"bar\"");
    Object object1 = message1;
    Object object2 = message2;
    ImmutableMultimap<String, Object> actualObjects =
        multimapOf("a", object1, "a", object2, "b", object1);

    assertThat(actualObjects)
        .containsExactlyEntriesIn(multimapOf("a", message1, "a", message2, "b", message1))
        .inOrder();
    assertThat(actualObjects).hasSize(3);
    assertThat(actualObjects).containsEntry("a", eqMessage2);
  }

  @Test
  public void testMultimapOverloads_objects_actuallyNotMessages() {
    TestMessage2 message1 = TestMessage2.newBuilder().setOInt(1).addRString("foo").build();
    TestMessage2 message2 = TestMessage2.newBuilder().setOInt(2).addRString("bar").build();
    ImmutableMultimap<Object, Object> altActualObjects =
        multimapOf("a", "Foo!", "a", "Baz!", "b", 42);

    assertThat(altActualObjects)
        .containsExactlyEntriesIn(
            multimapOf("b", 21 * 2, "a", "Ba" + "z!", "a", "Foo! Bar!".substring(0, 4)));
    assertThat(altActualObjects).doesNotContainEntry("a", message1);
    assertThat(altActualObjects).doesNotContainEntry("b", message2);
  }
}
