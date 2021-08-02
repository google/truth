package com.google.common.truth.extension.generator.internal.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class MiddleClass extends AClass {
  MethodSource<JavaClassSource> factoryMethod;
  Class<?> usersMiddleClass;

  public static MiddleClass of(Class<?> aClass) {
    return MiddleClass.builder().usersMiddleClass(aClass).build();
  }

  public static MiddleClass of(JavaClassSource middle, MethodSource factory) {
    return MiddleClass.builder().generated(middle).factoryMethod(factory).build();
  }

  public String getSimpleName() {
    return (usersMiddleClass == null)
            ? super.generated.getName()
            : usersMiddleClass.getName();
  }

  public void makeChildExtend(JavaClassSource child) {
    if (usersMiddleClass == null)
      child.extendSuperType(generated);
    else
      child.extendSuperType(usersMiddleClass);
  }

  public String getCanonicalName() {
    return (usersMiddleClass == null)
            ? super.generated.getCanonicalName()
            : usersMiddleClass.getCanonicalName();
  }
}
