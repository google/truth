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

import static com.google.common.truth.Truth.ASSERT;

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

  @Test public void isEqualTo() {
    ASSERT.that(objectArray("A", 5L)).isEqualTo(objectArray("A", 5L));
  }

  @Test public void isEqualTo_Same() {
    Object[] same = objectArray("A", 5L);
    ASSERT.that(same).isEqualTo(same);
  }

  @Test public void asList() {
    ASSERT.that(objectArray("A", 5L)).asList().has().anyOf("A");
  }

  @Test public void isEqualTo_Fail_UnequalOrdering() {
    try {
      ASSERT.that(objectArray("A", 5L)).isEqualTo(objectArray(5L, "A"));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("Not true that <(Object[]) [A, 5]> is equal to <[5, A]>");
    }
  }

  @Test public void isEqualTo_Fail_NotAnArray() {
    try {
      ASSERT.that(objectArray("A", 5L)).isEqualTo(new Object());
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Incompatible types compared.");
      ASSERT.that(e.getMessage()).contains("expected: Object");
      ASSERT.that(e.getMessage()).contains("actual: Object[]");
    }
  }

  @Test public void isNotEqualTo_SameLengths() {
    ASSERT.that(objectArray("A", 5L)).isNotEqualTo(objectArray("C", 5L));
  }

  @Test public void isNotEqualTo_DifferentLengths() {
    ASSERT.that(objectArray("A", 5L)).isNotEqualTo(objectArray("A", 5L, "c"));
  }

  @Test public void isNotEqualTo_DifferentTypes() {
    ASSERT.that(objectArray("A", 5L)).isNotEqualTo(new Object());
  }

  @Test public void isNotEqualTo_FailEquals() {
    try {
      ASSERT.that(objectArray("A", 5L)).isNotEqualTo(objectArray("A", 5L));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is("<(Object[]) [A, 5]> unexpectedly equal to [A, 5].");
    }
  }

  @Test public void isNotEqualTo_FailSame() {
    try {
      Object[] same = objectArray("A", 5L);
      ASSERT.that(same).isNotEqualTo(same);
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).is("<(Object[]) [A, 5]> unexpectedly equal to [A, 5].");
    }
  }

  private static Object[] objectArray(Object ... ts) {
    return ts;
  }

  @Test public void stringArrayIsEqualTo() {
    ASSERT.that(objectArray("A", "B")).isEqualTo(objectArray("A", "B"));
  }

  @Test public void stringArrayAsList() {
    ASSERT.that(objectArray("A", "B")).asList().has().anyOf("A");
  }

  @Test public void stringArrayIsEqualTo_Fail_UnequalOrdering() {
    try {
      ASSERT.that(objectArray("A", "B")).isEqualTo(objectArray("B", "A"));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("Not true that <(String[]) [A, B]> is equal to <[B, A]>");
    }
  }

  private static String[] objectArray(String... ts) {
    return ts;
  }

  @Test public void SetArrayIsEqualTo_Fail_UnequalOrdering() {
    try {
      ASSERT.that(objectArray(ImmutableSet.of("A"), ImmutableSet.of("B")))
          .isEqualTo(objectArray(ImmutableSet.of("B"), ImmutableSet.of("A")));
      throw new Error("Expected to throw.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage())
          .is("Not true that <(Set[]) [[A], [B]]> is equal to <[[B], [A]]>");
          // Maybe one day:
          // .is("Not true that <(Set<String>[]) [[A], [B]]> is equal to <[[B], [A]]>");
    }
  }

  private static Set[] objectArray(Set... ts) {
    return ts;
  }
}
