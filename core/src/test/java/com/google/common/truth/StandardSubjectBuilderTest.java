/*
 * Copyright (c) 2019 Google, Inc.
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

import static com.google.common.truth.Truth.assert_;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link StandardSubjectBuilder}. */
@RunWith(JUnit4.class)
public final class StandardSubjectBuilderTest extends BaseSubjectTestCase {
  @Test
  public void failNoMessage() {
    expectFailure.whenTesting().fail();
    assertThatFailure().hasMessageThat().isEmpty();
  }

  @Test
  public void failWithMessage() {
    expectFailure.whenTesting().fail("at index %s", 1);
    assertThatFailure().hasMessageThat().isEqualTo("at index 1");
  }

  @Test
  public void failNullMessage() {
    try {
      assert_().fail(null);
      throw new AssertionError("should have thrown NullPointerException");
    } catch (NullPointerException expected) {
    }
  }
}
