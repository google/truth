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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.protobuf.Message;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for {@link MultimapWithProtoValuesSubject}.
 *
 * <p>Individual equality fuzzing is thoroughly tested by {@link ProtoSubjectTest}, while fuzzy
 * equality testing is thoroughly tested by {@link com.google.common.truth.MultimapSubjectTest}.
 * Thus, we simply check that all of the exposed methods work in basic cases, and trust that the
 * implementation ensures correctness in the cross-product of the many ways one can do things.
 */
@RunWith(Parameterized.class)
public class MultimapWithProtoValuesSubjectTest extends ProtoSubjectTestBase {

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

  public MultimapWithProtoValuesSubjectTest(TestType testType) {
    super(testType);
  }

  @Test
  public void testPlain_isEmpty() {
    expectThat(ImmutableMultimap.of()).isEmpty();
    expectThat(multimapOf(1, message1)).isNotEmpty();

    try {
      assertThat(multimapOf(1, message1)).isEmpty();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(ImmutableMap.of()).isNotEmpty();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_hasSize() {
    expectThat(multimapOf(1, message1, 1, message2, 2, message1)).hasSize(3);

    try {
      assertThat(multimapOf(1, message1)).hasSize(3);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_containsKey() {
    expectThat(multimapOf(1, message1, 1, message2, 2, message1)).containsKey(1);
    expectThat(multimapOf(1, message1, 1, message2, 2, message1)).doesNotContainKey(3);

    try {
      assertThat(multimapOf(1, message1, 1, message2, 2, message1)).containsKey(3);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(multimapOf(1, message1, 1, message2, 2, message1)).doesNotContainKey(2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_containsEntry() {
    expectThat(multimapOf(1, message1, 1, message2, 2, message1)).containsEntry(1, eqMessage2);
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .doesNotContainEntry(2, eqMessage2);

    try {
      assertThat(multimapOf(1, message1, 1, message2, 2, message1)).containsEntry(2, eqMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(multimapOf(1, message1, 1, message2, 2, message1))
          .doesNotContainEntry(1, eqMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_containsExactlyEntriesIn() {
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .containsExactlyEntriesIn(multimapOf(1, eqMessage2, 2, eqMessage1, 1, eqMessage1));
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .containsExactlyEntriesIn(multimapOf(1, eqMessage1, 1, eqMessage2, 2, eqMessage1))
        .inOrder();

    try {
      assertThat(multimapOf(1, message1))
          .containsExactlyEntriesIn(multimapOf(1, eqMessage1, 2, eqMessage2));
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(multimapOf(1, message1, 2, message2))
          .containsExactlyEntriesIn(multimapOf(2, eqMessage2, 1, eqMessage1))
          .inOrder();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_valuesForKey() {
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .valuesForKey(1)
        .containsExactly(eqMessage2, eqMessage1);
    expectThat(multimapOf(1, message1, 1, message2, 2, message1)).valuesForKey(2).hasSize(1);

    try {
      assertThat(multimapOf(1, message1, 1, message2, 2, message1))
          .valuesForKey(1)
          .containsExactly(eqMessage2, eqMessage1)
          .inOrder();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testFluent_containsEntry() {
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .ignoringFieldsForValues(ignoreFieldNumber)
        .containsEntry(1, eqIgnoredMessage2);
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .ignoringRepeatedFieldOrderForValues()
        .doesNotContainEntry(1, eqIgnoredMessage2);

    try {
      assertThat(multimapOf(1, message1, 1, message2, 2, message1))
          .ignoringFieldsForValues(ignoreFieldNumber)
          .containsEntry(1, eqRepeatedMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectSubstr(
          expected,
          "is equivalent according to "
              + "assertThat(proto)"
              + ".ignoringFields("
              + fullMessageName()
              + ".o_int)"
              + ".isEqualTo(target)");
    }

    try {
      assertThat(multimapOf(1, message1, 1, message2, 2, message1))
          .ignoringRepeatedFieldOrderForValues()
          .doesNotContainEntry(1, eqRepeatedMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectSubstr(
          expected,
          "is equivalent according to "
              + "assertThat(proto).ignoringRepeatedFieldOrder().isEqualTo(target)");
    }
  }

  @Test
  public void testFluent_containsExactlyEntriesIn() {
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .ignoringFieldsForValues(ignoreFieldNumber)
        .containsExactlyEntriesIn(
            multimapOf(1, eqIgnoredMessage2, 2, eqIgnoredMessage1, 1, eqIgnoredMessage1));
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .ignoringRepeatedFieldOrderForValues()
        .containsExactlyEntriesIn(
            multimapOf(1, eqRepeatedMessage1, 1, eqRepeatedMessage2, 2, eqRepeatedMessage1))
        .inOrder();

    try {
      assertThat(multimapOf(1, message1))
          .ignoringRepeatedFieldOrderForValues()
          .containsExactlyEntriesIn(multimapOf(2, eqRepeatedMessage2, 1, eqRepeatedMessage1));
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(multimapOf(1, message1, 2, message2))
          .ignoringFieldsForValues(ignoreFieldNumber)
          .containsExactlyEntriesIn(multimapOf(2, eqIgnoredMessage2, 1, eqIgnoredMessage1))
          .inOrder();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testFluent_valuesForKey() {
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .valuesForKey(1)
        .ignoringFields(ignoreFieldNumber)
        .containsExactly(eqIgnoredMessage2, eqIgnoredMessage1);
    expectThat(multimapOf(1, message1, 1, message2, 2, message1))
        .valuesForKey(2)
        .ignoringRepeatedFieldOrder()
        .containsExactly(eqRepeatedMessage1);

    try {
      assertThat(multimapOf(1, message1, 1, message2, 2, message1))
          .valuesForKey(1)
          .ignoringFields(ignoreFieldNumber)
          .containsExactly(eqRepeatedMessage1, eqRepeatedMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testCompareMultipleMessageTypes() {
    // Don't run this test twice.
    if (!testIsRunOnce()) {
      return;
    }

    expectThat(
            ImmutableMultimap.of(
                2,
                TestMessage2.newBuilder().addRString("foo").addRString("bar").build(),
                2,
                TestMessage2.newBuilder().addRString("quibble").addRString("frozzit").build(),
                3,
                TestMessage3.newBuilder().addRString("baz").addRString("qux").build()))
        .ignoringRepeatedFieldOrderForValues()
        .containsExactlyEntriesIn(
            ImmutableMultimap.of(
                2,
                TestMessage2.newBuilder().addRString("frozzit").addRString("quibble").build(),
                3,
                TestMessage3.newBuilder().addRString("qux").addRString("baz").build(),
                2,
                TestMessage2.newBuilder().addRString("bar").addRString("foo").build()));
  }
}
