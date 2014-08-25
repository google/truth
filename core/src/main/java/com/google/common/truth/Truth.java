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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;

/**
 * Truth - a proposition framework for tests, supporting JUnit style
 * assertion and assumption semantics in a fluent style.
 *
 * Truth is the simplest entry point class. A developer can statically
 * import the assertThat() method to get easy access to the library's
 * capabilities. Then, instead of writing:
 *
 * <pre>{@code
 * Assert.assertEquals(a, b);
 * Assert.assertTrue(c);
 * Assert.assertTrue(d.contains(a));
 * Assert.assertTrue(d.contains(a) && d.contains(b));
 * Assert.assertTrue(d.contains(a) || d.contains(b) || d.contains(c));
 * }</pre>
 * one would write:
 * <pre>{@code
 * assertThat(a).isEqualTo(b);
 * assertThat(c).isTrue();
 * assertThat(d).has().item(a);
 * assertThat(d).has().allOf(a, b);
 * assertThat(d).has().anyOf(a, b, c);
 * }</pre>
 *
 * Tests should be easier to read, and flow more clearly.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class Truth {

  public static final FailureStrategy THROW_ASSERTION_ERROR =
      new FailureStrategy() {
        @Override public void failComparing(
            String message, CharSequence expected, CharSequence actual) {
          throw Platform.comparisonFailure(message, expected.toString(), actual.toString());
        }
      };

  public static final TestVerb ASSERT = new TestVerb(THROW_ASSERTION_ERROR);

  public static TestVerb assert_() { return ASSERT; }

  private static FailureStrategy getFailureStrategy() {
    return THROW_ASSERTION_ERROR;
  }

  @CheckReturnValue
  public static Subject<DefaultSubject, Object> assertThat(Object target) {
    return new DefaultSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  @GwtIncompatible("ClassSubject.java")
  public static ClassSubject assertThat(Class<?> target) {
    return new ClassSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static IntegerSubject assertThat(Long target) {
    return new IntegerSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static IntegerSubject assertThat(Integer target) {
    return new IntegerSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static BooleanSubject assertThat(Boolean target) {
    return new BooleanSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static StringSubject assertThat(String target) {
    return new StringSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static <T, C extends Iterable<T>> IterableSubject<? extends IterableSubject<?, T, C>, T, C>
      assertThat(Iterable<T> target) {
    return IterableSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static <T, C extends Collection<T>>
      CollectionSubject<? extends CollectionSubject<?, T, C>, T, C>
      assertThat(Collection<T> target) {
    return CollectionSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static <T, C extends List<T>> ListSubject<? extends ListSubject<?, T, C>, T, C>
      assertThat(List<T> target) {
    return ListSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static <T> ObjectArraySubject<T> assertThat(T[] target) {
    return new ObjectArraySubject<T>(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static PrimitiveBooleanArraySubject assertThat(boolean[] target) {
    return new PrimitiveBooleanArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static PrimitiveIntArraySubject assertThat(int[] target) {
    return new PrimitiveIntArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static PrimitiveLongArraySubject assertThat(long[] target) {
    return new PrimitiveLongArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static PrimitiveCharArraySubject assertThat(char[] target) {
    return new PrimitiveCharArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static PrimitiveFloatArraySubject assertThat(float[] target) {
    return new PrimitiveFloatArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static PrimitiveDoubleArraySubject assertThat(double[] target) {
    return new PrimitiveDoubleArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static <T> OptionalSubject<T> assertThat(Optional<T> target) {
    return new OptionalSubject<T>(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public static <K, V, M extends Map<K, V>> MapSubject<? extends MapSubject<?, K, V, M>, K, V, M>
      assertThat(Map<K, V> target) {
    return MapSubject.create(getFailureStrategy(), target);
  }

}
