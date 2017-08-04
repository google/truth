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

import org.junit.Rule;
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
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();
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
    expectFailure.whenTesting().that(array(2, 5)).hasLength(1);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(int[]) [2, 5]> has length <1>");
  }

  @Test
  public void hasLengthNegative() {
    try {
      assertThat(array(2, 5)).hasLength(-1);
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void isEmpty() {
    assertThat(EMPTY).isEmpty();
  }

  @Test
  public void isEmptyFail() {
    expectFailure.whenTesting().that(array(2, 5)).isEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(int[]) [2, 5]> is empty");
  }

  @Test
  public void isNotEmpty() {
    assertThat(array(2, 5)).isNotEmpty();
  }

  @Test
  public void isNotEmptyFail() {
    expectFailure.whenTesting().that(EMPTY).isNotEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(int[]) []> is not empty");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    expectFailure.whenTesting().that(array(2, 3)).isEqualTo(array(3, 2));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(int[]) [2, 3]> is equal to <[3, 2]>");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailure.whenTesting().that(array(2, 3, 4)).isEqualTo(new Object());
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Incompatible types compared. expected: Object, actual: int[]");
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
    expectFailure.whenTesting().that(array(2, 3)).isNotEqualTo(array(2, 3));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(int[]) [2, 3]> unexpectedly equal to [2, 3].");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    int[] same = array(2, 3);
    expectFailure.whenTesting().that(same).isNotEqualTo(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(int[]) [2, 3]> unexpectedly equal to [2, 3].");
  }

  private static int[] array(int... ts) {
    return ts;
  }
}
