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
import static com.google.common.truth.Truth.assert_;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tests for Collection Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IterableTest {

  @Test public void hasSize() {
    assertThat(ImmutableList.of(1, 2, 3)).hasSize(3);
  }

  @Test public void hasSizeZero() {
    assertThat(ImmutableList.of()).hasSize(0);
  }

  @Test public void hasSizeFails() {
    try {
      assertThat(ImmutableList.of(1, 2, 3)).hasSize(4);
      fail();
    } catch (AssertionError expected) {
      assertThat(expected.getMessage())
          .isEqualTo("Not true that <[1, 2, 3]> has a size of <4>. It is <3>");
    }
  }

  @Test public void hasSizeNegative() {
    try {
      assertThat(ImmutableList.of(1, 2, 3)).hasSize(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test public void iteratesOver() {
    assertThat(iterable(1, 2, 3)).iteratesAs(1, 2, 3);
  }

  @Test public void iteratesOverAsList() {
    assertThat(iterable(1, 2, 3)).iteratesAs(asList(1, 2, 3));
  }

  @Test public void iteratesOverSequence_Legacy() {
    assertThat(iterable(1, 2, 3)).iteratesOverSequence(1, 2, 3);
  }

  @Test public void iteratesOverEmpty() {
    assertThat(iterable()).iteratesAs();
  }

  @Test public void iteratesOverWithOrderingFailure() {
    try {
      assertThat(iterable(1, 2, 3)).iteratesAs(2, 3, 1);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> iterates through <[2, 3, 1]>");
    }
  }

  @Test public void iteratesOverWithTooManyItemsFailure() {
    try {
      assertThat(iterable(1, 2, 3)).iteratesAs(1, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .isEqualTo("Not true that <[1, 2, 3]> iterates through <[1, 2, 3, 4]>");
    }
  }

  @Test public void iteratesOverWithTooFewItemsFailure() {
    try {
      assertThat(iterable(1, 2, 3)).iteratesAs(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <[1, 2, 3]> iterates through <[1, 2]>");
    }
  }

  @Test public void iteratesOverWithIncompatibleItems() {
    try {
      assertThat(iterable(1, 2, 3)).iteratesAs(1, 2, "a");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> iterates through <[1, 2, a]>");
    }
  }

  @SuppressWarnings("unchecked")
  @Test public void iteratesOverAsListWithIncompatibleItems() {
    try {
      assertThat(iterable(1, 2, 3)).iteratesAs(asList(1, 2, "a"));
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> iterates through <[1, 2, a]>");
    }
  }

  @Test public void iterableHasItem() {
    assertThat(iterable(1, 2, 3)).contains(1);
  }

  @Test public void iterableHasItemWithNull() {
    assertThat(iterable(1, null, 3)).contains(null);
  }

  @Test public void iterableHasItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("<[1, 2, 3]> should have contained <5>");
    }
  }

  @Test public void namedIterableHasItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).named("numbers").contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("\"numbers\" <[1, 2, 3]> should have contained <5>");
    }
  }

  @Test public void failureMessageIterableHasItemFailure() {
    try {
      assert_().withFailureMessage("custom msg").that(iterable(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("custom msg: <[1, 2, 3]> should have contained <5>");
    }
  }

  @Test public void iterableDoesntHaveItem() {
    assertThat(iterable(1, null, 3)).doesNotContain(5);
  }

  @Test public void iterableDoesntHaveItemWithNull() {
    assertThat(iterable(1, 2, 3)).doesNotContain(null);
  }

  @Test public void iterableDoesntHaveItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).doesNotContain(2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("<[1, 2, 3]> should not have contained <2>");
    }
  }

  @Test public void doesNotContainDuplicates() {
    assertThat(iterable(1, 2, 3)).containsNoDuplicates();
  }

  @Test public void doesNotContainDuplicatesMixedTypes() {
    List<Object> values = Lists.newArrayList();
    values.add(1);
    values.add(2);
    values.add(2L);
    values.add(3);
    assertThat(values).containsNoDuplicates();
  }

  @Test public void doesNotContainDuplicatesFailure() {
    try {
      assertThat(iterable(1, 2, 2, 3)).containsNoDuplicates();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .isEqualTo("<[1, 2, 2, 3]> has the following duplicates: <[2 x 2]>");
    }
  }

  @Test public void iterableHasAnyOf() {
    assertThat(iterable(1, 2, 3)).containsAnyOf(1, 5);
  }

  @Test public void iterableHasAnyOfWithNull() {
    assertThat(iterable(1, null, 3)).containsAnyOf(null, 5);
  }

  @Test public void iterableHasAnyOfWithNullInThirdAndFinalPosition() {
    assertThat(iterable(1, null, 3)).containsAnyOf(4, 5, (Integer) null);
  }

  @Test public void iterableHasAnyOfFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsAnyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <[1, 2, 3]> contains any of <[5, 6, 0]>");
    }
  }

  @Test public void iterableHasAllOfWithMany() {
    assertThat(iterable(1, 2, 3)).containsAllOf(1, 2);
  }

  @Test public void iterableHasAllOfWithDuplicates() {
    assertThat(iterable(1, 2, 2, 2, 3)).containsAllOf(2, 2);
  }

  @Test public void iterableHasAllOfWithNull() {
    assertThat(iterable(1, null, 3)).containsAllOf(3, (Integer) null);
  }

  @Test public void iterableHasAllOfWithNullAtThirdAndFinalPosition() {
    assertThat(iterable(1, null, 3)).containsAllOf(1, 3, null);
  }

  @Test public void iterableHasAllOfFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsAllOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains all of <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test public void iterableHasAllOfWithDuplicatesFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsAllOf(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains all of <[1, 2, 2, 2, 3, 4]>. "
          + "It is missing <[2 [2 copies], 4]>");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test public void iterableHasAllOfWithDuplicateMissingElements() {
    try {
      assertThat(iterable(1, 2)).containsAllOf(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2]> contains all of <[4, 4, 4]>. It is missing <[4 [3 copies]]>");
    }
  }

  @Test public void iterableHasAllOfWithNullFailure() {
    try {
      assertThat(iterable(1, null, 3)).containsAllOf(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, null, 3]> contains all of <[1, null, null, 3]>. "
          + "It is missing <[null]>");
    }
  }

  @Test public void iterableHasAllOfInOrder() {
    assertThat(iterable(3, 2, 5)).containsAllOf(3, 2, 5).inOrder();
  }

  @Test public void iterableHasAllOfInOrderWithGaps() {
    assertThat(iterable(3, 2, 5)).containsAllOf(3, 5).inOrder();
    assertThat(iterable(3, 2, 2, 4, 5)).containsAllOf(3, 2, 2, 5).inOrder();
    assertThat(iterable(3, 1, 4, 1, 5)).containsAllOf(3, 1, 5).inOrder();
  }

  @Test public void iterableHasAllOfInOrderWithNull() {
    assertThat(iterable(3, null, 5)).containsAllOf(3, null, 5).inOrder();
    assertThat(iterable(3, null, 7, 5)).containsAllOf(3, null, 5).inOrder();
  }

  @Test public void iterableHasAllOfInOrderWithFailure() {
    try {
      assertThat(iterable(1, null, 3)).containsAllOf(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .isEqualTo("Not true that <[1, null, 3]> contains all elements in order <[null, 1, 3]>");
    }
  }

  @Test public void iterableHasAllOfInOrderWithOneShotIterable() {
    final Iterator<Object> iterator = iterable(2, 1, null, 4, "a", 3, "b").iterator();
    Iterable<Object> iterable = new Iterable<Object>() {
      @Override public Iterator<Object> iterator() {
        return iterator;
      }
    };

    assertThat(iterable).containsAllOf(1, null, 3).inOrder();
  }

  @Test public void iterableHasAllOfInOrderWithOneShotIterableWrongOrder() {
    final Iterator<Object> iterator = iterable(2, 1, null, 4, "a", 3, "b").iterator();
    Iterable<Object> iterable = new Iterable<Object>() {
      @Override public Iterator<Object> iterator() {
        return iterator;
      }

      @Override public String toString() {
        return "BadIterable";
      }
    };

    try {
      assertThat(iterable).containsAllOf(1, 3, null).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <BadIterable> contains all elements in order <[1, 3, null]>");
    }
  }

  @Test public void iterableHasNoneOf() {
    assertThat(iterable(1, 2, 3)).containsNoneOf(4, 5, 6);
  }

  @Test public void iterableHasNoneOfFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsNoneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains none of <[1, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void iterableHasNoneOfFailureWithDuplicateInSubject() {
    try {
      assertThat(iterable(1, 2, 2, 3)).containsNoneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 2, 3]> contains none of <[1, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void iterableHasNoneOfFailureWithDuplicateInExpected() {
    try {
      assertThat(iterable(1, 2, 3)).containsNoneOf(1, 2, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains none of <[1, 2, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test public void arrayContainsExactly() {
    ImmutableList<String> iterable = ImmutableList.of("a", "b");
    String[] array = { "a", "b" };
    assertThat(iterable).containsExactly(array);
  }

  @Test public void iterableHasExactlyWithMany() {
    assertThat(iterable(1, 2, 3)).containsExactly(1, 2, 3);
  }

  @Test public void iterableHasExactlyOutOfOrder() {
    assertThat(iterable(1, 2, 3, 4)).containsExactly(3, 1, 4, 2);
  }

  @Test public void iterableHasExactlyWithDuplicates() {
    assertThat(iterable(1, 2, 2, 2, 3)).containsExactly(1, 2, 2, 2, 3);
  }

  @Test public void iterableHasExactlyWithDuplicatesOutOfOrder() {
    assertThat(iterable(1, 2, 2, 2, 3)).containsExactly(2, 1, 2, 3, 2);
  }

  @Test public void iterableHasExactlyWithNull() {
    assertThat(iterable(1, null, 3)).containsExactly(1, null, 3);
  }

  @Test public void iterableHasExactlyWithNullOutOfOrder() {
    assertThat(iterable(1, null, 3)).containsExactly(1, 3, (Integer) null);
  }

  @Test public void iterableHasExactlyMissingItemFailure() {
    try {
      assertThat(iterable(1, 2)).containsExactly(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2]> contains exactly <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test public void iterableHasExactlyUnexpectedItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsExactly(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains exactly <[1, 2]>. It has unexpected items <[3]>");
    }
  }

  @Test public void iterableHasExactlyWithDuplicatesNotEnoughItemsFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsExactly(1, 2, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains exactly <[1, 2, 2, 2, 3]>. "
          + "It is missing <[2 [2 copies]]>");
    }
  }

  @Test public void iterableHasExactlyWithDuplicatesMissingItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsExactly(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 3]> contains exactly <[1, 2, 2, 2, 3, 4]>. "
          + "It is missing <[2 [2 copies], 4]>");
    }
  }

  @Test public void iterableHasExactlyWithDuplicatesUnexpectedItemFailure() {
    try {
      assertThat(iterable(1, 2, 2, 2, 2, 3)).containsExactly(1, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, 2, 2, 2, 2, 3]> contains exactly <[1, 2, 2, 3]>. "
          + "It has unexpected items <[2 [2 copies]]>");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test public void iterableHasExactlyWithDuplicateMissingElements() {
    try {
      assertThat(iterable()).containsExactly(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[]> contains exactly <[4, 4, 4]>. It is missing <[4 [3 copies]]>");
    }
  }

  @Test public void iterableHasExactlyWithNullFailure() {
    try {
      assertThat(iterable(1, null, 3)).containsExactly(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, null, 3]> contains exactly <[1, null, null, 3]>. "
          + "It is missing <[null]>");
    }
  }

  @Test public void iterableHasExactlyInOrder() {
    assertThat(iterable(3, 2, 5)).containsExactly(3, 2, 5).inOrder();
  }

  @Test public void iterableHasExactlyInOrderWithNull() {
    assertThat(iterable(3, null, 5)).containsExactly(3, null, 5).inOrder();
  }

  @Test public void iterableHasExactlyInOrderWithFailure() {
    try {
      assertThat(iterable(1, null, 3)).containsExactly(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <[1, null, 3]> contains only these elements in order <[null, 1, 3]>");
    }
  }

  @Test public void iterableHasExactlyInOrderWithOneShotIterable() {
    final Iterator<Object> iterator = iterable(1, null, 3).iterator();
    Iterable<Object> iterable = new Iterable<Object>() {
      @Override public Iterator<Object> iterator() {
        return iterator;
      }
    };

    assertThat(iterable).containsExactly(1, null, 3).inOrder();
  }

  @Test public void iterableHasExactlyInOrderWithOneShotIterableWrongOrder() {
    final Iterator<Object> iterator = iterable(1, null, 3).iterator();
    Iterable<Object> iterable = new Iterable<Object>() {
      @Override public Iterator<Object> iterator() {
        return iterator;
      }

      @Override public String toString() {
        return "BadIterable";
      }
    };

    try {
      assertThat(iterable).containsExactly(1, 3, null).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo(
          "Not true that <BadIterable> contains only these elements in order <[1, 3, null]>");
    }
  }

  @Test public void iterableIsEmpty() {
    assertThat(iterable()).isEmpty();
  }

  @Test public void iterableIsEmptyWithFailure() {
    try {
      assertThat(iterable(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <[1, null, 3]> is empty");
    }
  }

  @Test public void iterableIsNotEmpty() {
    assertThat(iterable("foo")).isNotEmpty();
  }

  @Test public void iterableIsNotEmptyWithFailure() {
    try {
      assertThat(iterable()).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).isEqualTo("Not true that <[]> is not empty");
    }
  }

  /**
   * Helper that returns a general Iterable rather than a List.
   * This ensures that we test IterableSubject (rather than ListSubject).
   */
  private static Iterable<Object> iterable(Object... items) {
    return Arrays.asList(items);
  }

}
