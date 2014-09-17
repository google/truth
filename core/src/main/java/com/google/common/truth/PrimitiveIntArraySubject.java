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

import com.google.common.primitives.Ints;

import java.util.Arrays;
import java.util.List;

/**
 * A Subject to handle testing propositions for {@code int[]}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class PrimitiveIntArraySubject
    extends AbstractArraySubject<PrimitiveIntArraySubject, int[]> {

  PrimitiveIntArraySubject(FailureStrategy failureStrategy, int[] o) {
    super(failureStrategy, o);
  }

  @Override protected String underlyingType() {
    return "int";
  }

  @Override protected List<Integer> listRepresentation() {
    return Ints.asList(getSubject());
  }

  /**
   * A proposition that the provided Object[] is an array of the same length and type, and
   * contains elements such that each element in {@code expected} is equal to each element
   * in the subject, and in the same position.
   */
  @Override public void isEqualTo(Object expected) {
    int[] actual = getSubject();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      int[] expectedArray = (int[]) expected;
      if (!Arrays.equals(actual, expectedArray)) {
        fail("is equal to", Ints.asList(expectedArray));
      }
    } catch (ClassCastException e) {
      failWithBadType(expected);
    }
  }

  @Override public void isNotEqualTo(Object expected) {
    int[] actual = getSubject();
    try {
      int[] expectedArray = (int[]) expected;
      if (actual == expected || Arrays.equals(actual, expectedArray)) {
        failWithRawMessage("%s unexpectedly equal to %s.",
            getDisplaySubject(), Ints.asList(expectedArray));
      }
    } catch (ClassCastException ignored) {}
  }

  public ListSubject<?, Integer, List<Integer>> asList() {
    return ListSubject.create(failureStrategy, listRepresentation());
  }
}
