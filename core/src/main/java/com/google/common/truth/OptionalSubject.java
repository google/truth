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

import com.google.common.base.Optional;

/**
 * Propositions for {@link Optional}{@code <T>} subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class OptionalSubject<T> extends Subject<OptionalSubject<T>, Optional<T>> {

  OptionalSubject(FailureStrategy failureStrategy, Optional<T> subject) {
    super(failureStrategy, subject);
    check().that((Object) subject).named("Optional<T>").isNotNull();
  }

  /**
   * Fails if the {@link Optional}{@code <T>} is absent.
   */
  public void isPresent() {
    if (!getSubject().isPresent()) {
      failWithoutSubject("is present");
    }
  }

  /**
   * Fails if the {@link Optional}{@code <T>} is present.
   */
  public void isAbsent() {
    if (getSubject().isPresent()) {
      failWithoutSubject("is absent");
    }
  }

  /**
   * Fails if the {@link Optional}{@code <T>} does not have the given value.
   */
  public void hasValue(Object expected) {
    if (expected == null) {
      throw new NullPointerException("Optional cannot have a null value.");
    }
    if (!getSubject().isPresent()) {
      fail("has value", expected);
    } else {
      Object actual = getSubject().get();
      if (!getSubject().get().equals(expected)) {
        if (getSubject().get() instanceof String) {
          this.failureStrategy.failComparing("Optional<String> has an incorrect value.",
              (String) expected, (String) actual);
        } else {
          fail("has value", expected);
        }
      }
    }
  }
}
