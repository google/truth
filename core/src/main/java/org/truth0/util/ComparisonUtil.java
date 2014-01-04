/*
 * Copyright (c) 2013 Google
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
package org.truth0.util;


/**
 * Utilities for string comparisons.
 *
 * @author Christian Gruber (cgruber@google.com)
 */
public final class ComparisonUtil {
  private ComparisonUtil() {}

  /**
   * Returns a message appropriate for string comparisons.
   *
   * TODO(cgruber): Do something closer to what JUnit's {@code ComparisonFailure} does.
   */
  public static String messageFor(String message, CharSequence expected, CharSequence actual) {
    return message + "\n\nExpected:\n" + expected + "\n\nActual\n" + actual;
  }
}
