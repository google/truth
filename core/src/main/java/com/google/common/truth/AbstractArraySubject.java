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

/**
 * A common supertype for Array subjects, abstracting some common display and error infrastructure.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public abstract class AbstractArraySubject<S extends AbstractArraySubject<S, T>, T>
    extends Subject<AbstractArraySubject<S, T>, T> {

  AbstractArraySubject(FailureStrategy failureStrategy, T subject) {
    super(failureStrategy, subject);
  }

  public void isEmpty() {
    if (!listRepresentation().isEmpty()) {
      fail("is empty");
    }
  }

  public void isNotEmpty() {
    if (listRepresentation().isEmpty()) {
      fail("is not empty");
    }
  }

  public void hasLength(int length) {
    checkArgument(length >= 0, "length (%s) must be >= 0");
    if (listRepresentation().size() != length) {
      fail("has length", length);
    }
  }

  @Override public S named(String name) {
    return (S) super.named(name);
  }

  abstract String underlyingType();

  abstract List<?> listRepresentation();

  @Override protected String getDisplaySubject() {
    return (internalCustomName() == null)
        ? "<(" + underlyingType() + "[]) " + listRepresentation() + ">"
        : "\"" + this.internalCustomName() + "\"";
  }

  void failWithBadType(Object expected) {
    String expectedType = (expected.getClass().isArray())
        ? expected.getClass().getComponentType().getName() + "[]"
        : expected.getClass().getName();
    failWithRawMessage("Incompatible types compared. expected: %s, actual: %s[]",
        Platform.compressType(expectedType), underlyingType());
  }
}
