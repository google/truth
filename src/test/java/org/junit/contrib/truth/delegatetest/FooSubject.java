package org.junit.contrib.truth.delegatetest;

import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.subjects.Subject;

/**
 * A simple example Subject to demonstrate extension.
 * 
 * @author Christian Gruber (christianedwardgruber@gmail.com)
 */
public class FooSubject extends Subject<Foo> {
  
  public static final Class<FooSubject> FOO = FooSubject.class;

  public FooSubject(FailureStrategy failureStrategy, Foo subject) {
    super(failureStrategy, subject);
  }

  public Subject<Foo> matches(Foo object) {
    if (getSubject().value != object.value) {
      fail("matches", getSubject(), object);
    }
    return this;
  }

}
