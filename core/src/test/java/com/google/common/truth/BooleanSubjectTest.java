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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Boolean Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class BooleanSubjectTest extends BaseSubjectTestCase {

  @Test
  public void isTrue() {
    assertThat(true).isTrue();
  }

  @Test
  public void nullIsTrueFailing() {
    expectFailureWhenTestingThat(null).isTrue();
    assertFailureKeys("expected", "but was");
    assertFailureValue("expected", "true");
    assertFailureValue("but was", "null");
  }

  @Test
  public void nullIsFalseFailing() {
    expectFailureWhenTestingThat(null).isFalse();
    assertFailureKeys("expected", "but was");
    assertFailureValue("expected", "false");
    assertFailureValue("but was", "null");
  }

  @Test
  public void isTrueFailing() {
    expectFailureWhenTestingThat(false).isTrue();
    assertFailureKeys("expected to be true");
  }

  @Test
  public void isFalse() {
    assertThat(false).isFalse();
  }

  @Test
  public void isFalseFailing() {
    expectFailureWhenTestingThat(true).isFalse();
    assertFailureKeys("expected to be false");
  }

  private BooleanSubject expectFailureWhenTestingThat(Boolean actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
