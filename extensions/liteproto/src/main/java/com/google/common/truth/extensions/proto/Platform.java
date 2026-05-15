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

package com.google.common.truth.extensions.proto;

import static com.google.common.base.Strings.lenientFormat;

import com.google.protobuf.MessageLite;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class Platform {

  private Platform() {}

  // It is wrong to compare protos using their string representations. The MessageLite runtime
  // deliberately prefixes debug strings with their Object.toString() to discourage string
  // comparison. However, this reads poorly in tests, and makes it harder to identify differences
  // from the strings alone. So, we manually strip this prefix.
  // In case the class names are actually relevant, Subject.isEqualTo() will add them back for us.
  // TODO(user): Maybe get a way to do this upstream.
  static String getTrimmedToString(@Nullable MessageLite messageLite) {
    String subjectString = String.valueOf(messageLite);
    String trimmedSubjectString = subjectString.trim();
    if (trimmedSubjectString.startsWith("# ")) {
      String objectToString =
          lenientFormat(
              "# %s@%s",
              messageLite.getClass().getName(), Integer.toHexString(messageLite.hashCode()));
      if (trimmedSubjectString.startsWith(objectToString)) {
        subjectString = trimmedSubjectString.replaceFirst(Pattern.quote(objectToString), "").trim();
      }
    }

    return subjectString.isEmpty() ? "[empty proto]" : subjectString;
  }
}
