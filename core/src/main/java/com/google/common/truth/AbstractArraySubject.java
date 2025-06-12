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

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static java.lang.reflect.Array.getLength;

import org.jspecify.annotations.Nullable;

/**
 * A common supertype for Array subjects, abstracting some common display and error infrastructure.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
abstract class AbstractArraySubject extends Subject {
  private final @Nullable Object actual;

  AbstractArraySubject(FailureMetadata metadata, @Nullable Object actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /** Checks that the actual array is empty (i.e., that {@code array.length == 0}). */
  public final void isEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected an empty array"));
    } else if (getLength(actual) > 0) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Checks that the actual array is not empty (i.e., that {@code array.length > 0}). */
  public final void isNotEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected a nonempty array"));
    } else if (getLength(actual) == 0) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /**
   * Checks that the actual array has the given length.
   *
   * @throws IllegalArgumentException if {@code length < 0}
   */
  public final void hasLength(int length) {
    if (length < 0) {
      failWithoutActual(
          simpleFact("could not perform length check because expected length is negative"),
          fact("expected length", length),
          fact("array was", actualCustomStringRepresentationForPackageMembersToCall()));
    } else if (actual == null) {
      failWithActual(fact("expected an array with length", length));
    } else {
      check("length").that(getLength(actual)).isEqualTo(length);
    }
  }
}
