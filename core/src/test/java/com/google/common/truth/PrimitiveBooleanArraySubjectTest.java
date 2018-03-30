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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link com.google.common.truth.PrimitiveBooleanArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveBooleanArraySubjectTest extends BaseSubjectTestCase {

  @Test
  public void isEqualTo() {
    assertThat(array(true, false, true)).isEqualTo(array(true, false, true));
  }

  @SuppressWarnings("TruthSelfEquals")
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
    expectFailureWhenTestingThat(array(true, false, true)).isEqualTo(array(false, true, true));
    assertFailureValue("differs at index", "[0]");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailureWhenTestingThat(array(true, false, true)).isEqualTo(new Object());
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
    expectFailureWhenTestingThat(array(true, false)).isNotEqualTo(array(true, false));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    boolean[] same = array(true, false);
    expectFailureWhenTestingThat(same).isNotEqualTo(same);
  }

  private static boolean[] array(boolean... ts) {
    return ts;
  }

  private PrimitiveBooleanArraySubject expectFailureWhenTestingThat(boolean[] actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
