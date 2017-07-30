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
  BigDecimalSubject(FailureStrategy failureStrategy, @Nullable BigDecimal actual) {
    super(failureStrategy, actual);
  }

  /**
   * Fails if the subject's value is not equal to the value of the given {@link BigDecimal}. (i.e.,
   * fails if {@code actual.comparesTo(expected) != 0}).
   *
   * <p><b>Note:</b> The scale of the BigDecimal is ignored. If you want to compare the values and
   * the scales, use {@link #isEqualTo(Object)}.
   */
  public void isEqualToIgnoringScale(BigDecimal expected) {
    compareValues(expected);
  }

  /**
   * Fails if the subject's value is not equal to the value of the {@link BigDecimal} created from
   * the expected string (i.e., fails if {@code actual.comparesTo(new BigDecimal(expected)) != 0}).
   *
   * <p><b>Note:</b> The scale of the BigDecimal is ignored. If you want to compare the values and
   * the scales, use {@link #isEqualTo(Object)}.
   */
  public void isEqualToIgnoringScale(String expected) {
    compareValues(new BigDecimal(expected));
  }

  /**
   * Fails if the subject's value is not equal to the value of the {@link BigDecimal} created from
   * the expected {@code long} (i.e., fails if {@code actual.comparesTo(new BigDecimal(expected)) !=
   * 0}).
   *
   * <p><b>Note:</b> The scale of the BigDecimal is ignored. If you want to compare the values and
   * the scales, use {@link #isEqualTo(Object)}.
   */
  public void isEqualToIgnoringScale(long expected) {
    compareValues(new BigDecimal(expected));
  }

  /**
   * Fails if the subject's value and scale is not equal to the given {@link BigDecimal}.
   *
   * <p><b>Note:</b> If you only want to compare the values of the BigDecimals and not their scales,
   * use {@link #isEqualToIgnoringScale(BigDecimal)} instead.
   */
  @Override // To express more specific javadoc
  public void isEqualTo(@Nullable Object expected) {
    super.isEqualTo(expected);
  }

  /**
   * Fails if the subject is not equivalent to the given value according to {@link
   * Comparable#compareTo}, (i.e., fails if {@code a.comparesTo(b) != 0}). This method behaves
   * identically to (the more clearly named) {@link #isEqualToIgnoringScale(BigDecimal)}.
   *
   * <p><b>Note:</b> Do not use this method for checking object equality. Instead, use {@link
   * #isEqualTo(Object)}.
   */
  @Override
  public void isEquivalentAccordingToCompareTo(BigDecimal expected) {
    compareValues(expected);
  }

  private void compareValues(BigDecimal expected) {
    if (actual().compareTo(expected) != 0) {
      failWithRawMessage(
          "%s should have had the same value as <%s> (scale is ignored)",
          actualAsString(), expected);
    }
  }
}
