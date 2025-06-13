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

import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.FailureAssertions.assertFailureValueIndexed;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.jspecify.annotations.Nullable;
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
@SuppressWarnings({
  // "Iterable" is specific enough to establish that we're testing IterableSubject.
  "PreferredInterfaceType",
  // We intentionally test mismatches.
  // TODO(cpovirk): Maybe suppress at a finer scope.
  "TruthIncompatibleType",
})
public class IterableSubjectTest {

  @Test
  public void hasSize() {
    assertThat(ImmutableList.of(1, 2, 3)).hasSize(3);
  }

  @Test
  @SuppressWarnings({"TruthIterableIsEmpty", "IsEmptyTruth"})
  public void hasSizeZero() {
    assertThat(ImmutableList.of()).hasSize(0);
  }

  @Test
  public void hasSizeFails() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(ImmutableList.of(1, 2, 3)).hasSize(4));
    assertFailureValue(e, "value of", "iterable.size()");
  }

  @Test
  public void hasSizeNegative() {
    assertThrows(
        IllegalArgumentException.class, () -> assertThat(ImmutableList.of(1, 2, 3)).hasSize(-1));
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
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1L, 2L, 3L, 2L)).contains(2));
    assertFailureKeys(
        e,
        "expected to contain",
        "an instance of",
        "but did not",
        "though it did contain",
        "full contents");
    assertFailureValue(e, "expected to contain", "2");
    assertFailureValue(e, "an instance of", "java.lang.Integer");
    assertFailureValue(e, "though it did contain", "[2 [2 copies]] (java.lang.Long)");
    assertFailureValue(e, "full contents", "[1, 2, 3, 2]");
  }

  @Test
  public void iterableContainsFailsWithSameToStringAndNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, "null")).contains(null));
    assertFailureValue(e, "an instance of", "null type");
  }

  @Test
  public void iterableContainsFailure() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3)).contains(5));
    assertFailureKeys(e, "expected to contain", "but was");
    assertFailureValue(e, "expected to contain", "5");
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
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3)).doesNotContain(2));
    assertFailureKeys(e, "expected not to contain", "but was");
    assertFailureValue(e, "expected not to contain", "2");
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
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 2, 3)).containsNoDuplicates());
    assertFailureKeys(e, "expected not to contain duplicates", "but contained", "full contents");
    assertFailureValue(e, "but contained", "[2 x 2]");
    assertFailureValue(e, "full contents", "[1, 2, 2, 3]");
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
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3)).containsAnyOf(5, 6, 0));
    assertFailureKeys(e, "expected to contain any of", "but was");
    assertFailureValue(e, "expected to contain any of", "[5, 6, 0]");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndHomogeneousList() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1L, 2L, 3L)).containsAnyOf(2, 3));
    assertFailureKeys(
        e, "expected to contain any of", "but did not", "though it did contain", "full contents");
    assertFailureValue(e, "expected to contain any of", "[2, 3] (java.lang.Integer)");
    assertFailureValue(e, "though it did contain", "[2, 3] (java.lang.Long)");
    assertFailureValue(e, "full contents", "[1, 2, 3]");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(3L, 3L)).containsAnyOf(2, 3, 3));
    assertFailureKeys(
        e, "expected to contain any of", "but did not", "though it did contain", "full contents");
    assertFailureValue(e, "expected to contain any of", "[2, 3 [2 copies]] (java.lang.Integer)");
    assertFailureValue(e, "though it did contain", "[3 [2 copies]] (java.lang.Long)");
    assertFailureValue(e, "full contents", "[3, 3]");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndNullInSubject() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(null, "abc")).containsAnyOf("def", "null"));
    assertFailureKeys(
        e, "expected to contain any of", "but did not", "though it did contain", "full contents");
    assertFailureValue(e, "expected to contain any of", "[def, null] (java.lang.String)");
    assertFailureValue(e, "though it did contain", "[null (null type)]");
    assertFailureValue(e, "full contents", "[null, abc]");
  }

  @Test
  public void iterableContainsAnyOfFailsWithSameToStringAndNullInExpectation() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList("null", "abc")).containsAnyOf("def", null));
    assertFailureKeys(
        e, "expected to contain any of", "but did not", "though it did contain", "full contents");
    assertFailureValue(
        e, "expected to contain any of", "[def (java.lang.String), null (null type)]");
    assertFailureValue(e, "though it did contain", "[null] (java.lang.String)");
    assertFailureValue(e, "full contents", "[null, abc]");
  }

  @Test
  public void iterableContainsAnyOfWithOneShotIterable() {
    List<Object> contents = asList(2, 1, "b");
    Iterable<Object> oneShot = new OneShotIterable<>(contents.iterator(), "OneShotIterable");

    assertThat(oneShot).containsAnyOf(3, "a", 7, "b", 0);
  }

  @Test
  public void iterableContainsAnyInIterable() {
    assertThat(asList(1, 2, 3)).containsAnyIn(asList(1, 10, 100));

    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, 2, 3)).containsAnyIn(asList(5, 6, 0)));
    assertFailureKeys(e, "expected to contain any of", "but was");
    assertFailureValue(e, "expected to contain any of", "[5, 6, 0]");
  }

  @Test
  public void iterableContainsAnyInArray() {
    assertThat(asList(1, 2, 3)).containsAnyIn(new Integer[] {1, 10, 100});

    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2, 3)).containsAnyIn(new Integer[] {5, 6, 0}));
    assertFailureKeys(e, "expected to contain any of", "but was");
    assertFailureValue(e, "expected to contain any of", "[5, 6, 0]");
  }

  @Test
  public void iterableContainsAtLeast() {
    assertThat(asList(1, 2, 3)).containsAtLeast(1, 2);
  }

  @Test
  public void iterableContainsAtLeastWithMany() {
    assertThat(asList(1, 2, 3)).containsAtLeast(1, 2);
  }

  @Test
  public void iterableContainsAtLeastWithDuplicates() {
    assertThat(asList(1, 2, 2, 2, 3)).containsAtLeast(2, 2);
  }

  @Test
  public void iterableContainsAtLeastWithNull() {
    assertThat(asList(1, null, 3)).containsAtLeast(3, (Integer) null);
  }

  @Test
  public void iterableContainsAtLeastWithNullAtThirdAndFinalPosition() {
    assertThat(asList(1, null, 3)).containsAtLeast(1, 3, (Object) null);
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
  public void iterableContainsAtLeastElementsInOutOfOrderDoesNotStringify() {
    CountsToStringCalls o = new CountsToStringCalls();
    List<Object> actual = asList(o, 1);
    List<Object> expected = asList(1, o);
    assertThat(actual).containsAtLeastElementsIn(expected);
    assertThat(o.calls).isEqualTo(0);
    expectFailure(
        whenTesting -> whenTesting.that(actual).containsAtLeastElementsIn(expected).inOrder());
    assertThat(o.calls).isGreaterThan(0);
  }

  @Test
  public void iterableContainsAtLeastFailure() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3)).containsAtLeast(1, 2, 4));
    assertFailureKeys(e, "missing (1)", "---", "expected to contain at least", "but was");
    assertFailureValue(e, "missing (1)", "4");
    assertFailureValue(e, "expected to contain at least", "[1, 2, 4]");
  }

  @Test
  public void iterableContainsAtLeastWithExtras() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList("y", "x")).containsAtLeast("x", "y", "z"));
    assertFailureValue(e, "missing (1)", "z");
  }

  @Test
  public void iterableContainsAtLeastWithExtraCopiesOfOutOfOrder() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList("y", "x")).containsAtLeast("x", "y", "y"));
    assertFailureValue(e, "missing (1)", "y");
  }

  @Test
  public void iterableContainsAtLeastWithDuplicatesFailure() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, 2, 3)).containsAtLeast(1, 2, 2, 2, 3, 4));
    assertFailureValue(e, "missing (3)", "2 [2 copies], 4");
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void iterableContainsAtLeastWithDuplicateMissingElements() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2)).containsAtLeast(4, 4, 4));
    assertFailureValue(e, "missing (3)", "4 [3 copies]");
  }

  @Test
  public void iterableContainsAtLeastWithNullFailure() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, null, 3)).containsAtLeast(1, null, null, 3));
    assertFailureValue(e, "missing (1)", "null");
  }

  @Test
  public void iterableContainsAtLeastFailsWithSameToStringAndHomogeneousList() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1L, 2L)).containsAtLeast(1, 2));
    assertFailureValue(e, "missing (2)", "1, 2 (java.lang.Integer)");
    assertFailureValue(e, "though it did contain (2)", "1, 2 (java.lang.Long)");
  }

  @Test
  public void iterableContainsAtLeastFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1L, 2L, 2L)).containsAtLeast(1, 1, 2));
    assertFailureValue(e, "missing (3)", "1 [2 copies], 2 (java.lang.Integer)");
    assertFailureValue(e, "though it did contain (3)", "1, 2 [2 copies] (java.lang.Long)");
  }

  @Test
  public void iterableContainsAtLeastFailsWithSameToStringAndHomogeneousListWithNull() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList("null", "abc")).containsAtLeast("abc", null));
    assertFailureValue(e, "missing (1)", "null (null type)");
    assertFailureValue(e, "though it did contain (1)", "null (java.lang.String)");
  }

  @Test
  public void iterableContainsAtLeastFailsWithSameToStringAndHeterogeneousListWithDuplicates() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2, 2L, 3L, 3L)).containsAtLeast(2L, 2L, 3, 3));
    assertFailureValue(e, "missing (3)", "2 (java.lang.Long), 3 (java.lang.Integer) [2 copies]");
    assertFailureValue(
        e, "though it did contain (3)", "2 (java.lang.Integer), 3 (java.lang.Long) [2 copies]");
  }

  @Test
  public void iterableContainsAtLeastFailsWithEmptyString() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList("a", null)).containsAtLeast("", null));

    assertFailureKeys(e, "missing (1)", "---", "expected to contain at least", "but was");
    assertFailureValue(e, "missing (1)", "");
  }

  @Test
  public void iterableContainsAtLeastInOrder() {
    assertThat(asList(3, 2, 5)).containsAtLeast(3, 2, 5).inOrder();
  }

  @Test
  public void iterableContainsAtLeastInOrderWithGaps() {
    assertThat(asList(3, 2, 5)).containsAtLeast(3, 5).inOrder();
    assertThat(asList(3, 2, 2, 4, 5)).containsAtLeast(3, 2, 2, 5).inOrder();
    assertThat(asList(3, 1, 4, 1, 5)).containsAtLeast(3, 1, 5).inOrder();
    assertThat(asList("x", "y", "y", "z")).containsAtLeast("x", "y", "z").inOrder();
    assertThat(asList("x", "x", "y", "z")).containsAtLeast("x", "y", "z").inOrder();
    assertThat(asList("z", "x", "y", "z")).containsAtLeast("x", "y", "z").inOrder();
    assertThat(asList("x", "x", "y", "z", "x")).containsAtLeast("x", "y", "z", "x").inOrder();
  }

  @Test
  public void iterableContainsAtLeastInOrderWithNull() {
    assertThat(asList(3, null, 5)).containsAtLeast(3, null, 5).inOrder();
    assertThat(asList(3, null, 7, 5)).containsAtLeast(3, null, 5).inOrder();
  }

  @Test
  public void iterableContainsAtLeastInOrderWithFailure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, null, 3)).containsAtLeast(null, 1, 3).inOrder());
    assertFailureKeys(
        e,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue(e, "expected order for required elements", "[null, 1, 3]");
    assertFailureValue(e, "but was", "[1, null, 3]");
  }

  @Test
  public void iterableContainsAtLeastInOrderWithFailureWithActualOrder() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2, null, 3, 4)).containsAtLeast(null, 1, 3).inOrder());
    assertFailureKeys(
        e,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but order was",
        "full contents");
    assertFailureValue(e, "expected order for required elements", "[null, 1, 3]");
    assertFailureValue(e, "but order was", "[1, null, 3]");
    assertFailureValue(e, "full contents", "[1, 2, null, 3, 4]");
  }

  @Test
  public void iterableContainsAtLeastInOrderWithOneShotIterable() {
    List<Object> contents = asList(2, 1, null, 4, "a", 3, "b");
    Iterable<Object> oneShot = new OneShotIterable<>(contents.iterator(), contents.toString());

    assertThat(oneShot).containsAtLeast(1, null, 3).inOrder();
  }

  @Test
  public void iterableContainsAtLeastInOrderWithOneShotIterableWrongOrder() {
    List<Object> contents = asList(2, 1, null, 4, "a", 3, "b");
    Iterable<Object> oneShot = new OneShotIterable<>(contents.iterator(), "BadIterable");

    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(oneShot).containsAtLeast(1, 3, (Object) null).inOrder());
    assertFailureKeys(
        e,
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "but was");
    assertFailureValue(e, "expected order for required elements", "[1, 3, null]");
    assertFailureValue(e, "but was", "BadIterable"); // TODO(b/231966021): Output its elements.
  }

  private static final class OneShotIterable<E> implements Iterable<E> {
    private final Iterator<E> iterator;
    private final String toString;

    OneShotIterable(Iterator<E> iterator, String toString) {
      this.iterator = iterator;
      this.toString = toString;
    }

    @Override
    public Iterator<E> iterator() {
      return iterator;
    }

    @Override
    public String toString() {
      return toString;
    }
  }

  @Test
  public void iterableContainsAtLeastInOrderWrongOrderAndMissing() {
    expectFailure(whenTesting -> whenTesting.that(asList(1, 2)).containsAtLeast(2, 1, 3).inOrder());
  }

  @Test
  @SuppressWarnings("ContainsAllElementsInWithVarArgsToContainsAtLeast")
  public void iterableContainsAtLeastElementsInIterable() {
    assertThat(asList(1, 2, 3)).containsAtLeastElementsIn(asList(1, 2));

    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2, 3)).containsAtLeastElementsIn(asList(1, 2, 4)));
    assertFailureKeys(e, "missing (1)", "---", "expected to contain at least", "but was");
    assertFailureValue(e, "missing (1)", "4");
    assertFailureValue(e, "expected to contain at least", "[1, 2, 4]");
  }

  @Test
  @SuppressWarnings("ContainsAllElementsInWithVarArgsToContainsAtLeast")
  public void iterableContainsAtLeastElementsInCanUseFactPerElement() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(asList("abc"))
                    .containsAtLeastElementsIn(asList("123\n456", "789")));
    assertFailureKeys(
        e, "missing (2)", "#1", "#2", "---", "expected to contain at least", "but was");
    assertFailureValue(e, "#1", "123\n456");
    assertFailureValue(e, "#2", "789");
  }

  @Test
  public void iterableContainsAtLeastElementsInArray() {
    assertThat(asList(1, 2, 3)).containsAtLeastElementsIn(new Integer[] {1, 2});

    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(asList(1, 2, 3))
                    .containsAtLeastElementsIn(new Integer[] {1, 2, 4}));
    assertFailureKeys(e, "missing (1)", "---", "expected to contain at least", "but was");
    assertFailureValue(e, "missing (1)", "4");
    assertFailureValue(e, "expected to contain at least", "[1, 2, 4]");
  }

  @Test
  public void iterableContainsNoneOf() {
    assertThat(asList(1, 2, 3)).containsNoneOf(4, 5, 6);
  }

  @Test
  public void iterableContainsNoneOfFailure() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3)).containsNoneOf(1, 2, 4));
    assertFailureKeys(e, "expected not to contain any of", "but contained", "full contents");
    assertFailureValue(e, "expected not to contain any of", "[1, 2, 4]");
    assertFailureValue(e, "but contained", "[1, 2]");
    assertFailureValue(e, "full contents", "[1, 2, 3]");
  }

  @Test
  public void iterableContainsNoneOfFailureWithDuplicateInSubject() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 2, 3)).containsNoneOf(1, 2, 4));
    assertFailureValue(e, "but contained", "[1, 2]");
  }

  @Test
  public void iterableContainsNoneOfFailureWithDuplicateInExpected() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3)).containsNoneOf(1, 2, 2, 4));
    assertFailureValue(e, "but contained", "[1, 2]");
  }

  @Test
  public void iterableContainsNoneOfFailureWithEmptyString() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList("")).containsNoneOf("", null));
    assertFailureKeys(e, "expected not to contain any of", "but contained", "full contents");
    assertFailureValue(e, "expected not to contain any of", "[\"\" (empty String), null]");
    assertFailureValue(e, "but contained", "[\"\" (empty String)]");
    assertFailureValue(e, "full contents", "[]");
  }

  @Test
  @SuppressWarnings("ContainsNoneInWithVarArgsToContainsNoneOf")
  public void iterableContainsNoneInIterable() {
    assertThat(asList(1, 2, 3)).containsNoneIn(asList(4, 5, 6));
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, 2, 3)).containsNoneIn(asList(1, 2, 4)));
    assertFailureKeys(e, "expected not to contain any of", "but contained", "full contents");
    assertFailureValue(e, "expected not to contain any of", "[1, 2, 4]");
    assertFailureValue(e, "but contained", "[1, 2]");
    assertFailureValue(e, "full contents", "[1, 2, 3]");
  }

  @Test
  public void iterableContainsNoneInArray() {
    assertThat(asList(1, 2, 3)).containsNoneIn(new Integer[] {4, 5, 6});
    expectFailure(
        whenTesting -> whenTesting.that(asList(1, 2, 3)).containsNoneIn(new Integer[] {1, 2, 4}));
  }

  @Test
  public void iterableContainsExactlyArray() {
    String[] stringArray = {"a", "b"};
    ImmutableList<String[]> iterable = ImmutableList.of(stringArray);
    // This test fails w/o the explicit cast
    assertThat(iterable).containsExactly((Object) stringArray);
  }

  @Test
  // We tell people to call containsExactlyElementsIn, but we still test containsExactly.
  @SuppressWarnings("ContainsExactlyVariadic")
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
    expectFailure(
        whenTesting -> whenTesting.that(actual).containsExactlyElementsIn(expected).inOrder());
    assertThat(o.calls).isGreaterThan(0);
  }

  @Test
  public void iterableContainsExactlyWithEmptyString() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(asList()).containsExactly(""));

    assertFailureValue(e, "missing (1)", "");
  }

  @Test
  public void iterableContainsExactlyWithEmptyStringAndUnexpectedItem() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList("a", null)).containsExactly(""));

    assertFailureKeys(e, "missing (1)", "unexpected (2)", "---", "expected", "but was");
    assertFailureValue(e, "missing (1)", "");
    assertFailureValue(e, "unexpected (2)", "a, null");
  }

  @Test
  public void iterableContainsExactlyWithEmptyStringAndMissingItem() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList("")).containsExactly("a", null));

    assertFailureValue(e, "missing (2)", "a, null");
    assertFailureValue(e, "unexpected (1)", "");
  }

  @Test
  public void iterableContainsExactlyWithEmptyStringAmongMissingItems() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList("a")).containsExactly("", "b"));

    assertFailureKeys(
        e, "missing (2)", "#1", "#2", "", "unexpected (1)", "#1", "---", "expected", "but was");
    assertFailureValueIndexed(e, "#1", 0, "");
    assertFailureValueIndexed(e, "#2", 0, "b");
    assertFailureValueIndexed(e, "#1", 1, "a");
  }

  @Test
  public void iterableContainsExactlySingleElement() {
    assertThat(asList(1)).containsExactly(1);

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(asList(1)).containsExactly(2));
    assertFailureKeys(e, "value of", "expected", "but was");
    assertFailureValue(e, "value of", "iterable.onlyElement()");
  }

  @Test
  public void iterableContainsExactlySingleElementNoEqualsMagic() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1)).containsExactly(1L));
    assertFailureValueIndexed(e, "an instance of", 0, "java.lang.Long");
  }

  @Test
  @SuppressWarnings("ContainsExactlyElementsInWithVarArgsToExactly")
  public void iterableContainsExactlyWithElementsThatThrowWhenYouCallHashCode() {
    HashCodeThrower one = new HashCodeThrower();
    HashCodeThrower two = new HashCodeThrower();

    assertThat(asList(one, two)).containsExactly(two, one);
    assertThat(asList(one, two)).containsExactly(one, two).inOrder();
    assertThat(asList(one, two)).containsExactlyElementsIn(asList(two, one));
    assertThat(asList(one, two)).containsExactlyElementsIn(asList(one, two)).inOrder();
  }

  @Test
  public void iterableContainsExactlyWithElementsThatThrowWhenYouCallHashCodeFailureTooMany() {
    HashCodeThrower one = new HashCodeThrower();
    HashCodeThrower two = new HashCodeThrower();

    expectFailure(whenTesting -> whenTesting.that(asList(one, two)).containsExactly(one));
  }

  @Test
  public void iterableContainsExactlyWithElementsThatThrowWhenYouCallHashCodeOneMismatch() {
    HashCodeThrower one = new HashCodeThrower();
    HashCodeThrower two = new HashCodeThrower();

    expectFailure(whenTesting -> whenTesting.that(asList(one, one)).containsExactly(one, two));
  }

  private static class HashCodeThrower {
    @Override
    public boolean equals(@Nullable Object other) {
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
  @SuppressWarnings({"ContainsExactlyNone", "TruthSelfEquals"})
  public void iterableContainsExactlyElementsInInOrderPassesWithEmptyExpectedAndActual() {
    assertThat(ImmutableList.of()).containsExactlyElementsIn(ImmutableList.of()).inOrder();
  }

  @Test
  @SuppressWarnings("ContainsExactlyNone")
  public void iterableContainsExactlyElementsInWithEmptyExpected() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList("foo")).containsExactlyElementsIn(ImmutableList.of()));
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  @SuppressWarnings("ContainsExactlyElementsInWithVarArgsToExactly")
  public void iterableContainsExactlyElementsInErrorMessageIsInOrder() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(asList("foo OR bar"))
                    .containsExactlyElementsIn(asList("foo", "bar")));
    assertFailureValue(e, "missing (2)", "foo, bar");
    assertFailureValue(e, "unexpected (1)", "foo OR bar");
  }

  @Test
  public void iterableContainsExactlyMissingItemFailure() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2)).containsExactly(1, 2, 4));
    assertFailureValue(e, "missing (1)", "4");
  }

  @Test
  public void iterableContainsExactlyUnexpectedItemFailure() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3)).containsExactly(1, 2));
    assertFailureValue(e, "unexpected (1)", "3");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesNotEnoughItemsFailure() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, 2, 3)).containsExactly(1, 2, 2, 2, 3));
    assertFailureValue(e, "missing (2)", "2 [2 copies]");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesMissingItemFailure() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, 2, 3)).containsExactly(1, 2, 2, 2, 3, 4));
    assertFailureValue(e, "missing (3)", "2 [2 copies], 4");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesMissingItemsWithNewlineFailure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(asList("a", "b", "foo\nbar"))
                    .containsExactly("a", "b", "foo\nbar", "foo\nbar", "foo\nbar"));
    assertFailureKeys(e, "missing (2)", "#1 [2 copies]", "---", "expected", "but was");
    assertFailureValue(e, "#1 [2 copies]", "foo\nbar");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesMissingAndExtraItemsWithNewlineFailure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList("a\nb", "a\nb")).containsExactly("foo\nbar", "foo\nbar"));
    assertFailureKeys(
        e,
        "missing (2)",
        "#1 [2 copies]",
        "",
        "unexpected (2)",
        "#1 [2 copies]",
        "---",
        "expected",
        "but was");
    assertFailureValueIndexed(e, "#1 [2 copies]", 0, "foo\nbar");
    assertFailureValueIndexed(e, "#1 [2 copies]", 1, "a\nb");
  }

  @Test
  public void iterableContainsExactlyWithDuplicatesUnexpectedItemFailure() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, 2, 2, 2, 2, 3)).containsExactly(1, 2, 2, 3));
    assertFailureValue(e, "unexpected (2)", "2 [2 copies]");
  }

  /*
   * Slightly subtle test to ensure that if multiple equal elements are found
   * to be missing we only reference it once in the output message.
   */
  @Test
  public void iterableContainsExactlyWithDuplicateMissingElements() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList()).containsExactly(4, 4, 4));
    assertFailureValue(e, "missing (3)", "4 [3 copies]");
  }

  @Test
  public void iterableContainsExactlyWithNullFailure() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, null, 3)).containsExactly(1, null, null, 3));
    assertFailureValue(e, "missing (1)", "null");
  }

  @Test
  public void iterableContainsExactlyWithMissingAndExtraElements() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3)).containsExactly(1, 2, 4));
    assertFailureValue(e, "missing (1)", "4");
    assertFailureValue(e, "unexpected (1)", "3");
  }

  @Test
  public void iterableContainsExactlyWithDuplicateMissingAndExtraElements() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1, 2, 3, 3)).containsExactly(1, 2, 4, 4));
    assertFailureValue(e, "missing (2)", "4 [2 copies]");
    assertFailureValue(e, "unexpected (2)", "3 [2 copies]");
  }

  @Test
  public void iterableContainsExactlyWithCommaSeparatedVsIndividual() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList("a, b")).containsExactly("a", "b"));
    assertFailureKeys(
        e, "missing (2)", "#1", "#2", "", "unexpected (1)", "#1", "---", "expected", "but was");
    assertFailureValueIndexed(e, "#1", 0, "a");
    assertFailureValueIndexed(e, "#2", 0, "b");
    assertFailureValueIndexed(e, "#1", 1, "a, b");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHomogeneousList() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1L, 2L)).containsExactly(1, 2));
    assertFailureValue(e, "missing (2)", "1, 2 (java.lang.Integer)");
    assertFailureValue(e, "unexpected (2)", "1, 2 (java.lang.Long)");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndListWithNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1L, 2L)).containsExactly(null, 1, 2));
    assertFailureValue(
        e, "missing (3)", "null (null type), 1 (java.lang.Integer), 2 (java.lang.Integer)");
    assertFailureValue(e, "unexpected (2)", "1, 2 (java.lang.Long)");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHeterogeneousList() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1L, 2)).containsExactly(1, null, 2L));
    assertFailureValue(
        e, "missing (3)", "1 (java.lang.Integer), null (null type), 2 (java.lang.Long)");
    assertFailureValue(e, "unexpected (2)", "1 (java.lang.Long), 2 (java.lang.Integer)");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHomogeneousListWithDuplicates() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1L, 2L)).containsExactly(1, 2, 2));
    assertFailureValue(e, "missing (3)", "1, 2 [2 copies] (java.lang.Integer)");
    assertFailureValue(e, "unexpected (2)", "1, 2 (java.lang.Long)");
  }

  @Test
  public void iterableContainsExactlyFailsWithSameToStringAndHeterogeneousListWithDuplicates() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(asList(1L, 2)).containsExactly(1, null, null, 2L, 2L));
    assertFailureValue(
        e,
        "missing (5)",
        "1 (java.lang.Integer), null (null type) [2 copies], 2 (java.lang.Long) [2 copies]");
    assertFailureValue(e, "unexpected (2)", "1 (java.lang.Long), 2 (java.lang.Integer)");
  }

  @Test
  public void iterableContainsExactlyWithOneIterableGivesWarning() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2, 3, 4)).containsExactly(asList(1, 2, 3, 4)));
    assertThat(e).hasMessageThat().contains(CONTAINS_EXACTLY_ITERABLE_WARNING);
  }

  @Test
  @SuppressWarnings("ContainsExactlyElementsInWithVarArgsToExactly")
  public void iterableContainsExactlyElementsInWithOneIterableDoesNotGiveWarning() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2, 3, 4)).containsExactlyElementsIn(asList(1, 2, 3)));
    assertFailureValue(e, "unexpected (1)", "4");
  }

  @Test
  public void iterableContainsExactlyWithTwoIterableDoesNotGivesWarning() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2, 3, 4)).containsExactly(asList(1, 2), asList(3, 4)));
    assertThat(e).hasMessageThat().doesNotContain(CONTAINS_EXACTLY_ITERABLE_WARNING);
  }

  private static final String CONTAINS_EXACTLY_ITERABLE_WARNING =
      "Passing an iterable to the varargs method containsExactly(Object...) is "
          + "often not the correct thing to do. Did you mean to call "
          + "containsExactlyElementsIn(Iterable) instead?";

  @Test
  public void iterableContainsExactlyWithOneNonIterableDoesNotGiveWarning() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 3, 4)).containsExactly(1));
    assertFailureValue(e, "unexpected (3)", "2, 3, 4");
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
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, null, 3)).containsExactly(null, 1, 3).inOrder());
    assertFailureKeys(e, "contents match, but order was wrong", "expected", "but was");
    assertFailureValue(e, "expected", "[null, 1, 3]");
  }

  @Test
  public void iterableContainsExactlyInOrderWithOneShotIterable() {
    Iterator<Object> iterator = asList((Object) 1, null, 3).iterator();
    Iterable<Object> iterable = () -> iterator;
    assertThat(iterable).containsExactly(1, null, 3).inOrder();
  }

  @Test
  public void iterableContainsExactlyInOrderWithOneShotIterableWrongOrder() {
    Iterator<Object> iterator = asList((Object) 1, null, 3).iterator();
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

    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(iterable).containsExactly(1, 3, null).inOrder());
    assertFailureKeys(e, "contents match, but order was wrong", "expected", "but was");
    assertFailureValue(e, "expected", "[1, 3, null]");
  }

  @Test
  public void iterableWithNoToStringOverride() {
    Iterable<Integer> iterable = () -> Iterators.forArray(1, 2, 3);

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(iterable).containsExactly(1, 2).inOrder());
    assertFailureValue(e, "but was", "[1, 2, 3]");
  }

  @Test
  @SuppressWarnings("ContainsExactlyElementsInWithVarArgsToExactly")
  public void iterableContainsExactlyElementsInIterable() {
    assertThat(asList(1, 2)).containsExactlyElementsIn(asList(1, 2));

    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2)).containsExactlyElementsIn(asList(1, 2, 4)));
    assertFailureValue(e, "missing (1)", "4");
  }

  @Test
  public void iterableContainsExactlyElementsInArray() {
    assertThat(asList(1, 2)).containsExactlyElementsIn(new Integer[] {1, 2});

    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList(1, 2)).containsExactlyElementsIn(new Integer[] {1, 2, 4}));
    assertFailureValue(e, "missing (1)", "4");
  }

  @Test
  @SuppressWarnings("UndefinedEquals") // Iterable equality isn't defined, but null equality is
  public void nullEqualToNull() {
    assertThat((Iterable<?>) null).isEqualTo(null);
  }

  @Test
  public void nullEqualToSomething() {
    expectFailure(
        whenTesting -> whenTesting.that((Iterable<?>) null).isEqualTo(ImmutableList.of()));
  }

  @Test
  public void somethingEqualToNull() {
    expectFailure(whenTesting -> whenTesting.that(ImmutableList.of()).isEqualTo(null));
  }

  @Test
  public void somethingEqualToSomething() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(ImmutableList.of()).isEqualTo(ImmutableList.of("a")));
    // isEqualTo uses the containsExactly style of message:
    assertFailureValue(e, "missing (1)", "a");
  }

  @Test
  public void isEqualToNotConsistentWithEquals() {
    TreeSet<String> actual = new TreeSet<>(CASE_INSENSITIVE_ORDER);
    TreeSet<String> expected = new TreeSet<>(CASE_INSENSITIVE_ORDER);
    actual.add("one");
    expected.add("ONE");
    /*
     * Our contract doesn't guarantee that the following test will pass. It *currently* does,
     * though, and if we change that behavior, we want this test to let us know.
     */
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void isEqualToNotConsistentWithEquals_failure() {
    TreeSet<String> actual = new TreeSet<>(CASE_INSENSITIVE_ORDER);
    TreeSet<String> expected = new TreeSet<>(CASE_INSENSITIVE_ORDER);
    actual.add("one");
    expected.add("ONE");
    actual.add("two");
    expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expected));
    // The exact message generated is unspecified.
  }

  @Test
  public void iterableIsEmpty() {
    assertThat(asList()).isEmpty();
  }

  @Test
  public void iterableIsEmptyWithFailure() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(asList(1, null, 3)).isEmpty());
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void iterableIsNotEmpty() {
    assertThat(asList("foo")).isNotEmpty();
  }

  @Test
  public void iterableIsNotEmptyWithFailure() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(asList()).isNotEmpty());
    assertFailureKeys(e, "expected not to be empty");
  }

  @Test
  public void iterableIsInStrictOrder() {
    assertThat(asList()).isInStrictOrder();
    assertThat(asList(1)).isInStrictOrder();
    assertThat(asList(1, 2, 3, 4)).isInStrictOrder();
  }

  @Test
  public void isInStrictOrderFailure() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 2, 2, 4)).isInStrictOrder());
    assertFailureKeys(
        e, "expected to be in strict order", "but contained", "followed by", "full contents");
    assertFailureValue(e, "but contained", "2");
    assertFailureValue(e, "followed by", "2");
    assertFailureValue(e, "full contents", "[1, 2, 2, 4]");
  }

  @Test
  public void isInStrictOrderWithNonComparableElementsFailure() {
    assertThrows(
        ClassCastException.class,
        () -> assertThat(asList((Object) 1, "2", 3, "4")).isInStrictOrder());
  }

  @Test
  public void iterableIsInOrder() {
    assertThat(asList()).isInOrder();
    assertThat(asList(1)).isInOrder();
    assertThat(asList(1, 1, 2, 3, 3, 3, 4)).isInOrder();
  }

  @Test
  public void isInOrderFailure() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(asList(1, 3, 2, 4)).isInOrder());
    assertFailureKeys(
        e, "expected to be in order", "but contained", "followed by", "full contents");
    assertFailureValue(e, "but contained", "3");
    assertFailureValue(e, "followed by", "2");
    assertFailureValue(e, "full contents", "[1, 3, 2, 4]");
  }

  @Test
  public void isInOrderMultipleFailures() {
    expectFailure(whenTesting -> whenTesting.that(asList(1, 3, 2, 4, 0)).isInOrder());
  }

  @Test
  public void isInOrderWithNonComparableElementsFailure() {
    assertThrows(
        ClassCastException.class, () -> assertThat(asList((Object) 1, "2", 2, "3")).isInOrder());
  }

  @Test
  public void iterableIsInStrictOrderWithComparator() {
    Iterable<String> emptyStrings = asList();
    assertThat(emptyStrings).isInStrictOrder(COMPARE_AS_DECIMAL);
    assertThat(asList("1")).isInStrictOrder(COMPARE_AS_DECIMAL);
    // Note: Use "10" and "20" to distinguish numerical and lexicographical ordering.
    assertThat(asList("1", "2", "10", "20")).isInStrictOrder(COMPARE_AS_DECIMAL);
  }

  @Test
  public void iterableIsInStrictOrderWithComparatorFailure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList("1", "2", "2", "10")).isInStrictOrder(COMPARE_AS_DECIMAL));
    assertFailureKeys(
        e, "expected to be in strict order", "but contained", "followed by", "full contents");
    assertFailureValue(e, "but contained", "2");
    assertFailureValue(e, "followed by", "2");
    assertFailureValue(e, "full contents", "[1, 2, 2, 10]");
  }

  @Test
  public void iterableIsInOrderWithComparator() {
    Iterable<String> emptyStrings = asList();
    assertThat(emptyStrings).isInOrder(COMPARE_AS_DECIMAL);
    assertThat(asList("1")).isInOrder(COMPARE_AS_DECIMAL);
    assertThat(asList("1", "1", "2", "10", "10", "10", "20")).isInOrder(COMPARE_AS_DECIMAL);
  }

  @Test
  public void iterableIsInOrderWithComparatorFailure() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(asList("1", "10", "2", "20")).isInOrder(COMPARE_AS_DECIMAL));
    assertFailureKeys(
        e, "expected to be in order", "but contained", "followed by", "full contents");
    assertFailureValue(e, "but contained", "10");
    assertFailureValue(e, "followed by", "2");
    assertFailureValue(e, "full contents", "[1, 10, 2, 20]");
  }

  @SuppressWarnings("CompareProperty") // avoiding Java 8 API under Android
  private static final Comparator<String> COMPARE_AS_DECIMAL =
      (a, b) -> Integer.valueOf(a).compareTo(Integer.valueOf(b));

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

  // We can't use Comparators.comparing under old versions of Android.
  @SuppressWarnings({
    "CompareProperty",
    "DoubleProperty_ExtractTernaryHead",
    "FloatProperty_ExtractTernaryHead",
    "IntegerProperty_ExtractTernaryHead",
    "LongProperty_ExtractTernaryHead",
  })
  // Even though Integer.compare was added in Java 7, we use it even under old versions of Android,
  // even without library desugaring on: It and a few other APIs are *always* desguared:
  // https://r8.googlesource.com/r8/+/a7563f86014d44f961f40fc109ab1c1073f2ee4e/src/main/java/com/android/tools/r8/ir/desugar/BackportedMethodRewriter.java
  // Now, if this code weren't in Truth's *tests*, then it would cause Animal Sniffer to complain.
  // In that case, we might fall back to the deprecated Guava Ints.compare.
  private static final Comparator<Foo> FOO_COMPARATOR = (a, b) -> Integer.compare(a.x, b.x);

  @Test
  public void iterableOrderedByBaseClassComparator() {
    Iterable<Bar> targetList = asList(new Bar(1), new Bar(2), new Bar(3));
    assertThat(targetList).isInOrder(FOO_COMPARATOR);
    assertThat(targetList).isInStrictOrder(FOO_COMPARATOR);
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
  @SuppressWarnings("deprecation") // test of a mistaken call
  public void isNotIn() {
    ImmutableList<String> actual = ImmutableList.of("a");

    assertThat(actual).isNotIn(ImmutableList.of(ImmutableList.of("b"), ImmutableList.of("c")));

    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).isNotIn(ImmutableList.of("a", "b")));
    assertThat(e)
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
  @SuppressWarnings({"IncompatibleArgumentType", "deprecation"}) // test of a mistaken call
  public void isNoneOf() {
    ImmutableList<String> actual = ImmutableList.of("a");

    assertThat(actual).isNoneOf(ImmutableList.of("b"), ImmutableList.of("c"));

    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).isNoneOf("a", "b"));
    assertThat(e)
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
}
