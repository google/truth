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

/** {@link Correspondence} implementations for testing purposes. */
final class TestCorrespondences {
  /**
   * A correspondence between strings and integers which tests whether the string parses as the
   * integer. Parsing is as specified by {@link Integer#decode(String)}. It considers null to
   * correspond to null only.
   */
  static final Correspondence<String, Integer> STRING_PARSES_TO_INTEGER_CORRESPONDENCE =
      new Correspondence<String, Integer>() {

        @Override
        public boolean compare(@Nullable String actual, @Nullable Integer expected) {
          if (actual == null) {
            return expected == null;
          }
          try {
            // Older versions of Android reject leading plus signs, per the pre-Java-7 contract:
            // https://docs.oracle.com/javase/6/docs/api/java/lang/Integer.html#decode(java.lang.String)
            // https://docs.oracle.com/javase/7/docs/api/java/lang/Integer.html#decode(java.lang.String)
            if (actual.startsWith("+")) {
              actual = actual.substring(1);
            }
            return Integer.decode(actual).equals(expected);
          } catch (NumberFormatException e) {
            return false;
          }
        }

        @Override
        public String toString() {
          return "parses to";
        }
      };

  /**
   * A correspondence between integers which tests whether they are within 10 of each other. Smart
   * diffing is enabled, with a formatted diff showing the actual value less the expected value.
   * Does not support null values.
   */
  static final Correspondence<Integer, Integer> WITHIN_10_OF =
      new Correspondence<Integer, Integer>() {

        @Override
        public boolean compare(Integer actual, Integer expected) {
          return Math.abs(actual - expected) <= 10;
        }

        @Override
        public String formatDiff(Integer actual, Integer expected) {
          return Integer.toString(actual - expected);
        }

        @Override
        public String toString() {
          return "is within 10 of";
        }
      };

  private TestCorrespondences() {}
}
