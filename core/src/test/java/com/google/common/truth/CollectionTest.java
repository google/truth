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
    assertThat(collection(1, 2, 3)).has().item(1);
  }

  @Test public void collectionHasItemWithNull() {
    assertThat(collection(1, null, 3)).has().item(null);
  }

  @Test public void collectionHasItemFailure() {
    try {
      assertThat(collection(1, 2, 3)).has().item(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionHasAnyOf() {
    assertThat(collection(1, 2, 3)).has().anyOf(1, 5);
  }

  @Test public void collectionHasAnyOfWithNull() {
    assertThat(collection(1, null, 3)).has().anyOf(null, 5);
  }

  @Test public void collectionHasAnyOfWithNullInThirdAndFinalPosition() {
    assertThat(collection(1, null, 3)).has().anyOf(4, 5, (Integer) null);
  }

  @Test public void collectionHasAnyOfFailure() {
    try {
      assertThat(collection(1, 2, 3)).has().anyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void collectionHasAllOfWithMany() {
    assertThat(collection(1, 2, 3)).has().allOf(1, 2);
  }

  @Test public void collectionHasAllOfWithDuplicates() {
    assertThat(collection(1, 2, 2, 2, 3)).has().allOf(2, 2);
  }

  @Test public void collectionHasAllOfWithNull() {
    assertThat(collection(1, null, 3)).has().allOf(3, null);
  }

  @Test public void collectionHasAllOfWithNullAtThirdAndFinalPosition() {
    assertThat(collection(1, null, 3)).has().allOf(1, 3, (Integer) null);
  }

  @Test public void collectionHasAllOfFailure() {
    try {
      assertThat(collection(1, 2, 3)).has().allOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("4");
    }
  }

  @Test public void collectionHasAllOfWithDuplicatesFailure() {
    try {
      assertThat(collection(1, 2, 3)).has().allOf(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains all elements in");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("2 [2 copies], 4");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test public void collectionHasAllOfWithDuplicateMissingElements() {
    try {
      assertThat(collection(1, 2)).has().allOf(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("4 [3 copies]");
    }
  }

  @Test public void collectionHasAllOfWithNullFailure() {
    try {
      assertThat(collection(1, null, 3)).has().allOf(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("null");
    }
  }

  @Test public void collectionHasAllOfInOrder() {
    assertThat(collection(3, 2, 5)).has().allOf(3, 2, 5).inOrder();
  }

  @Test public void collectionHasAllOfInOrderWithNull() {
    assertThat(collection(3, null, 5)).has().allOf(3, null, 5).inOrder();
  }

  @Test public void collectionHasAllOfInOrderWithFailure() {
    try {
      assertThat(collection(1, null, 3)).has().allOf(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains all elements in order");
    }
  }

  @Test public void collectionHasNoneOf() {
    assertThat(collection(1, 2, 3)).has().noneOf(4, 5, 6);
  }

  @Test public void collectionHasNoneOfFailure() {
    try {
      assertThat(collection(1, 2, 3)).has().noneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .isEqualTo("Not true that <[1, 2, 3]> contains no elements in <[1, 2, 4]>. "
              + "It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasNoneOfFailureWithDuplicateInSubject() {
    try {
      assertThat(collection(1, 2, 2, 3)).has().noneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .isEqualTo("Not true that <[1, 2, 2, 3]> contains no elements in <[1, 2, 4]>. "
              + "It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasNoneOfFailureWithDuplicateInExpected() {
    try {
      assertThat(collection(1, 2, 3)).has().noneOf(1, 2, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .isEqualTo("Not true that <[1, 2, 3]> contains no elements in <[1, 2, 2, 4]>. "
              + "It contains <[1, 2]>");
    }
  }

  @Test public void collectionHasExactlyWithMany() {
    assertThat(collection(1, 2, 3)).has().exactly(1, 2, 3);
  }

  @Test public void collectionHasExactlyOutOfOrder() {
    assertThat(collection(1, 2, 3, 4)).has().exactly(3, 1, 4, 2);
  }

  @Test public void collectionHasExactlyWithDuplicates() {
    assertThat(collection(1, 2, 2, 2, 3)).has().exactly(1, 2, 2, 2, 3);
  }

  @Test public void collectionHasExactlyWithDuplicatesOutOfOrder() {
    assertThat(collection(1, 2, 2, 2, 3)).has().exactly(2, 1, 2, 3, 2);
  }

  @Test public void collectionHasExactlyWithNull() {
    assertThat(collection(1, null, 3)).has().exactly(1, null, 3);
  }

  @Test public void collectionHasExactlyWithNullOutOfOrder() {
    assertThat(collection(1, null, 3)).has().exactly(1, 3, (Integer) null);
  }

  @Test public void collectionHasExactlyMissingItemFailure() {
    try {
      assertThat(collection(1, 2)).has().exactly(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("4");
    }
  }

  @Test public void collectionHasExactlyUnexpectedItemFailure() {
    try {
      assertThat(collection(1, 2, 3)).has().exactly(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has unexpected items");
      assertThat(e.getMessage()).contains("3");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesNotEnoughItemsFailure() {
    try {
      assertThat(collection(1, 2, 3)).has().exactly(1, 2, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains only the elements in");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("2 [2 copies]");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesMissingItemFailure() {
    try {
      assertThat(collection(1, 2, 3)).has().exactly(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains only the elements in");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("2 [2 copies], 4");
    }
  }

  @Test public void collectionHasExactlyWithDuplicatesUnexpectedItemFailure() {
    try {
      assertThat(collection(1, 2, 2, 2, 2, 3)).has().exactly(1, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains only the elements in");
      assertThat(e.getMessage()).contains("has unexpected items");
      assertThat(e.getMessage()).contains("2 [2 copies]");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test public void collectionHasExactlyWithDuplicateMissingElements() {
    try {
      assertThat(collection()).has().exactly(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("4 [3 copies]");
    }
  }

  @Test public void collectionHasExactlyWithNullFailure() {
    try {
      assertThat(collection(1, null, 3)).has().exactly(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("null");
    }
  }

  @Test public void collectionHasExactlyInOrder() {
    assertThat(collection(3, 2, 5)).has().exactly(3, 2, 5).inOrder();
  }

  @Test public void collectionHasExactlyInOrderWithNull() {
    assertThat(collection(3, null, 5)).has().exactly(3, null, 5).inOrder();
  }

  @Test public void collectionHasExactlyInOrderWithFailure() {
    try {
      assertThat(collection(1, null, 3)).has().exactly(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains only these elements in order");
    }
  }

  @Test public void collectionIsEmpty() {
    assertThat(collection()).isEmpty();
  }

  @Test public void collectionIsEmptyWithFailure() {
    try {
      assertThat(collection(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is empty");
    }
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasAllOfInOrderHackedWithTooManyItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = assertThat((Collection<Integer>)list).has().allOf(1, null, 3);
    list.add(6);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasAllOfInOrderHackedWithTooFewItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = assertThat((Collection<Integer>)list).has().allOf(1, null, 3);
    list.remove(1);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasAllOfInOrderHackedWithNoItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = assertThat((Collection<Integer>)list).has().allOf(1, null, 3);
    list.clear();
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithTooManyItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = assertThat((Collection<Integer>)list).has().exactly(1, null, 3);
    list.add(6);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithTooFewItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = assertThat((Collection<Integer>)list).has().exactly(1, null, 3);
    list.remove(1);
    validateHackedFailure(o);
  }

  /**
   * This tests the rather unwieldly case where someone alters the
   * collection out from under the Subject before inOrder() is called.
   */
  @Test public void collectionHasExactlyInOrderHackedWithNoItemsFailure() {
    ArrayList<Integer> list = new ArrayList<Integer>(collection(1, null, 3));
    Ordered o = assertThat((Collection<Integer>)list).has().exactly(1, null, 3);
    list.clear();
    validateHackedFailure(o);
  }

  /** Factored out failure condition for "hacked" failures of inOrder() */
  private void validateHackedFailure(Ordered ordered) {
    try {
      ordered.inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("in order");
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
