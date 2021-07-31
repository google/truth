package com.google.common.truth.extension.generator.internal.model;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

public class MiddleClass extends AClass {
   public final MethodSource<JavaClassSource> factoryMethod;
   public final Class<?> usersMiddleClass;

   public MiddleClass(JavaClassSource generated, MethodSource<JavaClassSource> factoryMethod, final Class<?> usersMiddleClass) {
      super(generated);
      this.factoryMethod = factoryMethod;
      this.usersMiddleClass = usersMiddleClass;
   }

    public String getName() {
        return (usersMiddleClass==null)?super.generated.getName():usersMiddleClass.getName();
    }
}
