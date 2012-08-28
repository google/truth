package org.truth0;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

import org.truth0.codegen.CompilingClassLoader;
import org.truth0.codegen.CompilingClassLoader.CompilerException;
import org.truth0.codegen.IteratingWrapperClassBuilder;
import org.truth0.subjects.Subject;
import org.truth0.subjects.SubjectFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
/**
 * A verb that iterates over data and applies the predicate iteratively
 */
public class IteratingVerb<T> extends AbstractVerb {

  private static final String CANNOT_WRAP_MSG = "Cannot build an iterating wrapper around ";

  private static LoadingCache<SubjectFactory<?,?>, Class<?>> WRAPPER_CACHE = CacheBuilder.newBuilder().build(
      new CacheLoader<SubjectFactory<?,?>, Class<?>>() {
        @Override public Class<?> load(SubjectFactory<?,?> subjectFactory) throws Exception {
          return compileWrapperClass(subjectFactory);
        }
      });

  private final Iterable<T> data;

  public IteratingVerb(Iterable<T> data, FailureStrategy fs) {
    super(fs);
    this.data = data;
  }

  public <S extends Subject<S,T>, SF extends SubjectFactory<S, T>> S thatEach(SF factory) {
    return wrap(getFailureStrategy(), factory, data);
  }

  private <S extends Subject<S,T>, SF extends SubjectFactory<S, T>>
      S wrap(FailureStrategy fs, SF factory, Iterable<T> data) {
    Type t = factory.getSubjectClass();
    Class<?> wrapperClass;
    try {
      wrapperClass = WRAPPER_CACHE.get(factory);
      return instantiate(wrapperClass, t, fs, factory, data);
    } catch (ExecutionException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    }
  }

  @SuppressWarnings("unchecked")
  private <SF, S> S instantiate(Class<?> wrapperType, Type t, FailureStrategy fs, SF factory,
      Iterable<T> data) {
    try {
      Constructor<S> c = (Constructor<S>)wrapperType.getConstructors()[0];
      return c.newInstance(fs, factory, data);
    } catch (SecurityException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    } catch (InstantiationException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    }
  }

  private static Class<?> compileWrapperClass(SubjectFactory<?, ?> subjectFactory) {
    IteratingWrapperClassBuilder builder = new IteratingWrapperClassBuilder(subjectFactory);
    String out = builder.build().toString();
    ClassLoader classLoader;
    try {
      classLoader = new CompilingClassLoader(
          subjectFactory.getSubjectClass().getClassLoader(), builder.className, out, null);
    } catch (CompilerException e) {
      throw new Error("Could not compile class " + builder.className + " with source:\n" + out , e);
    }
    try {
      Class<?> wrapper = classLoader.loadClass(builder.className);
      return wrapper;
    } catch (ClassNotFoundException e) {
      throw new Error("Could not load class " + subjectFactory.getSubjectClass().getSimpleName(), e);
    }
  }

}