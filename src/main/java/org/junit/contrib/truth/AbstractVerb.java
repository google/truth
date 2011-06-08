package org.junit.contrib.truth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.contrib.truth.subjects.Subject;


public class AbstractVerb {

  private final FailureStrategy failureStrategy;

  public AbstractVerb(FailureStrategy failureStrategy) {
    this.failureStrategy = failureStrategy;
  }

  protected FailureStrategy getFailureStrategy() {
    return failureStrategy;
  }

  public <S extends Subject<S,T>, T, SF extends DelegatedSubjectFactory<S, T>> 
      DelegatedSubjectFactory<S, T> _for(Class<SF> factoryClass) {
    try {
      Constructor<SF> c = factoryClass.getConstructor(FailureStrategy.class);
      DelegatedSubjectFactory<S, T> factory = c.newInstance(getFailureStrategy());
      return factory;
    } catch (InstantiationException e) {
      throw new RuntimeException("Could not instantiate "
          + factoryClass.getSimpleName(), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Could not obtain an accessible constructor "
          + factoryClass.getSimpleName(), e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Could not instantiate"
          + factoryClass.getSimpleName(), e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Could not obtain a constructor for "
          + factoryClass.getSimpleName(), e);
    }
  }
  
  public static abstract class DelegatedSubjectFactory<S extends Subject<S,T>, T>
      extends AbstractVerb {

    public DelegatedSubjectFactory(FailureStrategy failureStrategy) {
      super(failureStrategy);
    }

    public abstract S that(T that);
  }
}
