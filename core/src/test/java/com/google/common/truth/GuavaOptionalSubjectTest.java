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
import static org.junit.Assert.fail;

import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Guava {@link Optional} Subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class GuavaOptionalSubjectTest extends BaseSubjectTestCase {

  @Test
  public void isPresent() {
    assertThat(Optional.of("foo")).isPresent();
  }

  @Test
  public void isPresentFailing() {
    expectFailureWhenTestingThat(Optional.absent()).isPresent();
    assertFailureKeys("expected to be present");
  }

  @Test
  public void isPresentFailing_named() {
    expectFailureWhenTestingThat(Optional.absent()).named("name").isPresent();
    assertFailureValue("name", "name");
  }

  @Test
  public void isPresentFailingNull() {
    expectFailureWhenTestingThat(null).isPresent();
    assertFailureKeys("expected present optional", "but was");
  }

  @Test
  public void isAbsent() {
    assertThat(Optional.absent()).isAbsent();
  }

  @Test
  public void isAbsentFailing() {
    expectFailureWhenTestingThat(Optional.of("foo")).isAbsent();
    assertFailureKeys("expected to be absent", "but was present with value");
    assertFailureValue("but was present with value", "foo");
  }

  @Test
  public void isAbsentFailingNull() {
    expectFailureWhenTestingThat(null).isAbsent();
    assertFailureKeys("expected absent optional", "but was");
  }

  @Test
  public void hasValue() {
    assertThat(Optional.of("foo")).hasValue("foo");
  }

  @Test
  public void hasValue_failingWithAbsent() {
    expectFailureWhenTestingThat(Optional.absent()).hasValue("foo");
    assertFailureKeys("expected to have value", "but was absent");
    assertFailureValue("expected to have value", "foo");
  }

  @Test
  public void hasValue_npeWithNullParameter() {
    try {
      assertThat(Optional.of("foo")).hasValue(null);
      fail("Expected NPE");
    } catch (NullPointerException expected) {
      assertThat(expected).hasMessageThat().contains("Optional");
    }
  }

  @Test
  public void hasValue_failingWithWrongValue() {
    expectFailureWhenTestingThat(Optional.of("foo")).hasValue("boo");
    assertFailureValue("value of", "optional.get()");
  }

  @Test
  public void hasValue_failingWithWrongValue_named() {
    expectFailureWhenTestingThat(Optional.of("foo")).named("bar").hasValue("boo");
    assertFailureValue("value of", "bar.get()");
  }

  private GuavaOptionalSubject expectFailureWhenTestingThat(Optional<?> actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
