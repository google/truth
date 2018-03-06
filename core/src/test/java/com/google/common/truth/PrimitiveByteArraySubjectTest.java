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

import com.google.common.annotations.GwtIncompatible;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link com.google.common.truth.PrimitiveByteArraySubject}.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class PrimitiveByteArraySubjectTest extends BaseSubjectTestCase {
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
  public void isEqualTo_Fail_shortVersion() {
    byte[] actual = new byte[] {124, 112, 12, 11, 10};
    byte[] expect = new byte[] {24, 12, 2, 1, 0};
    expectFailureWhenTestingThat(actual).isEqualTo(expect);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "expected: [24, 12, 2, 1, 0]; but was: [124, 112, 12, 11, 10] expected:<[180C020100]> "
                + "but was:<[7C700C0B0A]>");
  }

  @Test
  @GwtIncompatible("Platform.comparisionFailure")
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
    expectFailureWhenTestingThat(actual).isEqualTo(expect);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "expected: [124, 112, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 7, 101, 120, 97, 109, 112, 108, "
                + "101, 3, 99, 111, 109, 0, 0, 1, 0, 1]; but was: [124, 112, 1, 0, 0, 1, 0, 0, 0, "
                + "0, 0, 0, 7, 101, 120, 97, 109, 112, 108, 101, 3, 99, 111, 109, 0, 0, 1, 0, 0] "
                + "expected:<...C6503636F6D000001000[1]> but was:<...C6503636F6D000001000[0]>");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    expectFailureWhenTestingThat(array(BYTE_0, (byte) 123)).isEqualTo(array((byte) 123, BYTE_0));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("expected: [123, 0]; but was: [0, 123] expected:<[7B00]> but was:<[007B]>");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailureWhenTestingThat(array(BYTE_0, BYTE_1)).isEqualTo(new int[] {});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("wrong type; expected: int[]; but was: byte[]");
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
    expectFailureWhenTestingThat(array(BYTE_0, BYTE_1)).isNotEqualTo(array(BYTE_0, BYTE_1));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <0001> is not equal to <0001>");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    byte[] same = array(BYTE_0, BYTE_1);
    expectFailureWhenTestingThat(same).isNotEqualTo(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <0001> is not equal to <0001>");
  }

  private static byte[] array(byte... ts) {
    return ts;
  }

  private PrimitiveByteArraySubject expectFailureWhenTestingThat(byte[] actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
