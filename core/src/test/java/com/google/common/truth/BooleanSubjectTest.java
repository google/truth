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

import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Boolean Subjects.
 */
@RunWith(JUnit4.class)
public class BooleanSubjectTest {

  @Test
  public void isTrue() {
    assertThat(true).isTrue();
  }

  @Test
  public void nullIsTrueFailing() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Boolean) null).isTrue());
    assertFailureKeys(e, "expected", "but was");
    assertFailureValue(e, "expected", "true");
    assertFailureValue(e, "but was", "null");
  }

  @Test
  public void nullIsFalseFailing() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Boolean) null).isFalse());
    assertFailureKeys(e, "expected", "but was");
    assertFailureValue(e, "expected", "false");
    assertFailureValue(e, "but was", "null");
  }

  @Test
  public void isTrueFailing() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(false).isTrue());
    assertFailureKeys(e, "expected to be true");
  }

  @Test
  public void isFalse() {
    assertThat(false).isFalse();
  }

  @Test
  public void isFalseFailing() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(true).isFalse());
    assertFailureKeys(e, "expected to be false");
  }
}
