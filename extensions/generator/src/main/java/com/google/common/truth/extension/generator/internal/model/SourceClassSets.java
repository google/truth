package com.google.common.truth.extension.generator.internal.model;

import java.util.HashSet;
import java.util.Set;

public class SourceClassSets {

    private final String packageForOverall;
    private final Set<Class<?>[]> simplePackageOfClasses = new HashSet<>();
    private final Set<PackageAndClasses> packageAndClasses = new HashSet<>();

    public SourceClassSets(final String packageForOverall) {
        this.packageForOverall = packageForOverall;
    }

    public void generateFromPackagesOf(Class<?>... classes) {
        simplePackageOfClasses.add(classes);
    }

    /**
     * Useful for generating Java module Subjects and put them in our package.
     *
     * I.e. for UUID.class you can't create a Subject in the same package as it (not allowed)
     *
     * @param targetPackageName
     * @param classes
     */
    public void generateFrom(String targetPackageName, Class<?>... classes) {
        packageAndClasses.add(new PackageAndClasses(targetPackageName, classes));
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
}
