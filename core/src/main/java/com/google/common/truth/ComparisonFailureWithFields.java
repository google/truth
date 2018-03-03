/*
 * Copyright (c) 2014 Google, Inc.
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
import static com.google.common.base.Strings.commonPrefix;
import static com.google.common.base.Strings.commonSuffix;
import static com.google.common.truth.Field.field;
import static com.google.common.truth.Field.makeMessage;
import static com.google.common.truth.Platform.ComparisonFailureMessageStrategy.OMIT_COMPARISON_FAILURE_GENERATED_MESSAGE;
import static com.google.common.truth.SubjectUtils.concat;
import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Math.max;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Platform.PlatformComparisonFailure;
import javax.annotation.Nullable;

/**
 * An {@link AssertionError} (usually a JUnit {@code ComparisonFailure}, but not under GWT) composed
 * of structured {@link Field} instances and other string messages.
 *
 * <p>This class includes logic to format expected and actual values for easier reading.
 */
final class ComparisonFailureWithFields extends PlatformComparisonFailure {
  static ComparisonFailureWithFields create(
      ImmutableList<String> messages,
      ImmutableList<Field> headFields,
      ImmutableList<Field> tailFields,
      String expected,
      String actual,
      @Nullable Throwable cause) {
    ImmutableList<Field> fields = makeFields(headFields, tailFields, expected, actual);
    return new ComparisonFailureWithFields(messages, fields, expected, actual, cause);
  }

  final ImmutableList<Field> fields;

  private ComparisonFailureWithFields(
      ImmutableList<String> messages,
      ImmutableList<Field> fields,
      String expected,
      String actual,
      @Nullable Throwable cause) {
    super(
        makeMessage(messages, fields),
        checkNotNull(expected),
        checkNotNull(actual),
        /* suffix= */ null,
        cause,
        OMIT_COMPARISON_FAILURE_GENERATED_MESSAGE);
    this.fields = checkNotNull(fields);
  }

  private static ImmutableList<Field> makeFields(
      ImmutableList<Field> headFields,
      ImmutableList<Field> tailFields,
      String expected,
      String actual) {
    return concat(headFields, formatExpectedAndActual(expected, actual), tailFields);
  }

  /**
   * Returns one or more fields describing the difference between the given expected and actual
   * values.
   *
   * <p>Currently, that means either 2 fields (one each for expected and actual) or 1 field with a
   * diff-like (but much simpler) view.
   *
   * <p>In the case of 2 fields, the fields contain either the full expected and actual values or,
   * if the values have a long prefix or suffix in common, abbreviated values with "…" at the
   * beginning or end.
   */
  @VisibleForTesting
  static ImmutableList<Field> formatExpectedAndActual(String expected, String actual) {
    ImmutableList<Field> result;

    result = Platform.makeDiff(expected, actual);
    if (result != null) {
      return result;
    }

    result = removeCommonPrefixAndSuffix(expected, actual);
    if (result != null) {
      return result;
    }

    return ImmutableList.of(field("expected", expected), field("but was", actual));
  }

  @Nullable
  private static ImmutableList<Field> removeCommonPrefixAndSuffix(String expected, String actual) {
    int originalExpectedLength = expected.length();

    // TODO(cpovirk): Use something like BreakIterator where available.
    /*
     * TODO(cpovirk): If the abbreviated values contain newlines, maybe expand them to contain a
     * newline on each end so that we don't start mid-line? That way, horizontally aligned text will
     * remain horizontally aligned. But of course, for many multi-line strings, we won't enter this
     * method at all because we'll generate diff-style output instead. So we might not need to worry
     * too much about newlines here.
     */
    int prefix = commonPrefix(expected, actual).length();
    prefix = max(0, prefix - CONTEXT);
    while (prefix > 0 && validSurrogatePairAt(expected, prefix - 1)) {
      prefix--;
    }
    // No need to hide the prefix unless it's long.
    if (prefix > 3) {
      expected = "…" + expected.substring(prefix);
      actual = "…" + actual.substring(prefix);
    }

    int suffix = commonSuffix(expected, actual).length();
    suffix = max(0, suffix - CONTEXT);
    while (suffix > 0 && validSurrogatePairAt(expected, expected.length() - suffix - 1)) {
      suffix--;
    }
    // No need to hide the suffix unless it's long.
    if (suffix > 3) {
      expected = expected.substring(0, expected.length() - suffix) + "…";
      actual = actual.substring(0, actual.length() - suffix) + "…";
    }

    if (originalExpectedLength - expected.length() < WORTH_HIDING) {
      return null;
    }

    return ImmutableList.of(field("expected", expected), field("but was", actual));
  }

  private static final int CONTEXT = 20;
  private static final int WORTH_HIDING = 60;

  // From c.g.c.base.Strings.
  private static boolean validSurrogatePairAt(CharSequence string, int index) {
    return index >= 0
        && index <= (string.length() - 2)
        && isHighSurrogate(string.charAt(index))
        && isLowSurrogate(string.charAt(index + 1));
  }
}
