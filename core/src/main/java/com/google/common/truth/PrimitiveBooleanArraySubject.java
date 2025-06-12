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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.primitives.Booleans;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@code boolean[]}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public final class PrimitiveBooleanArraySubject extends Subject {
  private final boolean @Nullable [] actual;

  private PrimitiveBooleanArraySubject(FailureMetadata metadata, boolean @Nullable [] actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public IterableSubject asList() {
    return checkNoNeedToDisplayBothValues("asList()").that(Booleans.asList(checkNotNull(actual)));
  }

  /** Checks that the actual array is empty (i.e., that {@code array.length == 0}). */
  public void isEmpty() {
    arrayIsEmptyImpl();
  }

  /** Checks that the actual array is not empty (i.e., that {@code array.length > 0}). */
  public void isNotEmpty() {
    arrayIsNotEmptyImpl();
  }

  /**
   * Checks that the actual array has the given length.
   *
   * @throws IllegalArgumentException if {@code length < 0}
   */
  public void hasLength(int length) {
    arrayHasLengthImpl(length);
  }

  static Factory<PrimitiveBooleanArraySubject, boolean[]> booleanArrays() {
    return PrimitiveBooleanArraySubject::new;
  }
}
