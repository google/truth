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

import static org.truth0.Truth.ASSERT;
import static org.truth0.subjects.IntegerSubject.INTEGER;
import static org.truth0.subjects.StringSubject.STRING;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Collection Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IterationOverSubjectTest {

  @Test public void collectionItemsAreGreaterThan() {
    Iterable<Long> data = iterable(2l, 5l, 9l);
    ASSERT.in(data).thatEach(INTEGER).isNotEqualTo(4);
    try {
      ASSERT.in(data).thatEach(INTEGER).isNotEqualTo(9);
      ASSERT.fail("Expected assertion to fail on element 3.");
    } catch (AssertionError e) {
      if (e.getMessage().startsWith("Expected assertion to fail")) {
        throw e;
      }
    }
  }

  @Test public void collectionItemsContainText() {
    Iterable<String> data = iterable("AfooB", "BfooA");
    ASSERT.in(data).thatEach(STRING).contains("foo");
    try {
      ASSERT.in(data).thatEach(STRING).isEqualTo("AfooB");
      ASSERT.fail("Expected assertion to fail on element 2.");
    } catch (AssertionError e) {
      if (e.getMessage().startsWith("Expected assertion to fail")) {
        throw e;
      }
    }
  }

  /**
   * Helper that returns a general Collection rather than a List.
   * This ensures that we test CollectionSubject (rather than ListSubject).
   */
  private static <T> Iterable<T> iterable(T... items) {
    return Arrays.asList(items);
  }
}
