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

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.google.j2objc.annotations.J2ObjCIncompatible;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The primary entry point for <a href="https://truth.dev">Truth</a>, a library for fluent test
 * assertions.
 *
 * <p>Compare these example JUnit assertions...
 *
 * <pre>{@code
 * assertEquals(b, a);
 * assertTrue(c);
 * assertTrue(d.contains(a));
 * assertTrue(d.contains(a) && d.contains(b));
 * assertTrue(d.contains(a) || d.contains(b) || d.contains(c));
 * }</pre>
 *
 * ...to their Truth equivalents...
 *
 * <pre>{@code
 * assertThat(a).isEqualTo(b);
 * assertThat(c).isTrue();
 * assertThat(d).contains(a);
 * assertThat(d).containsAtLeast(a, b);
 * assertThat(d).containsAnyOf(a, b, c);
 * }</pre>
 *
 * <p>Advantages of Truth:
 *
 * <ul>
 *   <li>aligns all the "actual" values on the left
 *   <li>produces more detailed failure messages
 *   <li>provides richer operations (like {@link IterableSubject#containsExactly})
 * </ul>
 *
 * <p>For more information about the methods in this class, see <a
 * href="https://truth.dev/faq#full-chain">this FAQ entry</a>.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>The most common way to extend Truth is to write a custom {@link Subject}. (The other, much
 * less common way is to write a custom {@link FailureStrategy}.) For more information, visit those
 * types' docs.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@NullMarked
public final class Truth {
  private Truth() {}

  @SuppressWarnings("ConstantCaseForConstants") // Despite the "Builder" name, it's not mutable.
  private static final StandardSubjectBuilder ASSERT =
      StandardSubjectBuilder.forCustomFailureStrategy(
          failure -> {
            throw failure;
          });

  /**
   * Begins a call chain with the fluent Truth API. If the check made by the chain fails, it will
   * throw {@link AssertionError}.
   */
  @SuppressWarnings("MemberName") // The underscore is a weird but intentional choice.
  public static StandardSubjectBuilder assert_() {
    return ASSERT;
  }

  /**
   * Begins an assertion that, if it fails, will prepend the given message to the failure message.
   *
   * <p>This method is a shortcut for {@code assert_().withMessage(...)}.
   *
   * <p>To set a message when using a custom subject, use {@code assertWithMessage(...).}{@link
   * StandardSubjectBuilder#about about(...)}, as discussed in <a
   * href="https://truth.dev/faq#java8">this FAQ entry</a>.
   */
  public static StandardSubjectBuilder assertWithMessage(@Nullable String messageToPrepend) {
    return assert_().withMessage(messageToPrepend);
  }

  /**
   * Begins an assertion that, if it fails, will prepend the given message to the failure message.
   *
   * <p><b>Note:</b> the arguments will be substituted into the format template using {@link
   * com.google.common.base.Strings#lenientFormat Strings.lenientFormat}. Note this only supports
   * the {@code %s} specifier.
   *
   * <p>This method is a shortcut for {@code assert_().withMessage(...)}.
   *
   * <p>To set a message when using a custom subject, use {@code assertWithMessage(...).}{@link
   * StandardSubjectBuilder#about about(...)}, as discussed in <a
   * href="https://truth.dev/faq#java8">this FAQ entry</a>.
   *
   * @throws IllegalArgumentException if the number of placeholders in the format string does not
   *     equal the number of given arguments
   */
  public static StandardSubjectBuilder assertWithMessage(String format, @Nullable Object... args) {
    return assert_().withMessage(format, args);
  }

  /**
   * Given a factory for some {@code Subject} class, returns a builder whose {@code that(actual)}
   * method creates instances of that class.
   */
  public static <S extends Subject, T> SimpleSubjectBuilder<S, T> assertAbout(
      Subject.Factory<S, T> factory) {
    return assert_().about(factory);
  }

  /**
   * A generic, advanced method of extension of Truth to new types, which is documented on {@link
   * CustomSubjectBuilder}. Extension creators should prefer {@link Subject.Factory} if possible.
   */
  public static <CustomSubjectBuilderT extends CustomSubjectBuilder>
      CustomSubjectBuilderT assertAbout(
          CustomSubjectBuilder.Factory<CustomSubjectBuilderT> factory) {
    return assert_().about(factory);
  }

  public static <ComparableT extends Comparable<?>> ComparableSubject<ComparableT> assertThat(
      @Nullable ComparableT actual) {
    return assert_().that(actual);
  }

  public static BigDecimalSubject assertThat(@Nullable BigDecimal actual) {
    return assert_().that(actual);
  }

  public static Subject assertThat(@Nullable Object actual) {
    return assert_().that(actual);
  }

  @GwtIncompatible("ClassSubject.java")
  @J2ktIncompatible
  public static ClassSubject assertThat(@Nullable Class<?> actual) {
    return assert_().that(actual);
  }

  /**
   * Begins an assertion about a {@link Throwable}.
   *
   * <p>Truth does not provide its own support for calling a method and automatically catching an
   * expected exception, only for asserting on the exception after it has been caught. To catch the
   * exception, we suggest {@link org.junit.Assert#assertThrows(Class,
   * org.junit.function.ThrowingRunnable) assertThrows} (JUnit), <a
   * href="https://kotlinlang.org/api/latest/kotlin.test/kotlin.test/assert-fails-with.html">{@code
   * assertFailsWith}</a> ({@code kotlin.test}), or similar functionality from your testing library
   * of choice.
   *
   * <pre>
   * InvocationTargetException expected =
   *     assertThrows(InvocationTargetException.class, () -> method.invoke(null));
   * assertThat(expected).hasCauseThat().isInstanceOf(IOException.class);
   * </pre>
   */
  public static ThrowableSubject assertThat(@Nullable Throwable actual) {
    return assert_().that(actual);
  }

  public static LongSubject assertThat(@Nullable Long actual) {
    return assert_().that(actual);
  }

  public static DoubleSubject assertThat(@Nullable Double actual) {
    return assert_().that(actual);
  }

  public static FloatSubject assertThat(@Nullable Float actual) {
    return assert_().that(actual);
  }

  public static IntegerSubject assertThat(@Nullable Integer actual) {
    return assert_().that(actual);
  }

  public static BooleanSubject assertThat(@Nullable Boolean actual) {
    return assert_().that(actual);
  }

  public static StringSubject assertThat(@Nullable String actual) {
    return assert_().that(actual);
  }

  public static IterableSubject assertThat(@Nullable Iterable<?> actual) {
    return assert_().that(actual);
  }

  @SuppressWarnings("AvoidObjectArrays")
  public static <T extends @Nullable Object> ObjectArraySubject<T> assertThat(
      T @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveBooleanArraySubject assertThat(boolean @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveShortArraySubject assertThat(short @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveIntArraySubject assertThat(int @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveLongArraySubject assertThat(long @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveByteArraySubject assertThat(byte @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveCharArraySubject assertThat(char @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveFloatArraySubject assertThat(float @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveDoubleArraySubject assertThat(double @Nullable [] actual) {
    return assert_().that(actual);
  }

  public static GuavaOptionalSubject assertThat(
      com.google.common.base.@Nullable Optional<?> actual) {
    return assert_().that(actual);
  }

  public static MapSubject assertThat(@Nullable Map<?, ?> actual) {
    return assert_().that(actual);
  }

  public static MultimapSubject assertThat(@Nullable Multimap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static MultisetSubject assertThat(@Nullable Multiset<?> actual) {
    return assert_().that(actual);
  }

  public static TableSubject assertThat(@Nullable Table<?, ?, ?> actual) {
    return assert_().that(actual);
  }

  /**
   * @since 1.3.0 (present in {@link Truth8} since before 1.0)
   */
  @SuppressWarnings({
    "Java7ApiChecker", // no more dangerous than wherever the user got the Optional
    "NullableOptional", // Truth always accepts nulls, no matter the type
  })
  public static OptionalSubject assertThat(@Nullable Optional<?> actual) {
    return assert_().that(actual);
  }

  /**
   * @since 1.3.0 (present in {@link Truth8} since before 1.0)
   */
  @SuppressWarnings("Java7ApiChecker") // no more dangerous than wherever the user got the Stream
  public static OptionalIntSubject assertThat(@Nullable OptionalInt actual) {
    return assert_().that(actual);
  }

  /**
   * @since 1.4.0 (present in {@link Truth8} since before 1.0)
   */
  @SuppressWarnings("Java7ApiChecker") // no more dangerous than wherever the user got the Stream
  public static OptionalLongSubject assertThat(@Nullable OptionalLong actual) {
    return assert_().that(actual);
  }

  /**
   * @since 1.4.0 (present in {@link Truth8} since before 1.0)
   */
  @SuppressWarnings("Java7ApiChecker") // no more dangerous than wherever the user got the Stream
  public static OptionalDoubleSubject assertThat(@Nullable OptionalDouble actual) {
    return assert_().that(actual);
  }

  /**
   * @since 1.4.0 (present in {@link Truth8} since before 1.0)
   */
  @SuppressWarnings("Java7ApiChecker") // no more dangerous than wherever the user got the Stream
  public static StreamSubject assertThat(@Nullable Stream<?> actual) {
    return assert_().that(actual);
  }

  /**
   * @since 1.4.0 (present in {@link Truth8} since before 1.0)
   */
  @SuppressWarnings("Java7ApiChecker") // no more dangerous than wherever the user got the Stream
  public static IntStreamSubject assertThat(@Nullable IntStream actual) {
    return assert_().that(actual);
  }

  /**
   * @since 1.4.0 (present in {@link Truth8} since before 1.0)
   */
  @SuppressWarnings("Java7ApiChecker") // no more dangerous than wherever the user got the Stream
  public static LongStreamSubject assertThat(@Nullable LongStream actual) {
    return assert_().that(actual);
  }

  // TODO(b/64757353): Add support for DoubleStream?

  /**
   * @since 1.4.0 (present in {@link Truth8} since before 1.0)
   */
  @GwtIncompatible
  @J2ObjCIncompatible
  @J2ktIncompatible
  public static PathSubject assertThat(@Nullable Path actual) {
    return assert_().that(actual);
  }

  /**
   * An {@code AssertionError} that omits "java.lang.AssertionError:" from the beginning of its
   * toString() representation.
   */
  // TODO(cpovirk): Consider eliminating this, adding its functionality to AssertionErrorWithFacts?
  @SuppressWarnings("OverrideThrowableToString") // We intentionally replace the normal format.
  static final class SimpleAssertionError extends AssertionError {
    private SimpleAssertionError(String message, @Nullable Throwable cause) {
      super(checkNotNull(message), cause);
    }

    static SimpleAssertionError create(String message, @Nullable Throwable cause) {
      return new SimpleAssertionError(message, cause);
    }

    static SimpleAssertionError createWithNoStack(String message, @Nullable Throwable cause) {
      SimpleAssertionError error = create(message, cause);
      error.setStackTrace(new StackTraceElement[0]);
      return error;
    }

    static SimpleAssertionError createWithNoStack(String message) {
      return createWithNoStack(message, /* cause= */ null);
    }

    @Override
    public String toString() {
      return checkNotNull(getLocalizedMessage());
    }
  }
}
