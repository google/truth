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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link com.google.common.truth.Platform} methods which are swapped in for GWT.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class PlatformTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  // isInstance checking

  @Test
  public void testIsInstanceOfType_Java() {
    assertThat(Platform.isInstanceOfTypeJava(new Object(), Object.class)).isTrue();
    assertThat(Platform.isInstanceOfTypeJava("string", String.class)).isTrue();
  }

  @Test
  public void testIsInstanceOfType_Java_Fail() {
    expectFailure
        .whenTesting()
        .that(Platform.isInstanceOfTypeJava(new ArrayList<String>(), Set.class))
        .isTrue();
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("expected to be true");
  }

  @Test
  public void testIsInstanceOfType_Java_Superclass() {
    assertThat(Platform.isInstanceOfTypeJava(new ArrayList<String>(), AbstractCollection.class))
        .isTrue();
  }

  @Test
  public void testIsInstanceOfType_Java_Interface() {
    assertThat(Platform.isInstanceOfTypeJava(new ArrayList<String>(), Iterable.class)).isTrue();
    assertThat(Platform.isInstanceOfTypeJava(new ArrayList<String>(), List.class)).isTrue();
  }

  @Test
  public void testIsInstanceOfType_GWT() {
    assertThat(Platform.isInstanceOfTypeGWT(new Object(), Object.class)).isTrue();
    assertThat(Platform.isInstanceOfTypeGWT("string", String.class)).isTrue();
  }

  @Test
  public void testIsInstanceOfType_GWT_Fail() {
    expectFailure
        .whenTesting()
        .that(Platform.isInstanceOfTypeGWT(new ArrayList<String>(), Set.class))
        .isTrue();
    assertThat(expectFailure.getFailure()).hasMessageThat().contains("expected to be true");
  }

  @Test
  public void testIsInstanceOfType_GWT_Superclass() {
    assertThat(Platform.isInstanceOfTypeGWT(new ArrayList<String>(), AbstractCollection.class))
        .isTrue();
  }

  @Test
  public void testIsInstanceOfType_GWT_Interface() {
    assertThat(Platform.isInstanceOfTypeGWT(new ArrayList<String>(), Iterable.class)).isTrue();
    assertThat(Platform.isInstanceOfTypeGWT(new ArrayList<String>(), List.class)).isTrue();
  }

}
