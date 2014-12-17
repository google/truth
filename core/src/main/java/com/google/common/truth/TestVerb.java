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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;

public class TestVerb extends AbstractVerb<TestVerb> {
  private final String failureMessage;

  public TestVerb(FailureStrategy failureStrategy) {
    super(failureStrategy);
    this.failureMessage = null;
  }

  public TestVerb(FailureStrategy failureStrategy, String failureMessage) {
    super(failureStrategy);
    this.failureMessage = failureMessage;
  }

  @CheckReturnValue
  public <T extends Comparable<?>> ComparableSubject<?, T> that(T target) {
    return new ComparableSubject(getFailureStrategy(), target) {};
  }

  @CheckReturnValue
  public Subject<DefaultSubject, Object> that(Object target) {
    return new DefaultSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  @GwtIncompatible("ClassSubject.java")
  public ClassSubject that(Class<?> target) {
    return new ClassSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public ThrowableSubject that(Throwable target) {
    return new ThrowableSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public LongSubject that(Long target) {
    return new LongSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public IntegerSubject that(Integer target) {
    return new IntegerSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public BooleanSubject that(Boolean target) {
    return new BooleanSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public StringSubject that(String target) {
    return new StringSubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <T, C extends Iterable<T>> IterableSubject<? extends IterableSubject<?, T, C>, T, C>
      that(Iterable<T> target) {
    return IterableSubject.create(getFailureStrategy(), target);
  }

  /**
   * @deprecated Use the {@link Iterable} overload instead.
   */
  @Deprecated
  @CheckReturnValue
  public <T, C extends Collection<T>>
      CollectionSubject<? extends CollectionSubject<?, T, C>, T, C> that(Collection<T> target) {
    return CollectionSubject.create(getFailureStrategy(), target);
  }

  /**
   * @deprecated Use the {@link Iterable} overload instead.
   */
  @Deprecated
  @CheckReturnValue
  public <T, C extends List<T>> ListSubject<? extends ListSubject<?, T, C>, T, C>
      that(List<T> target) {
    return ListSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <T> ObjectArraySubject<T> that(T[] target) {
    return new ObjectArraySubject<T>(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public PrimitiveBooleanArraySubject that(boolean[] target) {
    return new PrimitiveBooleanArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public PrimitiveIntArraySubject that(int[] target) {
    return new PrimitiveIntArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public PrimitiveLongArraySubject that(long[] target) {
    return new PrimitiveLongArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public PrimitiveCharArraySubject that(char[] target) {
    return new PrimitiveCharArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public PrimitiveByteArraySubject that(byte[] target) {
    return new PrimitiveByteArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public PrimitiveFloatArraySubject that(float[] target) {
    return new PrimitiveFloatArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public PrimitiveDoubleArraySubject that(double[] target) {
    return new PrimitiveDoubleArraySubject(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <T> OptionalSubject<T> that(Optional<T> target) {
    return new OptionalSubject<T>(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <K, V, M extends Map<K, V>> MapSubject<? extends MapSubject<?, K, V, M>, K, V, M>
      that(Map<K, V> target) {
    return MapSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <K, V, M extends Multimap<K, V>>
      MultimapSubject<? extends MultimapSubject<?, K, V, M>, K, V, M> that(Multimap<K, V> target) {
    return MultimapSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <K, V, M extends ListMultimap<K, V>>
      ListMultimapSubject<? extends ListMultimapSubject<?, K, V, M>, K, V, M> that(
      ListMultimap<K, V> target) {
    return ListMultimapSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <K, V, M extends SetMultimap<K, V>>
      SetMultimapSubject<? extends SetMultimapSubject<?, K, V, M>, K, V, M> that(
      SetMultimap<K, V> target) {
    return SetMultimapSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <E, M extends Multiset<E>>
      MultisetSubject<? extends MultisetSubject<?, E, M>, E, M> that(Multiset<E> target) {
    return MultisetSubject.create(getFailureStrategy(), target);
  }

  @CheckReturnValue
  public <R, C, V, M extends Table<R, C, V>>
      TableSubject<? extends TableSubject<?, R, C, V, M>, R, C, V, M> that(Table<R, C, V> target) {
    return TableSubject.create(getFailureStrategy(), target);
  }

  @Override
  @CheckReturnValue
  public TestVerb withFailureMessage(String failureMessage) {
    return new TestVerb(getFailureStrategy(), failureMessage); // Must be a new instance.
  }

  @Override public String getFailureMessage() {
    return failureMessage;
  }
}
