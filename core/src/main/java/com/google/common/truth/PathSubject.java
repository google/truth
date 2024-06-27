/*
 * Copyright (c) 2017 Google, Inc.
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

/**
 * Assertions for {@link Path} instances.
 *
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@GwtIncompatible
@J2ObjCIncompatible
@J2ktIncompatible
public final class PathSubject extends Subject {
  PathSubject(FailureMetadata failureMetadata, Path actual) {
    super(failureMetadata, actual);
  }

  /**
   * Obsolete factory instance. This factory was previously necessary for assertions like {@code
   * assertWithMessage(...).about(intStreams()).that(stream)....}. Now, you can perform assertions
   * like that without the {@code about(...)} call.
   *
   * @deprecated Instead of {@code about(paths()).that(...)}, use just {@code that(...)}. Similarly,
   *     instead of {@code assertAbout(paths()).that(...)}, use just {@code assertThat(...)}.
   */
  @Deprecated
  @SuppressWarnings("InlineMeSuggester") // We want users to remove the surrounding call entirely.
  public static Factory<PathSubject, Path> paths() {
    return PathSubject::new;
  }
}
