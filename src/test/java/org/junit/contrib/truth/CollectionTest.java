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
package org.junit.contrib.truth;

import static org.junit.Assert.fail;
import static org.junit.contrib.truth.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collection;

/**
 * Tests for Collection Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class CollectionTest {

  @Test public void listContains() {
    ASSERT.that(collection(1, 2, 3)).contains(1);
  }

  @Test public void listContainsWithChaining() {
    ASSERT.that(collection(1, 2, 3)).contains(1).and().contains(2);
  }

  @Test public void listContainsWithNull() {
    ASSERT.that(collection(1, null, 3)).contains(null);
  }

  @Test public void listContainsWith2KindsOfChaining() {
    Collection<Integer> foo = collection(1, 2, 3);
    Collection<Integer> bar = foo;
    ASSERT.that(foo).is(bar).and().contains(1).and().contains(2);
  }

  @Test public void listContainsFailureWithChaining() {
    try {
      ASSERT.that(collection(1, 2, 3)).contains(1).and().contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {}
  }

  @Test public void listContainsFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void listContainsAnyOf() {
    ASSERT.that(collection(1, 2, 3)).containsAnyOf(1, 5);
  }

  @Test public void listContainsAnyOfWithNull() {
    ASSERT.that(collection(1, null, 3)).containsAnyOf(null, 5);
  }

  @Test public void listContainsAnyOfFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).containsAnyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void listContainsAllOf() {
    ASSERT.that(collection(1, 2, 3)).containsAllOf(1, 2);
  }

  @Test public void listContainsAllOfWithDuplicates() {
    ASSERT.that(collection(1, 2, 2, 2, 3)).containsAllOf(2, 2);
  }

  @Test public void listContainsAllOfWithNull() {
    ASSERT.that(collection(1, null, 3)).containsAllOf(3, null);
  }

  @Test public void listContainsAllOfFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).containsAllOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that").and().contains("<4>");
    }
  }

  @Test public void listContainsAllOfWithDuplicatesFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).containsAllOf(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().contains("<3 copies of 2>")
          .and().contains("<4>");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test public void listContainsAllOfWithDuplicateMissingElements() {
    try {
      ASSERT.that(collection(1, 2)).containsAllOf(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().endsWith("contains <3 copies of 4>");
    }
  }

  @Test public void listContainsAllOfWithNullFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).containsAllOf(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().contains("<2 copies of null>");
    }
  }

  /**
   * Helper that returns a general Collection rather than a List.
   * This ensures that we test CollectionSubject (rather than ListSubject).
   */
  private static <T> Collection<T> collection(T... items) {
    return Arrays.asList(items);
  }
}
