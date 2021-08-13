package com.google.common.truth.extension.generator.internal;

import com.google.common.truth.Subject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassUtils {

  public static Set<Class<?>> collectSourceClasses(String... modelPackages) {
    // for all classes in package
    SubTypesScanner subTypesScanner = new SubTypesScanner(false);

    ConfigurationBuilder build = new ConfigurationBuilder()
            .forPackages(modelPackages)
            .filterInputsBy(new FilterBuilder().includePackage(modelPackages))
            .setScanners(subTypesScanner)
            .setExpandSuperTypes(true);

    Reflections reflections = new Reflections(build);
    reflections.expandSuperTypes(); // get things that extend something that extend object

    // https://github.com/ronmamo/reflections/issues/126
    Set<Class<? extends Enum>> subTypesOfEnums = reflections.getSubTypesOf(Enum.class);

    Set<Class<?>> allTypes = reflections.getSubTypesOf(Object.class)
            // remove Subject classes from previous runs
            .stream().filter(x -> !Subject.class.isAssignableFrom(x))
            .collect(Collectors.toSet());
    allTypes.addAll(subTypesOfEnums);
    return allTypes;
  }

  public static String maybeGetSimpleName(Type elementType) {
    return (elementType instanceof Class<?>) ? ((Class<?>) elementType).getSimpleName() : elementType.getTypeName();
  }

  static Type getStrippedReturnTypeFirstGenericParam(Method method) {
    Type genericReturnType = method.getGenericReturnType();
    return getStrippedReturnTypeFirstGenericParam(genericReturnType);
  }

  private static Type getStrippedReturnTypeFirstGenericParam(Type genericReturnType) {
    Class<?> keyType = Object.class; // default fall back
    if (genericReturnType instanceof ParameterizedType) {
      ParameterizedType parameterizedReturnType = (ParameterizedType) genericReturnType;
      Type[] actualTypeArguments = parameterizedReturnType.getActualTypeArguments();
      if (actualTypeArguments.length > 0) { // must have at least 1
        Type key = actualTypeArguments[0];
        return getStrippedReturnType(key);
      }
    } else if (genericReturnType instanceof Class<?>) {
      return genericReturnType; // terminal
    }
    return keyType;
  }

  private static Type getStrippedReturnType(Type key) {
    if (key instanceof ParameterizedType) {
      // strip type arguments
      // could potentially add this as a type parameter to the method instead?
      ParameterizedType parameterizedKey = (ParameterizedType) key;
      Type rawType = parameterizedKey.getRawType();
      Type recursive = getStrippedReturnTypeFirstGenericParam(rawType);
      return recursive;
    } else if (key instanceof WildcardType) {
      // strip type arguments
      // could potentially add this as a type parameter to the method instead?
      WildcardType wildcardKey = (WildcardType) key;
      Type[] upperBounds = wildcardKey.getUpperBounds();
      if (upperBounds.length > 0) {
        Type upperBound = upperBounds[0];
        Type recursive = getStrippedReturnType(upperBound);
        return recursive;
      }
    }
    // else
    return key;
  }

}
