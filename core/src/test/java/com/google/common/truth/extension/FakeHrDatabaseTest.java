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
import static com.google.common.truth.extension.EmployeeSubject.assertThat;

import com.google.common.truth.extension.Employee.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class FakeHrDatabaseTest {

  // Note: not real employee IDs :-)

  private static final Employee KURT =
      Employee.create("kak", 37802, "Kurt Alfred Kluever", Location.NYC, false);

  private static final Employee SUNDAR =
      Employee.create("sundar", 5243, "Sundar Pichai", Location.MTV, true);

  // Notice that we static import two different assertThat methods.

  // These assertions use the EmployeeSubject.assertThat(Employee) overload and the
  // EmployeeSubject-specific methods.

  @Test
  public void relocatePresent() {
    FakeHrDatabase db = new FakeHrDatabase();
    db.put(KURT);
    db.relocate(KURT.id(), Location.MTV);
    Employee movedKurt = db.get(KURT.id());
    assertThat(movedKurt).hasLocation(Location.MTV);
    assertThat(movedKurt).hasUsername("kak");
  }

  // These assertions use the EmployeeSubject.assertThat(Employee) overload but the assertion
  // methods inherited from Subject.

  @Test
  public void getPresent() {
    FakeHrDatabase db = new FakeHrDatabase();
    db.put(KURT);
    assertThat(db.get(KURT.id())).isEqualTo(KURT);
  }

  @Test
  public void getAbsent() {
    FakeHrDatabase db = new FakeHrDatabase();
    db.put(KURT);
    assertThat(db.get(SUNDAR.id())).isNull();
  }

  // These assertions use Truth.assertThat() overloads

  @Test
  public void getByLocation() {
    FakeHrDatabase db = new FakeHrDatabase();
    db.put(KURT);
    assertThat(db.getByLocation(Location.NYC)).containsExactly(KURT);
  }
}
