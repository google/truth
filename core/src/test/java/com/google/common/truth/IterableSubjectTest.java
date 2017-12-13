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
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link IterableSubject} APIs, excluding those that use {@link Correspondence} (which
 * are tested in {@link IterableSubjectCorrespondenceTest}.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IterableSubjectTest extends BaseSubjectTestCase {

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
    expectFailure.whenTesting().that(ImmutableList.of(1, 2, 3)).hasSize(4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2, 3]> has a size of <4>. It is <3>");
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
    expectFailure.whenTesting().that(asList(1L, 2L, 3L, 2L)).contains(2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "<[1, 2, 3, 2]> should have contained <2 (java.lang.Integer)> but doesn't. However, "
                + "it does contain <[2 [2 copies]] (java.lang.Long)>.");
  }

  @Test
  public void iterableContainsFailsWithSameToStringAndNull() {
    expectFailure.whenTesting().that(asList(1, "null")).contains(null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "<[1, null]> should have contained <null (null type)> but doesn't. However, it does "
                + "contain <[null] (java.lang.String)>.");
  }

  @Test
  public void iterableContainsFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).contains(5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<[1, 2, 3]> should have contained <5>");
  }

  @Test
  public void namedIterableContainsFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).named("numbers").contains(5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("numbers (<[1, 2, 3]>) should have contained <5>");
  }

  @Test
  public void failureMessageIterableContainsFailure() {
    expectFailure.whenTesting().withMessage("custom msg").that(asList(1, 2, 3)).contains(5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("custom msg: <[1, 2, 3]> should have contained <5>");
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
    expectFailure.whenTesting().that(asList(1, 2, 3)).doesNotContain(2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<[1, 2, 3]> should not have contained <2>");
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
    expectFailure.whenTesting().that(asList(1, 2, 2, 3)).containsNoDuplicates();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<[1, 2, 2, 3]> has the following duplicates: <[2 x 2]>");
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
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsAnyOf(5, 6, 0);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2, 3]> contains any of <[5, 6, 0]>");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndHomogeneousList() {
    expectFailure.whenTesting().that(asList(1L, 2L, 3L)).containsAnyOf(2, 3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains any of <[2, 3] (java.lang.Integer)>. "
                + "However, it does contain <[2, 3] (java.lang.Long)>.");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    expectFailure.whenTesting().that(asList(3L, 3L)).containsAnyOf(2, 3, 3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[3, 3]> contains any of <[2, 3, 3] (java.lang.Integer)>. "
                + "However, it does contain <[3 [2 copies]] (java.lang.Long)>.");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndNullInSubject() {
    expectFailure.whenTesting().that(asList(null, "abc")).containsAnyOf("def", "null");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[null, abc]> contains any of <[def, null] (java.lang.String)>. "
                + "However, it does contain <[null (null type)]>.");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndNullInExpectation() {
    expectFailure.whenTesting().that(asList("null", "abc")).containsAnyOf("def", null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[null, abc]> contains any of "
                + "<[def (java.lang.String), null (null type)]>. "
                + "However, it does contain <[null] (java.lang.String)>.");
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
  public void iterableContainsAnyInIterable() {
    assertThat(asList(1, 2, 3)).containsAnyIn(asList(1, 10, 100));

    expectFailure.whenTesting().that(asList(1, 2, 3)).containsAnyIn(asList(5, 6, 0));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2, 3]> contains any element in <[5, 6, 0]>");
  }

  @Test
  public void iterableContainsAnyInArray() {
    assertThat(asList(1, 2, 3)).containsAnyIn(new Integer[] {1, 10, 100});

    expectFailure.whenTesting().that(asList(1, 2, 3)).containsAnyIn(new Integer[] {5, 6, 0});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2, 3]> contains any element in <[5, 6, 0]>");
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
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsAllOf(1, 2, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2, 3]> contains all of <[1, 2, 4]>. It is missing <[4]>");
  }

  @Test
  public void iterableContainsAllOfWithExtras() {
    expectFailure.whenTesting().that(asList("y", "x")).containsAllOf("x", "y", "z");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[y, x]> contains all of <[x, y, z]>. It is missing <[z]>");
  }

  @Test
  public void iterableContainsAllOfWithExtraCopiesOfOutOfOrder() {
    expectFailure.whenTesting().that(asList("y", "x")).containsAllOf("x", "y", "y");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[y, x]> contains all of <[x, y, y]>. It is missing <[y]>");
  }

  @Test
  public void iterableContainsAllOfWithDuplicatesFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsAllOf(1, 2, 2, 2, 3, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains all of <[1, 2, 2, 2, 3, 4]>. "
                + "It is missing <[2 [2 copies], 4]>");
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void iterableContainsAllOfWithDuplicateMissingElements() {
    expectFailure.whenTesting().that(asList(1, 2)).containsAllOf(4, 4, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2]> contains all of <[4, 4, 4]>. It is missing <[4 [3 copies]]>");
  }

  @Test
  public void iterableContainsAllOfWithNullFailure() {
    expectFailure.whenTesting().that(asList(1, null, 3)).containsAllOf(1, null, null, 3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, null, 3]> contains all of <[1, null, null, 3]>. "
                + "It is missing <[null]>");
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousList() {
    expectFailure.whenTesting().that(asList(1L, 2L)).containsAllOf(1, 2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2]> contains all of <[1, 2]>. It is missing "
                + "<[1, 2] (java.lang.Integer)>. However, it does contain "
                + "<[1, 2] (java.lang.Long)>.");
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    expectFailure.whenTesting().that(asList(1L, 2L, 2L)).containsAllOf(1, 1, 2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 2]> contains all of <[1, 1, 2]>. It is missing "
                + "<[1 [2 copies], 2] (java.lang.Integer)>. However, it does contain "
                + "<[1, 2 [2 copies]] (java.lang.Long)>.");
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousListWithNull() {
    expectFailure.whenTesting().that(asList("null", "abc")).containsAllOf("abc", null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[null, abc]> contains all of <[abc, null]>. It is missing "
                + "<[null (null type)]>. However, it does contain <[null] (java.lang.String)>.");
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHeterogeneousListWithDuplicates() {
    expectFailure.whenTesting().that(asList(1, 2, 2L, 3L, 3L)).containsAllOf(2L, 2L, 3, 3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 2, 3, 3]> contains all of <[2, 2, 3, 3]>. It is missing "
                + "<[2 (java.lang.Long), 3 (java.lang.Integer) [2 copies]]>. However, it does "
                + "contain <[2 (java.lang.Integer), 3 (java.lang.Long) [2 copies]]>.");
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
    expectFailure.whenTesting().that(asList(1, null, 3)).containsAllOf(null, 1, 3).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, null, 3]> contains all elements in order <[null, 1, 3]>");
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

    expectFailure.whenTesting().that(iterable).containsAllOf(1, 3, (Object) null).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <BadIterable> contains all elements in order <[1, 3, null]>");
  }

  @Test
  public void iterableContainsAllInIterable() {
    assertThat(asList(1, 2, 3)).containsAllIn(asList(1, 2));

    expectFailure.whenTesting().that(asList(1, 2, 3)).containsAllIn(asList(1, 2, 4));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains all elements in <[1, 2, 4]>. It is missing <[4]>");
  }

  @Test
  public void iterableContainsAllInArray() {
    assertThat(asList(1, 2, 3)).containsAllIn(new Integer[] {1, 2});

    expectFailure.whenTesting().that(asList(1, 2, 3)).containsAllIn(new Integer[] {1, 2, 4});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains all elements in <[1, 2, 4]>. It is missing <[4]>");
  }

  @Test
  public void iterableContainsNoneOf() {
    assertThat(asList(1, 2, 3)).containsNoneOf(4, 5, 6);
  }

  @Test
  public void iterableContainsNoneOfFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsNoneOf(1, 2, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2, 3]> contains none of <[1, 2, 4]>. It contains <[1, 2]>");
  }

  @Test
  public void iterableContainsNoneOfFailureWithDuplicateInSubject() {
    expectFailure.whenTesting().that(asList(1, 2, 2, 3)).containsNoneOf(1, 2, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 2, 3]> contains none of <[1, 2, 4]>. It contains <[1, 2]>");
  }

  @Test
  public void iterableContainsNoneOfFailureWithDuplicateInExpected() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsNoneOf(1, 2, 2, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains none of <[1, 2, 2, 4]>. It contains <[1, 2]>");
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
  public void iterableContainsNoneInIterable() {
    assertThat(asList(1, 2, 3)).containsNoneIn(asList(4, 5, 6));
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsNoneIn(asList(1, 2, 4));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains no elements in <[1, 2, 4]>. It contains <[1, 2]>");
  }

  @Test
  public void iterableContainsNoneInArray() {
    assertThat(asList(1, 2, 3)).containsNoneIn(new Integer[] {4, 5, 6});
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsNoneIn(new Integer[] {1, 2, 4});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains no elements in <[1, 2, 4]>. It contains <[1, 2]>");
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

    expectFailure.whenTesting().that(asList(one, two)).containsExactly(one);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[HCT, HCT]> contains exactly <[HCT]>. "
                + "It has unexpected items <[HCT]>");
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
    expectFailure.whenTesting().that(asList("foo")).containsExactlyElementsIn(ImmutableList.of());
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[foo]> is empty");
  }

  @Test
  public void iterableContainsExactlyElementsInErrorMessageIsOrdered() {
    expectFailure
        .whenTesting()
        .that(asList("foo OR bar"))
        .containsExactlyElementsIn(asList("foo", "bar"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[foo OR bar]> contains exactly <[foo, bar]>. "
                + "It is missing <[foo, bar]> and has unexpected items <[foo OR bar]>");
  }

  @Test
  public void iterableContainsExactlyMissingItemFailure() {
    expectFailure.whenTesting().that(asList(1, 2)).containsExactly(1, 2, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2]> contains exactly <[1, 2, 4]>. It is missing <[4]>");
  }

  @Test
  public void iterableContainsExactlyUnexpectedItemFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsExactly(1, 2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains exactly <[1, 2]>. It has unexpected items <[3]>");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesNotEnoughItemsFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsExactly(1, 2, 2, 2, 3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains exactly <[1, 2, 2, 2, 3]>. "
                + "It is missing <[2 [2 copies]]>");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesMissingItemFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsExactly(1, 2, 2, 2, 3, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains exactly <[1, 2, 2, 2, 3, 4]>. "
                + "It is missing <[2 [2 copies], 4]>");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesUnexpectedItemFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 2, 2, 2, 3)).containsExactly(1, 2, 2, 3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 2, 2, 2, 3]> contains exactly <[1, 2, 2, 3]>. "
                + "It has unexpected items <[2 [2 copies]]>");
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void iterableContainsExactlyWithDuplicateMissingElements() {
    expectFailure.whenTesting().that(asList()).containsExactly(4, 4, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[]> contains exactly <[4, 4, 4]>. It is missing <[4 [3 copies]]>");
  }

  @Test
  public void iterableContainsExactlyWithNullFailure() {
    expectFailure.whenTesting().that(asList(1, null, 3)).containsExactly(1, null, null, 3);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, null, 3]> contains exactly <[1, null, null, 3]>. "
                + "It is missing <[null]>");
  }

  @Test
  public void iterableContainsExactlyWithMissingAndExtraElements() {
    expectFailure.whenTesting().that(asList(1, 2, 3)).containsExactly(1, 2, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains exactly <[1, 2, 4]>. "
                + "It is missing <[4]> and has unexpected items <[3]>");
  }

  @Test
  public void iterableContainsExactlyWithDuplicateMissingAndExtraElements() {
    expectFailure.whenTesting().that(asList(1, 2, 3, 3)).containsExactly(1, 2, 4, 4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3, 3]> contains exactly <[1, 2, 4, 4]>. "
                + "It is missing <[4 [2 copies]]> and has unexpected items <[3 [2 copies]]>");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHomogeneousList() {
    expectFailure.whenTesting().that(asList(1L, 2L)).containsExactly(1, 2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2]> contains exactly <[1, 2]>. It is missing "
                + "<[1, 2] (java.lang.Integer)> and has unexpected items "
                + "<[1, 2] (java.lang.Long)>");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndListWithNull() {
    expectFailure.whenTesting().that(asList(1L, 2L)).containsExactly(null, 1, 2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2]> contains exactly <[null, 1, 2]>. It is missing "
                + "<[null (null type), 1 (java.lang.Integer), 2 (java.lang.Integer)]> and has "
                + "unexpected items <[1, 2] (java.lang.Long)>");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHeterogeneousList() {
    expectFailure.whenTesting().that(asList(1L, 2)).containsExactly(1, null, 2L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2]> contains exactly <[1, null, 2]>. It is missing "
                + "<[1 (java.lang.Integer), null (null type), 2 (java.lang.Long)]> and has "
                + "unexpected items <[1 (java.lang.Long), 2 (java.lang.Integer)]>");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    expectFailure.whenTesting().that(asList(1L, 2L)).containsExactly(1, 2, 2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2]> contains exactly <[1, 2, 2]>. It is missing "
                + "<[1, 2 [2 copies]] (java.lang.Integer)> and has unexpected items "
                + "<[1, 2] (java.lang.Long)>");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHeterogeneousListWithDuplicates() {
    expectFailure.whenTesting().that(asList(1L, 2)).containsExactly(1, null, null, 2L, 2L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2]> contains exactly <[1, null, null, 2, 2]>. It is missing "
                + "<[1 (java.lang.Integer), null (null type) [2 copies], "
                + "2 (java.lang.Long) [2 copies]]> and has unexpected items "
                + "<[1 (java.lang.Long), 2 (java.lang.Integer)]>");
  }

  @Test
  public void iterableContainsExactlyWithOneIterableGivesWarning() {
    expectFailure.whenTesting().that(asList(1, 2, 3, 4)).containsExactly(asList(1, 2, 3, 4));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3, 4]> contains exactly <[[1, 2, 3, 4]]>. "
                + "It is missing <[[1, 2, 3, 4]]> and has unexpected items <[1, 2, 3, 4]>. "
                + "Passing an iterable to the varargs method containsExactly(Object...) is "
                + "often not the correct thing to do. Did you mean to call "
                + "containsExactlyElementsIn(Iterable) instead?");
  }

  @Test
  public void iterableContainsExactlyElementsInWithOneIterableDoesNotGiveWarning() {
    expectFailure.whenTesting().that(asList(1, 2, 3, 4)).containsExactlyElementsIn(asList(1, 2, 3));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3, 4]> contains exactly <[1, 2, 3]>. "
                + "It has unexpected items <[4]>");
  }

  @Test
  public void iterableContainsExactlyWithTwoIterableDoesNotGivesWarning() {
    expectFailure
        .whenTesting()
        .that(asList(1, 2, 3, 4))
        .containsExactly(asList(1, 2), asList(3, 4));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3, 4]> contains exactly <[[1, 2], [3, 4]]>. "
                + "It is missing <[[1, 2], [3, 4]]> and has unexpected items <[1, 2, 3, 4]>");
  }

  @Test
  public void iterableContainsExactlyWithOneNonIterableDoesNotGiveWarning() {
    expectFailure.whenTesting().that(asList(1, 2, 3, 4)).containsExactly(1);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3, 4]> contains exactly <[1]>. "
                + "It has unexpected items <[2, 3, 4]>");
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
    expectFailure.whenTesting().that(asList(1, null, 3)).containsExactly(null, 1, 3).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, null, 3]> contains exactly these elements in order "
                + "<[null, 1, 3]>");
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

    expectFailure.whenTesting().that(iterable).containsExactly(1, 3, null).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <BadIterable> contains exactly "
                + "these elements in order <[1, 3, null]>");
  }

  @Test
  public void iterableWithNoToStringOverride() {
    Iterable<Integer> iterable =
        new Iterable<Integer>() {
          @Override
          public Iterator<Integer> iterator() {
            return Iterators.forArray(1, 2, 3);
          }
        };

    expectFailure.whenTesting().that(iterable).containsExactly(1, 2).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> contains exactly <[1, 2]>. It has unexpected items <[3]>");
  }

  @Test
  public void iterableContainsExactlyElementsInIterable() {
    assertThat(asList(1, 2)).containsExactlyElementsIn(asList(1, 2));

    expectFailure.whenTesting().that(asList(1, 2)).containsExactlyElementsIn(asList(1, 2, 4));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2]> contains exactly <[1, 2, 4]>. It is missing <[4]>");
  }

  @Test
  public void iterableContainsExactlyElementsInArray() {
    assertThat(asList(1, 2)).containsExactlyElementsIn(new Integer[] {1, 2});

    expectFailure
        .whenTesting()
        .that(asList(1, 2))
        .containsExactlyElementsIn(new Integer[] {1, 2, 4});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, 2]> contains exactly <[1, 2, 4]>. It is missing <[4]>");
  }

  @Test
  public void iterableIsEmpty() {
    assertThat(asList()).isEmpty();
  }

  @Test
  public void iterableIsEmptyWithFailure() {
    expectFailure.whenTesting().that(asList(1, null, 3)).isEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[1, null, 3]> is empty");
  }

  @Test
  public void iterableIsNotEmpty() {
    assertThat(asList("foo")).isNotEmpty();
  }

  @Test
  public void iterableIsNotEmptyWithFailure() {
    expectFailure.whenTesting().that(asList()).isNotEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <[]> is not empty");
  }

  @Test
  public void iterableIsStrictlyOrdered() {
    assertThat(asList()).isStrictlyOrdered();
    assertThat(asList(1)).isStrictlyOrdered();
    assertThat(asList(1, 2, 3, 4)).isStrictlyOrdered();
  }

  @Test
  public void isStrictlyOrderedFailure() {
    expectFailure.whenTesting().that(asList(1, 2, 2, 4)).isStrictlyOrdered();
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("is strictly ordered");
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("<2> <2>");
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
    expectFailure.whenTesting().that(asList(1, 3, 2, 4)).isOrdered();
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("is ordered");
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("<3> <2>");
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
    expectFailure
        .whenTesting()
        .that(asList("1", "2", "2", "10"))
        .isStrictlyOrdered(COMPARE_AS_DECIMAL);
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("is strictly ordered");
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("<2> <2>");
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
    expectFailure.whenTesting().that(asList("1", "10", "2", "20")).isOrdered(COMPARE_AS_DECIMAL);
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("is ordered");
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("<10> <2>");
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

  @Test
  public void isIn() {
    ImmutableList<String> actual = ImmutableList.of("a");
    ImmutableList<String> expectedA = ImmutableList.of("a");
    ImmutableList<String> expectedB = ImmutableList.of("b");
    ImmutableList<ImmutableList<String>> expected = ImmutableList.of(expectedA, expectedB);

    assertThat(actual).isIn(expected);
  }

  @Test
  public void isNotIn() {
    ImmutableList<String> actual = ImmutableList.of("a");
    ImmutableList<String> expectedB = ImmutableList.of("b");
    ImmutableList<String> expectedC = ImmutableList.of("c");
    ImmutableList<ImmutableList<String>> expected = ImmutableList.of(expectedB, expectedC);

    assertThat(actual).isNotIn(expected);
  }

  @Test
  public void isAnyOf() {
    ImmutableList<String> actual = ImmutableList.of("a");
    ImmutableList<String> expectedA = ImmutableList.of("a");
    ImmutableList<String> expectedB = ImmutableList.of("b");

    assertThat(actual).isAnyOf(expectedA, expectedB);
  }

  @Test
  public void isNoneOf() {
    ImmutableList<String> actual = ImmutableList.of("a");
    ImmutableList<String> expectedB = ImmutableList.of("b");
    ImmutableList<String> expectedC = ImmutableList.of("c");

    assertThat(actual).isNoneOf(expectedB, expectedC);
  }
}
