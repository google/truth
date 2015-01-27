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

import java.util.Map;

import javax.annotation.CheckReturnValue;

@CheckReturnValue
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

  public <T extends Comparable<?>> ComparableSubject<?, T> that(T target) {
    return new ComparableSubject(getFailureStrategy(), target) {};
  }

  public Subject<DefaultSubject, Object> that(Object target) {
    return new DefaultSubject(getFailureStrategy(), target);
  }

  @GwtIncompatible("ClassSubject.java")
  public ClassSubject that(Class<?> target) {
    return new ClassSubject(getFailureStrategy(), target);
  }

  public ThrowableSubject that(Throwable target) {
    return new ThrowableSubject(getFailureStrategy(), target);
  }

  public LongSubject that(Long target) {
    return new LongSubject(getFailureStrategy(), target);
  }

  public DoubleSubject that(Double target) {
    return new DoubleSubject(getFailureStrategy(), target);
  }

  public IntegerSubject that(Integer target) {
    return new IntegerSubject(getFailureStrategy(), target);
  }

  public BooleanSubject that(Boolean target) {
    return new BooleanSubject(getFailureStrategy(), target);
  }

  public StringSubject that(String target) {
    return new StringSubject(getFailureStrategy(), target);
  }

  public <T, C extends Iterable<T>> IterableSubject<? extends IterableSubject<?, T, C>, T, C>
      that(Iterable<T> target) {
    return IterableSubject.create(getFailureStrategy(), target);
  }

  public <T> ObjectArraySubject<T> that(T[] target) {
    return new ObjectArraySubject<T>(getFailureStrategy(), target);
  }

  public PrimitiveBooleanArraySubject that(boolean[] target) {
    return new PrimitiveBooleanArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveIntArraySubject that(int[] target) {
    return new PrimitiveIntArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveLongArraySubject that(long[] target) {
    return new PrimitiveLongArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveCharArraySubject that(char[] target) {
    return new PrimitiveCharArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveByteArraySubject that(byte[] target) {
    return new PrimitiveByteArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveFloatArraySubject that(float[] target) {
    return new PrimitiveFloatArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveDoubleArraySubject that(double[] target) {
    return new PrimitiveDoubleArraySubject(getFailureStrategy(), target);
  }

  public <T> OptionalSubject<T> that(Optional<T> target) {
    return new OptionalSubject<T>(getFailureStrategy(), target);
  }

  public <K, V, M extends Map<K, V>> MapSubject<? extends MapSubject<?, K, V, M>, K, V, M>
      that(Map<K, V> target) {
    return MapSubject.create(getFailureStrategy(), target);
  }

  public <K, V, M extends Multimap<K, V>>
      MultimapSubject<? extends MultimapSubject<?, K, V, M>, K, V, M> that(Multimap<K, V> target) {
    return MultimapSubject.create(getFailureStrategy(), target);
  }

  public <K, V, M extends ListMultimap<K, V>>
      ListMultimapSubject<? extends ListMultimapSubject<?, K, V, M>, K, V, M> that(
      ListMultimap<K, V> target) {
    return ListMultimapSubject.create(getFailureStrategy(), target);
  }

  public <K, V, M extends SetMultimap<K, V>>
      SetMultimapSubject<? extends SetMultimapSubject<?, K, V, M>, K, V, M> that(
      SetMultimap<K, V> target) {
    return SetMultimapSubject.create(getFailureStrategy(), target);
  }

  public <E, M extends Multiset<E>>
      MultisetSubject<? extends MultisetSubject<?, E, M>, E, M> that(Multiset<E> target) {
    return MultisetSubject.create(getFailureStrategy(), target);
  }

  public <R, C, V, M extends Table<R, C, V>>
      TableSubject<? extends TableSubject<?, R, C, V, M>, R, C, V, M> that(Table<R, C, V> target) {
    return TableSubject.create(getFailureStrategy(), target);
  }

  @Override
  public TestVerb withFailureMessage(String failureMessage) {
    return new TestVerb(getFailureStrategy(), failureMessage); // Must be a new instance.
  }

  @Override public String getFailureMessage() {
    return failureMessage;
  }
}
