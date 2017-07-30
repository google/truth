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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.StringUtil.format;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
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
 * In a fluent assertion chain, an object with which you can do any of the following:
 *
 * <ul>
 *   <li>Set an optional message with {@link #withMessage}.
 *   <li>Specify the type of {@code Subject} to create with {@link #about(SubjectFactory)}.
 *   <li>For the types of {@code Subject} built into Truth, directly specify the value under test
 *       with {@link #that(Object)}.
 * </ul>
 *
 * <p>TODO(cpovirk): Link to a doc about the full assertion chain.
 *
 * <h2>For people extending Truth</h2>
 *
 * <p>TODO(cpovirk): Link to a doc about custom subjects.
 */
// TODO(cpovirk): remove TestVerb superclass
public class StandardSubjectBuilder extends TestVerb {
  /**
   * Returns a new instance that invokes the given {@code FailureStrategy} when a check fails. Most
   * users should not need this. If you think you do, see the documentation on {@link
   * FailureStrategy}.
   */
  public static StandardSubjectBuilder forCustomFailureStrategy(FailureStrategy failureStrategy) {
    return new StandardSubjectBuilder(failureStrategy);
  }

  private final FailureStrategy failureStrategyDoNotReferenceDirectly;

  StandardSubjectBuilder(FailureStrategy failureStrategy) {
    super(failureStrategy);
    this.failureStrategyDoNotReferenceDirectly = checkNotNull(failureStrategy);
  }

  @Override // temporarily
  @SuppressWarnings({"unchecked", "rawtypes"})
  public final <ComparableT extends Comparable<?>> ComparableSubject<?, ComparableT> that(
      @Nullable ComparableT actual) {
    return new ComparableSubject(failureStrategy(), actual) {};
  }

  @Override // temporarily
  public final BigDecimalSubject that(@Nullable BigDecimal actual) {
    return new BigDecimalSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final Subject<DefaultSubject, Object> that(@Nullable Object actual) {
    return new DefaultSubject(failureStrategy(), actual);
  }

  @GwtIncompatible("ClassSubject.java")
  @Override // temporarily
  public final ClassSubject that(@Nullable Class<?> actual) {
    return new ClassSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final ThrowableSubject that(@Nullable Throwable actual) {
    return ThrowableSubject.create(failureStrategy(), actual);
  }

  @Override // temporarily
  public final LongSubject that(@Nullable Long actual) {
    return new LongSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final DoubleSubject that(@Nullable Double actual) {
    return new DoubleSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final FloatSubject that(@Nullable Float actual) {
    return new FloatSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final IntegerSubject that(@Nullable Integer actual) {
    return new IntegerSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final BooleanSubject that(@Nullable Boolean actual) {
    return new BooleanSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final StringSubject that(@Nullable String actual) {
    return new StringSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final IterableSubject that(@Nullable Iterable<?> actual) {
    return new IterableSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final SortedSetSubject that(@Nullable SortedSet<?> actual) {
    return new SortedSetSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final <T> ObjectArraySubject<T> that(@Nullable T[] actual) {
    return new ObjectArraySubject<T>(failureStrategy(), actual);
  }

  @Override // temporarily
  public final PrimitiveBooleanArraySubject that(@Nullable boolean[] actual) {
    return new PrimitiveBooleanArraySubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final PrimitiveShortArraySubject that(@Nullable short[] actual) {
    return new PrimitiveShortArraySubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final PrimitiveIntArraySubject that(@Nullable int[] actual) {
    return new PrimitiveIntArraySubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final PrimitiveLongArraySubject that(@Nullable long[] actual) {
    return new PrimitiveLongArraySubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final PrimitiveCharArraySubject that(@Nullable char[] actual) {
    return new PrimitiveCharArraySubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final PrimitiveByteArraySubject that(@Nullable byte[] actual) {
    return new PrimitiveByteArraySubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final PrimitiveFloatArraySubject that(@Nullable float[] actual) {
    return new PrimitiveFloatArraySubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final PrimitiveDoubleArraySubject that(@Nullable double[] actual) {
    return new PrimitiveDoubleArraySubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final GuavaOptionalSubject that(@Nullable Optional<?> actual) {
    return new GuavaOptionalSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final MapSubject that(@Nullable Map<?, ?> actual) {
    return new MapSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final SortedMapSubject that(@Nullable SortedMap<?, ?> actual) {
    return new SortedMapSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final MultimapSubject that(@Nullable Multimap<?, ?> actual) {
    return new MultimapSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final ListMultimapSubject that(@Nullable ListMultimap<?, ?> actual) {
    return new ListMultimapSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final SetMultimapSubject that(@Nullable SetMultimap<?, ?> actual) {
    return new SetMultimapSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final MultisetSubject that(@Nullable Multiset<?> actual) {
    return new MultisetSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final TableSubject that(@Nullable Table<?, ?, ?> actual) {
    return new TableSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final AtomicLongMapSubject that(@Nullable AtomicLongMap<?> actual) {
    return new AtomicLongMapSubject(failureStrategy(), actual);
  }

  @Override // temporarily
  public final StandardSubjectBuilder withMessage(@Nullable String messageToPrepend) {
    return withMessage("%s", messageToPrepend);
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
  @Override // temporarily
  public final StandardSubjectBuilder withMessage(
      @Nullable String format, Object /* @NullableType */... args) {
    return new StandardSubjectBuilder(
        new MessagePrependingFailureStrategy(failureStrategy(), format, args));
  }

  /**
   * Given a factory for some {@code Subject} class, returns a builder whose {@code that(actual)}
   * method creates instances of that class. Created subjects use the previously set failure
   * strategy and any previously set failure message.
   */
  @Override // temporarily
  public final <S extends Subject<S, A>, A> SimpleSubjectBuilder<S, A> about(
      SubjectFactory<S, A> factory) {
    return new SimpleSubjectBuilder<S, A>(failureStrategy(), factory);
  }

  /**
   * A generic, advanced method of extension of Truth to new types, which is documented on {@link
   * CustomSubjectBuilder}. Extension creators should prefer {@link SubjectFactory} if possible.
   */
  public final <CustomSubjectBuilderT extends CustomSubjectBuilder> CustomSubjectBuilderT about(
      CustomSubjectBuilderFactory<CustomSubjectBuilderT> factory) {
    return factory.createSubjectBuilder(failureStrategy());
  }

  /** Triggers the failure strategy with an empty failure message */
  @Override // temporarily
  public final void fail() {
    failureStrategy().fail("");
  }

  /** Triggers the failure strategy with the given failure message */
  @Override // temporarily
  public final void fail(@Nullable String format, Object /*@NullableType*/... args) {
    failureStrategy().fail(format(format, args));
  }

  private FailureStrategy failureStrategy() {
    checkStatePreconditions();
    return failureStrategyDoNotReferenceDirectly;
  }

  /**
   * Extension point invoked before every assertion. This allows {@link Expect} to check that it's
   * been set up properly as a {@code TestRule}.
   */
  void checkStatePreconditions() {}

  private static final class MessagePrependingFailureStrategy extends FailureStrategy {
    private static final String PLACEHOLDER_ERR =
        "Incorrect number of args (%s) for the given placeholders (%s) in string template:\"%s\"";

    private final FailureStrategy delegate;
    private final String format;
    private final Object[] args;

    MessagePrependingFailureStrategy(
        FailureStrategy delegate, @Nullable String format, @Nullable Object... args) {
      this.delegate = checkNotNull(delegate);
      this.format = format;
      this.args = args;
      int placeholders = countPlaceholders(format);
      checkArgument(
          placeholders == args.length, PLACEHOLDER_ERR, args.length, placeholders, format);
    }

    @Override
    public void fail(String message) {
      delegate.fail(prependFailureMessageIfAny(message));
    }

    @Override
    public void fail(String message, Throwable cause) {
      delegate.fail(prependFailureMessageIfAny(message), cause);
    }

    @Override
    public void failComparing(String message, CharSequence expected, CharSequence actual) {
      delegate.failComparing(prependFailureMessageIfAny(message), expected, actual);
    }

    @Override
    public void failComparing(
        String message, CharSequence expected, CharSequence actual, Throwable cause) {
      delegate.failComparing(prependFailureMessageIfAny(message), expected, actual, cause);
    }

    private String prependFailureMessageIfAny(String message) {
      return format == null ? message : StringUtil.format(format, args) + ": " + message;
    }
  }

  @VisibleForTesting
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
