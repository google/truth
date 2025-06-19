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

import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.FailureAssertions.assertFailureValueIndexed;
import static com.google.common.truth.Truth.assertThat;

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
public class ObjectArraySubjectTest {
  private static final Object[] EMPTY = new Object[0];

  @Test
  public void isEqualTo() {
    assertThat(array("A", 5L)).isEqualTo(array("A", 5L));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualTo_same() {
    Object[] same = array("A", 5L);
    assertThat(same).isEqualTo(same);
  }

  @Test
  public void asList() {
    assertThat(array("A", 5L)).asList().contains("A");
  }

  @Test
  public void asListOnNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Object[]) null).asList());
    assertFailureKeys(e, "cannot perform assertions on the contents of a null array");
  }

  @Test
  public void hasLength() {
    assertThat(EMPTY).hasLength(0);
    assertThat(array("A", 5L)).hasLength(2);
    assertThat(new Object[][] {}).hasLength(0);
    assertThat(new Object[][] {{}}).hasLength(1);
  }

  @Test
  public void hasLengthFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(array("A", 5L)).hasLength(1));
    assertFailureValue(e, "value of", "array.length");
  }

  @Test
  public void hasLengthMultiFail() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(new Object[][] {{"A"}, {5L}}).hasLength(1));
    assertFailureValue(e, "value of", "array.length");
  }

  @Test
  public void hasLengthNullArray() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Object[]) null).hasLength(1));
    assertFailureKeys(e, "expected an array with length", "but was");
    assertFailureValue(e, "expected an array with length", "1");
  }

  @Test
  public void hasLengthNegative() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(array(2, 5)).hasLength(-1));
    assertFailureKeys(
        e,
        "could not perform length check because expected length was negative",
        "expected length",
        "array was");
    assertFailureValue(e, "expected length", "-1");
    assertFailureValue(e, "array was", "[2, 5]");
  }

  @Test
  public void isEmpty() {
    assertThat(EMPTY).isEmpty();
    assertThat(new Object[][] {}).isEmpty();
  }

  @Test
  public void isEmptyFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(array("A", 5L)).isEmpty());
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void isEmptyNullArray() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Object[]) null).isEmpty());
    assertFailureKeys(e, "expected an empty array", "but was");
  }

  @Test
  public void isNotEmpty() {
    assertThat(array("A", 5L)).isNotEmpty();
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEmpty();
  }

  @Test
  public void isNotEmptyFail() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(EMPTY).isNotEmpty());
    assertFailureKeys(e, "expected not to be empty");
  }

  @Test
  public void isNotEmptyNullArray() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that((Object[]) null).isNotEmpty());
    assertFailureKeys(e, "expected a nonempty array", "but was");
  }

  @Test
  public void isEqualTo_fail_unequalOrdering() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array("A", 5L)).isEqualTo(array(5L, "A")));
    assertFailureValue(e, "differs at index", "[0]");
  }

  @Test
  public void isEqualTo_fail_unequalOrderingMultiDimensional_00() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(new Object[][] {{"A"}, {5L}})
                    .isEqualTo(new Object[][] {{5L}, {"A"}}));
    assertFailureValue(e, "differs at index", "[0][0]");
  }

  @Test
  public void isEqualTo_fail_unequalOrderingMultiDimensional_01() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(new Object[][] {{"A", "B"}, {5L}})
                    .isEqualTo(new Object[][] {{"A"}, {5L}}));
    assertFailureValue(e, "wrong length for index", "[0]");
    assertFailureValueIndexed(e, "expected", 1, "1");
    assertFailureValueIndexed(e, "but was", 1, "2");
  }

  @Test
  public void isEqualTo_fail_unequalOrderingMultiDimensional_11() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(new Object[][] {{"A"}, {5L}})
                    .isEqualTo(new Object[][] {{"A"}, {5L, 6L}}));
    assertFailureValue(e, "wrong length for index", "[1]");
    assertFailureValueIndexed(e, "expected", 1, "2");
    assertFailureValueIndexed(e, "but was", 1, "1");
  }

  @Test
  public void isEqualTo_fail_notAnArray() {
    expectFailure(whenTesting -> whenTesting.that(array("A", 5L)).isEqualTo(new Object()));
  }

  @Test
  public void isNotEqualTo_sameLengths() {
    assertThat(array("A", 5L)).isNotEqualTo(array("C", 5L));
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEqualTo(new Object[][] {{"C"}, {5L}});
  }

  @Test
  public void isNotEqualTo_differentLengths() {
    assertThat(array("A", 5L)).isNotEqualTo(array("A", 5L, "c"));
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEqualTo(new Object[][] {{"A", "c"}, {5L}});
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEqualTo(new Object[][] {{"A"}, {5L}, {"C"}});
  }

  @Test
  public void isNotEqualTo_differentTypes() {
    assertThat(array("A", 5L)).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualTo_failEquals() {
    expectFailure(whenTesting -> whenTesting.that(array("A", 5L)).isNotEqualTo(array("A", 5L)));
  }

  @Test
  public void isNotEqualTo_failEqualsMultiDimensional() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(new Object[][] {{"A"}, {5L}})
                    .isNotEqualTo(new Object[][] {{"A"}, {5L}}));
    assertFailureValue(e, "expected not to be", "[[A], [5]]");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_failSame() {
    Object[] same = array("A", 5L);
    expectFailure(whenTesting -> whenTesting.that(same).isNotEqualTo(same));
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_failSameMultiDimensional() {
    Object[][] same = new Object[][] {{"A"}, {5L}};
    expectFailure(whenTesting -> whenTesting.that(same).isNotEqualTo(same));
  }

  @Test
  public void stringArrayIsEqualTo() {
    assertThat(array("A", "B")).isEqualTo(array("A", "B"));
    assertThat(new String[][] {{"A"}, {"B"}}).isEqualTo(new String[][] {{"A"}, {"B"}});
  }

  @Test
  public void stringArrayAsList() {
    assertThat(array("A", "B")).asList().contains("A");
  }

  @Test
  public void multiDimensionalStringArrayAsList() {
    String[] ab = {"A", "B"};
    assertThat(new String[][] {ab, {"C"}}).asList().contains(ab);
  }

  @Test
  public void stringArrayIsEqualTo_fail_unequalLength() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array("A", "B")).isEqualTo(array("B")));
    assertFailureKeys(e, "expected", "but was", "wrong length", "expected", "but was");
    assertFailureValueIndexed(e, "expected", 1, "1");
    assertFailureValueIndexed(e, "but was", 1, "2");
  }

  @Test
  public void stringArrayIsEqualTo_fail_unequalLengthMultiDimensional() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(new String[][] {{"A"}, {"B"}}).isEqualTo(new String[][] {{"A"}}));
    assertFailureKeys(e, "expected", "but was", "wrong length", "expected", "but was");
    assertFailureValueIndexed(e, "expected", 1, "1");
    assertFailureValueIndexed(e, "but was", 1, "2");
  }

  @Test
  public void stringArrayIsEqualTo_fail_unequalOrdering() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(array("A", "B")).isEqualTo(array("B", "A")));
    assertFailureValue(e, "differs at index", "[0]");
  }

  @Test
  public void stringArrayIsEqualTo_fail_unequalOrderingMultiDimensional() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(new String[][] {{"A"}, {"B"}})
                    .isEqualTo(new String[][] {{"B"}, {"A"}}));
    assertFailureValue(e, "differs at index", "[0][0]");
  }

  @Test
  public void setArrayIsEqualTo_fail_unequalOrdering() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(array(ImmutableSet.of("A"), ImmutableSet.of("B")))
                    .isEqualTo(array(ImmutableSet.of("B"), ImmutableSet.of("A"))));
    assertFailureValue(e, "differs at index", "[0]");
    // Maybe one day:
    // .hasMessage("Not true that <(Set<String>[]) [[A], [B]]> is equal to <[[B], [A]]>");
  }

  @Test
  public void primitiveMultiDimensionalArrayIsEqualTo() {
    assertThat(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6}});
  }

  @Test
  public void primitiveMultiDimensionalArrayIsEqualTo_fail_unequalOrdering() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(new int[][] {{1, 2}, {3}, {4, 5, 6}})
                    .isEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6, 7}}));
    assertFailureValue(e, "wrong length for index", "[2]");
    assertFailureValueIndexed(e, "expected", 1, "4");
    assertFailureValueIndexed(e, "but was", 1, "3");
  }

  @Test
  public void primitiveMultiDimensionalArrayIsNotEqualTo() {
    assertThat(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isNotEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6, 7}});
  }

  @Test
  public void primitiveMultiDimensionalArrayIsNotEqualTo_fail_equal() {
    expectFailure(
        whenTesting ->
            whenTesting
                .that(new int[][] {{1, 2}, {3}, {4, 5, 6}})
                .isNotEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6}}));
  }

  @Test
  public void boxedAndUnboxed() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(new Object[] {new int[] {0}})
                    .isEqualTo(new Object[] {new Integer[] {0}}));
    assertFailureValue(e, "wrong type for index", "[0]");
    assertFailureValueIndexed(e, "expected", 1, "Object[]");
    assertFailureValueIndexed(e, "but was", 1, "int[]");
  }

  private static Object[] array(Object... ts) {
    return ts;
  }

  private static String[] array(String... ts) {
    return ts;
  }

  private static Set<?>[] array(Set<?>... ts) {
    return ts;
  }
}
