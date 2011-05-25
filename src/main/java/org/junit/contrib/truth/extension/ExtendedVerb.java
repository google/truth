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
package org.junit.contrib.truth.extension;

import java.util.List;

import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.TestVerb;
import org.junit.contrib.truth.Truth;
import org.junit.contrib.truth.subjects.ListSubject;


public class ExtendedVerb extends TestVerb {
	public static ExtendedVerb ASSERT = new ExtendedVerb(Truth.THROW_ASSERTION_ERROR);
	
	public ExtendedVerb(FailureStrategy failureStrategy) {
		super(failureStrategy);
	}
	
	public <T> ListSubject<T> that(List<T> list) {
		return new ListSubject<T>(getFailureStrategy(), list);
	}
}