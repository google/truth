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
public class SubjectPropertiesTest {

  @Test public void testHasNoSuchField() {
    try {
      ASSERT.that(new A()).hasField("noField");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A> has a field named <noField>");
    }
  }

  @Test public void testHasPublicField() {
    ASSERT.that(new A()).hasField("publicField");
  }

  @Test public void testHasFieldWithNullSubject() {
    Object nullObject = null;
    try {
      ASSERT.that(nullObject).hasField("publicField");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Cannot determine a field name from a null object.");
    }
  }

  @Test public void testHasPublicFieldWithValue() {
    ASSERT.that(new A("value", null)).hasField("publicField").withValue("value");
  }

  @Test public void testHasPublicFieldWithWrongValue() {
    try {
      ASSERT.that(new A("aValue", null)).hasField("publicField").withValue("wrongValue");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A>'s field <publicField> contains expected value " +
                "<wrongValue>. It contains value <aValue>");
    }
  }

  @Test public void testHasPublicFieldWithNullValue() {
    ASSERT.that(new A(null, null)).hasField("publicField").withValue(null);
  }

  @Test public void testHasPublicFieldWithWrongNullValue() {
    try {
      ASSERT.that(new A(null, null)).hasField("publicField").withValue("wrongValue");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A>'s field <publicField> contains expected value " +
                "<wrongValue>. It contains value <null>");
    }
  }

  @Test public void testHasPublicFieldWithValueInBadField() {
    try {
      ASSERT.that(new A("value", null)).hasField("noField").withValue("value");
      ASSERT.fail("Should have thrown an assertion error.");
    } catch (AssertionError expected) {
      ASSERT.that(expected.getMessage())
          .is("Not true that <A> has a field named <noField>");
    }
  }

  @Test public void testHasPrivateField() {
    ASSERT.that(new A()).hasField("privateField");
  }

  @Test public void testHasProvidedFieldWithValue() {
    ASSERT.that(new A(null, "value")).hasField("privateField").withValue("value");
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
