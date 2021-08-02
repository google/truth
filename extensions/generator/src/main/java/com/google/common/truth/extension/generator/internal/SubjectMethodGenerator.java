package com.google.common.truth.extension.generator.internal;

import com.google.common.collect.Sets;
import com.google.common.flogger.FluentLogger;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.Subject;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.reflections.ReflectionUtils.*;

/**
 * @author Antony Stubbs
 */
// todo needs refactoring into different strategies, interface
public class SubjectMethodGenerator {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final Map<String, Class> compiledSubjects;
  private final Map<String, ThreeSystem> generatedSubjects;

  public SubjectMethodGenerator(final Set<ThreeSystem> allTypes) {
    this.generatedSubjects = allTypes.stream().collect(Collectors.toMap(x -> x.classUnderTest.getSimpleName(), x -> x));

    Reflections reflections = new Reflections("com.google.common.truth", "io.confluent");
    Set<Class<? extends Subject>> subTypes = reflections.getSubTypesOf(Subject.class);

    Map<String, Class> maps = new HashMap<>();
    subTypes.stream().forEach(x -> maps.put(x.getSimpleName(), x));
    this.compiledSubjects = maps;
  }


  public void addTests(JavaClassSource parent, Class<?> classUnderTest) {
    Collection<Method> getters = getMethods(classUnderTest);

    //
    for (Method method : getters) {
      addFieldAccessors(method, parent, classUnderTest);
    }
  }

  private Collection<Method> getMethods(final Class<?> classUnderTest) {
    Set<Method> getters = ReflectionUtils.getAllMethods(classUnderTest,
            withModifier(Modifier.PUBLIC), withPrefix("get"), withParametersCount(0));

    Set<Method> issers = ReflectionUtils.getAllMethods(classUnderTest,
            withModifier(Modifier.PUBLIC), withPrefix("is"), withParametersCount(0));

    getters.addAll(issers);

    return removeOverridden(getters);
  }

  private Collection<Method> removeOverridden(final Set<Method> getters) {
    Map<String, Method> result = new HashMap<>();
    for (Method getter : getters) {
      String sig = getSignature(getter);
      if (result.containsKey(sig)) {
        Method existing = result.get(sig);
        Class<?> existingDeclaringClass = existing.getDeclaringClass();
        Class<?> newDeclaringClass = getter.getDeclaringClass();

        if (existingDeclaringClass.isAssignableFrom(newDeclaringClass)) {
          // replace
          result.put(sig, getter);
        } else {
          // skip
        }
      } else {
        result.put(sig, getter);
      }
    }
    return result.values();
  }

