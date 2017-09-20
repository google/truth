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
 * <h3>For people extending Truth</h3>
 *
 * <p>TODO(cpovirk): Link to a doc about custom subjects.
 */
public abstract class CustomSubjectBuilder {
  /**
   * In a fluent assertion chain, the argument to the "custom" overload of {@link
   * StandardSubjectBuilder#about(CustomSubjectBuilder.Factory) about}, the method that specifies
   * what kind of {@link Subject} to create.
   *
   * <p>TODO(cpovirk): Link to a doc about the full assertion chain.
   *
   * <h3>For people extending Truth</h3>
   *
   * <p>TODO(cpovirk): Link to a doc about custom subjects.
   */
  public interface Factory<CustomSubjectBuilderT extends CustomSubjectBuilder> {
    /** Creates a new {@link CustomSubjectBuilder} of the appropriate type. */
    CustomSubjectBuilderT createSubjectBuilder(FailureMetadata metadata);
  }

  private final FailureMetadata metadata;

  /**
   * @deprecated When you switch from {@link CustomSubjectBuilderFactory} to {@link
   *     CustomSubjectBuilder.Factory}, you'll switch to using the other constructor, which accepts
   *     a {@link FailureMetadata} instead of a {@link FailureStrategy}.
   */
  @Deprecated
  protected CustomSubjectBuilder(FailureStrategy failureStrategy) {
    this.metadata = FailureMetadata.forFailureStrategy(failureStrategy);
  }

  /**
   * @deprecated When you switch your {@link Subject} implementations from accepting a {@link
   *     FailureStrategy} to accepting a {@link FailureMetadata}, you'll switch from calling this
   *     method to calling {@link #metadata}.
   */
  @Deprecated
  protected final FailureStrategy failureStrategy() {
    return metadata.legacyStrategy();
  }

  /** Constructor for use by subclasses. */
  protected CustomSubjectBuilder(FailureMetadata metadata) {
    this.metadata = checkNotNull(metadata);
  }

  /**
   * Returns the {@link FailureMetadata} instance that {@code that} methods should pass to {@link
   * Subject} constructors.
   */
  protected final FailureMetadata metadata() {
    return metadata;
  }

  // TODO(user,cgruber): Better enforce that subclasses implement a that() method.
}
