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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.lenientFormat;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

final class LazyMessage {
  private static final String PLACEHOLDER_ERR =
      "Incorrect number of args (%s) for the given placeholders (%s) in string template:\"%s\"";

  private final String format;
  private final Object[] args;

  LazyMessage(@NullableDecl String format, @NullableDecl Object... args) {
    this.format = format;
    this.args = args;
    int placeholders = countPlaceholders(format);
    checkArgument(placeholders == args.length, PLACEHOLDER_ERR, args.length, placeholders, format);
  }

  @Override
  public String toString() {
    return lenientFormat(format, args);
  }

  @VisibleForTesting
  static int countPlaceholders(@NullableDecl String template) {
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

  static ImmutableList<String> evaluateAll(ImmutableList<LazyMessage> messages) {
    ImmutableList.Builder<String> result = ImmutableList.builder();
    for (LazyMessage message : messages) {
      result.add(message.toString());
    }
    return result.build();
  }
}
