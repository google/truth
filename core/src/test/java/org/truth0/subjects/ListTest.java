/*
 * Copyright (c) 2011 David Beaumont
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
import java.util.Comparator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for List Subjects.
 *
 * @author David Beaumont
 */
@RunWith(JUnit4.class)
public class ListTest {

  @Test public void listContainsSequenceWithEmptyList() {
    ASSERT.that(Arrays.asList(1, 2, 3)).containsSequence(Arrays.<Integer>asList());
  }

  @Test public void listContainsSequenceWithSingleton() {
    ASSERT.that(Arrays.asList(1)).containsSequence(Arrays.asList(1));
  }

  @Test public void listContainsSequenceAtEnd() {
    ASSERT.that(Arrays.asList(1, 2, 3)).containsSequence(Arrays.asList(2, 3));
  }

  @Test public void listContainsSequenceAtStart() {
    ASSERT.that(Arrays.asList(1, 2, 3)).containsSequence(Arrays.asList(1, 2));
  }

  @Test public void listContainsSequenceWithManyFalseStarts() {
    ASSERT.that(Arrays.asList(1, 1, 2, 1, 1, 2, 3, 4)).containsSequence(Arrays.asList(1, 2, 3));
  }

  @Test public void listContainsSequenceTooShortFailure() {
    try {
      ASSERT.that(Arrays.asList(1, 2, 3)).containsSequence(Arrays.asList(1, 2, 3, 4));
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("contains sequence");
      ASSERT.that(e.getMessage()).contains("[1, 2, 3, 4]");
    }
  }

  @Test public void listContainsSequenceNotContiguousFailure() {
    try {
      ASSERT.that(Arrays.asList(1, 2, 2, 3)).containsSequence(Arrays.asList(1, 2, 3));
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("contains sequence");
      ASSERT.that(e.getMessage()).contains("[1, 2, 3]");
    }
  }

  @Test public void listIsOrdered() {
    ASSERT.that(Arrays.<Integer>asList()).isOrdered();
    ASSERT.that(Arrays.asList(1)).isOrdered();
    ASSERT.that(Arrays.asList(1, 2, 3, 4)).isOrdered();
  }

  @Test public void isOrderedFailure() {
    try {
      ASSERT.that(Arrays.asList(1, 2, 2, 4)).isOrdered();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("is strictly ordered");
      ASSERT.that(e.getMessage()).contains("<2> <2>");
    }
  }

  @Test public void isOrderedWithNonComparableElementsFailure() {
    try {
      ASSERT.that(Arrays.<Object>asList(1, "2", 3, "4")).isOrdered();
      fail("Should have thrown.");
    } catch (ClassCastException e) {}
  }

  @Test public void listIsPartiallyOrdered() {
    ASSERT.that(Arrays.<Integer>asList()).isPartiallyOrdered();
    ASSERT.that(Arrays.asList(1)).isPartiallyOrdered();
    ASSERT.that(Arrays.asList(1, 1, 2, 3, 3, 3, 4)).isPartiallyOrdered();
  }

  @Test public void isPartiallyOrderedFailure() {
    try {
      ASSERT.that(Arrays.asList(1, 3, 2, 4)).isPartiallyOrdered();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("is partially ordered");
      ASSERT.that(e.getMessage()).contains("<3> <2>");
    }
  }

  @Test public void isPartiallyOrderedWithNonComparableElementsFailure() {
    try {
      ASSERT.that(Arrays.<Object>asList(1, "2", 2, "3")).isPartiallyOrdered();
      fail("Should have thrown.");
    } catch (ClassCastException e) {}
  }

  @Test public void listIsOrderedWithComparator() {
    ASSERT.that(Arrays.<String>asList()).isOrdered(COMPARE_AS_DECIMAL);
    ASSERT.that(Arrays.asList("1")).isOrdered(COMPARE_AS_DECIMAL);
    // Note: Use "10" and "20" to distinguish numerical and lexicographical ordering.
    ASSERT.that(Arrays.asList("1", "2", "10", "20")).isOrdered(COMPARE_AS_DECIMAL);
  }

  @Test public void listIsOrderedWithComparatorFailure() {
    try {
      ASSERT.that(Arrays.asList("1", "2", "2", "10")).isOrdered(COMPARE_AS_DECIMAL);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("is strictly ordered");
      ASSERT.that(e.getMessage()).contains("<2> <2>");
    }
  }

  @Test public void listIsPartiallyOrderedWithComparator() {
    ASSERT.that(Arrays.<String>asList()).isPartiallyOrdered(COMPARE_AS_DECIMAL);
    ASSERT.that(Arrays.asList("1")).isPartiallyOrdered(COMPARE_AS_DECIMAL);
    ASSERT.that(Arrays.asList("1", "1", "2", "10", "10", "10", "20"))
        .isPartiallyOrdered(COMPARE_AS_DECIMAL);
  }

  @Test public void listIsPartiallyOrderedWithComparatorFailure() {
    try {
      ASSERT.that(Arrays.asList("1", "10", "2", "20")).isPartiallyOrdered(COMPARE_AS_DECIMAL);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      ASSERT.that(e.getMessage()).contains("is partially ordered");
      ASSERT.that(e.getMessage()).contains("<10> <2>");
    }
  }

  private static final Comparator<String> COMPARE_AS_DECIMAL = new Comparator<String>() {
    @Override public int compare(String a, String b) {
      return Integer.valueOf(a).compareTo(Integer.valueOf(b));
    }
  };
}
