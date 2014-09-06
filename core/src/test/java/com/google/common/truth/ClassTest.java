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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for introspective Subject behaviour.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class ClassTest {

  @Test public void testDeclaresField_NoSuchField() {
    try {
      assertThat(A.class).declaresField("noField");
      assert_().fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage())
          .isEqualTo("Not true that <A> has a field named <noField>");
    }
  }

  @Test public void testDeclaresField_Public() {
    assertThat(A.class).declaresField("publicField");
  }

  @Test public void testDeclaresField_NullSubject() {
    Class<?> nullClass = null;
    try {
      assertThat(nullClass).declaresField("publicField");
      assert_().fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage())
          .isEqualTo("Cannot determine a field name from a null class.");
    }
  }

  @Test public void testDeclaresField_Private() {
    assertThat(A.class).declaresField("privateField");
  }

  @Test public void testIsAssignableFromSame() {
    assertThat(String.class.isAssignableFrom(String.class)).isTrue();
    assertThat(String.class).isAssignableFrom(String.class);

    assertThat(Object.class.isAssignableFrom(String.class)).isTrue();
    assertThat(Object.class).isAssignableFrom(String.class);
  }

  @Test public void testIsAssignableFromParent() {
    assertThat(Exception.class.isAssignableFrom(NullPointerException.class)).isTrue();
    assertThat(Exception.class).isAssignableFrom(NullPointerException.class);
  }

  @Test public void testIsAssignableFrom_reversed() {
    assertThat(String.class.isAssignableFrom(Object.class)).isFalse();
    try {
      assertThat(String.class).isAssignableFrom(Object.class);
      assert_().fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage())
          .isEqualTo("Not true that <class java.lang.String> "
              + "is assignable from <class java.lang.Object>");
    }
  }

  @Test public void testIsAssignableFrom_reversedDifferentTypes() {
    assertThat(String.class.isAssignableFrom(Exception.class)).isFalse();
    try {
      assertThat(String.class).isAssignableFrom(Exception.class);
      assert_().fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      assertThat(expected.getMessage())
          .isEqualTo("Not true that <class java.lang.String> "
              + "is assignable from <class java.lang.Exception>");
    }
  }

  public static class A {
    public String publicField = null;
    @SuppressWarnings("unused")
    private String privateField = null;
    public A() {}
    public A(String publicField, String privateField) {
      this.publicField = publicField;
      this.privateField = privateField;
    }
  }

}
