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
package org.truth0.subjects;

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
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
      ASSERT.that(e.getMessage()).contains("2 [2 copies], 4");
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
      ASSERT.that(e.getMessage()).contains("4 [3 copies]");
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

  @Test public void collectionHasNoneOf() {
    ASSERT.that(collection(1, 2, 3)).has().noneOf(4, 5, 6);
  }

  @Test public void collectionHasNoneOfFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().noneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .isEqualTo("Not true that <[1, 2, 3]> has none of <[1, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasNoneOfFailureWithDuplicateInSubject() {
    try {
      ASSERT.that(collection(1, 2, 2, 3)).has().noneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .isEqualTo("Not true that <[1, 2, 2, 3]> has none of <[1, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasNoneOfFailureWithDuplicateInExpected() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().noneOf(1, 2, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .isEqualTo("Not true that <[1, 2, 3]> has none of <[1, 2, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasExactlyWithMany() {
    ASSERT.that(collection(1, 2, 3)).has().exactly(1, 2, 3);
  }

  @Test public void collectionHasExactlyOutOfOrder() {
    ASSERT.that(collection(1, 2, 3, 4)).has().exactly(3, 1, 4, 2);
  }

  @Test public void collectionHasExactlyWithDuplicates() {
    ASSERT.that(collection(1, 2, 2, 2, 3)).has().exactly(1, 2, 2, 2, 3);
  }

  @Test public void collectionHasExactlyWithDuplicatesOutOfOrder() {
    ASSERT.that(collection(1, 2, 2, 2, 3)).has().exactly(2, 1, 2, 3, 2);
  }

  @Test public void collectionHasExactlyWithNull() {
    ASSERT.that(collection(1, null, 3)).has().exactly(1, null, 3);
  }

  @Test public void collectionHasExactlyWithNullOutOfOrder() {
    ASSERT.that(collection(1, null, 3)).has().exactly(1, 3, null);
  }

  @Test public void collectionHasExactlyMissingItemFailure() {
    try {
      ASSERT.that(collection(1, 2)).has().exactly(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("4");
    }
  }

  @Test public void collectionHasExactlyUnexpectedItemFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().exactly(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has unexpected items");
      ASSERT.that(e.getMessage()).contains("3");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesNotEnoughItemsFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().exactly(1, 2, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has exactly");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("2 [2 copies]");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesMissingItemFailure() {
    try {
      ASSERT.that(collection(1, 2, 3)).has().exactly(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has exactly");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("2 [2 copies], 4");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesUnexpectedItemFailure() {
    try {
      ASSERT.that(collection(1, 2, 2, 2, 2, 3)).has().exactly(1, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has exactly");
      ASSERT.that(e.getMessage()).contains("has unexpected items");
      ASSERT.that(e.getMessage()).contains("2 [2 copies]");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test public void collectionHasExactlyWithDuplicateMissingElements() {
    try {
      ASSERT.that(collection()).has().exactly(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("4 [3 copies]");
    }
  }

  @Test public void collectionHasExactlyWithNullFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).has().exactly(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("null");
    }
  }

  @Test public void collectionHasExactlyInOrder() {
    ASSERT.that(collection(3, 2, 5)).has().exactly(3, 2, 5).inOrder();
  }

  @Test public void collectionHasExactlyInOrderWithNull() {
    ASSERT.that(collection(3, null, 5)).has().exactly(3, null, 5).inOrder();
  }

  @Test public void collectionHasExactlyInOrderWithFailure() {
    try {
      ASSERT.that(collection(1, null, 3)).has().exactly(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has exactly in order");
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

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithTooManyItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = ASSERT.that((Collection<Integer>)list).has().exactly(1, null, 3);
    list.add(6);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithTooFewItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = ASSERT.that((Collection<Integer>)list).has().exactly(1, null, 3);
    list.remove(1);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithNoItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = ASSERT.that((Collection<Integer>)list).has().exactly(1, null, 3);
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
      ASSERT.that(e.getMessage()).contains("in order");
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
