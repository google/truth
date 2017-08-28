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
  public final <T extends Comparable<?>> ComparableSubject<?, T> that(@Nullable T target) {
    return new ComparableSubject(failureStrategy(), target) {};
  }

  @Override // temporarily
  public final BigDecimalSubject that(@Nullable BigDecimal target) {
    return new BigDecimalSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final Subject<DefaultSubject, Object> that(@Nullable Object target) {
    return new DefaultSubject(failureStrategy(), target);
  }

  @GwtIncompatible("ClassSubject.java")
  @Override // temporarily
  public final ClassSubject that(@Nullable Class<?> target) {
    return new ClassSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final ThrowableSubject that(@Nullable Throwable target) {
    return ThrowableSubject.create(failureStrategy(), target);
  }

  @Override // temporarily
  public final LongSubject that(@Nullable Long target) {
    return new LongSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final DoubleSubject that(@Nullable Double target) {
    return new DoubleSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final FloatSubject that(@Nullable Float target) {
    return new FloatSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final IntegerSubject that(@Nullable Integer target) {
    return new IntegerSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final BooleanSubject that(@Nullable Boolean target) {
    return new BooleanSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final StringSubject that(@Nullable String target) {
    return new StringSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final IterableSubject that(@Nullable Iterable<?> target) {
    return new IterableSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final SortedSetSubject that(@Nullable SortedSet<?> target) {
    return new SortedSetSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final <T> ObjectArraySubject<T> that(@Nullable T[] target) {
    return new ObjectArraySubject<T>(failureStrategy(), target);
  }

  @Override // temporarily
  public final PrimitiveBooleanArraySubject that(@Nullable boolean[] target) {
    return new PrimitiveBooleanArraySubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final PrimitiveShortArraySubject that(@Nullable short[] target) {
    return new PrimitiveShortArraySubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final PrimitiveIntArraySubject that(@Nullable int[] target) {
    return new PrimitiveIntArraySubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final PrimitiveLongArraySubject that(@Nullable long[] target) {
    return new PrimitiveLongArraySubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final PrimitiveCharArraySubject that(@Nullable char[] target) {
    return new PrimitiveCharArraySubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final PrimitiveByteArraySubject that(@Nullable byte[] target) {
    return new PrimitiveByteArraySubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final PrimitiveFloatArraySubject that(@Nullable float[] target) {
    return new PrimitiveFloatArraySubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final PrimitiveDoubleArraySubject that(@Nullable double[] target) {
    return new PrimitiveDoubleArraySubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final GuavaOptionalSubject that(@Nullable Optional<?> target) {
    return new GuavaOptionalSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final MapSubject that(@Nullable Map<?, ?> target) {
    return new MapSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final SortedMapSubject that(@Nullable SortedMap<?, ?> target) {
    return new SortedMapSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final MultimapSubject that(@Nullable Multimap<?, ?> target) {
    return new MultimapSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final ListMultimapSubject that(@Nullable ListMultimap<?, ?> target) {
    return new ListMultimapSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final SetMultimapSubject that(@Nullable SetMultimap<?, ?> target) {
    return new SetMultimapSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final MultisetSubject that(@Nullable Multiset<?> target) {
    return new MultisetSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final TableSubject that(@Nullable Table<?, ?, ?> target) {
    return new TableSubject(failureStrategy(), target);
  }

  @Override // temporarily
  public final AtomicLongMapSubject that(@Nullable AtomicLongMap<?> target) {
    return new AtomicLongMapSubject(failureStrategy(), target);
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
  // TODO(cpovirk): Remove SF type parameter. Change `SF factory` to `SubjectFactory<S, A> factory`
  public final <S extends Subject<S, A>, A, SF extends SubjectFactory<S, A>>
      SimpleSubjectBuilder<S, A> about(SF factory) {
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
