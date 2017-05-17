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

import java.util.Arrays;

/**
 * A {@code FailureStrategy} defines how assertion failures are handled. In {@link Truth}, failures
 * {@linkplain Truth#THROW_ASSERTION_ERROR throw AssertionErrors}; other assertion entry-points like
 * {@link Expect} and {@link TruthJUnit#assume} have different failure behavior.
 *
 * <p>It should generally be unnecessary for Truth SPI developers to define their own {@code
 * FailureStrategy} implementations, they should instead use {@link Subject#check} to invoke other
 * subjects using the current strategy. When you really do need to create your own strategy prefer
 * to extend {@link AbstractFailureStrategy} rather than this class directly.
 *
 * <p>Implementation Note: the concrete methods in this class will be made abstract in the near
 * future (see b/37472530); please do not depend on these existing implementations in new code,
 * prefer {@code AbstractFailureStrategy}.
 */
public abstract class FailureStrategy {
  /**
   * Report an assertion failure with a text message. This method should generally delegate to
   * {@link #fail(String, Throwable)}.
   */
  public void fail(String message) {
    fail(message, null);
  }

  /**
   * Report an assertion failure with a text message and a throwable that indicates the cause of the
   * failure. This will be reported as the cause of the exception raised by this method (if one is
   * thrown) or otherwise recorded by the strategy as the underlying cause of the failure.
   */
  public void fail(String message, Throwable cause) {
    AssertionError up = new AssertionError(message);
    if (cause == null) {
      cause = new AssertionError(message);
    }
    try {
      up.initCause(cause);
    } catch (IllegalStateException alreadyInitializedBecauseOfHarmonyBug) {
      // https://code.google.com/p/android/issues/detail?id=29378
      // No message, but it's the best we can do without awful hacks.
      throw new AssertionError(cause);
    }
    throw stripTruthStackFrames(up);
  }

  /**
   * Convenience method to report string-comparison failures with more detail (e.g. character
   * differences). This method should generally delegate to {@link #failComparing(String,
   * CharSequence, CharSequence, Throwable)}.
   */
  public void failComparing(String message, CharSequence expected, CharSequence actual) {
    failComparing(message, expected, actual, null);
  }

  /**
   * Convenience method to report string-comparison failures with more detail (e.g. character
   * differences), along with a throwable that indicates the cause of the failure. This will be
   * reported as the cause of the exception raised by this method (if one is thrown) or otherwise
   * recorded by the strategy as the underlying cause of the failure.
   */
  public void failComparing(
      String message, CharSequence expected, CharSequence actual, Throwable cause) {
    fail(StringUtil.messageFor(message, expected, actual), cause);
  }

  /**
   * Strips stack frames from the throwable that have a class starting with com.google.common.truth.
   */
  static <T extends Throwable> T stripTruthStackFrames(T throwable) {
    StackTraceElement[] stackTrace = throwable.getStackTrace();

    int i = 0;
    while (i < stackTrace.length
        && stackTrace[i].getClassName().startsWith("com.google.common.truth")) {
      i++;
    }
    throwable.setStackTrace(Arrays.copyOfRange(stackTrace, i, stackTrace.length));
    return throwable;
  }
}
