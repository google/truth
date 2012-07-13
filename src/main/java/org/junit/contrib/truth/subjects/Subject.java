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


import java.lang.reflect.Field;

import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.TestVerb;
import org.junit.contrib.truth.util.GwtCompatible;
import org.junit.contrib.truth.util.GwtIncompatible;

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
  private final And<S> chain;

  public Subject(FailureStrategy failureStrategy, T subject) {
    this.failureStrategy = failureStrategy;
    this.subject = subject;

    this.chain = new And<S>(){
      @SuppressWarnings("unchecked")
      @Override public S and() {
        return (S)Subject.this;
      }
    };
  }

  /**
   * A method which wraps the current Subject concrete
   * subtype in a chaining "And" object.
   */
  protected final And<S> nextChain() {
    return chain;
  }

  public And<S> is(T other) {

    if (getSubject() == null) {
      if(other != null) {
        fail("is", other);
      }
    } else {
      if (!getSubject().equals(other)) {
        fail("is", other);
      }
    }
    return nextChain();
  }

  public And<S> isNull() {
    if (getSubject() != null) {
      failWithoutSubject("is null");
    }
    return nextChain();
  }

  public And<S> isNotNull() {
    if (getSubject() == null) {
      failWithoutSubject("is not null");
    }
    return nextChain();
  }

  public And<S> isEqualTo(Object other) {
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

  public And<S> isNotEqualTo(Object other) {
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

  @GwtIncompatible("Class.isInstance")
  public And<S> isA(Class<?> clazz) {
    if (!clazz.isInstance(getSubject())) {
      fail("is a", clazz.getName());
    }
    return nextChain();
  }

  @GwtIncompatible("Class.isInstance")
  public And<S> isNotA(Class<?> clazz) {
    if (clazz.isInstance(getSubject())) {
      fail("is not a", clazz.getName());
    }
    return nextChain();
  }

  protected T getSubject() {
    return subject;
  }

  protected TestVerb check() {
    return new TestVerb(failureStrategy);
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

  public void hasFieldValue(String fieldName, Object expected) {
    hasField(fieldName);
    if (getSubject() == null) {
      failWithoutSubject("Not true that <null> contains expected value <" + expected + ">");
      return; // not all failures throw exceptions.
    }
    Class<?> clazz = getSubject().getClass();
    try {
      Field f = clazz.getField(fieldName);
      Object actual = f.get(getSubject());
      if (expected == actual || (expected != null && expected.equals(actual))) {
        return;
      } else {
        StringBuilder message = new StringBuilder("Not true that ");
        message.append("<").append(clazz.getSimpleName()).append(">'s");
        message.append(" field <").append(fieldName).append(">");
        message.append(" contains expected value <").append(expected).append(">.");
        message.append(" It contains value <").append(actual).append(">");
        failureStrategy.fail(message.toString());
      }
    } catch (NoSuchFieldException e) {
      // Do nothing - we'll break or pass along this error as part of the hasField() call.
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Could not access field " + fieldName + " on class "
          + clazz.getSimpleName(), e);
    }

  }

  public void hasField(String fieldName) {
    if (getSubject() == null) {
      failureStrategy.fail("Cannot determine a field name from a null object.");
      return; // not all failures throw exceptions.
    }
    check().that(getSubject().getClass()).hasField(fieldName);
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
