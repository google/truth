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

import static org.junit.Assert.fail;
import static org.junit.contrib.truth.Truth.ASSERT;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Collection Subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class IterableTest {



  @Test public void iterableContainsWithNull() {
    ASSERT.that(iterable(1, null, 3)).contains(null);
  }

  @Test public void iterableContainsWith2KindsOfChaining() {
    Iterable<Integer> foo = iterable(1, 2, 3);
    Iterable<Integer> bar = foo;
    ASSERT.that(foo).is(bar).and().contains(1).and().contains(2);
  }

  @Test public void iterableContainsFailureWithChaining() {
    try {
      ASSERT.that(iterable(1, 2, 3)).contains(1).and().contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {}
  }

  @Test public void iterableContainsFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).contains(5);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iterablehasContentsAnyOrder() {
    ASSERT.that(iterable(1, 2, 3)).hasContentsAnyOrder(2, 3, 1);
  }

  @Test public void iterablehasContentsAnyOrder_Fail() {
    try {
      ASSERT.that(iterable(1, 2, 3)).hasContentsAnyOrder(2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iterablehasContentsInOrder() {
    ASSERT.that(iterable(1, 2, 3)).hasContentsInOrder(1, 2, 3);
  }

  @Test public void iterablehasContentsInOrder_Fail() {
    try {
      ASSERT.that(iterable(1, 2, 3)).hasContentsInOrder(2, 3, 1);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iterableIsEmpty_Success() {
    ASSERT.that(iterable()).isEmpty();
  }

  @Test public void iterableIsEmpty_Fail() {
    try {
      ASSERT.that(iterable("foo")).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iterableIsNotEmpty_Success() {
    ASSERT.that(iterable("foo")).isNotEmpty();
  }

  @Test public void iterableIsNotEmpty_Fail() {
    try {
      ASSERT.that(iterable()).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  /**
   * Helper that returns a general Collection rather than a List.
   * This ensures that we test CollectionSubject (rather than ListSubject).
   */
  private static <T> Iterable<T> iterable(T... items) {
    return Arrays.asList(items);
  }

}
