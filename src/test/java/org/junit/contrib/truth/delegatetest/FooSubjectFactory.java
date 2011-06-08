package org.junit.contrib.truth.delegatetest;

import org.junit.contrib.truth.AbstractVerb.DelegatedSubjectFactory;
import org.junit.contrib.truth.FailureStrategy;


public class FooSubjectFactory extends DelegatedSubjectFactory<FooSubject, Foo> {
  
  public FooSubjectFactory(FailureStrategy failureStrategy) {
    super(failureStrategy);
  }

  @Override public FooSubject that(Foo that) {
    return new FooSubject(getFailureStrategy(), that);
  }

}
