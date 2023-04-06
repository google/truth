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
import static com.google.common.truth.SubjectUtils.countDuplicatesAndAddTypeInfo;
import static com.google.common.truth.SubjectUtils.hasMatchingToStringPair;
import static com.google.common.truth.SubjectUtils.objectToTypeName;
import static com.google.common.truth.SubjectUtils.retainMatchingToString;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Propositions for {@link Multimap} subjects.
 *
 * @author Daniel Ploch
 * @author Kurt Alfred Kluever
 */
public class MultimapSubject extends Subject {

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED = () -> {};

  private final @Nullable Multimap<?, ?> actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected MultimapSubject(FailureMetadata metadata, @Nullable Multimap<?, ?> multimap) {
    this(metadata, multimap, null);
  }

  MultimapSubject(
      FailureMetadata metadata,
      @Nullable Multimap<?, ?> multimap,
      @Nullable String typeDescription) {
    super(metadata, multimap, typeDescription);
    this.actual = multimap;
  }

  /** Fails if the multimap is not empty. */
  public final void isEmpty() {
    if (!checkNotNull(actual).isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Fails if the multimap is empty. */
  public final void isNotEmpty() {
    if (checkNotNull(actual).isEmpty()) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Fails if the multimap does not have the given size. */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    check("size()").that(checkNotNull(actual).size()).isEqualTo(expectedSize);
  }

  /** Fails if the multimap does not contain the given key. */
  public final void containsKey(@Nullable Object key) {
    check("keySet()").that(checkNotNull(actual).keySet()).contains(key);
  }

  /** Fails if the multimap contains the given key. */
  public final void doesNotContainKey(@Nullable Object key) {
    check("keySet()").that(checkNotNull(actual).keySet()).doesNotContain(key);
  }

  /** Fails if the multimap does not contain the given entry. */
  public final void containsEntry(@Nullable Object key, @Nullable Object value) {
    // TODO(kak): Can we share any of this logic w/ MapSubject.containsEntry()?
    checkNotNull(actual);
    if (!actual.containsEntry(key, value)) {
      Map.Entry<@Nullable Object, @Nullable Object> entry = immutableEntry(key, value);
      ImmutableList<Map.Entry<@Nullable Object, @Nullable Object>> entryList =
          ImmutableList.of(entry);
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
                    retainMatchingToString(actual.entries(), /* itemsToCheck = */ entryList))),
            fact("full contents", actualCustomStringRepresentationForPackageMembersToCall()));
      } else if (actual.containsKey(key)) {
        failWithoutActual(
            fact("expected to contain entry", entry),
            simpleFact("but did not"),
            fact("though it did contain values with that key", actual.asMap().get(key)),
            fact("full contents", actualCustomStringRepresentationForPackageMembersToCall()));
      } else if (actual.containsValue(value)) {
        Set<@Nullable Object> keys = new LinkedHashSet<>();
        for (Map.Entry<?, ?> actualEntry : actual.entries()) {
          if (Objects.equal(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithoutActual(
            fact("expected to contain entry", entry),
            simpleFact("but did not"),
            fact("though it did contain keys with that value", keys),
            fact("full contents", actualCustomStringRepresentationForPackageMembersToCall()));
      } else {
        failWithActual("expected to contain entry", immutableEntry(key, value));
      }
    }
  }

  /** Fails if the multimap contains the given entry. */
  public final void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    checkNoNeedToDisplayBothValues("entries()")
        .that(checkNotNull(actual).entries())
        .doesNotContain(immutableEntry(key, value));
  }

  /**
   * Returns a context-aware {@link Subject} for making assertions about the values for the given
   * key within the {@link Multimap}.
   *
   * <p>This method performs no checks on its own and cannot cause test failures. Subsequent
   * assertions must be chained onto this method call to test properties of the {@link Multimap}.
   */
  @SuppressWarnings("unchecked") // safe because we only read, not write
  /*
   * non-final because it's overridden by MultimapWithProtoValuesSubject.
   *
   * If we really, really wanted it to be final, we could investigate whether
   * MultimapWithProtoValuesFluentAssertion could provide its own valuesForKey method. But that
   * would force callers to perform any configuration _before_ the valuesForKey call, while
   * currently they must perform it _after_.
   */
  public IterableSubject valuesForKey(@Nullable Object key) {
    return check("valuesForKey(%s)", key)
        .that(((Multimap<@Nullable Object, @Nullable Object>) checkNotNull(actual)).get(key));
  }

