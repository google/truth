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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Range;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Comparable} values.
 *
 * @author Kurt Alfred Kluever
 * @param <T> the type of the object being tested by this {@code ComparableSubject}
 */
// TODO(b/136040841): Consider further tightening this to the proper `extends Comparable<? super T>`
public abstract class ComparableSubject<T extends Comparable<?>> extends Subject {
  /**
   * The actual value, which has type {@code T} except in unusual circumstances. The unusual
   * circumstances can happen under J2CL, where {@code JsEnum} types implement {@code Comparable} at
   * compile time (b/132736149) but not at runtime.
   */
  private final @Nullable Object actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected ComparableSubject(FailureMetadata metadata, @Nullable T actual) {
    this(metadata, (Object) actual);
  }

  /**
   * Constructor for use internally to work around the J2CL strangeness documented on {@link
   * #actual}.
   */
  private ComparableSubject(FailureMetadata metadata, @Nullable Object actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /** Checks that the actual value is in {@code range}. */
  public final void isIn(Range<T> range) {
    if (!range.contains(actualAsT())) {
      failWithActual("expected to be in range", range);
    }
  }

  /** Checks that the actual value is <i>not</i> in {@code range}. */
  public final void isNotIn(Range<T> range) {
    if (range.contains(actualAsT())) {
      failWithActual("expected not to be in range", range);
    }
  }

  /**
   * Checks that the actual value is equivalent to {@code other} according to {@link
   * Comparable#compareTo}, (i.e., checks that {@code a.comparesTo(b) == 0}).
   *
   * <p><b>Note:</b> Do not use this method for checking object equality. Instead, use {@link
   * #isEqualTo(Object)}.
   */
  public void isEquivalentAccordingToCompareTo(@Nullable T expected) {
    if (actualAsComparable().compareTo(checkNotNull(expected)) != 0) {
      failWithActual("expected value that sorts equal to", expected);
    }
  }

  /**
   * Checks that the actual value is greater than {@code other}.
   *
   * <p>To check that the actual value is greater than <i>or equal to</i> {@code other}, use {@link
   * #isAtLeast}.
   */
  public final void isGreaterThan(@Nullable T other) {
    if (actualAsComparable().compareTo(checkNotNull(other)) <= 0) {
      failWithActual("expected to be greater than", other);
    }
  }

  /**
   * Checks that the actual value is less than {@code other}.
   *
   * <p>To check that the actual value is less than <i>or equal to</i> {@code other}, use {@link
   * #isAtMost}.
   */
  public final void isLessThan(@Nullable T other) {
    if (actualAsComparable().compareTo(checkNotNull(other)) >= 0) {
      failWithActual("expected to be less than", other);
    }
  }

  /**
   * Checks that the actual value is less than or equal to {@code other}.
   *
   * <p>To check that the actual value is <i>strictly</i> less than {@code other}, use {@link
   * #isLessThan}.
   */
  public final void isAtMost(@Nullable T other) {
    if (actualAsComparable().compareTo(checkNotNull(other)) > 0) {
      failWithActual("expected to be at most", other);
    }
  }

  /**
   * Checks that the actual value is greater than or equal to {@code other}.
   *
   * <p>To check that the actual value is <i>strictly</i> greater than {@code other}, use {@link
   * #isGreaterThan}.
   */
  public final void isAtLeast(@Nullable T other) {
    if (actualAsComparable().compareTo(checkNotNull(other)) < 0) {
      failWithActual("expected to be at least", other);
    }
  }

  /**
   * Factory for {@link ComparableSubject}, with an actual-value type of {@code Object} to work
   * around the J2CL strangeness documented on {@link #actual}.
   */
  static <T extends Comparable<?>> Factory<ComparableSubject<T>, Object> comparables() {
    return (metadata, actual) -> new ComparableSubject<T>(metadata, actual) {};
  }

  @SuppressWarnings("unchecked")
  private Comparable<Object> actualAsComparable() {
    return checkNotNull((Comparable<Object>) actual);
  }

  @SuppressWarnings("unchecked")
  private T actualAsT() {
    return (T) checkNotNull(actual);
  }
}
