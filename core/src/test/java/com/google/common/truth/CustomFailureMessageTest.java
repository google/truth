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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests (and effectively sample code) for custom error message for checks.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class CustomFailureMessageTest extends BaseSubjectTestCase {

  @Test
  public void assertWithMessageThat() {
    expectFailure.whenTesting().withMessage("This is a custom message").that(false).isTrue();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .startsWith("This is a custom message\n");
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
  public void assertWithMessageThat_withPlaceholders() {
    expectFailure
        .whenTesting()
        .withMessage("This is a %s %s", "custom", "message")
        .that(false)
        .isTrue();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .startsWith("This is a custom message\n");
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
