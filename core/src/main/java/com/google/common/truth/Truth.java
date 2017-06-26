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
import com.google.common.truth.AbstractVerb.DelegatedVerb;
import com.google.common.util.concurrent.AtomicLongMap;
import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nullable;

/**
 * Truth - a proposition framework for tests, supporting JUnit style assertion and assumption
 * semantics in a fluent style.
 *
 * <p>Truth is the simplest entry point class. A developer can statically import the assertThat()
 * method to get easy access to the library's capabilities. Then, instead of writing:
 *
 * <pre>{@code
 * Assert.assertEquals(a, b);
 * Assert.assertTrue(c);
 * Assert.assertTrue(d.contains(a));
 * Assert.assertTrue(d.contains(a) && d.contains(b));
 * Assert.assertTrue(d.contains(a) || d.contains(b) || d.contains(c));
 * }</pre>
 *
 * one would write:
 *
 * <pre>{@code
 * assertThat(a).isEqualTo(b);
 * assertThat(c).isTrue();
 * assertThat(d).contains(a);
 * assertThat(d).containsAllOf(a, b);
 * assertThat(d).containsAnyOf(a, b, c);
 * }</pre>
 *
 * <p>Tests should be easier to read, and flow more clearly.
 *
 * <p>Often, tests assert a relationship between a value produced by the test (the "actual" value)
 * and some reference value (the "expected" value). It is strongly recommended that the actual value
 * is made the subject of the assertion. For example:
 *
 * <pre>{@code
 * assertThat(actual).isEqualTo(expected);  // recommended
 * assertThat(expected).isEqualTo(actual);  // not recommended
 * assertThat(actual).isIn(expectedPossibilities);  // recommended
 * assertThat(expectedPossibilities).contains(actual);  // not recommended
 * }</pre>
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public final class Truth {
  private Truth() {}

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
              // No message, but it's the best we can do without awful hacks.
              throw stripTruthStackFrames(new AssertionError(cause));
            }
          }
          return stripTruthStackFrames(failure);
        }
      };

  private static final TestVerb ASSERT = new TestVerb(THROW_ASSERTION_ERROR);

  public static TestVerb assert_() {
    return ASSERT;
  }

  /**
   * Returns a {@link TestVerb} that will prepend the given message to the failure message in the
   * event of a test failure.
   */
  public static TestVerb assertWithMessage(String messageToPrepend) {
    return assert_().withFailureMessage(messageToPrepend);
  }

  /**
   * Returns a {@link TestVerb} that will prepend the formatted message using the specified
   * arguments to the failure message in the event of a test failure.
   *
   * <p><b>Note:</b> The failure message template string only supports the {@code "%s"} specifier,
   * not the full range of {@link java.util.Formatter} specifiers.
   *
   * @throws IllegalArgumentException if the number of placeholders in the format string does not
   *     equal the number of given arguments
   */
  public static TestVerb assertWithMessage(String format, Object... args) {
    return assert_().withFailureMessage(format, args);
  }

  /**
   * The recommended method of extension of Truth to new types, which is documented in {@link
   * com.google.common.truth.delegation.DelegationTest}.
   *
   * @param factory a SubjectFactory<S, T> implementation
   * @return A custom verb for the type returned by the SubjectFactory
   */
  public static <S extends Subject<S, T>, T> DelegatedVerb<S, T> assertAbout(
      SubjectFactory<S, T> factory) {
    return assert_().about(factory);
  }

  /**
   * A generic, advanced method of extension of Truth to new types, which is documented on {@link
   * DelegatedVerbFactory}. Extension creators should prefer {@link SubjectFactory} if possible.
   *
   * @param <V> the type of {@link AbstractDelegatedVerb} to return
   * @param factory a {@code DelegatedVerbFactory<V>} implementation
   * @return A custom verb of type {@code <V>}
   */
  public static <V extends AbstractDelegatedVerb> V assertAbout(DelegatedVerbFactory<V> factory) {
    return assert_().about(factory);
  }

  public static <T extends Comparable<?>> ComparableSubject<?, T> assertThat(@Nullable T target) {
    return assert_().that(target);
  }

  public static BigDecimalSubject assertThat(@Nullable BigDecimal target) {
    return assert_().that(target);
  }

  public static Subject<DefaultSubject, Object> assertThat(@Nullable Object target) {
    return assert_().that(target);
  }

  @GwtIncompatible("ClassSubject.java")
  public static ClassSubject assertThat(@Nullable Class<?> target) {
    return assert_().that(target);
  }

  public static ThrowableSubject assertThat(@Nullable Throwable target) {
    return assert_().that(target);
  }

  public static LongSubject assertThat(@Nullable Long target) {
    return assert_().that(target);
  }

  public static DoubleSubject assertThat(@Nullable Double target) {
    return assert_().that(target);
  }

  public static FloatSubject assertThat(@Nullable Float target) {
    return assert_().that(target);
  }

  public static IntegerSubject assertThat(@Nullable Integer target) {
    return assert_().that(target);
  }

  public static BooleanSubject assertThat(@Nullable Boolean target) {
    return assert_().that(target);
  }

  public static StringSubject assertThat(@Nullable String target) {
    return assert_().that(target);
  }

  public static IterableSubject assertThat(@Nullable Iterable<?> target) {
    return assert_().that(target);
  }

  public static SortedSetSubject assertThat(@Nullable SortedSet<?> target) {
    return assert_().that(target);
  }

  public static <T> ObjectArraySubject<T> assertThat(@Nullable T[] target) {
    return assert_().that(target);
  }

  public static PrimitiveBooleanArraySubject assertThat(@Nullable boolean[] target) {
    return assert_().that(target);
  }

  public static PrimitiveShortArraySubject assertThat(@Nullable short[] target) {
    return assert_().that(target);
  }

  public static PrimitiveIntArraySubject assertThat(@Nullable int[] target) {
    return assert_().that(target);
  }

  public static PrimitiveLongArraySubject assertThat(@Nullable long[] target) {
    return assert_().that(target);
  }

  public static PrimitiveByteArraySubject assertThat(@Nullable byte[] target) {
    return assert_().that(target);
  }

  public static PrimitiveCharArraySubject assertThat(@Nullable char[] target) {
    return assert_().that(target);
  }

  public static PrimitiveFloatArraySubject assertThat(@Nullable float[] target) {
    return assert_().that(target);
  }

  public static PrimitiveDoubleArraySubject assertThat(@Nullable double[] target) {
    return assert_().that(target);
  }

  public static GuavaOptionalSubject assertThat(@Nullable Optional<?> target) {
    return assert_().that(target);
  }

  public static MapSubject assertThat(@Nullable Map<?, ?> target) {
    return assert_().that(target);
  }

  public static SortedMapSubject assertThat(@Nullable SortedMap<?, ?> target) {
    return assert_().that(target);
  }

  public static MultimapSubject assertThat(@Nullable Multimap<?, ?> target) {
    return assert_().that(target);
  }

  public static ListMultimapSubject assertThat(@Nullable ListMultimap<?, ?> target) {
    return assert_().that(target);
  }

  public static SetMultimapSubject assertThat(@Nullable SetMultimap<?, ?> target) {
    return assert_().that(target);
  }

  public static MultisetSubject assertThat(@Nullable Multiset<?> target) {
    return assert_().that(target);
  }

  public static TableSubject assertThat(@Nullable Table<?, ?, ?> target) {
    return assert_().that(target);
  }

  public static AtomicLongMapSubject assertThat(@Nullable AtomicLongMap<?> target) {
    return assert_().that(target);
  }
}
