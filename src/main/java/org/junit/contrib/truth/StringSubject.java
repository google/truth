package org.junit.contrib.truth;

public class StringSubject extends Subject<String> {
	public StringSubject(FailureStrategy failureStrategy, String string) {
		super(failureStrategy, string);
	}

	public StringSubject contains(String string) {
		if (!getSubject().contains(string)) {
			fail("contains", string);
		}
		return this;
	}

}
