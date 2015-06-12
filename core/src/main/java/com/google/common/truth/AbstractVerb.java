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

import static com.google.common.truth.StringUtil.format;

import com.google.common.annotations.GwtIncompatible;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

public abstract class AbstractVerb<T extends AbstractVerb<T>> {
  private final FailureStrategy failureStrategy;

  public AbstractVerb(FailureStrategy failureStrategy) {
    this.failureStrategy = failureStrategy;
  }

  protected FailureStrategy getFailureStrategy() {
    return (getFailureMessage() != null)
        ? new MessagePrependingFailureStrategy(failureStrategy, getFailureMessage())
        : failureStrategy;
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
   * Overrides the failure message of the subsequent subject's propositions.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param factory a SubjectFactory<S, T> implementation
   * @returns A custom verb for the type returned by the SubjectFactory
   */
  @CheckReturnValue
  public abstract T withFailureMessage(String failureMessage);

  protected abstract String getFailureMessage();

  /**
   * The recommended method of extension of Truth to new types, which is
   * documented in {@link com.google.common.truth.delegation.DelegationTest }.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param factory
   *          a SubjectFactory<S, T> implementation
   * @returns A custom verb for the type returned by the SubjectFactory
   */
  public <S extends Subject<S, T>, T, SF extends SubjectFactory<S, T>> DelegatedVerb<S, T>
      about(SF factory) {
    return new DelegatedVerb<S, T>(getFailureStrategy(), factory);
  }

  /**
   * A special Verb implementation which wraps a SubjectFactory
   */
  public static class DelegatedVerb<S extends Subject<S, T>, T> {
    private final SubjectFactory<S, T> factory;
    private final FailureStrategy failureStrategy;

    public DelegatedVerb(FailureStrategy fs, SubjectFactory<S, T> factory) {
      this.factory = factory;
      this.failureStrategy = fs;
    }

    @CheckReturnValue
    public S that(T target) {
      return factory.getSubject(failureStrategy, target);
    }
  }

  @GwtIncompatible("com.google.common.truth.IteratingVerb")
  @J2ObjCIncompatible("Code generation and loading.")
  public <T> IteratingVerb<T> in(Iterable<T> data) {
    return new IteratingVerb<T>(data, getFailureStrategy());
  }

  protected static class MessagePrependingFailureStrategy extends FailureStrategy {
    private final FailureStrategy delegate;
    private final String failureMessagePrepend;

    protected MessagePrependingFailureStrategy(FailureStrategy delegate, String failureMessage) {
      this.delegate = delegate;
      this.failureMessagePrepend = failureMessage;
    }

    @Override
    public void fail(String message) {
      delegate.fail(failureMessagePrepend + ": " + message);
    }

    @Override
    public void fail(String message, Throwable cause) {
      delegate.fail(failureMessagePrepend + ": " + message, cause);
    }

    @Override
    public void failComparing(String message, CharSequence expected, CharSequence actual) {
      delegate.fail(
          failureMessagePrepend + ": " + StringUtil.messageFor(message, expected, actual));
    }
  }
}
