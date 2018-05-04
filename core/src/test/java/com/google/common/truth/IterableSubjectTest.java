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
import java.util.List;
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
    expectFailureWhenTestingThat(ImmutableList.of(1, 2, 3)).hasSize(4);
    assertFailureValue("value of", "iterable.size()");
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
    expectFailureWhenTestingThat(asList(1L, 2L, 3L, 2L)).contains(2);
    assertFailureKeys(
        "expected to contain",
        "an instance of",
        "but did not",
        "though it did contain",
        "full contents");
    assertFailureValue("expected to contain", "2");
    assertFailureValue("an instance of", "java.lang.Integer");
    assertFailureValue("though it did contain", "[2 [2 copies]] (java.lang.Long)");
    assertFailureValue("full contents", "[1, 2, 3, 2]");
  }

  @Test
  public void iterableContainsFailsWithSameToStringAndNull() {
    expectFailureWhenTestingThat(asList(1, "null")).contains(null);
    assertFailureValue("an instance of", "null type");
  }

  @Test
  public void iterableContainsFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).contains(5);
    assertFailureKeys("expected to contain", "but was");
    assertFailureValue("expected to contain", "5");
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
    expectFailureWhenTestingThat(asList(1, 2, 3)).doesNotContain(2);
    assertFailureKeys("expected not to contain", "but was");
    assertFailureValue("expected not to contain", "2");
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
    expectFailureWhenTestingThat(asList(1, 2, 2, 3)).containsNoDuplicates();
    assertFailureKeys("expected not to contain duplicates", "but contained", "full contents");
    assertFailureValue("but contained", "[2 x 2]");
    assertFailureValue("full contents", "[1, 2, 2, 3]");
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
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsAnyOf(5, 6, 0);
    assertFailureKeys("expected to contain any of", "but was");
    assertFailureValue("expected to contain any of", "[5, 6, 0]");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndHomogeneousList() {
    expectFailureWhenTestingThat(asList(1L, 2L, 3L)).containsAnyOf(2, 3);
    assertFailureKeys(
        "expected to contain any of", "but did not", "though it did contain", "full contents");
    assertFailureValue("expected to contain any of", "[2, 3] (java.lang.Integer)");
    assertFailureValue("though it did contain", "[2, 3] (java.lang.Long)");
    assertFailureValue("full contents", "[1, 2, 3]");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    expectFailureWhenTestingThat(asList(3L, 3L)).containsAnyOf(2, 3, 3);
    assertFailureKeys(
        "expected to contain any of", "but did not", "though it did contain", "full contents");
    assertFailureValue("expected to contain any of", "[2, 3 [2 copies]] (java.lang.Integer)");
    assertFailureValue("though it did contain", "[3 [2 copies]] (java.lang.Long)");
    assertFailureValue("full contents", "[3, 3]");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndNullInSubject() {
    expectFailureWhenTestingThat(asList(null, "abc")).containsAnyOf("def", "null");
    assertFailureKeys(
        "expected to contain any of", "but did not", "though it did contain", "full contents");
    assertFailureValue("expected to contain any of", "[def, null] (java.lang.String)");
    assertFailureValue("though it did contain", "[null (null type)]");
    assertFailureValue("full contents", "[null, abc]");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndNullInExpectation() {
    expectFailureWhenTestingThat(asList("null", "abc")).containsAnyOf("def", null);
    assertFailureKeys(
        "expected to contain any of", "but did not", "though it did contain", "full contents");
    assertFailureValue("expected to contain any of", "[def (java.lang.String), null (null type)]");
    assertFailureValue("though it did contain", "[null] (java.lang.String)");
    assertFailureValue("full contents", "[null, abc]");
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

    expectFailureWhenTestingThat(asList(1, 2, 3)).containsAnyIn(asList(5, 6, 0));
    assertFailureKeys("expected to contain any of", "but was");
    assertFailureValue("expected to contain any of", "[5, 6, 0]");
  }

  @Test
  public void iterableContainsAnyInArray() {
    assertThat(asList(1, 2, 3)).containsAnyIn(new Integer[] {1, 10, 100});

    expectFailureWhenTestingThat(asList(1, 2, 3)).containsAnyIn(new Integer[] {5, 6, 0});
    assertFailureKeys("expected to contain any of", "but was");
    assertFailureValue("expected to contain any of", "[5, 6, 0]");
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

  /*
   * Test that we only call toString() if the assertion fails -- that is, not just if the elements
   * are out of order, but only if someone actually calls inOrder(). There are 2 reasons for this:
   *
   * 1. Calling toString() uses extra time and space. (To be fair, Iterable assertions often use a
   * lot of those already.)
   *
   * 2. Some toString() methods are buggy. Arguably we shouldn't accommodate these, especially since
   * those users are in for a nasty surprise if their tests actually fail someday, but I don't want
   * to bite that off now. (Maybe Fact should catch exceptions from toString()?)
   */
  @Test
  public void iterableContainsAllOutOfOrderDoesNotStringify() {
    CountsToStringCalls o = new CountsToStringCalls();
    List<Object> actual = asList(o, 1);
    List<Object> expected = asList(1, o);
    assertThat(actual).containsAllIn(expected);
    assertThat(o.calls).isEqualTo(0);
    expectFailureWhenTestingThat(actual).containsAllIn(expected).inOrder();
    assertThat(o.calls).isGreaterThan(0);
  }

  @Test
  public void iterableContainsAllOfFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsAllOf(1, 2, 4);
    assertFailureKeys("missing (1)", "---", "expected to contain at least", "but was");
    assertFailureValue("missing (1)", "4");
    assertFailureValue("expected to contain at least", "[1, 2, 4]");
  }

  @Test
  public void iterableContainsAllOfWithExtras() {
    expectFailureWhenTestingThat(asList("y", "x")).containsAllOf("x", "y", "z");
    assertFailureValue("missing (1)", "z");
  }

  @Test
  public void iterableContainsAllOfWithExtraCopiesOfOutOfOrder() {
    expectFailureWhenTestingThat(asList("y", "x")).containsAllOf("x", "y", "y");
    assertFailureValue("missing (1)", "y");
  }

  @Test
  public void iterableContainsAllOfWithDuplicatesFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsAllOf(1, 2, 2, 2, 3, 4);
    assertFailureValue("missing (3)", "2 [2 copies], 4");
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void iterableContainsAllOfWithDuplicateMissingElements() {
    expectFailureWhenTestingThat(asList(1, 2)).containsAllOf(4, 4, 4);
    assertFailureValue("missing (3)", "4 [3 copies]");
  }

  @Test
  public void iterableContainsAllOfWithNullFailure() {
    expectFailureWhenTestingThat(asList(1, null, 3)).containsAllOf(1, null, null, 3);
    assertFailureValue("missing (1)", "null");
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousList() {
    expectFailureWhenTestingThat(asList(1L, 2L)).containsAllOf(1, 2);
    assertFailureValue("missing (2)", "1, 2 (java.lang.Integer)");
    assertFailureValue("though it did contain (2)", "1, 2 (java.lang.Long)");
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    expectFailureWhenTestingThat(asList(1L, 2L, 2L)).containsAllOf(1, 1, 2);
    assertFailureValue("missing (3)", "1 [2 copies], 2 (java.lang.Integer)");
    assertFailureValue("though it did contain (3)", "1, 2 [2 copies] (java.lang.Long)");
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHomogeneousListWithNull() {
    expectFailureWhenTestingThat(asList("null", "abc")).containsAllOf("abc", null);
    assertFailureValue("missing (1)", "null (null type)");
    assertFailureValue("though it did contain (1)", "null (java.lang.String)");
  }

  @Test
  public void iterableContainsAllOfFailsWithSameToStringAndHeterogeneousListWithDuplicates() {
    expectFailureWhenTestingThat(asList(1, 2, 2L, 3L, 3L)).containsAllOf(2L, 2L, 3, 3);
    assertFailureValue("missing (3)", "2 (java.lang.Long), 3 (java.lang.Integer) [2 copies]");
    assertFailureValue(
        "though it did contain (3)", "2 (java.lang.Integer), 3 (java.lang.Long) [2 copies]");
  }

  @Test
  public void iterableContainsAllOfFailsWithEmptyString() {
    expectFailureWhenTestingThat(asList("a", null)).containsAllOf("", null);

    assertFailureKeys("missing (1)", "---", "expected to contain at least", "but was");
    assertFailureValue("missing (1)", "");
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
    expectFailureWhenTestingThat(asList(1, null, 3)).containsAllOf(null, 1, 3).inOrder();
    assertFailureKeys(
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue("expected order for required elements", "[null, 1, 3]");
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

    expectFailureWhenTestingThat(iterable).containsAllOf(1, 3, (Object) null).inOrder();
    assertFailureKeys(
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue("expected order for required elements", "[1, 3, null]");
  }

  @Test
  public void iterableContainsAllOfInOrderWrongOrderAndMissing() {
    expectFailureWhenTestingThat(asList(1, 2)).containsAllOf(2, 1, 3).inOrder();
  }

  @Test
  public void iterableContainsAllInIterable() {
    assertThat(asList(1, 2, 3)).containsAllIn(asList(1, 2));

    expectFailureWhenTestingThat(asList(1, 2, 3)).containsAllIn(asList(1, 2, 4));
    assertFailureKeys("missing (1)", "---", "expected to contain at least", "but was");
    assertFailureValue("missing (1)", "4");
    assertFailureValue("expected to contain at least", "[1, 2, 4]");
  }

  @Test
  public void iterableContainsAllInCanUseFactPerElement() {
    expectFailureWhenTestingThat(asList("abc")).containsAllIn(asList("123\n456", "789"));
    assertFailureKeys("missing (2)", "#1", "#2", "---", "expected to contain at least", "but was");
    assertFailureValue("#1", "123\n456");
    assertFailureValue("#2", "789");
  }

  @Test
  public void iterableContainsAllInArray() {
    assertThat(asList(1, 2, 3)).containsAllIn(new Integer[] {1, 2});

    expectFailureWhenTestingThat(asList(1, 2, 3)).containsAllIn(new Integer[] {1, 2, 4});
    assertFailureKeys("missing (1)", "---", "expected to contain at least", "but was");
    assertFailureValue("missing (1)", "4");
    assertFailureValue("expected to contain at least", "[1, 2, 4]");
  }

  @Test
  public void iterableContainsNoneOf() {
    assertThat(asList(1, 2, 3)).containsNoneOf(4, 5, 6);
  }

  @Test
  public void iterableContainsNoneOfFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsNoneOf(1, 2, 4);
    assertFailureKeys("expected not to contain any of", "but contained", "full contents");
    assertFailureValue("expected not to contain any of", "[1, 2, 4]");
    assertFailureValue("but contained", "[1, 2]");
    assertFailureValue("full contents", "[1, 2, 3]");
  }

  @Test
  public void iterableContainsNoneOfFailureWithDuplicateInSubject() {
    expectFailureWhenTestingThat(asList(1, 2, 2, 3)).containsNoneOf(1, 2, 4);
    assertFailureValue("but contained", "[1, 2]");
  }

  @Test
  public void iterableContainsNoneOfFailureWithDuplicateInExpected() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsNoneOf(1, 2, 2, 4);
    assertFailureValue("but contained", "[1, 2]");
  }

  @Test
  public void iterableContainsNoneOfFailureWithEmptyString() {
    expectFailureWhenTestingThat(asList("")).containsNoneOf("", null);
    assertFailureKeys("expected not to contain any of", "but contained", "full contents");
    assertFailureValue("expected not to contain any of", "[\"\" (empty String), null]");
    assertFailureValue("but contained", "[\"\" (empty String)]");
    assertFailureValue("full contents", "[]");
  }

  @Test
  public void iterableContainsNoneInIterable() {
    assertThat(asList(1, 2, 3)).containsNoneIn(asList(4, 5, 6));
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsNoneIn(asList(1, 2, 4));
    assertFailureKeys("expected not to contain any of", "but contained", "full contents");
    assertFailureValue("expected not to contain any of", "[1, 2, 4]");
    assertFailureValue("but contained", "[1, 2]");
    assertFailureValue("full contents", "[1, 2, 3]");
  }

  @Test
  public void iterableContainsNoneInArray() {
    assertThat(asList(1, 2, 3)).containsNoneIn(new Integer[] {4, 5, 6});
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsNoneIn(new Integer[] {1, 2, 4});
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
  public void iterableContainsExactlyOutOfOrderDoesNotStringify() {
    CountsToStringCalls o = new CountsToStringCalls();
    List<Object> actual = asList(o, 1);
    List<Object> expected = asList(1, o);
    assertThat(actual).containsExactlyElementsIn(expected);
    assertThat(o.calls).isEqualTo(0);
    expectFailureWhenTestingThat(actual).containsExactlyElementsIn(expected).inOrder();
    assertThat(o.calls).isGreaterThan(0);
  }

  @Test
  public void iterableContainsExactlyWithEmptyString() {
    expectFailureWhenTestingThat(asList()).containsExactly("");

    assertFailureValue("missing (1)", "");
  }

  @Test
  public void iterableContainsExactlyWithEmptyStringAndUnexpectedItem() {
    expectFailureWhenTestingThat(asList("a", null)).containsExactly("");

    assertFailureKeys("missing (1)", "unexpected (2)", "---", "expected", "but was");
    assertFailureValue("missing (1)", "");
    assertFailureValue("unexpected (2)", "a, null");
  }

  @Test
  public void iterableContainsExactlyWithEmptyStringAndMissingItem() {
    expectFailureWhenTestingThat(asList("")).containsExactly("a", null);

    assertFailureValue("missing (2)", "a, null");
    assertFailureValue("unexpected (1)", "");
  }

  @Test
  public void iterableContainsExactlyWithEmptyStringAmongMissingItems() {
    expectFailureWhenTestingThat(asList("a")).containsExactly("", "b");

    assertFailureKeys(
        "missing (2)", "#1", "#2", "", "unexpected (1)", "#1", "---", "expected", "but was");
    assertFailureValueIndexed("#1", 0, "");
    assertFailureValueIndexed("#2", 0, "b");
    assertFailureValueIndexed("#1", 1, "a");
  }

  @Test
  public void iterableContainsExactlySingleElement() {
    assertThat(asList(1)).containsExactly(1);

    expectFailureWhenTestingThat(asList(1)).containsExactly(2);
    assertFailureKeys("value of", "expected", "but was");
    assertFailureValue("value of", "iterable.onlyElement()");
  }

  @Test
  public void iterableContainsExactlySingleElementNoEqualsMagic() {
    expectFailureWhenTestingThat(asList(1)).containsExactly(1L);
    assertFailureValueIndexed("an instance of", 0, "java.lang.Long");
  }

  @Test
  public void iterableContainsExactlyWithElementsThatThrowWhenYouCallHashCode() {
    HashCodeThrower one = new HashCodeThrower();
    HashCodeThrower two = new HashCodeThrower();

    assertThat(asList(one, two)).containsExactly(two, one);
    assertThat(asList(one, two)).containsExactly(one, two).inOrder();
    assertThat(asList(one, two)).containsExactlyElementsIn(asList(two, one));
    assertThat(asList(one, two)).containsExactlyElementsIn(asList(one, two)).inOrder();

    expectFailureWhenTestingThat(asList(one, two)).containsExactly(one);
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
    expectFailureWhenTestingThat(asList("foo")).containsExactlyElementsIn(ImmutableList.of());
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void iterableContainsExactlyElementsInErrorMessageIsOrdered() {
    expectFailureWhenTestingThat(asList("foo OR bar"))
        .containsExactlyElementsIn(asList("foo", "bar"));
    assertFailureValue("missing (2)", "foo, bar");
    assertFailureValue("unexpected (1)", "foo OR bar");
  }

  @Test
  public void iterableContainsExactlyMissingItemFailure() {
    expectFailureWhenTestingThat(asList(1, 2)).containsExactly(1, 2, 4);
    assertFailureValue("missing (1)", "4");
  }

  @Test
  public void iterableContainsExactlyUnexpectedItemFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsExactly(1, 2);
    assertFailureValue("unexpected (1)", "3");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesNotEnoughItemsFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsExactly(1, 2, 2, 2, 3);
    assertFailureValue("missing (2)", "2 [2 copies]");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesMissingItemFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsExactly(1, 2, 2, 2, 3, 4);
    assertFailureValue("missing (3)", "2 [2 copies], 4");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesMissingItemsWithNewlineFailure() {
    expectFailureWhenTestingThat(asList("a", "b", "foo\nbar"))
        .containsExactly("a", "b", "foo\nbar", "foo\nbar", "foo\nbar");
    assertFailureKeys("missing (2)", "#1 [2 copies]", "---", "expected", "but was");
    assertFailureValue("#1 [2 copies]", "foo\nbar");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesMissingAndExtraItemsWithNewlineFailure() {
    expectFailureWhenTestingThat(asList("a\nb", "a\nb")).containsExactly("foo\nbar", "foo\nbar");
    assertFailureKeys(
        "missing (2)",
        "#1 [2 copies]",
        "",
        "unexpected (2)",
        "#1 [2 copies]",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed("#1 [2 copies]", 0, "foo\nbar");
    assertFailureValueIndexed("#1 [2 copies]", 1, "a\nb");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesUnexpectedItemFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 2, 2, 2, 3)).containsExactly(1, 2, 2, 3);
    assertFailureValue("unexpected (2)", "2 [2 copies]");
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void iterableContainsExactlyWithDuplicateMissingElements() {
    expectFailureWhenTestingThat(asList()).containsExactly(4, 4, 4);
    assertFailureValue("missing (3)", "4 [3 copies]");
  }

  @Test
  public void iterableContainsExactlyWithNullFailure() {
    expectFailureWhenTestingThat(asList(1, null, 3)).containsExactly(1, null, null, 3);
    assertFailureValue("missing (1)", "null");
  }

  @Test
  public void iterableContainsExactlyWithMissingAndExtraElements() {
    expectFailureWhenTestingThat(asList(1, 2, 3)).containsExactly(1, 2, 4);
    assertFailureValue("missing (1)", "4");
    assertFailureValue("unexpected (1)", "3");
  }

  @Test
  public void iterableContainsExactlyWithDuplicateMissingAndExtraElements() {
    expectFailureWhenTestingThat(asList(1, 2, 3, 3)).containsExactly(1, 2, 4, 4);
    assertFailureValue("missing (2)", "4 [2 copies]");
    assertFailureValue("unexpected (2)", "3 [2 copies]");
  }

  @Test
  public void iterableContainsExactlyWithCommaSeparatedVsIndividual() {
    expectFailureWhenTestingThat(asList("a, b")).containsExactly("a", "b");
    assertFailureKeys(
        "missing (2)", "#1", "#2", "", "unexpected (1)", "#1", "---", "expected", "but was");
    assertFailureValueIndexed("#1", 0, "a");
    assertFailureValueIndexed("#2", 0, "b");
    assertFailureValueIndexed("#1", 1, "a, b");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHomogeneousList() {
    expectFailureWhenTestingThat(asList(1L, 2L)).containsExactly(1, 2);
    assertFailureValue("missing (2)", "1, 2 (java.lang.Integer)");
    assertFailureValue("unexpected (2)", "1, 2 (java.lang.Long)");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndListWithNull() {
    expectFailureWhenTestingThat(asList(1L, 2L)).containsExactly(null, 1, 2);
    assertFailureValue(
        "missing (3)", "null (null type), 1 (java.lang.Integer), 2 (java.lang.Integer)");
    assertFailureValue("unexpected (2)", "1, 2 (java.lang.Long)");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHeterogeneousList() {
    expectFailureWhenTestingThat(asList(1L, 2)).containsExactly(1, null, 2L);
    assertFailureValue(
        "missing (3)", "1 (java.lang.Integer), null (null type), 2 (java.lang.Long)");
    assertFailureValue("unexpected (2)", "1 (java.lang.Long), 2 (java.lang.Integer)");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    expectFailureWhenTestingThat(asList(1L, 2L)).containsExactly(1, 2, 2);
    assertFailureValue("missing (3)", "1, 2 [2 copies] (java.lang.Integer)");
    assertFailureValue("unexpected (2)", "1, 2 (java.lang.Long)");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHeterogeneousListWithDuplicates() {
    expectFailureWhenTestingThat(asList(1L, 2)).containsExactly(1, null, null, 2L, 2L);
    assertFailureValue(
        "missing (5)",
        "1 (java.lang.Integer), null (null type) [2 copies], 2 (java.lang.Long) [2 copies]");
    assertFailureValue("unexpected (2)", "1 (java.lang.Long), 2 (java.lang.Integer)");
  }

  @Test
  public void iterableContainsExactlyWithOneIterableGivesWarning() {
    expectFailureWhenTestingThat(asList(1, 2, 3, 4)).containsExactly(asList(1, 2, 3, 4));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains(CONTAINS_EXACTLY_ITERABLE_WARNING);
  }

  @Test
  public void iterableContainsExactlyElementsInWithOneIterableDoesNotGiveWarning() {
    expectFailureWhenTestingThat(asList(1, 2, 3, 4)).containsExactlyElementsIn(asList(1, 2, 3));
    assertFailureValue("unexpected (1)", "4");
  }

  @Test
  public void iterableContainsExactlyWithTwoIterableDoesNotGivesWarning() {
    expectFailureWhenTestingThat(asList(1, 2, 3, 4)).containsExactly(asList(1, 2), asList(3, 4));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .doesNotContain(CONTAINS_EXACTLY_ITERABLE_WARNING);
  }

  private static final String CONTAINS_EXACTLY_ITERABLE_WARNING =
      "Passing an iterable to the varargs method containsExactly(Object...) is "
          + "often not the correct thing to do. Did you mean to call "
          + "containsExactlyElementsIn(Iterable) instead?";

  @Test
  public void iterableContainsExactlyWithOneNonIterableDoesNotGiveWarning() {
    expectFailureWhenTestingThat(asList(1, 2, 3, 4)).containsExactly(1);
    assertFailureValue("unexpected (3)", "2, 3, 4");
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
    expectFailureWhenTestingThat(asList(1, null, 3)).containsExactly(null, 1, 3).inOrder();
    assertFailureKeys("contents match, but order was wrong", "expected", "but was");
    assertFailureValue("expected", "[null, 1, 3]");
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

    expectFailureWhenTestingThat(iterable).containsExactly(1, 3, null).inOrder();
    assertFailureKeys("contents match, but order was wrong", "expected", "but was");
    assertFailureValue("expected", "[1, 3, null]");
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

    expectFailureWhenTestingThat(iterable).containsExactly(1, 2).inOrder();
    assertFailureValue("but was", "[1, 2, 3]");
  }

  @Test
  public void iterableContainsExactlyElementsInIterable() {
    assertThat(asList(1, 2)).containsExactlyElementsIn(asList(1, 2));

    expectFailureWhenTestingThat(asList(1, 2)).containsExactlyElementsIn(asList(1, 2, 4));
    assertFailureValue("missing (1)", "4");
  }

  @Test
  public void iterableContainsExactlyElementsInArray() {
    assertThat(asList(1, 2)).containsExactlyElementsIn(new Integer[] {1, 2});

    expectFailureWhenTestingThat(asList(1, 2)).containsExactlyElementsIn(new Integer[] {1, 2, 4});
    assertFailureValue("missing (1)", "4");
  }

  @Test
  public void iterableIsEmpty() {
    assertThat(asList()).isEmpty();
  }

  @Test
  public void iterableIsEmptyWithFailure() {
    expectFailureWhenTestingThat(asList(1, null, 3)).isEmpty();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void iterableIsNotEmpty() {
    assertThat(asList("foo")).isNotEmpty();
  }

  @Test
  public void iterableIsNotEmptyWithFailure() {
    expectFailureWhenTestingThat(asList()).isNotEmpty();
    assertFailureKeys("expected not to be empty");
  }

  @Test
  public void iterableIsStrictlyOrdered() {
    assertThat(asList()).isStrictlyOrdered();
    assertThat(asList(1)).isStrictlyOrdered();
    assertThat(asList(1, 2, 3, 4)).isStrictlyOrdered();
  }

  @Test
  public void isStrictlyOrderedFailure() {
    expectFailureWhenTestingThat(asList(1, 2, 2, 4)).isStrictlyOrdered();
    assertFailureKeys(
        "expected to be strictly ordered", "but contained", "followed by", "full contents");
    assertFailureValue("but contained", "2");
    assertFailureValue("followed by", "2");
    assertFailureValue("full contents", "[1, 2, 2, 4]");
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
    expectFailureWhenTestingThat(asList(1, 3, 2, 4)).isOrdered();
    assertFailureKeys("expected to be ordered", "but contained", "followed by", "full contents");
    assertFailureValue("but contained", "3");
    assertFailureValue("followed by", "2");
    assertFailureValue("full contents", "[1, 3, 2, 4]");
  }

  @Test
  public void isOrderedMultipleFailures() {
    expectFailureWhenTestingThat(asList(1, 3, 2, 4, 0)).isOrdered();
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
    expectFailureWhenTestingThat(asList("1", "2", "2", "10")).isStrictlyOrdered(COMPARE_AS_DECIMAL);
    assertFailureKeys(
        "expected to be strictly ordered", "but contained", "followed by", "full contents");
    assertFailureValue("but contained", "2");
    assertFailureValue("followed by", "2");
    assertFailureValue("full contents", "[1, 2, 2, 10]");
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
    expectFailureWhenTestingThat(asList("1", "10", "2", "20")).isOrdered(COMPARE_AS_DECIMAL);
    assertFailureKeys("expected to be ordered", "but contained", "followed by", "full contents");
    assertFailureValue("but contained", "10");
    assertFailureValue("followed by", "2");
    assertFailureValue("full contents", "[1, 10, 2, 20]");
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

    assertThat(actual).isNotIn(ImmutableList.of(ImmutableList.of("b"), ImmutableList.of("c")));

    expectFailureWhenTestingThat(actual).isNotIn(ImmutableList.of("a", "b"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "The actual value is an Iterable, and you've written a test that compares it to some "
                + "objects that are not Iterables. Did you instead mean to check whether its "
                + "*contents* match any of the *contents* of the given values? If so, call "
                + "containsNoneOf(...)/containsNoneIn(...) instead. Non-iterables: [a, b]");
  }

  @Test
  public void isAnyOf() {
    ImmutableList<String> actual = ImmutableList.of("a");
    ImmutableList<String> expectedA = ImmutableList.of("a");
    ImmutableList<String> expectedB = ImmutableList.of("b");

    assertThat(actual).isAnyOf(expectedA, expectedB);
  }

  @Test
  @SuppressWarnings("IncompatibleArgumentType")
  public void isNoneOf() {
    ImmutableList<String> actual = ImmutableList.of("a");

    assertThat(actual).isNoneOf(ImmutableList.of("b"), ImmutableList.of("c"));

    expectFailureWhenTestingThat(actual).isNoneOf("a", "b");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "The actual value is an Iterable, and you've written a test that compares it to some "
                + "objects that are not Iterables. Did you instead mean to check whether its "
                + "*contents* match any of the *contents* of the given values? If so, call "
                + "containsNoneOf(...)/containsNoneIn(...) instead. Non-iterables: [a, b]");
  }

  private static final class CountsToStringCalls {
    int calls;

    @Override
    public String toString() {
      calls++;
      return super.toString();
    }
  }

  private IterableSubject expectFailureWhenTestingThat(Iterable<?> actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
