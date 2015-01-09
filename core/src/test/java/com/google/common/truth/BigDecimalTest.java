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

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;

import static com.google.common.truth.TruthJUnit.assume;
import static com.google.common.truth.Truth.assert_;

import static org.junit.Assert.fail;

/**
 * Tests for BigDecimal Subjects.
 *
 * @author Richard Friend
 */
@RunWith(JUnit4.class)
public class BigDecimalTest {
    @Rule public final Expect EXPECT = Expect.create();

    @Test public void simpleEquality() {
        assert_().that(new BigDecimal("1.2")).is(new BigDecimal("1.2"));
        assert_().that(new BigDecimal("1.2")).is("1.2");
    }

    @Test public void equalityWithDifferentScales() {
        assert_().that(new BigDecimal("1")).is(new BigDecimal("1.0000"));
        assert_().that(new BigDecimal("2.000")).is("2.00");
    }

    @Test public void simpleInequality() {
        assert_().that(new BigDecimal("1.2")).isNotEqualTo("1.3");
        assert_().that(new BigDecimal("1.2")).isNotEqualTo(new BigDecimal("1.3"));
    }

    @Test public void equalityFail() {
        try {
            assert_().that(new BigDecimal("4.12")).isEqualTo(new BigDecimal("4.13"));
            fail("Should have thrown");
        } catch (AssertionError expected) {
            assert_().that(expected.getMessage()).contains("Not true that <4.12> is equal to <4.13>");
        }
    }

    @Test public void equalityFailStringOther() {
        try {
            assert_().that(new BigDecimal("4.12")).isEqualTo("4.13");
            fail("Should have thrown");
        } catch (AssertionError expected) {
            assert_().that(expected.getMessage()).contains("Not true that <4.12> is equal to <4.13>");
        }
    }

    @Test public void inequalityFail() {
        try {
            assert_().that(new BigDecimal("4")).isNotEqualTo(new BigDecimal("4"));
            fail("Should have thrown");
        } catch (AssertionError expected) {
            assert_().that(expected.getMessage()).contains("Not true that <4> is not equal to <4>");
        }
    }

    @Test public void inequalityFailStringOther() {
        try {
            assert_().that(new BigDecimal("4")).isNotEqualTo("4");
            fail("Should have thrown");
        } catch (AssertionError expected) {
            assert_().that(expected.getMessage()).contains("Not true that <4> is not equal to <4>");
        }
    }

    @Test public void assumptionFail() {
        try {
            assume().that(new BigDecimal("4")).isEqualTo(5);
            fail("Should have thrown");
        } catch (AssumptionViolatedException expected) {
        }
    }

    @Test public void inclusiveRangeContainment() {
        EXPECT.that(new BigDecimal("2")).isInclusivelyInRange("1", "3");
        EXPECT.that(new BigDecimal("3")).isInclusivelyInRange("2", "4");
        EXPECT.that(new BigDecimal("4")).isInclusivelyInRange("2", "4");
    }

    @Test public void inclusiveRangeContainmentFailure() {
        try {
            assert_().that(new BigDecimal("1")).isInclusivelyInRange("2", "4");
            fail("Should have thrown");
        } catch (AssertionError e) {
            assert_().that(e.getMessage()).contains("Not true that <1> is inclusively in range <2> <4>");
        }
        try {
            assert_().that(new BigDecimal("5")).isInclusivelyInRange("2", "4");
            fail("Should have thrown");
        } catch (AssertionError e) {
            assert_().that(e.getMessage()).contains("Not true that <5> is inclusively in range <2> <4>");
        }
    }

    @Test public void inclusiveRangeContainmentInversionError() {
        try {
            assert_().that(BigDecimal.ONE).isInclusivelyInRange("4", "2");
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test public void exclusiveRangeContainment() {
        EXPECT.that(new BigDecimal(3)).isBetween("2", "5");
        EXPECT.that(new BigDecimal(4)).isBetween("2", "5");
    }

    @Test public void exclusiveRangeContainmentFailure() {
        try {
            assert_().that(new BigDecimal("5")).isBetween("2", "5");
            fail("Should have thrown");
        } catch (AssertionError e) {
            assert_().that(e.getMessage()).contains("Not true that <5> is in between <2> <5>");
        }
    }

    @Test public void exclusiveRangeContainmentInversionError() {
        try {
            assert_().that(BigDecimal.ONE).isBetween("5", "2");
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test public void equalityOfNulls() {
        assert_().that((BigDecimal) null).isEqualTo((BigDecimal) null);
    }

    @Test public void equalityOfNullsFail() {
        try {
            assert_().that((BigDecimal) null).isEqualTo(5);
            fail("Should have thrown");
        } catch (AssertionError e) {
            assert_().that(e.getMessage()).contains("Not true that <null> is equal to <5>");
        }
        try {
            assert_().that(new BigDecimal(5)).isEqualTo((BigDecimal) null);
            fail("Should have thrown");
        } catch (AssertionError e) {
            assert_().that(e.getMessage()).contains("Not true that <5> is equal to <null>");
        }
    }

    @Test public void inequalityOfNulls() {
        assert_().that((BigDecimal) null).isNotEqualTo("4");
        assert_().that(new BigDecimal(4)).isNotEqualTo((BigDecimal) null);
    }

    @Test public void equalityWithNull() {
        try {
            assert_().that(new BigDecimal("1.2")).is((BigDecimal) null);
        } catch (AssertionError expected) {
            assert_().that(expected.getMessage()).contains("Not true that <1.2> is equal to <null>");
        }
    }
    @Test public void equalityWithNullSubject() {
        try {
            assert_().that((BigDecimal) null).is(new BigDecimal("1.2"));
        } catch (AssertionError expected) {
            assert_().that(expected.getMessage()).contains("Not true that <null> is equal to <1.2>");
        }
    }

    @Test public void inequalityOfNullsFail() {
        try {
            assert_().that((BigDecimal) null).isNotEqualTo((BigDecimal) null);
            fail("Should have thrown");
        } catch (AssertionError e) {
            assert_().that(e.getMessage()).contains("Not true that <null> is not equal to <null>");
        }
    }

    @Test public void closeTo(){
        assert_().that(new BigDecimal("5.5")).withinOffset("5.0", "0.5");
        assert_().that(new BigDecimal("5.40")).withinOffset("5.00", "0.500");
    }

    @Test public void closeToFails(){
        try {
            assert_().that(new BigDecimal("5.501")).withinOffset(new BigDecimal("5.00"), new BigDecimal("0.500"));
            fail("Should have thrown");
        } catch (AssertionError e) {
            assert_().that(e.getMessage()).contains("Not true that <5.501> is in range <4.500> <5.500>");
        }
    }
}
