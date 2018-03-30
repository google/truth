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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link com.google.common.truth.ObjectArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class ObjectArraySubjectTest extends BaseSubjectTestCase {
  private static final Object[] EMPTY = new Object[0];

  @Test
  public void isEqualTo() {
    assertThat(objectArray("A", 5L)).isEqualTo(objectArray("A", 5L));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_Same() {
    Object[] same = objectArray("A", 5L);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(objectArray("A", 5L)).asList().contains("A");
  }

  @Test
  public void hasLength() {
    assertThat(EMPTY).hasLength(0);
    assertThat(objectArray("A", 5L)).hasLength(2);
    assertThat(new Object[][] {}).hasLength(0);
    assertThat(new Object[][] {{}}).hasLength(1);
  }

  @Test
  public void hasLengthFail() {
    expectFailureWhenTestingThat(objectArray("A", 5L)).hasLength(1);
    assertFailureValue("value of", "array.length");
  }

  @Test
  public void hasLengthFailNamed() {
    expectFailureWhenTestingThat(objectArray("A", 5L)).named("foo").hasLength(1);
    assertFailureValue("value of", "foo.length");
  }

  @Test
  public void hasLengthMultiFail() {
    expectFailureWhenTestingThat(new Object[][] {{"A"}, {5L}}).hasLength(1);
    assertFailureValue("value of", "array.length");
  }

  @Test
  public void hasLengthNegative() {
    try {
      assertThat(objectArray(2, 5)).hasLength(-1);
      fail("Should have failed");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void isEmpty() {
    assertThat(EMPTY).isEmpty();
    assertThat(new Object[][] {}).isEmpty();
  }

  @Test
  public void isEmptyFail() {
    expectFailureWhenTestingThat(objectArray("A", 5L)).isEmpty();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void isNotEmpty() {
    assertThat(objectArray("A", 5L)).isNotEmpty();
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEmpty();
  }

  @Test
  public void isNotEmptyFail() {
    expectFailureWhenTestingThat(EMPTY).isNotEmpty();
    assertFailureKeys("expected not to be empty");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    expectFailureWhenTestingThat(objectArray("A", 5L)).isEqualTo(objectArray(5L, "A"));
    assertFailureValue("differs at index", "[0]");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrderingMultiDimensional_00() {
    expectFailureWhenTestingThat(new Object[][] {{"A"}, {5L}})
        .isEqualTo(new Object[][] {{5L}, {"A"}});
    assertFailureValue("differs at index", "[0][0]");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrderingMultiDimensional_01() {
    expectFailureWhenTestingThat(new Object[][] {{"A", "B"}, {5L}})
        .isEqualTo(new Object[][] {{"A"}, {5L}});
    assertFailureValue("wrong length for index", "[0]");
    assertFailureValueIndexed("expected", 1, "1");
    assertFailureValueIndexed("but was", 1, "2");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrderingMultiDimensional_11() {
    expectFailureWhenTestingThat(new Object[][] {{"A"}, {5L}})
        .isEqualTo(new Object[][] {{"A"}, {5L, 6L}});
    assertFailureValue("wrong length for index", "[1]");
    assertFailureValueIndexed("expected", 1, "2");
    assertFailureValueIndexed("but was", 1, "1");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailureWhenTestingThat(objectArray("A", 5L)).isEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_SameLengths() {
    assertThat(objectArray("A", 5L)).isNotEqualTo(objectArray("C", 5L));
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEqualTo(new Object[][] {{"C"}, {5L}});
  }

  @Test
  public void isNotEqualTo_DifferentLengths() {
    assertThat(objectArray("A", 5L)).isNotEqualTo(objectArray("A", 5L, "c"));
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEqualTo(new Object[][] {{"A", "c"}, {5L}});
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEqualTo(new Object[][] {{"A"}, {5L}, {"C"}});
  }

  @Test
  public void isNotEqualTo_DifferentTypes() {
    assertThat(objectArray("A", 5L)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_FailEquals() {
    expectFailureWhenTestingThat(objectArray("A", 5L)).isNotEqualTo(objectArray("A", 5L));
  }

  @Test
  public void isNotEqualTo_FailEqualsMultiDimensional() {
    expectFailureWhenTestingThat(new Object[][] {{"A"}, {5L}})
        .isNotEqualTo(new Object[][] {{"A"}, {5L}});
    assertFailureValue("expected not to be", "[[A], [5]]");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    Object[] same = objectArray("A", 5L);
    expectFailureWhenTestingThat(same).isNotEqualTo(same);
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSameMultiDimensional() {
    Object[][] same = new Object[][] {{"A"}, {5L}};
    expectFailureWhenTestingThat(same).isNotEqualTo(same);
  }

  @Test
  public void stringArrayIsEqualTo() {
    assertThat(objectArray("A", "B")).isEqualTo(objectArray("A", "B"));
    assertThat(new String[][] {{"A"}, {"B"}}).isEqualTo(new String[][] {{"A"}, {"B"}});
  }

  @Test
  public void stringArrayAsList() {
    assertThat(objectArray("A", "B")).asList().contains("A");
  }

  @Test
  public void multiDimensionalStringArrayAsList() {
    String[] ab = {"A", "B"};
    assertThat(new String[][] {ab, {"C"}}).asList().contains(ab);
  }

  @Test
  public void stringArrayIsEqualTo_Fail_UnequalLength() {
    expectFailureWhenTestingThat(objectArray("A", "B")).isEqualTo(objectArray("B"));
    assertFailureKeys("expected", "but was", "wrong length", "expected", "but was");
    assertFailureValueIndexed("expected", 1, "1");
    assertFailureValueIndexed("but was", 1, "2");
  }

  @Test
  public void stringArrayIsEqualTo_Fail_UnequalLengthMultiDimensional() {
    expectFailureWhenTestingThat(new String[][] {{"A"}, {"B"}}).isEqualTo(new String[][] {{"A"}});
    assertFailureKeys("expected", "but was", "wrong length", "expected", "but was");
    assertFailureValueIndexed("expected", 1, "1");
    assertFailureValueIndexed("but was", 1, "2");
  }

  @Test
  public void stringArrayIsEqualTo_Fail_UnequalOrdering() {
    expectFailureWhenTestingThat(objectArray("A", "B")).isEqualTo(objectArray("B", "A"));
    assertFailureValue("differs at index", "[0]");
  }

  @Test
  public void stringArrayIsEqualTo_Fail_UnequalOrderingMultiDimensional() {
    expectFailureWhenTestingThat(new String[][] {{"A"}, {"B"}})
        .isEqualTo(new String[][] {{"B"}, {"A"}});
    assertFailureValue("differs at index", "[0][0]");
  }

  @Test
  public void setArrayIsEqualTo_Fail_UnequalOrdering() {
    expectFailureWhenTestingThat(objectArray(ImmutableSet.of("A"), ImmutableSet.of("B")))
        .isEqualTo(objectArray(ImmutableSet.of("B"), ImmutableSet.of("A")));
    assertFailureValue("differs at index", "[0]");
    // Maybe one day:
    // .hasMessage("Not true that <(Set<String>[]) [[A], [B]]> is equal to <[[B], [A]]>");
  }

  @Test
  public void primitiveMultiDimensionalArrayIsEqualTo() {
    assertThat(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6}});
  }

  @Test
  public void primitiveMultiDimensionalArrayIsEqualTo_Fail_UnequalOrdering() {
    expectFailureWhenTestingThat(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6, 7}});
    assertFailureValue("wrong length for index", "[2]");
    assertFailureValueIndexed("expected", 1, "4");
    assertFailureValueIndexed("but was", 1, "3");
  }

  @Test
  public void primitiveMultiDimensionalArrayIsNotEqualTo() {
    assertThat(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isNotEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6, 7}});
  }

  @Test
  public void primitiveMultiDimensionalArrayIsNotEqualTo_Fail_Equal() {
    expectFailureWhenTestingThat(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isNotEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6}});
  }

  @Test
  public void boxedAndUnboxed() {
    expectFailureWhenTestingThat(new Object[] {new int[] {0}})
        .isEqualTo(new Object[] {new Integer[] {0}});
    assertFailureValue("wrong type for index", "[0]");
    assertFailureValueIndexed("expected", 1, "Object[]");
    assertFailureValueIndexed("but was", 1, "int[]");
  }

  private static Object[] objectArray(Object... ts) {
    return ts;
  }

  private static String[] objectArray(String... ts) {
    return ts;
  }

  private static Set[] objectArray(Set... ts) {
    return ts;
  }

  private ObjectArraySubject<?> expectFailureWhenTestingThat(Object[] actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
