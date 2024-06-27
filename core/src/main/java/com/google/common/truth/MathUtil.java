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

import static java.lang.Math.subtractExact;

import com.google.common.primitives.Doubles;
import org.jspecify.annotations.NullMarked;

/** Math utilities to be shared by numeric subjects. */
@NullMarked
final class MathUtil {
  private MathUtil() {}

  /**
   * Returns true iff {@code left} and {@code right} are values within {@code tolerance} of each
   * other.
   */
  /* package */ static boolean equalWithinTolerance(long left, long right, long tolerance) {
    try {
      // subtractExact is always desugared.
      @SuppressWarnings("Java7ApiChecker")
      long absDiff = Math.abs(subtractExact(left, right));
      return 0 <= absDiff && absDiff <= Math.abs(tolerance);
    } catch (ArithmeticException e) {
      // The numbers are so far apart their difference isn't even a long.
      return false;
    }
  }

  /**
   * Returns true iff {@code left} and {@code right} are values within {@code tolerance} of each
   * other.
   */
  /* package */ static boolean equalWithinTolerance(int left, int right, int tolerance) {
    try {
      // subtractExact is always desugared.
      @SuppressWarnings("Java7ApiChecker")
      int absDiff = Math.abs(subtractExact(left, right));
      return 0 <= absDiff && absDiff <= Math.abs(tolerance);
    } catch (ArithmeticException e) {
      // The numbers are so far apart their difference isn't even a int.
      return false;
    }
  }

  /**
   * Returns true iff {@code left} and {@code right} are finite values within {@code tolerance} of
   * each other. Note that both this method and {@link #notEqualWithinTolerance} returns false if
   * either {@code left} or {@code right} is infinite or NaN.
   */
  public static boolean equalWithinTolerance(double left, double right, double tolerance) {
    return Math.abs(left - right) <= Math.abs(tolerance);
  }

  /**
   * Returns true iff {@code left} and {@code right} are finite values within {@code tolerance} of
   * each other. Note that both this method and {@link #notEqualWithinTolerance} returns false if
   * either {@code left} or {@code right} is infinite or NaN.
   */
  public static boolean equalWithinTolerance(float left, float right, float tolerance) {
    return equalWithinTolerance((double) left, (double) right, (double) tolerance);
  }

  /**
   * Returns true iff {@code left} and {@code right} are finite values not within {@code tolerance}
   * of each other. Note that both this method and {@link #equalWithinTolerance} returns false if
   * either {@code left} or {@code right} is infinite or NaN.
   */
  public static boolean notEqualWithinTolerance(double left, double right, double tolerance) {
    if (Doubles.isFinite(left) && Doubles.isFinite(right)) {
      return Math.abs(left - right) > Math.abs(tolerance);
    } else {
      return false;
    }
  }

  /**
   * Returns true iff {@code left} and {@code right} are finite values not within {@code tolerance}
   * of each other. Note that both this method and {@link #equalWithinTolerance} returns false if
   * either {@code left} or {@code right} is infinite or NaN.
   */
  public static boolean notEqualWithinTolerance(float left, float right, float tolerance) {
    return notEqualWithinTolerance((double) left, (double) right, (double) tolerance);
  }
}
