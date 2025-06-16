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
import static com.google.common.truth.BigDecimalSubject.bigDecimals;
import static com.google.common.truth.BooleanSubject.booleans;
import static com.google.common.truth.ClassSubject.classes;
import static com.google.common.truth.DoubleSubject.doubles;
import static com.google.common.truth.FloatSubject.floats;
import static com.google.common.truth.GuavaOptionalSubject.guavaOptionals;
import static com.google.common.truth.IntStreamSubject.intStreams;
import static com.google.common.truth.IntegerSubject.integers;
import static com.google.common.truth.IterableSubject.iterables;
import static com.google.common.truth.LongStreamSubject.longStreams;
import static com.google.common.truth.LongSubject.longs;
import static com.google.common.truth.MapSubject.maps;
import static com.google.common.truth.MultimapSubject.multimaps;
import static com.google.common.truth.MultisetSubject.multisets;
import static com.google.common.truth.OptionalDoubleSubject.optionalDoubles;
import static com.google.common.truth.OptionalIntSubject.optionalInts;
import static com.google.common.truth.OptionalLongSubject.optionalLongs;
import static com.google.common.truth.OptionalSubject.optionals;
import static com.google.common.truth.PathSubject.paths;
import static com.google.common.truth.PrimitiveBooleanArraySubject.booleanArrays;
import static com.google.common.truth.PrimitiveByteArraySubject.byteArrays;
import static com.google.common.truth.PrimitiveCharArraySubject.charArrays;
import static com.google.common.truth.PrimitiveDoubleArraySubject.doubleArrays;
import static com.google.common.truth.PrimitiveFloatArraySubject.floatArrays;
import static com.google.common.truth.PrimitiveIntArraySubject.intArrays;
import static com.google.common.truth.PrimitiveLongArraySubject.longArrays;
import static com.google.common.truth.PrimitiveShortArraySubject.shortArrays;
import static com.google.common.truth.StreamSubject.streams;
import static com.google.common.truth.StringSubject.strings;
import static com.google.common.truth.Subject.objects;
import static com.google.common.truth.TableSubject.tables;
import static com.google.common.truth.ThrowableSubject.throwables;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.google.j2objc.annotations.J2ObjCIncompatible;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * In a fluent assertion chain, an object with which you can do any of the following:
 *
 * <ul>
 *   <li>Set an optional message with {@link #withMessage}.
 *   <li>Specify the type of {@link Subject} to create with {@link #about(Subject.Factory)}.
 *   <li>For the types of {@link Subject} built into Truth, directly specify the value under test
 *       with {@link #that(Object)}.
 * </ul>
 *
 * <p>For more information about the methods in this class, see <a
 * href="https://truth.dev/faq#full-chain">this FAQ entry</a>.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>You won't extend this type. When you write a custom subject, see <a
 * href="https://truth.dev/extension">our doc on extensions</a>.
 */
public class StandardSubjectBuilder {
  /**
   * Returns a new instance that invokes the given {@link FailureStrategy} when a check fails. Most
   * users should not need this. If you think you do, see the documentation on {@link
   * FailureStrategy}.
   */
  public static StandardSubjectBuilder forCustomFailureStrategy(FailureStrategy strategy) {
    return new StandardSubjectBuilder(FailureMetadata.forFailureStrategy(strategy));
  }

  private final FailureMetadata metadataDoNotReferenceDirectly;

  /**
   * Constructor for use by {@link Expect}. To create an instance of {@link StandardSubjectBuilder},
   * use {@link #forCustomFailureStrategy}.
   */
  StandardSubjectBuilder(FailureMetadata metadata) {
    this.metadataDoNotReferenceDirectly = checkNotNull(metadata);
  }

  public final <ComparableT extends Comparable<?>> ComparableSubject<ComparableT> that(
      @Nullable ComparableT actual) {
    return about(ComparableSubject.<ComparableT>comparables()).that(actual);
  }

  public final BigDecimalSubject that(@Nullable BigDecimal actual) {
    return about(bigDecimals()).that(actual);
  }

  public final Subject that(@Nullable Object actual) {
    return about(objects()).that(actual);
  }

  @GwtIncompatible("ClassSubject.java")
  @J2ktIncompatible
  public final ClassSubject that(@Nullable Class<?> actual) {
    return about(classes()).that(actual);
  }

  public final ThrowableSubject that(@Nullable Throwable actual) {
    return about(throwables()).that(actual);
  }

  public final LongSubject that(@Nullable Long actual) {
    return about(longs()).that(actual);
  }

  public final DoubleSubject that(@Nullable Double actual) {
    return about(doubles()).that(actual);
  }

  public final FloatSubject that(@Nullable Float actual) {
    return about(floats()).that(actual);
  }

  public final IntegerSubject that(@Nullable Integer actual) {
    return about(integers()).that(actual);
  }

  public final BooleanSubject that(@Nullable Boolean actual) {
    return about(booleans()).that(actual);
  }

  public final StringSubject that(@Nullable String actual) {
    return about(strings()).that(actual);
  }

  public final IterableSubject that(@Nullable Iterable<?> actual) {
    return about(iterables()).that(actual);
  }

  @SuppressWarnings("AvoidObjectArrays")
  public final <T extends @Nullable Object> ObjectArraySubject<T> that(T @Nullable [] actual) {
    return about(ObjectArraySubject.<T>objectArrays()).that(actual);
  }

  public final PrimitiveBooleanArraySubject that(boolean @Nullable [] actual) {
    return about(booleanArrays()).that(actual);
  }

  public final PrimitiveShortArraySubject that(short @Nullable [] actual) {
    return about(shortArrays()).that(actual);
  }

  public final PrimitiveIntArraySubject that(int @Nullable [] actual) {
    return about(intArrays()).that(actual);
  }

  public final PrimitiveLongArraySubject that(long @Nullable [] actual) {
    return about(longArrays()).that(actual);
  }

  public final PrimitiveCharArraySubject that(char @Nullable [] actual) {
    return about(charArrays()).that(actual);
  }

  public final PrimitiveByteArraySubject that(byte @Nullable [] actual) {
    return about(byteArrays()).that(actual);
  }

  public final PrimitiveFloatArraySubject that(float @Nullable [] actual) {
    return about(floatArrays()).that(actual);
  }

  public final PrimitiveDoubleArraySubject that(double @Nullable [] actual) {
    return about(doubleArrays()).that(actual);
  }

  public final GuavaOptionalSubject that(com.google.common.base.@Nullable Optional<?> actual) {
    return about(guavaOptionals()).that(actual);
  }

  public final MapSubject that(@Nullable Map<?, ?> actual) {
    return about(maps()).that(actual);
  }

  public final MultimapSubject that(@Nullable Multimap<?, ?> actual) {
    return about(multimaps()).that(actual);
  }

  public final MultisetSubject that(@Nullable Multiset<?> actual) {
    return about(multisets()).that(actual);
  }

  public final TableSubject that(@Nullable Table<?, ?, ?> actual) {
    return about(tables()).that(actual);
  }

  /**
   * @since 1.3.0 (with access to {@link OptionalSubject} previously part of {@code
   *     truth-java8-extension})
   */
  @SuppressWarnings("NullableOptional") // Truth always accepts nulls, no matter the type
  public final OptionalSubject that(@Nullable Optional<?> actual) {
    return about(optionals()).that(actual);
  }

  /**
   * @since 1.4.0 (with access to {@link OptionalIntSubject} previously part of {@code
   *     truth-java8-extension})
   */
  public final OptionalIntSubject that(@Nullable OptionalInt actual) {
    return about(optionalInts()).that(actual);
  }

  /**
   * @since 1.4.0 (with access to {@link OptionalLongSubject} previously part of {@code
   *     truth-java8-extension})
   */
  public final OptionalLongSubject that(@Nullable OptionalLong actual) {
    return about(optionalLongs()).that(actual);
  }

  /**
   * @since 1.4.0 (with access to {@link OptionalDoubleSubject} previously part of {@code
   *     truth-java8-extension})
   */
  public final OptionalDoubleSubject that(@Nullable OptionalDouble actual) {
    return about(optionalDoubles()).that(actual);
  }

  /**
   * @since 1.3.0 (with access to {@link StreamSubject} previously part of {@code
   *     truth-java8-extension})
   */
  public final StreamSubject that(@Nullable Stream<?> actual) {
    return about(streams()).that(actual);
  }

  /**
   * @since 1.4.0 (with access to {@link IntStreamSubject} previously part of {@code
   *     truth-java8-extension})
   */
  public final IntStreamSubject that(@Nullable IntStream actual) {
    return about(intStreams()).that(actual);
  }

  /**
   * @since 1.4.0 (with access to {@link LongStreamSubject} previously part of {@code
   *     truth-java8-extension})
   */
  public final LongStreamSubject that(@Nullable LongStream actual) {
    return about(longStreams()).that(actual);
  }

  // TODO(b/64757353): Add support for DoubleStream?

  /**
   * @since 1.4.0 (with access to {@link PathSubject} previously part of {@code
   *     truth-java8-extension})
   */
  @GwtIncompatible
  @J2ObjCIncompatible
  @J2ktIncompatible
  public final PathSubject that(@Nullable Path actual) {
    return about(paths()).that(actual);
  }

  /**
   * Returns a new instance that will output the given message before the main failure message. If
   * this method is called multiple times, the messages will appear in the order that they were
   * specified.
   */
  public final StandardSubjectBuilder withMessage(@Nullable String message) {
    return withMessage("%s", message);
  }

  /**
   * Returns a new instance that will output the given message before the main failure message. If
   * this method is called multiple times, the messages will appear in the order that they were
   * specified.
   *
   * <p><b>Note:</b> the arguments will be substituted into the format template using {@link
   * com.google.common.base.Strings#lenientFormat Strings.lenientFormat}. Note this only supports
   * the {@code %s} specifier.
   *
   * @throws IllegalArgumentException if the number of placeholders in the format string does not
   *     equal the number of given arguments
   */
  public final StandardSubjectBuilder withMessage(String format, @Nullable Object... args) {
    return new StandardSubjectBuilder(metadata().withMessage(format, args));
  }

  /**
   * Given a factory for some {@link Subject} class, returns a builder whose {@link
   * SimpleSubjectBuilder#that that(actual)} method creates instances of that class. Created
   * subjects use the previously set failure strategy and any previously set failure message.
   */
  public final <S extends Subject, A> SimpleSubjectBuilder<S, A> about(
      Subject.Factory<S, A> factory) {
    return SimpleSubjectBuilder.create(metadata(), factory);
  }

  /**
   * A generic, advanced method of extension of Truth to new types, which is documented on {@link
   * CustomSubjectBuilder}. Extension creators should prefer {@link Subject.Factory} if possible.
   */
  public final <CustomSubjectBuilderT extends CustomSubjectBuilder> CustomSubjectBuilderT about(
      CustomSubjectBuilder.Factory<CustomSubjectBuilderT> factory) {
    return factory.createSubjectBuilder(metadata());
  }

  /**
   * Reports a failure.
   *
   * <p>To set a message, first call {@link #withMessage} (or, more commonly, use the shortcut
   * {@link Truth#assertWithMessage}).
   */
  public final void fail() {
    metadata().fail(ImmutableList.of());
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
