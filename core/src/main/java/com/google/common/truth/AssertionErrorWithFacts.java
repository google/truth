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
import static com.google.common.truth.Fact.makeMessage;

import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.Nullable;

/**
 * An {@link AssertionError} composed of structured {@link Fact} instances and other string
 * messages.
 */
@SuppressWarnings("OverrideThrowableToString") // We intentionally hide the class name.
final class AssertionErrorWithFacts extends AssertionError implements ErrorWithFacts {
  private final ImmutableList<Fact> facts;

  private AssertionErrorWithFacts(
      ImmutableList<String> messages, ImmutableList<Fact> facts, @Nullable Throwable cause) {
    super(makeMessage(messages, facts), cause);
    this.facts = checkNotNull(facts);
  }

  static AssertionErrorWithFacts create(
      ImmutableList<String> messages, ImmutableList<Fact> facts, @Nullable Throwable cause) {
    return new AssertionErrorWithFacts(messages, facts, cause);
  }

  static AssertionError createWithoutFacts(String message, @Nullable Throwable cause) {
    return create(ImmutableList.of(message), ImmutableList.of(), cause);
  }

  static AssertionError createWithoutFactsOrStack(String message, @Nullable Throwable cause) {
    AssertionError error = createWithoutFacts(message, cause);
    error.setStackTrace(new StackTraceElement[0]);
    return error;
  }

  static AssertionError createWithoutFactsOrStack(String message) {
    return createWithoutFactsOrStack(message, /* cause= */ null);
  }

  @Override
  public String toString() {
    return checkNotNull(getLocalizedMessage());
  }

  @Override
  public ImmutableList<Fact> facts() {
    return facts;
  }
}
