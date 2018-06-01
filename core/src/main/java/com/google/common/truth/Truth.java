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

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Optional;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.AtomicLongMap;
import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * The primary entry point for Truth, a fluent framework for test assertions.
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
 * assertThat(d).containsAllOf(a, b);
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
 * href="https://google.github.io/truth/faq#full-chain">this FAQ entry</a>.
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
public final class Truth {
  private Truth() {}

  private static final FailureStrategy THROW_ASSERTION_ERROR =
      new FailureStrategy() {
        @Override
        public void fail(AssertionError failure) {
          throw failure;
        }
      };

  private static final StandardSubjectBuilder ASSERT =
      StandardSubjectBuilder.forCustomFailureStrategy(THROW_ASSERTION_ERROR);

  /**
   * Begins a call chain with the fluent Truth API. If the check made by the chain fails, it will
   * throw {@link AssertionError}.
   */
  public static StandardSubjectBuilder assert_() {
    return ASSERT;
  }

  /**
   * Returns a {@link StandardSubjectBuilder} that will prepend the given message to the failure
   * message in the event of a test failure.
   */
  public static StandardSubjectBuilder assertWithMessage(String messageToPrepend) {
    return assert_().withMessage(messageToPrepend);
  }

  /**
   * Returns a {@link StandardSubjectBuilder} that will prepend the formatted message using the
   * specified arguments to the failure message in the event of a test failure.
   *
   * <p><b>Note:</b> the arguments will be substituted into the format template using {@link
   * com.google.common.base.Strings#lenientFormat Strings.lenientFormat}. Note this only supports
   * the {@code %s} specifier.
   *
   * @throws IllegalArgumentException if the number of placeholders in the format string does not
   *     equal the number of given arguments
   */
  public static StandardSubjectBuilder assertWithMessage(String format, Object... args) {
    return assert_().withMessage(format, args);
  }

  /**
   * Given a factory for some {@code Subject} class, returns a builder whose {@code that(actual)}
   * method creates instances of that class.
   */
  public static <S extends Subject<S, T>, T> SimpleSubjectBuilder<S, T> assertAbout(
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

  public static <T extends Comparable<?>> ComparableSubject<?, T> assertThat(
      @NullableDecl T actual) {
    return assert_().that(actual);
  }

  public static BigDecimalSubject assertThat(@NullableDecl BigDecimal actual) {
    return assert_().that(actual);
  }

  public static Subject<DefaultSubject, Object> assertThat(@NullableDecl Object actual) {
    return assert_().that(actual);
  }

  @GwtIncompatible("ClassSubject.java")
  public static ClassSubject assertThat(@NullableDecl Class<?> actual) {
    return assert_().that(actual);
  }

  public static ThrowableSubject assertThat(@NullableDecl Throwable actual) {
    return assert_().that(actual);
  }

  public static LongSubject assertThat(@NullableDecl Long actual) {
    return assert_().that(actual);
  }

  public static DoubleSubject assertThat(@NullableDecl Double actual) {
    return assert_().that(actual);
  }

  public static FloatSubject assertThat(@NullableDecl Float actual) {
    return assert_().that(actual);
  }

  public static IntegerSubject assertThat(@NullableDecl Integer actual) {
    return assert_().that(actual);
  }

  public static BooleanSubject assertThat(@NullableDecl Boolean actual) {
    return assert_().that(actual);
  }

  public static StringSubject assertThat(@NullableDecl String actual) {
    return assert_().that(actual);
  }

  public static IterableSubject assertThat(@NullableDecl Iterable<?> actual) {
    return assert_().that(actual);
  }

  public static SortedSetSubject assertThat(@NullableDecl SortedSet<?> actual) {
    return assert_().that(actual);
  }

  public static <T> ObjectArraySubject<T> assertThat(@NullableDecl T[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveBooleanArraySubject assertThat(@NullableDecl boolean[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveShortArraySubject assertThat(@NullableDecl short[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveIntArraySubject assertThat(@NullableDecl int[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveLongArraySubject assertThat(@NullableDecl long[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveByteArraySubject assertThat(@NullableDecl byte[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveCharArraySubject assertThat(@NullableDecl char[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveFloatArraySubject assertThat(@NullableDecl float[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveDoubleArraySubject assertThat(@NullableDecl double[] actual) {
    return assert_().that(actual);
  }

  public static GuavaOptionalSubject assertThat(@NullableDecl Optional<?> actual) {
    return assert_().that(actual);
  }

  public static MapSubject assertThat(@NullableDecl Map<?, ?> actual) {
    return assert_().that(actual);
  }

  public static SortedMapSubject assertThat(@NullableDecl SortedMap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static MultimapSubject assertThat(@NullableDecl Multimap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static ListMultimapSubject assertThat(@NullableDecl ListMultimap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static SetMultimapSubject assertThat(@NullableDecl SetMultimap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static MultisetSubject assertThat(@NullableDecl Multiset<?> actual) {
    return assert_().that(actual);
  }

  public static TableSubject assertThat(@NullableDecl Table<?, ?, ?> actual) {
    return assert_().that(actual);
  }

  public static AtomicLongMapSubject assertThat(@NullableDecl AtomicLongMap<?> actual) {
    return assert_().that(actual);
  }

  /**
   * An {@code AssertionError} that (a) always supports a cause, even under old versions of Android
   * and (b) omits "java.lang.AssertionError:" from the beginning of its toString() representation.
   */
  static final class SimpleAssertionError extends AssertionError {
    /** Separate cause field, in case initCause() fails. */
    @NullableDecl private final Throwable cause;

    // TODO(cpovirk): Figure out if we ever pass a null message to this.
    private SimpleAssertionError(
        String message, @NullableDecl String suffix, @NullableDecl Throwable cause) {
      super(appendSuffixIfNotNull(message, suffix));
      this.cause = cause;

      try {
        initCause(cause);
      } catch (IllegalStateException alreadyInitializedBecauseOfHarmonyBug) {
        // https://code.google.com/p/android/issues/detail?id=29378
        // We fall back to overriding getCause(). Well, we *always* override getCause(), so even
        // when initCause() works, it isn't doing much for us here other than forcing future
        // initCause() attempts to fail loudly rather than be silently ignored.
      }
    }

    static SimpleAssertionError create(
        String message, @NullableDecl String suffix, @NullableDecl Throwable cause) {
      return new SimpleAssertionError(message, suffix, cause);
    }

    static SimpleAssertionError createWithNoStack(String message, @NullableDecl Throwable cause) {
      SimpleAssertionError error = new SimpleAssertionError(message, /* suffix= */ null, cause);
      error.setStackTrace(new StackTraceElement[0]);
      return error;
    }

    static SimpleAssertionError createWithNoStack(String message) {
      return createWithNoStack(message, null);
    }

    @Override
    @SuppressWarnings("UnsynchronizedOverridesSynchronized")
    public Throwable getCause() {
      return cause;
    }

    @Override
    public String toString() {
      return getLocalizedMessage();
    }
  }

  @NullableDecl
  static String appendSuffixIfNotNull(String message, String suffix) {
    if (suffix != null) {
      message += "\n" + suffix;
    }
    return message;
  }
}
