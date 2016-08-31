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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.SubjectUtils.accumulate;
import static com.google.common.truth.SubjectUtils.countDuplicates;
import static java.util.Arrays.asList;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Propositions for {@link Iterable} subjects.
 *
 * @author Kurt Alfred Kluever
 * @author Pete Gillin
 */
// Can't be final since MultisetSubject extends it
public class IterableSubject extends Subject<IterableSubject, Iterable<?>> {

  // TODO(kak): Make this package-protected?
  protected IterableSubject(FailureStrategy failureStrategy, @Nullable Iterable<?> list) {
    super(failureStrategy, list);
  }

  /** Fails if the subject is not empty. */
  public final void isEmpty() {
    if (!Iterables.isEmpty(getSubject())) {
      fail("is empty");
    }
  }

  /** Fails if the subject is empty. */
  public final void isNotEmpty() {
    if (Iterables.isEmpty(getSubject())) {
      // TODO(kak): "Not true that <[]> is not empty" doesn't really need the <[]>,
      // since it's empty. But would the bulkier "the subject" really be better?
      // At best, we could *replace* <[]> with a given label (rather than supplementing it).
      // Perhaps the right failure message is just "<[]> should not have been empty"
      fail("is not empty");
    }
  }

  /** Fails if the subject does not have the given size. */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = Iterables.size(getSubject());
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /** Attests (with a side-effect failure) that the subject contains the supplied item. */
  public final void contains(@Nullable Object element) {
    if (!Iterables.contains(getSubject(), element)) {
      failWithRawMessage("%s should have contained <%s>", getDisplaySubject(), element);
    }
  }

  /** Attests (with a side-effect failure) that the subject does not contain the supplied item. */
  public final void doesNotContain(@Nullable Object element) {
    if (Iterables.contains(getSubject(), element)) {
      failWithRawMessage("%s should not have contained <%s>", getDisplaySubject(), element);
    }
  }

  /** Attests that the subject does not contain duplicate elements. */
  public final void containsNoDuplicates() {
    List<Entry<?>> duplicates = Lists.newArrayList();
    for (Multiset.Entry<?> entry : LinkedHashMultiset.create(getSubject()).entrySet()) {
      if (entry.getCount() > 1) {
        duplicates.add(entry);
      }
    }
    if (!duplicates.isEmpty()) {
      failWithRawMessage("%s has the following duplicates: <%s>", getDisplaySubject(), duplicates);
    }
  }

