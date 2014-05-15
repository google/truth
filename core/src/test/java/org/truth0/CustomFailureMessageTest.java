/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0;

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;
import static org.truth0.subjects.StringSubject.STRING;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

/**
 * Tests (and effectively sample code) for custom error messages for propositions.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class CustomFailureMessageTest {
  @Test public void customMessage() {
    try {
      ASSERT.withFailureMessage("This is a custom message.").that(false).isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).is("This is a custom message.");
    }
  }

  @Test public void customMessageOnDelegate() {
    try {
      ASSERT.withFailureMessage("This is a custom message.")
          .about(STRING).that("foo").isEqualTo("bar");
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).is("This is a custom message.");
    }
  }

  @Test public void customMessageOnForEach() {
    try {
      ASSERT.withFailureMessage("This is a custom message.")
          .in(Arrays.asList("a1", "b1", "c1")).thatEach(STRING).contains("b");
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).is("This is a custom message.");
    }
  }
}
