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

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.ObjectArraySubject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;

/**
 * Tests for {@link ObjectArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class ObjectArraySubjectTest {

  private static final Object[] EMPTY = new Object[0];

  @Test public void isEqualTo() {
    assertThat(objectArray("A", 5L)).isEqualTo(objectArray("A", 5L));
  }

  @Test public void isEqualTo_Same() {
    Object[] same = objectArray("A", 5L);
    assertThat(same).isEqualTo(same);
  }

  @Test public void asList() {
    assertThat(objectArray("A", 5L)).asList().has().anyOf("A");
  }

  @Test public void hasLength() {
    assertThat(EMPTY).hasLength(0);
    assertThat(objectArray("A", 5L)).hasLength(2);
  }

  @Test public void hasLengthFail() {
    try {
      assertThat(objectArray("A", 5L)).hasLength(1);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .is("Not true that <(Object[]) [A, 5]> has length <1>");
    }
  }

  @Test public void hasLengthNegative() {
    try {
      assertThat(objectArray(2, 5)).hasLength(-1);
      throw new Error("Expected to throw.");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test public void isEmpty() {
    assertThat(EMPTY).isEmpty();
  }

  @Test public void isEmptyFail() {
    try {
      assertThat(objectArray("A", 5L)).isEmpty();
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .is("Not true that <(Object[]) [A, 5]> is empty");
    }
  }

  @Test public void isNotEmpty() {
    assertThat(objectArray("A", 5L)).isNotEmpty();
  }

  @Test public void isNotEmptyFail() {
    try {
      assertThat(EMPTY).isNotEmpty();
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .is("Not true that <(Object[]) []> is not empty");
    }
  }

  @Test public void isEqualTo_Fail_UnequalOrdering() {
    try {
      assertThat(objectArray("A", 5L)).isEqualTo(objectArray(5L, "A"));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .is("Not true that <(Object[]) [A, 5]> is equal to <[5, A]>");
    }
  }

  @Test public void isEqualTo_Fail_NotAnArray() {
    try {
      assertThat(objectArray("A", 5L)).isEqualTo(new Object());
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("Incompatible types compared.");
      assertThat(e.getMessage()).contains("expected: Object");
      assertThat(e.getMessage()).contains("actual: Object[]");
    }
  }

  @Test public void isNotEqualTo_SameLengths() {
    assertThat(objectArray("A", 5L)).isNotEqualTo(objectArray("C", 5L));
  }

  @Test public void isNotEqualTo_DifferentLengths() {
    assertThat(objectArray("A", 5L)).isNotEqualTo(objectArray("A", 5L, "c"));
  }

  @Test public void isNotEqualTo_DifferentTypes() {
    assertThat(objectArray("A", 5L)).isNotEqualTo(new Object());
  }

  @Test public void isNotEqualTo_FailEquals() {
    try {
      assertThat(objectArray("A", 5L)).isNotEqualTo(objectArray("A", 5L));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).is("<(Object[]) [A, 5]> unexpectedly equal to [A, 5].");
    }
  }

  @Test public void isNotEqualTo_FailSame() {
    try {
      Object[] same = objectArray("A", 5L);
      assertThat(same).isNotEqualTo(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).is("<(Object[]) [A, 5]> unexpectedly equal to [A, 5].");
    }
  }

  private static Object[] objectArray(Object ... ts) {
    return ts;
  }

  @Test public void stringArrayIsEqualTo() {
    assertThat(objectArray("A", "B")).isEqualTo(objectArray("A", "B"));
  }

  @Test public void stringArrayAsList() {
    assertThat(objectArray("A", "B")).asList().has().anyOf("A");
  }

  @Test public void stringArrayIsEqualTo_Fail_UnequalOrdering() {
    try {
      assertThat(objectArray("A", "B")).isEqualTo(objectArray("B", "A"));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .is("Not true that <(String[]) [A, B]> is equal to <[B, A]>");
    }
  }

  private static String[] objectArray(String... ts) {
    return ts;
  }

  @Test public void SetArrayIsEqualTo_Fail_UnequalOrdering() {
    try {
      assertThat(objectArray(ImmutableSet.of("A"), ImmutableSet.of("B")))
          .isEqualTo(objectArray(ImmutableSet.of("B"), ImmutableSet.of("A")));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      assertThat(e.getMessage())
          .is("Not true that <(Set[]) [[A], [B]]> is equal to <[[B], [A]]>");
          // Maybe one day:
          // .is("Not true that <(Set<String>[]) [[A], [B]]> is equal to <[[B], [A]]>");
    }
  }

  private static Set[] objectArray(Set... ts) {
    return ts;
  }
}
