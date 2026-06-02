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
import com.google.common.truth.MapSubject;
import com.google.protobuf.Message;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for {@link MapWithProtoValuesSubject}.
 *
 * <p>Individual equality fuzzing is thoroughly tested by {@link ProtoSubjectTest}, while fuzzy
 * equality testing is thoroughly tested by {@link com.google.common.truth.MapSubjectTest}. Thus, we
 * simply check that all of the exposed methods work in basic cases, and trust that the
 * implementation ensures correctness in the cross-product of the many ways one can do things.
 */
@RunWith(Parameterized.class)
public class MapWithProtoValuesSubjectTest extends ProtoSubjectTestBase {

  private final Message message1 = parse("o_int: 1 r_string: \"foo\" r_string: \"bar\"");
  private final Message eqMessage1 = parse("o_int: 1 r_string: \"foo\" r_string: \"bar\"");
  private final Message eqRepeatedMessage1 = parse("o_int: 1 r_string: \"bar\" r_string: \"foo\"");
  private final Message eqIgnoredMessage1 = parse("o_int: 2 r_string: \"foo\" r_string: \"bar\"");
  private final Message message2 = parse("o_int: 3 r_string: \"baz\" r_string: \"qux\"");
  private final Message eqMessage2 = parse("o_int: 3 r_string: \"baz\" r_string: \"qux\"");
  private final Message eqRepeatedMessage2 = parse("o_int: 3 r_string: \"qux\" r_string: \"baz\"");
  private final Message eqIgnoredMessage2 = parse("o_int: 4 r_string: \"baz\" r_string: \"qux\"");

