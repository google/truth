package com.google.common.truth.extension.generator.internal.model;

import org.jboss.forge.roaster.model.source.JavaClassSource;

public class ManagedClassSet<T> {
   final Class<T> sourceClass;
   final JavaClassSource generatedClass;

   public ManagedClassSet(final Class<T> sourceClass, final JavaClassSource generatedClass) {
      this.sourceClass = sourceClass;
      this.generatedClass = generatedClass;
   }
}
