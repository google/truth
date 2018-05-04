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
import static com.google.common.truth.IntStreamSubject.intStreams;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Java 8 {@link IntStream} Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public final class IntStreamSubjectTest {

  @Test
  public void testIsEqualTo() throws Exception {
    IntStream stream = IntStream.of(42);
    assertThat(stream).isEqualTo(stream);
  }

  @Test
  public void testIsEqualToList() throws Exception {
    IntStream stream = IntStream.of(42);
    List<Integer> list = asList(42);
    AssertionError unused = expectFailure(whenTesting -> whenTesting.that(stream).isEqualTo(list));
  }

  @Test
  public void testNullStream_fails() throws Exception {
    IntStream nullStream = null;
    try {
      assertThat(nullStream).isEmpty();
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testNullStreamIsNull() throws Exception {
    IntStream nullStream = null;
    assertThat(nullStream).isNull();
  }

  @Test
  public void testIsSameAs() throws Exception {
    IntStream stream = IntStream.of(1);
    assertThat(stream).isSameAs(stream);
  }

  @Test
  public void testIsEmpty() throws Exception {
    assertThat(IntStream.of()).isEmpty();
  }

  @Test
  public void testIsEmpty_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(IntStream.of(42)).isEmpty());
  }

  @Test
  public void testIsNotEmpty() throws Exception {
    assertThat(IntStream.of(42)).isNotEmpty();
  }

  @Test
  public void testIsNotEmpty_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(IntStream.of()).isNotEmpty());
  }

  @Test
  public void testHasSize() throws Exception {
    assertThat(IntStream.of(42)).hasSize(1);
  }

  @Test
  public void testHasSize_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(IntStream.of(42)).hasSize(2));
  }

  @Test
  public void testContainsNoDuplicates() throws Exception {
    assertThat(IntStream.of(42)).containsNoDuplicates();
  }

  @Test
  public void testContainsNoDuplicates_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(IntStream.of(42, 42)).containsNoDuplicates());
  }

  @Test
  public void testContains() throws Exception {
    assertThat(IntStream.of(42)).contains(42);
  }

  @Test
  public void testContains_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(IntStream.of(42)).contains(100));
  }

  @Test
  public void testContainsAnyOf() throws Exception {
    assertThat(IntStream.of(42)).containsAnyOf(42, 43);
  }

  @Test
  public void testContainsAnyOf_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(IntStream.of(42)).containsAnyOf(43, 44));
  }

  @Test
  public void testContainsAnyIn() throws Exception {
    assertThat(IntStream.of(42)).containsAnyIn(asList(42, 43));
  }

  @Test
  public void testContainsAnyIn_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting -> whenTesting.that(IntStream.of(42)).containsAnyIn(asList(43, 44)));
  }

  @Test
  public void testDoesNotContain() throws Exception {
    assertThat(IntStream.of(42)).doesNotContain(43);
  }

  @Test
  public void testDoesNotContain_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(IntStream.of(42)).doesNotContain(42));
  }

  @Test
  public void testContainsNoneOf() throws Exception {
    assertThat(IntStream.of(42)).containsNoneOf(43, 44);
  }

  @Test
  public void testContainsNoneOf_fails() throws Exception {
    AssertionError unused =
        expectFailure(whenTesting -> whenTesting.that(IntStream.of(42)).containsNoneOf(42, 43));
  }

  @Test
  public void testContainsNoneIn() throws Exception {
    assertThat(IntStream.of(42)).containsNoneIn(asList(43, 44));
  }

  @Test
  public void testContainsNoneIn_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting -> whenTesting.that(IntStream.of(42)).containsNoneIn(asList(42, 43)));
  }

  @Test
  public void testContainsAllOf() throws Exception {
    assertThat(IntStream.of(42, 43)).containsAllOf(42, 43);
  }

  @Test
  public void testContainsAllOf_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting -> whenTesting.that(IntStream.of(42, 43)).containsAllOf(42, 43, 44));
  }

  @Test
  public void testContainsAllOf_inOrder() throws Exception {
    assertThat(IntStream.of(42, 43)).containsAllOf(42, 43).inOrder();
  }

  @Test
  public void testContainsAllOf_inOrder_fails() throws Exception {
    try {
      assertThat(IntStream.of(42, 43)).containsAllOf(43, 42).inOrder();
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(
          expected,
          "required elements were all found, but order was wrong",
          "expected order for required elements",
          "but was");
      assertFailureValue(expected, "expected order for required elements", "[43, 42]");
    }
  }

  @Test
  public void testContainsAllIn() throws Exception {
    assertThat(IntStream.of(42, 43)).containsAllIn(asList(42, 43));
  }

  @Test
  public void testContainsAllIn_fails() throws Exception {
    AssertionError unused =
        expectFailure(
            whenTesting ->
                whenTesting.that(IntStream.of(42, 43)).containsAllIn(asList(42, 43, 44)));
  }

  @Test
  public void testContainsAllIn_inOrder() throws Exception {
    assertThat(IntStream.of(42, 43)).containsAllIn(asList(42, 43)).inOrder();
  }

  @Test
  public void testContainsAllIn_inOrder_fails() throws Exception {
    try {
      assertThat(IntStream.of(42, 43)).containsAllIn(asList(43, 42)).inOrder();
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(
          expected,
          "required elements were all found, but order was wrong",
          "expected order for required elements",
          "but was");
      assertFailureValue(expected, "expected order for required elements", "[43, 42]");
    }
  }

  @Test
  public void testContainsExactly() throws Exception {
    assertThat(IntStream.of(42, 43)).containsExactly(42, 43);
  }

  @Test
  public void testContainsExactly_fails() throws Exception {
    try {
      assertThat(IntStream.of(42, 43)).containsExactly(42);
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(expected, "unexpected (1)", "---", "expected", "but was");
      assertFailureValue(expected, "expected", "[42]");
    }
  }

  @Test
  public void testContainsExactly_inOrder() throws Exception {
    assertThat(IntStream.of(42, 43)).containsExactly(42, 43).inOrder();
  }

  @Test
  public void testContainsExactly_inOrder_fails() throws Exception {
    try {
      assertThat(IntStream.of(42, 43)).containsExactly(43, 42).inOrder();
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(expected, "contents match, but order was wrong", "expected", "but was");
      assertFailureValue(expected, "expected", "[43, 42]");
    }
  }

  @Test
  public void testContainsExactlyElementsIn() throws Exception {
    assertThat(IntStream.of(42, 43)).containsExactlyElementsIn(asList(42, 43));
    assertThat(IntStream.of(42, 43)).containsExactlyElementsIn(asList(43, 42));
  }

  @Test
  public void testContainsExactlyElementsIn_fails() throws Exception {
    try {
      assertThat(IntStream.of(42, 43)).containsExactlyElementsIn(asList(42));
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(expected, "unexpected (1)", "---", "expected", "but was");
      assertFailureValue(expected, "expected", "[42]");
    }
  }

  @Test
  public void testContainsExactlyElementsIn_inOrder() throws Exception {
    assertThat(IntStream.of(42, 43)).containsExactlyElementsIn(asList(42, 43)).inOrder();
  }

  @Test
  public void testContainsExactlyElementsIn_inOrder_fails() throws Exception {
    try {
      assertThat(IntStream.of(42, 43)).containsExactlyElementsIn(asList(43, 42)).inOrder();
      fail();
    } catch (AssertionError expected) {
      assertFailureKeys(expected, "contents match, but order was wrong", "expected", "but was");
      assertFailureValue(expected, "expected", "[43, 42]");
    }
  }

  @Test
  public void testContainsExactlyElementsIn_inOrder_intStream() throws Exception {
    assertThat(IntStream.of(1, 2, 3, 4)).containsExactly(1, 2, 3, 4).inOrder();
  }

  private static AssertionError expectFailure(
      ExpectFailure.SimpleSubjectBuilderCallback<IntStreamSubject, IntStream> assertionCallback) {
    return ExpectFailure.expectFailureAbout(intStreams(), assertionCallback);
  }
}
