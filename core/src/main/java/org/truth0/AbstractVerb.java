package org.truth0;

import static org.truth0.util.StringUtil.format;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;

import org.truth0.subjects.Subject;
import org.truth0.subjects.SubjectFactory;

import javax.annotation.CheckReturnValue;

@GwtCompatible
public abstract class AbstractVerb<T extends AbstractVerb<T>> {
  private final FailureStrategy failureStrategy;

  public AbstractVerb(FailureStrategy failureStrategy) {
    this.failureStrategy = failureStrategy;
  }

  protected FailureStrategy getFailureStrategy() {
    return (getFailureMessage() != null)
        ? new MessageOverridingFailureStrategy(failureStrategy, getFailureMessage())
        : failureStrategy;
  }

  /**
   * Triggers the failure strategy with an empty failure message
   */
  public void fail() {
    getFailureStrategy().fail("");
  }

  /**
   * Triggers the failure strategy with the given failure message
   */
  public void fail(String format, Object... args) {
    getFailureStrategy().fail(format(format, args));
  }

  /**
   * Overrides the failure message of the subsequent subject's propositions.
   *
   * @see org.truth0.delegatetest.DelegationTest
   * @param factory a SubjectFactory<S, T> implementation
   * @returns A custom verb for the type returned by the SubjectFactory
   */
  @CheckReturnValue
  public abstract T withFailureMessage(String failureMessage);

  protected abstract String getFailureMessage();

  /**
   * The recommended method of extension of Truth to new types, which is
   * documented in {@link org.truth0.delegatetest.DelegationTest }.
   *
   * @see org.truth0.delegatetest.DelegationTest
   * @param factory
   *          a SubjectFactory<S, T> implementation
   * @returns A custom verb for the type returned by the SubjectFactory
   */
  public <S extends Subject<S, T>, T, SF extends SubjectFactory<S, T>> DelegatedVerb<S, T>
      about(SF factory) {
    return new DelegatedVerb<S, T>(getFailureStrategy(), factory);
  }

  /**
   * A special Verb implementation which wraps a SubjectFactory
   */
  public static class DelegatedVerb<S extends Subject<S, T>, T> {
    private final SubjectFactory<S, T> factory;
    private final FailureStrategy failureStrategy;

    public DelegatedVerb(FailureStrategy fs, SubjectFactory<S, T> factory) {
      this.factory = factory;
      this.failureStrategy = fs;
    }

    @CheckReturnValue
    public S that(T target) {
      return factory.getSubject(failureStrategy, target);
    }
  }

  @GwtIncompatible("org.truth0.IteratingVerb")
  public <T> IteratingVerb<T> in(Iterable<T> data) {
    return new IteratingVerb<T>(data, getFailureStrategy());
  }

  protected static class MessageOverridingFailureStrategy extends FailureStrategy {
    private final FailureStrategy delegate;
    private final String failureMessageOverride;

    protected MessageOverridingFailureStrategy(FailureStrategy delegate, String failureMessage) {
      this.delegate = delegate;
      this.failureMessageOverride = failureMessage;
    }

    @Override
    public void fail(String ignored) {
      delegate.fail(failureMessageOverride);
    }

    @Override
    public void fail(String message, Throwable cause) {
      delegate.fail(failureMessageOverride, cause);
    }

    @Override
    public void failComparing(String message, CharSequence ignore, CharSequence ignore2) {
      delegate.fail(failureMessageOverride);
    }
  }
}
