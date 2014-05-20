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
package org.truth0.gwt;

import static java.util.Arrays.asList;
import static org.truth0.Truth.ASSERT;

import com.google.gwt.junit.client.GWTTestCase;

import org.truth0.util.Platform;

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
    return "org.truth0.gwt.TruthTest";
  }

  public void testBuildClasses() {
    new Inventory().toString(); // force invocation.
  }

  public void testBoolean() {
    ASSERT.that(true).isTrue();
    ASSERT.that(false).isFalse();
  }

  public void testInteger() {
    ASSERT.that(457923).is(457923);
    try {
      ASSERT.that(457923).is(1);
      ASSERT.fail("Should have thrown an exception");
    } catch (Throwable t) {
      // succeeds
    }
  }

  public void testString() {
    ASSERT.that("blah").contains("ah");
    ASSERT.that("blah").startsWith("bl");
    ASSERT.that("blah").endsWith("ah");
    try {
      ASSERT.that("blah").contains("foo");
      ASSERT.fail("Should have thrown an exception");
    } catch (Throwable t) {
      // succeeds
    }
  }

  public void testIterable() {
    ASSERT.that((Iterable<Integer>)asList(1, 2, 3)).iteratesAs(1, 2, 3);
  }

  public void testCollection() {
    ASSERT.that((Collection<Integer>)asList(1, 2, 3)).has().allOf(1, 2, 3).inOrder();
  }

  public void testList() {
    ASSERT.that(asList(1, 2, 3)).has().allOf(1, 2, 3).inOrder();
  }

  public void testObjectArray() {
    Set[] setOfString = { new HashSet<String>(asList("foo", "bar", "bash")) };
    ASSERT.that(setOfString).asList()
        .has().item(new HashSet<String>(asList("foo", "bar", "bash")));
  }

  public void testDefault() {
    ASSERT.that(new Object()).isNotNull();
    ASSERT.that(new ArrayList<String>()).isA(AbstractList.class);
  }

  public void testInvokePlatformMethods() {
    Platform.isInstanceOfType(new Object(), Object.class);
  }
}
