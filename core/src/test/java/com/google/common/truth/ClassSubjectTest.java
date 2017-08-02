/*
 * Copyright (c) 2011 Google, Inc.
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
 * Tests for introspective Subject behaviour.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class ClassSubjectTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void testIsAssignableTo_same() {
    assertThat(String.class).isAssignableTo(String.class);
  }

  @Test
  public void testIsAssignableTo_parent() {
    assertThat(String.class).isAssignableTo(Object.class);
    assertThat(NullPointerException.class).isAssignableTo(Exception.class);
  }

  @Test
  public void testIsAssignableTo_reversed() {
    expectFailure.whenTesting().that(Object.class).isAssignableTo(String.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <class java.lang.Object> "
                + "is assignable to <class java.lang.String>");
  }

  @Test
  public void testIsAssignableTo_reversedDifferentTypes() {
    expectFailure.whenTesting().that(String.class).isAssignableTo(Exception.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <class java.lang.String> "
                + "is assignable to <class java.lang.Exception>");
  }
}
