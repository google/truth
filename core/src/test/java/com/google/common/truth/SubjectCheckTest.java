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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for String Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class SubjectCheckTest {

  private static class MySubject extends Subject<MySubject, String> {
    private MySubject(FailureStrategy failureStrategy, String subject) {
      super(failureStrategy, subject);
    }

    public void isPropagatingMessage() {
      check().that("foo").isEqualTo("bar");
    }
  }

  @Test
  public void checkPreservesOverriddenMessage() {
    try{
      assertWithMessage("blah").about(new SubjectFactory<MySubject, String>() {
        @Override
        public MySubject getSubject(FailureStrategy fs, String that) {
          return new MySubject(fs, that);
        }
      }).that("").isPropagatingMessage();
    } catch (AssertionError expected) {
      assertThat(expected.getMessage()).startsWith("blah: ");
    }
  }

  @Test
  public void throwOnDoubleCallToWithFailureMessage() {
    try{
      assertWithMessage("blah").withFailureMessage("foo");
    } catch (IllegalStateException expected) {
      assertThat(expected)
          .hasMessage("Overriding failure message has already been set for this call-chain.");
    }
  }
}
