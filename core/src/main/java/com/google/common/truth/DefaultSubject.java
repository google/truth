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

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * @deprecated Use plain {@link Subject} instead. At the moment, {@code Subject} has type
 *     parameters, so you may wish to use {@code Subject<?, ?>}. However, those type parameters will
 *     soon go away, so you may wish to start using raw {@code Subject} now to prepare.
 */
@Deprecated
public class DefaultSubject extends Subject {
  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check}{@code .that(actual)}.
   */
  protected DefaultSubject(FailureMetadata metadata, @NullableDecl Object o) {
    super(metadata, o);
  }
}
