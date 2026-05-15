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

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static jsinterop.annotations.JsPackage.GLOBAL;

import com.google.common.base.Strings;
import java.util.List;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jspecify.annotations.Nullable;


/**
 * Extracted routines that need to be swapped in for GWT, to allow for minimal deltas between the
 * GWT and non-GWT version.
 */
final class Platform {
  private Platform() {}

  /** Returns true if the instance is assignable to the type Clazz. */
  static boolean isInstanceOfType(Object instance, Class<?> clazz) {
    if (clazz.isInterface()) {
      throw new UnsupportedOperationException(
          "On Web platforms, we can't determine whether an object is an instance of an interface"
              + " Class");
    }

    for (Class<?> current = instance.getClass();
        current != null;
        current = current.getSuperclass()) {
      if (current.equals(clazz)) {
        return true;
      }
    }
    return false;
  }

  /** Determines if the given actual value contains a match for the given regex. */
  static boolean containsMatch(String actual, String regex) {
    return compile(regex).test(actual);
  }

  /** Determines if the given actual value is fully matched by the given regex. */
  static boolean matches(String actual, String regex) {
    /*
     * When String.matches checks for a match, it will use a NativeRegExp to search for (roughly)
     * /^regex$/. But before that, we create a NativeRegExp inputs for just /regex/. That performs a
     * syntax check on the user's input, so the message from any syntax error will show the user's
     * input.
     */
    NativeRegExp unused = new NativeRegExp(regex);
    return actual.matches(regex);
  }

  static void cleanStackTrace(Throwable throwable) {
    // Do nothing. See notes in StackTraceCleanerTest.
  }

  static @Nullable String inferDescription() {
    return null;
  }

  static @Nullable List<Fact> makeDiff(String expected, String actual) {
    /*
     * IIUC, GWT messages lose their newlines by the time users see them. Given that, users are
     * likely better served by showing the expected and actual values with mangled newlines than by
     * showing a diff with mangled newlines (which would look similar but with + and - inserted into
     * it). Hopefully no one under GWT has long, nearly identical messages. In any case, they've
     * always been stuck like this.
     */
    return null;
  }

  static String doubleToString(double value) {
    // This probably doesn't match Java perfectly, but we do our best.
    if (value == Double.POSITIVE_INFINITY) {
      return "Infinity";
    } else if (value == Double.NEGATIVE_INFINITY) {
      return "-Infinity";
    } else if (value == 0 && 1 / value < 0) {
      return "-0.0";
    } else {
      // TODO(cpovirk): Would it make more sense to pass `undefined` for the locale? But how?
      // Then again, we're already hardcoding "Infinity," an English word, above....
      String result = toLocaleString(value);
      return (parseDouble(result) == value) ? result : Double.toString(value);
    }
  }

  static String floatToString(float value) {
    // This probably doesn't match Java perfectly, but we do our best.
    if (value == Float.POSITIVE_INFINITY) {
      return "Infinity";
    } else if (value == Float.NEGATIVE_INFINITY) {
      return "-Infinity";
    } else if (value == 0 && 1 / value < 0) {
      return "-0.0";
    } else if (value == 0) {
      return "0.0";
    } else {
      // TODO(cpovirk): Would it make more sense to pass `undefined` for the locale? But how?
      // Then again, we're already hardcoding "Infinity," an English word, above....
      String result = toLocaleString(value);
      return (parseFloat(result) == value) ? result : Float.toString(value);
    }
  }

  private static String toLocaleString(double value) {
    // Receive a double as a parameter so that "(Object) value" does not box it.
    return ((NativeNumber) (Object) value).toLocaleString("en-US", JavaLikeOptions.INSTANCE);
  }

  @JsType(isNative = true, namespace = "jspb")
  private static class Message {
    public native String serialize();
  }

  @JsMethod(namespace = "jspb.debug")
  private static native Object dump(Message msg) /*-{
    // Empty stub to make GWT happy. This will never get executed under GWT.
    throw new Error();
  }-*/;

