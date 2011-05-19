package org.junit.contrib.truth;

import org.junit.internal.AssumptionViolatedException;

public class Truth {
	public static FailureStrategy THROW_ASSERTION_ERROR = new FailureStrategy() {
		@Override
		public void fail(String message) {
			throw new AssertionError(message);			
		}		
	};
	
	public static FailureStrategy THROW_ASSUMPTION_VIOLATED_EXCEPTION = new FailureStrategy() {		
		@Override
		public void fail(String message) {
			throw new AssumptionViolatedException(message);
		}
	};
	
	public static TestVerb ASSERT = new TestVerb(THROW_ASSERTION_ERROR);

	public static TestVerb ASSUME = new TestVerb(THROW_ASSUMPTION_VIOLATED_EXCEPTION);
}
