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

import static com.google.common.base.Strings.repeat;
import static com.google.common.truth.ComparisonFailureWithFields.formatExpectedAndActual;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link ComparisonFailureWithFields}. */
@RunWith(JUnit4.class)
public class ComparisonFailureWithFieldsTest {
  @Test
  public void formatAllDifferent() {
    runFormatTest(
        "foo", "bar",
        "foo", "bar");
  }

  @Test
  public void formatShortOverlap() {
    runFormatTest(
        "bar", "baz",
        "bar", "baz");
  }

  @Test
  public void formatLongOverlapStart() {
    runFormatTest(repeat("b", 100) + "aa", repeat("b", 100) + "oo", "…bbbbbaa", "…bbbbboo");
  }

  @Test
  public void formatLongOverlapEnd() {
    runFormatTest("ba" + repeat("r", 100), "fu" + repeat("r", 100), "barrrrr…", "furrrrr…");
  }

  @Test
  public void formatLongOverlapStartAlsoSmallAtEnd() {
    runFormatTest(
        repeat("b", 100) + "aa" + repeat("t", 7),
        repeat("b", 100) + "oo" + repeat("t", 7),
        "…bbbbbaattttttt",
        "…bbbbboottttttt");
  }

  @Test
  public void formatLongOverlapEndAlsoSmallAtStart() {
    runFormatTest(
        repeat("a", 7) + "ba" + repeat("r", 100),
        repeat("a", 7) + "fu" + repeat("r", 100),
        "aaaaaaabarrrrr…",
        "aaaaaaafurrrrr…");
  }

  @Test
  public void formatLongOverlapBoth() {
    runFormatTest(
        repeat("r", 40) + "a" + repeat("g", 40),
        repeat("r", 40) + "u" + repeat("g", 40),
        "…rrrrraggggg…",
        "…rrrrruggggg…");
  }

  @Test
  public void formatNoSplitSurrogateStart() {
    runFormatTest(
        repeat("b", 100) + "\uD8AB\uDCABbbbbaa",
        repeat("b", 100) + "\uD8AB\uDCABbbbboo",
        "…\uD8AB\uDCABbbbbaa",
        "…\uD8AB\uDCABbbbboo");
  }

  @Test
  public void formatNoSplitSurrogateEnd() {
    runFormatTest(
        "barrrr\uD8AB\uDCAB" + repeat("r", 100),
        "furrrr\uD8AB\uDCAB" + repeat("r", 100),
        "barrrr\uD8AB\uDCAB…",
        "furrrr\uD8AB\uDCAB…");
  }

  private static void runFormatTest(
      String expected, String actual, String expectedExpected, String expectedActual) {
    ImmutableList<Field> fields = formatExpectedAndActual(expected, actual);
    assertThat(fields).hasSize(2);
    assertThat(fields.get(0).key).isEqualTo("expected");
    assertThat(fields.get(1).key).isEqualTo("but was");
    assertThat(fields.get(0).value).isEqualTo(expectedExpected);
    assertThat(fields.get(1).value).isEqualTo(expectedActual);
  }
}
