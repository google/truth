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

import static com.google.common.truth.IterableSubjectTest.STRING_PARSES_TO_INTEGER_CORRESPONDENCE;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Rule;
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
public class MultimapSubjectTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void listMultimapIsEqualTo_passes() {
    ImmutableListMultimap<String, String> multimapA =
        ImmutableListMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "russell", "cobain")
            .build();
    ImmutableListMultimap<String, String> multimapB =
        ImmutableListMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "russell", "cobain")
            .build();

    assertThat(multimapA.equals(multimapB)).isTrue();

    assertThat(multimapA).isEqualTo(multimapB);
  }

  @Test
  public void listMultimapIsEqualTo_fails() {
    ImmutableListMultimap<String, String> multimapA =
        ImmutableListMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "russell", "cobain")
            .build();
    ImmutableListMultimap<String, String> multimapB =
        ImmutableListMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "cobain", "russell")
            .build();

    assertThat(multimapA.equals(multimapB)).isFalse();

    expectFailure.whenTesting().that(multimapA).isEqualTo(multimapB);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{kurt=[kluever, russell, cobain]}> contains exactly "
                + "<{kurt=[kluever, cobain, russell]}> in order. "
                + "The values for keys <[kurt]> are not in order");
  }

  @Test
  public void setMultimapIsEqualTo_passes() {
    ImmutableSetMultimap<String, String> multimapA =
        ImmutableSetMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "russell", "cobain")
            .build();
    ImmutableSetMultimap<String, String> multimapB =
        ImmutableSetMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "cobain", "russell")
            .build();

    assertThat(multimapA.equals(multimapB)).isTrue();

    assertThat(multimapA).isEqualTo(multimapB);
  }

  @Test
  public void setMultimapIsEqualTo_fails() {
    ImmutableSetMultimap<String, String> multimapA =
        ImmutableSetMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "russell", "cobain")
            .build();
    ImmutableSetMultimap<String, String> multimapB =
        ImmutableSetMultimap.<String, String>builder().putAll("kurt", "kluever", "russell").build();

    assertThat(multimapA.equals(multimapB)).isFalse();

    expectFailure.whenTesting().that(multimapA).isEqualTo(multimapB);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{kurt=[kluever, russell, cobain]}> contains exactly "
                + "<{kurt=[kluever, russell]}>. It has unexpected items <{kurt=[cobain]}>");
  }

  @Test
  public void isEqualTo_failsWithSameToString() {
    expectFailure
        .whenTesting()
        .that(ImmutableMultimap.of(1, "a", 1, "b", 2, "c"))
        .isEqualTo(ImmutableMultimap.of(1L, "a", 1L, "b", 2L, "c"));
    AssertionError e = expectFailure.getFailure();
    assertWithMessage("Full message: %s", e.getMessage())
        .that(e)
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{1=[a, b], 2=[c]}> contains exactly <{1=[a, b], 2=[c]}>. It is "
                + "missing <[1=a, 1=b, 2=c] (Map.Entry<java.lang.Long, java.lang.String>)> and "
                + "has unexpected items "
                + "<[1=a, 1=b, 2=c] (Map.Entry<java.lang.Integer, java.lang.String>)>");
  }

  @Test
  public void multimapIsEmpty() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of();
    assertThat(multimap).isEmpty();
  }

  @Test
  public void multimapIsEmptyWithFailure() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of(1, 5);
    expectFailure.whenTesting().that(multimap).isEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{1=[5]}> is empty");
  }

  @Test
  public void multimapIsNotEmpty() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of(1, 5);
    assertThat(multimap).isNotEmpty();
  }

  @Test
  public void multimapIsNotEmptyWithFailure() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of();
    expectFailure.whenTesting().that(multimap).isNotEmpty();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{}> is not empty");
  }

  @Test
  public void multimapNamedValuesForKey() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of(1, 5);
    expectFailure.whenTesting().that(multimap).named("multymap").valuesForKey(1).containsExactly(4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that "
                + "<Values for key <1> (<[5]>) in multymap (<{1=[5]}>)> contains exactly <[4]>. "
                + "It is missing <[4]> and has unexpected items <[5]>");
  }

  @Test
  public void valuesForKeyNamed() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of(1, 5);
    expectFailure.whenTesting().that(multimap).valuesForKey(1).named("valuez").containsExactly(4);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that "
                + "valuez (<Values for key <1> (<[5]>) in <{1=[5]}>>) contains exactly <[4]>. "
                + "It is missing <[4]> and has unexpected items <[5]>");
  }

  @Test
  public void hasSize() {
    assertThat(ImmutableMultimap.of(1, 2, 3, 4)).hasSize(2);
  }

  @Test
  public void hasSizeZero() {
    assertThat(ImmutableMultimap.of()).hasSize(0);
  }

  @Test
  public void hasSizeNegative() {
    try {
      assertThat(ImmutableMultimap.of(1, 2)).hasSize(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void containsKey() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).containsKey("kurt");
  }

  @Test
  public void containsKeyFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    expectFailure.whenTesting().that(multimap).containsKey("daniel");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{kurt=[kluever]}> contains key <daniel>");
  }

  @Test
  public void containsKeyNull() {
    Multimap<String, String> multimap = HashMultimap.create();
    multimap.put(null, "null");
    assertThat(multimap).containsKey(null);
  }

  @Test
  public void containsKeyNullFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    expectFailure.whenTesting().that(multimap).containsKey(null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{kurt=[kluever]}> contains key <null>");
  }

  @Test
  public void containsKey_failsWithSameToString() {
    expectFailure
        .whenTesting()
        .that(ImmutableMultimap.of(1L, "value1a", 1L, "value1b", 2L, "value2", "1", "value3"))
        .containsKey(1);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{1=[value1a, value1b], 2=[value2], 1=[value3]}> contains key "
                + "<1 (java.lang.Integer)>. However, it does contain keys "
                + "<[1 (java.lang.Long), 1 (java.lang.String)]>.");
  }

  @Test
  public void doesNotContainKey() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).doesNotContainKey("daniel");
    assertThat(multimap).doesNotContainKey(null);
  }

  @Test
  public void doesNotContainKeyFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    expectFailure.whenTesting().that(multimap).doesNotContainKey("kurt");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{kurt=[kluever]}> does not contain key <kurt>");
  }

  @Test
  public void doesNotContainNullKeyFailure() {
    Multimap<String, String> multimap = HashMultimap.create();
    multimap.put(null, "null");
    expectFailure.whenTesting().that(multimap).doesNotContainKey(null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{null=[null]}> does not contain key <null>");
  }

  @Test
  public void containsEntry() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).containsEntry("kurt", "kluever");
  }

  @Test
  public void containsEntryFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    expectFailure.whenTesting().that(multimap).containsEntry("daniel", "ploch");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{kurt=[kluever]}> contains entry <daniel=ploch>");
  }

  @Test
  public void containsEntryWithNullValueNullExpected() {
    ListMultimap<String, String> actual = ArrayListMultimap.create();
    actual.put("a", null);
    assertThat(actual).containsEntry("a", null);
  }

  @Test
  public void failContainsEntry() {
    ImmutableMultimap<String, String> actual = ImmutableMultimap.of("a", "A");
    expectFailure.whenTesting().that(actual).containsEntry("a", "a");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{a=[A]}> contains entry <a=a>. "
                + "However, it has a mapping from <a> to <[A]>");
  }

  @Test
  public void failContainsEntryWithNullValuePresentExpected() {
    ListMultimap<String, String> actual = ArrayListMultimap.create();
    actual.put("a", null);
    expectFailure.whenTesting().that(actual).containsEntry("a", "A");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{a=[null]}> contains entry <a=A>. "
                + "However, it has a mapping from <a> to <[null]>");
  }

  @Test
  public void failContainsEntryWithPresentValueNullExpected() {
    ImmutableMultimap<String, String> actual = ImmutableMultimap.of("a", "A");
    expectFailure.whenTesting().that(actual).containsEntry("a", null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{a=[A]}> contains entry <a=null>. "
                + "However, it has a mapping from <a> to <[A]>");
  }

  @Test
  public void containsEntry_failsWithSameToString() throws Exception {
    expectFailure
        .whenTesting()
        .that(ImmutableMultimap.builder().put(1, "1").put(1, 1L).put(1L, 1).put(2, 3).build())
        .containsEntry(1, 1);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{1=[1, 1], 1=[1], 2=[3]}> contains entry "
                + "<1=1 (Map.Entry<java.lang.Integer, java.lang.Integer>)>. However, it does "
                + "contain entries <[1=1 (Map.Entry<java.lang.Integer, java.lang.String>), "
                + "1=1 (Map.Entry<java.lang.Integer, java.lang.Long>), "
                + "1=1 (Map.Entry<java.lang.Long, java.lang.Integer>)]>");
  }

  @Test
  public void doesNotContainEntry() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).doesNotContainEntry("daniel", "ploch");
  }

  @Test
  public void doesNotContainEntryFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    expectFailure.whenTesting().that(multimap).doesNotContainEntry("kurt", "kluever");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{kurt=[kluever]}> does not contain entry <kurt=kluever>");
  }

  @Test
  public void valuesForKey() {
    ImmutableMultimap<Integer, String> multimap =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(multimap).valuesForKey(3).hasSize(3);
    assertThat(multimap).valuesForKey(4).containsExactly("four", "five");
    assertThat(multimap).valuesForKey(3).containsAllOf("one", "six").inOrder();
    assertThat(multimap).valuesForKey(5).isEmpty();
  }

  @Test
  public void valuesForKeyListMultimap() {
    ImmutableListMultimap<Integer, String> multimap =
        ImmutableListMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(multimap).valuesForKey(4).isStrictlyOrdered();
  }

  @Test
  public void containsExactlyEntriesIn() {
    ImmutableListMultimap<Integer, String> listMultimap =
        ImmutableListMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableSetMultimap<Integer, String> setMultimap = ImmutableSetMultimap.copyOf(listMultimap);

    assertThat(listMultimap).containsExactlyEntriesIn(setMultimap);
  }

  @Test
  public void containsExactlyEmpty() {
    ImmutableListMultimap<Integer, String> actual = ImmutableListMultimap.of();
    ImmutableSetMultimap<Integer, String> expected = ImmutableSetMultimap.of();

    assertThat(actual).containsExactlyEntriesIn(expected);
    assertThat(actual).containsExactlyEntriesIn(expected).inOrder();
  }

  @Test
  public void containsExactlyRejectsNull() {
    ImmutableMultimap<Integer, String> multimap =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    try {
      assertThat(multimap).containsExactlyEntriesIn(null);
      fail("Should have thrown.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void containsExactlyRespectsDuplicates() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "two", 3, "one", 4, "five", 4, "five");
    ImmutableListMultimap<Integer, String> expected =
        ImmutableListMultimap.of(3, "two", 4, "five", 3, "one", 4, "five", 3, "one");

    assertThat(actual).containsExactlyEntriesIn(expected);
  }

  @Test
  public void containsExactlyRespectsDuplicatesFailure() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "two", 3, "one", 4, "five", 4, "five");
    ImmutableSetMultimap<Integer, String> expected = ImmutableSetMultimap.copyOf(actual);

    expectFailure.whenTesting().that(actual).containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s>. "
                    + "It has unexpected items <{3=[one], 4=[five]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyFailureMissing() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");

    expectFailure.whenTesting().that(actual).containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s>. "
                    + "It is missing <{3=[six], 4=[five]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyFailureExtra() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.put(4, "nine");
    actual.put(5, "eight");

    expectFailure.whenTesting().that(actual).containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s>. "
                    + "It has unexpected items <{4=[nine], 5=[eight]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyFailureBoth() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");
    actual.put(4, "nine");
    actual.put(5, "eight");

    expectFailure.whenTesting().that(actual).containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s>. "
                    + "It is missing <{3=[six], 4=[five]}> "
                    + "and has unexpected items <{4=[nine], 5=[eight]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyFailureWithEmptyStringMissing() {
    expectFailure.whenTesting().that(ImmutableMultimap.of()).containsExactly("", "a");

    assertThat(expectFailure.getFailure().getMessage())
        .isEqualTo(
            "Not true that <{}> contains exactly <{\"\" (empty String)=[a]}>. "
                + "It is missing <{\"\" (empty String)=[a]}>");
  }

  @Test
  public void containsExactlyFailureWithEmptyStringExtra() {
    expectFailure
        .whenTesting()
        .that(ImmutableMultimap.of("a", "", "", ""))
        .containsExactly("a", "");

    assertThat(expectFailure.getFailure().getMessage())
        .isEqualTo(
            "Not true that <{a=[], =[]}> contains exactly <{a=[\"\" (empty String)]}>. "
                + "It has unexpected items <{\"\" (empty String)=[\"\" (empty String)]}>");
  }

  @Test
  public void containsExactlyFailureWithEmptyStringBoth() {
    expectFailure.whenTesting().that(ImmutableMultimap.of("a", "")).containsExactly("", "a");

    assertThat(expectFailure.getFailure().getMessage())
        .isEqualTo(
            "Not true that <{a=[]}> contains exactly <{\"\" (empty String)=[a]}>. "
                + "It is missing <{\"\" (empty String)=[a]}> "
                + "and has unexpected items <{a=[\"\" (empty String)]}>");
  }

  @Test
  public void containsExactlyInOrder() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(actual).containsExactlyEntriesIn(expected).inOrder();
  }

  @Test
  public void containsExactlyInOrderDifferentTypes() {
    ImmutableListMultimap<Integer, String> listMultimap =
        ImmutableListMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableSetMultimap<Integer, String> setMultimap = ImmutableSetMultimap.copyOf(listMultimap);

    assertThat(listMultimap).containsExactlyEntriesIn(setMultimap).inOrder();
  }

  @Test
  public void containsExactlyInOrderFailure() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(4, "four", 3, "six", 4, "five", 3, "two", 3, "one");

    assertThat(actual).containsExactlyEntriesIn(expected);
    expectFailure.whenTesting().that(actual).containsExactlyEntriesIn(expected).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .startsWith(
            String.format("Not true that <%s> contains exactly <%s> in order. ", actual, expected));
  }

  @Test
  public void containsExactlyInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "six", 3, "two", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsExactlyEntriesIn(expected);
    expectFailure.whenTesting().that(actual).containsExactlyEntriesIn(expected).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s> in order. "
                    + "The values for keys <[3]> are not in order",
                actual, expected));
  }

  @Test
  public void containsExactlyVararg() {
    ImmutableListMultimap<Integer, String> listMultimap =
        ImmutableListMultimap.of(1, "one", 3, "six", 3, "two");

    assertThat(listMultimap).containsExactly(1, "one", 3, "six", 3, "two");
  }

  @Test
  public void containsExactlyVarargWithNull() {
    Multimap<Integer, String> listMultimap =
        LinkedListMultimap.create(ImmutableListMultimap.of(1, "one", 3, "six", 3, "two"));
    listMultimap.put(4, null);

    assertThat(listMultimap).containsExactly(1, "one", 3, "six", 3, "two", 4, null);
  }

  @Test
  public void containsExactlyVarargFailureMissing() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");

    expectFailure
        .whenTesting()
        .that(actual)
        .containsExactly(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s>. "
                    + "It is missing <{3=[six], 4=[five]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyVarargFailureExtra() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.put(4, "nine");
    actual.put(5, "eight");

    expectFailure
        .whenTesting()
        .that(actual)
        .containsExactly(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s>. "
                    + "It has unexpected items <{4=[nine], 5=[eight]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyVarargFailureBoth() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");
    actual.put(4, "nine");
    actual.put(5, "eight");

    expectFailure
        .whenTesting()
        .that(actual)
        .containsExactly(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s>. "
                    + "It is missing <{3=[six], 4=[five]}> "
                    + "and has unexpected items <{4=[nine], 5=[eight]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyVarargRespectsDuplicates() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "two", 3, "one", 4, "five", 4, "five");

    assertThat(actual).containsExactly(3, "two", 4, "five", 3, "one", 4, "five", 3, "one");
  }

  @Test
  public void containsExactlyVarargRespectsDuplicatesFailure() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "two", 3, "one", 4, "five", 4, "five");
    ImmutableSetMultimap<Integer, String> expected = ImmutableSetMultimap.copyOf(actual);

    expectFailure.whenTesting().that(actual).containsExactly(3, "one", 3, "two", 4, "five");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s>. "
                    + "It has unexpected items <{3=[one], 4=[five]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyVarargInOrder() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(actual)
        .containsExactly(3, "one", 3, "six", 3, "two", 4, "five", 4, "four")
        .inOrder();
  }

  @Test
  public void containsExactlyVarargInOrderFailure() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(4, "four", 3, "six", 4, "five", 3, "two", 3, "one");

    assertThat(actual).containsExactly(4, "four", 3, "six", 4, "five", 3, "two", 3, "one");
    expectFailure
        .whenTesting()
        .that(actual)
        .containsExactly(4, "four", 3, "six", 4, "five", 3, "two", 3, "one")
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .startsWith(
            String.format("Not true that <%s> contains exactly <%s> in order. ", actual, expected));
  }

  @Test
  public void containsExactlyVarargInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "six", 3, "two", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsExactly(3, "six", 3, "two", 3, "one", 4, "five", 4, "four");
    expectFailure
        .whenTesting()
        .that(actual)
        .containsExactly(3, "six", 3, "two", 3, "one", 4, "five", 4, "four")
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Not true that <%s> contains exactly <%s> in order. "
                    + "The values for keys <[3]> are not in order",
                actual, expected));
  }

  @Test
  public void containsExactlyEntriesIn_homogeneousMultimap_failsWithSameToString()
      throws Exception {
    expectFailure
        .whenTesting()
        .that(ImmutableMultimap.of(1, "a", 1, "b", 2, "c"))
        .containsExactlyEntriesIn(ImmutableMultimap.of(1L, "a", 1L, "b", 2L, "c"));
    assertWithMessage("Full message: %s", expectFailure.getFailure().getMessage())
        .that(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{1=[a, b], 2=[c]}> contains exactly <{1=[a, b], 2=[c]}>. It is "
                + "missing <[1=a, 1=b, 2=c] (Map.Entry<java.lang.Long, java.lang.String>)> and "
                + "has unexpected items "
                + "<[1=a, 1=b, 2=c] (Map.Entry<java.lang.Integer, java.lang.String>)>");
  }

  @Test
  public void containsExactlyEntriesIn_heterogeneousMultimap_failsWithSameToString()
      throws Exception {
    expectFailure
        .whenTesting()
        .that(ImmutableMultimap.of(1, "a", 1, "b", 2L, "c"))
        .containsExactlyEntriesIn(ImmutableMultimap.of(1L, "a", 1L, "b", 2, "c"));
    assertWithMessage("Full message: %s", expectFailure.getFailure().getMessage())
        .that(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{1=[a, b], 2=[c]}> contains exactly <{1=[a, b], 2=[c]}>. It is "
                + "missing <["
                + "1=a (Map.Entry<java.lang.Long, java.lang.String>), "
                + "1=b (Map.Entry<java.lang.Long, java.lang.String>), "
                + "2=c (Map.Entry<java.lang.Integer, java.lang.String>)]> "
                + "and has unexpected items <["
                + "1=a (Map.Entry<java.lang.Integer, java.lang.String>), "
                + "1=b (Map.Entry<java.lang.Integer, java.lang.String>), "
                + "2=c (Map.Entry<java.lang.Long, java.lang.String>)]>");
  }

  @Test
  public void comparingValuesUsing_containsEntry_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("def", 789);
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsExpectedKeyHasWrongValues() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("def", 123);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+456, +789]}> contains at least one entry with "
                + "key <def> and a value that parses to <123>. "
                + "However, it has a mapping from that key to <[+456, +789]>");
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsWrongKeyHasExpectedValue() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("xyz", 789);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+456, +789]}> contains at least one entry with "
                + "key <xyz> and a value that parses to <789>. "
                + "However, the following keys are mapped to such values: <[def]>");
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsMissingExpectedKeyAndValue() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("xyz", 321);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+456, +789]}> contains at least one entry with "
                + "key <xyz> and a value that parses to <321>");
  }

  @Test
  public void comparingValuesUsing_containsEntry_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", new Object());
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsEntry("def", 123);
      fail("Should have thrown.");
    } catch (ClassCastException expected) {
    }
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_successExcludeKeyHasWrongValues() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("def", 123);
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_successWrongKeyHasExcludedValue() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("xyz", 789);
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_successMissingExcludedKeyAndValue() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("xyz", 321);
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_failure() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("def", 789);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+456, +789]}> did not contain an entry with "
                + "key <def> and a value that parses to <789>. "
                + "It maps that key to the following such values: <[+789]>");
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", new Object());
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .doesNotContainEntry("def", 789);
      fail("Should have thrown.");
    } catch (ClassCastException expected) {
    }
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "def", 64, "abc", 123);
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_missingKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "def", 64, "abc", 123);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{def=[+64, 0x40, +128]}> contains exactly one element that has a "
                + "key that is equal to and a value that parses to the key and value of each "
                + "element of <[def=64, def=128, def=64, abc=123]>. It is missing an element "
                + "that has a key that is equal to and a value that parses to the key and value "
                + "of <abc=123>");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_extraKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "def", 64);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+64, 0x40, +128]}> contains exactly one element "
                + "that has a key that is equal to and a value that parses to the key and value "
                + "of each element of <[def=64, def=128, def=64]>. It has unexpected elements "
                + "<[abc=+123]>");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_wrongValueForKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "def", 128, "abc", 123);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    String expectedPreamble =
        "Not true that <{abc=[+123], def=[+64, 0x40, +128]}> contains exactly one element that "
            + "has a key that is equal to and a value that parses to the key and value of each "
            + "element of <[def=64, def=128, def=128, abc=123]>. It contains at least one "
            + "element that matches each expected element, and every element it contains matches "
            + "at least one expected element, but there was no 1:1 mapping between all the "
            + "actual and expected elements. Using the most complete 1:1 mapping (or one such "
            + "mapping, if there is a tie), it is missing an element that has a key that is "
            + "equal to and a value that parses to the key and value of <def=128> and has "
            + "unexpected elements ";
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isAnyOf(expectedPreamble + "<[def=+64]>", expectedPreamble + "<[def=0x40]>");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", new Object());
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 123, "def", 64, "abc", 123);
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactlyEntriesIn(expected);
      fail("Should have thrown.");
    } catch (ClassCastException e) {
      // expected
    }
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_inOrder_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("abc", 123, "def", 64, "def", 64, "def", 128);
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_inOrder_wrongKeyOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 64, "def", 128, "abc", 123);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected)
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+64, 0x40, +128]}> contains, in order, exactly one "
                + "element that has a key that is equal to and a value that parses to the key "
                + "and value of each element of <[def=64, def=64, def=128, abc=123]>");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_inOrder_wrongValueOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("abc", 123, "def", 64, "def", 128, "def", 64);
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected)
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+64, 0x40, +128]}> contains, in order, exactly one "
                + "element that has a key that is equal to and a value that parses to the key "
                + "and value of each element of <[abc=123, def=64, def=128, def=64]>");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_failsWithNamed() {
    ImmutableListMultimap<String, String> actual = ImmutableListMultimap.of("abc", "+123");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("abc", 123, "def", 456);
    expectFailure
        .whenTesting()
        .that(actual)
        .named("multymap")
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that multymap (<{abc=[+123]}>) contains exactly one element that has a key "
                + "that is equal to and a value that parses to the key and value of each element "
                + "of <[abc=123, def=456]>. It is missing an element that has a key that is "
                + "equal to and a value that parses to the key and value of <def=456>");
  }

  @Test
  public void comparingValuesUsing_containsExactly_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 128, "def", 64, "abc", 123);
  }

  @Test
  public void comparingValuesUsing_containsExactly_missingKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("def", "+64", "def", "0x40", "def", "+128");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 128, "def", 64, "abc", 123);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{def=[+64, 0x40, +128]}> contains exactly one element that has a "
                + "key that is equal to and a value that parses to the key and value of each "
                + "element of <[def=64, def=128, def=64, abc=123]>. It is missing an element "
                + "that has a key that is equal to and a value that parses to the key and value "
                + "of <abc=123>");
  }

  @Test
  public void comparingValuesUsing_containsExactly_extraKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 128, "def", 64);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+64, 0x40, +128]}> contains exactly one element "
                + "that has a key that is equal to and a value that parses to the key and value "
                + "of each element of <[def=64, def=128, def=64]>. It has unexpected elements "
                + "<[abc=+123]>");
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongValueForKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 128, "def", 128, "abc", 123);
    String expectedPreamble =
        "Not true that <{abc=[+123], def=[+64, 0x40, +128]}> contains exactly one element that "
            + "has a key that is equal to and a value that parses to the key and value of each "
            + "element of <[def=64, def=128, def=128, abc=123]>. It contains at least one "
            + "element that matches each expected element, and every element it contains matches "
            + "at least one expected element, but there was no 1:1 mapping between all the "
            + "actual and expected elements. Using the most complete 1:1 mapping (or one such "
            + "mapping, if there is a tie), it is missing an element that has a key that is "
            + "equal to and a value that parses to the key and value of <def=128> and has "
            + "unexpected elements ";
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isAnyOf(expectedPreamble + "<[def=+64]>", expectedPreamble + "<[def=0x40]>");
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", new Object());
    try {
      assertThat(actual)
          .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
          .containsExactly("def", 64, "def", 123, "def", 64, "abc", 123);
      fail("Should have thrown.");
    } catch (ClassCastException e) {
      // expected
    }
  }

  @Test
  public void comparingValuesUsing_containsExactly_inOrder_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("abc", 123, "def", 64, "def", 64, "def", 128);
  }

  @Test
  public void comparingValuesUsing_containsExactly_inOrder_wrongKeyOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 64, "def", 128, "abc", 123)
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+64, 0x40, +128]}> contains, in order, exactly one "
                + "element that has a key that is equal to and a value that parses to the key "
                + "and value of each element of <[def=64, def=64, def=128, abc=123]>");
  }

  @Test
  public void comparingValuesUsing_containsExactly_inOrder_wrongValueOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    expectFailure
        .whenTesting()
        .that(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("abc", 123, "def", 64, "def", 128, "def", 64)
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+64, 0x40, +128]}> contains, in order, exactly one "
                + "element that has a key that is equal to and a value that parses to the key "
                + "and value of each element of <[abc=123, def=64, def=128, def=64]>");
  }

  @Test
  public void comparingValuesUsing_containsExactly_failsWithNamed() {
    ImmutableListMultimap<String, String> actual = ImmutableListMultimap.of("abc", "+123");
    expectFailure
        .whenTesting()
        .that(actual)
        .named("multymap")
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("abc", 123, "def", 456);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that multymap (<{abc=[+123]}>) contains exactly one element that has a key "
                + "that is equal to and a value that parses to the key and value of each element "
                + "of <[abc=123, def=456]>. It is missing an element that has a key that is "
                + "equal to and a value that parses to the key and value of <def=456>");
  }
}
