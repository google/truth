/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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


import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;

import org.truth0.FailureStrategy;
import org.truth0.TestVerb;
import org.truth0.util.ReflectionUtil;

import java.lang.reflect.Field;

/**
 * Propositions for arbitrarily typed subjects and for properties
 * of Object
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@GwtCompatible(emulated = true)
public class Subject<S extends Subject<S,T>,T> {
  protected final FailureStrategy failureStrategy;
  private final T subject;

  public Subject(FailureStrategy failureStrategy, T subject) {
    this.failureStrategy = failureStrategy;
    this.subject = subject;
  }

  public void is(Object other) {
    isEqualTo(other);
  }

  public void isNull() {
    if (getSubject() != null) {
      failWithoutSubject("is null");
    }
  }

  public void isNotNull() {
    if (getSubject() == null) {
      failWithoutSubject("is not null");
    }
  }

  public void isEqualTo(Object other) {
    if (getSubject() == null) {
      if(other != null) {
        fail("is equal to", other);
      }
    } else {
      if (!getSubject().equals(other)) {
        fail("is equal to", other);
      }
    }
  }

  public void isNotEqualTo(Object other) {
    if (getSubject() == null) {
      if(other == null) {
        fail("is not equal to", (Object)null);
      }
    } else {
      if (getSubject().equals(other)) {
        fail("is not equal to", other);
      }
    }
  }

  @GwtIncompatible("Class.isInstance")
  public void isA(Class<?> clazz) {
    if (!clazz.isInstance(getSubject())) {
      fail("is a", clazz.getName());
    }
  }

  @GwtIncompatible("Class.isInstance")
  public void isNotA(Class<?> clazz) {
    if (clazz.isInstance(getSubject())) {
      fail("is not a", clazz.getName());
    }
  }

  protected T getSubject() {
    return subject;
  }

  protected T getDisplaySubject() {
    return getSubject();
  }

  protected TestVerb check() {
    return new TestVerb(failureStrategy);
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param messageParts the expectations against which the subject is compared
   */
  protected void fail(String verb, Object... messageParts) {
    StringBuilder message = new StringBuilder("Not true that ");
    message.append("<").append(getDisplaySubject()).append("> ").append(verb);
    for (Object part : messageParts) {
      message.append(" <").append(part).append(">");
    }
    failureStrategy.fail(message.toString());
  }

  /**
   * Assembles a failure message and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param messageParts the expectations against which the subject is compared
   */
  protected void failWithBadResults(String verb, Object expected, String failVerb, Object actual) {
    StringBuilder message = new StringBuilder("Not true that ");
    message.append("<").append(getDisplaySubject()).append("> ").append(verb);
    message.append(" <").append(expected).append(">");
    message.append(" it ").append(failVerb);
    message.append(" <").append(actual).append(">");
    failureStrategy.fail(message.toString());
  }

  /**
   * Assembles a failure message with an alternative representation of the wrapped subject
   * and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param expected the expected value of the proposition
   * @param actual the custom representation of the subject to be reported in the failure.
   */
  protected void failWithCustomSubject(String verb, Object expected, Object actual) {
    StringBuilder message = new StringBuilder("Not true that ");
    message.append((actual == null) ? "null reference" : actual).append(" ").append(verb);
    failureStrategy.fail(message.toString());
  }

  /**
   * Assembles a failure message wihtout a given subject and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   */
  protected void failWithoutSubject(String verb) {
    StringBuilder message = new StringBuilder("Not true that ");
    message.append("the subject ").append(verb);
    failureStrategy.fail(message.toString());
  }

  /**
   * Passes through a failure message verbatim.  Used for {@link Subject} subclasses which
   * need to provide alternate language for more fit-to-purpose error messages.
   *
   * @param message the full message to be passed to the failure.
   */
  protected void failWithRawMessage(String message, Object ... parameters) {
    failureStrategy.fail(String.format(message.toString(), parameters));
  }

  @GwtIncompatible("java.lang.reflect.Field")
  public HasField hasField(final String fieldName) {
    final T subject = getSubject();
    if (subject == null) {
      failureStrategy.fail("Cannot determine a field name from a null object.");
      // Needed for Expect and other non-terminal failure strategies
      return new HasField() {
        @Override public void withValue(Object value) {
          Subject.this.fail("Cannot test the presence of a value in a null object.");
        }
      };
    }
    final Class<?> subjectClass = subject.getClass();
    final Field field;
    try {
      field = ReflectionUtil.getField(subjectClass, fieldName);
      field.setAccessible(true);
    } catch (NoSuchFieldException e) {
      StringBuilder message = new StringBuilder("Not true that ");
      message.append("<").append(subjectClass.getSimpleName()).append(">");
      message.append(" has a field named <").append(fieldName).append(">");
      failureStrategy.fail(message.toString());

      // Needed for Expect and other non-terminal failure strategies
      return new HasField() {
        @Override public void withValue(Object value) {
          Subject.this.fail("Cannot test the presence of a value in a non-present field.");
        }
      };
    }
    return new HasField() {
      @Override public void withValue(Object expected) {
        try {
          Object actual = field.get(subject);
          if (expected == actual || (expected != null && expected.equals(actual))) {
            return;
          } else {
            StringBuilder message = new StringBuilder("Not true that ");
            message.append("<").append(subjectClass.getSimpleName()).append(">'s");
            message.append(" field <").append(fieldName).append(">");
            message.append(" contains expected value <").append(expected).append(">.");
            message.append(" It contains value <").append(actual).append(">");
            failureStrategy.fail(message.toString());
          }
        } catch (IllegalArgumentException e) {
          throw new RuntimeException(
              "Error checking field " + fieldName + " while testing for value " + expected);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(
              "Cannot access field " + fieldName + " to test for value " + expected);
        }
      }
    };
  }

  @GwtIncompatible("java.lang.reflect.Field")
  public static interface HasField {
    /**
     * Supplementary assertion in which a present field can be tested
     * to determine if it contains a given value.
     */
    void withValue(Object value);
  }

  /**
   * @deprecated This method is not a proposition, but the default Object equality method.
   *     Testing code should use "is" or "isEqualTo" propositions for equality tests.
   */
  @Deprecated
  @Override public boolean equals(Object o) {
    isEqualTo(o);
    return false;
  }

  /**
   * @deprecated Equals/Hashcode is not supported on Subjects. Their only use is as a holder of
   *     propositions. Use of equals() is deprecated and forwards to isEqualTo() and
   *     hashCode() is disallowed.
   */
  @Deprecated
  @Override public int hashCode() {
    throw new UnsupportedOperationException(""
        + "Equals/Hashcode is not supported on Subjects. Their only use is as a holder of "
        + "propositions. Use of equals() is deprecated and forwards to isEqualTo() and "
        + "hashCode() is disallowed.");
  }
}
