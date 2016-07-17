/*
 * Copyright (c) 2015 Google, Inc.
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

import java.util.Comparator;

import javax.tools.JavaFileObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.testing.compile.JavaFileObjects;

/**
 * Tests for FauxComparable objects.
 *
 * @author Ben Blank
 */
@RunWith(JUnit4.class)
public class FauxComparableTest {
  private static final Comparator<IncomparableType> ALWAYS_LESS =
      new Comparator<IncomparableType>() {
        @Override
        public int compare(IncomparableType o1, IncomparableType o2) {
          return -1;
        }

        @Override
        public String toString() {
          return "always_less";
        }
      };

  private static final Comparator<IncomparableType> ALWAYS_EQUAL =
      new Comparator<IncomparableType>() {
        @Override
        public int compare(IncomparableType o1, IncomparableType o2) {
          return 0;
        }

        @Override
        public String toString() {
          return "always_equal";
        }
      };

  private static final Comparator<IncomparableType> ALWAYS_MORE =
      new Comparator<IncomparableType>() {
        @Override
        public int compare(IncomparableType o1, IncomparableType o2) {
          return 1;
        }

        @Override
        public String toString() {
          return "always_more";
        }
      };

  private static final IncomparableType FOO = new IncomparableType("foo");
  private static final IncomparableType BAR = new IncomparableType("bar");

  @Test
  public void testNull() {
    try {
      assertThat(FOO).whenComparedUsing(null);
      fail("should have thrown");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void isEquivalentAccordingToCompareTo() {
    try {
      assertThat(FOO).whenComparedUsing(ALWAYS_LESS).isEquivalentAccordingToComparator(BAR);
      fail("should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains(
              "<foo> should have been equivalent to <bar> according to comparator <always_less>");
    }
    assertThat(FOO).whenComparedUsing(ALWAYS_EQUAL).isEquivalentAccordingToComparator(BAR);
    try {
      assertThat(FOO).whenComparedUsing(ALWAYS_MORE).isEquivalentAccordingToComparator(BAR);
      fail("should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains(
              "<foo> should have been equivalent to <bar> according to comparator <always_more>");
    }
  }

  @Test
  public void isGreaterThan() {
    try {
      assertThat(FOO).whenComparedUsing(ALWAYS_LESS).isGreaterThan(BAR);
      fail("should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains(
              "Not true that <foo> is greater than <bar> according to comparator <always_less>");
    }
    try {
      assertThat(FOO).whenComparedUsing(ALWAYS_EQUAL).isGreaterThan(BAR);
      fail("should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains(
              "Not true that <foo> is greater than <bar> according to comparator <always_equal>");
    }
    assertThat(FOO).whenComparedUsing(ALWAYS_MORE).isGreaterThan(BAR);
  }

  @Test
  public void isLessThan() {
    assertThat(FOO).whenComparedUsing(ALWAYS_LESS).isLessThan(BAR);
    try {
      assertThat(FOO).whenComparedUsing(ALWAYS_EQUAL).isLessThan(BAR);
      fail("should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains(
              "Not true that <foo> is less than <bar> according to comparator <always_equal>");
    }
    try {
      assertThat(FOO).whenComparedUsing(ALWAYS_MORE).isLessThan(BAR);
      fail("should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains("Not true that <foo> is less than <bar> according to comparator <always_more>");
    }
  }

  @Test
  public void isAtMost() {
    assertThat(FOO).whenComparedUsing(ALWAYS_LESS).isAtMost(BAR);
    assertThat(FOO).whenComparedUsing(ALWAYS_EQUAL).isAtMost(BAR);
    try {
      assertThat(FOO).whenComparedUsing(ALWAYS_MORE).isAtMost(BAR);
      fail("should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains("Not true that <foo> is at most <bar> according to comparator <always_more>");
    }
  }

  @Test
  public void isAtLeast() {
    try {
      assertThat(FOO).whenComparedUsing(ALWAYS_LESS).isAtLeast(BAR);
      fail("should have thrown");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains("Not true that <foo> is at least <bar> according to comparator <always_less>");
    }
    assertThat(FOO).whenComparedUsing(ALWAYS_EQUAL).isAtLeast(BAR);
    assertThat(FOO).whenComparedUsing(ALWAYS_MORE).isAtLeast(BAR);
  }

  @Test
  public void broadComparator() {
    assertThat(FOO)
        .whenComparedUsing(
            new Comparator<Object>() {
              @Override
              public int compare(Object o1, Object o2) {
                return 0;
              }
            })
        .isEquivalentAccordingToComparator(BAR);
  }

  @Test
  public void incompatibleComparatorDoesntCompile() {
    JavaFileObject file =
        JavaFileObjects.forSourceLines(
            "test.MyTest",
            "package test;",
            "import static com.google.common.truth.Truth.assertThat;",
            "import java.util.Comparator;",
            "class MyTest {",
            "  private static final Comparator<String> COMPARATOR = new Comparator<String>() {",
            "    @Override",
            "    public int compare(String s1, String s2) {",
            "      return 0;",
            "    }",
            "  };",
            "  public void testFoo() {",
            "    assertThat(new Object()).whenComparedUsing(COMPARATOR);",
            "  }",
            "}");

    assertAbout(javaSource())
        .that(file)
        .failsToCompile()
        .withErrorContaining(
            "java.util.Comparator<java.lang.String> cannot be converted to java.util.Comparator<? super java.lang.Object>")
        .in(file)
        .onLine(12);
  }

  @Test
  public void incompatibleTypeDoesntCompile() {
    JavaFileObject file =
        JavaFileObjects.forSourceLines(
            "test.MyTest",
            "package test;",
            "import static com.google.common.truth.Truth.assertThat;",
            "import java.util.Comparator;",
            "class MyTest {",
            "  private static final Comparator<String> COMPARATOR = new Comparator<String>() {",
            "    @Override",
            "    public int compare(String s1, String s2) {",
            "      return 0;",
            "    }",
            "  };",
            "  public void testFoo() {",
            "    assertThat(\"\").whenComparedUsing(COMPARATOR).isGreaterThan(6);",
            "  }",
            "}");

    assertAbout(javaSource())
        .that(file)
        .failsToCompile()
        .withErrorContaining(
            "int cannot be converted to java.lang.String")
        .in(file)
        .onLine(12);
  }

  private static class IncomparableType {
    private final String name;

    public IncomparableType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }
}
