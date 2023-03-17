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

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.ComparableSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.LongSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A <a href="https://github.com/google/truth">Truth</a> subject for {@link Employee}.
 *
 * @author Kurt Alfred Kluever (kak@google.com)
 */
public final class EmployeeSubject extends Subject {

  // User-defined entry point
  public static EmployeeSubject assertThat(@Nullable Employee employee) {
    return assertAbout(employees()).that(employee);
  }

  // Static method for getting the subject factory (for use with assertAbout())
  public static Subject.Factory<EmployeeSubject, Employee> employees() {
    return EmployeeSubject::new;
  }

  private final Employee actual;

  private EmployeeSubject(FailureMetadata failureMetadata, @Nullable Employee subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  // User-defined test assertion SPI below this point

  public void hasName(String name) {
    name().isEqualTo(name);
  }

  public void hasUsername(String username) {
    username().isEqualTo(username);
  }

  public void hasId(long id) {
    id().isEqualTo(id);
  }

  public void hasLocation(Employee.Location location) {
    location().isEqualTo(location);
  }

  public void isCeo() {
    if (!actual.isCeo()) {
      failWithActual(simpleFact("expected to be CEO"));
    }
  }

  public void isNotCeo() {
    if (actual.isCeo()) {
      failWithActual(simpleFact("expected not to be CEO"));
    }
  }

  // Chained subjects methods below this point

  public StringSubject name() {
    return check("name()").that(actual.name());
  }

  public StringSubject username() {
    return check("username()").that(actual.username());
  }

  public LongSubject id() {
    return check("id()").that(actual.id());
  }

  public ComparableSubject<Employee.Location> location() {
    return check("location()").that(actual.location());
  }
}
