/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0.subjects;


import org.truth0.FailureStrategy;

/**
 * Propositions for Integral numeric subjects
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
abstract public class AnIntegerSubject<S extends Subject<S,T>,T> extends Subject<S,T> {
  public AnIntegerSubject(FailureStrategy failureStrategy, T subject) {
    super(failureStrategy, subject);
  }

  abstract protected Long getSubjectAsLong();

  /**
   * Attests that a Subject<Integer> is inclusively within the {@code lower} and
   * {@code upper} bounds provided or fails.
   *
   * @throws IllegalArgumentException
   *           if the lower bound is greater than the upper.
   */
  public void isInclusivelyInRange(long lower, long upper) {
    ensureOrderedBoundaries(lower, upper);
    if (!(lower <= getSubjectAsLong() && getSubjectAsLong() <= upper)) {
      fail("is inclusively in range", lower, upper);
    }
  }

  /**
   * Attests that a Subject<Integer> is exclusively within the {@code lower} and
   * {@code upper} bounds provided or fails.
   *
   * @throws IllegalArgumentException
   *           if the lower bound is greater than the upper.
   */
  public void isBetween(long lower, long upper) {
    ensureOrderedBoundaries(lower, upper);
    if (!(lower < getSubjectAsLong() && getSubjectAsLong() < upper)) {
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

  public void isEqualTo(Integer other) {
    isEqualTo(other == null ? null : other.longValue());
  }

  public void isEqualTo(Short other) {
    isEqualTo(other == null ? null : other.longValue());
  }

  public void isEqualTo(Byte other) {
    isEqualTo(other == null ? null : other.longValue());
  }

  public void isEqualTo(Long other) {
    if (other == null) {
      if(getSubject() != null) {
        fail("is equal to", (Object)null);
      }
    } else {
      if (getSubject() == null || !other.equals(getSubjectAsLong())) {
        fail("is equal to", other);
      }
    }
  }

  public void isNotEqualTo(Integer other) {
    isNotEqualTo(other == null ? null : other.longValue());
  }

  public void isNotEqualTo(Short other) {
    isNotEqualTo(other == null ? null : other.longValue());
  }

  public void isNotEqualTo(Byte other) {
    isNotEqualTo(other == null ? null : other.longValue());
  }

  public void isNotEqualTo(Long other) {
    if (other == null) {
      if(getSubject() == null) {
        fail("is not equal to", (Object)null);
      }
    } else {
      if (getSubject() != null && other.equals(getSubjectAsLong())) {
        fail("is not equal to", other);
      }
    }
  }

  public void is(long other) {
    isEqualTo(other);
  }

  public void isGreaterThan(long other) {
    if (!(getSubjectAsLong() > other)) {
      fail("is greater than", other);
    }
  }

  public void isGreaterThanOrEqual(long other) {
    if (!(getSubjectAsLong() >= other)) {
      fail("is greater than or equal to", other);
    }
  }

  public void isLessThan(long other) {
    if (!(getSubjectAsLong() < other)) {
      fail("is less than", other);
    }
  }

  public void isLessThanOrEqual(long other) {
    if (!(getSubjectAsLong() <= other)) {
      fail("is less than or equal to", other);
    }
  }
}
