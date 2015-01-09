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

import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.math.BigDecimal;

/**
 * Propositions for BigDecimal subjects. Supports matching against String's representing BigDecimals as a programmer convenience.
 *
 * @author Richard Friend
 */

public class BigDecimalSubject extends Subject<BigDecimalSubject, BigDecimal> {

    private static final int EQUAL = 0;

    public BigDecimalSubject(FailureStrategy failureStrategy, BigDecimal subject) {
        super(failureStrategy, subject);
    }

    public void is(String other) {
        isEqualTo(new BigDecimal(other));
    }

    public void is(BigDecimal other) {
        isEqualTo((other));
    }

    public void isEqualTo(BigDecimal other) {
        if (getSubject() == null) {
            if (other != null) {
                fail("is equal to", other);
            }
        } else {
            if (other == null || !equals(getSubject(), other)) {
                fail("is equal to", other);
            }
        }
    }

    public void isEqualTo(String other) {
        isEqualTo(new BigDecimal(other));
    }


    public void isNotEqualTo(BigDecimal other) {
        if (getSubject() == null) {
            if (other == null) {
                fail("is not equal to", (BigDecimal) null);
            }
        } else {
            if (other != null && equals(getSubject(), other)) {
                fail("is not equal to", other);
            }
        }
    }

    public void isNotEqualTo(String other) {
        isNotEqualTo(new BigDecimal(other));
    }

    /**
     * Attests that a Subject<BigDecimal> is inclusively within the {@code lower} and
     * {@code upper} bounds provided or fails.
     *
     * @throws IllegalArgumentException if the lower bound is greater than the upper.
     */
    public void isInclusivelyInRange(BigDecimal lower, BigDecimal upper) {
        if (!Range.closed(lower, upper).contains(getSubject())) {
            fail("is inclusively in range", lower, upper);
        }
    }

    /**
     * Attests that a Subject<BigDecimal> is inclusively within the {@code lower} and
     * {@code upper} bounds provided or fails.
     *
     * @throws IllegalArgumentException if the lower bound is greater than the upper.
     */
    public void isInclusivelyInRange(String lower, String upper) {
        isInclusivelyInRange(new BigDecimal(lower), new BigDecimal(upper));
    }

    /**
     * Attests that a Subject<BigDecimal> is exclusively within the {@code lower} and
     * {@code upper} bounds provided or fails.
     *
     * @throws IllegalArgumentException if the lower bound is greater than the upper.
     */
    public void isBetween(BigDecimal lower, BigDecimal upper) {
        if (!Range.open(lower, upper).contains(getSubject())) {
            fail("is in between", lower, upper);
        }
    }

    /**
     * Attests that a Subject<BigDecimal> is exclusively within the {@code lower} and
     * {@code upper} bounds provided or fails.
     *
     * @throws IllegalArgumentException if the lower bound is greater than the upper.
     */
    public void isBetween(String lower, String upper) {
        isBetween(new BigDecimal(lower), new BigDecimal(upper));
    }


    /**
     * Attests that a Subject<BigDecimal> is within the {@code offset} provided or fails.
     *
     * @throws IllegalArgumentException if the lower bound is greater than the upper.
     */
    public void withinOffset(BigDecimal other, BigDecimal offset) {
        BigDecimal lower = other.subtract(offset);
        BigDecimal upper = other.add(offset);
        if (!Range.closed(lower, upper).contains(getSubject())) {
            fail("is in range", lower, upper);
        }
    }

    /**
     * Attests that a Subject<BigDecimal> is within the {@code offset} provided or fails.
     *
     * @throws IllegalArgumentException if the lower bound is greater than the upper.
     */
    public void withinOffset(String other, String offset) {
        withinOffset(new BigDecimal(other), new BigDecimal(offset));
    }

    private boolean equals(BigDecimal a, BigDecimal b) {
        return Ordering.natural().compare(a, b) == EQUAL;
    }
}
