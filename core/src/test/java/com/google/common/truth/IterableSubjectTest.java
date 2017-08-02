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

import static com.google.common.collect.Collections2.permutations;
import static com.google.common.truth.Correspondence.tolerance;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.Rule;
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
public class IterableSubjectTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

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
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <[1, 2, 3]> has a size of <4>. It is <3>");
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
  public void iterableContains() {
    assertThat(asList(1, 2, 3)).contains(1);
  }

  @Test
  public void iterableContainsWithNull() {
    assertThat(asList(1, null, 3)).contains(null);
  }

  @Test
  public void iterableContainsFailsWithSameToString() {
    try {
      assertThat(asList(1L, 2L, 3L, 2L)).contains(2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "<[1, 2, 3, 2]> should have contained <2 (java.lang.Integer)> but doesn't. However, "
                  + "it does contain <[2 [2 copies]] (java.lang.Long)>.");
    }
  }

  @Test
  public void iterableContainsFailsWithSameToStringAndNull() {
    try {
      assertThat(asList(1, "null")).contains(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "<[1, null]> should have contained <null (null type)> but doesn't. However, it does "
                  + "contain <[null] (java.lang.String)>.");
    }
  }

  @Test
  public void iterableContainsFailure() {
    try {
      assertThat(asList(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("<[1, 2, 3]> should have contained <5>");
    }
  }

  @Test
  public void namedIterableContainsFailure() {
    try {
      assertThat(asList(1, 2, 3)).named("numbers").contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("numbers (<[1, 2, 3]>) should have contained <5>");
    }
  }

  @Test
  public void failureMessageIterableContainsFailure() {
    try {
      assertWithMessage("custom msg").that(asList(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("custom msg: <[1, 2, 3]> should have contained <5>");
    }
  }

  @Test
  public void iterableDoesNotContain() {
    assertThat(asList(1, null, 3)).doesNotContain(5);
  }

  @Test
  public void iterableDoesNotContainNull() {
    assertThat(asList(1, 2, 3)).doesNotContain(null);
  }

  @Test
  public void iterableDoesNotContainFailure() {
    try {
      assertThat(asList(1, 2, 3)).doesNotContain(2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("<[1, 2, 3]> should not have contained <2>");
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
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("<[1, 2, 2, 3]> has the following duplicates: <[2 x 2]>");
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
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <[1, 2, 3]> contains any of <[5, 6, 0]>");
    }
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndHomogeneousList() {
    try {
      assertThat(asList(1L, 2L, 3L)).containsAnyOf(2, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2, 3]> contains any of <[2, 3] (java.lang.Integer)>. "
                  + "However, it does contain <[2, 3] (java.lang.Long)>.");
    }
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    try {
      assertThat(asList(3L, 3L)).containsAnyOf(2, 3, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[3, 3]> contains any of <[2, 3, 3] (java.lang.Integer)>. "
                  + "However, it does contain <[3 [2 copies]] (java.lang.Long)>.");
    }
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndNullInSubject() {
    try {
      assertThat(asList(null, "abc")).containsAnyOf("def", "null");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[null, abc]> contains any of <[def, null] (java.lang.String)>. "
                  + "However, it does contain <[null (null type)]>.");
    }
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndNullInExpectation() {
    try {
      assertThat(asList("null", "abc")).containsAnyOf("def", null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[null, abc]> contains any of "
                  + "<[def (java.lang.String), null (null type)]>. "
                  + "However, it does contain <[null] (java.lang.String)>.");
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
    assertThat(asList(1, null, 3)).containsAllOf(1, 3, (Object) null);
  }

  @Test
  public void iterableContainsAllOfFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsAllOf(1, 2, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <[1, 2, 3]> contains all of <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test
  public void iterableContainsAllOfWithExtras() {
    try {
      assertThat(asList("y", "x")).containsAllOf("x", "y", "z");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("Not true that <[y, x]> contains all of <[x, y, z]>. It is missing <[z]>");
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
          .hasMessageThat()
          .isEqualTo("Not true that <[y, x]> contains all of <[x, y, y]>. It is missing <[y]>");
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, null, 3]> contains all of <[1, null, null, 3]>. "
                  + "It is missing <[null]>");
    }
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousList() {
    try {
      assertThat(asList(1L, 2L)).containsAllOf(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2]> contains all of <[1, 2]>. It is missing "
                  + "<[1, 2] (java.lang.Integer)>. However, it does contain "
                  + "<[1, 2] (java.lang.Long)>.");
    }
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    try {
      assertThat(asList(1L, 2L, 2L)).containsAllOf(1, 1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2, 2]> contains all of <[1, 1, 2]>. It is missing "
                  + "<[1 [2 copies], 2] (java.lang.Integer)>. However, it does contain "
                  + "<[1, 2 [2 copies]] (java.lang.Long)>.");
    }
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousListWithNull() {
    try {
      assertThat(asList("null", "abc")).containsAllOf("abc", null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[null, abc]> contains all of <[abc, null]>. It is missing "
                  + "<[null (null type)]>. However, it does contain <[null] (java.lang.String)>.");
    }
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHeterogeneousListWithDuplicates() {
    try {
      assertThat(asList(1, 2, 2L, 3L, 3L)).containsAllOf(2L, 2L, 3, 3);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2, 2, 3, 3]> contains all of <[2, 2, 3, 3]>. It is missing "
                  + "<[2 (java.lang.Long), 3 (java.lang.Integer) [2 copies]]>. However, it does "
                  + "contain <[2 (java.lang.Integer), 3 (java.lang.Long) [2 copies]]>.");
    }
  }

  @Test
  public void iterableContainsAllOfFailsWithEmptyString() {
    expectFailure.whenTesting().that(asList("a", null)).containsAllOf("", null);

    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[a, null]> contains all of <[\"\" (empty String), null]>. "
                + "It is missing <[\"\" (empty String)]>");
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
          .hasMessageThat()
          .isEqualTo("Not true that <[1, null, 3]> contains all elements in order <[null, 1, 3]>");
    }
  }

  @Test
  public void iterableContainsAllOfInOrderWithOneShotIterable() {
    final Iterable<Object> iterable = Arrays.<Object>asList(2, 1, null, 4, "a", 3, "b");
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
      assertThat(iterable).containsAllOf(1, 3, (Object) null).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Not true that <BadIterable> contains all elements in order <[1, 3, null]>");
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2, 3]> contains none of <[1, 2, 2, 4]>. It contains <[1, 2]>");
    }
  }

  @Test
  public void iterableContainsNoneOfFailureWithEmptyString() {
    expectFailure.whenTesting().that(asList("")).containsNoneOf("", null);

    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[]> contains none of <[\"\" (empty String), null]>. "
                + "It contains <[\"\" (empty String)]>");
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
    assertThat(iterable).containsExactly((Object[]) array);
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
  public void iterableContainsExactlyWithOnlyNullPassedAsNullArray() {
    // Truth is tolerant of this erroneous varargs call.
    Iterable<Object> actual = asList((Object) null);
    assertThat(actual).containsExactly((Object[]) null);
  }

  @Test
  public void iterableContainsExactlyWithOnlyNull() {
    Iterable<Object> actual = asList((Object) null);
    assertThat(actual).containsExactly((Object) null);
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
  public void iterableContainsExactlyWithEmptyString() {
    expectFailure.whenTesting().that(asList()).containsExactly("");

    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[]> contains exactly <[\"\" (empty String)]>. "
                + "It is missing <[\"\" (empty String)]>");
  }

  @Test
  public void iterableContainsExactlyWithEmptyStringAndUnexpectedItem() {
    expectFailure.whenTesting().that(asList("a", null)).containsExactly("");

    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[a, null]> contains exactly <[\"\" (empty String)]>. "
                + "It is missing <[\"\" (empty String)]> and has unexpected items <[a, null]>");
  }

  @Test
  public void iterableContainsExactlyWithEmptyStringAndMissingItem() {
    expectFailure.whenTesting().that(asList("")).containsExactly("a", null);

    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[]> contains exactly <[a, null]>. "
                + "It is missing <[a, null]> and has unexpected items <[\"\" (empty String)]>");
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
          .hasMessageThat()
          .isEqualTo(
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
  public void iterableContainsExactlyElementsInInOrderPassesWithEmptyExpectedAndActual() {
    assertThat(ImmutableList.of()).containsExactlyElementsIn(ImmutableList.of()).inOrder();
  }

  @Test
  public void iterableContainsExactlyElementsInWithEmptyExpected() {
    try {
      assertThat(asList("foo")).containsExactlyElementsIn(ImmutableList.of());
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("Not true that <[foo]> is empty");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void iterableContainsExactlyElementsInErrorMessageIsOrdered() {
    try {
      assertThat(asList("foo OR bar")).containsExactlyElementsIn(asList("foo", "bar"));
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo("Not true that <[1, 2]> contains exactly <[1, 2, 4]>. It is missing <[4]>");
    }
  }

  @Test
  public void iterableContainsExactlyUnexpectedItemFailure() {
    try {
      assertThat(asList(1, 2, 3)).containsExactly(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2, 3, 3]> contains exactly <[1, 2, 4, 4]>. "
                  + "It is missing <[4 [2 copies]]> and has unexpected items <[3 [2 copies]]>");
    }
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHomogeneousList() {
    try {
      assertThat(asList(1L, 2L)).containsExactly(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2]> contains exactly <[1, 2]>. It is missing "
                  + "<[1, 2] (java.lang.Integer)> and has unexpected items "
                  + "<[1, 2] (java.lang.Long)>");
    }
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndListWithNull() {
    try {
      assertThat(asList(1L, 2L)).containsExactly(null, 1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2]> contains exactly <[null, 1, 2]>. It is missing "
                  + "<[null (null type), 1 (java.lang.Integer), 2 (java.lang.Integer)]> and has "
                  + "unexpected items <[1, 2] (java.lang.Long)>");
    }
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHeterogeneousList() {
    try {
      assertThat(asList(1L, 2)).containsExactly(1, null, 2L);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2]> contains exactly <[1, null, 2]>. It is missing "
                  + "<[1 (java.lang.Integer), null (null type), 2 (java.lang.Long)]> and has "
                  + "unexpected items <[1 (java.lang.Long), 2 (java.lang.Integer)]>");
    }
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    try {
      assertThat(asList(1L, 2L)).containsExactly(1, 2, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2]> contains exactly <[1, 2, 2]>. It is missing "
                  + "<[1, 2 [2 copies]] (java.lang.Integer)> and has unexpected items "
                  + "<[1, 2] (java.lang.Long)>");
    }
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHeterogeneousListWithDuplicates() {
    try {
      assertThat(asList(1L, 2)).containsExactly(1, null, null, 2L, 2L);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, 2]> contains exactly <[1, null, null, 2, 2]>. It is missing "
                  + "<[1 (java.lang.Integer), null (null type) [2 copies], "
                  + "2 (java.lang.Long) [2 copies]]> and has unexpected items "
                  + "<[1 (java.lang.Long), 2 (java.lang.Integer)]>");
    }
  }

  @Test
  public void iterableContainsExactlyWithOneIterableGivesWarning() {
    try {
      assertThat(asList(1, 2, 3, 4)).containsExactly(asList(1, 2, 3, 4));
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
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
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[1, null, 3]> contains exactly these elements in order "
                  + "<[null, 1, 3]>");
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
          .hasMessageThat()
          .isEqualTo(
              "Not true that <BadIterable> contains exactly "
                  + "these elements in order <[1, 3, null]>");
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
      assertThat(e).hasMessageThat().isEqualTo("Not true that <[1, null, 3]> is empty");
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
      assertThat(e).hasMessageThat().isEqualTo("Not true that <[]> is not empty");
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
      assertThat(e).hasMessageThat().contains("is strictly ordered");
      assertThat(e).hasMessageThat().contains("<2> <2>");
    }
  }

  @Test
  public void isStrictlyOrderedWithNonComparableElementsFailure() {
    try {
      assertThat(asList((Object) 1, "2", 3, "4")).isStrictlyOrdered();
      fail("Should have thrown.");
    } catch (ClassCastException expected) {
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
      assertThat(e).hasMessageThat().contains("is ordered");
      assertThat(e).hasMessageThat().contains("<3> <2>");
    }
  }

  @Test
  public void isOrderedWithNonComparableElementsFailure() {
    try {
      assertThat(asList((Object) 1, "2", 2, "3")).isOrdered();
      fail("Should have thrown.");
    } catch (ClassCastException expected) {
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
      assertThat(e).hasMessageThat().contains("is strictly ordered");
      assertThat(e).hasMessageThat().contains("<2> <2>");
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
      assertThat(e).hasMessageThat().contains("is ordered");
      assertThat(e).hasMessageThat().contains("<10> <2>");
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
          return (a.x < b.x) ? -1 : ((a.x > b.x) ? 1 : 0);
        }
      };

  @Test
  public void iterableOrderedByBaseClassComparator() {
    Iterable<Bar> targetList = asList(new Bar(1), new Bar(2), new Bar(3));
    assertThat(targetList).isOrdered(FOO_COMPARATOR);
    assertThat(targetList).isStrictlyOrdered(FOO_COMPARATOR);
  }

  /**
   * A correspondence between strings and integers which tests whether the string parses as the
   * integer. Parsing is as specified by {@link Integer#decode(String)}. It considers null to
   * correspond to null only.
   */
  static final Correspondence<String, Integer> STRING_PARSES_TO_INTEGER_CORRESPONDENCE =
      new Correspondence<String, Integer>() {

        @Override
        public boolean compare(@Nullable String actual, @Nullable Integer expected) {
          if (actual == null) {
            return expected == null;
          }
          try {
            return Integer.decode(actual).equals(expected);
          } catch (NumberFormatException e) {
            return false;
          }
        }

        @Override
        public String toString() {
          return "parses to";
        }
      };

  @Test
  public void comparingElementsUsing_contains_success() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(456);
  }

  @Test
  public void comparingElementsUsing_contains_failure() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .contains(2345);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[not a number, +123, +456, +789]> contains at least one element that"
                  + " parses to <2345>");
    }
  }

  @Test
  public void comparingElementsUsing_contains_null() {
    List<String> actual = Arrays.asList("+123", null, "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .contains(null);
  }

  @Test
  public void comparingElementsUsing_wrongTypeInActual() {
    ImmutableList<?> actual = ImmutableList.of("valid", 123);
    IterableSubject.UsingCorrespondence<String, Integer> intermediate =
        assertThat(actual).comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE);
    try {
      intermediate.contains(456);
      fail("Expected ClassCastException as actual Iterable contains a non-String");
    } catch (ClassCastException expected) {
    }
  }

  @Test
  public void comparingElementsUsing_doesNotContain_success() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContain(2345);
  }

  @Test
  public void comparingElementsUsing_doesNotContains_failure() {
    ImmutableList<String> actual = ImmutableList.of("not a number", "+123", "+456", "+789");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .doesNotContain(456);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "<[not a number, +123, +456, +789]> should not have contained an element that "
                  + "parses to <456>. It contained the following such elements: <[+456]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_inOrder_success() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_successOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_successNonGreedy() {
    // (We use doubles with approximate equality for this test, because we can't illustrate this
    // case with the string parsing correspondence used in the other tests, because one string
    // won't parse to more than one integer.)
    ImmutableList<Double> expected = ImmutableList.of(1.0, 1.1, 1.2);
    ImmutableList<Double> actual = ImmutableList.of(1.05, 1.15, 0.95);
    // The comparingElementsUsing test with a tolerance of 0.1 should succeed by pairing 1.0 with
    // 0.95, 1.1 with 1.05, and 1.2 with 1.15. A left-to-right greedy implementation would fail as
    // it would pair 1.0 with 1.05 and 1.1 with 1.15, and fail to pair 1.2 with 0.95. Check that the
    // implementation is truly non-greedy by testing all permutations.
    for (List<Double> permutedActual : permutations(actual)) {
      assertThat(permutedActual)
          .comparingElementsUsing(tolerance(0.1))
          .containsExactlyElementsIn(expected);
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "0x40", "0x80");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+64, +128, 0x40, 0x80]> contains exactly one element that "
                  + "parses to each element of <[64, 128, 256, 128]>. "
                  + "It is missing an element that parses to <256>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_inOrder_passesWhenBothEmpty() {
    ImmutableList<Integer> expected = ImmutableList.of();
    ImmutableList<String> actual = ImmutableList.of();
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsExpectedIsEmpty() {
    ImmutableList<Integer> expected = ImmutableList.of();
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "0x40", "0x80");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().isEqualTo("Not true that <[+64, +128, 0x40, 0x80]> is empty");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMultipleMissingCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+64", "0x40", "0x40");
    // Actual list has candidate matches for 64 only, and is missing 128, 256, and the other 128.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+64, +64, 0x40, 0x40]> contains exactly one element that "
                  + "parses to each element of <[64, 128, 256, 128]>. "
                  + "It is missing an element that parses to each of <[128, 256, 128]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsOrderedMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 512);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256");
    // Actual list has in-order candidate matches for 64, 128, and 256, but is missing 512.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+64, +128, +256]> contains exactly one element that "
                  + "parses to each element of <[64, 128, 256, 512]>. "
                  + "It is missing an element that parses to <512>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "cheese");
    // Actual list has candidate matches for all the expected, but has extra cheese.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+64, +128, +256, cheese]> contains exactly one element that "
                  + "parses to each element of <[64, 128, 256, 128]>. "
                  + "It has unexpected elements <[cheese]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsOrderedExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80", "cheese");
    // Actual list has in-order candidate matches for all the expected, but has extra cheese.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+64, +128, +256, 0x80, cheese]> contains exactly one element that "
                  + "parses to each element of <[64, 128, 256, 128]>. "
                  + "It has unexpected elements <[cheese]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMissingAndExtraCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "jalapenos", "cheese");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256 and has
    // extra jalapenos and cheese.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+64, +128, jalapenos, cheese]> contains exactly one element that "
                  + "parses to each element of <[64, 128, 256, 128]>. "
                  + "It is missing an element that parses to <256> "
                  + "and has unexpected elements <[jalapenos, cheese]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMissingElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+128, +64, +256]> contains exactly one element that parses "
                  + "to each element of <[64, 128, 256, 128]>. It contains at least one element "
                  + "that matches each expected element, and every element it contains matches at "
                  + "least one expected element, but there was no 1:1 mapping between all the "
                  + "actual and expected elements. Using the most complete 1:1 mapping (or one "
                  + "such mapping, if there is a tie), it is missing an element that parses to "
                  + "<128>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsExtraElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x80", "0x40");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
    } catch (AssertionError e) {
      String expectedPreamble =
          "Not true that <[+128, +64, +256, 0x80, 0x40]> contains exactly one element that parses "
              + "to each element of <[64, 128, 256, 128]>. It contains at least one element "
              + "that matches each expected element, and every element it contains matches at "
              + "least one expected element, but there was no 1:1 mapping between all the "
              + "actual and expected elements. Using the most complete 1:1 mapping (or one "
              + "such mapping, if there is a tie), it has unexpected elements ";
      assertThat(e)
          .hasMessageThat()
          .isAnyOf(expectedPreamble + "<[0x40]>", expectedPreamble + "<[+64]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_failsMissingAndExtraInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected);
    } catch (AssertionError e) {
      String expectedPreamble =
          "Not true that <[+128, +64, +256, 0x40]> contains exactly one element that parses "
              + "to each element of <[64, 128, 256, 128]>. It contains at least one element "
              + "that matches each expected element, and every element it contains matches at "
              + "least one expected element, but there was no 1:1 mapping between all the "
              + "actual and expected elements. Using the most complete 1:1 mapping (or one "
              + "such mapping, if there is a tie), it is missing an element that parses to "
              + "<128> and has unexpected elements ";
      assertThat(e)
          .hasMessageThat()
          .isAnyOf(expectedPreamble + "<[0x40]>", expectedPreamble + "<[+64]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_inOrder_failsOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyElementsIn(expected)
          .inOrder();
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+128, +64, 0x80, +256]> contains, in order, exactly one element "
                  + "that parses to each element of <[64, 128, 256, 128]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactlyElementsIn_null() {
    List<Integer> expected = Arrays.asList(128, null);
    List<String> actual = Arrays.asList(null, "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyElementsIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsExactly_inOrder_success() {
    ImmutableList<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(64, 128, 256, 128)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsExactly_successOutOfOrder() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "0x80", "+256");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(64, 128, 256, 128);
  }

  @Test
  public void comparingElementsUsing_containsExactly_failsMissingAndExtraInOneToOne() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactly(64, 128, 256, 128);
    } catch (AssertionError e) {
      String expectedPreamble =
          "Not true that <[+128, +64, +256, 0x40]> contains exactly one element that parses "
              + "to each element of <[64, 128, 256, 128]>. It contains at least one element "
              + "that matches each expected element, and every element it contains matches at "
              + "least one expected element, but there was no 1:1 mapping between all the "
              + "actual and expected elements. Using the most complete 1:1 mapping (or one "
              + "such mapping, if there is a tie), it is missing an element that parses to "
              + "<128> and has unexpected elements ";
      assertThat(e)
          .hasMessageThat()
          .isAnyOf(expectedPreamble + "<[0x40]>", expectedPreamble + "<[+64]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsExactly_nullValueInArray() {
    List<String> actual = Arrays.asList(null, "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(128, null);
  }

  @Test
  public void comparingElementsUsing_containsExactly_nullArray() {
    // Truth is tolerant of this erroneous varargs call.
    List<String> actual = Arrays.asList((String) null);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly((Integer[]) null)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsAllIn_inOrder_success() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "+256", "0x80", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsAllIn_successOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsAllIn_successNonGreedy() {
    // (We use doubles with approximate equality for this test, because we can't illustrate this
    // case with the string parsing correspondence used in the other tests, because one string
    // won't parse to more than one integer.)
    ImmutableList<Double> expected = ImmutableList.of(1.0, 1.1, 1.2);
    ImmutableList<Double> actual = ImmutableList.of(99.999, 1.05, 99.999, 1.15, 0.95, 99.999);
    // The comparingElementsUsing test with a tolerance of 0.1 should succeed by pairing 1.0 with
    // 0.95, 1.1 with 1.05, and 1.2 with 1.15. A left-to-right greedy implementation would fail as
    // it would pair 1.0 with 1.05 and 1.1 with 1.15, and fail to pair 1.2 with 0.95. Check that the
    // implementation is truly non-greedy by testing all permutations.
    for (List<Double> permutedActual : permutations(actual)) {
      assertThat(permutedActual).comparingElementsUsing(tolerance(0.1)).containsAllIn(expected);
    }
  }

  @Test
  public void comparingElementsUsing_containsAllIn_failsMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "0x40", "0x80", "fum");
    // Actual list has candidate matches for 64, 128, and the other 128, but is missing 256.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsAllIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[fee, +64, +128, fi, fo, 0x40, 0x80, fum]> contains at least one "
                  + "element that parses to each element of <[64, 128, 256, 128]>. "
                  + "It is missing an element that parses to <256>");
    }
  }

  @Test
  public void comparingElementsUsing_containsAllIn_failsMultipleMissingCandidates() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+64", "fi", "fo", "0x40", "0x40", "fum");
    // Actual list has candidate matches for 64 only, and is missing 128, 256, and the other 128.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsAllIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[fee, +64, +64, fi, fo, 0x40, 0x40, fum]> contains at least one "
                  + "element that parses to each element of <[64, 128, 256, 128]>. "
                  + "It is missing an element that parses to each of <[128, 256, 128]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsAllIn_failsOrderedMissingOneCandidate() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 512);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "fi", "fo", "+128", "+256", "fum");
    // Actual list has in-order candidate matches for 64, 128, and 256, but is missing 512.
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsAllIn(expected);
      fail("Expected failure");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[fee, +64, fi, fo, +128, +256, fum]> contains at least one "
                  + "element that parses to each element of <[64, 128, 256, 512]>. "
                  + "It is missing an element that parses to <512>");
    }
  }

  @Test
  public void comparingElementsUsing_containsAllIn_failsMissingElementInOneToOne() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "fi", "fo", "+64", "+256", "fum");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsAllIn(expected);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[fee, +128, fi, fo, +64, +256, fum]> contains at least one element "
                  + "that parses to each element of <[64, 128, 256, 128]>. It contains at least "
                  + "one element that matches each expected element, but there was no 1:1 mapping "
                  + "between all the expected elements and any subset of the actual elements. "
                  + "Using the most complete 1:1 mapping (or one such mapping, if there is a tie), "
                  + "it is missing an element that parses to <128>");
    }
  }

  @Test
  public void comparingElementsUsing_containsAllIn_inOrder_failsOutOfOrder() {
    ImmutableList<Integer> expected = ImmutableList.of(64, 128, 256, 128);
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsAllIn(expected)
          .inOrder();
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[fee, +128, +64, fi, fo, 0x80, +256, fum]> contains, in order, "
                  + "at least one element that parses to each element of <[64, 128, 256, 128]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsAllIn_null() {
    List<Integer> expected = Arrays.asList(128, null);
    List<String> actual = Arrays.asList(null, "fee", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsAllOf_inOrder_success() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+64", "+128", "fi", "fo", "+256", "0x80", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllOf(64, 128, 256, 128)
        .inOrder();
  }

  @Test
  public void comparingElementsUsing_containsAllOf_successOutOfOrder() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "+64", "fi", "fo", "0x80", "+256", "fum");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllOf(64, 128, 256, 128);
  }

  @Test
  public void comparingElementsUsing_containsAllOf_failsMissingElementInOneToOne() {
    ImmutableList<String> actual =
        ImmutableList.of("fee", "+128", "fi", "fo", "+64", "+256", "fum");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsAllOf(64, 128, 256, 128);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[fee, +128, fi, fo, +64, +256, fum]> contains at least one element "
                  + "that parses to each element of <[64, 128, 256, 128]>. It contains at least "
                  + "one element that matches each expected element, but there was no 1:1 mapping "
                  + "between all the expected elements and any subset of the actual elements. "
                  + "Using the most complete 1:1 mapping (or one such mapping, if there is a tie), "
                  + "it is missing an element that parses to <128>");
    }
  }

  @Test
  public void comparingElementsUsing_containsAllOf_nullValueInArray() {
    List<String> actual = Arrays.asList(null, "fee", "0x80");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAllOf(128, null);
  }

  @Test
  public void comparingElementsUsing_containsAnyOf_success() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyOf(255, 256, 257);
  }

  @Test
  public void comparingElementsUsing_containsAnyOf_failure() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsAnyOf(255, 256, 257);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+128, +64, This is not the string you're looking for, 0x40]> "
                  + "contains at least one element that parses to any of <[255, 256, 257]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsAnyOf_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyOf(255, null, 257);
  }

  @Test
  public void comparingElementsUsing_containsAnyIn_success() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    ImmutableList<Integer> expected = ImmutableList.of(255, 256, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsAnyIn_failure() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    ImmutableList<Integer> expected = ImmutableList.of(255, 256, 257);
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsAnyIn(expected);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+128, +64, This is not the string you're looking for, 0x40]> "
                  + "contains at least one element that parses to any element in "
                  + "<[255, 256, 257]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsAnyIn_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    List<Integer> expected = asList(255, null, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAnyIn(expected);
  }

  @Test
  public void comparingElementsUsing_containsNoneOf_success() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneOf(255, 256, 257);
  }

  @Test
  public void comparingElementsUsing_containsNoneOf_failure() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsNoneOf(255, 256, 257);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+128, +64, +256, 0x40]> contains no element that parses to any of "
                  + "<[255, 256, 257]>. It contains at least one element that parses to each of "
                  + "<[256]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsNoneOf_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsNoneOf(255, null, 257);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+128, +64, null, 0x40]> contains no element that parses to any of "
                  + "<[255, null, 257]>. It contains at least one element that parses to each of "
                  + "<[null]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsNoneIn_success() {
    ImmutableList<String> actual =
        ImmutableList.of("+128", "+64", "This is not the string you're looking for", "0x40");
    ImmutableList<Integer> excluded = ImmutableList.of(255, 256, 257);
    assertThat(actual)
        .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsNoneIn(excluded);
  }

  @Test
  public void comparingElementsUsing_containsNoneIn_failure() {
    ImmutableList<String> actual = ImmutableList.of("+128", "+64", "+256", "0x40");
    ImmutableList<Integer> excluded = ImmutableList.of(255, 256, 257);
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsNoneIn(excluded);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+128, +64, +256, 0x40]> contains no element that parses to "
                  + "any element in <[255, 256, 257]>. It contains at least one element that "
                  + "parses to each of <[256]>");
    }
  }

  @Test
  public void comparingElementsUsing_containsNoneIn_null() {
    List<String> actual = asList("+128", "+64", null, "0x40");
    List<Integer> excluded = asList(255, null, 257);
    try {
      assertThat(actual)
          .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsNoneIn(excluded);
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Not true that <[+128, +64, null, 0x40]> contains no element that parses to "
                  + "any element in <[255, null, 257]>. It contains at least one element that "
                  + "parses to each of <[null]>");
    }
  }
}
