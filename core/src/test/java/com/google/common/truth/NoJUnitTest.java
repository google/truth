/*
 * Copyright (c) 2021 Google, Inc.
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

import com.google.common.collect.ImmutableList;

/** Truth-using binary to be run without JUnit on the classpath to verify that it still works. */
public final class NoJUnitTest {
  public static void main(String[] args) {
    try {
      assertThat("a").isEqualTo("b");
      throw new Error("assertion should have failed");
    } catch (AssertionError expected) {
      ImmutableList<Fact> facts = ((AssertionErrorWithFacts) expected).facts();
      assertThat(facts.get(0).key).isEqualTo("expected");
      assertThat(facts.get(0).value).isEqualTo("b");
      assertThat(facts.get(1).key).isEqualTo("but was");
      assertThat(facts.get(1).value).isEqualTo("a");
    }
  }

  private NoJUnitTest() {}
}
