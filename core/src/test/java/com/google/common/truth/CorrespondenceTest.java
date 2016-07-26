/*
 * Copyright (c) 2016 Google, Inc.
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

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Correspondence}.
 *
 * @author Pete Gillin
 */
@RunWith(JUnit4.class)
public final class CorrespondenceTest {

  private static final Correspondence<Object, Object> INSTANCE =
      new Correspondence<Object, Object>() {

        @Override
        public boolean compare(Object actual, Object expected) {
          return false;
        }

        @Override
        public String toString() {
          return "example";
        }
      };

  @Test
  public void testEquals_throws() {
    try {
      INSTANCE.equals(new Object());
      fail("Expected UnsupportedOperationException from Correspondence.equals");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @Test
  public void testHashCode_throws() {
    try {
      INSTANCE.hashCode();
      fail("Expected UnsupportedOperationException from Correspondence.hashCode");
    } catch (UnsupportedOperationException expected) {
    }
  }
}
