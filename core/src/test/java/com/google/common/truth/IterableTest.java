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

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Tests for Collection Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IterableTest {

  @Test public void iteratesOver() {
    ASSERT.that(iterable(1, 2, 3)).iteratesAs(1, 2, 3);
  }

  @Test public void iteratesOverAsList() {
    ASSERT.that(iterable(1, 2, 3)).iteratesAs(asList(1, 2, 3));
  }

  @Test public void iteratesOverLegacy() {
    ASSERT.that(iterable(1, 2, 3)).iteratesOverSequence(1, 2, 3);
  }

  @Test public void iteratesOverEmpty() {
    ASSERT.that(iterable()).iteratesAs();
  }

  @Test public void iteratesOverWithOrderingFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).iteratesAs(2, 3, 1);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iteratesOverWithTooManyItemsFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).iteratesAs(1, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iteratesOverWithTooFewItemsFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).iteratesAs(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iteratesOverWithIncompatibleItems() {
    try {
      ASSERT.that(iterable(1, 2, 3)).iteratesAs(1, 2, "a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @SuppressWarnings("unchecked")
  @Test public void iteratesOverAsListWithIncompatibleItems() {
    try {
      ASSERT.that(iterable(1, 2, 3)).iteratesAs(asList(1, 2, "a"));
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iterableIsEmpty() {
    ASSERT.that(iterable()).isEmpty();
  }

  @Test public void iterableIsEmptyWithFailure() {
    try {
      ASSERT.that(iterable(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is empty");
    }
  }

  @Test public void iterableIsNotEmpty() {
    ASSERT.that(iterable("foo")).isNotEmpty();
  }

  @Test public void iterableIsNotEmptyWithFailure() {
    try {
      ASSERT.that(iterable()).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is not empty");
    }
  }

  @Test public void collectionHasItem() {
    ASSERT.that(iterable(1, 2, 3)).contains(1);
  }

  @Test public void collectionHasItemWithNull() {
    ASSERT.that(iterable(1, null, 3)).contains(null);
  }

  @Test public void collectionHasItemFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionDoesntHaveItem() {
    ASSERT.that(iterable(1, null, 3)).doesNotContain(5);
  }

  @Test public void collectionDoesntHaveItemWithNull() {
    ASSERT.that(iterable(1, 2, 3)).doesNotContain(null);
  }

  @Test public void collectionDoesntHaveItemFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).doesNotContain(2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionHasAnyOf() {
    ASSERT.that(iterable(1, 2, 3)).containsAnyOf(1, 5);
  }

  @Test public void collectionHasAnyOfWithNull() {
    ASSERT.that(iterable(1, null, 3)).containsAnyOf(null, 5);
  }

  @Test public void collectionHasAnyOfWithNullInThirdAndFinalPosition() {
    ASSERT.that(iterable(1, null, 3)).containsAnyOf(4, 5, (Integer) null);
  }

  @Test public void collectionHasAnyOfFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).containsAnyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionHasAllOfWithMany() {
    ASSERT.that(iterable(1, 2, 3)).containsAllOf(1, 2);
  }

  @Test public void collectionHasAllOfWithDuplicates() {
    ASSERT.that(iterable(1, 2, 2, 2, 3)).containsAllOf(2, 2);
  }

  @Test public void collectionHasAllOfWithNull() {
    ASSERT.that(iterable(1, null, 3)).containsAllOf(3, (Integer) null);
  }

  @Test public void collectionHasAllOfWithNullAtThirdAndFinalPosition() {
    ASSERT.that(iterable(1, null, 3)).containsAllOf(1, 3, null);
  }

  @Test public void collectionHasAllOfFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).containsAllOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("4");
    }
  }

  @Test public void collectionHasAllOfWithDuplicatesFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).containsAllOf(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("contains all of");
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
      ASSERT.that(iterable(1, 2)).containsAllOf(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("4 [3 copies]");
    }
  }

  @Test public void collectionHasAllOfWithNullFailure() {
    try {
      ASSERT.that(iterable(1, null, 3)).containsAllOf(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("null");
    }
  }

  @Test public void collectionHasAllOfInOrder() {
    ASSERT.that(iterable(3, 2, 5)).containsAllOf(3, 2, 5).inOrder();
  }

  @Test public void collectionHasAllOfInOrderWithNull() {
    ASSERT.that(iterable(3, null, 5)).containsAllOf(3, null, 5).inOrder();
  }

  @Test public void collectionHasAllOfInOrderWithFailure() {
    try {
      ASSERT.that(iterable(1, null, 3)).containsAllOf(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("contains all elements in order");
    }
  }

  @Test public void collectionHasNoneOf() {
    ASSERT.that(iterable(1, 2, 3)).containsNoneOf(4, 5, 6);
  }

  @Test public void collectionHasNoneOfFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).containsNoneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains none of <[1, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasNoneOfFailureWithDuplicateInSubject() {
    try {
      ASSERT.that(iterable(1, 2, 2, 3)).containsNoneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 2, 3]> contains none of <[1, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasNoneOfFailureWithDuplicateInExpected() {
    try {
      ASSERT.that(iterable(1, 2, 3)).containsNoneOf(1, 2, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains none of <[1, 2, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasExactlyWithMany() {
    ASSERT.that(iterable(1, 2, 3)).containsOnlyElements(1, 2, 3);
  }

  @Test public void collectionHasExactlyOutOfOrder() {
    ASSERT.that(iterable(1, 2, 3, 4)).containsOnlyElements(3, 1, 4, 2);
  }

  @Test public void collectionHasExactlyWithDuplicates() {
    ASSERT.that(iterable(1, 2, 2, 2, 3)).containsOnlyElements(1, 2, 2, 2, 3);
  }

  @Test public void collectionHasExactlyWithDuplicatesOutOfOrder() {
    ASSERT.that(iterable(1, 2, 2, 2, 3)).containsOnlyElements(2, 1, 2, 3, 2);
  }

  @Test public void collectionHasExactlyWithNull() {
    ASSERT.that(iterable(1, null, 3)).containsOnlyElements(1, null, 3);
  }

  @Test public void collectionHasExactlyWithNullOutOfOrder() {
    ASSERT.that(iterable(1, null, 3)).containsOnlyElements(1, 3, (Integer) null);
  }

  @Test public void collectionHasExactlyMissingItemFailure() {
    try {
      ASSERT.that(iterable(1, 2)).containsOnlyElements(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("4");
    }
  }

  @Test public void collectionHasExactlyUnexpectedItemFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).containsOnlyElements(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("has unexpected items");
      ASSERT.that(e.getMessage()).contains("3");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesNotEnoughItemsFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).containsOnlyElements(1, 2, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("contains only");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("2 [2 copies]");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesMissingItemFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).containsOnlyElements(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("contains only");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("2 [2 copies], 4");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesUnexpectedItemFailure() {
    try {
      ASSERT.that(iterable(1, 2, 2, 2, 2, 3)).containsOnlyElements(1, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("contains only");
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
      ASSERT.that(iterable()).containsOnlyElements(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("4 [3 copies]");
    }
  }

  @Test public void collectionHasExactlyWithNullFailure() {
    try {
      ASSERT.that(iterable(1, null, 3)).containsOnlyElements(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is missing");
      ASSERT.that(e.getMessage()).contains("null");
    }
  }

  @Test public void collectionHasExactlyInOrder() {
    ASSERT.that(iterable(3, 2, 5)).containsOnlyElements(3, 2, 5).inOrder();
  }

  @Test public void collectionHasExactlyInOrderWithNull() {
    ASSERT.that(iterable(3, null, 5)).containsOnlyElements(3, null, 5).inOrder();
  }

  @Test public void collectionHasExactlyInOrderWithFailure() {
    try {
      ASSERT.that(iterable(1, null, 3)).containsOnlyElements(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("contains only these elements in order");
    }
  }

  @Test public void collectionIsEmpty() {
    ASSERT.that(iterable()).isEmpty();
  }

  @Test public void collectionIsEmptyWithFailure() {
    try {
      ASSERT.that(iterable(1, null, 3)).isEmpty();
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
    ArrayList<Integer> list = new ArrayList<Integer>(asList(1, null, 3));
    Ordered o = ASSERT.that((Iterable<Integer>) list).containsAllOf(1, null, 3);
    list.add(6);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasAllOfInOrderHackedWithTooFewItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(asList(1, null, 3));
    Ordered o = ASSERT.that((Iterable<Integer>) list).containsAllOf(1, null, 3);
    list.remove(1);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasAllOfInOrderHackedWithNoItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(asList(1, null, 3));
    Ordered o = ASSERT.that((Iterable<Integer>) list).containsAllOf(1, null, 3);
    list.clear();
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithTooManyItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(asList(1, null, 3));
    Ordered o = ASSERT.that((Iterable<Integer>) list).containsOnlyElements(1, null, 3);
    list.add(6);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithTooFewItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(asList(1, null, 3));
    Ordered o = ASSERT.that((Iterable<Integer>) list).containsOnlyElements(1, null, 3);
    list.remove(1);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithNoItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(asList(1, null, 3));
    Ordered o = ASSERT.that((Iterable<Integer>) list).containsOnlyElements(1, null, 3);
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
  private static <T> Iterable<T> iterable(T... items) {
    return Arrays.asList(items);
  }

}
