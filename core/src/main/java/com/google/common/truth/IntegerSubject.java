/*
 * Copyright (c) 2011 Google, Inc.
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

/**
 * Propositions for {@link Integer} subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
// Can't be final because we use codegen to generate a subclass
public class IntegerSubject extends ComparableSubject<IntegerSubject, Integer> {

  public IntegerSubject(FailureStrategy failureStrategy, Integer integer) {
    super(failureStrategy, integer);
  }

  public void isEqualTo(Object other) {
    super.isEqualTo(other);
  }

  public void isNotEqualTo(Object other) {
    super.isNotEqualTo(other);
  }

  public void isEqualTo(long other) {
    super.isEqualTo(Ints.saturatedCast(other));
  }

  public void is(long other) {
    isEqualTo(other);
  }

  public void is(short other) {
    isEqualTo(other);
  }

  public void is(byte other) {
    isEqualTo(other);
  }
}
