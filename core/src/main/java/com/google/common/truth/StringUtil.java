/*
 * Copyright (c) 2013 Google, Inc.
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

import javax.annotation.Nullable;

/**
 * Utilities for string comparisons.
 *
 * @author Christian Gruber (cgruber@google.com)
 */
final class StringUtil {
  private StringUtil() {}

  /**
   * Returns a message appropriate for string comparisons.
   *
   * <p>TODO(cgruber): Do something closer to what JUnit's {@code ComparisonFailure} does.
   */
  static String messageFor(String message, CharSequence expected, CharSequence actual) {
    return checkNotNull(message)
        + "\n\nExpected:\n"
        + checkNotNull(expected)
        + "\n\nActual:\n"
        + checkNotNull(actual);
  }

  /**
   * Substitutes each {@code %s} in {@code template} with an argument. These are matched by position
   * - the first {@code %s} gets {@code args[0]}, etc. If there are more arguments than
   * placeholders, the unmatched arguments will be appended to the end of the formatted message in
   * square braces.
   *
   * <p>Cribbed from Guava's {@link com.google.common.base.Preconditions} to allow for a
   * GWT-compatible alternative to {@link String#format(String, Object...)}
   *
   * @param template a string containing 0 or more {@code %s} placeholders
   * @param args the arguments to be substituted into the message template. Arguments are converted
   *     to strings using {@link String#valueOf(Object)}. Arguments can be null.
   */
  static String format(@Nullable String template, Object... args) {
    template = String.valueOf(template); // null -> "null"

    // start substituting the arguments into the '%s' placeholders
    StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
    int templateStart = 0;
    int i = 0;
    while (i < args.length) {
      int placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }
      builder.append(template.substring(templateStart, placeholderStart));
      builder.append(args[i++]);
      templateStart = placeholderStart + 2;
    }
    builder.append(template.substring(templateStart));

    // if we run out of placeholders, append the extra args in square braces
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);
      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }
      builder.append(']');
    }

    return builder.toString();
  }

  /**
   * Compresses a type's string format by stripping boring prefix.
   *
   * <p>Inspired by JavaWriter, but without using {@link java.util.regex.Pattern} which is not
   * available under GWT.
   */
  static String compressType(String type) {
    type = typeOnly(type);
    return type.replaceAll("java\\.lang\\.|java\\.util\\.", "");
  }

  private static String typeOnly(String type) {
    // TODO(cpovirk): Always pass getName() rather than toString(), then eliminate this.
    type = stripIfPrefixed(type, "class ");
    type = stripIfPrefixed(type, "interface ");
    return type;
  }

  private static String stripIfPrefixed(String string, String prefix) {
    return string.startsWith(prefix) ? string.substring(prefix.length()) : string;
  }
}
