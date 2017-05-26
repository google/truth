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
 * FailureStrategy} implementations. When you really do need to create your own strategy prefer to
 * extend {@link AbstractFailureStrategy} rather than this class directly.
 *
 * <p>Alternatives to creating a custom {@code FailureStrategy} implementation:
 *
 * <ul>
 *   <li>For unit tests of a custom subject and assert on the failure behavior use {@link
 *       ExpectFailure}
 *   <li>To create subjects of other types within your own subject (e.g. for chained assertions) use
 *       {@link Subject#check}
 *   <li>To return a no-op subject after a previous assertion has failed (e.g. for chained
 *       assertions) use {@link Subject#ignoreCheck}
 * </ul>
 */
public abstract class FailureStrategy {
  /**
   * Report an assertion failure with a text message. This method should generally delegate to
   * {@link #fail(String, Throwable)}.
   */
  public abstract void fail(String message);

  /**
   * Report an assertion failure with a text message and a throwable that indicates the cause of the
   * failure. This will be reported as the cause of the exception raised by this method (if one is
   * thrown) or otherwise recorded by the strategy as the underlying cause of the failure.
   */
  public abstract void fail(String message, Throwable cause);

  /**
   * Convenience method to report string-comparison failures with more detail (e.g. character
   * differences). This method should generally delegate to {@link #failComparing(String,
   * CharSequence, CharSequence, Throwable)}.
   */
  public abstract void failComparing(String message, CharSequence expected, CharSequence actual);

  /**
   * Convenience method to report string-comparison failures with more detail (e.g. character
   * differences), along with a throwable that indicates the cause of the failure. This will be
   * reported as the cause of the exception raised by this method (if one is thrown) or otherwise
   * recorded by the strategy as the underlying cause of the failure.
   */
  public abstract void failComparing(
      String message, CharSequence expected, CharSequence actual, Throwable cause);

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
