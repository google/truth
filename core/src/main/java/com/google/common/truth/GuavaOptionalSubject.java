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
import javax.annotation.Nullable;

/**
 * Propositions for Guava {@link Optional} subjects.
 *
 * @author Christian Gruber
 */
public final class GuavaOptionalSubject extends Subject<GuavaOptionalSubject, Optional<?>> {
  GuavaOptionalSubject(FailureStrategy failureStrategy, @Nullable Optional<?> subject) {
    super(failureStrategy, subject);
  }

  /** Fails if the {@link Optional}{@code <T>} is absent or the subject is null. */
  public void isPresent() {
    if (actual() == null || !actual().isPresent()) {
      failWithoutActual("is present");
    }
  }

  /** Fails if the {@link Optional}{@code <T>} is present or the subject is null.. */
  public void isAbsent() {
    if (actual() == null || actual().isPresent()) {
      fail("is absent");
    }
  }

  /**
   * Fails if the {@link Optional}{@code <T>} does not have the given value or the subject is null.
   *
   * <p>To make more complex assertions on the optional's value split your assertion in two:
   *
   * <pre>{@code
   * assertThat(myOptional).isPresent();
   * assertThat(myOptional.get()).contains("foo");
   * }</pre>
   */
  public void hasValue(Object expected) {
    if (expected == null) {
      throw new NullPointerException("Optional cannot have a null value.");
    }
    if (actual() == null || !actual().isPresent()) {
      fail("has value", expected);
    } else {
      Object actual = actual().get();
      if (!actual.equals(expected)) {
        if (actual.toString().equals(expected.toString())) {
          failWithRawMessage(
              "Not true that %s (%s) has value <%s> (%s)",
              actualAsString(), actual.getClass(), expected, expected.getClass());
        } else {
          fail("has value", expected);
        }
      }
    }
  }
}
