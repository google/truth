/*
 * Copyright (c) 2014 Google, Inc.
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
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MultisetSubject}. */
@RunWith(JUnit4.class)
public class MultisetSubjectTest {

  @Test
  public void hasCount() {
    ImmutableMultiset<String> multiset = ImmutableMultiset.of("kurt", "kurt", "kluever");
    assertThat(multiset).hasCount("kurt", 2);
    assertThat(multiset).hasCount("kluever", 1);
    assertThat(multiset).hasCount("alfred", 0);

    assertWithMessage("name").that(multiset).hasCount("kurt", 2);
  }

  @Test
  public void hasCountFail() {
    ImmutableMultiset<String> multiset = ImmutableMultiset.of("kurt", "kurt", "kluever");
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(multiset).hasCount("kurt", 3));
    assertFailureValue(e, "value of", "multiset.count(kurt)");
  }

  @Test
  public void hasCountFailNegative() {
    ImmutableMultiset<String> multiset = ImmutableMultiset.of("kurt", "kurt", "kluever");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(multiset).hasCount("kurt", -3));
    assertFailureKeys(
        e,
        "expected an element count that is negative, but that is impossible",
        "element",
        "expected count",
        "actual count",
        "multiset was");
    assertFailureValue(e, "element", "kurt");
    assertFailureValue(e, "expected count", "-3");
    assertFailureValue(e, "multiset was", "[kurt x 2, kluever]");
  }

  @Test
  public void hasCountOnNullMultiset() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Multiset<?>) null).hasCount("kurt", 3));
    assertFailureKeys(
        e,
        "cannot perform assertions on the contents of a null multiset",
        "element",
        "expected count");
    assertFailureValue(e, "element", "kurt");
    assertFailureValue(e, "expected count", "3");
  }
}
