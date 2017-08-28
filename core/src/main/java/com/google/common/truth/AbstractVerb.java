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
 *
 * @deprecated Instead of subclassing {@code AbstractVerb}, subclass {@link CustomSubjectBuilder}.
 *     {@code CustomSubjectBuilder} is the new way of defining custom {@code that()} methods, and it
 *     doesn't require you to write boilerplate to store and propagate the failure message.
 */
// TODO(cgruber) Remove the FailureMessageHolder inheritance and restructure to simplify verbs.
@Deprecated
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

  /*
   * TODO(cpovirk): Maybe provide a generic handleAssertionStart() method for Expect to override
   * instead of this?
   */
  protected FailureStrategy getFailureStrategy() {
    // TODO(cgruber): Extract this logic solely into the withMessage() methods.
    return hasFailureMessage()
        ? new MessagePrependingFailureStrategy(failureStrategy, this)
        : failureStrategy;
  }

  /** Triggers the failure strategy with an empty failure message */
  public void fail() {
    getFailureStrategy().fail("");
  }

  /** Triggers the failure strategy with the given failure message */
  public void fail(@Nullable String format, Object /*@NullableType*/... args) {
    getFailureStrategy().fail(format(format, args));
  }

  /**
   * Overrides the failure message of the subsequent subject's propositions.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param messageToPrepend a descriptive message.
   * @return A custom verb which will show the descriptive message along with the normal failure
   *     text.
   */
  // TODO(cgruber) try to delete this (binary incompatible, but see if there's a way.
  public abstract T withMessage(@Nullable String messageToPrepend);

  /**
   * Overrides the failure message of the subsequent subject's propositions.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param format a descriptive message with formatting template content.
   * @param args object parameters to be substituted into the message template.
   * @return A custom verb which will show the descriptive message along with the normal failure
   *     text.
   */
  public abstract T withMessage(@Nullable String format, Object /*@NullableType*/... args);

  /**
   * The recommended method of extension of Truth to new types, which is documented in {@link
   * com.google.common.truth.delegation.DelegationTest}.
   *
   * @see com.google.common.truth.delegation.DelegationTest
   * @param factory a {@code SubjectFactory<S, D>} implementation
   * @return A custom verb for the type returned by the SubjectFactory
   */
  public <S extends Subject<S, D>, D, SF extends SubjectFactory<S, D>> DelegatedVerb<S, D> about(
      SF factory) {
    return new DelegatedVerb<S, D>(getFailureStrategy(), factory);
  }

  /**
   * A generic, advanced method of extension of Truth to new types, which is documented on {@link
   * DelegatedVerbFactory}. Extension creators should prefer {@link SubjectFactory} if possible.
   *
   * @param <V> the type of {@link AbstractDelegatedVerb} to return
   * @param factory a {@code DelegatedVerbFactory<V>} implementation
   * @return A custom verb of type {@code <V>}
   * @deprecated When you switch from implementing {@link DelegatedVerbFactory} to implementing
   *     {@link CustomSubjectBuilderFactory}, you'll switch from this overload to {@linkplain
   *     #about(CustomSubjectBuilderFactory) the overload} that accepts a {@code
   *     CustomSubjectBuilderFactory}.
   */
  @Deprecated
  public final <V extends AbstractDelegatedVerb> V about(DelegatedVerbFactory<V> factory) {
    return factory.createVerb(getFailureStrategy());
  }

  /**
   * A special Verb implementation which wraps a SubjectFactory.
   *
   * @deprecated This class is being renamed to {@link SimpleSubjectBuilder}.
   */
  @Deprecated
  public static class DelegatedVerb<S extends Subject<S, T>, T> extends AbstractDelegatedVerb {
    private final FailureStrategy failureStrategy;
    private final SubjectFactory<S, T> subjectFactory;

    private static class Factory<S extends Subject<S, T>, T>
        implements DelegatedVerbFactory<DelegatedVerb<S, T>> {
      private final SubjectFactory<S, T> subjectFactory;

      private Factory(SubjectFactory<S, T> subjectFactory) {
        this.subjectFactory = subjectFactory;
      }

      @Override
      public DelegatedVerb<S, T> createVerb(FailureStrategy failureStrategy) {
        return new DelegatedVerb<S, T>(failureStrategy, subjectFactory);
      }
    }

    public DelegatedVerb(FailureStrategy failureStrategy, SubjectFactory<S, T> subjectFactory) {
      this.failureStrategy = checkNotNull(failureStrategy);
      this.subjectFactory = checkNotNull(subjectFactory);
    }

    public S that(@Nullable T target) {
      return subjectFactory.getSubject(failureStrategy, target);
    }
  }

  /**
   * @deprecated To prepend a message, use {@link StandardSubjectBuilder#withMessage}. If you are
   *     using {@code MessagePrependingFailureStrategy} to store and propagate the failure message
   *     as part of subclassing {@link AbstractVerb} or {@link TestVerb}, you will no longer need it
   *     when you migrate off those classes, as described in their deprecation text.
   */
  @Deprecated
  protected static final class MessagePrependingFailureStrategy extends FailureStrategy {
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

    @Override
    public void failComparing(
        String message, CharSequence expected, CharSequence actual, Throwable cause) {
      delegate.failComparing(prependFailureMessageIfAny(message), expected, actual, cause);
    }

    private String prependFailureMessageIfAny(String message) {
      return messageHolder.getFailureMessage() == null
          ? message
          : messageHolder.getFailureMessage() + ": " + message;
    }
  }
}
