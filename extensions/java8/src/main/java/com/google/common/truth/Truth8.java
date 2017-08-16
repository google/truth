/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.annotations.GwtIncompatible;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * A set of static methods to begin a Truth assertion chain for types that require java8.
 *
 * <p>Note: Usage of different failure strategies such as <em>assume</em> and <em>expect</em> should
 * rely on {@link
 * com.google.common.truth.AbstractVerb#about(com.google.common.truth.SubjectFactory)} to begin a
 * chain with those alternative behaviors.
 */
public final class Truth8 {
  public static OptionalSubject assertThat(@Nullable Optional<?> target) {
    return assertAbout(OptionalSubject.optionals()).that(target);
  }

  public static OptionalIntSubject assertThat(@Nullable OptionalInt target) {
    return assertAbout(OptionalIntSubject.optionalInts()).that(target);
  }

  public static OptionalLongSubject assertThat(@Nullable OptionalLong target) {
    return assertAbout(OptionalLongSubject.optionalLongs()).that(target);
  }

  public static OptionalDoubleSubject assertThat(@Nullable OptionalDouble target) {
    return assertAbout(OptionalDoubleSubject.optionalDoubles()).that(target);
  }

  public static StreamSubject assertThat(@Nullable Stream<?> target) {
    return assertAbout(StreamSubject.streams()).that(target);
  }

  public static IntStreamSubject assertThat(@Nullable IntStream target) {
    return assertAbout(IntStreamSubject.intStreams()).that(target);
  }

  public static LongStreamSubject assertThat(@Nullable LongStream target) {
    return assertAbout(LongStreamSubject.longStreams()).that(target);
  }

  // TODO(b/64757353): Add support for DoubleStream?

  // Not actually a Java 8 feature, but for now this is the best option since core Truth still has
  // to support Java environments without java.nio.file such as Android and J2CL.
  @GwtIncompatible
  public static PathSubject assertThat(@Nullable Path target) {
    return assertAbout(PathSubject.paths()).that(target);
  }

  private Truth8() {}
}
