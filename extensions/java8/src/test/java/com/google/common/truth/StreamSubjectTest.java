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

import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.StreamSubject.streams;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link Stream} Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public final class StreamSubjectTest {

  @Test
  public void testIsEqualTo() throws Exception {
    Stream<String> stream = Stream.of("hello");
    assertThat(stream).isEqualTo(stream);
  }

  @Test
  public void testIsEqualToList() throws Exception {
    Stream<String> stream = Stream.of("hello");
    List<String> list = asList("hello");
    AssertionError unused = expectFailure(whenTesting -> whenTesting.that(stream).isEqualTo(list));
  }

  @Test
  public void testNullStream_fails() throws Exception {
    Stream<String> nullStream = null;
    try {
      assertThat(nullStream).isEmpty();
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testNullStreamIsNull() throws Exception {
    Stream<String> nullStream = null;
    assertThat(nullStream).isNull();
  }

  @Test
  public void testIsSameAs() throws Exception {
    Stream<String> stream = Stream.of("hello");
    assertThat(stream).isSameAs(stream);
  }

  @Test
  public void testIsEmpty() throws Exception {
    assertThat(Stream.of()).isEmpty();
  }

  @Test
  public void testIsEmpty_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).isEmpty());
  }

  @Test
  public void testIsNotEmpty() throws Exception {
    assertThat(Stream.of("hello")).isNotEmpty();
  }

  @Test
  public void testIsNotEmpty_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(Stream.of()).isNotEmpty());
  }

  @Test
  public void testHasSize() throws Exception {
    assertThat(Stream.of("hello")).hasSize(1);
  }

  @Test
  public void testHasSize_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).hasSize(2));
  }

  @Test
  public void testContainsNoDuplicates() throws Exception {
    assertThat(Stream.of("hello")).containsNoDuplicates();
  }

  @Test
  public void testContainsNoDuplicates_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting -> whenTesting.that(Stream.of("hello", "hello")).containsNoDuplicates());
  }

  @Test
  public void testContains() throws Exception {
    assertThat(Stream.of("hello")).contains("hello");
  }

  @Test
  public void testContains_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).contains("goodbye"));
  }

  @Test
  public void testContainsAnyOf() throws Exception {
    assertThat(Stream.of("hello")).containsAnyOf("hello", "hell");
  }

  @Test
  public void testContainsAnyOf_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting -> whenTesting.that(Stream.of("hello")).containsAnyOf("goodbye", "good"));
  }

  @Test
  public void testContainsAnyIn() throws Exception {
    assertThat(Stream.of("hello")).containsAnyIn(asList("hello", "hell"));
  }

  @Test
  public void testContainsAnyIn_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting ->
                whenTesting.that(Stream.of("hello")).containsAnyIn(asList("goodbye", "good")));
  }

  @Test
  public void testDoesNotContain() throws Exception {
    assertThat(Stream.of("hello")).doesNotContain("goodbye");
  }

  @Test
  public void testDoesNotContain_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(Stream.of("hello")).doesNotContain("hello"));
  }

  @Test
  public void testContainsNoneOf() throws Exception {
    assertThat(Stream.of("hello")).containsNoneOf("goodbye", "good");
  }

  @Test
  public void testContainsNoneOf_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting -> whenTesting.that(Stream.of("hello")).containsNoneOf("hello", "hell"));
  }

  @Test
  public void testContainsNoneIn() throws Exception {
    assertThat(Stream.of("hello")).containsNoneIn(asList("goodbye", "good"));
  }

  @Test
  public void testContainsNoneIn_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting ->
                whenTesting.that(Stream.of("hello")).containsNoneIn(asList("hello", "hell")));
  }

  @Test
  public void testContainsAllOf() throws Exception {
    assertThat(Stream.of("hell", "hello")).containsAllOf("hell", "hello");
  }

  @Test
  public void testContainsAllOf_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsAllOf("hell", "hello", "goodbye"));
  }

  @Test
  public void testContainsAllOf_inOrder() throws Exception {
    assertThat(Stream.of("hell", "hello")).containsAllOf("hell", "hello").inOrder();
  }

  @Test
  public void testContainsAllOf_inOrder_fails() throws Exception {
    try {
      assertThat(Stream.of("hell", "hello")).containsAllOf("hello", "hell").inOrder();
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(
          expected,
          "required elements were all found, but order was wrong",
          "expected order for required elements",
          "but was");
      assertFailureValue(expected, "expected order for required elements", "[hello, hell]");
    }
  }

  @Test
  public void testContainsAllIn() throws Exception {
    assertThat(Stream.of("hell", "hello")).containsAllIn(asList("hell", "hello"));
  }

  @Test
  public void testContainsAllIn_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(Stream.of("hell", "hello"))
                    .containsAllIn(asList("hell", "hello", "goodbye")));
  }

  @Test
  public void testContainsAllIn_inOrder() throws Exception {
    assertThat(Stream.of("hell", "hello")).containsAllIn(asList("hell", "hello")).inOrder();
  }

  @Test
  public void testContainsAllIn_inOrder_fails() throws Exception {
    try {
      assertThat(Stream.of("hell", "hello")).containsAllIn(asList("hello", "hell")).inOrder();
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(
          expected,
          "required elements were all found, but order was wrong",
          "expected order for required elements",
          "but was");
      assertFailureValue(expected, "expected order for required elements", "[hello, hell]");
    }
  }

  @Test
  public void testContainsExactly() throws Exception {
    assertThat(Stream.of("hell", "hello")).containsExactly("hell", "hello");
    assertThat(Stream.of("hell", "hello")).containsExactly("hello", "hell");
  }

  @Test
  public void testContainsExactly_fails() throws Exception {
    try {
      assertThat(Stream.of("hell", "hello")).containsExactly("hell");
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(expected, "unexpected (1)", "---", "expected", "but was");
      assertFailureValue(expected, "expected", "[hell]");
    }
  }

  @Test
  public void testContainsExactly_inOrder() throws Exception {
    assertThat(Stream.of("hell", "hello")).containsExactly("hell", "hello").inOrder();
  }

  @Test
  public void testContainsExactly_inOrder_fails() throws Exception {
    try {
      assertThat(Stream.of("hell", "hello")).containsExactly("hello", "hell").inOrder();
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(expected, "contents match, but order was wrong", "expected", "but was");
      assertFailureValue(expected, "expected", "[hello, hell]");
    }
  }

  @Test
  public void testContainsExactlyElementsIn() throws Exception {
    assertThat(Stream.of("hell", "hello")).containsExactlyElementsIn(asList("hell", "hello"));
    assertThat(Stream.of("hell", "hello")).containsExactlyElementsIn(asList("hello", "hell"));
  }

  @Test
  public void testContainsExactlyElementsIn_fails() throws Exception {
    try {
      assertThat(Stream.of("hell", "hello")).containsExactlyElementsIn(asList("hell"));
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(expected, "unexpected (1)", "---", "expected", "but was");
      assertFailureValue(expected, "expected", "[hell]");
    }
  }

  @Test
  public void testContainsExactlyElementsIn_inOrder() throws Exception {
    assertThat(Stream.of("hell", "hello"))
        .containsExactlyElementsIn(asList("hell", "hello"))
        .inOrder();
  }

  @Test
  public void testContainsExactlyElementsIn_inOrder_fails() throws Exception {
    try {
      assertThat(Stream.of("hell", "hello"))
          .containsExactlyElementsIn(asList("hello", "hell"))
          .inOrder();
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(expected, "contents match, but order was wrong", "expected", "but was");
      assertFailureValue(expected, "expected", "[hello, hell]");
    }
  }

  private static AssertionError expectFailure(
      ExpectFailure.SimpleSubjectBuilderCallback<StreamSubject, Stream<?>> assertionCallback) {
    return ExpectFailure.expectFailureAbout(streams(), assertionCallback);
  }
}
