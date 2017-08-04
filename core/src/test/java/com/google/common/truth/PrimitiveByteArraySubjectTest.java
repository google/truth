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
 * Tests for {@link com.google.common.truth.PrimitiveByteArraySubject}.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class PrimitiveByteArraySubjectTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();
  private static final byte BYTE_0 = (byte) 0;
  private static final byte BYTE_1 = (byte) 1;
  private static final byte BYTE_2 = (byte) 2;

  @Test
  public void isEqualTo() {
    assertThat(array(BYTE_0, BYTE_1)).isEqualTo(array(BYTE_0, BYTE_1));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_Same() {
    byte[] same = array(BYTE_0, BYTE_1);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array(BYTE_0, BYTE_1, BYTE_2)).asList().containsAllOf(BYTE_0, BYTE_2);
  }

  @Test
  public void isEqualTo_Fail() {
    byte[] actual =
        new byte[] {
          124, 112, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 7, 101, 120, 97, 109, 112, 108, 101, 3, 99, 111,
          109, 0, 0, 1, 0, 0
        };
    byte[] expect =
        new byte[] {
          124, 112, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 7, 101, 120, 97, 109, 112, 108, 101, 3, 99, 111,
          109, 0, 0, 1, 0, 1
        };
    expectFailure.whenTesting().that(actual).isEqualTo(expect);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(byte[]) [124, 112, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 7, 101, 120, 97, "
                + "109, 112, 108, 101, 3, 99, 111, 109, 0, 0, 1, 0, 0]> is equal to "
                + "<[124, 112, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 7, 101, 120, 97, 109, 112, 108, "
                + "101, 3, 99, 111, 109, 0, 0, 1, 0, 1]>; "
                + "expected:<...C6503636F6D000001000[1]>"
                + " but was:<...C6503636F6D000001000[0]>");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    expectFailure.whenTesting().that(array(BYTE_0, BYTE_1)).isEqualTo(array(BYTE_1, BYTE_0));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(byte[]) [0, 1]> is equal to <[1, 0]>; "
                + "expected:<0[100]> but was:<0[001]>");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailure.whenTesting().that(array(BYTE_0, BYTE_1)).isEqualTo(new int[] {});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Incompatible types compared. expected: int[], actual: byte[]");
  }

  @Test
  public void isNotEqualTo_SameLengths() {
    assertThat(array(BYTE_0, BYTE_1)).isNotEqualTo(array(BYTE_1, BYTE_0));
  }

  @Test
  public void isNotEqualTo_DifferentLengths() {
    assertThat(array(BYTE_0, BYTE_1)).isNotEqualTo(array(BYTE_1, BYTE_0, BYTE_2));
  }

  @Test
  public void isNotEqualTo_DifferentTypes() {
    assertThat(array(BYTE_0, BYTE_1)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_FailEquals() {
    expectFailure.whenTesting().that(array(BYTE_0, BYTE_1)).isNotEqualTo(array(BYTE_0, BYTE_1));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(byte[]) [0, 1]> unexpectedly equal to [0, 1].");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    byte[] same = array(BYTE_0, BYTE_1);
    expectFailure.whenTesting().that(same).isNotEqualTo(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(byte[]) [0, 1]> unexpectedly equal to [0, 1].");
  }

  private static byte[] array(byte... ts) {
    return ts;
  }
}
