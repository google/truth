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
import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.FailureAssertions.assertFailureValueIndexed;
import static com.google.common.truth.SubjectTest.ForbidsEqualityChecksSubject.objectsForbiddingEqualityCheck;
import static com.google.common.truth.TestPlatform.assertIsNotComparisonFailureIfAvailable;
import static com.google.common.truth.TestPlatform.isGwt;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.assertThrows;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterators;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.testing.NullPointerTester;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Subject}. */
@RunWith(JUnit4.class)
public class SubjectTest {

  @Test
  @GwtIncompatible("NullPointerTester")
  @J2ktIncompatible
  @SuppressWarnings("GoogleInternalApi")
  /*
   * TODO(cpovirk): Reenable these tests publicly. Currently, we depend on guava-android, whose
   * NullPointerTester doesn't yet recognize type-use @Nullable annotations. And we can't mix the
   * -jre version of guava-testlib with the -android version of guava because the NullPointerTester
   *  feature we need requires a -jre-only API.
   */
  @org.junit.Ignore
  public void nullPointerTester() {
    assume().that(isAndroid()).isFalse(); // type-annotation @Nullable is not available

    NullPointerTester npTester = new NullPointerTester();
    npTester.setDefault(Fact.class, simpleFact("fact"));

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
    npTester.testAllPublicInstanceMethods(assertThat("hello"));
    npTester.testAllPublicInstanceMethods(assertThat(new Object()));
    npTester.testAllPublicInstanceMethods(assertThat(ImmutableTable.of()));
    npTester.testAllPublicInstanceMethods(assertThat(new Exception()));
  }

  @Test
  @GwtIncompatible("NullPointerTester")
  @J2ktIncompatible
  @org.junit.Ignore // TODO(cpovirk): Reenable publicly. (See nullPointerTester().)
  public void allAssertThatOverloadsAcceptNull() throws Exception {
    assume().that(isAndroid()).isFalse(); // type-annotation @Nullable is not available

    NullPointerTester npTester = new NullPointerTester();
    npTester.setDefault(Fact.class, simpleFact("fact"));
    for (Method method : Truth.class.getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers())
          && method.getName().equals("assertThat")
          && method.getParameterTypes().length == 1) {
        Object actual = null;
        Subject subject = (Subject) method.invoke(Truth.class, actual);

        subject.isNull();
        AssertionError e = assertThrows(AssertionError.class, () -> subject.isNotNull());
        assertThat(e).factKeys().containsExactly("expected not to be");
        assertThat(e).factValue("expected not to be").isEqualTo("null");

        subject.isEqualTo(null);
        assertThrows(AssertionError.class, () -> subject.isNotEqualTo(null));

        subject.isSameInstanceAs(null);
        subject.isNotSameInstanceAs(new Object());

        if (!(subject instanceof IterableSubject)) { // b/36000148
          subject.isNotIn(ImmutableList.of());
          subject.isNoneOf(new Object(), new Object());
        }

        e = assertThrows(AssertionError.class, () -> subject.isIn(ImmutableList.of()));
        assertThat(e).factKeys().contains("expected any of");

        subject.isNotEqualTo(new Object());
        subject.isEqualTo(null);
        e = assertThrows(AssertionError.class, () -> subject.isEqualTo(new Object()));
        assertThat(e).factKeys().containsExactly("expected", "but was").inOrder();
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

  @SuppressWarnings("TruthIncompatibleType") // Intentional for testing purposes.
  @Test
  public void toStringsAreIdentical() {
    IntWrapper wrapper = new IntWrapper();
    wrapper.wrapped = 5;
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(5).isEqualTo(wrapper));
    assertFailureKeys(e, "expected", "an instance of", "but was", "an instance of");
    assertFailureValue(e, "expected", "5");
    assertFailureValueIndexed(
        e,
        "an instance of",
        0,
        isGwt()
            ? "com.google.common.truth.SubjectTest$IntWrapper"
            : "com.google.common.truth.SubjectTest.IntWrapper");
    assertFailureValue(e, "but was", "(non-equal value with same string representation)");
    assertFailureValueIndexed(e, "an instance of", 1, "Integer");
  }

