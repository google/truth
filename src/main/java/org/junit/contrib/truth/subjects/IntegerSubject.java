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
import org.junit.contrib.truth.util.GwtCompatible;

/**
 * Propositions for Integral numeric subjects
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@GwtCompatible
public class IntegerSubject extends Subject<IntegerSubject, Long> {

  public IntegerSubject(FailureStrategy failureStrategy, Long i) {
    super(failureStrategy, i);
  }

  public IntegerSubject(FailureStrategy failureStrategy, Integer i) {
    super(failureStrategy, i == null ? null : Long.valueOf(i.longValue()));
  }

  /**
   * Attests that a Subject<Integer> is inclusively within the {@code lower} and
   * {@code upper} bounds provided or fails.
   *
   * @throws IllegalArgumentException
   *           if the lower bound is greater than the upper.
   */
  public And<IntegerSubject> isInclusivelyInRange(long lower, long upper) {
    ensureOrderedBoundaries(lower, upper);
    if (!(lower <= getSubject() && getSubject() <= upper)) {
      fail("is inclusively in range", lower, upper);
    }
    return nextChain();
  }

  /**
   * Attests that a Subject<Integer> is exclusively within the {@code lower} and
   * {@code upper} bounds provided or fails.
   *
   * @throws IllegalArgumentException
   *           if the lower bound is greater than the upper.
   */
  public And<IntegerSubject> isBetween(long lower, long upper) {
    ensureOrderedBoundaries(lower, upper);
    if (!(lower < getSubject() && getSubject() < upper)) {
      fail("is in between", lower, upper);
    }
    return nextChain();
  }

  /**
   * Guards against inverted lower/upper boundaries, and throws if
   * they are so inverted.
   */
  private void ensureOrderedBoundaries(long lower, long upper) {
    if (lower > upper) {
      throw new IllegalArgumentException(
          "Range inclusion parameter lower (" + lower + ") "
              + " should not be greater than upper (" + upper + ")");
    }
  }

  public And<IntegerSubject> isEqualTo(Integer other) {
    return isEqualTo((other == null) ? null : Long.valueOf(other.longValue()));
  }

  public And<IntegerSubject> isEqualTo(Long other) {
    if (getSubject() == null) {
      if(other != null) {
        fail("is equal to", other);
      }
    } else {
      if (!getSubject().equals(other)) {
        fail("is equal to", other);
      }
    }
    return nextChain();
  }

  public And<IntegerSubject> isNotEqualTo(Integer other) {
    return isNotEqualTo((other == null) ? null : Long.valueOf(other.longValue()));
  }

  public And<IntegerSubject> isNotEqualTo(Long other) {
    if (getSubject() == null) {
      if(other == null) {
        fail("is not equal to", other);
      }
    } else {
      // Coerce to a long.
      if (Long.valueOf(getSubject().longValue()).equals(other)) {
        fail("is not equal to", other);
      }
    }
    return nextChain();
  }

  public And<IntegerSubject> is(int other) {
    return super.is((long)other);
  }

  public And<IntegerSubject> is(short other) {
    return super.is((long)other);
  }

  public And<IntegerSubject> is(byte other) {
    return super.is((long)other);
  }


  public static final SubjectFactory<IntegerSubject, Long> INTEGER =
      new SubjectFactory<IntegerSubject, Long>() {
        @Override public IntegerSubject getSubject(FailureStrategy fs, Long target) {
          return new IntegerSubject(fs, target);
        }
      };
}
