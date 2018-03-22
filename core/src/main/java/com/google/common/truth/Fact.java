/*
 * Copyright (c) 2018 Google, Inc.
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
import static com.google.common.base.Strings.padEnd;
import static java.lang.Math.max;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;

/** A string key-value pair in a failure message, such as "expected: abc" or "but was: xyz." */
final class Fact {
  /**
   * Creates a fact with the given key and value, which will be printed in a format like "key:
   * value." The value is converted to a string by calling {@code String.valueOf} on it.
   */
  static Fact fact(String key, Object value) {
    return new Fact(key, String.valueOf(value));
  }

  /**
   * Creates a fact with no value, which will be printed in the format "key" (with no colon or
   * value).
   */
  static Fact factWithoutValue(String key) {
    return new Fact(key, null);
  }

  final String key;
  @Nullable final String value;

  private Fact(String key, @Nullable String value) {
    this.key = checkNotNull(key);
    this.value = value;
  }

  /**
   * Returns a simple string representation for the fact. While this is used by the old-style
   * messages and {@code TruthFailureSubject} output, we're moving away from the old-style messages
   * and onto {@link #makeMessage}, which aligns facts horizontally and indents multiline values.
   */
  @Override
  public String toString() {
    return value == null ? key : key + ": " + value;
  }

  /**
   * Formats the given messages and facts into a string for use as the message of a test failure. In
   * particular, this method horizontally aligns the beginning of fact values.
   */
  static String makeMessage(ImmutableList<String> messages, ImmutableList<Fact> facts) {
    int longestKeyLength = 0;
    boolean seenNewlineInValue = false;
    for (Fact fact : facts) {
      if (fact.value != null) {
        longestKeyLength = max(longestKeyLength, fact.key.length());
        // TODO(cpovirk): Look for other kinds of newlines.
        seenNewlineInValue |= fact.value.contains("\n");
      }
    }

    StringBuilder builder = new StringBuilder();
    for (String message : messages) {
      builder.append(message);
      builder.append('\n');
    }

    /*
     * *Usually* the first fact is printed at the beginning of a new line. However, when this
     * exception is the cause of another exception, that exception will print it starting after
     * "Caused by: " on the same line. The other exception sometimes also reuses this message as its
     * own message. In both of those scenarios, the first line doesn't start at column 0, so the
     * horizontal alignment is thrown off.
     *
     * There's not much we can do about this, short of always starting with a newline (which would
     * leave a blank line at the beginning of the message in the normal case).
     */
    for (Fact fact : facts) {
      if (seenNewlineInValue) {
        builder.append(fact.key);
        if (fact.value != null) {
          builder.append(":\n");
          builder.append(indent(fact.value));
        }
      } else {
        builder.append(padEnd(fact.key, longestKeyLength, ' '));
        if (fact.value != null) {
          builder.append(": ");
          builder.append(fact.value);
        }
      }
      builder.append('\n');
    }
    builder.setLength(builder.length() - 1); // remove trailing \n
    return builder.toString();
  }

  private static String indent(String value) {
    // We don't want to indent with \t because the text would align exactly with the stack trace.
    // We don't want to indent with \t\t because it would be very far for people with 8-space tabs.
    // Let's compromise and indent by 4 spaces, which is different than both 2- and 8-space tabs.
    return "    " + value.replaceAll("\n", "\n    ");
  }
}
