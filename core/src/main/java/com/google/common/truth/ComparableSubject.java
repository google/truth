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

  /** Checks that the subject is in {@code range}. */
  public final void isIn(Range<T> range) {
    if (!range.contains(actual())) {
      fail("is in", range);
    }
  }

  /** Checks that the subject is <i>not</i> in {@code range}. */
  public final void isNotIn(Range<T> range) {
    if (range.contains(actual())) {
      fail("is not in", range);
    }
  }

  /**
   * Checks that the subject is equivalent to {@code other} according to {@link
   * Comparable#compareTo}, (i.e., checks that {@code a.comparesTo(b) == 0}).
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
   * Checks that the subject is equivalent to {@code other} according to {@link
   * Comparable#compareTo}, (i.e., checks that {@code a.comparesTo(b) == 0}).
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

  /**
   * Checks that the subject is greater than {@code other}.
   *
   * <p>Use {@link #isAtLeast} to check that the subject is greater than <i>or equal to</i> {@code
   * other}.
   */
  public final void isGreaterThan(T other) {
    if (actual().compareTo(other) <= 0) {
      fail("is greater than", other);
    }
  }

  /**
   * Checks that the subject is less than {@code other}.
   *
   * <p>Use {@link #isAtMost} to check that the subject is less than <i>or equal to</i> {@code
   * other}.
   */
  public final void isLessThan(T other) {
    if (actual().compareTo(other) >= 0) {
      fail("is less than", other);
    }
  }

  /**
   * Checks that the subject is less than or equal to {@code other}.
   *
   * <p>Use {@link #isLessThan} to check that the subject is less than {@code other}.
   */
  public final void isAtMost(T other) {
    if (actual().compareTo(other) > 0) {
      fail("is at most", other);
    }
  }

  /**
   * Checks that the subject is greater than or equal to {@code other}.
   *
   * <p>Use {@link #isGreaterThan} to check that the subject is greater than {@code other}.
   */
  public final void isAtLeast(T other) {
    if (actual().compareTo(other) < 0) {
      fail("is at least", other);
    }
  }
}
