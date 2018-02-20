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

import static com.google.common.truth.Platform.ComparisonFailureMessageStrategy.INCLUDE_COMPARISON_FAILURE_GENERATED_MESSAGE;

import com.google.common.truth.Platform.PlatformComparisonFailure;

final class JUnitComparisonFailure extends PlatformComparisonFailure {
  JUnitComparisonFailure(
      String message, String expected, String actual, String suffix, Throwable cause) {
    super(message, expected, actual, suffix, cause, INCLUDE_COMPARISON_FAILURE_GENERATED_MESSAGE);
  }
}
