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

import static com.google.common.truth.StringUtil.format;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.annotations.GwtIncompatible;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Float Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class FloatSubjectTest extends BaseSubjectTestCase {
  private static final float NEARLY_MAX = 3.4028233E38f;
  private static final float NEGATIVE_MAX = -3.4028235E38f;
  private static final float NEGATIVE_NEARLY_MAX = -3.4028233E38f;
  private static final float JUST_OVER_MIN = 2.8E-45f;
  private static final float NEGATIVE_MIN = -1.4E-45f;
  private static final float JUST_UNDER_NEGATIVE_MIN = -2.8E-45f;
  private static final float GOLDEN = 1.23f;
  private static final float JUST_OVER_GOLDEN = 1.2300001f;

  private static final Subject.Factory<FloatSubject, Float> FLOAT_SUBJECT_FACTORY =
      new Subject.Factory<FloatSubject, Float>() {
        @Override
        public FloatSubject createSubject(FailureMetadata metadata, Float that) {
          return new FloatSubject(metadata, that);
        }
      };

  private static void expectFailureWithMessage(
      ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback,
      String failureMessage) {
    AssertionError assertionError =
        ExpectFailure.expectFailureAbout(FLOAT_SUBJECT_FACTORY, callback);
    assertThat(assertionError).hasMessageThat().isEqualTo(failureMessage);
  }

  @Test
  @GwtIncompatible("Math.nextAfter")
  public void testFloatConstants_matchNextAfter() {
    assertThat(Math.nextAfter(Float.MAX_VALUE, 0.0f)).isEqualTo(NEARLY_MAX);
    assertThat(-1.0f * Float.MAX_VALUE).isEqualTo(NEGATIVE_MAX);
    assertThat(Math.nextAfter(-1.0f * Float.MAX_VALUE, 0.0f)).isEqualTo(NEGATIVE_NEARLY_MAX);
    assertThat(Math.nextAfter(Float.MIN_VALUE, 1.0f)).isEqualTo(JUST_OVER_MIN);
    assertThat(-1.0f * Float.MIN_VALUE).isEqualTo(NEGATIVE_MIN);
    assertThat(Math.nextAfter(-1.0f * Float.MIN_VALUE, -1.0f)).isEqualTo(JUST_UNDER_NEGATIVE_MIN);
    assertThat(1.23f).isEqualTo(GOLDEN);
    assertThat(Math.nextAfter(1.23f, Float.POSITIVE_INFINITY)).isEqualTo(JUST_OVER_GOLDEN);
  }

  @Test
  @GwtIncompatible("GWT behavior difference")
  public void j2clCornerCases() {
    // From Float#equals(Object), though Float.NaN != Float.NaN, Float.NaN.equals(Float.NaN) should
    // be true, yet it's not under GWT
    assertThat(Float.NaN).isEqualTo(Float.NaN);
    assertThat(Float.NaN).isNaN();

    // 0.0f and -0.0f should be different
    assertThat(-0.0f).isNotEqualTo(0.0f);
    assertThatIsEqualToFails(-0.0f, 0.0f);

    // Under GWT, 1.23f.toString() is different than 1.23d.toString(), so the message omits types.
    expectFailure.whenTesting().that(1.23f).isEqualTo(1.23);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            format(
                "Not true that <%s> (java.lang.Float) is equal to <%s> (java.lang.Double)",
                1.23f, 1.23));
  }

  @Test
  public void isWithinOf() {
    assertThat(2.0f).isWithin(0.0f).of(2.0f);
    assertThat(2.0f).isWithin(0.00001f).of(2.0f);
    assertThat(2.0f).isWithin(1000.0f).of(2.0f);
    assertThat(2.0f).isWithin(1.00001f).of(3.0f);
    assertThatIsWithinFails(2.0f, 0.99999f, 3.0f);
    assertThatIsWithinFails(2.0f, 1000.0f, 1003.0f);
    assertThatIsWithinFails(2.0f, 1000.0f, Float.POSITIVE_INFINITY);
    assertThatIsWithinFails(2.0f, 1000.0f, Float.NaN);
    assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 1000.0f, 2.0f);
    assertThatIsWithinFails(Float.NaN, 1000.0f, 2.0f);
  }

  private static void assertThatIsWithinFails(
      final float actual, final float tolerance, final float expected) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(actual).named("testValue").isWithin(tolerance).of(expected);
          }
        };
    expectFailureWithMessage(
        callback,
        format(
            "testValue (<%s>) and <%s> should have been finite values within"
                + " <%s> of each other",
            actual, expected, tolerance));
  }

  @Test
  public void isNotWithinOf() {
    assertThatIsNotWithinFails(2.0f, 0.0f, 2.0f);
    assertThatIsNotWithinFails(2.0f, 0.00001f, 2.0f);
    assertThatIsNotWithinFails(2.0f, 1000.0f, 2.0f);
    assertThatIsNotWithinFails(2.0f, 1.00001f, 3.0f);
    assertThat(2.0f).isNotWithin(0.99999f).of(3.0f);
    assertThat(2.0f).isNotWithin(1000.0f).of(1003.0f);
    assertThatIsNotWithinFails(2.0f, 0.0f, Float.POSITIVE_INFINITY);
    assertThatIsNotWithinFails(2.0f, 0.0f, Float.NaN);
    assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 1000.0f, 2.0f);
    assertThatIsNotWithinFails(Float.NaN, 1000.0f, 2.0f);
  }

  private static void assertThatIsNotWithinFails(
      final float actual, final float tolerance, final float expected) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(actual).named("testValue").isNotWithin(tolerance).of(expected);
          }
        };
    expectFailureWithMessage(
        callback,
        format(
            "testValue (<%s>) and <%s> should have been finite values not within"
                + " <%s> of each other",
            actual, expected, tolerance));
  }

  @Test
  public void negativeTolerances() {
    isWithinNegativeToleranceThrowsIAE(5.0f, -0.5f, 4.9f);
    isWithinNegativeToleranceThrowsIAE(5.0f, -0.5f, 4.0f);

    isNotWithinNegativeToleranceThrowsIAE(5.0f, -0.5f, 4.9f);
    isNotWithinNegativeToleranceThrowsIAE(5.0f, -0.5f, 4.0f);

    isWithinNegativeToleranceThrowsIAE(+0.0f, -0.00001f, +0.0f);
    isWithinNegativeToleranceThrowsIAE(+0.0f, -0.00001f, -0.0f);
    isWithinNegativeToleranceThrowsIAE(-0.0f, -0.00001f, +0.0f);
    isWithinNegativeToleranceThrowsIAE(-0.0f, -0.00001f, -0.0f);

    isNotWithinNegativeToleranceThrowsIAE(+0.0f, -0.00001f, +1.0f);
    isNotWithinNegativeToleranceThrowsIAE(+0.0f, -0.00001f, -1.0f);
    isNotWithinNegativeToleranceThrowsIAE(-0.0f, -0.00001f, +1.0f);
    isNotWithinNegativeToleranceThrowsIAE(-0.0f, -0.00001f, -1.0f);

    isNotWithinNegativeToleranceThrowsIAE(+1.0f, -0.00001f, +0.0f);
    isNotWithinNegativeToleranceThrowsIAE(+1.0f, -0.00001f, -0.0f);
    isNotWithinNegativeToleranceThrowsIAE(-1.0f, -0.00001f, +0.0f);
    isNotWithinNegativeToleranceThrowsIAE(-1.0f, -0.00001f, -0.0f);

    // You know what's worse than zero? Negative zero.

    isWithinNegativeToleranceThrowsIAE(+0.0f, -0.0f, +0.0f);
    isWithinNegativeToleranceThrowsIAE(+0.0f, -0.0f, -0.0f);
    isWithinNegativeToleranceThrowsIAE(-0.0f, -0.0f, +0.0f);
    isWithinNegativeToleranceThrowsIAE(-0.0f, -0.0f, -0.0f);

    isNotWithinNegativeToleranceThrowsIAE(+1.0f, -0.0f, +0.0f);
    isNotWithinNegativeToleranceThrowsIAE(+1.0f, -0.0f, -0.0f);
    isNotWithinNegativeToleranceThrowsIAE(-1.0f, -0.0f, +0.0f);
    isNotWithinNegativeToleranceThrowsIAE(-1.0f, -0.0f, -0.0f);
  }

  private static void isWithinNegativeToleranceThrowsIAE(
      float actual, float tolerance, float expected) {
    try {
      assertThat(actual).isWithin(tolerance).of(expected);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae)
          .hasMessageThat()
          .isEqualTo("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  private static void isNotWithinNegativeToleranceThrowsIAE(
      float actual, float tolerance, float expected) {
    try {
      assertThat(actual).isNotWithin(tolerance).of(expected);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae)
          .hasMessageThat()
          .isEqualTo("tolerance (" + tolerance + ") cannot be negative");
    }
  }

  @Test
  public void nanTolerances() {
    try {
      assertThat(1.0f).isWithin(Float.NaN).of(1.0f);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be NaN");
    }
    try {
      assertThat(1.0f).isNotWithin(Float.NaN).of(2.0f);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be NaN");
    }
  }

  @Test
  public void infiniteTolerances() {
    try {
      assertThat(1.0f).isWithin(Float.POSITIVE_INFINITY).of(1.0f);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be POSITIVE_INFINITY");
    }
    try {
      assertThat(1.0f).isNotWithin(Float.POSITIVE_INFINITY).of(2.0f);
      fail("Expected IllegalArgumentException to be thrown but wasn't");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be POSITIVE_INFINITY");
    }
  }

  @Test
  public void isWithinOfZero() {
    assertThat(+0.0f).isWithin(0.00001f).of(+0.0f);
    assertThat(+0.0f).isWithin(0.00001f).of(-0.0f);
    assertThat(-0.0f).isWithin(0.00001f).of(+0.0f);
    assertThat(-0.0f).isWithin(0.00001f).of(-0.0f);

    assertThat(+0.0f).isWithin(0.0f).of(+0.0f);
    assertThat(+0.0f).isWithin(0.0f).of(-0.0f);
    assertThat(-0.0f).isWithin(0.0f).of(+0.0f);
    assertThat(-0.0f).isWithin(0.0f).of(-0.0f);
  }

  @Test
  public void isNotWithinOfZero() {
    assertThat(+0.0f).isNotWithin(0.00001f).of(+1.0f);
    assertThat(+0.0f).isNotWithin(0.00001f).of(-1.0f);
    assertThat(-0.0f).isNotWithin(0.00001f).of(+1.0f);
    assertThat(-0.0f).isNotWithin(0.00001f).of(-1.0f);

    assertThat(+1.0f).isNotWithin(0.00001f).of(+0.0f);
    assertThat(+1.0f).isNotWithin(0.00001f).of(-0.0f);
    assertThat(-1.0f).isNotWithin(0.00001f).of(+0.0f);
    assertThat(-1.0f).isNotWithin(0.00001f).of(-0.0f);

    assertThat(+1.0f).isNotWithin(0.0f).of(+0.0f);
    assertThat(+1.0f).isNotWithin(0.0f).of(-0.0f);
    assertThat(-1.0f).isNotWithin(0.0f).of(+0.0f);
    assertThat(-1.0f).isNotWithin(0.0f).of(-0.0f);

    assertThatIsNotWithinFails(-0.0f, 0.0f, 0.0f);
  }

  @Test
  public void isWithinZeroTolerance() {
    float max = Float.MAX_VALUE;
    float nearlyMax = NEARLY_MAX;
    assertThat(max).isWithin(0.0f).of(max);
    assertThat(nearlyMax).isWithin(0.0f).of(nearlyMax);
    assertThatIsWithinFails(max, 0.0f, nearlyMax);
    assertThatIsWithinFails(nearlyMax, 0.0f, max);

    float negativeMax = -1.0f * Float.MAX_VALUE;
    float negativeNearlyMax = NEGATIVE_NEARLY_MAX;
    assertThat(negativeMax).isWithin(0.0f).of(negativeMax);
    assertThat(negativeNearlyMax).isWithin(0.0f).of(negativeNearlyMax);
    assertThatIsWithinFails(negativeMax, 0.0f, negativeNearlyMax);
    assertThatIsWithinFails(negativeNearlyMax, 0.0f, negativeMax);

    float min = Float.MIN_VALUE;
    float justOverMin = JUST_OVER_MIN;
    assertThat(min).isWithin(0.0f).of(min);
    assertThat(justOverMin).isWithin(0.0f).of(justOverMin);
    assertThatIsWithinFails(min, 0.0f, justOverMin);
    assertThatIsWithinFails(justOverMin, 0.0f, min);

    float negativeMin = -1.0f * Float.MIN_VALUE;
    float justUnderNegativeMin = JUST_UNDER_NEGATIVE_MIN;
    assertThat(negativeMin).isWithin(0.0f).of(negativeMin);
    assertThat(justUnderNegativeMin).isWithin(0.0f).of(justUnderNegativeMin);
    assertThatIsWithinFails(negativeMin, 0.0f, justUnderNegativeMin);
    assertThatIsWithinFails(justUnderNegativeMin, 0.0f, negativeMin);
  }

  @Test
  public void isNotWithinZeroTolerance() {
    float max = Float.MAX_VALUE;
    float nearlyMax = NEARLY_MAX;
    assertThatIsNotWithinFails(max, 0.0f, max);
    assertThatIsNotWithinFails(nearlyMax, 0.0f, nearlyMax);
    assertThat(max).isNotWithin(0.0f).of(nearlyMax);
    assertThat(nearlyMax).isNotWithin(0.0f).of(max);

    float min = Float.MIN_VALUE;
    float justOverMin = JUST_OVER_MIN;
    assertThatIsNotWithinFails(min, 0.0f, min);
    assertThatIsNotWithinFails(justOverMin, 0.0f, justOverMin);
    assertThat(min).isNotWithin(0.0f).of(justOverMin);
    assertThat(justOverMin).isNotWithin(0.0f).of(min);
  }

  @Test
  public void isWithinNonFinite() {
    assertThatIsWithinFails(Float.NaN, 0.00001f, Float.NaN);
    assertThatIsWithinFails(Float.NaN, 0.00001f, Float.POSITIVE_INFINITY);
    assertThatIsWithinFails(Float.NaN, 0.00001f, Float.NEGATIVE_INFINITY);
    assertThatIsWithinFails(Float.NaN, 0.00001f, +0.0f);
    assertThatIsWithinFails(Float.NaN, 0.00001f, -0.0f);
    assertThatIsWithinFails(Float.NaN, 0.00001f, +1.0f);
    assertThatIsWithinFails(Float.NaN, 0.00001f, -0.0f);
    assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, Float.POSITIVE_INFINITY);
    assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, Float.NEGATIVE_INFINITY);
    assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, +0.0f);
    assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, +1.0f);
    assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, Float.NEGATIVE_INFINITY);
    assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, +0.0f);
    assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, +1.0f);
    assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsWithinFails(+1.0f, 0.00001f, Float.NaN);
    assertThatIsWithinFails(+1.0f, 0.00001f, Float.POSITIVE_INFINITY);
    assertThatIsWithinFails(+1.0f, 0.00001f, Float.NEGATIVE_INFINITY);
  }

  @Test
  public void isNotWithinNonFinite() {
    assertThatIsNotWithinFails(Float.NaN, 0.00001f, Float.NaN);
    assertThatIsNotWithinFails(Float.NaN, 0.00001f, Float.POSITIVE_INFINITY);
    assertThatIsNotWithinFails(Float.NaN, 0.00001f, Float.NEGATIVE_INFINITY);
    assertThatIsNotWithinFails(Float.NaN, 0.00001f, +0.0f);
    assertThatIsNotWithinFails(Float.NaN, 0.00001f, -0.0f);
    assertThatIsNotWithinFails(Float.NaN, 0.00001f, +1.0f);
    assertThatIsNotWithinFails(Float.NaN, 0.00001f, -0.0f);
    assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, Float.POSITIVE_INFINITY);
    assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, Float.NEGATIVE_INFINITY);
    assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, +0.0f);
    assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, +1.0f);
    assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, Float.NEGATIVE_INFINITY);
    assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, +0.0f);
    assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, +1.0f);
    assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, -0.0f);
    assertThatIsNotWithinFails(+1.0f, 0.00001f, Float.NaN);
    assertThatIsNotWithinFails(+1.0f, 0.00001f, Float.POSITIVE_INFINITY);
    assertThatIsNotWithinFails(+1.0f, 0.00001f, Float.NEGATIVE_INFINITY);
  }

  @Test
  public void isEqualTo() {
    float golden = GOLDEN;
    float justOverGolden = JUST_OVER_GOLDEN;
    assertThat(golden).isEqualTo(golden);
    assertThatIsEqualToFails(golden, justOverGolden);
    assertThat(Float.POSITIVE_INFINITY).isEqualTo(Float.POSITIVE_INFINITY);
    assertThat((Float) null).isEqualTo(null);
  }

  private static void assertThatIsEqualToFails(final float actual, final float expected) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(actual).isEqualTo(expected);
          }
        };
    expectFailureWithMessage(
        callback, format("Not true that <%s> is equal to <%s>", actual, expected));
  }

  @Test
  public void isNotEqualTo() {
    float golden = GOLDEN;
    float justOverGolden = JUST_OVER_GOLDEN;
    assertThatIsNotEqualToFails(golden);
    assertThat(golden).isNotEqualTo(justOverGolden);
    assertThatIsNotEqualToFails(Float.POSITIVE_INFINITY);
    assertThatIsNotEqualToFails(Float.NaN);
    assertThatIsNotEqualToFails(null);
    assertThat(1.23f).isNotEqualTo(1.23);
  }

  private static void assertThatIsNotEqualToFails(@Nullable final Float value) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(value).isNotEqualTo(value);
          }
        };
    expectFailureWithMessage(
        callback, format("Not true that <%s> is not equal to <%s>", value, value));
  }

  @Test
  public void isZero() {
    assertThat(0.0f).isZero();
    assertThat(-0.0f).isZero();
    assertThatIsZeroFails(Float.MIN_VALUE);
    assertThatIsZeroFails(-1.23f);
    assertThatIsZeroFails(Float.POSITIVE_INFINITY);
    assertThatIsZeroFails(Float.NaN);
    assertThatIsZeroFails(null);
  }

  private static void assertThatIsZeroFails(@Nullable final Float value) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(value).named("testValue").isZero();
          }
        };
    expectFailureWithMessage(callback, "Not true that testValue (<" + value + ">) is zero");
  }

  @Test
  public void isNonZero() {
    assertThatIsNonZeroFails(0.0f);
    assertThatIsNonZeroFails(-0.0f);
    assertThat(Float.MIN_VALUE).isNonZero();
    assertThat(-1.23f).isNonZero();
    assertThat(Float.POSITIVE_INFINITY).isNonZero();
    assertThat(Float.NaN).isNonZero();
    assertThatIsNonZeroFails(null);
  }

  private static void assertThatIsNonZeroFails(@Nullable final Float value) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(value).named("testValue").isNonZero();
          }
        };
    expectFailureWithMessage(callback, "Not true that testValue (<" + value + ">) is non-zero");
  }

  @Test
  public void isPositiveInfinity() {
    assertThat(Float.POSITIVE_INFINITY).isPositiveInfinity();
    assertThatIsPositiveInfinityFails(1.23f);
    assertThatIsPositiveInfinityFails(Float.NEGATIVE_INFINITY);
    assertThatIsPositiveInfinityFails(Float.NaN);
    assertThatIsPositiveInfinityFails(null);
  }

  private static void assertThatIsPositiveInfinityFails(@Nullable final Float value) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(value).named("testValue").isPositiveInfinity();
          }
        };
    expectFailureWithMessage(
        callback,
        "Not true that testValue (<" + value + ">) is equal to <" + Float.POSITIVE_INFINITY + ">");
  }

  @Test
  public void isNegativeInfinity() {
    assertThat(Float.NEGATIVE_INFINITY).isNegativeInfinity();
    assertThatIsNegativeInfinityFails(1.23f);
    assertThatIsNegativeInfinityFails(Float.POSITIVE_INFINITY);
    assertThatIsNegativeInfinityFails(Float.NaN);
    assertThatIsNegativeInfinityFails(null);
  }

  private static void assertThatIsNegativeInfinityFails(@Nullable final Float value) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(value).named("testValue").isNegativeInfinity();
          }
        };
    expectFailureWithMessage(
        callback,
        "Not true that testValue (<" + value + ">) is equal to <" + Float.NEGATIVE_INFINITY + ">");
  }

  @Test
  public void isNaN() {
    assertThatIsNaNFails(1.23f);
    assertThatIsNaNFails(Float.POSITIVE_INFINITY);
    assertThatIsNaNFails(Float.NEGATIVE_INFINITY);
    assertThatIsNaNFails(null);
  }

  private static void assertThatIsNaNFails(@Nullable final Float value) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(value).named("testValue").isNaN();
          }
        };
    expectFailureWithMessage(
        callback, "Not true that testValue (<" + value + ">) is equal to <" + Float.NaN + ">");
  }

  @Test
  public void isFinite() {
    assertThat(1.23f).isFinite();
    assertThat(Float.MAX_VALUE).isFinite();
    assertThat(-1.0 * Float.MIN_VALUE).isFinite();
    assertThatIsFiniteFails(Float.POSITIVE_INFINITY);
    assertThatIsFiniteFails(Float.NEGATIVE_INFINITY);
    assertThatIsFiniteFails(Float.NaN);
    assertThatIsFiniteFails(null);
  }

  private static void assertThatIsFiniteFails(@Nullable final Float value) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(value).named("testValue").isFinite();
          }
        };
    expectFailureWithMessage(callback, "testValue (<" + value + ">) should have been finite");
  }

  @Test
  public void isNotNaN() {
    assertThat(1.23f).isNotNaN();
    assertThat(Float.MAX_VALUE).isNotNaN();
    assertThat(-1.0 * Float.MIN_VALUE).isNotNaN();
    assertThat(Float.POSITIVE_INFINITY).isNotNaN();
    assertThat(Float.NEGATIVE_INFINITY).isNotNaN();
    assertThatIsNotNaNFails(Float.NaN);
    assertThatIsNotNaNFails(null);
  }

  private static void assertThatIsNotNaNFails(@Nullable final Float value) {
    ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback =
        new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
          @Override
          public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
            expect.that(value).named("testValue").isNotNaN();
          }
        };
    expectFailureWithMessage(callback, "testValue (<" + value + ">) should not have been NaN");
  }
}
