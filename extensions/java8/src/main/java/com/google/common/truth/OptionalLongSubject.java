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
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Propositions for Java 8 {@link OptionalLong} subjects.
 *
 * @author Ben Douglass
 */
public final class OptionalLongSubject extends Subject<OptionalLongSubject, OptionalLong> {
  OptionalLongSubject(
      FailureMetadata failureMetadata,
      @NullableDecl OptionalLong subject,
      @NullableDecl String typeDescription) {
    super(failureMetadata, subject, typeDescription);
  }

  /** Fails if the {@link OptionalLong} is empty or the subject is null. */
  public void isPresent() {
    if (actual() == null) {
      failWithActual(simpleFact("expected present optional"));
    } else if (!actual().isPresent()) {
      failWithoutActual(simpleFact("expected to be present"));
    }
  }

  /** Fails if the {@link OptionalLong} is present or the subject is null. */
  public void isEmpty() {
    if (actual() == null) {
      failWithActual(simpleFact("expected empty optional"));
    } else if (actual().isPresent()) {
      failWithoutActual(
          simpleFact("expected to be empty"),
          fact("but was present with value", actual().getAsLong()));
    }
  }

  /**
   * Fails if the {@link OptionalLong} does not have the given value or the subject is null. More
   * sophisticated comparisons can be done using {@link #hasValueThat()}.
   */
  public void hasValue(long expected) {
    if (actual() == null) {
      failWithActual("expected an optional with value", expected);
    } else if (!actual().isPresent()) {
      failWithoutActual(fact("expected to have value", expected), simpleFact("but was absent"));
    } else {
      checkNoNeedToDisplayBothValues("getAsLong()").that(actual().getAsLong()).isEqualTo(expected);
    }
  }

  /**
   * Prepares for a check regarding the value contained within the {@link OptionalLong}. Fails
   * immediately if the subject is empty.
   */
  public LongSubject hasValueThat() {
    if (actual() == null || !actual().isPresent()) {
      isPresent(); // fails
      return ignoreCheck().that(0L);
    } else {
      return check().that(actual().getAsLong());
    }
  }

  public static Subject.Factory<OptionalLongSubject, OptionalLong> optionalLongs() {
    return (metadata, subject) -> new OptionalLongSubject(metadata, subject, "optionalLong");
  }
}
