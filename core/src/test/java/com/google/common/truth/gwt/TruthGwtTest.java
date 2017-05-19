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
package com.google.common.truth.gwt;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;
import static java.util.Arrays.asList;

import com.google.gwt.junit.client.GWTTestCase;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Test of Truth under GWT - should be enough tests here to force compilation of all Subject
 * implementations.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */

public class TruthGwtTest extends GWTTestCase {
   @Override public String getModuleName() {
     return "com.google.common.truth.gwt.TruthTest";
   }

  public void testBuildClasses() {
    new Inventory().toString(); // force invocation.
  }

  public void testBoolean() {
    assertThat(true).isTrue();
    assertThat(false).isFalse();

    try {
      assertThat(true).isFalse();
    } catch (AssertionError expected) {
      return;
    }
  }

  public void testInteger() {
    assertThat(457923).isEqualTo(457923);
    try {
      assertThat(457923).isEqualTo(1);
    } catch (AssertionError expected) {
      return;
    }
    assert_().fail("Should have thrown an assertion error");
  }

  public void testString() {
    assertThat("blah").contains("ah");
    assertThat("blah").startsWith("bl");
    assertThat("blah").endsWith("ah");

    try {
      assertThat("blah").contains("foo");
    } catch (AssertionError expected) {
      return;
    }
    assert_().fail("Should have thrown an assertion error");
  }

  public void testString_match() {
    assertThat("blah").matches("b[la]+h");
    assertThat("blah").matches("^b.+h$");
    assertThat("blah").containsMatch("ah");
    assertThat("blah").containsMatch("b[la]{2}h");
    assertThat("blah").doesNotMatch("ah");
    assertThat("blah").doesNotContainMatch("oh");
  }

  public void testString_matchesFail() {
    try {
      assertThat("blah").matches("b[lu]+h");
    } catch (AssertionError expected) {
      return;
    }
    assert_().fail("Should have thrown an assertion error");
  }

  public void testString_containsMatchFail() {
    try {
      assertThat("blah").containsMatch("o");
    } catch (AssertionError expected) {
      return;
    }
    assert_().fail("Should have thrown an assertion error");
  }

  public void testString_doesNotMatchFail() {
    try {
      assertThat("blah").doesNotMatch("blah");
    } catch (AssertionError expected) {
      return;
    }
    assert_().fail("Should have thrown an assertion error");
  }

  public void testString_doesNotContainMatchFail() {
    try {
      assertThat("blah").doesNotContainMatch("a");
    } catch (AssertionError expected) {
      return;
    }
    assert_().fail("Should have thrown an assertion error");
  }

  public void testIterable() {
    assertThat((Iterable<Integer>) asList(1, 2, 3)).containsExactly(1, 2, 3).inOrder();
  }

  public void testCollection() {
    assertThat((Collection<Integer>) asList(1, 2, 3)).containsExactly(1, 2, 3).inOrder();
  }

  public void testList() {
    assertThat(asList(1, 2, 3)).containsExactly(1, 2, 3).inOrder();
  }

  public void testObjectArray() {
    Set[] setOfString = {new HashSet<String>(asList("foo", "bar", "bash"))};
    assertThat(setOfString).asList().contains(new HashSet<String>(asList("foo", "bar", "bash")));
  }

  public void testDefault() {
    assertThat(new Object()).isNotNull();
    assertThat(new ArrayList<String>()).isInstanceOf(AbstractList.class);
  }

  public void testLegacyAssert_() {
    assert_().that(new Object()).isNotNull();
    assert_().that(new ArrayList<String>()).isInstanceOf(AbstractList.class);
  }
}
