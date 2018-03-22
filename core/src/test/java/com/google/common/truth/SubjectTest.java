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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.TestPlatform.isGwt;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterators;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.Subject.Factory;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for generic Subject behavior.
 *
 * @author David Saff
 * @author Christian Gruber
 */
@RunWith(JUnit4.class)
public class SubjectTest extends BaseSubjectTestCase {

  @Test
  @GwtIncompatible("NullPointerTester")
  public void nullPointerTester() {
    NullPointerTester npTester = new NullPointerTester();

    // TODO(kak): Automatically generate this list with reflection,
    // or maybe use AbstractPackageSanityTests?
    npTester.testAllPublicInstanceMethods(assertThat(BigDecimal.TEN));
    npTester.testAllPublicInstanceMethods(assertThat(false));
    npTester.testAllPublicInstanceMethods(assertThat(String.class));
    npTester.testAllPublicInstanceMethods(assertThat((Comparable<String>) "hello"));
    npTester.testAllPublicInstanceMethods(assertThat(2d));
    npTester.testAllPublicInstanceMethods(assertThat(2f));
    npTester.testAllPublicInstanceMethods(assertThat(Optional.absent()));
    npTester.testAllPublicInstanceMethods(assertThat(1));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableList.of()));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableListMultimap.of()));
    npTester.testAllPublicInstanceMethods(assertThat(1L));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableMap.of()));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableMultimap.of()));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableMultiset.of()));
    npTester.testAllPublicInstanceMethods(assertThat(new Object[0]));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableSetMultimap.of()));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableSortedMap.of()));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableSortedSet.of()));
    npTester.testAllPublicInstanceMethods(assertThat("hello"));
    npTester.testAllPublicInstanceMethods(assertThat(new Object()));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableTable.of()));
    npTester.testAllPublicInstanceMethods(assertThat(new Exception()));
  }

  @Test
  @GwtIncompatible("NullPointerTester")
  public void allAssertThatOverloadsAcceptNull() throws Exception {
    NullPointerTester npTester = new NullPointerTester();
    for (Method method : Truth.class.getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers())
          && method.getName().equals("assertThat")
          && method.getParameterTypes().length == 1) {
        Object actual = null;
        Subject<?, ?> subject = (Subject<?, ?>) method.invoke(Truth.class, actual);

        subject.isNull();
        try {
          subject.isNotNull(); // should throw
          throw new Error("assertThat(null).isNotNull() should throw an exception!");
        } catch (AssertionError expected) {
          assertThat(expected)
              .hasMessageThat()
              .isEqualTo("Not true that the subject is a non-null reference");
        }

        subject.isEqualTo(null);
        try {
          subject.isNotEqualTo(null); // should throw
          throw new Error("assertThat(null).isNotEqualTo(null) should throw an exception!");
        } catch (AssertionError expected) {
        }

        subject.isSameAs(null);
        subject.isNotSameAs(new Object());

        subject.isNotIn(ImmutableList.<Object>of());
        subject.isNoneOf(new Object(), new Object());

        try {
          subject.isIn(ImmutableList.of());
          throw new Error("Expected to fail");
        } catch (AssertionError expected) {
          assertThat(expected).hasMessageThat().contains("is equal to any element in");
        }

        // TODO(cpovirk): Fix bug.
        if (!(subject instanceof AbstractArraySubject)) {
          // check all public assertion methods for correct null handling
          npTester.testAllPublicInstanceMethods(subject);
        }

        subject.isNotEqualTo(new Object());
        subject.isEqualTo(null);
        try {
          subject.isEqualTo(new Object()); // should throw
          throw new Error("assertThat(null).isEqualTo(<non-null>) should throw an exception!");
        } catch (AssertionError expected) {
          assertThat(expected).hasMessageThat().contains("Not true that ");
          assertThat(expected).hasMessageThat().contains(" is equal to ");
        }
      }
    }
  }

  private static final Object OBJECT_1 =
      new Object() {
        @Override
        public String toString() {
          return "Object 1";
        }
      };
  private static final Object OBJECT_2 =
      new Object() {
        @Override
        public String toString() {
          return "Object 2";
        }
      };

  @Test
  public void toStringsAreIdentical() {
    IntWrapper wrapper = new IntWrapper();
    wrapper.wrapped = 5;
    expectFailure.whenTesting().that(5).isEqualTo(wrapper);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <5> (java.lang.Integer) "
                + "is equal to <5> (com.google.common.truth.SubjectTest$IntWrapper)");
  }

  private static class IntWrapper {
    int wrapped;

    @Override
    public String toString() {
      return Integer.toString(wrapped);
    }
  }

  @Test
  public void isSameAsWithNulls() {
    Object o = null;
    assertThat(o).isSameAs(null);
  }

  @Test
  public void isSameAsFailureWithNulls() {
    Object o = null;
    expectFailure.whenTesting().that(o).isSameAs("a");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is the same instance as <a>");
  }

  @Test
  public void isSameAsWithSameObject() {
    Object a = new Object();
    Object b = a;
    assertThat(a).isSameAs(b);
  }

  @Test
  public void isSameAsFailureWithObjects() {
    Object a = OBJECT_1;
    Object b = OBJECT_2;
    expectFailure.whenTesting().that(a).isSameAs(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <Object 1> is the same instance as <Object 2>");
  }

  @Test
  public void isSameAsFailureWithComparableObjects_nonString() {
    Object a = UnsignedInteger.valueOf(42);
    Object b = UnsignedInteger.fromIntBits(42);
    expectFailure.whenTesting().that(a).isSameAs(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <42> is the same instance as <42> "
                + "(although their toString() representations are the same)");
  }

  @Test
  @GwtIncompatible("String equality under JS")
  public void isSameAsFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder("ab").toString();
    expectFailure.whenTesting().that(a).isSameAs(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <ab> is the same instance as <ab>"
                + " (although their toString() representations are the same)");
  }

  @Test
  public void isSameAsFailureWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    expectFailure.whenTesting().that(a).isSameAs(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <true> (java.lang.String) is the same"
                + " instance as <true> (java.lang.Boolean)");
  }

  @Test
  public void isNotSameAsWithNulls() {
    Object o = null;
    assertThat(o).isNotSameAs("a");
  }

  @Test
  public void isNotSameAsFailureWithNulls() {
    Object o = null;
    expectFailure.whenTesting().that(o).isNotSameAs(null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not the same instance as <null>");
  }

  @Test
  public void isNotSameAsWithObjects() {
    Object a = new Object();
    Object b = new Object();
    assertThat(a).isNotSameAs(b);
  }

  @Test
  public void isNotSameAsFailureWithSameObject() {
    Object a = OBJECT_1;
    Object b = a;
    expectFailure.whenTesting().that(a).isNotSameAs(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <Object 1> is not the same instance as <Object 1>");
  }

  @Test
  public void isNotSameAsWithComparableObjects_nonString() {
    Object a = UnsignedInteger.valueOf(42);
    Object b = UnsignedInteger.fromIntBits(42);
    assertThat(a).isNotSameAs(b);
  }

  @Test
  @GwtIncompatible("String equality under JS")
  public void isNotSameAsWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder("ab").toString();
    assertThat(a).isNotSameAs(b);
  }

  @Test
  public void isNotSameAsWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    assertThat(a).isNotSameAs(b);
  }

  @Test
  public void isNull() {
    Object o = null;
    assertThat(o).isNull();
  }

  @Test
  public void isNullFail() {
    Object o = new Object();
    expectFailure.whenTesting().that(o).isNull();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <" + o.toString() + "> is null");
  }

  @Test
  public void isNullWhenSubjectForbidsIsEqualTo() {
    assertAbout(objectsForbiddingEqualityCheck()).that(null).isNull();
  }

  @Test
  public void isNullWhenSubjectForbidsIsEqualToFail() {
    expectFailure.whenTesting().about(objectsForbiddingEqualityCheck()).that(new Object()).isNull();
  }

  @Test
  public void stringIsNullFail() {
    expectFailure.whenTesting().that("foo").isNull();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"foo\"> is null");
  }

  @Test
  public void isNullBadEqualsImplementation() {
    expectFailure.whenTesting().that(new ThrowsOnEqualsNull()).isNull();
  }

  @Test
  public void isNotNull() {
    Object o = new Object();
    assertThat(o).isNotNull();
  }

  @Test
  public void isNotNullFail() {
    Object o = null;
    expectFailure.whenTesting().that(o).isNotNull();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that the subject is a non-null reference");
  }

  @Test
  public void isNotNullBadEqualsImplementation() {
    assertThat(new ThrowsOnEqualsNull()).isNotNull();
  }

  @Test
  public void isNotNullWhenSubjectForbidsIsEqualTo() {
    assertAbout(objectsForbiddingEqualityCheck()).that(new Object()).isNotNull();
  }

  @Test
  public void isNotNullWhenSubjectForbidsIsEqualToFail() {
    expectFailure.whenTesting().about(objectsForbiddingEqualityCheck()).that(null).isNotNull();
  }

  @Test
  public void isEqualToWithNulls() {
    Object o = null;
    assertThat(o).isEqualTo(null);
  }

  @Test
  public void isEqualToFailureWithNulls() {
    Object o = null;
    expectFailure.whenTesting().that(o).isEqualTo("a");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is equal to <a>");
  }

  @Test
  public void isEqualToStringWithNullVsNull() {
    expectFailure.whenTesting().that("null").isEqualTo(null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"null\"> is null");
  }

  @Test
  public void isEqualToObjectWhoseToStringSaysNullVsNull() {
    expectFailure.whenTesting().that(new ObjectWhoseToStringSaysNull()).isEqualTo(null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <null> is equal to <null> (although their toString() representations are the same)");
  }

  private static final class ObjectWhoseToStringSaysNull {
    @Override
    public String toString() {
      return "null";
    }
  }

  @Test
  public void isEqualToNullVsStringWithNull() {
    Object o = null;
    expectFailure.whenTesting().that(o).isEqualTo("null");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <null> is equal to <null> (although their toString() representations are the same)");
  }

  @Test
  public void isEqualToWithSameObject() {
    Object a = new Object();
    Object b = a;
    assertThat(a).isEqualTo(b);
  }

  @Test
  public void isEqualToFailureWithObjects() {
    Object a = OBJECT_1;
    Object b = OBJECT_2;
    expectFailure.whenTesting().that(a).isEqualTo(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <Object 1> is equal to <Object 2>");
  }

  @Test
  public void isEqualToWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder("ab").toString();
    assertThat(a).isEqualTo(b);
  }

  @Test
  public void isEqualToFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder("aa").toString();
    expectFailure.whenTesting().that(a).isEqualTo(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <ab> is equal to <aa>");
  }

  @Test
  public void isEqualToFailureWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    expectFailure.whenTesting().that(a).isEqualTo(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <true> (java.lang.String) is equal to" + " <true> (java.lang.Boolean)");
  }

  @Test
  public void isEqualToNullBadEqualsImplementation() {
    expectFailure.whenTesting().that(new ThrowsOnEqualsNull()).isEqualTo(null);
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isEqualToSameInstanceBadEqualsImplementation() {
    Object o = new ThrowsOnEquals();
    assertThat(o).isEqualTo(o);
  }

  @Test
  public void isNotEqualToWithNulls() {
    Object o = null;
    assertThat(o).isNotEqualTo("a");
  }

  @Test
  public void isNotEqualToFailureWithNulls() {
    Object o = null;
    expectFailure.whenTesting().that(o).isNotEqualTo(null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not equal to <null>");
  }

  @Test
  public void isNotEqualToWithObjects() {
    Object a = new Object();
    Object b = new Object();
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  public void isNotEqualToFailureWithObjects() {
    Object o = new Integer(1);
    expectFailure.whenTesting().that(o).isNotEqualTo(new Integer(1));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <1> is not equal to <1>");
  }

  @Test
  public void isNotEqualToFailureWithSameObject() {
    Object a = OBJECT_1;
    Object b = a;
    expectFailure.whenTesting().that(a).isNotEqualTo(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <Object 1> is not equal to <Object 1>");
  }

  @Test
  public void isNotEqualToWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder("aa").toString();
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  public void isNotEqualToFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder("ab").toString();
    expectFailure.whenTesting().that(a).isNotEqualTo(b);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <ab> is not equal to <ab>");
  }

  @Test
  public void isNotEqualToWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  public void isNotEqualToNullBadEqualsImplementation() {
    assertThat(new ThrowsOnEqualsNull()).isNotEqualTo(null);
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualToSameInstanceBadEqualsImplementation() {
    Object o = new ThrowsOnEquals();
    expectFailure.whenTesting().that(o).isNotEqualTo(o);
  }

  @Test
  public void isInstanceOfExactType() {
    assertThat("a").isInstanceOf(String.class);
  }

  @Test
  public void isInstanceOfSuperclass() {
    assertThat(3).isInstanceOf(Number.class);
  }

  @Test
  public void isInstanceOfImplementedInterface() {
    if (isGwt()) {
      try {
        assertThat("a").isInstanceOf(CharSequence.class);
        fail();
      } catch (UnsupportedOperationException expected) {
      }
      return;
    }

    assertThat("a").isInstanceOf(CharSequence.class);
  }

  @Test
  public void isInstanceOfUnrelatedClass() {
    expectFailure.whenTesting().that(4.5).isInstanceOf(Long.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <4.5> is an instance of <java.lang.Long>."
                + " It is an instance of <java.lang.Double>");
  }

  @Test
  public void isInstanceOfUnrelatedInterface() {
    if (isGwt()) {
      try {
        assertThat(4.5).isInstanceOf(CharSequence.class);
        fail();
      } catch (UnsupportedOperationException expected) {
      }
      return;
    }

    expectFailure.whenTesting().that(4.5).isInstanceOf(CharSequence.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <4.5> is an instance of <java.lang.CharSequence>."
                + " It is an instance of <java.lang.Double>");
  }

  @Test
  public void isInstanceOfClassForNull() {
    expectFailure.whenTesting().that((Object) null).isInstanceOf(Long.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is an instance of <java.lang.Long>");
  }

  @Test
  public void isInstanceOfInterfaceForNull() {
    expectFailure.whenTesting().that((Object) null).isInstanceOf(CharSequence.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is an instance of <java.lang.CharSequence>");
  }

  @Test
  public void isNotInstanceOfUnrelatedClass() {
    assertThat("a").isNotInstanceOf(Long.class);
  }

  @Test
  public void isNotInstanceOfUnrelatedInterface() {
    if (isGwt()) {
      try {
        assertThat(5).isNotInstanceOf(CharSequence.class);
        fail();
      } catch (UnsupportedOperationException expected) {
      }
      return;
    }

    assertThat(5).isNotInstanceOf(CharSequence.class);
  }

  @Test
  public void isNotInstanceOfExactType() {
    expectFailure.whenTesting().that(5).isNotInstanceOf(Integer.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<5> expected not to be an instance of java.lang.Integer, but was.");
  }

  @Test
  public void isNotInstanceOfSuperclass() {
    expectFailure.whenTesting().that(5).isNotInstanceOf(Number.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<5> expected not to be an instance of java.lang.Number, but was.");
  }

  @Test
  public void isNotInstanceOfImplementedInterface() {
    if (isGwt()) {
      try {
        assertThat("a").isNotInstanceOf(CharSequence.class);
        fail();
      } catch (UnsupportedOperationException expected) {
      }
      return;
    }

    expectFailure.whenTesting().that("a").isNotInstanceOf(CharSequence.class);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("<\"a\"> expected not to be an instance of java.lang.CharSequence, but was.");
  }

  @Test
  public void isIn() {
    assertThat("b").isIn(oneShotIterable("a", "b", "c"));
  }

  @Test
  public void isInJustTwo() {
    assertThat("b").isIn(oneShotIterable("a", "b"));
  }

  @Test
  public void isInFailure() {
    expectFailure.whenTesting().that("x").isIn(oneShotIterable("a", "b", "c"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"x\"> is equal to any element in <[a, b, c]>");
  }

  @Test
  public void isInNullInListWithNull() {
    assertThat((String) null).isIn(oneShotIterable("a", "b", (String) null));
  }

  @Test
  public void isInNonnullInListWithNull() {
    assertThat("b").isIn(oneShotIterable("a", "b", (String) null));
  }

  @Test
  public void isInNullFailure() {
    expectFailure.whenTesting().that((String) null).isIn(oneShotIterable("a", "b", "c"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is equal to any element in <[a, b, c]>");
  }

  @Test
  public void isInEmptyFailure() {
    expectFailure.whenTesting().that("b").isIn(ImmutableList.<String>of());
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"b\"> is equal to any element in <[]>");
  }

  @Test
  public void isAnyOf() {
    assertThat("b").isAnyOf("a", "b", "c");
  }

  @Test
  public void isAnyOfJustTwo() {
    assertThat("b").isAnyOf("a", "b");
  }

  @Test
  public void isAnyOfFailure() {
    expectFailure.whenTesting().that("x").isAnyOf("a", "b", "c");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"x\"> is equal to any element in <[a, b, c]>");
  }

  @Test
  public void isAnyOfNullInListWithNull() {
    assertThat((String) null).isAnyOf("a", "b", (String) null);
  }

  @Test
  public void isAnyOfNonnullInListWithNull() {
    assertThat("b").isAnyOf("a", "b", (String) null);
  }

  @Test
  public void isAnyOfNullFailure() {
    expectFailure.whenTesting().that((String) null).isAnyOf("a", "b", "c");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is equal to any element in <[a, b, c]>");
  }

  @Test
  public void isNotIn() {
    assertThat("x").isNotIn(oneShotIterable("a", "b", "c"));
  }

  @Test
  public void isNotInFailure() {
    expectFailure.whenTesting().that("b").isNotIn(oneShotIterable("a", "b", "c"));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"b\"> is not in [a, b, c].");
  }

  @Test
  public void isNotInNull() {
    assertThat((String) null).isNotIn(oneShotIterable("a", "b", "c"));
  }

  @Test
  public void isNotInNullFailure() {
    expectFailure
        .whenTesting()
        .that((String) null)
        .isNotIn(oneShotIterable("a", "b", (String) null));
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not in [a, b, null].");
  }

  @Test
  public void isNotInEmpty() {
    assertThat("b").isNotIn(ImmutableList.<String>of());
  }

  @Test
  public void isNoneOf() {
    assertThat("x").isNoneOf("a", "b", "c");
  }

  @Test
  public void isNoneOfFailure() {
    expectFailure.whenTesting().that("b").isNoneOf("a", "b", "c");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <\"b\"> is not in [a, b, c].");
  }

  @Test
  public void isNoneOfNull() {
    assertThat((String) null).isNoneOf("a", "b", "c");
  }

  @Test
  public void isNoneOfNullFailure() {
    expectFailure.whenTesting().that((String) null).isNoneOf("a", "b", (String) null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <null> is not in [a, b, null].");
  }

  @Test
  @SuppressWarnings("EqualsIncompatibleType")
  public void equalsThrowsUSOE() {
    try {
      boolean unused = assertThat(5).equals(5);
    } catch (UnsupportedOperationException expected) {
      assertThat(expected)
          .hasMessageThat()
          .isEqualTo("If you meant to test object equality, use .isEqualTo(other) instead.");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void hashCodeThrowsUSOE() {
    try {
      int unused = assertThat(5).hashCode();
    } catch (UnsupportedOperationException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Subject.hashCode() is not supported.");
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void ignoreCheckDiscardsFailures() {
    assertThat((Object) null).ignoreCheck().that("foo").isNull();
  }

  private static <T> Iterable<T> oneShotIterable(final T... values) {
    final Iterator<T> iterator = Iterators.forArray(values);
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return iterator;
      }

      @Override
      public String toString() {
        return Arrays.toString(values);
      }
    };
  }

  @AutoValue
  abstract static class Foo {
    public static Foo create(Collection<Long> nums) {
      return new AutoValue_SubjectTest_Foo(nums);
    }

    abstract Collection<Long> nums();
  }

  @Test
  public void disambiguationWithSameToString_autovalue() {
    Foo foo1 = Foo.create(Arrays.asList(1L, 2L));
    Foo foo2 = Foo.create(ImmutableSet.of(1L, 2L));

    expectFailure.whenTesting().that(foo1).isEqualTo(foo2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <Foo{nums=[1, 2]}> is equal to <Foo{nums=[1, 2]}> "
                + "(although their toString() representations are the same)");
  }

  @Test
  public void disambiguationWithSameToString_immutableSets() {
    ImmutableSet<Integer> ints = ImmutableSet.of(1, 2, 3);
    ImmutableSet<Long> longs = ImmutableSet.of(1L, 2L, 3L);

    expectFailure.whenTesting().that(ints).isEqualTo(longs);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <[1, 2, 3]> is equal to <[1, 2, 3]> "
                + "(although their toString() representations are the same)");
  }

  private static final class ThrowsOnEqualsNull {
    @Override
    public boolean equals(Object obj) {
      checkNotNull(obj); // buggy implementation but one that we're working around, at least for now
      return super.equals(obj);
    }
  }

  private static final class ThrowsOnEquals {
    @Override
    public boolean equals(Object obj) {
      throw new UnsupportedOperationException();
      // buggy implementation but one that we're working around, at least for now
    }
  }

  private static final class ForbidsEqualityChecksSubject
      extends Subject<ForbidsEqualityChecksSubject, Object> {
    ForbidsEqualityChecksSubject(FailureMetadata metadata, @Nullable Object actual) {
      super(metadata, actual);
    }

    // Not sure how to feel about this, but people do it:

    @Override
    public void isEqualTo(@Nullable Object expected) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void isNotEqualTo(@Nullable Object unexpected) {
      throw new UnsupportedOperationException();
    }
  }

  private static Subject.Factory<ForbidsEqualityChecksSubject, Object>
      objectsForbiddingEqualityCheck() {
    return new Factory<ForbidsEqualityChecksSubject, Object>() {
      @Override
      public ForbidsEqualityChecksSubject createSubject(FailureMetadata metadata, Object actual) {
        return new ForbidsEqualityChecksSubject(metadata, actual);
      }
    };
  }
}
