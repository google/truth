/*
 * Copyright (c) 2014 Google, Inc.
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

import com.google.common.truth.MathUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link MathUtil} used by numeric subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class MathUtilTest {
  @Test
  public void floatEquals() {
    assertThat(MathUtil.equals(1.3f, 1.3f, 0.00000000000001f)).isTrue();
    assertThat(MathUtil.equals(1.3f, 1.3f, 0.0f)).isTrue();
    assertThat(MathUtil.equals(0.0f, 1.0f + 2.0f - 3.0f, 0.00000000000000000000000000000001f))
        .isTrue();
    assertThat(MathUtil.equals(1.3f, 1.303f, 0.004f)).isTrue();
    assertThat(MathUtil.equals(1.3f, 1.303f, 0.002f)).isFalse();
    assertThat(MathUtil.equals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 0.01f)).isFalse();
    assertThat(MathUtil.equals(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.01f)).isFalse();
    assertThat(MathUtil.equals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.01f)).isFalse();
    assertThat(MathUtil.equals(Float.NaN, Float.NaN, 0.01f)).isFalse();
  }

  @Test
  public void doubleEquals() {
    assertThat(MathUtil.equals(1.3d, 1.3d, 0.00000000000001d)).isTrue();
    assertThat(MathUtil.equals(1.3d, 1.3d, 0.0d)).isTrue();
    assertThat(MathUtil.equals(0.0d, 1.0d + 2.0d - 3.0d, 0.00000000000000000000000000000001d))
        .isTrue();
    assertThat(MathUtil.equals(1.3d, 1.303d, 0.004d)).isTrue();
    assertThat(MathUtil.equals(1.3d, 1.303d, 0.002d)).isFalse();
    assertThat(MathUtil.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.01d))
        .isFalse();
    assertThat(MathUtil.equals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.01d))
        .isFalse();
    assertThat(MathUtil.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.01d))
        .isFalse();
    assertThat(MathUtil.equals(Double.NaN, Double.NaN, 0.01d)).isFalse();
  }

  @Test
  public void floatNotEquals() {
    assertThat(MathUtil.notEquals(1.3f, 1.3f, 0.00000000000001f)).isFalse();
    assertThat(MathUtil.notEquals(1.3f, 1.3f, 0.0f)).isFalse();
    assertThat(MathUtil.notEquals(0.0f, 1.0f + 2.0f - 3.0f, 0.00000000000000000000000000000001f))
        .isFalse();
    assertThat(MathUtil.notEquals(1.3f, 1.303f, 0.004f)).isFalse();
    assertThat(MathUtil.notEquals(1.3f, 1.303f, 0.002f)).isTrue();
    assertThat(MathUtil.notEquals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 0.01f))
        .isFalse();
    assertThat(MathUtil.notEquals(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.01f))
        .isFalse();
    assertThat(MathUtil.notEquals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.01f))
        .isFalse();
    assertThat(MathUtil.notEquals(Float.NaN, Float.NaN, 0.01f)).isFalse();
  }

  @Test
  public void doubleNotEquals() {
    assertThat(MathUtil.notEquals(1.3d, 1.3d, 0.00000000000001d)).isFalse();
    assertThat(MathUtil.notEquals(1.3d, 1.3d, 0.0d)).isFalse();
    assertThat(MathUtil.notEquals(0.0d, 1.0d + 2.0d - 3.0d, 0.00000000000000000000000000000001d))
        .isFalse();
    assertThat(MathUtil.notEquals(1.3d, 1.303d, 0.004d)).isFalse();
    assertThat(MathUtil.notEquals(1.3d, 1.303d, 0.002d)).isTrue();
    assertThat(MathUtil.notEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.01d))
        .isFalse();
    assertThat(MathUtil.notEquals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.01d))
        .isFalse();
    assertThat(MathUtil.notEquals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.01d))
        .isFalse();
    assertThat(MathUtil.notEquals(Double.NaN, Double.NaN, 0.01d)).isFalse();
  }

  @Test
  public void equalsDifferentTypes() {
    assertThat(MathUtil.equals(1.3d, 1.3f, 0.00000000000001d)).isFalse();
    assertThat(MathUtil.equals(1.3f, 1.3d, 0.00000000000001f)).isFalse();
  }

  // TODO(cgruber): More complicated ways to break float/double casting to make sure.

}
