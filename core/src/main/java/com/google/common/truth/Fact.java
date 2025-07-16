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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.padEnd;
import static com.google.common.base.Strings.padStart;
import static com.google.common.truth.Platform.doubleToString;
import static com.google.common.truth.Platform.floatToString;
import static com.google.common.truth.Platform.stringValueForFailure;
import static java.lang.Math.max;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * A string key-value pair in a failure message, such as "expected: abc" or "but was: xyz."
 *
 * <p>Most Truth users will never interact with this type. It appears in the Truth API only as a
 * parameter to methods like {@link Subject#failWithActual(Fact, Fact...)}, which are used only by
 * custom {@link Subject} implementations.
 *
 * <p>If you are writing a custom {@link Subject}, see <a
 * href="https://truth.dev/failure_messages">our tips on writing failure messages</a>.
 */
public final class Fact implements Serializable {
  /**
   * Creates a fact with the given key and value, which will be printed in a format like "key:
   * value." The value is converted to a string by calling {@code String.valueOf} on it.
   */
  public static Fact fact(String key, @Nullable Object value) {
    return new Fact(key, stringValueForFailure(value), /* padStart= */ false);
  }

  /**
   * Creates a fact with no value, which will be printed in the format "key" (with no colon or
   * value).
   *
   * <p>In most cases, prefer {@linkplain #fact key-value facts}, which give Truth more flexibility
   * in how to format the fact for display. {@code simpleFact} is useful primarily for:
   *
   * <ul>
   *   <li>messages from no-arg assertions. For example, {@code isNotEmpty()} would generate the
   *       fact "expected not to be empty"
   *   <li>prose that is part of a larger message. For example, {@code contains()} sometimes
   *       displays facts like "expected to contain: ..." <i>"but did not"</i> "though it did
   *       contain: ..."
   * </ul>
   */
  public static Fact simpleFact(String key) {
    return new Fact(key, null, /* padStart= */ false);
  }

  /** Creates a fact with the given key and the value returned by the given {@link Supplier}. */
  static Fact factFromSupplier(String key, Supplier<?> valueSupplier) {
    // We delay evaluation of Supplier.get() until we are inside stringValueForFailure.
    return fact(
        key,
        stringValueForFailure(
            new Object() {
              @Override
              public String toString() {
                return String.valueOf(valueSupplier.get());
              }
            }));
  }

  /**
   * Creates a fact with the given key and value, which will be printed in a format like "key:
   * value." The numeric value is converted to a string with delimiting commas.
   */
  static Fact numericFact(String key, @Nullable Number value) {
    return new Fact(key, formatNumericValue(value), /* padStart= */ true);
  }

  /**
   * Formats the given numeric value as a string with delimiting commas.
   *
   * <p><b>Note:</b> only {@link Long}, {@link Integer}, {@link Float}, {@link Double} and {@link
   * BigDecimal} are supported.
   */
  static String formatNumericValue(@Nullable Number value) {
    if (value == null) {
      return "null";
    }
    // the value must be a numeric type
    checkArgument(
        value instanceof Long
            || value instanceof Integer
            || value instanceof Float
            || value instanceof Double
            || value instanceof BigDecimal,
        "Value (%s) must be either a Long, Integer, Float, Double, or BigDecimal.",
        value);

    // DecimalFormat is not available on all platforms, so we do the formatting manually.

    if (!(value instanceof BigDecimal) && isInfiniteOrNaN(value.doubleValue())) {
      return value.toString();
    }
    String stringValue =
        value instanceof Double
            ? doubleToString((double) value)
            : value instanceof Float //
                ? floatToString((float) value)
                : value.toString();
    if (stringValue.contains("E")) {
      return stringValue;
    }
    int decimalIndex = stringValue.indexOf('.');
    if (decimalIndex == -1) {
      return formatWholeNumericValue(stringValue);
    }
    String wholeNumbers = stringValue.substring(0, decimalIndex);
    String decimal = stringValue.substring(decimalIndex);
    return formatWholeNumericValue(wholeNumbers) + decimal;
  }

  private static boolean isInfiniteOrNaN(double d) {
    return Double.isInfinite(d) || Double.isNaN(d);
  }

  private static String formatWholeNumericValue(String stringValue) {
    boolean isNegative = stringValue.startsWith("-");
    if (isNegative) {
      stringValue = stringValue.substring(1);
    }

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < stringValue.length(); i++) {
      builder.append(stringValue.charAt(i));
      if ((stringValue.length() - i - 1) % 3 == 0 && i != stringValue.length() - 1) {
        builder.append(',');
      }
    }
    return isNegative ? "-" + builder : builder.toString();
  }

  private final String key;
  private final @Nullable String value;
  private final boolean padStart;

  private Fact(String key, @Nullable String value, boolean padStart) {
    this.key = checkNotNull(key);
    this.value = value;
    this.padStart = padStart;
  }

  String getKey() {
    return key;
  }

  @Nullable
  String getValue() {
    return value;
  }

  /**
   * Returns a simple string representation for the fact. While this is used in the output of {@code
   * TruthFailureSubject}, it's not used in normal failure messages, which automatically align facts
   * horizontally and indent multiline values.
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
    int longestIntPartValueLength = 0;
    boolean seenNewlineInValue = false;
    for (Fact fact : facts) {
      if (fact.value != null) {
        longestKeyLength = max(longestKeyLength, fact.key.length());
        if (fact.padStart) {
          int decimalIndex = fact.value.indexOf('.');
          if (decimalIndex != -1) {
            longestIntPartValueLength = max(longestIntPartValueLength, decimalIndex);
          } else {
            longestIntPartValueLength = max(longestIntPartValueLength, fact.value.length());
          }
        }
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
      if (fact.value == null) {
        builder.append(fact.key);
      } else if (seenNewlineInValue) {
        builder.append(fact.key);
        builder.append(":\n");
        builder.append(indent(fact.value));
      } else {
        builder.append(padEnd(fact.key, longestKeyLength, ' '));
        builder.append(": ");
        if (fact.padStart) {
          int decimalIndex = fact.value.indexOf('.');
          if (decimalIndex != -1) {
            builder.append(
                padStart(fact.value.substring(0, decimalIndex), longestIntPartValueLength, ' '));
            builder.append(fact.value.substring(decimalIndex));
          } else {
            builder.append(padStart(fact.value, longestIntPartValueLength, ' '));
          }
        } else {
          builder.append(fact.value);
        }
      }
      builder.append('\n');
    }
    if (builder.length() > 0) {
      builder.setLength(builder.length() - 1); // remove trailing \n
    }
    return builder.toString();
  }

  private static String indent(String value) {
    // We don't want to indent with \t because the text would align exactly with the stack trace.
    // We don't want to indent with \t\t because it would be very far for people with 8-space tabs.
    // Let's compromise and indent by 4 spaces, which is different than both 2- and 8-space tabs.
    return "    " + value.replace("\n", "\n    ");
  }
}
