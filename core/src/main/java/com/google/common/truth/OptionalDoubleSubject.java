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
import static com.google.common.truth.Platform.doubleToString;

import java.util.OptionalDouble;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link OptionalDouble} values.
 *
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@IgnoreJRERequirement
public final class OptionalDoubleSubject extends Subject {

  private final @Nullable OptionalDouble actual;

  private OptionalDoubleSubject(FailureMetadata failureMetadata, @Nullable OptionalDouble actual) {
    super(failureMetadata, actual);
    this.actual = actual;
  }

  /** Checks that the actual {@link OptionalDouble} contains a value. */
  public void isPresent() {
    if (actual == null) {
      failWithActual(simpleFact("expected present optional"));
    } else if (!actual.isPresent()) {
      failWithoutActual(simpleFact("expected to be present"));
    }
  }

  /** Checks that the actual {@link OptionalDouble} does not contain a value. */
  public void isEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected empty optional"));
    } else if (actual.isPresent()) {
      failWithoutActual(
          simpleFact("expected to be empty"),
          fact("but was present with value", doubleToString(actual.getAsDouble())));
    }
  }

  /**
   * Checks that the actual {@link OptionalDouble} contains the given value. This method is
   * <i>not</i> recommended when the code under test is doing any kind of arithmetic, since the
   * exact result of floating point arithmetic is sensitive to apparently trivial changes. More
   * sophisticated comparisons can be done using {@code assertThat(optional.getAsDouble())…}. This
   * method is recommended when the code under test is specified as either copying a value without
   * modification from its input or returning a well-defined literal or constant value.
   */
  public void hasValue(double expected) {
    if (actual == null) {
      failWithActual("expected an optional with value", expected);
    } else if (!actual.isPresent()) {
      failWithoutActual(
          fact("expected to have value", doubleToString(expected)), simpleFact("but was absent"));
    } else {
      checkNoNeedToDisplayBothValues("getAsDouble()")
          .that(actual.getAsDouble())
          .isEqualTo(expected);
    }
  }

  /**
   * Obsolete factory instance. This factory was previously necessary for assertions like {@code
   * assertWithMessage(...).about(optionalDoubles()).that(optional)....}. Now, you can perform
   * assertions like that without the {@code about(...)} call.
   *
   * @deprecated Instead of {@code about(optionalDoubles()).that(...)}, use just {@code that(...)}.
   *     Similarly, instead of {@code assertAbout(optionalDoubles()).that(...)}, use just {@code
   *     assertThat(...)}.
   */
  @Deprecated
  @SuppressWarnings("InlineMeSuggester") // We want users to remove the surrounding call entirely.
  public static Factory<OptionalDoubleSubject, OptionalDouble> optionalDoubles() {
    return OptionalDoubleSubject::new;
  }
}
