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
package com.google.common.truth.extension;

import static com.google.common.truth.ExpectFailure.expectFailureAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.extension.EmployeeSubject.assertThat;
import static com.google.common.truth.extension.EmployeeSubject.employees;

import com.google.common.truth.ExpectFailure.SimpleSubjectBuilderCallback;
import com.google.common.truth.extension.Employee.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class EmployeeSubjectTest {

  // Note: not real employee IDs :-)

  private static final Employee KURT =
      Employee.create("kak", 37802, "Kurt Alfred Kluever", Location.NYC, false);

  @Test
  public void id() {
    assertThat(KURT).hasId(37802);
    expectFailure(whenTesting -> whenTesting.that(KURT).hasId(12345));
  }

  @Test
  public void name() {
    assertThat(KURT).hasName("Kurt Alfred Kluever");
    expectFailure(whenTesting -> whenTesting.that(KURT).hasName("Sundar Pichai"));
  }

  @Test
  public void username() {
    assertThat(KURT).hasUsername("kak");
    // Here's an example of asserting on the failure message:
    AssertionError failure = expectFailure(whenTesting -> whenTesting.that(KURT).hasName("sundar"));
    assertThat(failure).hasMessageThat().contains("username");
  }

  private static AssertionError expectFailure(
      SimpleSubjectBuilderCallback<EmployeeSubject, Employee> callback) {
    return expectFailureAbout(employees(), callback);
  }
}
