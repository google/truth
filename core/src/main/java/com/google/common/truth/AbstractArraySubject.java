/*
 * Copyright (c) 2014 Google, Inc.
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.truth.Fact.simpleFact;

import java.lang.reflect.Array;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * A common supertype for Array subjects, abstracting some common display and error infrastructure.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
abstract class AbstractArraySubject<S extends AbstractArraySubject<S, T>, T> extends Subject<S, T> {
  AbstractArraySubject(
      FailureMetadata metadata, @NullableDecl T actual, @NullableDecl String typeDescription) {
    super(metadata, actual, typeDescription);
  }

  /** Fails if the array is not empty (i.e. {@code array.length != 0}). */
  public final void isEmpty() {
    if (length() > 0) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Fails if the array is empty (i.e. {@code array.length == 0}). */
  public final void isNotEmpty() {
    if (length() == 0) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /**
   * Fails if the array does not have the given length.
   *
   * @throws IllegalArgumentException if {@code length < 0}
   */
  public final void hasLength(int length) {
    checkArgument(length >= 0, "length (%s) must be >= 0");
    check("length").that(length()).isEqualTo(length);
  }

  private int length() {
    return Array.getLength(actual());
  }
}
