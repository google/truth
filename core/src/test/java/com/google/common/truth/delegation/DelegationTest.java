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
package com.google.common.truth.delegation;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth.assert_;
import static com.google.common.truth.delegation.FooSubject.foo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** A test that's more or less intended to show how one uses an extended verb. */
@RunWith(JUnit4.class)
public class DelegationTest {
  @Test
  public void assertAboutThat() {
    assertAbout(foo()).that(new Foo(5)).matches(new Foo(2 + 3));
  }

  @Test
  public void assertAboutThatFailure() {
    try {
      assertAbout(foo()).that(new Foo(5)).matches(new Foo(4));
    } catch (AssertionError e) {
      assertThat(e).hasMessageThat().contains("Not true that");
      assertThat(e).hasMessageThat().contains("matches");
      return;
    }
    assert_().fail("Should have thrown.");
  }

  @Test
  public void customTypeProposition() {
    assertAbout(foo()).that(new Foo(5)).matches(new Foo(2 + 3));
  }

  @Test
  public void customTypePropositionWithFailureMessage() {
    try {
      assertWithMessage("failureMessage").about(foo()).that(new Foo(5)).matches(new Foo(4));
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("failureMessage: Not true that <Foo(5)> matches <Foo(4)>");
      return;
    }
    assert_().fail("Should have thrown.");
  }
}
