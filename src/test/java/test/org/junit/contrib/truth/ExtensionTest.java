package test.org.junit.contrib.truth;

import static org.junit.contrib.truth.extension.ExtendedVerb.ASSERT;

import java.util.Arrays;
import org.junit.Test;

public class ExtensionTest {	
	@Test public void listContains() {
		ASSERT.that(Arrays.asList(1, 2, 3)).contains(1);
		ASSERT.that(4).is(4);
	}
}
