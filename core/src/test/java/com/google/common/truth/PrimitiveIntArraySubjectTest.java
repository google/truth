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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link com.google.common.truth.PrimitiveIntArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveIntArraySubjectTest extends BaseSubjectTestCase {
  private static final int[] EMPTY = new int[0];

  @Test
  public void isEqualTo() {
    assertThat(array(2, 5)).isEqualTo(array(2, 5));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_Same() {
    int[] same = array(2, 5);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array(5, 2, 9)).asList().containsAllOf(2, 9);
  }

  @Test
  public void hasLength() {
    assertThat(EMPTY).hasLength(0);
    assertThat(array(2, 5)).hasLength(2);
  }

  @Test
  public void hasLengthFail() {
    expectFailureWhenTestingThat(array(2, 5)).hasLength(1);
    assertFailureValue("value of", "array.length");
  }

  @Test
  public void hasLengthNegative() {
    try {
      assertThat(array(2, 5)).hasLength(-1);
      fail("Should have failed.");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void isEmpty() {
    assertThat(EMPTY).isEmpty();
  }

  @Test
  public void isEmptyFail() {
    expectFailureWhenTestingThat(array(2, 5)).isEmpty();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void isNotEmpty() {
    assertThat(array(2, 5)).isNotEmpty();
  }

  @Test
  public void isNotEmptyFail() {
    expectFailureWhenTestingThat(EMPTY).isNotEmpty();
    assertFailureKeys("expected not to be empty");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    expectFailureWhenTestingThat(array(2, 3)).isEqualTo(array(3, 2));
    assertFailureKeys("expected", "but was", "differs at index");
    assertFailureValue("expected", "[3, 2]");
    assertFailureValue("but was", "[2, 3]");
    assertFailureValue("differs at index", "[0]");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailureWhenTestingThat(array(2, 3, 4)).isEqualTo(new Object());
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
    expectFailureWhenTestingThat(array(2, 3)).isNotEqualTo(array(2, 3));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    int[] same = array(2, 3);
    expectFailureWhenTestingThat(same).isNotEqualTo(same);
  }

  private static int[] array(int... ts) {
    return ts;
  }

  private PrimitiveIntArraySubject expectFailureWhenTestingThat(int[] actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
