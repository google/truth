package com.google.common.truth.extension.generator;

import lombok.Getter;
import lombok.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Use this class to prepare the set of source classes to generate for, and settings for different types of sources.
 */
@Getter
public class SourceClassSets {

  private final String packageForOverall;
  private Set<Class<?>[]> simplePackageOfClasses = new HashSet<>();
  private Set<Class<?>> simpleClasses = new HashSet<>();
  private Set<PackageAndClasses> packageAndClasses = new HashSet<>();

  /**
   * @param packageForOverall the package to put the overall access points
   */
  public SourceClassSets(final String packageForOverall) {
    this.packageForOverall = packageForOverall;
  }

  public void generateAllFoundInPackagesOf(Class<?>... classes) {
    simplePackageOfClasses.add(classes);
  }

  /**
   * Useful for generating Java module Subjects and put them in our package.
   * <p>
   * I.e. for UUID.class you can't create a Subject in the same package as it (not allowed)
   *
   * @param targetPackageName
   * @param classes
   */
  public void generateFrom(String targetPackageName, Class<?>... classes) {
    packageAndClasses.add(new PackageAndClasses(targetPackageName, classes));
  }

  public void generateFrom(Set<Class<?>> classes) {
    this.simpleClasses = classes;
  }

  @Value
  public class PackageAndClasses {
    String targetPackageName;
    Class<?>[] classes;
  }

}
