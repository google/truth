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
package org.junit.contrib.truth.subjects;

import org.junit.contrib.truth.FailureStrategy;

/**
 * Propositions for arbitrarily typed subjects and for properties
 * of Object
 * 
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class Subject<T> {
  private final FailureStrategy failureStrategy;
  private final T subject;
  public Subject(FailureStrategy failureStrategy, T subject) {
    this.failureStrategy = failureStrategy;
    this.subject = subject;
  }
  
  public Subject<T> is(T other) {

    if (getSubject() == null) { 
      if(other != null) {
        fail("is", other);
      }
    } else {
      if (!getSubject().equals(other)) {
        fail("is", other);
      }
    }
    return this;
  }

  public Subject<T> isNull() {
    if (getSubject() != null) {
      failWithoutSubject("is null");
    }
    return this;
  }
  
  public Subject<T> isNotNull() {
    if (getSubject() == null) {
      failWithoutSubject("is not null");
    }
    return nextChain();
  }

  private Subject<T> nextChain() {
	return this;
  }

  public Subject<T> isEqualTo(Object other) {
    if (getSubject() == null) { 
      if(other != null) {
        fail("is equal to", other);
      }
    } else {
      if (!getSubject().equals(other)) {
        fail("is equal to", other);
      }
    }
    return nextChain();
  }

  public Subject<T> isNotEqualTo(Object other) {
    if (getSubject() == null) { 
      if(other == null) {
        fail("is not equal to", other);
      }
    } else {
      if (getSubject().equals(other)) {
        fail("is not equal to", other);
      }
    }
    return nextChain();
  }

  public Subject<T> isA(Class<?> clazz) {
    if (!clazz.isInstance(getSubject())) {
      fail("is a", clazz.getName());
    }
    return nextChain();
  }

  public Subject<T> isNotA(Class<?> clazz) {
    if (clazz.isInstance(getSubject())) {
      fail("is not a", clazz.getName());
    }
    return nextChain();
  }

  protected T getSubject() {
    return subject;
  }

  protected void fail(String verb, Object... messageParts) {
    String message = "Not true that ";
    message += "<" + getSubject() + "> " + verb;
    for (Object part : messageParts) {
      message += " <" + part + ">";
    }
    failureStrategy.fail(message);
  }

  protected void failWithoutSubject(String verb) {
    String message = "Not true that ";
    message += "the subject " + verb;
    failureStrategy.fail(message);
  }

  /**
   * A convenience class to allow for chaining in the fluent API
   * style, such that subjects can make propositions in series.  
   * i.e. ASSERT.that(blah).isNotNull().and().contains(b).and().isNotEmpty();
   */
  public static interface And<C> {
    /**
     * Returns the next object in the chain of anded objects.
     */
    C and();
  }  
}
