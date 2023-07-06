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
import static com.google.common.truth.SubjectUtils.countDuplicates;
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
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.truth.Correspondence.DiffFormatter;
import com.google.common.truth.SubjectUtils.DuplicateGroupedAndTyped;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

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
public class IterableSubject extends Subject {

  private final @Nullable Iterable<?> actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected IterableSubject(FailureMetadata metadata, @Nullable Iterable<?> iterable) {
    super(metadata, iterable);
    this.actual = iterable;
  }

  @Override
  protected String actualCustomStringRepresentation() {
    if (actual != null) {
      // Check the value of iterable.toString() against the default Object.toString() implementation
      // so we can avoid things like "com.google.common.graph.Traverser$GraphTraverser$1@5e316c74"
      String objectToString =
          actual.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(actual));
      if (actual.toString().equals(objectToString)) {
        return Iterables.toString(actual);
      }
    }
    return super.actualCustomStringRepresentation();
  }

  @Override
  public void isEqualTo(@Nullable Object expected) {
    @SuppressWarnings("UndefinedEquals") // method contract requires testing iterables for equality
    boolean equal = Objects.equal(actual, expected);
    if (equal) {
      return;
    }

    // Fail but with a more descriptive message:

    if (actual instanceof List && expected instanceof List) {
      containsExactlyElementsIn((List<?>) expected).inOrder();
    } else if ((actual instanceof Set && expected instanceof Set)
        || (actual instanceof Multiset && expected instanceof Multiset)) {
      containsExactlyElementsIn((Collection<?>) expected);
    } else {
      /*
       * TODO(b/18430105): Consider a special message if comparing incompatible collection types
       * (similar to what MultimapSubject has).
       */
      super.isEqualTo(expected);
    }
  }

  /** Fails if the subject is not empty. */
  public final void isEmpty() {
    if (!Iterables.isEmpty(checkNotNull(actual))) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Fails if the subject is empty. */
  public final void isNotEmpty() {
    if (Iterables.isEmpty(checkNotNull(actual))) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Fails if the subject does not have the given size. */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = size(checkNotNull(actual));
    check("size()").that(actualSize).isEqualTo(expectedSize);
  }

  /** Checks (with a side-effect failure) that the subject contains the supplied item. */
  public final void contains(@Nullable Object element) {
    if (!Iterables.contains(checkNotNull(actual), element)) {
      List<@Nullable Object> elementList = newArrayList(element);
      if (hasMatchingToStringPair(actual, elementList)) {
        failWithoutActual(
            fact("expected to contain", element),
            fact("an instance of", objectToTypeName(element)),
            simpleFact("but did not"),
            fact(
                "though it did contain",
                countDuplicatesAndAddTypeInfo(
                    retainMatchingToString(actual, /* itemsToCheck= */ elementList))),
            fullContents());
      } else {
        failWithActual("expected to contain", element);
      }
    }
  }

  /** Checks (with a side-effect failure) that the subject does not contain the supplied item. */
  public final void doesNotContain(@Nullable Object element) {
    if (Iterables.contains(checkNotNull(actual), element)) {
      failWithActual("expected not to contain", element);
    }
  }

  /** Checks that the subject does not contain duplicate elements. */
  public final void containsNoDuplicates() {
    List<Multiset.Entry<?>> duplicates = newArrayList();
    for (Multiset.Entry<?> entry : LinkedHashMultiset.create(checkNotNull(actual)).entrySet()) {
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
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    containsAnyIn(accumulate(first, second, rest));
  }

  /**
   * Checks that the subject contains at least one of the objects contained in the provided
   * collection or fails.
   */
  // TODO(cpovirk): Consider using makeElementFacts-style messages here, in contains(), etc.
  public final void containsAnyIn(@Nullable Iterable<?> expected) {
    checkNotNull(expected);
    Collection<?> actual = iterableToCollection(checkNotNull(this.actual));
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
                  retainMatchingToString(checkNotNull(this.actual), /* itemsToCheck= */ expected))),
          fullContents());
    } else {
      failWithActual("expected to contain any of", expected);
    }
  }

  /**
   * Checks that the subject contains at least one of the objects contained in the provided array or
   * fails.
   */
  @SuppressWarnings("AvoidObjectArrays")
  public final void containsAnyIn(@Nullable Object[] expected) {
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
  public final Ordered containsAtLeast(
      @Nullable Object firstExpected,
      @Nullable Object secondExpected,
      @Nullable Object @Nullable ... restOfExpected) {
    return containsAtLeastElementsIn(accumulate(firstExpected, secondExpected, restOfExpected));
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
  public final Ordered containsAtLeastElementsIn(Iterable<?> expectedIterable) {
    List<?> actual = Lists.newLinkedList(checkNotNull(this.actual));
    Collection<?> expected = iterableToCollection(expectedIterable);

    List<@Nullable Object> missing = newArrayList();
    List<@Nullable Object> actualNotInOrder = newArrayList();

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
      return failAtLeast(expected, missing);
    }

    return ordered
        ? IN_ORDER
        : new Ordered() {
          @Override
          public void inOrder() {
            ImmutableList.Builder<Fact> facts = ImmutableList.builder();
            facts.add(simpleFact("required elements were all found, but order was wrong"));
            facts.add(fact("expected order for required elements", expected));
            List<Object> actualOrder =
                Lists.newArrayList(checkNotNull(IterableSubject.this.actual));
            if (actualOrder.retainAll(expected)) {
              facts.add(fact("but order was", actualOrder));
              facts.add(fullContents());
              failWithoutActual(facts.build());
            } else {
              failWithActual(facts.build());
            }
          }
        };
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
  @SuppressWarnings("AvoidObjectArrays")
  public final Ordered containsAtLeastElementsIn(@Nullable Object[] expected) {
    return containsAtLeastElementsIn(asList(expected));
  }

  private Ordered failAtLeast(Collection<?> expected, Collection<?> missingRawObjects) {
    List<?> nearMissRawObjects =
        retainMatchingToString(checkNotNull(actual), /* itemsToCheck= */ missingRawObjects);

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
   * Removes at most the given number of available elements from the input list and adds them to the
   * given output collection.
   */
  private static void moveElements(
      List<?> input, Collection<@Nullable Object> output, int maxElements) {
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
  public final Ordered containsExactly(@Nullable Object @Nullable ... varargs) {
    List<@Nullable Object> expected =
        (varargs == null) ? newArrayList((@Nullable Object) null) : asList(varargs);
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
  public final Ordered containsExactlyElementsIn(@Nullable Iterable<?> expected) {
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
  @SuppressWarnings({
    "AvoidObjectArrays",
    "ContainsExactlyElementsInUnnecessaryWrapperAroundArray",
    "ContainsExactlyElementsInWithVarArgsToExactly"
  })
  public final Ordered containsExactlyElementsIn(@Nullable Object @Nullable [] expected) {
    return containsExactlyElementsIn(asList(checkNotNull(expected)));
  }

  private Ordered containsExactlyElementsIn(
      @Nullable Iterable<?> required, boolean addElementsInWarning) {
    checkNotNull(required);
    Iterator<?> actualIter = checkNotNull(actual).iterator();
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
           * TODO(b/135918662): Consider always throwing ComparisonFailure if there is exactly one
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
        List<@Nullable Object> missing = newArrayList();
        missing.add(requiredElement);
        Iterators.addAll(missing, requiredIter);

        // Extra elements that the subject had but shouldn't have.
        List<@Nullable Object> extra = newArrayList();

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
      Iterable<Multiset.Entry<?>> first, Iterable<Multiset.Entry<?>> second) {
    boolean firstHasMultiple = hasMultiple(first);
    boolean secondHasMultiple = hasMultiple(second);
    if ((firstHasMultiple || secondHasMultiple) && anyContainsCommaOrNewline(first, second)) {
      return FACT_PER_ELEMENT;
    }
    if (firstHasMultiple && containsEmptyOrLong(first)) {
      return FACT_PER_ELEMENT;
    }
    if (secondHasMultiple && containsEmptyOrLong(second)) {
      return FACT_PER_ELEMENT;
    }
    return ALL_IN_ONE_FACT;
  }

  private static boolean anyContainsCommaOrNewline(Iterable<Multiset.Entry<?>>... lists) {
    for (Multiset.Entry<?> entry : concat(lists)) {
      String s = String.valueOf(entry.getElement());
      if (s.contains("\n") || s.contains(",")) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasMultiple(Iterable<Multiset.Entry<?>> entries) {
    int totalCount = 0;
    for (Multiset.Entry<?> entry : entries) {
      totalCount += entry.getCount();
      if (totalCount > 1) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsEmptyOrLong(Iterable<Multiset.Entry<?>> entries) {
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
      @Nullable Object firstExcluded,
      @Nullable Object secondExcluded,
      @Nullable Object @Nullable ... restOfExcluded) {
    containsNoneIn(accumulate(firstExcluded, secondExcluded, restOfExcluded));
  }

  /**
   * Checks that the actual iterable contains none of the elements contained in the excluded
   * iterable or fails. (Duplicates are irrelevant to this test, which fails if any of the actual
   * elements equal any of the excluded.)
   */
  public final void containsNoneIn(Iterable<?> excluded) {
    Collection<?> actual = iterableToCollection(checkNotNull(this.actual));
    List<@Nullable Object> present = new ArrayList<>();
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
  @SuppressWarnings("AvoidObjectArrays")
  public final void containsNoneIn(@Nullable Object[] excluded) {
    containsNoneIn(asList(excluded));
  }

  /** Ordered implementation that does nothing because it's already known to be true. */
  private static final Ordered IN_ORDER = () -> {};

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED = () -> {};

  /**
   * Fails if the iterable is not strictly ordered, according to the natural ordering of its
   * elements. Strictly ordered means that each element in the iterable is <i>strictly</i> greater
   * than the element that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  /*
   * non-final because it's overridden by IterableOfProtosSubject.
   *
   * If we really, really wanted it to be final, we could make IterableOfProtosSubject implement a
   * package-private(?) interface that redeclares this method as deprecated.
   *
   * Alternatively, we could avoid deprecating the method there, relying instead on Error Prone
   * static analysis. It's _possible_ that users would be confused by having one overload deprecated
   * and the other not, anyway.
   */
  public void isInStrictOrder() {
    isInStrictOrder(Ordering.natural());
  }

  /**
   * Fails if the iterable is not strictly ordered, according to the given comparator. Strictly
   * ordered means that each element in the iterable is <i>strictly</i> greater than the element
   * that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   */
  @SuppressWarnings({"unchecked"})
  public final void isInStrictOrder(Comparator<?> comparator) {
    checkNotNull(comparator);
    pairwiseCheck(
        "expected to be in strict order",
        new PairwiseChecker() {
          @Override
          public boolean check(@Nullable Object prev, @Nullable Object next) {
            return ((Comparator<@Nullable Object>) comparator).compare(prev, next) < 0;
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
  // non-final because it's overridden by IterableOfProtosSubject. See isInStrictOrder.
  public void isInOrder() {
    isInOrder(Ordering.natural());
  }

  /**
   * Fails if the iterable is not ordered, according to the given comparator. Ordered means that
   * each element in the iterable is greater than or equal to the element that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   */
  @SuppressWarnings({"unchecked"})
  public final void isInOrder(Comparator<?> comparator) {
    checkNotNull(comparator);
    pairwiseCheck(
        "expected to be in order",
        new PairwiseChecker() {
          @Override
          public boolean check(@Nullable Object prev, @Nullable Object next) {
            return ((Comparator<@Nullable Object>) comparator).compare(prev, next) <= 0;
          }
        });
  }

  private interface PairwiseChecker {
    boolean check(@Nullable Object prev, @Nullable Object next);
  }

  private void pairwiseCheck(String expectedFact, PairwiseChecker checker) {
    Iterator<?> iterator = checkNotNull(actual).iterator();
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

  /**
   * @deprecated You probably meant to call {@link #containsNoneOf} instead.
   */
  @Override
  @Deprecated
  public void isNoneOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    super.isNoneOf(first, second, rest);
  }

  /**
   * @deprecated You probably meant to call {@link #containsNoneIn} instead.
   */
  @Override
  @Deprecated
  public void isNotIn(@Nullable Iterable<?> iterable) {
    checkNotNull(iterable);
    if (Iterables.contains(iterable, actual)) {
      failWithActual("expected not to be any of", iterable);
    }
    List<@Nullable Object> nonIterables = new ArrayList<>();
    for (Object element : iterable) {
      if (!(element instanceof Iterable<?>)) {
        nonIterables.add(element);
      }
    }
    if (!nonIterables.isEmpty()) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "The actual value is an Iterable, and you've written a test that compares it to "
                      + "some objects that are not Iterables. Did you instead mean to check "
                      + "whether its *contents* match any of the *contents* of the given values? "
                      + "If so, call containsNoneOf(...)/containsNoneIn(...) instead. "
                      + "Non-iterables: %s",
                  nonIterables)));
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
  public <A extends @Nullable Object, E extends @Nullable Object>
      UsingCorrespondence<A, E> comparingElementsUsing(
          Correspondence<? super A, ? super E> correspondence) {
    return new UsingCorrespondence<>(this, correspondence);
  }

  /**
   * Starts a method chain for a check in which failure messages may use the given {@link
   * DiffFormatter} to describe the difference between an actual elements (i.e. an element of the
   * {@link Iterable} under test) and the element it is expected to be equal to, but isn't. The
   * actual and expected elements must be of type {@code T}. The check is actually executed by
   * continuing the method chain. You may well want to use {@link
   * UsingCorrespondence#displayingDiffsPairedBy} to specify how the elements should be paired up
   * for diffing. For example:
   *
   * <pre>{@code
   * assertThat(actualFoos)
   *     .formattingDiffsUsing(FooTestHelper::formatDiff)
   *     .displayingDiffsPairedBy(Foo::getId)
   *     .containsExactly(foo1, foo2, foo3);
   * }</pre>
   *
   * where {@code actualFoos} is an {@code Iterable<Foo>}, {@code FooTestHelper.formatDiff} is a
   * static method taking two {@code Foo} arguments and returning a {@link String}, {@code
   * Foo.getId} is a no-arg instance method returning some kind of ID, and {@code foo1}, {code
   * foo2}, and {@code foo3} are {@code Foo} instances.
   *
   * <p>Unlike when using {@link #comparingElementsUsing}, the elements are still compared using
   * object equality, so this method does not affect whether a test passes or fails.
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual element that is not of type {@code T}.
   *
   * @since 1.1
   */
  public <T> UsingCorrespondence<T, T> formattingDiffsUsing(
      DiffFormatter<? super T, ? super T> formatter) {
    return comparingElementsUsing(Correspondence.<T>equality().formattingDiffsUsing(formatter));
  }

  /**
   * A partially specified check in which the actual elements (normally the elements of the {@link
   * Iterable} under test) are compared to expected elements using a {@link Correspondence}. The
   * expected elements are of type {@code E}. Call methods on this object to actually execute the
   * check.
   */
  public static class UsingCorrespondence<A extends @Nullable Object, E extends @Nullable Object> {

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
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#equals(Object)} is not supported on Truth subjects or intermediate
     *     classes. If you are writing a test assertion (actual vs. expected), use methods liks
     *     {@link #containsExactlyElementsIn(Iterable)} instead.
     */
    @DoNotCall(
        "UsingCorrespondence.equals() is not supported. Did you mean to call"
            + " containsExactlyElementsIn(expected) instead of equals(expected)?")
    @Deprecated
    @Override
    public final boolean equals(@Nullable Object o) {
      throw new UnsupportedOperationException(
          "UsingCorrespondence.equals() is not supported. Did you mean to call"
              + " containsExactlyElementsIn(expected) instead of equals(expected)?");
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#hashCode()} is not supported on Truth types.
     */
    @DoNotCall("UsingCorrespondence.hashCode() is not supported.")
    @Deprecated
    @Override
    public final int hashCode() {
      throw new UnsupportedOperationException("UsingCorrespondence.hashCode() is not supported.");
    }

    /**
     * @throws UnsupportedOperationException always
     * @deprecated {@link Object#toString()} is not supported on Truth subjects.
     */
    @Deprecated
    @DoNotCall("UsingCorrespondence.toString() is not supported.")
    @Override
    public final String toString() {
      throw new UnsupportedOperationException(
          "UsingCorrespondence.toString() is not supported. Did you mean to call"
              + " assertThat(foo.toString()) instead of assertThat(foo).toString()?");
    }

    /**
     * Specifies a way to pair up unexpected and missing elements in the message when an assertion
     * fails. For example:
     *
     * <pre>{@code
     * assertThat(actualRecords)
     *     .comparingElementsUsing(RECORD_CORRESPONDENCE)
     *     .displayingDiffsPairedBy(MyRecord::getId)
     *     .containsExactlyElementsIn(expectedRecords);
     * }</pre>
     *
     * <p><b>Important</b>: The {code keyFunction} function must be able to accept both the actual
     * and the unexpected elements, i.e. it must satisfy {@code Function<? super A, ?>} as well as
     * {@code Function<? super E, ?>}. If that constraint is not met then a subsequent method may
     * throw {@link ClassCastException}. Use the two-parameter overload if you need to specify
     * different key functions for the actual and expected elements.
     *
     * <p>On assertions where it makes sense to do so, the elements are paired as follows: they are
     * keyed by {@code keyFunction}, and if an unexpected element and a missing element have the
     * same non-null key then the they are paired up. (Elements with null keys are not paired.) The
     * failure message will show paired elements together, and a diff will be shown if the {@link
     * Correspondence#formatDiff} method returns non-null.
     *
     * <p>The expected elements given in the assertion should be uniquely keyed by {@code
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
     * <p>If the {@code apply} method on the key function throws an exception then the element will
     * be treated as if it had a null key and not paired. (The first such exception will be noted in
     * the failure message.)
     *
     * <p>Note that calling this method makes no difference to whether a test passes or fails, it
     * just improves the message if it fails.
     */
    public UsingCorrespondence<A, E> displayingDiffsPairedBy(Function<? super E, ?> keyFunction) {
      @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
      Function<? super A, ?> actualKeyFunction = (Function<? super A, ?>) keyFunction;
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
     * <p>The expected elements given in the assertion should be uniquely keyed by {@code
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
     * <p>If the {@code apply} method on either of the key functions throws an exception then the
     * element will be treated as if it had a null key and not paired. (The first such exception
     * will be noted in the failure message.)
     *
     * <p>Note that calling this method makes no difference to whether a test passes or fails, it
     * just improves the message if it fails.
     */
    public UsingCorrespondence<A, E> displayingDiffsPairedBy(
        Function<? super A, ?> actualKeyFunction, Function<? super E, ?> expectedKeyFunction) {
      return new UsingCorrespondence<>(
          subject, correspondence, new Pairer(actualKeyFunction, expectedKeyFunction));
    }

    /**
     * Checks that the subject contains at least one element that corresponds to the given expected
     * element.
     */
    /*
     * TODO(cpovirk): Do we want @Nullable on usages of E? Probably not, since it could throw errors
     * during comparisons? Or maybe we should take the risk for user convenience? If we make
     * changes, also make them in MapSubject, MultimapSubject, and possibly others.
     */
    public void contains(E expected) {
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
      for (A actual : getCastActual()) {
        if (correspondence.safeCompare(actual, expected, exceptions)) {
          // Found a match, but we still need to fail if we hit an exception along the way.
          if (exceptions.hasCompareException()) {
            subject.failWithoutActual(
                ImmutableList.<Fact>builder()
                    .addAll(exceptions.describeAsMainCause())
                    .add(fact("expected to contain", expected))
                    .addAll(correspondence.describeForIterable())
                    .add(fact("found match (but failing because of exception)", actual))
                    .add(subject.fullContents())
                    .build());
          }
          return;
        }
      }
      // Found no match. Fail, reporting elements that have the correct key if there are any.
      if (pairer.isPresent()) {
        List<A> keyMatches = pairer.get().pairOne(expected, getCastActual(), exceptions);
        if (!keyMatches.isEmpty()) {
          subject.failWithoutActual(
              ImmutableList.<Fact>builder()
                  .add(fact("expected to contain", expected))
                  .addAll(correspondence.describeForIterable())
                  .add(simpleFact("but did not"))
                  .addAll(
                      formatExtras(
                          "though it did contain elements with correct key",
                          expected,
                          keyMatches,
                          exceptions))
                  .add(simpleFact("---"))
                  .add(subject.fullContents())
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
          return;
        }
      }
      subject.failWithoutActual(
          ImmutableList.<Fact>builder()
              .add(fact("expected to contain", expected))
              .addAll(correspondence.describeForIterable())
              .add(subject.butWas())
              .addAll(exceptions.describeAsAdditionalInfo())
              .build());
    }

    /** Checks that none of the actual elements correspond to the given element. */
    public void doesNotContain(E excluded) {
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
      List<A> matchingElements = new ArrayList<>();
      for (A actual : getCastActual()) {
        if (correspondence.safeCompare(actual, excluded, exceptions)) {
          matchingElements.add(actual);
        }
      }
      // Fail if we found any matches.
      if (!matchingElements.isEmpty()) {
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .add(fact("expected not to contain", excluded))
                .addAll(correspondence.describeForIterable())
                .add(fact("but contained", countDuplicates(matchingElements)))
                .add(subject.fullContents())
                .addAll(exceptions.describeAsAdditionalInfo())
                .build());
        return;
      }
      // Found no match, but we still need to fail if we hit an exception along the way.
      if (exceptions.hasCompareException()) {
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .addAll(exceptions.describeAsMainCause())
                .add(fact("expected not to contain", excluded))
                .addAll(correspondence.describeForIterable())
                .add(simpleFact("found no match (but failing because of exception)"))
                .add(subject.fullContents())
                .build());
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
    public final Ordered containsExactly(@Nullable E @Nullable ... expected) {
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
    public Ordered containsExactlyElementsIn(@Nullable Iterable<? extends E> expected) {
      List<A> actualList = iterableToList(getCastActual());
      List<? extends E> expectedList = iterableToList(checkNotNull(expected));

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
      // Exceptions from Correspondence.compare are stored and treated as if false was returned.
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
      ImmutableSetMultimap<Integer, Integer> candidateMapping =
          findCandidateMapping(actualList, expectedList, exceptions);
      if (failIfCandidateMappingHasMissingOrExtra(
          actualList, expectedList, candidateMapping, exceptions)) {
        return ALREADY_FAILED;
      }
      // We know that every expected element maps to at least one actual element, and vice versa.
      // Find a maximal 1:1 mapping, and check it for completeness.
      ImmutableBiMap<Integer, Integer> maximalOneToOneMapping =
          findMaximalOneToOneMapping(candidateMapping);
      if (failIfOneToOneMappingHasMissingOrExtra(
          actualList, expectedList, maximalOneToOneMapping, exceptions)) {
        return ALREADY_FAILED;
      }
      // Check whether we caught any exceptions from Correspondence.compare. We do the any-order
      // assertions treating exceptions as if false was returned before this, because the failure
      // messages are normally more useful (e.g. reporting that the actual iterable contained an
      // unexpected null) but we are contractually obliged to throw here if the assertions passed.
      if (exceptions.hasCompareException()) {
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .addAll(exceptions.describeAsMainCause())
                .add(fact("expected", expected))
                .addAll(correspondence.describeForIterable())
                .add(simpleFact("found all expected elements (but failing because of exception)"))
                .add(subject.fullContents())
                .build());
        return ALREADY_FAILED;
      }
      // The 1:1 mapping is complete, so the test succeeds (but we know from above that the mapping
      // is not in order).
      return new Ordered() {
        @Override
        public void inOrder() {
          subject.failWithActual(
              ImmutableList.<Fact>builder()
                  .add(simpleFact("contents match, but order was wrong"))
                  .add(fact("expected", expected))
                  .addAll(correspondence.describeForIterable())
                  .build());
        }
      };
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
    @SuppressWarnings("AvoidObjectArrays")
    public Ordered containsExactlyElementsIn(E @Nullable [] expected) {
      return containsExactlyElementsIn(asList(checkNotNull(expected)));
    }

    /**
     * Returns whether the actual and expected iterators have the same number of elements and, when
     * iterated pairwise, every pair of actual and expected values satisfies the correspondence.
     * Returns false if any comparison threw an exception.
     */
    private boolean correspondInOrderExactly(
        Iterator<? extends A> actual, Iterator<? extends E> expected) {
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
      while (actual.hasNext() && expected.hasNext()) {
        A actualElement = actual.next();
        E expectedElement = expected.next();
        // Return false if the elements didn't correspond, or if the correspondence threw an
        // exception. We'll fall back on the any-order assertion in this case.
        if (!correspondence.safeCompare(actualElement, expectedElement, exceptions)) {
          return false;
        }
      }
      // No need to check the ExceptionStore, as we'll already have returned false on any exception.
      return !(actual.hasNext() || expected.hasNext());
    }

    /**
     * Given a list of actual elements and a list of expected elements, finds a many:many mapping
     * between actual and expected elements where a pair of elements maps if it satisfies the
     * correspondence. Returns this mapping as a multimap where the keys are indexes into the actual
     * list and the values are indexes into the expected list. Any exceptions are treated as if the
     * elements did not correspond, and the exception added to the store.
     */
    private ImmutableSetMultimap<Integer, Integer> findCandidateMapping(
        List<? extends A> actual,
        List<? extends E> expected,
        Correspondence.ExceptionStore exceptions) {
      ImmutableSetMultimap.Builder<Integer, Integer> mapping = ImmutableSetMultimap.builder();
      for (int actualIndex = 0; actualIndex < actual.size(); actualIndex++) {
        for (int expectedIndex = 0; expectedIndex < expected.size(); expectedIndex++) {
          if (correspondence.safeCompare(
              actual.get(actualIndex), expected.get(expectedIndex), exceptions)) {
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
        ImmutableSetMultimap<Integer, Integer> mapping,
        Correspondence.ExceptionStore exceptions) {
      List<? extends A> extra = findNotIndexed(actual, mapping.keySet());
      List<? extends E> missing = findNotIndexed(expected, mapping.inverse().keySet());
      if (!missing.isEmpty() || !extra.isEmpty()) {
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .addAll(describeMissingOrExtra(missing, extra, exceptions))
                .add(fact("expected", expected))
                .addAll(correspondence.describeForIterable())
                .add(subject.butWas())
                .addAll(exceptions.describeAsAdditionalInfo())
                .build());
        return true;
      }
      return false;
    }

    /**
     * Given a list of missing elements and a list of extra elements, at least one of which must be
     * non-empty, returns facts describing them. Exceptions from calling {@link
     * Correspondence#formatDiff} are stored in {@code exceptions}.
     */
    private ImmutableList<Fact> describeMissingOrExtra(
        List<? extends E> missing,
        List<? extends A> extra,
        Correspondence.ExceptionStore exceptions) {
      if (pairer.isPresent()) {
        Pairing pairing = pairer.get().pair(missing, extra, exceptions);
        if (pairing != null) {
          return describeMissingOrExtraWithPairing(pairing, exceptions);
        } else {
          return ImmutableList.<Fact>builder()
              .addAll(describeMissingOrExtraWithoutPairing(missing, extra))
              .add(
                  simpleFact(
                      "a key function which does not uniquely key the expected elements was"
                          + " provided and has consequently been ignored"))
              .build();
        }
      } else if (missing.size() == 1 && extra.size() >= 1) {
        return ImmutableList.<Fact>builder()
            .add(fact("missing (1)", missing.get(0)))
            .addAll(formatExtras("unexpected", missing.get(0), extra, exceptions))
            .add(simpleFact("---"))
            .build();
      } else {
        return describeMissingOrExtraWithoutPairing(missing, extra);
      }
    }

    private ImmutableList<Fact> describeMissingOrExtraWithoutPairing(
        List<? extends E> missing, List<? extends A> extra) {
      return makeElementFactsForBoth("missing", missing, "unexpected", extra);
    }

    private ImmutableList<Fact> describeMissingOrExtraWithPairing(
        Pairing pairing, Correspondence.ExceptionStore exceptions) {
      ImmutableList.Builder<Fact> facts = ImmutableList.builder();
      for (Object key : pairing.pairedKeysToExpectedValues.keySet()) {
        E missing = pairing.pairedKeysToExpectedValues.get(key);
        List<A> extras = pairing.pairedKeysToActualValues.get(key);
        facts.add(fact("for key", key));
        facts.add(fact("missing", missing));
        facts.addAll(formatExtras("unexpected", missing, extras, exceptions));
        facts.add(simpleFact("---"));
      }
      if (!pairing.unpairedActualValues.isEmpty() || !pairing.unpairedExpectedValues.isEmpty()) {
        facts.add(simpleFact("elements without matching keys:"));
        facts.addAll(
            describeMissingOrExtraWithoutPairing(
                pairing.unpairedExpectedValues, pairing.unpairedActualValues));
      }
      return facts.build();
    }

    private ImmutableList<Fact> formatExtras(
        String label,
        E missing,
        List<? extends A> extras,
        Correspondence.ExceptionStore exceptions) {
      List<@Nullable String> diffs = new ArrayList<>(extras.size());
      boolean hasDiffs = false;
      for (int i = 0; i < extras.size(); i++) {
        A extra = extras.get(i);
        String diff = correspondence.safeFormatDiff(extra, missing, exceptions);
        diffs.add(diff);
        if (diff != null) {
          hasDiffs = true;
        }
      }
      if (hasDiffs) {
        ImmutableList.Builder<Fact> extraFacts = ImmutableList.builder();
        extraFacts.add(simpleFact(lenientFormat("%s (%s)", label, extras.size())));
        for (int i = 0; i < extras.size(); i++) {
          A extra = extras.get(i);
          extraFacts.add(fact(lenientFormat("#%s", i + 1), extra));
          if (diffs.get(i) != null) {
            extraFacts.add(fact("diff", diffs.get(i)));
          }
        }
        return extraFacts.build();
      } else {
        return ImmutableList.of(
            fact(lenientFormat("%s (%s)", label, extras.size()), countDuplicates(extras)));
      }
    }

    /**
     * Returns all the elements of the given list other than those with the given indexes. Assumes
     * that all the given indexes really are valid indexes into the list.
     */
    private <T extends @Nullable Object> List<T> findNotIndexed(
        List<T> list, Set<Integer> indexes) {
      if (indexes.size() == list.size()) {
        // If there are as many distinct valid indexes are there are elements in the list then every
        // index must be in there once.
        return asList();
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
        List<? extends A> actual,
        List<? extends E> expected,
        BiMap<Integer, Integer> mapping,
        Correspondence.ExceptionStore exceptions) {
      List<? extends A> extra = findNotIndexed(actual, mapping.keySet());
      List<? extends E> missing = findNotIndexed(expected, mapping.values());
      if (!missing.isEmpty() || !extra.isEmpty()) {
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .add(
                    simpleFact(
                        "in an assertion requiring a 1:1 mapping between the expected and the"
                            + " actual elements, each actual element matches as least one expected"
                            + " element, and vice versa, but there was no 1:1 mapping"))
                .add(
                    simpleFact(
                        "using the most complete 1:1 mapping (or one such mapping, if there is a"
                            + " tie)"))
                .addAll(describeMissingOrExtra(missing, extra, exceptions))
                .add(fact("expected", expected))
                .addAll(correspondence.describeForIterable())
                .add(subject.butWas())
                .addAll(exceptions.describeAsAdditionalInfo())
                .build());
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
    public final Ordered containsAtLeast(E first, E second, E @Nullable ... rest) {
      return containsAtLeastElementsIn(accumulate(first, second, rest));
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
    public Ordered containsAtLeastElementsIn(Iterable<? extends E> expected) {
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
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
      ImmutableSetMultimap<Integer, Integer> candidateMapping =
          findCandidateMapping(actualList, expectedList, exceptions);
      if (failIfCandidateMappingHasMissing(
          actualList, expectedList, candidateMapping, exceptions)) {
        return ALREADY_FAILED;
      }
      // We know that every expected element maps to at least one actual element, and vice versa.
      // Find a maximal 1:1 mapping, and check it for completeness.
      ImmutableBiMap<Integer, Integer> maximalOneToOneMapping =
          findMaximalOneToOneMapping(candidateMapping);
      if (failIfOneToOneMappingHasMissing(
          actualList, expectedList, maximalOneToOneMapping, exceptions)) {
        return ALREADY_FAILED;
      }
      // Check whether we caught any exceptions from Correspondence.compare. As with
      // containsExactlyElementIn, we do the any-order assertions treating exceptions as if false
      // was returned before this, but we are contractually obliged to throw here if the assertions
      // passed.
      if (exceptions.hasCompareException()) {
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .addAll(exceptions.describeAsMainCause())
                .add(fact("expected to contain at least", expected))
                .addAll(correspondence.describeForIterable())
                .add(simpleFact("found all expected elements (but failing because of exception)"))
                .add(subject.fullContents())
                .build());
        return ALREADY_FAILED;
      }
      // The 1:1 mapping maps all the expected elements, so the test succeeds (but we know from
      // above that the mapping is not in order).
      return new Ordered() {
        @Override
        public void inOrder() {
          subject.failWithActual(
              ImmutableList.<Fact>builder()
                  .add(simpleFact("required elements were all found, but order was wrong"))
                  .add(fact("expected order for required elements", expected))
                  .addAll(correspondence.describeForIterable())
                  .build());
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
    @SuppressWarnings("AvoidObjectArrays")
    public Ordered containsAtLeastElementsIn(E[] expected) {
      return containsAtLeastElementsIn(asList(expected));
    }

    /**
     * Returns whether all the elements of the expected iterator and any subset of the elements of
     * the actual iterator can be paired up in order, such that every pair of actual and expected
     * elements satisfies the correspondence. Returns false if any comparison threw an exception.
     */
    private boolean correspondInOrderAllIn(
        Iterator<? extends A> actual, Iterator<? extends E> expected) {
      // We take a greedy approach here, iterating through the expected elements and pairing each
      // with the first applicable actual element. This is fine for the in-order test, since there's
      // no way that paring an expected element with a later actual element permits a solution which
      // couldn't be achieved by pairing it with the first. (For the any-order test, we may want to
      // pair an expected element with a later actual element so that we can pair the earlier actual
      // element with a later expected element, but that doesn't apply here.)
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
      while (expected.hasNext()) {
        E expectedElement = expected.next();
        // Return false if we couldn't find the expected exception, or if the correspondence threw
        // an exception. We'll fall back on the any-order assertion in this case.
        if (!findCorresponding(actual, expectedElement, exceptions)
            || exceptions.hasCompareException()) {
          return false;
        }
      }
      return true;
    }

    /**
     * Advances the actual iterator looking for an element which corresponds to the expected
     * element. Returns whether or not it finds one.
     */
    private boolean findCorresponding(
        Iterator<? extends A> actual, E expectedElement, Correspondence.ExceptionStore exceptions) {
      while (actual.hasNext()) {
        A actualElement = actual.next();
        if (correspondence.safeCompare(actualElement, expectedElement, exceptions)) {
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
        ImmutableSetMultimap<Integer, Integer> mapping,
        Correspondence.ExceptionStore exceptions) {
      List<? extends E> missing = findNotIndexed(expected, mapping.inverse().keySet());
      if (!missing.isEmpty()) {
        List<? extends A> extra = findNotIndexed(actual, mapping.keySet());
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .addAll(describeMissing(missing, extra, exceptions))
                .add(fact("expected to contain at least", expected))
                .addAll(correspondence.describeForIterable())
                .add(subject.butWas())
                .addAll(exceptions.describeAsAdditionalInfo())
                .build());
        return true;
      }
      return false;
    }

    /**
     * Given a list of missing elements, which must be non-empty, and a list of extra elements,
     * returns a list of facts describing the missing elements, diffing against the extra ones where
     * appropriate.
     */
    private ImmutableList<Fact> describeMissing(
        List<? extends E> missing,
        List<? extends A> extra,
        Correspondence.ExceptionStore exceptions) {
      if (pairer.isPresent()) {
        Pairing pairing = pairer.get().pair(missing, extra, exceptions);
        if (pairing != null) {
          return describeMissingWithPairing(pairing, exceptions);
        } else {
          return ImmutableList.<Fact>builder()
              .addAll(describeMissingWithoutPairing(missing))
              .add(
                  simpleFact(
                      "a key function which does not uniquely key the expected elements was"
                          + " provided and has consequently been ignored"))
              .build();
        }
      } else {
        // N.B. For containsAny, we do not treat having exactly one missing element as a special
        // case (as we do for containsExactly). Showing extra elements has lower utility for
        // containsAny (because they are allowed by the assertion) so we only show them if the user
        // has explicitly opted in by specifying a pairing.
        return describeMissingWithoutPairing(missing);
      }
    }

    private ImmutableList<Fact> describeMissingWithoutPairing(List<? extends E> missing) {
      return makeElementFactsForBoth("missing", missing, "unexpected", ImmutableList.of());
    }

    private ImmutableList<Fact> describeMissingWithPairing(
        Pairing pairing, Correspondence.ExceptionStore exceptions) {
      ImmutableList.Builder<Fact> facts = ImmutableList.builder();
      for (Object key : pairing.pairedKeysToExpectedValues.keySet()) {
        E missing = pairing.pairedKeysToExpectedValues.get(key);
        List<A> extras = pairing.pairedKeysToActualValues.get(key);
        facts.add(fact("for key", key));
        facts.add(fact("missing", missing));
        facts.addAll(
            formatExtras("did contain elements with that key", missing, extras, exceptions));
        facts.add(simpleFact("---"));
      }
      if (!pairing.unpairedExpectedValues.isEmpty()) {
        facts.add(simpleFact("elements without matching keys:"));
        facts.addAll(describeMissingWithoutPairing(pairing.unpairedExpectedValues));
      }
      return facts.build();
    }

    /**
     * Given a list of expected elements, and a 1:1 mapping between actual and expected elements
     * specified as a bimap of indexes into an actual list to indexes into the expected list, checks
     * that every expected element maps to an actual element. Actual elements which do not map to
     * any expected elements are ignored.
     */
    private boolean failIfOneToOneMappingHasMissing(
        List<? extends A> actual,
        List<? extends E> expected,
        BiMap<Integer, Integer> mapping,
        Correspondence.ExceptionStore exceptions) {
      List<? extends E> missing = findNotIndexed(expected, mapping.values());
      if (!missing.isEmpty()) {
        List<? extends A> extra = findNotIndexed(actual, mapping.keySet());
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .add(
                    simpleFact(
                        "in an assertion requiring a 1:1 mapping between the expected and a subset"
                            + " of the actual elements, each actual element matches as least one"
                            + " expected element, and vice versa, but there was no 1:1 mapping"))
                .add(
                    simpleFact(
                        "using the most complete 1:1 mapping (or one such mapping, if there is a"
                            + " tie)"))
                .addAll(describeMissing(missing, extra, exceptions))
                .add(fact("expected to contain at least", expected))
                .addAll(correspondence.describeForIterable())
                .add(subject.butWas())
                .addAll(exceptions.describeAsAdditionalInfo())
                .build());
        return true;
      }
      return false;
    }

    /**
     * Checks that the subject contains at least one element that corresponds to at least one of the
     * expected elements.
     */
    @SafeVarargs
    public final void containsAnyOf(E first, E second, E @Nullable ... rest) {
      containsAnyIn(accumulate(first, second, rest));
    }

    /**
     * Checks that the subject contains at least one element that corresponds to at least one of the
     * expected elements.
     */
    public void containsAnyIn(Iterable<? extends E> expected) {
      Collection<A> actual = iterableToCollection(getCastActual());
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
      for (E expectedItem : expected) {
        for (A actualItem : actual) {
          if (correspondence.safeCompare(actualItem, expectedItem, exceptions)) {
            // Found a match, but we still need to fail if we hit an exception along the way.
            if (exceptions.hasCompareException()) {
              subject.failWithoutActual(
                  ImmutableList.<Fact>builder()
                      .addAll(exceptions.describeAsMainCause())
                      .add(fact("expected to contain any of", expected))
                      .addAll(correspondence.describeForIterable())
                      .add(simpleFact("found match (but failing because of exception)"))
                      .add(subject.fullContents())
                      .build());
            }
            return;
          }
        }
      }
      // Found no match. Fail, reporting elements that have a correct key if there are any.
      if (pairer.isPresent()) {
        Pairing pairing =
            pairer.get().pair(iterableToList(expected), iterableToList(actual), exceptions);
        if (pairing != null) {
          if (!pairing.pairedKeysToExpectedValues.isEmpty()) {
            subject.failWithoutActual(
                ImmutableList.<Fact>builder()
                    .add(fact("expected to contain any of", expected))
                    .addAll(correspondence.describeForIterable())
                    .add(subject.butWas())
                    .addAll(describeAnyMatchesByKey(pairing, exceptions))
                    .addAll(exceptions.describeAsAdditionalInfo())
                    .build());
          } else {
            subject.failWithoutActual(
                ImmutableList.<Fact>builder()
                    .add(fact("expected to contain any of", expected))
                    .addAll(correspondence.describeForIterable())
                    .add(subject.butWas())
                    .add(simpleFact("it does not contain any matches by key, either"))
                    .addAll(exceptions.describeAsAdditionalInfo())
                    .build());
          }
        } else {
          subject.failWithoutActual(
              ImmutableList.<Fact>builder()
                  .add(fact("expected to contain any of", expected))
                  .addAll(correspondence.describeForIterable())
                  .add(subject.butWas())
                  .add(
                      simpleFact(
                          "a key function which does not uniquely key the expected elements was"
                              + " provided and has consequently been ignored"))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        }
      } else {
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .add(fact("expected to contain any of", expected))
                .addAll(correspondence.describeForIterable())
                .add(subject.butWas())
                .addAll(exceptions.describeAsAdditionalInfo())
                .build());
      }
    }

    /**
     * Checks that the subject contains at least one element that corresponds to at least one of the
     * expected elements.
     */
    @SuppressWarnings("AvoidObjectArrays")
    public void containsAnyIn(E[] expected) {
      containsAnyIn(asList(expected));
    }

    private ImmutableList<Fact> describeAnyMatchesByKey(
        Pairing pairing, Correspondence.ExceptionStore exceptions) {
      ImmutableList.Builder<Fact> facts = ImmutableList.builder();
      for (Object key : pairing.pairedKeysToExpectedValues.keySet()) {
        E expected = pairing.pairedKeysToExpectedValues.get(key);
        List<A> got = pairing.pairedKeysToActualValues.get(key);
        facts.add(fact("for key", key));
        facts.add(fact("expected any of", expected));
        facts.addAll(formatExtras("but got", expected, got, exceptions));
        facts.add(simpleFact("---"));
      }
      return facts.build();
    }

    /**
     * Checks that the subject contains no elements that correspond to any of the given elements.
     * (Duplicates are irrelevant to this test, which fails if any of the subject elements
     * correspond to any of the given elements.)
     */
    @SafeVarargs
    public final void containsNoneOf(
        E firstExcluded, E secondExcluded, E @Nullable ... restOfExcluded) {
      containsNoneIn(accumulate(firstExcluded, secondExcluded, restOfExcluded));
    }

    /**
     * Checks that the subject contains no elements that correspond to any of the given elements.
     * (Duplicates are irrelevant to this test, which fails if any of the subject elements
     * correspond to any of the given elements.)
     */
    public void containsNoneIn(Iterable<? extends E> excluded) {
      Collection<A> actual = iterableToCollection(getCastActual());
      ListMultimap<E, A> present = LinkedListMultimap.create();
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
      for (E excludedItem : Sets.newLinkedHashSet(excluded)) {
        for (A actualItem : actual) {
          if (correspondence.safeCompare(actualItem, excludedItem, exceptions)) {
            present.put(excludedItem, actualItem);
          }
        }
      }
      // Fail if we found any matches.
      if (!present.isEmpty()) {
        ImmutableList.Builder<Fact> facts = ImmutableList.builder();
        facts.add(fact("expected not to contain any of", annotateEmptyStrings(excluded)));
        facts.addAll(correspondence.describeForIterable());
        for (E excludedItem : present.keySet()) {
          List<A> actualItems = present.get(excludedItem);
          facts.add(fact("but contained", annotateEmptyStrings(actualItems)));
          facts.add(fact("corresponding to", excludedItem));
          facts.add(simpleFact("---"));
        }
        facts.add(subject.fullContents());
        facts.addAll(exceptions.describeAsAdditionalInfo());
        subject.failWithoutActual(facts.build());

        return;
      }
      // Found no match, but we still need to fail if we hit an exception along the way.
      if (exceptions.hasCompareException()) {
        subject.failWithoutActual(
            ImmutableList.<Fact>builder()
                .addAll(exceptions.describeAsMainCause())
                .add(fact("expected not to contain any of", annotateEmptyStrings(excluded)))
                .addAll(correspondence.describeForIterable())
                .add(simpleFact("found no matches (but failing because of exception)"))
                .add(subject.fullContents())
                .build());
      }
    }

    /**
     * Checks that the subject contains no elements that correspond to any of the given elements.
     * (Duplicates are irrelevant to this test, which fails if any of the subject elements
     * correspond to any of the given elements.)
     */
    @SuppressWarnings("AvoidObjectArrays")
    public void containsNoneIn(E[] excluded) {
      containsNoneIn(asList(excluded));
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Iterable<A> getCastActual() {
      return (Iterable<A>) checkNotNull(subject.actual);
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
      @Nullable Pairing pair(
          List<? extends E> expectedValues,
          List<? extends A> actualValues,
          Correspondence.ExceptionStore exceptions) {
        Pairing pairing = new Pairing();

        // Populate expectedKeys with the keys of the corresponding elements of expectedValues.
        // We do this ahead of time to avoid invoking the key function twice for each element.
        List<@Nullable Object> expectedKeys = new ArrayList<>(expectedValues.size());
        for (E expected : expectedValues) {
          expectedKeys.add(expectedKey(expected, exceptions));
        }

        // Populate pairedKeysToExpectedValues with *all* the expected values with non-null keys.
        // We will remove the unpaired keys later. Return null if we find a duplicate key.
        for (int i = 0; i < expectedValues.size(); i++) {
          E expected = expectedValues.get(i);
          Object key = expectedKeys.get(i);
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
          Object key = actualKey(actual, exceptions);
          if (pairing.pairedKeysToExpectedValues.containsKey(key)) {
            pairing.pairedKeysToActualValues.put(checkNotNull(key), actual);
          } else {
            pairing.unpairedActualValues.add(actual);
          }
        }

        // Populate unpairedExpectedValues and remove unpaired keys from pairedKeysToExpectedValues.
        for (int i = 0; i < expectedValues.size(); i++) {
          E expected = expectedValues.get(i);
          Object key = expectedKeys.get(i);
          if (!pairing.pairedKeysToActualValues.containsKey(key)) {
            pairing.unpairedExpectedValues.add(expected);
            pairing.pairedKeysToExpectedValues.remove(key);
          }
        }

        return pairing;
      }

      List<A> pairOne(
          E expectedValue,
          Iterable<? extends A> actualValues,
          Correspondence.ExceptionStore exceptions) {
        Object key = expectedKey(expectedValue, exceptions);
        List<A> matches = new ArrayList<>();
        if (key != null) {
          for (A actual : actualValues) {
            if (key.equals(actualKey(actual, exceptions))) {
              matches.add(actual);
            }
          }
        }
        return matches;
      }

      private @Nullable Object actualKey(A actual, Correspondence.ExceptionStore exceptions) {
        try {
          return actualKeyFunction.apply(actual);
        } catch (RuntimeException e) {
          exceptions.addActualKeyFunctionException(
              IterableSubject.UsingCorrespondence.Pairer.class, e, actual);
          return null;
        }
      }

      private @Nullable Object expectedKey(E expected, Correspondence.ExceptionStore exceptions) {
        try {
          return expectedKeyFunction.apply(expected);
        } catch (RuntimeException e) {
          exceptions.addExpectedKeyFunctionException(
              IterableSubject.UsingCorrespondence.Pairer.class, e, expected);
          return null;
        }
      }
    }

    /** A description of a pairing between expected and actual values. N.B. This is mutable. */
    private final class Pairing {

      /**
       * Map from keys used in the pairing to the expected value with that key. Iterates in the
       * order the expected values appear in the input. Will never contain null keys.
       */
      private final Map<Object, E> pairedKeysToExpectedValues = new LinkedHashMap<>();

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
