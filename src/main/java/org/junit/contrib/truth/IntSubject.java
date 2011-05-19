package org.junit.contrib.truth;

public class IntSubject extends Subject<Integer> {
	public IntSubject(FailureStrategy failureStrategy, int i) {
		super(failureStrategy, i);
	}
}
