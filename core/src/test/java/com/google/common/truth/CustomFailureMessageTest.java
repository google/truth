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
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth.assert_;
import static org.junit.Assert.fail;

import com.google.common.collect.Range;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests (and effectively sample code) for custom error message for propositions.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class CustomFailureMessageTest {
  @Test
  public void assertWithMessageIsPrepended() {
    try {
      assertWithMessage("Invalid month").that(13).isIn(Range.closed(1, 12));
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Invalid month: Not true that <13> is in <[1‥12]>");
    }
  }

  @Test
  public void assertWithMessageIsPrependedWithNamed() {
    try {
      assertWithMessage("Invalid month")
          .that(13)
          .named("Septober")
          .isIn(Range.closed(1, 12));
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage("Invalid month: Not true that Septober (<13>) is in <[1‥12]>");
    }
  }

  @Test
  public void assertWithMessageThat() {
    try {
      assertWithMessage("This is a custom message").that(false).isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "This is a custom message: The subject was expected to be true, but was false");
    }
  }

  @Test
  public void customMessageIsPrepended() {
    try {
      assert_()
          .withFailureMessage("Invalid month")
          .that(13)
          .isIn(Range.closed(1, 12));
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Invalid month: Not true that <13> is in <[1‥12]>");
    }
  }

  @Test
  public void customMessageIsPrependedWithNamed() {
    try {
      assert_()
          .withFailureMessage("Invalid month")
          .that(13)
          .named("Septober")
          .isIn(Range.closed(1, 12));
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage("Invalid month: Not true that Septober (<13>) is in <[1‥12]>");
    }
  }

  @Test
  public void customMessage() {
    try {
      assert_()
          .withFailureMessage("This is a custom message")
          .that(false)
          .isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "This is a custom message: The subject was expected to be true, but was false");
    }
  }
}
