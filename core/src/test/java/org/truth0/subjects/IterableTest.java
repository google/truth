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

import static org.junit.Assert.fail;
import static org.truth0.Truth.ASSERT;

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

  @Test public void iteratesOverSequence() {
    ASSERT.that(iterable(1, 2, 3)).iteratesOverSequence(1, 2, 3);
  }

  @Test public void iteratesOverEmptySequence() {
    ASSERT.that(iterable()).iteratesOverSequence();
  }

  @Test public void iteratesOverSequenceWithOrderingFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).iteratesOverSequence(2, 3, 1);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iteratesOverSequenceWithTooManyItemsFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).iteratesOverSequence(1, 2, 3, 4);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iteratesOverSequenceWithTooFewItemsFailure() {
    try {
      ASSERT.that(iterable(1, 2, 3)).iteratesOverSequence(1, 2);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
    }
  }

  @Test public void iterableIsEmpty() {
    ASSERT.that(iterable()).isEmpty();
  }

  @Test public void iterableIsEmptyWithFailure() {
    try {
      ASSERT.that(iterable(1, null, 3)).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is empty");
    }
  }

  @Test public void iterableIsNotEmpty() {
    ASSERT.that(iterable("foo")).isNotEmpty();
  }

  @Test public void iterableIsNotEmptyWithFailure() {
    try {
      ASSERT.that(iterable()).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("Not true that");
      ASSERT.that(e.getMessage()).contains("is not empty");
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
