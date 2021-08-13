package com.google.common.truth.extension.generator.internal;

import com.google.common.flogger.FluentLogger;
import com.google.common.truth.*;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.extension.generator.internal.ClassUtils.getStrippedReturnTypeFirstGenericParam;
import static com.google.common.truth.extension.generator.internal.ClassUtils.maybeGetSimpleName;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.*;
import static java.util.Optional.*;
import static java.util.function.Predicate.not;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;
import static org.apache.commons.lang3.StringUtils.*;
import static org.reflections.ReflectionUtils.*;

/**
 * @author Antony Stubbs
 */
// todo needs refactoring into different strategies, interface
public class SubjectMethodGenerator {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final Map<String, Class<?>> classPathSubjectTypes = new HashMap<>();
  private final Map<String, ThreeSystem> generatedSubjects;
  private ThreeSystem context;

  public SubjectMethodGenerator(final Set<ThreeSystem> allTypes) {
    this.generatedSubjects = allTypes.stream().collect(Collectors.toMap(x -> x.classUnderTest.getName(), x -> x));

    Reflections reflections = new Reflections("com.google.common.truth", "io.confluent");
    Set<Class<? extends Subject>> subjectTypes = reflections.getSubTypesOf(Subject.class);

    subjectTypes.forEach(x -> classPathSubjectTypes.put(x.getSimpleName(), x));
  }

  public void addTests(ThreeSystem system) {
    this.context = system;

    //
    Class<?> classUnderTest = system.getClassUnderTest();
    JavaClassSource generated = system.getParent().getGenerated();

    //
    {
      Collection<Method> getters = getMethods(system)
              // output a consistent ordering - alphabetical my method name
              .stream().sorted((o1, o2) -> Comparator.comparing(Method::getName).compare(o1, o2))
              .collect(Collectors.toList());
      for (Method method : getters) {
        addFieldAccessors(method, generated, classUnderTest);
      }
    }

    //
    {
      Collection<Method> toers = getMethods(classUnderTest, input -> {
        if (input == null) return false;
        String name = input.getName();
        // exclude lombok builder methods
        return startsWith(name, "to") && !endsWith(name, "Builder");
      });
      toers = removeOverridden(toers);
      for (Method method : toers) {
        addChainStrategy(method, generated, method.getReturnType());
      }
    }
  }

  private <T extends Member> Predicate<T> withSuffix(String suffix) {
    return input -> input != null && input.getName().startsWith(suffix);
  }

  private Collection<Method> getMethods(ThreeSystem system) {
    Class<?> classUnderTest = system.getClassUnderTest();
    boolean legacyMode = system.isLegacyMode();

    Set<Method> union = new HashSet<>();
    Set<Method> getters = getMethods(classUnderTest, withPrefix("get"));
    Set<Method> issers = getMethods(classUnderTest, withPrefix("is"));

    // also get all other methods, regardless of their prefix
    Predicate<Method> expectSetters = not(withPrefix("set"));
    Set<Method> legacy = (legacyMode) ? getMethods(classUnderTest, expectSetters) : Set.of();

    union.addAll(getters);
    union.addAll(issers);
    union.addAll(legacy);

    return removeOverridden(union);
  }

  private Set<Method> getMethods(Class<?> classUnderTest, Predicate<Method> prefix) {
    // if shaded, can't access package private methods
    boolean isShaded = context.isShaded();
    Predicate skip = (ignore) -> true;
    Predicate shadedPredicate = (isShaded) ? withModifier(PUBLIC) : skip;

    return ReflectionUtils.getAllMethods(classUnderTest,
            not(withModifier(PRIVATE)), not(withModifier(PROTECTED)), shadedPredicate, prefix, withParametersCount(0));
  }

