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

import com.google.common.annotations.GwtIncompatible;

/**
 * Propositions for string subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class StringSubject extends ComparableSubject<StringSubject, String> {

  public StringSubject(FailureStrategy failureStrategy, String string) {
    super(failureStrategy, string);
  }

  @Override protected String getDisplaySubject() {
    String name = (internalCustomName() == null) ? "" : "\"" + internalCustomName() + "\" ";
    return name + "<" + quote(getSubject()) + ">";
  }

  @Override public void is(Object expected) {
    isEqualTo(expected);
  }

  @Override public void isEqualTo(Object expected) {
    if (getSubject() == null) {
      if (expected != null) {
        if (expected instanceof String) {
          failWithRawMessage("Not true that null reference is equal to <%s>",
              quote((String) expected));
        } else {
          failWithRawMessage("Not true that null reference is equal to (%s)<%s>",
              expected.getClass().getName(), expected);
        }
      }
    } else {
      if (expected == null) {
        isNull();
      } else if (!(expected instanceof String)) {
        failWithRawMessage("Not true that %s is equal to (%s)<%s>",
            getDisplaySubject(), expected.getClass().getName(), expected);
      } else if (!getSubject().equals(expected)) {
        if (expected instanceof String) {
          failureStrategy.failComparing("", (String) expected, getSubject());
        } else {
          failWithRawMessage("Not true that %s equal to (%s)<%s>",
              getDisplaySubject(), expected.getClass().getName(), expected);
        }
      }
    }
  }

  /**
   * Fails if the string is not null.
   */
  @Override public void isNull() {
    if (getSubject() != null) {
      failWithRawMessage("Not true that %s is null", getDisplaySubject());
    }
  }

  /**
   * Fails if the string does not contain the given sequence.
   */
  public void contains(CharSequence string) {
    if (string == null) {
      throw new IllegalArgumentException("Cannot test that a string contains a null reference");
    }
    if (getSubject() == null) {
      failWithRawMessage("Not true that null reference contains <%s>", quote(string));
    } else if (!getSubject().contains(string)) {
      fail("contains", quote(string));
    }
  }

  /**
   * Fails if the string contains the given sequence.
   */
  public void doesNotContain(CharSequence string) {
    if (string == null) {
      throw new IllegalArgumentException(
              "Cannot test that a string does not contain a null reference");
    }
    if (getSubject() == null) {
      failWithRawMessage("Not true that null reference contains <%s>", quote(string));
    } else if (getSubject().contains(string)) {
      failWithRawMessage("%s unexpectedly contains <%s>", getDisplaySubject(), quote(string));
    }
  }

  /**
   * Fails if the string does not start with the given string.
   */
  public void startsWith(String string) {
    if (string == null) {
      throw new IllegalArgumentException("Cannot test that a string starts with a null reference");
    }
    if (getSubject() == null) {
      failWithRawMessage("Not true that null reference starts with <%s>", quote(string));
    } else if (!getSubject().startsWith(string)) {
      fail("starts with", quote(string));
    }
  }

  /**
   * Fails if the string does not end with the given string.
   */
  public void endsWith(String string) {
    if (string == null) {
      throw new IllegalArgumentException("Cannot test that a string ends with a null reference");
    }
    if (getSubject() == null) {
      failWithRawMessage("Not true that null reference ends with <%s>", quote(string));
    } else if (!getSubject().endsWith(string)) {
      fail("ends with", quote(string));
    }
  }

  /**
   * Fails if the string does not match the given regex.
   */
  @GwtIncompatible("java.util.regex.Pattern")
  public void matches(String regex) {
    if (!getSubject().matches(regex)) {
      fail("matches", regex);
    }
  }

  /**
   * Fails if the string does not match the given regex.
   */
  @GwtIncompatible("java.util.regex.Pattern")
  public void matches(java.util.regex.Pattern regex) {
    if(!regex.matcher(getSubject()).matches()) {
      fail("matches", regex);
    }
  }

  /**
   * Fails if the string matches the given regex.
   */
  @GwtIncompatible("java.util.regex.Pattern")
  public void doesNotMatch(String regex) {
    if (getSubject().matches(regex)) {
      fail("fails to match", regex);
    }
  }

  /**
   * Fails if the string matches the given regex.
   */
  @GwtIncompatible("java.util.regex.Pattern")
  public void doesNotMatch(java.util.regex.Pattern regex) {
    if(regex.matcher(getSubject()).matches()) {
      fail("fails to match", regex);
    }
  }

  /**
   * @deprecated Use a {@code for each} style loop over your {@code Iterable<Integer>} instead.
   */
  @Deprecated
  public static final SubjectFactory<StringSubject, String> STRING =
      new SubjectFactory<StringSubject, String>() {
        @Override public StringSubject getSubject(FailureStrategy fs, String target) {
          return new StringSubject(fs, target);
        }
      };

  private static String quote(CharSequence toBeWrapped) {
    return "\"" + toBeWrapped + "\"";
  }
}
