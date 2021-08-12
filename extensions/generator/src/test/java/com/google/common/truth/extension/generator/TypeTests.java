package com.google.common.truth.extension.generator;

import org.junit.Test;

public class TypeTests {

  /**
   * Sanity tests for how arrays are handled in reflection
   */
  @Test
  public void arrays() {
    int[] ints = new int[]{0, 1};
    String[] strings = new String[]{};
    EnumTest[] enums = new EnumTest[]{};

    Class<? extends int[]> aClass = ints.getClass();
    Class<?> componentType = aClass.getComponentType();
    Class<?> componentType1 = strings.getClass().getComponentType();
    Class<?> enumscomp = enums.getClass().getComponentType();
    Class<?> enumsco2mp = enums.getClass().getComponentType();
  }

  enum EnumTest{
    ONE, TWO
  }
}
