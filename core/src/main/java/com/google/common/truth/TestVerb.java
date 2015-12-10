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

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

@CheckReturnValue
public class TestVerb extends AbstractVerb<TestVerb> {
  public TestVerb(FailureStrategy failureStrategy) {
    super(checkNotNull(failureStrategy));
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
  public TestVerb withFailureMessage(@Nullable String format, @Nullable Object... args) {
    checkNotNull(args);
    int numOfPlaceholders = countPlaceholders(format);
    if (numOfPlaceholders != args.length) {
      throw new IllegalArgumentException(
          StringUtil.format(
              "The number of placeholders (%s) in the format string (%s) "
                  + "does not equal the number of args (%s) given",
              numOfPlaceholders,
              format,
              args.length));
    }
    return new TestVerb(failureStrategyWithMessage(format, args));
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

  // TODO(b/26080262): Remove the <T> type param
  public <T> IterableSubject that(@Nullable Iterable<T> target) {
    return new IterableSubject(getFailureStrategy(), target);
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

  public MultimapSubject that(@Nullable Multimap<?, ?> target) {
    return new MultimapSubject(getFailureStrategy(), target);
  }

  public ListMultimapSubject that(@Nullable ListMultimap<?, ?> target) {
    return new ListMultimapSubject(getFailureStrategy(), target);
  }

  public SetMultimapSubject that(@Nullable SetMultimap<?, ?> target) {
    return new SetMultimapSubject(getFailureStrategy(), target);
  }

  // TODO(b/26080262): Remove the <E> type param
  public <E> MultisetSubject that(@Nullable Multiset<E> target) {
    return new MultisetSubject(getFailureStrategy(), target);
  }

  public TableSubject that(@Nullable Table<?, ?, ?> target) {
    return new TableSubject(getFailureStrategy(), target);
  }

  static int countPlaceholders(@Nullable String template) {
    if (template == null) {
      return 0;
    }
    int index = 0;
    int count = 0;
    while (true) {
      index = template.indexOf("%s", index);
      if (index == -1) {
        break;
      }
      index++;
      count++;
    }
    return count;
  }
}
