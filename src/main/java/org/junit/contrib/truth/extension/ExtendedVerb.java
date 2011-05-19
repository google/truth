package org.junit.contrib.truth.extension;

import java.util.List;

import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.TestVerb;
import org.junit.contrib.truth.Truth;


public class ExtendedVerb extends TestVerb {
	public static ExtendedVerb ASSERT = new ExtendedVerb(Truth.THROW_ASSERTION_ERROR);
	
	public ExtendedVerb(FailureStrategy failureStrategy) {
		super(failureStrategy);
	}
	
	public <T> ListSubject<T> that(List<T> list) {
		return new ListSubject<T>(getFailureStrategy(), list);
	}
}