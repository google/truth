package com.google.common.truth.extension.generator.internal.model;

import org.jboss.forge.roaster.model.source.JavaClassSource;

public class ThreeSystem {
   @Override
   public String toString() {
      return "ThreeSystem{" +
              "classUnderTest=" + classUnderTest + '}';
   }

   public final Class<?> classUnderTest;
   public final ParentClass parent;
   public final MiddleClass middle;
   public final JavaClassSource child;

   public ThreeSystem(final Class<?> classUnderTest, final ParentClass parent, final MiddleClass middle, final JavaClassSource child) {
      this.classUnderTest = classUnderTest;
      this.parent = parent;
      this.middle = middle;
      this.child = child;
   }
}
