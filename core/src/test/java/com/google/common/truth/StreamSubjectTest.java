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

/** Tests for {@link StreamSubject}. */
@RunWith(JUnit4.class)
public final class StreamSubjectTest {

  @SuppressWarnings({"deprecation", "TruthSelfEquals"}) // test of a possibly mistaken call
  @Test
  public void isEqualToSameInstancePreviouslyConsumed() {
    Stream<String> stream = Stream.of("hello");
    stream.forEach(e -> {}); // Consume it so that we can verify that isEqualTo still works
    assertThat(stream).isEqualTo(stream);
  }

  @SuppressWarnings({"deprecation", "TruthSelfEquals"}) // test of a possibly mistaken call
  @Test
  public void isEqualToSameInstanceDoesNotConsume() {
    Stream<String> stream = Stream.of("hello");
    assertThat(stream).isEqualTo(stream);
    assertThat(stream).containsExactly("hello");
  }

  @SuppressWarnings({
    "deprecation", // test of a possibly mistaken call
    "StreamToString", // not very useful but the best we can do
  })
  @Test
  public void isEqualToFailurePreviouslyConsumed() {
    Stream<String> stream = Stream.of("hello");
    stream.forEach(e -> {}); // Consume it so that we can verify that isEqualTo still works
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(stream).isEqualTo(Stream.of("hello")));
    assertThat(e)
        .factValue("but was")
        .isEqualTo("Stream that has already been operated upon or closed: " + stream);
    assertThat(e)
        .hasMessageThat()
        .contains("Warning: Stream equality is based on object identity.");
  }

  @SuppressWarnings("deprecation") // test of a possibly mistaken call
  @Test
  public void isEqualToFailureNotPreviouslyConsumed() {
    Stream<String> stream = Stream.of("hello");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(stream).isEqualTo(Stream.of("hello")));
    assertThat(e).factValue("but was").isEqualTo("[hello]");
    assertThat(e)
        .hasMessageThat()
        .contains("Warning: Stream equality is based on object identity.");
  }

  @SuppressWarnings({
    "SelfAssertion", // test of a possibly mistaken call
    "deprecation", // test of a possibly mistaken call
    "StreamToString", // not very useful but the best we can do
  })
  @Test
  public void isNotEqualToSameInstance() {
    Stream<String> stream = Stream.of("hello");
    stream.forEach(e -> {}); // Consume it so that we can verify that isNotEqualTo still works
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(stream).isNotEqualTo(stream));
    assertThat(e).factKeys().containsExactly("expected not to be");
    assertThat(e)
        .factValue("expected not to be")
        .isEqualTo("Stream that has already been operated upon or closed: " + stream);
  }

  @SuppressWarnings("deprecation") // test of a possibly mistaken call
  @Test
  public void isNotEqualToOtherInstance() {
    Stream<String> stream = Stream.of("hello");
    stream.forEach(e -> {}); // Consume it so that we can verify that isNotEqualTo still works
    assertThat(stream).isNotEqualTo(Stream.of("hello"));
  }

  @Test
  public void nullStream_fails() {
    Stream<String> nullStream = null;
    expectFailure(whenTesting -> whenTesting.that(nullStream).isEmpty());
  }

  @Test
  public void nullStreamIsNull() {
    Stream<String> nullStream = null;
    assertThat(nullStream).isNull();
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void isSameInstanceAs() {
    Stream<String> stream = Stream.of("hello");
    assertThat(stream).isSameInstanceAs(stream);
  }

  @Test
  public void isEmpty() {
    assertThat(Stream.of()).isEmpty();
  }

  @Test
  public void isEmpty_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).isEmpty());
  }

  @Test
  public void isNotEmpty() {
    assertThat(Stream.of("hello")).isNotEmpty();
  }

  @Test
  public void isNotEmpty_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of()).isNotEmpty());
  }

  @Test
  public void hasSize() {
    assertThat(Stream.of("hello")).hasSize(1);
  }

  @Test
  public void hasSize_fails() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).hasSize(2));
    assertThat(e).factValue("value of").isEqualTo("stream.size()");
  }

  @Test
  public void containsNoDuplicates() {
    assertThat(Stream.of("hello")).containsNoDuplicates();
  }

  @Test
  public void containsNoDuplicates_fails() {
    expectFailure(
        whenTesting -> whenTesting.that(Stream.of("hello", "hello")).containsNoDuplicates());
  }

  @Test
  public void contains() {
    assertThat(Stream.of("hello")).contains("hello");
  }

  @Test
  public void contains_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).contains("goodbye"));
  }

  @Test
  public void containsAnyOf() {
    assertThat(Stream.of("hello")).containsAnyOf("hello", "hell");
  }

  @Test
  public void containsAnyOf_fails() {
    expectFailure(
        whenTesting -> whenTesting.that(Stream.of("hello")).containsAnyOf("goodbye", "good"));
  }

  @Test
  public void containsAnyIn() {
    assertThat(Stream.of("hello")).containsAnyIn(asList("hello", "hell"));
  }

  @Test
  public void containsAnyIn_fails() {
    expectFailure(
        whenTesting ->
            whenTesting.that(Stream.of("hello")).containsAnyIn(asList("goodbye", "good")));
  }

  @Test
  public void doesNotContain() {
    assertThat(Stream.of("hello")).doesNotContain("goodbye");
  }

  @Test
  public void doesNotContain_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).doesNotContain("hello"));
  }

  @Test
  public void containsNoneOf() {
    assertThat(Stream.of("hello")).containsNoneOf("goodbye", "good");
  }

  @Test
  public void containsNoneOf_fails() {
    expectFailure(
        whenTesting -> whenTesting.that(Stream.of("hello")).containsNoneOf("hello", "hell"));
  }

  @Test
  public void containsNoneIn() {
    assertThat(Stream.of("hello")).containsNoneIn(asList("goodbye", "good"));
  }

  @Test
  public void containsNoneIn_fails() {
    expectFailure(
        whenTesting ->
            whenTesting.that(Stream.of("hello")).containsNoneIn(asList("hello", "hell")));
  }

  @Test
  public void containsAtLeast() {
    assertThat(Stream.of("hell", "hello")).containsAtLeast("hell", "hello");
  }

  @Test
  public void containsAtLeast_fails() {
    expectFailure(
        whenTesting ->
            whenTesting
                .that(Stream.of("hell", "hello"))
                .containsAtLeast("hell", "hello", "goodbye"));
  }

  @Test
  public void containsAtLeast_inOrder() {
    assertThat(Stream.of("hell", "hello")).containsAtLeast("hell", "hello").inOrder();
  }

  @Test
  public void containsAtLeast_inOrder_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsAtLeast("hello", "hell")
                    .inOrder());
    assertFailureKeys(
        e,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue(e, "expected order for required elements", "[hello, hell]");
  }

  @Test
  public void containsAtLeastElementsIn() {
    assertThat(Stream.of("hell", "hello")).containsAtLeastElementsIn(asList("hell", "hello"));
  }

  @Test
  public void containsAtLeastElementsIn_fails() {
    expectFailure(
        whenTesting ->
            whenTesting
                .that(Stream.of("hell", "hello"))
                .containsAtLeastElementsIn(asList("hell", "hello", "goodbye")));
  }

  @Test
  public void containsAtLeastElementsIn_inOrder() {
    assertThat(Stream.of("hell", "hello"))
        .containsAtLeastElementsIn(asList("hell", "hello"))
        .inOrder();
  }

  @Test
  public void containsAtLeastElementsIn_inOrder_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsAtLeastElementsIn(asList("hello", "hell"))
                    .inOrder());
    assertFailureKeys(
        e,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue(e, "expected order for required elements", "[hello, hell]");
  }

  @Test
  public void containsExactly() {
    assertThat(Stream.of("hell", "hello")).containsExactly("hell", "hello");
    assertThat(Stream.of("hell", "hello")).containsExactly("hello", "hell");
  }

  @Test
  public void containsExactly_nullObject() {
    assertThat(Stream.of((Object) null)).containsExactly((Object) null);
  }

  @Test
  @J2ktIncompatible // Kotlin can't pass a null array for a varargs parameter
  public void containsExactly_nullObjectArray() {
    StreamSubject subject = assertThat(Stream.of((Object) null));
    try {
      subject.containsExactly((Object[]) null);
    } catch (NullPointerException e) {
      // OK: possibly the implementation can't handle a null array parameter.
    }
  }

  @Test
  public void containsExactly_fails() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(Stream.of("hell", "hello")).containsExactly("hell"));
    assertFailureKeys(e, "unexpected (1)", "---", "expected", "but was");
    assertFailureValue(e, "expected", "[hell]");
  }

  @Test
  public void containsExactly_inOrder() {
    assertThat(Stream.of("hell", "hello")).containsExactly("hell", "hello").inOrder();
  }

  @Test
  public void containsExactly_inOrder_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsExactly("hello", "hell")
                    .inOrder());
    assertFailureKeys(e, "contents match, but order was wrong", "expected", "but was");
    assertFailureValue(e, "expected", "[hello, hell]");
  }

  @Test
  public void containsExactlyElementsIn() {
    assertThat(Stream.of("hell", "hello")).containsExactlyElementsIn(asList("hell", "hello"));
    assertThat(Stream.of("hell", "hello")).containsExactlyElementsIn(asList("hello", "hell"));
  }

  @Test
  public void containsExactlyElementsIn_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsExactlyElementsIn(asList("hell")));
    assertFailureKeys(e, "unexpected (1)", "---", "expected", "but was");
    assertFailureValue(e, "expected", "[hell]");
  }

  @Test
  public void containsExactlyElementsIn_inOrder() {
    assertThat(Stream.of("hell", "hello"))
        .containsExactlyElementsIn(asList("hell", "hello"))
        .inOrder();
  }

  @Test
  public void containsExactlyElementsIn_inOrder_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsExactlyElementsIn(asList("hello", "hell"))
                    .inOrder());
    assertFailureKeys(e, "contents match, but order was wrong", "expected", "but was");
    assertFailureValue(e, "expected", "[hello, hell]");
  }

  @Test
  public void isInOrder() {
    assertThat(Stream.of()).isInOrder();
    assertThat(Stream.of(1)).isInOrder();
    assertThat(Stream.of(1, 1, 2, 3, 3, 3, 4)).isInOrder();
  }

  @Test
  public void isInOrder_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of(1, 3, 2, 4)).isInOrder());
  }

  @Test
  public void isInStrictOrder() {
    assertThat(Stream.of()).isInStrictOrder();
    assertThat(Stream.of(1)).isInStrictOrder();
    assertThat(Stream.of(1, 2, 3, 4)).isInStrictOrder();
  }

  @Test
  public void isInStrictOrder_fails() {
    expectFailure(whenTesting -> whenTesting.that(Stream.of(1, 2, 2, 4)).isInStrictOrder());
  }
}
