/*
 * Copyright (c) 2018 Google, Inc.
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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Subject for {@link AssertionError} objects thrown by Truth. {@code TruthFailureSubject} contains
 * methods for asserting about the individual "facts" of those failures. This allows tests to avoid
 * asserting about the same fact more often than necessary, including avoiding asserting about facts
 * that are set by other subjects that the main subject delegates to. This keeps tests shorter and
 * less fragile.
 *
 * <p>To create an instance, call {@link ExpectFailure#assertThat}.
 *
 * <p>This class accepts any {@code AssertionError} value, but it will throw an exception if a
 * caller tries to access the facts of an error that wasn't produced by Truth.
 */
public final class TruthFailureSubject extends ThrowableSubject {
  static final Fact HOW_TO_TEST_KEYS_WITHOUT_VALUES =
      simpleFact(
          "To test that a key is present without a value, "
              + "use factKeys().contains(...) or a similar method.");

  /*
   * TODO(cpovirk): Expose this publicly once it can have the right type. That can't happen until we
   * add type parameters to ThrowableSubject, make TruthFailureSubject not extend ThrowableSubject,
   * remove the type parameters from Subject altogether, or *maybe* just loosen the type parameters
   * on Factory. At that point, also mention it in the class-level docs.
   */

  /**
   * Package-private factory for creating {@link TruthFailureSubject} instances. At the moment, the
   * only public way to create an instance is {@link ExpectFailure#assertThat}.
   */
  static Factory<ThrowableSubject, Throwable> truthFailures() {
    return FACTORY;
  }

  private static final Factory<ThrowableSubject, Throwable> FACTORY =
      new Factory<ThrowableSubject, Throwable>() {
        @Override
        public ThrowableSubject createSubject(FailureMetadata metadata, Throwable actual) {
          return new TruthFailureSubject(metadata, actual, "failure");
        }
      };

  TruthFailureSubject(
      FailureMetadata metadata,
      @NullableDecl Throwable throwable,
      @NullableDecl String typeDescription) {
    super(metadata, throwable, typeDescription);
  }

  /** Returns a subject for the list of fact keys. */
  public IterableSubject factKeys() {
    if (!(actual() instanceof ErrorWithFacts)) {
      failWithActual(simpleFact("expected a failure thrown by Truth's new failure API"));
      return ignoreCheck().that(ImmutableList.of());
    }
    ErrorWithFacts error = (ErrorWithFacts) actual();
    return check("factKeys()").that(getFactKeys(error));
  }

  private static ImmutableList<String> getFactKeys(ErrorWithFacts error) {
    ImmutableList.Builder<String> facts = ImmutableList.builder();
    for (Fact fact : error.facts()) {
      facts.add(fact.key);
    }
    return facts.build();
  }

  /**
   * Returns a subject for the value with the given name.
   *
   * <p>The value is always a string, the {@code String.valueOf} representation of the value passed
   * to {@link Fact#fact}.
   *
   * <p>The value is never null:
   *
   * <ul>
   *   <li>In the case of {@linkplain Fact#simpleFact facts that have no value}, {@code factValue}
   *       throws an exception. To test for such facts, use {@link #factKeys()}{@code
   *       .contains(...)} or a similar method.
   *   <li>In the case of facts that have a value that is rendered as "null" (such as those created
   *       with {@code fact("key", null)}), {@code factValue} considers them have a string value,
   *       the string "null."
   * </ul>
   *
   * <p>If the failure under test contains more than one fact with the given key, this method will
   * fail the test. To assert about such a failure, use {@linkplain #factValue(String, int) the
   * other overload} of {@code factValue}.
   */
  public StringSubject factValue(String key) {
    return doFactValue(key, null);
  }

  /**
   * Returns a subject for the value of the {@code index}-th instance of the fact with the given
   * name. Most Truth failures do not contain multiple facts with the same key, so most tests should
   * use {@linkplain #factValue(String) the other overload} of {@code factValue}.
   */
  public StringSubject factValue(String key, int index) {
    checkArgument(index >= 0, "index must be nonnegative: %s", index);
    return doFactValue(key, index);
  }

  private StringSubject doFactValue(String key, @NullableDecl Integer index) {
    checkNotNull(key);
    if (!(actual() instanceof ErrorWithFacts)) {
      failWithActual(simpleFact("expected a failure thrown by Truth's new failure API"));
      return ignoreCheck().that("");
    }
    ErrorWithFacts error = (ErrorWithFacts) actual();

    /*
     * We don't care as much about including the actual Throwable and its facts in these because
     * the Throwable will be attached as a cause in nearly all cases.
     */
    ImmutableList<Fact> factsWithName = factsWithName(error, key);
    if (factsWithName.isEmpty()) {
      failWithoutActual(
          fact("expected to contain fact", key), fact("but contained only", getFactKeys(error)));
      return ignoreCheck().that("");
    }
    if (index == null && factsWithName.size() > 1) {
      failWithoutActual(
          fact("expected to contain a single fact with key", key),
          fact("but contained multiple", factsWithName));
      return ignoreCheck().that("");
    }
    if (index != null && index > factsWithName.size()) {
      failWithoutActual(
          fact("for key", key),
          fact("index too high", index),
          fact("fact count was", factsWithName.size()));
      return ignoreCheck().that("");
    }
    String value = factsWithName.get(firstNonNull(index, 0)).value;
    if (value == null) {
      if (index == null) {
        failWithoutActual(
            simpleFact("expected to have a value"),
            fact("for key", key),
            simpleFact("but the key was present with no value"),
            HOW_TO_TEST_KEYS_WITHOUT_VALUES);
      } else {
        failWithoutActual(
            simpleFact("expected to have a value"),
            fact("for key", key),
            fact("and index", index),
            simpleFact("but the key was present with no value"),
            HOW_TO_TEST_KEYS_WITHOUT_VALUES);
      }
      return ignoreCheck().that("");
    }
    StandardSubjectBuilder check =
        index == null ? check("factValue(%s)", key) : check("factValue(%s, %s)", key, index);
    return check.that(value);
  }

  private static ImmutableList<Fact> factsWithName(ErrorWithFacts error, String key) {
    ImmutableList.Builder<Fact> facts = ImmutableList.builder();
    for (Fact fact : error.facts()) {
      if (fact.key.equals(key)) {
        facts.add(fact);
      }
    }
    return facts.build();
  }
}
