package org.junit.contrib.truth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.contrib.truth.subjects.Subject;
import org.junit.contrib.truth.subjects.SubjectFactory;
import org.junit.contrib.truth.util.CompilingClassLoader;
import org.junit.contrib.truth.util.CompilingClassLoader.CompilerException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
/**
 * A verb that iterates over data and applies the predicate iteratively
 */
public  class IteratingVerb<T> extends AbstractVerb {

  private static final String CANNOT_WRAP_MSG = "Cannot build an iterating wrapper around ";

  private static LoadingCache<Class<?>, Class<?>> WRAPPER_CACHE = CacheBuilder.newBuilder().build(
      new CacheLoader<Class<?>, Class<?>>() {
        @Override public Class<?> load(Class<?> subjectClass) throws Exception {
          return compileWrapperClass(subjectClass);
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
      wrapperClass = WRAPPER_CACHE.get(factory.getSubjectClass());
      return instantiate(wrapperClass, t, fs, factory, data);
    } catch (ExecutionException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    }
  }

  @SuppressWarnings("unchecked")
  private <SF, S> S instantiate(Class<?> wrapperType, Type t, FailureStrategy fs, SF factory,
      Iterable<T> data) {
    try {
      return (S) wrapperType.cast(wrapperType.getConstructors()[0].newInstance(fs, factory, data));
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

  private static Class<?> compileWrapperClass(Class<?> subjectClass) {
    List<Method> methods = Arrays.asList(subjectClass.getDeclaredMethods());
    String subjectName = subjectClass.getSimpleName();
    StringBuffer code = new StringBuffer();
    code.append("package ").append(subjectClass.getPackage().getName()).append("\n\n");
    code.append("public class ").append(subjectName).append("IteratingWrapper ")
        .append("extends ").append(subjectName).append(" {\n");

    code.append("}\n");

    String out = code.toString();
    // System.out.println(out);
    ClassLoader classLoader;
    try {
      classLoader = new CompilingClassLoader(
          subjectClass.getClassLoader(), subjectClass.getName(), out, null);
    } catch (CompilerException e) {
      throw new Error("Could not compile class " + subjectName + " with source:\n" + out , e);
    }
    try {
      return classLoader.loadClass(subjectName);
    } catch (ClassNotFoundException e) {
      throw new Error("Could not load class " + subjectName, e);
    }
  }


}