/*
 * Copyright (c) 2017 Google, Inc.
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

import junit.framework.TestCase;

/**
 * JUnit3 tests for {@link StackTraceCleaner}.
 *
 * <p>The "main" tests are in {@link StackTraceCleanerTest}.
 */
public class StackTraceCleanerJUnit3Test extends TestCase {
  public void testSimple() {
    try {
      assertThat(0).isEqualTo(1);
      throw new Error();
    } catch (AssertionError failure) {
      assertThat(failure.getStackTrace()).hasLength(1);
    }
  }
}
