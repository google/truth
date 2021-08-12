package com.google.common.truth.extension.generator;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

/**
 * Use this class to prepare the set of source classes to generate for, and settings for different types of sources.
 */
@Getter
public class SourceClassSets {

  private final String packageForOverall;
  private final Set<Class<?>[]> simplePackageOfClasses = new HashSet<>();
  private final Set<Class<?>> simpleClasses = new HashSet<>();
  private final Set<PackageAndClasses> packageAndClasses = new HashSet<>();
  private final Set<Class<?>> legacyBeans = new HashSet<>();
  private final Set<PackageAndClasses> legacyPackageAndClasses = new HashSet<>();
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

  public SourceClassSets(Class<?> packageFromClass) {
    this(packageFromClass.getPackage().getName());
  }

  public void generateAllFoundInPackagesOf(Class<?>... classes) {
    simplePackageOfClasses.add(classes);
  }

  /**
   * Useful for generating Java module Subjects and put them in our package.
   * <p>
   * I.e. for UUID.class you can't create a Subject in the same package as it (not allowed).
   */
  public void generateFrom(String targetPackageName, Class<?>... classes) {
    packageAndClasses.add(new PackageAndClasses(targetPackageName, classes));
  }

  public void generateFrom(Class<?>... classes) {
    this.simpleClasses.addAll(stream(classes).collect(Collectors.toSet()));
  }

  public void generateFrom(Set<Class<?>> classes) {
    this.simpleClasses.addAll(classes);
  }

  /**
   * Shades the given source classes into the base package, suffixed with the source package
   */
  public void generateFromShaded(Class<?>... classes) {
    Set<PackageAndClasses> packageAndClassesStream = mapToPackageSets(classes);
    this.packageAndClasses.addAll(packageAndClassesStream);
  }

  private Set<PackageAndClasses> mapToPackageSets(Class<?>[] classes) {
    ImmutableListMultimap<Package, Class<?>> grouped = Multimaps.index(asList(classes), Class::getPackage);

    return grouped.keySet().stream().map(x -> {
      Class<?>[] classSet = grouped.get(x).toArray(new Class<?>[0]);
      PackageAndClasses newSet = new PackageAndClasses(getTargetPackageName(x),
              classSet);
      return newSet;
    }).collect(Collectors.toSet());
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
    Set<PackageAndClasses> packageAndClassesStream = mapToPackageSets(clazzes);
    this.legacyPackageAndClasses.addAll(packageAndClassesStream);
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

    Set<Class<?>> collect = getPackageAndClasses().stream().flatMap(x ->
            stream(x.classes)
    ).collect(Collectors.toSet());
    union.addAll(collect);

    union.addAll(getLegacyPackageAndClasses().stream().flatMap(x -> stream(x.classes)).collect(Collectors.toSet()));

    union.addAll(getSimplePackageOfClasses().stream().flatMap(x -> stream(x)).collect(Collectors.toSet()));

    // todo need more elegant solution than this
    this.classSetCache = union;
    return union;
  }

  // todo shouldn't be public?
  public void addIfMissing(final Set<? extends Class<?>> clazzes) {
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
            || getLegacyPackageAndClasses().stream().anyMatch(x-> asList(x.classes).contains(theClass));
  }

  @Value
  public static class PackageAndClasses {
    String targetPackageName;
    Class<?>[] classes;
  }

}
