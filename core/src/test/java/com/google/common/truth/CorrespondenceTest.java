/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.truth.Correspondence.tolerance;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Correspondence}.
 *
 * @author Pete Gillin
 */
@RunWith(JUnit4.class)
public final class CorrespondenceTest extends BaseSubjectTestCase {
  // Tests of the abstract base class (just assert that equals and hashCode throw).

  private static final Correspondence<Object, Object> INSTANCE =
      new Correspondence<Object, Object>() {

        @Override
        public boolean compare(Object actual, Object expected) {
          return false;
        }

        @Override
        public String toString() {
          return "has example property";
        }
      };

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void testEquals_throws() {
    try {
      INSTANCE.equals(new Object());
      fail("Expected UnsupportedOperationException from Correspondence.equals");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated method
  public void testHashCode_throws() {
    try {
      INSTANCE.hashCode();
      fail("Expected UnsupportedOperationException from Correspondence.hashCode");
    } catch (UnsupportedOperationException expected) {
    }
  }

  // Tests of the 'from' factory method.

  private static final Correspondence<String, String> STRING_PREFIX_EQUALITY =
      Correspondence.from(
          new Correspondence.BinaryPredicate<String, String>() {
            @Override
            public boolean apply(String actual, String expected) {
              return actual.startsWith(expected);
            }
          },
          "starts with");

  @Test
  public void testFrom_compare() {
    assertThat(STRING_PREFIX_EQUALITY.compare("foot", "foo")).isTrue();
    assertThat(STRING_PREFIX_EQUALITY.compare("foot", "foot")).isTrue();
    assertThat(STRING_PREFIX_EQUALITY.compare("foo", "foot")).isFalse();
  }

  @Test
  public void testFrom_formatDiff() {
    assertThat(STRING_PREFIX_EQUALITY.formatDiff("foo", "foot")).isNull();
  }

  @Test
  public void testFrom_toString() {
    assertThat(STRING_PREFIX_EQUALITY.toString()).isEqualTo("starts with");
  }

  @Test
  public void testFrom_viaIterableSubjectContainsExactly_success() {
    assertThat(ImmutableList.of("foot", "barn"))
        .comparingElementsUsing(STRING_PREFIX_EQUALITY)
        .containsExactly("foo", "bar");
  }

  @Test
  public void testFrom_viaIterableSubjectContainsExactly_failure() {
    expectFailure
        .whenTesting()
        .that(ImmutableList.of("foot", "barn", "gallon"))
        .comparingElementsUsing(STRING_PREFIX_EQUALITY)
        .containsExactly("foot", "barn");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[foot, barn, gallon]> contains exactly one element that starts with "
                + "each element of <[foot, barn]>. It has unexpected elements <[gallon]>");
  }

  @Test
  public void testFrom_viaIterableSubjectContainsExactly_null() {
    expectFailure
        .whenTesting()
        .that(asList("foot", "barn", null))
        .comparingElementsUsing(STRING_PREFIX_EQUALITY)
        .containsExactly("foot", "barn");
    assertFailureKeys(
        "Not true that <[foot, barn, null]> contains exactly one element that starts with each "
            + "element of <[foot, barn]>. It has unexpected elements <[null]>",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, foot) threw java.lang.NullPointerException");
  }

  // Tests of the 'transform' factory methods.

  private static final Correspondence<String, Integer> LENGTHS =
      Correspondence.transforming(
          new Function<String, Integer>() {
            @Override
            @NullableDecl
            public Integer apply(String str) {
              return str.length();
            }
          },
          "has a length of");

  private static final Correspondence<String, Integer> HYPHEN_INDEXES =
      Correspondence.transforming(
          new Function<String, Integer>() {
            @Override
            @NullableDecl
            public Integer apply(String str) {
              int index = str.indexOf('-');
              return (index >= 0) ? index : null;
            }
          },
          "has a hyphen at an index of");

  @Test
  public void testTransforming_actual_compare() {
    assertThat(LENGTHS.compare("foo", 3)).isTrue();
    assertThat(LENGTHS.compare("foot", 4)).isTrue();
    assertThat(LENGTHS.compare("foo", 4)).isFalse();
  }

  @Test
  public void testTransforming_actual_formatDiff() {
    assertThat(LENGTHS.formatDiff("foo", 4)).isNull();
  }

  @Test
  public void testTransforming_actual_toString() {
    assertThat(LENGTHS.toString()).isEqualTo("has a length of");
  }

  @Test
  public void testTransforming_actual_viaIterableSubjectContainsExactly_success() {
    assertThat(ImmutableList.of("feet", "barns", "gallons"))
        .comparingElementsUsing(LENGTHS)
        .containsExactly(4, 5, 7)
        .inOrder();
  }

  @Test
  public void testTransforming_actual_viaIterableSubjectContainsExactly_failure() {
    expectFailure
        .whenTesting()
        .that(ImmutableList.of("feet", "barns", "gallons"))
        .comparingElementsUsing(LENGTHS)
        .containsExactly(4, 5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[feet, barns, gallons]> contains exactly one element that has a length "
                + "of each element of <[4, 5]>. It has unexpected elements <[gallons]>");
  }

  @Test
  public void testTransforming_actual_viaIterableSubjectContainsExactly_nullActual() {
    expectFailure
        .whenTesting()
        .that(asList("feet", "barns", null))
        .comparingElementsUsing(LENGTHS)
        .containsExactly(4, 5);
    assertFailureKeys(
        "Not true that <[feet, barns, null]> contains exactly one element that has a length of "
            + "each element of <[4, 5]>. It has unexpected elements <[null]>",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, 4) threw java.lang.NullPointerException");
  }

  @Test
  public void testTransforming_actual_viaIterableSubjectContainsExactly_nullTransformed() {
    // "mailing-list" and "chat-room" have hyphens at index 7 and 4 respectively.
    // "forum" contains no hyphen so the Function in HYPHEN_INDEXES transforms it to null.
    assertThat(ImmutableList.of("mailing-list", "chat-room", "forum"))
        .comparingElementsUsing(HYPHEN_INDEXES)
        .containsExactly(7, 4, null)
        .inOrder();
  }

  // Tests of the 'tolerance' factory method. Includes both direct tests of the compare method and
  // indirect tests using it in a basic call chain.

  @Test
  public void testTolerance_compare_doubles() {
    assertThat(tolerance(0.0).compare(2.0, 2.0)).isTrue();
    assertThat(tolerance(0.00001).compare(2.0, 2.0)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0, 2.0)).isTrue();
    assertThat(tolerance(1.00001).compare(2.0, 3.0)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0, 1003.0)).isFalse();
    assertThat(tolerance(1000.0).compare(2.0, Double.POSITIVE_INFINITY)).isFalse();
    assertThat(tolerance(1000.0).compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
        .isFalse();
    assertThat(tolerance(1000.0).compare(2.0, Double.NaN)).isFalse();
    assertThat(tolerance(1000.0).compare(Double.NaN, Double.NaN)).isFalse();
    assertThat(tolerance(0.0).compare(-0.0, 0.0)).isTrue();
  }

