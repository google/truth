/*
 * Copyright (c) 2015 Google, Inc.
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

import java.util.Comparator;

import javax.annotation.Nullable;

/**
 * Propositions which use an explicit {@link Comparator}.
 *
 * @author Ben Blank
 */
public class FauxComparable<S extends Subject<S, T>, T> {
  private final Comparator<? super T> comparator;
  private final Subject<S, T> subject;

  public FauxComparable(Comparator<? super T> comparator, @Nullable Subject<S, T> subject) {
    this.comparator = comparator;
    this.subject = subject;
  }

  /**
   * Fails if the subject is not equivalent to the given value according to
   * the supplied {@link Comparator}, (i.e., fails if
   * {@code comparator.compare(a, b) != 0}).
   *
   * <p>
   * <b>Note:</b> Do not use this method for checking object equality.
   * Instead, use {@link #isEqualTo(Object)}.
   */
  public void isEquivalentAccordingToComparator(T other) {
    if (comparator.compare(subject.getSubject(), other) != 0) {
      subject.failWithRawMessage(
          "%s should have been equivalent to <%s> according to comparator <%s>",
          subject.getDisplaySubject(),
          other,
          comparator);
    }
  }

  /**
   * Fails if the subject is not greater than the given value.
   */
  public final void isGreaterThan(T other) {
    if (comparator.compare(subject.getSubject(), other) <= 0) {
      subject.failWithRawMessage(
          "Not true that %s is greater than <%s> according to comparator <%s>",
          subject.getDisplaySubject(),
          other,
          comparator);
    }
  }

  /**
   * Fails if the subject is not less than the given value.
   */
  public final void isLessThan(T other) {
    if (comparator.compare(subject.getSubject(), other) >= 0) {
      subject.failWithRawMessage(
          "Not true that %s is less than <%s> according to comparator <%s>",
          subject.getDisplaySubject(),
          other,
          comparator);
    }
  }

  /**
   * Fails if the subject is greater than the given value.
   */
  public final void isAtMost(T other) {
    if (comparator.compare(subject.getSubject(), other) > 0) {
      subject.failWithRawMessage(
          "Not true that %s is at most <%s> according to comparator <%s>",
          subject.getDisplaySubject(),
          other,
          comparator);
    }
  }

  /**
   * Fails if the subject is less than the given value.
   */
  public final void isAtLeast(T other) {
    if (comparator.compare(subject.getSubject(), other) < 0) {
      subject.failWithRawMessage(
          "Not true that %s is at least <%s> according to comparator <%s>",
          subject.getDisplaySubject(),
          other,
          comparator);
    }
  }
}
