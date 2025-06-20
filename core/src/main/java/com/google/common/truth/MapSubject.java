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
import static com.google.common.collect.Maps.immutableEntry;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Platform.lenientFormatForFailure;
import static com.google.common.truth.Platform.stringValueForFailure;
import static com.google.common.truth.SubjectUtils.countDuplicatesAndAddTypeInfo;
import static com.google.common.truth.SubjectUtils.hasMatchingToStringPair;
import static com.google.common.truth.SubjectUtils.objectToTypeName;
import static com.google.common.truth.SubjectUtils.retainMatchingToString;
import static java.util.Collections.singletonList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.truth.Correspondence.DiffFormatter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Map} values.
 *
 * @author Christian Gruber
 * @author Kurt Alfred Kluever
 */
public class MapSubject extends Subject {
  private final @Nullable Map<?, ?> actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected MapSubject(FailureMetadata metadata, @Nullable Map<?, ?> actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  @Override
  public final void isEqualTo(@Nullable Object expected) {
    if (Objects.equals(actual, expected)) {
      return;
    }

    // Fail but with a more descriptive message:

    if (actual == null || !(expected instanceof Map)) {
      super.isEqualTo(expected);
      return;
    }

    containsEntriesInAnyOrder(actual, (Map<?, ?>) expected, /* allowUnexpected= */ false);
  }

  /** Checks that the actual map is empty. */
  public final void isEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected an empty map"));
    } else if (!actual.isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Checks that the actual map is not empty. */
  public final void isNotEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected a nonempty map"));
    } else if (actual.isEmpty()) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Checks that the actual map has the given size. */
  public final void hasSize(int size) {
    if (actual == null) {
      failWithActual("expected a map with size", size);
    } else if (size < 0) {
      failWithoutActual(
          simpleFact("expected a map with a negative size, but that is impossible"),
          fact("expected size", size),
          fact("actual size", actual.size()),
          actualContents());
    } else {
      check("size()").that(actual.size()).isEqualTo(size);
    }
  }

  /** Checks that the actual map contains the given key. */
  public final void containsKey(@Nullable Object key) {
    if (actual == null) {
      failWithActual("expected a map that contains key", key);
      return;
    }
    check("keySet()").that(actual.keySet()).contains(key);
  }

  /** Checks that the actual map does not contain the given key. */
  public final void doesNotContainKey(@Nullable Object key) {
    if (actual == null) {
      failWithActual("expected a map that does not contain key", key);
      return;
    }
    check("keySet()").that(actual.keySet()).doesNotContain(key);
  }

