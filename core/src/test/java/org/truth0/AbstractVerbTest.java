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
package org.truth0;

import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Tests for Boolean Subjects.
 * 
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(Theories.class)
public class AbstractVerbTest {
	private String failureMessage = null;
	
	private AbstractVerb captureFailure = new AbstractVerb(new FailureStrategy() {		
		@Override
		public void fail(String message) {
			failureMessage = message;
		}
	});
	
	@DataPoints public static String[] strings = new String[] {"a", "b"};
	
	@Test public void noArgFail() {
		captureFailure.fail();
		ASSERT.that(failureMessage).is("");
	}
	
	@Theory public void argFail(String message) {
		captureFailure.fail(message);
		ASSERT.that(failureMessage).is(message);
	}
}
