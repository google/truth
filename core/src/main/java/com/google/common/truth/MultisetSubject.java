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

import com.google.common.collect.Multiset;
import javax.annotation.Nullable;

/**
 * Propositions for {@link Multiset} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class MultisetSubject extends IterableSubject {

  MultisetSubject(FailureMetadata metadata, @Nullable Multiset<?> multiset) {
    super(metadata, multiset);
  }

  @Override
  public MultisetSubject named(String format, Object... args) {
    super.named(format, args);
    return this;
  }

  /** Fails if the element does not have the given count. */
  public final void hasCount(@Nullable Object element, int expectedCount) {
    checkArgument(expectedCount >= 0, "expectedCount(%s) must be >= 0", expectedCount);
    int actualCount = ((Multiset<?>) actual()).count(element);
    if (actualCount != expectedCount) {
      failWithBadResults("has a count for <" + element + "> of", expectedCount, "is", actualCount);
    }
  }
}
