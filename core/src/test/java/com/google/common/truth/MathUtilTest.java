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

import static com.google.common.truth.Truth.ASSERT;

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

  @Test public void floatEquality() {
    ASSERT.that(MathUtil.equals(1.3f, 1.3f, 0.00000000000001f)).isTrue();
    ASSERT.that(MathUtil.equals(1.3f, 1.3f, 0.0f)).isTrue();
    ASSERT.that(MathUtil.equals(0.0f, 1.0f+2.0-3.0, 0.00000000000000000000000000000001f)).isTrue();
  }

  @Test public void doubleEquality() {
    ASSERT.that(MathUtil.equals(1.3d, 1.3d, 0.00000000000001f)).isTrue();
    ASSERT.that(MathUtil.equals(1.3d, 1.3d, 0.0f)).isTrue();
    ASSERT.that(MathUtil.equals(0.0d, 1.0d+2.0-3.0, 0.00000000000000000000000000000001d)).isTrue();
  }

  @Test public void doubleEqualityDifferentTypes() {
    ASSERT.that(MathUtil.equals(1.3d, 1.3f, 0.00000000000001d)).isFalse();
    ASSERT.that(MathUtil.equals(1.3f, 1.3d, 0.00000000000001f)).isFalse();
  }

  // TODO(user): More complicated ways to break float/double casting to make sure.

}
