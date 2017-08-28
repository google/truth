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

import javax.annotation.Nullable;

/*>>>import org.checkerframework.checker.nullness.compatqual.NullableType;*/

/**
 * In a fluent assertion chain, exposes the most common {@code that} method, which accepts a value
 * under test and returns a {@link Subject}.
 *
 * <p>TODO(cpovirk): Link to a doc about the full assertion chain.
 *
 * <h2>For people extending Truth</h2>
 *
 * <p>TODO(cpovirk): Link to a doc about custom subjects.
 */
// TODO(cpovirk): remove DelegatedVerb superclass
public final class SimpleSubjectBuilder<SubjectT extends Subject<SubjectT, ActualT>, ActualT>
    extends AbstractVerb.DelegatedVerb<SubjectT, ActualT> {
  private final FailureStrategy failureStrategy;
  private final SubjectFactory<SubjectT, ActualT> subjectFactory;

  SimpleSubjectBuilder(
      FailureStrategy failureStrategy, SubjectFactory<SubjectT, ActualT> subjectFactory) {
    super(failureStrategy, subjectFactory);
    this.failureStrategy = checkNotNull(failureStrategy);
    this.subjectFactory = checkNotNull(subjectFactory);
  }

  @Override // temporarily
  public SubjectT that(@Nullable ActualT actual) {
    return subjectFactory.getSubject(failureStrategy, actual);
  }
}
