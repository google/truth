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

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests (and effectively sample code) for the Expect verb (implemented as a rule)
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class RelabeledSubjectsTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void namedIncludesActualStringValue() {
    expectFailure.whenTesting().that("kurt kluever").named("rad dude").startsWith("frazzle");
    AssertionError expected = expectFailure.getFailure();
    assertThat(expected).hasMessageThat().contains("\"kurt kluever\"");
    assertThat(expected).hasMessageThat().contains("rad dude");
    assertThat(expected).hasMessageThat().contains("frazzle");
  }

  @Test
  public void namedIncludesActualIntegerValue() {
    expectFailure.whenTesting().that(13).named("Septober").isLessThan(12);
    AssertionError expected = expectFailure.getFailure();
    assertThat(expected).hasMessageThat().contains("13");
    assertThat(expected).hasMessageThat().contains("12");
    assertThat(expected).hasMessageThat().contains("Septober");
  }

  @Test
  public void relabeledBooleans() {
    expectFailure.whenTesting().that(false).named("Foo").isTrue();
    // TODO(kak): This could probably be simplified + shortened a bit...
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Foo (<false>) was expected to be true, but was false");
  }

  @Test
  public void relabeledObject() {
    expectFailure.whenTesting().that("a string").named("Foo").isInstanceOf(Integer.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that Foo (<\"a string\">) is an instance of <java.lang.Integer>");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("It is an instance of <java.lang.String>");
  }

  @Test
  public void relabelledCollections() {
    expectFailure
        .whenTesting()
        .that(Arrays.asList("a", "b", "c"))
        .named("crazy list")
        .containsAllOf("c", "d");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that crazy list (<[a, b, c]>) contains all of <[c, d]>. "
                + "It is missing <[d]>");
  }

  @Test
  public void relabelledPrimitiveFloatArrays() {
    float[] actual = {1.3f, 1.1f};
    expectFailure
        .whenTesting()
        .that(actual)
        .named("crazy list")
        .usingTolerance(1.0E-7)
        .containsExactly(1.3f, 1.0f);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that crazy list (<[1.3, 1.1]>) contains exactly one element that is a "
                + "finite number within 1.0E-7 of each element of <[1.3, 1.0]>. It is missing an "
                + "element that is a finite number within 1.0E-7 of <1.0> and has unexpected "
                + "elements <[1.1]>");
  }

  @Test
  public void relabelledPrimitiveLongArrays() {
    long[] actual = {123L, 456L};
    expectFailure.whenTesting().that(actual).named("crazy list").asList().contains(789L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("crazy list (<[123, 456]>) should have contained <789>");
  }

  @Test
  public void relabelledObjectArrays() {
    String[] actual = {"cat", "dog"};
    expectFailure.whenTesting().that(actual).named("crazy list").asList().contains("rabbit");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("crazy list (<[cat, dog]>) should have contained <rabbit>");
  }
}
