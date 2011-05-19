package org.junit.contrib.truth;

public class TestVerb {
	private final FailureStrategy failureStrategy;

	public TestVerb(FailureStrategy failureStrategy) {
		this.failureStrategy = failureStrategy;
	}

	public IntSubject that(int i) {
		return new IntSubject(getFailureStrategy(), i);
	}

	public StringSubject that(String string) {
		return new StringSubject(getFailureStrategy(), string);
	}

	protected FailureStrategy getFailureStrategy() {
		return failureStrategy;
	}
}
