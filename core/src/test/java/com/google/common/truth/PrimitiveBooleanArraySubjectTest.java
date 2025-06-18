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
 * Tests for {@link com.google.common.truth.PrimitiveBooleanArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveBooleanArraySubjectTest {

  @Test
  public void isEqualTo() {
    assertThat(array(true, false, true)).isEqualTo(array(true, false, true));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_same() {
    boolean[] same = array(true, false, true);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array(true, true, false)).asList().containsAtLeast(true, false);
  }

  @Test
  public void isEqualTo_fail_unequalOrdering() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(array(true, false, true)).isEqualTo(array(false, true, true)));
    assertFailureValue(e, "differs at index", "[0]");
  }

  @Test
  public void isEqualTo_fail_notAnArray() {
    expectFailure(
        whenTesting -> whenTesting.that(array(true, false, true)).isEqualTo(new Object()));
  }

  @Test
  public void isNotEqualTo_sameLengths() {
    assertThat(array(true, false)).isNotEqualTo(array(true, true));
  }

  @Test
  public void isNotEqualTo_differentLengths() {
    assertThat(array(true, false)).isNotEqualTo(array(true, false, true));
  }

  @Test
  public void isNotEqualTo_differentTypes() {
    assertThat(array(true, false)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_failEquals() {
    expectFailure(
        whenTesting -> whenTesting.that(array(true, false)).isNotEqualTo(array(true, false)));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_failSame() {
    boolean[] same = array(true, false);
    expectFailure(whenTesting -> whenTesting.that(same).isNotEqualTo(same));
  }

  @Test
  public void hasLengthNullArray() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((boolean[]) null).hasLength(1));
    assertFailureKeys(e, "expected an array with length", "but was");
    assertFailureValue(e, "expected an array with length", "1");
  }

  private static boolean[] array(boolean... ts) {
    return ts;
  }
}
