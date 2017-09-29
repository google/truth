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
 * <p>For more information about the methods in this class, see <a
 * href="https://google.github.io/truth/faq#full-chain">this FAQ entry</a>.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>You won't extend this type. When you write a custom subject, see <a
 * href="https://google.github.io/truth/extension">our doc on extensions</a>.
 */
public final class SimpleSubjectBuilder<SubjectT extends Subject<SubjectT, ActualT>, ActualT> {
  private final FailureMetadata metadata;
  private final Subject.Factory<SubjectT, ActualT> subjectFactory;

  SimpleSubjectBuilder(
      FailureMetadata metadata, Subject.Factory<SubjectT, ActualT> subjectFactory) {
    this.metadata = checkNotNull(metadata);
    this.subjectFactory = checkNotNull(subjectFactory);
  }

  public SubjectT that(@Nullable ActualT actual) {
    return subjectFactory.createSubject(metadata, actual);
  }
}
