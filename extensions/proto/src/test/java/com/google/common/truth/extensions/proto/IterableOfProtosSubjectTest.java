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

import com.google.common.base.Function;
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

    expectFailureWhenTesting().that(listOf(message1)).isEmpty();
    expectThatFailure().isNotNull();

    expectFailureWhenTesting().that(listOf()).isNotEmpty();
    expectThatFailure().isNotNull();
  }

  @Test
  public void testPlain_hasSize() {
    expectThat(listOf(message1, message2)).hasSize(2);

    expectFailureWhenTesting().that(listOf(message1)).hasSize(3);
    expectThatFailure().isNotNull();
  }

  @Test
  public void testPlain_containsNoDuplicates() {
    expectThat(listOf(message1, message2)).containsNoDuplicates();

    expectFailureWhenTesting().that(listOf(message1, eqMessage1)).containsNoDuplicates();
    expectThatFailure().isNotNull();
  }

  @Test
  public void testPlain_contains() {
    expectThat(listOf(message1, message2)).contains(eqMessage2);
    expectThat(listOf(message1, message2)).doesNotContain(eqIgnoredMessage1);

    expectFailureWhenTesting().that(listOf(message1, message2)).contains(eqIgnoredMessage1);
    expectThatFailure().isNotNull();

    expectFailureWhenTesting().that(listOf(message1, message2)).doesNotContain(eqMessage1);
    expectThatFailure().isNotNull();
  }

  @Test
  public void testPlain_containsAny() {
    expectThat(listOf(message1, message2)).containsAnyOf(eqIgnoredMessage1, eqMessage2);
    expectThat(listOf(message1, message2)).containsAnyIn(listOf(eqIgnoredMessage1, eqMessage2));
    expectThat(listOf(message1, message2)).containsAnyIn(arrayOf(eqIgnoredMessage1, eqMessage2));

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsAnyOf(eqIgnoredMessage1, eqIgnoredMessage2);
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsAnyIn(listOf(eqIgnoredMessage1, eqIgnoredMessage2));
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsAnyIn(arrayOf(eqIgnoredMessage1, eqIgnoredMessage2));
    expectThatFailure().isNotNull();
  }

  @Test
  public void testPlain_containsAll() {
    expectThat(listOf(message1, message2, eqIgnoredMessage1)).containsAllOf(eqMessage1, eqMessage2);
    expectThat(listOf(message1, message2, eqIgnoredMessage1))
        .containsAllIn(listOf(eqMessage1, eqMessage2));
    expectThat(listOf(message1, message2, eqIgnoredMessage1))
        .containsAllIn(arrayOf(eqMessage1, eqMessage2));

    expectFailureWhenTesting().that(listOf(message1)).containsAllOf(eqMessage1, eqMessage2);
    expectThatFailure().isNotNull();

    expectFailureWhenTesting().that(listOf(message1)).containsAllIn(listOf(eqMessage1, eqMessage2));
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1))
        .containsAllIn(arrayOf(eqMessage1, eqMessage2));
    expectThatFailure().isNotNull();
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
    expectThat(listOf(message1, message2))
        .containsExactlyElementsIn(arrayOf(eqMessage2, eqMessage1));
    expectThat(listOf(message1, message2))
        .containsExactlyElementsIn(arrayOf(eqMessage1, eqMessage2))
        .inOrder();

    expectFailureWhenTesting().that(listOf(message1)).containsExactly(eqMessage1, eqMessage2);
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsExactly(eqMessage2, eqMessage1)
        .inOrder();
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1))
        .containsExactlyElementsIn(listOf(eqMessage1, eqMessage2));
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsExactlyElementsIn(listOf(eqMessage2, eqMessage1))
        .inOrder();
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1))
        .containsExactlyElementsIn(arrayOf(eqMessage1, eqMessage2));
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsExactlyElementsIn(arrayOf(eqMessage2, eqMessage1))
        .inOrder();
    expectThatFailure().isNotNull();
  }

  @Test
  public void testPlain_containsNone() {
    expectThat(listOf(message1)).containsNoneOf(eqMessage2, eqIgnoredMessage1);
    expectThat(listOf(message1)).containsNoneIn(listOf(eqMessage2, eqIgnoredMessage1));
    expectThat(listOf(message1)).containsNoneIn(arrayOf(eqMessage2, eqIgnoredMessage1));

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsNoneOf(eqMessage2, eqIgnoredMessage1);
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsNoneIn(listOf(eqMessage2, eqIgnoredMessage1));
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .containsNoneIn(arrayOf(eqMessage2, eqIgnoredMessage1));
    expectThatFailure().isNotNull();
  }

  @Test
  public void testPlain_isOrdered() {
    expectThat(listOf(message1, eqMessage1, message2)).isOrdered(compareByOIntAscending());
    expectThat(listOf(message1, message2)).isStrictlyOrdered(compareByOIntAscending());

    expectFailureWhenTesting().that(listOf(message2, message1)).isOrdered(compareByOIntAscending());
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, eqMessage1, message2))
        .isStrictlyOrdered(compareByOIntAscending());
    expectThatFailure().isNotNull();
  }

  @Test
  public void testFluent_contains() {
    expectThat(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .contains(eqIgnoredMessage1);
    expectThat(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .doesNotContain(eqIgnoredMessage1);

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .contains(eqRepeatedMessage1);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "is equivalent according to "
                + "assertThat(proto)"
                + ".ignoringFields("
                + fullMessageName()
                + ".o_int)"
                + ".isEqualTo(target)");

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .doesNotContain(eqRepeatedMessage1);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "is equivalent according to "
                + "assertThat(proto).ignoringRepeatedFieldOrder().isEqualTo(target)");
  }

  @Test
  public void testFluent_containsAny() {
    expectThat(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .containsAnyOf(eqIgnoredMessage1, eqRepeatedMessage2);
    expectThat(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .containsAnyIn(listOf(eqIgnoredMessage1, eqRepeatedMessage2));

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .containsAnyOf(eqRepeatedMessage1, eqRepeatedMessage2);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "is equivalent according to "
                + "assertThat(proto)"
                + ".ignoringFields("
                + fullMessageName()
                + ".o_int)"
                + ".isEqualTo(target)");

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .containsAnyIn(listOf(eqIgnoredMessage1, eqIgnoredMessage2));
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "is equivalent according to "
                + "assertThat(proto).ignoringRepeatedFieldOrder().isEqualTo(target)");
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

    expectFailureWhenTesting()
        .that(listOf(message1))
        .ignoringRepeatedFieldOrder()
        .containsAllOf(eqMessage1, eqMessage2);
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1))
        .ignoringRepeatedFieldOrder()
        .containsAllIn(listOf(eqMessage1, eqMessage2));
    expectThatFailure().isNotNull();
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

    expectFailureWhenTesting()
        .that(listOf(message1))
        .ignoringRepeatedFieldOrder()
        .containsExactly(eqMessage1, eqMessage2);
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .containsExactly(eqMessage2, eqMessage1)
        .inOrder();
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1))
        .ignoringRepeatedFieldOrder()
        .containsExactlyElementsIn(listOf(eqMessage1, eqMessage2));
    expectThatFailure().isNotNull();

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .containsExactlyElementsIn(listOf(eqMessage2, eqMessage1))
        .inOrder();
    expectThatFailure().isNotNull();
  }

  @Test
  public void testFluent_containsNone() {
    expectThat(listOf(message1))
        .ignoringFields(ignoreFieldNumber)
        .containsNoneOf(eqMessage2, eqRepeatedMessage1);
    expectThat(listOf(message1))
        .ignoringRepeatedFieldOrder()
        .containsNoneIn(listOf(eqMessage2, eqIgnoredMessage1));

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .ignoringFields(ignoreFieldNumber)
        .containsNoneOf(eqRepeatedMessage1, eqIgnoredMessage2);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "is equivalent according to "
                + "assertThat(proto)"
                + ".ignoringFields("
                + fullMessageName()
                + ".o_int)"
                + ".isEqualTo(target)");

    expectFailureWhenTesting()
        .that(listOf(message1, message2))
        .ignoringRepeatedFieldOrder()
        .containsNoneIn(listOf(eqIgnoredMessage1, eqRepeatedMessage2));
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "is equivalent according to "
                + "assertThat(proto).ignoringRepeatedFieldOrder().isEqualTo(target)");
  }

  @Test
  public void testFluent_correspondenceToString() {
    // Some arbitrary tests to ensure Correspondence.toString() is well-behaved.
    // Not intended to be comprehensive.

    // TODO(user): Consider actually adding newlines as the strings are formatted here to make the
    // error messages look prettier. Might require some thought to avoid eating too much vertical
    // space, also indentation adds complexity.

    expectFailureWhenTesting()
        .that(listOf(message1))
        .withPartialScope(FieldScopes.fromSetFields(message2))
        .ignoringRepeatedFieldOrder()
        .contains(message2);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "assertThat(proto).withPartialScope(FieldScopes.fromSetFields({o_int: 3\n"
                + "r_string: \"baz\"\n"
                + "r_string: \"qux\"\n"
                + "})).ignoringRepeatedFieldOrder().isEqualTo(target)");

    expectFailureWhenTesting()
        .that(listOf(message1))
        .ignoringRepeatedFieldOrder()
        .ignoringFieldScope(
            FieldScopes.ignoringFields(getFieldNumber("o_int"), getFieldNumber("r_string")))
        .ignoringFieldAbsence()
        .contains(message2);
    expectThatFailure()
        .hasMessageThat()
        .contains(
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

    expectFailureWhenTesting()
        .that(listOf(message1))
        .ignoringFields(getFieldNumber("o_enum"), getFieldNumber("o_test_message"))
        .reportingMismatchesOnly()
        .contains(message2);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "assertThat(proto)"
                + ".ignoringFields("
                + fullMessageName()
                + ".o_enum, "
                + fullMessageName()
                + ".o_test_message)"
                + ".reportingMismatchesOnly()"
                + ".isEqualTo(target)");
  }

  @Test
  public void testFormatDiff() {
    expectFailureWhenTesting()
        .that(listOf(message1))
        .ignoringRepeatedFieldOrder()
        .containsExactly(message2);
    expectThatFailure()
        .hasMessageThat()
        .contains(
            "(diff: Differences were found:\n"
                + "modified: o_int: 3 -> 1\n"
                + "added: r_string[0]: \"foo\"\n"
                + "added: r_string[1]: \"bar\"\n"
                + "deleted: r_string[0]: \"baz\"\n"
                + "deleted: r_string[1]: \"qux\"\n");
  }

  @Test
  public void testDisplayingDiffsPairedBy() {
    Message actualInt3 = parse("o_int: 3 r_string: 'foo'");
    Message actualInt4 = parse("o_int: 4 r_string: 'bar'");
    Message expectedInt3 = parse("o_int: 3 r_string: 'baz'");
    Message expectedInt4 = parse("o_int: 4 r_string: 'qux'");

    Function<Message, Integer> getInt =
        new Function<Message, Integer>() {
          @Override
          public Integer apply(Message message) {
            return (Integer) message.getField(getFieldDescriptor("o_int"));
          }
        };

    expectFailureWhenTesting()
        .that(listOf(actualInt3, actualInt4))
        .displayingDiffsPairedBy(getInt)
        .containsExactly(expectedInt3, expectedInt4);
    expectThatFailure().hasMessageThat().contains("modified: r_string[0]: \"baz\" -> \"foo\"");
    expectThatFailure().hasMessageThat().contains("modified: r_string[0]: \"qux\" -> \"bar\"");
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
