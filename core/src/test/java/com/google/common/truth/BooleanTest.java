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
 * Tests for Boolean Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class BooleanTest {
  @Test
  public void isTrue() {
    assertThat(true).isTrue();
  }

  @Test
  public void isTrueFailing() {
    try {
      assertThat(false).isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("The subject was expected to be true, but was false");
    }
  }

  @Test
  public void isFalse() {
    assertThat(false).isFalse();
  }

  @Test
  public void isFalseFailing() {
    try {
      assertThat(true).isFalse();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("The subject was expected to be false, but was true");
    }
  }
}
