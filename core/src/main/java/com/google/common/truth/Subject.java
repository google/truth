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

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CharMatcher.whitespace;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.primitives.Booleans.asList;
import static com.google.common.primitives.Bytes.asList;
import static com.google.common.primitives.Chars.asList;
import static com.google.common.primitives.Ints.asList;
import static com.google.common.primitives.Longs.asList;
import static com.google.common.primitives.Shorts.asList;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Platform.classMetadataUnsupported;
import static com.google.common.truth.Platform.doubleToString;
import static com.google.common.truth.Platform.floatToString;
import static com.google.common.truth.Platform.isInstanceOfType;
import static com.google.common.truth.Platform.isKotlinRange;
import static com.google.common.truth.Platform.kotlinRangeContains;
import static com.google.common.truth.Platform.stringValueForFailure;
import static com.google.common.truth.Subject.EqualityCheck.SAME_INSTANCE;
import static com.google.common.truth.SubjectUtils.accumulate;
import static com.google.common.truth.SubjectUtils.append;
import static com.google.common.truth.SubjectUtils.concat;
import static com.google.common.truth.SubjectUtils.longName;
import static com.google.common.truth.SubjectUtils.sandwich;
import static java.lang.Double.doubleToLongBits;
import static java.lang.Float.floatToIntBits;
import static java.lang.reflect.Array.getLength;
import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import com.google.common.truth.FailureMetadata.OldAndNewValuesAreSimilar;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.ForOverride;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An object that lets you perform checks on the value under test. For example, {@code Subject}
 * contains {@link #isEqualTo(Object)} and {@link #isInstanceOf(Class)}, and {@link StringSubject}
 * contains {@link StringSubject#startsWith startsWith(String)}.
 *
 * <p>To create a {@code Subject} instance, most users will call an {@link Truth#assertThat
 * assertThat} method. For information about other ways to create an instance, see <a
 * href="https://truth.dev/faq#full-chain">this FAQ entry</a>.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>For information about writing a custom {@link Subject}, see <a
 * href="https://truth.dev/extension">our doc on extensions</a>.
 *
 * @author David Saff
 * @author Christian Gruber
 */
public class Subject {
  /**
   * In a fluent assertion chain, the argument to the common overload of {@link
   * StandardSubjectBuilder#about(Subject.Factory) about}, the method that specifies what kind of
   * {@link Subject} to create.
   *
   * <p>For more information about the fluent chain, see <a
   * href="https://truth.dev/faq#full-chain">this FAQ entry</a>.
   *
   * <h3>For people extending Truth</h3>
   *
   * <p>When you write a custom subject, see <a href="https://truth.dev/extension">our doc on
   * extensions</a>. It explains where {@code Subject.Factory} fits into the process.
   */
  public interface Factory<SubjectT extends Subject, ActualT> {
    /** Creates a new {@link Subject}. */
    SubjectT createSubject(FailureMetadata metadata, @Nullable ActualT actual);
  }

  private final FailureMetadata metadata;
  private final @Nullable Object actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected Subject(FailureMetadata metadata, @Nullable Object actual) {
    this.metadata = metadata.updateForSubject(this);
    this.actual = actual;
  }

  /** Checks that the value under test is null. */
  public void isNull() {
    standardIsEqualTo(null);
  }

  /** Checks that the value under test is not null. */
  public void isNotNull() {
    standardIsNotEqualTo(null);
  }

  /**
   * Checks that the value under test is equal to the given object. For the purposes of this
   * comparison, two objects are equal if any of the following is true:
   *
   * <ul>
   *   <li>they are equal according to {@link Objects#equals}
   *   <li>they are arrays and are considered equal by the appropriate {@link Arrays#equals}
   *       overload
   *   <li>they are boxed integer types ({@link Byte}, {@link Short}, {@link Character}, {@link
   *       Integer}, or {@link Long}) and they are numerically equal when converted to {@link Long}.
   *   <li>the actual value is a boxed floating-point type ({@link Double} or {@link Float}), the
   *       expected value is an {@link Integer}, and the two are numerically equal when converted to
   *       {@link Double}. (This allows {@code assertThat(someDouble).isEqualTo(0)} to pass.)
   * </ul>
   *
   * <p><b>Note:</b> This method does not test the {@link Object#equals} implementation itself; it
   * <i>assumes</i> that method is functioning correctly according to its contract. Testing an
   * {@code equals} implementation requires a utility such as <a
   * href="https://mvnrepository.com/artifact/com.google.guava/guava-testlib">guava-testlib</a>'s <a
   * href="https://static.javadoc.io/com.google.guava/guava-testlib/23.0/com/google/common/testing/EqualsTester.html">EqualsTester</a>.
   *
   * <p>In some cases, this method might not even call {@code equals}. It may instead perform other
   * tests that will return the same result as long as {@code equals} is implemented according to
   * the contract for its type.
   */
  /*
   * TODO(cpovirk): Possibly ban overriding isEqualTo+isNotEqualTo in favor of a
   * compareForEquality(Object, Object) method. That way, people would need to override only one
   * method, they would get a ComparisonFailure and other message niceties, and they'd have less to
   * test.
   */
  public void isEqualTo(@Nullable Object expected) {
    standardIsEqualTo(expected);
  }

  private void standardIsEqualTo(@Nullable Object expected) {
    ComparisonResult difference = compareForEquality(expected);
    if (!difference.valuesAreEqual()) {
      failEqualityCheck(EqualityCheck.EQUAL, expected, difference);
    }
  }

  /**
   * Checks that the value under test is not equal to the given object. The meaning of equality is
   * the same as for the {@link #isEqualTo} method.
   */
  public void isNotEqualTo(@Nullable Object other) {
    standardIsNotEqualTo(other);
  }

  private void standardIsNotEqualTo(@Nullable Object other) {
    ComparisonResult difference = compareForEquality(other);
    if (difference.valuesAreEqual()) {
      String otherAsString = formatActualOrExpected(other);
      if (actualCustomStringRepresentation().equals(otherAsString)) {
        failWithoutActual(fact("expected not to be", otherAsString));
      } else {
        failWithoutActual(
            fact("expected not to be", otherAsString),
            fact(
                "but was; string representation of actual value",
                actualCustomStringRepresentation()));
      }
    }
  }

  /**
   * Returns whether {@code actual} equals {@code expected} differ and, in some cases, a description
   * of how they differ.
   *
   * <p>The equality check follows the rules described on {@link #isEqualTo}.
   */
  private ComparisonResult compareForEquality(@Nullable Object expected) {
    if (actual == null && expected == null) {
      return ComparisonResult.equal();
    } else if (actual == null || expected == null) {
      return ComparisonResult.differentNoDescription();
    } else if (actual instanceof byte[] && expected instanceof byte[]) {
      /*
       * For a special error message and to use faster Arrays.equals to avoid at least one timeout.
       *
       * TODO(cpovirk): For performance, use Arrays.equals for other array types (here and/or in
       * checkArrayEqualsRecursive)? Exception: double[] and float[], whose GWT implementations I
       * think may have both false positives and false negatives (so we can't even use Arrays.equals
       * as a fast path for them, nor deepEquals for an Object[] that might contain them). We would
       * still fall back to the slower checkArrayEqualsRecursive to produce a nicer failure message
       * -- but naturally only for tests that are about to fail, when performance matters less.
       */
      return checkByteArrayEquals((byte[]) expected, (byte[]) actual);
    } else if (actual.getClass().isArray() && expected.getClass().isArray()) {
      return checkArrayEqualsRecursive(expected, actual, "");
    } else if (isIntegralBoxedPrimitive(actual) && isIntegralBoxedPrimitive(expected)) {
      return ComparisonResult.fromEqualsResult(integralValue(actual) == integralValue(expected));
    } else if (actual instanceof Double && expected instanceof Double) {
      return ComparisonResult.fromEqualsResult(
          Double.compare((Double) actual, (Double) expected) == 0);
    } else if (actual instanceof Float && expected instanceof Float) {
      return ComparisonResult.fromEqualsResult(
          Float.compare((Float) actual, (Float) expected) == 0);
    } else if (actual instanceof Double && expected instanceof Integer) {
      return ComparisonResult.fromEqualsResult(
          Double.compare((Double) actual, (Integer) expected) == 0);
    } else if (actual instanceof Float && expected instanceof Integer) {
      return ComparisonResult.fromEqualsResult(
          Double.compare((Float) actual, (Integer) expected) == 0);
    } else {
      return ComparisonResult.fromEqualsResult(actual == expected || actual.equals(expected));
    }
  }

  private static boolean isIntegralBoxedPrimitive(@Nullable Object o) {
    return o instanceof Byte
        || o instanceof Short
        || o instanceof Character
        || o instanceof Integer
        || o instanceof Long;
  }

  private static long integralValue(Object o) {
    if (o instanceof Character) {
      return (Character) o;
    } else if (o instanceof Number) {
      return ((Number) o).longValue();
    } else {
      throw new AssertionError(o + " must be either a Character or a Number.");
    }
  }

  /**
   * Checks that the value under test is the same instance as the given object.
   *
   * <p>This method considers {@code null} to be "the same instance as" {@code null} and not the
   * same instance as anything else.
   */
  public final void isSameInstanceAs(@Nullable Object expected) {
    if (actual != expected) {
      failEqualityCheck(
          SAME_INSTANCE,
          expected,
          /*
           * Pass through *whether* the values are equal so that failEqualityCheck() can print that
           * information. But remove the description of the difference, which is always about
           * content, since people calling isSameInstanceAs() are explicitly not interested in
           * content, only object identity.
           */
          compareForEquality(expected).withoutDescription());
    }
  }

  /**
   * Checks that the value under test is not the same instance as the given object.
   *
   * <p>This method considers {@code null} to be "the same instance as" {@code null} and not the
   * same instance as anything else.
   */
  public final void isNotSameInstanceAs(@Nullable Object other) {
    if (actual == other) {
      /*
       * We use actualCustomStringRepresentation() because it might be overridden to be better than
       * actual.toString()/other.toString().
       */
      failWithoutActual(
          fact("expected not to be specific instance", actualCustomStringRepresentation()));
    }
  }

  /** Checks that the value under test is an instance of the given class. */
  public void isInstanceOf(@Nullable Class<?> clazz) {
    if (clazz == null) {
      failWithoutActual(
          simpleFact("could not perform instanceof check because expected type was null"),
          actualValue("value to check was"));
      return;
    }
    clazz = Primitives.wrap(clazz);
    if (actual == null) {
      failWithActual("expected instance of", longName(clazz));
      return;
    }
    if (!isInstanceOfType(actual, clazz)) {
      if (classMetadataUnsupported()) {
        throw new UnsupportedOperationException(
            actualCustomStringRepresentation()
                + ", an instance of "
                + longName(actual.getClass())
                + ", may or may not be an instance of "
                + longName(clazz)
                + ". Under -XdisableClassMetadata, we do not have enough information to tell.");
      }
      failWithoutActual(
          fact("expected instance of", longName(clazz)),
          fact("but was instance of", longName(actual.getClass())),
          fact("with value", actualCustomStringRepresentation()));
    }
  }

  /** Checks that the value under test is not an instance of the given class. */
  public void isNotInstanceOf(@Nullable Class<?> clazz) {
    if (clazz == null) {
      failWithoutActual(
          simpleFact("could not perform instanceof check because expected type was null"),
          actualValue("value to check was"));
      return;
    }
    clazz = Primitives.wrap(clazz);
    if (classMetadataUnsupported()) {
      throw new UnsupportedOperationException(
          "isNotInstanceOf is not supported under -XdisableClassMetadata");
    }
    if (actual == null) {
      return; // null is not an instance of clazz.
    }
    if (isInstanceOfType(actual, clazz)) {
      failWithActual("expected not to be an instance of", longName(clazz));
      /*
       * TODO(cpovirk): Consider including actual.getClass() if it's not clazz itself but only a
       * subtype.
       */
    }
  }

  /** Checks that the value under test is equal to any element in the given iterable. */
  public void isIn(@Nullable Iterable<?> iterable) {
    if (iterable == null) {
      failWithoutActual(
          simpleFact(
              "could not perform equality check because iterable of elements to compare to was"
                  + " null"),
          valueToCompareWas());
      return;
    }
    if (!contains(iterable, actual)) {
      failWithActual("expected any of", iterable);
    }
  }

  private static boolean contains(Iterable<?> haystack, @Nullable Object needle) {
    if (isKotlinRange(haystack)) {
      return kotlinRangeContains(haystack, needle);
    }
    return Iterables.contains(haystack, needle);
  }

  /** Checks that the value under test is equal to any of the given elements. */
  public void isAnyOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    isIn(accumulate(first, second, rest));
  }

  /** Checks that the value under test is not equal to any element in the given iterable. */
  public void isNotIn(@Nullable Iterable<?> iterable) {
    if (iterable == null) {
      failWithoutActual(
          simpleFact(
              "could not perform equality check because iterable of elements to compare to was"
                  + " null"),
          valueToCompareWas());
      return;
    }
    if (Iterables.contains(iterable, actual)) {
      failWithActual("expected not to be any of", iterable);
    }
  }

  /** Checks that the value under test is not equal to any of the given elements. */
  public void isNoneOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    isNotIn(accumulate(first, second, rest));
  }

  /** Returns the actual value under test. */
  final @Nullable Object actual() {
    return actual;
  }

  /**
   * Returns a string representation of the actual value for inclusion in failure messages.
   *
   * <p>Subjects should override this with care.
   *
   * <p>By default, this method returns {@code String.valueOf(getActualValue())} for most types. It
   * does have some special logic for a few cases, like arrays.
   */
  /*
   * TODO(cpovirk): Consider potential improvements to formatting APIs. For example, if users want
   * to format the actual value specially, then it seems likely that they'll want to format the
   * expected value specially, too. And that applies just as well to APIs like isIn(). Maybe we'll
   * want an API that supports formatting those values, too (like formatActualOrExpected below)? See
   * also the related b/70930431.
   */
  @ForOverride
  protected String actualCustomStringRepresentation() {
    return formatActualOrExpected(actual);
  }

  /**
   * Access to {@link #actualCustomStringRepresentation()} from within the package. For creating
   * {@link Fact} instances, we should use {@link #butWas} or {@link #actualValue} instead of this.
   * This method is useful primarily for delegating from one subject's {@link
   * #actualCustomStringRepresentation} method to another's.
   */
  final String actualCustomStringRepresentationForPackageMembersToCall() {
    return actualCustomStringRepresentation();
  }

  private static String formatActualOrExpected(@Nullable Object o) {
    if (o instanceof byte[]) {
      return base16((byte[]) o);
    } else if (o != null && o.getClass().isArray()) {
      return String.valueOf(arrayAsListRecursively(o));
    } else if (o instanceof Double) {
      return doubleToString((Double) o);
    } else if (o instanceof Float) {
      return floatToString((Float) o);
    } else {
      // TODO(cpovirk): Consider renaming the called method to mention "NonArray."
      /*
       * TODO(cpovirk): Should the called method and arrayAsListRecursively(...) both call back into
       * formatActualOrExpected for its handling of byte[] and float/double? Or is there some other
       * restructuring of this set of methods that we should undertake?
       */
      return stringValueForFailure(o);
    }
  }

  // We could add a dep on com.google.common.io, but that seems overkill for base16 encoding
  private static String base16(byte[] bytes) {
    StringBuilder sb = new StringBuilder(2 * bytes.length);
    for (byte b : bytes) {
      sb.append(hexDigitsUpper[(b >> 4) & 0xf]).append(hexDigitsUpper[b & 0xf]);
    }
    return sb.toString();
  }

  private static final char[] hexDigitsUpper = "0123456789ABCDEF".toCharArray();

  private static @Nullable Object arrayAsListRecursively(@Nullable Object input) {
    if (input instanceof Object[]) {
      return Lists.<@Nullable Object, @Nullable Object>transform(
          asList((@Nullable Object[]) input), Subject::arrayAsListRecursively);
    } else if (input instanceof boolean[]) {
      return asList((boolean[]) input);
    } else if (input instanceof int[]) {
      return asList((int[]) input);
    } else if (input instanceof long[]) {
      return asList((long[]) input);
    } else if (input instanceof short[]) {
      return asList((short[]) input);
    } else if (input instanceof byte[]) {
      return asList((byte[]) input);
    } else if (input instanceof double[]) {
      return doubleArrayAsString((double[]) input);
    } else if (input instanceof float[]) {
      return floatArrayAsString((float[]) input);
    } else if (input instanceof char[]) {
      return asList((char[]) input);
    } else {
      return input;
    }
  }

  /**
   * The result of comparing two objects for equality. This includes both the "equal"/"not-equal"
   * bit and, in the case of "not equal," optional facts describing the difference.
   */
  private static final class ComparisonResult {
    /**
     * If {@code equal} is true, returns an equal result; if false, a non-equal result with no
     * description.
     */
    static ComparisonResult fromEqualsResult(boolean equal) {
      return equal ? EQUAL : DIFFERENT_NO_DESCRIPTION;
    }

    /** Returns a non-equal result with the given description. */
    static ComparisonResult differentWithDescription(Fact... facts) {
      return new ComparisonResult(ImmutableList.copyOf(facts));
    }

    /** Returns an equal result. */
    static ComparisonResult equal() {
      return EQUAL;
    }

    /** Returns a non-equal result with no description. */
    static ComparisonResult differentNoDescription() {
      return DIFFERENT_NO_DESCRIPTION;
    }

    private static final ComparisonResult EQUAL = new ComparisonResult(null);
    private static final ComparisonResult DIFFERENT_NO_DESCRIPTION =
        new ComparisonResult(ImmutableList.of());

    private final @Nullable ImmutableList<Fact> facts;

    private ComparisonResult(@Nullable ImmutableList<Fact> facts) {
      this.facts = facts;
    }

    boolean valuesAreEqual() {
      return facts == null;
    }

    ImmutableList<Fact> factsOrEmpty() {
      return firstNonNull(facts, ImmutableList.of());
    }

    /** Returns an instance with the same "equal"/"not-equal" bit but with no description. */
    ComparisonResult withoutDescription() {
      return fromEqualsResult(valuesAreEqual());
    }
  }

  /**
   * Returns {@link ComparisonResult#equal} if the arrays are equal. If not equal, returns a string
   * comparing the two arrays, displaying them in the style "[1, 2, 3]" to supplement the main
   * failure message, which uses the style "010203."
   */
  private static ComparisonResult checkByteArrayEquals(byte[] expected, byte[] actual) {
    if (Arrays.equals(expected, actual)) {
      return ComparisonResult.equal();
    }
    return ComparisonResult.differentWithDescription(
        fact("expected", Arrays.toString(expected)), fact("but was", Arrays.toString(actual)));
  }

  /**
   * Returns {@link ComparisonResult#equal} if the arrays are equal, recursively. If not equal,
   * returns the string of the index at which they're different.
   */
  /*
   * TODO(cpovirk): Decide whether it's worthwhile to go to this trouble to display the index at
   * which the arrays differ. If we were to stop doing that, we could mostly delegate to
   * Arrays.equals() and our float/double arrayEquals methods. (We'd use deepEquals, but it doesn't
   * have our special double/float handling for GWT.)
   */
  private static ComparisonResult checkArrayEqualsRecursive(
      Object expectedArray, Object actualArray, String lastIndex) {
    if (expectedArray == actualArray) {
      return ComparisonResult.equal();
    }
    String expectedType = arrayType(expectedArray);
    String actualType = arrayType(actualArray);
    if (!expectedType.equals(actualType)) {
      Fact indexFact =
          lastIndex.isEmpty() ? simpleFact("wrong type") : fact("wrong type for index", lastIndex);
      return ComparisonResult.differentWithDescription(
          indexFact, fact("expected", expectedType), fact("but was", actualType));
    }
    int actualLength = getLength(actualArray);
    int expectedLength = getLength(expectedArray);
    if (expectedLength != actualLength) {
      Fact indexFact =
          lastIndex.isEmpty()
              ? simpleFact("wrong length")
              : fact("wrong length for index", lastIndex);
      return ComparisonResult.differentWithDescription(
          indexFact, fact("expected", expectedLength), fact("but was", actualLength));
    }
    for (int i = 0; i < actualLength; i++) {
      String index = lastIndex + "[" + i + "]";
      Object expected = Array.get(expectedArray, i);
      Object actual = Array.get(actualArray, i);
      if (actual != null
          && actual.getClass().isArray()
          && expected != null
          && expected.getClass().isArray()) {
        ComparisonResult result = checkArrayEqualsRecursive(expected, actual, index);
        if (!result.valuesAreEqual()) {
          return result;
        }
      } else if (!gwtSafeObjectEquals(actual, expected)) {
        return ComparisonResult.differentWithDescription(fact("differs at index", index));
      }
    }
    return ComparisonResult.equal();
  }

  private static String arrayType(Object array) {
    if (array.getClass() == boolean[].class) {
      return "boolean[]";
    } else if (array.getClass() == int[].class) {
      return "int[]";
    } else if (array.getClass() == long[].class) {
      return "long[]";
    } else if (array.getClass() == short[].class) {
      return "short[]";
    } else if (array.getClass() == byte[].class) {
      return "byte[]";
    } else if (array.getClass() == double[].class) {
      return "double[]";
    } else if (array.getClass() == float[].class) {
      return "float[]";
    } else if (array.getClass() == char[].class) {
      return "char[]";
    } else {
      return "Object[]";
    }
  }

  private static boolean gwtSafeObjectEquals(@Nullable Object actual, @Nullable Object expected) {
    if (actual instanceof Double && expected instanceof Double) {
      return doubleToLongBits((Double) actual) == doubleToLongBits((Double) expected);
    } else if (actual instanceof Float && expected instanceof Float) {
      return floatToIntBits((Float) actual) == floatToIntBits((Float) expected);
    } else {
      return Objects.equals(actual, expected);
    }
  }

  private static List<String> doubleArrayAsString(double[] items) {
    List<String> itemAsStrings = new ArrayList<>(items.length);
    for (double item : items) {
      itemAsStrings.add(doubleToString(item));
    }
    return itemAsStrings;
  }

  private static List<String> floatArrayAsString(float[] items) {
    List<String> itemAsStrings = new ArrayList<>(items.length);
    for (float item : items) {
      itemAsStrings.add(floatToString(item));
    }
    return itemAsStrings;
  }

  /**
   * Returns a builder for creating a derived subject.
   *
   * <p>Derived subjects retain the {@link FailureStrategy} and {@linkplain
   * StandardSubjectBuilder#withMessage messages} of the current subject, and in some cases, they
   * automatically supplement their failure message with information about the original subject.
   *
   * <p>For example, {@link ThrowableSubject#hasMessageThat}, which returns a {@link StringSubject},
   * is implemented with {@code check("getMessage()").that(actual.getMessage())}.
   *
   * <p>The arguments to {@code check} describe how the new subject was derived from the old,
   * formatted like a chained method call. This allows Truth to include that information in its
   * failure messages. For example, {@code assertThat(caught).hasCauseThat().hasMessageThat()} will
   * produce a failure message that includes the string "throwable.getCause().getMessage()," thanks
   * to internal {@code check} calls that supplied "getCause()" and "getMessage()" as arguments.
   *
   * <p>If the method you're delegating to accepts parameters, you can pass {@code check} a format
   * string. For example, {@link MultimapSubject#valuesForKey} calls {@code
   * check("valuesForKey(%s)", key)}.
   *
   * <p>If you aren't really delegating to an instance method on the actual value -- maybe you're
   * calling a static method, or you're calling a chain of several methods -- you can supply
   * whatever string will be most useful to users. For example, if you're delegating to {@code
   * getOnlyElement(actual.colors())}, you might call {@code check("onlyColor()")}.
   *
   * @param format a template with {@code %s} placeholders
   * @param args the arguments to be inserted into those placeholders
   */
  protected final StandardSubjectBuilder check(String format, @Nullable Object... args) {
    return doCheck(OldAndNewValuesAreSimilar.DIFFERENT, format, args);
  }

  // TODO(b/134064106): Figure out a public API for this.

  final StandardSubjectBuilder checkNoNeedToDisplayBothValues(
      String format, @Nullable Object... args) {
    return doCheck(OldAndNewValuesAreSimilar.SIMILAR, format, args);
  }

  /**
   * Returns a builder for creating a subject for an object that is "close enough" to the original
   * actual value.
   *
   * <p>This is a niche API: When one {@link Subject} wants to delegate to another, it should
   * normally use {@link #check(String, Object...)}, which augments the failure message by:
   *
   * <ul>
   *   <li>specifying which <i>part</i> of the actual value we are asserting about (e.g., "value of:
   *       foo.size()")
   *   <li>including both the original actual value and the relevant part of that value (e.g., both
   *       the collection and its size)
   * </ul>
   *
   * Thus, {@code substituteCheck()} is useful only when the new assertion is still (roughly
   * speaking) "about" the entire actual value. For example, {@link StreamSubject} uses this method
   * to create an {@link IterableSubject} for the contents of the stream.
   *
   * <p>The result of {@code substituteCheck()} should be used carefully. For example, it should
   * <b>never</b> be returned to users unless the new subject has the exact full actual value as the
   * original one had. (As of this writing, we never do that, but that may change someday
   * (b/135436697? b/333091510?).) That's because:
   *
   * <ul>
   *   <li>If the assertion is about only a <i>part</i> of the actual value (or about some derived
   *       value, as in {@link ObjectArraySubject#asList}), then we should use {@link #check(String,
   *       Object...)}, as discussed above. (In some cases, we should instead introduce a non-{@link
   *       Subject} type, such as {@link StringSubject.CaseInsensitiveStringComparison}.)
   *   <li>Otherwise, we don't want to present an assertion as about the original actual value when
   *       it's actually about some "stand-in" value. For example (though this isn't a great
   *       example), we wouldn't want for {@code assertThat(stream).isEqualTo(expected)} to use
   *       {@code substituteCheck().that(stream.toList())} internally: The test would be comparing
   *       the <i>list</i> for equality, but the message would suggest that it's comparing the
   *       <i>stream</i>.
   * </ul>
   *
   * Additionally, consider that the messages produced by the new {@link Subject} will be used
   * directly. So, if the messages in {@code FooSubject} refers to "the actual foo," then any {@link
   * Subject} that uses {@code substituteCheck()} to create a {@code FooSubject} will still see "the
   * actual foo" in the messages, even if the original actual value was of some different type.
   */
  final StandardSubjectBuilder substituteCheck() {
    return new StandardSubjectBuilder(metadata);
  }

  private StandardSubjectBuilder doCheck(
      OldAndNewValuesAreSimilar valuesAreSimilar, String format, @Nullable Object[] args) {
    LazyMessage message = LazyMessage.create(format, args);
    return new StandardSubjectBuilder(
        metadata.updateForCheckCall(
            valuesAreSimilar, /* descriptionUpdate= */ input -> input + "." + message));
  }

  /**
   * Begins a new call chain that ignores any failures. This is useful for subjects that normally
   * delegate with to other subjects by using {@link #check} but have already reported a failure. In
   * such cases it may still be necessary to return a {@link Subject} instance even though any
   * subsequent assertions are meaningless. For example, if a user chains together more {@link
   * ThrowableSubject#hasCauseThat} calls than the actual exception has causes, {@code hasCauseThat}
   * returns {@code ignoreCheck().that(... a dummy exception ...)}.
   */
  protected final StandardSubjectBuilder ignoreCheck() {
    return StandardSubjectBuilder.forCustomFailureStrategy(failure -> {});
  }

  /**
   * Fails, reporting a message with two "{@linkplain Fact facts}":
   *
   * <ul>
   *   <li><i>key</i>: <i>value</i>
   *   <li>but was: <i>actual value</i>.
   * </ul>
   *
   * <p>This is the simplest failure API. For more advanced needs, see {@linkplain
   * #failWithActual(Fact, Fact...) the other overload} and {@link #failWithoutActual(Fact, Fact...)
   * failWithoutActual}.
   *
   * <p>Example usage: The check {@code contains(String)} calls {@code failWithActual("expected to
   * contain", string)}.
   *
   * <p><b>Note:</b> While Truth's {@code fail*()} methods usually throw {@link AssertionError},
   * they do not do so in all cases: When users use an alternative {@link FailureStrategy}, such as
   * {@link Expect}, the {@code fail*()} methods may instead record the failure somewhere and then
   * return. To accommodate this, {@link Subject} methods should typically {@code return} after
   * calling a {@code fail*()} method, rather than continue onward to potentially fail a second time
   * or throw an exception. For cases in which a method needs to return another {@link Subject} to
   * the user, see {@link #ignoreCheck()}.
   */
  protected final void failWithActual(String key, @Nullable Object value) {
    failWithActual(fact(key, value));
  }

  /**
   * Fails, reporting a message with the given facts, followed by an automatically added fact of the
   * form:
   *
   * <ul>
   *   <li>but was: <i>actual value</i>.
   * </ul>
   *
   * <p>If you have only one fact to report (and it's a {@linkplain Fact#fact key-value fact}),
   * prefer {@linkplain #failWithActual(String, Object) the simpler overload}.
   *
   * <p>Example usage: The check {@code isEmpty()} calls {@code failWithActual(simpleFact("expected
   * to be empty"))}.
   *
   * <p><b>Note:</b> While Truth's {@code fail*()} methods usually throw {@link AssertionError},
   * they do not do so in all cases: When users use an alternative {@link FailureStrategy}, such as
   * {@link Expect}, the {@code fail*()} methods may instead record the failure somewhere and then
   * return. To accommodate this, {@link Subject} methods should typically {@code return} after
   * calling a {@code fail*()} method, rather than continue onward to potentially fail a second time
   * or throw an exception. For cases in which a method needs to return another {@link Subject} to
   * the user, see {@link #ignoreCheck()}.
   */
  protected final void failWithActual(Fact first, Fact... rest) {
    metadata.fail(sandwich(first, rest, butWas()));
  }

  // TODO(cpovirk): Consider making this protected if there's a need for it.
  /**
   * Internal variant of {@link #failWithActual(Fact, Fact...)} that accepts an {@link Iterable}.
   *
   * <p><b>Note:</b> While Truth's {@code fail*()} methods usually throw {@link AssertionError},
   * they do not do so in all cases: When users use an alternative {@link FailureStrategy}, such as
   * {@link Expect}, the {@code fail*()} methods may instead record the failure somewhere and then
   * return. To accommodate this, {@link Subject} methods should typically {@code return} after
   * calling a {@code fail*()} method, rather than continue onward to potentially fail a second time
   * or throw an exception. For cases in which a method needs to return another {@link Subject} to
   * the user, see {@link #ignoreCheck()}.
   */
  final void failWithActual(Iterable<Fact> facts) {
    metadata.fail(append(ImmutableList.copyOf(facts), butWas()));
  }

  enum EqualityCheck {
    EQUAL("expected"),
    SAME_INSTANCE("expected specific instance");

    final String keyForExpected;

    EqualityCheck(String keyForExpected) {
      this.keyForExpected = keyForExpected;
    }
  }

  /**
   * Special version of {@link #failEqualityCheck} for use from {@link IterableSubject}, documented
   * further there.
   *
   * <p><b>Note:</b> While Truth's {@code fail*()} methods usually throw {@link AssertionError},
   * they do not do so in all cases: When users use an alternative {@link FailureStrategy}, such as
   * {@link Expect}, the {@code fail*()} methods may instead record the failure somewhere and then
   * return. To accommodate this, {@link Subject} methods should typically {@code return} after
   * calling a {@code fail*()} method, rather than continue onward to potentially fail a second time
   * or throw an exception. For cases in which a method needs to return another {@link Subject} to
   * the user, see {@link #ignoreCheck()}.
   */
  final void failEqualityCheckForEqualsWithoutDescription(@Nullable Object expected) {
    failEqualityCheck(EqualityCheck.EQUAL, expected, ComparisonResult.differentNoDescription());
  }

  /**
   * Fails, potentially producing a {@code ComparisonFailure}.
   *
   * <p><b>Note:</b> While Truth's {@code fail*()} methods usually throw {@link AssertionError},
   * they do not do so in all cases: When users use an alternative {@link FailureStrategy}, such as
   * {@link Expect}, the {@code fail*()} methods may instead record the failure somewhere and then
   * return. To accommodate this, {@link Subject} methods should typically {@code return} after
   * calling a {@code fail*()} method, rather than continue onward to potentially fail a second time
   * or throw an exception. For cases in which a method needs to return another {@link Subject} to
   * the user, see {@link #ignoreCheck()}.
   */
  private void failEqualityCheck(
      EqualityCheck equalityCheck, @Nullable Object expected, ComparisonResult difference) {
    String actualString = actualCustomStringRepresentation();
    String expectedString = formatActualOrExpected(expected);
    String actualClass = actual == null ? "(null reference)" : longName(actual.getClass());
    String expectedClass = expected == null ? "(null reference)" : longName(expected.getClass());

    /*
     * It's a little odd for expectedString to be formatActualOrExpected(expected) but actualString
     * *not* to be formatActualOrExpected(actual), since we're going to compare the two. Instead,
     * actualString is actualCustomStringRepresentation() -- as it is for other assertions, since
     * users may have overridden that method. While actualCustomStringRepresentation() defaults to
     * formatActualOrExpected(actual), it's only a default.
     *
     * What we really want here is probably to delete actualCustomStringRepresentation() and migrate
     * users to formatActualOrExpected(actual).
     */
    boolean sameToStrings = actualString.equals(expectedString);
    boolean sameClassNames = actualClass.equals(expectedClass);
    // TODO(cpovirk): Handle "same class name, different class loader."
    // `equal` is always false for isEqualTo, but it varies for isSameInstanceAs:
    boolean equal = difference.valuesAreEqual();

    if (equalityCheck == EqualityCheck.EQUAL
        && (tryFailForTrailingWhitespaceOnly(expected) || tryFailForEmptyString(expected))) {
      // tryFailForTrailingWhitespaceOnly or tryFailForEmptyString reported a failure, so we're done
      return;
    }

    if (sameToStrings) {
      if (sameClassNames) {
        String doppelgangerDescription =
            equal
                ? "(different but equal instance of same class with same string representation)"
                : "(non-equal instance of same class with same string representation)";
        failEqualityCheckNoComparisonFailure(
            difference,
            fact(equalityCheck.keyForExpected, expectedString),
            fact("but was", doppelgangerDescription));
      } else {
        failEqualityCheckNoComparisonFailure(
            difference,
            fact(equalityCheck.keyForExpected, expectedString),
            fact("an instance of", expectedClass),
            fact("but was", "(non-equal value with same string representation)"),
            fact("an instance of", actualClass));
      }
    } else {
      if (equalityCheck == EqualityCheck.EQUAL && actual != null && expected != null) {
        metadata.failEqualityCheck(difference.factsOrEmpty(), expectedString, actualString);
      } else {
        failEqualityCheckNoComparisonFailure(
            difference,
            fact(equalityCheck.keyForExpected, expectedString),
            fact("but was", actualString));
      }
    }
  }

  /**
   * Checks whether the actual and expected values are strings that match except for trailing
   * whitespace. If so, reports a failure and returns true.
   */
  private boolean tryFailForTrailingWhitespaceOnly(@Nullable Object expected) {
    if (!(actual instanceof String) || !(expected instanceof String)) {
      return false;
    }

    /*
     * TODO(cpovirk): Consider applying this for non-String types. The danger there is that we don't
     * know whether toString() (or actualCustomStringRepresentation/formatActualOrExpected) and
     * equals() are consistent for those types.
     */
    String actualString = (String) actual;
    String expectedString = (String) expected;
    String actualNoTrailing = whitespace().trimTrailingFrom(actualString);
    String expectedNoTrailing = whitespace().trimTrailingFrom(expectedString);
    String expectedTrailing =
        escapeWhitespace(expectedString.substring(expectedNoTrailing.length()));
    String actualTrailing = escapeWhitespace(actualString.substring(actualNoTrailing.length()));

    if (!actualNoTrailing.equals(expectedNoTrailing)) {
      return false;
    }

    if (actualString.startsWith(expectedString)) {
      failWithoutActual(
          fact("expected", expectedString),
          fact("but contained extra trailing whitespace", actualTrailing));
    } else if (expectedString.startsWith(actualString)) {
      failWithoutActual(
          fact("expected", expectedString),
          fact("but was missing trailing whitespace", expectedTrailing));
    } else {
      failWithoutActual(
          fact("expected", expectedString),
          fact("with trailing whitespace", expectedTrailing),
          fact("but trailing whitespace was", actualTrailing));
    }

    return true;
  }

  private static String escapeWhitespace(String in) {
    StringBuilder out = new StringBuilder();
    for (char c : in.toCharArray()) {
      out.append(escapeWhitespace(c));
    }
    return out.toString();
  }

  private static String escapeWhitespace(char c) {
    switch (c) {
      case '\t':
        return "\\t";
      case '\n':
        return "\\n";
      case '\f':
        return "\\f";
      case '\r':
        return "\\r";
      case ' ':
        return "␣";
      default:
        return new String(asUnicodeHexEscape(c));
    }
  }

  /**
   * Checks whether the actual and expected values are empty strings. If so, reports a failure and
   * returns true.
   */
  private boolean tryFailForEmptyString(@Nullable Object expected) {
    if (!(actual instanceof String) || !(expected instanceof String)) {
      return false;
    }

    String actualString = (String) actual;
    String expectedString = (String) expected;
    if (actualString.isEmpty()) {
      failWithoutActual(fact("expected", expectedString), simpleFact("but was an empty string"));
      return true;
    } else if (expectedString.isEmpty()) {
      failWithoutActual(simpleFact("expected an empty string"), fact("but was", actualString));
      return true;
    }

    // Neither string was empty
    return false;
  }

  // From SourceCodeEscapers:

  private static final char[] hexDigitsLower = "0123456789abcdef".toCharArray();

  private static char[] asUnicodeHexEscape(char c) {
    // Equivalent to String.format("\\u%04x", (int) c);
    char[] r = new char[6];
    r[0] = '\\';
    r[1] = 'u';
    r[5] = hexDigitsLower[c & 0xF];
    c = (char) (c >>> 4);
    r[4] = hexDigitsLower[c & 0xF];
    c = (char) (c >>> 4);
    r[3] = hexDigitsLower[c & 0xF];
    c = (char) (c >>> 4);
    r[2] = hexDigitsLower[c & 0xF];
    return r;
  }

  private void failEqualityCheckNoComparisonFailure(ComparisonResult difference, Fact... facts) {
    // TODO(cpovirk): Is it possible for difference.factsOrEmpty() to be nonempty? If not, remove.
    metadata.fail(concat(asList(facts), difference.factsOrEmpty()));
  }

  /**
   * Fails, reporting a message with the given facts, <i>without automatically adding the actual
   * value.</i>
   *
   * <p>Most failure messages should report the actual value, so most checks should call {@link
   * #failWithActual(Fact, Fact...) failWithActual} instead. However, {@code failWithoutActual} is
   * useful in some cases:
   *
   * <ul>
   *   <li>when the actual value is obvious from the rest of the message. For example, {@code
   *       isNotEmpty()} calls {@code failWithoutActual(simpleFact("expected not to be empty")}.
   *   <li>when the actual value shouldn't come last or should have a different key than the default
   *       of "but was." For example, {@code isNotWithin(...).of(...)} calls {@code
   *       failWithoutActual} so that it can put the expected and actual values together, followed
   *       by the tolerance.
   * </ul>
   *
   * <p>Example usage: The check {@code isEmpty()} calls {@code failWithActual(simpleFact("expected
   * to be empty"))}.
   *
   * <p><b>Note:</b> While Truth's {@code fail*()} methods usually throw {@link AssertionError},
   * they do not do so in all cases: When users use an alternative {@link FailureStrategy}, such as
   * {@link Expect}, the {@code fail*()} methods may instead record the failure somewhere and then
   * return. To accommodate this, {@link Subject} methods should typically {@code return} after
   * calling a {@code fail*()} method, rather than continue onward to potentially fail a second time
   * or throw an exception. For cases in which a method needs to return another {@link Subject} to
   * the user, see {@link #ignoreCheck()}.
   */
  protected final void failWithoutActual(Fact first, Fact... rest) {
    metadata.fail(ImmutableList.copyOf(Lists.asList(first, rest)));
  }

  // TODO(cpovirk): Consider making this protected if there's a need for it.
  final void failWithoutActual(Iterable<Fact> facts) {
    metadata.fail(ImmutableList.copyOf(facts));
  }

  /**
   * Special failure method for {@link ThrowableSubject} to use when users try to assert about the
   * cause or message of a null {@link Throwable}.
   *
   * <p><b>Note:</b> While Truth's {@code fail*()} methods usually throw {@link AssertionError},
   * they do not do so in all cases: When users use an alternative {@link FailureStrategy}, such as
   * {@link Expect}, the {@code fail*()} methods may instead record the failure somewhere and then
   * return. To accommodate this, {@link Subject} methods should typically {@code return} after
   * calling a {@code fail*()} method, rather than continue onward to potentially fail a second time
   * or throw an exception. For cases in which a method needs to return another {@link Subject} to
   * the user, see {@link #ignoreCheck()}.
   */
  final void failForNullThrowable(String message) {
    metadata.failForNullThrowable(message);
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#equals(Object)} is not supported on Truth subjects. If you are
   *     writing a test assertion (actual vs. expected), use {@link #isEqualTo(Object)} instead.
   */
  @DoNotCall(
      "Subject.equals() is not supported. Did you mean to call"
          + " assertThat(actual).isEqualTo(expected) instead of"
          + " assertThat(actual).equals(expected)?")
  @Deprecated
  @Override
  public final boolean equals(@Nullable Object other) {
    throw new UnsupportedOperationException(
        "Subject.equals() is not supported. Did you mean to call"
            + " assertThat(actual).isEqualTo(expected) instead of"
            + " assertThat(actual).equals(expected)?");
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#hashCode()} is not supported on Truth subjects.
   */
  @DoNotCall("Subject.hashCode() is not supported.")
  @Deprecated
  @Override
  public final int hashCode() {
    throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#toString()} is not supported on Truth subjects.
   */
  @Deprecated
  @Override
  public
  String toString() {
    throw new UnsupportedOperationException(
        "Subject.toString() is not supported. Did you mean to call assertThat(foo.toString())"
            + " instead of assertThat(foo).toString()?");
  }

  /**
   * Returns a "but was: [actual value]" fact. This method should be rarely needed, since Truth
   * inserts a "but was" fact by default for assertions. However, it's occasionally useful for calls
   * to {@code failWithoutActual} that want a "but was" fact but don't want it to come last, where
   * Truth inserts it by default.
   */
  /*
   * TODO(cpovirk): Consider giving this protected access.
   *
   * It is likely better than what users would otherwise do -- `fact("but was", actual)`, which
   * ignores actualCustomStringRepresentation() (which is inaccessible outside the package).
   *
   * But I want to think more about this. In particular, if people use this to reimplement
   * isEqualTo(), I would be sad that they're missing out on its normal special handling. That's
   * probably not enough reason to avoid adding this, but we can hold it back for now.
   */
  final Fact butWas() {
    return actualValue("but was");
  }

  /**
   * Returns a "[key]: [actual value]" fact. This method should be rarely needed, since Truth
   * inserts a "but was" fact by default for assertions. (Furthermore, for cases in which we want an
   * acatual-value fact but not as the <i>final</i> fact, where Truth puts the actual value by
   * default, Truth offers {@link #butWas()}.) However, {@code actualValue} occasionally useful when
   * the actual value should be identified by a different key than "but was."
   */
  // TODO(cpovirk): Consider giving this protected access, as with butWas() itself.
  final Fact actualValue(String key) {
    return fact(key, actualCustomStringRepresentation());
  }

  final void arrayIsEmptyImpl() {
    if (actual == null) {
      failWithActual(simpleFact("expected an empty array"));
    } else if (getLength(actual) > 0) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  final void arrayIsNotEmptyImpl() {
    if (actual == null) {
      failWithActual(simpleFact("expected a nonempty array"));
    } else if (getLength(actual) == 0) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  final void arrayHasLengthImpl(int length) {
    if (length < 0) {
      failWithoutActual(
          simpleFact("could not perform length check because expected length was negative"),
          fact("expected length", length),
          actualValue("array was"));
    } else if (actual == null) {
      failWithActual("expected an array with length", length);
    } else {
      check("length").that(getLength(actual)).isEqualTo(length);
    }
  }

  static ImmutableList.Builder<Fact> factsBuilder() {
    return ImmutableList.builder();
  }

  /*
   * Computed lazily so that we're not doing expensive string operations during every assertion,
   * only during every failure.
   */
  final String typeDescription() {
    String typeDescriptionOverride = TYPE_DESCRIPTION_OVERRIDES.get(getClass());
    if (typeDescriptionOverride != null) {
      return typeDescriptionOverride;
    }
    /*
     * J2CL doesn't store enough metadata to know whether "Foo$BarSubject" is a nested class, so it
     * can't tell whether the simple name is "Foo$BarSubject" or just "BarSubject": b/71808768. It
     * returns "Foo$BarSubject" to err on the side of preserving information. We want just
     * "BarSubject," so we strip any likely enclosing type ourselves.
     */
    String subjectClass = getClass().getSimpleName().replaceFirst(".*[$]", "");
    String actualClass =
        (subjectClass.endsWith("Subject") && !subjectClass.equals("Subject"))
            ? subjectClass.substring(0, subjectClass.length() - "Subject".length())
            : "Object";
    return UPPER_CAMEL.to(LOWER_CAMEL, actualClass);
  }

  private Fact valueToCompareWas() {
    return actualValue("value to compare was");
  }

  static Factory<Subject, Object> objects() {
    return Subject::new;
  }

  /**
   * A mapping from some {@link Subject} subclasses to descriptions of the types they're testing.
   * For example, {@link ThrowableSubject} has the description "throwable." Normally, Truth is able
   * to infer this description from the class name. However, if we lack runtime type information
   * (notably, under J2CL with class metadata off), we might not have access to the original class
   * name. (As of this writing, we don't run Truth's own tests under J2CL with class metadata off,
   * but our users may run their own tests that way.)
   *
   * <p>Since Truth can normally infer this on its own, this mechanism is not something that would
   * normally be useful outside of core Truth. But to support running Truth's own tests run with
   * class metadata off, it's easier to tweak the {@link Subject} code to hard-code the descriptions
   * we want than to generalize the tests to accept obfuscated names.
   *
   * <p>That said, we do sometimes use this mechanism to provide a simpler description than the one
   * derived from the class name. For example, rather than say "primitiveBooleanArray," we say just
   * "array." In theory, users could want to do that, too. Maybe someday we will provide them with a
   * way to plug in their own descriptions.
   *
   * <p>Even within Truth itself, not all types need a {@code TYPE_DESCRIPTION_OVERRIDES} entry: At
   * least with Truth's current failure messages, the type appears only when a {@link Subject} uses
   * assertion chaining. (This can happen because the {@link Subject} exposes chaining it in its
   * API, like in {@link ThrowableSubject#hasCauseThat}, or because it uses it internally, like in
   * {@link MultisetSubject#hasCount}.)
   *
   * <p>Notice that we look up map values by the exact runtime {@link Subject} class, not using
   * {@link Class#isInstance}. We do this so that a subclass does not inherit a description override
   * from its superclass. That way, Truth can normally infer a description for it, which is likely
   * to be more specific. For example, FooExceptionSubject would produce "fooException," not
   * "throwable").
   */
  @SuppressWarnings("GoogleInternalApi") // The reference to a Google-internal class is stripped
  private static final ImmutableMap<Class<? extends Subject>, String> TYPE_DESCRIPTION_OVERRIDES =
      new ImmutableMap.Builder<Class<? extends Subject>, String>()
          // keep-sorted start
          .put(GuavaOptionalSubject.class, "optional")
          .put(IntStreamSubject.class, "stream")
          .put(IterableSubject.class, "iterable")
          .put(LongStreamSubject.class, "stream")
          .put(MapSubject.class, "map")
          .put(MultimapSubject.class, "multimap")
          .put(MultisetSubject.class, "multiset")
          .put(ObjectArraySubject.class, "array")
          .put(OptionalDoubleSubject.class, "optionalDouble")
          .put(OptionalIntSubject.class, "optionalInt")
          .put(OptionalLongSubject.class, "optionalLong")
          .put(OptionalSubject.class, "optional")
          .put(PrimitiveBooleanArraySubject.class, "array")
          .put(PrimitiveByteArraySubject.class, "array")
          .put(PrimitiveCharArraySubject.class, "array")
          .put(PrimitiveDoubleArraySubject.class, "array")
          .put(PrimitiveFloatArraySubject.class, "array")
          .put(PrimitiveIntArraySubject.class, "array")
          .put(PrimitiveLongArraySubject.class, "array")
          .put(PrimitiveShortArraySubject.class, "array")
          .put(StreamSubject.class, "stream")
          .put(StringSubject.class, "string")
          .put(TableSubject.class, "table")
          .put(ThrowableSubject.class, "throwable")
          .put(TruthFailureSubject.class, "failure")
          // keep-sorted end
          .buildOrThrow();
}
