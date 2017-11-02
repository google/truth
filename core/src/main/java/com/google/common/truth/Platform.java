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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.ComparisonFailure;

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
    return isInstanceOfTypeJava(instance, clazz);
  }

  /**
   * Returns true if the instance is assignable to the type Clazz (suitable for a JVM environment).
   */
  static boolean isInstanceOfTypeJava(Object instance, Class<?> clazz) {
    return clazz.isInstance(instance);
  }

  /**
   * Returns true if the instance is assignable to the type Clazz (suitable for a GWT environment).
   */
  static boolean isInstanceOfTypeGWT(Object instance, Class<?> clazz) {
    String className = clazz.getName();
    Set<String> types = new LinkedHashSet<String>();
    types.add(instance.getClass().getCanonicalName());
    addTypeNames(instance.getClass(), types);
    return types.contains(className);
  }

  private static void addInterfaceNames(Class<?>[] interfaces, Set<String> types) {
    for (Class<?> interfaze : interfaces) {
      types.add(interfaze.getName());
      addInterfaceNames(interfaze.getInterfaces(), types);
    }
  }

  private static void addTypeNames(Class<?> clazz, Set<String> types) {
    for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
      types.add(current.getName());
      addInterfaceNames(current.getInterfaces(), types);
    }
  }

  private static final Pattern TYPE_PATTERN = Pattern.compile("(?:[\\w$]+\\.)*([\\w\\.*$]+)");

  /** Inspired by JavaWriter. */
  static String compressType(String type) {
    type = typeOnly(type);
    StringBuilder sb = new StringBuilder();
    Matcher m = TYPE_PATTERN.matcher(type);
    int pos = 0;

    while (true) {
      boolean found = m.find(pos);
      // Copy non-matching characters like "<".
      int typeStart = found ? m.start() : type.length();
      sb.append(type, pos, typeStart);
      if (!found) {
        break;
      }
      // Copy a single class name, shortening it if possible.
      String name = m.group(0);
      name = stripIfInPackage(name, "java.lang.");
      name = stripIfInPackage(name, "java.util.");
      sb.append(name);

      pos = m.end();
    }
    return sb.toString();
  }

  private static String typeOnly(String type) {
    type = stripIfPrefixed(type, "class ");
    type = stripIfPrefixed(type, "interface ");
    return type;
  }

  private static String stripIfPrefixed(String string, String prefix) {
    return string.startsWith(prefix) ? string.substring(prefix.length()) : string;
  }

  private static String stripIfInPackage(String type, String packagePrefix) {
    if (type.startsWith(packagePrefix)
        && (type.indexOf('.', packagePrefix.length()) == -1)
        && Character.isUpperCase(type.charAt(packagePrefix.length()))) {
      return type.substring(packagePrefix.length());
    }
    return type;
  }

  static AssertionError comparisonFailure(String message, String expected, String actual) {
    return new ComparisonFailure(message, expected, actual);
  }

  /** Determines if the given subject contains a match for the given regex. */
  static boolean containsMatch(String actual, String regex) {
    return Pattern.compile(regex).matcher(actual).find();
  }

  /** Returns the length of an array. */
  static int getArrayLength(Object array) {
    return Array.getLength(array);
  }

  /** Returns the item in the array at index i. */
  static Object getFromArray(Object array, int i) {
    return Array.get(array, i);
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

  /**
   * Returns true if stack trace cleaning is explicitly disabled in a system property. This switch
   * os intended to be used when attempting to debug the frameworks which are collapsed or filtered
   * out of stack traces by the cleaner.
   */
  static boolean isStackTraceCleaningDisabled() {
    return Boolean.parseBoolean(
        System.getProperty("com.google.common.truth.disable_stack_trace_cleaning"));
  }
}
