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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A Subject for {@code Object[]} and more generically {@code T[]}.
 *
 * @author Christian Gruber
 */
public final class ObjectArraySubject<T> extends AbstractArraySubject<ObjectArraySubject<T>, T[]> {
  private final String typeName;
  private final int numberOfDimensions;

  ObjectArraySubject(FailureMetadata metadata, @Nullable T[] o) {
    super(metadata, o);
    typeName = typeNameFromInstance(o);
    numberOfDimensions = numberOfDimensions(o);
  }

  @Override
  protected String underlyingType() {
    return typeName;
  }

  @Override
  String brackets() {
    return Strings.repeat("[]", numberOfDimensions);
  }

  @Override
  protected List<?> listRepresentation() {
    // Note: we don't use an ImmutableList or FluentIterable.toList
    // because some arrays have null, and ImmutableList doesn't allow null.
    return Lists.newArrayList(stringableIterable(actual()));
  }

  private static Iterable<?> stringableIterable(Object[] array) {
    return Iterables.transform(Arrays.asList(array), STRINGIFY);
  }

  private static final Function<Object, Object> STRINGIFY =
      new Function<Object, Object>() {
        @Override
        public Object apply(@Nullable Object input) {
          if (input != null && input.getClass().isArray()) {
            Iterable<?> iterable;
            if (input.getClass() == boolean[].class) {
              iterable = Booleans.asList((boolean[]) input);
            } else if (input.getClass() == int[].class) {
              iterable = Ints.asList((int[]) input);
            } else if (input.getClass() == long[].class) {
              iterable = Longs.asList((long[]) input);
            } else if (input.getClass() == short[].class) {
              iterable = Shorts.asList((short[]) input);
            } else if (input.getClass() == byte[].class) {
              iterable = Bytes.asList((byte[]) input);
            } else if (input.getClass() == double[].class) {
              iterable = Doubles.asList((double[]) input);
            } else if (input.getClass() == float[].class) {
              iterable = Floats.asList((float[]) input);
            } else if (input.getClass() == char[].class) {
              iterable = Chars.asList((char[]) input);
            } else {
              iterable = Arrays.asList((Object[]) input);
            }
            return Iterables.transform(iterable, STRINGIFY);
          }
          return input;
        }
      };

  private static String typeNameFromInstance(Object instance) {
    if (instance == null) {
      return "null reference of unknown array type";
    } else {
      if (!instance.getClass().isArray()) {
        throw new IllegalArgumentException(
            instance.getClass().getName() + " instance passed into T[] subject.");
      }
      Class<?> type = instance.getClass().getComponentType();
      if (type.isPrimitive()) {
        throw new IllegalArgumentException("Primitive array passed into T[] subject.");
      }
      while (type.isArray()) {
        type = type.getComponentType();
      }
      // TODO(cgruber): Improve the compression of arrays with generic types like Set<Foo>[]
      //     That will need extracting of all of the type information, or a string representation
      //     that compressType can handle.
      return Platform.compressType(type.toString());
    }
  }

  private static int numberOfDimensions(Object instance) {
    if (instance == null) {
      return 0;
    }
    Class<?> type = instance.getClass();
    int dimensions = 0;
    while (type.isArray()) {
      dimensions++;
      type = type.getComponentType();
    }
    return dimensions;
  }

  /**
   * A check that the provided Object[] is an array of the same length and type, and contains
   * elements such that each element in {@code expected} is equal to each element in the subject,
   * and in the same position.
   */
  @Override
  public void isEqualTo(Object expected) {
    Object[] actual = actual();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      Object[] expectedArray = (Object[]) expected;
      if (actual.length != expectedArray.length) {
        failWithRawMessage(
            "%s has length %s. Expected length is %s",
            actualAsString(), actual.length, expectedArray.length);
      } else {
        String index = checkArrayEqualsRecursive(expectedArray, actual, "");
        if (index != null) {
          failWithBadResults(
              "is equal to", stringableIterable(expectedArray), "differs at index", index);
        }
      }
    } catch (ClassCastException e) {
      failWithBadType(expected);
    }
  }

  /**
   * Returns null if the arrays are equal, recursively. If not equal, returns the string of the
   * index at which they're different.
   */
  @Nullable
  private String checkArrayEqualsRecursive(
      Object expectedArray, Object actualArray, String lastIndex) {
    int actualLength = Array.getLength(actualArray);
    int expectedLength = Array.getLength(expectedArray);
    for (int i = 0; i < actualLength || i < expectedLength; i++) {
      String index = lastIndex + "[" + i + "]";
      if (i < expectedLength && i < actualLength) {
        Object expected = Array.get(expectedArray, i);
        Object actual = Array.get(actualArray, i);
        if (actual != null
            && actual.getClass().isArray()
            && expected != null
            && expected.getClass().isArray()) {
          String result = checkArrayEqualsRecursive(expected, actual, index);
          if (result != null) {
            return result;
          }
          continue;
        } else if (Objects.equal(actual, expected)) {
          continue;
        }
      }
      return index;
    }
    return null;
  }

  @Override
  public void isNotEqualTo(Object expected) {
    Object[] actual = actual();
    try {
      Object[] expectedArray = (Object[]) expected;
      if (actual == expected || checkArrayEqualsRecursive(expectedArray, actual, "") == null) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", actualAsString(), stringableIterable(expectedArray));
      }
    } catch (ClassCastException ignored) {
      // If it's not Object[] then it's not equal and the test passes.
    }
  }

  public IterableSubject asList() {
    return internalCustomName() != null
        ? check().that(Arrays.asList(actual())).named(internalCustomName())
        : check().that(Arrays.asList(actual()));
  }
}
