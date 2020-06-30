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
import static com.google.common.collect.Maps.immutableEntry;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.SubjectUtils.countDuplicatesAndAddTypeInfo;
import static com.google.common.truth.SubjectUtils.hasMatchingToStringPair;
import static com.google.common.truth.SubjectUtils.objectToTypeName;
import static com.google.common.truth.SubjectUtils.retainMatchingToString;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Propositions for {@link Map} subjects.
 *
 * @author Christian Gruber
 * @author Kurt Alfred Kluever
 */
public class MapSubject extends Subject {
  private final Map<?, ?> actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected MapSubject(FailureMetadata metadata, @Nullable Map<?, ?> map) {
    super(metadata, map);
    this.actual = map;
  }

  @Override
  public final void isEqualTo(@Nullable Object other) {
    if (Objects.equal(actual, other)) {
      return;
    }

    // Fail but with a more descriptive message:

    if (actual == null || !(other instanceof Map)) {
      super.isEqualTo(other);
      return;
    }

    containsEntriesInAnyOrder((Map<?, ?>) other, "is equal to", false);
  }

  /** Fails if the map is not empty. */
  public final void isEmpty() {
    if (!actual.isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Fails if the map is empty. */
  public final void isNotEmpty() {
    if (actual.isEmpty()) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Fails if the map does not have the given size. */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize (%s) must be >= 0", expectedSize);
    check("size()").that(actual.size()).isEqualTo(expectedSize);
  }

  /** Fails if the map does not contain the given key. */
  public final void containsKey(@Nullable Object key) {
    check("keySet()").that(actual.keySet()).contains(key);
  }

  /** Fails if the map contains the given key. */
  public final void doesNotContainKey(@Nullable Object key) {
    check("keySet()").that(actual.keySet()).doesNotContain(key);
  }

  /** Fails if the map does not contain the given entry. */
  public final void containsEntry(@Nullable Object key, @Nullable Object value) {
    Map.Entry<Object, Object> entry = Maps.immutableEntry(key, value);
    if (!actual.entrySet().contains(entry)) {
      List<Object> keyList = Lists.newArrayList(key);
      List<Object> valueList = Lists.newArrayList(value);
      if (hasMatchingToStringPair(actual.keySet(), keyList)) {
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that <%s> contains entry <%s (%s)>. However, it does contain keys "
                        + "<%s>.",
                    actualCustomStringRepresentationForPackageMembersToCall(),
                    entry,
                    objectToTypeName(entry),
                    countDuplicatesAndAddTypeInfo(
                        retainMatchingToString(actual.keySet(), keyList /* itemsToCheck */)))));
      } else if (hasMatchingToStringPair(actual.values(), valueList)) {
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that <%s> contains entry <%s (%s)>. However, it does contain values "
                        + "<%s>.",
                    actualCustomStringRepresentationForPackageMembersToCall(),
                    entry,
                    objectToTypeName(entry),
                    countDuplicatesAndAddTypeInfo(
                        retainMatchingToString(actual.values(), valueList /* itemsToCheck */)))));
      } else if (actual.containsKey(key)) {
        Object actualValue = actual.get(key);
        /*
         * In the case of a null expected or actual value, clarify that the key *is* present and
         * *is* expected to be present. That is, get() isn't returning null to indicate that the key
         * is missing, and the user isn't making an assertion that the key is missing.
         */
        StandardSubjectBuilder check = check("get(%s)", key);
        if (value == null || actualValue == null) {
          check = check.withMessage("key is present but with a different value");
        }
        // See the comment on IterableSubject's use of failEqualityCheckForEqualsWithoutDescription.
        check.that(actualValue).failEqualityCheckForEqualsWithoutDescription(value);
      } else if (actual.containsValue(value)) {
        Set<Object> keys = new LinkedHashSet<>();
        for (Map.Entry<?, ?> actualEntry : actual.entrySet()) {
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
        failWithActual("expected to contain entry", entry);
      }
    }
  }

  /** Fails if the map contains the given entry. */
  public final void doesNotContainEntry(@Nullable Object key, @Nullable Object value) {
    checkNoNeedToDisplayBothValues("entrySet()")
        .that(actual.entrySet())
        .doesNotContain(immutableEntry(key, value));
  }

  /** Fails if the map is not empty. */
  @CanIgnoreReturnValue
  public final Ordered containsExactly() {
    return containsExactlyEntriesIn(ImmutableMap.of());
  }

  /**
   * Fails if the map does not contain exactly the given set of key/value pairs.
   *
   * <p><b>Warning:</b> the use of varargs means that we cannot guarantee an equal number of
   * key/value pairs at compile time. Please make sure you provide varargs in key/value pairs!
   *
   * <p>The arguments must not contain duplicate keys.
   */
  @CanIgnoreReturnValue
  public final Ordered containsExactly(
      @Nullable Object k0, @Nullable Object v0, Object... rest) {
    return containsExactlyEntriesIn(accumulateMap("containsExactly", k0, v0, rest));
  }

  @CanIgnoreReturnValue
  public final Ordered containsAtLeast(
      @Nullable Object k0, @Nullable Object v0, Object... rest) {
    return containsAtLeastEntriesIn(accumulateMap("containsAtLeast", k0, v0, rest));
  }

  private static Map<Object, Object> accumulateMap(
      String functionName, @Nullable Object k0, @Nullable Object v0, Object... rest) {
    checkArgument(
        rest.length % 2 == 0,
        "There must be an equal number of key/value pairs "
            + "(i.e., the number of key/value parameters (%s) must be even).",
        rest.length + 2);

    Map<Object, Object> expectedMap = Maps.newLinkedHashMap();
    expectedMap.put(k0, v0);
    Multiset<Object> keys = LinkedHashMultiset.create();
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

  /** Fails if the map does not contain exactly the given set of entries in the given map. */
  @CanIgnoreReturnValue
  public final Ordered containsExactlyEntriesIn(Map<?, ?> expectedMap) {
    if (expectedMap.isEmpty()) {
      if (actual.isEmpty()) {
        return IN_ORDER;
      } else {
        isEmpty(); // fails
        return ALREADY_FAILED;
      }
    }
    boolean containsAnyOrder =
        containsEntriesInAnyOrder(expectedMap, "contains exactly", /* allowUnexpected= */ false);
    if (containsAnyOrder) {
      return new MapInOrder(expectedMap, "contains exactly these entries in order");
    } else {
      return ALREADY_FAILED;
    }
  }

  /** Fails if the map does not contain at least the given set of entries in the given map. */
  @CanIgnoreReturnValue
  public final Ordered containsAtLeastEntriesIn(Map<?, ?> expectedMap) {
    if (expectedMap.isEmpty()) {
      return IN_ORDER;
    }
    boolean containsAnyOrder =
        containsEntriesInAnyOrder(expectedMap, "contains at least", /* allowUnexpected= */ true);
    if (containsAnyOrder) {
      return new MapInOrder(expectedMap, "contains at least these entries in order");
    } else {
      return ALREADY_FAILED;
    }
  }

  @CanIgnoreReturnValue
  private boolean containsEntriesInAnyOrder(
      Map<?, ?> expectedMap, String failVerb, boolean allowUnexpected) {
    MapDifference<Object, Object, Object> diff =
        MapDifference.create(actual, expectedMap, allowUnexpected, EQUALITY);
    if (diff.isEmpty()) {
      return true;
    }
    failWithoutActual(
        simpleFact(
            lenientFormat(
                "Not true that <%s> %s <%s>. It %s",
                actualCustomStringRepresentationForPackageMembersToCall(),
                failVerb,
                expectedMap,
                diff.describe(VALUE_DIFFERENCE_FORMAT))));
    return false;
  }

  private interface ValueTester<A, E> {
    boolean test(@Nullable A actualValue, @Nullable E expectedValue);
  }

  @SuppressWarnings("UnnecessaryAnonymousClass") // for Java 7 compatibility
  private static final ValueTester<Object, Object> EQUALITY =
      new ValueTester<Object, Object>() {
        @Override
        public boolean test(@Nullable Object actualValue, @Nullable Object expectedValue) {
          return Objects.equal(actualValue, expectedValue);
        }
      };

  // This is mostly like the MapDifference code in com.google.common.collect, generalized to remove
  // the requirement that the values of the two maps are of the same type and are compared with a
  // symmetric Equivalence.
  private static class MapDifference<K, A, E> {
    private final Map<K, E> missing;
    private final Map<K, A> unexpected;
    private final Map<K, ValueDifference<A, E>> wrongValues;
    private final Set<K> allKeys;

    static <K, A, E> MapDifference<K, A, E> create(
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
          A actualValue = unexpected.remove(expectedKey);
          if (!valueTester.test(actualValue, expectedValue)) {
            wrongValues.put(expectedKey, new ValueDifference<>(actualValue, expectedValue));
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

    String describe(Function<ValueDifference<A, E>, String> valueDiffFormat) {
      boolean includeKeyTypes = includeKeyTypes();
      StringBuilder description = new StringBuilder();
      if (!missing.isEmpty()) {
        description
            .append("is missing keys for the following entries: ")
            .append(includeKeyTypes ? addKeyTypes(missing) : missing);
      }
      if (!unexpected.isEmpty()) {
        if (description.length() > 0) {
          description.append(" and ");
        }
        description
            .append("has the following entries with unexpected keys: ")
            .append(includeKeyTypes ? addKeyTypes(unexpected) : unexpected);
      }
      if (!wrongValues.isEmpty()) {
        if (description.length() > 0) {
          description.append(" and ");
        }
        Map<K, String> wrongValuesFormatted = Maps.transformValues(wrongValues, valueDiffFormat);
        description
            .append("has the following entries with matching keys but different values: ")
            .append(includeKeyTypes ? addKeyTypes(wrongValuesFormatted) : wrongValuesFormatted);
      }
      return description.toString();
    }

    private boolean includeKeyTypes() {
      // We will annotate all the keys in the diff with their types if any of the keys involved have
      // the same toString() without being equal.
      Set<K> keys = Sets.newHashSet();
      keys.addAll(missing.keySet());
      keys.addAll(unexpected.keySet());
      keys.addAll(wrongValues.keySet());
      return hasMatchingToStringPair(keys, allKeys);
    }
  }

  private static class ValueDifference<A, E> {
    private final A actual;
    private final E expected;

    ValueDifference(@Nullable A actual, @Nullable E expected) {
      this.actual = actual;
      this.expected = expected;
    }
  }

  /** A formatting function for value differences when compared for equality. */
  @SuppressWarnings("UnnecessaryAnonymousClass") // for Java 7 compatibility
  private static final Function<ValueDifference<Object, Object>, String> VALUE_DIFFERENCE_FORMAT =
      new Function<ValueDifference<Object, Object>, String>() {
        @Override
        public String apply(ValueDifference<Object, Object> values) {
          boolean includeTypes =
              String.valueOf(values.actual).equals(String.valueOf(values.expected));
          return lenientFormat(
              "(expected %s but got %s)",
              includeTypes ? new TypedToStringWrapper(values.expected) : values.expected,
              includeTypes ? new TypedToStringWrapper(values.actual) : values.actual);
        }
      };

  private static final Map<Object, Object> addKeyTypes(Map<?, ?> in) {
    Map<Object, Object> out = Maps.newLinkedHashMap();
    for (Map.Entry<?, ?> entry : in.entrySet()) {
      out.put(new TypedToStringWrapper(entry.getKey()), entry.getValue());
    }
    return out;
  }

  private static class TypedToStringWrapper {

    private final Object delegate;

    TypedToStringWrapper(Object delegate) {
      this.delegate = delegate;
    }

    @Override
    public boolean equals(Object other) {
      return Objects.equal(delegate, other);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(delegate);
    }

    @Override
    public String toString() {
      return lenientFormat("%s (%s)", delegate, objectToTypeName(delegate));
    }
  }

  private class MapInOrder implements Ordered {

    private final Map<?, ?> expectedMap;
    private final String failVerb;

    MapInOrder(Map<?, ?> expectedMap, String failVerb) {
      this.expectedMap = expectedMap;
      this.failVerb = failVerb;
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
          Lists.newArrayList(Sets.intersection(expectedMap.keySet(), actual.keySet()));
      List<?> actualKeyOrder =
          Lists.newArrayList(Sets.intersection(actual.keySet(), expectedMap.keySet()));
      if (!actualKeyOrder.equals(expectedKeyOrder)) {
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that <%s> %s <%s>",
                    actualCustomStringRepresentationForPackageMembersToCall(),
                    failVerb,
                    expectedMap)));
      }
    }
  }

  /** Ordered implementation that does nothing because it's already known to be true. */
  @SuppressWarnings("UnnecessaryAnonymousClass") // for Java 7 compatibility
  private static final Ordered IN_ORDER =
      new Ordered() {
        @Override
        public void inOrder() {}
      };

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  @SuppressWarnings("UnnecessaryAnonymousClass") // for Java 7 compatibility
  private static final Ordered ALREADY_FAILED =
      new Ordered() {
        @Override
        public void inOrder() {}
      };

  /**
   * Starts a method chain for a check in which the actual values (i.e. the values of the {@link
   * Map} under test) are compared to expected values using the given {@link Correspondence}. The
   * actual values must be of type {@code A}, the expected values must be of type {@code E}. The
   * check is actually executed by continuing the method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualMap)
   *   .comparingValuesUsing(correspondence)
   *   .containsEntry(expectedKey, expectedValue);
   * }</pre>
   *
   * where {@code actualMap} is a {@code Map<?, A>} (or, more generally, a {@code Map<?, ? extends
   * A>}), {@code correspondence} is a {@code Correspondence<A, E>}, and {@code expectedValue} is an
   * {@code E}.
   *
   * <p>Note that keys will always be compared with regular object equality ({@link Object#equals}).
   *
   * <p>Any of the methods on the returned object may throw {@link ClassCastException} if they
   * encounter an actual value that is not of type {@code A} or an expected value that is not of
   * type {@code E}.
   */
  public final <A, E> UsingCorrespondence<A, E> comparingValuesUsing(
      Correspondence<? super A, ? super E> correspondence) {
    return new UsingCorrespondence<>(correspondence);
  }

  /**
   * A partially specified check in which the actual values (i.e. the values of the {@link Map}
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
     * Fails if the map does not contain an entry with the given key and a value that corresponds to
     * the given value.
     */
    public void containsEntry(@Nullable Object expectedKey, @Nullable E expectedValue) {
      if (actual.containsKey(expectedKey)) {
        // Found matching key.
        A actualValue = getCastSubject().get(expectedKey);
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        if (correspondence.safeCompare(actualValue, expectedValue, exceptions)) {
          // The expected key had the expected value. There's no need to check exceptions here,
          // because if Correspondence.compare() threw then safeCompare() would return false.
          return;
        }
        // Found matching key with non-matching value.
        @Nullable
        String diff = correspondence.safeFormatDiff(actualValue, expectedValue, exceptions);
        if (diff != null) {
          failWithoutActual(
              ImmutableList.<Fact>builder()
                  .add(
                      simpleFact(
                          lenientFormat(
                              "Not true that <%s> contains an entry with key <%s> and a value that "
                                  + "%s <%s>. However, it has a mapping from that key to <%s> "
                                  + "(diff: %s)",
                              actualCustomStringRepresentationForPackageMembersToCall(),
                              expectedKey,
                              correspondence,
                              expectedValue,
                              actualValue,
                              diff)))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        } else {
          failWithoutActual(
              ImmutableList.<Fact>builder()
                  .add(
                      simpleFact(
                          lenientFormat(
                              "Not true that <%s> contains an entry with key <%s> and a value that "
                                  + "%s <%s>. However, it has a mapping from that key to <%s>",
                              actualCustomStringRepresentationForPackageMembersToCall(),
                              expectedKey,
                              correspondence,
                              expectedValue,
                              actualValue)))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        }
      } else {
        // Did not find matching key. Look for the matching value with a different key.
        Set<Object> keys = new LinkedHashSet<>();
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        for (Map.Entry<?, A> actualEntry : getCastSubject().entrySet()) {
          if (correspondence.safeCompare(actualEntry.getValue(), expectedValue, exceptions)) {
            keys.add(actualEntry.getKey());
          }
        }
        if (!keys.isEmpty()) {
          // Found matching values with non-matching keys.
          failWithoutActual(
              ImmutableList.<Fact>builder()
                  .add(
                      simpleFact(
                          lenientFormat(
                              "Not true that <%s> contains an entry with key <%s> and a value that "
                                  + "%s <%s>. However, the following keys are mapped to such "
                                  + "values: <%s>",
                              actualCustomStringRepresentationForPackageMembersToCall(),
                              expectedKey,
                              correspondence,
                              expectedValue,
                              keys)))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        } else {
          // Did not find matching key or value.
          failWithoutActual(
              ImmutableList.<Fact>builder()
                  .add(
                      simpleFact(
                          lenientFormat(
                              "Not true that <%s> contains an entry with key <%s> and a value that "
                                  + "%s <%s>",
                              actualCustomStringRepresentationForPackageMembersToCall(),
                              expectedKey,
                              correspondence,
                              expectedValue)))
                  .addAll(exceptions.describeAsAdditionalInfo())
                  .build());
        }
      }
    }

    /**
     * Fails if the map contains an entry with the given key and a value that corresponds to the
     * given value.
     */
    public void doesNotContainEntry(
        @Nullable Object excludedKey, @Nullable E excludedValue) {
      if (actual.containsKey(excludedKey)) {
        // Found matching key. Fail if the value matches, too.
        A actualValue = getCastSubject().get(excludedKey);
        Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
        if (correspondence.safeCompare(actualValue, excludedValue, exceptions)) {
          // The matching key had a matching value. There's no need to check exceptions here,
          // because if Correspondence.compare() threw then safeCompare() would return false.
          failWithoutActual(
              simpleFact(
                  lenientFormat(
                      "Not true that <%s> does not contain an entry with key <%s> and a value that "
                          + "%s <%s>. It maps that key to <%s>",
                      actualCustomStringRepresentationForPackageMembersToCall(),
                      excludedKey,
                      correspondence,
                      excludedValue,
                      actualValue)));
        }
        // The value didn't match, but we still need to fail if we hit an exception along the way.
        if (exceptions.hasCompareException()) {
          failWithActual(
              ImmutableList.<Fact>builder()
                  .addAll(exceptions.describeAsMainCause())
                  .add(
                      simpleFact(
                          "comparing contents by testing that no entry had the forbidden key and "
                              + "a value that "
                              + correspondence
                              + " the forbidden value"))
                  .add(fact("forbidden key", excludedKey))
                  .add(fact("forbidden value", excludedValue))
                  .build());
        }
      }
    }

    /**
     * Fails if the map does not contain exactly the given set of keys mapping to values that
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
    public Ordered containsExactly(@Nullable Object k0, @Nullable E v0, Object... rest) {
      @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
      Map<Object, E> expectedMap = (Map<Object, E>) accumulateMap("containsExactly", k0, v0, rest);
      return containsExactlyEntriesIn(expectedMap);
    }

    /**
     * Fails if the map does not contain at least the given set of keys mapping to values that
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
    public Ordered containsAtLeast(@Nullable Object k0, @Nullable E v0, Object... rest) {
      @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
      Map<Object, E> expectedMap = (Map<Object, E>) accumulateMap("containsAtLeast", k0, v0, rest);
      return containsAtLeastEntriesIn(expectedMap);
    }

    /**
     * Fails if the map does not contain exactly the keys in the given map, mapping to values that
     * correspond to the values of the given map.
     */
    @CanIgnoreReturnValue
    public Ordered containsExactlyEntriesIn(Map<?, ? extends E> expectedMap) {
      if (expectedMap.isEmpty()) {
        if (actual.isEmpty()) {
          return IN_ORDER;
        } else {
          isEmpty(); // fails
          return ALREADY_FAILED;
        }
      }
      return internalContainsEntriesIn("exactly", expectedMap, false);
    }

    /**
     * Fails if the map does not contain at least the keys in the given map, mapping to values that
     * correspond to the values of the given map.
     */
    @CanIgnoreReturnValue
    public Ordered containsAtLeastEntriesIn(Map<?, ? extends E> expectedMap) {
      if (expectedMap.isEmpty()) {
        return IN_ORDER;
      }
      return internalContainsEntriesIn("at least", expectedMap, true);
    }

    private <K, V extends E> Ordered internalContainsEntriesIn(
        String modifier, Map<K, V> expectedMap, boolean allowUnexpected) {
      final Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forMapValues();
      MapDifference<Object, A, V> diff =
          MapDifference.create(
              getCastSubject(),
              expectedMap,
              allowUnexpected,
              new ValueTester<A, E>() {
                @Override
                public boolean test(A actualValue, E expectedValue) {
                  return correspondence.safeCompare(actualValue, expectedValue, exceptions);
                }
              });
      if (diff.isEmpty()) {
        // The maps correspond exactly. There's no need to check exceptions here, because if
        // Correspondence.compare() threw then safeCompare() would return false and the diff would
        // record that we had the wrong value for that key.
        return new MapInOrder(
            expectedMap,
            lenientFormat(
                "contains, in order, %s one entry that has a key that is equal to and a "
                    + "value that %s the key and value of each entry of",
                modifier, correspondence));
      }
      failWithoutActual(
          ImmutableList.<Fact>builder()
              .add(
                  simpleFact(
                      lenientFormat(
                          "Not true that <%s> contains %s one entry that has a key that is "
                              + "equal to and a value that %s the key and value of each entry of "
                              + "<%s>. It %s",
                          actualCustomStringRepresentationForPackageMembersToCall(),
                          modifier,
                          correspondence,
                          expectedMap,
                          diff.describe(this.<V>valueDiffFormat(exceptions)))))
              .addAll(exceptions.describeAsAdditionalInfo())
              .build());
      return ALREADY_FAILED;
    }

    /**
     * Returns a formatting function for value differences when compared using the current
     * correspondence.
     */
    private final <V extends E> Function<ValueDifference<A, V>, String> valueDiffFormat(
        final Correspondence.ExceptionStore exceptions) {
      return new Function<ValueDifference<A, V>, String>() {
        @Override
        public String apply(ValueDifference<A, V> values) {
          @Nullable
          String diffString =
              correspondence.safeFormatDiff(values.actual, values.expected, exceptions);
          if (diffString != null) {
            return lenientFormat(
                "(expected %s but got %s, diff: %s)", values.expected, values.actual, diffString);
          } else {
            return lenientFormat("(expected %s but got %s)", values.expected, values.actual);
          }
        }
      };
    }

    @SuppressWarnings("unchecked") // throwing ClassCastException is the correct behaviour
    private Map<?, A> getCastSubject() {
      return (Map<?, A>) actual;
    }
  }
}
