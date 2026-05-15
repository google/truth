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

import com.google.auto.value.AutoValue;

/** Represents an employee. */
@AutoValue
public abstract class Employee {
  public static Employee create(
      String username, long id, String name, Location location, boolean isCeo) {
    return new AutoValue_Employee(username, id, name, location, isCeo);
  }

  abstract String username();

  abstract long id();

  abstract String name();

  abstract Location location();

  abstract boolean isCeo();

  public enum Location {
    MTV,
    PIT,
    CHI,
    NYC
  }
}
