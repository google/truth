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
import java.nio.file.Path;

/** Assertions for {@link Path} instances. */
@GwtIncompatible
public final class PathSubject extends Subject<PathSubject, Path> {
  private PathSubject(FailureStrategy failureStrategy, Path actual) {
    super(failureStrategy, actual);
  }

  private static final SubjectFactory<PathSubject, Path> FACTORY =
      new SubjectFactory<PathSubject, Path>() {
        @Override
        public PathSubject getSubject(FailureStrategy failureStrategy, Path path) {
          return new PathSubject(failureStrategy, path);
        }
      };

  public static SubjectFactory<PathSubject, Path> paths() {
    return FACTORY;
  }
}
