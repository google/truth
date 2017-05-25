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

import com.google.protobuf.Message;
import java.util.Collection;
import java.util.Comparator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for {@link IterableOfProtosSubject}.
 *
 * <p>Individual equality fuzzing is thoroughly tested by {@link ProtoSubjectTest}, while fuzzy
 * equality testing is thoroughly tested by {@link com.google.common.truth.IterableSubjectTest}.
 * Thus, we simply check that all of the exposed methods work in basic cases, and trust that the
 * implementation ensures correctness in the cross-product of the many ways one can do things.
 */
@RunWith(Parameterized.class)
public class IterableOfProtosSubjectTest extends ProtoSubjectTestBase {

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

  public IterableOfProtosSubjectTest(TestType testType) {
    super(testType);
  }

  @Test
  public void testPlain_isEmpty() {
    expectThat(listOf()).isEmpty();
    expectThat(listOf(message1)).isNotEmpty();

    try {
      assertThat(listOf(message1)).isEmpty();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf()).isNotEmpty();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_hasSize() {
    expectThat(listOf(message1, message2)).hasSize(2);

    try {
      assertThat(listOf(message1)).hasSize(3);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_containsNoDuplicates() {
    expectThat(listOf(message1, message2)).containsNoDuplicates();

    try {
      assertThat(listOf(message1, eqMessage1)).containsNoDuplicates();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_contains() {
    expectThat(listOf(message1, message2)).contains(eqMessage2);
    expectThat(listOf(message1, message2)).doesNotContain(eqIgnoredMessage1);

    try {
      assertThat(listOf(message1, message2)).contains(eqIgnoredMessage1);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1, message2)).doesNotContain(eqMessage1);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_containsAny() {
    expectThat(listOf(message1, message2)).containsAnyOf(eqIgnoredMessage1, eqMessage2);
    expectThat(listOf(message1, message2)).containsAnyIn(listOf(eqIgnoredMessage1, eqMessage2));

    try {
      assertThat(listOf(message1, message2)).containsAnyOf(eqIgnoredMessage1, eqIgnoredMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1, message2))
          .containsAnyIn(listOf(eqIgnoredMessage1, eqIgnoredMessage2));
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_containsAll() {
    expectThat(listOf(message1, message2, eqIgnoredMessage1)).containsAllOf(eqMessage1, eqMessage2);
    expectThat(listOf(message1, message2, eqIgnoredMessage1))
        .containsAllIn(listOf(eqMessage1, eqMessage2));

    try {
      assertThat(listOf(message1)).containsAllOf(eqMessage1, eqMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1)).containsAllIn(listOf(eqMessage1, eqMessage2));
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_containsExactly() {
    expectThat(listOf(message1, message2)).containsExactly(eqMessage2, eqMessage1);
    expectThat(listOf(message1, message2)).containsExactly(eqMessage1, eqMessage2).inOrder();
    expectThat(listOf(message1, message2))
        .containsExactlyElementsIn(listOf(eqMessage2, eqMessage1));
    expectThat(listOf(message1, message2))
        .containsExactlyElementsIn(listOf(eqMessage1, eqMessage2))
        .inOrder();

    try {
      assertThat(listOf(message1)).containsExactly(eqMessage1, eqMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1, message2)).containsExactly(eqMessage2, eqMessage1).inOrder();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1)).containsExactlyElementsIn(listOf(eqMessage1, eqMessage2));
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1, message2))
          .containsExactlyElementsIn(listOf(eqMessage2, eqMessage1))
          .inOrder();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_containsNone() {
    expectThat(listOf(message1)).containsNoneOf(eqMessage2, eqIgnoredMessage1);
    expectThat(listOf(message1)).containsNoneIn(listOf(eqMessage2, eqIgnoredMessage1));

    try {
      assertThat(listOf(message1, message2)).containsNoneOf(eqMessage2, eqIgnoredMessage1);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1, message2)).containsNoneIn(listOf(eqMessage2, eqIgnoredMessage1));
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testPlain_isOrdered() {
    expectThat(listOf(message1, eqMessage1, message2)).isOrdered(compareByOIntAscending());
    expectThat(listOf(message1, message2)).isStrictlyOrdered(compareByOIntAscending());

    try {
      assertThat(listOf(message2, message1)).isOrdered(compareByOIntAscending());
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1, eqMessage1, message2))
          .isStrictlyOrdered(compareByOIntAscending());
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testFluent_contains() {
    expectThat(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .contains(eqIgnoredMessage1);
    expectThat(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .doesNotContain(eqIgnoredMessage1);

    try {
      assertThat(listOf(message1, message2))
          .ignoringFields(ignoreFieldNumber)
          .contains(eqRepeatedMessage1);
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
      assertThat(listOf(message1, message2))
          .ignoringRepeatedFieldOrder()
          .doesNotContain(eqRepeatedMessage1);
      expectedFailure();
    } catch (AssertionError expected) {
      expectSubstr(
          expected,
          "is equivalent according to "
              + "assertThat(proto).ignoringRepeatedFieldOrder().isEqualTo(target)");
    }
  }

  @Test
  public void testFluent_containsAny() {
    expectThat(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .containsAnyOf(eqIgnoredMessage1, eqRepeatedMessage2);
    expectThat(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .containsAnyIn(listOf(eqIgnoredMessage1, eqRepeatedMessage2));

    try {
      assertThat(listOf(message1, message2))
          .ignoringFields(ignoreFieldNumber)
          .containsAnyOf(eqRepeatedMessage1, eqRepeatedMessage2);
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
      assertThat(listOf(message1, message2))
          .ignoringRepeatedFieldOrder()
          .containsAnyIn(listOf(eqIgnoredMessage1, eqIgnoredMessage2));
      expectedFailure();
    } catch (AssertionError expected) {
      expectSubstr(
          expected,
          "is equivalent according to "
              + "assertThat(proto).ignoringRepeatedFieldOrder().isEqualTo(target)");
    }
  }

  @Test
  public void testFluent_containsAll() {
    // TODO(peteg): containsAll and containsExactly don't surface Correspondence.toString().
    // We should add a string test here once they do.

    expectThat(listOf(message1, message2, eqRepeatedMessage2))
        .ignoringFields(ignoreFieldNumber)
        .containsAllOf(eqIgnoredMessage1, eqIgnoredMessage2);
    expectThat(listOf(message1, message2, eqIgnoredMessage1))
        .ignoringRepeatedFieldOrder()
        .containsAllIn(listOf(eqRepeatedMessage1, eqRepeatedMessage2));

    try {
      assertThat(listOf(message1))
          .ignoringRepeatedFieldOrder()
          .containsAllOf(eqMessage1, eqMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1))
          .ignoringRepeatedFieldOrder()
          .containsAllIn(listOf(eqMessage1, eqMessage2));
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testFluent_containsExactly() {
    expectThat(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .containsExactly(eqIgnoredMessage2, eqIgnoredMessage1);
    expectThat(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .containsExactly(eqRepeatedMessage1, eqRepeatedMessage2)
        .inOrder();
    expectThat(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .containsExactlyElementsIn(listOf(eqIgnoredMessage2, eqIgnoredMessage1));
    expectThat(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .containsExactlyElementsIn(listOf(eqRepeatedMessage1, eqRepeatedMessage2))
        .inOrder();

    try {
      assertThat(listOf(message1))
          .ignoringRepeatedFieldOrder()
          .containsExactly(eqMessage1, eqMessage2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1, message2))
          .ignoringRepeatedFieldOrder()
          .containsExactly(eqMessage2, eqMessage1)
          .inOrder();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1))
          .ignoringRepeatedFieldOrder()
          .containsExactlyElementsIn(listOf(eqMessage1, eqMessage2));
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }

    try {
      assertThat(listOf(message1, message2))
          .ignoringRepeatedFieldOrder()
          .containsExactlyElementsIn(listOf(eqMessage2, eqMessage1))
          .inOrder();
      expectedFailure();
    } catch (AssertionError expected) {
      expectFailureNotMissing(expected);
    }
  }

  @Test
  public void testFluent_containsNone() {
    expectThat(listOf(message1))
        .ignoringFields(ignoreFieldNumber)
        .containsNoneOf(eqMessage2, eqRepeatedMessage1);
    expectThat(listOf(message1))
        .ignoringRepeatedFieldOrder()
        .containsNoneIn(listOf(eqMessage2, eqIgnoredMessage1));

    try {
      assertThat(listOf(message1, message2))
          .ignoringFields(ignoreFieldNumber)
          .containsNoneOf(eqRepeatedMessage1, eqIgnoredMessage2);
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
      assertThat(listOf(message1, message2))
          .ignoringRepeatedFieldOrder()
          .containsNoneIn(listOf(eqIgnoredMessage1, eqRepeatedMessage2));
      expectedFailure();
    } catch (AssertionError expected) {
      expectSubstr(
          expected,
          "is equivalent according to "
              + "assertThat(proto).ignoringRepeatedFieldOrder().isEqualTo(target)");
    }
  }

  @Test
  public void testFluent_correspondenceToString() {
    // Some arbitrary tests to ensure Correspondence.toString() is well-behaved.
    // Not intended to be comprehensive.

    // TODO(user): Consider actually adding newlines as the strings are formatted here to make the
    // error messages look prettier. Might require some thought to avoid eating too much vertical
    // space, also indentation adds complexity.

    try {
      assertThat(listOf(message1))
          .withPartialScope(FieldScopes.fromSetFields(message2))
          .ignoringRepeatedFieldOrder()
          .contains(message2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectSubstr(
          expected,
          "assertThat(proto).withPartialScope(FieldScopes.fromSetFields({o_int: 3\n"
              + "r_string: \"baz\"\n"
              + "r_string: \"qux\"\n"
              + "})).ignoringRepeatedFieldOrder().isEqualTo(target)");
    }

    try {
      assertThat(listOf(message1))
          .ignoringRepeatedFieldOrder()
          .ignoringFieldScope(
              FieldScopes.ignoringFields(getFieldNumber("o_int"), getFieldNumber("r_string")))
          .ignoringFieldAbsence()
          .contains(message2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectSubstr(
          expected,
          "assertThat(proto)"
              + ".ignoringRepeatedFieldOrder()"
              + ".ignoringFieldScope("
              + "FieldScopes.ignoringFields("
              + fullMessageName()
              + ".o_int, "
              + fullMessageName()
              + ".r_string))"
              + ".ignoringFieldAbsence()"
              + ".isEqualTo(target)");
    }

    try {
      assertThat(listOf(message1))
          .ignoringFields(4, 7)
          .reportingMismatchesOnly()
          .contains(message2);
      expectedFailure();
    } catch (AssertionError expected) {
      expectSubstr(
          expected,
          "assertThat(proto)"
              + ".ignoringFields("
              + fullMessageName()
              + ".o_enum, "
              + fullMessageName()
              + ".o_test_message)"
              + ".reportingMismatchesOnly()"
              + ".isEqualTo(target)");
    }
  }

  @Test
  public void testCompareMultipleMessageTypes() {
    // Don't run this test twice.
    if (!testIsRunOnce()) {
      return;
    }

    expectThat(
            listOf(
                TestMessage2.newBuilder().addRString("foo").addRString("bar").build(),
                TestMessage3.newBuilder().addRString("baz").addRString("qux").build()))
        .ignoringRepeatedFieldOrder()
        .containsExactly(
            TestMessage3.newBuilder().addRString("qux").addRString("baz").build(),
            TestMessage2.newBuilder().addRString("bar").addRString("foo").build());
  }

  private Comparator<Message> compareByOIntAscending() {
    return new Comparator<Message>() {
      @Override
      public int compare(Message message1, Message message2) {
        return Integer.compare(
            (Integer) message1.getField(getFieldDescriptor("o_int")),
            (Integer) message2.getField(getFieldDescriptor("o_int")));
      }
    };
  }
}
