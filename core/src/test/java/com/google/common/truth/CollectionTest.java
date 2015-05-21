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
import java.util.Collection;

/**
 * Tests for Collection Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
// TODO(kak): Move these all to IterableTest
@RunWith(JUnit4.class)
public class CollectionTest {
  @Test
  public void collectionContains() {
    assertThat(collection(1, 2, 3)).contains(1);
  }

  @Test
  public void collectionContainsWithNull() {
    assertThat(collection(1, null, 3)).contains(null);
  }

  @Test
  public void collectionContainsFailure() {
    try {
      assertThat(collection(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<[1, 2, 3]> should have contained <5>");
    }
  }

  @Test
  public void collectionContainsAnyOf() {
    assertThat(collection(1, 2, 3)).containsAnyOf(1, 5);
  }

  @Test
  public void collectionContainsAnyOfWithNull() {
    assertThat(collection(1, null, 3)).containsAnyOf(null, 5);
  }

  @Test
  public void collectionContainsAnyOfWithNullInThirdAndFinalPosition() {
    assertThat(collection(1, null, 3)).containsAnyOf(4, 5, (Integer) null);
  }

  @Test
  public void collectionContainsAnyOfFailure() {
    try {
      assertThat(collection(1, 2, 3)).containsAnyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
    }
  }

  @Test
  public void collectionContainsAllOfWithMany() {
    assertThat(collection(1, 2, 3)).containsAllOf(1, 2);
  }

  @Test
  public void collectionContainsAllOfWithDuplicates() {
    assertThat(collection(1, 2, 2, 2, 3)).containsAllOf(2, 2);
  }

  @Test
  public void collectionContainsAllOfWithNull() {
    assertThat(collection(1, null, 3)).containsAllOf(3, null);
  }

  @Test
  public void collectionContainsAllOfWithNullAtThirdAndFinalPosition() {
    assertThat(collection(1, null, 3)).containsAllOf(1, 3, (Integer) null);
  }

  @Test
  public void collectionContainsAllOfFailure() {
    try {
      assertThat(collection(1, 2, 3)).containsAllOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <[1, 2, 3]> contains all of <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test
  public void collectionContainsAllOfWithDuplicatesFailure() {
    try {
      assertThat(collection(1, 2, 3)).containsAllOf(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains all of");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("2 [2 copies], 4");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void collectionContainsAllOfWithDuplicateMissingElements() {
    try {
      assertThat(collection(1, 2)).containsAllOf(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("4 [3 copies]");
    }
  }

  @Test
  public void collectionContainsAllOfWithNullFailure() {
    try {
      assertThat(collection(1, null, 3)).containsAllOf(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("null");
    }
  }

  @Test
  public void collectionContainsAllOfInOrder() {
    assertThat(collection(3, 2, 5)).containsAllOf(3, 2, 5).inOrder();
  }

  @Test
  public void collectionContainsAllOfInOrderWithNull() {
    assertThat(collection(3, null, 5)).containsAllOf(3, null, 5).inOrder();
  }

  @Test
  public void collectionContainsAllOfInOrderWithFailure() {
    try {
      assertThat(collection(1, null, 3)).containsAllOf(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains all elements in order");
    }
  }

  @Test
  public void collectionContainsNoneOf() {
    assertThat(collection(1, 2, 3)).containsNoneOf(4, 5, 6);
  }

  @Test
  public void collectionContainsNoneOfFailure() {
    try {
      assertThat(collection(1, 2, 3)).containsNoneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains none of <[1, 2, 4]>. " + "It contains <[1, 2]>");
    }
  }

  @Test
  public void collectionContainsNoneOfFailureWithDuplicateInSubject() {
    try {
      assertThat(collection(1, 2, 2, 3)).containsNoneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 2, 3]> contains none of <[1, 2, 4]>. "
                  + "It contains <[1, 2]>");
    }
  }

  @Test
  public void collectionContainsNoneOfFailureWithDuplicateInExpected() {
    try {
      assertThat(collection(1, 2, 3)).containsNoneOf(1, 2, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains none of <[1, 2, 2, 4]>. "
                  + "It contains <[1, 2]>");
    }
  }

  @Test
  public void collectionContainsExactlyWithMany() {
    assertThat(collection(1, 2, 3)).containsExactly(1, 2, 3);
  }

  @Test
  public void collectionContainsExactlyOutOfOrder() {
    assertThat(collection(1, 2, 3, 4)).containsExactly(3, 1, 4, 2);
  }

  @Test
  public void collectionContainsExactlyWithDuplicates() {
    assertThat(collection(1, 2, 2, 2, 3)).containsExactly(1, 2, 2, 2, 3);
  }

  @Test
  public void collectionContainsExactlyWithDuplicatesOutOfOrder() {
    assertThat(collection(1, 2, 2, 2, 3)).containsExactly(2, 1, 2, 3, 2);
  }

  @Test
  public void collectionContainsExactlyWithNull() {
    assertThat(collection(1, null, 3)).containsExactly(1, null, 3);
  }

  @Test
  public void collectionContainsExactlyWithNullOutOfOrder() {
    assertThat(collection(1, null, 3)).containsExactly(1, 3, (Integer) null);
  }

  @Test
  public void collectionContainsExactlyMissingItemFailure() {
    try {
      assertThat(collection(1, 2)).containsExactly(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("4");
    }
  }

  @Test
  public void collectionContainsExactlyUnexpectedItemFailure() {
    try {
      assertThat(collection(1, 2, 3)).containsExactly(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("has unexpected items");
      assertThat(e.getMessage()).contains("3");
    }
  }

  @Test
  public void collectionContainsExactlyWithDuplicatesNotEnoughItemsFailure() {
    try {
      assertThat(collection(1, 2, 3)).containsExactly(1, 2, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains exactly <[1, 2, 2, 2, 3]>. "
                  + "It is missing <[2 [2 copies]]>");
    }
  }

  @Test
  public void collectionContainsExactlyWithDuplicatesMissingItemFailure() {
    try {
      assertThat(collection(1, 2, 3)).containsExactly(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains exactly <[1, 2, 2, 2, 3, 4]>. "
                  + "It is missing <[2 [2 copies], 4]>");
    }
  }

  @Test
  public void collectionContainsExactlyWithDuplicatesUnexpectedItemFailure() {
    try {
      assertThat(collection(1, 2, 2, 2, 2, 3)).containsExactly(1, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 2, 2, 2, 3]> contains exactly <[1, 2, 2, 3]>. "
                  + "It has unexpected items <[2 [2 copies]]>");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void collectionContainsExactlyWithDuplicateMissingElements() {
    try {
      assertThat(collection()).containsExactly(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("4 [3 copies]");
    }
  }

  @Test
  public void collectionContainsExactlyWithNullFailure() {
    try {
      assertThat(collection(1, null, 3)).containsExactly(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is missing");
      assertThat(e.getMessage()).contains("null");
    }
  }

  @Test
  public void collectionContainsExactlyInOrder() {
    assertThat(collection(3, 2, 5)).containsExactly(3, 2, 5).inOrder();
  }

  @Test
  public void collectionContainsExactlyInOrderWithNull() {
    assertThat(collection(3, null, 5)).containsExactly(3, null, 5).inOrder();
  }

  @Test
  public void collectionContainsExactlyInOrderWithFailure() {
    try {
      assertThat(collection(1, null, 3)).containsExactly(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("contains only these elements in order");
    }
  }

  @Test
  public void collectionIsEmpty() {
    assertThat(collection()).isEmpty();
  }

  @Test
  public void collectionIsEmptyWithFailure() {
    try {
      assertThat(collection(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Not true that");
      assertThat(e.getMessage()).contains("is empty");
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
