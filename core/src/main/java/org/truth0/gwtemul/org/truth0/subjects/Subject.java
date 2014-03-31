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

import java.util.Arrays;
import java.util.List;

import org.truth0.FailureStrategy;
import org.truth0.TestVerb;

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
    actual = ((actual == null) ? "null reference" : actual);
    String message = "Not true that <" + getDisplaySubject() + "> " + verb + " <" + expected + ">."
        + " It " + failVerb + " <" + actual + ">";
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
    actual = ((actual == null) ? "null reference" : actual);
    String message = "Not true that <" + actual + "> " + verb + " <" + expected + ">";
    failureStrategy.fail(message);
  }

  /**
   * Assembles a failure message wihtout a given subject and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   */
  protected void failWithoutSubject(String verb) {
    failureStrategy.fail("Not true that the subject " + verb);
  }

  /**
   * Passes through a failure message verbatim.  Used for {@link Subject} subclasses which
   * need to provide alternate language for more fit-to-purpose error messages.
   *
   * @param message the full message to be passed to the failure.
   */
  protected void failWithRawMessage(String message, Object ... parameters) {
    failureStrategy.fail(format(message, parameters));
  }

  private static String format(final String format, final Object... parameters) {
    // TODO(cgruber) Possibly trap %% items.
    List<String> parts = Arrays.asList(format.split("[%][s]", -1));
    if (parts.size() - 1 != parameters.length) {
      throw new IllegalArgumentException("Format string \""
          + format + "\" does not have " + parameters.length + " substitution variables.");
    }
    StringBuffer buffer = new StringBuffer(parts.get(0));
    for (int i = 0; i < parameters.length ; i++) {
      buffer.append(parameters[i]).append(parts.get(i+1));
    }
    return buffer.toString();
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