  /** Checks that the actual map contains the given entry. */
  public final void containsEntry(@Nullable Object key, @Nullable Object value) {
    Entry<?, ?> entry = immutableEntry(key, value);
    if (actual == null) {
      failWithActual("expected a map that contains entry", entry);
      return;
    }
    if (!actual.entrySet().contains(entry)) {
      List<?> keyList = singletonList(key);
      List<?> valueList = singletonList(value);
      if (actual.containsKey(key)) {
        Object actualValue = actual.get(key);
        if (Objects.equals(actualValue, value)) {
          /*
           * `contains(entry(key, value))` returned `false`, but `get(key)` returned a result equal
           * to `value`. We're probably looking at an `IdentityHashMap`, which compares values (not
           * just keys!) using `==`.
           *
           * `IdentityHashMap` isn't following the contract for `Map`, so we're within our rights to
           * do whatever we want. But it's probably simplest for us and best for users if we just
           * make the assertion pass: While users probably *do* want us to follow the
           * `IdentityHashMap` behavior of comparing *keys* with `==`, they probably *don't* want us
           * to follow the same behavior for *values*.
           */
          return;
        }
        /*
         * In the case of a null expected or actual map, clarify that the key *is* present and
         * *is* expected to be present. That is, get() isn't returning null to indicate that the key
         * is missing, and the user isn't making an assertion that the key is missing.
         */
        StandardSubjectBuilder check = check("get(%s)", key);
        if (value == null || actualValue == null) {
          check = check.withMessage("key is present but with a different value");
        }
        // See the comment on IterableSubject's use of failEqualityCheckForEqualsWithoutDescription.
        check.that(actualValue).failEqualityCheckForEqualsWithoutDescription(value);
      } else if (hasMatchingToStringPair(actual.keySet(), keyList)) {
        failWithoutActual(
            fact("expected to contain entry", entry),
            fact("an instance of", objectToTypeName(entry)),
            simpleFact("but did not"),
            fact(
                "though it did contain keys",
                countDuplicatesAndAddTypeInfo(
                    retainMatchingToString(actual.keySet(), /* itemsToCheck= */ keyList))),
            fullContents());
      } else if (actual.containsValue(value)) {
        Set<@Nullable Object> keys = new LinkedHashSet<>();
        for (Map.Entry<?, ?> actualEntry : actual.entrySet()) {
          if (Objects.equals(actualEntry.getValue(), value)) {
            keys.add(actualEntry.getKey());
          }
        }
        failWithoutActual(
            fact("expected to contain entry", entry),
            simpleFact("but did not"),
            fact("though it did contain keys with that value", keys),
            fullContents());
      } else if (hasMatchingToStringPair(actual.values(), valueList)) {
        failWithoutActual(
            fact("expected to contain entry", entry),
            fact("an instance of", objectToTypeName(entry)),
            simpleFact("but did not"),
            fact(
                "though it did contain values",
                countDuplicatesAndAddTypeInfo(
                    retainMatchingToString(actual.values(), /* itemsToCheck= */ valueList))),
            fullContents());
      } else {
        failWithActual("expected to contain entry", entry);
      }
    }
  }

