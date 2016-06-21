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

import static com.google.common.truth.Truth.assertThat;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Throwable} subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class ThrowableSubjectTest {
  @Test
  public void hasMessage() {
    NullPointerException npe = new NullPointerException("message");
    assertThat(npe).hasMessage("message");
  }

  @Test
  public void hasMessage_null() {
    assertThat(new NullPointerException()).hasMessage(null);
    assertThat(new NullPointerException(null)).hasMessage(null);
  }

  @Test
  public void hasMessage_failure() {
    NullPointerException subject = new NullPointerException("message");
    try {
      assertThat(subject).hasMessage("foobar");
      throw new Error("Expected to fail.");
    } catch (ComparisonFailure expected) {
      assertThat(expected.getMessage())
          .isEqualTo(
              "<java.lang.NullPointerException: message> does not have message <foobar> "
                  + "expected:<[foobar]> but was:<[message]>");
      assertThat(expected.getCause()).isEqualTo(subject);
    }
  }

  @Test
  public void hasMessage_MessageHasNullMessage_failure() {
    try {
      assertThat(new NullPointerException("message")).hasMessage(null);
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage())
          .isEqualTo("Not true that <java.lang.NullPointerException: message> has message <null>");
    }
  }

  @Test
  public void hasMessage_NullMessageHasMessage_failure() {
    try {
      assertThat(new NullPointerException(null)).hasMessage("message");
      throw new Error("Expected to fail.");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage())
          .isEqualTo("Not true that <java.lang.NullPointerException> has message <message>");
    }
  }

  @Test
  public void inheritedMethodChainsSubject() {
    NullPointerException expected = new NullPointerException("expected");
    NullPointerException actual = new NullPointerException("actual");
    try {
      assertThat(actual).isEqualTo(expected);
      throw new Error("Expected to fail.");
    } catch (AssertionError thrown) {
      assertThat(thrown.getCause()).isEqualTo(actual);
      /*
       * TODO(cpovirk): consider a special case for isEqualTo and isSameAs that adds |expected| as a
       * suppressed exception
       */
    }
  }
}
