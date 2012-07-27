package org.junit.contrib.truth;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.contrib.truth.subjects.Subject;
import org.junit.contrib.truth.subjects.SubjectFactory;
import org.junit.contrib.truth.util.CompilingClassLoader;
import org.junit.contrib.truth.util.CompilingClassLoader.CompilerException;
import org.junit.contrib.truth.util.ReflectionUtil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
/**
 * A verb that iterates over data and applies the predicate iteratively
 */
public  class IteratingVerb<T> extends AbstractVerb {

  private static final String ITERATING_WRAPPER = "IteratingWrapper";

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
    Class<?> subjectClass = subjectFactory.getSubjectClass();
    String wrapperClassName = subjectClass.getSimpleName() + ITERATING_WRAPPER;
    List<Method> methods = Arrays.asList(subjectClass.getMethods());
    String subjectName = subjectClass.getSimpleName();
    Class<?> targetType = ReflectionUtil.capture(subjectClass, 1);
    StringBuffer code = new StringBuffer();
    code.append("package ").append(subjectClass.getPackage().getName()).append(";\n\n");
    code.append("public class ").append(wrapperClassName).append(" ")
        .append("extends ").append(subjectName).append(" {\n");
    appendConstructor(wrapperClassName, targetType, code);
    for (Method m : methods)  { appendMethodWrapper(code, subjectClass, targetType, m); }
    code.append("}\n");
    String out = code.toString();
    ClassLoader classLoader;
    try {
      classLoader = new CompilingClassLoader(
          subjectClass.getClassLoader(), subjectClass.getName() + ITERATING_WRAPPER, out, null);
    } catch (CompilerException e) {
      throw new Error("Could not compile class " + wrapperClassName + " with source:\n" + out , e);
    }
    try {
      Class<?> wrapper = classLoader.loadClass(subjectClass.getName() + ITERATING_WRAPPER);
      return wrapper;
    } catch (ClassNotFoundException e) {
      throw new Error("Could not load class " + subjectName, e);
    }
  }

  private static void appendConstructor(String name, Class<?> targetType, StringBuffer code) {
    code.append("\n");
    code.append("  private final ").append(FailureStrategy.class.getName()).append(" fs;\n");
    code.append("  private final ").append(SubjectFactory.class.getName()).append(" sf;\n");
    code.append("  private final ").append(Iterable.class.getName())
        .append("<")
        .append(targetType.getName())
        .append(">")
        .append(" data;\n");
    code.append("  public ").append(name).append("(\n")
        .append("    ").append(FailureStrategy.class.getName()).append(" fs,\n")
        .append("    ").append(SubjectFactory.class.getName()).append(" sf,\n")
        .append("    ").append(Iterable.class.getName())
                       .append("<")
                       .append(targetType.getName())
                       .append(">")
                       .append(" data\n");
    code.append("  ) {\n");
    code.append("    super(fs, (").append(targetType.getName()).append(")null);\n");
    code.append("    this.fs = fs;\n");
    code.append("    this.sf = sf;\n");
    code.append("    this.data = data;\n");
    code.append("  }\n");
  }

  private static void appendMethodWrapper(
      StringBuffer code,
      Class<?> subjectType,
      Class<?> targetType,
      Method method) {
    boolean shouldWrap =
        !method.getDeclaringClass().equals(Subject.class) &&
        !method.getDeclaringClass().equals(Object.class) &&
        processModifiers(code, method.getModifiers()); // this must come last
    if (shouldWrap) {
      code.append(" ").append(method.getReturnType().getName().replace('$', '.'));
      code.append(" ").append(method.getName()).append("(");
      Class<?>[] parameters = method.getParameterTypes();
      Annotation[][] annotations = method.getParameterAnnotations();
      for (int i = 0, len = parameters.length; i < len ; i++ ) {
        if (i > 0) code.append(", ");
        for (Annotation a : annotations[i]) {
          code.append("@").append(a.annotationType().getName()).append(" ");
        }
        code.append(parameters[i].getName()).append(" arg").append(i);
      }
      code.append(") {\n");

      code.append("    for (").append(targetType.getName().replace('$', '.'))
          .append(" item : data) {\n");
      code.append("      ").append(subjectType.getName())
                           .append(" subject = (")
                           .append(subjectType.getName())
                           .append(")sf.getSubject(fs, item);\n");

      // execute subject
      code.append("      subject.").append(method.getName()).append("(");
      for (int i = 0, len = parameters.length; i < len ; i++ ) {
        if (i > 0) code.append(", ");
        code.append("arg").append(i);
      }
      code.append(");\n");

      code.append("    }\n");
      code.append("    return nextChain();\n");
      code.append("  }\n");
    }
  }

  private static boolean processModifiers(StringBuffer code, int modifiers) {
    if (Modifier.isFinal(modifiers) ||
        Modifier.isPrivate(modifiers) ||
        Modifier.isStatic(modifiers)) {
      return false;
    }

    if (Modifier.isProtected(modifiers)) {
      code.append("\n  public ");
    } else if (Modifier.isPublic(modifiers)) {
      code.append("\n  public ");
    }
    return true;
  }

}