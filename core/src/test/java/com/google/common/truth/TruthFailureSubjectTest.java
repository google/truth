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
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.TruthFailureSubject.HOW_TO_TEST_KEYS_WITHOUT_VALUES;
import static com.google.common.truth.TruthFailureSubject.truthFailures;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TruthFailureSubject}. */
@RunWith(JUnit4.class)
public class TruthFailureSubjectTest {
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
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(failure(fact("foo", "the foo")))
                    .factKeys()
                    .containsExactly("bar"));
    Truth.assertThat(e).hasMessageThat().contains("value of: failure.factKeys()");
    // TODO(cpovirk): Switch to using fact-based assertions once IterableSubject uses them.
  }

  // factValue(String)

  @Test
  public void factValue() {
    assertThat(fact("foo", "the foo")).factValue("foo").isEqualTo("the foo");
  }

  @Test
  public void factValueFailWrongValue() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(failure(fact("foo", "the foo")))
                    .factValue("foo")
                    .isEqualTo("the bar"));
    assertFailureValue(e, "value of", "failure.factValue(foo)");
  }

  @Test
  public void factValueFailNoSuchKey() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(failure(fact("foo", "the foo"))).factValue("bar"));
    assertFailureKeys(e, "expected to contain fact", "but contained only");
    assertFailureValue(e, "expected to contain fact", "bar");
    assertFailureValue(e, "but contained only", "[foo]");
  }

  @Test
  public void factValueFailMultipleKeys() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(failure(fact("foo", "the foo"), fact("foo", "the other foo")))
                    .factValue("foo"));
    assertFailureKeys(e, "expected to contain a single fact with key", "but contained multiple");
    assertFailureValue(e, "expected to contain a single fact with key", "foo");
    assertFailureValue(e, "but contained multiple", "[foo: the foo, foo: the other foo]");
  }

  @Test
  public void factValueFailNoValue() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(failure(simpleFact("foo"))).factValue("foo"));
    assertFailureKeys(
        e,
        "expected to have a value",
        "for key",
        "but the key was present with no value",
        HOW_TO_TEST_KEYS_WITHOUT_VALUES.key);
    assertFailureValue(e, "for key", "foo");
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
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(failure(fact("foo", "the foo")))
                    .factValue("foo", 0)
                    .isEqualTo("the bar"));
    assertFailureValue(e, "value of", "failure.factValue(foo, 0)");
  }

  @Test
  public void factValueIntFailNoSuchKey() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(failure(fact("foo", "the foo"))).factValue("bar", 0));
    assertFailureKeys(e, "expected to contain fact", "but contained only");
    assertFailureValue(e, "expected to contain fact", "bar");
    assertFailureValue(e, "but contained only", "[foo]");
  }

  @Test
  public void factValueIntFailNotEnoughWithKey() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(failure(fact("foo", "the foo"))).factValue("foo", 5));
    assertFailureKeys(e, "for key", "index too high", "fact count was");
    assertFailureValue(e, "for key", "foo");
    assertFailureValue(e, "index too high", "5");
    assertFailureValue(e, "fact count was", "1");
  }

  @Test
  public void factValueIntFailNoValue() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(failure(simpleFact("foo"))).factValue("foo", 0));
    assertFailureKeys(
        e,
        "expected to have a value",
        "for key",
        "and index",
        "but the key was present with no value",
        HOW_TO_TEST_KEYS_WITHOUT_VALUES.key);
    assertFailureValue(e, "for key", "foo");
    assertFailureValue(e, "and index", "0");
  }

  // other tests

  @Test
  public void nonTruthErrorFactKeys() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(new AssertionError()).factKeys());
    assertFailureKeys(e, "expected a failure thrown by Truth's failure API", "but was");
  }

  @Test
  public void nonTruthErrorFactValue() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(new AssertionError()).factValue("foo"));
    assertFailureKeys(e, "expected a failure thrown by Truth's failure API", "but was");
  }

  private TruthFailureSubject assertThat(Fact... facts) {
    return ExpectFailure.assertThat(failure(facts));
  }

  private static AssertionError expectFailure(
      ExpectFailure.SimpleSubjectBuilderCallback<TruthFailureSubject, AssertionError>
          assertionCallback) {
    return ExpectFailure.expectFailureAbout(truthFailures(), assertionCallback);
  }

  private AssertionErrorWithFacts failure(Fact... facts) {
    return new AssertionErrorWithFacts(
        ImmutableList.<String>of(), ImmutableList.copyOf(facts), /* cause= */ null);
  }
}
