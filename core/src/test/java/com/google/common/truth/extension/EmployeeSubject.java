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

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import javax.annotation.Nullable;

/**
 * A <a href="https://github.com/google/truth">Truth</a> subject for {@link Employee}.
 *
 * @author Kurt Alfred Kluever (kak@google.com)
 */
public final class EmployeeSubject extends Subject<EmployeeSubject, Employee> {

  // User-defined entry point
  public static EmployeeSubject assertThat(@Nullable Employee employee) {
    return assertAbout(EMPLOYEE_SUBJECT_FACTORY).that(employee);
  }

  // Static method for getting the subject factory (for use with assertAbout())
  public static SubjectFactory<EmployeeSubject, Employee> employees() {
    return EMPLOYEE_SUBJECT_FACTORY;
  }

  // Boiler-plate SubjectFactory for EmployeeSubject
  private static final SubjectFactory<EmployeeSubject, Employee> EMPLOYEE_SUBJECT_FACTORY =
      new SubjectFactory<EmployeeSubject, Employee>() {
        @Override
        public EmployeeSubject getSubject(
            FailureStrategy failureStrategy, @Nullable Employee target) {
          return new EmployeeSubject(failureStrategy, target);
        }
      };

  private EmployeeSubject(FailureStrategy failureStrategy, @Nullable Employee subject) {
    super(failureStrategy, subject);
  }

  // User-defined test assertion SPI below this point

  public void hasName(String name) {
    if (!getSubject().name().equals(name)) {
      fail("has name", name);
    }
  }

  public void hasUsername(String username) {
    if (!getSubject().username().equals(username)) {
      fail("has username", username);
    }
  }

  public void hasId(long id) {
    if (getSubject().id() != id) {
      fail("has id", id);
    }
  }

  public void hasLocation(Employee.Location location) {
    if (getSubject().location() != location) {
      fail("has location", location);
    }
  }

  public void isCeo() {
    if (!getSubject().isCeo()) {
      fail("is CEO");
    }
  }

  public void isNotCeo() {
    if (getSubject().isCeo()) {
      fail("is not CEO");
    }
  }

  // TODO(kak): Add methods that return other subjects. E.g.,
  // public StringSubject username() {}
  // public IterableSubject languages() {}
}
