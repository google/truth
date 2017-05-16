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
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtIncompatible;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Propositions for string subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
// TODO(kak): Make this final
public class StringSubject extends ComparableSubject<StringSubject, String> {
  // TODO(kak): Make this package-private
  public StringSubject(FailureStrategy failureStrategy, @Nullable String string) {
    super(failureStrategy, string);
  }

  @Override
  protected String actualCustomStringRepresentation() {
    return quote(actual());
  }

  @Override
  public void isEqualTo(@Nullable Object expected) {
    if (actual() == null) {
      if (expected != null) {
        if (expected instanceof String) {
          failWithRawMessage(
              "Not true that " + actualAsString() + " is equal to <%s>", quote((String) expected));
        } else {
          failWithRawMessage(
              "Not true that " + actualAsString() + " is equal to (%s)<%s>",
              expected.getClass().getName(),
              expected);
        }
      }
    } else {
      if (expected == null) {
        isNull();
      } else if (!(expected instanceof String)) {
        failWithRawMessage(
            "Not true that %s is equal to (%s)<%s>",
            actualAsString(), expected.getClass().getName(), expected);
      } else if (!actual().equals(expected)) {
        if (internalCustomName() != null) {
          failureStrategy.failComparing(
              "\"" + internalCustomName() + "\":", (String) expected, actual());
        } else {
          failureStrategy.failComparing("", (String) expected, actual());
        }
      }
    }
  }

  /** @deprecated Use {@link #isEqualTo} instead. String comparison is consistent with equality. */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(String other) {
    super.isEquivalentAccordingToCompareTo(other);
  }

  /** Fails if the string is not null. */
  @Override
  public void isNull() {
    if (actual() != null) {
      failWithRawMessage("Not true that %s is null", actualAsString());
    }
  }

  /** Fails if the string does not have the given length. */
  public void hasLength(int expectedLength) {
    checkArgument(expectedLength >= 0, "expectedLength(%s) must be >= 0", expectedLength);
    int actualLength = actual().length();
    if (actualLength != expectedLength) {
      failWithRawMessage(
          "Not true that %s has a length of %s. It is %s.",
          actualAsString(), expectedLength, actualLength);
    }
  }

  /** Fails if the string is not equal to the zero-length "empty string." */
  public void isEmpty() {
    if (actual() == null) {
      failWithRawMessage("Not true that null reference is empty");
    } else if (!actual().isEmpty()) {
      fail("is empty");
    }
  }

  /** Fails if the string is equal to the zero-length "empty string." */
  public void isNotEmpty() {
    if (actual() == null) {
      failWithRawMessage("Not true that null reference is not empty");
    } else if (actual().isEmpty()) {
      fail("is not empty");
    }
  }

  /** Fails if the string does not contain the given sequence. */
  public void contains(CharSequence string) {
    checkNotNull(string);
    if (actual() == null) {
      failWithRawMessage("Not true that null reference contains <%s>", quote(string));
    } else if (!actual().contains(string)) {
      fail("contains", quote(string));
    }
  }

  /** Fails if the string contains the given sequence. */
  public void doesNotContain(CharSequence string) {
    checkNotNull(string);
    if (actual() == null) {
      failWithRawMessage("Not true that null reference contains <%s>", quote(string));
    } else if (actual().contains(string)) {
      failWithRawMessage("%s unexpectedly contains <%s>", actualAsString(), quote(string));
    }
  }

  /** Fails if the string does not start with the given string. */
  public void startsWith(String string) {
    checkNotNull(string);
    if (actual() == null) {
      failWithRawMessage("Not true that null reference starts with <%s>", quote(string));
    } else if (!actual().startsWith(string)) {
      fail("starts with", quote(string));
    }
  }

  /** Fails if the string does not end with the given string. */
  public void endsWith(String string) {
    checkNotNull(string);
    if (actual() == null) {
      failWithRawMessage("Not true that null reference ends with <%s>", quote(string));
    } else if (!actual().endsWith(string)) {
      fail("ends with", quote(string));
    }
  }

  /** Fails if the string does not match the given regex. */
  public void matches(String regex) {
    if (!actual().matches(regex)) {
      fail("matches", regex);
    }
  }

  /** Fails if the string does not match the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void matches(Pattern regex) {
    if (!regex.matcher(actual()).matches()) {
      fail("matches", regex);
    }
  }

  /** Fails if the string matches the given regex. */
  public void doesNotMatch(String regex) {
    if (actual().matches(regex)) {
      fail("fails to match", regex);
    }
  }

  /** Fails if the string matches the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void doesNotMatch(Pattern regex) {
    if (regex.matcher(actual()).matches()) {
      fail("fails to match", regex);
    }
  }

  /** Fails if the string does not contain a match on the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void containsMatch(Pattern pattern) {
    if (!pattern.matcher(actual()).find()) {
      failWithRawMessage("%s should have contained a match for <%s>", actualAsString(), pattern);
    }
  }

  /** Fails if the string does not contain a match on the given regex. */
  public void containsMatch(String regex) {
    if (!Platform.containsMatch(actual(), regex)) {
      failWithRawMessage("%s should have contained a match for <%s>", actualAsString(), regex);
    }
  }

  /** Fails if the string contains a match on the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void doesNotContainMatch(Pattern pattern) {
    if (pattern.matcher(actual()).find()) {
      failWithRawMessage(
          "%s should not have contained a match for <%s>", actualAsString(), pattern);
    }
  }

  /** Fails if the string contains a match on the given regex. */
  public void doesNotContainMatch(String regex) {
    if (Platform.containsMatch(actual(), regex)) {
      failWithRawMessage("%s should not have contained a match for <%s>", actualAsString(), regex);
    }
  }

  private static String quote(@Nullable CharSequence toBeWrapped) {
    return (toBeWrapped == null) ? "null" : "\"" + toBeWrapped + "\"";
  }
}
