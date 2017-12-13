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
import static com.google.common.truth.StringUtil.format;

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
 * In a fluent assertion chain, an object with which you can do any of the following:
 *
 * <ul>
 *   <li>Set an optional message with {@link #withMessage}.
 *   <li>Specify the type of {@code Subject} to create with {@link #about(Subject.Factory)}.
 *   <li>For the types of {@code Subject} built into Truth, directly specify the value under test
 *       with {@link #that(Object)}.
 * </ul>
 *
 * <p>For more information about the methods in this class, see <a
 * href="https://google.github.io/truth/faq#full-chain">this FAQ entry</a>.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>You won't extend this type. When you write a custom subject, see <a
 * href="https://google.github.io/truth/extension">our doc on extensions</a>.
 */
public class StandardSubjectBuilder {
  /**
   * Returns a new instance that invokes the given {@code FailureStrategy} when a check fails. Most
   * users should not need this. If you think you do, see the documentation on {@link
   * FailureStrategy}.
   */
  public static StandardSubjectBuilder forCustomFailureStrategy(FailureStrategy failureStrategy) {
    return new StandardSubjectBuilder(FailureMetadata.forFailureStrategy(failureStrategy));
  }

  private final FailureMetadata metadataDoNotReferenceDirectly;

  StandardSubjectBuilder(FailureMetadata metadata) {
    this.metadataDoNotReferenceDirectly = checkNotNull(metadata);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public final <ComparableT extends Comparable<?>> ComparableSubject<?, ComparableT> that(
      @Nullable ComparableT actual) {
    return new ComparableSubject(metadata(), actual) {};
  }

  public final BigDecimalSubject that(@Nullable BigDecimal actual) {
    return new BigDecimalSubject(metadata(), actual);
  }

  public final Subject<DefaultSubject, Object> that(@Nullable Object actual) {
    return new DefaultSubject(metadata(), actual);
  }

  @GwtIncompatible("ClassSubject.java")
  public final ClassSubject that(@Nullable Class<?> actual) {
    return new ClassSubject(metadata(), actual);
  }

  public final ThrowableSubject that(@Nullable Throwable actual) {
    return new ThrowableSubject(metadata(), actual);
  }

  public final LongSubject that(@Nullable Long actual) {
    return new LongSubject(metadata(), actual);
  }

  public final DoubleSubject that(@Nullable Double actual) {
    return new DoubleSubject(metadata(), actual);
  }

  public final FloatSubject that(@Nullable Float actual) {
    return new FloatSubject(metadata(), actual);
  }

  public final IntegerSubject that(@Nullable Integer actual) {
    return new IntegerSubject(metadata(), actual);
  }

  public final BooleanSubject that(@Nullable Boolean actual) {
    return new BooleanSubject(metadata(), actual);
  }

  public final StringSubject that(@Nullable String actual) {
    return new StringSubject(metadata(), actual);
  }

  public final IterableSubject that(@Nullable Iterable<?> actual) {
    return new IterableSubject(metadata(), actual);
  }

  public final SortedSetSubject that(@Nullable SortedSet<?> actual) {
    return new SortedSetSubject(metadata(), actual);
  }

  public final <T> ObjectArraySubject<T> that(@Nullable T[] actual) {
    return new ObjectArraySubject<T>(metadata(), actual);
  }

  public final PrimitiveBooleanArraySubject that(@Nullable boolean[] actual) {
    return new PrimitiveBooleanArraySubject(metadata(), actual);
  }

  public final PrimitiveShortArraySubject that(@Nullable short[] actual) {
    return new PrimitiveShortArraySubject(metadata(), actual);
  }

  public final PrimitiveIntArraySubject that(@Nullable int[] actual) {
    return new PrimitiveIntArraySubject(metadata(), actual);
  }

  public final PrimitiveLongArraySubject that(@Nullable long[] actual) {
    return new PrimitiveLongArraySubject(metadata(), actual);
  }

  public final PrimitiveCharArraySubject that(@Nullable char[] actual) {
    return new PrimitiveCharArraySubject(metadata(), actual);
  }

  public final PrimitiveByteArraySubject that(@Nullable byte[] actual) {
    return new PrimitiveByteArraySubject(metadata(), actual);
  }

  public final PrimitiveFloatArraySubject that(@Nullable float[] actual) {
    return new PrimitiveFloatArraySubject(metadata(), actual);
  }

  public final PrimitiveDoubleArraySubject that(@Nullable double[] actual) {
    return new PrimitiveDoubleArraySubject(metadata(), actual);
  }

  public final GuavaOptionalSubject that(@Nullable Optional<?> actual) {
    return new GuavaOptionalSubject(metadata(), actual);
  }

  public final MapSubject that(@Nullable Map<?, ?> actual) {
    return new MapSubject(metadata(), actual);
  }

  public final SortedMapSubject that(@Nullable SortedMap<?, ?> actual) {
    return new SortedMapSubject(metadata(), actual);
  }

  public final MultimapSubject that(@Nullable Multimap<?, ?> actual) {
    return new MultimapSubject(metadata(), actual);
  }

  public final ListMultimapSubject that(@Nullable ListMultimap<?, ?> actual) {
    return new ListMultimapSubject(metadata(), actual);
  }

  public final SetMultimapSubject that(@Nullable SetMultimap<?, ?> actual) {
    return new SetMultimapSubject(metadata(), actual);
  }

  public final MultisetSubject that(@Nullable Multiset<?> actual) {
    return new MultisetSubject(metadata(), actual);
  }

  public final TableSubject that(@Nullable Table<?, ?, ?> actual) {
    return new TableSubject(metadata(), actual);
  }

  public final AtomicLongMapSubject that(@Nullable AtomicLongMap<?> actual) {
    return new AtomicLongMapSubject(metadata(), actual);
  }

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
  public final StandardSubjectBuilder withMessage(
      @Nullable String format, Object /* @NullableType */... args) {
    return new StandardSubjectBuilder(metadata().withMessage(format, args));
  }

  /**
   * Given a factory for some {@code Subject} class, returns a builder whose {@code that(actual)}
   * method creates instances of that class. Created subjects use the previously set failure
   * strategy and any previously set failure message.
   */
  public final <S extends Subject<S, A>, A> SimpleSubjectBuilder<S, A> about(
      Subject.Factory<S, A> factory) {
    return new SimpleSubjectBuilder<S, A>(metadata(), factory);
  }

  public final <CustomSubjectBuilderT extends CustomSubjectBuilder> CustomSubjectBuilderT about(
      CustomSubjectBuilder.Factory<CustomSubjectBuilderT> factory) {
    return factory.createSubjectBuilder(metadata());
  }

  /** Triggers the failure strategy with an empty failure message */
  public final void fail() {
    metadata().fail("");
  }

  /** Triggers the failure strategy with the given failure message */
  public final void fail(@Nullable String format, Object /*@NullableType*/... args) {
    metadata().fail(format(format, args));
  }

  private FailureMetadata metadata() {
    checkStatePreconditions();
    return metadataDoNotReferenceDirectly;
  }

  /**
   * Extension point invoked before every assertion. This allows {@link Expect} to check that it's
   * been set up properly as a {@code TestRule}.
   */
  void checkStatePreconditions() {}
}
