package org.truth0.codegen;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.truth0.subjects.Subject;
import org.truth0.subjects.SubjectFactory;
import org.truth0.util.ReflectionUtil;

/**
 * A builder of classes to wrap a provided SubjectFactory's concrete Subject subclass.
 * The generated class will be a direct subclass of the concrete Subject subclass, but
 * each public, protected, or friendly method not declared by Object will be wrapped
 * such that invocations on it will be invoked on a new Subject instance populated
 * with an element in the provided collection.  This allows for a type-safe, IDE-discoverable
 * Subject in a for-each style.
 */
public class IteratingWrapperClassBuilder {

  /**
   * <p>A string intended for use in String.format() representing the
   *    text of the code of the wrapper class.
   *
   * <p>Format parameters include:
   * <ol>
   *   <li>package name</li>
   *   <li>simple name of a concrete subtype of Subject</li>
   *   <li>the fully qualified name of the target type</li>
   *   <li>the text of the code of the wrapped methods</li>
   * </ol>
   * </p>
   */
  private static final String CLASS_TEMPLATE =
      "package %1$s;%n" +
      "%n" +
      "import org.truth0.FailureStrategy;%n" +
      "import org.truth0.subjects.SubjectFactory;%n" +
      "%n" +
      "public class %2$sIteratingWrapper extends %2$s {%n" +
      "%n" +
      "  private final SubjectFactory subjectFactory;%n" +
      "  private final Iterable<%3$s> data;%n" +
      "%n" +
      "  public %2$sIteratingWrapper(%n" +
      "      FailureStrategy failureStrategy,%n" +
      "      SubjectFactory<?, ?> subjectFactory,%n" +
      "      Iterable<%3$s> data%n" +
      "  ) {%n" +
      "    super(failureStrategy, (%3$s)null);%n" +
      "    this.subjectFactory = subjectFactory;%n" +
      "    this.data = data;%n" +
      "  }%n" +
      "%n" +
      "%4$s" +
      "}%n";

  /**
   * <p>A string intended for use in String.format() representing the
   *    text of the code of all wrapped methods.
   *
   * <p>Format parameters include:
   * <ol>
   *   <li>visibility</li>
   *   <li>fully qualified name of the return type</li>
   *   <li>method name</li>
   *   <li>method's parameter list</li>
   *   <li>the target type of the Subject</li>
   *   <li>concrete subtype of Subject to be wrapped</li>
   *   <li>parameter list</li>
   * </ol>
   * </p>
   */
  private static final String WRAPPER_METHOD_TEMPLATE =
      "  %1$s %2$s %3$s(%4$s) {%n" +
      "    for (%5$s item : data) {%n" +
      "      %6$s subject = (%6$s)subjectFactory.getSubject(failureStrategy, item);%n" +
      "      subject.%3$s(%7$s);%n" +
      "    }%n" +
      "  }%n";


  private static final int TARGET_TYPE_PARAMETER = 1;

  private static final String ITERATING_WRAPPER = "IteratingWrapper";

  private final SubjectFactory<?, ?> subjectFactory;

  public final String className;

  public IteratingWrapperClassBuilder(SubjectFactory<?,?> subjectFactory) {
    this.subjectFactory = subjectFactory;
    this.className = subjectFactory.getSubjectClass().getCanonicalName() + ITERATING_WRAPPER;
  }

  public String build() {
    Class<?> subjectClass = subjectFactory.getSubjectClass();
    List<Method> methods = Arrays.asList(subjectClass.getMethods());
    Class<?> targetType = ReflectionUtil.typeParameter(subjectClass, TARGET_TYPE_PARAMETER);

    StringBuilder methodWrappers = new StringBuilder();
    for (Method m : methods)  {
      appendMethodWrapper(methodWrappers, subjectClass, targetType, m);
    }
    String code = String.format(
        CLASS_TEMPLATE,
        subjectClass.getPackage().getName(),
        subjectClass.getSimpleName(),
        targetType.getCanonicalName(),
        methodWrappers.toString());

    return code;
  }

  private void appendMethodWrapper(
      StringBuilder code,
      Class<?> subjectType,
      Class<?> targetType,
      Method method) {
    int modifiers = method.getModifiers();
    boolean shouldWrap =
        !method.getDeclaringClass().equals(Subject.class) &&
        !method.getDeclaringClass().equals(Object.class) &&
        !(isFinal(modifiers) || isPrivate(modifiers) || isStatic(modifiers));

    if (shouldWrap) {
      code.append(String.format(
          WRAPPER_METHOD_TEMPLATE,
          stringVisibility(modifiers),
          method.getReturnType().getCanonicalName(),
          method.getName(),
          methodSignature(
              method.getParameterTypes(),
              method.getParameterAnnotations()).toString(),
          targetType.getCanonicalName(),
          subjectType.getCanonicalName(),
          methodParameterList(method.getParameterTypes().length)
          ));
    }
  }

  private static StringBuilder methodParameterList(int length) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; i++) {
      if (i > 0) builder.append(", ");
      builder.append("arg").append(0);
    }
    return builder;
  }

  /** Builds a string for the parameters within a method signature. */
  private static StringBuilder methodSignature(Class<?>[] parameters, Annotation[][] annotations) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0, iLen = parameters.length; i < iLen; i++) {
      if (i > 0) builder.append(", ");
      for (int j = 0, jLen = annotations[i].length; j < jLen; j++) {
        if (j > 0) builder.append(" ");
        builder.append("@").append(annotations[i][j].annotationType().getCanonicalName());
        builder.append(" ");
      }
      builder.append(parameters[i].getCanonicalName());
      builder.append(" arg").append(i);
    }
    return builder;
  }

  private static String stringVisibility(int modifiers) {
    if (Modifier.isProtected(modifiers)) {
      return "protected";
    } else if (Modifier.isPublic(modifiers)) {
      return "public";
    } else {
      return "";
    }
  }

}