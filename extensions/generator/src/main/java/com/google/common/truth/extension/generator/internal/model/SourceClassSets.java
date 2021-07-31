package com.google.common.truth.extension.generator.internal.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  public class PackageAndClasses {
    final String targetPackageName;

    final Class<?>[] classes;

    private PackageAndClasses(final String targetPackageName, final Class<?>[] classes) {
      this.targetPackageName = targetPackageName;
      this.classes = classes;
    }

    public String getTargetPackageName() {
      return targetPackageName;
    }

    public Class<?>[] getClasses() {
      return classes;
    }
  }

  public Set<Class<?>[]> getSimplePackageOfClasses() {
    return simplePackageOfClasses;
  }

  public Set<PackageAndClasses> getPackageAndClasses() {
    return packageAndClasses;
  }

  public String getPackageForOverall() {
    return packageForOverall;
  }

  public Set<Class<?>> getSimpleClasses() {
    return this.simpleClasses;
  }

}
