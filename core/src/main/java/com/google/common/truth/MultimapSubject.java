/*
 * Copyright (c) 2014 Google, Inc.
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
import static com.google.common.collect.Maps.immutableEntry;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.SubjectUtils.HUMAN_UNDERSTANDABLE_EMPTY_STRING;
import static com.google.common.truth.SubjectUtils.countDuplicates;
import static com.google.common.truth.SubjectUtils.countDuplicatesAndAddTypeInfo;
import static com.google.common.truth.SubjectUtils.hasMatchingToStringPair;
import static com.google.common.truth.SubjectUtils.objectToTypeName;
import static com.google.common.truth.SubjectUtils.retainMatchingToString;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Multimap} values.
 *
 * @author Daniel Ploch
 * @author Kurt Alfred Kluever
 */
public class MultimapSubject extends Subject {
  private final @Nullable Multimap<?, ?> actual;

  /**
   * The constructor is for use by subclasses only. If you want to create an instance of this class
   * itself, call {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected MultimapSubject(FailureMetadata metadata, @Nullable Multimap<?, ?> actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /** Checks that the actual multimap is empty. */
  public final void isEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected an empty multimap"));
    } else if (!actual.isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Checks that the actual multimap is not empty. */
  public final void isNotEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected a nonempty multimap"));
    } else if (actual.isEmpty()) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Checks that the actual multimap has the given size. */
  public final void hasSize(int size) {
    if (actual == null) {
      failWithActual("expected a multimap with size", size);
    } else if (size < 0) {
      failWithoutActual(
          simpleFact("expected a multimap with a negative size, but that is impossible"),
          fact("expected size", size),
          fact("actual size", actual.size()),
          actualContents());
    } else {
      check("size()").that(actual.size()).isEqualTo(size);
    }
  }

  /** Checks that the actual multimap contains the given key. */
  public final void containsKey(@Nullable Object key) {
    if (actual == null) {
      failWithActual("expected a multimap that contains key", key);
      return;
    }
    check("keySet()").that(actual.keySet()).contains(key);
  }

  /** Checks that the actual multimap does not contain the given key. */
  public final void doesNotContainKey(@Nullable Object key) {
    if (actual == null) {
      failWithActual("expected a multimap that does not contain key", key);
      return;
    }
    check("keySet()").that(actual.keySet()).doesNotContain(key);
  }

  /** Checks that the actual multimap contains the given entry. */
  public final void containsEntry(@Nullable Object key, @Nullable Object value) {
    // TODO(kak): Can we share any of this logic w/ MapSubject.containsEntry()?
    Entry<?, ?> entry = immutableEntry(key, value);
    if (actual == null) {
      failWithActual("expected a multimap that contains entry", entry);
      return;
    }
    if (!actual.containsEntry(key, value)) {
      ImmutableList<Entry<?, ?>> entryList = ImmutableList.of(entry);
      // TODO(cpovirk): If the key is present but not with the right value, we could fail using
      // something like valuesForKey(key).contains(value). Consider whether this is worthwhile.
      if (hasMatchingToStringPair(actual.entries(), entryList)) {
        failWithoutActual(
            fact("expected to contain entry", entry),
            fact("an instance of", objectToTypeName(entry)),
            simpleFact("but did not"),
            fact(
                "though it did contain",
                countDuplicatesAndAddTypeInfo(
                    retainMatchingToString(actual.entries(), /* itemsToCheck= */ entryList))),
            fullContents());
      } else if (actual.containsKey(key)) {
        failWithoutActual(
            fact("expected to contain entry", entry),
            simpleFact("but did not"),
            fact("though it did contain values with that key", actual.asMap().get(key)),
            fullContents());
      } else if (actual.containsValue(value)) {
        Set<@Nullable Object> keys = new LinkedHashSet<>();
        for (Entry<?, ?> actualEntry : actual.entries()) {
          if (Objects.equals(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithoutActual(
            fact("expected to contain entry", entry),
            simpleFact("but did not"),
            fact("though it did contain keys with that value", keys),
            fullContents());
      } else {
        failWithActual("expected to contain entry", immutableEntry(key, value));
      }
    }
  }

  /** Checks that the actual multimap does not contain the given entry. */
  public final void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    Entry<?, ?> entry = immutableEntry(key, value);
    if (actual == null) {
      failWithActual("expected a multimap that does not contain entry", entry);
      return;
    }
    checkNoNeedToDisplayBothValues("entries()").that(actual.entries()).doesNotContain(entry);
  }

  /**
   * Returns a {@link Subject} for making assertions about the values for the given key within the
   * {@link Multimap}.
   *
   * <p>This method performs no checks on its own and cannot cause test failures. Subsequent
   * assertions must be chained onto this method call to test properties of the {@link Multimap}.
   * (There is one small exception: This method does check whether the actual value is null, failing
   * if so.)
   */
  // non-final because it's overridden by MultimapWithProtoValuesSubject.
  public IterableSubject valuesForKey(@Nullable Object key) {
    if (actual == null) {
      failWithoutActual(
          simpleFact("cannot perform assertions on the contents of a null multimap"),
          fact("requested key", key));
      return ignoreCheck().that(ImmutableList.of());
    }
    @SuppressWarnings("unchecked") // safe because we only read, not write
    Multimap<@Nullable Object, ?> castActual = (Multimap<@Nullable Object, ?>) actual;
    return check("valuesForKey(%s)", key).that(castActual.get(key));
  }

  @Override
  public final void isEqualTo(@Nullable Object other) {
    @SuppressWarnings("UndefinedEquals") // the contract of this method is to follow Multimap.equals
    boolean isEqual = Objects.equals(actual, other);
    if (isEqual) {
      return;
    }

    // Fail but with a more descriptive message:
    if (actual == null || other == null) {
      super.isEqualTo(other);
    } else if ((actual instanceof ListMultimap && other instanceof SetMultimap)
        || (actual instanceof SetMultimap && other instanceof ListMultimap)) {
      String actualType = (actual instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
      String otherType = (other instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
      failWithoutActual(
          fact("expected", other),
          fact("an instance of", otherType),
          butWas(),
          fact("an instance of", actualType),
          simpleFact(
              lenientFormat(
                  "a %s cannot equal a %s if either is non-empty", actualType, otherType)));
    } else if (actual instanceof ListMultimap) {
      containsExactlyEntriesIn((Multimap<?, ?>) other).inOrder();
    } else if (actual instanceof SetMultimap) {
      containsExactlyEntriesIn((Multimap<?, ?>) other);
    } else {
      super.isEqualTo(other);
    }
  }

  /**
   * Checks that the actual multimap contains precisely the same entries as the argument {@link
   * Multimap}.
   *
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify that
   * the two multimaps iterate fully in the same order. That is, their key sets iterate in the same
   * order, and the value collections for each key iterate in the same order.
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactlyEntriesIn(@Nullable Multimap<?, ?> expected) {
    if (expected == null) {
      failWithoutActual(
          simpleFact("could not perform containment check because expected multimap was null"),
          actualContents());
      return ALREADY_FAILED;
    } else if (actual == null) {
      failWithActual("expected a multimap that contains exactly", expected);
      return ALREADY_FAILED;
    } else if (expected.isEmpty()) {
      return containsExactly();
    }

    ListMultimap<?, ?> missing = difference(expected, actual);
    ListMultimap<?, ?> extra = difference(actual, expected);

    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the actual multimap but not enough times. Similarly for unexpected extra items.
    if (!missing.isEmpty()) {
      if (!extra.isEmpty()) {
        boolean addTypeInfo = hasMatchingToStringPair(missing.entries(), extra.entries());
        // Note: The usage of countDuplicatesAndAddTypeInfo() below causes entries no longer to be
        // grouped by key in the 'missing' and 'unexpected items' parts of the message (we still
        // show the actual and expected multimaps in the standard format).
        String missingDisplay =
            addTypeInfo
                ? countDuplicatesAndAddTypeInfo(annotateEmptyStringsMultimap(missing).entries())
                : countDuplicatesMultimap(annotateEmptyStringsMultimap(missing));
        String extraDisplay =
            addTypeInfo
                ? countDuplicatesAndAddTypeInfo(annotateEmptyStringsMultimap(extra).entries())
                : countDuplicatesMultimap(annotateEmptyStringsMultimap(extra));
        failWithActual(
            fact("missing", missingDisplay),
            fact("unexpected", extraDisplay),
            simpleFact("---"),
            fact("expected", annotateEmptyStringsMultimap(expected)));
        return ALREADY_FAILED;
      } else {
        failWithActual(
            fact("missing", countDuplicatesMultimap(annotateEmptyStringsMultimap(missing))),
            simpleFact("---"),
            fact("expected", annotateEmptyStringsMultimap(expected)));
        return ALREADY_FAILED;
      }
    } else if (!extra.isEmpty()) {
      failWithActual(
          fact("unexpected", countDuplicatesMultimap(annotateEmptyStringsMultimap(extra))),
          simpleFact("---"),
          fact("expected", annotateEmptyStringsMultimap(expected)));
      return ALREADY_FAILED;
    }

    return MultimapInOrder.create(this, actual, /* allowUnexpected= */ false, expected);
  }

  /**
   * Checks that the actual multimap contains at least the entries in the argument {@link Multimap}.
   *
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify that
   * the entries are present in the same order as given. That is, the keys are present in the given
   * order in the key set, and the values for each key are present in the given order order in the
   * value collections.
   */
  @CanIgnoreReturnValue
  public final Ordered containsAtLeastEntriesIn(@Nullable Multimap<?, ?> expected) {
    if (expected == null) {
      failWithoutActual(
          simpleFact("could not perform containment check because expected multimap was null"),
          actualContents());
      return ALREADY_FAILED;
    } else if (actual == null) {
      failWithActual("expected a multimap that contains at least", expected);
      return ALREADY_FAILED;
    } else if (expected.isEmpty()) {
      return IN_ORDER;
    }

    ListMultimap<?, ?> missing = difference(expected, actual);

    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the actual multimap but not enough times. Similarly for unexpected extra items.
    if (!missing.isEmpty()) {
      failWithActual(
          fact("missing", countDuplicatesMultimap(annotateEmptyStringsMultimap(missing))),
          simpleFact("---"),
          fact("expected to contain at least", annotateEmptyStringsMultimap(expected)));
      return ALREADY_FAILED;
    }

    return MultimapInOrder.create(this, actual, /* allowUnexpected= */ true, expected);
  }

  /** Checks that the actual multimap is empty. */
  @CanIgnoreReturnValue
  public final Ordered containsExactly() {
    isEmpty();
    return IN_ORDER; // for discussion, see MapSubject.containsExactly()
  }

  /**
   * Checks that the actual multimap contains exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactly(
      @Nullable Object k0, @Nullable Object v0, @Nullable Object... rest) {
    return containsExactlyEntriesIn(accumulateMultimap(k0, v0, rest));
  }

  /**
   * Checks that the actual multimap contains at least the given key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   */
  @CanIgnoreReturnValue
  public final Ordered containsAtLeast(
      @Nullable Object k0, @Nullable Object v0, @Nullable Object... rest) {
    return containsAtLeastEntriesIn(accumulateMultimap(k0, v0, rest));
  }

  private static ListMultimap<@Nullable Object, @Nullable Object> accumulateMultimap(
      @Nullable Object k0, @Nullable Object v0, @Nullable Object... rest) {
    checkArgument(
        rest.length % 2 == 0,
        "There must be an equal number of key/value pairs "
            + "(i.e., the number of key/value parameters (%s) must be even).",
        rest.length + 2);

    LinkedListMultimap<@Nullable Object, @Nullable Object> expectedMultimap =
        LinkedListMultimap.create();
    expectedMultimap.put(k0, v0);
    for (int i = 0; i < rest.length; i += 2) {
      expectedMultimap.put(rest[i], rest[i + 1]);
    }
    return expectedMultimap;
  }

  private Factory<IterableSubject, Iterable<?>> iterableEntries() {
    return (metadata, actual) -> IterableEntries.create(metadata, actual, this);
  }

  private static final class IterableEntries extends IterableSubject {
    private final MultimapSubject multimapSubject;

    private IterableEntries(
        FailureMetadata metadata, @Nullable Iterable<?> actual, MultimapSubject multimapSubject) {
      super(metadata, actual);
      this.multimapSubject = multimapSubject;
    }

    @Override
    protected String actualCustomStringRepresentation() {
      // We want to use the multimap's toString() instead of the iterable of entries' toString():
      return multimapSubject.actualCustomStringRepresentationForPackageMembersToCall();
    }

    static IterableEntries create(
        FailureMetadata metadata, @Nullable Iterable<?> actual, MultimapSubject multimapSubject) {
      return new IterableEntries(metadata, actual, multimapSubject);
    }
  }

  private static final class MultimapInOrder implements Ordered {
    private final MultimapSubject subject;
    private final Multimap<?, ?> actual;
    private final Multimap<?, ?> expectedMultimap;
    private final boolean allowUnexpected;

    private MultimapInOrder(
        MultimapSubject subject,
        Multimap<?, ?> actual,
        boolean allowUnexpected,
        Multimap<?, ?> expectedMultimap) {
      this.subject = subject;
      this.actual = actual;
      this.expectedMultimap = expectedMultimap;
      this.allowUnexpected = allowUnexpected;
    }

    /**
     * Checks whether entries in expected appear in the same order in actual.
     *
     * <p>We allow for actual to have more items than the expected to support both {@link
     * #containsExactly} and {@link #containsAtLeast}.
     */
    @Override
    public void inOrder() {
      // We use the fact that Sets.intersection's result has the same order as the first parameter
      @SuppressWarnings("nullness") // TODO: b/339070656: Remove suppression after fix.
      boolean keysInOrder =
          new ArrayList<>(Sets.intersection(actual.keySet(), expectedMultimap.keySet()))
              .equals(new ArrayList<>(expectedMultimap.keySet()));

      LinkedHashSet<@Nullable Object> keysWithValuesOutOfOrder = new LinkedHashSet<>();
      for (Object key : expectedMultimap.keySet()) {
        List<?> actualVals = new ArrayList<@Nullable Object>(get(actual, key));
        List<?> expectedVals = new ArrayList<>(get(expectedMultimap, key));
        Iterator<?> actualIterator = actualVals.iterator();
        for (Object value : expectedVals) {
          if (!advanceToFind(actualIterator, value)) {
            keysWithValuesOutOfOrder.add(key);
            break;
          }
        }
      }

      if (!keysInOrder) {
        if (!keysWithValuesOutOfOrder.isEmpty()) {
          failWithActual(
              simpleFact("contents match, but order was wrong"),
              simpleFact("keys are not in order"),
              fact("keys with out-of-order values", keysWithValuesOutOfOrder),
              simpleFact("---"),
              fact(
                  allowUnexpected ? "expected to contain at least" : "expected", expectedMultimap));
        } else {
          failWithActual(
              simpleFact("contents match, but order was wrong"),
              simpleFact("keys are not in order"),
              simpleFact("---"),
              fact(
                  allowUnexpected ? "expected to contain at least" : "expected", expectedMultimap));
        }
      } else if (!keysWithValuesOutOfOrder.isEmpty()) {
        failWithActual(
            simpleFact("contents match, but order was wrong"),
            fact("keys with out-of-order values", keysWithValuesOutOfOrder),
            simpleFact("---"),
            fact(allowUnexpected ? "expected to contain at least" : "expected", expectedMultimap));
      }
    }

    private void failWithActual(Fact first, Fact... rest) {
      subject.failWithActual(first, rest);
    }

    static MultimapInOrder create(
        MultimapSubject subject,
        Multimap<?, ?> actual,
        boolean allowUnexpected,
        Multimap<?, ?> expectedMultimap) {
      return new MultimapInOrder(subject, actual, allowUnexpected, expectedMultimap);
    }
  }

  /**
   * Advances the iterator until it either returns value, or has no more elements.
   *
   * <p>Returns true if the value was found, false if the end was reached before finding it.
   *
   * <p>This is basically the same as {@link com.google.common.collect.Iterables#contains}, but
   * where the contract explicitly states that the iterator isn't advanced beyond the value if the
   * value is found.
   */
  private static boolean advanceToFind(Iterator<?> iterator, @Nullable Object value) {
    while (iterator.hasNext()) {
      if (Objects.equals(iterator.next(), value)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("EmptyList") // ImmutableList doesn't support nullable types
  private static <V extends @Nullable Object> Collection<V> get(
      Multimap<?, V> multimap, @Nullable Object key) {
    if (multimap.containsKey(key)) {
      return requireNonNull(multimap.asMap().get(key));
    } else {
      return emptyList();
    }
  }

  private static ListMultimap<?, ?> difference(Multimap<?, ?> minuend, Multimap<?, ?> subtrahend) {
    ListMultimap<@Nullable Object, @Nullable Object> difference = LinkedListMultimap.create();
    for (Object key : minuend.keySet()) {
      List<?> valDifference =
          difference(new ArrayList<>(get(minuend, key)), new ArrayList<>(get(subtrahend, key)));
      difference.putAll(key, valDifference);
    }
    return difference;
  }

  private static List<?> difference(List<?> minuend, List<?> subtrahend) {
    LinkedHashMultiset<@Nullable Object> remaining =
        LinkedHashMultiset.<@Nullable Object>create(subtrahend);
    List<@Nullable Object> difference = new ArrayList<>();
    for (Object elem : minuend) {
      if (!remaining.remove(elem)) {
        difference.add(elem);
      }
    }
    return difference;
  }

  private static String countDuplicatesMultimap(Multimap<?, ?> multimap) {
    List<String> entries = new ArrayList<>();
    for (Object key : multimap.keySet()) {
      entries.add(key + "=" + countDuplicates(get(multimap, key)));
    }

    StringBuilder sb = new StringBuilder();
    sb.append("{");
    Joiner.on(", ").appendTo(sb, entries);
    sb.append("}");
    return sb.toString();
  }

  /**
   * Returns a multimap with all empty strings (as keys or values) replaced by a non-empty human
   * understandable indicator for an empty string.
   *
   * <p>Returns the given multimap if it contains no empty strings.
   */
  private static Multimap<?, ?> annotateEmptyStringsMultimap(Multimap<?, ?> multimap) {
    if (multimap.containsKey("") || multimap.containsValue("")) {
      ListMultimap<@Nullable Object, @Nullable Object> annotatedMultimap =
          LinkedListMultimap.create();
      for (Entry<?, ?> entry : multimap.entries()) {
        Object key =
            Objects.equals(entry.getKey(), "") ? HUMAN_UNDERSTANDABLE_EMPTY_STRING : entry.getKey();
        Object value =
            Objects.equals(entry.getValue(), "")
                ? HUMAN_UNDERSTANDABLE_EMPTY_STRING
                : entry.getValue();
        annotatedMultimap.put(key, value);
      }
      return annotatedMultimap;
    } else {
      return multimap;
    }
  }

  /**
   * Starts a method chain for a check in which the actual values (i.e. the values of the {@link
   * Multimap} under test) are compared to expected values using the given {@link Correspondence}.
   * The actual values must be of type {@code A}, and the expected values must be of type {@code E}.
   * The check is actually executed by continuing the method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualMultimap)
   *     .comparingValuesUsing(correspondence)
   *     .containsEntry(expectedKey, expectedValue);
   * }</pre>
   *
   * where {@code actualMultimap} is a {@code Multimap<?, A>} (or, more generally, a {@code
   * Multimap<?, ? extends A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and
   * {@code expectedValue} is an {@code E}.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual multimap that is not of type {@code A}.
   */
  public <A extends @Nullable Object, E extends @Nullable Object>
      UsingCorrespondence<A, E> comparingValuesUsing(
          Correspondence<? super A, ? super E> correspondence) {
    return UsingCorrespondence.create(this, correspondence);
  }

  // TODO(b/69154276): Add formattingDiffsUsing, like we have on MapSubject, once we have
  // implemented Smart Diffs for multimaps. We could add it now, but there is no way it could have
  // any effect, and it would not be testable.

  /**
   * A partially specified check in which the actual values (i.e. the values of the {@link Multimap}
   * under test) are compared to expected values using a {@link Correspondence}. The expected values
   * are of type {@code E}. Call methods on this object to actually execute the check.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   */
  public static final class UsingCorrespondence<
      A extends @Nullable Object, E extends @Nullable Object> {
    private final MultimapSubject subject;
    private final Correspondence<? super A, ? super E> correspondence;
    private final @Nullable Multimap<?, ?> actual;

    private UsingCorrespondence(
        MultimapSubject subject, Correspondence<? super A, ? super E> correspondence) {
      this.subject = subject;
      this.correspondence = checkNotNull(correspondence);
      this.actual = subject.actual;
    }

    /**
     * Checks that the actual multimap contains an entry with the given key and a value that
     * corresponds to the given value.
     */
    @SuppressWarnings("nullness") // TODO: b/423853632 - Remove after checker is fixed.
    public void containsEntry(@Nullable Object key, E value) {
      Entry<Object, E> entry = immutableEntry(key, value);
      if (actual == null) {
        failWithActual("expected a multimap that contains entry", entry);
        return;
      }
      Collection<A> actualValues = castActual(actual).asMap().get(key);
      if (actualValues != null) {
        // Found matching key.
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (A actualValue : actualValues) {
          if (correspondence.safeCompare(actualValue, value, exceptions)) {
            // Found matching key and value, but we still need to fail if we hit an exception along
            // the way.
            if (exceptions.hasCompareException()) {
              failWithoutActual(
                  factsBuilder()
                      .addAll(exceptions.describeAsMainCause())
                      .add(fact("expected to contain entry", entry))
                      .addAll(correspondence.describeForMapValues())
                      .add(
                          fact(
                              "found match (but failing because of exception)",
                              immutableEntry(key, actualValue)))
                      .add(
                          fact(
                              "full contents",
                              actualCustomStringRepresentationForPackageMembersToCall()))
                      .build());
            }
            return;
          }
        }
        // Found matching key with non-matching values.
        failWithoutActual(
            factsBuilder()
                .add(fact("expected to contain entry", entry))
                .addAll(correspondence.describeForMapValues())
                .add(simpleFact("but did not"))
                .add(fact("though it did contain values for that key", actualValues))
                .add(
                    fact(
                        "full contents", actualCustomStringRepresentationForPackageMembersToCall()))
                .addAll(exceptions.describeAsAdditionalInfo())
                .build());
      } else {
        // Did not find matching key.
        Set<Entry<?, ?>> entries = new LinkedHashSet<>();
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (Entry<?, A> actualEntry : castActual(actual).entries()) {
          if (correspondence.safeCompare(actualEntry.getValue(), value, exceptions)) {
            entries.add(actualEntry);
          }
        }
        if (!entries.isEmpty()) {
          // Found matching values with non-matching keys.
          failWithoutActual(
              factsBuilder()
                  .add(fact("expected to contain entry", entry))
                  .addAll(correspondence.describeForMapValues())
                  .add(simpleFact("but did not"))
                  // The corresponding failure in the non-Correspondence case reports the keys
                  // mapping to the expected value. Here, we show the full entries, because for some
                  // Correspondences it may not be obvious which of the actual values it was that
                  // corresponded to the expected value.
                  .add(fact("though it did contain entries with matching values", entries))
                  .add(
                      fact(
                          "full contents",
                          actualCustomStringRepresentationForPackageMembersToCall()))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        } else {
          // Did not find matching key or value.
          failWithoutActual(
              factsBuilder()
                  .add(fact("expected to contain entry", entry))
                  .addAll(correspondence.describeForMapValues())
                  .add(simpleFact("but did not"))
                  .add(
                      fact(
                          "full contents",
                          actualCustomStringRepresentationForPackageMembersToCall()))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        }
      }
    }

    /**
     * Checks that the actual multimap does not contain an entry with the given key and a value that
     * corresponds to the given value.
     */
    public void doesNotContainEntry(@Nullable Object key, E value) {
      Entry<?, E> entry = immutableEntry(key, value);
      if (actual == null) {
        failWithActual("expected a multimap that does not contain entry", entry);
        return;
      }
      Collection<A> actualValues = castActual(actual).asMap().get(key);
      if (actualValues != null) {
        List<A> matchingValues = new ArrayList<>();
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (A actualValue : actualValues) {
          if (correspondence.safeCompare(actualValue, value, exceptions)) {
            matchingValues.add(actualValue);
          }
        }
        // Fail if we found a matching value for the key.
        if (!matchingValues.isEmpty()) {
          failWithoutActual(
              factsBuilder()
                  .add(fact("expected not to contain entry", entry))
                  .addAll(correspondence.describeForMapValues())
                  .add(fact("but contained that key with matching values", matchingValues))
                  .add(
                      fact(
                          "full contents",
                          actualCustomStringRepresentationForPackageMembersToCall()))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        } else {
          // No value matched, but we still need to fail if we hit an exception along the way.
          if (exceptions.hasCompareException()) {
            failWithoutActual(
                factsBuilder()
                    .addAll(exceptions.describeAsMainCause())
                    .add(fact("expected not to contain entry", entry))
                    .addAll(correspondence.describeForMapValues())
                    .add(simpleFact("found no match (but failing because of exception)"))
                    .add(
                        fact(
                            "full contents",
                            actualCustomStringRepresentationForPackageMembersToCall()))
                    .build());
          }
        }
      }
    }

    /**
     * Checks that the actual multimap contains exactly the keys in the given multimap, mapping to
     * values that correspond to the values of the given multimap.
     *
     * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify
     * that the two Multimaps iterate fully in the same order. That is, their key sets iterate in
     * the same order, and the corresponding value collections for each key iterate in the same
     * order.
     */
    @CanIgnoreReturnValue
    public Ordered containsExactlyEntriesIn(@Nullable Multimap<?, ? extends E> expected) {
      if (expected == null) {
        failWithoutActual(
            simpleFact("could not perform containment check because expected multimap was null"),
            actualContents());
        return ALREADY_FAILED;
      } else if (actual == null) {
        failWithActual("expected a multimap that contains exactly", expected);
        return ALREADY_FAILED;
      }
      return internalContainsExactlyEntriesIn(actual, expected);
    }

    /*
     * This helper exists so that we can declare the simpler, type-parameter-free signature for the
     * public containsExactlyEntriesIn method. This is recommended by Effective Java item 31 (3rd
     * edition).
     */
    private <K extends @Nullable Object, V extends E> Ordered internalContainsExactlyEntriesIn(
        Multimap<?, ?> actual, Multimap<K, V> expected) {
      // Note: The non-fuzzy MultimapSubject.containsExactlyEntriesIn has a custom implementation
      // and produces somewhat better failure messages simply asserting about the iterables of
      // entries would: it formats the expected values as  k=[v1, v2] rather than k=v1, k=v2; and in
      // the case where inOrder() fails it says the keys and/or the values for some keys are out of
      // order. We don't bother with that here. It would be nice, but it would be a lot of added
      // complexity for little gain.
      return subject
          .substituteCheck()
          .about(subject.iterableEntries())
          .that(actual.entries())
          .comparingElementsUsing(MultimapSubject.<K, A, V>entryCorrespondence(correspondence))
          .containsExactlyElementsIn(expected.entries());
    }

    /**
     * Checks that the actual multimap contains at least the keys in the given multimap, mapping to
     * values that correspond to the values of the given multimap.
     *
     * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify
     * that the two Multimaps iterate fully in the same order. That is, their key sets iterate in
     * the same order, and the corresponding value collections for each key iterate in the same
     * order.
     */
    @CanIgnoreReturnValue
    public Ordered containsAtLeastEntriesIn(@Nullable Multimap<?, ? extends E> expected) {
      if (expected == null) {
        failWithoutActual(
            simpleFact("could not perform containment check because expected multimap was null"),
            actualContents());
        return ALREADY_FAILED;
      } else if (actual == null) {
        failWithActual("expected a multimap that contains at least", expected);
        return ALREADY_FAILED;
      }
      return internalContainsAtLeastEntriesIn(actual, expected);
    }

    /*
     * This helper exists so that we can declare the simpler, type-parameter-free signature for the
     * public containsAtLeastEntriesIn method. This is recommended by Effective Java item 31 (3rd
     * edition).
     */
    private <K extends @Nullable Object, V extends E> Ordered internalContainsAtLeastEntriesIn(
        Multimap<?, ?> actual, Multimap<K, V> expected) {
      // Note: The non-fuzzy MultimapSubject.containsAtLeastEntriesIn has a custom implementation
      // and produces somewhat better failure messages simply asserting about the iterables of
      // entries would: it formats the expected values as  k=[v1, v2] rather than k=v1, k=v2; and in
      // the case where inOrder() fails it says the keys and/or the values for some keys are out of
      // order. We don't bother with that here. It would be nice, but it would be a lot of added
      // complexity for little gain.
      return subject
          .substituteCheck()
          .about(subject.iterableEntries())
          .that(actual.entries())
          .comparingElementsUsing(MultimapSubject.<K, A, V>entryCorrespondence(correspondence))
          .containsAtLeastElementsIn(expected.entries());
    }

    /**
     * Checks that the actual multimap contains exactly the given set of key/value pairs.
     *
     * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
     * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
     */
    @CanIgnoreReturnValue
    public Ordered containsExactly(@Nullable Object k0, @Nullable E v0, @Nullable Object... rest) {
      @SuppressWarnings("unchecked")
      Multimap<?, E> expectedMultimap = (Multimap<?, E>) accumulateMultimap(k0, v0, rest);
      return containsExactlyEntriesIn(expectedMultimap);
    }

    /** Checks that the actual multimap is empty. */
    @CanIgnoreReturnValue
    public Ordered containsExactly() {
      return subject.containsExactly();
    }

    /**
     * Checks that the actual multimap contains at least the given key/value pairs.
     *
     * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
     * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
     */
    @CanIgnoreReturnValue
    public Ordered containsAtLeast(@Nullable Object k0, @Nullable E v0, @Nullable Object... rest) {
      @SuppressWarnings("unchecked")
      Multimap<?, E> expectedMultimap = (Multimap<?, E>) accumulateMultimap(k0, v0, rest);
      return containsAtLeastEntriesIn(expectedMultimap);
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Multimap<?, A> castActual(Multimap<?, ?> actual) {
      return (Multimap<?, A>) actual;
    }

    private String actualCustomStringRepresentationForPackageMembersToCall() {
      return subject.actualCustomStringRepresentationForPackageMembersToCall();
    }

    private Fact actualContents() {
      return subject.actualContents();
    }

    private void failWithActual(String key, @Nullable Object value) {
      subject.failWithActual(key, value);
    }

    private void failWithoutActual(Iterable<Fact> facts) {
      subject.failWithoutActual(facts);
    }

    private void failWithoutActual(Fact first, Fact... rest) {
      subject.failWithoutActual(first, rest);
    }

    static <E extends @Nullable Object, A extends @Nullable Object>
        UsingCorrespondence<A, E> create(
            MultimapSubject subject, Correspondence<? super A, ? super E> correspondence) {
      return new UsingCorrespondence<>(subject, correspondence);
    }
  }

  private static <
          K extends @Nullable Object, A extends @Nullable Object, E extends @Nullable Object>
      Correspondence<Entry<K, A>, Entry<K, E>> entryCorrespondence(
          Correspondence<? super A, ? super E> valueCorrespondence) {
    return Correspondence.from(
        (actual, expected) ->
            Objects.equals(actual.getKey(), expected.getKey())
                && valueCorrespondence.compare(actual.getValue(), expected.getValue()),
        lenientFormat(
            "has a key that is equal to and a value that %s the key and value of",
            valueCorrespondence));
  }

  private Fact fullContents() {
    return actualValue("full contents");
  }

  private Fact actualContents() {
    return actualValue("actual contents");
  }

  /** Ordered implementation that does nothing because it's already known to be true. */
  private static final Ordered IN_ORDER = () -> {};

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED = () -> {};

  static Factory<MultimapSubject, Multimap<?, ?>> multimaps() {
    return MultimapSubject::new;
  }
}
