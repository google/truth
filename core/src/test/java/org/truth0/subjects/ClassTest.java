/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0.subjects;

import static org.truth0.Truth.ASSERT;

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
      ASSERT.that(A.class).declaresField("noField");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A> has a field named <noField>");
    }
  }

  @Test public void testDeclaresField_Public() {
    ASSERT.that(A.class).declaresField("publicField");
  }

  @Test public void testDeclaresField_NullSubject() {
    Class<?> nullClass = null;
    try {
      ASSERT.that(nullClass).declaresField("publicField");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Cannot determine a field name from a null class.");
    }
  }

  @Test public void testDeclaresField_Private() {
    ASSERT.that(A.class).declaresField("privateField");
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
