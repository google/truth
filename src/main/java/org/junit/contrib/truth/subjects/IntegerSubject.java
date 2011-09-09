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
package org.junit.contrib.truth.subjects;

import org.junit.contrib.truth.FailureStrategy;

/**
 * Propositions for Integral numeric subjects
 * 
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class IntegerSubject extends Subject<Long> {

  private static final String RANGE_BOUNDS_OUT_OF_ORDER_MSG = "Range inclusion parameter lower (%d) should not be greater than upper (%d)";

  public IntegerSubject(FailureStrategy failureStrategy, Long i) {
    super(failureStrategy, i);
  }
  
  public IntegerSubject(FailureStrategy failureStrategy, Integer i) {
    super(failureStrategy, i == null ? null : new Long(i.longValue()));
  }

  /**
   * Attests that a Subject<Integer> is inclusively within the {@code lower} and
   * {@code upper} bounds provided or fails.
   * 
   * @throws IllegalArgumentException
   *           if the lower bound is greater than the upper.
   */
  public IntegerSubject isInclusivelyInRange(long lower, long upper) {
    ensureOrderedBoundaries(lower, upper);
    if (!(lower <= getSubject() && getSubject() <= upper)) {
      fail("is inclusively in range", lower, upper);
    }
    return this;
  }

  /**
   * Attests that a Subject<Integer> is exclusively within the {@code lower} and
   * {@code upper} bounds provided or fails.
   * 
   * @throws IllegalArgumentException
   *           if the lower bound is greater than the upper.
   */
  public IntegerSubject isBetween(long lower, long upper) {
    ensureOrderedBoundaries(lower, upper);
    if (!(lower < getSubject() && getSubject() < upper)) {
      fail("is in between", lower, upper);
    }
    return this;
  }

  /**
   * Guards against inverted lower/upper boundaries, and throws if 
   * they are so inverted.
   */
  private void ensureOrderedBoundaries(long lower, long upper) {
    if (lower > upper) {
      throw new IllegalArgumentException(String.format(
          RANGE_BOUNDS_OUT_OF_ORDER_MSG, lower, upper));
    }
  }

  public IntegerSubject isEqualTo(Integer other) {
    return isEqualTo((other == null) ? null : new Long(other.longValue()));
  }
  
  public IntegerSubject isEqualTo(Long other) {
    if (getSubject() == null) { 
      if(other != null) {
        fail("is equal to", other);
      }
    } else {
      // Coerce to a long.
      if (!new Long(getSubject().longValue()).equals(other)) {
        fail("is equal to", other);
      }
    }
    return this;
  }

  public IntegerSubject isNotEqualTo(Integer other) {
    return isNotEqualTo((other == null) ? null : new Long(other.longValue()));
  }

  public IntegerSubject isNotEqualTo(Long other) {
    if (getSubject() == null) { 
      if(other == null) {
        fail("is not equal to", other);
      }
    } else {
      // Coerce to a long.
      if (new Long(getSubject().longValue()).equals(other)) {
        fail("is not equal to", other);
      }
    }
    return this;
  }
  
  
}
