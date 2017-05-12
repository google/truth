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

import javax.annotation.Nullable;

/**
 * Propositions for {@link Throwable} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class ThrowableSubject extends Subject<ThrowableSubject, Throwable> {
  static ThrowableSubject create(FailureStrategy failureStrategy, @Nullable Throwable throwable) {
    return new ThrowableSubject(causeInsertingStrategy(failureStrategy, throwable), throwable);
  }

  private ThrowableSubject(FailureStrategy failureStrategy, @Nullable Throwable throwable) {
    super(failureStrategy, throwable);
  }

  /*
   * TODO(cpovirk): consider a special case for isEqualTo and isSameAs that adds |expected| as a
   * suppressed exception
   */

  /**
   * Fails if the subject does not have the given message.
   *
   * @deprecated Use {@code hasMessageThat().isEqualTo(expected)} instead. You may also consider
   *     using inexact matching of the message (e.g. {@code hasMessageThat().contains(substring)})
   *     for less brittle tests.
   */
  @Deprecated
  public void hasMessage(@Nullable String expected) {
    hasMessageThat().isEqualTo(expected);
  }

  /** Returns a {@code StringSubject} to make assertions about the throwable's message. */
  public StringSubject hasMessageThat() {
    return new StringSubject(badMessageStrategy(failureStrategy), actual().getMessage());
  }

  /**
   * Returns a new {@code ThrowableSubject} that supports assertions on this throwable's direct
   * cause. This method can be invoked repeatedly (e.g. {@code
   * assertThat(e).hasCauseThat().hasCauseThat()....} to assert on a particular indirect cause.
   */
  public ThrowableSubject hasCauseThat() {
    // provides a more helpful error message if hasCauseThat() methods are chained too deep
    // e.g. assertThat(new Exception()).hCT().hCT()....
    // TODO(diamondm) in keeping with other subjects' behavior this should still NPE if the subject
    // *itself* is null, since there's no context to lose. See also b/37645583
    if (actual() == null) {
      failWithRawMessage("Causal chain is not deep enough - add a .isNotNull() check?");
      return ignoreCheck()
          .that(
              new Throwable() {
                @Override
                public Throwable fillInStackTrace() {
                  return this;
                }
              });
    }
    return new ThrowableSubject(badCauseStrategy(failureStrategy), actual().getCause());
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
        // TODO(cpovirk): add defaultCause as a suppressed exception? If fail() throws...
      }

      @Override
      public void failComparing(String message, CharSequence expected, CharSequence actual) {
        delegate.failComparing(message, expected, actual, defaultCause);
      }

      @Override
      public void failComparing(
          String message, CharSequence expected, CharSequence actual, Throwable cause) {
        delegate.failComparing(message, expected, actual, cause);
        // TODO(cpovirk): add defaultCause as a suppressed exception? If failComparing() throws...
      }
    };
  }

  private FailureStrategy badMessageStrategy(final FailureStrategy delegate) {
    return new FailureStrategy() {
      private String prependMessage(String message) {
        String name = actual().getClass().getName();
        if (internalCustomName() != null) {
          name = internalCustomName() + "(" + name + ")";
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

      @Override
      public void failComparing(
          String message, CharSequence expected, CharSequence actual, Throwable cause) {
        delegate.failComparing(prependMessage(message), expected, actual, cause);
      }
    };
  }

  private FailureStrategy badCauseStrategy(final FailureStrategy delegate) {
    return new FailureStrategy() {
      private String prependMessage(String message) {
        String name = actual().getClass().getName();
        if (internalCustomName() != null) {
          name = internalCustomName() + "(" + name + ")";
        }
        return "Unexpected cause for " + name + ":" + (message.isEmpty() ? "" : " " + message);
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

      @Override
      public void failComparing(
          String message, CharSequence expected, CharSequence actual, Throwable cause) {
        delegate.failComparing(prependMessage(message), expected, actual, cause);
      }
    };
  }
}
