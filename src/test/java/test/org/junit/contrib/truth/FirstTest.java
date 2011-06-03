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
package test.org.junit.contrib.truth;
import static org.junit.Assert.fail;
import static org.junit.contrib.truth.Truth.ASSERT;
import static org.junit.contrib.truth.Truth.ASSUME;

import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FirstTest {
	@Test public void addition() {
		ASSERT.that(2 + 2).is(4);
	}
	
	@Test public void additionFail() {
		try {
			ASSERT.that(2 + 2).is(5);
		} catch (AssertionError expected) {
			ASSERT.that(expected.getMessage()).contains("Not true that <4> is <5>");
			return;
		}
		fail("Should have thrown");
	}

	@Test public void additionAssumptionFail() {
		try {
			ASSUME.that(2 + 2).is(5);
		} catch (AssumptionViolatedException expected) {
			return;
		}
		fail("Should have thrown");
	}

	@Test public void stringContainsFail() {
		try {
			ASSERT.that("abc").contains("d");
		} catch (AssertionError expected) {
			ASSERT.that(expected.getMessage()).contains("Not true that <abc> contains <d>");
			return;
		}
		fail("Should have thrown");
	}
	
	@Test public void chain() {
		ASSERT.that("abc").contains("a").contains("b");
	}
	
	@Test public void stringIs() {
		ASSERT.that("abc").is("abc");
	}
}
