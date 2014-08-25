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

/**
 * Propositions for Long subjects
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
// Can't be final because we use codegen to generate a subclass
public class LongSubject extends ComparableSubject<LongSubject, Long> {

  public LongSubject(FailureStrategy failureStrategy, Long subject) {
    super(failureStrategy, subject);
  }

  /**
   * Attests that a Subject<Long> is inclusively within the {@code lower} and
   * {@code upper} bounds provided or fails.
   *
   * @throws IllegalArgumentException
   *           if the lower bound is greater than the upper.
   */
  public void isInclusivelyInRange(long lower, long upper) {
    ensureOrderedBoundaries(lower, upper);
    if (!(lower <= getSubject() && getSubject() <= upper)) {
      fail("is inclusively in range", lower, upper);
    }
  }

  /**
   * Attests that a Subject<Long> is exclusively within the {@code lower} and
   * {@code upper} bounds provided or fails.
   *
   * @throws IllegalArgumentException
   *           if the lower bound is greater than the upper.
   */
  public void isBetween(long lower, long upper) {
    ensureOrderedBoundaries(lower, upper);
    if (!(lower < getSubject() && getSubject() < upper)) {
      fail("is in between", lower, upper);
    }
  }

  /**
   * Guards against inverted lower/upper boundaries, and throws if
   * they are so inverted.
   */
  private static void ensureOrderedBoundaries(long lower, long upper) {
    if (lower > upper) {
      throw new IllegalArgumentException(
          "Range inclusion parameter lower (" + lower + ") "
              + " should not be greater than upper (" + upper + ")");
    }
  }

  public void isEqualTo(Object other) {
    super.isEqualTo(other);
  }

  public void isNotEqualTo(Object other) {
    super.isNotEqualTo(other);
  }

  public void isEqualTo(int other) {
    isEqualTo((long) other);
  }

  public void is(int other) {
    isEqualTo((long) other);
  }

  public void is(short other) {
    isEqualTo((long) other);
  }

  public void is(byte other) {
    isEqualTo((long) other);
  }

  public static final SubjectFactory<LongSubject, Long> LONG =
      new SubjectFactory<LongSubject, Long>() {
        @Override public LongSubject getSubject(FailureStrategy fs, Long target) {
          return new LongSubject(fs, target);
        }
      };
}
