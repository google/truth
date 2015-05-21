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

import com.google.common.truth.PrimitiveBooleanArraySubject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PrimitiveBooleanArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveBooleanArraySubjectTest {
  @Test
  public void isEqualTo() {
    assertThat(array(true, false, true)).isEqualTo(array(true, false, true));
  }

  @Test
  public void isEqualTo_Same() {
    boolean[] same = array(true, false, true);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array(true, true, false)).asList().containsAllOf(true, false);
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    try {
      assertThat(array(true, false, true)).isEqualTo(array(false, true, true));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e)
          .hasMessage(
              "Not true that <(boolean[]) [true, false, true]> is equal to <[false, true, true]>");
    }
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    try {
      assertThat(array(true, false, true)).isEqualTo(new Object());
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .contains("Incompatible types compared. expected: Object, actual: boolean[]");
    }
  }

  @Test
  public void isNotEqualTo_SameLengths() {
    assertThat(array(true, false)).isNotEqualTo(array(true, true));
  }

  @Test
  public void isNotEqualTo_DifferentLengths() {
    assertThat(array(true, false)).isNotEqualTo(array(true, false, true));
  }

  @Test
  public void isNotEqualTo_DifferentTypes() {
    assertThat(array(true, false)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_FailEquals() {
    try {
      assertThat(array(true, false)).isNotEqualTo(array(true, false));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<(boolean[]) [true, false]> unexpectedly equal to [true, false].");
    }
  }

  @Test
  public void isNotEqualTo_FailSame() {
    try {
      boolean[] same = array(true, false);
      assertThat(same).isNotEqualTo(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("<(boolean[]) [true, false]> unexpectedly equal to [true, false].");
    }
  }

  private static boolean[] array(boolean... ts) {
    return ts;
  }
}
