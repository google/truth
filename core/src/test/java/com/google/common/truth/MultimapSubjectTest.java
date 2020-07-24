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

import static com.google.common.truth.TestCorrespondences.CASE_INSENSITIVE_EQUALITY;
import static com.google.common.truth.TestCorrespondences.CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE;
import static com.google.common.truth.TestCorrespondences.STRING_PARSES_TO_INTEGER_CORRESPONDENCE;
import static com.google.common.truth.Truth.assertThat;
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

    expectFailureWhenTestingThat(multimapA).isEqualTo(multimapB);
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys with out-of-order values",
        "---",
        "expected",
        "but was");
    assertFailureValue("keys with out-of-order values", "[kurt]");
    assertFailureValue("expected", "{kurt=[kluever, cobain, russell]}");
    assertFailureValue("but was", "{kurt=[kluever, russell, cobain]}");
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

    expectFailureWhenTestingThat(multimapA).isEqualTo(multimapB);
    assertFailureKeys("unexpected", "---", "expected", "but was");
    assertFailureValue("unexpected", "{kurt=[cobain]}");
    assertFailureValue("expected", "{kurt=[kluever, russell]}");
    assertFailureValue("but was", "{kurt=[kluever, russell, cobain]}");
  }

  @Test
  public void setMultimapIsEqualToListMultimap_fails() {
    ImmutableSetMultimap<String, String> multimapA =
        ImmutableSetMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "russell", "cobain")
            .build();
    ImmutableListMultimap<String, String> multimapB =
        ImmutableListMultimap.<String, String>builder()
            .putAll("kurt", "kluever", "russell", "cobain")
            .build();
    expectFailureWhenTestingThat(multimapA).isEqualTo(multimapB);
    assertFailureKeys(
        "expected",
        "an instance of",
        "but was",
        "an instance of",
        "a SetMultimap cannot equal a ListMultimap if either is non-empty");
    assertFailureValueIndexed("an instance of", 0, "ListMultimap");
    assertFailureValueIndexed("an instance of", 1, "SetMultimap");
  }

  @Test
  public void isEqualTo_failsWithSameToString() {
    expectFailureWhenTestingThat(ImmutableMultimap.of(1, "a", 1, "b", 2, "c"))
        .isEqualTo(ImmutableMultimap.of(1L, "a", 1L, "b", 2L, "c"));
    assertFailureKeys("missing", "unexpected", "---", "expected", "but was");
    assertFailureValue("missing", "[1=a, 1=b, 2=c] (Map.Entry<java.lang.Long, java.lang.String>)");
    assertFailureValue(
        "unexpected", "[1=a, 1=b, 2=c] (Map.Entry<java.lang.Integer, java.lang.String>)");
    assertFailureValue("expected", "{1=[a, b], 2=[c]}");
    assertFailureValue("but was", "{1=[a, b], 2=[c]}");
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
    expectFailureWhenTestingThat(actual).containsEntry("b", "B");
    assertFailureKeys("expected to contain entry", "but was");
    assertFailureValue("expected to contain entry", "b=B");
    assertFailureValue("but was", "{a=[A]}");
  }

  @Test
  public void failContainsEntryFailsWithWrongValueForKey() {
    ImmutableMultimap<String, String> actual = ImmutableMultimap.of("a", "A");
    expectFailureWhenTestingThat(actual).containsEntry("a", "a");
    assertFailureKeys(
        "expected to contain entry",
        "but did not",
        "though it did contain values with that key",
        "full contents");
    assertFailureValue("though it did contain values with that key", "[A]");
  }

  @Test
  public void failContainsEntryWithNullValuePresentExpected() {
    ListMultimap<String, String> actual = ArrayListMultimap.create();
    actual.put("a", null);
    expectFailureWhenTestingThat(actual).containsEntry("a", "A");
    assertFailureKeys(
        "expected to contain entry",
        "but did not",
        "though it did contain values with that key",
        "full contents");
    assertFailureValue("though it did contain values with that key", "[null]");
  }

  @Test
  public void failContainsEntryWithPresentValueNullExpected() {
    ImmutableMultimap<String, String> actual = ImmutableMultimap.of("a", "A");
    expectFailureWhenTestingThat(actual).containsEntry("a", null);
    assertFailureKeys(
        "expected to contain entry",
        "but did not",
        "though it did contain values with that key",
        "full contents");
    assertFailureValue("expected to contain entry", "a=null");
  }

  @Test
  public void failContainsEntryFailsWithWrongKeyForValue() {
    ImmutableMultimap<String, String> actual = ImmutableMultimap.of("a", "A");
    expectFailureWhenTestingThat(actual).containsEntry("b", "A");
    assertFailureKeys(
        "expected to contain entry",
        "but did not",
        "though it did contain keys with that value",
        "full contents");
    assertFailureValue("though it did contain keys with that value", "[a]");
  }

  @Test
  public void containsEntry_failsWithSameToString() throws Exception {
    expectFailureWhenTestingThat(
            ImmutableMultimap.builder().put(1, "1").put(1, 1L).put(1L, 1).put(2, 3).build())
        .containsEntry(1, 1);
    assertFailureKeys(
        "expected to contain entry",
        "an instance of",
        "but did not",
        "though it did contain",
        "full contents");
    assertFailureValue("expected to contain entry", "1=1");
    assertFailureValue("an instance of", "Map.Entry<java.lang.Integer, java.lang.Integer>");
    assertFailureValue(
        "though it did contain",
        "[1=1 (Map.Entry<java.lang.Integer, java.lang.String>), "
            + "1=1 (Map.Entry<java.lang.Integer, java.lang.Long>), "
            + "1=1 (Map.Entry<java.lang.Long, java.lang.Integer>)]");
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
    assertFailureKeys("unexpected", "---", "expected", "but was");
    assertFailureValue("unexpected", "{3=[one], 4=[five]}");
    assertFailureValue("expected", "{3=[one, two], 4=[five]}");
    assertFailureValue("but was", "{3=[one, two, one], 4=[five, five]}");
  }

  @Test
  public void containsExactlyFailureMissing() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.remove(3, "six");
    actual.remove(4, "five");

    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected);
    assertFailureKeys("missing", "---", "expected", "but was");
    assertFailureValue("missing", "{3=[six], 4=[five]}");
  }

  @Test
  public void containsExactlyFailureExtra() {
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ListMultimap<Integer, String> actual = LinkedListMultimap.create(expected);
    actual.put(4, "nine");
    actual.put(5, "eight");

    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected);
    assertFailureKeys("unexpected", "---", "expected", "but was");
    assertFailureValue("unexpected", "{4=[nine], 5=[eight]}");
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
    assertFailureKeys("missing", "unexpected", "---", "expected", "but was");
    assertFailureValue("missing", "{3=[six], 4=[five]}");
    assertFailureValue("unexpected", "{4=[nine], 5=[eight]}");
  }

  @Test
  public void containsExactlyFailureWithEmptyStringMissing() {
    expectFailureWhenTestingThat(ImmutableMultimap.of()).containsExactly("", "a");
    assertFailureKeys("missing", "---", "expected", "but was");
    assertFailureValue("missing", "{\"\" (empty String)=[a]}");
    assertFailureValue("expected", "{\"\" (empty String)=[a]}");
    assertFailureValue("but was", "{}");
  }

  @Test
  public void containsExactlyFailureWithEmptyStringExtra() {
    expectFailureWhenTestingThat(ImmutableMultimap.of("a", "", "", "")).containsExactly("a", "");
    assertFailureKeys("unexpected", "---", "expected", "but was");
    assertFailureValue("unexpected", "{\"\" (empty String)=[\"\" (empty String)]}");
    assertFailureValue("expected", "{a=[\"\" (empty String)]}");
    assertFailureValue("but was", "{a=[], =[]}");
  }

  @Test
  public void containsExactlyFailureWithEmptyStringBoth() {
    expectFailureWhenTestingThat(ImmutableMultimap.of("a", "")).containsExactly("", "a");
    assertFailureKeys("missing", "unexpected", "---", "expected", "but was");
    assertFailureValue("missing", "{\"\" (empty String)=[a]}");
    assertFailureValue("unexpected", "{a=[\"\" (empty String)]}");
    assertFailureValue("expected", "{\"\" (empty String)=[a]}");
    assertFailureValue("but was", "{a=[]}");
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
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys are not in order",
        "keys with out-of-order values",
        "---",
        "expected",
        "but was");
    assertFailureValue("keys with out-of-order values", "[4, 3]");
  }

  @Test
  public void containsExactlyInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "six", 3, "two", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsExactlyEntriesIn(expected);
    expectFailureWhenTestingThat(actual).containsExactlyEntriesIn(expected).inOrder();
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys with out-of-order values",
        "---",
        "expected",
        "but was");
    assertFailureValue("keys with out-of-order values", "[3]");
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
    assertFailureKeys("missing", "---", "expected", "but was");
    assertFailureValue("missing", "{3=[six], 4=[five]}");
    assertFailureValue("expected", "{3=[one, six, two], 4=[five, four]}");
    assertFailureValue("but was", "{3=[one, two], 4=[four]}");
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
    assertFailureKeys("unexpected", "---", "expected", "but was");
    assertFailureValue("unexpected", "{4=[nine], 5=[eight]}");
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
    assertFailureKeys("missing", "unexpected", "---", "expected", "but was");
    assertFailureValue("missing", "{3=[six], 4=[five]}");
    assertFailureValue("unexpected", "{4=[nine], 5=[eight]}");
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

    expectFailureWhenTestingThat(actual).containsExactly(3, "one", 3, "two", 4, "five");
    assertFailureKeys("unexpected", "---", "expected", "but was");
    assertFailureValue("unexpected", "{3=[one], 4=[five]}");
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

    assertThat(actual).containsExactly(4, "four", 3, "six", 4, "five", 3, "two", 3, "one");
    expectFailureWhenTestingThat(actual)
        .containsExactly(4, "four", 3, "six", 4, "five", 3, "two", 3, "one")
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys are not in order",
        "keys with out-of-order values",
        "---",
        "expected",
        "but was");
    assertFailureValue("keys with out-of-order values", "[4, 3]");
  }

  @Test
  public void containsExactlyVarargInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(actual).containsExactly(3, "six", 3, "two", 3, "one", 4, "five", 4, "four");
    expectFailureWhenTestingThat(actual)
        .containsExactly(3, "six", 3, "two", 3, "one", 4, "five", 4, "four")
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys with out-of-order values",
        "---",
        "expected",
        "but was");
    assertFailureValue("keys with out-of-order values", "[3]");
  }

  @Test
  public void containsExactlyEntriesIn_homogeneousMultimap_failsWithSameToString()
      throws Exception {
    expectFailureWhenTestingThat(ImmutableMultimap.of(1, "a", 1, "b", 2, "c"))
        .containsExactlyEntriesIn(ImmutableMultimap.of(1L, "a", 1L, "b", 2L, "c"));
    assertFailureKeys("missing", "unexpected", "---", "expected", "but was");
    assertFailureValue("missing", "[1=a, 1=b, 2=c] (Map.Entry<java.lang.Long, java.lang.String>)");
    assertFailureValue(
        "unexpected", "[1=a, 1=b, 2=c] (Map.Entry<java.lang.Integer, java.lang.String>)");
  }

  @Test
  public void containsExactlyEntriesIn_heterogeneousMultimap_failsWithSameToString()
      throws Exception {
    expectFailureWhenTestingThat(ImmutableMultimap.of(1, "a", 1, "b", 2L, "c"))
        .containsExactlyEntriesIn(ImmutableMultimap.of(1L, "a", 1L, "b", 2, "c"));
    assertFailureKeys("missing", "unexpected", "---", "expected", "but was");
    assertFailureValue(
        "missing",
        "[1=a (Map.Entry<java.lang.Long, java.lang.String>), "
            + "1=b (Map.Entry<java.lang.Long, java.lang.String>), "
            + "2=c (Map.Entry<java.lang.Integer, java.lang.String>)]");
    assertFailureValue(
        "unexpected",
        "[1=a (Map.Entry<java.lang.Integer, java.lang.String>), "
            + "1=b (Map.Entry<java.lang.Integer, java.lang.String>), "
            + "2=c (Map.Entry<java.lang.Long, java.lang.String>)]");
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
    assertFailureKeys("missing", "---", "expected to contain at least", "but was");
    assertFailureValue("missing", "{3=[one], 4=[five]}");
    assertFailureValue("expected to contain at least", "{3=[one, two, one], 4=[five, five]}");
    assertFailureValue("but was", "{3=[one, two], 4=[five]}");
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
    assertFailureKeys("missing", "---", "expected to contain at least", "but was");
    assertFailureValue("missing", "{3=[six], 4=[five]}");
  }

  @Test
  public void containsAtLeastFailureWithEmptyStringMissing() {
    expectFailureWhenTestingThat(ImmutableMultimap.of("key", "value")).containsAtLeast("", "a");
    assertFailureKeys("missing", "---", "expected to contain at least", "but was");
    assertFailureValue("missing", "{\"\" (empty String)=[a]}");
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
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys are not in order",
        "keys with out-of-order values",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue("keys with out-of-order values", "[3]");
    assertFailureValue("expected to contain at least", "{4=[four], 3=[six, two, one]}");
    assertFailureValue("but was", "{3=[one, six, two], 4=[five, four]}");
  }

  @Test
  public void containsAtLeastInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");
    ImmutableMultimap<Integer, String> expected =
        ImmutableMultimap.of(3, "six", 3, "one", 4, "five", 4, "four");

    assertThat(actual).containsAtLeastEntriesIn(expected);
    expectFailureWhenTestingThat(actual).containsAtLeastEntriesIn(expected).inOrder();
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys with out-of-order values",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue("keys with out-of-order values", "[3]");
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
    assertFailureKeys("missing", "---", "expected to contain at least", "but was");
    assertFailureValue("missing", "{3=[six], 4=[five]}");
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

    expectFailureWhenTestingThat(actual).containsAtLeast(3, "one", 3, "one", 3, "one", 4, "five");
    assertFailureKeys("missing", "---", "expected to contain at least", "but was");
    assertFailureValue("missing", "{3=[one [2 copies]]}");
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

    assertThat(actual).containsAtLeast(4, "four", 3, "six", 3, "two", 3, "one");
    expectFailureWhenTestingThat(actual)
        .containsAtLeast(4, "four", 3, "six", 3, "two", 3, "one")
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys are not in order",
        "keys with out-of-order values",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue("keys with out-of-order values", "[3]");
    assertFailureValue("expected to contain at least", "{4=[four], 3=[six, two, one]}");
    assertFailureValue("but was", "{3=[one, six, two], 4=[five, four]}");
  }

  @Test
  public void containsAtLeastVarargInOrderFailureValuesOnly() {
    ImmutableMultimap<Integer, String> actual =
        ImmutableMultimap.of(3, "one", 3, "six", 3, "two", 4, "five", 4, "four");

    assertThat(actual).containsAtLeast(3, "two", 3, "one", 4, "five", 4, "four");
    expectFailureWhenTestingThat(actual)
        .containsAtLeast(3, "two", 3, "one", 4, "five", 4, "four")
        .inOrder();
    assertFailureKeys(
        "contents match, but order was wrong",
        "keys with out-of-order values",
        "---",
        "expected to contain at least",
        "but was");
    assertFailureValue("keys with out-of-order values", "[3]");
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
    assertFailureKeys(
        "expected to contain entry",
        "testing whether",
        "but did not",
        "though it did contain values for that key",
        "full contents");
    assertFailureValue("expected to contain entry", "def=123");
    assertFailureValue("testing whether", "actual value parses to expected value");
    assertFailureValue("though it did contain values for that key", "[+456, +789]");
    assertFailureValue("full contents", "{abc=[+123], def=[+456, +789]}");
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsWrongKeyHasExpectedValue() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("xyz", 789);
    assertFailureKeys(
        "expected to contain entry",
        "testing whether",
        "but did not",
        "though it did contain entries with matching values",
        "full contents");
    assertFailureValue("though it did contain entries with matching values", "[def=+789]");
  }

  @Test
  public void comparingValuesUsing_containsEntry_failsMissingExpectedKeyAndValue() {
    ImmutableListMultimap<String, String> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", "+789");
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("xyz", 321);
    assertFailureKeys(
        "expected to contain entry", "testing whether", "but did not", "full contents");
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
        "expected to contain entry",
        "testing whether",
        "but did not",
        "though it did contain values for that key",
        "full contents",
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
        "expected to contain entry",
        "testing whether",
        "but did not",
        "though it did contain entries with matching values",
        "full contents",
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
        "expected to contain entry",
        "testing whether",
        "found match (but failing because of exception)",
        "full contents");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ZWEI) threw java.lang.NullPointerException");
    assertFailureValue("found match (but failing because of exception)", "2=zwei");
  }

  @Test
  public void comparingValuesUsing_containsEntry_wrongTypeInActual() {
    ImmutableListMultimap<String, Object> actual =
        ImmutableListMultimap.of("abc", "+123", "def", "+456", "def", 789);
    expectFailureWhenTestingThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsEntry("def", 789);
    assertFailureKeys(
        "expected to contain entry",
        "testing whether",
        "but did not",
        "though it did contain values for that key",
        "full contents",
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
    assertFailureKeys(
        "expected not to contain entry",
        "testing whether",
        "but contained that key with matching values",
        "full contents");
    assertFailureValue("expected not to contain entry", "def=789");
    assertFailureValue("testing whether", "actual value parses to expected value");
    assertFailureValue("but contained that key with matching values", "[+789]");
    assertFailureValue("full contents", "{abc=[+123], def=[+456, +789]}");
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
        "expected not to contain entry",
        "testing whether",
        "but contained that key with matching values",
        "full contents",
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
        "expected not to contain entry",
        "testing whether",
        "found no match (but failing because of exception)",
        "full contents");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(null, ZWEI) threw java.lang.NullPointerException");
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
        "expected not to contain entry",
        "testing whether",
        "found no match (but failing because of exception)",
        "full contents");
    assertThatFailure()
        .factValue("first exception")
        .startsWith("compare(789, 789) threw java.lang.ClassCastException");
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
  public void comparingValuesUsing_containsExactly_nullKey() {
    ListMultimap<String, String> actual = ArrayListMultimap.create();
    actual.put(null, "+123");
    assertThat(actual)
        .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
        .containsExactly(null, 123);
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
