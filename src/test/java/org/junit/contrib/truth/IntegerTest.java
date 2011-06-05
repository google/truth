package org.junit.contrib.truth;

import static org.junit.Assert.fail;
import static org.junit.contrib.truth.Truth.ASSERT;
import static org.junit.contrib.truth.Truth.ASSUME;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.truth.Expect;
import org.junit.internal.AssumptionViolatedException;

public class IntegerTest {
  @Rule public Expect EXPECT = Expect.create();

  @Test public void addition() {
    ASSERT.that(2 + 2).is(4);
  }

  @Test public void additionFail() {
    try {
      ASSERT.that(2 + 2).is(5);
      fail("Should have thrown");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage()).contains("Not true that <4> is <5>");
    }
  }

  @Test public void additionAssumptionFail() {
    try {
      ASSUME.that(2 + 2).is(5);
      fail("Should have thrown");
    } catch (AssumptionViolatedException expected) {}
  }

  @Test public void inclusiveRangeContainment() {
    EXPECT.that(2).isInclusivelyInRange(2, 4);
    EXPECT.that(3).isInclusivelyInRange(2, 4);
    EXPECT.that(4).isInclusivelyInRange(2, 4);
  }

  @Test public void inclusiveRangeContainmentFailure() {
    try {
      ASSERT.that(1).isInclusivelyInRange(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {}
    try {
      ASSERT.that(5).isInclusivelyInRange(2, 4);
      fail("Should have thrown");
    } catch (AssertionError e) {}
  }

  @Test public void inclusiveRangeContainmentInversionError() {
    try {
      ASSERT.that(Integer.MAX_VALUE).isInclusivelyInRange(4, 2);
      fail("Should have thrown");
    } catch (IllegalArgumentException e) {}
  }

  @Test public void exclusiveRangeContainment() {
    EXPECT.that(3).isBetween(2, 5);
    EXPECT.that(4).isBetween(2, 5);
  }

  @Test public void exclusiveRangeContainmentFailure() {
    try {
      ASSERT.that(Integer.MAX_VALUE).isBetween(2, 5);
      fail("Should have thrown");
    } catch (AssertionError e) {}
  }

  @Test public void exclusiveRangeContainmentInversionError() {
    try {
      ASSERT.that(Integer.MAX_VALUE).isBetween(5, 2);
      fail("Should have thrown");
    } catch (IllegalArgumentException e) {}
  }

}
