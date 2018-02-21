/*
 * Copyright (c) 2018 Google, Inc.
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

import static com.google.common.truth.Field.field;
import static com.google.common.truth.Field.makeMessage;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Field}. */
@RunWith(JUnit4.class)
public class FieldTest {
  @Test
  public void string() {
    assertThat(field("foo", "bar").toString()).isEqualTo("foo: bar");
  }

  @Test
  public void oneFields() {
    assertThat(makeMessage(ImmutableList.<String>of(), ImmutableList.of(field("foo", "bar"))))
        .isEqualTo("foo: bar");
  }

  @Test
  public void twoFields() {
    assertThat(
            makeMessage(
                ImmutableList.<String>of(),
                ImmutableList.of(field("foo", "bar"), field("longer name", "other value"))))
        .isEqualTo("foo        : bar\nlonger name: other value");
  }

  @Test
  public void newline() {
    assertThat(makeMessage(ImmutableList.<String>of(), ImmutableList.of(field("foo", "bar\nbaz"))))
        .isEqualTo("foo:\n    bar\n    baz");
  }

  @Test
  public void withMessage() {
    assertThat(
            makeMessage(ImmutableList.<String>of("hello"), ImmutableList.of(field("foo", "bar"))))
        .isEqualTo("hello\nfoo: bar");
  }
}
