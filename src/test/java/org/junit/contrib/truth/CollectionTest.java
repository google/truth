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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.contrib.truth.subjects.Ordered;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Collection Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class CollectionTest {

  @Test public void collectionContains() {
    ASSERT.that(collection(1, 2, 3)).contains(1);
  }

  @Test public void collectionContainsWithChaining() {
    ASSERT.that(collection(1, 2, 3)).contains(1).and().contains(2);
  }

  @Test public void collectionContainsWithNull() {
    ASSERT.that(collection(1, null, 3)).contains(null);
  }

  @Test public void collectionContainsWith2KindsOfChaining() {
    Collection<Integer> foo = collection(1, 2, 3);
    Collection<Integer> bar = foo;
    ASSERT.that(foo).is(bar).and().contains(1).and().contains(2);
  }

  @Test public void collectionContainsFailureWithChaining() {
    try {
      ASSERT.that(collection(1, 2, 3)).contains(1).and().contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {}
  }

  @Test public void collectionContainsFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionContainsAnyOf() {
    ASSERT.that(collection(1, 2, 3)).containsAnyOf(1, 5);
  }

  @Test public void collectionContainsAnyOfWithNull() {
    ASSERT.that(collection(1, null, 3)).containsAnyOf(null, 5);
  }

  @Test public void collectionContainsAnyOfFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).containsAnyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionContainsWithMany() {
    ASSERT.that(collection(1, 2, 3)).contains(1, 2);
  }

  @Test public void collectionContainsWithDuplicates() {
    ASSERT.that(collection(1, 2, 2, 2, 3)).contains(2, 2);
  }

  @Test public void collectionContainsWithManyWithNull() {
    ASSERT.that(collection(1, null, 3)).contains(3, null);
  }

  @Test public void collectionContainsWithManyFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).contains(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that").and().contains("<4>");
    }
  }

  @Test public void collectionContainsWithDuplicatesFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).contains(1, 2, 2, 2, 3, 4);
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
  @Test public void collectionContainsAllOfWithDuplicateMissingElements() {
    try {
      ASSERT.that(collection(1, 2)).contains(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().endsWith("contains <3 copies of 4>");
    }
  }

  @Test public void collectionContainsWithNullFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).contains(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().contains("<2 copies of null>");
    }
  }

  @Test public void collectionContainsInOrder() {
    ASSERT.that(collection(3, 2, 5)).contains(3, 2, 5).inOrder();
  }

  @Test public void collectionContainsInOrderWithNull() {
    ASSERT.that(collection(3, null, 5)).contains(3, null, 5).inOrder();
  }

  @Test public void collectionContainsInOrderWithFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).contains(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().contains("iterates through");
    }
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionContainsInOrderWithHackedFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>();
    list.addAll(collection(1, null, 3));
    Ordered<?> o = ASSERT.that(list).contains(1, null, 3);
    list.add(6);
    try {
      o.inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().contains("iterates through");
    }
    list.remove(1);
    try {
      o.inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().contains("iterates through");
    }
    list.clear();
    try {
      o.inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().contains("iterates through");
    }
  }

  @Test public void emptyCollection() {
    ASSERT.that(collection()).isEmpty();
  }

  @Test public void emptyCollectionWithFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that")
          .and().contains("is empty");
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
