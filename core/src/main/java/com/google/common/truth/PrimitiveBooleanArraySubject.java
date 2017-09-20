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

import com.google.common.primitives.Booleans;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A Subject to handle testing propositions for {@code boolean[]}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public final class PrimitiveBooleanArraySubject
    extends AbstractArraySubject<PrimitiveBooleanArraySubject, boolean[]> {
  PrimitiveBooleanArraySubject(FailureMetadata metadata, @Nullable boolean[] o) {
    super(metadata, o);
  }

  @Override
  protected String underlyingType() {
    return "boolean";
  }

  @Override
  protected List<Boolean> listRepresentation() {
    return Booleans.asList(actual());
  }

  /**
   * A proposition that the actual array and {@code expected} are arrays of the same length and
   * type, containing elements such that each element in {@code expected} is equal to each element
   * in the actual array, and in the same position.
   */
  @Override
  public void isEqualTo(Object expected) {
    boolean[] actual = actual();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      boolean[] expectedArray = (boolean[]) expected;
      if (!Arrays.equals(actual, expectedArray)) {
        fail("is equal to", Booleans.asList(expectedArray));
      }
    } catch (ClassCastException e) {
      failWithBadType(expected);
    }
  }

  /**
   * A proposition that the actual array and {@code expected} are not arrays of the same length and
   * type, containing elements such that each element in {@code expected} is equal to each element
   * in the actual array, and in the same position.
   */
  @Override
  public void isNotEqualTo(Object expected) {
    boolean[] actual = actual();
    try {
      boolean[] expectedArray = (boolean[]) expected;
      if (actual == expected || Arrays.equals(actual, expectedArray)) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", actualAsString(), Booleans.asList(expectedArray));
      }
    } catch (ClassCastException ignored) {
      // If it's not boolean[] then it's not equal and the test passes.
    }
  }

  public IterableSubject asList() {
    return internalCustomName() != null
        ? check().that(listRepresentation()).named(internalCustomName())
        : check().that(listRepresentation());
  }
}
