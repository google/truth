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
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Integer Subjects.
 *
 * @author David Saff
 * @author Christian Gruber
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class IntegerSubjectTest extends BaseSubjectTestCase {

  @Test
  public void simpleEquality() {
    assertThat(2 + 2).isEqualTo(4);
  }

  @Test
  public void equalityWithLongs() {
    int x = 0;
    assertThat(x).isEqualTo(0L);
    expectFailure.whenTesting().that(x).isNotEqualTo(0L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <0> is not equal to <0>");
  }

  @Test
  public void intIsInt() {
    assertThat(4).isEqualTo(4);
  }

  @Test
  public void simpleInequality() {
    assertThat(2 + 2).isNotEqualTo(5);
  }

  @Test
  public void equalityFail() {
    expectFailure.whenTesting().that(2 + 2).isEqualTo(5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is equal to <5>");
  }

  @Test
  public void inequalityFail() {
    expectFailure.whenTesting().that(2 + 2).isNotEqualTo(4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <4> is not equal to <4>");
  }

  @Test
  public void assertThatIntegerNullIsEqualToNull() {
    assertThat((Integer) null).isEqualTo((Integer) null);
    assertThat((Integer) null).isEqualTo((Long) null);
    assertThat((Integer) null).isEqualTo((Object) null);
  }

  @Test
  public void assertThatLongNullIsEqualToNull() {
    assertThat((Long) null).isEqualTo((Integer) null);
    assertThat((Long) null).isEqualTo((Long) null);
    assertThat((Long) null).isEqualTo((Object) null);
  }

  @Test
  public void equalityOfNullsFail_actualNull() {
    expectFailure.whenTesting().that((Long) null).isEqualTo(5);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <null> is equal to <5>");
  }

  @Test
  public void equalityOfNullsFail_expectNull() {
    expectFailure.whenTesting().that(5).isEqualTo((Integer) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <5> is equal to <null>");
  }

  @Test
  public void assertThatIntegerIsNotEqualToNull() {
    assertThat(4).isNotEqualTo((Long) null);
    assertThat(4).isNotEqualTo((Integer) null);
    assertThat(4).isNotEqualTo((Object) null);
  }

  @Test
  public void assertThatLongIsNotEqualToNull() {
    assertThat(4L).isNotEqualTo((Long) null);
    assertThat(4L).isNotEqualTo((Integer) null);
    assertThat(4L).isNotEqualTo((Object) null);
  }

  @Test
  public void assertThatLongNullIsNotEqualTo() {
    assertThat((Long) null).isNotEqualTo(4);
    assertThat((Long) null).isNotEqualTo(4L);
  }

  @Test
  public void assertThatIntegerNullIsNotEqualTo() {
    assertThat((Integer) null).isNotEqualTo(4);
    assertThat((Integer) null).isNotEqualTo(4L);
  }

  @Test
  public void assertThatIntegerIsEqualToLong() {
    assertThat(4).isEqualTo(new Long(4L));
    assertThat(new Integer(4)).isEqualTo(new Long(4L));
    assertThat(new Integer(4)).isEqualTo(4L);
  }

  @Test
  public void assertThatLongIsEqualToInteger() {
    assertThat(4L).isEqualTo(new Integer(4));
    assertThat(new Long(4L)).isEqualTo(new Integer(4));
    assertThat(new Long(4L)).isEqualTo(4);
  }

  @Test
  public void assertThatIntegerNullIsNotEqualToIntegerNull_shouldFail() {
    expectFailure.whenTesting().that((Integer) null).isNotEqualTo((Integer) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not equal to <null>");
  }

  @Test
  public void assertThatIntegerNullIsNotEqualToLongNull_shouldFail() {
    expectFailure.whenTesting().that((Integer) null).isNotEqualTo((Long) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not equal to <null>");
  }

  @Test
  public void assertThatIntegerNullIsNotEqualToObjectNull_shouldFail() {
    expectFailure.whenTesting().that((Integer) null).isNotEqualTo((Object) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not equal to <null>");
  }

  @Test
  public void assertThatLongNullIsNotEqualToIntegerNull_shouldFail() {
    expectFailure.whenTesting().that((Long) null).isNotEqualTo((Integer) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .contains("Not true that <null> is not equal to <null>");
  }

  @Test
  public void assertThatLongNullIsNotEqualToLongNull_shouldFail() {
    expectFailure.whenTesting().that((Long) null).isNotEqualTo((Long) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not equal to <null>");
  }

  @Test
  public void assertThatLongNullIsNotEqualToObjectNull_shouldFail() {
    expectFailure.whenTesting().that((Long) null).isNotEqualTo((Object) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not equal to <null>");
  }

  @Test
  public void primitives() {
    Assert.assertEquals(4, 4L);
    Assert.assertEquals(4L, 4);
    assertThat(4L).isEqualTo(4);
    assertThat(new Long(4L)).isEqualTo(4);
  }

  @SuppressWarnings("EqualsIncompatibleType")
  @Test
  public void boxedPrimitives() {
    // Java says boxed primitives are not .equals().
    // Check the boolean expression with JUnit and Truth:
    Assert.assertFalse(new Integer(4).equals(new Long(4L)));
    Assert.assertFalse(new Long(4L).equals(new Integer(4)));
    assertThat(new Integer(4).equals(new Long(4L))).isFalse();
    assertThat(new Long(4L).equals(new Integer(4))).isFalse();

    // JUnit says boxed primitives are not .equals()
    try {
      Assert.assertEquals(new Integer(4), new Long(4L)); // this throws!
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("expected: java.lang.Integer<4> but was: java.lang.Long<4>");
    }
    try {
      Assert.assertEquals(new Long(4L), new Integer(4)); // this throws!
      fail("Should have thrown");
    } catch (AssertionError expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("expected: java.lang.Long<4> but was: java.lang.Integer<4>");
    }
  }

  @SuppressWarnings("EqualsIncompatibleType")
  @Test
  public void mixedBoxedAndUnboxedPrimitives() {
    // Java says boxed primitives are not .equals() to primitives.
    Assert.assertFalse(new Integer(4).equals(4L));
    Assert.assertFalse(new Integer(4).equals(new Long(4L)));
    Assert.assertFalse(new Long(4L).equals(4));
    Assert.assertFalse(new Long(4L).equals(new Integer(4)));
    assertThat(new Integer(4).equals(4L)).isFalse();
    assertThat(new Long(4L).equals(4)).isFalse();

    // JUnit won't even let you do this comparison (compile error!)
    // "reference to assertEquals is ambiguous"
    // Assert.assertEquals(new Integer(4), 4L);
    // Assert.assertEquals(4L, new Integer(4));
    // Assert.assertEquals(new Long(4L), 4);
    // Assert.assertEquals(4, new Long(4L));
    // Assert.assertEquals(4, new Integer(4));
    // Assert.assertEquals(new Long(4L), 4L);
  }

  @Test
  public void overflowOnPrimitives() {
    assertThat(Long.MIN_VALUE).isNotEqualTo(Integer.MIN_VALUE);
    assertThat(Long.MAX_VALUE).isNotEqualTo(Integer.MAX_VALUE);

    assertThat(Integer.MIN_VALUE).isNotEqualTo(Long.MIN_VALUE);
    assertThat(Integer.MAX_VALUE).isNotEqualTo(Long.MAX_VALUE);

    assertThat(Integer.MIN_VALUE).isEqualTo((long) Integer.MIN_VALUE);
    assertThat(Integer.MAX_VALUE).isEqualTo((long) Integer.MAX_VALUE);
  }

  @Test
  public void overflowOnPrimitives_shouldBeEqualAfterCast_min() {
    expectFailure.whenTesting().that(Integer.MIN_VALUE).isNotEqualTo((long) Integer.MIN_VALUE);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <-2147483648> is not equal to <-2147483648>");
  }

  @Test
  public void overflowOnPrimitives_shouldBeEqualAfterCast_max() {
    expectFailure.whenTesting().that(Integer.MAX_VALUE).isNotEqualTo((long) Integer.MAX_VALUE);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <2147483647> is not equal to <2147483647>");
  }

  @Test
  public void overflowBetweenIntegerAndLong_shouldBeDifferent_min() {
    expectFailure.whenTesting().that(Integer.MIN_VALUE).isEqualTo(Long.MIN_VALUE);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <-2147483648> is equal to <-9223372036854775808>");
  }

  @Test
  public void overflowBetweenIntegerAndLong_shouldBeDifferent_max() {
    expectFailure.whenTesting().that(Integer.MAX_VALUE).isEqualTo(Long.MAX_VALUE);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <2147483647> is equal to <9223372036854775807>");
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void testPrimitivesVsBoxedPrimitivesVsObject_int() {
    int int42 = 42;
    Integer integer42 = new Integer(42);
    Object object42 = (Object) 42;

    assertThat(int42).isEqualTo(int42);
    assertThat(integer42).isEqualTo(int42);
    assertThat(object42).isEqualTo(int42);

    assertThat(int42).isEqualTo(integer42);
    assertThat(integer42).isEqualTo(integer42);
    assertThat(object42).isEqualTo(integer42);

    assertThat(int42).isEqualTo(object42);
    assertThat(integer42).isEqualTo(object42);
    assertThat(object42).isEqualTo(object42);
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void testPrimitivesVsBoxedPrimitivesVsObject_long() {
    long longPrim42 = 42;
    Long long42 = new Long(42);
    Object object42 = (Object) 42L;

    assertThat(longPrim42).isEqualTo(longPrim42);
    assertThat(long42).isEqualTo(longPrim42);
    assertThat(object42).isEqualTo(longPrim42);

    assertThat(longPrim42).isEqualTo(long42);
    assertThat(long42).isEqualTo(long42);
    assertThat(object42).isEqualTo(long42);

    assertThat(longPrim42).isEqualTo(object42);
    assertThat(long42).isEqualTo(object42);
    assertThat(object42).isEqualTo(object42);
  }

  @Test
  public void testAllCombinations_pass() {
    assertThat(42).isEqualTo(42L);
    assertThat(42).isEqualTo(new Long(42L));
    assertThat(new Integer(42)).isEqualTo(42L);
    assertThat(new Integer(42)).isEqualTo(new Long(42L));
    assertThat(42L).isEqualTo(42);
    assertThat(42L).isEqualTo(new Integer(42));
    assertThat(new Long(42L)).isEqualTo(42);
    assertThat(new Long(42L)).isEqualTo(new Integer(42));

    assertThat(42).isEqualTo(42);
    assertThat(42).isEqualTo(new Integer(42));
    assertThat(new Integer(42)).isEqualTo(42);
    assertThat(new Integer(42)).isEqualTo(new Integer(42));
    assertThat(42L).isEqualTo(42L);
    assertThat(42L).isEqualTo(new Long(42L));
    assertThat(new Long(42L)).isEqualTo(42L);
    assertThat(new Long(42L)).isEqualTo(new Long(42L));
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_int_long() {
    expectFailure.whenTesting().that(42).isNotEqualTo(42L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_int_Long() {
    expectFailure.whenTesting().that(42).isNotEqualTo(new Long(42L));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_Integer_long() {
    expectFailure.whenTesting().that(new Integer(42)).isNotEqualTo(42L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_Integer_Long() {
    expectFailure.whenTesting().that(new Integer(42)).isNotEqualTo(new Long(42L));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_long_int() {
    expectFailure.whenTesting().that(42L).isNotEqualTo(42);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_long_Integer() {
    expectFailure.whenTesting().that(42L).isNotEqualTo(new Integer(42));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_Long_int() {
    expectFailure.whenTesting().that(new Long(42L)).isNotEqualTo(42);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_Long_Integer() {
    expectFailure.whenTesting().that(new Long(42L)).isNotEqualTo(new Integer(42));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_int_int() {
    expectFailure.whenTesting().that(42).isNotEqualTo(42);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_int_Integer() {
    expectFailure.whenTesting().that(42).isNotEqualTo(new Integer(42));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_Integer_int() {
    expectFailure.whenTesting().that(new Integer(42)).isNotEqualTo(42);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_Integer_Integer() {
    expectFailure.whenTesting().that(new Integer(42)).isNotEqualTo(new Integer(42));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_long_long() {
    expectFailure.whenTesting().that(42L).isNotEqualTo(42L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_long_Long() {
    expectFailure.whenTesting().that(42L).isNotEqualTo(new Long(42L));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_Long_long() {
    expectFailure.whenTesting().that(new Long(42L)).isNotEqualTo(42L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericTypeWithSameValue_shouldBeEqual_Long_Long() {
    expectFailure.whenTesting().that(new Long(42L)).isNotEqualTo(new Long(42L));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <42>");
  }

  @Test
  public void testNumericPrimitiveTypes_isNotEqual_shouldFail_intToChar() {
    expectFailure.whenTesting().that(42).isNotEqualTo((char) 42);
    // 42 in ASCII is '*'
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <42> is not equal to <*>");
  }

  @Test
  public void testNumericPrimitiveTypes_isNotEqual_shouldFail_charToInt() {
    expectFailure.whenTesting().that((char) 42).isNotEqualTo(42);
    // 42 in ASCII is '*'
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <*> is not equal to <42>");
  }

  private static final Subject.Factory<DefaultSubject, Object> DEFAULT_SUBJECT_FACTORY =
      new Subject.Factory<DefaultSubject, Object>() {
        @Override
        public DefaultSubject createSubject(FailureMetadata metadata, Object that) {
          return new DefaultSubject(metadata, that);
        }
      };

  private static void expectFailureWithMessage(
      ExpectFailure.SimpleSubjectBuilderCallback<DefaultSubject, Object> callback,
      String failureMessage) {
    AssertionError assertionError =
        ExpectFailure.expectFailureAbout(DEFAULT_SUBJECT_FACTORY, callback);
    assertThat(assertionError).hasMessageThat().isEqualTo(failureMessage);
  }

  @Test
  public void testNumericPrimitiveTypes() {
    byte byte42 = (byte) 42;
    short short42 = (short) 42;
    char char42 = (char) 42;
    int int42 = 42;
    long long42 = (long) 42;

    ImmutableSet<Object> fortyTwos =
        ImmutableSet.<Object>of(byte42, short42, char42, int42, long42);
    for (Object actual : fortyTwos) {
      for (Object expected : fortyTwos) {
        assertThat(actual).isEqualTo(expected);
      }
    }

    ImmutableSet<Object> fortyTwosNoChar = ImmutableSet.<Object>of(byte42, short42, int42, long42);
    for (final Object actual : fortyTwosNoChar) {
      for (final Object expected : fortyTwosNoChar) {
        ExpectFailure.SimpleSubjectBuilderCallback<DefaultSubject, Object> actualFirst =
            new ExpectFailure.SimpleSubjectBuilderCallback<DefaultSubject, Object>() {
              @Override
              public void invokeAssertion(SimpleSubjectBuilder<DefaultSubject, Object> expect) {
                expect.that(actual).isNotEqualTo(expected);
              }
            };
        ExpectFailure.SimpleSubjectBuilderCallback<DefaultSubject, Object> expectedFirst =
            new ExpectFailure.SimpleSubjectBuilderCallback<DefaultSubject, Object>() {
              @Override
              public void invokeAssertion(SimpleSubjectBuilder<DefaultSubject, Object> expect) {
                expect.that(expected).isNotEqualTo(actual);
              }
            };
        expectFailureWithMessage(actualFirst, "Not true that <42> is not equal to <42>");
        expectFailureWithMessage(expectedFirst, "Not true that <42> is not equal to <42>");
      }
    }

    byte byte41 = (byte) 41;
    short short41 = (short) 41;
    char char41 = (char) 41;
    int int41 = 41;
    long long41 = (long) 41;

    ImmutableSet<Object> fortyOnes =
        ImmutableSet.<Object>of(byte41, short41, char41, int41, long41);

    for (Object actual : fortyTwos) {
      for (Object expected : fortyOnes) {
        assertThat(actual).isNotEqualTo(expected);
        assertThat(expected).isNotEqualTo(actual);
      }
    }
  }
}
