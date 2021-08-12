package com.google.common.truth.extension.generator.internal.model;

import lombok.*;
import org.checkerframework.common.util.report.qual.ReportWrite;
import org.jboss.forge.roaster.model.source.JavaClassSource;

@Getter
public class ThreeSystem {

  @Setter
  boolean legacyMode = false;

  public Class<?> classUnderTest;

  public ParentClass parent;
  public MiddleClass middle;
  public JavaClassSource child;

  public ThreeSystem(Class<?> classUnderTest, ParentClass parent, MiddleClass middle, JavaClassSource child) {
    this.classUnderTest = classUnderTest;
    this.parent = parent;
    this.middle = middle;
    this.child = child;
  }

  @Override
  public String toString() {
    return "ThreeSystem{" +
            "classUnderTest=" + classUnderTest + '}';
  }

  public boolean isShaded() {
    return !packagesAreContained();
  }

// todo rename
  private boolean packagesAreContained() {
    Package underTestPackage = classUnderTest.getPackage();
    String subjectPackage = parent.getGenerated().getPackage();
    return underTestPackage.getName().contains(subjectPackage);
  }
}
