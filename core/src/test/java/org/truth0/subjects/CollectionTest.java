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
package org.truth0.subjects;

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
public class CollectionTest {

  @Test public void collectionHasItem() {
    ASSERT.that(collection(1, 2, 3)).has().item(1);
  }

  @Test public void collectionHasItemWithNull() {
    ASSERT.that(collection(1, null, 3)).has().item(null);
  }

  @Test public void collectionHasItemFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().item(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionHasAnyOf() {
    ASSERT.that(collection(1, 2, 3)).has().anyOf(1, 5);
  }

  @Test public void collectionHasAnyOfWithNull() {
    ASSERT.that(collection(1, null, 3)).has().anyOf(null, 5);
  }

  @Test public void collectionHasAnyOfWithNullInThirdAndFinalPosition() {
    ASSERT.that(collection(1, null, 3)).has().anyOf(4, 5, null);
  }

  @Test public void collectionHasAnyOfFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().anyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionHasAllOfWithMany() {
    ASSERT.that(collection(1, 2, 3)).has().allOf(1, 2);
  }

  @Test public void collectionHasAllOfWithDuplicates() {
    ASSERT.that(collection(1, 2, 2, 2, 3)).has().allOf(2, 2);
  }

  @Test public void collectionHasAllOfWithNull() {
    ASSERT.that(collection(1, null, 3)).has().allOf(3, null);
  }

  @Test public void collectionHasAllOfWithNullAtThirdAndFinalPosition() {
    ASSERT.that(collection(1, null, 3)).has().allOf(1, 3, null);
  }

  @Test public void collectionHasAllOfFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().allOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("4");
    }
  }

  @Test public void collectionHasAllOfWithDuplicatesFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().allOf(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has all of");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("2 copies of 2, 4");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test public void collectionHasAllOfWithDuplicateMissingElements() {
    try {
      ASSERT.that(collection(1, 2)).has().allOf(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("3 copies of 4");
    }
  }

  @Test public void collectionHasAllOfWithNullFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).has().allOf(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("null");
    }
  }

  @Test public void collectionHasAllOfInOrder() {
    ASSERT.that(collection(3, 2, 5)).has().allOf(3, 2, 5).inOrder();
  }

  @Test public void collectionHasAllOfInOrderWithNull() {
    ASSERT.that(collection(3, null, 5)).has().allOf(3, null, 5).inOrder();
  }

  @Test public void collectionHasAllOfInOrderWithFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).has().allOf(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has all in order");
    }
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasAllOfInOrderHackedWithTooManyItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = ASSERT.that((Collection<Integer>)list).has().allOf(1, null, 3);
    list.add(6);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasAllOfInOrderHackedWithTooFewItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = ASSERT.that((Collection<Integer>)list).has().allOf(1, null, 3);
    list.remove(1);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasAllOfInOrderHackedWithNoItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = ASSERT.that((Collection<Integer>)list).has().allOf(1, null, 3);
    list.clear();
    validateHackedFailure(o);
  }

  /** Factored out failure condition for "hacked" failures of inOrder() */
  private void validateHackedFailure(Ordered ordered) {
    try {
      ordered.inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has all in order");
    }
  }

  @Test public void collectionIsEmpty() {
    ASSERT.that(collection()).isEmpty();
  }

  @Test public void collectionIsEmptyWithFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is empty");
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
