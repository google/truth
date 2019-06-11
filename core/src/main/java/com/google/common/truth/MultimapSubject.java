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
import static com.google.common.truth.Facts.facts;
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
import com.google.common.collect.Maps;
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
import java.util.Map.Entry;
import java.util.Set;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Propositions for {@link Multimap} subjects.
 *
 * @author Daniel Ploch
 * @author Kurt Alfred Kluever
 */
public class MultimapSubject extends Subject {

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED =
      new Ordered() {
        @Override
        public void inOrder() {}
      };

  private final Multimap<?, ?> actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check}{@code .that(actual)}.
   */
  protected MultimapSubject(FailureMetadata metadata, @NullableDecl Multimap<?, ?> multimap) {
    this(metadata, multimap, null);
  }

  MultimapSubject(
      FailureMetadata metadata,
      @NullableDecl Multimap<?, ?> multimap,
      @NullableDecl String typeDescription) {
    super(metadata, multimap, typeDescription);
    this.actual = multimap;
  }

  /** Fails if the multimap is not empty. */
  public final void isEmpty() {
    if (!actual.isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Fails if the multimap is empty. */
  public final void isNotEmpty() {
    if (actual.isEmpty()) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Fails if the multimap does not have the given size. */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    check("size()").that(actual.size()).isEqualTo(expectedSize);
  }

  /** Fails if the multimap does not contain the given key. */
  public final void containsKey(@NullableDecl Object key) {
    check("keySet()").that(actual.keySet()).contains(key);
  }

  /** Fails if the multimap contains the given key. */
  public final void doesNotContainKey(@NullableDecl Object key) {
    check("keySet()").that(actual.keySet()).doesNotContain(key);
  }

  /** Fails if the multimap does not contain the given entry. */
  public final void containsEntry(@NullableDecl Object key, @NullableDecl Object value) {
    // TODO(kak): Can we share any of this logic w/ MapSubject.containsEntry()?
    if (!actual.containsEntry(key, value)) {
      Entry<Object, Object> entry = Maps.immutableEntry(key, value);
      List<Entry<Object, Object>> entryList = ImmutableList.of(entry);
      if (hasMatchingToStringPair(actual.entries(), entryList)) {
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that <%s> contains entry <%s (%s)>. However, it does contain entries "
                        + "<%s>",
                    actualCustomStringRepresentationForPackageMembersToCall(),
                    entry,
                    objectToTypeName(entry),
                    countDuplicatesAndAddTypeInfo(
                        retainMatchingToString(actual.entries(), entryList /* itemsToCheck */)))));
      } else if (actual.containsKey(key)) {
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that <%s> contains entry <%s>. However, it has a mapping from <%s>"
                        + " to <%s>",
                    actualCustomStringRepresentationForPackageMembersToCall(),
                    entry,
                    key,
                    actual.asMap().get(key))));
      } else if (actual.containsValue(value)) {
        Set<Object> keys = new LinkedHashSet<>();
        for (Entry<?, ?> actualEntry : actual.entries()) {
          if (Objects.equal(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that <%s> contains entry <%s>. "
                        + "However, the following keys are mapped to <%s>: %s",
                    actualCustomStringRepresentationForPackageMembersToCall(),
                    entry,
                    value,
                    keys)));
      } else {
        failWithActual("expected to contain entry", Maps.immutableEntry(key, value));
      }
    }
  }

  /** Fails if the multimap contains the given entry. */
  public final void doesNotContainEntry(@NullableDecl Object key, @NullableDecl Object value) {
    checkNoNeedToDisplayBothValues("entries()")
        .that(actual.entries())
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
  public IterableSubject valuesForKey(@NullableDecl Object key) {
    return check("valuesForKey(%s)", key).that(((Multimap<Object, Object>) actual).get(key));
  }

  @Override
  public final void isEqualTo(@NullableDecl Object other) {
    @SuppressWarnings("UndefinedEquals") // the contract of this method is to follow Multimap.equals
    boolean isEqual = Objects.equal(actual, other);
    if (isEqual) {
      return;
    }

    // Fail but with a more descriptive message:
    if ((actual instanceof ListMultimap && other instanceof SetMultimap)
        || (actual instanceof SetMultimap && other instanceof ListMultimap)) {
      String mapType1 = (actual instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
      String mapType2 = (other instanceof ListMultimap) ? "ListMultimap" : "SetMultimap";
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that %s <%s> is equal to %s <%s>. "
                      + "A %s cannot equal a %s if either is non-empty.",
                  mapType1,
                  actualCustomStringRepresentationForPackageMembersToCall(),
                  mapType2,
                  other,
                  mapType1,
                  mapType2)));
    } else if (actual instanceof ListMultimap) {
      containsExactlyEntriesIn((Multimap<?, ?>) other).inOrder();
    } else if (actual instanceof SetMultimap) {
      containsExactlyEntriesIn((Multimap<?, ?>) other);
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
    ListMultimap<?, ?> missing = difference(expectedMultimap, actual);
    ListMultimap<?, ?> extra = difference(actual, expectedMultimap);

    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the subject but not enough times. Similarly for unexpected extra items.
    if (!missing.isEmpty()) {
      if (!extra.isEmpty()) {
        boolean addTypeInfo = hasMatchingToStringPair(missing.entries(), extra.entries());
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that <%s> contains exactly <%s>. "
                        + "It is missing <%s> and has unexpected items <%s>",
                    actualCustomStringRepresentationForPackageMembersToCall(),
                    annotateEmptyStringsMultimap(expectedMultimap),
                    // Note: The usage of countDuplicatesAndAddTypeInfo() below causes entries no
                    // longer to be grouped by key in the 'missing' and 'unexpected items' parts of
                    // the message (we still show the actual and expected multimaps in the standard
                    // format).
                    addTypeInfo
                        ? countDuplicatesAndAddTypeInfo(
                            annotateEmptyStringsMultimap(missing).entries())
                        : countDuplicatesMultimap(annotateEmptyStringsMultimap(missing)),
                    addTypeInfo
                        ? countDuplicatesAndAddTypeInfo(
                            annotateEmptyStringsMultimap(extra).entries())
                        : countDuplicatesMultimap(annotateEmptyStringsMultimap(extra)))));
        return ALREADY_FAILED;
      } else {
        failWithBadResults(
            "contains exactly",
            annotateEmptyStringsMultimap(expectedMultimap),
            "is missing",
            countDuplicatesMultimap(annotateEmptyStringsMultimap(missing)));
        return ALREADY_FAILED;
      }
    } else if (!extra.isEmpty()) {
      failWithBadResults(
          "contains exactly",
          annotateEmptyStringsMultimap(expectedMultimap),
          "has unexpected items",
          countDuplicatesMultimap(annotateEmptyStringsMultimap(extra)));
      return ALREADY_FAILED;
    }

    return new MultimapInOrder("contains exactly", expectedMultimap);
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
    ListMultimap<?, ?> missing = difference(expectedMultimap, actual);

    // TODO(kak): Possible enhancement: Include "[1 copy]" if the element does appear in
    // the subject but not enough times. Similarly for unexpected extra items.
    if (!missing.isEmpty()) {
      failWithBadResults(
          "contains at least",
          annotateEmptyStringsMultimap(expectedMultimap),
          "is missing",
          countDuplicatesMultimap(annotateEmptyStringsMultimap(missing)));
      return ALREADY_FAILED;
    }

    return new MultimapInOrder("contains at least", expectedMultimap);
  }

  /** Fails if the multimap is not empty. */
  @CanIgnoreReturnValue
  public final Ordered containsExactly() {
    return check().about(iterableEntries()).that(actual.entries()).containsExactly();
  }

  /**
   * Fails if the multimap does not contain exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactly(
      @NullableDecl Object k0, @NullableDecl Object v0, Object... rest) {
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
      @NullableDecl Object k0, @NullableDecl Object v0, Object... rest) {
    return containsAtLeastEntriesIn(accumulateMultimap(k0, v0, rest));
  }

  private static Multimap<Object, Object> accumulateMultimap(
      @NullableDecl Object k0, @NullableDecl Object v0, Object... rest) {
    checkArgument(
        rest.length % 2 == 0,
        "There must be an equal number of key/value pairs "
            + "(i.e., the number of key/value parameters (%s) must be even).",
        rest.length + 2);

    LinkedListMultimap<Object, Object> expectedMultimap = LinkedListMultimap.create();
    expectedMultimap.put(k0, v0);
    for (int i = 0; i < rest.length; i += 2) {
      expectedMultimap.put(rest[i], rest[i + 1]);
    }
    return expectedMultimap;
  }

  private Factory<IterableSubject, Iterable<?>> iterableEntries() {
    return new Factory<IterableSubject, Iterable<?>>() {
      @Override
      public IterableSubject createSubject(FailureMetadata metadata, Iterable<?> actual) {
        return new IterableEntries(metadata, MultimapSubject.this, actual);
      }
    };
  }

  private static class IterableEntries extends IterableSubject {
    private final String stringRepresentation;

    IterableEntries(FailureMetadata metadata, MultimapSubject multimapSubject, Iterable<?> actual) {
      super(metadata, actual);
      // We want to use the multimap's toString() instead of the iterable of entries' toString():
      this.stringRepresentation = multimapSubject.actual.toString();
    }

    @Override
    protected String actualCustomStringRepresentation() {
      return stringRepresentation;
    }
  }

  private class MultimapInOrder implements Ordered {
    private final Multimap<?, ?> expectedMultimap;
    private final String verb;

    MultimapInOrder(String verb, Multimap<?, ?> expectedMultimap) {
      this.expectedMultimap = expectedMultimap;
      this.verb = verb;
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
      boolean keysInOrder =
          Lists.newArrayList(Sets.intersection(actual.keySet(), expectedMultimap.keySet()))
              .equals(Lists.newArrayList(expectedMultimap.keySet()));

      LinkedHashSet<Object> keysWithValuesOutOfOrder = Sets.newLinkedHashSet();
      for (Object key : expectedMultimap.keySet()) {
        List<?> actualVals = Lists.newArrayList(get(actual, key));
        List<?> expectedVals = Lists.newArrayList(get(expectedMultimap, key));
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
          failWithoutActual(
              simpleFact(
                  lenientFormat(
                      "Not true that <%s> %s <%s> in order. The keys are not in order, "
                          + "and the values for keys <%s> are not in order either",
                      actualCustomStringRepresentationForPackageMembersToCall(),
                      verb,
                      expectedMultimap,
                      keysWithValuesOutOfOrder)));
        } else {
          failWithoutActual(
              simpleFact(
                  lenientFormat(
                      "Not true that <%s> %s <%s> in order. The keys are not in order",
                      actualCustomStringRepresentationForPackageMembersToCall(),
                      verb,
                      expectedMultimap)));
        }
      } else if (!keysWithValuesOutOfOrder.isEmpty()) {
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that <%s> %s <%s> in order. "
                        + "The values for keys <%s> are not in order",
                    actualCustomStringRepresentationForPackageMembersToCall(),
                    verb,
                    expectedMultimap,
                    keysWithValuesOutOfOrder)));
      }
    }
  }

  /**
   * Advances the iterator until it either returns value, or has no more elements.
   *
   * <p>Returns true if the value was found, false if the end was reached before finding it.
   *
   * <p>This is basically the same as {@link Iterables#contains}, but where the contract explicitly
   * states that the iterator isn't advanced beyond the value if the value is found.
   */
  private static boolean advanceToFind(Iterator<?> iterator, Object value) {
    while (iterator.hasNext()) {
      if (Objects.equal(iterator.next(), value)) {
        return true;
      }
    }
    return false;
  }

  private static <K, V> Collection<V> get(Multimap<K, V> multimap, @NullableDecl Object key) {
    if (multimap.containsKey(key)) {
      return multimap.asMap().get(key);
    } else {
      return Collections.emptyList();
    }
  }

  private static ListMultimap<?, ?> difference(Multimap<?, ?> minuend, Multimap<?, ?> subtrahend) {
    ListMultimap<Object, Object> difference = LinkedListMultimap.create();
    for (Object key : minuend.keySet()) {
      List<?> valDifference =
          difference(
              Lists.newArrayList(get(minuend, key)), Lists.newArrayList(get(subtrahend, key)));
      difference.putAll(key, valDifference);
    }
    return difference;
  }

  private static List<?> difference(List<?> minuend, List<?> subtrahend) {
    LinkedHashMultiset<Object> remaining = LinkedHashMultiset.<Object>create(subtrahend);
    List<Object> difference = Lists.newArrayList();
    for (Object elem : minuend) {
      if (!remaining.remove(elem)) {
        difference.add(elem);
      }
    }
    return difference;
  }

  private static <K, V> String countDuplicatesMultimap(Multimap<K, V> multimap) {
    List<String> entries = new ArrayList<>();
    for (K key : multimap.keySet()) {
      entries.add(key + "=" + SubjectUtils.countDuplicates(multimap.get(key)));
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
      ListMultimap<Object, Object> annotatedMultimap = LinkedListMultimap.create();
      for (Entry<?, ?> entry : multimap.entries()) {
        Object key = "".equals(entry.getKey()) ? HUMAN_UNDERSTANDABLE_EMPTY_STRING : entry.getKey();
        Object value =
            "".equals(entry.getValue()) ? HUMAN_UNDERSTANDABLE_EMPTY_STRING : entry.getValue();
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
  public <A, E> UsingCorrespondence<A, E> comparingValuesUsing(
      Correspondence<? super A, ? super E> correspondence) {
    return new UsingCorrespondence<>(correspondence);
  }

  /**
   * A partially specified check in which the actual values (i.e. the values of the {@link Multimap}
   * under test) are compared to expected values using a {@link Correspondence}. The expected values
   * are of type {@code E}. Call methods on this object to actually execute the check.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   */
  public final class UsingCorrespondence<A, E> {

    private final Correspondence<? super A, ? super E> correspondence;

    private UsingCorrespondence(Correspondence<? super A, ? super E> correspondence) {
      this.correspondence = checkNotNull(correspondence);
    }

    /**
     * Fails if the multimap does not contain an entry with the given key and a value that
     * corresponds to the given value.
     */
    public void containsEntry(@NullableDecl Object expectedKey, @NullableDecl E expectedValue) {
      if (actual.containsKey(expectedKey)) {
        // Found matching key.
        Collection<A> actualValues = getCastActual().asMap().get(expectedKey);
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (A actualValue : actualValues) {
          if (correspondence.safeCompare(actualValue, expectedValue, exceptions)) {
            // Found matching key and value, but we still need to fail if we hit an exception along
            // the way.
            if (exceptions.hasCompareException()) {
              failWithActual(
                  exceptions
                      .describeAsMainCause()
                      .and(
                          simpleFact(
                              "comparing contents by testing that at least one entry had a key "
                                  + "equal to the expected key and a value that "
                                  + correspondence
                                  + " the expected value"),
                          fact("expected key", expectedKey),
                          fact("expected value", expectedValue)));
            }
            return;
          }
        }
        // Found matching key with non-matching values.
        failWithoutActual(
            facts(
                    simpleFact(
                        lenientFormat(
                            "Not true that <%s> contains at least one entry with key <%s> and a "
                                + "value that %s <%s>. However, it has a mapping from that key to "
                                + "<%s>",
                            actualCustomStringRepresentationForPackageMembersToCall(),
                            expectedKey,
                            correspondence,
                            expectedValue,
                            actualValues)))
                .and(exceptions.describeAsAdditionalInfo()));
      } else {
        // Did not find matching key.
        Set<Object> keys = new LinkedHashSet<>();
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (Entry<?, A> actualEntry : getCastActual().entries()) {
          if (correspondence.safeCompare(actualEntry.getValue(), expectedValue, exceptions)) {
            keys.add(actualEntry.getKey());
          }
        }
        if (!keys.isEmpty()) {
          // Found matching values with non-matching keys.
          failWithoutActual(
              facts(
                      simpleFact(
                          lenientFormat(
                              "Not true that <%s> contains at least one entry with key <%s> and a "
                                  + "value that %s <%s>. However, the following keys are mapped to "
                                  + "such values: <%s>",
                              actualCustomStringRepresentationForPackageMembersToCall(),
                              expectedKey,
                              correspondence,
                              expectedValue,
                              keys)))
                  .and(exceptions.describeAsAdditionalInfo()));
        } else {
          // Did not find matching key or value.
          failWithoutActual(
              facts(
                      simpleFact(
                          lenientFormat(
                              "Not true that <%s> contains at least one entry with key <%s> and a "
                                  + "value that %s <%s>",
                              actualCustomStringRepresentationForPackageMembersToCall(),
                              expectedKey,
                              correspondence,
                              expectedValue)))
                  .and(exceptions.describeAsAdditionalInfo()));
        }
      }
    }

    /**
     * Fails if the multimap contains an entry with the given key and a value that corresponds to
     * the given value.
     */
    public void doesNotContainEntry(
        @NullableDecl Object excludedKey, @NullableDecl E excludedValue) {
      if (actual.containsKey(excludedKey)) {
        Collection<A> actualValues = getCastActual().asMap().get(excludedKey);
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
              facts(
                      simpleFact(
                          lenientFormat(
                              "Not true that <%s> did not contain an entry with key <%s> and a"
                                  + " value that %s <%s>. It maps that key to the following such"
                                  + " values: <%s>",
                              actualCustomStringRepresentationForPackageMembersToCall(),
                              excludedKey,
                              correspondence,
                              excludedValue,
                              matchingValues)))
                  .and(exceptions.describeAsAdditionalInfo()));
        } else {
          // No value matched, but we still need to fail if we hit an exception along the way.
          if (exceptions.hasCompareException()) {
            failWithActual(
                exceptions
                    .describeAsMainCause()
                    .and(
                        simpleFact(
                            "comparing contents by testing that no entry had the forbidden key and "
                                + "a value that "
                                + correspondence
                                + " the forbidden value"),
                        fact("forbidden key", excludedKey),
                        fact("forbidden value", excludedValue)));
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
    public <K, V extends E> Ordered containsExactlyEntriesIn(Multimap<K, V> expectedMultimap) {
      // Note: The non-fuzzy MultimapSubject.containsExactlyEntriesIn has a custom implementation
      // and produces somewhat better failure messages simply asserting about the iterables of
      // entries would: it formats the expected values as  k=[v1, v2] rather than k=v1, k=v2; and in
      // the case where inOrder() fails it says the keys and/or the values for some keys are out of
      // order. We don't bother with that here. It would be nice, but it would be a lot of added
      // complexity for little gain.
      return check()
          .about(iterableEntries())
          .that(actual.entries())
          .comparingElementsUsing(new EntryCorrespondence<K, A, V>(correspondence))
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
    public <K, V extends E> Ordered containsAtLeastEntriesIn(Multimap<K, V> expectedMultimap) {
      // Note: The non-fuzzy MultimapSubject.containsAtLeastEntriesIn has a custom implementation
      // and produces somewhat better failure messages simply asserting about the iterables of
      // entries would: it formats the expected values as  k=[v1, v2] rather than k=v1, k=v2; and in
      // the case where inOrder() fails it says the keys and/or the values for some keys are out of
      // order. We don't bother with that here. It would be nice, but it would be a lot of added
      // complexity for little gain.
      return check()
          .about(iterableEntries())
          .that(actual.entries())
          .comparingElementsUsing(new EntryCorrespondence<K, A, V>(correspondence))
          .containsAllIn(expectedMultimap.entries());
    }

    /**
     * Fails if the multimap does not contain exactly the given set of key/value pairs.
     *
     * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
     * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
     */
    @CanIgnoreReturnValue
    public <K, V extends E> Ordered containsExactly(
        @NullableDecl Object k0, @NullableDecl Object v0, Object... rest) {
      @SuppressWarnings("unchecked")
      Multimap<K, V> expectedMultimap = (Multimap<K, V>) accumulateMultimap(k0, v0, rest);
      return containsExactlyEntriesIn(expectedMultimap);
    }

    /**
     * Fails if the multimap does not contain at least the given key/value pairs.
     *
     * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
     * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
     */
    @CanIgnoreReturnValue
    public <K, V extends E> Ordered containsAtLeast(
        @NullableDecl Object k0, @NullableDecl Object v0, Object... rest) {
      @SuppressWarnings("unchecked")
      Multimap<K, V> expectedMultimap = (Multimap<K, V>) accumulateMultimap(k0, v0, rest);
      return containsAtLeastEntriesIn(expectedMultimap);
    }

    /** Fails if the multimap is not empty. */
    @CanIgnoreReturnValue
    public <K, V extends E> Ordered containsExactly() {
      return MultimapSubject.this.containsExactly();
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Multimap<?, A> getCastActual() {
      return (Multimap<?, A>) actual;
    }
  }

  private static final class EntryCorrespondence<K, A, E>
      extends Correspondence<Entry<K, A>, Entry<K, E>> {

    private final Correspondence<? super A, ? super E> valueCorrespondence;

    EntryCorrespondence(Correspondence<? super A, ? super E> valueCorrespondence) {
      this.valueCorrespondence = valueCorrespondence;
    }

    @Override
    public boolean compare(Entry<K, A> actual, Entry<K, E> expected) {
      return actual.getKey().equals(expected.getKey())
          && valueCorrespondence.compare(actual.getValue(), expected.getValue());
    }

    @Override
    public String toString() {
      return lenientFormat(
          "has a key that is equal to and a value that %s the key and value of",
          valueCorrespondence);
    }
  }
}