  private final int ignoreFieldNumber = getFieldNumber("o_int");

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return ProtoSubjectTestBase.parameters();
  }

  public MapWithProtoValuesSubjectTest(TestType testType) {
    super(testType);
  }

  @Test
  public void plain_isEqualTo() {
    expectThat(mapOf(1, message1, 2, message2)).isEqualTo(mapOf(2, eqMessage2, 1, eqMessage1));
    expectThat(mapOf(1, message2)).isNotEqualTo(mapOf(1, message1));

    expectFailure(
        whenTesting ->
            whenTesting
                .that(mapOf(1, message2, 2, message1))
                .isEqualTo(mapOf(1, eqMessage1, 2, eqMessage2)));

    expectFailure(
        whenTesting -> whenTesting.that(mapOf(1, message1)).isNotEqualTo(mapOf(1, eqMessage1)));
  }

  @Test
  public void plain_isEmpty() {
    expectThat(ImmutableMap.<Object, Message>of()).isEmpty();
    expectThat(mapOf(1, message1)).isNotEmpty();

    expectFailure(whenTesting -> whenTesting.that(mapOf(1, message1)).isEmpty());

    expectFailure(whenTesting -> whenTesting.that(ImmutableMap.<Object, Message>of()).isNotEmpty());
  }

  @Test
  public void plain_hasSize() {
    expectThat(mapOf(1, message1, 2, message2)).hasSize(2);

    expectFailure(whenTesting -> whenTesting.that(mapOf(1, message1)).hasSize(3));
  }

  @Test
  public void plain_containsKey() {
    expectThat(mapOf(1, message1, 2, message2)).containsKey(1);
    expectThat(mapOf(1, message1, 2, message2)).doesNotContainKey(3);

    expectFailure(whenTesting -> whenTesting.that(mapOf(1, message1, 2, message2)).containsKey(3));

    expectFailure(
        whenTesting -> whenTesting.that(mapOf(1, message1, 2, message2)).doesNotContainKey(2));
  }

  @Test
  public void plain_containsEntry() {
    expectThat(mapOf(1, message1, 2, message2)).containsEntry(2, eqMessage2);
    expectThat(mapOf(1, message1, 2, message2)).doesNotContainEntry(1, eqMessage2);

    expectFailure(
        whenTesting ->
            whenTesting.that(mapOf(1, message1, 2, message2)).containsEntry(2, eqMessage1));

    expectFailure(
        whenTesting ->
            whenTesting.that(mapOf(1, message1, 2, message2)).doesNotContainEntry(2, eqMessage2));
  }

  @Test
  public void plain_containsExactly() {
    expectThat(mapOf(1, message1, 2, message2)).containsExactly(2, eqMessage2, 1, eqMessage1);
    expectThat(mapOf(1, message1, 2, message2))
        .containsExactly(1, eqMessage1, 2, eqMessage2)
        .inOrder();
    expectThat(mapOf(1, message1, 2, message2))
        .containsExactlyEntriesIn(mapOf(2, eqMessage2, 1, eqMessage1));
    expectThat(mapOf(1, message1, 2, message2))
        .containsExactlyEntriesIn(mapOf(1, eqMessage1, 2, eqMessage2))
        .inOrder();

    expectFailure(
        whenTesting ->
            whenTesting.that(mapOf(1, message1)).containsExactly(1, eqMessage1, 2, eqMessage2));

    expectFailure(
        whenTesting ->
            whenTesting
                .that(mapOf(1, message1, 2, message2))
                .containsExactly(2, eqMessage2, 1, eqMessage1)
                .inOrder());

    expectFailure(
        whenTesting ->
            whenTesting
                .that(mapOf(1, message1))
                .containsExactlyEntriesIn(mapOf(2, eqMessage2, 1, eqMessage1)));

    expectFailure(
        whenTesting ->
            whenTesting
                .that(mapOf(1, message1, 2, message2))
                .containsExactlyEntriesIn(mapOf(2, eqMessage2, 1, eqMessage1))
                .inOrder());
  }

  @Test
  public void fluent_containsEntry() {
    expectThat(mapOf(1, message1, 2, message2))
        .ignoringFieldsForValues(ignoreFieldNumber)
        .containsEntry(1, eqIgnoredMessage1);
    expectThat(mapOf(1, message1, 2, message2))
        .ignoringRepeatedFieldOrderForValues()
        .doesNotContainEntry(1, eqIgnoredMessage1);

    AssertionError failure =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(mapOf(1, message1, 2, message2))
                    .ignoringFieldsForValues(ignoreFieldNumber)
                    .containsEntry(1, eqRepeatedMessage1));
    expectThatFailure(failure)
        .hasMessageThat()
        .contains(
            "is equivalent according to "
                + "assertThat(proto)"
                + ".ignoringFields("
                + fullMessageName()
                + ".o_int)"
                + ".isEqualTo(target)");

    failure =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(mapOf(1, message1, 2, message2))
                    .ignoringRepeatedFieldOrderForValues()
                    .doesNotContainEntry(1, eqRepeatedMessage1));
    expectThatFailure(failure)
        .hasMessageThat()
        .contains(
            "is equivalent according to "
                + "assertThat(proto).ignoringRepeatedFieldOrder().isEqualTo(target)");
  }

  @Test
  public void fluent_containsExactly() {
    expectThat(mapOf(1, message1, 2, message2))
        .ignoringFieldsForValues(ignoreFieldNumber)
        .containsExactly(2, eqIgnoredMessage2, 1, eqIgnoredMessage1);
    expectThat(mapOf(1, message1, 2, message2))
        .ignoringRepeatedFieldOrderForValues()
        .containsExactly(1, eqRepeatedMessage1, 2, eqRepeatedMessage2)
        .inOrder();
    expectThat(mapOf(1, message1, 2, message2))
        .ignoringFieldsForValues(ignoreFieldNumber)
        .containsExactlyEntriesIn(mapOf(2, eqIgnoredMessage2, 1, eqIgnoredMessage1));
    expectThat(mapOf(1, message1, 2, message2))
        .ignoringRepeatedFieldOrderForValues()
        .containsExactlyEntriesIn(mapOf(1, eqRepeatedMessage1, 2, eqRepeatedMessage2))
        .inOrder();

    expectFailure(
        whenTesting ->
            whenTesting
                .that(mapOf(1, message1))
                .ignoringRepeatedFieldOrderForValues()
                .containsExactly(1, eqRepeatedMessage1, 2, eqMessage2));

    expectFailure(
        whenTesting ->
            whenTesting
                .that(mapOf(1, message1, 2, message2))
                .ignoringFieldsForValues(ignoreFieldNumber)
                .containsExactly(2, eqIgnoredMessage2, 1, eqIgnoredMessage1)
                .inOrder());

    expectFailure(
        whenTesting ->
            whenTesting
                .that(mapOf(1, message1))
                .ignoringRepeatedFieldOrderForValues()
                .containsExactlyEntriesIn(mapOf(2, eqRepeatedMessage2, 1, eqRepeatedMessage1)));

    expectFailure(
        whenTesting ->
            whenTesting
                .that(mapOf(1, message1, 2, message2))
                .ignoringFieldsForValues(ignoreFieldNumber)
                .containsExactlyEntriesIn(mapOf(2, eqIgnoredMessage2, 1, eqIgnoredMessage1))
                .inOrder());
  }

  @Test
  public void compareMultipleMessageTypes() {
    // Don't run this test twice.
    if (!testIsRunOnce()) {
      return;
    }

    expectThat(
            ImmutableMap.of(
                2,
                TestMessage2.newBuilder().addRString("foo").addRString("bar").build(),
                3,
                TestMessage3.newBuilder().addRString("baz").addRString("qux").build()))
        .ignoringRepeatedFieldOrderForValues()
        .containsExactly(
            3, TestMessage3.newBuilder().addRString("qux").addRString("baz").build(),
            2, TestMessage2.newBuilder().addRString("bar").addRString("foo").build());
  }

  @Test
  public void methodNamesEndWithForValues() {
    checkMethodNamesEndWithForValues(MapWithProtoValuesSubject.class, MapSubject.class);
    checkMethodNamesEndWithForValues(MapWithProtoValuesFluentAssertion.class, MapSubject.class);
  }
}
