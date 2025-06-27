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

import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.stream.LongStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link LongStream} Subjects.
 */
@RunWith(JUnit4.class)
public final class LongStreamSubjectTest {

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void isEqualTo() {
    LongStream stream = LongStream.of(42);
    assertThat(stream).isEqualTo(stream);
  }

  @Test
  public void isEqualToList() {
    LongStream stream = LongStream.of(42);
    List<Long> list = asList(42L);
    expectFailure(whenTesting -> whenTesting.that(stream).isEqualTo(list));
  }

  @Test
  public void nullStream_fails() {
    expectFailure(whenTesting -> whenTesting.that((LongStream) null).isEmpty());
  }

  @Test
  public void nullStreamIsNull() {
    assertThat((LongStream) null).isNull();
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void isSameInstanceAs() {
    LongStream stream = LongStream.of(1);
    assertThat(stream).isSameInstanceAs(stream);
  }

  @Test
  public void isEmpty() {
    assertThat(LongStream.of()).isEmpty();
  }

  @Test
  public void isEmpty_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(42)).isEmpty());
  }

  @Test
  public void isNotEmpty() {
    assertThat(LongStream.of(42)).isNotEmpty();
  }

  @Test
  public void isNotEmpty_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of()).isNotEmpty());
  }

  @Test
  public void hasSize() {
    assertThat(LongStream.of(42)).hasSize(1);
  }

  @Test
  public void hasSize_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(42)).hasSize(2));
  }

  @Test
  public void containsNoDuplicates() {
    assertThat(LongStream.of(42)).containsNoDuplicates();
  }

  @Test
  public void containsNoDuplicates_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(42, 42)).containsNoDuplicates());
  }

  @Test
  public void contains() {
    assertThat(LongStream.of(42)).contains(42);
  }

  @Test
  public void contains_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(42)).contains(100));
  }

  @Test
  public void containsAnyOf() {
    assertThat(LongStream.of(42)).containsAnyOf(42, 43);
  }

  @Test
  public void containsAnyOf_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(42)).containsAnyOf(43, 44));
  }

  @Test
  public void containsAnyIn() {
    assertThat(LongStream.of(42)).containsAnyIn(asList(42L, 43L));
  }

  @Test
  public void containsAnyIn_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(42)).containsAnyIn(asList(43, 44)));
  }

  @Test
  public void doesNotContain() {
    assertThat(LongStream.of(42)).doesNotContain(43);
  }

  @Test
  public void doesNotContain_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(42)).doesNotContain(42));
  }

  @Test
  public void containsNoneOf() {
    assertThat(LongStream.of(42)).containsNoneOf(43, 44);
  }

  @Test
  public void containsNoneOf_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(42)).containsNoneOf(42, 43));
  }

  @Test
  public void containsNoneIn() {
    assertThat(LongStream.of(42)).containsNoneIn(asList(43, 44));
  }

  @Test
  public void containsNoneIn_fails() {
    expectFailure(
        whenTesting -> whenTesting.that(LongStream.of(42)).containsNoneIn(asList(42L, 43L)));
  }

  @Test
  public void containsAtLeast() {
    assertThat(LongStream.of(42, 43)).containsAtLeast(42, 43);
  }

  @Test
  public void containsAtLeast_fails() {
    expectFailure(
        whenTesting -> whenTesting.that(LongStream.of(42, 43)).containsAtLeast(42, 43, 44));
  }

  @Test
  public void containsAtLeast_inOrder() {
    assertThat(LongStream.of(42, 43)).containsAtLeast(42, 43).inOrder();
  }

  @Test
  public void containsAtLeast_inOrder_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(LongStream.of(42, 43)).containsAtLeast(43, 42).inOrder());
    assertFailureKeys(
        e,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue(e, "expected order for required elements", "[43, 42]");
  }

  @Test
  public void containsAtLeastElementsIn() {
    assertThat(LongStream.of(42, 43)).containsAtLeastElementsIn(asList(42L, 43L));
  }

  @Test
  public void containsAtLeastElementsIn_fails() {
    expectFailure(
        whenTesting ->
            whenTesting
                .that(LongStream.of(42, 43))
                .containsAtLeastElementsIn(asList(42L, 43L, 44L)));
  }

  @Test
  public void containsAtLeastElementsIn_wrongType_fails() {
    expectFailure(
        whenTesting ->
            whenTesting.that(LongStream.of(42, 43)).containsAtLeastElementsIn(asList(42, 43, 44)));
  }

  @Test
  public void containsAtLeastElementsIn_inOrder() {
    assertThat(LongStream.of(42, 43)).containsAtLeastElementsIn(asList(42L, 43L)).inOrder();
  }

  @Test
  public void containsAtLeastElementsIn_inOrder_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(LongStream.of(42, 43))
                    .containsAtLeastElementsIn(asList(43L, 42L))
                    .inOrder());
    assertFailureKeys(
        e,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue(e, "expected order for required elements", "[43, 42]");
  }

  @Test
  public void containsAtLeastElementsIn_inOrder_wrongType_fails() {
    expectFailure(
        whenTesting ->
            whenTesting
                .that(LongStream.of(42, 43))
                .containsAtLeastElementsIn(asList(43, 42))
                .inOrder());
  }

  @Test
  public void containsExactly() {
    assertThat(LongStream.of(42, 43)).containsExactly(42, 43);
  }

  @Test
  public void containsExactly_fails() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(LongStream.of(42, 43)).containsExactly(42));
    assertFailureKeys(e, "unexpected (1)", "---", "expected", "but was");
    assertFailureValue(e, "expected", "[42]");
  }

  @Test
  public void testContainsExactly_nullExpected() {
    AssertionError expected =
        expectFailure(
            whenTesting -> whenTesting.that(LongStream.of(42, 43)).containsExactly((long[]) null));
    assertFailureKeys(
        expected,
        "could not perform containment check because expected array was null",
        "actual contents");
  }

  @Test
  public void containsExactly_inOrder() {
    assertThat(LongStream.of(42, 43)).containsExactly(42, 43).inOrder();
  }

  @Test
  public void containsExactly_inOrder_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(LongStream.of(42, 43)).containsExactly(43, 42).inOrder());
    assertFailureKeys(e, "contents match, but order was wrong", "expected", "but was");
    assertFailureValue(e, "expected", "[43, 42]");
  }

  @Test
  public void containsExactlyElementsIn() {
    assertThat(LongStream.of(42, 43)).containsExactlyElementsIn(asList(42L, 43L));
    assertThat(LongStream.of(42, 43)).containsExactlyElementsIn(asList(43L, 42L));
  }

  @Test
  public void containsExactlyElementsIn_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(LongStream.of(42, 43)).containsExactlyElementsIn(asList(42L)));
    assertFailureKeys(e, "unexpected (1)", "---", "expected", "but was");
    assertFailureValue(e, "expected", "[42]");
  }

  @Test
  public void containsExactlyElementsIn_wrongType_fails() {
    expectFailure(
        whenTesting ->
            whenTesting.that(LongStream.of(42, 43)).containsExactlyElementsIn(asList(42)));
  }

  @Test
  public void containsExactlyElementsIn_inOrder() {
    assertThat(LongStream.of(42, 43)).containsExactlyElementsIn(asList(42L, 43L)).inOrder();
  }

  @Test
  public void containsExactlyElementsIn_inOrder_fails() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(LongStream.of(42, 43))
                    .containsExactlyElementsIn(asList(43L, 42L))
                    .inOrder());
    assertFailureKeys(e, "contents match, but order was wrong", "expected", "but was");
    assertFailureValue(e, "expected", "[43, 42]");
  }

  @Test
  public void containsExactlyElementsIn_inOrder_wrongType_fails() {
    expectFailure(
        whenTesting ->
            whenTesting
                .that(LongStream.of(42, 43))
                .containsExactlyElementsIn(asList(43, 42))
                .inOrder());
  }

  @Test
  public void isInOrder() {
    assertThat(LongStream.of()).isInOrder();
    assertThat(LongStream.of(1)).isInOrder();
    assertThat(LongStream.of(1, 1, 2, 3, 3, 3, 4)).isInOrder();
  }

  @Test
  public void isInOrder_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(1, 3, 2, 4)).isInOrder());
  }

  @Test
  public void isInStrictOrder() {
    assertThat(LongStream.of()).isInStrictOrder();
    assertThat(LongStream.of(1)).isInStrictOrder();
    assertThat(LongStream.of(1, 2, 3, 4)).isInStrictOrder();
  }

  @Test
  public void isInStrictOrder_fails() {
    expectFailure(whenTesting -> whenTesting.that(LongStream.of(1, 2, 2, 4)).isInStrictOrder());
  }
}
