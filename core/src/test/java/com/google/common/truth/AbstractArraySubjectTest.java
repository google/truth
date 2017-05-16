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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@code AbstractArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class AbstractArraySubjectTest {
  @Test
  public void foo() {
    String[] strings = {"Foo", "Bar"};
    TestableStringArraySubject subject =
        new TestableStringArraySubject(Truth.THROW_ASSERTION_ERROR, strings);
    assertThat(subject.actualAsString()).isEqualTo("<(String[]) [Foo, Bar]>");
  }

  @Test
  public void canBeUsedInSubjectFactories() {
    // This will fail to compile if the super-type of AbstractArraySubject
    // is incompatible with the generic bounds of SubjectFactory.
    class TestSubjectFactory extends SubjectFactory<TestableStringArraySubject, String[]> {
      @Override
      public TestableStringArraySubject getSubject(FailureStrategy fs, String[] that) {
        return new TestableStringArraySubject(fs, that);
      }
    }

    String[] strings = {"foo", "bar"};
    assertAbout(new TestSubjectFactory()).that(strings).hasLength(2);
  }

  static class TestableStringArraySubject
      extends AbstractArraySubject<TestableStringArraySubject, String[]> {
    public TestableStringArraySubject(FailureStrategy failureStrategy, String[] subject) {
      super(failureStrategy, subject);
    }

    @Override
    protected String underlyingType() {
      return "String";
    }

    @Override
    protected List<?> listRepresentation() {
      return Arrays.asList(actual());
    }
  }
}
