package com.google.common.truth.extension.generator.internal.model;

import org.jboss.forge.roaster.model.source.JavaClassSource;

public class AClass {
   public final JavaClassSource generated;

   AClass(final JavaClassSource generated) {
      this.generated = generated;
   }
}
