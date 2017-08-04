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
 * Tests for {@link com.google.common.truth.PrimitiveCharArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PrimitiveCharArraySubjectTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void isEqualTo() {
    assertThat(array('a', 'q')).isEqualTo(array('a', 'q'));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_Same() {
    char[] same = array('a', 'q');
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array('a', 'q', 'z')).asList().containsAllOf('a', 'z');
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    expectFailure.whenTesting().that(array('a', 'q')).isEqualTo(array('q', 'a'));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(char[]) [a, q]> is equal to <[q, a]>");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailure.whenTesting().that(array('a', 'q')).isEqualTo(new int[] {});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Incompatible types compared. expected: int[], actual: char[]");
  }

  @Test
  public void isNotEqualTo_SameLengths() {
    assertThat(array('a', 'q')).isNotEqualTo(array('q', 'a'));
  }

  @Test
  public void isNotEqualTo_DifferentLengths() {
    assertThat(array('a', 'q')).isNotEqualTo(array('q', 'a', 'b'));
  }

  @Test
  public void isNotEqualTo_DifferentTypes() {
    assertThat(array('a', 'q')).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_FailEquals() {
    expectFailure.whenTesting().that(array('a', 'q')).isNotEqualTo(array('a', 'q'));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(char[]) [a, q]> unexpectedly equal to [a, q].");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    char[] same = array('a', 'q');
    expectFailure.whenTesting().that(same).isNotEqualTo(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(char[]) [a, q]> unexpectedly equal to [a, q].");
  }

  private static char[] array(char... ts) {
    return ts;
  }
}
