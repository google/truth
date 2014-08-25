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

import static com.google.common.truth.Truth.ASSERT;
import static com.google.common.truth.Truth.assert_;
import static java.util.Arrays.asList;

import com.google.common.truth.Platform;
import com.google.gwt.junit.client.GWTTestCase;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Test of Truth under GWT - should be enough tests here to force compilation
 * of all Subject implementations.
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
    assert_().that(true).isTrue();
    assert_().that(false).isFalse();
  }

  public void testInteger() {
    assert_().that(457923).is(457923);
    try {
      assert_().that(457923).is(1);
      assert_().fail("Should have thrown an exception");
    } catch (Throwable t) {
      // succeeds
    }
  }

  public void testString() {
    assert_().that("blah").contains("ah");
    assert_().that("blah").startsWith("bl");
    assert_().that("blah").endsWith("ah");
    try {
      assert_().that("blah").contains("foo");
      assert_().fail("Should have thrown an exception");
    } catch (Throwable t) {
      // succeeds
    }
  }

  public void testIterable() {
    assert_().that((Iterable<Integer>)asList(1, 2, 3)).iteratesAs(1, 2, 3);
  }

  public void testCollection() {
    assert_().that((Collection<Integer>)asList(1, 2, 3)).has().allOf(1, 2, 3).inOrder();
  }

  public void testList() {
    assert_().that(asList(1, 2, 3)).has().allOf(1, 2, 3).inOrder();
  }

  public void testObjectArray() {
    Set[] setOfString = { new HashSet<String>(asList("foo", "bar", "bash")) };
    assert_().that(setOfString).asList()
        .has().item(new HashSet<String>(asList("foo", "bar", "bash")));
  }

  public void testDefault() {
    assert_().that(new Object()).isNotNull();
    assert_().that(new ArrayList<String>()).isA(AbstractList.class);
  }

  public void testLegacy() {
    ASSERT.that(new Object()).isNotNull();
    ASSERT.that(new ArrayList<String>()).isA(AbstractList.class);
  }

  public void testInvokePlatformMethods() {
    Platform.isInstanceOfType(new Object(), Object.class);
  }
}
