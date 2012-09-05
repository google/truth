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

}
