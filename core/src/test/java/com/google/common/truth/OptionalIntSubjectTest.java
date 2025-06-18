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
import static com.google.common.truth.Truth.assertThat;

import java.util.OptionalInt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link OptionalInt} Subjects.
 *
 * @author Ben Douglass
 */
@RunWith(JUnit4.class)
public class OptionalIntSubjectTest {

  @Test
  public void failOnNullSubject() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((OptionalInt) null).isEmpty());
    assertThat(e).factKeys().containsExactly("expected empty optional", "but was").inOrder();
  }

  @Test
  public void isPresent() {
    assertThat(OptionalInt.of(1337)).isPresent();
  }

  @Test
  public void isPresentFailing() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.empty()).isPresent());
    assertThat(e).factKeys().containsExactly("expected to be present");
  }

  @Test
  public void isEmpty() {
    assertThat(OptionalInt.empty()).isEmpty();
  }

  @Test
  public void isEmptyFailing() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.of(1337)).isEmpty());
    assertThat(e).factKeys().contains("expected to be empty");
    assertThat(e).factValue("but was present with value").isEqualTo("1337");
  }

  @Test
  public void isEmptyFailingNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((OptionalInt) null).isEmpty());
    assertThat(e).factKeys().containsExactly("expected empty optional", "but was").inOrder();
  }

  @Test
  public void hasValue() {
    assertThat(OptionalInt.of(1337)).hasValue(1337);
  }

  @Test
  public void hasValue_failingWithEmpty() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.empty()).hasValue(1337));
    assertThat(e).factKeys().containsExactly("expected to have value", "but was absent").inOrder();
    assertThat(e).factValue("expected to have value").isEqualTo("1337");
  }

  @Test
  public void hasValue_failingWithWrongValue() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(OptionalInt.of(1337)).hasValue(42));
    assertThat(e).factValue("value of").isEqualTo("optionalInt.getAsInt()");
  }
}
