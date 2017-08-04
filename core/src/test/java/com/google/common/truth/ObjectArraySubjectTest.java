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
import java.util.Set;
import org.junit.Rule;
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
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();
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
    expectFailure.whenTesting().that(objectArray("A", 5L)).hasLength(1);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(Object[]) [A, 5]> has length <1>");
  }

  @Test
  public void hasLengthMultiFail() {
    expectFailure.whenTesting().that(new Object[][] {{"A"}, {5L}}).hasLength(1);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(Object[][]) [[A], [5]]> has length <1>");
  }

  @Test
  public void hasLengthNegative() {
    try {
      assertThat(objectArray(2, 5)).hasLength(-1);
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
    expectFailure.whenTesting().that(objectArray("A", 5L)).isEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(Object[]) [A, 5]> is empty");
  }

  @Test
  public void isNotEmpty() {
    assertThat(objectArray("A", 5L)).isNotEmpty();
    assertThat(new Object[][] {{"A"}, {5L}}).isNotEmpty();
  }

  @Test
  public void isNotEmptyFail() {
    expectFailure.whenTesting().that(EMPTY).isNotEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <(Object[]) []> is not empty");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrdering() {
    expectFailure.whenTesting().that(objectArray("A", 5L)).isEqualTo(objectArray(5L, "A"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(Object[]) [A, 5]> is equal to <[5, A]>. It differs at index <[0]>");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrderingMultiDimensional_00() {
    expectFailure
        .whenTesting()
        .that(new Object[][] {{"A"}, {5L}})
        .isEqualTo(new Object[][] {{5L}, {"A"}});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(Object[][]) [[A], [5]]> is equal to <[[5], [A]]>."
                + " It differs at index <[0][0]>");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrderingMultiDimensional_01() {

    expectFailure
        .whenTesting()
        .that(new Object[][] {{"A", "B"}, {5L}})
        .isEqualTo(new Object[][] {{"A"}, {5L}});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(Object[][]) [[A, B], [5]]> is equal to <[[A], [5]]>."
                + " It differs at index <[0][1]>");
  }

  @Test
  public void isEqualTo_Fail_UnequalOrderingMultiDimensional_11() {
    expectFailure
        .whenTesting()
        .that(new Object[][] {{"A"}, {5L}})
        .isEqualTo(new Object[][] {{"A"}, {5L, 6L}});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(Object[][]) [[A], [5]]> is equal to <[[A], [5, 6]]>."
                + " It differs at index <[1][1]>");
  }

  @Test
  public void isEqualTo_Fail_NotAnArray() {
    expectFailure.whenTesting().that(objectArray("A", 5L)).isEqualTo(new Object());
    AssertionError e = expectFailure.getFailure();
    assertThat(e).hasMessageThat().contains("Incompatible types compared.");
    assertThat(e).hasMessageThat().contains("expected: Object");
    assertThat(e).hasMessageThat().contains("actual: Object[]");
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
    expectFailure.whenTesting().that(objectArray("A", 5L)).isNotEqualTo(objectArray("A", 5L));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(Object[]) [A, 5]> unexpectedly equal to [A, 5].");
  }

  @Test
  public void isNotEqualTo_FailEqualsMultiDimensional() {
    expectFailure
        .whenTesting()
        .that(new Object[][] {{"A"}, {5L}})
        .isNotEqualTo(new Object[][] {{"A"}, {5L}});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(Object[][]) [[A], [5]]> unexpectedly equal to [[A], [5]].");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSame() {
    Object[] same = objectArray("A", 5L);
    expectFailure.whenTesting().that(same).isNotEqualTo(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(Object[]) [A, 5]> unexpectedly equal to [A, 5].");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualTo_FailSameMultiDimensional() {
    Object[][] same = new Object[][] {{"A"}, {5L}};
    expectFailure.whenTesting().that(same).isNotEqualTo(same);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(Object[][]) [[A], [5]]> unexpectedly equal to [[A], [5]].");
  }

  private static Object[] objectArray(Object... ts) {
    return ts;
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
    expectFailure.whenTesting().that(objectArray("A", "B")).isEqualTo(objectArray("B"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(String[]) [A, B]> has length 2. Expected length is 1");
  }

  @Test
  public void stringArrayIsEqualTo_Fail_UnequalLengthMultiDimensional() {
    expectFailure
        .whenTesting()
        .that(new String[][] {{"A"}, {"B"}})
        .isEqualTo(new String[][] {{"A"}});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<(String[][]) [[A], [B]]> has length 2. Expected length is 1");
  }

  @Test
  public void stringArrayIsEqualTo_Fail_UnequalOrdering() {
    expectFailure.whenTesting().that(objectArray("A", "B")).isEqualTo(objectArray("B", "A"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(String[]) [A, B]> is equal to <[B, A]>. It differs at index <[0]>");
  }

  @Test
  public void stringArrayIsEqualTo_Fail_UnequalOrderingMultiDimensional() {
    expectFailure
        .whenTesting()
        .that(new String[][] {{"A"}, {"B"}})
        .isEqualTo(new String[][] {{"B"}, {"A"}});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(String[][]) [[A], [B]]> is equal to <[[B], [A]]>."
                + " It differs at index <[0][0]>");
  }

  private static String[] objectArray(String... ts) {
    return ts;
  }

  @Test
  public void SetArrayIsEqualTo_Fail_UnequalOrdering() {
    expectFailure
        .whenTesting()
        .that(objectArray(ImmutableSet.of("A"), ImmutableSet.of("B")))
        .isEqualTo(objectArray(ImmutableSet.of("B"), ImmutableSet.of("A")));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(Set[]) [[A], [B]]> is equal to <[[B], [A]]>. "
                + "It differs at index <[0]>");
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
    expectFailure
        .whenTesting()
        .that(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6, 7}});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <(int[][]) [[1, 2], [3], [4, 5, 6]]> "
                + "is equal to <[[1, 2], [3], [4, 5, 6, 7]]>. It differs at index <[2][3]>");
  }

  @Test
  public void primitiveMultiDimensionalArrayIsNotEqualTo() {
    assertThat(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isNotEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6, 7}});
  }

  @Test
  public void primitiveMultiDimensionalArrayIsNotEqualTo_Fail_Equal() {
    expectFailure
        .whenTesting()
        .that(new int[][] {{1, 2}, {3}, {4, 5, 6}})
        .isNotEqualTo(new int[][] {{1, 2}, {3}, {4, 5, 6}});
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "<(int[][]) [[1, 2], [3], [4, 5, 6]]> unexpectedly "
                + "equal to [[1, 2], [3], [4, 5, 6]].");
  }

  private static Set[] objectArray(Set... ts) {
    return ts;
  }
}
