package test.org.junit.contrib.truth;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.truth.Expect;

public class ExpectTest {
	@Rule public Expect EXPECT = Expect.create();
	
	@Test public void expectTrue() {
		EXPECT.that(4).is(4);
	}
	
	@Ignore @Test public void expectFail() {
		EXPECT.that("abc").contains("x").contains("y").contains("z");
	}
}
