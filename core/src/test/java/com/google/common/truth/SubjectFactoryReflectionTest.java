/*
 * Copyright (c) 2011 Google, Inc.
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
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for SubjectFactory generic type erasure workaround.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class SubjectFactoryReflectionTest {
  @Test
  public void genericErasureWorkaround() {
    SubjectFactory<StringSubject, String> factory =
        new SubjectFactory<StringSubject, String>() {
          @Override
          public StringSubject getSubject(FailureStrategy fs, String target) {
            return new StringSubject(fs, target);
          }
        };
    assertThat(factory.getSubjectClass()).isEqualTo(StringSubject.class);
  }

  @Test
  public void parameterizedSubject_shouldNotFail_Bug17658655() {
    try {
      new SubjectFactory<ParameterizedSubject<String>, String>() {
        @Override
        public ParameterizedSubject<String> getSubject(FailureStrategy fs, String target) {
          return new ParameterizedSubject<String>(fs, target);
        }
      };
    } catch (ClassCastException e) {
      fail("Should not throw with parameterized subjects");
    }
  }

  private static class ParameterizedSubject<T> extends Subject<ParameterizedSubject<T>, T> {
    ParameterizedSubject(FailureStrategy fs, T subject) {
      super(fs, subject);
    }
  }
}
