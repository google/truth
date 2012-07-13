package org.junit.contrib.truth;

import org.junit.contrib.truth.delegatetest.DelegationTest;
import org.junit.contrib.truth.subjects.Subject;
import org.junit.contrib.truth.subjects.SubjectFactory;
import org.junit.contrib.truth.util.GwtCompatible;

@GwtCompatible
public class AbstractVerb {

  private final FailureStrategy failureStrategy;

  public AbstractVerb(FailureStrategy failureStrategy) {
    this.failureStrategy = failureStrategy;
  }

  protected FailureStrategy getFailureStrategy() {
    return failureStrategy;
  }

	/**
	 * Triggers the failure strategy with an empty failure message
	 */
	public void fail() {
		failureStrategy.fail("");
	}

	/**
	 * Triggers the failure strategy with the given failure message
	 */
	public void fail(String message) {
		failureStrategy.fail(message);
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

  public <T> IterativeVerb<T> in(Iterable<T> data) {
    return new IterativeVerb<T>(data, getFailureStrategy());
  }

  /**
   * A verb that iterates over data and applies the predicate iteratively
   */
  public static class IterativeVerb<T>
      extends AbstractVerb {

    private final Iterable<T> data;

    public IterativeVerb(Iterable<T> data, FailureStrategy fs) {
      super(fs);
      this.data = data;
    }

    public <S extends Subject<S,T>, SF extends SubjectFactory<S, T>> S thatEach(SF factory) {
      // return wrapper around SubjectFactory that takes the call, but applies
      // it in turn to each item in the iterable.
      return factory.getSubject(getFailureStrategy(), data.iterator().next());
    }
  }

}
