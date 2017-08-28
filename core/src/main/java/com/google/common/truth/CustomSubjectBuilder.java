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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * In a fluent assertion chain, exposes one or more "custom" {@code that} methods, which accept a
 * value under test and return a {@link Subject}.
 *
 * <p>(Note that the "custom" {@code that} methods are not defined on {@code CustomSubjectBuilder}
 * itself, only on its subtypes, which are the types users actually interact with.)
 *
 * <p>TODO(cpovirk): Link to a doc about the full assertion chain.
 *
 * <h2>For people extending Truth</h2>
 *
 * <p>TODO(cpovirk): Link to a doc about custom subjects.
 */
public abstract class CustomSubjectBuilder {
  private final FailureStrategy failureStrategy;

  protected CustomSubjectBuilder(FailureStrategy failureStrategy) {
    this.failureStrategy = checkNotNull(failureStrategy);
  }

  protected final FailureStrategy failureStrategy() {
    return failureStrategy;
  }

  // TODO(user,cgruber): Better enforce that subclasses implement a that() method.
}
