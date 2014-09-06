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

import static com.google.common.truth.StringUtil.format;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Objects;

import java.lang.reflect.Field;

/**
 * Propositions for arbitrarily typed subjects and for properties
 * of Object
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class Subject<S extends Subject<S,T>,T> {
  protected final FailureStrategy failureStrategy;
  private final T subject;
  private String customName = null;

  public Subject(FailureStrategy failureStrategy, T subject) {
    this.failureStrategy = failureStrategy;
    this.subject = subject;
  }

  protected String internalCustomName() {
    return customName;
  }

  /**
   * Renames the subject so that this name appears in the error messages in place of string
   * representations of the subject.
   */
  @SuppressWarnings("unchecked")
  public S named(String name) {
    if (name == null) {
      // TODO: use check().withFailureMessage... here?
      throw new NullPointerException("Name passed to named() cannot be null.");
    }
    this.customName = name;
    return (S)this;
  }

  /**
   * Soft-deprecated in favor of {@link #named(String)}.
   */
  public S labeled(String label) {
    return named(label);
  }

  /**
   * Soft-deprecated in favor of {@link #isEqualTo(Object)}.
   */
  public void is(Object other) {
    isEqualTo(other);
  }

  public void isNull() {
    if (getSubject() != null) {
      fail("is null");
    }
  }

  public void isNotNull() {
    if (getSubject() == null) {
      failWithoutSubject("is a non-null reference");
    }
  }

  public void isEqualTo(Object other) {
    if (!Objects.equal(getSubject(), other)) {
      fail("is equal to", other);
    }
  }

  public void isNotEqualTo(Object other) {
    if (Objects.equal(getSubject(), other)) {
      fail("is not equal to", other);
    }
  }

  /**
   * Asserts that this object is an instance of the given class.
   */
  // TODO(user): @deprecated Use {@link #isInstanceOf(Class)} instead.
  public void isA(Class<?> clazz) {
    isInstanceOf(clazz);
  }

  /**
   * Asserts that this object is not an instance of the given class.
   *
   * @deprecated Use {@link #isNotInstanceOf(Class)} instead.
   */
  @Deprecated
  public void isNotA(Class<?> clazz) {
    isNotInstanceOf(clazz);
  }

  /**
   * Asserts that this object is an instance of the given class.
   */
  public void isInstanceOf(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException("clazz");
    }
    if (!Platform.isInstanceOfType(getSubject(), clazz)) {
      if (getSubject() != null) {
        failWithBadResults("is an instance of", clazz.getName(),
            "is an instance of", getSubject().getClass().getName());
      } else {
        fail("is an instance of", clazz.getName());
      }
    }
  }

  /**
   * Asserts that this object is not an instance of the given class.
   */
  public void isNotInstanceOf(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException("clazz");
    }
    if (getSubject() == null) {
      return; // null is not an instance of clazz.
    }
    if (Platform.isInstanceOfType(getSubject(), clazz)) {
      failWithRawMessage("%s expected not to be an instance of %s, but was.",
          getDisplaySubject(), clazz.getName());
    }
  }

  protected T getSubject() {
    return subject;
  }

  protected String getDisplaySubject() {
    String name = (customName == null) ? "" : "\"" + this.customName + "\" ";
    return name + "<" + getSubject() + ">";
  }

  /**
   * A convenience for implementers of {@link Subject} subclasses to use other truth
   * {@code Subject} wrappers within their own propositional logic.
   */
  protected TestVerb check() {
    return new TestVerb(failureStrategy);
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   *
   * @param verb the proposition being asserted
   */
  protected void fail(String verb) {
    failureStrategy.fail("Not true that " + getDisplaySubject() + " " + verb);
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy. Also performs
   * disambiguation if the subject and {@code part} have the same toString()'s.
   *
   * @param verb the proposition being asserted
   * @param part the value against which the subject is compared
   */
  protected void fail(String verb, Object part) {
    StringBuilder message = new StringBuilder("Not true that ")
        .append(getDisplaySubject()).append(" ");
    // If the subject and parts aren't null, and they have equal toString()'s but different
    // classes, we need to disambiguate them.
    boolean needsDisambiguation = (part != null) && (getSubject() != null)
        && getSubject().toString().equals(part.toString())
        && !getSubject().getClass().equals(part.getClass());
    if (needsDisambiguation) {
      message.append("(").append(getSubject().getClass().getName()).append(") ");
    }
    message.append(verb).append(" <").append(part).append(">");
    if (needsDisambiguation) {
      message.append(" (").append(part.getClass().getName()).append(")");
    }
    failureStrategy.fail(message.toString());
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param messageParts the expectations against which the subject is compared
   */
  protected void fail(String verb, Object... messageParts) {
    // For backwards binary compatibility
    if (messageParts.length == 0) {
      fail(verb);
    } else if (messageParts.length == 1) {
      fail(verb, messageParts[0]);
    } else {
      StringBuilder message = new StringBuilder("Not true that ");
      message.append(getDisplaySubject()).append(" ").append(verb);
      for (Object part : messageParts) {
        message.append(" <").append(part).append(">");
      }
      failureStrategy.fail(message.toString());
    }
  }

  /**
   * Assembles a failure message and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param messageParts the expectations against which the subject is compared
   */
  protected void failWithBadResults(String verb, Object expected, String failVerb, Object actual) {
    String message = format("Not true that %s %s <%s>. It %s <%s>",
            getDisplaySubject(),
            verb,
            expected,
            failVerb,
            ((actual == null) ? "null reference" : actual));
     failureStrategy.fail(message);
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
    String message = format("Not true that <%s> %s <%s>",
        ((actual == null) ? "null reference" : actual),
        verb,
        expected);
    failureStrategy.fail(message);
  }

  /**
   * Assembles a failure message without a given subject and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   */
  protected void failWithoutSubject(String verb) {
    String subject = this.customName == null ? "the subject" : "\"" + customName + "\"";
    failureStrategy.fail(format("Not true that %s %s", subject, verb));
  }

  /**
   * Passes through a failure message verbatim.  Used for {@link Subject} subclasses which
   * need to provide alternate language for more fit-to-purpose error messages.
   *
   *
   * @param message the message template to be passed to the failure.  Note, this method only
   *     guarantees to process {@code %s} tokens.  It is not guaranteed to be compatible
   *     with {@code String.format()}.  Any other formatting desired (such as floats or
   *     scientific notation) should be performed before the method call and the formatted
   *     value passed in as a string.
   * @param paramters the object parameters which will be applied to the message template.
   */
  protected void failWithRawMessage(String message, Object ... parameters) {
    failureStrategy.fail(format(message, parameters));
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
