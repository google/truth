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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.StringUtil.format;

import com.google.common.base.Preconditions;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

public abstract class AbstractVerb<T extends AbstractVerb<T>> {
  private final FailureStrategy failureStrategy;

  public AbstractVerb(FailureStrategy failureStrategy) {
    this.failureStrategy = checkNotNull(failureStrategy);
  }

  protected FailureStrategy getFailureStrategy() {
    return failureStrategy;
  }

  /**
   * Triggers the failure strategy with an empty failure message
   */
  public void fail() {
    getFailureStrategy().fail("");
  }

  /**
   * Triggers the failure strategy with the given failure message
   */
  public void fail(@Nullable String format, Object... args) {
    getFailureStrategy().fail(format(format, args));
  }

  /**
   * Overrides the failure message of the subsequent subject's propositions by prepending a
   * custom message.
   *
   * <p><b>Note:</b> The failure message template string only supports the {@code "%s"} specifier,
   * not the full range of {@link java.util.Formatter} specifiers.
   *
   *
   * <strong>SPI Notes for implementers</strong>
   *
   * <p>Note, all implementations should override this abstract method by the following
   * pattern: <pre>{@code
   *
   *   @Override
   *   public CustomVerb withFailureMessage(String format, Object... args) {
   *     return new CustomVerb(failureStrategyWithMessage(format, args));
   *   }
   * }</pre>
   *
   * <p>This permits the custom TestVerb to be returned as a strong type, so that any
   * {@code that(Foo foo)} methods are accessible in the returned type.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param failureMessage a descriptive message. {@code null} to remove any current message.
   * @return A custom verb which will show the descriptive message along with the normal failure
   *     text.
   */
  @CheckReturnValue
  public abstract T withFailureMessage(@Nullable String format, Object... args);

  /**
   * The recommended method of extension of Truth to new types, which is
   * documented in {@link com.google.common.truth.delegation.DelegationTest }.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param factory
   *          a SubjectFactory<S, T> implementation
   * @returns A custom verb for the type returned by the SubjectFactory
   */
  public <S extends Subject<S, T>, T, SF extends SubjectFactory<S, T>> DelegatedVerb<S, T> about(
      SF factory) {
    return new DelegatedVerb<S, T>(getFailureStrategy(), factory);
  }

  protected FailureStrategy failureStrategyWithMessage(String format, Object... args) {
    // TODO(cgruber) Should we consider loosening this?  Does it really matter?
    checkState(!(getFailureStrategy() instanceof DelegatingFailureStrategy),
        "Overriding failure message has already been set for this call-chain.");
    return new DelegatingFailureStrategy(getFailureStrategy(), format, args);
  }
  /**
   * A special Verb implementation which wraps a SubjectFactory
   */
  public static class DelegatedVerb<S extends Subject<S, T>, T> {
    private final SubjectFactory<S, T> factory;
    private final FailureStrategy failureStrategy;

    public DelegatedVerb(FailureStrategy failureStrategy, SubjectFactory<S, T> factory) {
      this.factory = checkNotNull(factory);
      this.failureStrategy = checkNotNull(failureStrategy);
    }

    @CheckReturnValue
    public S that(T target) {
      return factory.getSubject(failureStrategy, target);
    }
  }

  /**
   * A {@link FailureStrategy} which delegates to an original strategy, and overrides the message
   * when the various {@code fail()} methods are called, prepending the supplied formatted string
   * to the Subject-supplied error message.
   */
  private static class DelegatingFailureStrategy extends FailureStrategy {
    private final FailureStrategy delegate;
    private final String fmt;
    private final Object[] args;

    private DelegatingFailureStrategy(FailureStrategy delegate, String fmt, Object[] args) {
      this.delegate = delegate;
      // This is checked here, even though StringUtil.format would fail in a similar way, because
      // it's better to catch this immediately, and not in a deep stack trace.
      Preconditions.checkArgument(StringUtil.countOfPlaceholders(fmt) == args.length,
          "Too many parameters for " + args.length + " argument: \"" + fmt + "\"");
      this.fmt = fmt;
      this.args = args;
    }

    @Override
    public void fail(String message) {
      delegate.fail(StringUtil.format(fmt, args) + ": " + message);
    }

    @Override
    public void fail(String message, Throwable cause) {
      delegate.fail(StringUtil.format(fmt, args) + ": " + message, cause);
    }

    @Override
    public void failComparing(String message, CharSequence expected, CharSequence actual) {
      delegate.failComparing(
          StringUtil.format(fmt, args) + ": " + message, expected, actual);
    }
  }
}