  /** Attests that the subject contains at least one of the provided objects or fails. */
  public final void containsAnyOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
    containsAny("contains any of", accumulate(first, second, rest));
  }

  /**
   * Attests that the subject contains at least one of the objects contained in the provided
   * collection or fails.
   */
  public final void containsAnyIn(Iterable<?> expected) {
    containsAny("contains any element in", expected);
  }

  private void containsAny(String failVerb, Iterable<?> expected) {
    Collection<?> subject;
    if (getSubject() instanceof Collection) {
      // Should be safe to assume that any Iterable implementing Collection isn't a one-shot
      // iterable, right? I sure hope so.
      subject = (Collection<?>) getSubject();
    } else {
      // Would really like to use a HashSet here, but that would mean this would fail for elements
      // that don't implement hashCode correctly (or even throw an exception from it), where using
      // Iterables.contains would not fail.
      subject = Lists.newArrayList(getSubject());
    }

    for (Object item : expected) {
      if (subject.contains(item)) {
        return;
      }
    }
    fail(failVerb, expected);
  }

  /**
   * Attests that the actual iterable contains at least all of the expected elements or fails. If an
   * element appears more than once in the expected elements to this call then it must appear at
   * least that number of times in the actual elements.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The expected elements must appear in the given order
   * within the actual elements, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  public final Ordered containsAllOf(
      @Nullable Object firstExpected,
      @Nullable Object secondExpected,
      @Nullable Object... restOfExpected) {
    return containsAll(
        "contains all of", accumulate(firstExpected, secondExpected, restOfExpected));
  }

  /**
   * Attests that the actual iterable contains at least all of the expected elements or fails. If an
   * element appears more than once in the expected elements then it must appear at least that
   * number of times in the actual elements.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The expected elements must appear in the given order
   * within the actual elements, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  public final Ordered containsAllIn(Iterable<?> expected) {
    return containsAll("contains all elements in", expected);
  }

  private Ordered containsAll(String failVerb, Iterable<?> expectedIterable) {
    List<?> actual = Lists.newLinkedList(getSubject());
    List<?> expected = Lists.newArrayList(expectedIterable);

    List<Object> missing = Lists.newArrayList();
    List<Object> actualNotInOrder = Lists.newArrayList();

    boolean ordered = true;
    // step through the expected elements...
    for (Object e : expected) {
      int index = actual.indexOf(e);
      if (index != -1) { // if we find the element in the actual list...
        // drain all the elements that come before that element into actualNotInOrder
        moveElements(actual, actualNotInOrder, index);
        // and remove the element from the actual list
        actual.remove(0);
      } else { // otherwise try removing it from actualNotInOrder...
        if (actualNotInOrder.remove(e)) { // if it was in actualNotInOrder, we're not in order
          ordered = false;
        } else { // if it's not in actualNotInOrder, we're missing an expected element
          missing.add(e);
        }
      }
    }
    // if we have any missing expected elements, fail
    if (!missing.isEmpty()) {
      failWithBadResults(failVerb, expected, "is missing", countDuplicates(missing));
    }
    return ordered ? IN_ORDER : new NotInOrder("contains all elements in order", expected);
  }

  /**
   * Removes at most the given number of available elements from the input list and adds them to the
   * given output collection.
   */
  private static void moveElements(List<?> input, Collection<Object> output, int maxElements) {
    for (int i = 0; i < maxElements; i++) {
      output.add(input.remove(0));
    }
  }

  /**
   * Attests that a subject contains exactly the provided objects or fails.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3 times in the
   * parameters asserts that the object must likewise be duplicated exactly 3 times in the subject.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactly(@Nullable Object... varargs) {
    List<Object> expected = (varargs == null) ? Lists.newArrayList((Object) null) : asList(varargs);
    return containsExactly(
        "contains exactly",
        expected,
        varargs != null && varargs.length == 1 && varargs[0] instanceof Iterable);
  }

  /**
   * Attests that a subject contains exactly the provided objects or fails.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3 times in the {@code
   * Iterable} parameter asserts that the object must likewise be duplicated exactly 3 times in the
   * subject.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactlyElementsIn(Iterable<?> expected) {
    return containsExactly("contains exactly", expected, false);
  }

  private Ordered containsExactly(
      String failVerb, Iterable<?> required, boolean addElementsInWarning) {
    String failSuffix =
        addElementsInWarning
            ? ". Passing an iterable to the varargs method containsExactly(Object...) is "
                + "often not the correct thing to do. Did you mean to call "
                + "containsExactlyElementsIn(Iterable) instead?"
            : "";
    Iterator<?> actualIter = getSubject().iterator();
    Iterator<?> requiredIter = required.iterator();

    // Step through both iterators comparing elements pairwise.
    while (actualIter.hasNext() && requiredIter.hasNext()) {
      Object actualElement = actualIter.next();
      Object requiredElement = requiredIter.next();

      // As soon as we encounter a pair of elements that differ, we know that inOrder()
      // cannot succeed, so we can check the rest of the elements more normally.
      // Since any previous pairs of elements we iterated over were equal, they have no
      // effect on the result now.
      if (!Objects.equal(actualElement, requiredElement)) {
        // Missing elements; elements that are not missing will be removed as we iterate.
        Collection<Object> missing = Lists.newArrayList();
        missing.add(requiredElement);
        Iterators.addAll(missing, requiredIter);

        // Extra elements that the subject had but shouldn't have.
        Collection<Object> extra = Lists.newArrayList();

        // Remove all actual elements from missing, and add any that weren't in missing
        // to extra.
        if (!missing.remove(actualElement)) {
          extra.add(actualElement);
        }
        while (actualIter.hasNext()) {
          Object item = actualIter.next();
          if (!missing.remove(item)) {
            extra.add(item);
          }
        }

        // Fail if there are either missing or extra elements.

        // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
        // the subject but not enough times. Similarly for unexpected extra items.
        if (!missing.isEmpty()) {
          if (!extra.isEmpty()) {
            // Subject is both missing required elements and contains extra elements
            failWithRawMessage(
                "Not true that %s %s <%s>. It is missing <%s> and has unexpected items <%s>%s",
                getDisplaySubject(),
                failVerb,
                required,
                countDuplicates(missing),
                countDuplicates(extra),
                failSuffix);
          } else {
            failWithBadResultsAndSuffix(
                failVerb, required, "is missing", countDuplicates(missing), failSuffix);
          }
        }
        if (!extra.isEmpty()) {
          failWithBadResultsAndSuffix(
              failVerb, required, "has unexpected items", countDuplicates(extra), failSuffix);
        }

        // Since we know the iterables were not in the same order, inOrder() can just fail.
        return new NotInOrder("contains only these elements in order", required);
      }
    }

    // Here,  we must have reached the end of one of the iterators without finding any
    // pairs of elements that differ. If the actual iterator still has elements, they're
    // extras. If the required iterator has elements, they're missing elements.
    if (actualIter.hasNext()) {
      failWithBadResultsAndSuffix(
          failVerb,
          required,
          "has unexpected items",
          countDuplicates(Lists.newArrayList(actualIter)),
          failSuffix);
    } else if (requiredIter.hasNext()) {
      failWithBadResultsAndSuffix(
          failVerb,
          required,
          "is missing",
          countDuplicates(Lists.newArrayList(requiredIter)),
          failSuffix);
    }

    // If neither iterator has elements, we reached the end and the elements were in
    // order, so inOrder() can just succeed.
    return IN_ORDER;
  }

  /**
   * Fails with the bad results and a suffix.
   *
   * @param verb the proposition being asserted
   * @param expected the expectations against which the subject is compared
   * @param failVerb the failure of the proposition being asserted
   * @param actual the actual value the subject was compared against
   * @param suffix a suffix to append to the failure message
   */
  protected final void failWithBadResultsAndSuffix(
      String verb, Object expected, String failVerb, Object actual, String suffix) {
    failWithRawMessage(
        "Not true that %s %s <%s>. It %s <%s>%s",
        getDisplaySubject(),
        verb,
        expected,
        failVerb,
        (actual == null) ? "null reference" : actual,
        suffix);
  }

  /**
   * Attests that a actual iterable contains none of the excluded objects or fails. (Duplicates are
   * irrelevant to this test, which fails if any of the actual elements equal any of the excluded.)
   */
  public final void containsNoneOf(
      @Nullable Object firstExcluded,
      @Nullable Object secondExcluded,
      @Nullable Object... restOfExcluded) {
    containsNone("contains none of", accumulate(firstExcluded, secondExcluded, restOfExcluded));
  }

  /**
   * Attests that a actual iterable contains none of the elements contained in the excluded iterable
   * or fails. (Duplicates are irrelevant to this test, which fails if any of the actual elements
   * equal any of the excluded.)
   */
  public final void containsNoneIn(Iterable<?> excluded) {
    containsNone("contains no elements in", excluded);
  }

  private void containsNone(String failVerb, Iterable<?> excluded) {
    Collection<Object> present = new ArrayList<Object>();
    for (Object item : Sets.newLinkedHashSet(excluded)) {
      if (Iterables.contains(getSubject(), item)) {
        present.add(item);
      }
    }
    if (!present.isEmpty()) {
      failWithBadResults(failVerb, excluded, "contains", present);
    }
  }

  /** Ordered implementation that always fails. */
  private class NotInOrder implements Ordered {
    private final String check;
    private final Iterable<?> required;

    NotInOrder(String check, Iterable<?> required) {
      this.check = check;
      this.required = required;
    }

    @Override
    public void inOrder() {
      fail(check, required);
    }
  }

  /** Ordered implementation that does nothing because it's already known to be true. */
  private static final Ordered IN_ORDER =
      new Ordered() {
        @Override
        public void inOrder() {}
      };

  /**
   * Fails if the iterable is not strictly ordered, according to the natural ordering of its
   * elements. Strictly ordered means that each element in the iterable is <i>strictly</i> greater
   * than the element that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public final void isStrictlyOrdered() {
    isStrictlyOrdered((Ordering) Ordering.natural());
  }

  /**
   * Fails if the iterable is not strictly ordered, according to the given comparator. Strictly
   * ordered means that each element in the iterable is <i>strictly</i> greater than the element
   * that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   */
  @SuppressWarnings({"unchecked"})
  public final void isStrictlyOrdered(final Comparator<?> comparator) {
    checkNotNull(comparator);
    pairwiseCheck(
        new PairwiseChecker() {
          @Override
          public void check(Object prev, Object next) {
            if (((Comparator<Object>) comparator).compare(prev, next) >= 0) {
              fail("is strictly ordered", prev, next);
            }
          }
        });
  }

  /**
   * Fails if the iterable is not ordered, according to the natural ordering of its elements.
   * Ordered means that each element in the iterable is greater than or equal to the element that
   * preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public final void isOrdered() {
    isOrdered((Ordering) Ordering.natural());
  }

  /** @deprecated Use {@link #isOrdered} instead. */
  @Deprecated
  public final void isPartiallyOrdered() {
    isOrdered();
  }

  /**
   * Fails if the iterable is not ordered, according to the given comparator. Ordered means that
   * each element in the iterable is greater than or equal to the element that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   */
  @SuppressWarnings({"unchecked"})
  public final void isOrdered(final Comparator<?> comparator) {
    checkNotNull(comparator);
    pairwiseCheck(
        new PairwiseChecker() {
          @Override
          public void check(Object prev, Object next) {
            if (((Comparator<Object>) comparator).compare(prev, next) > 0) {
              fail("is ordered", prev, next);
            }
          }
        });
  }

  /** @deprecated Use {@link #isOrdered(Comparator)} instead. */
  @Deprecated
  public final void isPartiallyOrdered(final Comparator<?> comparator) {
    isOrdered(comparator);
  }

  private interface PairwiseChecker {
    void check(Object prev, Object next);
  }

  private void pairwiseCheck(PairwiseChecker checker) {
    Iterator<?> iterator = getSubject().iterator();
    if (iterator.hasNext()) {
      Object prev = iterator.next();
      while (iterator.hasNext()) {
        Object next = iterator.next();
        checker.check(prev, next);
        prev = next;
      }
    }
  }

  /**
   * Starts a method chain for a test proposition in which the actual elements (i.e. the elements of
   * the {@link Iterable} under test) are compared to expected elements using the given {@link
   * Correspondence}. The actual elements must be of type {@code A}, the expected elements must be
   * of type {@code E}. The proposition is actually executed by continuing the method chain. For
   * example: <pre>   {@code
   *   assertThat(actualIterable).comparingElementsUsing(correspondence).contains(expected);}</pre>
   * where {@code actualIterable} is an {@code Iterable<A>} (or, more generally, an {@code
   * Iterable<? extends A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and {@code
   * expected} is an {@code E}.
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual element that is not of type {@code A}.
   */
  public <A, E> UsingCorrespondence<A, E> comparingElementsUsing(
      Correspondence<A, E> correspondence) {
    return new UsingCorrespondence<A, E>(correspondence);
  }

  /**
   * A partially specified proposition in which the actual elements (normally the elements of the
   * {@link Iterable} under test) are compared to expected elements using a {@link Correspondence}.
   * The expected elements are of type {@code E}. Call methods on this object to actually execute
   * the proposition.
   *
   * <p>The actual elements may alternatively be from an array of doubles or floats (see {@link
   * PrimitiveDoubleArraySubject#withTolerance} and {@link
   * PrimitiveFloatArraySubject#withTolerance}).
   *
   * <p>NOTE: This class is under constructions and more methods will be added in future versions.
   */
  public final class UsingCorrespondence<A, E> {

    private final Correspondence<A, E> correspondence;

    private UsingCorrespondence(Correspondence<A, E> correspondence) {
      this.correspondence = checkNotNull(correspondence);
    }

    /**
     * Attests that the subject contains at least one element that corresponds to the given expected
     * element.
     */
    public void contains(@Nullable E expected) {
      for (A actual : getCastSubject()) {
        if (correspondence.compare(actual, expected)) {
          return;
        }
      }
      fail("contains at least one element that " + correspondence, expected);
    }

    /** Attests that none of the actual elements correspond to the given element. */
    public void doesNotContain(@Nullable E excluded) {
      List<A> matchingElements = new ArrayList<A>();
      for (A actual : getCastSubject()) {
        if (correspondence.compare(actual, excluded)) {
          matchingElements.add(actual);
        }
      }
      if (!matchingElements.isEmpty()) {
        failWithRawMessage(
            "%s should not have contained an element that %s <%s>. "
                + "It contained the following such elements: <%s>",
            getDisplaySubject(), correspondence, excluded, matchingElements);
      }
    }

    /**
     * Attests that subject contains exactly elements that correspond to the expected elements, i.e.
     * that there is a 1:1 mapping between the actual elements and the expected elements where each
     * pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method.
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private Ordered containsExactly(@Nullable E... expected) {
      throw new UnsupportedOperationException();
    }

    /**
     * Attests that subject contains exactly elements that correspond to the expected elements, i.e.
     * that there is a 1:1 mapping between the actual elements and the expected elements where each
     * pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method.
     *
     * <p>THIS METHOD IS UNDER CONSTRUCTION. It will work under some conditions, and throw
     * {@link UnsupportedOperationException} under others. DO NOT USE!
     */
    @CanIgnoreReturnValue
    // TODO(b/29966314): Finish implementing this, remove the "under construction" in the javadoc,
    // and make it public.
    Ordered containsExactlyElementsIn(Iterable<? extends E> expected) {
      // Check if the elements correspond in order. This allows the common case of a passing test
      // using inOrder() to complete in linear time.
      if (correspondInOrder(getCastSubject().iterator(), expected.iterator())) {
        return IN_ORDER;
      }
      // We know they don't correspond in order, so we're going to have to do an any-order test.
      // Find a many:many mapping between the indexes of the elements which correspond, and check
      // it for completeness.
      ImmutableList<A> actualList = ImmutableList.copyOf(getCastSubject());
      ImmutableList<? extends E> expectedList = ImmutableList.copyOf(expected);
      ImmutableSetMultimap<Integer, Integer> candidateMapping =
          findCandidateMapping(actualList, expectedList);
      failIfCandidateMappingHasMissingOrExtra(actualList, expectedList, candidateMapping);
      // We know that every expected element maps to at least one actual element, and vice versa.
      // Find a maximal 1:1 mapping, and check it for completeness.
      ImmutableBiMap<Integer, Integer> maximalOneToOneMapping =
          findMaximalOneToOneMapping(candidateMapping);
      failIfOneToOneMappingHasMissingOrExtra(actualList, expectedList, maximalOneToOneMapping);
      // The 1:1 mapping is complete, so the test succeeds (but we know from above that the mapping
      // is not in order).
      return new NotInOrder(
          "contains, in order, exactly one element that " + correspondence + " each element of",
          expected);
    }

    /**
     * Returns whether the actual and expected iterators have the same number of elements and, when
     * iterated pairwise, every pair of actual and expected values satisfies the correspondence.
     */
    private boolean correspondInOrder(
        Iterator<? extends A> actual, Iterator<? extends E> expected) {
      while (actual.hasNext() && expected.hasNext()) {
        A actualElement = actual.next();
        E expectedElement = expected.next();
        if (!correspondence.compare(actualElement, expectedElement)) {
          return false;
        }
      }
      return !(actual.hasNext() || expected.hasNext());
    }

    /**
     * Given a list of actual elements and a list of expected elements, finds a many:many mapping
     * between actual and expected elements where a pair of elements maps if it satisfies the
     * correspondence. Returns this mapping as a multimap where the keys are indexes into the actual
     * list and the values are indexes into the expected list.
     */
    private ImmutableSetMultimap<Integer, Integer> findCandidateMapping(
        List<? extends A> actual, List<? extends E> expected) {
      ImmutableSetMultimap.Builder<Integer, Integer> mapping = ImmutableSetMultimap.builder();
      for (int actualIndex = 0; actualIndex < actual.size(); actualIndex++) {
        for (int expectedIndex = 0; expectedIndex < expected.size(); expectedIndex++) {
          if (correspondence.compare(actual.get(actualIndex), expected.get(expectedIndex))) {
            mapping.put(actualIndex, expectedIndex);
          }
        }
      }
      return mapping.build();
    }

    /**
     * Given a list of actual elements, a list of expected elements, and a many:many mapping between
     * actual and expected elements specified as a multimap of indexes into the actual list to
     * indexes into the expected list, checks that every actual element maps to at least one
     * expected element and vice versa, and fails if this is not the case.
     */
    void failIfCandidateMappingHasMissingOrExtra(
        List<? extends A> actual,
        List<? extends E> expected,
        ImmutableMultimap<Integer, Integer> mapping) {
      ImmutableList<? extends A> extra = findNotIndexed(actual, mapping.keySet());
      ImmutableList<? extends E> missing = findNotIndexed(expected, mapping.inverse().keySet());
      Optional<String> missingOrExtraMessage = describeMissingOrExtra(extra, missing);
      if (missingOrExtraMessage.isPresent()) {
          failWithRawMessage(
              "Not true that %s contains exactly one element that %s each element of <%s>. It %s",
              getDisplaySubject(),
              correspondence,
              expected,
              missingOrExtraMessage.get());
      }
    }

    /**
     * Given a list of extra elements and a list of missing elements, returns an absent value if
     * both are empty, and otherwise returns a verb phrase (suitable for appearing after the subject
     * of the verb) describing them.
     */
    private Optional<String> describeMissingOrExtra(
        List<? extends A> extra, List<? extends E> missing) {
      if (!missing.isEmpty() && !extra.isEmpty()) {
        return Optional.of(
            StringUtil.format(
                "is missing an element that %s %s and has unexpected elements <%s>",
                correspondence, formatMissing(missing), extra));
      } else if (!missing.isEmpty()) {
        return Optional.of(
            StringUtil.format(
                "is missing an element that %s %s", correspondence, formatMissing(missing)));
      } else if (!extra.isEmpty()) {
        return Optional.of(StringUtil.format("has unexpected elements <%s>", extra));
      } else {
        return Optional.absent();
      }
    }

    /**
     * Returns all the elements of the given list other than those with the given indexes. Assumes
     * that all the given indexes really are valid indexes into the list.
     */
    private <T> ImmutableList<T> findNotIndexed(List<T> list, Set<Integer> indexes) {
      if (indexes.size() == list.size()) {
        // If there are as many distinct valid indexes are there are elements in the list then every
        // index must be in there once.
        return ImmutableList.of();
      }
      ImmutableList.Builder<T> notIndexed = ImmutableList.builder();
      for (int index = 0; index < list.size(); index++) {
        if (!indexes.contains(index)) {
          notIndexed.add(list.get(index));
        }
      }
      return notIndexed.build();
    }

    /**
     * Returns a description of the missing items suitable for inclusion in failure messages. If
     * there is a single item, returns {@code "<item>"}. Otherwise, returns
     * {@code "each of <[item, item, item]>"}.
     */
    private String formatMissing(List<?> missing) {
      if (missing.size() == 1) {
        return "<" + missing.get(0) + ">";
      } else {
        return "each of <" + missing + ">";
      }
    }

    /**
     * Given a many:many mapping between actual elements and expected elements, finds a 1:1
     * mapping which is the subset of that many:many mapping which includes the largest possible
     * number of elements. The input and output mappings are each described as a map or multimap
     * where the keys are indexes into the actual list and the values are indexes into the expected
     * list. If there are multiple possible output mappings tying for the largest possible, this
     * returns an arbitrary one.
     */
    private ImmutableBiMap<Integer, Integer> findMaximalOneToOneMapping(
        ImmutableMultimap<Integer, Integer> unused) {
      /*
       * Finding this 1:1 mapping is analogous to finding a maximum cardinality bipartite matching
       * (https://en.wikipedia.org/wiki/Matching_(graph_theory)#In_unweighted_bipartite_graphs).
       *  - The two sets of elements together correspond to the vertices of a graph.
       *  - The many:many mapping corresponds to the edges of that graph.
       *  - The graph is therefore bipartite, with the two sets of elements corresponding to the two
       * parts.
       *  - A 1:1 mapping corresponds to a matching on that bipartite graph (aka an independent edge
       * set, i.e. a subset of the edges with no common vertices).
       *  - And the 1:1 mapping which includes the largest possible number of elements corresponds
       * to the maximum cardinality matching.
       *
       * So we'll apply a standard algorithm for doing maximum cardinality bipartite matching.
       */
      throw new UnsupportedOperationException();
    }

    /**
     * Given a list of actual elements, a list of expected elements, and a 1:1 mapping between
     * actual and expected elements specified as a bimap of indexes into the actual list to indexes
     * into the expected list, checks that every actual element maps to an expected element and vice
     * versa, and fails if this is not the case.
     */
    void failIfOneToOneMappingHasMissingOrExtra(
        List<? extends A> actual,
        List<? extends E> expected,
        BiMap<Integer, Integer> mapping) {
      ImmutableList<? extends A> extra = findNotIndexed(actual, mapping.keySet());
      ImmutableList<? extends E> missing = findNotIndexed(expected, mapping.inverse().keySet());
      Optional<String> missingOrExtraMessage = describeMissingOrExtra(extra, missing);
      if (missingOrExtraMessage.isPresent()) {
        failWithRawMessage(
            "Not true that %s contains exactly one element that %s each element of <%s>. "
                + "It contains at least one element that matches each expected element, "
                + "and every element it contains matches at least one expected element, "
                + "but there was no 1:1 mapping between all the actual and expected elements. "
                + "Using the most complete 1:1 mapping (or one such mapping, if there is a tie), "
                + "it %s",
            getDisplaySubject(),
            correspondence,
            expected,
            missingOrExtraMessage.get());
      }
    }

    /**
     * Attests that the subject contains elements that corresponds to all of the expected elements,
     * i.e. that there is a 1:1 mapping between any subset of the actual elements and the expected
     * elements where each pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method. The elements must appear in the given order within the
     * subject, but they are not required to be consecutive.
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private Ordered containsAllOf(@Nullable E first, @Nullable E second, @Nullable E... rest) {
      throw new UnsupportedOperationException();
    }

    /**
     * Attests that the subject contains elements that corresponds to all of the expected elements,
     * i.e. that there is a 1:1 mapping between any subset of the actual elements and the expected
     * elements where each pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method. The elements must appear in the given order within the
     * subject, but they are not required to be consecutive.
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private Ordered containsAllIn(Iterable<E> unused) {
      throw new UnsupportedOperationException();
    }

    /**
     * Attests that the subject contains at least one element that corresponds to at least one of
     * the expected elements.
     */
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private void containsAnyOf(@Nullable E first, @Nullable E second, @Nullable E... rest) {
      throw new UnsupportedOperationException();
    }

    /**
     * Attests that the subject contains at least one element that corresponds to at least one of
     * the expected elements.
     */
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private void containsAnyIn(Iterable<E> unused) {
      throw new UnsupportedOperationException();
    }

    /**
     * Attests that the subject contains no elements that correspond to any of the given elements.
     * (Duplicates are irrelevant to this test, which fails if any of the subject elements
     * correspond to any of the given elements.)
     */
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private void containsNoneOf(@Nullable E first, @Nullable E second, @Nullable E... rest) {
      throw new UnsupportedOperationException();
    }

    /**
     * Attests that the subject contains no elements that correspond to any of the given elements.
     * (Duplicates are irrelevant to this test, which fails if any of the subject elements
     * correspond to any of the given elements.)
     */
    @SuppressWarnings("unused") // TODO(b/29966314): Implement this and make it public.
    private void containsNoneIn(Iterable<E> unused) {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Iterable<A> getCastSubject() {
      return (Iterable<A>) getSubject();
    }
  }
}
