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
import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link com.google.common.truth.PrimitiveCharArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
// We intentionally test mismatches.
// TODO(cpovirk): Maybe suppress at a finer scope.
@SuppressWarnings("TruthIncompatibleType")
public class PrimitiveCharArraySubjectTest extends BaseSubjectTestCase {

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
    assertThat(array('a', 'q', 'z')).asList().containsAtLeast('a', 'z');
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array('a', 'q')).isEqualTo(array('q', 'a')));
    assertFailureKeys(e, "expected", "but was", "differs at index");
    assertFailureValue(e, "expected", "[q, a]");
    assertFailureValue(e, "but was", "[a, q]");
    assertFailureValue(e, "differs at index", "[0]");
  }

  @Test
  public void isEqualTo_Fail_DifferentKindOfArray() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array('a', 'q')).isEqualTo(new int[] {}));
    assertFailureKeys(e, "expected", "but was", "wrong type", "expected", "but was");
    assertFailureValueIndexed(e, "expected", 1, "int[]");
    assertFailureValueIndexed(e, "but was", 1, "char[]");
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
    expectFailure(whenTesting -> whenTesting.that(array('a', 'q')).isNotEqualTo(array('a', 'q')));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    char[] same = array('a', 'q');
    expectFailure(whenTesting -> whenTesting.that(same).isNotEqualTo(same));
  }

  private static char[] array(char... ts) {
    return ts;
  }
}
