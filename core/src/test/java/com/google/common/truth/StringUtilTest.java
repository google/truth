/*
 * Copyright (c) 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.truth;

import static com.google.common.truth.Truth.assertThat;

import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link com.google.common.truth.StringUtil} methods. */
@RunWith(JUnit4.class)
public class StringUtilTest {
  // Type shortening.

  @Test
  public void compressType_JavaLang() {
    assertThat(StringUtil.compressType(String.class.toString())).isEqualTo("String");
  }

  @Test
  public void compressType_JavaUtil() {
    assertThat(StringUtil.compressType(Random.class.toString())).isEqualTo("Random");
  }

  @Test
  public void compressType_Generic() {
    assertThat(StringUtil.compressType("java.util.Set<java.lang.Integer>"))
        .isEqualTo("Set<Integer>");
  }

  @Test
  public void compressType_Uncompressed() {
    assertThat(StringUtil.compressType(Truth.class.toString()))
        .isEqualTo("com.google.common.truth.Truth");
  }

  @Test
  public void compressType_GenericWithPartialUncompress() {
    assertThat(StringUtil.compressType("java.util.Set<com.google.common.truth.Truth>"))
        .isEqualTo("Set<com.google.common.truth.Truth>");
  }

  @Test
  public void compressType_Primitive() {
    assertThat(StringUtil.compressType(int.class.toString())).isEqualTo("int");
  }
}
