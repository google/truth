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
  // TODO(kak): Make this package-private?
  public ThrowableSubject(FailureStrategy failureStrategy, @Nullable Throwable throwable) {
    super(causeInsertingStrategy(failureStrategy, throwable), throwable);
  }

  // TODO(kak): Should this be @Nullable or should we have .doesNotHaveMessage()?
  /** Fails if the subject does not have the given message. */
  // TODO(diamondm): deprecate this in favor of {@code hasMessageThat().isEqualTo(expected)}.
  public void hasMessage(@Nullable String expected) {
    String actual = actual().getMessage();
    if (!Objects.equal(expected, actual)) {
      if (expected != null && actual != null) {
        failureStrategy.failComparing(
            actualAsString() + " does not have message <" + expected + ">", expected, actual);
      } else {
        fail("has message", expected);
      }
    }
  }

  public StringSubject hasMessageThat() {
    return new StringSubject(badMessageStrategy(failureStrategy, this), actual().getMessage());
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

  private static FailureStrategy badMessageStrategy(
      final FailureStrategy delegate, final ThrowableSubject subject) {
    return new FailureStrategy() {
      private String prependMessage(String message) {
        String name = subject.actual().getClass().getName();
        if (subject.internalCustomName() != null) {
          name = subject.internalCustomName() + "(" + name + ")";
        }
        return "Unexpected message for " + name + ":" + (message.isEmpty() ? "" : " " + message);
      }

      @Override
      public void fail(String message) {
        delegate.fail(prependMessage(message));
      }

      @Override
      public void fail(String message, Throwable cause) {
        delegate.fail(prependMessage(message), cause);
      }

      @Override
      public void failComparing(String message, CharSequence expected, CharSequence actual) {
        delegate.failComparing(prependMessage(message), expected, actual);
      }
    };
  }
}
