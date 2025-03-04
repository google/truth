/*
 * Copyright (c) 2014 Google, Inc.
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
import static com.google.common.truth.FailureAssertions.assertFailureValueIndexed;
import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link com.google.common.truth.PrimitiveLongArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
// We intentionally test mismatches.
// TODO(cpovirk): Maybe suppress at a finer scope.
@SuppressWarnings("TruthIncompatibleType")
public class PrimitiveLongArraySubjectTest {

  @Test
  public void isEqualTo() {
    assertThat(array(2L, 5)).isEqualTo(array(2L, 5));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_Same() {
    long[] same = array(2L, 5);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array(5, 2, 9)).asList().containsAtLeast(2L, 9L);
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array(2, 3)).isEqualTo(array(3, 2)));
    assertFailureKeys(e, "expected", "but was", "differs at index");
    assertFailureValue(e, "expected", "[3, 2]");
    assertFailureValue(e, "but was", "[2, 3]");
    assertFailureValue(e, "differs at index", "[0]");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array(2, 3, 4)).isEqualTo(new int[] {}));
    assertFailureKeys(e, "expected", "but was", "wrong type", "expected", "but was");
    assertFailureValueIndexed(e, "expected", 1, "int[]");
    assertFailureValueIndexed(e, "but was", 1, "long[]");
  }

  @Test
  public void isNotEqualTo_SameLengths() {
    assertThat(array(2, 3)).isNotEqualTo(array(3, 2));
  }

  @Test
  public void isNotEqualTo_DifferentLengths() {
    assertThat(array(2, 3)).isNotEqualTo(array(2, 3, 1));
  }

  @Test
  public void isNotEqualTo_DifferentTypes() {
    assertThat(array(2, 3)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_FailEquals() {
    expectFailure(whenTesting -> whenTesting.that(array(2, 3)).isNotEqualTo(array(2, 3)));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    long[] same = array(2, 3);
    expectFailure(whenTesting -> whenTesting.that(same).isNotEqualTo(same));
  }

  private static long[] array(long... ts) {
    return ts;
  }
}
