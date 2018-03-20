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
 * Tests (and effectively sample code) for custom error message for checks.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class CustomFailureMessageTest {

  @Test
  public void assertWithMessageIsPrepended() {
    Range<Integer> range = Range.closed(1, 12);
    try {
      assertWithMessage("Invalid month").that(13).isIn(range);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Invalid month\nNot true that <13> is in <" + range + ">");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void assertWithMessageIsPrependedWithNamed() {
    Range<Integer> range = Range.closed(1, 12);
    try {
      assertWithMessage("Invalid month").that(13).named("Septober").isIn(range);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Invalid month\nNot true that Septober (<13>) is in <" + range + ">");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void assertWithMessageThat() {
    try {
      assertWithMessage("This is a custom message").that(false).isTrue();
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "This is a custom message\nThe subject was expected to be true, but was false");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void customMessageIsPrepended() {
    Range<Integer> range = Range.closed(1, 12);
    try {
      assert_().withMessage("Invalid month").that(13).isIn(range);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Invalid month\nNot true that <13> is in <" + range + ">");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void customMessageIsPrependedWithNamed() {
    Range<Integer> range = Range.closed(1, 12);
    try {
      assert_().withMessage("Invalid month").that(13).named("Septober").isIn(range);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Invalid month\nNot true that Septober (<13>) is in <" + range + ">");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void customMessage() {
    try {
      assert_().withMessage("This is a custom message").that(false).isTrue();
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "This is a custom message\nThe subject was expected to be true, but was false");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void countPlaceholders() {
    assertThat(LazyMessage.countPlaceholders(null)).isEqualTo(0);
    assertThat(LazyMessage.countPlaceholders("")).isEqualTo(0);
    assertThat(LazyMessage.countPlaceholders("%s")).isEqualTo(1);
    assertThat(LazyMessage.countPlaceholders("%s%s")).isEqualTo(2);
    assertThat(LazyMessage.countPlaceholders("%s%%s")).isEqualTo(2);
    assertThat(LazyMessage.countPlaceholders("hello")).isEqualTo(0);
    assertThat(LazyMessage.countPlaceholders("%shello")).isEqualTo(1);
    assertThat(LazyMessage.countPlaceholders("hello%s")).isEqualTo(1);
    assertThat(LazyMessage.countPlaceholders("hel%slo")).isEqualTo(1);
    assertThat(LazyMessage.countPlaceholders("hel%%slo")).isEqualTo(1);
    assertThat(LazyMessage.countPlaceholders("hel%s%slo")).isEqualTo(2);
    assertThat(LazyMessage.countPlaceholders("%shel%s%slo")).isEqualTo(3);
    assertThat(LazyMessage.countPlaceholders("hel%s%slo%s")).isEqualTo(3);
  }

  @Test
  public void assertWithMessageIsPrepended_withPlaceholders() {
    Range<Integer> range = Range.closed(1, 12);
    try {
      assertWithMessage("Invalid %s", "month").that(13).isIn(range);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Invalid month\nNot true that <13> is in <" + range + ">");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void assertWithMessageIsPrependedWithNamed_withPlaceholders() {
    Range<Integer> range = Range.closed(1, 12);
    try {
      assertWithMessage("Invalid %snth", "mo").that(13).named("Septober").isIn(range);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Invalid month\nNot true that Septober (<13>) is in <" + range + ">");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void assertWithMessageThat_withPlaceholders() {
    try {
      assertWithMessage("This is a %s %s", "custom", "message").that(false).isTrue();
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "This is a custom message\nThe subject was expected to be true, but was false");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void customMessageIsPrepended_withPlaceholders() {
    Range<Integer> range = Range.closed(1, 12);
    try {
      assert_().withMessage("In%slid%snth", "va", " mo").that(13).isIn(range);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Invalid month\nNot true that <13> is in <" + range + ">");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void customMessageIsPrependedWithNamed_withPlaceholders() {
    Range<Integer> range = Range.closed(1, 12);
    try {
      assert_().withMessage("Inval%sd mon%s", 'i', "th").that(13).named("Septober").isIn(range);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Invalid month\nNot true that Septober (<13>) is in <" + range + ">");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void customMessage_withPlaceholders() {
    try {
      assert_().withMessage("This is a %s %s", "custom", "message").that(false).isTrue();
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo(
              "This is a custom message\nThe subject was expected to be true, but was false");
      return;
    }
    fail("Should have thrown");
  }

  @Test
  public void extraPlaceholderThrowsIae() {
    try {
      assert_().withMessage("This is a %s %s", "custom").that(true).isTrue();
      fail("Should have thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void missingPlaceholderThrowsIae() {
    try {
      assert_().withMessage("This is a %s", "custom", "message").that(true).isTrue();
      fail("Should have thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void noPlaceholdersWithArgsThrowsIae() {
    try {
      assert_().withMessage("This is a custom message", "bad arg").that(true).isTrue();
      fail("Should have thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void placeholdersArentEagerlyEvaluated() {
    Object toStringThrows =
        new Object() {
          @Override
          public String toString() {
            throw new RuntimeException("Don't call me!");
          }
        };
    assertWithMessage("Evaluating this will blow up: %s", toStringThrows).that(true).isTrue();
    assert_().withMessage("Evaluating this will blow up: %s", toStringThrows).that(true).isTrue();
  }
}
