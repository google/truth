package test.org.junit.contrib.truth;
import static org.junit.Assert.fail;
import static org.junit.contrib.truth.Truth.ASSERT;
import static org.junit.contrib.truth.Truth.ASSUME;

import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

public class FirstTest {
	@Test public void addition() {
		ASSERT.that(2 + 2).is(4);
	}
	
	@Test public void additionFail() {
		try {
			ASSERT.that(2 + 2).is(5);
		} catch (AssertionError expected) {
			ASSERT.that(expected.getMessage()).contains("Not true: <4> is <5>");
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

	@Test public void stringContainsFail() {
		try {
			ASSERT.that("abc").contains("d");
		} catch (AssertionError expected) {
			ASSERT.that(expected.getMessage()).contains("Not true: <abc> contains <d>");
			return;
		}
		fail("Should have thrown");
	}
	
	@Test public void chain() {
		ASSERT.that("abc").contains("a").contains("b");
	}
	
	@Test public void stringIs() {
		ASSERT.that("abc").is("abc");
	}
}
