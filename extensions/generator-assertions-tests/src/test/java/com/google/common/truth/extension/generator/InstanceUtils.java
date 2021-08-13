package com.google.common.truth.extension.generator;

public class InstanceUtils {
  static <T> T createInstance(Class<T> clazz) {
    return GeneratedAssertionTests.PODAM_FACTORY.manufacturePojo(clazz);
  }
}
