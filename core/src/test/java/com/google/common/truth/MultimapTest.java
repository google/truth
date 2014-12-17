/*
 * Copyright (c) 2014 Google, Inc.
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
import static org.junit.Assert.fail;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Multimap Subjects.
 *
 * @author Daniel Ploch
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class MultimapTest {
  @Test public void multimapIsEmpty() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of();
    assertThat(multimap).isEmpty();
  }

  @Test public void multimapIsEmptyWithFailure() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of(1, 5);
    try {
      assertThat(multimap).isEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{1=[5]}> is empty");
    }
  }

  @Test public void multimapIsNotEmpty() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of(1, 5);
    assertThat(multimap).isNotEmpty();
  }

  @Test public void multimapIsNotEmptyWithFailure() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of();
    try {
      assertThat(multimap).isNotEmpty();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{}> is not empty");
    }
  }

  @Test public void hasSize() {
    assertThat(ImmutableMultimap.of(1, 2, 3, 4)).hasSize(2);
  }

  @Test public void hasSizeZero() {
    assertThat(ImmutableMultimap.of()).hasSize(0);
  }

  @Test public void hasSizeNegative() {
    try {
      assertThat(ImmutableMultimap.of(1, 2)).hasSize(-1);
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  @Test public void containsKey() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).containsKey("kurt");
  }

  @Test public void containsKeyFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    try {
      assertThat(multimap).containsKey("daniel");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("Not true that <{kurt=[kluever]}> contains key <daniel>");
    }
  }

  @Test public void containsKeyNull() {
    Multimap<String, String> multimap = HashMultimap.create();
    multimap.put(null, "null");
    assertThat(multimap).containsKey(null);
  }

  @Test public void containsKeyNullFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    try {
      assertThat(multimap).containsKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "Not true that <{kurt=[kluever]}> contains key <null>");
    }
  }

  @Test public void doesNotContainKey() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).doesNotContainKey("daniel");
    assertThat(multimap).doesNotContainKey(null);
  }

  @Test public void doesNotContainKeyFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    try {
      assertThat(multimap).doesNotContainKey("kurt");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "Not true that <{kurt=[kluever]}> does not contain key <kurt>");
    }
  }

  @Test public void doesNotContainNullKeyFailure() {
    Multimap<String, String> multimap = HashMultimap.create();
    multimap.put(null, "null");
    try {
      assertThat(multimap).doesNotContainKey(null);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "Not true that <{null=[null]}> does not contain key <null>");
    }
  }

  @Test public void containsEntry() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).containsEntry("kurt", "kluever");
  }

  @Test public void containsEntryFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    try {
      assertThat(multimap).containsEntry("daniel", "ploch");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "Not true that <{kurt=[kluever]}> contains entry <daniel=ploch>");
    }
  }

  @Test public void doesNotContainEntry() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).doesNotContainEntry("daniel", "ploch");
  }

  @Test public void doesNotContainEntryFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    try {
      assertThat(multimap).doesNotContainEntry("kurt", "kluever");
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          "Not true that <{kurt=[kluever]}> does not contain entry <kurt=kluever>");
    }
  }

  @Test public void valuesForKey() {
    ImmutableMultimap<Integer, String> multimap = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(multimap).valuesForKey(3).hasSize(3);
    assertThat(multimap).valuesForKey(4).containsExactly("four", "five");
    assertThat(multimap).valuesForKey(3).containsAllOf("one", "six").inOrder();
    assertThat(multimap).valuesForKey(5).isEmpty();
  }

  @Test public void valuesForKeyListMultimap() {
    ImmutableListMultimap<Integer, String> multimap = ImmutableListMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(multimap).valuesForKey(4).isStrictlyOrdered();
  }

  @Test public void containsExactly() {
    ImmutableListMultimap<Integer, String> listMultimap = ImmutableListMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableSetMultimap<Integer, String> setMultimap = ImmutableSetMultimap.copyOf(listMultimap);

    assertThat(listMultimap).containsExactly(setMultimap);
  }

  @Test public void containsExactlyEmpty() {
    ImmutableListMultimap<Integer, String> actual = ImmutableListMultimap.of();
    ImmutableSetMultimap<Integer, String> expected = ImmutableSetMultimap.of();

    assertThat(actual).containsExactly(expected);
    assertThat(actual).containsExactly(expected).inOrder();
  }

  @Test public void containsExactlyRejectsNull() {
    ImmutableMultimap<Integer, String> multimap = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    try {
      assertThat(multimap).containsExactly(null);
      fail("Should have thrown.");
    } catch (NullPointerException expected) {}
  }

  @Test public void containsExactlyRespectsDuplicates() {
    ImmutableListMultimap<Integer, String> actual = ImmutableListMultimap.of(
        3, "one", 3, "two", 3, "one", 4, "five", 4, "five");
    ImmutableListMultimap<Integer, String> expected = ImmutableListMultimap.of(
        3, "two", 4, "five", 3, "one", 4, "five", 3, "one");

    assertThat(actual).containsExactly(expected);
  }

  @Test public void containsExactlyRespectsDuplicatesFailure() {
    ImmutableListMultimap<Integer, String> actual = ImmutableListMultimap.of(
        3, "one", 3, "two", 3, "one", 4, "five", 4, "five");
    ImmutableSetMultimap<Integer, String> expected = ImmutableSetMultimap.copyOf(actual);

    try {
      assertThat(actual).containsExactly(expected);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          String.format(""
                  + "Not true that <%s> contains exactly <%s>. "
                  + "It has unexpected items <{3=[one], 4=[five]}>",
              actual, expected));
    }
  }

  @Test public void containsExactlyFailureMissing() {
    ImmutableMultimap<Integer, String> expected = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");

    try {
      assertThat(actual).containsExactly(expected);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          String.format(""
                  + "Not true that <%s> contains exactly <%s>. "
                  + "It is missing <{3=[six], 4=[five]}>",
              actual, expected));
    }
  }

  @Test public void containsExactlyFailureExtra() {
    ImmutableMultimap<Integer, String> expected = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.put(4, "nine");
    actual.put(5, "eight");

    try {
      assertThat(actual).containsExactly(expected);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          String.format(""
                  + "Not true that <%s> contains exactly <%s>. "
                  + "It has unexpected items <{4=[nine], 5=[eight]}>",
              actual, expected));
    }
  }

  @Test public void containsExactlyFailureBoth() {
    ImmutableMultimap<Integer, String> expected = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");
    actual.put(4, "nine");
    actual.put(5, "eight");

    try {
      assertThat(actual).containsExactly(expected);
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          String.format(""
                  + "Not true that <%s> contains exactly <%s>. "
                  + "It is missing <{3=[six], 4=[five]}> "
                  + "and has unexpected items <{4=[nine], 5=[eight]}>",
              actual, expected));
    }
  }

  @Test public void containsExactlyInOrder() {
    ImmutableMultimap<Integer, String> actual = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(actual).containsExactly(expected).inOrder();
  }

  @Test public void containsExactlyInOrderDifferentTypes() {
    ImmutableListMultimap<Integer, String> listMultimap = ImmutableListMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableSetMultimap<Integer, String> setMultimap = ImmutableSetMultimap.copyOf(listMultimap);

    assertThat(listMultimap).containsExactly(setMultimap).inOrder();
  }

  @Test public void containsExactlyInOrderFailure() {
    ImmutableMultimap<Integer, String> actual = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected = ImmutableMultimap.of(
        4, "four", 3, "six", 4, "five", 3, "two", 3, "one");

    assertThat(actual).containsExactly(expected);
    try {
      assertThat(actual).containsExactly(expected).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e.getMessage()).startsWith(
          String.format(
              "Not true that <%s> contains exactly <%s> in order. ",
              actual, expected));
    }
  }

  @Test public void containsExactlyInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual = ImmutableMultimap.of(
        3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected = ImmutableMultimap.of(
        3, "six", 3, "two", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsExactly(expected);
    try {
      assertThat(actual).containsExactly(expected).inOrder();
      fail("Should have thrown.");
    } catch (AssertionError e) {
      assertThat(e).hasMessage(
          String.format(""
                  + "Not true that <%s> contains exactly <%s> in order. "
                  + "The values for keys <[3]> are not in order",
              actual, expected));
    }
  }
}
