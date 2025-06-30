/*
 * Copyright (c) 2025 Google, Inc.
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

import static com.google.common.truth.SubjectUtils.longName;
import static com.google.common.truth.TestPlatform.isGwt;
import static com.google.common.truth.Truth.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link SubjectUtils}. */
@RunWith(JUnit4.class)
public class SubjectUtilsTest {
  @Test
  public void longNameOfBoxedPrimitive() {
    assertThat(longName(Long.class)).isEqualTo("Long");
  }

  @Test
  public void longNameOfBoxedPrimitiveArray() {
    assertThat(longName(Long[].class)).isEqualTo("Long[]");
  }

  @Test
  public void longNameOfPrimitiveArray() {
    assertThat(longName(long[].class)).isEqualTo("long[]");
  }

  @Test
  public void longNameOfTwoDimensionalPrimitiveArray() {
    assertThat(longName(long[][].class)).isEqualTo("long[][]");
  }

  @Test
  public void longNameOfObjectArray() {
    assertThat(longName(Object[].class)).isEqualTo("Object[]");
  }

  @Test
  public void longNameOfStringArray() {
    assertThat(longName(String[].class)).isEqualTo("String[]");
  }

  @Test
  public void longNameOfList() {
    assertThat(longName(List.class)).isEqualTo("java.util.List");
  }

  @Test
  public void longNameOfLinkedHashMap() {
    assertThat(longName(LinkedHashMap.class)).isEqualTo("LinkedHashMap");
  }

  @Test
  public void longNameOfLinkedHashSet() {
    assertThat(longName(LinkedHashSet.class)).isEqualTo("LinkedHashSet");
  }

  @Test
  public void longNameOfHashMap() {
    assertThat(longName(HashMap.class)).isEqualTo("HashMap");
  }

  @Test
  public void longNameOfHashSet() {
    assertThat(longName(HashSet.class)).isEqualTo("HashSet");
  }

  @Test
  public void longNameOfError() {
    assertThat(longName(Error.class)).isEqualTo("Error");
  }

  @Test
  public void longNameOfTimeoutException() {
    assertThat(longName(TimeoutException.class)).isEqualTo("TimeoutException");
  }

  @Test
  public void longNameOfCustomClass() {
    String expected =
        isGwt()
            ? "com.google.common.truth.SubjectUtilsTest$MyClass"
            : "com.google.common.truth.SubjectUtilsTest.MyClass";
    assertThat(longName(MyClass.class)).isEqualTo(expected);
  }

  private static final class MyClass {}
}
