/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.junit.contrib.truth.codegen;

import static org.junit.contrib.truth.Truth.ASSERT;

import org.junit.Test;
import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.subjects.Subject;
import org.junit.contrib.truth.subjects.SubjectFactory;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for the IteratingWrapperClassBuilder
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IteratingWrapperClassBuilderTest {

  private static final String BAR_EXPECTED =
      "package org.junit.contrib.truth.codegen;\n" +
      "\n" +
      "import org.junit.contrib.truth.FailureStrategy;\n" +
      "import org.junit.contrib.truth.subjects.SubjectFactory;\n" +
      "\n" +
      "public class BarSubjectIteratingWrapper extends BarSubject {\n" +
      "\n" +
      "    private final SubjectFactory subjectFactory;\n" +
      "    private final Iterable<String> data;\n" +
      "\n" +
      "  public BarSubjectIteratingWrapper(\n" +
      "      FailureStrategy failureStrategy,\n" +
      "      SubjectFactory<?, ?> subjectFactory,\n" +
      "      Iterable<String> data\n" +
      "  ) {\n" +
      "    super(failureStrategy, null);\n" +
      "    this.subjectFactory = subjectFactory;\n" +
      "    this.data = data;\n" +
      "  }\n" +
      "\n" +
      "  public org.junit.contrib.truth.subjects.Subject.And startsWith(java.lang.String) {\n" +
      "    for (java.lang.String item : data) {\n" +
      "      org.junit.contrib.truth.codegen.BarSubject subject = (org.junit.contrib.truth.codegen.BarSubject)subjectFactory.getSubject(failureStrategy);\n" +
      "    }\n" +
      "  }\n" +
      "}\n";

  @Test public void testSubjectWrapperGeneration_PlainClass() {
    IteratingWrapperClassBuilder builder = new IteratingWrapperClassBuilder(BarSubject.BAR);
    String code = builder.build().toString();
    System.out.println("Code:\n" + code);
    ASSERT.that(code).is(BAR_EXPECTED);
  }

  private static final String FOO_EXPECTED =
      "package org.junit.contrib.truth.codegen;\n" +
      "\n" +
      "import org.junit.contrib.truth.FailureStrategy;\n" +
      "import org.junit.contrib.truth.subjects.SubjectFactory;\n" +
      "\n" +
      "public class FooSubjectIteratingWrapper extends FooSubject {\n" +
      "\n" +
      "    private final SubjectFactory subjectFactory;\n" +
      "    private final Iterable<String> data;\n" +
      "\n" +
      "  public FooSubjectIteratingWrapper(\n" +
      "      FailureStrategy failureStrategy,\n" +
      "      SubjectFactory<?, ?> subjectFactory,\n" +
      "      Iterable<String> data\n" +
      "  ) {\n" +
      "    super(failureStrategy, null);\n" +
      "    this.subjectFactory = subjectFactory;\n" +
      "    this.data = data;\n" +
      "  }\n" +
      "\n" +
      "  public org.junit.contrib.truth.subjects.Subject.And endsWith(java.lang.String) {\n" +
      "    for (java.lang.String item : data) {\n" +
      "      org.junit.contrib.truth.codegen.IteratingWrapperClassBuilderTest.FooSubject subject = (org.junit.contrib.truth.codegen.IteratingWrapperClassBuilderTest.FooSubject)subjectFactory.getSubject(failureStrategy);\n" +
      "    }\n" +
      "  }\n" +
      "}\n";

  @Test public void testSubjectWrapperGeneration_InnerClass() {
    IteratingWrapperClassBuilder builder = new IteratingWrapperClassBuilder(FooSubject.FOO);
    String code = builder.build().toString();
    System.out.println("Code:\n" + code);
    ASSERT.that(code).is(FOO_EXPECTED);
      }

  public static class FooSubject extends Subject<FooSubject, String> {

    public static final SubjectFactory<FooSubject, String> FOO =
        new SubjectFactory<FooSubject, String>() {
          @Override public FooSubject getSubject(FailureStrategy fs, String target) {
            return new FooSubject(fs, target);
          }
        };

    public FooSubject(FailureStrategy failureStrategy, String subject) {
      super(failureStrategy, subject);
    }

    public And<FooSubject> endsWith(String suffix) {
      if (getSubject().endsWith(suffix)) {
        fail("matches", getSubject(), suffix);
      }
      return nextChain();
    }

  }
}
