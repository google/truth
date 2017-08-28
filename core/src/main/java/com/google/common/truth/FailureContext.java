/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nullable;

/**
 * An abstract type which holds a context message and formats it on demand.
 *
 * <p>{@code FailureMessageHolder} is a parent to those Verb types which will need to be modified
 * with failure message state.
 *
 * @deprecated If you are using this class to store and propagate the failure message as part of
 *     subclassing {@link AbstractVerb} or {@link TestVerb}, you will no longer need it when you
 *     migrate away from those classes, as described in their deprecation text.
 */
// TODO(cgruber) Extract supplementary message state from the Verb hierarchy entirely.
//     Requres lots of client fixes.
@Deprecated
public class FailureContext {
  private static final String PLACEHOLDER_ERR =
      "Incorrect number of args (%s) for the given placeholders (%s) in string template:\"%s\"";
  private final String format;
  private final Object[] args;

  public FailureContext(@Nullable String format, @Nullable Object... args) {
    this.format = format;
    this.args = args;
    int placeholders = countPlaceholders(format);
    checkArgument(placeholders == args.length, PLACEHOLDER_ERR, args.length, placeholders, format);
  }

  @Nullable
  protected String getFailureMessage() {
    return hasFailureMessage() ? StringUtil.format(format, args) : null;
  }

  protected boolean hasFailureMessage() {
    return format != null;
  }

  static int countPlaceholders(@Nullable String template) {
    if (template == null) {
      return 0;
    }
    int index = 0;
    int count = 0;
    while (true) {
      index = template.indexOf("%s", index);
      if (index == -1) {
        break;
      }
      index++;
      count++;
    }
    return count;
  }
}
