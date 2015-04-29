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

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

@CheckReturnValue
public class TestVerb extends AbstractVerb<TestVerb> {
  @Nullable private final String failureMessage;

  public TestVerb(FailureStrategy failureStrategy) {
    this(failureStrategy, null);
  }

  public TestVerb(FailureStrategy failureStrategy, @Nullable String failureMessage) {
    super(failureStrategy);
    this.failureMessage = failureMessage;
  }

  public <T extends Comparable<?>> ComparableSubject<?, T> that(@Nullable T target) {
    return new ComparableSubject(getFailureStrategy(), target) {};
  }

  public BigDecimalSubject that(@Nullable BigDecimal target) {
    return new BigDecimalSubject(getFailureStrategy(), target);
  }

  public Subject<DefaultSubject, Object> that(@Nullable Object target) {
    return new DefaultSubject(getFailureStrategy(), target);
  }

  @GwtIncompatible("ClassSubject.java")
  public ClassSubject that(@Nullable Class<?> target) {
    return new ClassSubject(getFailureStrategy(), target);
  }

  public ThrowableSubject that(@Nullable Throwable target) {
    return new ThrowableSubject(getFailureStrategy(), target);
  }

  public LongSubject that(@Nullable Long target) {
    return new LongSubject(getFailureStrategy(), target);
  }

  public DoubleSubject that(@Nullable Double target) {
    return new DoubleSubject(getFailureStrategy(), target);
  }

  public IntegerSubject that(@Nullable Integer target) {
    return new IntegerSubject(getFailureStrategy(), target);
  }

  public BooleanSubject that(@Nullable Boolean target) {
    return new BooleanSubject(getFailureStrategy(), target);
  }

  public StringSubject that(@Nullable String target) {
    return new StringSubject(getFailureStrategy(), target);
  }

  public <T, C extends Iterable<T>> IterableSubject<? extends IterableSubject<?, T, C>, T, C>
      that(@Nullable Iterable<T> target) {
    return IterableSubject.create(getFailureStrategy(), target);
  }

  public <T> ObjectArraySubject<T> that(@Nullable T[] target) {
    return new ObjectArraySubject<T>(getFailureStrategy(), target);
  }

  public PrimitiveBooleanArraySubject that(@Nullable boolean[] target) {
    return new PrimitiveBooleanArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveIntArraySubject that(@Nullable int[] target) {
    return new PrimitiveIntArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveLongArraySubject that(@Nullable long[] target) {
    return new PrimitiveLongArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveCharArraySubject that(@Nullable char[] target) {
    return new PrimitiveCharArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveByteArraySubject that(@Nullable byte[] target) {
    return new PrimitiveByteArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveFloatArraySubject that(@Nullable float[] target) {
    return new PrimitiveFloatArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveDoubleArraySubject that(@Nullable double[] target) {
    return new PrimitiveDoubleArraySubject(getFailureStrategy(), target);
  }

  public <T> OptionalSubject<T> that(@Nullable Optional<T> target) {
    return new OptionalSubject<T>(getFailureStrategy(), target);
  }

  public MapSubject that(@Nullable Map<?, ?> target) {
    return new MapSubject(getFailureStrategy(), target);
  }

  public <K, V, M extends Multimap<K, V>>
      MultimapSubject<? extends MultimapSubject<?, K, V, M>, K, V, M> that(
          @Nullable Multimap<K, V> target) {
    return MultimapSubject.create(getFailureStrategy(), target);
  }

  public <K, V, M extends ListMultimap<K, V>>
      ListMultimapSubject<? extends ListMultimapSubject<?, K, V, M>, K, V, M> that(
          @Nullable ListMultimap<K, V> target) {
    return ListMultimapSubject.create(getFailureStrategy(), target);
  }

  public <K, V, M extends SetMultimap<K, V>>
      SetMultimapSubject<? extends SetMultimapSubject<?, K, V, M>, K, V, M> that(
          @Nullable SetMultimap<K, V> target) {
    return SetMultimapSubject.create(getFailureStrategy(), target);
  }

  public <E, M extends Multiset<E>>
      MultisetSubject<? extends MultisetSubject<?, E, M>, E, M> that(
          @Nullable Multiset<E> target) {
    return MultisetSubject.create(getFailureStrategy(), target);
  }

  public TableSubject that(@Nullable Table<?, ?, ?> target) {
    return new TableSubject(getFailureStrategy(), target);
  }

  @Override
  public TestVerb withFailureMessage(@Nullable String failureMessage) {
    return new TestVerb(getFailureStrategy(), failureMessage); // Must be a new instance.
  }

  @Override @Nullable public String getFailureMessage() {
    return failureMessage;
  }
}
