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

import com.google.common.collect.Range;

/**
 * Propositions for {@link Comparable} typed subjects.
 *
 * @author Kurt Alfred Kluever
 */
public abstract class ComparableSubject<S extends Subject<S, T>, T extends Comparable>
    extends Subject<S, T> {

  protected ComparableSubject(FailureStrategy failureStrategy, T subject) {
    super(failureStrategy, subject);
  }

  public final void isIn(Range<T> range) {
    if (!range.contains(getSubject())) {
      fail("is in", range);
    }
  }

  public final void isNotIn(Range<T> range) {
    if (range.contains(getSubject())) {
      fail("is not in", range);
    }
  }

  public final void isGreaterThan(T other) {
    if (getSubject().compareTo(other) <= 0) {
      fail("is greater than", other);
    }
  }

  public final void isLessThan(T other) {
    if (getSubject().compareTo(other) >= 0) {
      fail("is less than", other);
    }
  }
}
