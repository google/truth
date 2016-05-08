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

import com.google.common.base.Objects;

import javax.annotation.Nullable;

/**
 * Propositions for {@link Throwable} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class ThrowableSubject extends Subject<ThrowableSubject, Throwable> {
  public ThrowableSubject(FailureStrategy failureStrategy, @Nullable Throwable throwable) {
    super(causeInsertingStrategy(failureStrategy, throwable), throwable);
  }

  // TODO(kak): Should this be @Nullable or should we have .doesNotHaveMessage()?
  /**
   * Fails if the subject does not have the given message.
   */
  public void hasMessage(@Nullable String expected) {
    String actual = getSubject().getMessage();
    if (!Objects.equal(expected, actual)) {
      if (expected != null && actual != null) {
        failureStrategy.failComparing(
            getDisplaySubject() + " does not have message <" + expected + ">", expected, actual);
      } else {
        fail("has message", expected);
      }
    }
  }

  private static FailureStrategy causeInsertingStrategy(
      final FailureStrategy delegate, final Throwable defaultCause) {
    return new FailureStrategy() {
      @Override
      public void fail(String message) {
        delegate.fail(message, defaultCause);
      }

      @Override
      public void fail(String message, Throwable cause) {
        delegate.fail(message, cause);
        // TODO(cpovirk): add defaultCause as a suppressed exception?
      }

      @Override
      public void failComparing(String message, CharSequence expected, CharSequence actual) {
        // Invoke failComparing directly on the delegate so that it can do custom comparisons if
        // possible. Notably, the default FailureStrategy throws a ComparisonFailure which some IDEs
        // have special support for.
        try {
          delegate.failComparing(message, expected, actual);
        } catch (AssertionError e) {
          e.initCause(defaultCause);
          throw e;
        }
      }
    };
  }
}
