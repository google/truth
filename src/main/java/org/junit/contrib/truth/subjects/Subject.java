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

public class Subject<S extends Subject<S,T>,T> {
  
  private final FailureStrategy failureStrategy;
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
    if (!(getSubject() == other)) {
      fail("is", other);
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
