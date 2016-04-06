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
import static com.google.common.truth.StringUtil.format;

import javax.annotation.Nullable;

/*>>>import org.checkerframework.checker.nullness.compatqual.NullableType;*/
/**
 * A parent type for some infrastructure used in the Verb.
 */
// TODO(cgruber) Remove the FailureMessageHolder inheritance and restructure to simplify verbs.
public abstract class AbstractVerb<T extends AbstractVerb<T>> extends FailureContext {
  private final FailureStrategy failureStrategy;

  public AbstractVerb(FailureStrategy failureStrategy) {
    this(failureStrategy, null);
  }

  public AbstractVerb(
      FailureStrategy failureStrategy, @Nullable String format, Object /*@NullableType*/... args) {
    super(format, args);
    this.failureStrategy = checkNotNull(failureStrategy);
  }

  protected FailureStrategy getFailureStrategy() {
    // TODO(cgruber): Extract this logic solely into the withFailureMessage() methods.
    return hasFailureMessage()
        ? new MessagePrependingFailureStrategy(failureStrategy, this)
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
  public void fail(@Nullable String format, Object /*@NullableType*/... args) {
    getFailureStrategy().fail(format(format, args));
  }

  /**
   * Overrides the failure message of the subsequent subject's propositions.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param failureMessage a descriptive message.
   * @return A custom verb which will show the descriptive message along with the normal failure
   *     text.
   */
  // TODO(cgruber) try to delete this (binary incompatible, but see if there's a way.
  public abstract T withFailureMessage(@Nullable String failureMessage);

  /**
   * Overrides the failure message of the subsequent subject's propositions.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param format a descriptive message with formatting template content.
   * @param args object parameters to be substituted into the message template.
   * @return A custom verb which will show the descriptive message along with the normal failure
   *     text.
   */
  public abstract T withFailureMessage(@Nullable String format, Object /*@NullableType*/... args);

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

  /**
   * A special Verb implementation which wraps a SubjectFactory
   */
  public static final class DelegatedVerb<S extends Subject<S, T>, T> {
    private final SubjectFactory<S, T> factory;
    private final FailureStrategy failureStrategy;

    public DelegatedVerb(FailureStrategy failureStrategy, SubjectFactory<S, T> factory) {
      this.factory = checkNotNull(factory);
      this.failureStrategy = checkNotNull(failureStrategy);
    }

    public S that(T target) {
      return factory.getSubject(failureStrategy, target);
    }

    public DelegatedVerb<S, T> withFailureMessage(@Nullable String failureMessage) {
      return failureMessage == null
          ? withFailureMessage(null, new Object[0]) // force the right overload
          : withFailureMessage("%s", failureMessage);
    }

    public DelegatedVerb<S, T> withFailureMessage(
        @Nullable String format, Object /* @NullableType */... args) {
      FailureContext holder = new FailureContext(format, args);
      return new DelegatedVerb<S, T>(
          new MessagePrependingFailureStrategy(failureStrategy, holder), factory);
    }
  }

  protected static class MessagePrependingFailureStrategy extends FailureStrategy {
    private final FailureStrategy delegate;
    private final FailureContext messageHolder;

    public MessagePrependingFailureStrategy(
        FailureStrategy delegate, FailureContext messageHolder) {
      this.delegate = checkNotNull(delegate);
      this.messageHolder = checkNotNull(messageHolder);
    }

    @Override
    public void fail(String message) {
      delegate.fail(prependFailureMessageIfAny(message));
    }

    @Override
    public void fail(String message, Throwable cause) {
      delegate.fail(prependFailureMessageIfAny(message), cause);
    }

    @Override
    public void failComparing(String message, CharSequence expected, CharSequence actual) {
      delegate.failComparing(prependFailureMessageIfAny(message), expected, actual);
    }

    private String prependFailureMessageIfAny(String message) {
      return messageHolder.getFailureMessage() == null
          ? message
          : messageHolder.getFailureMessage() + ": " + message;
    }
  }
}
