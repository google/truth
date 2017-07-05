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

/**
 * A skeletal implementation of {@code FailureStrategy} that defines {@link #fail(String)} and both
 * {@code #failComparing} methods in terms of {@link #fail(String, Throwable)}. For most
 * implementations this should be sufficient and easier to use than directly extending {@code
 * FailureStrategy}.
 */
public abstract class AbstractFailureStrategy extends FailureStrategy {
  @Override
  public final void fail(String message) {
    fail(message, null);
  }

  @Override
  public final void failComparing(String message, CharSequence expected, CharSequence actual) {
    failComparing(message, expected, actual, null);
  }

  @Override
  public void failComparing(
      String message, CharSequence expected, CharSequence actual, Throwable cause) {
    // TODO(diamondm) should this be fail(Platform.comparisonFailure(...).getMessage());?
    // It unnecessarily allocates an exception, but ensures a consistent failure message.
    fail(StringUtil.messageFor(message, expected, actual), cause);
  }
}
