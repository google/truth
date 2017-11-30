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
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link ComparableSubject} calls that should fail to compile.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class ComparableSubjectCompileTest {
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
