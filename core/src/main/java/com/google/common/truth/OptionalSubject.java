/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Optional} values.
 *
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@IgnoreJRERequirement
public final class OptionalSubject extends Subject {
  @SuppressWarnings("NullableOptional") // Truth always accepts nulls, no matter the type
  private final @Nullable Optional<?> actual;

  private OptionalSubject(
      FailureMetadata failureMetadata,
      @SuppressWarnings("NullableOptional") // Truth always accepts nulls, no matter the type
          @Nullable Optional<?> actual) {
    super(failureMetadata, actual);
    this.actual = actual;
  }

  // TODO(cpovirk): Consider making OptionalIntSubject and OptionalLongSubject delegate to this.

  /** Checks that the actual {@link Optional} contains a value. */
  public void isPresent() {
    if (actual == null) {
      failWithActual(simpleFact("expected present optional"));
    } else if (!actual.isPresent()) {
      failWithoutActual(simpleFact("expected to be present"));
    }
  }

  /** Checks that the actual {@link Optional} does not contain a value. */
  public void isEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected empty optional"));
    } else if (actual.isPresent()) {
      failWithoutActual(
          simpleFact("expected to be empty"), fact("but was present with value", actual.get()));
    }
  }

  /**
   * Checks that the actual {@link Optional} contains the given value.
   *
   * <p>To make more complex assertions on the optional's value, split your assertion in two:
   *
   * <pre>{@code
   * assertThat(myOptional).isPresent();
   * assertThat(myOptional.get()).contains("foo");
   * }</pre>
   */
  public void hasValue(@Nullable Object expected) {
    if (expected == null) {
      failWithoutActual(
          simpleFact("expected an optional with a null value, but that is impossible"),
          fact("was", actual));
    } else if (actual == null) {
      failWithActual("expected an optional with value", expected);
    } else if (!actual.isPresent()) {
      failWithoutActual(fact("expected to have value", expected), simpleFact("but was empty"));
    } else {
      checkNoNeedToDisplayBothValues("get()").that(actual.get()).isEqualTo(expected);
    }
  }

  /**
   * Obsolete factory instance. This factory was previously necessary for assertions like {@code
   * assertWithMessage(...).about(paths()).that(path)....}. Now, you can perform assertions like
   * that without the {@code about(...)} call.
   *
   * @deprecated Instead of {@code about(optionals()).that(...)}, use just {@code that(...)}.
   *     Similarly, instead of {@code assertAbout(optionals()).that(...)}, use just {@code
   *     assertThat(...)}.
   */
  @Deprecated
  @SuppressWarnings("InlineMeSuggester") // We want users to remove the surrounding call entirely.
  public static Factory<OptionalSubject, Optional<?>> optionals() {
    return OptionalSubject::new;
  }
}
