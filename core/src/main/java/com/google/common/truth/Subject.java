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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Platform.doubleToString;
import static com.google.common.truth.Platform.floatToString;
import static com.google.common.truth.Platform.isKotlinRange;
import static com.google.common.truth.Platform.kotlinRangeContains;
import static com.google.common.truth.Platform.stringValueOfNonFloatingPoint;
import static com.google.common.truth.Subject.EqualityCheck.SAME_INSTANCE;
import static com.google.common.truth.SubjectUtils.accumulate;
import static com.google.common.truth.SubjectUtils.append;
import static com.google.common.truth.SubjectUtils.concat;
import static com.google.common.truth.SubjectUtils.sandwich;
import static java.util.Arrays.asList;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.google.common.truth.FailureMetadata.OldAndNewValuesAreSimilar;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.ForOverride;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

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

  private final @Nullable FailureMetadata metadata;
  private final @Nullable Object actual;
  private final @Nullable String typeDescriptionOverride;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected Subject(FailureMetadata metadata, @Nullable Object actual) {
    this(metadata, actual, /* typeDescriptionOverride= */ null);
  }

  /**
   * Special constructor that lets subclasses provide a description of the type they're testing. For
   * example, {@link ThrowableSubject} passes the description "throwable." Normally, Truth is able
   * to infer this name from the class name. However, if we lack runtime type information (notably,
   * under j2cl with class metadata off), we might not have access to the original class name.
   *
   * <p>We don't expect to make this a public API: Class names are nearly always available. It's
   * just that we want to be able to run Truth's own tests run with class metadata off, and it's
   * easier to tweak the subjects to know their own names rather than generalize the tests to accept
   * obfuscated names.
   */
  Subject(
      @Nullable FailureMetadata metadata,
      @Nullable Object actual,
      @Nullable String typeDescriptionOverride) {
    this.metadata = metadata == null ? null : metadata.updateForSubject(this);
    this.actual = actual;
    this.typeDescriptionOverride = typeDescriptionOverride;
  }

  /** Fails if the subject is not null. */
  public void isNull() {
    standardIsEqualTo(null);
  }

  /** Fails if the subject is null. */
  public void isNotNull() {
    standardIsNotEqualTo(null);
  }

  /**
   * Fails if the subject is not equal to the given object. For the purposes of this comparison, two
   * objects are equal if any of the following is true:
   *
   * <ul>
   *   <li>they are equal according to {@link Objects#equal}
   *   <li>they are arrays and are considered equal by the appropriate {@link Arrays#equals}
   *       overload
   *   <li>they are boxed integer types ({@code Byte}, {@code Short}, {@code Character}, {@code
   *       Integer}, or {@code Long}) and they are numerically equal when converted to {@code Long}.
   *   <li>the actual value is a boxed floating-point type ({@code Double} or {@code Float}), the
   *       expected value is an {@code Integer}, and the two are numerically equal when converted to
   *       {@code Double}. (This allows {@code assertThat(someDouble).isEqualTo(0)} to pass.)
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
   * Fails if the subject is equal to the given object. The meaning of equality is the same as for
   * the {@link #isEqualTo} method.
   */
  public void isNotEqualTo(@Nullable Object unexpected) {
    standardIsNotEqualTo(unexpected);
  }

  private void standardIsNotEqualTo(@Nullable Object unexpected) {
    ComparisonResult difference = compareForEquality(unexpected);
    if (difference.valuesAreEqual()) {
      String unexpectedAsString = formatActualOrExpected(unexpected);
      if (actualCustomStringRepresentation().equals(unexpectedAsString)) {
        failWithoutActual(fact("expected not to be", unexpectedAsString));
      } else {
        failWithoutActual(
            fact("expected not to be", unexpectedAsString),
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
      return (long) ((Character) o).charValue();
    } else if (o instanceof Number) {
      return ((Number) o).longValue();
    } else {
      throw new AssertionError(o + " must be either a Character or a Number.");
    }
  }

  /** Fails if the subject is not the same instance as the given object. */
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

  /** Fails if the subject is the same instance as the given object. */
  public final void isNotSameInstanceAs(@Nullable Object unexpected) {
    if (actual == unexpected) {
      /*
       * We use actualCustomStringRepresentation() because it might be overridden to be better than
       * actual.toString()/unexpected.toString().
       */
      failWithoutActual(
          fact("expected not to be specific instance", actualCustomStringRepresentation()));
    }
  }

  /** Fails if the subject is not an instance of the given class. */
  public void isInstanceOf(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException("clazz");
    }
    if (actual == null) {
      failWithActual("expected instance of", clazz.getName());
      return;
    }
    if (!isInstanceOfType(actual, clazz)) {
      if (classMetadataUnsupported()) {
        throw new UnsupportedOperationException(
            actualCustomStringRepresentation()
                + ", an instance of "
                + actual.getClass().getName()
                + ", may or may not be an instance of "
                + clazz.getName()
                + ". Under -XdisableClassMetadata, we do not have enough information to tell.");
      }
      failWithoutActual(
          fact("expected instance of", clazz.getName()),
          fact("but was instance of", actual.getClass().getName()),
          fact("with value", actualCustomStringRepresentation()));
    }
  }

  /** Fails if the subject is an instance of the given class. */
  public void isNotInstanceOf(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException("clazz");
    }
    if (classMetadataUnsupported()) {
      throw new UnsupportedOperationException(
          "isNotInstanceOf is not supported under -XdisableClassMetadata");
    }
    if (actual == null) {
      return; // null is not an instance of clazz.
    }
    if (isInstanceOfType(actual, clazz)) {
      failWithActual("expected not to be an instance of", clazz.getName());
      /*
       * TODO(cpovirk): Consider including actual.getClass() if it's not clazz itself but only a
       * subtype.
       */
    }
  }

  private static boolean isInstanceOfType(Object instance, Class<?> clazz) {
    checkArgument(
        !clazz.isPrimitive(),
        "Cannot check instanceof for primitive type %s. Pass the wrapper class instead.",
        clazz.getSimpleName());
    /*
     * TODO(cpovirk): Make the message include `Primitives.wrap(clazz).getSimpleName()` once that
     * method is available in a public guava-gwt release that we depend on.
     */
    return Platform.isInstanceOfType(instance, clazz);
  }

  /** Fails unless the subject is equal to any element in the given iterable. */
  public void isIn(@Nullable Iterable<?> iterable) {
    checkNotNull(iterable);
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

  /** Fails unless the subject is equal to any of the given elements. */
  public void isAnyOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    isIn(accumulate(first, second, rest));
  }

  /** Fails if the subject is equal to any element in the given iterable. */
  public void isNotIn(@Nullable Iterable<?> iterable) {
    checkNotNull(iterable);
    if (Iterables.contains(iterable, actual)) {
      failWithActual("expected not to be any of", iterable);
    }
  }

  /** Fails if the subject is equal to any of the given elements. */
  public void isNoneOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    isNotIn(accumulate(first, second, rest));
  }

  /** Returns the actual value under test. */
  final @Nullable Object actual() {
    return actual;
  }

  /**
   * Supplies the direct string representation of the actual value to other methods which may prefix
   * or otherwise position it in an error message. This should only be overridden to provide an
   * improved string representation of the value under test, as it would appear in any given error
   * message, and should not be used for additional prefixing.
   *
   * <p>Subjects should override this with care.
   *
   * <p>By default, this returns {@code String.ValueOf(getActualValue())}.
   */
  /*
   * TODO(cpovirk): Consider whether this API pulls its weight. If users want to format the actual
   * value, maybe they should do so themselves? Of course, they won't have a chance to use a custom
   * format for inherited implementations like isEqualTo(). But if they want to format the actual
   * value specially, then it seems likely that they'll want to format the expected value specially,
   * too. And that applies just as well to APIs like isIn(). Maybe we'll want an API that supports
   * formatting those values, too (like formatActualOrExpected below)? See also the related
   * b/70930431. But note that we are likely to use this from FailureMetadata, at least in the short
   * term, for better or for worse.
   */
  @ForOverride
  protected String actualCustomStringRepresentation() {
    return formatActualOrExpected(actual);
  }

  final String actualCustomStringRepresentationForPackageMembersToCall() {
    return actualCustomStringRepresentation();
  }

  private String formatActualOrExpected(@Nullable Object o) {
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
      return stringValueOfNonFloatingPoint(o);
    }
  }

  // We could add a dep on com.google.common.io, but that seems overkill for base16 encoding
  private static String base16(byte[] bytes) {
    StringBuilder sb = new StringBuilder(2 * bytes.length);
    for (byte b : bytes) {
      sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
    }
    return sb.toString();
  }

  private static final char[] hexDigits = "0123456789ABCDEF".toCharArray();

  private static @Nullable Object arrayAsListRecursively(@Nullable Object input) {
    if (input instanceof Object[]) {
      return Lists.<@Nullable Object, @Nullable Object>transform(
          asList((@Nullable Object[]) input), Subject::arrayAsListRecursively);
    } else if (input instanceof boolean[]) {
      return Booleans.asList((boolean[]) input);
    } else if (input instanceof int[]) {
      return Ints.asList((int[]) input);
    } else if (input instanceof long[]) {
      return Longs.asList((long[]) input);
    } else if (input instanceof short[]) {
      return Shorts.asList((short[]) input);
    } else if (input instanceof byte[]) {
      return Bytes.asList((byte[]) input);
    } else if (input instanceof double[]) {
      return doubleArrayAsString((double[]) input);
    } else if (input instanceof float[]) {
      return floatArrayAsString((float[]) input);
    } else if (input instanceof char[]) {
      return Chars.asList((char[]) input);
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
        new ComparisonResult(ImmutableList.<Fact>of());

    private final @Nullable ImmutableList<Fact> facts;

    private ComparisonResult(@Nullable ImmutableList<Fact> facts) {
      this.facts = facts;
    }

    boolean valuesAreEqual() {
      return facts == null;
    }

    ImmutableList<Fact> factsOrEmpty() {
      return firstNonNull(facts, ImmutableList.<Fact>of());
    }

    /** Returns an instance with the same "equal"/"not-equal" bit but with no description. */
    ComparisonResult withoutDescription() {
      return fromEqualsResult(valuesAreEqual());
    }
  }

  /**
   * Returns null if the arrays are equal. If not equal, returns a string comparing the two arrays,
   * displaying them in the style "[1, 2, 3]" to supplement the main failure message, which uses the
   * style "010203."
   */
  private static ComparisonResult checkByteArrayEquals(byte[] expected, byte[] actual) {
    if (Arrays.equals(expected, actual)) {
      return ComparisonResult.equal();
    }
    return ComparisonResult.differentWithDescription(
        fact("expected", Arrays.toString(expected)), fact("but was", Arrays.toString(actual)));
  }

  /**
   * Returns null if the arrays are equal, recursively. If not equal, returns the string of the
   * index at which they're different.
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
    int actualLength = Array.getLength(actualArray);
    int expectedLength = Array.getLength(expectedArray);
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
      return Double.doubleToLongBits((Double) actual) == Double.doubleToLongBits((Double) expected);
    } else if (actual instanceof Float && expected instanceof Float) {
      return Float.floatToIntBits((Float) actual) == Float.floatToIntBits((Float) expected);
    } else {
      return Objects.equal(actual, expected);
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
   * Returns a builder for creating a derived subject but without providing information about how
   * the derived subject will relate to the current subject. In most cases, you should provide such
   * information by using {@linkplain #check(String, Object...) the other overload}.
   *
   * @deprecated Use {@linkplain #check(String, Object...) the other overload}, which requires you
   *     to supply more information to include in any failure messages.
   */
  @Deprecated
  final StandardSubjectBuilder check() {
    return new StandardSubjectBuilder(checkNotNull(metadata).updateForCheckCall());
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

  private StandardSubjectBuilder doCheck(
      OldAndNewValuesAreSimilar valuesAreSimilar, String format, @Nullable Object[] args) {
    LazyMessage message = new LazyMessage(format, args);
    return new StandardSubjectBuilder(
        checkNotNull(metadata)
            .updateForCheckCall(
                valuesAreSimilar, /* descriptionUpdate= */ input -> input + "." + message));
  }

  /**
   * Begins a new call chain that ignores any failures. This is useful for subjects that normally
   * delegate with to other subjects by using {@link #check} but have already reported a failure. In
   * such cases it may still be necessary to return a {@code Subject} instance even though any
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
   */
  protected final void failWithActual(Fact first, Fact... rest) {
    doFail(sandwich(first, rest, butWas()));
  }

  // TODO(cpovirk): Consider making this protected if there's a need for it.
  final void failWithActual(Iterable<Fact> facts) {
    doFail(append(ImmutableList.copyOf(facts), butWas()));
  }

  /**
   * Reports a failure constructing a message from a simple verb.
   *
   * @param check the check being asserted
   * @deprecated Prefer to construct {@link Fact}-style methods, typically by using {@link
   *     #failWithActual(Fact, Fact...) failWithActual}{@code (}{@link Fact#simpleFact
   *     simpleFact(...)}{@code )}. However, if you want to preserve your exact failure message as a
   *     migration aid, you can inline this method (and then inline the resulting method call, as
   *     well).
   */
  @Deprecated
  final void fail(String check) {
    fail(check, new Object[0]);
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   *
   * @param verb the check being asserted
   * @param other the value against which the subject is compared
   * @deprecated Prefer to construct {@link Fact}-style methods, typically by using {@link
   *     #failWithActual(String, Object)}. However, if you want to preserve your exact failure
   *     message as a migration aid, you can inline this method (and then inline the resulting
   *     method call, as well).
   */
  @Deprecated
  final void fail(String verb, Object other) {
    fail(verb, new Object[] {other});
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   *
   * @param verb the check being asserted
   * @param messageParts the expectations against which the subject is compared
   * @deprecated Prefer to construct {@link Fact}-style methods, typically by using {@link
   *     #failWithActual(Fact, Fact...)}. However, if you want to preserve your exact failure
   *     message as a migration aid, you can inline this method.
   */
  @Deprecated
  final void fail(String verb, @Nullable Object... messageParts) {
    StringBuilder message = new StringBuilder("Not true that <");
    message.append(actualCustomStringRepresentation()).append("> ").append(verb);
    for (Object part : messageParts) {
      message.append(" <").append(part).append(">");
    }
    failWithoutActual(simpleFact(message.toString()));
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
   */
  final void failEqualityCheckForEqualsWithoutDescription(@Nullable Object expected) {
    failEqualityCheck(EqualityCheck.EQUAL, expected, ComparisonResult.differentNoDescription());
  }

  private void failEqualityCheck(
      EqualityCheck equalityCheck, @Nullable Object expected, ComparisonResult difference) {
    String actualString = actualCustomStringRepresentation();
    String expectedString = formatActualOrExpected(expected);
    String actualClass = actual == null ? "(null reference)" : actual.getClass().getName();
    String expectedClass = expected == null ? "(null reference)" : expected.getClass().getName();

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
        checkNotNull(metadata)
            .failEqualityCheck(difference.factsOrEmpty(), expectedString, actualString);
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

  private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

  private static char[] asUnicodeHexEscape(char c) {
    // Equivalent to String.format("\\u%04x", (int) c);
    char[] r = new char[6];
    r[0] = '\\';
    r[1] = 'u';
    r[5] = HEX_DIGITS[c & 0xF];
    c = (char) (c >>> 4);
    r[4] = HEX_DIGITS[c & 0xF];
    c = (char) (c >>> 4);
    r[3] = HEX_DIGITS[c & 0xF];
    c = (char) (c >>> 4);
    r[2] = HEX_DIGITS[c & 0xF];
    return r;
  }

  private void failEqualityCheckNoComparisonFailure(ComparisonResult difference, Fact... facts) {
    // TODO(cpovirk): Is it possible for difference.factsOrEmpty() to be nonempty? If not, remove.
    doFail(concat(asList(facts), difference.factsOrEmpty()));
  }

  /**
   * Assembles a failure message and passes it to the FailureStrategy
   *
   * @param verb the check being asserted
   * @param expected the expectations against which the subject is compared
   * @param failVerb the failure of the check being asserted
   * @param actual the actual value the subject was compared against
   * @deprecated Prefer to construct {@link Fact}-style methods, typically by using {@link
   *     #failWithActual(Fact, Fact...)}. However, if you want to preserve your exact failure
   *     message as a migration aid, you can inline this method.
   */
  @Deprecated
  final void failWithBadResults(String verb, Object expected, String failVerb, Object actual) {
    String message =
        lenientFormat(
            "Not true that <%s> %s <%s>. It %s <%s>",
            actualCustomStringRepresentation(),
            verb,
            expected,
            failVerb,
            (actual == null) ? "null reference" : actual);
    failWithoutActual(simpleFact(message));
  }

  /**
   * Assembles a failure message with an alternative representation of the wrapped subject and
   * passes it to the FailureStrategy
   *
   * @param verb the check being asserted
   * @param expected the expected value of the check
   * @param actual the custom representation of the subject to be reported in the failure.
   * @deprecated Prefer to construct {@link Fact}-style methods, typically by using {@link
   *     #failWithoutActual(Fact, Fact...)}. However, if you want to preserve your exact failure
   *     message as a migration aid, you can inline this method.
   */
  @Deprecated
  final void failWithCustomSubject(String verb, Object expected, Object actual) {
    String message =
        lenientFormat(
            "Not true that <%s> %s <%s>",
            (actual == null) ? "null reference" : actual, verb, expected);
    failWithoutActual(simpleFact(message));
  }

  /**
   * @deprecated Prefer to construct {@link Fact}-style methods, typically by using {@link
   *     #failWithoutActual(Fact, Fact...) failWithoutActual}{@code (}{@link Fact#simpleFact
   *     simpleFact(...)}{@code )}. However, if you want to preserve your exact failure message as a
   *     migration aid, you can inline this method.
   */
  @Deprecated
  final void failWithoutSubject(String check) {
    failWithoutActual(simpleFact(lenientFormat("Not true that the subject %s", check)));
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
   */
  protected final void failWithoutActual(Fact first, Fact... rest) {
    doFail(ImmutableList.copyOf(Lists.asList(first, rest)));
  }

  // TODO(cpovirk): Consider making this protected if there's a need for it.
  final void failWithoutActual(Iterable<Fact> facts) {
    doFail(ImmutableList.copyOf(facts));
  }

  /**
   * Assembles a failure message without a given subject and passes it to the FailureStrategy
   *
   * @param check the check being asserted
   * @deprecated Prefer to construct {@link Fact}-style methods, typically by using {@link
   *     #failWithoutActual(Fact, Fact...) failWithoutActual}{@code (}{@link Fact#simpleFact
   *     simpleFact(...)}{@code )}. However, if you want to preserve your exact failure message as a
   *     migration aid, you can inline this method (and then inline the resulting method call, as
   *     well).
   */
  @Deprecated
  final void failWithoutActual(String check) {
    failWithoutSubject(check);
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
  public final boolean equals(@Nullable Object o) {
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
   * Returns a "but was: <actual value>" string. This method should be rarely needed, since Truth
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
    return fact("but was", actualCustomStringRepresentation());
  }

  /*
   * Computed lazily so that we're not doing expensive string operations during every assertion,
   * only during every failure.
   */
  final String typeDescription() {
    return typeDescriptionOrGuess(getClass(), typeDescriptionOverride);
  }

  private static String typeDescriptionOrGuess(
      Class<? extends Subject> clazz, @Nullable String typeDescriptionOverride) {
    if (typeDescriptionOverride != null) {
      return typeDescriptionOverride;
    }
    /*
     * j2cl doesn't store enough metadata to know whether "Foo$BarSubject" is a nested class, so it
     * can't tell whether the simple name is "Foo$BarSubject" or just "BarSubject": b/71808768. It
     * returns "Foo$BarSubject" to err on the side of preserving information. We want just
     * "BarSubject," so we strip any likely enclosing type ourselves.
     */
    String subjectClass = clazz.getSimpleName().replaceFirst(".*[$]", "");
    String actualClass =
        (subjectClass.endsWith("Subject") && !subjectClass.equals("Subject"))
            ? subjectClass.substring(0, subjectClass.length() - "Subject".length())
            : "Object";
    return UPPER_CAMEL.to(LOWER_CAMEL, actualClass);
  }

  private static boolean classMetadataUnsupported() {
    // https://github.com/google/truth/issues/198
    // TODO(cpovirk): Consider whether to remove instanceof tests under GWT entirely.
    // TODO(cpovirk): Run more Truth tests under GWT, and add tests for this.
    return String.class.getSuperclass() == null;
  }

  private void doFail(ImmutableList<Fact> facts) {
    checkNotNull(metadata).fail(facts);
  }
}
