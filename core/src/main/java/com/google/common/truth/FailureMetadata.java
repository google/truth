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
 * {@code Subject.Factory} methods directly. Instead, they should use the appropriate factory
 * method:
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
    return new FailureMetadata(
        failureStrategy, ImmutableList.<Message>of(), ImmutableList.<Subject<?, ?>>of());
  }

  private final FailureStrategy strategy;

  /*
   * TODO(cpovirk): This implementation is wasteful. Probably it doesn't matter, since the APIs that
   * construct ImmutableLists are used primarily by APIs that are likely to allocate a lot, anyway.
   * Specifically, `messages` is used by the withMessage() varargs method, and `chain` is used by
   * chaining assertions like those for throwables and multimaps. But if it ever does matter, we
   * could use an immutable cactus stack -- or probably even avoid storing most of the chain
   * entirely (unless we end up wanting more of the chain to show "telescoping context," as in "the
   * int value of this optional in this list in this multimap").
   */

  private final ImmutableList<Message> messages;

  /*
   * We store Subject, rather than the actual value itself, so that we can call actualAsString().
   * Why not call the method immediately? First, it might be expensive, and second, the Subject
   * isn't initialized at the time we receive it. We *might* be able to make it safe to call if it
   * looks only at actual(), but it might try to look at fields initialized by a subclass, which
   * aren't ready yet.
   */
  private final ImmutableList<Subject<?, ?>> chain;

  FailureMetadata(
      FailureStrategy strategy,
      ImmutableList<Message> messages,
      ImmutableList<Subject<?, ?>> chain) {
    this.strategy = checkNotNull(strategy);
    this.messages = checkNotNull(messages);
    this.chain = checkNotNull(chain);
  }

  /**
   * Returns a new instance that includes the given subject in its chain of values. Truth users do
   * not need to call this method directly; Truth automatically accumulates context, starting from
   * the initial that(...) call and continuing into any chained calls, like {@link
   * ThrowableSubject#hasMessageThat}.
   */
  FailureMetadata updateForSubject(Subject<?, ?> subject) {
    ImmutableList<Subject<?, ?>> chain = append(this.chain, subject);
    return derive(messages, chain);
  }

  /**
   * Returns a new instance whose failures will contain the given message. The way for Truth users
   * to set a message is {@code check().withMessage(...).that(...)} (for calls from within a {@code
   * Subject}) or {@link Truth#assertWithMessage} (for most other calls).
   */
  FailureMetadata withMessage(String format, Object[] args) {
    ImmutableList<Message> messages = append(this.messages, new Message(format, args));
    return derive(messages, chain);
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

  void fail(String message) {
    strategy.fail(addToMessage(message), rootCause());
  }

  void fail(String message, Throwable cause) {
    strategy.fail(addToMessage(message), cause);
    // TODO(cpovirk): add rootCause() as a suppressed exception? If fail() throws...
  }

  void failComparing(String message, CharSequence expected, CharSequence actual) {
    strategy.failComparing(addToMessage(message), expected, actual, rootCause());
  }

  void failComparing(String message, CharSequence expected, CharSequence actual, Throwable cause) {
    strategy.failComparing(addToMessage(message), expected, actual, cause);
    // TODO(cpovirk): add rootCause() as a suppressed exception? If failComparing() throws...
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
         */
        result.append(":");
      } else {
        result.append(": ");
      }
    }
    result.append(body);
    return result.toString();
  }

  private FailureMetadata derive(
      ImmutableList<Message> messages, ImmutableList<Subject<?, ?>> chain) {
    return new FailureMetadata(strategy, messages, chain);
  }

  /**
   * Returns the first {@link Throwable} in the chain of actual values or {@code null} if none are
   * present. Typically, we'll have a root cause only if the assertion chain contains a {@link
   * ThrowableSubject}.
   */
  private Throwable rootCause() {
    for (Subject<?, ?> subject : chain) {
      if (subject.actual() instanceof Throwable) {
        return (Throwable) subject.actual();
      }
    }
    return null;
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

  private static <E> ImmutableList<E> append(ImmutableList<? extends E> list, E object) {
    return ImmutableList.<E>builder().addAll(list).add(object).build();
  }
}
