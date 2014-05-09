/*
 * Copyright (c) 2014 Google, Inc.
 * Copyright (c) 2013, Square, Inc.
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
package org.truth0.subjects;

import com.google.common.annotations.GwtCompatible;

import org.truth0.FailureStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@GwtCompatible
public class ObjectArraySubject<T> extends Subject<ObjectArraySubject<T>, T[]> {

  private final String typeName;

  public ObjectArraySubject(FailureStrategy failureStrategy, T[] o) {
    super(failureStrategy, o);
    typeName = typeNameFromInstance(o);
  }

  private String typeNameFromInstance(Object instance) {
    if (instance == null) {
      return "null reference of unknown array type";
    } else {
      if (!instance.getClass().isArray()) {
        throw new IllegalArgumentException(instance.getClass().getName()
            + " instance passed into T[] subject.");
      }
      Class<?> type = instance.getClass().getComponentType();
      if (type.isPrimitive()) {
        throw new IllegalArgumentException("Primitive array passed into T[] subject.");
      }
      // TODO(cgruber): Improve the compression of arrays with generic types like Set<Foo>[]
      //     That will need extracting of all of the type information, or a string representation
      //     that compressType can handle.
      return compressType(type.toString()) + "[]";
    }
  }

  private static final Pattern TYPE_PATTERN = Pattern.compile("(?:[\\w$]+\\.)*([\\w\\.*$]+)");

  /**
   * Inspired by JavaWriter.
   */
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
    return (string.startsWith(prefix)) ? string.substring(prefix.length()) : string;
  }

  private static String stripIfInPackage(String type, String packagePrefix) {
    if (type.startsWith(packagePrefix)
        && (type.indexOf('.', packagePrefix.length()) == -1)
        && Character.isUpperCase(type.charAt(packagePrefix.length()))) {
      return type.substring(packagePrefix.length());
    }
    return type;
  }

  @Override protected String getDisplaySubject() {
    return "<(" + typeName + ") " + Arrays.asList(getSubject()).toString() + ">";
  }

  /**
   * A proposition that the provided Object[] is an array of the same length and type, and
   * contains elements such that each element in {@code expected} is equal to each element
   * in the subject, and in the same position.
   */
  @Override public void isEqualTo(Object expected) {
    Object[] actual = getSubject();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      Object[] expectedArray = (Object[]) expected;
      if (!Arrays.equals(actual, expectedArray)) {
        fail("is equal to", Arrays.asList(expectedArray));
      }
    } catch (ClassCastException e) {
      String expectedType = (expected.getClass().isArray())
          ? compressType(expected.getClass().getComponentType().toString()) + "[]"
          : compressType(expected.getClass().toString());
      failWithRawMessage(
          "Incompatible types compared. expected: %s, actual: %s", expectedType, typeName);
    }
  }

  @Override public void isNotEqualTo(Object expected) {
    Object[] actual = getSubject();
    try {
      Object[] expectedArray = (Object[]) expected;
      if (actual == expected || Arrays.equals(actual, expectedArray)) {
        failWithRawMessage("%s unexpectedly equal to %s.",
            getDisplaySubject(), Arrays.asList(expectedArray));
      }
    } catch (ClassCastException ignored) {}
  }

  public ListSubject<?, T, List<T>> asList() {
    return ListSubject.create(failureStrategy, Arrays.asList(getSubject()));
  }
}
