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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A Subject to handle testing propositions for {@code Object[]} and more generically {@code T[]}.
 *
 * @author Christian Gruber
 */
public class ObjectArraySubject<T> extends AbstractArraySubject<ObjectArraySubject<T>, T[]> {
  private final String typeName;

  ObjectArraySubject(FailureStrategy failureStrategy, @Nullable T[] o) {
    super(failureStrategy, o);
    typeName = typeNameFromInstance(o);
  }

  @Override
  protected String underlyingType() {
    return typeName;
  }

  @Override
  protected List<T> listRepresentation() {
    return Arrays.asList(getSubject());
  }

  private String typeNameFromInstance(Object instance) {
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
      // TODO(cgruber): Improve the compression of arrays with generic types like Set<Foo>[]
      //     That will need extracting of all of the type information, or a string representation
      //     that compressType can handle.
      return Platform.compressType(type.toString());
    }
  }

  /**
   * A proposition that the provided Object[] is an array of the same length and type, and
   * contains elements such that each element in {@code expected} is equal to each element
   * in the subject, and in the same position.
   */
  @Override
  public void isEqualTo(Object expected) {
    Object[] actual = getSubject();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      Object[] expectedArray = (Object[]) expected;
      if (actual.length != expectedArray.length) {
        failWithRawMessage(
            "%s has length %s. Expected length is %s",
            getDisplaySubject(),
            actual.length,
            expectedArray.length);
      } else {
        for (int i = 0; i < actual.length; i++) {
          if (!Objects.equals(actual[i], expectedArray[i])) {
            failWithBadResults("is equal to", Arrays.asList(expectedArray), "differs at index", i);
          }
        }
      }
    } catch (ClassCastException e) {
      failWithBadType(expected);
    }
  }

  @Override
  public void isNotEqualTo(Object expected) {
    Object[] actual = getSubject();
    try {
      Object[] expectedArray = (Object[]) expected;
      if (actual == expected || Arrays.equals(actual, expectedArray)) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", getDisplaySubject(), Arrays.asList(expectedArray));
      }
    } catch (ClassCastException ignored) {
    }
  }

  public IterableSubject asList() {
    return new IterableSubject(failureStrategy, listRepresentation());
  }
}
