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
   * TODO(cgruber): Do something closer to what JUnit's {@code ComparisonFailure} does.
   */
  static String messageFor(String message, CharSequence expected, CharSequence actual) {
    return checkNotNull(message)
        + "\n\nExpected:\n"
        + checkNotNull(expected)
        + "\n\nActual:\n"
        + checkNotNull(actual);
  }

  /**
   * Substitutes each {@code %s} in {@code template} with an argument. These
   * are matched by position - the first {@code %s} gets {@code args[0]}, etc.
   * If there are more arguments than placeholders, the unmatched arguments will
   * be appended to the end of the formatted message in square braces.
   *
   * Cribbed from Guava's {@link com.google.common.base.Preconditions} to allow for a
   * GWT-compatible alternative to {@link String#format(String, Object...)}
   *
   * @param template a string containing 0 or more {@code %s} placeholders
   * @param args the arguments to be substituted into the message
   *     template. Arguments are converted to strings using
   *     {@link String#valueOf(Object)}. Arguments can be null.
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
    if (template.indexOf("%s", templateStart) >= 0) {
      throw new IllegalStateException(
          "Too many parameters for " + args.length + " argument: \"" + template + "\"");
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
   * Returns the number of {@code %s} placeholders in the template.
   */
  static int countOfPlaceholders(@Nullable String template) {
    template = String.valueOf(template); // null -> "null"
    int templateStart = 0;
    int count = 0;
    int placeholderStart = template.indexOf("%s", templateStart);
    while (placeholderStart != -1) {
      templateStart = placeholderStart + 2;
      count++;
      placeholderStart = template.indexOf("%s", templateStart);
    }
    return count;
  }
}