  private static class IntWrapper {
    int wrapped;

    @Override
    public String toString() {
      return Integer.toString(wrapped);
    }
  }

  @Test
  public void isSameInstanceAsWithNulls() {
    Object o = null;
    assertThat(o).isSameInstanceAs(null);
  }

  @Test
  public void isSameInstanceAsFailureWithNulls() {
    Object o = null;
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(o).isSameInstanceAs("a"));
    assertFailureKeys(e, "expected specific instance", "but was");
    assertFailureValue(e, "expected specific instance", "a");
  }

  @SuppressWarnings("SelfAssertion")
  @Test
  public void isSameInstanceAsWithSameObject() {
    Object a = new Object();
    assertThat(a).isSameInstanceAs(a);
  }

  @Test
  public void isSameInstanceAsFailureWithObjects() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(OBJECT_1).isSameInstanceAs(OBJECT_2));
    assertIsNotComparisonFailureIfAvailable(e);
  }

  @Test
  public void isSameInstanceAsFailureWithComparableObjects_nonString() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(UnsignedInteger.valueOf(42))
                    .isSameInstanceAs(UnsignedInteger.valueOf(42)));
    assertFailureKeys(e, "expected specific instance", "but was");
    assertFailureValue(e, "expected specific instance", "42");
    assertFailureValue(
        e,
        "but was",
        "(different but equal instance of same class with same string representation)");
  }

  @SuppressWarnings("UnnecessaryStringBuilder") // We need a non-identical String instance.
  @Test
  @GwtIncompatible("String equality under JS")
  public void isSameInstanceAsFailureWithComparableObjects() {
    expectFailure(
        whenTesting -> whenTesting.that("ab").isSameInstanceAs(new StringBuilder("ab").toString()));
  }

  @Test
  public void isSameInstanceAsFailureWithDifferentTypesAndSameToString() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("true").isSameInstanceAs(true));
    assertFailureKeys(
        e, "expected specific instance", "an instance of", "but was", "an instance of");
    assertFailureValue(e, "expected specific instance", "true");
    assertFailureValueIndexed(e, "an instance of", 0, "Boolean");
    assertFailureValue(e, "but was", "(non-equal value with same string representation)");
    assertFailureValueIndexed(e, "an instance of", 1, "String");
  }

  @Test
  public void isNotSameInstanceAsWithNulls() {
    Object o = null;
    assertThat(o).isNotSameInstanceAs("a");
  }

  @Test
  public void isNotSameInstanceAsFailureWithNulls() {
    Object o = null;
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(o).isNotSameInstanceAs(null));
    assertFailureKeys(e, "expected not to be specific instance");
    assertFailureValue(e, "expected not to be specific instance", "null");
  }

  @Test
  public void isNotSameInstanceAsWithObjects() {
    assertThat(new Object()).isNotSameInstanceAs(new Object());
  }

  @SuppressWarnings("SelfAssertion")
  @Test
  public void isNotSameInstanceAsFailureWithSameObject() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(OBJECT_1).isNotSameInstanceAs(OBJECT_1));
    assertFailureKeys(e, "expected not to be specific instance");
    assertFailureValue(e, "expected not to be specific instance", "Object 1");
  }

  @Test
  public void isNotSameInstanceAsWithComparableObjects_nonString() {
    assertThat(UnsignedInteger.valueOf(42)).isNotSameInstanceAs(UnsignedInteger.valueOf(42));
  }

  @SuppressWarnings("UnnecessaryStringBuilder") // We need a non-identical String instance.
  @Test
  @GwtIncompatible("String equality under JS")
  public void isNotSameInstanceAsWithComparableObjects() {
    assertThat("ab").isNotSameInstanceAs(new StringBuilder("ab").toString());
  }

  @Test
  public void isNotSameInstanceAsWithDifferentTypesAndSameToString() {
    assertThat("true").isNotSameInstanceAs(true);
  }

  @Test
  public void isNull() {
    assertThat((Object) null).isNull();
  }

  @Test
  public void isNullFail() {
    Object o = new Object();
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(o).isNull());
    assertFailureKeys(e, "expected", "but was");
    assertFailureValue(e, "expected", "null");
  }

  @Test
  public void isNullWhenSubjectForbidsIsEqualTo() {
    assertAbout(objectsForbiddingEqualityCheck()).that(null).isNull();
  }

  @Test
  public void isNullWhenSubjectForbidsIsEqualToFail() {
    expectFailure(
        whenTesting ->
            whenTesting.about(objectsForbiddingEqualityCheck()).that(new Object()).isNull());
  }

  @Test
  public void stringIsNullFail() {
    expectFailure(whenTesting -> whenTesting.that("foo").isNull());
  }

  @Test
  public void isNullBadEqualsImplementation() {
    expectFailure(whenTesting -> whenTesting.that(new ThrowsOnEqualsNull()).isNull());
  }

  @Test
  public void isNotNull() {
    Object o = new Object();
    assertThat(o).isNotNull();
  }

  @Test
  public void isNotNullFail() {
    Object o = null;
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(o).isNotNull());
    assertFailureKeys(e, "expected not to be");
    assertFailureValue(e, "expected not to be", "null");
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
    expectFailure(
        whenTesting -> whenTesting.about(objectsForbiddingEqualityCheck()).that(null).isNotNull());
  }

  @Test
  public void isEqualToWithNulls() {
    Object o = null;
    assertThat(o).isEqualTo(null);
  }

  @Test
  public void isEqualToFailureWithNulls() {
    Object o = null;
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(o).isEqualTo("a"));
    assertFailureKeys(e, "expected", "but was");
    assertFailureValue(e, "expected", "a");
    assertFailureValue(e, "but was", "null");
  }

  @Test
  public void isEqualToStringWithNullVsNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("null").isEqualTo(null));
    assertFailureKeys(e, "expected", "an instance of", "but was", "an instance of");
    assertFailureValue(e, "expected", "null");
    assertFailureValueIndexed(e, "an instance of", 0, "(null reference)");
    assertFailureValue(e, "but was", "(non-equal value with same string representation)");
    assertFailureValueIndexed(e, "an instance of", 1, "String");
  }

  @SuppressWarnings("SelfAssertion")
  @Test
  public void isEqualToWithSameObject() {
    Object a = new Object();
    assertThat(a).isEqualTo(a);
  }

  @Test
  public void isEqualToFailureWithObjects() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(OBJECT_1).isEqualTo(OBJECT_2));
    assertFailureKeys(e, "expected", "but was");
    assertFailureValue(e, "expected", "Object 2");
    assertFailureValue(e, "but was", "Object 1");
  }

  @SuppressWarnings("TruthIncompatibleType")
  @Test
  public void isEqualToFailureWithDifferentTypesAndSameToString() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("true").isEqualTo(true));
    assertFailureKeys(e, "expected", "an instance of", "but was", "an instance of");
    assertFailureValue(e, "expected", "true");
    assertFailureValueIndexed(e, "an instance of", 0, "Boolean");
    assertFailureValue(e, "but was", "(non-equal value with same string representation)");
    assertFailureValueIndexed(e, "an instance of", 1, "String");
  }

  @Test
  public void isEqualToNullBadEqualsImplementation() {
    expectFailure(whenTesting -> whenTesting.that(new ThrowsOnEqualsNull()).isEqualTo(null));
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
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(o).isNotEqualTo(null));
    assertFailureKeys(e, "expected not to be");
    assertFailureValue(e, "expected not to be", "null");
  }

  @Test
  public void isNotEqualToWithObjects() {
    assertThat(new Object()).isNotEqualTo(new Object());
  }

  @Test
  public void isNotEqualToFailureWithObjects() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(new IntId(1)).isNotEqualTo(new IntId(1)));
    assertFailureKeys(e, "expected not to be");
    assertFailureValue(e, "expected not to be", "1");
  }

  private static final class IntId {
    private final int id;

    IntId(int id) {
      this.id = id;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      return o instanceof IntId && id == ((IntId) o).id;
    }

    @Override
    public int hashCode() {
      return id;
    }

    @Override
    public String toString() {
      return String.valueOf(id);
    }
  }

  @SuppressWarnings("SelfAssertion")
  @Test
  public void isNotEqualToFailureWithSameObject() {
    expectFailure(whenTesting -> whenTesting.that(OBJECT_1).isNotEqualTo(OBJECT_1));
  }

  @SuppressWarnings("TruthIncompatibleType")
  @Test
  public void isNotEqualToWithDifferentTypesAndSameToString() {
    assertThat("true").isNotEqualTo(true);
  }

  @Test
  public void isNotEqualToNullBadEqualsImplementation() {
    assertThat(new ThrowsOnEqualsNull()).isNotEqualTo(null);
  }

  @SuppressWarnings("TruthSelfEquals")
  @Test
  public void isNotEqualToSameInstanceBadEqualsImplementation() {
    Object o = new ThrowsOnEquals();
    expectFailure(whenTesting -> whenTesting.that(o).isNotEqualTo(o));
  }

  @SuppressWarnings("IsInstanceString") // test is an intentional trivially true check
  @Test
  public void isInstanceOfExactType() {
    assertThat("a").isInstanceOf(String.class);
  }

  @SuppressWarnings("IsInstanceInteger") // test is an intentional trivially true check
  @Test
  public void isInstanceOfSuperclass() {
    assertThat(3).isInstanceOf(Number.class);
  }

  @SuppressWarnings("IsInstanceString") // test is an intentional trivially true check
  @Test
  public void isInstanceOfImplementedInterface() {
    if (isGwt()) {
      assertThrows(
          UnsupportedOperationException.class,
          () -> assertThat("a").isInstanceOf(CharSequence.class));
      return;
    }

    assertThat("a").isInstanceOf(CharSequence.class);
  }

  @Test
  public void isInstanceOfUnrelatedClass() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(4.5).isInstanceOf(Long.class));
    assertFailureKeys(e, "expected instance of", "but was instance of", "with value");
    assertFailureValue(e, "expected instance of", "Long");
    assertFailureValue(e, "but was instance of", "Double");
    assertFailureValue(e, "with value", "4.5");
  }

  @Test
  public void isInstanceOfUnrelatedInterface() {
    if (isGwt()) {
      assertThrows(
          UnsupportedOperationException.class,
          () -> assertThat(4.5).isInstanceOf(CharSequence.class));
      return;
    }

    expectFailure(whenTesting -> whenTesting.that(4.5).isInstanceOf(CharSequence.class));
  }

  @Test
  public void isInstanceOfClassForNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Object) null).isInstanceOf(Long.class));
    assertFailureKeys(e, "expected instance of", "but was");
    assertFailureValue(e, "expected instance of", "Long");
  }

  @Test
  public void isInstanceOfInterfaceForNull() {
    expectFailure(whenTesting -> whenTesting.that((Object) null).isInstanceOf(CharSequence.class));
  }

  @SuppressWarnings("IsInstanceInteger") // test is an intentional trivially true check
  @Test
  public void isInstanceOfPrimitiveType() {
    assertThat(1).isInstanceOf(int.class);
  }

  @Test
  public void isInstanceOfNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(1).isInstanceOf(null));
    assertFailureKeys(
        e,
        "could not perform instanceof check because expected type was null",
        "value to check was");
  }

  @Test
  public void isNotInstanceOfUnrelatedClass() {
    assertThat("a").isNotInstanceOf(Long.class);
  }

  @Test
  public void isNotInstanceOfUnrelatedInterface() {
    if (isGwt()) {
      assertThrows(
          UnsupportedOperationException.class,
          () -> assertThat(5).isNotInstanceOf(CharSequence.class));
      return;
    }

    assertThat(5).isNotInstanceOf(CharSequence.class);
  }

  @Test
  public void isNotInstanceOfExactType() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(5).isNotInstanceOf(Integer.class));
    assertFailureKeys(e, "expected not to be an instance of", "but was");
    assertFailureValue(e, "expected not to be an instance of", "Integer");
  }

  @Test
  public void isNotInstanceOfSuperclass() {
    expectFailure(whenTesting -> whenTesting.that(5).isNotInstanceOf(Number.class));
  }

  @Test
  public void isNotInstanceOfImplementedInterface() {
    if (isGwt()) {
      assertThrows(
          UnsupportedOperationException.class,
          () -> assertThat("a").isNotInstanceOf(CharSequence.class));
      return;
    }

    expectFailure(whenTesting -> whenTesting.that("a").isNotInstanceOf(CharSequence.class));
  }

  @Test
  public void isNotInstanceOfPrimitiveType() {
    expectFailure(whenTesting -> whenTesting.that(1).isNotInstanceOf(int.class));
  }

  @Test
  public void isNotInstanceOfNull() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(1).isNotInstanceOf(null));
    assertFailureKeys(
        e,
        "could not perform instanceof check because expected type was null",
        "value to check was");
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
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("x").isIn(oneShotIterable("a", "b", "c")));
    assertFailureKeys(e, "expected any of", "but was");
    assertFailureValue(e, "expected any of", "[a, b, c]");
  }

  @Test
  public void isInNullInListWithNull() {
    assertThat((String) null).isIn(oneShotIterable("a", "b", null));
  }

  @Test
  public void isInNonnullInListWithNull() {
    assertThat("b").isIn(oneShotIterable("a", "b", null));
  }

  @Test
  public void isInNullFailure() {
    expectFailure(
        whenTesting -> whenTesting.that((String) null).isIn(oneShotIterable("a", "b", "c")));
  }

  @Test
  public void isInEmptyFailure() {
    expectFailure(whenTesting -> whenTesting.that("b").isIn(ImmutableList.<String>of()));
  }

  @Test
  public void isInNullIterable() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(new Object()).isIn(null));
    assertFailureKeys(
        e,
        "could not perform equality check because iterable of elements to compare to was null",
        "value to compare was");
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
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("x").isAnyOf("a", "b", "c"));
    assertFailureKeys(e, "expected any of", "but was");
    assertFailureValue(e, "expected any of", "[a, b, c]");
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
    expectFailure(whenTesting -> whenTesting.that((String) null).isAnyOf("a", "b", "c"));
  }

  @Test
  public void isNotIn() {
    assertThat("x").isNotIn(oneShotIterable("a", "b", "c"));
  }

  @Test
  public void isNotInFailure() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that("b").isNotIn(oneShotIterable("a", "b", "c")));
    assertFailureKeys(e, "expected not to be any of", "but was");
    assertFailureValue(e, "expected not to be any of", "[a, b, c]");
  }

  @Test
  public void isNotInNull() {
    assertThat((String) null).isNotIn(oneShotIterable("a", "b", "c"));
  }

  @Test
  public void isNotInNullFailure() {
    expectFailure(
        whenTesting -> whenTesting.that((String) null).isNotIn(oneShotIterable("a", "b", null)));
  }

  @Test
  public void isNotInEmpty() {
    assertThat("b").isNotIn(ImmutableList.<String>of());
  }

  @Test
  public void isNotInNullIterable() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(new Object()).isNotIn(null));
    assertFailureKeys(
        e,
        "could not perform equality check because iterable of elements to compare to was null",
        "value to compare was");
  }

  @Test
  public void isNoneOf() {
    assertThat("x").isNoneOf("a", "b", "c");
  }

  @Test
  public void isNoneOfFailure() {
    AssertionError e = expectFailure(whenTesting -> whenTesting.that("b").isNoneOf("a", "b", "c"));
    assertFailureKeys(e, "expected not to be any of", "but was");
    assertFailureValue(e, "expected not to be any of", "[a, b, c]");
  }

  @Test
  public void isNoneOfNull() {
    assertThat((String) null).isNoneOf("a", "b", "c");
  }

  @Test
  public void isNoneOfNullFailure() {
    expectFailure(whenTesting -> whenTesting.that((String) null).isNoneOf("a", "b", (String) null));
  }

  @Test
  // test of a mistaken call
  @SuppressWarnings({"EqualsIncompatibleType", "DoNotCall", "deprecation"})
  public void equalsThrowsUSOE() {
    UnsupportedOperationException expected =
        assertThrows(UnsupportedOperationException.class, () -> assertThat(5).equals(5));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo(
            "Subject.equals() is not supported. Did you mean to call"
                + " assertThat(actual).isEqualTo(expected) instead of"
                + " assertThat(actual).equals(expected)?");
  }

  @Test
  // test of a mistaken call
  @SuppressWarnings({"DoNotCall", "deprecation"})
  public void hashCodeThrowsUSOE() {
    UnsupportedOperationException expected =
        assertThrows(UnsupportedOperationException.class, () -> assertThat(5).hashCode());
    assertThat(expected).hasMessageThat().isEqualTo("Subject.hashCode() is not supported.");
  }

  @Test
  public void ignoreCheckDiscardsFailures() {
    assertThat((Object) null).ignoreCheck().that("foo").isNull();
  }

  private static <T extends @Nullable Object> Iterable<T> oneShotIterable(T... values) {
    Iterator<T> iterator = Iterators.forArray(values);
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

  @Test
  @SuppressWarnings({
    "TruthIncompatibleType", // test of a mistaken call
    "UnnecessaryStringBuilder", // We need a type that doesn't implement value-based equals().
  })
  public void disambiguationWithSameToString() {
    /*
     * We use `Object` instead of `StringBuilder` to force the compiler to choose that(Object) over
     * that(Comparable): StringBuilder does not implement Comparable under Android Lollipop, so the
     * test would fail there at runtime.
     */
    Object stringBuilderAsObject = new StringBuilder("foo");
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(stringBuilderAsObject).isEqualTo(new StringBuilder("foo")));
    assertFailureKeys(e, "expected", "but was");
    assertFailureValue(e, "expected", "foo");
    assertFailureValue(
        e, "but was", "(non-equal instance of same class with same string representation)");
  }

  private static final class ThrowsOnEqualsNull {

    @SuppressWarnings({"EqualsHashCode", "SuperCallToObjectMethod"})
    @Override
    public boolean equals(@Nullable Object obj) {
      checkNotNull(obj); // buggy implementation but one that we're working around, at least for now
      return super.equals(obj);
    }
  }

  private static final class ThrowsOnEquals {

    @SuppressWarnings("EqualsHashCode")
    @Override
    public boolean equals(@Nullable Object obj) {
      throw new UnsupportedOperationException();
      // buggy implementation but one that we're working around, at least for now
    }
  }

  static final class ForbidsEqualityChecksSubject extends Subject {
    static Factory<ForbidsEqualityChecksSubject, Object> objectsForbiddingEqualityCheck() {
      return ForbidsEqualityChecksSubject::new;
    }

    ForbidsEqualityChecksSubject(FailureMetadata metadata, @Nullable Object actual) {
      super(metadata, actual);
    }

    // Not sure how to feel about this, but people do it:

    @Override
    public void isEqualTo(@Nullable Object expected) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void isNotEqualTo(@Nullable Object other) {
      throw new UnsupportedOperationException();
    }
  }

  private static boolean isAndroid() {
    return System.getProperty("java.runtime.name").contains("Android");
  }
}
