/*
 * Copyright (c) 2011 Google, Inc.
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
package com.google.common.truth;


/**
 * Truth - a proposition framework for tests, supporting JUnit style
 * assertion and assumption semantics in a fluent style.
 *
 * Truth is the simplest entry point class. A developer can statically
 * import the assert_() method to get easy access to the library's
 * capabilities. Then, instead of writing:
 *
 * <pre>{@code
 * Assert.assertEquals(a,b);
 * Assert.assertTrue(c);
 * Assert.assertTrue(d.contains(a) && d.contains(e));
 * Assert.assertTrue(d.contains(a) || d.contains(q) || d.contains(z));
 * }</pre>
 * one would write:
 * <pre>{@code
 * assert_().that(aString).equals(bString);
 * assert_().that(aBoolean).isTrue();
 * assert_().that(collection).has().item(aString);
 * assert_().that(collection).containsAllOf(aString, bString);
 * assert_().that(collection).containsAnyOf(aString, qString, zString);
 * }</pre>
 *
 * Tests should be easier to read, and flow more clearly.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class Truth {

  public static final FailureStrategy THROW_ASSERTION_ERROR =
      new FailureStrategy() {
        @Override public void failComparing(
            String message, CharSequence expected, CharSequence actual) {
          throw Platform.comparisonFailure(message, expected.toString(), actual.toString());
        }
      };

  private static final TestVerb ASSERT = new TestVerb(THROW_ASSERTION_ERROR);

  /* @deprecated prefer {@link com.google.common.truth.Truth#assert_()}. */
  public static TestVerb assert_() { return ASSERT; }

}
