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
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.TestPlatform.isGwt;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

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
import java.util.Iterator;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
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
    npTester.setDefault(Fact.class, simpleFact("fact"));
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
          assertThat(expected).factKeys().containsExactly("expected not to be");
          assertThat(expected).factValue("expected not to be").isEqualTo("null");
        }

        subject.isEqualTo(null);
        try {
          subject.isNotEqualTo(null); // should throw
          throw new Error("assertThat(null).isNotEqualTo(null) should throw an exception!");
        } catch (AssertionError expected) {
        }

        subject.isSameAs(null);
        subject.isNotSameAs(new Object());

        if (!(subject instanceof IterableSubject)) { // b/36000148
          subject.isNotIn(ImmutableList.<Object>of());
          subject.isNoneOf(new Object(), new Object());
        }

        try {
          subject.isIn(ImmutableList.of());
          throw new Error("Expected to fail");
        } catch (AssertionError expected) {
          assertThat(expected).factKeys().contains("expected any of");
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
          assertThat(expected).factKeys().containsExactly("expected", "but was").inOrder();
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
    assertFailureKeys("expected", "an instance of", "but was", "an instance of");
    assertFailureValue("expected", "5");
    assertFailureValueIndexed(
        "an instance of", 0, "com.google.common.truth.SubjectTest$IntWrapper");
    assertFailureValue("but was", "(non-equal value with same string representation)");
    assertFailureValueIndexed("an instance of", 1, "java.lang.Integer");
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
    assertFailureKeys("expected specific instance", "but was");
    assertFailureValue("expected specific instance", "a");
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
    assertThat(expectFailure.getFailure()).isNotInstanceOf(ComparisonFailureWithFacts.class);
  }

  @Test
  public void isSameAsFailureWithComparableObjects_nonString() {
    Object a = UnsignedInteger.valueOf(42);
    Object b = UnsignedInteger.fromIntBits(42);
    expectFailure.whenTesting().that(a).isSameAs(b);
    assertFailureKeys("expected specific instance", "but was");
    assertFailureValue("expected specific instance", "42");
    assertFailureValue(
        "but was", "(different but equal instance of same class with same string representation)");
  }

  @Test
  @GwtIncompatible("String equality under JS")
  public void isSameAsFailureWithComparableObjects() {
    Object a = "ab";
    Object b = new StringBuilder("ab").toString();
    expectFailure.whenTesting().that(a).isSameAs(b);
  }

  @Test
  public void isSameAsFailureWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    expectFailure.whenTesting().that(a).isSameAs(b);
    assertFailureKeys("expected specific instance", "an instance of", "but was", "an instance of");
    assertFailureValue("expected specific instance", "true");
    assertFailureValueIndexed("an instance of", 0, "java.lang.Boolean");
    assertFailureValue("but was", "(non-equal value with same string representation)");
    assertFailureValueIndexed("an instance of", 1, "java.lang.String");
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
    assertFailureKeys("expected not to be specific instance");
    assertFailureValue("expected not to be specific instance", "null");
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
    assertFailureKeys("expected not to be specific instance");
    assertFailureValue("expected not to be specific instance", "Object 1");
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
    assertFailureKeys("expected", "but was");
    assertFailureValue("expected", "null");
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
    assertFailureKeys("expected not to be");
    assertFailureValue("expected not to be", "null");
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
    assertFailureKeys("expected", "but was");
    assertFailureValue("expected", "a");
    assertFailureValue("but was", "null");
  }

  @Test
  public void isEqualToStringWithNullVsNull() {
    expectFailure.whenTesting().that("null").isEqualTo(null);
    assertFailureKeys("expected", "an instance of", "but was", "an instance of");
    assertFailureValue("expected", "null");
    assertFailureValueIndexed("an instance of", 0, "(null reference)");
    assertFailureValue("but was", "(non-equal value with same string representation)");
    assertFailureValueIndexed("an instance of", 1, "java.lang.String");
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
    assertFailureKeys("expected", "but was");
    assertFailureValue("expected", "Object 2");
    assertFailureValue("but was", "Object 1");
  }

  @Test
  public void isEqualToFailureWithDifferentTypesAndSameToString() {
    Object a = "true";
    Object b = true;
    expectFailure.whenTesting().that(a).isEqualTo(b);
    assertFailureKeys("expected", "an instance of", "but was", "an instance of");
    assertFailureValue("expected", "true");
    assertFailureValueIndexed("an instance of", 0, "java.lang.Boolean");
    assertFailureValue("but was", "(non-equal value with same string representation)");
    assertFailureValueIndexed("an instance of", 1, "java.lang.String");
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
    assertFailureKeys("expected not to be");
    assertFailureValue("expected not to be", "null");
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
    assertFailureKeys("expected not to be");
    assertFailureValue("expected not to be", "1");
  }

  @Test
  public void isNotEqualToFailureWithSameObject() {
    Object a = OBJECT_1;
    Object b = a;
    expectFailure.whenTesting().that(a).isNotEqualTo(b);
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
    assertFailureKeys("expected instance of", "but was instance of", "with value");
    assertFailureValue("expected instance of", "java.lang.Long");
    assertFailureValue("but was instance of", "java.lang.Double");
    assertFailureValue("with value", "4.5");
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
  }

  @Test
  public void isInstanceOfClassForNull() {
    expectFailure.whenTesting().that((Object) null).isInstanceOf(Long.class);
    assertFailureKeys("expected instance of", "but was");
    assertFailureValue("expected instance of", "java.lang.Long");
  }

  @Test
  public void isInstanceOfInterfaceForNull() {
    expectFailure.whenTesting().that((Object) null).isInstanceOf(CharSequence.class);
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
    assertFailureKeys("expected not to be an instance of", "but was");
    assertFailureValue("expected not to be an instance of", "java.lang.Integer");
  }

  @Test
  public void isNotInstanceOfSuperclass() {
    expectFailure.whenTesting().that(5).isNotInstanceOf(Number.class);
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
    assertFailureKeys("expected any of", "but was");
    assertFailureValue("expected any of", "[a, b, c]");
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
  }

  @Test
  public void isInEmptyFailure() {
    expectFailure.whenTesting().that("b").isIn(ImmutableList.<String>of());
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
    assertFailureKeys("expected any of", "but was");
    assertFailureValue("expected any of", "[a, b, c]");
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
  }

  @Test
  public void isNotIn() {
    assertThat("x").isNotIn(oneShotIterable("a", "b", "c"));
  }

  @Test
  public void isNotInFailure() {
    expectFailure.whenTesting().that("b").isNotIn(oneShotIterable("a", "b", "c"));
    assertFailureKeys("expected not to be any of", "but was");
    assertFailureValue("expected not to be any of", "[a, b, c]");
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
    assertFailureKeys("expected not to be any of", "but was");
    assertFailureValue("expected not to be any of", "[a, b, c]");
  }

  @Test
  public void isNoneOfNull() {
    assertThat((String) null).isNoneOf("a", "b", "c");
  }

  @Test
  public void isNoneOfNullFailure() {
    expectFailure.whenTesting().that((String) null).isNoneOf("a", "b", (String) null);
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

  @Test
  public void disambiguationWithSameToString() {
    ImmutableSet<Integer> ints = ImmutableSet.of(1, 2, 3);
    ImmutableSet<Long> longs = ImmutableSet.of(1L, 2L, 3L);

    expectFailure.whenTesting().that(ints).isEqualTo(longs);
    assertFailureKeys("expected", "but was");
    assertFailureValue("expected", "[1, 2, 3]");
    assertFailureValue(
        "but was", "(non-equal instance of same class with same string representation)");
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
    ForbidsEqualityChecksSubject(FailureMetadata metadata, @NullableDecl Object actual) {
      super(metadata, actual);
    }

    // Not sure how to feel about this, but people do it:

    @Override
    public void isEqualTo(@NullableDecl Object expected) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void isNotEqualTo(@NullableDecl Object unexpected) {
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
