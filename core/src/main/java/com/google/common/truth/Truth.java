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
import javax.annotation.Nullable;

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

  /**
   * @deprecated If you are passing this to a {@link Subject} constructor, instead call {@link
   *     #assertAbout(SubjectFactory)}{@code .that(...)}, passing its associated {@link
   *     SubjectFactory} (which you might need to create). If you are calling {@code fail} on this
   *     object directly, instead use {@link #assert_()}{@code .fail()}, or {@code throw new
   *     AssertionError()} directly.
   */
  @Deprecated
  public static final FailureStrategy THROW_ASSERTION_ERROR =
      new AbstractFailureStrategy() {
        @Override
        public void fail(String message, Throwable cause) {
          throw stripFramesAndTryToAddCause(new AssertionError(message), cause);
        }

        @Override
        public void failComparing(
            String message, CharSequence expected, CharSequence actual, @Nullable Throwable cause) {
          AssertionError e =
              Platform.comparisonFailure(message, expected.toString(), actual.toString());
          throw stripFramesAndTryToAddCause(e, cause);
        }

        private AssertionError stripFramesAndTryToAddCause(
            AssertionError failure, @Nullable Throwable cause) {
          if (cause != null) {
            try {
              failure.initCause(cause);
            } catch (IllegalStateException alreadyInitializedBecauseOfHarmonyBug) {
              // https://code.google.com/p/android/issues/detail?id=29378
              // Skip initCause. That's sad, but it's the best we can do without awful hacks.
              // TODO(cpovirk): Actually, maybe we can override getCause(). Try that someday.
            }
          }
          return stripTruthStackFrames(failure);
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
   * <p><b>Note:</b> The failure message template string only supports the {@code "%s"} specifier,
   * not the full range of {@link java.util.Formatter} specifiers.
   *
   * @throws IllegalArgumentException if the number of placeholders in the format string does not
   *     equal the number of given arguments
   */
  public static StandardSubjectBuilder assertWithMessage(String format, Object... args) {
    return assert_().withMessage(format, args);
  }

  /**
   * The recommended method of extension of Truth to new types, which is documented in {@link
   * com.google.common.truth.delegation.DelegationTest}.
   *
   * @param factory a SubjectFactory<S, T> implementation
   * @return A custom verb for the type returned by the SubjectFactory
   * @deprecated When you switch from {@link SubjectFactory} to {@link Subject.Factory}, you'll
   *     switch from this overload to the {@code Subject.Factory} overload.
   */
  @Deprecated
  public static <S extends Subject<S, T>, T> SimpleSubjectBuilder<S, T> assertAbout(
      SubjectFactory<S, T> factory) {
    return assert_().about(factory);
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
   *
   * @deprecated When you switch from {@link CustomSubjectBuilderFactory} to {@link
   *     CustomSubjectBuilder.Factory}, you'll switch from this overload to the {@code
   *     CustomSubjectBuilder.Factory} overload.
   */
  @Deprecated
  public static <CustomSubjectBuilderT extends CustomSubjectBuilder>
      CustomSubjectBuilderT assertAbout(
          CustomSubjectBuilderFactory<CustomSubjectBuilderT> factory) {
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

  public static <T extends Comparable<?>> ComparableSubject<?, T> assertThat(@Nullable T actual) {
    return assert_().that(actual);
  }

  public static BigDecimalSubject assertThat(@Nullable BigDecimal actual) {
    return assert_().that(actual);
  }

  public static Subject<DefaultSubject, Object> assertThat(@Nullable Object actual) {
    return assert_().that(actual);
  }

  @GwtIncompatible("ClassSubject.java")
  public static ClassSubject assertThat(@Nullable Class<?> actual) {
    return assert_().that(actual);
  }

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

  public static SortedSetSubject assertThat(@Nullable SortedSet<?> actual) {
    return assert_().that(actual);
  }

  public static <T> ObjectArraySubject<T> assertThat(@Nullable T[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveBooleanArraySubject assertThat(@Nullable boolean[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveShortArraySubject assertThat(@Nullable short[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveIntArraySubject assertThat(@Nullable int[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveLongArraySubject assertThat(@Nullable long[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveByteArraySubject assertThat(@Nullable byte[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveCharArraySubject assertThat(@Nullable char[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveFloatArraySubject assertThat(@Nullable float[] actual) {
    return assert_().that(actual);
  }

  public static PrimitiveDoubleArraySubject assertThat(@Nullable double[] actual) {
    return assert_().that(actual);
  }

  public static GuavaOptionalSubject assertThat(@Nullable Optional<?> actual) {
    return assert_().that(actual);
  }

  public static MapSubject assertThat(@Nullable Map<?, ?> actual) {
    return assert_().that(actual);
  }

  public static SortedMapSubject assertThat(@Nullable SortedMap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static MultimapSubject assertThat(@Nullable Multimap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static ListMultimapSubject assertThat(@Nullable ListMultimap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static SetMultimapSubject assertThat(@Nullable SetMultimap<?, ?> actual) {
    return assert_().that(actual);
  }

  public static MultisetSubject assertThat(@Nullable Multiset<?> actual) {
    return assert_().that(actual);
  }

  public static TableSubject assertThat(@Nullable Table<?, ?, ?> actual) {
    return assert_().that(actual);
  }

  public static AtomicLongMapSubject assertThat(@Nullable AtomicLongMap<?> actual) {
    return assert_().that(actual);
  }
}
