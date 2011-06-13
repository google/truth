package org.junit.contrib.truth;

import org.junit.contrib.truth.subjects.Subject;
import org.junit.contrib.truth.subjects.SubjectFactory;


public class AbstractVerb {

  private final FailureStrategy failureStrategy;

  public AbstractVerb(FailureStrategy failureStrategy) {
    this.failureStrategy = failureStrategy;
  }

  protected FailureStrategy getFailureStrategy() {
    return failureStrategy;
  }

  /**
   * The recommended method of extension of Truth to new types, which is 
   * documented in {@link DelegationTest }.  
   * 
   * @see DelegationTest
   * @param factory a SubjectFactory<S, T> implementation
   * @returns A custom verb for the type returned by the SubjectFactory
   */
  public <S extends Subject<S,T>, T, SF extends SubjectFactory<S, T>> 
      DelegatedVerb<S, T> about(SF factory) {
      return new DelegatedVerb<S, T>(getFailureStrategy(), factory);
  }
  
  /**
   * A special Verb implementation which wraps a SubjectFactory
   */
  public static class DelegatedVerb<S extends Subject<S,T>, T>
      extends AbstractVerb {
    
    private final SubjectFactory<S, T> factory;

    public DelegatedVerb(FailureStrategy fs, SubjectFactory<S, T> factory) {
      super(fs);
      this.factory = factory;
    }

    public S that(T target) {
      return factory.getSubject(getFailureStrategy(), target);
    }
  }
}
