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

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.junit.Assert.fail;

import com.google.common.collect.Range;
import com.google.testing.compile.JavaFileObjects;
import java.math.BigDecimal;
import javax.tools.JavaFileObject;
import org.junit.Rule;
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
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void testNulls() {
    try {
      assertThat(6).isEquivalentAccordingToCompareTo(null);
      fail();
    } catch (NullPointerException expected) {
    }
    try {
      assertThat(6).isGreaterThan(null);
      fail();
    } catch (NullPointerException expected) {
    }
    try {
      assertThat(6).isLessThan(null);
      fail();
    } catch (NullPointerException expected) {
    }
    try {
      assertThat(6).isAtMost(null);
      fail();
    } catch (NullPointerException expected) {
    }
    try {
      assertThat(6).isAtLeast(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void isInRange() {
    Range<Integer> oneToFive = Range.closed(1, 5);
    assertThat(4).isIn(oneToFive);

    expectFailure.whenTesting().that(6).isIn(oneToFive);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <6> is in <" + oneToFive + ">");
  }

  @Test
  public void isNotInRange() {
    Range<Integer> oneToFive = Range.closed(1, 5);
    assertThat(6).isNotIn(oneToFive);

    expectFailure.whenTesting().that(4).isNotIn(oneToFive);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is not in <" + oneToFive + ">");
  }

  @Test
  public void isEquivalentAccordingToCompareTo() {
    assertThat(new BigDecimal("2.0")).isEquivalentAccordingToCompareTo(new BigDecimal("2.00"));

    expectFailure
        .whenTesting()
        .that(new BigDecimal("2.0"))
        .isEquivalentAccordingToCompareTo(new BigDecimal("2.1"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<2.0> should have had the same value as <2.1> (scale is ignored)");
  }

  @Test
  public void isGreaterThan_failsEqual() {
    assertThat(5).isGreaterThan(4);

    expectFailure.whenTesting().that(4).isGreaterThan(4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is greater than <4>");
  }

  @Test
  public void isGreaterThan_failsSmaller() {
    expectFailure.whenTesting().that(3).isGreaterThan(4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <3> is greater than <4>");
  }

  @Test
  public void isLessThan_failsEqual() {
    assertThat(4).isLessThan(5);

    expectFailure.whenTesting().that(4).isLessThan(4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is less than <4>");
  }

  @Test
  public void isLessThan_failsGreater() {
    expectFailure.whenTesting().that(4).isLessThan(3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is less than <3>");
  }

  @Test
  public void isAtMost() {
    assertThat(5).isAtMost(5);
    assertThat(5).isAtMost(6);

    expectFailure.whenTesting().that(4).isAtMost(3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is at most <3>");
  }

  @Test
  public void isAtLeast() {
    assertThat(4).isAtLeast(3);
    assertThat(4).isAtLeast(4);

    expectFailure.whenTesting().that(4).isAtLeast(5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is at least <5>");
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
    assertThat(new ComparableType(2)).named("comparable").isLessThan(new ComparableType(3));
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

  @Test
  public void comparableMixedTypesDontCompile() {
    JavaFileObject file =
        JavaFileObjects.forSourceLines(
            "test.MyTest",
            "package test;",
            "import static com.google.common.truth.Truth.assertThat;",
            "class MyTest {",
            "  public void testFoo() {",
            "    assertThat(new ComparableType(3)).isLessThan(\"kak\");",
            "  }",
            "  private static final class ComparableType implements Comparable<ComparableType> {",
            "    private final int wrapped;",
            "    private ComparableType(int toWrap) {",
            "      this.wrapped = toWrap;",
            "    }",
            "    @Override public int compareTo(ComparableType other) {",
            "      return wrapped - ((ComparableType) other).wrapped;",
            "    }",
            "  }",
            "}");

    assertAbout(javaSource())
        .that(file)
        .failsToCompile()
        .withErrorContaining("java.lang.String cannot be converted to test.MyTest.ComparableType")
        .in(file)
        .onLine(5);
  }

  @Test
  public void rawComparableTypeMixedTypes() {
    JavaFileObject file =
        JavaFileObjects.forSourceLines(
            "test.MyTest",
            "package test;",
            "import static com.google.common.truth.Truth.assertThat;",
            "class MyTest {",
            "  public void testFoo() {",
            "    assertThat(new RawComparableType(3)).isLessThan(\"kak\");",
            "  }",
            "  private static final class RawComparableType implements Comparable {",
            "    private final int wrapped;",
            "    private RawComparableType(int toWrap) {",
            "      this.wrapped = toWrap;",
            "    }",
            "    @Override public int compareTo(Object other) {",
            "      return wrapped - ((RawComparableType) other).wrapped;",
            "    }",
            "  }",
            "}");
    assertAbout(javaSource())
        .that(file)
        .failsToCompile()
        .withErrorContaining(
            "java.lang.String cannot be converted to test.MyTest.RawComparableType")
        .in(file)
        .onLine(5);
  }
}
