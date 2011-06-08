package org.junit.contrib.truth.delegatetest;

import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.subjects.Subject;

/**
 * A simple example Subject to demonstrate extension.
 * 
 * @author Christian Gruber (christianedwardgruber@gmail.com)
 */
public class FooSubject extends Subject<FooSubject, Foo> {

  public FooSubject(FailureStrategy failureStrategy, Foo subject) {
    super(failureStrategy, subject);
  }

  public And<FooSubject> matches(Foo object) {
    if (getSubject().value != object.value) {
      fail("matches", getSubject(), object);
    }
    return nextChain();
  }

}
