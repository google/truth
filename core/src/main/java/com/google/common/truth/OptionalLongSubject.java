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

import java.util.OptionalLong;
import org.jspecify.annotations.Nullable;

/**
 * Propositions for Java 8 {@link OptionalLong} subjects.
 *
 * @author Ben Douglass
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@IgnoreJRERequirement
public final class OptionalLongSubject extends Subject {
  private final @Nullable OptionalLong actual;

  private OptionalLongSubject(FailureMetadata failureMetadata, @Nullable OptionalLong subject) {
    super(failureMetadata, subject, /* typeDescriptionOverride= */ "optionalLong");
    this.actual = subject;
  }

  /** Fails if the {@link OptionalLong} is empty or the subject is null. */
  public void isPresent() {
    if (actual == null) {
      failWithActual(simpleFact("expected present optional"));
    } else if (!actual.isPresent()) {
      failWithoutActual(simpleFact("expected to be present"));
    }
  }

  /** Fails if the {@link OptionalLong} is present or the subject is null. */
  public void isEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected empty optional"));
    } else if (actual.isPresent()) {
      failWithoutActual(
          simpleFact("expected to be empty"),
          fact("but was present with value", actual.getAsLong()));
    }
  }

  /**
   * Fails if the {@link OptionalLong} does not have the given value or the subject is null. More
   * sophisticated comparisons can be done using {@code assertThat(optional.getAsLong())…}.
   */
  public void hasValue(long expected) {
    if (actual == null) {
      failWithActual("expected an optional with value", expected);
    } else if (!actual.isPresent()) {
      failWithoutActual(fact("expected to have value", expected), simpleFact("but was absent"));
    } else {
      checkNoNeedToDisplayBothValues("getAsLong()").that(actual.getAsLong()).isEqualTo(expected);
    }
  }

  /**
   * Obsolete factory instance. This factory was previously necessary for assertions like {@code
   * assertWithMessage(...).about(optionalLongs()).that(optional)....}. Now, you can perform
   * assertions like that without the {@code about(...)} call.
   *
   * @deprecated Instead of {@code about(optionalLongs()).that(...)}, use just {@code that(...)}.
   *     Similarly, instead of {@code assertAbout(optionalLongs()).that(...)}, use just {@code
   *     assertThat(...)}.
   */
  @Deprecated
  @SuppressWarnings("InlineMeSuggester") // We want users to remove the surrounding call entirely.
  public static Factory<OptionalLongSubject, OptionalLong> optionalLongs() {
    return OptionalLongSubject::new;
  }
}
