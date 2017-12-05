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

import static com.google.common.truth.StringUtil.format;

import java.util.LinkedHashSet;
import java.util.Set;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

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
    return isInstanceOfTypeGWT(instance, clazz);
  }

  /**
   * Returns true if the instance is assignable to the type Clazz (though in GWT clazz can only be a
   * concrete class that is an ancestor class of the instance or the direct type of the instance.
   */
  static boolean isInstanceOfTypeGWT(Object instance, Class<?> clazz) {
    String className = clazz.getName();
    Set<String> types = new LinkedHashSet<String>();
    addTypeNames(instance.getClass(), types);
    return types.contains(className);
  }

  private static void addTypeNames(Class<?> clazz, Set<String> types) {
    for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
      types.add(current.getName());
      // addInterfaceNames(current.getInterfaces(), types);
    }
  }

  /** This is a no-op in GWT as it relies on matching. */
  static String compressType(String type) {
    return type;
  }

  static AssertionError comparisonFailure(
      String message, String expected, String actual, Throwable cause) {
    AssertionError failure =
        new AssertionError(format("%s: expected: %s actual: %s", message, expected, actual));
    failure.initCause(cause); // Not affected by Android bug
    return failure;
  }

  /** Determines if the given subject contains a match for the given regex. */
  static boolean containsMatch(String subject, String regex) {
    return compile(regex).test(subject);
  }

  /** Returns the length of an array. */
  static int getArrayLength(Object array) {
    if (array == null || !array.getClass().isArray()) {
      throw new IllegalArgumentException("not an array: " + array);
    }

    if (array.getClass() == boolean[].class) {
      return ((boolean[]) array).length;
    } else if (array.getClass() == int[].class) {
      return ((int[]) array).length;
    } else if (array.getClass() == long[].class) {
      return ((long[]) array).length;
    } else if (array.getClass() == short[].class) {
      return ((short[]) array).length;
    } else if (array.getClass() == byte[].class) {
      return ((byte[]) array).length;
    } else if (array.getClass() == double[].class) {
      return ((double[]) array).length;
    } else if (array.getClass() == float[].class) {
      return ((float[]) array).length;
    } else if (array.getClass() == char[].class) {
      return ((char[]) array).length;
    } else {
      return ((Object[]) array).length;
    }
  }

  /** Returns the item in the array at index i. */
  static Object getFromArray(Object array, int i) {
    if (array == null) {
      throw new NullPointerException("array is null");
    } else if (!array.getClass().isArray()) {
      throw new IllegalArgumentException("not an array: " + array);
    } else if (i < 0 || i >= getArrayLength(array)) {
      throw new ArrayIndexOutOfBoundsException(i);
    }

    if (array.getClass() == boolean[].class) {
      return ((boolean[]) array)[i];
    } else if (array.getClass() == int[].class) {
      return ((int[]) array)[i];
    } else if (array.getClass() == long[].class) {
      return ((long[]) array)[i];
    } else if (array.getClass() == short[].class) {
      return ((short[]) array)[i];
    } else if (array.getClass() == byte[].class) {
      return ((byte[]) array)[i];
    } else if (array.getClass() == double[].class) {
      return ((double[]) array)[i];
    } else if (array.getClass() == float[].class) {
      return ((float[]) array)[i];
    } else if (array.getClass() == char[].class) {
      return ((char[]) array)[i];
    } else {
      return ((Object[]) array)[i];
    }
  }

  /**
   * Returns an array containing all of the exceptions that were suppressed to deliver the given
   * exception. Delegates to the getSuppressed() method on Throwable that is available in Java 1.7+
   */
  static Throwable[] getSuppressed(Throwable throwable) {
    return throwable.getSuppressed();
  }

  /** Always returns false. Stack traces will be cleaned by default. */
  static boolean isStackTraceCleaningDisabled() {
    return false;
  }

  /** Returns a human readable string representation of the throwable's stack trace. */
  static String getStackTraceAsString(Throwable throwable) {
    // TODO(cpovirk): Write a naive implementation that at least dumps the main exception's stack.
    return throwable.toString();
  }

  /**
   * A GWT-swapped version of test rule interface that does nothing. All methods extended from
   * {@link org.junit.rules.TestRule} needs to be stripped.
   */
  interface JUnitTestRule {}

  // TODO(user): Move this logic to a common location.
  private static NativeRegExp compile(String pattern) {
    return new NativeRegExp(pattern);
  }

  @JsType(isNative = true, name = "RegExp", namespace = JsPackage.GLOBAL)
  private static class NativeRegExp {
    public NativeRegExp(String pattern) {}

    public native boolean test(String input);
  }
}