  /**
   * Turns an object (typically an expected or actual value) into a string for use in a failure
   * message. Note that this method does not handle floating-point values the way we want on all
   * platforms, so some callers may wish to use {@link #doubleToString} or {@link #floatToString}
   * where appropriate.
   */
  static String stringValueForFailure(@Nullable Object o) {
    // Check if we are in J2CL mode by probing a system property that only exists in GWT.
    boolean inJ2clMode = "doesntexist".equals(System.getProperty("superdevmode", "doesntexist"));
    if (inJ2clMode && o instanceof Message) {
      Message msg = (Message) o;
      boolean dumpAvailable =
          "true".equals(System.getProperty("goog.DEBUG", "true"))
              && !"true".equals(System.getProperty("COMPILED", "false"));
      return dumpAvailable ? dump(msg).toString() : msg.serialize();
    }
    return String.valueOf(o);
  }

  /** Returns a human readable string representation of the throwable's stack trace. */
  static String getStackTraceAsString(Throwable throwable) {
    // TODO(cpovirk): Write a naive implementation that at least dumps the main exception's stack.
    return throwable.toString();
  }

  /**
   * A substitute for {@link org.junit.rules.TestRule} that contains no methods, since we can't
   * implement that type under GWT/J2CL.
   */
  interface JUnitTestRule {}

  static String expectFailureWarningIfWeb() {
    return " Note: One possible reason for a failure not to be caught is for the test to throw some"
        + " other exception before the failure would have happened. Under GWT, such an"
        + " exception is hidden by this message. The non-GWT tests do not have this problem,"
        + " so you may wish to debug them first. If you're still having this problem,"
        + " consider temporarily modifying the GWT copy of PlatformBaseSubjectTestCase to"
        + " remove the call to ensureFailureCaught(). Removing that call will let any other"
        + " exception fall through. (But of course it will also prevent the test from"
        + " verifying that the expected failure occurred.)";
  }

  static boolean forceInferDescription() {
    return false; // irrelevant because we can infer descriptions only under the JVM
  }

  // TODO(user): Move this logic to a common location.
  private static NativeRegExp compile(String pattern) {
    return new NativeRegExp(pattern);
  }

  @JsType(isNative = true, name = "RegExp", namespace = GLOBAL)
  private static class NativeRegExp {
    public NativeRegExp(@Nullable String pattern) {}

    public native boolean test(@Nullable String input);
  }

  @JsType(isNative = true, name = "Number", namespace = GLOBAL)
  private interface NativeNumber {
    String toLocaleString(Object locales, ToLocaleStringOptions options);
  }

  @JsType(isNative = true, name = "?", namespace = GLOBAL) // "structural type"; see JsType Javadoc
  private interface ToLocaleStringOptions {
    @JsProperty
    int getMinimumFractionDigits();

    @JsProperty
    int getMaximumFractionDigits();

    @JsProperty
    boolean getUseGrouping();
  }

  private static final class JavaLikeOptions implements ToLocaleStringOptions {
    private static final ToLocaleStringOptions INSTANCE = new JavaLikeOptions();

    @Override
    public int getMinimumFractionDigits() {
      return 1;
    }

    @Override
    public int getMaximumFractionDigits() {
      return 20;
    }

    @Override
    public boolean getUseGrouping() {
      return false;
    }
  }

  static AssertionError makeComparisonFailure(
      List<String> messages,
      List<Fact> facts,
      String unusedExpected,
      String unusedActual,
      @Nullable Throwable cause) {
    return AssertionErrorWithFacts.create(messages, facts, cause);
  }

  static boolean isKotlinRange(Iterable<?> iterable) {
    return false;
  }

  static boolean kotlinRangeContains(Iterable<?> haystack, @Nullable Object needle) {
    throw new AssertionError(); // never called under GWT because isKotlinRange returns false
  }

  static boolean classMetadataUnsupported() {
    return String.class.getSuperclass() == null;
  }

  static String lenientFormatForFailure(
      @Nullable String template, @Nullable Object @Nullable ... args) {
    return Strings.lenientFormat(template, args);
  }
}

