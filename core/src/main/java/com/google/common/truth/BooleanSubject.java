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

import static com.google.common.truth.Fact.simpleFact;

import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Boolean} values.
 */
public final class BooleanSubject extends Subject {
  private final @Nullable Boolean actual;

  private BooleanSubject(FailureMetadata metadata, @Nullable Boolean actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /** Checks that the actual value is {@code true}. */
  public void isTrue() {
    if (actual == null) {
      isEqualTo(true); // fails
    } else if (!actual) {
      failWithoutActual(simpleFact("expected to be true"));
    }
  }

  /** Checks that the actual value is {@code false}. */
  public void isFalse() {
    if (actual == null) {
      isEqualTo(false); // fails
    } else if (actual) {
      failWithoutActual(simpleFact("expected to be false"));
    }
  }

  static Factory<BooleanSubject, Boolean> booleans() {
    return BooleanSubject::new;
  }
}
