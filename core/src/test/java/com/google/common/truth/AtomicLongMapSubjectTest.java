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
package com.google.common.truth;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.util.concurrent.AtomicLongMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link AtomicLongMap} subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public final class AtomicLongMapSubjectTest extends BaseSubjectTestCase {

  @Test
  public void isEqualToFail() {
    AtomicLongMap<String> alm1 = AtomicLongMap.create();
    AtomicLongMap<String> alm2 = AtomicLongMap.create();

    expectFailureWhenTestingThat(alm1).isEqualTo(alm2);
  }

  @Test
  public void isEmpty() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    assertThat(actual).isEmpty();
  }

  @Test
  public void isEmptyWithFailure() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("foo");

    expectFailureWhenTestingThat(actual).isEmpty();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void isNotEmpty() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("FOO");
    assertThat(actual).isNotEmpty();
  }

  @Test
  public void isNotEmptyWithFailure() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    expectFailureWhenTestingThat(actual).isNotEmpty();
    assertFailureKeys("expected not to be empty");
  }

  @Test
  public void hasSize() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    assertThat(actual).hasSize(1);
  }

  @Test
  public void hasSizeZero() {
    assertThat(AtomicLongMap.create()).hasSize(0);
  }

  @Test
  public void hasSizeNegative() {
    try {
      assertThat(AtomicLongMap.create()).hasSize(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void hasSizeFails() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).hasSize(2);
    assertFailureValue("value of", "atomicLongMap.size()");
  }

  @Test
  public void hasSum() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    actual.getAndIncrement("kurt");
    assertThat(actual).hasSum(2);
  }

  @Test
  public void hasSumZero() {
    assertThat(AtomicLongMap.create()).hasSum(0);
  }

  @Test
  public void hasSumNegative() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndDecrement("kurt");
    assertThat(actual).hasSum(-1);
  }

  @Test
  public void hasSumFails() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).hasSum(2);
    assertFailureValue("value of", "atomicLongMap.sum()");
  }

  @Test
  public void containsKey() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    assertThat(actual).containsKey("kurt");
  }

  @Test
  public void containsKeyFailure() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).containsKey("greg");
    assertFailureKeys("value of", "expected to contain", "but was", "atomicLongMap was");
    assertFailureValue("value of", "atomicLongMap.asMap().keySet()");
    assertFailureValue("expected to contain", "greg");
  }

  @Test
  public void doesNotContainKey() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    assertThat(actual).doesNotContainKey("greg");
  }

  @Test
  public void doesNotContainKeyFailure() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).doesNotContainKey("kurt");
    assertFailureKeys("value of", "expected not to contain", "but was", "atomicLongMap was");
    assertFailureValue("value of", "atomicLongMap.asMap().keySet()");
    assertFailureValue("expected not to contain", "kurt");
  }

  @Test
  public void doesNotContainNullKey() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    try {
      assertThat(actual).doesNotContainKey(null);
      fail("Should have thrown.");
    } catch (NullPointerException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("AtomicLongMap does not support null keys");
    }
  }

  @Test
  public void containsEntry() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    assertThat(actual).containsEntry("kurt", 1);
  }

  @Test
  public void containsEntryZeroDoesNotContain() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    // This passes, which is maybe surprising or maybe not. See the TODO in AtomicLongMapSubject.
    assertThat(actual).containsEntry("kurt", 0);
  }

  @Test
  public void containsEntryFailure() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).containsEntry("greg", 2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{kurt=1}> contains entry <greg=2>");
  }

  @Test
  public void doesNotContainEntry() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    assertThat(actual).doesNotContainEntry("greg", 2);
  }

  @Test
  public void doesNotContainEntryNullKey() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    try {
      assertThat(actual).doesNotContainEntry(null, 2);
      fail();
    } catch (NullPointerException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("AtomicLongMap does not support null keys");
    }
  }

  @Test
  public void doesNotContainEntryFailure() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).doesNotContainEntry("kurt", 1);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{kurt=1}> does not contain entry <kurt=1>");
  }

  @Test
  public void failMapContainsKey() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).containsKey("greg");
    assertFailureKeys("value of", "expected to contain", "but was", "atomicLongMap was");
    assertFailureValue("value of", "atomicLongMap.asMap().keySet()");
    assertFailureValue("expected to contain", "greg");
  }

  @Test
  public void failMapLacksKey() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).doesNotContainKey("kurt");
    assertFailureKeys("value of", "expected not to contain", "but was", "atomicLongMap was");
    assertFailureValue("value of", "atomicLongMap.asMap().keySet()");
    assertFailureValue("expected not to contain", "kurt");
  }

  @Test
  public void containsKeyWithValue() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    assertThat(actual).containsEntry("kurt", 1);
  }

  @Test
  public void failMapContainsKeyWithValue() {
    AtomicLongMap<String> actual = AtomicLongMap.create();
    actual.getAndIncrement("kurt");
    expectFailureWhenTestingThat(actual).containsEntry("kurt", 2);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{kurt=1}> contains entry <kurt=2>");
  }

  private AtomicLongMapSubject expectFailureWhenTestingThat(AtomicLongMap<?> actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