  @Test
  public void testTolerance_compare_floats() {
    assertThat(tolerance(0.0).compare(2.0f, 2.0f)).isTrue();
    assertThat(tolerance(0.00001).compare(2.0f, 2.0f)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0f, 2.0f)).isTrue();
    assertThat(tolerance(1.00001).compare(2.0f, 3.0f)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0f, 1003.0f)).isFalse();
    assertThat(tolerance(1000.0).compare(2.0f, Float.POSITIVE_INFINITY)).isFalse();
    assertThat(tolerance(1000.0).compare(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY))
        .isFalse();
    assertThat(tolerance(1000.0).compare(2.0f, Float.NaN)).isFalse();
    assertThat(tolerance(1000.0).compare(Float.NaN, Float.NaN)).isFalse();
    assertThat(tolerance(0.0).compare(-0.0f, 0.0f)).isTrue();
  }

  @Test
  public void testTolerance_compare_doublesVsInts() {
    assertThat(tolerance(0.0).compare(2.0, 2)).isTrue();
    assertThat(tolerance(0.00001).compare(2.0, 2)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0, 2)).isTrue();
    assertThat(tolerance(1.00001).compare(2.0, 3)).isTrue();
    assertThat(tolerance(1000.0).compare(2.0, 1003)).isFalse();
  }

  @Test
  public void testTolerance_compare_negativeTolerance() {
    try {
      tolerance(-0.05).compare(1.0, 2.0);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("tolerance (-0.05) cannot be negative");
    }
  }

  @Test
  public void testTolerance_compare_null() {
    try {
      tolerance(0.05).compare(1.0, null);
      fail("Expected NullPointerException to be thrown but wasn't");
    } catch (NullPointerException expected) {
    }
    try {
      tolerance(0.05).compare(null, 2.0);
      fail("Expected NullPointerException to be thrown but wasn't");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testTolerance_viaIterableSubjectContains_success() {
    assertThat(ImmutableList.of(1.02, 2.04, 3.08))
        .comparingElementsUsing(tolerance(0.05))
        .contains(2.0);
  }

  @Test
  public void testTolerance_viaIterableSubjectContains_failure() {
    expectFailure
        .whenTesting()
        .that(ImmutableList.of(1.02, 2.04, 3.08))
        .comparingElementsUsing(tolerance(0.05))
        .contains(3.01);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1.02, 2.04, 3.08]> contains at least one element that "
                + "is a finite number within 0.05 of <3.01>");
  }
}
