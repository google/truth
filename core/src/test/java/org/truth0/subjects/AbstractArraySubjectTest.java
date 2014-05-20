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
package org.truth0.subjects;

import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.truth0.FailureStrategy;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@code AbstractArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class AbstractArraySubjectTest {

  @Test public void foo() {
    CapturingFailureStrategy failureStrategy = new CapturingFailureStrategy();
    String[] strings = { "Foo", "Bar" };
    TestableStringArraySubject subject = new TestableStringArraySubject(failureStrategy, strings);
    ASSERT.that(subject.getDisplaySubject()).isEqualTo("<(String[]) [Foo, Bar]>");
  }

  class TestableStringArraySubject extends AbstractArraySubject<String[]> {
    public TestableStringArraySubject(FailureStrategy failureStrategy, String[] subject) {
      super(failureStrategy, subject);
    }
    @Override protected String underlyingType() { return "String"; }
    @Override protected List<?> listRepresentation() { return Arrays.asList(getSubject()); }
    @Override protected String getDisplaySubject() { return super.getDisplaySubject(); }
  }

  class CapturingFailureStrategy extends FailureStrategy {
    @Override public void fail(String message, Throwable cause) { /* noop */ }
  }
}
