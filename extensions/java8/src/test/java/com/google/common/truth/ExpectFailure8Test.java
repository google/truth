/*
 * Copyright (c) 2017 Google, Inc.
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

import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.ExpectFailure.expectFailureAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.truth.ExpectFailure.SimpleSubjectBuilderCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link ExpectFailure}'s Java 8 support. */
@RunWith(JUnit4.class)
public final class ExpectFailure8Test {

  @Test
  public void testExpectFailure() throws Exception {
    AssertionError failure1 = expectFailure(whenTesting -> whenTesting.that(4).isEqualTo(5));
    assertThat(failure1).hasMessageThat().contains("<4> is equal to <5>");

    // verify multiple independent failures can be caught in the same test
    AssertionError failure2 = expectFailure(whenTesting -> whenTesting.that(5).isEqualTo(4));
    assertThat(failure2).hasMessageThat().contains("<5> is equal to <4>");
  }

  @Test
  public void testExpectFailureAbout() {
    AssertionError expected =
        expectFailureAbout(
            STRINGS,
            (SimpleSubjectBuilderCallback<StringSubject, String>)
                whenTesting -> whenTesting.that("foo").contains("bar"));
    assertThat(expected).hasMessageThat().contains("<\"foo\"> contains <\"bar\">");
  }

  private static final SubjectFactory<StringSubject, String> STRINGS =
      new SubjectFactory<StringSubject, String>() {
        @Override
        public StringSubject getSubject(FailureStrategy fs, String target) {
          return new StringSubject(fs, target);
        }
      };
}
