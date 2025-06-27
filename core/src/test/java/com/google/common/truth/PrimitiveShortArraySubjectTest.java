/*
 * Copyright (c) 2015 Google, Inc.
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
 * Tests for {@link PrimitiveShortArraySubject}.
 */
@RunWith(JUnit4.class)
public class PrimitiveShortArraySubjectTest {

  @Test
  public void isEqualTo() {
    assertThat(array(1, 0, 1)).isEqualTo(array(1, 0, 1));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_same() {
    short[] same = array(1, 0, 1);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array(1, 1, 0)).asList().containsAtLeast((short) 1, (short) 0);
  }

  @Test
  public void asListWithoutCastingFails() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(array(1, 1, 0)).asList().containsAtLeast(1, 0));
    assertFailureKeys(
        e,
        "value of",
        "missing (2)",
        "though it did contain (3)",
        "---",
        "expected to contain at least",
        "but was");
  }

  @Test
  public void isEqualTo_fail_unequalOrdering() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array(1, 0, 1)).isEqualTo(array(0, 1, 1)));
    assertFailureKeys(e, "expected", "but was", "differs at index");
    assertFailureValue(e, "expected", "[0, 1, 1]");
    assertFailureValue(e, "but was", "[1, 0, 1]");
    assertFailureValue(e, "differs at index", "[0]");
  }

  @Test
  public void isEqualTo_fail_notAnArray() {
    expectFailure(whenTesting -> whenTesting.that(array(1, 0, 1)).isEqualTo(new Object()));
  }

  @Test
  public void isNotEqualTo_sameLengths() {
    assertThat(array(1, 0)).isNotEqualTo(array(1, 1));
  }

  @Test
  public void isNotEqualTo_differentLengths() {
    assertThat(array(1, 0)).isNotEqualTo(array(1, 0, 1));
  }

  @Test
  public void isNotEqualTo_differentTypes() {
    assertThat(array(1, 0)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_failEquals() {
    expectFailure(whenTesting -> whenTesting.that(array(1, 0)).isNotEqualTo(array(1, 0)));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_failSame() {
    short[] same = array(1, 0);
    expectFailure(whenTesting -> whenTesting.that(same).isNotEqualTo(same));
  }

  @Test
  public void hasLengthNullArray() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((short[]) null).hasLength(1));
    assertFailureKeys(e, "expected an array with length", "but was");
    assertFailureValue(e, "expected an array with length", "1");
  }

  private static short[] array(int a, int b, int c) {
    return new short[] {(short) a, (short) b, (short) c};
  }

  private static short[] array(int a, int b) {
    return new short[] {(short) a, (short) b};
  }
}
