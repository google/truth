package com.google.common.truth.extension.generator.testing.legacy;


import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder(toBuilder = true)
@RequiredArgsConstructor
public class NonBeanLegacy {
  final int age;
  final String name;

  public int age() {
    return age;
  }

  public String name() {
    return name;
  }
}
