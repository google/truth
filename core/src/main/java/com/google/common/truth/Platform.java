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

import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Lists.transform;
import static com.google.common.truth.Field.field;
import static com.google.common.truth.Platform.ComparisonFailureMessageStrategy.INCLUDE_COMPARISON_FAILURE_GENERATED_MESSAGE;
import static com.google.common.truth.Truth.appendSuffixIfNotNull;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.junit.ComparisonFailure;
import org.junit.rules.TestRule;

/**
 * Extracted routines that need to be swapped in for GWT, to allow for minimal deltas between the
 * GWT and non-GWT version.
 *
 * @author Christian Gruber (cgruber@google.com)
 */
final class Platform {
  private Platform() {}

  /** Returns true if the instance is assignable to the type Clazz. */
  static boolean isInstanceOfType(Object instance, Class<?> clazz) {
    return clazz.isInstance(instance);
  }

  /** Determines if the given subject contains a match for the given regex. */
  static boolean containsMatch(String actual, String regex) {
    return Pattern.compile(regex).matcher(actual).find();
  }

  /**
   * Returns an array containing all of the exceptions that were suppressed to deliver the given
   * exception. If suppressed exceptions are not supported (pre-Java 1.7), an empty array will be
   * returned.
   */
  static Throwable[] getSuppressed(Throwable throwable) {
    try {
      Method getSuppressed = throwable.getClass().getMethod("getSuppressed");
      return (Throwable[]) getSuppressed.invoke(throwable);
    } catch (NoSuchMethodException e) {
      return new Throwable[0];
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  /** Returns the class object for the given class name, or {@code null} if no class is found. */
  static Class<?> classForName(String fullyQualifiedClassName) {
    try {
      return Class.forName(fullyQualifiedClassName);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /** Returns true if the former class is assignable from the latter one. */
  static boolean isAssignableFrom(Class<?> superClass, Class<?> subClass) {
    return superClass.isAssignableFrom(subClass);
  }

  /**
   * Returns true if stack trace cleaning is explicitly disabled in a system property. This switch
   * os intended to be used when attempting to debug the frameworks which are collapsed or filtered
   * out of stack traces by the cleaner.
   */
  static boolean isStackTraceCleaningDisabled() {
    return Boolean.parseBoolean(
        System.getProperty("com.google.common.truth.disable_stack_trace_cleaning"));
  }

  @Nullable
  static ImmutableList<Field> makeDiff(String expected, String actual) {
    // For now, we just hide a common prefix or suffix. TODO(cpovirk): Add real diffing.

    ImmutableList<String> expectedLines = splitLines(expected);
    ImmutableList<String> actualLines = splitLines(actual);
    ImmutableList<String> originalExpectedLines = expectedLines;
    ImmutableList<String> originalActualLines = actualLines;

    int prefix = commonPrefix(expectedLines, actualLines);
    // No need to hide the prefix unless it's long.
    if (prefix > CONTEXT + 1) {
      // Truncate expectedLines and actualLines so that the suffix doesn't extend into them.
      expectedLines = expectedLines.subList(prefix, expectedLines.size());
      actualLines = actualLines.subList(prefix, actualLines.size());
    } else {
      prefix = 0;
    }

    int suffix = commonSuffix(expectedLines, actualLines);
    // No need to hide the suffix unless it's long.
    if (suffix > CONTEXT + 1) {
      // No need to update expectedLines and actualLines further; we don't read them again.
    } else {
      suffix = 0;
    }

    if (prefix == 0 && suffix == 0) {
      return null;
    }

    return ImmutableList.of(
        field("diff", hideLines(originalExpectedLines, originalActualLines, prefix, suffix)));
  }

  private static String hideLines(
      List<String> expectedLines, List<String> actualLines, int prefix, int suffix) {
    StringBuilder builder = new StringBuilder();

    if (prefix > CONTEXT) {
      builder.append(" ⋮\n");
    }
    if (prefix > 0) {
      appendSubListPrefixed(builder, ' ', expectedLines, prefix - CONTEXT, prefix);
    }

    appendSubListPrefixed(builder, '-', expectedLines, prefix, expectedLines.size() - suffix);
    appendSubListPrefixed(builder, '+', actualLines, prefix, actualLines.size() - suffix);

    if (suffix > 0) {
      appendSubListPrefixed(
          builder,
          ' ',
          expectedLines,
          expectedLines.size() - suffix,
          expectedLines.size() - suffix + CONTEXT);
    }
    if (suffix > CONTEXT) {
      builder.append(" ⋮\n");
    }
    builder.setLength(builder.length() - 1); // remove trailing newline

    return builder.toString();
  }

  private static void appendSubListPrefixed(
      StringBuilder builder, char prefix, List<String> lines, int start, int end) {
    List<String> subLines = lines.subList(start, end);
    List<String> prefixed = prefixAll(prefix, subLines);
    Joiner.on('\n').appendTo(builder, prefixed);
    if (start < end) {
      builder.append('\n');
    }
  }

  private static List<String> prefixAll(final char prefix, List<String> strings) {
    return transform(
        strings,
        new Function<String, String>() {
          @Override
          public String apply(String input) {
            return prefix + input;
          }
        });
  }

  private static ImmutableList<String> splitLines(String s) {
    // splitToList is @Beta, so we avoid it.
    return ImmutableList.copyOf(Splitter.onPattern("\r?\n").split(s));
  }

  private static int commonPrefix(List<?> a, List<?> b) {
    int i;
    for (i = 0; i < a.size() && i < b.size() && equal(a.get(i), b.get(i)); i++) {}
    return i;
  }

  private static int commonSuffix(List<?> a, List<?> b) {
    return commonPrefix(reverse(a), reverse(b));
  }

  private static final int CONTEXT = 3;

  enum ComparisonFailureMessageStrategy {
    OMIT_COMPARISON_FAILURE_GENERATED_MESSAGE,
    INCLUDE_COMPARISON_FAILURE_GENERATED_MESSAGE;
  }

  // TODO(cpovirk): Figure out which parameters can be null (and whether we want them to be).
  abstract static class PlatformComparisonFailure extends ComparisonFailure {
    private final String message;

    /** Separate cause field, in case initCause() fails. */
    @Nullable private final Throwable cause;

    @Nullable private final String suffix;

    private final ComparisonFailureMessageStrategy messageStrategy;

    // TODO(cpovirk): Do we ever pass null for message, expected, or actual?
    PlatformComparisonFailure(
        @Nullable String message,
        @Nullable String expected,
        @Nullable String actual,
        @Nullable String suffix,
        @Nullable Throwable cause,
        ComparisonFailureMessageStrategy messageStrategy) {
      super(message, expected, actual);
      this.message = message;
      this.suffix = suffix;
      this.cause = cause;
      this.messageStrategy = messageStrategy;

      try {
        initCause(cause);
      } catch (IllegalStateException alreadyInitializedBecauseOfHarmonyBug) {
        // See Truth.SimpleAssertionError.
      }
    }

    @Override
    public final String getMessage() {
      String body =
          messageStrategy == INCLUDE_COMPARISON_FAILURE_GENERATED_MESSAGE
              ? super.getMessage()
              : message;
      return appendSuffixIfNotNull(body, suffix);
    }

    @Override
    @SuppressWarnings("UnsynchronizedOverridesSynchronized")
    public final Throwable getCause() {
      return cause;
    }

    @Override
    public final String toString() {
      return getLocalizedMessage();
    }
  }

  static String doubleToString(double value) {
    return Double.toString(value);
  }

  static String floatToString(float value) {
    return Float.toString(value);
  }

  /** Returns a human readable string representation of the throwable's stack trace. */
  static String getStackTraceAsString(Throwable throwable) {
    return Throwables.getStackTraceAsString(throwable);
  }

  /** Tests if current platform is Android. */
  static boolean isAndroid() {
    return System.getProperties().getProperty("java.runtime.name").contains("Android");
  }

  /**
   * Wrapping interface of {@link TestRule} to be used within truth.
   *
   * <p>Note that the sole purpose of this interface is to allow it to be swapped in GWT
   * implementation.
   */
  interface JUnitTestRule extends TestRule {}

  static final String EXPECT_FAILURE_WARNING_IF_GWT = "";
}