  @Override
  public final void isEqualTo(@Nullable Object other) {
    @SuppressWarnings("UndefinedEquals") // the contract of this method is to follow Multimap.equals
    boolean isEqual = Objects.equal(actual, other);
    if (isEqual) {
      return;
    }

    // Fail but with a more descriptive message:
    if ((actual instanceof ListMultimap && other instanceof SetMultimap)
        || (actual instanceof SetMultimap && other instanceof ListMultimap)) {
      String actualType = (actual instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
      String otherType = (other instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
      failWithoutActual(
          fact("expected", other),
          fact("an instance of", otherType),
          fact("but was", actualCustomStringRepresentationForPackageMembersToCall()),
          fact("an instance of", actualType),
          simpleFact(
              lenientFormat(
                  "a %s cannot equal a %s if either is non-empty", actualType, otherType)));
    } else if (actual instanceof ListMultimap) {
      containsExactlyEntriesIn((Multimap<?, ?>) checkNotNull(other)).inOrder();
    } else if (actual instanceof SetMultimap) {
      containsExactlyEntriesIn((Multimap<?, ?>) checkNotNull(other));
    } else {
      super.isEqualTo(other);
    }
  }

  /**
   * Fails if the {@link Multimap} does not contain precisely the same entries as the argument
   * {@link Multimap}.
   *
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify that
   * the two multimaps iterate fully in the same order. That is, their key sets iterate in the same
   * order, and the value collections for each key iterate in the same order.
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactlyEntriesIn(Multimap<?, ?> expectedMultimap) {
    checkNotNull(expectedMultimap, "expectedMultimap");
    checkNotNull(actual);
    ListMultimap<?, ?> missing = difference(expectedMultimap, actual);
    ListMultimap<?, ?> extra = difference(actual, expectedMultimap);

    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the subject but not enough times. Similarly for unexpected extra items.
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
            fact("expected", annotateEmptyStringsMultimap(expectedMultimap)));
        return ALREADY_FAILED;
      } else {
        failWithActual(
            fact("missing", countDuplicatesMultimap(annotateEmptyStringsMultimap(missing))),
            simpleFact("---"),
            fact("expected", annotateEmptyStringsMultimap(expectedMultimap)));
        return ALREADY_FAILED;
      }
    } else if (!extra.isEmpty()) {
      failWithActual(
          fact("unexpected", countDuplicatesMultimap(annotateEmptyStringsMultimap(extra))),
          simpleFact("---"),
          fact("expected", annotateEmptyStringsMultimap(expectedMultimap)));
      return ALREADY_FAILED;
    }

    return new MultimapInOrder(/* allowUnexpected = */ false, expectedMultimap);
  }

  /**
   * Fails if the {@link Multimap} does not contain at least the entries in the argument {@link
   * Multimap}.
   *
   * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify that
   * the entries are present in the same order as given. That is, the keys are present in the given
   * order in the key set, and the values for each key are present in the given order order in the
   * value collections.
   */
  @CanIgnoreReturnValue
  public final Ordered containsAtLeastEntriesIn(Multimap<?, ?> expectedMultimap) {
    checkNotNull(expectedMultimap, "expectedMultimap");
    checkNotNull(actual);
    ListMultimap<?, ?> missing = difference(expectedMultimap, actual);

    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the subject but not enough times. Similarly for unexpected extra items.
    if (!missing.isEmpty()) {
      failWithActual(
          fact("missing", countDuplicatesMultimap(annotateEmptyStringsMultimap(missing))),
          simpleFact("---"),
          fact("expected to contain at least", annotateEmptyStringsMultimap(expectedMultimap)));
      return ALREADY_FAILED;
    }

    return new MultimapInOrder(/* allowUnexpected = */ true, expectedMultimap);
  }

