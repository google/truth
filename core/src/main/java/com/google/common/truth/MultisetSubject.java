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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Multiset;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Multiset} values.
 *
 * @author Kurt Alfred Kluever
 */
public final class MultisetSubject extends IterableSubject {

  private final @Nullable Multiset<?> actual;

  private MultisetSubject(FailureMetadata metadata, @Nullable Multiset<?> multiset) {
    super(metadata, multiset, /* typeDescriptionOverride= */ "multiset");
    this.actual = multiset;
  }

  /**
   * Checks that the actual multiset has exactly the given number of occurrences of the given
   * element.
   */
  public final void hasCount(@Nullable Object element, int expectedCount) {
    checkArgument(expectedCount >= 0, "expectedCount(%s) must be >= 0", expectedCount);
    int actualCount = checkNotNull(actual).count(element);
    check("count(%s)", element).that(actualCount).isEqualTo(expectedCount);
  }

  static Factory<MultisetSubject, Multiset<?>> multisets() {
    return MultisetSubject::new;
  }
}