  private Collection<Method> removeOverridden(final Collection<Method> getters) {
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
   * In priority order - most specific first. Types that are native to {@link Truth} - i.e. you can call {@link
   * Truth#assertThat}(...) with it. Note that this does not include {@link Truth8} types.
   */
  @Getter
  private static final HashSet<Class<?>> nativeTypes = new LinkedHashSet();

  @Getter
  private static final HashSet<Class<?>> nativeTypesTruth8 = new LinkedHashSet();

  static {
    //
    Class<?>[] classes = {
            Map.class,
            Iterable.class,
            List.class,
            Set.class,
            Number.class,
            Throwable.class,
            BigDecimal.class,
            String.class,
            Comparable.class,
            Class.class, // Enum#getDeclaringClass
            Double.class,
            Long.class,
            Integer.class,
            Short.class,
            Boolean.class
    };
    nativeTypes.addAll(Arrays.stream(classes).collect(Collectors.toList()));

    //
    nativeTypesTruth8.add(Optional.class);
    nativeTypesTruth8.add(Stream.class);
  }

  private void addFieldAccessors(Method method, JavaClassSource generated, Class<?> classUnderTest) {
    Class<?> returnType = getWrappedReturnType(method);

    // todo skip static methods for now - just need to make template a bit more advanced
    if (methodIsStatic(method))
      return;

    if (Boolean.class.isAssignableFrom(returnType)) {
      addBooleanStrategy(method, generated, classUnderTest);
    } else {

      if (Collection.class.isAssignableFrom(returnType)) {
        addHasElementStrategy(method, generated, classUnderTest);
      }

      if (Optional.class.isAssignableFrom(returnType)) {
        addOptionalStrategy(method, generated, classUnderTest);
      }

      if (Map.class.isAssignableFrom(returnType)) {
        addMapStrategy(method, generated, classUnderTest);
      }

      addEqualityStrategy(method, generated, classUnderTest);
    }

    addChainStrategy(method, generated, returnType);

    generated.addImport(Fact.class)
            .setStatic(true)
            .setName(Fact.class.getCanonicalName() + ".*");
  }

  private Class<?> getWrappedReturnType(Method method) {
    Class<?> wrapped = primitiveToWrapper(method.getReturnType());
    return wrapped;
  }

  private void addEqualityStrategy(Method method, JavaClassSource generated, Class<?> classUnderTest) {
    equalityStrategyGeneric(method, generated, false);
    equalityStrategyGeneric(method, generated, true);
  }

  private void equalityStrategyGeneric(Method method, JavaClassSource generated, boolean positive) {
    boolean primitive = method.getReturnType().isPrimitive();
    String equality = primitive ? " == expected" : ".equals(expected)";

    String body = "" +
            "  if (%s(actual.%s()%s)) {\n" +
            "    failWithActual(fact(\"expected %s %sto be equal to\", expected));\n" +
            "  }\n";

    String testPrefix = positive ? "" : "!";
    String say = positive ? "" : "NOT ";
    String fieldName = removeStart(method.getName(), "get");
    body = format(body, testPrefix, method.getName(), equality, fieldName, say);

    String methodName = "has" + capitalize(fieldName) + capitalize(say.toLowerCase()).trim() + "EqualTo";
    MethodSource<JavaClassSource> newMethod = generated.addMethod();
    newMethod.setName(methodName)
            .setReturnTypeVoid()
            .setBody(body)
            .setPublic();
    newMethod.addParameter(method.getReturnType(), "expected");

    newMethod.getJavaDoc().setText("Simple check for equality for all fields.");

    copyThrownExceptions(method, newMethod);
  }

  private void addMapStrategy(Method method, JavaClassSource generated, Class<?> classUnderTest) {
    addMapStrategyGeneric(method, generated, false);
    addMapStrategyGeneric(method, generated, true);
  }

  private MethodSource<JavaClassSource> addMapStrategyGeneric(Method method, JavaClassSource generated, boolean positive) {
    String testPrefix = positive ? "" : "!";

    String body = "" +
            "  if (%sactual.%s().containsKey(expected)) {\n" +
            "    failWithActual(fact(\"expected %s %sto have key\", expected));\n" +
            "  }\n";

    String say = positive ? "" : "NOT ";
    String fieldName = removeStart(method.getName(), "get");
    body = format(body, testPrefix, method.getName(), fieldName, say);

    String methodName = "has" + capitalize(fieldName) + capitalize(say.toLowerCase()).trim() + "WithKey";
    MethodSource<JavaClassSource> newMethod = generated.addMethod();
    newMethod
            .setName(methodName)
            .setReturnTypeVoid()
            .setBody(body)
            .setPublic();

    // parameter
    Type keyType = getStrippedReturnTypeFirstGenericParam(method);
    newMethod.addParameter(keyType.getTypeName(), "expected");

    //
    newMethod.getJavaDoc().setText(format("Check Maps for containing a given {@link %s} key.", maybeGetSimpleName(keyType)));

    copyThrownExceptions(method, newMethod);

    return newMethod;
  }

  private void addOptionalStrategy(Method method, JavaClassSource generated, Class<?> classUnderTest) {
    addOptionalStrategyGeneric(method, generated, false);
    addOptionalStrategyGeneric(method, generated, true);
  }

  private MethodSource<JavaClassSource> addOptionalStrategyGeneric(Method method, JavaClassSource generated, boolean positive) {
    String testPrefix = positive ? "" : "!";
    String body = "" +
            "  if (%sactual.%s().isPresent()) {\n" +
            "    failWithActual(simpleFact(\"expected %s %sto be present\"));\n" +
            "  }\n";

    String say = positive ? "" : "NOT ";
    String fieldName = removeStart(method.getName(), "get");
    body = format(body, testPrefix, method.getName(), fieldName, say);

    String methodName = "has" + capitalize(fieldName) + capitalize(say.toLowerCase()).trim() + "Present";
    MethodSource<JavaClassSource> newMethod = generated.addMethod();
    newMethod
            .setName(methodName)
            .setReturnTypeVoid()
            .setBody(body)
            .setPublic();

    newMethod.getJavaDoc().setText("Checks Optional fields for presence.");

    copyThrownExceptions(method, newMethod);

    return newMethod;
  }

  private void addHasElementStrategy(Method method, JavaClassSource generated, Class<?> classUnderTest) {
    addHasElementStrategyGeneric(method, generated, false);
    addHasElementStrategyGeneric(method, generated, true);
  }

  private MethodSource<JavaClassSource> addHasElementStrategyGeneric(Method method, JavaClassSource generated, boolean positive) {
    String body = "" +
            "  if (%sactual.%s().contains(expected)) {\n" +
            "    failWithActual(fact(\"expected %s %sto have element\", expected));\n" +
            "  }\n";
    String testPrefix = positive ? "" : "!";

    String fieldName = removeStart(method.getName(), "get");

    String say = positive ? "" : "NOT ";
    body = format(body, testPrefix, method.getName(), fieldName, say);

    String methodName = "has" + capitalize(fieldName) + capitalize(say.toLowerCase()).trim() + "WithElement";
    MethodSource<JavaClassSource> newMethod = generated.addMethod();
    newMethod
            .setName(methodName)
            .setReturnTypeVoid()
            .setBody(body)
            .setPublic();


    Type elementType = getStrippedReturnTypeFirstGenericParam(method);
    newMethod.addParameter(elementType.getTypeName(), "expected");

    newMethod.getJavaDoc().setText(format("Checks if a {@link %s} element is, or is not contained in the collection.", maybeGetSimpleName(elementType)));

    copyThrownExceptions(method, newMethod);

    return newMethod;
  }

  private void addBooleanStrategy(Method method, JavaClassSource generated, Class<?> classUnderTest) {
    addBooleanGeneric(method, generated, true);
    addBooleanGeneric(method, generated, false);
  }

  private MethodSource<JavaClassSource> addBooleanGeneric(Method method, JavaClassSource generated, boolean positive) {
    String testPrefix = positive ? "" : "!";
    String say = positive ? "" : "NOT ";

    String body = "" +
            "  if (%sactual.%s()) {\n" +
            "    failWithActual(simpleFact(\"expected %sto be %s\"));\n" +
            "  }\n";

    String noun = StringUtils.remove(method.getName(), "is");

    body = format(body, testPrefix, method.getName(), say, noun);

    String methodName = removeStart(method.getName(), "is");
    methodName = "is" + capitalize(say.toLowerCase()).trim() + methodName;
    MethodSource<JavaClassSource> booleanMethod = generated.addMethod();
    booleanMethod
            .setName(methodName)
            .setReturnTypeVoid()
            .setBody(body)
            .setPublic();

    copyThrownExceptions(method, booleanMethod);

    booleanMethod.getJavaDoc().setText("Simple is or is not expectation for boolean fields.");

    return booleanMethod;
  }

  private void copyThrownExceptions(Method method, MethodSource<JavaClassSource> generated) {
    Class<? extends Exception>[] exceptionTypes = (Class<? extends Exception>[]) method.getExceptionTypes();
    Stream<Class<? extends Exception>> runtimeExceptions = Arrays.stream(exceptionTypes)
            .filter(x -> !RuntimeException.class.isAssignableFrom(x));
    runtimeExceptions.forEach(generated::addThrows);
  }

  private MethodSource<JavaClassSource> addChainStrategy(Method method, JavaClassSource generated, Class<?> returnType) {
    boolean isCoveredByNonPrimitiveStandardSubjects = isTypeCoveredUnderStandardSubjects(returnType);

    Optional<ClassOrGenerated> subjectForType = getSubjectForType(returnType);

    // no subject to chain
    if (subjectForType.isEmpty() && !isCoveredByNonPrimitiveStandardSubjects) {
      logger.at(WARNING).log("Cant find subject for " + returnType);
      return null;
    }

    ClassOrGenerated subjectClass = subjectForType.get();

    String nameForChainMethod = createNameForChainMethod(method);
    MethodSource<JavaClassSource> has = generated.addMethod()
            .setName(nameForChainMethod)
            .setPublic();

    StringBuilder body = new StringBuilder("isNotNull();\n");
    String check = format("return check(\"%s()\")", method.getName());
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

    if (methodIsStatic(method)) {
      body.append(format(".that(%s.%s());", method.getDeclaringClass().getSimpleName(), method.getName()));
    } else {
      body.append(format(".that(actual.%s());", method.getName()));
    }

    has.setBody(body.toString());

    has.setReturnType(subjectClass.getSubjectSimpleName());

    generated.addImport(subjectClass.getSubjectQualifiedName());

    has.getJavaDoc().setText("Returns the Subject for the given field type, so you can chain on other assertions.");

    copyThrownExceptions(method, has);

    return has;
  }

  private boolean methodIsStatic(Method method) {
    return withModifier(STATIC).test(method);
  }

  /**
   * Attempt to swap get for has, but only if it starts with get - otherwise leave it alone.
   */
  private String createNameForChainMethod(final Method method) {
    String name = method.getName();

    if (context.isLegacyMode())
      return "has" + capitalize(name);

    if (name.startsWith("get")) {
      name = removeStart(name, "get");
      return "has" + name;
    } else if (name.startsWith("is")) {
      return "has" + removeStart(name, "is");
    } else if (name.startsWith("to")) {
      return "has" + capitalize(name);
    } else
      return name;
  }

  // todo cleanup
  private boolean isTypeCoveredUnderStandardSubjects(final Class<?> returnType) {
    // todo should only do this, if we can't find a more specific subect for the returnType
    // todo should check if class is assignable from the super subjects, instead of checking names
    // todo use qualified names
    // todo add support truth8 extensions - optional etc
    // todo try generating classes for DateTime packages, like Instant and Duration
    // todo this is of course too aggressive

//    boolean isCoveredByNonPrimitiveStandardSubjects = specials.contains(returnType.getSimpleName());
    final Class<?> normalised = (returnType.isArray())
            ? returnType.getComponentType()
            : returnType;

    List<Class<?>> assignable = nativeTypes.stream().filter(x ->
            x.isAssignableFrom(normalised)
    ).collect(Collectors.toList());
    boolean isCoveredByNonPrimitiveStandardSubjects = !assignable.isEmpty();

    // todo is it an array of objects?
    boolean array = returnType.isArray();
    Class<?>[] classes = returnType.getClasses();
    String typeName = returnType.getTypeName();
    Class<?> componentType = returnType.getComponentType();

    return isCoveredByNonPrimitiveStandardSubjects || array;
  }

  private Optional<ClassOrGenerated> getSubjectForType(final Class<?> type) {
    String name;

    // if primitive, wrap and get wrapped Subject
    if (type.isPrimitive()) {
      Class<?> wrapped = primitiveToWrapper(type);
      name = wrapped.getName();
    } else {
      name = type.getName();
    }

    // arrays
    if (type.isArray()) {
      Class<?> componentType = type.getComponentType();
      if (componentType.isPrimitive()) {
        // PrimitiveBooleanArraySubject
        String subjectPrefix = "Primitive" + componentType.getSimpleName() + "Array";
        Class<?> compiledSubjectForTypeName = getCompiledSubjectForTypeName(subjectPrefix);
        return ClassOrGenerated.ofClass(compiledSubjectForTypeName);
      } else {
        return ClassOrGenerated.ofClass(ObjectArraySubject.class);
      }
    }

    //
    Optional<ClassOrGenerated> subject = getGeneratedOrCompiledSubjectFromString(name);

    // Can't find any generated ones or compiled ones - fall back to native subjects
    if (subject.isEmpty()) {
      Optional<Class<?>> nativeSubjectForType = getClosestTruthNativeSubjectForType(type);
      subject = ClassOrGenerated.ofClass(nativeSubjectForType);
      if (subject.isPresent())
        logger.at(INFO).log("Falling back to native interface subject %s for type %s", subject.get().clazz, type);
    }

    return subject;
  }

  private Optional<Class<?>> getClosestTruthNativeSubjectForType(final Class<?> type) {
    Class<?> normalised = primitiveToWrapper(type);
    Optional<Class<?>> highestPriorityNativeType = nativeTypes.stream().filter(x -> x.isAssignableFrom(normalised)).findFirst();
    if (highestPriorityNativeType.isPresent()) {
      Class<?> aClass = highestPriorityNativeType.get();
      Class<?> compiledSubjectForTypeName = getCompiledSubjectForTypeName(aClass.getSimpleName());
      return ofNullable(compiledSubjectForTypeName);
    }
    return empty();
  }

  private Optional<ClassOrGenerated> getGeneratedOrCompiledSubjectFromString(final String name) {
    boolean isObjectArray = name.endsWith("[]");
    if (isObjectArray)
      return of(new ClassOrGenerated(ObjectArraySubject.class, null));

    Optional<ThreeSystem> subjectFromGenerated = getSubjectFromGenerated(name);// takes precedence
    if (subjectFromGenerated.isPresent()) {
      return of(new ClassOrGenerated(null, subjectFromGenerated.get()));
    }

    Class<?> aClass = getCompiledSubjectForTypeName(name);
    if (aClass != null)
      return of(new ClassOrGenerated(aClass, null));

    return empty();
  }

  private Class<?> getCompiledSubjectForTypeName(String name) {
    // remove package if exists
    if (name.contains("."))
      name = StringUtils.substringAfterLast(name, ".");

    String compoundName = name + "Subject";
    Class<?> aClass = this.classPathSubjectTypes.get(compoundName);
    return aClass;
  }

  private Optional<ThreeSystem> getSubjectFromGenerated(final String name) {
    return Optional.ofNullable(this.generatedSubjects.get(name));
  }

  public void addTests(final Set<ThreeSystem> allTypes) {
    for (ThreeSystem system : allTypes) {
      addTests(system);
    }

    // only serialise results, when all have finished - useful for debugging
    for (ThreeSystem c : allTypes) {
      Utils.writeToDisk(c.parent.getGenerated());
    }
  }

  /**
   * An `Either` type that represents a {@link Subject} for a type being either an existing Compiled class, or a new
   * Generated class.
   */
  public static class ClassOrGenerated {

    /**
     * an existing Subject class on the class path
     */
    final Class<?> clazz;

    /**
     * a new generated Subject
     */
    final ThreeSystem generated;

    ClassOrGenerated(final Class<?> clazz, final ThreeSystem generated) {
      this.clazz = clazz;
      this.generated = generated;
    }

    static Optional<ClassOrGenerated> ofClass(Optional<Class<?>> clazz) {
      if (clazz.isPresent())
        return of(new ClassOrGenerated(clazz.get(), null));
      else return empty();
    }

    static Optional<ClassOrGenerated> ofClass(Class<?> compiledSubjectForTypeName) {
      return ofClass(of(compiledSubjectForTypeName));
    }

    String getSubjectSimpleName() {
      if (clazz == null)
        return this.generated.middle.getSimpleName();
      else
        return clazz.getSimpleName();
    }

    String getSubjectQualifiedName() {
      if (clazz == null)
        return this.generated.middle.getCanonicalName();
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
      return (isGenerated()) ? generated.middle.getCanonicalName() : clazz.getCanonicalName();
    }
  }
}
