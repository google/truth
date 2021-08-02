package com.google.common.truth.extension.generator.testModel;


import lombok.RequiredArgsConstructor;

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
