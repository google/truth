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
/**
 * @deprecated Instead of subclassing {@code TestVerb}, subclass {@link CustomSubjectBuilder}.
 *     {@code CustomSubjectBuilder} is the new way of defining custom {@code that()} methods, and it
 *     doesn't require you to write boilerplate to store and propagate the failure message.
 */
@Deprecated
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
  public <ComparableT extends Comparable<?>> ComparableSubject<?, ComparableT> that(
      @Nullable ComparableT actual) {
    return new ComparableSubject(getFailureStrategy(), actual) {};
  }

  public BigDecimalSubject that(@Nullable BigDecimal actual) {
    return new BigDecimalSubject(getFailureStrategy(), actual);
  }

  public Subject<DefaultSubject, Object> that(@Nullable Object actual) {
    return new DefaultSubject(getFailureStrategy(), actual);
  }

  @GwtIncompatible("ClassSubject.java")
  public ClassSubject that(@Nullable Class<?> actual) {
    return new ClassSubject(getFailureStrategy(), actual);
  }

  public ThrowableSubject that(@Nullable Throwable actual) {
    return ThrowableSubject.create(getFailureStrategy(), actual);
  }

  public LongSubject that(@Nullable Long actual) {
    return new LongSubject(getFailureStrategy(), actual);
  }

  public DoubleSubject that(@Nullable Double actual) {
    return new DoubleSubject(getFailureStrategy(), actual);
  }

  public FloatSubject that(@Nullable Float actual) {
    return new FloatSubject(getFailureStrategy(), actual);
  }

  public IntegerSubject that(@Nullable Integer actual) {
    return new IntegerSubject(getFailureStrategy(), actual);
  }

  public BooleanSubject that(@Nullable Boolean actual) {
    return new BooleanSubject(getFailureStrategy(), actual);
  }

  public StringSubject that(@Nullable String actual) {
    return new StringSubject(getFailureStrategy(), actual);
  }

  public IterableSubject that(@Nullable Iterable<?> actual) {
    return new IterableSubject(getFailureStrategy(), actual);
  }

  public SortedSetSubject that(@Nullable SortedSet<?> actual) {
    return new SortedSetSubject(getFailureStrategy(), actual);
  }

  public <T> ObjectArraySubject<T> that(@Nullable T[] actual) {
    return new ObjectArraySubject<T>(getFailureStrategy(), actual);
  }

  public PrimitiveBooleanArraySubject that(@Nullable boolean[] actual) {
    return new PrimitiveBooleanArraySubject(getFailureStrategy(), actual);
  }

  public PrimitiveShortArraySubject that(@Nullable short[] actual) {
    return new PrimitiveShortArraySubject(getFailureStrategy(), actual);
  }

  public PrimitiveIntArraySubject that(@Nullable int[] actual) {
    return new PrimitiveIntArraySubject(getFailureStrategy(), actual);
  }

  public PrimitiveLongArraySubject that(@Nullable long[] actual) {
    return new PrimitiveLongArraySubject(getFailureStrategy(), actual);
  }

  public PrimitiveCharArraySubject that(@Nullable char[] actual) {
    return new PrimitiveCharArraySubject(getFailureStrategy(), actual);
  }

  public PrimitiveByteArraySubject that(@Nullable byte[] actual) {
    return new PrimitiveByteArraySubject(getFailureStrategy(), actual);
  }

  public PrimitiveFloatArraySubject that(@Nullable float[] actual) {
    return new PrimitiveFloatArraySubject(getFailureStrategy(), actual);
  }

  public PrimitiveDoubleArraySubject that(@Nullable double[] actual) {
    return new PrimitiveDoubleArraySubject(getFailureStrategy(), actual);
  }

  public GuavaOptionalSubject that(@Nullable Optional<?> actual) {
    return new GuavaOptionalSubject(getFailureStrategy(), actual);
  }

  public MapSubject that(@Nullable Map<?, ?> actual) {
    return new MapSubject(getFailureStrategy(), actual);
  }

  public SortedMapSubject that(@Nullable SortedMap<?, ?> actual) {
    return new SortedMapSubject(getFailureStrategy(), actual);
  }

  public MultimapSubject that(@Nullable Multimap<?, ?> actual) {
    return new MultimapSubject(getFailureStrategy(), actual);
  }

  public ListMultimapSubject that(@Nullable ListMultimap<?, ?> actual) {
    return new ListMultimapSubject(getFailureStrategy(), actual);
  }

  public SetMultimapSubject that(@Nullable SetMultimap<?, ?> actual) {
    return new SetMultimapSubject(getFailureStrategy(), actual);
  }

  public MultisetSubject that(@Nullable Multiset<?> actual) {
    return new MultisetSubject(getFailureStrategy(), actual);
  }

  public TableSubject that(@Nullable Table<?, ?, ?> actual) {
    return new TableSubject(getFailureStrategy(), actual);
  }

  public AtomicLongMapSubject that(@Nullable AtomicLongMap<?> actual) {
    return new AtomicLongMapSubject(getFailureStrategy(), actual);
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
