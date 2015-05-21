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
    assertThat(iterable(1, 2, 3)).contains(1);
  }

  @Test
  public void iterableContainsItemWithNull() {
    assertThat(iterable(1, null, 3)).contains(null);
  }

  @Test
  public void iterableContainsItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<[1, 2, 3]> should have contained <5>");
    }
  }

  @Test
  public void namedIterableContainsItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).named("numbers").contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("numbers (<[1, 2, 3]>) should have contained <5>");
    }
  }

  @Test
  public void failureMessageIterableContainsItemFailure() {
    try {
      assertWithMessage("custom msg").that(iterable(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("custom msg: <[1, 2, 3]> should have contained <5>");
    }
  }

  @Test
  public void iterableDoesntHaveItem() {
    assertThat(iterable(1, null, 3)).doesNotContain(5);
  }

  @Test
  public void iterableDoesntHaveItemWithNull() {
    assertThat(iterable(1, 2, 3)).doesNotContain(null);
  }

  @Test
  public void iterableDoesntHaveItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).doesNotContain(2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<[1, 2, 3]> should not have contained <2>");
    }
  }

  @Test
  public void doesNotContainDuplicates() {
    assertThat(iterable(1, 2, 3)).containsNoDuplicates();
  }

  @Test
  public void doesNotContainDuplicatesMixedTypes() {
    List<Object> values = Lists.newArrayList();
    values.add(1);
    values.add(2);
    values.add(2L);
    values.add(3);
    assertThat(values).containsNoDuplicates();
  }

  @Test
  public void doesNotContainDuplicatesFailure() {
    try {
      assertThat(iterable(1, 2, 2, 3)).containsNoDuplicates();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<[1, 2, 2, 3]> has the following duplicates: <[2 x 2]>");
    }
  }

  @Test
  public void iterableContainsAnyOf() {
    assertThat(iterable(1, 2, 3)).containsAnyOf(1, 5);
  }

  @Test
  public void iterableContainsAnyOfWithNull() {
    assertThat(iterable(1, null, 3)).containsAnyOf(null, 5);
  }

  @Test
  public void iterableContainsAnyOfWithNullInThirdAndFinalPosition() {
    assertThat(iterable(1, null, 3)).containsAnyOf(4, 5, (Integer) null);
  }

  @Test
  public void iterableContainsAnyOfFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsAnyOf(5, 6, 0);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <[1, 2, 3]> contains any of <[5, 6, 0]>");
    }
  }

  @Test
  public void iterableContainsAnyOfWithOneShotIterable() {
    final Iterator<Object> iterator = iterable(2, 1, "b").iterator();
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
    assertThat(iterable(1, 2, 3)).containsAllOf(1, 2);
  }

  @Test
  public void iterableContainsAllOfWithDuplicates() {
    assertThat(iterable(1, 2, 2, 2, 3)).containsAllOf(2, 2);
  }

  @Test
  public void iterableContainsAllOfWithNull() {
    assertThat(iterable(1, null, 3)).containsAllOf(3, (Integer) null);
  }

  @Test
  public void iterableContainsAllOfWithNullAtThirdAndFinalPosition() {
    assertThat(iterable(1, null, 3)).containsAllOf(1, 3, null);
  }

  @Test
  public void iterableContainsAllOfFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsAllOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <[1, 2, 3]> contains all of <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test
  public void iterableContainsAllOfWithExtras() {
    try {
      assertThat(iterable("y", "x")).containsAllOf("x", "y", "z");
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
      assertThat(iterable("y", "x")).containsAllOf("x", "y", "y");
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
      assertThat(iterable(1, 2, 3)).containsAllOf(1, 2, 2, 2, 3, 4);
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
      assertThat(iterable(1, 2)).containsAllOf(4, 4, 4);
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
      assertThat(iterable(1, null, 3)).containsAllOf(1, null, null, 3);
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
    assertThat(iterable(3, 2, 5)).containsAllOf(3, 2, 5).inOrder();
  }

  @Test
  public void iterableContainsAllOfInOrderWithGaps() {
    assertThat(iterable(3, 2, 5)).containsAllOf(3, 5).inOrder();
    assertThat(iterable(3, 2, 2, 4, 5)).containsAllOf(3, 2, 2, 5).inOrder();
    assertThat(iterable(3, 1, 4, 1, 5)).containsAllOf(3, 1, 5).inOrder();
    assertThat(iterable("x", "y", "y", "z")).containsAllOf("x", "y", "z").inOrder();
    assertThat(iterable("x", "x", "y", "z")).containsAllOf("x", "y", "z").inOrder();
    assertThat(iterable("z", "x", "y", "z")).containsAllOf("x", "y", "z").inOrder();
    assertThat(iterable("x", "x", "y", "z", "x")).containsAllOf("x", "y", "z", "x").inOrder();
  }

  @Test
  public void iterableContainsAllOfInOrderWithNull() {
    assertThat(iterable(3, null, 5)).containsAllOf(3, null, 5).inOrder();
    assertThat(iterable(3, null, 7, 5)).containsAllOf(3, null, 5).inOrder();
  }

  @Test
  public void iterableContainsAllOfInOrderWithFailure() {
    try {
      assertThat(iterable(1, null, 3)).containsAllOf(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <[1, null, 3]> contains all elements in order <[null, 1, 3]>");
    }
  }

  @Test
  public void iterableContainsAllOfInOrderWithOneShotIterable() {
    final Iterable<Object> iterable = iterable(2, 1, null, 4, "a", 3, "b");
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
    final Iterator<Object> iterator = iterable(2, 1, null, 4, "a", 3, "b").iterator();
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
    assertThat(iterable(1, 2, 3)).containsNoneOf(4, 5, 6);
  }

  @Test
  public void iterableContainsNoneOfFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsNoneOf(1, 2, 4);
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
      assertThat(iterable(1, 2, 2, 3)).containsNoneOf(1, 2, 4);
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
      assertThat(iterable(1, 2, 3)).containsNoneOf(1, 2, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, 2, 3]> contains none of <[1, 2, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test
  public void listContainsExactlyArray() {
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
    assertThat(iterable(1, 2, 3)).containsExactly(1, 2, 3);
  }

  @Test
  public void iterableContainsExactlyOutOfOrder() {
    assertThat(iterable(1, 2, 3, 4)).containsExactly(3, 1, 4, 2);
  }

  @Test
  public void iterableContainsExactlyWithDuplicates() {
    assertThat(iterable(1, 2, 2, 2, 3)).containsExactly(1, 2, 2, 2, 3);
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesOutOfOrder() {
    assertThat(iterable(1, 2, 2, 2, 3)).containsExactly(2, 1, 2, 3, 2);
  }

  @Test
  public void iterableContainsExactlyWithOnlyNull() {
    Iterable<Object> actual = iterable((Object) null);
    assertThat(actual).containsExactly(null);
  }

  @Test
  public void iterableContainsExactlyWithNullSecond() {
    assertThat(iterable(1, null)).containsExactly(1, null);
  }

  @Test
  public void iterableContainsExactlyWithNullThird() {
    assertThat(iterable(1, 2, null)).containsExactly(1, 2, null);
  }

  @Test
  public void iterableContainsExactlyWithNull() {
    assertThat(iterable(1, null, 3)).containsExactly(1, null, 3);
  }

  @Test
  public void iterableContainsExactlyWithNullOutOfOrder() {
    assertThat(iterable(1, null, 3)).containsExactly(1, 3, (Integer) null);
  }

  @Test
  public void iterableContainsExactlyWithElementsThatThrowWhenYouCallHashCode() {
    HashCodeThrower one = new HashCodeThrower();
    HashCodeThrower two = new HashCodeThrower();

    assertThat(iterable(one, two)).containsExactly(two, one);
    assertThat(iterable(one, two)).containsExactly(one, two).inOrder();
    assertThat(iterable(one, two)).containsExactlyElementsIn(iterable(two, one));
    assertThat(iterable(one, two)).containsExactlyElementsIn(iterable(one, two)).inOrder();

    try {
      assertThat(iterable(one, two)).containsExactly(one);
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
      assertThat(iterable(1, 2)).containsExactly(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage("Not true that <[1, 2]> contains exactly <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test
  public void iterableContainsExactlyUnexpectedItemFailure() {
    try {
      assertThat(iterable(1, 2, 3)).containsExactly(1, 2);
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
      assertThat(iterable(1, 2, 3)).containsExactly(1, 2, 2, 2, 3);
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
      assertThat(iterable(1, 2, 3)).containsExactly(1, 2, 2, 2, 3, 4);
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
      assertThat(iterable(1, 2, 2, 2, 2, 3)).containsExactly(1, 2, 2, 3);
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
      assertThat(iterable()).containsExactly(4, 4, 4);
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
      assertThat(iterable(1, null, 3)).containsExactly(1, null, null, 3);
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
      assertThat(iterable(1, 2, 3)).containsExactly(1, 2, 4);
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
      assertThat(iterable(1, 2, 3, 3)).containsExactly(1, 2, 4, 4);
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
      assertThat(iterable(1, 2, 3, 4)).containsExactly(iterable(1, 2, 3, 4));
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
      assertThat(iterable(1, 2, 3, 4)).containsExactlyElementsIn(iterable(1, 2, 3));
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
      assertThat(iterable(1, 2, 3, 4)).containsExactly(iterable(1, 2), iterable(3, 4));
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
      assertThat(iterable(1, 2, 3, 4)).containsExactly(1);
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
    assertThat(iterable(3, 2, 5)).containsExactly(3, 2, 5).inOrder();
  }

  @Test
  public void iterableContainsExactlyInOrderWithNull() {
    assertThat(iterable(3, null, 5)).containsExactly(3, null, 5).inOrder();
  }

  @Test
  public void iterableContainsExactlyInOrderWithFailure() {
    try {
      assertThat(iterable(1, null, 3)).containsExactly(null, 1, 3).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <[1, null, 3]> contains only these elements in order <[null, 1, 3]>");
    }
  }

  @Test
  public void iterableContainsExactlyInOrderWithOneShotIterable() {
    final Iterator<Object> iterator = iterable(1, null, 3).iterator();
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
    final Iterator<Object> iterator = iterable(1, null, 3).iterator();
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
    assertThat(iterable()).isEmpty();
  }

  @Test
  public void iterableIsEmptyWithFailure() {
    try {
      assertThat(iterable(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <[1, null, 3]> is empty");
    }
  }

  @Test
  public void iterableIsNotEmpty() {
    assertThat(iterable("foo")).isNotEmpty();
  }

  @Test
  public void iterableIsNotEmptyWithFailure() {
    try {
      assertThat(iterable()).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <[]> is not empty");
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
