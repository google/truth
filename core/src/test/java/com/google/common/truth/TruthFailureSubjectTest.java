/*
 * Copyright (c) 2018 Google, Inc.
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

package com.google.common.truth;

import static com.google.common.truth.Field.field;
import static com.google.common.truth.TruthFailureSubject.truthFailures;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TruthFailureSubject}. */
@RunWith(JUnit4.class)
public class TruthFailureSubjectTest extends BaseSubjectTestCase {
  // TODO(cpovirk): Switch to using field-based assertions once Truth generates field-based errors.

  // fieldKeys()

  @Test
  public void fieldKeys() {
    assertThat(field("foo", "the foo")).fieldKeys().containsExactly("foo");
  }

  @Test
  public void fieldKeysFail() {
    expectFailureWhenTestingThat(field("foo", "the foo")).fieldKeys().containsExactly("bar");
    Truth.assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("value of: failure.fieldKeys()");
  }

  // fieldValue(String)

  @Test
  public void fieldValue() {
    assertThat(field("foo", "the foo")).fieldValue("foo").isEqualTo("the foo");
  }

  @Test
  public void fieldValueFailWrongValue() {
    expectFailureWhenTestingThat(field("foo", "the foo")).fieldValue("foo").isEqualTo("the bar");
    Truth.assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("value of: failure.fieldValue(foo)");
  }

  @Test
  public void fieldValueFailNoSuchKey() {
    Object unused = expectFailureWhenTestingThat(field("foo", "the foo")).fieldValue("bar");
    assertMessage("expected to contain field: bar\nbut contained only: [foo]");
  }

  @Test
  public void fieldValueFailMultipleKeys() {
    Object unused =
        expectFailureWhenTestingThat(field("foo", "the foo"), field("foo", "the other foo"))
            .fieldValue("foo");
    assertMessage(
        "expected to contain a single field with key: foo\n"
            + "but contained multiple: [foo: the foo, foo: the other foo]");
  }

  // fieldValue(String, int)

  @Test
  public void fieldValueInt() {
    assertThat(field("foo", "the foo")).fieldValue("foo", 0).isEqualTo("the foo");
  }

  @Test
  public void fieldValueIntMultipleKeys() {
    assertThat(field("foo", "the foo"), field("foo", "the other foo"))
        .fieldValue("foo", 1)
        .isEqualTo("the other foo");
  }

  @Test
  public void fieldValueIntFailNegative() {
    try {
      assertThat(field("foo", "the foo")).fieldValue("foo", -1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void fieldValueIntFailWrongValue() {
    expectFailureWhenTestingThat(field("foo", "the foo")).fieldValue("foo", 0).isEqualTo("the bar");
    Truth.assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("value of: failure.fieldValue(foo, 0)");
  }

  @Test
  public void fieldValueIntFailNoSuchKey() {
    Object unused = expectFailureWhenTestingThat(field("foo", "the foo")).fieldValue("bar", 0);
    assertMessage("expected to contain field: bar\nbut contained only: [foo]");
  }

  @Test
  public void fieldValueIntFailNotEnoughWithKey() {
    Object unused = expectFailureWhenTestingThat(field("foo", "the foo")).fieldValue("foo", 5);
    assertMessage("for key: foo\nindex too high: 5\nfield count was: 1");
  }

  // other tests

  @Test
  public void nonTruthErrorFieldKeys() {
    Object unused = expectFailureWhenTestingThat(new AssertionError()).fieldKeys();
    assertMessage("expected a failure thrown by Truth's new failure API");
  }

  @Test
  public void nonTruthErrorFieldValue() {
    Object unused = expectFailureWhenTestingThat(new AssertionError()).fieldValue("foo");
    assertMessage("expected a failure thrown by Truth's new failure API");
  }

  private TruthFailureSubject assertThat(Field... fields) {
    return ExpectFailure.assertThat(failure(fields));
  }

  private TruthFailureSubject expectFailureWhenTestingThat(Field... fields) {
    return expectFailureWhenTestingThat(failure(fields));
  }

  private TruthFailureSubject expectFailureWhenTestingThat(AssertionError failure) {
    return (TruthFailureSubject) expectFailure.whenTesting().about(truthFailures()).that(failure);
  }

  private AssertionErrorWithFields failure(Field... fields) {
    return AssertionErrorWithFields.create(
        ImmutableList.<String>of(), ImmutableList.copyOf(fields), /* cause= */ null);
  }

  private void assertMessage(String expected) {
    ExpectFailure.assertThat(expectFailure.getFailure()).hasMessageThat().isEqualTo(expected);
  }
}