  /** Checks that the actual map does not contain the given entry. */
  public final void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    Entry<?, ?> entry = immutableEntry(key, value);
    if (actual == null) {
      failWithActual("expected a map that does not contain entry", entry);
      return;
    }
    checkNoNeedToDisplayBothValues("entrySet()").that(actual.entrySet()).doesNotContain(entry);
  }

  /** Checks that the actual map is empty. */
  @CanIgnoreReturnValue
  public final Ordered containsExactly() {
    isEmpty();
    // If the actual value was empty, then it is vacuously in order, so we'd return IN_ORDER.
    // If the actual value not empty (or was null), then we'd return ALREADY_FAILED.
    // Luckily, the two are equivalent, so it doesn't matter which we pick.
    return IN_ORDER;
  }

  /**
   * Checks that the actual map contains exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   *
   * <p>The arguments must not contain duplicate keys.
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactly(
      @Nullable Object k0, @Nullable Object v0, @Nullable Object... rest) {
    return containsExactlyEntriesIn(accumulateMap("containsExactly", k0, v0, rest));
  }

  @CanIgnoreReturnValue
  public final Ordered containsAtLeast(
      @Nullable Object k0, @Nullable Object v0, @Nullable Object... rest) {
    return containsAtLeastEntriesIn(accumulateMap("containsAtLeast", k0, v0, rest));
  }

  private static Map<@Nullable Object, @Nullable Object> accumulateMap(
      String functionName, @Nullable Object k0, @Nullable Object v0, @Nullable Object... rest) {
    checkArgument(
        rest.length % 2 == 0,
        "There must be an equal number of key/value pairs "
            + "(i.e., the number of key/value parameters (%s) must be even).",
        rest.length + 2);

    Map<@Nullable Object, @Nullable Object> expectedMap = new LinkedHashMap<>();
    expectedMap.put(k0, v0);
    Multiset<@Nullable Object> keys = LinkedHashMultiset.create();
    keys.add(k0);
    for (int i = 0; i < rest.length; i += 2) {
      Object key = rest[i];
      expectedMap.put(key, rest[i + 1]);
      keys.add(key);
    }
    checkArgument(
        keys.size() == expectedMap.size(),
        "Duplicate keys (%s) cannot be passed to %s().",
        keys,
        functionName);
    return expectedMap;
  }

  /** Checks that the actual map contains exactly the given set of entries in the given map. */
  @CanIgnoreReturnValue
  public final Ordered containsExactlyEntriesIn(@Nullable Map<?, ?> expected) {
    if (expected == null) {
      failWithoutActual(
          simpleFact("could not perform containment check because expected map was null"),
          actualContents());
      return ALREADY_FAILED;
    } else if (actual == null) {
      failWithActual("expected a map that contains exactly", expected);
      return ALREADY_FAILED;
    } else if (expected.isEmpty()) {
      return containsExactly();
    }
    return containsEntriesInAnyOrder(actual, expected, /* allowUnexpected= */ false)
        ? MapInOrder.create(
            this, actual, expected, /* allowUnexpected= */ false, /* correspondence= */ null)
        : ALREADY_FAILED;
  }

  /** Checks that the actual map contains at least the given set of entries in the given map. */
  @CanIgnoreReturnValue
  public final Ordered containsAtLeastEntriesIn(@Nullable Map<?, ?> expected) {
    if (expected == null) {
      failWithoutActual(
          simpleFact("could not perform containment check because expected map was null"),
          actualContents());
      return ALREADY_FAILED;
    } else if (actual == null) {
      failWithActual("expected a map that contains at least", expected);
      return ALREADY_FAILED;
    } else if (expected.isEmpty()) {
      return IN_ORDER;
    }
    return containsEntriesInAnyOrder(actual, expected, /* allowUnexpected= */ true)
        ? MapInOrder.create(
            this, actual, expected, /* allowUnexpected= */ true, /* correspondence= */ null)
        : ALREADY_FAILED;
  }

  @CanIgnoreReturnValue
  private boolean containsEntriesInAnyOrder(
      Map<?, ?> actual, Map<?, ?> expected, boolean allowUnexpected) {
    MapDifference<@Nullable Object, @Nullable Object, @Nullable Object> diff =
        MapDifference.create(actual, expected, allowUnexpected, Objects::equals);
    if (diff.isEmpty()) {
      return true;
    }
    // TODO(cpovirk): Consider adding a special-case where the diff contains exactly one key which
    // is present with the wrong value, doing an isEqualTo assertion on the values. Pro: This gives
    // us all the extra power of isEqualTo, including maybe throwing a ComparisonFailure. Con: It
    // might be misleading to report a single mismatched value when the assertion was on the whole
    // map - this could be mitigated by adding extra info explaining that. (Would need to ensure
    // that it still fails in cases where e.g. the value is 1 and it should be 1L, where isEqualTo
    // succeeds: perhaps failEqualityCheckForEqualsWithoutDescription will do the right thing.)
    // First, we need to decide whether this kind of cleverness is a line we want to cross.
    // (See also containsEntry, which does do an isEqualTo-like assertion when the expected key is
    // present with the wrong value, which may be the closest we currently get to this.)
    failWithoutActual(
        factsBuilder()
            .addAll(diff.describe(/* differ= */ null))
            .add(simpleFact("---"))
            .add(fact(allowUnexpected ? "expected to contain at least" : "expected", expected))
            .add(butWas())
            .build());
    return false;
  }

  private interface ValueTester<A extends @Nullable Object, E extends @Nullable Object> {
    boolean test(A actualValue, E expectedValue);
  }

  private interface Differ<A extends @Nullable Object, E extends @Nullable Object> {
    @Nullable String diff(A actual, E expected);
  }

  // This is mostly like the MapDifference code in com.google.common.collect, generalized to remove
  // the requirement that the values of the two maps are of the same type and are compared with a
  // symmetric Equivalence.
  @SuppressWarnings("ImmutableMemberCollection") // null elements (b/173628387#comment8)
  private static final class MapDifference<
      K extends @Nullable Object, A extends @Nullable Object, E extends @Nullable Object> {
    private final Map<K, E> missing;
    private final Map<K, A> unexpected;
    private final Map<K, ValueDifference<A, E>> wrongValues;
    private final Set<K> allKeys;

    static <K extends @Nullable Object, A extends @Nullable Object, E extends @Nullable Object>
        MapDifference<K, A, E> create(
            Map<? extends K, ? extends A> actual,
            Map<? extends K, ? extends E> expected,
            boolean allowUnexpected,
            ValueTester<? super A, ? super E> valueTester) {
      Map<K, A> unexpected = new LinkedHashMap<>(actual);
      Map<K, E> missing = new LinkedHashMap<>();
      Map<K, ValueDifference<A, E>> wrongValues = new LinkedHashMap<>();
      for (Map.Entry<? extends K, ? extends E> expectedEntry : expected.entrySet()) {
        K expectedKey = expectedEntry.getKey();
        E expectedValue = expectedEntry.getValue();
        if (actual.containsKey(expectedKey)) {
          @SuppressWarnings("UnnecessaryCast") // needed by nullness checker
          A actualValue = (A) unexpected.remove(expectedKey);
          if (!valueTester.test(actualValue, expectedValue)) {
            wrongValues.put(expectedKey, ValueDifference.create(actualValue, expectedValue));
          }
        } else {
          missing.put(expectedKey, expectedValue);
        }
      }
      if (allowUnexpected) {
        unexpected.clear();
      }
      return new MapDifference<>(
          missing, unexpected, wrongValues, Sets.union(actual.keySet(), expected.keySet()));
    }

    private MapDifference(
        Map<K, E> missing,
        Map<K, A> unexpected,
        Map<K, ValueDifference<A, E>> wrongValues,
        Set<K> allKeys) {
      this.missing = missing;
      this.unexpected = unexpected;
      this.wrongValues = wrongValues;
      this.allKeys = allKeys;
    }

    boolean isEmpty() {
      return missing.isEmpty() && unexpected.isEmpty() && wrongValues.isEmpty();
    }

    ImmutableList<Fact> describe(@Nullable Differ<? super A, ? super E> differ) {
      boolean includeKeyTypes = includeKeyTypes();
      ImmutableList.Builder<Fact> facts = ImmutableList.builder();
      if (!wrongValues.isEmpty()) {
        facts.add(simpleFact("keys with wrong values"));
      }
      for (Map.Entry<K, ValueDifference<A, E>> entry : wrongValues.entrySet()) {
        facts.add(fact("for key", maybeAddType(entry.getKey(), includeKeyTypes)));
        facts.addAll(entry.getValue().describe(differ));
      }
      if (!missing.isEmpty()) {
        facts.add(simpleFact("missing keys"));
      }
      for (Map.Entry<K, E> entry : missing.entrySet()) {
        facts.add(fact("for key", maybeAddType(entry.getKey(), includeKeyTypes)));
        facts.add(fact("expected value", entry.getValue()));
      }
      if (!unexpected.isEmpty()) {
        facts.add(simpleFact("unexpected keys"));
      }
      for (Map.Entry<K, A> entry : unexpected.entrySet()) {
        facts.add(fact("for key", maybeAddType(entry.getKey(), includeKeyTypes)));
        facts.add(fact("unexpected value", entry.getValue()));
      }
      return facts.build();
    }

    private boolean includeKeyTypes() {
      // We will annotate all the keys in the diff with their types if any of the keys involved have
      // the same toString() without being equal.
      Set<K> keys = new HashSet<>();
      keys.addAll(missing.keySet());
      keys.addAll(unexpected.keySet());
      keys.addAll(wrongValues.keySet());
      return hasMatchingToStringPair(keys, allKeys);
    }
  }

  private static final class ValueDifference<
      A extends @Nullable Object, E extends @Nullable Object> {
    private final A actual;
    private final E expected;

    private ValueDifference(A actual, E expected) {
      this.actual = actual;
      this.expected = expected;
    }

    ImmutableList<Fact> describe(@Nullable Differ<? super A, ? super E> differ) {
      boolean includeTypes =
          differ == null && String.valueOf(actual).equals(String.valueOf(expected));
      ImmutableList.Builder<Fact> facts =
          factsBuilder()
              .add(fact("expected value", maybeAddType(expected, includeTypes)))
              .add(fact("but got value", maybeAddType(actual, includeTypes)));

      if (differ != null) {
        String diffString = differ.diff(actual, expected);
        if (diffString != null) {
          facts.add(fact("diff", diffString));
        }
      }
      return facts.build();
    }

    static <A extends @Nullable Object, E extends @Nullable Object> ValueDifference<A, E> create(
        A actual, E expected) {
      return new ValueDifference<>(actual, expected);
    }
  }

  private static String maybeAddType(@Nullable Object o, boolean includeTypes) {
    return includeTypes
        ? lenientFormatForFailure("%s (%s)", o, objectToTypeName(o))
        : stringValueForFailure(o);
  }

  private static final class MapInOrder implements Ordered {
    private final MapSubject subject;
    private final Map<?, ?> actual;
    private final Map<?, ?> expectedMap;
    private final boolean allowUnexpected;
    private final @Nullable Correspondence<?, ?> correspondence;

    private MapInOrder(
        MapSubject subject,
        Map<?, ?> actual,
        Map<?, ?> expectedMap,
        boolean allowUnexpected,
        @Nullable Correspondence<?, ?> correspondence) {
      this.subject = subject;
      this.actual = actual;
      this.expectedMap = expectedMap;
      this.allowUnexpected = allowUnexpected;
      this.correspondence = correspondence;
    }

    /**
     * Checks whether the common elements between actual and expected are in the same order.
     *
     * <p>This doesn't check whether the keys have the same values or whether all the required keys
     * are actually present. That was supposed to be done before the "in order" part.
     */
    @Override
    public void inOrder() {
      // We're using the fact that Sets.intersection keeps the order of the first set.
      List<?> expectedKeyOrder =
          new ArrayList<>(Sets.intersection(expectedMap.keySet(), actual.keySet()));
      List<?> actualKeyOrder =
          new ArrayList<>(Sets.intersection(actual.keySet(), expectedMap.keySet()));
      if (!actualKeyOrder.equals(expectedKeyOrder)) {
        ImmutableList.Builder<Fact> facts =
            factsBuilder()
                .add(
                    simpleFact(
                        allowUnexpected
                            ? "required entries were all found, but order was wrong"
                            : "entries match, but order was wrong"))
                .add(
                    fact(
                        allowUnexpected ? "expected to contain at least" : "expected",
                        expectedMap));
        if (correspondence != null) {
          facts.addAll(correspondence.describeForMapValues());
        }
        failWithActual(facts.build());
      }
    }

    private void failWithActual(Iterable<Fact> facts) {
      subject.failWithActual(facts);
    }

    static MapInOrder create(
        MapSubject subject,
        Map<?, ?> actual,
        Map<?, ?> expectedMap,
        boolean allowUnexpected,
        @Nullable Correspondence<?, ?> correspondence) {
      return new MapInOrder(subject, actual, expectedMap, allowUnexpected, correspondence);
    }
  }

  /** Ordered implementation that does nothing because it's already known to be true. */
  private static final Ordered IN_ORDER = () -> {};

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED = () -> {};

  /**
   * Starts a method chain for a check in which the actual values (i.e. the values of the {@link
   * Map} under test) are compared to expected values using the given {@link Correspondence}. The
   * actual values must be of type {@code A}, the expected values must be of type {@code E}. The
   * check is actually executed by continuing the method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualMap)
   *     .comparingValuesUsing(correspondence)
   *     .containsEntry(expectedKey, expectedValue);
   * }</pre>
   *
   * where {@code actualMap} is a {@code Map<?, A>} (or, more generally, a {@code Map<?, ? extends
   * A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and {@code expectedValue} is an
   * {@code E}.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual map that is not of type {@code A} or an expected value that is not of type
   * {@code E}.
   */
  public final <A extends @Nullable Object, E extends @Nullable Object>
      UsingCorrespondence<A, E> comparingValuesUsing(
          Correspondence<? super A, ? super E> correspondence) {
    return UsingCorrespondence.create(this, correspondence);
  }

  /**
   * Starts a method chain for a check in which failure messages may use the given {@link
   * DiffFormatter} to describe the difference between an actual map (i.e. a value in the {@link
   * Map} under test) and the value it is expected to be equal to, but isn't. The actual and
   * expected values must be of type {@code V}. The check is actually executed by continuing the
   * method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualMap)
   *     .formattingDiffsUsing(FooTestHelper::formatDiff)
   *     .containsExactly(key1, foo1, key2, foo2, key3, foo3);
   * }</pre>
   *
   * where {@code actualMap} is a {@code Map<?, Foo>} (or, more generally, a {@code Map<?, ? extends
   * Foo>}), {@code FooTestHelper.formatDiff} is a static method taking two {@code Foo} arguments
   * and returning a {@link String}, and {@code foo1}, {@code foo2}, and {@code foo3} are {@code
   * Foo} instances.
   *
   * <p>Unlike when using {@link #comparingValuesUsing}, the values are still compared using object
   * equality, so this method does not affect whether a test passes or fails.
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter a value that is not of type {@code V}.
   *
   * @since 1.1
   */
  public final <V extends @Nullable Object> UsingCorrespondence<V, V> formattingDiffsUsing(
      DiffFormatter<? super V, ? super V> formatter) {
    return comparingValuesUsing(Correspondence.<V>equality().formattingDiffsUsing(formatter));
  }

  /**
   * A partially specified check in which the actual values (i.e. the values of the {@link Map}
   * under test) are compared to expected values using a {@link Correspondence}. The expected values
   * are of type {@code E}. Call methods on this object to actually execute the check.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   */
  public static final class UsingCorrespondence<
      A extends @Nullable Object, E extends @Nullable Object> {
    private final MapSubject subject;
    private final Correspondence<? super A, ? super E> correspondence;
    private final @Nullable Map<?, ?> actual;

    private UsingCorrespondence(
        MapSubject subject, Correspondence<? super A, ? super E> correspondence) {
      this.subject = subject;
      this.correspondence = checkNotNull(correspondence);
      this.actual = subject.actual;
    }

    /**
     * Checks that the actual map contains an entry with the given key and a value that corresponds
     * to the given value.
     */
    @SuppressWarnings("UnnecessaryCast") // needed by nullness checker
    public void containsEntry(@Nullable Object key, E value) {
      if (actual == null) {
        failWithActual("expected a map that contains entry", immutableEntry(key, value));
        return;
      }
      if (actual.containsKey(key)) {
        // Found matching key.
        A actualValue = castActual(actual).get(key);
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        if (correspondence.safeCompare((A) actualValue, value, exceptions)) {
          // The expected key had the expected value. There's no need to check exceptions here,
          // because if Correspondence.compare() threw then safeCompare() would return false.
          return;
        }
        // Found matching key with non-matching value.
        String diff = correspondence.safeFormatDiff((A) actualValue, value, exceptions);
        if (diff != null) {
          failWithoutActual(
              factsBuilder()
                  .add(fact("for key", key))
                  .add(fact("expected value", value))
                  .addAll(correspondence.describeForMapValues())
                  .add(fact("but got value", actualValue))
                  .add(fact("diff", diff))
                  .add(fact("full map", actualCustomStringRepresentationForPackageMembersToCall()))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        } else {
          failWithoutActual(
              factsBuilder()
                  .add(fact("for key", key))
                  .add(fact("expected value", value))
                  .addAll(correspondence.describeForMapValues())
                  .add(fact("but got value", actualValue))
                  .add(fact("full map", actualCustomStringRepresentationForPackageMembersToCall()))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        }
      } else {
        // Did not find matching key. Look for the matching value with a different key.
        Set<@Nullable Object> keys = new LinkedHashSet<>();
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (Map.Entry<?, A> actualEntry : castActual(actual).entrySet()) {
          if (correspondence.safeCompare(actualEntry.getValue(), value, exceptions)) {
            keys.add(actualEntry.getKey());
          }
        }
        if (!keys.isEmpty()) {
          // Found matching values with non-matching keys.
          failWithoutActual(
              factsBuilder()
                  .add(fact("for key", key))
                  .add(fact("expected value", value))
                  .addAll(correspondence.describeForMapValues())
                  .add(simpleFact("but was missing"))
                  .add(fact("other keys with matching values", keys))
                  .add(fact("full map", actualCustomStringRepresentationForPackageMembersToCall()))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        } else {
          // Did not find matching key or value.
          failWithoutActual(
              factsBuilder()
                  .add(fact("for key", key))
                  .add(fact("expected value", value))
                  .addAll(correspondence.describeForMapValues())
                  .add(simpleFact("but was missing"))
                  .add(fact("full map", actualCustomStringRepresentationForPackageMembersToCall()))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        }
      }
    }

    /**
     * Checks that the actual map does not contain an entry with the given key and a value that
     * corresponds to the given value.
     */
    @SuppressWarnings("UnnecessaryCast") // needed by nullness checker
    public void doesNotContainEntry(@Nullable Object key, E value) {
      if (actual == null) {
        failWithActual("expected a map that does not contain entry", immutableEntry(key, value));
        return;
      }
      if (actual.containsKey(key)) {
        // Found matching key. Fail if the value matches, too.
        A actualValue = castActual(actual).get(key);
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        if (correspondence.safeCompare((A) actualValue, value, exceptions)) {
          // The matching key had a matching value. There's no need to check exceptions here,
          // because if Correspondence.compare() threw then safeCompare() would return false.
          failWithoutActual(
              factsBuilder()
                  .add(fact("expected not to contain", immutableEntry(key, value)))
                  .addAll(correspondence.describeForMapValues())
                  .add(
                      fact(
                          "but contained",
                          Maps.<@Nullable Object, @Nullable A>immutableEntry(key, actualValue)))
                  .add(fact("full map", actualCustomStringRepresentationForPackageMembersToCall()))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        }
        // The value didn't match, but we still need to fail if we hit an exception along the way.
        if (exceptions.hasCompareException()) {
          failWithoutActual(
              factsBuilder()
                  .addAll(exceptions.describeAsMainCause())
                  .add(fact("expected not to contain", immutableEntry(key, value)))
                  .addAll(correspondence.describeForMapValues())
                  .add(simpleFact("found no match (but failing because of exception)"))
                  .add(fact("full map", actualCustomStringRepresentationForPackageMembersToCall()))
                  .build());
        }
      }
    }

    /**
     * Checks that the actual map contains exactly the given set of keys mapping to values that
     * correspond to the given values.
     *
     * <p>The values must all be of type {@code E}, and a {@link ClassCastException} will be thrown
     * if any other type is encountered.
     *
     * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
     * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
     */
    // TODO(b/25744307): Can we add an error-prone check that rest.length % 2 == 0?
    // For bonus points, checking that the even-numbered values are of type E would be sweet.
    @CanIgnoreReturnValue
    public Ordered containsExactly(@Nullable Object k0, @Nullable E v0, @Nullable Object... rest) {
      @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
      Map<Object, E> expectedMap = (Map<Object, E>) accumulateMap("containsExactly", k0, v0, rest);
      return containsExactlyEntriesIn(expectedMap);
    }

    /**
     * Checks that the actual map contains at least the given set of keys mapping to values that
     * correspond to the given values.
     *
     * <p>The values must all be of type {@code E}, and a {@link ClassCastException} will be thrown
     * if any other type is encountered.
     *
     * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
     * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
     */
    // TODO(b/25744307): Can we add an error-prone check that rest.length % 2 == 0?
    // For bonus points, checking that the even-numbered values are of type E would be sweet.
    @CanIgnoreReturnValue
    public Ordered containsAtLeast(@Nullable Object k0, @Nullable E v0, @Nullable Object... rest) {
      @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
      Map<Object, E> expectedMap = (Map<Object, E>) accumulateMap("containsAtLeast", k0, v0, rest);
      return containsAtLeastEntriesIn(expectedMap);
    }

    /**
     * Checks that the actual map contains exactly the keys in the given map, mapping to values that
     * correspond to the values of the given map.
     */
    @CanIgnoreReturnValue
    public Ordered containsExactlyEntriesIn(@Nullable Map<?, ? extends E> expected) {
      if (expected == null) {
        failWithoutActual(
            simpleFact("could not perform containment check because expected map was null"),
            actualContents());
        return ALREADY_FAILED;
      } else if (expected.isEmpty()) {
        return subject.containsExactly();
      } else if (actual == null) {
        failWithActual("expected a map that contains exactly", expected);
        return ALREADY_FAILED;
      }
      return internalContainsEntriesIn(actual, expected, /* allowUnexpected= */ false);
    }

    /**
     * Checks that the actual map contains at least the keys in the given map, mapping to values
     * that correspond to the values of the given map.
     */
    @CanIgnoreReturnValue
    public Ordered containsAtLeastEntriesIn(@Nullable Map<?, ? extends E> expected) {
      if (expected == null) {
        failWithoutActual(
            simpleFact("could not perform containment check because expected map was null"),
            actualContents());
        return ALREADY_FAILED;
      } else if (expected.isEmpty()) {
        return IN_ORDER;
      } else if (actual == null) {
        failWithActual("expected a map that contains at least", expected);
        return ALREADY_FAILED;
      }
      return internalContainsEntriesIn(actual, expected, /* allowUnexpected= */ true);
    }

    private <K extends @Nullable Object, V extends E> Ordered internalContainsEntriesIn(
        Map<?, ?> actual, Map<K, V> expected, boolean allowUnexpected) {
      Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
      MapDifference<@Nullable Object, A, V> diff =
          MapDifference.create(
              castActual(actual),
              expected,
              allowUnexpected,
              (actualValue, expectedValue) ->
                  correspondence.safeCompare(actualValue, expectedValue, exceptions));
      if (diff.isEmpty()) {
        // The maps correspond exactly. There's no need to check exceptions here, because if
        // Correspondence.compare() threw then safeCompare() would return false and the diff would
        // record that we had the wrong value for that key.
        return MapInOrder.create(subject, actual, expected, allowUnexpected, correspondence);
      }
      failWithoutActual(
          factsBuilder()
              .addAll(diff.describe(differ(exceptions)))
              .add(simpleFact("---"))
              .add(fact(allowUnexpected ? "expected to contain at least" : "expected", expected))
              .addAll(correspondence.describeForMapValues())
              .add(butWas())
              .addAll(exceptions.describeAsAdditionalInfo())
              .build());
      return ALREADY_FAILED;
    }

    private <V extends E> Differ<A, V> differ(Correspondence.ExceptionStore exceptions) {
      return (actual, expected) -> correspondence.safeFormatDiff(actual, expected, exceptions);
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Map<?, A> castActual(Map<?, ?> actual) {
      return (Map<?, A>) actual;
    }

    private String actualCustomStringRepresentationForPackageMembersToCall() {
      return subject.actualCustomStringRepresentationForPackageMembersToCall();
    }

    private Fact actualContents() {
      return subject.actualContents();
    }

    private Fact butWas() {
      return subject.butWas();
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

    static <A extends @Nullable Object, E extends @Nullable Object>
        UsingCorrespondence<A, E> create(
            MapSubject subject, Correspondence<? super A, ? super E> correspondence) {
      return new UsingCorrespondence<>(subject, correspondence);
    }
  }

  private Fact fullContents() {
    return actualValue("full contents");
  }

  private Fact actualContents() {
    return actualValue("actual contents");
  }

  static Factory<MapSubject, Map<?, ?>> maps() {
    return MapSubject::new;
  }
}
