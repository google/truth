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

import com.google.common.collect.Multiset;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Multiset} values.
 *
 * @author Kurt Alfred Kluever
 */
public final class MultisetSubject extends IterableSubject {

  private final @Nullable Multiset<?> actual;

  private MultisetSubject(FailureMetadata metadata, @Nullable Multiset<?> actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /**
   * Checks that the actual multiset has exactly the given number of occurrences of the given
   * element.
   */
  public void hasCount(@Nullable Object element, int expectedCount) {
    if (actual == null) {
      failWithoutActual(
          simpleFact("cannot perform assertions on the contents of a null multiset"),
          fact("element", element),
          fact("expected count", expectedCount));
    } else if (expectedCount < 0) {
      failWithoutActual(
          simpleFact("expected an element count that is negative, but that is impossible"),
          fact("element", element),
          fact("expected count", expectedCount),
          fact("actual count", actual.count(element)),
          actualValue("multiset was"));
    } else {
      check("count(%s)", element).that(actual.count(element)).isEqualTo(expectedCount);
    }
  }

  static Factory<MultisetSubject, Multiset<?>> multisets() {
    return MultisetSubject::new;
  }
}
