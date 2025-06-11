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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.collect.Range;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Comparable Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class ComparableSubjectTest {

  @Test
  @SuppressWarnings("deprecation") // test of an unnecessary use of isEquivalentAccordingToCompareTo
  public void testNullActual() {
    expectFailure(
        whenTesting -> whenTesting.that((Integer) null).isEquivalentAccordingToCompareTo(6));
    expectFailure(whenTesting -> whenTesting.that((Integer) null).isGreaterThan(6));
    expectFailure(whenTesting -> whenTesting.that((Integer) null).isLessThan(6));
    expectFailure(whenTesting -> whenTesting.that((Integer) null).isAtMost(6));
    expectFailure(whenTesting -> whenTesting.that((Integer) null).isAtLeast(6));
  }

  @Test
  // test of a mistaken call and of unnecessary use of isEquivalentAccordingToCompareTo
  @SuppressWarnings({"deprecation", "IntegerComparison"})
  public void testNullExpected() {
    expectFailure(whenTesting -> whenTesting.that(6).isEquivalentAccordingToCompareTo(null));
    expectFailure(whenTesting -> whenTesting.that(6).isGreaterThan(null));
    expectFailure(whenTesting -> whenTesting.that(6).isLessThan(null));
    expectFailure(whenTesting -> whenTesting.that(6).isAtMost(null));
    expectFailure(whenTesting -> whenTesting.that(6).isAtLeast(null));
    expectFailure(whenTesting -> whenTesting.that(6).isIn((Range<Integer>) null));
    expectFailure(whenTesting -> whenTesting.that(6).isNotIn((Range<Integer>) null));
  }

  @Test
  public void isInRange() {
    Range<Integer> oneToFive = Range.closed(1, 5);
    assertThat(4).isIn(oneToFive);

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(6).isIn(oneToFive));
    assertThat(e).factValue("expected to be in range").isEqualTo(oneToFive.toString());
  }

  @Test
  public void isNotInRange() {
    Range<Integer> oneToFive = Range.closed(1, 5);
    assertThat(6).isNotIn(oneToFive);

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(4).isNotIn(oneToFive));
    assertThat(e).factValue("expected not to be in range").isEqualTo(oneToFive.toString());
  }

  @Test
  public void isEquivalentAccordingToCompareTo() {
    assertThat(new StringComparedByLength("abc"))
        .isEquivalentAccordingToCompareTo(new StringComparedByLength("xyz"));

    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(new StringComparedByLength("abc"))
                    .isEquivalentAccordingToCompareTo(new StringComparedByLength("abcd")));
    assertFailureValue(e, "expected value that sorts equal to", "abcd");
  }

  private static final class StringComparedByLength implements Comparable<StringComparedByLength> {
    private final String value;

    StringComparedByLength(String value) {
      this.value = checkNotNull(value);
    }

    @Override
    public int compareTo(StringComparedByLength other) {
      /*
       * Even though Integer.compare was added in Java 7, we use it even under old versions of
       * Android, as discussed in IterableSubjectTest.
       */
      return Integer.compare(value.length(), other.value.length());
    }

    @Override
    public String toString() {
      return value;
    }
  }

  @Test
  public void isGreaterThan_failsEqual() {
    assertThat(5).isGreaterThan(4);

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(4).isGreaterThan(4));
    assertFailureValue(e, "expected to be greater than", "4");
  }

  @Test
  public void isGreaterThan_failsSmaller() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(3).isGreaterThan(4));
    assertFailureValue(e, "expected to be greater than", "4");
  }

  @Test
  public void isLessThan_failsEqual() {
    assertThat(4).isLessThan(5);

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(4).isLessThan(4));
    assertFailureValue(e, "expected to be less than", "4");
  }

  @Test
  public void isLessThan_failsGreater() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(4).isLessThan(3));
    assertFailureValue(e, "expected to be less than", "3");
  }

  @Test
  public void isAtMost() {
    assertThat(5).isAtMost(5);
    assertThat(5).isAtMost(6);

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(4).isAtMost(3));
    assertFailureValue(e, "expected to be at most", "3");
  }

  @Test
  public void isAtLeast() {
    assertThat(4).isAtLeast(3);
    assertThat(4).isAtLeast(4);

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(4).isAtLeast(5));
    assertFailureValue(e, "expected to be at least", "5");
  }

  // Brief tests with other comparable types (no negative test cases)

  @Test
  public void longs() {
    assertThat(5L).isGreaterThan(4L);
    assertThat(4L).isLessThan(5L);

    assertThat(4L).isAtMost(4L);
    assertThat(4L).isAtMost(5L);
    assertThat(4L).isAtLeast(4L);
    assertThat(4L).isAtLeast(3L);

    Range<Long> range = Range.closed(2L, 4L);
    assertThat(3L).isIn(range);
    assertThat(5L).isNotIn(range);
  }

  @Test
  public void strings() {
    assertThat("kak").isGreaterThan("gak");
    assertThat("gak").isLessThan("kak");

    assertThat("kak").isAtMost("kak");
    assertThat("gak").isAtMost("kak");
    assertThat("kak").isAtLeast("kak");
    assertThat("kak").isAtLeast("gak");

    Range<String> range = Range.closed("a", "c");
    assertThat("b").isIn(range);
    assertThat("d").isNotIn(range);
  }

  @Test
  public void comparableType() {
    assertThat(new ComparableType(4)).isGreaterThan(new ComparableType(3));
    assertThat(new ComparableType(3)).isLessThan(new ComparableType(4));
  }

  @Test
  public void namedComparableType() {
    assertWithMessage("comparable").that(new ComparableType(2)).isLessThan(new ComparableType(3));
  }

  private static final class ComparableType implements Comparable<ComparableType> {
    private final int wrapped;

    private ComparableType(int toWrap) {
      this.wrapped = toWrap;
    }

    @Override
    public int compareTo(ComparableType other) {
      return wrapped - other.wrapped;
    }
  }

  @Test
  public void rawComparableType() {
    assertThat(new RawComparableType(3)).isLessThan(new RawComparableType(4));
  }

  @SuppressWarnings({"ComparableType", "rawtypes"})
  private static final class RawComparableType implements Comparable {
    private final int wrapped;

    private RawComparableType(int toWrap) {
      this.wrapped = toWrap;
    }

    @Override
    public int compareTo(Object other) {
      return wrapped - ((RawComparableType) other).wrapped;
    }

    @Override
    public String toString() {
      return Integer.toString(wrapped);
    }
  }
}
