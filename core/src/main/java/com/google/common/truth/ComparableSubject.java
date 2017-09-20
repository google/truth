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
import javax.annotation.Nullable;

/**
 * Propositions for {@link Comparable} typed subjects.
 *
 * @author Kurt Alfred Kluever
 */
public abstract class ComparableSubject<S extends ComparableSubject<S, T>, T extends Comparable>
    extends Subject<S, T> {
  /**
   * @deprecated Switch your {@code Subject} from accepting {@link FailureStrategy} (and exposing a
   *     {@link SubjectFactory}) to accepting a {@link FailureMetadata} (and exposing a {@link
   *     Subject.Factory}), at which point you'll call the {@code FailureMetadata} overload of this
   *     constructor instead.
   */
  @Deprecated
  protected ComparableSubject(FailureStrategy failureStrategy, @Nullable T actual) {
    super(failureStrategy, actual);
  }

  protected ComparableSubject(FailureMetadata metadata, @Nullable T actual) {
    super(metadata, actual);
  }

  /** Fails if the subject is not in the given range. */
  public final void isIn(Range<T> range) {
    if (!range.contains(actual())) {
      fail("is in", range);
    }
  }

  /** Fails if the subject is in the given range. */
  public final void isNotIn(Range<T> range) {
    if (range.contains(actual())) {
      fail("is not in", range);
    }
  }

  /**
   * Fails if the subject is not equivalent to the given value according to {@link
   * Comparable#compareTo}, (i.e., fails if {@code a.comparesTo(b) != 0}).
   *
   * <p><b>Note:</b> Do not use this method for checking object equality. Instead, use {@link
   * #isEqualTo(Object)}.
   */
  public void isEquivalentAccordingToCompareTo(T other) {
    if (actual().compareTo(other) != 0) {
      failWithRawMessage(
          "%s should have been equivalent to <%s> according to compareTo()",
          actualAsString(), other);
    }
  }

  /**
   * Fails if the subject is not equivalent to the given value according to {@link
   * Comparable#compareTo}, (i.e., fails if {@code a.comparesTo(b) != 0}).
   *
   * <p><b>Note:</b> Do not use this method for checking object equality. Instead, use {@link
   * #isEqualTo(Object)}.
   *
   * @deprecated Use {@link #isEquivalentAccordingToCompareTo} instead.
   */
  @Deprecated
  public void comparesEqualTo(T other) {
    isEquivalentAccordingToCompareTo(other);
  }

  /** Fails if the subject is not greater than the given value. */
  public final void isGreaterThan(T other) {
    if (actual().compareTo(other) <= 0) {
      fail("is greater than", other);
    }
  }

  /** Fails if the subject is not less than the given value. */
  public final void isLessThan(T other) {
    if (actual().compareTo(other) >= 0) {
      fail("is less than", other);
    }
  }

  /** Fails if the subject is greater than the given value. */
  public final void isAtMost(T other) {
    if (actual().compareTo(other) > 0) {
      fail("is at most", other);
    }
  }

  /** Fails if the subject is less than the given value. */
  public final void isAtLeast(T other) {
    if (actual().compareTo(other) < 0) {
      fail("is at least", other);
    }
  }
}
