package org.junit.contrib.truth;

public class Subject<T> {
	private final FailureStrategy failureStrategy;
	private final T subject;
	
	public Subject(FailureStrategy failureStrategy, T subject) {
		this.failureStrategy = failureStrategy;
		this.subject = subject;
	}

	public Subject<T> is(Object other) {
		if (!getSubject().equals(other)) {
			fail("is", other);
		}
		return this;
	}

	protected T getSubject() {
		return subject;
	}
	
	protected void fail(String verb, Object... messageParts) {
		String message = "Not true that ";
		message += "<" + getSubject() + "> " + verb;
		for (Object part : messageParts) {
			message += " <" + part + ">";
		}
		failureStrategy.fail(message);
	}
}
