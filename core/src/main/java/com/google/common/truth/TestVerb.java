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

/*>>>import org.checkerframework.checker.nullness.compatqual.NullableType;*/
public class TestVerb extends AbstractVerb<TestVerb> {
  public TestVerb(FailureStrategy failureStrategy) {
    this(failureStrategy, null);
  }

  public TestVerb(FailureStrategy failureStrategy, @Nullable String message) {
    this(
        failureStrategy,
        message == null ? null : "%s",
        message == null ? new Object[0] : new Object[] {message});
  }

  public TestVerb(
      FailureStrategy failureStrategy, @Nullable String format, Object /*@NullableType*/... args) {
    super(checkNotNull(failureStrategy), format, args);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
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
    return ThrowableSubject.create(getFailureStrategy(), target);
  }

  public LongSubject that(@Nullable Long target) {
    return new LongSubject(getFailureStrategy(), target);
  }

  public DoubleSubject that(@Nullable Double target) {
    return new DoubleSubject(getFailureStrategy(), target);
  }

  public FloatSubject that(@Nullable Float target) {
    return new FloatSubject(getFailureStrategy(), target);
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

  public IterableSubject that(@Nullable Iterable<?> target) {
    return new IterableSubject(getFailureStrategy(), target);
  }

  public SortedSetSubject that(@Nullable SortedSet<?> target) {
    return new SortedSetSubject(getFailureStrategy(), target);
  }

  public <T> ObjectArraySubject<T> that(@Nullable T[] target) {
    return new ObjectArraySubject<T>(getFailureStrategy(), target);
  }

  public PrimitiveBooleanArraySubject that(@Nullable boolean[] target) {
    return new PrimitiveBooleanArraySubject(getFailureStrategy(), target);
  }

  public PrimitiveShortArraySubject that(@Nullable short[] target) {
    return new PrimitiveShortArraySubject(getFailureStrategy(), target);
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

  public GuavaOptionalSubject that(@Nullable Optional<?> target) {
    return new GuavaOptionalSubject(getFailureStrategy(), target);
  }

  public MapSubject that(@Nullable Map<?, ?> target) {
    return new MapSubject(getFailureStrategy(), target);
  }

  public SortedMapSubject that(@Nullable SortedMap<?, ?> target) {
    return new SortedMapSubject(getFailureStrategy(), target);
  }

  public MultimapSubject that(@Nullable Multimap<?, ?> target) {
    return new MultimapSubject(getFailureStrategy(), target);
  }

  public ListMultimapSubject that(@Nullable ListMultimap<?, ?> target) {
    return new ListMultimapSubject(getFailureStrategy(), target);
  }

  public SetMultimapSubject that(@Nullable SetMultimap<?, ?> target) {
    return new SetMultimapSubject(getFailureStrategy(), target);
  }

  public MultisetSubject that(@Nullable Multiset<?> target) {
    return new MultisetSubject(getFailureStrategy(), target);
  }

  public TableSubject that(@Nullable Table<?, ?, ?> target) {
    return new TableSubject(getFailureStrategy(), target);
  }

  public AtomicLongMapSubject that(@Nullable AtomicLongMap<?> target) {
    return new AtomicLongMapSubject(getFailureStrategy(), target);
  }

  @Override
  public TestVerb withFailureMessage(@Nullable String failureMessage) {
    return withMessage(failureMessage);
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
  @Override
  public TestVerb withFailureMessage(@Nullable String format, Object /* @NullableType */... args) {
    return withMessage(format, args);
  }

  @Override
  public TestVerb withMessage(@Nullable String messageToPrepend) {
    return new TestVerb(getFailureStrategy(), "%s", messageToPrepend); // Must be a new instance.
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
  @Override
  public TestVerb withMessage(@Nullable String format, Object /* @NullableType */... args) {
    return new TestVerb(getFailureStrategy(), format, args); // Must be a new instance.
  }
}
