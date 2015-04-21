/*
 * Copyright (c) 2015 Google, Inc.
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

import java.math.BigDecimal;

import javax.annotation.Nullable;

/**
 * Propositions for {@link BigDecimal} typed subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class BigDecimalSubject extends ComparableSubject<BigDecimalSubject, BigDecimal> {

  BigDecimalSubject(FailureStrategy failureStrategy, @Nullable BigDecimal subject) {
    super(failureStrategy, subject);
  }

  /**
   * Fails if the subject's value is not equal to the value of the given {@link BigDecimal}.
   * (i.e., fails if {@code actual.comparesTo(expected) != 0}).
   *
   * <p><b>Note:</b> The scale of the BigDecimal is ignored.
   */
  public void isEqualToIgnoringScale(BigDecimal expected) {
    compareValues(expected);
  }

  /**
   * Fails if the subject's value is not equal to the value of the {@link BigDecimal} created from
   * the expected string (i.e., fails if {@code actual.comparesTo(new BigDecimal(expected)) != 0}).
   *
   * <p><b>Note:</b> The scale of the BigDecimal is ignored.
   */
  public void isEqualToIgnoringScale(String expected) {
    compareValues(new BigDecimal(expected));
  }

  /**
   * Fails if the subject's value is not equal to the value of the {@link BigDecimal} created from
   * the expected {@code long} (i.e., fails if
   * {@code actual.comparesTo(new BigDecimal(expected)) != 0}).
   *
   * <p><b>Note:</b> The scale of the BigDecimal is ignored.
   */
  public void isEqualToIgnoringScale(long expected) {
    compareValues(new BigDecimal(expected));
  }

  private void compareValues(BigDecimal expected) {
    if (getSubject().compareTo(expected) != 0) {
      failWithRawMessage("%s should have had the same value as <%s> (scale is ignored)",
          getDisplaySubject(), expected);
    }
  }
}
