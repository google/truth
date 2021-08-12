package com.google.common.truth.extension.generator;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.truth.extension.generator.internal.ClassUtils;
import lombok.Getter;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

/**
 * Use this class to prepare the set of source classes to generate for, and settings for different types of sources.
 */
@Getter
public class SourceClassSets {

  private final String packageForOverall;

  /**
   *
   */
  //todo rename
  private final Set<String> simplePackages = new HashSet<>();

  /**
   *
   */
  private final Set<Class<?>> simpleClasses = new HashSet<>();

  /**
   *
   */
  private final Set<TargetPackageAndClasses> targetPackageAndClasses = new HashSet<>();

  /**
   *
   */
  private final Set<Class<?>> legacyBeans = new HashSet<>();

  /**
   *
   */
  private final Set<TargetPackageAndClasses> legacyTargetPackageAndClasses = new HashSet<>();

  /**
   *
   */
  private Set<Class<?>> classSetCache;

  /**
   * @param packageForOverall the package to put the overall access points
   */
  public SourceClassSets(String packageForOverall) {
    this.packageForOverall = packageForOverall;
  }

  /**
   * Use the package of the parameter as the base package;
   */
  public SourceClassSets(Object packageFromObject) {
    this(packageFromObject.getClass().getPackage().getName());
  }

  /**
   * Use the package of this class base package;
   */
  public SourceClassSets(Class<?> packageFromClass) {
    this(packageFromClass.getPackage().getName());
  }

  public void generateAllFoundInPackagesOf(Class<?>... classes) {
    Set<String> collect = stream(classes).map(x -> x.getPackage().getName()).collect(toSet());
    simplePackages.addAll(collect);
  }

  public void generateAllFoundInPackages(Package... packages) {
    Set<String> collect = stream(packages).map(Package::getName).collect(toSet());
    simplePackages.addAll(collect);
  }

  public void generateAllFoundInPackages(String... packageNames) {
    simplePackages.addAll(stream(packageNames).collect(toSet()));
  }

  /**
   * Useful for generating Java module Subjects and put them in our package.
   * <p>
   * I.e. for UUID.class you can't create a Subject in the same package as it (not allowed).
   */
  public void generateFrom(String targetPackageName, Class<?>... classes) {
    targetPackageAndClasses.add(new TargetPackageAndClasses(targetPackageName, classes));
  }

  public void generateFrom(Class<?>... classes) {
    this.simpleClasses.addAll(stream(classes).collect(toSet()));
  }

  public void generateFrom(Set<Class<?>> classes) {
    this.simpleClasses.addAll(classes);
  }

  /**
   * Shades the given source classes into the base package, suffixed with the source package
   */
  public void generateFromShaded(Class<?>... classes) {
    Set<TargetPackageAndClasses> targetPackageAndClassesStream = mapToPackageSets(classes);
    this.targetPackageAndClasses.addAll(targetPackageAndClassesStream);
  }

  private Set<TargetPackageAndClasses> mapToPackageSets(Class<?>[] classes) {
    ImmutableListMultimap<Package, Class<?>> grouped = Multimaps.index(asList(classes), Class::getPackage);

    return grouped.keySet().stream().map(x -> {
      Class<?>[] classSet = grouped.get(x).toArray(new Class<?>[0]);
      TargetPackageAndClasses newSet = new TargetPackageAndClasses(getTargetPackageName(x),
              classSet);
      return newSet;
    }).collect(toSet());
  }

  private String getTargetPackageName(Package p) {
    return this.packageForOverall + ".shaded." + p.getName();
  }

  public void generateFromNonBean(Class<?>... nonBeanLegacyClass) {
    for (Class<?> beanLegacyClass : nonBeanLegacyClass) {
      legacyBeans.add(beanLegacyClass);
    }
  }

  public void generateFromShadedNonBean(Class<?>... clazzes) {
    Set<TargetPackageAndClasses> targetPackageAndClassesStream = mapToPackageSets(clazzes);
    this.legacyTargetPackageAndClasses.addAll(targetPackageAndClassesStream);
  }

  public void generateFrom(ClassLoader loader, String... classes) {
    Class[] as = stream(classes).map(x -> {
      try {
        return loader.loadClass(x);
      } catch (ClassNotFoundException e) {
        throw new GeneratorException("Cannot find class asked to generate from: " + x, e);
      }
    }).collect(Collectors.toList()).toArray(new Class[0]);
    generateFrom(as);
  }

  // todo shouldn't be public?
  public Set<Class<?>> getAllClasses() {
    Set<Class<?>> union = new HashSet<>();
    union.addAll(getSimpleClasses());
    union.addAll(getLegacyBeans());

    Set<Class<?>> collect = getTargetPackageAndClasses().stream().flatMap(x ->
            stream(x.classes)
    ).collect(toSet());
    union.addAll(collect);

    union.addAll(getLegacyTargetPackageAndClasses().stream().flatMap(x -> stream(x.classes)).collect(toSet()));

    union.addAll(getSimplePackages().stream().flatMap(
            x -> ClassUtils.collectSourceClasses(x).stream()).collect(toSet()));

    // todo need more elegant solution than this
    this.classSetCache = union;
    return union;
  }

  // todo shouldn't be public?
  public void addIfMissing(final Set<? extends Class<?>> clazzes) {
    getAllClasses(); // update class set cache
    clazzes.forEach(x -> {
      if (!classSetCache.contains(x))
        generateFrom(x);
    });
  }

  // todo shouldn't be public?
  public boolean isClassIncluded(final Class<?> clazz) {
    return classSetCache.contains(clazz);
  }

  public boolean isLegacyClass(final Class<?> theClass) {
    return getLegacyBeans().contains(theClass)
            || getLegacyTargetPackageAndClasses().stream().anyMatch(x -> asList(x.classes).contains(theClass));
  }

  /**
   * Container for classes and the target package they're to be produced into
   */
  @Value
  public static class TargetPackageAndClasses {
    String targetPackageName;
    Class<?>[] classes;
  }

}
