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

import com.google.common.truth.PrimitiveByteArraySubject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PrimitiveByteArraySubject}.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class PrimitiveByteArraySubjectTest {
  private static final byte BYTE_0 = (byte) 0;
  private static final byte BYTE_1 = (byte) 1;
  private static final byte BYTE_2 = (byte) 2;

  @Test
  public void isEqualTo() {
    assertThat(array(BYTE_0, BYTE_1)).isEqualTo(array(BYTE_0, BYTE_1));
  }

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
  public void isEqualTo_Fail_UnequalOrdering() {
    try {
      assertThat(array(BYTE_0, BYTE_1)).isEqualTo(array(BYTE_1, BYTE_0));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <(byte[]) [0, 1]> is equal to <[1, 0]>");
    }
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    try {
      assertThat(array(BYTE_0, BYTE_1)).isEqualTo(new int[] {});
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains("Incompatible types compared. expected: int[], actual: byte[]");
    }
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
    try {
      assertThat(array(BYTE_0, BYTE_1)).isNotEqualTo(array(BYTE_0, BYTE_1));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<(byte[]) [0, 1]> unexpectedly equal to [0, 1].");
    }
  }

  @Test
  public void isNotEqualTo_FailSame() {
    try {
      byte[] same = array(BYTE_0, BYTE_1);
      assertThat(same).isNotEqualTo(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<(byte[]) [0, 1]> unexpectedly equal to [0, 1].");
    }
  }

  private static byte[] array(byte... ts) {
    return ts;
  }
}
