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

public abstract class AbstractVerb<T extends AbstractVerb<T>> {
  private final FailureStrategy failureStrategy;

  public AbstractVerb(FailureStrategy failureStrategy) {
    this.failureStrategy = failureStrategy;
  }

  protected FailureStrategy getFailureStrategy() {
    return (getFailureMessage() != null)
        ? new MessageOverridingFailureStrategy(failureStrategy, getFailureMessage())
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
  public void fail(String format, Object... args) {
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

  @GwtIncompatible("org.truth0.IteratingVerb")
  public <T> IteratingVerb<T> in(Iterable<T> data) {
    return new IteratingVerb<T>(data, getFailureStrategy());
  }

  protected static class MessageOverridingFailureStrategy extends FailureStrategy {
    private final FailureStrategy delegate;
    private final String failureMessageOverride;

    protected MessageOverridingFailureStrategy(FailureStrategy delegate, String failureMessage) {
      this.delegate = delegate;
      this.failureMessageOverride = failureMessage;
    }

    @Override
    public void fail(String ignored) {
      delegate.fail(failureMessageOverride);
    }

    @Override
    public void fail(String message, Throwable cause) {
      delegate.fail(failureMessageOverride, cause);
    }

    @Override
    public void failComparing(String message, CharSequence ignore, CharSequence ignore2) {
      delegate.fail(failureMessageOverride);
    }
  }
}
