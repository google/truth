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

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.TruthFailureSubject.HOW_TO_TEST_KEYS_WITHOUT_VALUES;
import static com.google.common.truth.TruthFailureSubject.truthFailures;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TruthFailureSubject}. */
@RunWith(JUnit4.class)
public class TruthFailureSubjectTest extends BaseSubjectTestCase {
  // factKeys()

  @Test
  public void factKeys() {
    assertThat(fact("foo", "the foo")).factKeys().containsExactly("foo");
  }

  @Test
  public void factKeysNoValue() {
    assertThat(simpleFact("foo")).factKeys().containsExactly("foo");
  }

  @Test
  public void factKeysFail() {
    expectFailureWhenTestingThat(fact("foo", "the foo")).factKeys().containsExactly("bar");
    Truth.assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("value of: failure.factKeys()");
    // TODO(cpovirk): Switch to using fact-based assertions once IterableSubject uses them.
  }

  // factValue(String)

  @Test
  public void factValue() {
    assertThat(fact("foo", "the foo")).factValue("foo").isEqualTo("the foo");
  }

  @Test
  public void factValueFailWrongValue() {
    expectFailureWhenTestingThat(fact("foo", "the foo")).factValue("foo").isEqualTo("the bar");
    assertFailureValue("value of", "failure.factValue(foo)");
  }

  @Test
  public void factValueFailNoSuchKey() {
    Object unused = expectFailureWhenTestingThat(fact("foo", "the foo")).factValue("bar");
    assertFailureKeys("expected to contain fact", "but contained only");
    assertFailureValue("expected to contain fact", "bar");
    assertFailureValue("but contained only", "[foo]");
  }

  @Test
  public void factValueFailMultipleKeys() {
    Object unused =
        expectFailureWhenTestingThat(fact("foo", "the foo"), fact("foo", "the other foo"))
            .factValue("foo");
    assertFailureKeys("expected to contain a single fact with key", "but contained multiple");
    assertFailureValue("expected to contain a single fact with key", "foo");
    assertFailureValue("but contained multiple", "[foo: the foo, foo: the other foo]");
  }

  @Test
  public void factValueFailNoValue() {
    Object unused = expectFailureWhenTestingThat(simpleFact("foo")).factValue("foo");
    assertFailureKeys(
        "expected to have a value",
        "for key",
        "but the key was present with no value",
        HOW_TO_TEST_KEYS_WITHOUT_VALUES.key);
    assertFailureValue("for key", "foo");
  }

  // factValue(String, int)

  @Test
  public void factValueInt() {
    assertThat(fact("foo", "the foo")).factValue("foo", 0).isEqualTo("the foo");
  }

  @Test
  public void factValueIntMultipleKeys() {
    assertThat(fact("foo", "the foo"), fact("foo", "the other foo"))
        .factValue("foo", 1)
        .isEqualTo("the other foo");
  }

  @Test
  public void factValueIntFailNegative() {
    try {
      assertThat(fact("foo", "the foo")).factValue("foo", -1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void factValueIntFailWrongValue() {
    expectFailureWhenTestingThat(fact("foo", "the foo")).factValue("foo", 0).isEqualTo("the bar");
    assertFailureValue("value of", "failure.factValue(foo, 0)");
  }

  @Test
  public void factValueIntFailNoSuchKey() {
    Object unused = expectFailureWhenTestingThat(fact("foo", "the foo")).factValue("bar", 0);
    assertFailureKeys("expected to contain fact", "but contained only");
    assertFailureValue("expected to contain fact", "bar");
    assertFailureValue("but contained only", "[foo]");
  }

  @Test
  public void factValueIntFailNotEnoughWithKey() {
    Object unused = expectFailureWhenTestingThat(fact("foo", "the foo")).factValue("foo", 5);
    assertFailureKeys("for key", "index too high", "fact count was");
    assertFailureValue("for key", "foo");
    assertFailureValue("index too high", "5");
    assertFailureValue("fact count was", "1");
  }

  @Test
  public void factValueIntFailNoValue() {
    Object unused = expectFailureWhenTestingThat(simpleFact("foo")).factValue("foo", 0);
    assertFailureKeys(
        "expected to have a value",
        "for key",
        "and index",
        "but the key was present with no value",
        HOW_TO_TEST_KEYS_WITHOUT_VALUES.key);
    assertFailureValue("for key", "foo");
    assertFailureValue("and index", "0");
  }

  // other tests

  @Test
  public void nonTruthErrorFactKeys() {
    Object unused = expectFailureWhenTestingThat(new AssertionError()).factKeys();
    assertFailureKeys("expected a failure thrown by Truth's new failure API", "but was");
  }

  @Test
  public void nonTruthErrorFactValue() {
    Object unused = expectFailureWhenTestingThat(new AssertionError()).factValue("foo");
    assertFailureKeys("expected a failure thrown by Truth's new failure API", "but was");
  }

  private TruthFailureSubject assertThat(Fact... facts) {
    return ExpectFailure.assertThat(failure(facts));
  }

  private TruthFailureSubject expectFailureWhenTestingThat(Fact... facts) {
    return expectFailureWhenTestingThat(failure(facts));
  }

  private TruthFailureSubject expectFailureWhenTestingThat(AssertionError failure) {
    return (TruthFailureSubject) expectFailure.whenTesting().about(truthFailures()).that(failure);
  }

  private AssertionErrorWithFacts failure(Fact... facts) {
    return AssertionErrorWithFacts.create(
        ImmutableList.<String>of(), ImmutableList.copyOf(facts), /* cause= */ null);
  }
}
