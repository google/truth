package com.google.common.truth.extension.generator.internal;

import com.google.common.flogger.FluentLogger;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

public class SourceChecking {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  static boolean checkSource(Class<?> source, Optional<String> targetPackage) {
    if (isAnonymous(source))
      return true;

    if (isBuilder(source))
      return true;

    if (isTestClass(source))
      return true;

    if (isJavaPackage(source, targetPackage))
      return true;

    return false;
  }


  private static boolean isJavaPackage(Class<?> source, Optional<String> targetPackage) {
    boolean isBlank = targetPackage.isEmpty() || StringUtils.isBlank(targetPackage.get());
    if (source.getPackage().getName().startsWith("java.") && isBlank)
      throw new IllegalArgumentException("Cannot construct Subject's for external modules without changing their " +
              "destination package. See SourceClassSets#generateFrom(String, Class<?>...)");
    return false;
  }

  /**
   * If any method is annotated with something with Test in it, then assume it's a test class
   */
  private static boolean isTestClass(Class<?> source) {
    boolean hasTestAnnotatedMethod = !ReflectionUtils.getMethods(source,
            x -> Arrays.stream(x.getAnnotations())
                    .anyMatch(y -> y.annotationType()
                            .getSimpleName().contains("Test"))).isEmpty();
    boolean nameEndsInTest = source.getSimpleName().endsWith("Test");
    boolean isIndeed = hasTestAnnotatedMethod || nameEndsInTest;
    if (isIndeed) {
      logger.at(Level.FINE).log("Skipping a test class %s", source);
    }
    return isIndeed;
  }

  private static boolean isBuilder(Class<?> source) {
    String simpleName = source.getSimpleName();
    if (simpleName.contains("Builder")) {
      logger.at(Level.FINE).log("Skipping builder class %s", source);
      return true;
    }
    return false;
  }

  private static boolean isAnonymous(Class<?> source) {
    if (source.isAnonymousClass()) {
      logger.at(Level.FINE).log("Skipping anonymous class %s", source);
      return true;
    }
    return false;
  }
}
