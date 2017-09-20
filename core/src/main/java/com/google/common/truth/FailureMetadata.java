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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;

/**
 * An opaque, immutable object containing state from the previous calls in the fluent assertion
 * chain. It appears primarily as a parameter to {@link Subject} constructors (and {@link
 * Subject.Factory} methods), which should pass it to the superclass constructor and not otherwise
 * use or store it. In particular, users should not attempt to call {@code Subject} constructors or
 * {@code Subject.Factory} methods directly. Instead, they should use the appopriate factory method:
 *
 * <ul>
 *   <li>If you're writing a test: {@link Truth#assertAbout(Subject.Factory)}{@code .that(...)}
 *   <li>If you're creating a derived subject from within another subject: {@code
 *       check().about(...).that(...)}
 *   <li>If you're testing your subject to verify that assertions fail when they should: {@link
 *       ExpectFailure}
 * </ul>
 *
 * <p>(One exception: Implementations of {@link CustomSubjectBuilder} do directly call constructors,
 * using their {@link CustomSubjectBuilder#metadata} method to get an instance to pass to the
 * constructor.)
 */
public final class FailureMetadata {
  static FailureMetadata forFailureStrategy(FailureStrategy failureStrategy) {
    return new FailureMetadata(failureStrategy, ImmutableList.<Message>of(), null);
  }

  private final FailureStrategy strategy;
  private final ImmutableList<Message> messages;
  // TODO(cpovirk): Maybe store a root *object*, too, or even a full chain of objects.
  @Nullable private final Throwable rootCause;

  FailureMetadata(
      FailureStrategy strategy, ImmutableList<Message> messages, @Nullable Throwable rootCause) {
    this.strategy = checkNotNull(strategy);
    this.messages = checkNotNull(messages);
    this.rootCause = rootCause;
  }

  FailureStrategy legacyStrategy() {
    return new MessageAddingFailureStrategy();
  }

  /**
   * Returns a new instance whose "root cause" (used as the cause of the {@code AssertionError}) is
   * set to the given value, or returns {@code this} if a root cause has already been set. Truth
   * users do not need to call this method directly; Truth automatically sets the first {@code
   * Throwable} it sees as the root cause.
   */
  FailureMetadata offerRootCause(Throwable rootCause) {
    checkNotNull(rootCause);
    return this.rootCause == null ? new FailureMetadata(strategy, messages, rootCause) : this;
  }

  /**
   * Returns a new instance whose failures will contain the given message. The way for Truth users
   * to set a message is {@code check().withMessage(...).that(...)} (for calls from within a {@code
   * Subject}) or {@link Truth#assertWithMessage} (for most other calls).
   */
  FailureMetadata withMessage(String format, Object[] args) {
    return new FailureMetadata(strategy, prepend(messages, new Message(format, args)), rootCause);
  }

  private static final class Message {
    private static final String PLACEHOLDER_ERR =
        "Incorrect number of args (%s) for the given placeholders (%s) in string template:\"%s\"";

    private final String format;
    private final Object[] args;

    Message(@Nullable String format, @Nullable Object... args) {
      this.format = format;
      this.args = args;
      int placeholders = countPlaceholders(format);
      checkArgument(
          placeholders == args.length, PLACEHOLDER_ERR, args.length, placeholders, format);
    }

    @Override
    public String toString() {
      return StringUtil.format(format, args);
    }
  }

  private final class MessageAddingFailureStrategy extends FailureStrategy {
    @Override
    public void fail(String message) {
      strategy.fail(addToMessage(message), rootCause);
    }

    @Override
    public void fail(String message, Throwable cause) {
      strategy.fail(addToMessage(message), cause);
      // TODO(cpovirk): add defaultCause as a suppressed exception? If fail() throws...
    }

    @Override
    public void failComparing(String message, CharSequence expected, CharSequence actual) {
      strategy.failComparing(addToMessage(message), expected, actual, rootCause);
    }

    @Override
    public void failComparing(
        String message, CharSequence expected, CharSequence actual, Throwable cause) {
      strategy.failComparing(addToMessage(message), expected, actual, cause);
      // TODO(cpovirk): add defaultCause as a suppressed exception? If failComparing() throws...
    }

    private String addToMessage(String body) {
      StringBuilder result = new StringBuilder(body.length());
      Joiner.on(": ").appendTo(result, messages);
      if (!messages.isEmpty()) {
        if (body.isEmpty()) {
          /*
           * The only likely case of an empty body is with failComparing(). In that case, we still
           * want a colon because ComparisonFailure will construct a message of the form
           * "<ourString> <theirString>." For consistency with the normal behavior of withMessage,
           * we want a colon between the parts.
           *
           * That actually makes it sound like we'd want to *always* include the trailing colon in
           * the case of failComparing(), but I don't want to bite that off now in case it requires
           * updating more existing Subjects' tests.
           *
           * Note that this doesn't necessarily even work completely yet: It assumes that the whole
           * assertion chain uses FailureMetadata. If at any point we switch back to
           * FailureStrategy, we lose the ability to append the colon only once because each
           * FailureStrategy wrapper has no knowledge of the rest. Or maybe there's some way to make
           * it work, but I have sunk too much time into this as it is :( Anyway, the problem will
           * go away once we standardize on FailureMetadata. And the entire "problem" consists of an
           * extra space in the failure messages, anyway.
           */
          result.append(":");
        } else {
          result.append(": ");
        }
      }
      result.append(body);
      return result.toString();
    }
  }

  @VisibleForTesting
  static int countPlaceholders(@Nullable String template) {
    if (template == null) {
      return 0;
    }
    int index = 0;
    int count = 0;
    while (true) {
      index = template.indexOf("%s", index);
      if (index == -1) {
        break;
      }
      index++;
      count++;
    }
    return count;
  }

  private static ImmutableList<Message> prepend(ImmutableList<Message> messages, Message message) {
    return ImmutableList.<Message>builder().addAll(messages).add(message).build();
  }
}
