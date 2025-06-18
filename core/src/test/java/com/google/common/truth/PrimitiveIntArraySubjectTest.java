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
import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link com.google.common.truth.PrimitiveIntArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveIntArraySubjectTest {
  private static final int[] EMPTY = new int[0];

  @Test
  public void isEqualTo() {
    assertThat(array(2, 5)).isEqualTo(array(2, 5));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_same() {
    int[] same = array(2, 5);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array(5, 2, 9)).asList().containsAtLeast(2, 9);
  }

  @Test
  public void hasLength() {
    assertThat(EMPTY).hasLength(0);
    assertThat(array(2, 5)).hasLength(2);
  }

  @Test
  public void hasLengthFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(array(2, 5)).hasLength(1));
    assertFailureValue(e, "value of", "array.length");
  }

  @Test
  public void hasLengthNegative() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(array(2, 5)).hasLength(-1));
    assertFailureKeys(
        e,
        "could not perform length check because expected length is negative",
        "expected length",
        "array was");
    assertFailureValue(e, "expected length", "-1");
    assertFailureValue(e, "array was", "[2, 5]");
  }

  @Test
  public void hasLengthNullArray() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((int[]) null).hasLength(1));
    assertFailureKeys(e, "expected an array with length", "but was");
    assertFailureValue(e, "expected an array with length", "1");
  }

  @Test
  public void isEmpty() {
    assertThat(EMPTY).isEmpty();
  }

  @Test
  public void isEmptyFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(array(2, 5)).isEmpty());
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void isNotEmpty() {
    assertThat(array(2, 5)).isNotEmpty();
  }

  @Test
  public void isNotEmptyFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(EMPTY).isNotEmpty());
    assertFailureKeys(e, "expected not to be empty");
  }

  @Test
  public void isEqualTo_fail_unequalOrdering() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array(2, 3)).isEqualTo(array(3, 2)));
    assertFailureKeys(e, "expected", "but was", "differs at index");
    assertFailureValue(e, "expected", "[3, 2]");
    assertFailureValue(e, "but was", "[2, 3]");
    assertFailureValue(e, "differs at index", "[0]");
  }

  @Test
  public void isEqualTo_fail_notAnArray() {
    expectFailure(whenTesting -> whenTesting.that(array(2, 3, 4)).isEqualTo(new Object()));
  }

  @Test
  public void isNotEqualTo_sameLengths() {
    assertThat(array(2, 3)).isNotEqualTo(array(3, 2));
  }

  @Test
  public void isNotEqualTo_differentLengths() {
    assertThat(array(2, 3)).isNotEqualTo(array(2, 3, 1));
  }

  @Test
  public void isNotEqualTo_differentTypes() {
    assertThat(array(2, 3)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_failEquals() {
    expectFailure(whenTesting -> whenTesting.that(array(2, 3)).isNotEqualTo(array(2, 3)));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_failSame() {
    int[] same = array(2, 3);
    expectFailure(whenTesting -> whenTesting.that(same).isNotEqualTo(same));
  }

  private static int[] array(int... ts) {
    return ts;
  }
}
