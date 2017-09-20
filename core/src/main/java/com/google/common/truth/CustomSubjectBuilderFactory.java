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

/**
 * In a fluent assertion chain, the argument to the "custom" overload of {@link
 * StandardSubjectBuilder#about(CustomSubjectBuilderFactory) about}, the method that specifies what
 * kind of {@link Subject} to create.
 *
 * <p>TODO(cpovirk): Link to a doc about the full assertion chain.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>TODO(cpovirk): Link to a doc about custom subjects.
 *
 * @deprecated When you switch your {@link Subject} implementations from accepting a {@link
 *     FailureStrategy} to accepting a {@link FailureMetadata}, you'll switch their {@link
 *     CustomSubjectBuilder} to start using {@link FailureMetadata}, too, and so you'll switch to
 *     creating it with {@link CustomSubjectBuilder.Factory} instead of {@link
 *     CustomSubjectBuilderFactory}.
 */
@Deprecated
public interface CustomSubjectBuilderFactory<CustomSubjectBuilderT extends CustomSubjectBuilder> {
  /** Creates a new {@link CustomSubjectBuilder} of the appropriate type. */
  CustomSubjectBuilderT createSubjectBuilder(FailureStrategy failureStrategy);
}
