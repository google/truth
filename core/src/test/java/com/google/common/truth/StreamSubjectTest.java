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
package com.google.common.truth;

import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;

import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Stream} Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public final class StreamSubjectTest {

  @SuppressWarnings({"deprecation", "TruthSelfEquals"}) // test of a possibly mistaken call
  @Test
  public void testIsEqualToSameInstancePreviouslyConsumed() {
    Stream<String> stream = Stream.of("hello");
    stream.forEach(e -> {}); // Consume it so that we can verify that isEqualTo still works
    assertThat(stream).isEqualTo(stream);
  }

  @SuppressWarnings({"deprecation", "TruthSelfEquals"}) // test of a possibly mistaken call
  @Test
  public void testIsEqualToSameInstanceDoesNotConsume() {
    Stream<String> stream = Stream.of("hello");
    assertThat(stream).isEqualTo(stream);
    assertThat(stream).containsExactly("hello");
  }

  @SuppressWarnings({
    "deprecation", // test of a possibly mistaken call
    "StreamToString", // not very useful but the best we can do
  })
  @Test
  public void testIsEqualToFailurePreviouslyConsumed() {
    Stream<String> stream = Stream.of("hello");
    stream.forEach(e -> {}); // Consume it so that we can verify that isEqualTo still works
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(stream).isEqualTo(Stream.of("hello")));
    assertThat(failure)
        .factValue("but was")
        .isEqualTo("Stream that has already been operated upon or closed: " + stream);
    assertThat(failure)
        .hasMessageThat()
        .contains("Warning: Stream equality is based on object identity.");
  }

  @SuppressWarnings("deprecation") // test of a possibly mistaken call
  @Test
  public void testIsEqualToFailureNotPreviouslyConsumed() {
    Stream<String> stream = Stream.of("hello");
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(stream).isEqualTo(Stream.of("hello")));
    assertThat(failure).factValue("but was").isEqualTo("[hello]");
    assertThat(failure)
        .hasMessageThat()
        .contains("Warning: Stream equality is based on object identity.");
  }

  @SuppressWarnings({
    "SelfAssertion", // test of a possibly mistaken call
    "deprecation", // test of a possibly mistaken call
    "StreamToString", // not very useful but the best we can do
  })
  @Test
  public void testIsNotEqualToSameInstance() {
    Stream<String> stream = Stream.of("hello");
    stream.forEach(e -> {}); // Consume it so that we can verify that isNotEqualTo still works
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(stream).isNotEqualTo(stream));
    assertThat(failure).factKeys().containsExactly("expected not to be");
    assertThat(failure)
        .factValue("expected not to be")
        .isEqualTo("Stream that has already been operated upon or closed: " + stream);
  }

  @SuppressWarnings("deprecation") // test of a possibly mistaken call
  @Test
  public void testIsNotEqualToOtherInstance() {
    Stream<String> stream = Stream.of("hello");
    stream.forEach(e -> {}); // Consume it so that we can verify that isNotEqualTo still works
    assertThat(stream).isNotEqualTo(Stream.of("hello"));
  }

  @Test
  public void testNullStream_fails() {
    Stream<String> nullStream = null;
    expectFailure(whenTesting -> whenTesting.that(nullStream).isEmpty());
  }

  @Test
  public void testNullStreamIsNull() {
    Stream<String> nullStream = null;
    assertThat(nullStream).isNull();
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void testIsSameInstanceAs() {
    Stream<String> stream = Stream.of("hello");
    assertThat(stream).isSameInstanceAs(stream);
  }

  @Test
  public void testIsEmpty() {
    assertThat(Stream.of()).isEmpty();
  }

  @Test
  public void testIsEmpty_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).isEmpty());
  }

  @Test
  public void testIsNotEmpty() {
    assertThat(Stream.of("hello")).isNotEmpty();
  }

  @Test
  public void testIsNotEmpty_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of()).isNotEmpty());
  }

  @Test
  public void testHasSize() {
    assertThat(Stream.of("hello")).hasSize(1);
  }

  @Test
  public void testHasSize_fails() {
    AssertionError failure =
        expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).hasSize(2));
    assertThat(failure).factValue("value of").isEqualTo("stream.size()");
  }

  @Test
  public void testContainsNoDuplicates() {
    assertThat(Stream.of("hello")).containsNoDuplicates();
  }

  @Test
  public void testContainsNoDuplicates_fails() {
    expectFailure(
        whenTesting -> whenTesting.that(Stream.of("hello", "hello")).containsNoDuplicates());
  }

  @Test
  public void testContains() {
    assertThat(Stream.of("hello")).contains("hello");
  }

  @Test
  public void testContains_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).contains("goodbye"));
  }

  @Test
  public void testContainsAnyOf() {
    assertThat(Stream.of("hello")).containsAnyOf("hello", "hell");
  }

  @Test
  public void testContainsAnyOf_fails() {
    expectFailure(
        whenTesting -> whenTesting.that(Stream.of("hello")).containsAnyOf("goodbye", "good"));
  }

  @Test
  public void testContainsAnyIn() {
    assertThat(Stream.of("hello")).containsAnyIn(asList("hello", "hell"));
  }

  @Test
  public void testContainsAnyIn_fails() {
    expectFailure(
        whenTesting ->
            whenTesting.that(Stream.of("hello")).containsAnyIn(asList("goodbye", "good")));
  }

  @Test
  public void testDoesNotContain() {
    assertThat(Stream.of("hello")).doesNotContain("goodbye");
  }

  @Test
  public void testDoesNotContain_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).doesNotContain("hello"));
  }

  @Test
  public void testContainsNoneOf() {
    assertThat(Stream.of("hello")).containsNoneOf("goodbye", "good");
  }

  @Test
  public void testContainsNoneOf_fails() {
    expectFailure(
        whenTesting -> whenTesting.that(Stream.of("hello")).containsNoneOf("hello", "hell"));
  }

  @Test
  public void testContainsNoneIn() {
    assertThat(Stream.of("hello")).containsNoneIn(asList("goodbye", "good"));
  }

  @Test
  public void testContainsNoneIn_fails() {
    expectFailure(
        whenTesting ->
            whenTesting.that(Stream.of("hello")).containsNoneIn(asList("hello", "hell")));
  }

  @Test
  public void testContainsAtLeast() {
    assertThat(Stream.of("hell", "hello")).containsAtLeast("hell", "hello");
  }

  @Test
  public void testContainsAtLeast_fails() {
    expectFailure(
        whenTesting ->
            whenTesting
                .that(Stream.of("hell", "hello"))
                .containsAtLeast("hell", "hello", "goodbye"));
  }

  @Test
  public void testContainsAtLeast_inOrder() {
    assertThat(Stream.of("hell", "hello")).containsAtLeast("hell", "hello").inOrder();
  }

  @Test
  public void testContainsAtLeast_inOrder_fails() {
    AssertionError expected =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsAtLeast("hello", "hell")
                    .inOrder());
    assertFailureKeys(
        expected,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue(expected, "expected order for required elements", "[hello, hell]");
  }

  @Test
  public void testContainsAtLeastElementsIn() {
    assertThat(Stream.of("hell", "hello")).containsAtLeastElementsIn(asList("hell", "hello"));
  }

  @Test
  public void testContainsAtLeastElementsIn_fails() {
    expectFailure(
        whenTesting ->
            whenTesting
                .that(Stream.of("hell", "hello"))
                .containsAtLeastElementsIn(asList("hell", "hello", "goodbye")));
  }

  @Test
  public void testContainsAtLeastElementsIn_inOrder() {
    assertThat(Stream.of("hell", "hello"))
        .containsAtLeastElementsIn(asList("hell", "hello"))
        .inOrder();
  }

  @Test
  public void testContainsAtLeastElementsIn_inOrder_fails() {
    AssertionError expected =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsAtLeastElementsIn(asList("hello", "hell"))
                    .inOrder());
    assertFailureKeys(
        expected,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue(expected, "expected order for required elements", "[hello, hell]");
  }

  @Test
  public void testContainsExactly() {
    assertThat(Stream.of("hell", "hello")).containsExactly("hell", "hello");
    assertThat(Stream.of("hell", "hello")).containsExactly("hello", "hell");
  }

  @Test
  public void testContainsExactly_null() {
    assertThat(Stream.of((Object) null)).containsExactly((Object) null);
    assertThat(Stream.of((Object) null)).containsExactly((Object[]) null);
  }

  @Test
  public void testContainsExactly_fails() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(Stream.of("hell", "hello")).containsExactly("hell"));
    assertFailureKeys(expected, "unexpected (1)", "---", "expected", "but was");
    assertFailureValue(expected, "expected", "[hell]");
  }

  @Test
  public void testContainsExactly_inOrder() {
    assertThat(Stream.of("hell", "hello")).containsExactly("hell", "hello").inOrder();
  }

  @Test
  public void testContainsExactly_inOrder_fails() {
    AssertionError expected =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsExactly("hello", "hell")
                    .inOrder());
    assertFailureKeys(expected, "contents match, but order was wrong", "expected", "but was");
    assertFailureValue(expected, "expected", "[hello, hell]");
  }

  @Test
  public void testContainsExactlyElementsIn() {
    assertThat(Stream.of("hell", "hello")).containsExactlyElementsIn(asList("hell", "hello"));
    assertThat(Stream.of("hell", "hello")).containsExactlyElementsIn(asList("hello", "hell"));
  }

  @Test
  public void testContainsExactlyElementsIn_fails() {
    AssertionError expected =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsExactlyElementsIn(asList("hell")));
    assertFailureKeys(expected, "unexpected (1)", "---", "expected", "but was");
    assertFailureValue(expected, "expected", "[hell]");
  }

  @Test
  public void testContainsExactlyElementsIn_inOrder() {
    assertThat(Stream.of("hell", "hello"))
        .containsExactlyElementsIn(asList("hell", "hello"))
        .inOrder();
  }

  @Test
  public void testContainsExactlyElementsIn_inOrder_fails() {
    AssertionError expected =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsExactlyElementsIn(asList("hello", "hell"))
                    .inOrder());
    assertFailureKeys(expected, "contents match, but order was wrong", "expected", "but was");
    assertFailureValue(expected, "expected", "[hello, hell]");
  }

  @Test
  public void testIsInOrder() {
    assertThat(Stream.of()).isInOrder();
    assertThat(Stream.of(1)).isInOrder();
    assertThat(Stream.of(1, 1, 2, 3, 3, 3, 4)).isInOrder();
  }

  @Test
  public void testIsInOrder_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of(1, 3, 2, 4)).isInOrder());
  }

  @Test
  public void testIsInStrictOrder() {
    assertThat(Stream.of()).isInStrictOrder();
    assertThat(Stream.of(1)).isInStrictOrder();
    assertThat(Stream.of(1, 2, 3, 4)).isInStrictOrder();
  }

  @Test
  public void testIsInStrictOrder_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of(1, 2, 2, 4)).isInStrictOrder());
  }
}
