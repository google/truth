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

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.truth.DiffUtils.generateUnifiedDiff;
import static com.google.common.truth.Fact.fact;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
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

  static void cleanStackTrace(Throwable throwable) {
    StackTraceCleaner.cleanStackTrace(throwable);
  }

  /**
   * Tries to infer a name for the root actual value from the bytecode. The "root" actual value is
   * the value passed to {@code assertThat} or {@code that}, as distinct from any later actual
   * values produced by chaining calls like {@code hasMessageThat}.
   */
  static String inferDescription() {
    if (isInferDescriptionDisabled()) {
      return null;
    }

    AssertionError stack = new AssertionError();
    /*
     * cleanStackTrace() lets users turn off cleaning, so it's possible that we'll end up operating
     * on an uncleaned stack trace. That should be mostly harmless. We could try force-enabling
     * cleaning for inferDescription() only, but if anyone is turning it off, it might be because of
     * bugs or confusing stack traces. Force-enabling it here might trigger those same problems.
     */
    cleanStackTrace(stack);
    if (stack.getStackTrace().length == 0) {
      return null;
    }
    StackTraceElement top = stack.getStackTrace()[0];
    try {
      /*
       * Invoke ActualValueInference reflectively so that Truth can be compiled and run without its
       * dependency, ASM, on the classpath.
       *
       * Also, mildly obfuscate the class name that we're looking up. The obfuscation prevents R8
       * from detecting the usage of ActualValueInference. That in turn lets users exclude it from
       * the compile-time classpath if they want. (And then *that* probably makes it easier and/or
       * safer for R8 users (i.e., Android users) to exclude it from the *runtime* classpath. It
       * would do no good there, anyway, since ASM won't find any .class files to load under
       * Android. Perhaps R8 will even omit ASM automatically once it detects that it's "unused?")
       *
       * TODO(cpovirk): Add a test that runs R8 without ASM present.
       */
      String clazz =
          Joiner.on('.').join("com", "google", "common", "truth", "ActualValueInference");
      return (String)
          Class.forName(clazz)
              .getDeclaredMethod("describeActualValue", String.class, String.class, int.class)
              .invoke(null, top.getClassName(), top.getMethodName(), top.getLineNumber());
    } catch (IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException
        | ClassNotFoundException
        | LinkageError
        | RuntimeException e) {
      // Some possible reasons:
      // - Inside Google, we omit ActualValueInference entirely under Android.
      // - Outside Google, someone is running without ASM on the classpath.
      // - There's a bug.
      // - We don't handle a new bytecode feature.
      // TODO(cpovirk): Log a warning, at least for non-ClassNotFoundException, non-LinkageError?
      return null;
    }
  }

  private static final String DIFF_KEY = "diff (-expected +actual)";

  static @Nullable ImmutableList<Fact> makeDiff(String expected, String actual) {
    ImmutableList<String> expectedLines = splitLines(expected);
    ImmutableList<String> actualLines = splitLines(actual);
    List<String> unifiedDiff =
        generateUnifiedDiff(expectedLines, actualLines, /* contextSize= */ 3);
    if (unifiedDiff.isEmpty()) {
      return ImmutableList.of(
          fact(DIFF_KEY, "(line contents match, but line-break characters differ)"));
      // TODO(cpovirk): Possibly include the expected/actual value, too?
    }
    String result = Joiner.on("\n").join(unifiedDiff);
    if (result.length() > expected.length() && result.length() > actual.length()) {
      return null;
    }
    return ImmutableList.of(fact(DIFF_KEY, result));
  }

  private static ImmutableList<String> splitLines(String s) {
    // splitToList is @Beta, so we avoid it.
    return ImmutableList.copyOf(Splitter.onPattern("\r?\n").split(s));
  }

  abstract static class PlatformComparisonFailure extends ComparisonFailure {
    private final String message;

    /** Separate cause field, in case initCause() fails. */
    private final @Nullable Throwable cause;

    PlatformComparisonFailure(
        String message, String expected, String actual, @Nullable Throwable cause) {
      super(message, expected, actual);
      this.message = message;
      this.cause = cause;

      try {
        initCause(cause);
      } catch (IllegalStateException alreadyInitializedBecauseOfHarmonyBug) {
        // See Truth.SimpleAssertionError.
      }
    }

    @Override
    public final String getMessage() {
      return message;
    }

    @Override
    @SuppressWarnings("UnsynchronizedOverridesSynchronized")
    public final Throwable getCause() {
      return cause;
    }

    // To avoid printing the class name before the message.
    // TODO(cpovirk): Write a test that fails without this. Ditto for SimpleAssertionError.
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
    return System.getProperty("java.runtime.name").contains("Android");
  }

  /**
   * Wrapping interface of {@link TestRule} to be used within truth.
   *
   * <p>Note that the sole purpose of this interface is to allow it to be swapped in GWT
   * implementation.
   */
  interface JUnitTestRule extends TestRule {}

  static final String EXPECT_FAILURE_WARNING_IF_GWT = "";

  // TODO(cpovirk): Share code with StackTraceCleaner?
  private static boolean isInferDescriptionDisabled() {
    // Reading system properties might be forbidden.
    try {
      return Boolean.parseBoolean(
          System.getProperty("com.google.common.truth.disable_infer_description"));
    } catch (SecurityException e) {
      // Hope for the best.
      return false;
    }
  }

  static AssertionError makeComparisonFailure(
      ImmutableList<String> messages,
      ImmutableList<Fact> facts,
      String expected,
      String actual,
      @Nullable Throwable cause) {
    Class<?> comparisonFailureClass;
    try {
      comparisonFailureClass = Class.forName("com.google.common.truth.ComparisonFailureWithFacts");
    } catch (LinkageError | ClassNotFoundException probablyJunitNotOnClasspath) {
      /*
       * LinkageError makes sense, but ClassNotFoundException shouldn't happen:
       * ComparisonFailureWithFacts should be there, even if its JUnit 4 dependency is not. But it's
       * harmless to catch an "impossible" exception, and if someone decides to strip the class out
       * (perhaps along with Platform.PlatformComparisonFailure, to satisfy a tool that is unhappy
       * because it can't find the latter's superclass because JUnit 4 is also missing?), presumably
       * we should still fall back to a plain AssertionError.
       *
       * TODO(cpovirk): Consider creating and using yet another class like AssertionErrorWithFacts,
       * not actually extending ComparisonFailure but still exposing getExpected() and getActual()
       * methods.
       */
      return new AssertionErrorWithFacts(messages, facts, cause);
    }
    Class<? extends AssertionError> asAssertionErrorSubclass =
        comparisonFailureClass.asSubclass(AssertionError.class);

    Constructor<? extends AssertionError> constructor;
    try {
      constructor =
          asAssertionErrorSubclass.getDeclaredConstructor(
              ImmutableList.class,
              ImmutableList.class,
              String.class,
              String.class,
              Throwable.class);
    } catch (NoSuchMethodException e) {
      // That constructor exists.
      throw newLinkageError(e);
    }

    try {
      return constructor.newInstance(messages, facts, expected, actual, cause);
    } catch (InvocationTargetException e) {
      throwIfUnchecked(e.getCause());
      // That constructor has no `throws` clause.
      throw newLinkageError(e);
    } catch (InstantiationException e) {
      // The class is a concrete class.
      throw newLinkageError(e);
    } catch (IllegalAccessException e) {
      // We're accessing a class from within its package.
      throw newLinkageError(e);
    }
  }

  private static LinkageError newLinkageError(Throwable cause) {
    LinkageError error = new LinkageError(cause.toString());
    error.initCause(cause);
    return error;
  }
}
