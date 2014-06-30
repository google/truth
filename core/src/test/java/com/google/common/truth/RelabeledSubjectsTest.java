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

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

/**
 * Tests (and effectively sample code) for the Expect verb (implemented as a
 * rule)
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class RelabeledSubjectsTest {

  @Test public void relabeledBooleans() {
    try {
      ASSERT.that(false).named("Foo").isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).is("\"Foo\" was expected to be true, but was false");
    }
  }

  @Test public void relabeledObject() {
    try {
      ASSERT.that(new Object()).named("Foo").isA(Integer.class);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .contains("Not true that \"Foo\" is an instance of <java.lang.Integer>");
      ASSERT.that(expected.getMessage())
          .contains("It is an instance of <java.lang.Object>");
    }
  }

  @Test public void relabelledCollections() {
    try {
      ASSERT.that(Arrays.asList("a", "b", "c")).named("crazy list").has().allOf("c", "d");
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that \"crazy list\" has all of <[c, d]>. It is missing <[d]>");
    }
  }

  @Test public void relabelledPrimitiveFloatArrays() {
    float[] expected = { 1.3f, 1.0f };
    float[] actual = { 1.3f, 1.0f };
    try {
      ASSERT.that(actual).named("crazy list").isNotEqualTo(expected, 0.0000001f);
      fail("Should have thrown");
    } catch (AssertionError error) {
      ASSERT.that(error.getMessage()).is("\"crazy list\" unexpectedly equal to [1.3, 1.0]");
    }
  }
}
