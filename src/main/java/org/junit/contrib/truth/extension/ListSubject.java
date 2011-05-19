package org.junit.contrib.truth.extension;

import java.util.List;

import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.Subject;

public class ListSubject<T> extends Subject<List<T>> {
	public ListSubject(FailureStrategy failureStrategy, List<T> list) {
		super(failureStrategy, list);
	}

	public ListSubject<T> contains(Object item) {
		if (! getSubject().contains(item)) {
			fail("contains", item);
		}
		return this;
	}
}
