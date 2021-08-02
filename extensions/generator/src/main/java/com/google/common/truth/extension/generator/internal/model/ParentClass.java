package com.google.common.truth.extension.generator.internal.model;

import lombok.Value;
import org.jboss.forge.roaster.model.source.JavaClassSource;

@Value
public class ParentClass extends AClass {
   public ParentClass(JavaClassSource generated) {
      super(generated);
   }
}
