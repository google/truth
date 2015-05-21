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

import java.util.List;

import javax.annotation.Nullable;

/**
 * A common supertype for Array subjects, abstracting some common display and error infrastructure.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public abstract class AbstractArraySubject<S extends AbstractArraySubject<S, T>, T>
    extends Subject<S, T> {
  AbstractArraySubject(FailureStrategy failureStrategy, @Nullable T subject) {
    super(failureStrategy, subject);
  }

  /**
   * Fails if the array is not empty (i.e. {@code array.length != 0}).
   */
  public void isEmpty() {
    if (!listRepresentation().isEmpty()) {
      fail("is empty");
    }
  }

  /**
   * Fails if the array is empty (i.e. {@code array.length == 0}).
   */
  public void isNotEmpty() {
    if (listRepresentation().isEmpty()) {
      fail("is not empty");
    }
  }

  /**
   * Fails if the array does not have the given length.
   *
   * @throws {@link IllegalArgumentException} if {@code length < 0}
   */
  public void hasLength(int length) {
    checkArgument(length >= 0, "length (%s) must be >= 0");
    if (listRepresentation().size() != length) {
      fail("has length", length);
    }
  }

  @Override
  public S named(String name) {
    return (S) super.named(name);
  }

  abstract String underlyingType();

  abstract List<?> listRepresentation();

  @Override
  protected String getDisplaySubject() {
    return (internalCustomName() == null)
        ? "<(" + underlyingType() + "[]) " + listRepresentation() + ">"
        : this.internalCustomName();
  }

  void failWithBadType(Object expected) {
    String expectedType = (expected.getClass().isArray())
        ? expected.getClass().getComponentType().getName() + "[]"
        : expected.getClass().getName();
    failWithRawMessage(
        "Incompatible types compared. expected: %s, actual: %s[]",
        Platform.compressType(expectedType),
        underlyingType());
  }
}
