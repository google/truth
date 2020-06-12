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

import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.truth.TestCorrespondences.CASE_INSENSITIVE_EQUALITY;
import static com.google.common.truth.TestCorrespondences.CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE;
import static com.google.common.truth.TestCorrespondences.STRING_PARSES_TO_INTEGER_CORRESPONDENCE;
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
public class MultimapSubjectTest extends BaseSubjectTestCase {

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

    expectFailureWhenTestingThat(multimapA).isEqualTo(multimapB);
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

    expectFailureWhenTestingThat(multimapA).isEqualTo(multimapB);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{kurt=[kluever, russell, cobain]}> contains exactly "
                + "<{kurt=[kluever, russell]}>. It has unexpected items <{kurt=[cobain]}>");
  }

  @Test
  public void isEqualTo_failsWithSameToString() {
    expectFailureWhenTestingThat(ImmutableMultimap.of(1, "a", 1, "b", 2, "c"))
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
    expectFailureWhenTestingThat(multimap).isEmpty();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void multimapIsNotEmpty() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of(1, 5);
    assertThat(multimap).isNotEmpty();
  }

  @Test
  public void multimapIsNotEmptyWithFailure() {
    ImmutableMultimap<Integer, Integer> multimap = ImmutableMultimap.of();
    expectFailureWhenTestingThat(multimap).isNotEmpty();
    assertFailureKeys("expected not to be empty");
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
    expectFailureWhenTestingThat(multimap).containsKey("daniel");
    assertFailureKeys("value of", "expected to contain", "but was", "multimap was");
    assertFailureValue("value of", "multimap.keySet()");
    assertFailureValue("expected to contain", "daniel");
    assertFailureValue("but was", "[kurt]");
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
    expectFailureWhenTestingThat(multimap).containsKey(null);
    assertFailureKeys("value of", "expected to contain", "but was", "multimap was");
    assertFailureValue("value of", "multimap.keySet()");
    assertFailureValue("expected to contain", "null");
    assertFailureValue("but was", "[kurt]");
  }

  @Test
  public void containsKey_failsWithSameToString() {
    expectFailureWhenTestingThat(
            ImmutableMultimap.of(1L, "value1a", 1L, "value1b", 2L, "value2", "1", "value3"))
        .containsKey(1);
    assertFailureKeys(
        "value of",
        "expected to contain",
        "an instance of",
        "but did not",
        "though it did contain",
        "full contents",
        "multimap was");
    assertFailureValue("value of", "multimap.keySet()");
    assertFailureValue("expected to contain", "1");
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
    expectFailureWhenTestingThat(multimap).doesNotContainKey("kurt");
    assertFailureKeys("value of", "expected not to contain", "but was", "multimap was");
    assertFailureValue("value of", "multimap.keySet()");
    assertFailureValue("expected not to contain", "kurt");
    assertFailureValue("but was", "[kurt]");
  }

  @Test
  public void doesNotContainNullKeyFailure() {
    Multimap<String, String> multimap = HashMultimap.create();
    multimap.put(null, "null");
    expectFailureWhenTestingThat(multimap).doesNotContainKey(null);
    assertFailureKeys("value of", "expected not to contain", "but was", "multimap was");
    assertFailureValue("value of", "multimap.keySet()");
    assertFailureValue("expected not to contain", "null");
    assertFailureValue("but was", "[null]");
  }

  @Test
  public void containsEntry() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    assertThat(multimap).containsEntry("kurt", "kluever");
  }

  @Test
  public void containsEntryFailure() {
    ImmutableMultimap<String, String> multimap = ImmutableMultimap.of("kurt", "kluever");
    expectFailureWhenTestingThat(multimap).containsEntry("daniel", "ploch");
    assertFailureKeys("expected to contain entry", "but was");
    assertFailureValue("expected to contain entry", "daniel=ploch");
    assertFailureValue("but was", "{kurt=[kluever]}");
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
    expectFailureWhenTestingThat(actual).containsEntry("a", "a");
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
    expectFailureWhenTestingThat(actual).containsEntry("a", "A");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{a=[null]}> contains entry <a=A>. "
                + "However, it has a mapping from <a> to <[null]>");
  }

  @Test
  public void failContainsEntryWithPresentValueNullExpected() {
    ImmutableMultimap<String, String> actual = ImmutableMultimap.of("a", "A");
    expectFailureWhenTestingThat(actual).containsEntry("a", null);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{a=[A]}> contains entry <a=null>. "
                + "However, it has a mapping from <a> to <[A]>");
  }

  @Test
  public void containsEntry_failsWithSameToString() throws Exception {
    expectFailureWhenTestingThat(
            ImmutableMultimap.builder().put(1, "1").put(1, 1L).put(1L, 1).put(2, 3).build())
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
    expectFailureWhenTestingThat(multimap).doesNotContainEntry("kurt", "kluever");
    assertFailureKeys("value of", "expected not to contain", "but was");
    assertFailureValue("value of", "multimap.entries()");
    assertFailureValue("expected not to contain", "kurt=kluever");
    assertFailureValue("but was", "[kurt=kluever]");
  }

  @Test
  public void valuesForKey() {
    ImmutableMultimap<Integer, String> multimap =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(multimap).valuesForKey(3).hasSize(3);
    assertThat(multimap).valuesForKey(4).containsExactly("four", "five");
    assertThat(multimap).valuesForKey(3).containsAtLeast("one", "six").inOrder();
    assertThat(multimap).valuesForKey(5).isEmpty();
  }

  @Test
  public void valuesForKeyListMultimap() {
    ImmutableListMultimap<Integer, String> multimap =
        ImmutableListMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(multimap).valuesForKey(4).isInStrictOrder();
  }

  @Test
  public void containsExactlyEntriesIn() {
    ImmutableListMultimap<Integer, String> listMultimap =
        ImmutableListMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableSetMultimap<Integer, String> setMultimap = ImmutableSetMultimap.copyOf(listMultimap);

    assertThat(listMultimap).containsExactlyEntriesIn(setMultimap);
  }

  @Test
  public void containsExactlyNoArg() {
    ImmutableMultimap<Integer, String> actual = ImmutableMultimap.of();

    assertThat(actual).containsExactly();
    assertThat(actual).containsExactly().inOrder();

    expectFailureWhenTestingThat(ImmutableMultimap.of(42, "Answer", 42, "6x7")).containsExactly();
    assertFailureKeys("expected to be empty", "but was");
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

    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
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

    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
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

    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
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

    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
                "Not true that <%s> contains exactly <%s>. "
                    + "It is missing <{3=[six], 4=[five]}> "
                    + "and has unexpected items <{4=[nine], 5=[eight]}>",
                actual, expected));
  }

  @Test
  public void containsExactlyFailureWithEmptyStringMissing() {
    expectFailureWhenTestingThat(ImmutableMultimap.of()).containsExactly("", "a");

    assertThat(expectFailure.getFailure().getMessage())
        .isEqualTo(
            "Not true that <{}> contains exactly <{\"\" (empty String)=[a]}>. "
                + "It is missing <{\"\" (empty String)=[a]}>");
  }

  @Test
  public void containsExactlyFailureWithEmptyStringExtra() {
    expectFailureWhenTestingThat(ImmutableMultimap.of("a", "", "", "")).containsExactly("a", "");

    assertThat(expectFailure.getFailure().getMessage())
        .isEqualTo(
            "Not true that <{a=[], =[]}> contains exactly <{a=[\"\" (empty String)]}>. "
                + "It has unexpected items <{\"\" (empty String)=[\"\" (empty String)]}>");
  }

  @Test
  public void containsExactlyFailureWithEmptyStringBoth() {
    expectFailureWhenTestingThat(ImmutableMultimap.of("a", "")).containsExactly("", "a");

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
    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .startsWith(
            lenientFormat("Not true that <%s> contains exactly <%s> in order. ", actual, expected));
  }

  @Test
  public void containsExactlyInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "six", 3, "two", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsExactlyEntriesIn(expected);
    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
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

    expectFailureWhenTestingThat(actual)
        .containsExactly(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
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

    expectFailureWhenTestingThat(actual)
        .containsExactly(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
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

    expectFailureWhenTestingThat(actual)
        .containsExactly(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
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

    expectFailureWhenTestingThat(actual).containsExactly(3, "one", 3, "two", 4, "five");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
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
    expectFailureWhenTestingThat(actual)
        .containsExactly(4, "four", 3, "six", 4, "five", 3, "two", 3, "one")
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .startsWith(
            lenientFormat("Not true that <%s> contains exactly <%s> in order. ", actual, expected));
  }

  @Test
  public void containsExactlyVarargInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "six", 3, "two", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsExactly(3, "six", 3, "two", 3, "one", 4, "five", 4, "four");
    expectFailureWhenTestingThat(actual)
        .containsExactly(3, "six", 3, "two", 3, "one", 4, "five", 4, "four")
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
                "Not true that <%s> contains exactly <%s> in order. "
                    + "The values for keys <[3]> are not in order",
                actual, expected));
  }

  @Test
  public void containsExactlyEntriesIn_homogeneousMultimap_failsWithSameToString()
      throws Exception {
    expectFailureWhenTestingThat(ImmutableMultimap.of(1, "a", 1, "b", 2, "c"))
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
    expectFailureWhenTestingThat(ImmutableMultimap.of(1, "a", 1, "b", 2L, "c"))
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
  public void containsAtLeastEntriesIn() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableSetMultimap<Integer, String> expected =
        ImmutableSetMultimap.of(3, "one", 3, "six", 3, "two", 4, "five");

    assertThat(actual).containsAtLeastEntriesIn(expected);
  }

  @Test
  public void containsAtLeastEmpty() {
    ImmutableListMultimap<Integer, String> actual = ImmutableListMultimap.of(3, "one");
    ImmutableSetMultimap<Integer, String> expected = ImmutableSetMultimap.of();

    assertThat(actual).containsAtLeastEntriesIn(expected);
    assertThat(actual).containsAtLeastEntriesIn(expected).inOrder();
  }

  @Test
  public void containsAtLeastRejectsNull() {
    ImmutableMultimap<Integer, String> multimap =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    try {
      assertThat(multimap).containsAtLeastEntriesIn(null);
      fail("Should have thrown.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void containsAtLeastRespectsDuplicates() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "two", 3, "one", 4, "five", 4, "five");
    ImmutableListMultimap<Integer, String> expected =
        ImmutableListMultimap.of(3, "two", 4, "five", 3, "one", 4, "five", 3, "one");

    assertThat(actual).containsAtLeastEntriesIn(expected);
  }

  @Test
  public void containsAtLeastRespectsDuplicatesFailure() {
    ImmutableListMultimap<Integer, String> expected =
        ImmutableListMultimap.of(3, "one", 3, "two", 3, "one", 4, "five", 4, "five");
    ImmutableSetMultimap<Integer, String> actual = ImmutableSetMultimap.copyOf(expected);

    expectFailureWhenTestingThat(actual).containsAtLeastEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
                "Not true that <%s> contains at least <%s>. "
                    + "It is missing <{3=[one], 4=[five]}>",
                actual, expected));
  }

  @Test
  public void containsAtLeastFailureMissing() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");
    actual.put(50, "hawaii");

    expectFailureWhenTestingThat(actual).containsAtLeastEntriesIn(expected);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
                "Not true that <%s> contains at least <%s>. "
                    + "It is missing <{3=[six], 4=[five]}>",
                actual, expected));
  }

  @Test
  public void containsAtLeastFailureWithEmptyStringMissing() {
    expectFailureWhenTestingThat(ImmutableMultimap.of("key", "value")).containsAtLeast("", "a");

    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{key=[value]}> contains at least <{\"\" (empty String)=[a]}>. "
                + "It is missing <{\"\" (empty String)=[a]}>");
  }

  @Test
  public void containsAtLeastInOrder() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 4, "five", 4, "four");

    assertThat(actual).containsAtLeastEntriesIn(expected).inOrder();
  }

  @Test
  public void containsAtLeastInOrderDifferentTypes() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableSetMultimap<Integer, String> expected =
        ImmutableSetMultimap.of(3, "one", 3, "six", 4, "five", 4, "four");

    assertThat(actual).containsAtLeastEntriesIn(expected).inOrder();
  }

  @Test
  public void containsAtLeastInOrderFailure() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(4, "four", 3, "six", 3, "two", 3, "one");

    assertThat(actual).containsAtLeastEntriesIn(expected);
    expectFailureWhenTestingThat(actual).containsAtLeastEntriesIn(expected).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .startsWith(
            lenientFormat(
                "Not true that <%s> contains at least <%s> in order. ", actual, expected));
  }

  @Test
  public void containsAtLeastInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "six", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsAtLeastEntriesIn(expected);
    expectFailureWhenTestingThat(actual).containsAtLeastEntriesIn(expected).inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
                "Not true that <%s> contains at least <%s> in order. "
                    + "The values for keys <[3]> are not in order",
                actual, expected));
  }

  @Test
  public void containsAtLeastVararg() {
    ImmutableListMultimap<Integer, String> listMultimap =
        ImmutableListMultimap.of(1, "one", 3, "six", 3, "two", 3, "one");

    assertThat(listMultimap).containsAtLeast(1, "one", 3, "six", 3, "two");
  }

  @Test
  public void containsAtLeastVarargWithNull() {
    Multimap<Integer, String> listMultimap =
        LinkedListMultimap.create(ImmutableListMultimap.of(1, "one", 3, "six", 3, "two"));
    listMultimap.put(4, null);

    assertThat(listMultimap).containsAtLeast(1, "one", 3, "two", 4, null);
  }

  @Test
  public void containsAtLeastVarargFailureMissing() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");
    actual.put(3, "nine");

    expectFailureWhenTestingThat(actual)
        .containsAtLeast(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
                "Not true that <%s> contains at least <%s>. "
                    + "It is missing <{3=[six], 4=[five]}>",
                actual, expected));
  }

  @Test
  public void containsAtLeastVarargRespectsDuplicates() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "two", 3, "one", 4, "five", 4, "five");

    assertThat(actual).containsAtLeast(3, "two", 4, "five", 3, "one", 3, "one");
  }

  @Test
  public void containsAtLeastVarargRespectsDuplicatesFailure() {
    ImmutableListMultimap<Integer, String> actual =
        ImmutableListMultimap.of(3, "one", 3, "two", 4, "five", 4, "five");
    ImmutableListMultimap<Integer, String> expected =
        ImmutableListMultimap.of(3, "one", 3, "one", 3, "one", 4, "five");

    expectFailureWhenTestingThat(actual).containsAtLeast(3, "one", 3, "one", 3, "one", 4, "five");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
                "Not true that <%s> contains at least <%s>. "
                    + "It is missing <{3=[one [2 copies]]}>",
                actual, expected));
  }

  @Test
  public void containsAtLeastVarargInOrder() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(actual).containsAtLeast(3, "one", 3, "six", 4, "five", 4, "four").inOrder();
  }

  @Test
  public void containsAtLeastVarargInOrderFailure() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(4, "four", 3, "six", 3, "two", 3, "one");

    assertThat(actual).containsAtLeast(4, "four", 3, "six", 3, "two", 3, "one");
    expectFailureWhenTestingThat(actual)
        .containsAtLeast(4, "four", 3, "six", 3, "two", 3, "one")
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .startsWith(
            lenientFormat(
                "Not true that <%s> contains at least <%s> in order. ", actual, expected));
  }

  @Test
  public void containsAtLeastVarargInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "two", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsAtLeast(3, "two", 3, "one", 4, "five", 4, "four");
    expectFailureWhenTestingThat(actual)
        .containsAtLeast(3, "two", 3, "one", 4, "five", 4, "four")
        .inOrder();
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            lenientFormat(
                "Not true that <%s> contains at least <%s> in order. "
                    + "The values for keys <[3]> are not in order",
                actual, expected));
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
    expectFailureWhenTestingThat(actual)
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
    expectFailureWhenTestingThat(actual)
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("xyz", 321);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{abc=[+123], def=[+456, +789]}> contains at least one entry with "
                + "key <xyz> and a value that parses to <321>");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesException_expectedKeyHasWrongValues() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(2, "two");
    actual.put(2, "deux");
    actual.put(2, null);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsEntry(2, "ZWEI");
    // The test fails because the expected key doesn't have a match for the expected value. We are
    // bound also to hit a NPE from compare(null, ZWEI) along the way, and should also report that.
    assertFailureKeys(
        "Not true that <{1=[one], 2=[two, deux, null]}> contains at least one entry with key <2> "
            + "and a value that equals (ignoring case) <ZWEI>. However, it has a mapping from that "
            + "key to <[two, deux, null]>",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ZWEI) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesException_wrongKeyHasExpectedValue() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(3, "two");
    actual.put(3, null);
    actual.put(3, "zwei");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsEntry(2, "ZWEI");
    // The test fails and does not contain the expected key, but does contain the expected value
    // we the wrong key. We are bound also to hit a NPE from compare(null, ZWEI) along the way, and
    // should also report that.
    assertFailureKeys(
        "Not true that <{1=[one], 3=[two, null, zwei]}> contains at least one entry with key <2> "
            + "and a value that equals (ignoring case) <ZWEI>. However, the following keys are "
            + "mapped to such values: <[3]>",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ZWEI) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsEntry_handlesException_alwaysFails() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(2, "two");
    actual.put(2, null);
    actual.put(2, "zwei");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsEntry(2, "ZWEI");
    // The multimap does contain the expected entry, but no reasonable implementation could find
    // it without hitting the NPE from compare(null, ZWEI) first, so we are contractually required
    // to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing values",
        "first exception",
        "comparing contents by testing that at least one entry had a key equal to the expected key "
            + "and a value that equals (ignoring case) the expected value",
        "expected key",
        "expected value",
        "but was");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ZWEI) threw java.lang.NullPointerException");
    assertFailureValue("expected key", "2");
    assertFailureValue("expected value", "ZWEI");
  }

  @Test
  public void comparingValuesUsing_containsEntry_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", 789);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("def", 789);
    assertFailureKeys(
        "Not true that <{abc=[+123], def=[+456, 789]}> contains at least one entry with key <def> "
            + "and a value that parses to <789>. However, it has a mapping from that key to "
            + "<[+456, 789]>",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(789, 789) threw java.lang.ClassCastException");
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
    expectFailureWhenTestingThat(actual)
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
  public void comparingValuesUsing_doesNotContainEntry_handlesException_didContainEntry() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(2, "two");
    actual.put(2, null);
    actual.put(2, "zwei");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .doesNotContainEntry(2, "ZWEI");
    // The test fails because it does contain the expected entry. We are bound to also hit the NPE
    // from compare(null, ZWEI) along the way, and should also report that.
    assertFailureKeys(
        "Not true that <{1=[one], 2=[two, null, zwei]}> did not contain an entry with key <2> and "
            + "a value that equals (ignoring case) <ZWEI>. It maps that key to the following such "
            + "values: <[zwei]>",
        "additionally, one or more exceptions were thrown while comparing values",
        "first exception");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ZWEI) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_handlesException_didNotContainEntry() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(2, "two");
    actual.put(2, "deux");
    actual.put(2, null);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .doesNotContainEntry(2, "ZWEI");
    // The test would pass if compare(null, ZWEI) returned false. But it actually throws NPE, and
    // we are bound to hit that, so we are contractually required to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing values",
        "first exception",
        "comparing contents by testing that no entry had the forbidden key and a value that "
            + "equals (ignoring case) the forbidden value",
        "forbidden key",
        "forbidden value",
        "but was");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ZWEI) threw java.lang.NullPointerException");
    assertFailureValue("forbidden key", "2");
    assertFailureValue("forbidden value", "ZWEI");
  }

  @Test
  public void comparingValuesUsing_doesNotContainEntry_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", 789);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .doesNotContainEntry("def", 789);
    assertFailureKeys(
        "one or more exceptions were thrown while comparing values",
        "first exception",
        "comparing contents by testing that no entry had the forbidden key and a value that "
            + "parses to the forbidden value",
        "forbidden key",
        "forbidden value",
        "but was");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(789, 789) threw java.lang.ClassCastException");
    assertFailureValue("forbidden key", "def");
    assertFailureValue("forbidden value", "789");
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys("missing (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (1)", "abc=123");
    // TODO(b/69154276): Address the fact that we show "expected" as a list of entries and "but was"
    // as a multimap, which looks a bit odd.
    assertFailureValue("expected", "[def=64, def=128, def=64, abc=123]");
    assertFailureValue(
        "testing whether",
        "actual element has a key that is equal to and a value that parses to the key and value of"
            + " expected element");
    assertFailureValue("but was", "{def=[+64, 0x40, +128]}");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_extraKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "def", 64);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys("unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("unexpected (1)", "abc=+123");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_wrongValueForKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "def", 128, "abc", 123);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and the actual elements,"
            + " each actual element matches as least one expected element, and vice versa, but"
            + " there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "def=128");
    assertThatFailure().factValue("unexpected (1)").isAnyOf("[def=+64]", "[def=0x40]");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_handlesException() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(2, null);
    actual.put(2, "deux");
    actual.put(2, "zwei");
    ImmutableListMultimap<Integer, String> expected =
        ImmutableListMultimap.of(1, "ONE", 2, "TWO", 2, "DEUX", 2, "ZWEI");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("missing (1)", "2=TWO");
    assertFailureValue("unexpected (1)", "[2=null]");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(2=null, 2=TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_handlesException_alwaysFails() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(2, null);
    actual.put(2, "two");
    actual.put(2, "deux");
    ListMultimap<Integer, String> expected = LinkedListMultimap.create();
    expected.put(1, "ONE");
    expected.put(2, "TWO");
    expected.put(2, "DEUX");
    expected.put(2, null);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE)
        .containsExactlyEntriesIn(expected);
    // CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE.compare(null, null) returns true, so there is a
    // mapping between actual and expected entries where they all correspond. However, no
    // reasonable implementation would find that mapping without hitting the (null, "TWO") case
    // along the way, and that throws NPE, so we are contractually required to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing elements",
        "first exception",
        "expected",
        "testing whether",
        "found all expected elements (but failing because of exception)",
        "full contents");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(2=null, 2=TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.<String, Object>of(
            "abc", "+123", "def", "+64", "def", "0x40", "def", 999);
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 123, "def", 64, "abc", 123);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected);
    assertFailureKeys(
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("missing (1)", "def=123");
    assertFailureValue("unexpected (1)", "[def=999]");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(def=999, def=64) threw java.lang.ClassCastException");
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected)
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong", "expected", "testing whether", "but was");
    assertFailureValue("expected", "[def=64, def=64, def=128, abc=123]");
  }

  @Test
  public void comparingValuesUsing_containsExactlyEntriesIn_inOrder_wrongValueOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("abc", 123, "def", 64, "def", 128, "def", 64);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactlyEntriesIn(expected)
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong", "expected", "testing whether", "but was");
    assertFailureValue("expected", "[abc=123, def=64, def=128, def=64]");
  }

  @Test
  public void comparingValuesUsing_containsExactlyNoArgs() {
    ImmutableListMultimap<String, String> actual = ImmutableListMultimap.of();

    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly();
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly()
        .inOrder();

    expectFailureWhenTestingThat(ImmutableListMultimap.of("abc", "+123"))
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly();
    assertFailureKeys("expected to be empty", "but was");
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 128, "def", 64, "abc", 123);
    assertFailureKeys("missing (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("missing (1)", "abc=123");
  }

  @Test
  public void comparingValuesUsing_containsExactly_extraKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 128, "def", 64);
    assertFailureKeys("unexpected (1)", "---", "expected", "testing whether", "but was");
    assertFailureValue("unexpected (1)", "abc=+123");
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongValueForKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 128, "def", 128, "abc", 123);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and the actual elements,"
            + " each actual element matches as least one expected element, and vice versa, but"
            + " there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "def=128");
    assertThatFailure().factValue("unexpected (1)").isAnyOf("[def=+64]", "[def=0x40]");
  }

  @Test
  public void comparingValuesUsing_containsExactly_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.<String, Object>of(
            "abc", "+123", "def", "+64", "def", "0x40", "def", 999);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 123, "def", 64, "abc", 123);
    assertFailureKeys(
        "missing (1)",
        "unexpected (1)",
        "---",
        "expected",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("missing (1)", "def=123");
    assertFailureValue("unexpected (1)", "[def=999]");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(def=999, def=64) threw java.lang.ClassCastException");
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
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("def", 64, "def", 64, "def", 128, "abc", 123)
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong", "expected", "testing whether", "but was");
    assertFailureValue("expected", "[def=64, def=64, def=128, abc=123]");
  }

  @Test
  public void comparingValuesUsing_containsExactly_inOrder_wrongValueOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly("abc", 123, "def", 64, "def", 128, "def", 64)
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong", "expected", "testing whether", "but was");
    assertFailureValue("expected", "[abc=123, def=64, def=128, def=64]");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "abc", 123);
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_missingKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("def", "+64", "def", "0x40", "def", "+128", "abc", "+99");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "def", 64, "abc", 123);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
        "missing (1)", "---", "expected to contain at least", "testing whether", "but was");
    assertFailureValue("missing (1)", "abc=123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_wrongValueForKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+64", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 128, "def", 128, "abc", 123);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and a subset of the actual"
            + " elements, each actual element matches as least one expected element, and vice"
            + " versa, but there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "def=128");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_handlesException() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(2, null);
    actual.put(2, "deux");
    actual.put(2, "zwei");
    ImmutableListMultimap<Integer, String> expected =
        ImmutableListMultimap.of(1, "ONE", 2, "TWO", 2, "DEUX");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("missing (1)", "2=TWO");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(2=null, 2=TWO) threw java.lang.NullPointerException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_handlesException_alwaysFails() {
    ListMultimap<Integer, String> actual = LinkedListMultimap.create();
    actual.put(1, "one");
    actual.put(2, null);
    actual.put(2, "two");
    actual.put(2, "deux");
    ListMultimap<Integer, String> expected = LinkedListMultimap.create();
    expected.put(1, "ONE");
    expected.put(2, "TWO");
    expected.put(2, "DEUX");
    expected.put(2, null);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE)
        .containsAtLeastEntriesIn(expected);
    // CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE.compare(null, null) returns true, so there is a
    // mapping between actual and expected entries where they all correspond. However, no
    // reasonable implementation would find that mapping without hitting the (null, "TWO") case
    // along the way, and that throws NPE, so we are contractually required to fail.
    assertFailureKeys(
        "one or more exceptions were thrown while comparing elements",
        "first exception",
        "expected to contain at least",
        "testing whether",
        "found all expected elements (but failing because of exception)",
        "full contents");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(2=null, 2=TWO) threw java.lang.NullPointerException");
    assertFailureValue("expected to contain at least", "[1=ONE, 2=TWO, 2=DEUX, 2=null]");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.<String, Object>of(
            "abc", "+123", "def", "+64", "def", "0x40", "def", 999);
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 123, "def", 64, "abc", 123);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
    assertFailureKeys(
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("missing (1)", "def=123");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(def=999, def=64) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_inOrder_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of(
            "def", "+64", "abc", "+123", "def", "0x40", "m", "+1", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 64, "def", 128, "abc", 123);
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected);
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_inOrder_wrongKeyOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of(
            "abc", "+123", "def", "+64", "m", "+1", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("def", 64, "def", 64, "def", 128, "abc", 123);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected)
        .inOrder();
    assertFailureKeys(
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "testing whether",
        "but was");
    assertFailureValue(
        "expected order for required elements", "[def=64, def=64, def=128, abc=123]");
  }

  @Test
  public void comparingValuesUsing_containsAtLeastEntriesIn_inOrder_wrongValueOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of(
            "abc", "+123", "def", "+64", "m", "+1", "def", "0x40", "def", "+128");
    ImmutableListMultimap<String, Integer> expected =
        ImmutableListMultimap.of("abc", 123, "def", 64, "def", 128, "def", 64);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeastEntriesIn(expected)
        .inOrder();
    assertFailureKeys(
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "testing whether",
        "but was");
    assertFailureValue(
        "expected order for required elements", "[abc=123, def=64, def=128, def=64]");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of(
            "abc", "+123", "def", "+64", "m", "+1", "def", "0x40", "def", "+128");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 64, "def", 128, "def", 64, "abc", 123);
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_missingKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("def", "+64", "def", "0x40", "m", "+1", "def", "+128");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 64, "def", 128, "def", 64, "abc", 123);
    assertFailureKeys(
        "missing (1)", "---", "expected to contain at least", "testing whether", "but was");
    assertFailureValue("missing (1)", "abc=123");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_wrongValueForKey() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of(
            "abc", "+123", "def", "+64", "m", "+1", "def", "0x40", "def", "+128");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 64, "def", 128, "def", 128, "abc", 123);
    assertFailureKeys(
        "in an assertion requiring a 1:1 mapping between the expected and a subset of the actual"
            + " elements, each actual element matches as least one expected element, and vice"
            + " versa, but there was no 1:1 mapping",
        "using the most complete 1:1 mapping (or one such mapping, if there is a tie)",
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was");
    assertFailureValue("missing (1)", "def=128");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.<String, Object>of(
            "abc", "+123", "def", "+64", "def", "0x40", "def", 999, "m", "+1");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 64, "def", 123, "def", 64, "abc", 123);
    assertFailureKeys(
        "missing (1)",
        "---",
        "expected to contain at least",
        "testing whether",
        "but was",
        "additionally, one or more exceptions were thrown while comparing elements",
        "first exception");
    assertFailureValue("missing (1)", "def=123");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(def=999, def=64) threw java.lang.ClassCastException");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_inOrder_success() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of(
            "abc", "+123", "def", "+64", "m", "+1", "def", "0x40", "def", "+128");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("abc", 123, "def", 64, "def", 64, "def", 128);
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_inOrder_wrongKeyOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of(
            "abc", "+123", "def", "+64", "def", "0x40", "m", "+1", "def", "+128");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("def", 64, "def", 64, "def", 128, "abc", 123)
        .inOrder();
    assertFailureKeys(
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "testing whether",
        "but was");
    assertFailureValue(
        "expected order for required elements", "[def=64, def=64, def=128, abc=123]");
  }

  @Test
  public void comparingValuesUsing_containsAtLeast_inOrder_wrongValueOrder() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of(
            "abc", "+123", "m", "+1", "def", "+64", "def", "0x40", "def", "+128");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsAtLeast("abc", 123, "def", 64, "def", 128, "def", 64)
        .inOrder();
    assertFailureKeys(
        "required elements were all found, but order was wrong",
        "expected order for required elements",
        "testing whether",
        "but was");
    assertFailureValue(
        "expected order for required elements", "[abc=123, def=64, def=128, def=64]");
  }

  private MultimapSubject expectFailureWhenTestingThat(Multimap<?, ?> actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
