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

import javax.annotation.Nullable;

/**
 * Propositions for {@link Integer} subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 * @author Kurt Alfred Kluever
 */
public class IntegerSubject extends ComparableSubject<IntegerSubject, Integer> {
  // TODO(kak): Make this package-protected?
  public IntegerSubject(FailureStrategy failureStrategy, @Nullable Integer integer) {
    super(failureStrategy, integer);
  }

  /** @deprecated Use {@link #isEqualTo} instead. Integer comparison is consistent with equality. */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(Integer other) {
    super.isEquivalentAccordingToCompareTo(other);
  }
}
