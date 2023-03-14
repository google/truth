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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Propositions for {@code long} subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
public class LongSubject extends ComparableSubject<Long> {
  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected LongSubject(FailureMetadata metadata, @Nullable Long actual) {
    super(metadata, actual);
  }

  /**
   * @deprecated Use {@link #isEqualTo} instead. Long comparison is consistent with equality.
   */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(@Nullable Long other) {
    super.isEquivalentAccordingToCompareTo(other);
  }

  /**
   * Checks that the subject is greater than {@code other}.
   *
   * <p>To check that the subject is greater than <i>or equal to</i> {@code other}, use {@link
   * #isAtLeast}.
   */
  public final void isGreaterThan(int other) {
    isGreaterThan((long) other);
  }

  /**
   * Checks that the subject is less than {@code other}.
   *
   * <p>To check that the subject is less than <i>or equal to</i> {@code other}, use {@link
   * #isAtMost} .
   */
  public final void isLessThan(int other) {
    isLessThan((long) other);
  }

  /**
   * Checks that the subject is less than or equal to {@code other}.
   *
   * <p>To check that the subject is <i>strictly</i> less than {@code other}, use {@link
   * #isLessThan}.
   */
  public final void isAtMost(int other) {
    isAtMost((long) other);
  }

  /**
   * Checks that the subject is greater than or equal to {@code other}.
   *
   * <p>To check that the subject is <i>strictly</i> greater than {@code other}, use {@link
   * #isGreaterThan}.
   */
  public final void isAtLeast(int other) {
    isAtLeast((long) other);
  }
}
