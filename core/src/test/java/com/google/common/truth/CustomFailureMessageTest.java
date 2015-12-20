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
      assertWithMessage("Invalid month").that(13).named("Septober").isIn(Range.closed(1, 12));
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
      assert_().withFailureMessage("Invalid month").that(13).isIn(Range.closed(1, 12));
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
      assert_().withFailureMessage("This is a custom message").that(false).isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "This is a custom message: The subject was expected to be true, but was false");
    }
  }

  @Test
  public void countPlaceholders() {
    assertThat(TestVerb.countPlaceholders(null)).isEqualTo(0);
    assertThat(TestVerb.countPlaceholders("")).isEqualTo(0);
    assertThat(TestVerb.countPlaceholders("%s")).isEqualTo(1);
    assertThat(TestVerb.countPlaceholders("%s%s")).isEqualTo(2);
    assertThat(TestVerb.countPlaceholders("%s%%s")).isEqualTo(2);
    assertThat(TestVerb.countPlaceholders("hello")).isEqualTo(0);
    assertThat(TestVerb.countPlaceholders("%shello")).isEqualTo(1);
    assertThat(TestVerb.countPlaceholders("hello%s")).isEqualTo(1);
    assertThat(TestVerb.countPlaceholders("hel%slo")).isEqualTo(1);
    assertThat(TestVerb.countPlaceholders("hel%%slo")).isEqualTo(1);
    assertThat(TestVerb.countPlaceholders("hel%s%slo")).isEqualTo(2);
    assertThat(TestVerb.countPlaceholders("%shel%s%slo")).isEqualTo(3);
    assertThat(TestVerb.countPlaceholders("hel%s%slo%s")).isEqualTo(3);
  }

  @Test
  public void assertWithMessageIsPrepended_withPlaceholders() {
    try {
      assertWithMessage("Invalid %s", "month").that(13).isIn(Range.closed(1, 12));
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Invalid month: Not true that <13> is in <[1‥12]>");
    }
  }

  @Test
  public void assertWithMessageIsPrependedWithNamed_withPlaceholders() {
    try {
      assertWithMessage("Invalid %snth", "mo").that(13).named("Septober").isIn(Range.closed(1, 12));
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage("Invalid month: Not true that Septober (<13>) is in <[1‥12]>");
    }
  }

  @Test
  public void assertWithMessageThat_withPlaceholders() {
    try {
      assertWithMessage("This is a %s %s", "custom", "message").that(false).isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "This is a custom message: The subject was expected to be true, but was false");
    }
  }

  @Test
  public void customMessageIsPrepended_withPlaceholders() {
    try {
      assert_().withFailureMessage("In%slid%snth", "va", " mo").that(13).isIn(Range.closed(1, 12));
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Invalid month: Not true that <13> is in <[1‥12]>");
    }
  }

  @Test
  public void customMessageIsPrependedWithNamed_withPlaceholders() {
    try {
      assert_()
          .withFailureMessage("Inval%sd mon%s", 'i', "th")
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
  public void customMessage_withPlaceholders() {
    try {
      assert_().withFailureMessage("This is a %s %s", "custom", "message").that(false).isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "This is a custom message: The subject was expected to be true, but was false");
    }
  }

  @Test
  public void extraPlaceholderThrowsIae() {
    try {
      assert_().withFailureMessage("This is a %s %s", "custom").that(true).isTrue();
      fail("Should have thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void missingPlaceholderThrowsIae() {
    try {
      assert_().withFailureMessage("This is a %s", "custom", "message").that(true).isTrue();
      fail("Should have thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void noPlaceholdersWithArgsThrowsIae() {
    try {
      assert_().withFailureMessage("This is a custom message", "bad arg").that(true).isTrue();
      fail("Should have thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void placeholderWithoutArgsThrows() {
    try {
      assertWithMessage("This is a %s").that(true).isTrue();
      fail("Should have thrown");
    } catch (IllegalArgumentException expected) {
    }

    try {
      assert_().withFailureMessage("This is a %s").that(true).isTrue();
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
    assert_()
        .withFailureMessage("Evaluating this will blow up: %s", toStringThrows)
        .that(true)
        .isTrue();
  }
}
