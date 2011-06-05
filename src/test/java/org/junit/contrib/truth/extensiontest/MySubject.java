package org.junit.contrib.truth.extensiontest;

import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.subjects.Subject;

/**
 * A simple example Subject to demonstrate extension.
 * 
 * @author Christian Gruber (christianedwardgruber@gmail.com)
 */
public class MySubject extends Subject<MyType> {

  public MySubject(FailureStrategy failureStrategy, MyType subject) {
    super(failureStrategy, subject);
  }

  public Subject<MyType> matches(MyType object) {
    if (getSubject().value != object.value) {
      fail("matches", getSubject(), object);
    }
    return this;
  }

}
