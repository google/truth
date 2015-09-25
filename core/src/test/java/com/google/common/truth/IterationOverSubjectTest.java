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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;
import static com.google.common.truth.delegation.FooSubject.foo;

import com.google.common.truth.delegation.Foo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

/**
 * Tests for {@code assert_().in(data).thatEach(subject())}.
 *
 * @author David Saff
 * @author Christian Gruber
 */
@RunWith(JUnit4.class)
public class IterationOverSubjectTest {
  @Test
  public void collectionPropositionWithMultipleArguments() {
    Iterable<Foo> data = Arrays.asList(new Foo(2 + 3), new Foo(2 + 4));
    assert_().in(data).thatEach(foo()).matchesAny(new Foo(5), new Foo(6));
    try {
      assert_().in(data).thatEach(foo()).matchesAny(new Foo(6), new Foo(7));
      assert_().fail("Expected assertion to fail on element 1.");
    } catch (AssertionError expected) {
      assertThat(expected).hasMessage("Not true that <Foo(5)> matches <[Foo(6), Foo(7)]>");
    }
  }
}
