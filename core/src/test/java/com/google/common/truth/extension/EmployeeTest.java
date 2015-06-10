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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.extension.Employee.Location;
import static com.google.common.truth.extension.EmployeeSubject.assertThat;
import static com.google.common.truth.extension.EmployeeSubject.employee;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EmployeeTest {

  // This is not my real employee ID :-)
  private static final Employee KURT =
      Employee.create("kak", 37802, "Kurt Alfred Kluever", Location.NYC);

  @Test
  public void testEmployee() {
    // These assertions use Truth.assertThat() overloads
    assertThat("kurt alfred kluever").contains("alfred");
    assertThat(42).isGreaterThan(41);

    // These assertions use the EmployeeSubject.assertThat(Employee) overload
    assertThat(KURT).hasId(37802);
    assertThat(KURT).hasUsername("kak");
    assertThat(KURT).hasName("Kurt Alfred Kluever");
    assertThat(KURT).hasLocation(Location.NYC);
  }
}
