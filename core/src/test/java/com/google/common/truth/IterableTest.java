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
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Tests for Collection Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IterableTest {
  @Test
  public void hasSize() {
    assertThat(ImmutableList.of(1, 2, 3)).hasSize(3);
  }

  @Test
  public void hasSizeZero() {
    assertThat(ImmutableList.of()).hasSize(0);
  }

  @Test
  public void hasSizeFails() {
    try {
      assertThat(ImmutableList.of(1, 2, 3)).hasSize(4);
      fail();
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <[1, 2, 3]> has a size of <4>. It is <3>");
    }
  }

  @Test
  public void hasSizeNegative() {
    try {
      assertThat(ImmutableList.of(1, 2, 3)).hasSize(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void iterableContainsItem() {
    assertThat(asList(1, 2, 3)).contains(1);
  }

  @Test
  public void iterableContainsItemWithNull() {
    assertThat(asList(1, null, 3)).contains(null);
  }

  @Test
  public void iterableContainsItemFailure() {
    try {
      assertThat(asList(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<[1, 2, 3]> should have contained <5>");
    }
  }

  @Test
  public void namedIterableContainsItemFailure() {
    try {
      assertThat(asList(1, 2, 3)).named("numbers").contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("numbers (<[1, 2, 3]>) should have contained <5>");
    }
  }

  @Test
  public void failureMessageIterableContainsItemFailure() {
    try {
      assertWithMessage("custom msg").that(asList(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("custom msg: <[1, 2, 3]> should have contained <5>");
    }
  }

  @Test
  public void iterableDoesntHaveItem() {
    assertThat(asList(1, null, 3)).doesNotContain(5);
  }

  @Test
  public void iterableDoesntHaveItemWithNull() {
    assertThat(asList(1, 2, 3)).doesNotContain(null);
  }

  @Test
  public void iterableDoesntHaveItemFailure() {
    try {
      assertThat(asList(1, 2, 3)).doesNotContain(2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<[1, 2, 3]> should not have contained <2>");
    }
  }

  @Test
  public void doesNotContainDuplicates() {
    assertThat(asList(1, 2, 3)).containsNoDuplicates();
  }

  @Test
  public void doesNotContainDuplicatesMixedTypes() {
    assertThat(asList(1, 2, 2L, 3)).containsNoDuplicates();
  }

  @Test
  public void doesNotContainDuplicatesFailure() {
    try {
      assertThat(asList(1, 2, 2, 3)).containsNoDuplicates();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<[1, 2, 2, 3]> has the following duplicates: <[2 x 2]>");
    }
  }

  @Test
  public void iterableContainsAnyOf() {
    assertThat(asList(1, 2, 3)).containsAnyOf(1, 5);
  }

  @Test
  public void iterableContainsAnyOfWithNull() {
    assertThat(asList(1, null, 3)).containsAnyOf(null, 5);
  }

  @Test
  public void iterableContainsAnyOfWithNullInThirdAndFinalPosition() {
    assertThat(asList(1, null, 3)).containsAnyOf(4, 5, (Integer) null);
  }

  @Test
  public void iterableContainsAnyOfFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsAnyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <[1, 2, 3]> contains any of <[5, 6, 0]>");
    }
  }

  @Test
  public void iterableContainsAnyOfWithOneShotIterable() {
    final Iterator<Object> iterator = asList((Object) 2, 1, "b").iterator();
    Iterable<Object> iterable =
        new Iterable<Object>() {
          @Override
          public Iterator<Object> iterator() {
            return iterator;
          }
        };

    assertThat(iterable).containsAnyOf(3, "a", 7, "b", 0);
  }

  @Test
  public void iterableContainsAllOfWithMany() {
    assertThat(asList(1, 2, 3)).containsAllOf(1, 2);
  }

  @Test
  public void iterableContainsAllOfWithDuplicates() {
    assertThat(asList(1, 2, 2, 2, 3)).containsAllOf(2, 2);
  }

  @Test
  public void iterableContainsAllOfWithNull() {
    assertThat(asList(1, null, 3)).containsAllOf(3, (Integer) null);
  }

  @Test
  public void iterableContainsAllOfWithNullAtThirdAndFinalPosition() {
    assertThat(asList(1, null, 3)).containsAllOf(1, 3, null);
  }

  @Test
  public void iterableContainsAllOfFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsAllOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <[1, 2, 3]> contains all of <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test
  public void iterableContainsAllOfWithExtras() {
    try {
      assertThat(asList("y", "x")).containsAllOf("x", "y", "z");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage("Not true that <[y, x]> contains all of <[x, y, z]>. It is missing <[z]>");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void iterableContainsAllOfWithExtraCopiesOfOutOfOrder() {
    try {
      assertThat(asList("y", "x")).containsAllOf("x", "y", "y");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage("Not true that <[y, x]> contains all of <[x, y, y]>. It is missing <[y]>");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void iterableContainsAllOfWithDuplicatesFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsAllOf(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains all of <[1, 2, 2, 2, 3, 4]>. "
                  + "It is missing <[2 [2 copies], 4]>");
    }
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void iterableContainsAllOfWithDuplicateMissingElements() {
    try {
      assertThat(asList(1, 2)).containsAllOf(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2]> contains all of <[4, 4, 4]>. It is missing <[4 [3 copies]]>");
    }
  }

  @Test
  public void iterableContainsAllOfWithNullFailure() {
    try {
      assertThat(asList(1, null, 3)).containsAllOf(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, null, 3]> contains all of <[1, null, null, 3]>. "
                  + "It is missing <[null]>");
    }
  }

  @Test
  public void iterableContainsAllOfInOrder() {
    assertThat(asList(3, 2, 5)).containsAllOf(3, 2, 5).inOrder();
  }

  @Test
  public void iterableContainsAllOfInOrderWithGaps() {
    assertThat(asList(3, 2, 5)).containsAllOf(3, 5).inOrder();
    assertThat(asList(3, 2, 2, 4, 5)).containsAllOf(3, 2, 2, 5).inOrder();
    assertThat(asList(3, 1, 4, 1, 5)).containsAllOf(3, 1, 5).inOrder();
    assertThat(asList("x", "y", "y", "z")).containsAllOf("x", "y", "z").inOrder();
    assertThat(asList("x", "x", "y", "z")).containsAllOf("x", "y", "z").inOrder();
    assertThat(asList("z", "x", "y", "z")).containsAllOf("x", "y", "z").inOrder();
    assertThat(asList("x", "x", "y", "z", "x")).containsAllOf("x", "y", "z", "x").inOrder();
  }

  @Test
  public void iterableContainsAllOfInOrderWithNull() {
    assertThat(asList(3, null, 5)).containsAllOf(3, null, 5).inOrder();
    assertThat(asList(3, null, 7, 5)).containsAllOf(3, null, 5).inOrder();
  }

  @Test
  public void iterableContainsAllOfInOrderWithFailure() {
    try {
      assertThat(asList(1, null, 3)).containsAllOf(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <[1, null, 3]> contains all elements in order <[null, 1, 3]>");
    }
  }

  @Test
  public void iterableContainsAllOfInOrderWithOneShotIterable() {
    final Iterable<Object> iterable = asList(2, 1, null, 4, "a", 3, "b");
    final Iterator<Object> iterator = iterable.iterator();
    Iterable<Object> oneShot =
        new Iterable<Object>() {
          @Override
          public Iterator<Object> iterator() {
            return iterator;
          }

          @Override
          public String toString() {
            return Iterables.toString(iterable);
          }
        };

    assertThat(oneShot).containsAllOf(1, null, 3).inOrder();
  }

  @Test
  public void iterableContainsAllOfInOrderWithOneShotIterableWrongOrder() {
    final Iterator<Object> iterator = asList((Object) 2, 1, null, 4, "a", 3, "b").iterator();
    Iterable<Object> iterable =
        new Iterable<Object>() {
          @Override
          public Iterator<Object> iterator() {
            return iterator;
          }

          @Override
          public String toString() {
            return "BadIterable";
          }
        };

    try {
      assertThat(iterable).containsAllOf(1, 3, null).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <BadIterable> contains all elements in order <[1, 3, null]>");
    }
  }

  @Test
  public void iterableContainsNoneOf() {
    assertThat(asList(1, 2, 3)).containsNoneOf(4, 5, 6);
  }

  @Test
  public void iterableContainsNoneOfFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsNoneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains none of <[1, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test
  public void iterableContainsNoneOfFailureWithDuplicateInSubject() {
    try {
      assertThat(asList(1, 2, 2, 3)).containsNoneOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 2, 3]> contains none of <[1, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test
  public void iterableContainsNoneOfFailureWithDuplicateInExpected() {
    try {
      assertThat(asList(1, 2, 3)).containsNoneOf(1, 2, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains none of <[1, 2, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test
  public void iterableContainsExactlyArray() {
    String[] stringArray = {"a", "b"};
    ImmutableList<String[]> iterable = ImmutableList.of(stringArray);
    // This test fails w/o the explicit cast
    assertThat(iterable).containsExactly((Object) stringArray);
  }

  @Test
  public void arrayContainsExactly() {
    ImmutableList<String> iterable = ImmutableList.of("a", "b");
    String[] array = {"a", "b"};
    assertThat(iterable).containsExactly(array);
  }

  @Test
  public void iterableContainsExactlyWithMany() {
    assertThat(asList(1, 2, 3)).containsExactly(1, 2, 3);
  }

  @Test
  public void iterableContainsExactlyOutOfOrder() {
    assertThat(asList(1, 2, 3, 4)).containsExactly(3, 1, 4, 2);
  }

  @Test
  public void iterableContainsExactlyWithDuplicates() {
    assertThat(asList(1, 2, 2, 2, 3)).containsExactly(1, 2, 2, 2, 3);
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesOutOfOrder() {
    assertThat(asList(1, 2, 2, 2, 3)).containsExactly(2, 1, 2, 3, 2);
  }

  @Test
  public void iterableContainsExactlyWithOnlyNull() {
    Iterable<Object> actual = asList((Object) null);
    assertThat(actual).containsExactly(null);
  }

  @Test
  public void iterableContainsExactlyWithNullSecond() {
    assertThat(asList(1, null)).containsExactly(1, null);
  }

  @Test
  public void iterableContainsExactlyWithNullThird() {
    assertThat(asList(1, 2, null)).containsExactly(1, 2, null);
  }

  @Test
  public void iterableContainsExactlyWithNull() {
    assertThat(asList(1, null, 3)).containsExactly(1, null, 3);
  }

  @Test
  public void iterableContainsExactlyWithNullOutOfOrder() {
    assertThat(asList(1, null, 3)).containsExactly(1, 3, (Integer) null);
  }

  @Test
  public void iterableContainsExactlyWithElementsThatThrowWhenYouCallHashCode() {
    HashCodeThrower one = new HashCodeThrower();
    HashCodeThrower two = new HashCodeThrower();

    assertThat(asList(one, two)).containsExactly(two, one);
    assertThat(asList(one, two)).containsExactly(one, two).inOrder();
    assertThat(asList(one, two)).containsExactlyElementsIn(asList(two, one));
    assertThat(asList(one, two)).containsExactlyElementsIn(asList(one, two)).inOrder();

    try {
      assertThat(asList(one, two)).containsExactly(one);
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessage(
              "Not true that <[HCT, HCT]> contains exactly <[HCT]>. "
                  + "It has unexpected items <[HCT]>");
      return;
    }
    fail();
  }

  private static class HashCodeThrower {
    @Override
    public boolean equals(Object other) {
      return this == other;
    }

    @Override
    public int hashCode() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return "HCT";
    }
  }

  @Test
  public void iterableContainsExactlyElementsInErrorMessageIsOrdered() {
    try {
      assertThat(asList("foo OR bar")).containsExactlyElementsIn(asList("foo", "bar"));
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[foo OR bar]> contains exactly <[foo, bar]>. "
                  + "It is missing <[foo, bar]> and has unexpected items <[foo OR bar]>");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void iterableContainsExactlyMissingItemFailure() {
    try {
      assertThat(asList(1, 2)).containsExactly(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <[1, 2]> contains exactly <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test
  public void iterableContainsExactlyUnexpectedItemFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsExactly(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains exactly <[1, 2]>. It has unexpected items <[3]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesNotEnoughItemsFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsExactly(1, 2, 2, 2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains exactly <[1, 2, 2, 2, 3]>. "
                  + "It is missing <[2 [2 copies]]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesMissingItemFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsExactly(1, 2, 2, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains exactly <[1, 2, 2, 2, 3, 4]>. "
                  + "It is missing <[2 [2 copies], 4]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesUnexpectedItemFailure() {
    try {
      assertThat(asList(1, 2, 2, 2, 2, 3)).containsExactly(1, 2, 2, 3);
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
  public void iterableContainsExactlyWithDuplicateMissingElements() {
    try {
      assertThat(asList()).containsExactly(4, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[]> contains exactly <[4, 4, 4]>. It is missing <[4 [3 copies]]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithNullFailure() {
    try {
      assertThat(asList(1, null, 3)).containsExactly(1, null, null, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, null, 3]> contains exactly <[1, null, null, 3]>. "
                  + "It is missing <[null]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithMissingAndExtraElements() {
    try {
      assertThat(asList(1, 2, 3)).containsExactly(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains exactly <[1, 2, 4]>. "
                  + "It is missing <[4]> and has unexpected items <[3]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithDuplicateMissingAndExtraElements() {
    try {
      assertThat(asList(1, 2, 3, 3)).containsExactly(1, 2, 4, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3, 3]> contains exactly <[1, 2, 4, 4]>. "
                  + "It is missing <[4 [2 copies]]> and has unexpected items <[3 [2 copies]]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithOneIterableGivesWarning() {
    try {
      assertThat(asList(1, 2, 3, 4)).containsExactly(asList(1, 2, 3, 4));
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3, 4]> contains exactly <[[1, 2, 3, 4]]>. "
                  + "It is missing <[[1, 2, 3, 4]]> and has unexpected items <[1, 2, 3, 4]>. "
                  + "Passing an iterable to the varargs method containsExactly(Object...) is "
                  + "often not the correct thing to do. Did you mean to call "
                  + "containsExactlyElementsIn(Iterable) instead?");
    }
  }

  @Test
  public void iterableContainsExactlyElementsInWithOneIterableDoesNotGiveWarning() {
    try {
      assertThat(asList(1, 2, 3, 4)).containsExactlyElementsIn(asList(1, 2, 3));
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3, 4]> contains exactly <[1, 2, 3]>. "
                  + "It has unexpected items <[4]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithTwoIterableDoesNotGivesWarning() {
    try {
      assertThat(asList(1, 2, 3, 4)).containsExactly(asList(1, 2), asList(3, 4));
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3, 4]> contains exactly <[[1, 2], [3, 4]]>. "
                  + "It is missing <[[1, 2], [3, 4]]> and has unexpected items <[1, 2, 3, 4]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithOneNonIterableDoesNotGiveWarning() {
    try {
      assertThat(asList(1, 2, 3, 4)).containsExactly(1);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3, 4]> contains exactly <[1]>. "
                  + "It has unexpected items <[2, 3, 4]>");
    }
  }

  @Test
  public void iterableContainsExactlyInOrder() {
    assertThat(asList(3, 2, 5)).containsExactly(3, 2, 5).inOrder();
  }

  @Test
  public void iterableContainsExactlyInOrderWithNull() {
    assertThat(asList(3, null, 5)).containsExactly(3, null, 5).inOrder();
  }

  @Test
  public void iterableContainsExactlyInOrderWithFailure() {
    try {
      assertThat(asList(1, null, 3)).containsExactly(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, null, 3]> contains only these elements in order <[null, 1, 3]>");
    }
  }

  @Test
  public void iterableContainsExactlyInOrderWithOneShotIterable() {
    final Iterator<Object> iterator = asList((Object) 1, null, 3).iterator();
    Iterable<Object> iterable =
        new Iterable<Object>() {
          @Override
          public Iterator<Object> iterator() {
            return iterator;
          }
        };
    assertThat(iterable).containsExactly(1, null, 3).inOrder();
  }

  @Test
  public void iterableContainsExactlyInOrderWithOneShotIterableWrongOrder() {
    final Iterator<Object> iterator = asList((Object) 1, null, 3).iterator();
    Iterable<Object> iterable =
        new Iterable<Object>() {
          @Override
          public Iterator<Object> iterator() {
            return iterator;
          }

          @Override
          public String toString() {
            return "BadIterable";
          }
        };

    try {
      assertThat(iterable).containsExactly(1, 3, null).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <BadIterable> contains only these elements in order <[1, 3, null]>");
    }
  }

  @Test
  public void iterableIsEmpty() {
    assertThat(asList()).isEmpty();
  }

  @Test
  public void iterableIsEmptyWithFailure() {
    try {
      assertThat(asList(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <[1, null, 3]> is empty");
    }
  }

  @Test
  public void iterableIsNotEmpty() {
    assertThat(asList("foo")).isNotEmpty();
  }

  @Test
  public void iterableIsNotEmptyWithFailure() {
    try {
      assertThat(asList()).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <[]> is not empty");
    }
  }

  @Test
  public void iterableIsStrictlyOrdered() {
    assertThat(asList()).isStrictlyOrdered();
    assertThat(asList(1)).isStrictlyOrdered();
    assertThat(asList(1, 2, 3, 4)).isStrictlyOrdered();
  }

  @Test
  public void isStrictlyOrderedFailure() {
    try {
      assertThat(asList(1, 2, 2, 4)).isStrictlyOrdered();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("is strictly ordered");
      assertThat(e.getMessage()).contains("<2> <2>");
    }
  }

  @Test
  public void isStrictlyOrderedWithNonComparableElementsFailure() {
    try {
      assertThat(asList((Object) 1, "2", 3, "4")).isStrictlyOrdered();
      fail("Should have thrown.");
    } catch (ClassCastException e) {
    }
  }

  @Test
  public void iterableIsOrdered() {
    assertThat(asList()).isOrdered();
    assertThat(asList(1)).isOrdered();
    assertThat(asList(1, 1, 2, 3, 3, 3, 4)).isOrdered();
  }

  @Test
  public void isOrderedFailure() {
    try {
      assertThat(asList(1, 3, 2, 4)).isOrdered();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("is ordered");
      assertThat(e.getMessage()).contains("<3> <2>");
    }
  }

  @Test
  public void isOrderedWithNonComparableElementsFailure() {
    try {
      assertThat(asList((Object) 1, "2", 2, "3")).isOrdered();
      fail("Should have thrown.");
    } catch (ClassCastException e) {
    }
  }

  @Test
  public void iterableIsStrictlyOrderedWithComparator() {
    Iterable<String> emptyStrings = asList();
    assertThat(emptyStrings).isStrictlyOrdered(COMPARE_AS_DECIMAL);
    assertThat(asList("1")).isStrictlyOrdered(COMPARE_AS_DECIMAL);
    // Note: Use "10" and "20" to distinguish numerical and lexicographical ordering.
    assertThat(asList("1", "2", "10", "20")).isStrictlyOrdered(COMPARE_AS_DECIMAL);
  }

  @Test
  public void iterableIsStrictlyOrderedWithComparatorFailure() {
    try {
      assertThat(asList("1", "2", "2", "10")).isStrictlyOrdered(COMPARE_AS_DECIMAL);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("is strictly ordered");
      assertThat(e.getMessage()).contains("<2> <2>");
    }
  }

  @Test
  public void iterableIsOrderedWithComparator() {
    Iterable<String> emptyStrings = asList();
    assertThat(emptyStrings).isOrdered(COMPARE_AS_DECIMAL);
    assertThat(asList("1")).isOrdered(COMPARE_AS_DECIMAL);
    assertThat(asList("1", "1", "2", "10", "10", "10", "20")).isOrdered(COMPARE_AS_DECIMAL);
  }

  @Test
  public void iterableIsOrderedWithComparatorFailure() {
    try {
      assertThat(asList("1", "10", "2", "20")).isOrdered(COMPARE_AS_DECIMAL);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("is ordered");
      assertThat(e.getMessage()).contains("<10> <2>");
    }
  }

  private static final Comparator<String> COMPARE_AS_DECIMAL =
      new Comparator<String>() {
        @Override
        public int compare(String a, String b) {
          return Integer.valueOf(a).compareTo(Integer.valueOf(b));
        }
      };

  private static class Foo {
    private final int x;

    private Foo(int x) {
      this.x = x;
    }
  }

  private static class Bar extends Foo {
    private Bar(int x) {
      super(x);
    }
  }

  private static final Comparator<Foo> FOO_COMPARATOR =
      new Comparator<Foo>() {
        @Override
        public int compare(Foo a, Foo b) {
          return Integer.compare(a.x, b.x);
        }
      };

  @Test
  public void iterableOrderedByBaseClassComparator() {
    Iterable<Bar> targetList = asList(new Bar(1), new Bar(2), new Bar(3));
    assertThat(targetList).isOrdered(FOO_COMPARATOR);
    assertThat(targetList).isStrictlyOrdered(FOO_COMPARATOR);
  }
}
