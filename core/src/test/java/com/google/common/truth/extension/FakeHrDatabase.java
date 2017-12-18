/*
 * Copyright (c) 2017 Google, Inc.
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.extension.Employee.Location;
import java.util.HashMap;
import java.util.Map;

/** In-memory implementation of {@link HrDatabase}, suitable for testing. */
public final class FakeHrDatabase implements HrDatabase {
  private final Map<Long, Employee> employees = new HashMap<>();

  public void put(Employee employee) {
    employees.put(employee.id(), employee);
  }

  @Override
  public Employee get(long id) {
    return employees.get(id);
  }

  @Override
  public void relocate(long id, Location location) {
    checkNotNull(location);
    Employee old = get(id);
    checkState(old != null, "No employee found with ID %s", id);
    employees.put(id, Employee.create(old.username(), old.id(), old.name(), location, old.isCeo()));
  }

  @Override
  public ImmutableSet<Employee> getByLocation(Location location) {
    checkNotNull(location);
    ImmutableSet.Builder<Employee> result = ImmutableSet.builder();
    for (Employee employee : employees.values()) {
      if (employee.location() == location) {
        result.add(employee);
      }
    }
    return result.build();
  }
}
