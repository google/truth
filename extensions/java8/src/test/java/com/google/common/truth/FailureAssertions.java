/*
 * Copyright (c) 2017 Google, Inc.
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

import static com.google.common.truth.ExpectFailure.assertThat;

/** Convenience methods for Java 8 Truth Subject tests, similar to BaseSubjectTestCase. */
final class FailureAssertions {
  static void assertFailureKeys(AssertionError e, String... keys) {
    assertThat(e).factKeys().containsExactlyElementsIn(keys).inOrder();
  }

  static void assertFailureValue(AssertionError e, String key, String value) {
    assertThat(e).factValue(key).isEqualTo(value);
  }

  static void assertFailureValueIndexed(AssertionError e, String key, int index, String value) {
    assertThat(e).factValue(key, index).isEqualTo(value);
  }

  private FailureAssertions() {}
}
