/*
 * Copyright (c) 2018 Google, Inc.
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

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.makeMessage;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Fact}. */
@RunWith(JUnit4.class)
public class FactTest {
  @Test
  public void string() {
    assertThat(fact("foo", "bar").toString()).isEqualTo("foo: bar");
  }

  @Test
  public void stringWithoutValue() {
    assertThat(simpleFact("foo").toString()).isEqualTo("foo");
  }

  @Test
  public void oneFacts() {
    assertThat(makeMessage(ImmutableList.<String>of(), ImmutableList.of(fact("foo", "bar"))))
        .isEqualTo("foo: bar");
  }

  @Test
  public void twoFacts() {
    assertThat(
            makeMessage(
                ImmutableList.<String>of(),
                ImmutableList.of(fact("foo", "bar"), fact("longer name", "other value"))))
        .isEqualTo("foo        : bar\nlonger name: other value");
  }

  @Test
  public void oneFactWithoutValue() {
    assertThat(makeMessage(ImmutableList.<String>of(), ImmutableList.of(simpleFact("foo"))))
        .isEqualTo("foo");
  }

  @Test
  public void twoFactsOneWithoutValue() {
    assertThat(
            makeMessage(
                ImmutableList.<String>of(),
                ImmutableList.of(fact("hello", "there"), simpleFact("foo"))))
        .isEqualTo("hello: there\nfoo");
  }

  @Test
  public void newline() {
    assertThat(makeMessage(ImmutableList.<String>of(), ImmutableList.of(fact("foo", "bar\nbaz"))))
        .isEqualTo("foo:\n    bar\n    baz");
  }

  @Test
  public void newlineWithoutValue() {
    assertThat(
            makeMessage(
                ImmutableList.<String>of(),
                ImmutableList.of(fact("hello", "there\neveryone"), simpleFact("xyz"))))
        .isEqualTo("hello:\n    there\n    everyone\nxyz");
  }

  @Test
  public void withMessage() {
    assertThat(makeMessage(ImmutableList.<String>of("hello"), ImmutableList.of(fact("foo", "bar"))))
        .isEqualTo("hello\nfoo: bar");
  }
}
