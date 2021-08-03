package com.google.common.truth.extension.generator;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use this class to prepare the set of source classes to generate for, and settings for different types of sources.
 */
@Getter
public class SourceClassSets {

  private final String packageForOverall;
  private final Set<Class<?>[]> simplePackageOfClasses = new HashSet<>();
  private final Set<Class<?>> simpleClasses = new HashSet<>();
  private final Set<PackageAndClasses> packageAndClasses = new HashSet<>();

  /**
   * @param packageForOverall the package to put the overall access points
   */
  public SourceClassSets(final String packageForOverall) {
    this.packageForOverall = packageForOverall;
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
    this.simpleClasses.addAll(Arrays.stream(classes).collect(Collectors.toSet()));
  }

  public void generateFrom(Set<Class<?>> classes) {
    this.simpleClasses.addAll(classes);
  }

  /**
   * Shades the given source classes into the base package, suffixed with the source package
   */
  public void generateFromShaded(Class<?>... classes) {
    ImmutableListMultimap<Package, Class<?>> grouped = Multimaps.index(Arrays.asList(classes), Class::getPackage);

    grouped.keySet().forEach(x -> {
      Class<?>[] classSet = grouped.get(x).toArray(new Class<?>[0]);
      PackageAndClasses newSet = new PackageAndClasses(getTargetPackageName(x),
              classSet);
      packageAndClasses.add(newSet);
    });
  }

  private String getTargetPackageName(Package p) {
    return this.packageForOverall + ".shaded." + p.getName();
  }

  @Value
  public static class PackageAndClasses {
    String targetPackageName;
    Class<?>[] classes;
  }

}
