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
package org.truth0.subjects;


import com.google.common.annotations.GwtCompatible;
import com.google.common.primitives.Booleans;

import org.truth0.FailureStrategy;

import java.util.Arrays;
import java.util.List;

/**
 * A Subject to handle testing propositions for {@code boolean[]}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@GwtCompatible
public class PrimitiveBooleanArraySubject extends AbstractArraySubject<boolean[]> {
  public PrimitiveBooleanArraySubject(FailureStrategy failureStrategy, boolean[] o) {
    super(failureStrategy, o);
  }

  @Override protected String underlyingType() {
    return "boolean";
  }

  @Override protected List<Boolean> listRepresentation() {
    return Booleans.asList(getSubject());
  }

  /**
   * A proposition that the provided Object[] is an array of the same length and type, and
   * contains elements such that each element in {@code expected} is equal to each element
   * in the subject, and in the same position.
   */
  @Override public void isEqualTo(Object expected) {
    boolean[] actual = getSubject();
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

  @Override public void isNotEqualTo(Object expected) {
    boolean[] actual = getSubject();
    try {
      boolean[] expectedArray = (boolean[]) expected;
      if (actual == expected || Arrays.equals(actual, expectedArray)) {
        failWithRawMessage("%s unexpectedly equal to %s.",
            getDisplaySubject(), Booleans.asList(expectedArray));
      }
    } catch (ClassCastException ignored) {}
  }

  public ListSubject<?, Boolean, List<Boolean>> asList() {
    return ListSubject.create(failureStrategy, listRepresentation());
  }

}