  private String getSignature(final Method getter) {
    return getter.getName() + Arrays.stream(getter.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
  }

  /**
   * In priority order - most specific first
   */
  private final HashSet<Class<?>> nativeTypes = Sets.newHashSet(
          Map.class,
          Set.class,
          List.class,
          Iterable.class,
          Number.class,
          Throwable.class,
          BigDecimal.class,
          String.class,
          Comparable.class,
          Class.class // Enum#getDeclaringClass
  );

  private void addFieldAccessors(Method method, JavaClassSource generated, Class<?> classUnderTest) {
    Class<?> returnType = method.getReturnType();

    if(returnType.isAssignableFrom(Boolean.class)){
      addBooleanMethod();
    }

    boolean isCoveredByNonPrimitiveStandardSubjects = isTypeCoveredUnderStandardSubjects(returnType);

    Optional<ClassOrGenerated> subjectForType = getSubjectForType(returnType);

    // no subject to chain
    // todo needs two passes - one to generate the custom classes, then one to use them in other classes
    // should generate all base classes first, then run the test creator pass afterwards
    if (subjectForType.isEmpty() && !isCoveredByNonPrimitiveStandardSubjects) {
      logger.at(WARNING).log("Cant find subject for " + returnType);
      // todo log
      return;
    }

    ClassOrGenerated subjectClass = subjectForType.get();

    // todo add versions with and with the get
    MethodSource<JavaClassSource> has = generated.addMethod()
            .setName(createNameForSubjectMethod(method))
            .setPublic();

    StringBuilder body = new StringBuilder("isNotNull();\n");
    String check = format("return check(\"%s\")", createNameForSubjectMethod(method));
    body.append(check);

    boolean notPrimitive = !returnType.isPrimitive();
    boolean needsAboutCall = notPrimitive && !isCoveredByNonPrimitiveStandardSubjects;

    if (needsAboutCall || subjectClass.isGenerated()) {
      String aboutName;
      aboutName = Utils.getFactoryName(returnType); // take a guess
      body.append(format(".about(%s())", aboutName));

      // import
      String factoryContainer = subjectClass.getFactoryContainerName();
      Import anImport = generated.addImport(factoryContainer);
      String name = factoryContainer + "." + aboutName;
      anImport.setName(name) // todo better way to do static method import?
              .setStatic(true);
    }

//        String methodPrefix = (returnType.getSimpleName().contains("boolean")) ? "is" : "get";
//        body.append(".that(actual." + methodPrefix + capitalize(method.getName()) + "());");
    body.append(format(".that(actual.%s());", method.getName()));

    has.setBody(body.toString());

    has.setReturnType(subjectClass.getSubjectSimpleName());

    generated.addImport(subjectClass.getSubjectQualifiedName());
  }

  private String createNameForSubjectMethod(final Method method) {
    String name = method.getName();
    name = StringUtils.removeStart("get", name);
    return "has" + name;
  }

  private boolean isTypeCoveredUnderStandardSubjects(final Class<?> returnType) {
    // todo should only do this, if we can't find a more specific subect for the returnType
    // todo should check if class is assignable from the super subjects, instead of checking names
    // todo use qualified names
    // todo add support truth8 extensions - optional etc
    // todo try generatin classes for DateTime pakages, like Instant and Duration
    // todo this is of course too aggressive

//    boolean isCoveredByNonPrimitiveStandardSubjects = specials.contains(returnType.getSimpleName());
    boolean isCoveredByNonPrimitiveStandardSubjects = nativeTypes.stream().anyMatch(x -> x.isAssignableFrom(returnType));

    // todo is it an array of objects?
    boolean array = returnType.isArray();
    Class<?>[] classes = returnType.getClasses();
    String typeName = returnType.getTypeName();
    Class<?> componentType = returnType.getComponentType();

    return isCoveredByNonPrimitiveStandardSubjects || array;
  }

  private Optional<ClassOrGenerated> getSubjectForType(final Class<?> type) {
    String name;
    if (type.isPrimitive()) {
      Class<?> wrapped = ClassUtils.primitiveToWrapper(type);
      name = wrapped.getSimpleName();
    } else {
      name = type.getSimpleName();
    }
    Optional<ClassOrGenerated> subject = getSubjectFromString(name);

    if (subject.isEmpty()) {
      if (Iterable.class.isAssignableFrom(type)) {
        subject = getSubjectForType(Iterable.class);
      }
    }

    // fall back to native subjects
    if (subject.isEmpty()) {
      subject = ClassOrGenerated.ofClass(getNativeSubjectForType(type));
      if (subject.isPresent())
        logger.at(INFO).log("Falling back to native interface subject %s for type %s", subject.get().clazz, type);
    }

    return subject;
  }

  private Optional<Class<?>> getNativeSubjectForType(final Class<?> type) {
    Optional<Class<?>> first = nativeTypes.stream().filter(x -> x.isAssignableFrom(type)).findFirst();
    if (first.isPresent()) {
      Class<?> aClass = first.get();
      Class<?> compiledSubjectForTypeName = getCompiledSubjectForTypeName(aClass.getSimpleName());
      return ofNullable(compiledSubjectForTypeName);
    }
    return empty();
  }

  private Optional<ClassOrGenerated> getSubjectFromString(final String name) {
    if (name.endsWith("[]"))
      return Optional.of(new ClassOrGenerated(ObjectArraySubject.class, null));

    Optional<ThreeSystem> subjectFromGenerated = getSubjectFromGenerated(name);// takes precedence
    if (subjectFromGenerated.isPresent()) {
      return Optional.of(new ClassOrGenerated(null, subjectFromGenerated.get()));
    }

    Class<?> aClass = getCompiledSubjectForTypeName(name);
    if (aClass != null)
      return Optional.of(new ClassOrGenerated(aClass, null));

    return empty();
  }

  private Class<?> getCompiledSubjectForTypeName(final String name) {
    Class<?> aClass = this.compiledSubjects.get(name + "Subject");
    return aClass;
  }

  private Optional<ThreeSystem> getSubjectFromGenerated(final String name) {
    return Optional.ofNullable(this.generatedSubjects.get(name));
  }

  public void addTests(final Set<ThreeSystem> allTypes) {
    for (ThreeSystem c : allTypes) {
      addTests(c.parent.generated, c.classUnderTest);
    }

    // only serialise results, when all have finished - useful for debugging
    for (ThreeSystem c : allTypes) {
      Utils.writeToDisk(c.parent.generated);
    }
  }

  static class ClassOrGenerated {
    final Class<?> clazz;
    final ThreeSystem generated;

    ClassOrGenerated(final Class<?> clazz, final ThreeSystem generated) {
      this.clazz = clazz;
      this.generated = generated;
    }

    static Optional<ClassOrGenerated> ofClass(Optional<Class<?>> clazz) {
      if (clazz.isPresent())
        return Optional.of(new ClassOrGenerated(clazz.get(), null));
      else return empty();
    }

    String getSubjectSimpleName() {
      if (clazz == null)
        return this.generated.middle.generated.getName();
      else
        return clazz.getSimpleName();
    }

    String getSubjectQualifiedName() {
      if (clazz == null)
        return this.generated.middle.generated.getQualifiedName();
      else
        return clazz.getCanonicalName();
    }

    @Override
    public String toString() {
      return "ClassOrGenerated{" +
              "clazz=" + clazz +
              ", generated=" + generated +
              '}';
    }

    public boolean isGenerated() {
      return generated != null;
    }

    public String getFactoryContainerName() {
      return (isGenerated()) ? generated.middle.generated.getCanonicalName() : clazz.getCanonicalName();
    }
  }
}
