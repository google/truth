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
import com.google.j2objc.annotations.J2ObjCIncompatible;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The obsolete entry point for assertions about Java 8 types.
 *
 * @deprecated Instead of this class's methods, use the identical methods declared in the main
 *     {@link Truth} class.
 */
@Deprecated
// The methods here are no more dangerous that wherever the user got the (e.g.) Stream.
@SuppressWarnings("Java7ApiChecker")
public final class Truth8 {
  @SuppressWarnings("AssertAboutOptionals") // suggests infinite recursion
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

  @GwtIncompatible
  @J2ObjCIncompatible
  @J2ktIncompatible
  public static PathSubject assertThat(@Nullable Path target) {
    return assertAbout(PathSubject.paths()).that(target);
  }

  private Truth8() {}
}
