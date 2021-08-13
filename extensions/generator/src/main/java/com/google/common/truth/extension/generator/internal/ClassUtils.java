package com.google.common.truth.extension.generator.internal;

import com.google.common.truth.Subject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Type;
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
}
