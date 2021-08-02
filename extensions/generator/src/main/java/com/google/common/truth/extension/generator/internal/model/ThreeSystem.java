package com.google.common.truth.extension.generator.internal.model;

import lombok.Value;
import org.jboss.forge.roaster.model.source.JavaClassSource;

@Value
public class ThreeSystem {
   @Override
   public String toString() {
      return "ThreeSystem{" +
              "classUnderTest=" + classUnderTest + '}';
   }

   public Class<?> classUnderTest;
   public ParentClass parent;
   public MiddleClass middle;
   public JavaClassSource child;
}
