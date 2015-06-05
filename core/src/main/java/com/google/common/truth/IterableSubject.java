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
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Propositions for {@link Iterable} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public class IterableSubject<S extends IterableSubject<S, T, C>, T, C extends Iterable<T>>
    extends Subject<S, C> {
  @SuppressWarnings({"unchecked", "rawtypes"})
  static <T, C extends Iterable<T>>
      IterableSubject<? extends IterableSubject<?, T, C>, T, C> create(
          FailureStrategy failureStrategy, @Nullable Iterable<T> list) {
    return new IterableSubject(failureStrategy, list);
  }

  protected IterableSubject(FailureStrategy failureStrategy, @Nullable C list) {
    super(failureStrategy, list);
  }

  /**
   * Fails if the subject is not empty.
   */
  public final void isEmpty() {
    if (!Iterables.isEmpty(getSubject())) {
      fail("is empty");
    }
  }

  /**
   * Fails if the subject is empty.
   */
  public final void isNotEmpty() {
    if (Iterables.isEmpty(getSubject())) {
      // TODO(kak): "Not true that <[]> is not empty" doesn't really need the <[]>,
      // since it's empty. But would the bulkier "the subject" really be better?
      // At best, we could *replace* <[]> with a given label (rather than supplementing it).
      // Perhaps the right failure message is just "<[]> should not have been empty"
      fail("is not empty");
    }
  }

  /**
   * Fails if the subject does not have the given size.
   */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = Iterables.size(getSubject());
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /**
   * Attests (with a side-effect failure) that the subject contains the
   * supplied item.
   */
  public final void contains(@Nullable Object element) {
    if (!Iterables.contains(getSubject(), element)) {
      failWithRawMessage("%s should have contained <%s>", getDisplaySubject(), element);
    }
  }

  /**
   * Attests (with a side-effect failure) that the subject does not contain
   * the supplied item.
   */
  public final void doesNotContain(@Nullable Object element) {
    if (Iterables.contains(getSubject(), element)) {
      failWithRawMessage("%s should not have contained <%s>", getDisplaySubject(), element);
    }
  }

  /**
   * Attests that the subject does not contain duplicate elements.
   */
  public final void containsNoDuplicates() {
    List<Entry<T>> duplicates = Lists.newArrayList();
    for (Multiset.Entry<T> entry : LinkedHashMultiset.create(getSubject()).entrySet()) {
      if (entry.getCount() > 1) {
        duplicates.add(entry);
      }
    }
    if (!duplicates.isEmpty()) {
      failWithRawMessage("%s has the following duplicates: <%s>", getDisplaySubject(), duplicates);
    }
  }

  /**
   * Attests that the subject contains at least one of the provided objects
   * or fails.
   */
  public final void containsAnyOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
    containsAny("contains any of", accumulate(first, second, rest));
  }

  /**
   * Attests that a Collection contains at least one of the objects contained
   * in the provided collection or fails.
   */
  public final void containsAnyIn(Iterable<?> expected) {
    containsAny("contains any element in", expected);
  }

  private void containsAny(String failVerb, Iterable<?> expected) {
    Collection<T> subject;
    if (getSubject() instanceof Collection) {
      // Should be safe to assume that any Iterable implementing Collection isn't a one-shot
      // iterable, right? I sure hope so.
      subject = (Collection<T>) getSubject();
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
   * Attests that the subject contains at least all of the provided objects
   * or fails, potentially permitting duplicates in both the subject and the
   * parameters (if the subject even can have duplicates).
   *
   * <p>Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
  public final Ordered containsAllOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
    return containsAll("contains all of", accumulate(first, second, rest));
  }

  /**
   * Attests that the subject contains at least all of the provided objects
   * or fails, potentially permitting duplicates in both the subject and the
   * parameters (if the subject even can have duplicates).
   *
   * <p>Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
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
   * Removes at most the given number of available elements from the input list
   * and adds them to the given output collection.
   */
  private static void moveElements(List<?> input, Collection<Object> output, int maxElements) {
    for (int i = 0; i < maxElements; i++) {
      output.add(input.remove(0));
    }
  }

  /**
   * Attests that a subject contains exactly the provided objects or fails.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3
   * times in the parameters asserts that the object must likewise be duplicated
   * exactly 3 times in the subject.
   *
   * <p>Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
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
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3
   * times in the {@code Iterable} parameter asserts that the object must
   * likewise be duplicated exactly 3 times in the subject.
   *
   * <p>Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
  public final Ordered containsExactlyElementsIn(Iterable<?> expected) {
    return containsExactly("contains exactly", expected, false);
  }

  private Ordered containsExactly(
      String failVerb, Iterable<?> required, boolean addElementsInWarning) {
    String failSuffix = addElementsInWarning
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
        ((actual == null) ? "null reference" : actual),
        suffix);
  }

  /**
   * Attests that a subject contains none of the provided objects
   * or fails, eliding duplicates.
   */
  public final void containsNoneOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
    containsNone("contains none of", accumulate(first, second, rest));
  }

  /**
   * Attests that a Collection contains none of the objects contained
   * in the provided collection or fails, eliding duplicates.
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

  /**
   * Ordered implementation that always fails.
   */
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

  /**
   * Ordered implementation that does nothing because it's already known to be true.
   */
  private static final Ordered IN_ORDER =
      new Ordered() {
        @Override
        public void inOrder() {}
      };

  /**
   * Fails if the list is not strictly ordered according to the natural ordering of its elements.
   * Null elements are not permitted.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public final void isStrictlyOrdered() {
    isStrictlyOrdered((Ordering) Ordering.natural());
  }

  /**
   * Fails if the list is not strictly ordered according to the given comparator.
   * Null elements are not permitted.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public final void isStrictlyOrdered(final Comparator<? super T> comparator) {
    checkNotNull(comparator);
    pairwiseCheck(
        new PairwiseChecker<T>() {
          @Override
          public void check(T prev, T next) {
            if (comparator.compare(prev, next) >= 0) {
              fail("is strictly ordered", prev, next);
            }
          }
        });
  }

  /**
   * Fails if the list is not ordered according to the natural ordering of its elements.
   * Null elements are not permitted.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public final void isOrdered() {
    isOrdered((Ordering) Ordering.natural());
  }

  /**
   * Fails if the list is not ordered according to the natural ordering of its elements.
   * Null elements are not permitted.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   * @deprecated Use {@link #isOrdered} instead.
   */
  @Deprecated
  public final void isPartiallyOrdered() {
    isOrdered();
  }

  /**
   * Fails if the list is not ordered according to the given comparator.
   * Null elements are not permitted.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public final void isOrdered(final Comparator<? super T> comparator) {
    checkNotNull(comparator);
    pairwiseCheck(
        new PairwiseChecker<T>() {
          @Override
          public void check(T prev, T next) {
            if (comparator.compare(prev, next) > 0) {
              fail("is partially ordered", prev, next);
            }
          }
        });
  }

  /**
   * Fails if the list is not ordered according to the given comparator.
   * Null elements are not permitted.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   * @deprecated Use {@link #isOrdered(Comparator)} instead.
   */
  @Deprecated
  public final void isPartiallyOrdered(final Comparator<? super T> comparator) {
    isOrdered(comparator);
  }

  private interface PairwiseChecker<T> {
    void check(T prev, T next);
  }

  private void pairwiseCheck(PairwiseChecker<T> checker) {
    Iterator<T> iterator = getSubject().iterator();
    if (iterator.hasNext()) {
      T prev = iterator.next();
      while (iterator.hasNext()) {
        T next = iterator.next();
        checker.check(prev, next);
        prev = next;
      }
    }
  }
}
