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
package org.truth0.delegatetest;

import static org.truth0.Truth.ASSERT;
import static org.truth0.delegatetest.FooSubject.FOO;

import org.junit.Test;

/**
 * A test that's more or less intended to show how one uses an extended verb.
 *
 */
public class DelegationTest {

  @Test public void customTypeProposition() {
    ASSERT.about(FOO).that(new Foo(5)).matches(new Foo(2 + 3));
  }

  @Test public void customTypePropositionWithFailure() {
    try {
      ASSERT.about(FOO).that(new Foo(5)).matches(new Foo(4));
      ASSERT.fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("matches");
    }
  }
}
