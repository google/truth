package com.google.common.truth.extension.generator.internal;

import org.atteo.evo.inflector.English;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

public class Utils {

  public static String writeToDisk(JavaClassSource javaClass) {
    return writeToDisk(javaClass, Optional.empty());
  }

  public static String writeToDisk(JavaClassSource javaClass, Optional<String> targetPackageName) {
    String classSource = javaClass.toString();
    String fileName = getFileName(javaClass, targetPackageName);
    try (PrintWriter out = new PrintWriter(fileName)) {
      out.println(classSource);
    } catch (FileNotFoundException e) {
      throw new IllegalStateException(format("Cannot write to file %s", fileName));
    }
    return classSource;
  }

  private static String getFileName(JavaClassSource javaClass, Optional<String> targetPackageName) {
    String directoryName = getDirectoryName(javaClass, targetPackageName);
    File dir = new File(directoryName);
    if (!dir.exists()) {
      boolean mkdir = dir.mkdirs();
    }
    return directoryName + javaClass.getName() + ".java";
  }

  private static String getDirectoryName(JavaClassSource javaClass, Optional<String> targetPackageName) {
    String parent = Paths.get("").toAbsolutePath().toString();
    String packageName = targetPackageName.isEmpty() ? javaClass.getPackage() : targetPackageName.get();

    // don't use, as won't be able to access package level access methods if we live in a different package
    // user can still use a custom target package if they like
    String packageNameSuffix = ".truth";

    List<String> ids = List.of("Parent", "Child");
    boolean isChildOrParent = ids.stream().anyMatch(x -> javaClass.getName().contains(x));

    String baseDirSuffix = (isChildOrParent) ? "truth-assertions-managed" : "truth-assertions-templates";

    String packageNameDir = packageName.replace(".", "/");

    return format("%s/target/generated-test-sources/%s/%s/", parent, baseDirSuffix, packageNameDir);
  }

  public static <T> String getFactoryName(Class<T> source) {
    String simpleName = source.getSimpleName();
    String plural = English.plural(simpleName);
    String normal = toLowerCaseFirstLetter(plural);
    return normal;
  }

  private static String toLowerCaseFirstLetter(String plural) {
    return plural.substring(0, 1).toLowerCase() + plural.substring(1);
  }

  public static void requireNotEmpty(final List<Class<?>> classes) {
    if (classes.isEmpty()) {
      throw new IllegalArgumentException("No classes to generate from");
    }
  }

  public static void requireNotEmpty(final String[] modelPackages) {
    if (modelPackages.length == 0) {
      throw new IllegalArgumentException("No packages to generate from");
    }
  }

  public static void requireNotEmpty(final Set<Class<?>> classes) {
    if (classes.isEmpty()) {
      throw new IllegalArgumentException("No classes to generate from");
    }
  }
}
