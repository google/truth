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
package org.junit.contrib.truth;

import static org.junit.contrib.truth.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for introspective Subject behaviour.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class SubjectPropertiesTest {

  @Test public void testHasField_NoSuchField() {
    try {
      ASSERT.that(new A()).hasField("noField");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A> has a field named <noField>");
    }
  }

  @Test public void testHasField_Public() {
    ASSERT.that(new A()).hasField("publicField");
  }

  @Test public void testHasField_NullSubject() {
    Object nullObject = null;
    try {
      ASSERT.that(nullObject).hasField("publicField");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that the subject <null> has a field named <publicField>");
    }
  }

  @Test public void testHasFieldValue_Public() {
    ASSERT.that(new A("value", null)).hasFieldValue("publicField", "value");
  }

  @Test public void testHasFieldValue_Public_WrongValue() {
    try {
      ASSERT.that(new A("aValue", null)).hasFieldValue("publicField", "wrongValue");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A>'s field <publicField> contains expected value " +
                "<wrongValue>. It contains value <aValue>");
    }
  }

  @Test public void testHasFieldValue_Public_NullValue() {
    ASSERT.that(new A(null, null)).hasFieldValue("publicField", null);
  }

  @Test public void testHasFieldValue_Public_WrongNullValue() {
    try {
      ASSERT.that(new A(null, null)).hasFieldValue("publicField", "wrongValue");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A>'s field <publicField> contains expected value " +
                "<wrongValue>. It contains value <null>");
    }
  }

  @Test public void testHasFieldValue_Public_BadField() {
    try {
      ASSERT.that(new A("value", null)).hasFieldValue("noField", "value");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A> has a field named <noField>");
    }
  }

  @Test public void testHasField_Private() {
    ASSERT.that(new A()).hasField("privateField");
  }

  @Test public void testHasFieldValue_Private() {
    ASSERT.that(new A(null, "value")).hasFieldValue("privateField", "value");
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
