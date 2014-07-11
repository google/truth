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
package org.truth0;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.TestVerb;
import com.google.common.truth.TruthJUnit;
import com.google.gwt.core.shared.GwtIncompatible;

/**
 * deprecated please use {@link com.google.common.truth.Truth#assert_()} and
 *     {@link TruthJUnit#assume()} to access these capabilities.
 */
//Deprecated
public class Truth {
  /** @deprecated prefer {@link com.google.common.truth.Truth#THROW_ASSERTION_ERROR}. */
  @Deprecated
  public static final FailureStrategy THROW_ASSERTION_ERROR =
          com.google.common.truth.Truth.THROW_ASSERTION_ERROR;

  /** @deprecated prefer {@link com.google.common.truth.TruthJUnit#THROW_ASSUMPTION_ERROR}. */
  @Deprecated
  @GwtIncompatible("JUnit4")
  public static final FailureStrategy THROW_ASSUMPTION_ERROR =
          com.google.common.truth.TruthJUnit.THROW_ASSUMPTION_ERROR;

  // TODO(cgruber): deprecated prefer {@link com.google.common.truth.Truth#assert_()}. */
  //Deprecated
  public static final TestVerb ASSERT = com.google.common.truth.Truth.assert_();

  // TODO(cgruber): deprecated prefer {@link com.google.common.truth.TruthJUnit#assume()}. */
  //Deprecated
  @GwtIncompatible("JUnit4")
  public static final TestVerb ASSUME = com.google.common.truth.TruthJUnit.assume();
}
