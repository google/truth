/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.junit.contrib.truth.subjects;

import org.junit.contrib.truth.FailureStrategy;

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
