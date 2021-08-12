package com.google.common.truth.extension.generator.internal;

import com.google.common.truth.Subject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.Set;
import java.util.stream.Collectors;

public class ClassUtils {

  public static Set<Class<?>> collectSourceClasses(String... modelPackages) {
    // for all classes in package
    SubTypesScanner subTypesScanner = new SubTypesScanner(false);

    Reflections reflections = new Reflections(modelPackages, subTypesScanner);
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

}
