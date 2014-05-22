/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0.subjects;

import org.truth0.FailureStrategy;

/**
 * Propositions for String subjects
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class StringSubject extends Subject<StringSubject, String> {
  public StringSubject(FailureStrategy failureStrategy, String string) {
    super(failureStrategy, string);
  }

  @Override protected String getDisplaySubject() {
    return (internalCustomName() == null)
            ? "<" + quote(getSubject()) + ">"
            : "\"" + internalCustomName() + "\"";
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

  @Override public void isNull() {
    if (getSubject() != null) {
      failWithRawMessage("Not true that %s is null", getDisplaySubject());
    }
  }

  public void contains(String string) {
    if (string == null) {
      throw new IllegalArgumentException("Cannot test that a string contains a null reference.");
    }
    if (getSubject() == null) {
      failWithRawMessage("Not true that null reference contains <%s>", quote(string));
    } else if (!getSubject().contains(string)) {
      fail("contains", quote(string));
    }
  }

  public void doesNotContain(String string) {
    if (string == null) {
      throw new IllegalArgumentException(
              "Cannot test that a string does not contain a null reference.");
    }
    if (getSubject() == null) {
      failWithRawMessage("Not true that null reference contains <%s>", quote(string));
    } else if (getSubject().contains(string)) {
      failWithRawMessage("%s unexpectedly contains <%s>", getDisplaySubject(), quote(string));
    }
  }

  public void startsWith(String string) {
    if (string == null) {
      throw new IllegalArgumentException("Cannot test that a string starts with a null reference.");
    }
    if (getSubject() == null) {
      failWithRawMessage("Not true that null reference starts with <%s>", quote(string));
    } else if (!getSubject().startsWith(string)) {
      fail("starts with", quote(string));
    }
  }

  public void endsWith(String string) {
    if (string == null) {
      throw new IllegalArgumentException("Cannot test that a string ends with a null reference.");
    }
    if (getSubject() == null) {
      failWithRawMessage("Not true that null reference ends with <%s>", quote(string));
    } else if (!getSubject().endsWith(string)) {
      fail("ends with", quote(string));
    }
  }

  public static final SubjectFactory<StringSubject, String> STRING =
      new SubjectFactory<StringSubject, String>() {
        @Override public StringSubject getSubject(FailureStrategy fs, String target) {
          return new StringSubject(fs, target);
        }
      };

  private static String quote(String toBeWrapped) {
    return "\"" + toBeWrapped + "\"";
  }
}
