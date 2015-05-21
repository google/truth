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
  @Test
  public void namedIncludesActualStringValue() {
    try {
      assertThat("kurt kluever").named("rad dude").startsWith("frazzle");
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage()).contains("\"kurt kluever\"");
      assertThat(expected.getMessage()).contains("rad dude");
      assertThat(expected.getMessage()).contains("frazzle");
    }
  }

  @Test
  public void namedIncludesActualIntegerValue() {
    try {
      assertThat(13).named("Septober").isLessThan(12);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage()).contains("13");
      assertThat(expected.getMessage()).contains("12");
      assertThat(expected.getMessage()).contains("Septober");
    }
  }

  @Test
  public void relabeledBooleans() {
    try {
      assertThat(false).named("Foo").isTrue();
      fail("Should have thrown");
    } catch (AssertionError expected) {
      // TODO(kak): This could probably be simplified + shortened a bit...
      assertThat(expected).hasMessage("Foo (<false>) was expected to be true, but was false");
    }
  }

  @Test
  public void relabeledObject() {
    try {
      assertThat("a string").named("Foo").isInstanceOf(Integer.class);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage())
          .contains("Not true that Foo (<\"a string\">) is an instance of <java.lang.Integer>");
      assertThat(expected.getMessage()).contains("It is an instance of <java.lang.String>");
    }
  }

  @Test
  public void relabelledCollections() {
    try {
      assertThat(Arrays.asList("a", "b", "c")).named("crazy list").containsAllOf("c", "d");
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that crazy list (<[a, b, c]>) contains all of <[c, d]>. "
                  + "It is missing <[d]>");
    }
  }

  @Test
  public void relabelledPrimitiveFloatArrays() {
    float[] expected = {1.3f, 1.0f};
    float[] actual = {1.3f, 1.0f};
    try {
      assertThat(actual).named("crazy list").isNotEqualTo(expected, 0.0000001f);
      fail("Should have thrown");
    } catch (AssertionError error) {
      assertThat(error).hasMessage("crazy list unexpectedly equal to [1.3, 1.0]");
    }
  }
}
