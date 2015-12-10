/*
 * Copyright (c) 2011 Google, Inc.
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

import static com.google.common.truth.StringUtil.countOfPlaceholders;
import static com.google.common.truth.StringUtil.format;
import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for String Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class StringUtilTest {
  @Test
  public void nullTemplate() {
    assertThat(format(null)).isEqualTo("null");
  }

  @Test
  public void noArgs() {
    assertThat(format("foo")).isEqualTo("foo");
  }

  @Test
  public void simpleReplacement() {
    assertThat(format("%s", "foo")).isEqualTo("foo");
  }

  @Test
  public void simpleNonStringReplacement() {
    assertThat(format("%s", 1L)).isEqualTo("1");
  }

  @Test
  public void multipleReplacement() {
    assertThat(format("%s: %s", 1L, true)).isEqualTo("1: true");
  }

  @Test
  public void tooManyArguments() {
    assertThat(format("%s", 1L, true)).isEqualTo("1 [true]");
  }

  @Test
  public void tooManyPlaceholders() {
    try {
    assertThat(format("%s: %s", true)).isEqualTo("1 [true]");
    fail("Expected to throw");
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessage("Too many parameters for 1 argument: \"%s: %s\"");
    }
  }

  @Test
  public void placeholderCountNull() {
    assertThat(countOfPlaceholders(null)).isEqualTo(0);
  }


  @Test
  public void placeholderCountNone() {
    assertThat(countOfPlaceholders("foo")).isEqualTo(0);
  }

  @Test
  public void placeholderCountOne() {
    assertThat(countOfPlaceholders("foo%sfoo")).isEqualTo(1);
  }

  @Test
  public void placeholderCountMany() {
    assertThat(countOfPlaceholders("foo%sfoo%s%s%s 3s%s")).isEqualTo(5);
  }

}
