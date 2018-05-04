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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PrimitiveShortArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveShortArraySubjectTest extends BaseSubjectTestCase {

  @Test
  public void isEqualTo() {
    assertThat(array(1, 0, 1)).isEqualTo(array(1, 0, 1));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_Same() {
    short[] same = array(1, 0, 1);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array(1, 1, 0)).asList().containsAllOf((short) 1, (short) 0);
  }

  @Test
  public void asListWithoutCastingFails() {
    expectFailureWhenTestingThat(array(1, 1, 0)).asList().containsAllOf(1, 0);
    assertFailureKeys(
        "value of",
        "missing (2)",
        "though it did contain (3)",
        "---",
        "expected to contain at least",
        "but was");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    expectFailureWhenTestingThat(array(1, 0, 1)).isEqualTo(array(0, 1, 1));
    assertFailureKeys("expected", "but was", "differs at index");
    assertFailureValue("expected", "[0, 1, 1]");
    assertFailureValue("but was", "[1, 0, 1]");
    assertFailureValue("differs at index", "[0]");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailureWhenTestingThat(array(1, 0, 1)).isEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_SameLengths() {
    assertThat(array(1, 0)).isNotEqualTo(array(1, 1));
  }

  @Test
  public void isNotEqualTo_DifferentLengths() {
    assertThat(array(1, 0)).isNotEqualTo(array(1, 0, 1));
  }

  @Test
  public void isNotEqualTo_DifferentTypes() {
    assertThat(array(1, 0)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_FailEquals() {
    expectFailureWhenTestingThat(array(1, 0)).isNotEqualTo(array(1, 0));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    short[] same = array(1, 0);
    expectFailureWhenTestingThat(same).isNotEqualTo(same);
  }

  private static short[] array(int a, int b, int c) {
    return new short[] {(short) a, (short) b, (short) c};
  }

  private static short[] array(int a, int b) {
    return new short[] {(short) a, (short) b};
  }

  private PrimitiveShortArraySubject expectFailureWhenTestingThat(short[] actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
