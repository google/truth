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
import org.jspecify.annotations.Nullable;

/**
 * The obsolete entry point for assertions about Java 8 types.
 *
 * @deprecated Instead of this class's methods, use the identical methods declared in the main
 *     {@link Truth} class. In most cases, you can <a
 *     href="https://github.com/google/truth/releases/tag/v1.4.0">migrate</a> your whole project
 *     mechanically: {@code git grep -l Truth8 | xargs perl -pi -e 's/\bTruth8\b/Truth/g;'}
 *     Migration is important <i>if</i> you static import {@code assertThat}: If you do not migrate,
 *     such static imports will become ambiguous in Truth 1.4.2, breaking your build.
 */
@Deprecated
// Replacing "Truth.assertThat" with "assertThat" would produce an infinite loop.
@SuppressWarnings({
  "StaticImportPreferred",
  "StaticImportPreferredForTruth",
})
public final class Truth8 {
  @SuppressWarnings({
    "AssertAboutOptionals", // suggests infinite recursion
    "NullableOptional", // Truth always accepts nulls, no matter the type
  })
  public static OptionalSubject assertThat(@Nullable Optional<?> actual) {
    return Truth.assertThat(actual);
  }

  public static OptionalIntSubject assertThat(@Nullable OptionalInt actual) {
    return Truth.assertThat(actual);
  }

  public static OptionalLongSubject assertThat(@Nullable OptionalLong actual) {
    return Truth.assertThat(actual);
  }

  public static OptionalDoubleSubject assertThat(@Nullable OptionalDouble actual) {
    return Truth.assertThat(actual);
  }

  public static StreamSubject assertThat(@Nullable Stream<?> actual) {
    return Truth.assertThat(actual);
  }

  public static IntStreamSubject assertThat(@Nullable IntStream actual) {
    return Truth.assertThat(actual);
  }

  public static LongStreamSubject assertThat(@Nullable LongStream actual) {
    return Truth.assertThat(actual);
  }

  @GwtIncompatible
  @J2ObjCIncompatible
  @J2ktIncompatible
  public static PathSubject assertThat(@Nullable Path actual) {
    return Truth.assertThat(actual);
  }

  private Truth8() {}
}
