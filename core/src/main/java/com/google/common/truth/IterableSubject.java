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
import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.IterableSubject.ElementFactGrouping.ALL_IN_ONE_FACT;
import static com.google.common.truth.IterableSubject.ElementFactGrouping.FACT_PER_ELEMENT;
import static com.google.common.truth.SubjectUtils.accumulate;
import static com.google.common.truth.SubjectUtils.annotateEmptyStrings;
import static com.google.common.truth.SubjectUtils.countDuplicatesAndAddTypeInfo;
import static com.google.common.truth.SubjectUtils.countDuplicatesAndMaybeAddTypeInfoReturnObject;
import static com.google.common.truth.SubjectUtils.entryString;
import static com.google.common.truth.SubjectUtils.hasMatchingToStringPair;
import static com.google.common.truth.SubjectUtils.iterableToCollection;
import static com.google.common.truth.SubjectUtils.iterableToList;
import static com.google.common.truth.SubjectUtils.objectToTypeName;
import static com.google.common.truth.SubjectUtils.retainMatchingToString;
import static java.util.Arrays.asList;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
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
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.truth.SubjectUtils.DuplicateGroupedAndTyped;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Propositions for {@link Iterable} subjects.
 *
 * <p><b>Note:</b>
 *
 * <ul>
 *   <li>Assertions may iterate through the given {@link Iterable} more than once. If you have an
 *       unusual implementation of {@link Iterable} which does not support multiple iterations
 *       (sometimes known as a "one-shot iterable"), you must copy your iterable into a collection
 *       which does (e.g. {@code ImmutableList.copyOf(iterable)} or, if your iterable may contain
 *       null, {@code newArrayList(iterable)}). If you don't, you may see surprising failures.
 *   <li>Assertions may also require that the elements in the given {@link Iterable} implement
 *       {@link Object#hashCode} correctly.
 * </ul>
 *
 * @author Kurt Alfred Kluever
 * @author Pete Gillin
 */
// Can't be final since MultisetSubject and SortedSetSubject extend it
public class IterableSubject extends Subject<IterableSubject, Iterable<?>> {

  // TODO(kak): Make this package-protected?
  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check}{@code .that(actual)}.
   */
  protected IterableSubject(FailureMetadata metadata, @NullableDecl Iterable<?> iterable) {
    super(metadata, iterable);
  }

  @Override
  protected String actualCustomStringRepresentation() {
    if (actual() != null) {
      // Check the value of iterable.toString() against the default Object.toString() implementation
      // so we can avoid things like "com.google.common.graph.Traverser$GraphTraverser$1@5e316c74"
      String objectToString =
          actual().getClass().getName()
              + '@'
              + Integer.toHexString(System.identityHashCode(actual()));
      if (actual().toString().equals(objectToString)) {
        return Iterables.toString(actual());
      }
    }
    return super.actualCustomStringRepresentation();
  }

  /** Fails if the subject is not empty. */
  public final void isEmpty() {
    if (!Iterables.isEmpty(actual())) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Fails if the subject is empty. */
  public final void isNotEmpty() {
    if (Iterables.isEmpty(actual())) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Fails if the subject does not have the given size. */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = size(actual());
    check("size()").that(actualSize).isEqualTo(expectedSize);
  }

  /** Checks (with a side-effect failure) that the subject contains the supplied item. */
  public final void contains(@NullableDecl Object element) {
    if (!Iterables.contains(actual(), element)) {
      List<Object> elementList = newArrayList(element);
      if (hasMatchingToStringPair(actual(), elementList)) {
        failWithoutActual(
            fact("expected to contain", element),
            fact("an instance of", objectToTypeName(element)),
            simpleFact("but did not"),
            fact(
                "though it did contain",
                countDuplicatesAndAddTypeInfo(
                    retainMatchingToString(actual(), elementList /* itemsToCheck */))),
            fullContents());
      } else {
        failWithActual("expected to contain", element);
      }
    }
  }

  /** Checks (with a side-effect failure) that the subject does not contain the supplied item. */
  public final void doesNotContain(@NullableDecl Object element) {
    if (Iterables.contains(actual(), element)) {
      failWithActual("expected not to contain", element);
    }
  }

  /** Checks that the subject does not contain duplicate elements. */
  public final void containsNoDuplicates() {
    List<Entry<?>> duplicates = newArrayList();
    for (Multiset.Entry<?> entry : LinkedHashMultiset.create(actual()).entrySet()) {
      if (entry.getCount() > 1) {
        duplicates.add(entry);
      }
    }
    if (!duplicates.isEmpty()) {
      failWithoutActual(
          simpleFact("expected not to contain duplicates"),
          fact("but contained", duplicates),
          fullContents());
    }
  }

  /** Checks that the subject contains at least one of the provided objects or fails. */
  public final void containsAnyOf(
      @NullableDecl Object first, @NullableDecl Object second, @NullableDecl Object... rest) {
    containsAnyIn(accumulate(first, second, rest));
  }

  /**
   * Checks that the subject contains at least one of the objects contained in the provided
   * collection or fails.
   */
  // TODO(cpovirk): Consider using makeElementFacts-style messages here, in contains(), etc.
  public final void containsAnyIn(Iterable<?> expected) {
    Collection<?> actual = iterableToCollection(actual());
    for (Object item : expected) {
      if (actual.contains(item)) {
        return;
      }
    }
    if (hasMatchingToStringPair(actual, expected)) {
      failWithoutActual(
          fact("expected to contain any of", countDuplicatesAndAddTypeInfo(expected)),
          simpleFact("but did not"),
          fact(
              "though it did contain",
              countDuplicatesAndAddTypeInfo(
                  retainMatchingToString(actual(), expected /* itemsToCheck */))),
          fullContents());
    } else {
      failWithActual("expected to contain any of", expected);
    }
  }

  /**
   * Checks that the subject contains at least one of the objects contained in the provided array or
   * fails.
   */
  public final void containsAnyIn(Object[] expected) {
    containsAnyIn(asList(expected));
  }

  /**
   * Checks that the actual iterable contains at least all of the expected elements or fails. If an
   * element appears more than once in the expected elements to this call then it must appear at
   * least that number of times in the actual elements.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The expected elements must appear in the given order
   * within the actual elements, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  public final Ordered containsAllOf(
      @NullableDecl Object firstExpected,
      @NullableDecl Object secondExpected,
      @NullableDecl Object... restOfExpected) {
    return containsAllIn(accumulate(firstExpected, secondExpected, restOfExpected));
  }

  /**
   * Checks that the actual iterable contains at least all of the expected elements or fails. If an
   * element appears more than once in the expected elements then it must appear at least that
   * number of times in the actual elements.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The expected elements must appear in the given order
   * within the actual elements, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  public final Ordered containsAllIn(Iterable<?> expectedIterable) {
    List<?> actual = Lists.newLinkedList(actual());
    final Collection<?> expected = iterableToCollection(expectedIterable);

    List<Object> missing = newArrayList();
    List<Object> actualNotInOrder = newArrayList();

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
      return failAllIn(expected, missing);
    }

    /*
     * TODO(cpovirk): In the NotInOrder case, also include a Fact that shows _only_ the required
     * elements (that is, without any extras) but in the order they were actually found. That should
     * make it easier for users to compare the actual order of the required elements to the expected
     * order. Or, if that's too much trouble, at least try to find a better title for the full
     * actual iterable than the default of "but was," which may _sound_ like it should show only the
     * required elements, rather than the full actual iterable.
     */
    return ordered
        ? IN_ORDER
        : new Ordered() {
          @Override
          public void inOrder() {
            failWithActual(
                simpleFact("required elements were all found, but order was wrong"),
                fact("expected order for required elements", expected));
          }
        };
  }

  private Ordered failAllIn(Collection<?> expected, Collection<?> missingRawObjects) {
    Collection<?> nearMissRawObjects =
        retainMatchingToString(actual(), missingRawObjects /* itemsToCheck */);

    ImmutableList.Builder<Fact> facts = ImmutableList.builder();
    facts.addAll(
        makeElementFactsForBoth(
            "missing", missingRawObjects, "though it did contain", nearMissRawObjects));
    /*
     * TODO(cpovirk): Make makeElementFactsForBoth support generating just "though it did contain"
     * rather than "though it did contain (2)?" Users might interpret the number as the *total*
     * number of actual elements (or the total number of non-matched elements). (Frankly, they might
     * think that even *without* the number.... Can we do better than the phrase "though it did
     * contain," which has been our standard so far?) Or maybe it's all clear enough in context,
     * since this error shows up only to inform users of type mismatches.
     */
    facts.add(fact("expected to contain at least", expected));
    facts.add(butWas());

    failWithoutActual(facts.build());
    return ALREADY_FAILED;
  }

  /**
   * Checks that the actual iterable contains at least all of the expected elements or fails. If an
   * element appears more than once in the expected elements then it must appear at least that
   * number of times in the actual elements.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The expected elements must appear in the given order
   * within the actual elements, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  public final Ordered containsAllIn(Object[] expected) {
    return containsAllIn(asList(expected));
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
   * Checks that a subject contains exactly the provided objects or fails.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3 times in the
   * parameters asserts that the object must likewise be duplicated exactly 3 times in the subject.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   *
   * <p>To test that the iterable contains the same elements as an array, prefer {@link
   * #containsExactlyElementsIn(Object[])}. It makes clear that the given array is a list of
   * elements, not an element itself. This helps human readers and avoids a compiler warning.
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactly(@NullableDecl Object... varargs) {
    List<Object> expected = (varargs == null) ? newArrayList((Object) null) : asList(varargs);
    return containsExactlyElementsIn(
        expected, varargs != null && varargs.length == 1 && varargs[0] instanceof Iterable);
  }

  /**
   * Checks that a subject contains exactly the provided objects or fails.
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
    return containsExactlyElementsIn(expected, false);
  }

  /**
   * Checks that a subject contains exactly the provided objects or fails.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3 times in the array
   * parameter asserts that the object must likewise be duplicated exactly 3 times in the subject.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactlyElementsIn(Object[] expected) {
    return containsExactlyElementsIn(asList(expected));
  }

  private Ordered containsExactlyElementsIn(
      final Iterable<?> required, boolean addElementsInWarning) {
    Iterator<?> actualIter = actual().iterator();
    Iterator<?> requiredIter = required.iterator();

    if (!requiredIter.hasNext()) {
      if (actualIter.hasNext()) {
        isEmpty(); // fails
        return ALREADY_FAILED;
      } else {
        return IN_ORDER;
      }
    }

    // Step through both iterators comparing elements pairwise.
    boolean isFirst = true;
    while (actualIter.hasNext() && requiredIter.hasNext()) {
      Object actualElement = actualIter.next();
      Object requiredElement = requiredIter.next();

      // As soon as we encounter a pair of elements that differ, we know that inOrder()
      // cannot succeed, so we can check the rest of the elements more normally.
      // Since any previous pairs of elements we iterated over were equal, they have no
      // effect on the result now.
      if (!Objects.equal(actualElement, requiredElement)) {
        if (isFirst && !actualIter.hasNext() && !requiredIter.hasNext()) {
          /*
           * There's exactly one actual element and exactly one expected element, and they don't
           * match, so throw a ComparisonFailure. The logical way to do that would be
           * `check(...).that(actualElement).isEqualTo(requiredElement)`. But isEqualTo has magic
           * behavior for arrays and primitives, behavior that's inconsistent with how this method
           * otherwise behaves. For consistency, we want to rely only on the equal() call we've
           * already made. So we expose a special method for this and call it from here.
           *
           * TODO(cpovirk): Consider always throwing ComparisonFailure if there is exactly one
           * missing and exactly one extra element, even if there were additional (matching)
           * elements. However, this will probably be useful less often, and it will be tricky to
           * explain. First, what would we say, "value of: iterable.onlyElementThatDidNotMatch()?"
           * And second, it feels weirder to call out a single element when the expected and actual
           * values had multiple elements. Granted, Fuzzy Truth already does this, so maybe it's OK?
           * But Fuzzy Truth doesn't (yet) make the mismatched value so prominent.
           */
          checkNoNeedToDisplayBothValues("onlyElement()")
              .that(actualElement)
              .failEqualityCheckForEqualsWithoutDescription(requiredElement);
          return ALREADY_FAILED;
        }
        // Missing elements; elements that are not missing will be removed as we iterate.
        Collection<Object> missing = newArrayList();
        missing.add(requiredElement);
        Iterators.addAll(missing, requiredIter);

        // Extra elements that the subject had but shouldn't have.
        Collection<Object> extra = newArrayList();

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

        if (missing.isEmpty() && extra.isEmpty()) {
          /*
           * This containsExactly() call is a success. But the iterables were not in the same order,
           * so return an object that will fail the test if the user calls inOrder().
           */
          return new Ordered() {
            @Override
            public void inOrder() {
              failWithActual(
                  simpleFact("contents match, but order was wrong"), fact("expected", required));
            }
          };
        }
        return failExactly(required, addElementsInWarning, missing, extra);
      }

      isFirst = false;
    }

    // Here,  we must have reached the end of one of the iterators without finding any
    // pairs of elements that differ. If the actual iterator still has elements, they're
    // extras. If the required iterator has elements, they're missing elements.
    if (actualIter.hasNext()) {
      return failExactly(
          required,
          addElementsInWarning,
          /* missingRawObjects= */ ImmutableList.of(),
          /* extraRawObjects= */ newArrayList(actualIter));
    } else if (requiredIter.hasNext()) {
      return failExactly(
          required,
          addElementsInWarning,
          /* missingRawObjects= */ newArrayList(requiredIter),
          /* extraRawObjects= */ ImmutableList.of());
    }

    // If neither iterator has elements, we reached the end and the elements were in
    // order, so inOrder() can just succeed.
    return IN_ORDER;
  }

  private Ordered failExactly(
      Iterable<?> required,
      boolean addElementsInWarning,
      Collection<?> missingRawObjects,
      Collection<?> extraRawObjects) {
    ImmutableList.Builder<Fact> facts = ImmutableList.builder();
    facts.addAll(
        makeElementFactsForBoth("missing", missingRawObjects, "unexpected", extraRawObjects));
    facts.add(fact("expected", required));
    facts.add(butWas());
    if (addElementsInWarning) {
      facts.add(
          simpleFact(
              "Passing an iterable to the varargs method containsExactly(Object...) is "
                  + "often not the correct thing to do. Did you mean to call "
                  + "containsExactlyElementsIn(Iterable) instead?"));
    }

    failWithoutActual(facts.build());
    return ALREADY_FAILED;
  }

  private static ImmutableList<Fact> makeElementFactsForBoth(
      String firstKey,
      Collection<?> firstCollection,
      String secondKey,
      Collection<?> secondCollection) {
    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the subject but not enough times. Similarly for unexpected extra items.
    boolean addTypeInfo = hasMatchingToStringPair(firstCollection, secondCollection);
    DuplicateGroupedAndTyped first =
        countDuplicatesAndMaybeAddTypeInfoReturnObject(firstCollection, addTypeInfo);
    DuplicateGroupedAndTyped second =
        countDuplicatesAndMaybeAddTypeInfoReturnObject(secondCollection, addTypeInfo);
    ElementFactGrouping grouping = pickGrouping(first.entrySet(), second.entrySet());

    ImmutableList.Builder<Fact> facts = ImmutableList.builder();
    ImmutableList<Fact> firstFacts = makeElementFacts(firstKey, first, grouping);
    ImmutableList<Fact> secondFacts = makeElementFacts(secondKey, second, grouping);
    facts.addAll(firstFacts);
    if (firstFacts.size() > 1 && secondFacts.size() > 1) {
      facts.add(simpleFact(""));
    }
    facts.addAll(secondFacts);
    facts.add(simpleFact("---"));
    return facts.build();
  }

  /**
   * Returns a list of facts (zero, one, or many, depending on the number of elements and the
   * grouping policy) describing the given missing, unexpected, or near-miss elements.
   */
  private static ImmutableList<Fact> makeElementFacts(
      String label, DuplicateGroupedAndTyped elements, ElementFactGrouping grouping) {
    if (elements.isEmpty()) {
      return ImmutableList.of();
    }

    if (grouping == ALL_IN_ONE_FACT) {
      return ImmutableList.of(fact(keyToGoWithElementsString(label, elements), elements));
    }

    ImmutableList.Builder<Fact> facts = ImmutableList.builder();
    facts.add(simpleFact(keyToServeAsHeader(label, elements)));
    int n = 1;
    for (Multiset.Entry<?> entry : elements.entrySet()) {
      int count = entry.getCount();
      Object item = entry.getElement();
      facts.add(fact(numberString(n, count), item));
      n += count;
    }
    return facts.build();
  }

  /*
   * Fact keys like "missing (1)" go against our recommendation that keys should be fixed strings.
   * But this violation lets the fact value contain only the elements (instead of also containing
   * the count), so it feels worthwhile.
   */

  private static String keyToGoWithElementsString(String label, DuplicateGroupedAndTyped elements) {
    /*
     * elements.toString(), which the caller is going to use, includes the homogeneous type (if
     * any), so we don't want to include it here. (And it's better to have it in the value, rather
     * than in the key, so that it doesn't push the horizontally aligned values over too far.)
     */
    return lenientFormat("%s (%s)", label, elements.totalCopies());
  }

  private static String keyToServeAsHeader(String label, DuplicateGroupedAndTyped elements) {
    /*
     * The caller of this method outputs each individual element manually (as opposed to calling
     * elements.toString()), so the homogeneous type isn't present unless we add it. Fortunately, we
     * can add it here without pushing the horizontally aligned values over, as this key won't have
     * an associated value, so it won't factor into alignment.
     */
    String key = keyToGoWithElementsString(label, elements);
    if (elements.homogeneousTypeToDisplay.isPresent()) {
      key += " (" + elements.homogeneousTypeToDisplay.get() + ")";
    }
    return key;
  }

  private static String numberString(int n, int count) {
    return count == 1 ? lenientFormat("#%s", n) : lenientFormat("#%s [%s copies]", n, count);
  }

  private static ElementFactGrouping pickGrouping(
      Iterable<Entry<?>> first, Iterable<Entry<?>> second) {
    if (anyHasMultiple(first, second) && anyContainsCommaOrNewline(first, second)) {
      return FACT_PER_ELEMENT;
    }
    if (hasMultiple(first) && containsEmptyOrLong(first)) {
      return FACT_PER_ELEMENT;
    }
    if (hasMultiple(second) && containsEmptyOrLong(second)) {
      return FACT_PER_ELEMENT;
    }
    return ALL_IN_ONE_FACT;
  }

  private static boolean anyContainsCommaOrNewline(Iterable<Entry<?>>... lists) {
    for (Entry<?> entry : concat(lists)) {
      String s = String.valueOf(entry.getElement());
      if (s.contains("\n") || s.contains(",")) {
        return true;
      }
    }
    return false;
  }

  private static boolean anyHasMultiple(Iterable<Entry<?>>... lists) {
    for (Iterable<Entry<?>> list : lists) {
      if (hasMultiple(list)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasMultiple(Iterable<Entry<?>> list) {
    return totalCount(list) > 1;
  }

  private static boolean containsEmptyOrLong(Iterable<Entry<?>> entries) {
    int totalLength = 0;
    for (Multiset.Entry<?> entry : entries) {
      String s = entryString(entry);
      if (s.isEmpty()) {
        return true;
      }
      totalLength += s.length();
    }
    return totalLength > 200;
  }

  private static int totalCount(Iterable<Entry<?>> entries) {
    int totalCount = 0;
    for (Multiset.Entry<?> entry : entries) {
      totalCount += entry.getCount();
    }
    return totalCount;
  }

  /**
   * Whether to output each missing/unexpected item as its own {@link Fact} or to group all those
   * items together into a single {@code Fact}.
   */
  enum ElementFactGrouping {
    ALL_IN_ONE_FACT,
    FACT_PER_ELEMENT;
  }

  /**
   * Checks that a actual iterable contains none of the excluded objects or fails. (Duplicates are
   * irrelevant to this test, which fails if any of the actual elements equal any of the excluded.)
   */
  public final void containsNoneOf(
      @NullableDecl Object firstExcluded,
      @NullableDecl Object secondExcluded,
      @NullableDecl Object... restOfExcluded) {
    containsNoneIn(accumulate(firstExcluded, secondExcluded, restOfExcluded));
  }

  /**
   * Checks that the actual iterable contains none of the elements contained in the excluded
   * iterable or fails. (Duplicates are irrelevant to this test, which fails if any of the actual
   * elements equal any of the excluded.)
   */
  public final void containsNoneIn(Iterable<?> excluded) {
    Collection<?> actual = iterableToCollection(actual());
    Collection<Object> present = new ArrayList<>();
    for (Object item : Sets.newLinkedHashSet(excluded)) {
      if (actual.contains(item)) {
        present.add(item);
      }
    }
    if (!present.isEmpty()) {
      failWithoutActual(
          fact("expected not to contain any of", annotateEmptyStrings(excluded)),
          fact("but contained", annotateEmptyStrings(present)),
          fullContents());
    }
  }

  /**
   * Checks that the actual iterable contains none of the elements contained in the excluded array
   * or fails. (Duplicates are irrelevant to this test, which fails if any of the actual elements
   * equal any of the excluded.)
   */
  public final void containsNoneIn(Object[] excluded) {
    containsNoneIn(asList(excluded));
  }

  /** Ordered implementation that does nothing because it's already known to be true. */
  private static final Ordered IN_ORDER =
      new Ordered() {
        @Override
        public void inOrder() {}
      };

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED =
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
    isStrictlyOrdered(Ordering.natural());
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
        "expected to be strictly ordered",
        new PairwiseChecker() {
          @Override
          public boolean check(Object prev, Object next) {
            return ((Comparator<Object>) comparator).compare(prev, next) < 0;
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
    isOrdered(Ordering.natural());
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
        "expected to be ordered",
        new PairwiseChecker() {
          @Override
          public boolean check(Object prev, Object next) {
            return ((Comparator<Object>) comparator).compare(prev, next) <= 0;
          }
        });
  }

  private interface PairwiseChecker {
    boolean check(Object prev, Object next);
  }

  private void pairwiseCheck(String expectedFact, PairwiseChecker checker) {
    Iterator<?> iterator = actual().iterator();
    if (iterator.hasNext()) {
      Object prev = iterator.next();
      while (iterator.hasNext()) {
        Object next = iterator.next();
        if (!checker.check(prev, next)) {
          failWithoutActual(
              simpleFact(expectedFact),
              fact("but contained", prev),
              fact("followed by", next),
              fullContents());
          return;
        }
        prev = next;
      }
    }
  }

  /** @deprecated You probably meant to call {@link #containsNoneOf} instead. */
  @Override
  @Deprecated
  public void isNoneOf(
      @NullableDecl Object first, @NullableDecl Object second, @NullableDecl Object... rest) {
    super.isNoneOf(first, second, rest);
  }

  /** @deprecated You probably meant to call {@link #containsNoneIn} instead. */
  @Override
  @Deprecated
  public void isNotIn(Iterable<?> iterable) {
    if (Iterables.contains(iterable, actual())) {
      failWithActual("expected not to be any of", iterable);
    }
    List<Object> nonIterables = new ArrayList<>();
    for (Object element : iterable) {
      if (!(element instanceof Iterable<?>)) {
        nonIterables.add(element);
      }
    }
    if (!nonIterables.isEmpty()) {
      failWithRawMessage(
          "The actual value is an Iterable, and you've written a test that compares it to some "
              + "objects that are not Iterables. Did you instead mean to check whether its "
              + "*contents* match any of the *contents* of the given values? If so, call "
              + "containsNoneOf(...)/containsNoneIn(...) instead. Non-iterables: %s",
          nonIterables);
    }
  }

  private Fact fullContents() {
    return fact("full contents", actualCustomStringRepresentationForPackageMembersToCall());
  }

  /**
   * Starts a method chain for a check in which the actual elements (i.e. the elements of the {@link
   * Iterable} under test) are compared to expected elements using the given {@link Correspondence}.
   * The actual elements must be of type {@code A}, the expected elements must be of type {@code E}.
   * The check is actually executed by continuing the method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualIterable).comparingElementsUsing(correspondence).contains(expected);
   * }</pre>
   *
   * where {@code actualIterable} is an {@code Iterable<A>} (or, more generally, an {@code
   * Iterable<? extends A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and {@code
   * expected} is an {@code E}.
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual element that is not of type {@code A}.
   */
  public <A, E> UsingCorrespondence<A, E> comparingElementsUsing(
      Correspondence<A, E> correspondence) {
    return new UsingCorrespondence<>(this, correspondence);
  }

  /**
   * A partially specified check in which the actual elements (normally the elements of the {@link
   * Iterable} under test) are compared to expected elements using a {@link Correspondence}. The
   * expected elements are of type {@code E}. Call methods on this object to actually execute the
   * check.
   */
  public static class UsingCorrespondence<A, E> {

    private final IterableSubject subject;
    private final Correspondence<? super A, ? super E> correspondence;
    private final Optional<Pairer> pairer;

    UsingCorrespondence(
        IterableSubject subject, Correspondence<? super A, ? super E> correspondence) {
      this.subject = checkNotNull(subject);
      this.correspondence = checkNotNull(correspondence);
      this.pairer = Optional.absent();
    }

    UsingCorrespondence(
        IterableSubject subject,
        Correspondence<? super A, ? super E> correspondence,
        Pairer pairer) {
      this.subject = checkNotNull(subject);
      this.correspondence = checkNotNull(correspondence);
      this.pairer = Optional.of(pairer);
    }

    /**
     * Specifies a way to pair up unexpected and missing elements in the message when an assertion
     * fails. For example:
     *
     * <pre>{@code
     * assertThat(actualRecords)
     *     .comparingElementsUsing(RECORD_CORRESPONDENCE)
     *     .displayingDiffsPairedBy(Record::getId)
     *     .containsExactlyElementsIn(expectedRecords);
     * }</pre>
     *
     * <p><b>Important</b>: The {code keyFunction} function must be able to accept both the actual
     * and the unexpected elements, i.e. it must satisfy {@code Function<? super A, ? extends
     * Object>} as well as {@code Function<? super E, ? extends Object>}. If that constraint is not
     * met then a subsequent method may throw {@link ClassCastException}. Use the two-parameter
     * overload if you need to specify different key functions for the actual and expected elements.
     *
     * <p>On assertions where it makes sense to do so, the elements are paired as follows: they are
     * keyed by {@code keyFunction}, and if an unexpected element and a missing element have the
     * same non-null key then the they are paired up. (Elements with null keys are not paired.) The
     * failure message will show paired elements together, and a diff will be shown if the {@link
     * Correspondence#formatDiff} method returns non-null.
     *
     * <p>The expected elements given in the assertion should be uniquely keyed by {@link
     * keyFunction}. If multiple missing elements have the same key then the pairing will be
     * skipped.
     *
     * <p>Useful key functions will have the property that key equality is less strict than the
     * correspondence, i.e. given {@code actual} and {@code expected} values with keys {@code
     * actualKey} and {@code expectedKey}, if {@code correspondence.compare(actual, expected)} is
     * true then it is guaranteed that {@code actualKey} is equal to {@code expectedKey}, but there
     * are cases where {@code actualKey} is equal to {@code expectedKey} but {@code
     * correspondence.compare(actual, expected)} is false.
     *
     * <p>Note that calling this method makes no difference to whether a test passes or fails, it
     * just improves the message if it fails.
     */
    public UsingCorrespondence<A, E> displayingDiffsPairedBy(
        Function<? super E, ? extends Object> keyFunction) {
      @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
      Function<? super A, ? extends Object> actualKeyFunction =
          (Function<? super A, ? extends Object>) keyFunction;
      return displayingDiffsPairedBy(actualKeyFunction, keyFunction);
    }

    /**
     * Specifies a way to pair up unexpected and missing elements in the message when an assertion
     * fails. For example:
     *
     * <pre>{@code
     * assertThat(actualFoos)
     *     .comparingElementsUsing(FOO_BAR_CORRESPONDENCE)
     *     .displayingDiffsPairedBy(Foo::getId, Bar::getFooId)
     *     .containsExactlyElementsIn(expectedBar);
     * }</pre>
     *
     * <p>On assertions where it makes sense to do so, the elements are paired as follows: the
     * unexpected elements are keyed by {@code actualKeyFunction}, the missing elements are keyed by
     * {@code expectedKeyFunction}, and if an unexpected element and a missing element have the same
     * non-null key then the they are paired up. (Elements with null keys are not paired.) The
     * failure message will show paired elements together, and a diff will be shown if the {@link
     * Correspondence#formatDiff} method returns non-null.
     *
     * <p>The expected elements given in the assertion should be uniquely keyed by {@link
     * expectedKeyFunction}. If multiple missing elements have the same key then the pairing will be
     * skipped.
     *
     * <p>Useful key functions will have the property that key equality is less strict than the
     * correspondence, i.e. given {@code actual} and {@code expected} values with keys {@code
     * actualKey} and {@code expectedKey}, if {@code correspondence.compare(actual, expected)} is
     * true then it is guaranteed that {@code actualKey} is equal to {@code expectedKey}, but there
     * are cases where {@code actualKey} is equal to {@code expectedKey} but {@code
     * correspondence.compare(actual, expected)} is false.
     *
     * <p>Note that calling this method makes no difference to whether a test passes or fails, it
     * just improves the message if it fails.
     */
    public UsingCorrespondence<A, E> displayingDiffsPairedBy(
        Function<? super A, ? extends Object> actualKeyFunction,
        Function<? super E, ? extends Object> expectedKeyFunction) {
      return new UsingCorrespondence<>(
          subject, correspondence, new Pairer(actualKeyFunction, expectedKeyFunction));
    }

    /**
     * Checks that the subject contains at least one element that corresponds to the given expected
     * element.
     */
    public void contains(@NullableDecl E expected) {
      for (A actual : getCastActual()) {
        if (correspondence.compare(actual, expected)) {
          return;
        }
      }
      if (pairer.isPresent()) {
        List<A> keyMatches = pairer.get().pairOne(expected, getCastActual());
        if (!keyMatches.isEmpty()) {
          subject.failWithRawMessage(
              "Not true that %s contains exactly one element that %s <%s>. It did contain the "
                  + "following elements with the correct key: <%s>",
              subject.actualAsString(),
              correspondence,
              expected,
              formatExtras(expected, keyMatches));
          return;
        }
      }
      subject.fail("contains at least one element that " + correspondence, expected);
    }

    /** Checks that none of the actual elements correspond to the given element. */
    public void doesNotContain(@NullableDecl E excluded) {
      List<A> matchingElements = new ArrayList<>();
      for (A actual : getCastActual()) {
        if (correspondence.compare(actual, excluded)) {
          matchingElements.add(actual);
        }
      }
      if (!matchingElements.isEmpty()) {
        subject.failWithRawMessage(
            "%s should not have contained an element that %s <%s>. "
                + "It contained the following such elements: <%s>",
            subject.actualAsString(), correspondence, excluded, matchingElements);
      }
    }

    /**
     * Checks that subject contains exactly elements that correspond to the expected elements, i.e.
     * that there is a 1:1 mapping between the actual elements and the expected elements where each
     * pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method.
     *
     * <p>To test that the iterable contains the elements corresponding to those in an array, prefer
     * {@link #containsExactlyElementsIn(Object[])}. It makes clear that the given array is a list
     * of elements, not an element itself. This helps human readers and avoids a compiler warning.
     */
    @SafeVarargs
    @CanIgnoreReturnValue
    public final Ordered containsExactly(@NullableDecl E... expected) {
      return containsExactlyElementsIn(
          (expected == null) ? newArrayList((E) null) : asList(expected));
    }

    /**
     * Checks that subject contains exactly elements that correspond to the expected elements, i.e.
     * that there is a 1:1 mapping between the actual elements and the expected elements where each
     * pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method.
     */
    @CanIgnoreReturnValue
    public Ordered containsExactlyElementsIn(final Iterable<? extends E> expected) {
      List<A> actualList = iterableToList(getCastActual());
      List<? extends E> expectedList = iterableToList(expected);

      if (expectedList.isEmpty()) {
        if (actualList.isEmpty()) {
          return IN_ORDER;
        } else {
          subject.isEmpty(); // fails
          return ALREADY_FAILED;
        }
      }

      // Check if the elements correspond in order. This allows the common case of a passing test
      // using inOrder() to complete in linear time.
      if (correspondInOrderExactly(actualList.iterator(), expectedList.iterator())) {
        return IN_ORDER;
      }
      // We know they don't correspond in order, so we're going to have to do an any-order test.
      // Find a many:many mapping between the indexes of the elements which correspond, and check
      // it for completeness.
      ImmutableSetMultimap<Integer, Integer> candidateMapping =
          findCandidateMapping(actualList, expectedList);
      if (failIfCandidateMappingHasMissingOrExtra(actualList, expectedList, candidateMapping)) {
        return ALREADY_FAILED;
      }
      // We know that every expected element maps to at least one actual element, and vice versa.
      // Find a maximal 1:1 mapping, and check it for completeness.
      ImmutableBiMap<Integer, Integer> maximalOneToOneMapping =
          findMaximalOneToOneMapping(candidateMapping);
      if (failIfOneToOneMappingHasMissingOrExtra(
          actualList, expectedList, maximalOneToOneMapping)) {
        return ALREADY_FAILED;
      }
      // The 1:1 mapping is complete, so the test succeeds (but we know from above that the mapping
      // is not in order).
      return new Ordered() {
        @Override
        public void inOrder() {
          subject.failWithActual(
              simpleFact("contents match, but order was wrong"),
              simpleFact(
                  "comparing contents by testing that each element "
                      + correspondence
                      + " an expected value"),
              fact("expected", expected));
        }
      };
      /*
       * TODO(cpovirk): Revisit the above when we change the other failure messagse generated by
       * Fuzzy Truth. Maybe the correspondence should be the value in a key-value fact? But that may
       * mean we should change existing correspondence implementations to use a different phrasing,
       * so I'm punting for now.
       */
    }

    /**
     * Checks that subject contains exactly elements that correspond to the expected elements, i.e.
     * that there is a 1:1 mapping between the actual elements and the expected elements where each
     * pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method.
     */
    @CanIgnoreReturnValue
    public Ordered containsExactlyElementsIn(E[] expected) {
      return containsExactlyElementsIn(asList(expected));
    }

    /**
     * Returns whether the actual and expected iterators have the same number of elements and, when
     * iterated pairwise, every pair of actual and expected values satisfies the correspondence.
     */
    private boolean correspondInOrderExactly(
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
     * expected element and vice versa, and fails if this is not the case. Returns whether the
     * assertion failed.
     */
    private boolean failIfCandidateMappingHasMissingOrExtra(
        List<? extends A> actual,
        List<? extends E> expected,
        ImmutableSetMultimap<Integer, Integer> mapping) {
      List<? extends A> extra = findNotIndexed(actual, mapping.keySet());
      List<? extends E> missing = findNotIndexed(expected, mapping.inverse().keySet());
      if (!missing.isEmpty() || !extra.isEmpty()) {
        subject.failWithRawMessage(
            "Not true that %s contains exactly one element that %s each element of <%s>. It %s",
            subject.actualAsString(),
            correspondence,
            expected,
            describeMissingOrExtra(missing, extra));
        return true;
      }
      return false;
    }

    /**
     * Given a list of missing elements and a list of extra elements, at least one of which must be
     * non-empty, returns a verb phrase (suitable for appearing after the subject of the verb)
     * describing them.
     */
    private String describeMissingOrExtra(List<? extends E> missing, List<? extends A> extra) {
      if (pairer.isPresent()) {
        @NullableDecl Pairing pairing = pairer.get().pair(missing, extra);
        if (pairing != null) {
          return describeMissingOrExtraWithPairing(pairing);
        } else {
          return describeMissingOrExtraWithoutPairing(correspondence.toString(), missing, extra)
              + ". (N.B. A key function which does not uniquely key the expected elements was "
              + "provided and has consequently been ignored.)";
        }
      } else if (missing.size() == 1 && extra.size() >= 1) {
        return lenientFormat(
            "is missing an element that %s <%s> and has unexpected elements <%s>",
            correspondence, missing.get(0), formatExtras(missing.get(0), extra));
      } else {
        return describeMissingOrExtraWithoutPairing(correspondence.toString(), missing, extra);
      }
    }

    private String describeMissingOrExtraWithoutPairing(
        String verb, List<? extends E> missing, List<? extends A> extra) {
      List<String> messages = newArrayList();
      if (!missing.isEmpty()) {
        messages.add(
            lenientFormat("is missing an element that %s %s", verb, formatMissing(missing)));
      }
      if (!extra.isEmpty()) {
        messages.add(lenientFormat("has unexpected elements <%s>", extra));
      }
      return Joiner.on(" and ").join(messages);
    }

    private String describeMissingOrExtraWithPairing(Pairing pairing) {
      List<String> messages = newArrayList();
      for (Object key : pairing.pairedKeysToExpectedValues.keySet()) {
        E missing = pairing.pairedKeysToExpectedValues.get(key);
        List<A> extras = pairing.pairedKeysToActualValues.get(key);
        messages.add(
            lenientFormat(
                "is missing an element that corresponds to <%s> and has unexpected elements <%s> "
                    + "with key %s",
                missing, formatExtras(missing, extras), key));
      }
      if (!pairing.unpairedActualValues.isEmpty() || !pairing.unpairedExpectedValues.isEmpty()) {
        messages.add(
            describeMissingOrExtraWithoutPairing(
                    "corresponds to", pairing.unpairedExpectedValues, pairing.unpairedActualValues)
                + " without matching keys");
      }
      if (messages.size() > 1) {
        messages.set(messages.size() - 1, "and " + messages.get(messages.size() - 1));
      }
      return Joiner.on(", ").join(messages);
    }

    private List<String> formatExtras(E missing, List<? extends A> extras) {
      List<String> extrasFormatted = new ArrayList<>();
      for (A extra : extras) {
        @NullableDecl String diff = correspondence.formatDiff(extra, missing);
        if (diff != null) {
          extrasFormatted.add(lenientFormat("%s (diff: %s)", extra, diff));
        } else {
          extrasFormatted.add(extra.toString());
        }
      }
      return extrasFormatted;
    }

    /**
     * Returns all the elements of the given list other than those with the given indexes. Assumes
     * that all the given indexes really are valid indexes into the list.
     */
    private <T> List<T> findNotIndexed(List<T> list, Set<Integer> indexes) {
      if (indexes.size() == list.size()) {
        // If there are as many distinct valid indexes are there are elements in the list then every
        // index must be in there once.
        return ImmutableList.of();
      }
      List<T> notIndexed = newArrayList();
      for (int index = 0; index < list.size(); index++) {
        if (!indexes.contains(index)) {
          notIndexed.add(list.get(index));
        }
      }
      return notIndexed;
    }

    /**
     * Returns a description of the missing items suitable for inclusion in failure messages. If
     * there is a single item, returns {@code "<item>"}. Otherwise, returns {@code "each of <[item,
     * item, item]>"}.
     */
    private String formatMissing(List<?> missing) {
      if (missing.size() == 1) {
        return "<" + missing.get(0) + ">";
      } else {
        return "each of <" + missing + ">";
      }
    }

    /**
     * Given a many:many mapping between actual elements and expected elements, finds a 1:1 mapping
     * which is the subset of that many:many mapping which includes the largest possible number of
     * elements. The input and output mappings are each described as a map or multimap where the
     * keys are indexes into the actual list and the values are indexes into the expected list. If
     * there are multiple possible output mappings tying for the largest possible, this returns an
     * arbitrary one.
     */
    private ImmutableBiMap<Integer, Integer> findMaximalOneToOneMapping(
        ImmutableMultimap<Integer, Integer> edges) {
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
      return GraphMatching.maximumCardinalityBipartiteMatching(edges);
    }

    /**
     * Given a list of actual elements, a list of expected elements, and a 1:1 mapping between
     * actual and expected elements specified as a bimap of indexes into the actual list to indexes
     * into the expected list, checks that every actual element maps to an expected element and vice
     * versa, and fails if this is not the case. Returns whether the assertion failed.
     */
    private boolean failIfOneToOneMappingHasMissingOrExtra(
        List<? extends A> actual, List<? extends E> expected, BiMap<Integer, Integer> mapping) {
      List<? extends A> extra = findNotIndexed(actual, mapping.keySet());
      List<? extends E> missing = findNotIndexed(expected, mapping.values());
      if (!missing.isEmpty() || !extra.isEmpty()) {
        subject.failWithRawMessage(
            "Not true that %s contains exactly one element that %s each element of <%s>. "
                + "It contains at least one element that matches each expected element, "
                + "and every element it contains matches at least one expected element, "
                + "but there was no 1:1 mapping between all the actual and expected elements. "
                + "Using the most complete 1:1 mapping (or one such mapping, if there is a tie), "
                + "it %s",
            subject.actualAsString(),
            correspondence,
            expected,
            describeMissingOrExtra(missing, extra));
        return true;
      }
      return false;
    }

    /**
     * Checks that the subject contains elements that corresponds to all of the expected elements,
     * i.e. that there is a 1:1 mapping between any subset of the actual elements and the expected
     * elements where each pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method. The elements must appear in the given order within the
     * subject, but they are not required to be consecutive.
     */
    @SafeVarargs
    @CanIgnoreReturnValue
    public final Ordered containsAllOf(
        @NullableDecl E first, @NullableDecl E second, @NullableDecl E... rest) {
      return containsAllIn(accumulate(first, second, rest));
    }

    /**
     * Checks that the subject contains elements that corresponds to all of the expected elements,
     * i.e. that there is a 1:1 mapping between any subset of the actual elements and the expected
     * elements where each pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method. The elements must appear in the given order within the
     * subject, but they are not required to be consecutive.
     */
    @CanIgnoreReturnValue
    public Ordered containsAllIn(final Iterable<? extends E> expected) {
      List<A> actualList = iterableToList(getCastActual());
      List<? extends E> expectedList = iterableToList(expected);
      // Check if the expected elements correspond in order to any subset of the actual elements.
      // This allows the common case of a passing test using inOrder() to complete in linear time.
      if (correspondInOrderAllIn(actualList.iterator(), expectedList.iterator())) {
        return IN_ORDER;
      }
      // We know they don't correspond in order, so we're going to have to do an any-order test.
      // Find a many:many mapping between the indexes of the elements which correspond, and check
      // it for completeness.
      ImmutableSetMultimap<Integer, Integer> candidateMapping =
          findCandidateMapping(actualList, expectedList);
      if (failIfCandidateMappingHasMissing(actualList, expectedList, candidateMapping)) {
        return ALREADY_FAILED;
      }
      // We know that every expected element maps to at least one actual element, and vice versa.
      // Find a maximal 1:1 mapping, and check it for completeness.
      ImmutableBiMap<Integer, Integer> maximalOneToOneMapping =
          findMaximalOneToOneMapping(candidateMapping);
      if (failIfOneToOneMappingHasMissing(actualList, expectedList, maximalOneToOneMapping)) {
        return ALREADY_FAILED;
      }
      // The 1:1 mapping maps all the expected elements, so the test succeeds (but we know from
      // above that the mapping is not in order).
      return new Ordered() {
        @Override
        public void inOrder() {
          subject.failWithActual(
              simpleFact("required elements were all found, but order was wrong"),
              simpleFact(
                  "comparing contents by testing that each element "
                      + correspondence
                      + " an expected value"),
              fact("expected order for required elements", expected));
        }
      };
    }

    /**
     * Checks that the subject contains elements that corresponds to all of the expected elements,
     * i.e. that there is a 1:1 mapping between any subset of the actual elements and the expected
     * elements where each pair of elements correspond.
     *
     * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
     * on the object returned by this method. The elements must appear in the given order within the
     * subject, but they are not required to be consecutive.
     */
    @CanIgnoreReturnValue
    public Ordered containsAllIn(E[] expected) {
      return containsAllIn(asList(expected));
    }

    /**
     * Returns whether all the elements of the expected iterator and any subset of the elements of
     * the actual iterator can be paired up in order, such that every pair of actual and expected
     * elements satisfies the correspondence.
     */
    private boolean correspondInOrderAllIn(
        Iterator<? extends A> actual, Iterator<? extends E> expected) {
      // We take a greedy approach here, iterating through the expected elements and pairing each
      // with the first applicable actual element. This is fine for the in-order test, since there's
      // no way that paring an expected element with a later actual element permits a solution which
      // couldn't be achieved by pairing it with the first. (For the any-order test, we may want to
      // pair an expected element with a later actual element so that we can pair the earlier actual
      // element with a later expected element, but that doesn't apply here.)
      while (expected.hasNext()) {
        E expectedElement = expected.next();
        if (!findCorresponding(actual, expectedElement)) {
          return false;
        }
      }
      return true;
    }

    /**
     * Advances the actual iterator looking for an element which corresponds to the expected
     * element. Returns whether or not it finds one.
     */
    private boolean findCorresponding(Iterator<? extends A> actual, E expectedElement) {
      while (actual.hasNext()) {
        A actualElement = actual.next();
        if (correspondence.compare(actualElement, expectedElement)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Given a list of actual elements, a list of expected elements, and a many:many mapping between
     * actual and expected elements specified as a multimap of indexes into an actual list to
     * indexes into the expected list, checks that every expected element maps to at least one
     * actual element, and fails if this is not the case. Actual elements which do not map to any
     * expected elements are ignored.
     */
    private boolean failIfCandidateMappingHasMissing(
        List<? extends A> actual,
        List<? extends E> expected,
        ImmutableSetMultimap<Integer, Integer> mapping) {
      List<? extends E> missing = findNotIndexed(expected, mapping.inverse().keySet());
      if (!missing.isEmpty()) {
        List<? extends A> extra = findNotIndexed(actual, mapping.keySet());
        subject.failWithRawMessage(
            "Not true that %s contains at least one element that %s each element of <%s>. It %s",
            subject.actualAsString(), correspondence, expected, describeMissing(missing, extra));
        return true;
      }
      return false;
    }

    /**
     * Given a list of missing elements, which must be non-empty, and a list of extra elements,
     * returns a verb phrase (suitable for appearing after the subject of the verb) describing the
     * missing elements, diffing against the extra ones where appropriate.
     */
    private String describeMissing(List<? extends E> missing, List<? extends A> extra) {
      if (pairer.isPresent()) {
        @NullableDecl Pairing pairing = pairer.get().pair(missing, extra);
        if (pairing != null) {
          return describeMissingWithPairing(pairing);
        } else {
          return describeMissingWithoutPairing(correspondence.toString(), missing)
              + ". (N.B. A key function which does not uniquely key the expected elements was "
              + "provided and has consequently been ignored.)";
        }
      } else {
        // N.B. For containsAny, we do not treat having exactly one missing element as a special
        // case (as we do for containsExactly). Showing extra elements has lower utility for
        // containsAny (because they are allowed by the assertion) so we only show them if the user
        // has explicitly opted in by specifying a pairing.
        return describeMissingWithoutPairing(correspondence.toString(), missing);
      }
    }

    private String describeMissingWithoutPairing(String verb, List<? extends E> missing) {
      return lenientFormat("is missing an element that %s %s", verb, formatMissing(missing));
    }

    private String describeMissingWithPairing(Pairing pairing) {
      List<String> messages = newArrayList();
      for (Object key : pairing.pairedKeysToExpectedValues.keySet()) {
        E missing = pairing.pairedKeysToExpectedValues.get(key);
        List<A> extras = pairing.pairedKeysToActualValues.get(key);
        messages.add(
            lenientFormat(
                "is missing an element that corresponds to <%s> (but did have elements <%s> with "
                    + "matching key %s)",
                missing, formatExtras(missing, extras), key));
      }
      if (!pairing.unpairedExpectedValues.isEmpty()) {
        messages.add(
            describeMissingWithoutPairing("corresponds to", pairing.unpairedExpectedValues)
                + " (without matching keys)");
      }
      if (messages.size() > 1) {
        messages.set(messages.size() - 1, "and " + messages.get(messages.size() - 1));
      }
      return Joiner.on(", ").join(messages);
    }

    /**
     * Given a list of expected elements, and a 1:1 mapping between actual and expected elements
     * specified as a bimap of indexes into an actual list to indexes into the expected list, checks
     * that every expected element maps to an actual element. Actual elements which do not map to
     * any expected elements are ignored.
     */
    private boolean failIfOneToOneMappingHasMissing(
        List<? extends A> actual, List<? extends E> expected, BiMap<Integer, Integer> mapping) {
      List<? extends E> missing = findNotIndexed(expected, mapping.values());
      if (!missing.isEmpty()) {
        List<? extends A> extra = findNotIndexed(actual, mapping.keySet());
        subject.failWithRawMessage(
            "Not true that %s contains at least one element that %s each element of <%s>. "
                + "It contains at least one element that matches each expected element, "
                + "but there was no 1:1 mapping between all the expected elements and any subset "
                + "of the actual elements. Using the most complete 1:1 mapping (or one such "
                + "mapping, if there is a tie), it %s",
            subject.actualAsString(), correspondence, expected, describeMissing(missing, extra));
        return true;
      }
      return false;
    }

    /**
     * Checks that the subject contains at least one element that corresponds to at least one of the
     * expected elements.
     */
    @SafeVarargs
    public final void containsAnyOf(
        @NullableDecl E first, @NullableDecl E second, @NullableDecl E... rest) {
      containsAny(
          lenientFormat("contains at least one element that %s any of", correspondence),
          accumulate(first, second, rest));
    }

    /**
     * Checks that the subject contains at least one element that corresponds to at least one of the
     * expected elements.
     */
    public void containsAnyIn(Iterable<? extends E> expected) {
      containsAny(
          lenientFormat("contains at least one element that %s any element in", correspondence),
          expected);
    }

    /**
     * Checks that the subject contains at least one element that corresponds to at least one of the
     * expected elements.
     */
    public void containsAnyIn(E[] expected) {
      containsAnyIn(asList(expected));
    }

    private void containsAny(String failVerb, Iterable<? extends E> expected) {
      Collection<A> actual = iterableToCollection(getCastActual());
      for (E expectedItem : expected) {
        for (A actualItem : actual) {
          if (correspondence.compare(actualItem, expectedItem)) {
            return;
          }
        }
      }
      if (pairer.isPresent()) {
        @NullableDecl
        Pairing pairing = pairer.get().pair(iterableToList(expected), iterableToList(actual));
        if (pairing != null) {
          if (!pairing.pairedKeysToExpectedValues.isEmpty()) {
            subject.failWithRawMessage(
                "Not true that %s %s <%s>. It contains the following values that match by key: %s",
                subject.actualAsString(), failVerb, expected, describeAnyMatchesByKey(pairing));
          } else {
            subject.failWithRawMessage(
                "Not true that %s %s <%s>. It does not contain any matches by key, either",
                subject.actualAsString(), failVerb, expected);
          }
        } else {
          subject.failWithRawMessage(
              "Not true that %s %s <%s>. (N.B. A key function which does not uniquely key the "
                  + "expected elements was provided and has consequently been ignored.)",
              subject.actualAsString(), failVerb, expected);
        }
      } else {
        subject.fail(failVerb, expected);
      }
    }

    private String describeAnyMatchesByKey(Pairing pairing) {
      List<String> messages = newArrayList();
      for (Object key : pairing.pairedKeysToExpectedValues.keySet()) {
        E expected = pairing.pairedKeysToExpectedValues.get(key);
        List<A> got = pairing.pairedKeysToActualValues.get(key);
        messages.add(
            lenientFormat(
                "with key %s, would have accepted %s, but got %s",
                key, expected, formatExtras(expected, got)));
      }
      return Joiner.on("; ").join(messages);
    }

    /**
     * Checks that the subject contains no elements that correspond to any of the given elements.
     * (Duplicates are irrelevant to this test, which fails if any of the subject elements
     * correspond to any of the given elements.)
     */
    @SafeVarargs
    public final void containsNoneOf(
        @NullableDecl E firstExcluded,
        @NullableDecl E secondExcluded,
        @NullableDecl E... restOfExcluded) {
      containsNone("any of", accumulate(firstExcluded, secondExcluded, restOfExcluded));
    }

    /**
     * Checks that the subject contains no elements that correspond to any of the given elements.
     * (Duplicates are irrelevant to this test, which fails if any of the subject elements
     * correspond to any of the given elements.)
     */
    public void containsNoneIn(Iterable<? extends E> excluded) {
      containsNone("any element in", excluded);
    }

    /**
     * Checks that the subject contains no elements that correspond to any of the given elements.
     * (Duplicates are irrelevant to this test, which fails if any of the subject elements
     * correspond to any of the given elements.)
     */
    public void containsNoneIn(E[] excluded) {
      containsNoneIn(asList(excluded));
    }

    private void containsNone(String excludedPrefix, Iterable<? extends E> excluded) {
      Collection<A> actual = iterableToCollection(getCastActual());
      ListMultimap<E, A> present = LinkedListMultimap.create();
      for (E excludedItem : Sets.newLinkedHashSet(excluded)) {
        for (A actualItem : actual) {
          if (correspondence.compare(actualItem, excludedItem)) {
            present.put(excludedItem, actualItem);
          }
        }
      }
      if (!present.isEmpty()) {
        StringBuilder presentDescription = new StringBuilder();
        for (E excludedItem : present.keySet()) {
          if (presentDescription.length() > 0) {
            presentDescription.append(", ");
          }
          List<A> actualItems = present.get(excludedItem);
          if (actualItems.size() == 1) {
            presentDescription
                .append(actualItems.get(0))
                .append(" which corresponds to ")
                .append(excludedItem);
          } else {
            presentDescription
                .append(actualItems)
                .append(" which all correspond to ")
                .append(excludedItem);
          }
        }
        subject.failWithRawMessage(
            "Not true that %s contains no element that %s %s <%s>. It contains <[%s]>",
            subject.actualAsString(), correspondence, excludedPrefix, excluded, presentDescription);
      }
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Iterable<A> getCastActual() {
      return (Iterable<A>) subject.actual();
    }

    // TODO(b/69154276): Consider commoning up some of the logic between IterableSubject.Pairer,
    // MapSubject.MapDifference, and MultimapSubject.difference(). We are likely to need something
    // similar again when we do the work to improve the failure messages from
    // MultimapSubject.UsingCorrespondence (because it won't be able to delegate to
    // IterableSubject.UsingCorrespondence like it does now). So it makes sense to do the
    // refactoring as part of that. Right now, we don't even know what Multimap is going to need.

    /**
     * A class which knows how to pair the actual and expected elements (see {@link
     * #displayingDiffsPairedBy}).
     */
    private final class Pairer {

      private final Function<? super A, ?> actualKeyFunction;
      private final Function<? super E, ?> expectedKeyFunction;

      Pairer(Function<? super A, ?> actualKeyFunction, Function<? super E, ?> expectedKeyFunction) {
        this.actualKeyFunction = actualKeyFunction;
        this.expectedKeyFunction = expectedKeyFunction;
      }

      /**
       * Returns a {@link Pairing} of the given expected and actual values, or {@code null} if the
       * expected values are not uniquely keyed.
       */
      @NullableDecl
      Pairing pair(List<? extends E> expectedValues, List<? extends A> actualValues) {
        Pairing pairing = new Pairing();

        // Populate pairedKeysToExpectedValues with *all* the expected values with non-null keys.
        // We will remove the unpaired keys later. Return null if we find a duplicate key.
        for (E expected : expectedValues) {
          @NullableDecl Object key = expectedKeyFunction.apply(expected);
          if (key != null) {
            if (pairing.pairedKeysToExpectedValues.containsKey(key)) {
              return null;
            } else {
              pairing.pairedKeysToExpectedValues.put(key, expected);
            }
          }
        }

        // Populate pairedKeysToActualValues and unpairedActualValues.
        for (A actual : actualValues) {
          @NullableDecl Object key = actualKeyFunction.apply(actual);
          if (pairing.pairedKeysToExpectedValues.containsKey(key)) {
            pairing.pairedKeysToActualValues.put(key, actual);
          } else {
            pairing.unpairedActualValues.add(actual);
          }
        }

        // Populate unpairedExpectedValues and remove unpaired keys from pairedKeysToExpectedValues.
        for (E expected : expectedValues) {
          @NullableDecl Object key = expectedKeyFunction.apply(expected);
          if (!pairing.pairedKeysToActualValues.containsKey(key)) {
            pairing.unpairedExpectedValues.add(expected);
            pairing.pairedKeysToExpectedValues.remove(key);
          }
        }

        return pairing;
      }

      List<A> pairOne(E expectedValue, Iterable<? extends A> actualValues) {
        @NullableDecl Object key = expectedKeyFunction.apply(expectedValue);
        List<A> matches = new ArrayList<>();
        if (key != null) {
          for (A actual : actualValues) {
            if (key.equals(actualKeyFunction.apply(actual))) {
              matches.add(actual);
            }
          }
        }
        return matches;
      }
    }

    /** An description of a pairing between expected and actual values. N.B. This is mutable. */
    private final class Pairing {

      /**
       * Map from keys used in the pairing to the expected value with that key. Iterates in the
       * order the expected values appear in the input. Will never contain null keys.
       */
      private final Map<Object, E> pairedKeysToExpectedValues = new LinkedHashMap<Object, E>();

      /**
       * Multimap from keys used in the pairing to the actual values with that key. Keys iterate in
       * the order they first appear in the actual values in the input, and values for each key
       * iterate in the order they appear too. Will never contain null keys.
       */
      private final ListMultimap<Object, A> pairedKeysToActualValues = LinkedListMultimap.create();

      /**
       * List of the expected values not used in the pairing. Iterates in the order they appear in
       * the input.
       */
      private final List<E> unpairedExpectedValues = newArrayList();

      /**
       * List of the actual values not used in the pairing. Iterates in the order they appear in the
       * input.
       */
      private final List<A> unpairedActualValues = newArrayList();
    }
  }
}
