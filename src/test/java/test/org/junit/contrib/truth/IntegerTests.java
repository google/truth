package test.org.junit.contrib.truth;
import static org.junit.Assert.fail;
import static org.junit.contrib.truth.Truth.ASSERT;
import static org.junit.contrib.truth.Truth.ASSUME;

import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

public class IntegerTests {
	@Test public void addition() {
		ASSERT.that(2 + 2).is(4);
	}
	
	@Test public void additionFail() {
		try {
			ASSERT.that(2 + 2).is(5);
		} catch (AssertionError expected) {
			ASSERT.that(expected.getMessage()).contains("Not true that <4> is <5>");
			return;
		}
		fail("Should have thrown");
	}

	@Test public void additionAssumptionFail() {
		try {
			ASSUME.that(2 + 2).is(5);
		} catch (AssumptionViolatedException expected) {
			return;
		}
		fail("Should have thrown");
	}

 @Test public void exclusiveRangeContainment() {
  ASSERT.that(4).isInclusivelyInRange(2,4);
 }

 
	
}
