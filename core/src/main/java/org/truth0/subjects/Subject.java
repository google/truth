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


import java.lang.reflect.Field;

import org.truth0.FailureStrategy;
import org.truth0.TestVerb;
import org.truth0.util.ReflectionUtil;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;

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

  public void is(T other) {

    if (getSubject() == null) {
      if(other != null) {
        fail("is", other);
      }
    } else {
      if (!getSubject().equals(other)) {
        fail("is", other);
      }
    }
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

  protected TestVerb check() {
    return new TestVerb(failureStrategy);
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   * @param verb the act being asserted
   * @param messageParts the expectations against which the subject is compared
   */
  protected void fail(String verb, Object... messageParts) {
    StringBuilder message = new StringBuilder("Not true that ");
    message.append("<").append(getSubject()).append("> ").append(verb);
    for (Object part : messageParts) {
      message.append(" <").append(part).append(">");
    }
    failureStrategy.fail(message.toString());
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   * @param verb the act being asserted
   * @param messageParts the expectations against which the subject is compared
   */
  protected void failWithBadResults(String verb, Object expected, String failVerb, Object actual) {
    StringBuilder message = new StringBuilder("Not true that ");
    message.append("<").append(getSubject()).append("> ").append(verb);
    message.append(" <").append(expected).append(">");
    message.append(" it ").append(failVerb);
    message.append(" <").append(actual).append(">");
    failureStrategy.fail(message.toString());
  }

  protected void failWithoutSubject(String verb) {
    StringBuilder message = new StringBuilder("Not true that ");
    message.append("the subject ").append(verb);
    failureStrategy.fail(message.toString());
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

}
