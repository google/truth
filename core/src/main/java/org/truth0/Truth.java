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

import org.junit.internal.AssumptionViolatedException;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;

/**
 * Truth - a proposition framework for tests, supporting JUnit style 
 * assertion and assumption semantics in a fluent style.
 * 
 * Truth is the simplest entry point class. A developer can statically
 * import the ASSERT and ASSUME constants to get easy access to the 
 * library's capabilities. Then, instead of writing:
 * <pre>{@code 
 * Assert.assertEquals(a,b);
 * Assert.assertTrue(c);
 * Assert.assertTrue(d.contains(a) && d.contains(e));
 * Assert.assertTrue(d.contains(a) || d.contains(q) || d.contains(z));
 * }</pre>
 * one would write:
 * <pre>{@code 
 * ASSERT.that(a).equals(b);
 * ASSERT.that(c).isTrue();
 * ASSERT.that(d).contains(a).and().contains(b);
 * // or
 * ASSERT.that(d).containsAllOf(a, b);
 * ASSERT.that(d).containsAnyOf(a, q, z);
 * }</pre>
 * 
 * Tests should be easier to read, and flow more clearly.
 * 
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@GwtCompatible(emulated = true)
public class Truth {
  public static final FailureStrategy THROW_ASSERTION_ERROR = 
      new FailureStrategy() {
        @Override public void fail(String message) {
          throw new AssertionError(message);
        }
      };

  @GwtIncompatible("JUnit4")
  public static final FailureStrategy THROW_ASSUMPTION_ERROR = 
      new FailureStrategy() {
        @Override public void fail(String message) {
          throw new AssumptionViolatedException(message);
        }
      };

  public static final TestVerb ASSERT = new TestVerb(THROW_ASSERTION_ERROR);

  @GwtIncompatible("JUnit4")
  public static final TestVerb ASSUME = new TestVerb(THROW_ASSUMPTION_ERROR);
}