  /** Fails if the multimap is not empty. */
  @CanIgnoreReturnValue
  @SuppressWarnings("deprecation") // TODO(b/134064106): design an alternative to no-arg check()
  public final Ordered containsExactly() {
    return check().about(iterableEntries()).that(checkNotNull(actual).entries()).containsExactly();
  }

  /**
   * Fails if the multimap does not contain exactly the given set of key/value pairs.
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
   * Fails if the multimap does not contain at least the given key/value pairs.
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
    return new Factory<IterableSubject, Iterable<?>>() {
      @Override
      public IterableSubject createSubject(FailureMetadata metadata, @Nullable Iterable<?> actual) {
        return new IterableEntries(metadata, MultimapSubject.this, checkNotNull(actual));
      }
    };
  }

  private static class IterableEntries extends IterableSubject {
    private final String stringRepresentation;

    IterableEntries(FailureMetadata metadata, MultimapSubject multimapSubject, Iterable<?> actual) {
      super(metadata, actual);
      // We want to use the multimap's toString() instead of the iterable of entries' toString():
      this.stringRepresentation = String.valueOf(multimapSubject.actual);
    }

    @Override
    protected String actualCustomStringRepresentation() {
      return stringRepresentation;
    }
  }

  private class MultimapInOrder implements Ordered {
    private final Multimap<?, ?> expectedMultimap;
    private final boolean allowUnexpected;

    MultimapInOrder(boolean allowUnexpected, Multimap<?, ?> expectedMultimap) {
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
      checkNotNull(actual);
      boolean keysInOrder =
          Lists.newArrayList(Sets.intersection(actual.keySet(), expectedMultimap.keySet()))
              .equals(Lists.newArrayList(expectedMultimap.keySet()));

      LinkedHashSet<@Nullable Object> keysWithValuesOutOfOrder = Sets.newLinkedHashSet();
      for (Object key : expectedMultimap.keySet()) {
        List<?> actualVals = Lists.newArrayList(get(actual, key));
        List<?> expectedVals = Lists.newArrayList(get(expectedMultimap, key));
        Iterator<?> actualIterator = actualVals.iterator();
        for (Object value : expectedVals) {
          if (!advanceToFind(actualIterator, value)) {
            boolean unused = keysWithValuesOutOfOrder.add(key);
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
      if (Objects.equal(iterator.next(), value)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("EmptyList") // ImmutableList doesn't support nullable types
  private static <V extends @Nullable Object> Collection<V> get(
      Multimap<?, V> multimap, @Nullable Object key) {
    if (multimap.containsKey(key)) {
      return checkNotNull(multimap.asMap().get(key));
    } else {
      return Collections.emptyList();
    }
  }

  private static ListMultimap<?, ?> difference(Multimap<?, ?> minuend, Multimap<?, ?> subtrahend) {
    ListMultimap<@Nullable Object, @Nullable Object> difference = LinkedListMultimap.create();
    for (Object key : minuend.keySet()) {
      List<?> valDifference =
          difference(
              Lists.newArrayList(get(minuend, key)), Lists.newArrayList(get(subtrahend, key)));
      difference.putAll(key, valDifference);
    }
    return difference;
  }

  private static List<?> difference(List<?> minuend, List<?> subtrahend) {
    LinkedHashMultiset<@Nullable Object> remaining =
        LinkedHashMultiset.<@Nullable Object>create(subtrahend);
    List<@Nullable Object> difference = Lists.newArrayList();
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
      entries.add(key + "=" + SubjectUtils.countDuplicates(get(multimap, key)));
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
      for (Map.Entry<?, ?> entry : multimap.entries()) {
        Object key =
            Objects.equal(entry.getKey(), "") ? HUMAN_UNDERSTANDABLE_EMPTY_STRING : entry.getKey();
        Object value =
            Objects.equal(entry.getValue(), "")
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
   *   .comparingValuesUsing(correspondence)
   *   .containsEntry(expectedKey, expectedValue);
   * }</pre>
   *
   * where {@code actualMultimap} is a {@code Multimap<?, A>} (or, more generally, a {@code
   * Multimap<?, ? extends A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and
   * {@code expectedValue} is an {@code E}.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual value that is not of type {@code A}.
   */
  public <A extends @Nullable Object, E extends @Nullable Object>
      UsingCorrespondence<A, E> comparingValuesUsing(
          Correspondence<? super A, ? super E> correspondence) {
    return new UsingCorrespondence<>(correspondence);
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
  public final class UsingCorrespondence<A extends @Nullable Object, E extends @Nullable Object> {

    private final Correspondence<? super A, ? super E> correspondence;

    private UsingCorrespondence(Correspondence<? super A, ? super E> correspondence) {
      this.correspondence = checkNotNull(correspondence);
    }

    /**
     * Fails if the multimap does not contain an entry with the given key and a value that
     * corresponds to the given value.
     */
    public void containsEntry(@Nullable Object expectedKey, E expectedValue) {
      if (checkNotNull(actual).containsKey(expectedKey)) {
        // Found matching key.
        Collection<A> actualValues = checkNotNull(getCastActual().asMap().get(expectedKey));
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (A actualValue : actualValues) {
          if (correspondence.safeCompare(actualValue, expectedValue, exceptions)) {
            // Found matching key and value, but we still need to fail if we hit an exception along
            // the way.
            if (exceptions.hasCompareException()) {
              failWithoutActual(
                  ImmutableList.<Fact>builder()
                      .addAll(exceptions.describeAsMainCause())
                      .add(
                          fact(
                              "expected to contain entry",
                              immutableEntry(expectedKey, expectedValue)))
                      .addAll(correspondence.describeForMapValues())
                      .add(
                          fact(
                              "found match (but failing because of exception)",
                              immutableEntry(expectedKey, actualValue)))
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
            ImmutableList.<Fact>builder()
                .add(fact("expected to contain entry", immutableEntry(expectedKey, expectedValue)))
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
        Set<Map.Entry<?, ?>> entries = new LinkedHashSet<>();
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (Map.Entry<?, A> actualEntry : getCastActual().entries()) {
          if (correspondence.safeCompare(actualEntry.getValue(), expectedValue, exceptions)) {
            entries.add(actualEntry);
          }
        }
        if (!entries.isEmpty()) {
          // Found matching values with non-matching keys.
          failWithoutActual(
              ImmutableList.<Fact>builder()
                  .add(
                      fact("expected to contain entry", immutableEntry(expectedKey, expectedValue)))
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
              ImmutableList.<Fact>builder()
                  .add(
                      fact("expected to contain entry", immutableEntry(expectedKey, expectedValue)))
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
     * Fails if the multimap contains an entry with the given key and a value that corresponds to
     * the given value.
     */
    public void doesNotContainEntry(@Nullable Object excludedKey, E excludedValue) {
      if (checkNotNull(actual).containsKey(excludedKey)) {
        Collection<A> actualValues = checkNotNull(getCastActual().asMap().get(excludedKey));
        List<A> matchingValues = new ArrayList<>();
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (A actualValue : actualValues) {
          if (correspondence.safeCompare(actualValue, excludedValue, exceptions)) {
            matchingValues.add(actualValue);
          }
        }
        // Fail if we found a matching value for the key.
        if (!matchingValues.isEmpty()) {
          failWithoutActual(
              ImmutableList.<Fact>builder()
                  .add(
                      fact(
                          "expected not to contain entry",
                          immutableEntry(excludedKey, excludedValue)))
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
                ImmutableList.<Fact>builder()
                    .addAll(exceptions.describeAsMainCause())
                    .add(
                        fact(
                            "expected not to contain entry",
                            immutableEntry(excludedKey, excludedValue)))
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
     * Fails if the map does not contain exactly the keys in the given multimap, mapping to values
     * that correspond to the values of the given multimap.
     *
     * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify
     * that the two Multimaps iterate fully in the same order. That is, their key sets iterate in
     * the same order, and the corresponding value collections for each key iterate in the same
     * order.
     */
    @CanIgnoreReturnValue
    public Ordered containsExactlyEntriesIn(Multimap<?, ? extends E> expectedMultimap) {
      return internalContainsExactlyEntriesIn(expectedMultimap);
    }

    /*
     * This helper exists so that we can declare the simpler, type-parameter-free signature for the
     * public containsExactlyEntriesIn method. This is recommended by Effective Java item 31 (3rd
     * edition).
     */
    @SuppressWarnings("deprecation") // TODO(b/134064106): design an alternative to no-arg check()
    private <K extends @Nullable Object, V extends E> Ordered internalContainsExactlyEntriesIn(
        Multimap<K, V> expectedMultimap) {
      // Note: The non-fuzzy MultimapSubject.containsExactlyEntriesIn has a custom implementation
      // and produces somewhat better failure messages simply asserting about the iterables of
      // entries would: it formats the expected values as  k=[v1, v2] rather than k=v1, k=v2; and in
      // the case where inOrder() fails it says the keys and/or the values for some keys are out of
      // order. We don't bother with that here. It would be nice, but it would be a lot of added
      // complexity for little gain.
      return check()
          .about(iterableEntries())
          .that(checkNotNull(actual).entries())
          .comparingElementsUsing(MultimapSubject.<K, A, V>entryCorrespondence(correspondence))
          .containsExactlyElementsIn(expectedMultimap.entries());
    }

    /**
     * Fails if the map does not contain at least the keys in the given multimap, mapping to values
     * that correspond to the values of the given multimap.
     *
     * <p>A subsequent call to {@link Ordered#inOrder} may be made if the caller wishes to verify
     * that the two Multimaps iterate fully in the same order. That is, their key sets iterate in
     * the same order, and the corresponding value collections for each key iterate in the same
     * order.
     */
    @CanIgnoreReturnValue
    public Ordered containsAtLeastEntriesIn(Multimap<?, ? extends E> expectedMultimap) {
      return internalContainsAtLeastEntriesIn(expectedMultimap);
    }

    /*
     * This helper exists so that we can declare the simpler, type-parameter-free signature for the
     * public containsAtLeastEntriesIn method. This is recommended by Effective Java item 31 (3rd
     * edition).
     */
    @SuppressWarnings("deprecation") // TODO(b/134064106): design an alternative to no-arg check()
    private <K extends @Nullable Object, V extends E> Ordered internalContainsAtLeastEntriesIn(
        Multimap<K, V> expectedMultimap) {
      // Note: The non-fuzzy MultimapSubject.containsAtLeastEntriesIn has a custom implementation
      // and produces somewhat better failure messages simply asserting about the iterables of
      // entries would: it formats the expected values as  k=[v1, v2] rather than k=v1, k=v2; and in
      // the case where inOrder() fails it says the keys and/or the values for some keys are out of
      // order. We don't bother with that here. It would be nice, but it would be a lot of added
      // complexity for little gain.
      return check()
          .about(iterableEntries())
          .that(checkNotNull(actual).entries())
          .comparingElementsUsing(MultimapSubject.<K, A, V>entryCorrespondence(correspondence))
          .containsAtLeastElementsIn(expectedMultimap.entries());
    }

    /**
     * Fails if the multimap does not contain exactly the given set of key/value pairs.
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

    /** Fails if the multimap is not empty. */
    @CanIgnoreReturnValue
    public Ordered containsExactly() {
      return MultimapSubject.this.containsExactly();
    }

    /**
     * Fails if the multimap does not contain at least the given key/value pairs.
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
    private Multimap<?, A> getCastActual() {
      return (Multimap<?, A>) checkNotNull(actual);
    }
  }

  private static <
          K extends @Nullable Object, A extends @Nullable Object, E extends @Nullable Object>
      Correspondence<Map.Entry<K, A>, Map.Entry<K, E>> entryCorrespondence(
          Correspondence<? super A, ? super E> valueCorrespondence) {
    return Correspondence.from(
        (Map.Entry<K, A> actual, Map.Entry<K, E> expected) ->
            Objects.equal(actual.getKey(), expected.getKey())
                && valueCorrespondence.compare(actual.getValue(), expected.getValue()),
        lenientFormat(
            "has a key that is equal to and a value that %s the key and value of",
            valueCorrespondence));
  }
}
