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

/**
 * In a fluent assertion chain, the argument to the common overload of {@link
 * StandardSubjectBuilder#about(SubjectFactory) about}, the method that specifies what kind of
 * {@link Subject} to create.
 *
 * <p>TODO(cpovirk): Link to a doc about the full assertion chain.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>TODO(cpovirk): Link to a doc about custom subjects.
 *
 * @deprecated When you switch your {@link Subject} implementations from accepting a {@link
 *     FailureStrategy} to accepting a {@link FailureMetadata}, you'll switch their factories to
 *     {@link Subject.Factory} instead of {@link SubjectFactory}.
 */
@Deprecated
public abstract class SubjectFactory<SubjectT extends Subject<SubjectT, ActualT>, ActualT> {
  // TODO(cpovirk): Rename to "createSubject" when changing to a nested type.
  /** Creates a new {@link Subject}. */
  public abstract SubjectT getSubject(FailureStrategy failureStrategy, ActualT actual);
}
