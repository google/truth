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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Fact.factWithoutValue;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicLongMap;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Propositions for {@link AtomicLongMap} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class AtomicLongMapSubject extends Subject<AtomicLongMapSubject, AtomicLongMap<?>> {
  AtomicLongMapSubject(FailureMetadata metadata, @NullableDecl AtomicLongMap<?> map) {
    super(metadata, map);
  }

  /**
   * @deprecated {@link AtomicLongMap} does not define equality (i.e., it does not implement
   *     equals()), so you probably don't want to call this method. Instead, perform your assertion
   *     on the map view (e.g., assertThat(atomicLongMap.asMap()).isEqualTo(EXPECTED_MAP)).
   */
  @Deprecated
  @Override
  public void isEqualTo(@NullableDecl Object other) {
    super.isEqualTo(other);
  }

  /**
   * @deprecated {@link AtomicLongMap} does not define equality (i.e., it does not implement
   *     equals()), so you probably don't want to call this method. Instead, perform your assertion
   *     on the map view (e.g., assertThat(atomicLongMap.asMap()).isNotEqualTo(UNEXPECTED_MAP)).
   */
  @Deprecated
  @Override
  public void isNotEqualTo(@NullableDecl Object other) {
    super.isNotEqualTo(other);
  }

  /** Fails if the {@link AtomicLongMap} is not empty. */
  public void isEmpty() {
    if (!actual().isEmpty()) {
      fail(factWithoutValue("expected to be empty"));
    }
  }

  /** Fails if the {@link AtomicLongMap} is empty. */
  public void isNotEmpty() {
    if (actual().isEmpty()) {
      failWithoutActual(factWithoutValue("expected not to be empty"));
    }
  }

  /** Fails if the {@link AtomicLongMap} does not have the given size. */
  public void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize (%s) must be >= 0", expectedSize);
    check("size()").that(actual().size()).isEqualTo(expectedSize);
  }

  /** Fails if the {@link AtomicLongMap} does not have the given sum. */
  public void hasSum(long expectedSum) {
    check("sum()").that(actual().sum()).isEqualTo(expectedSum);
  }

  /** Fails if the {@link AtomicLongMap} does not contain the given key. */
  public void containsKey(Object key) {
    checkNotNull(key, "AtomicLongMap does not support null keys");
    if (!actual().containsKey(key)) {
      fail("contains key", key);
    }
  }

  /** Fails if the {@link AtomicLongMap} contains the given key. */
  public void doesNotContainKey(Object key) {
    checkNotNull(key, "AtomicLongMap does not support null keys");
    if (actual().containsKey(key)) {
      fail("does not contain key", key);
    }
  }

  /** Fails if the {@link AtomicLongMap} does not contain the given entry. */
  public void containsEntry(Object key, long value) {
    checkNotNull(key, "AtomicLongMap does not support null keys");
    long actualValue = ((AtomicLongMap<Object>) actual()).get(key);
    if (actualValue != value) {
      fail("contains entry", Maps.immutableEntry(key, value));
    }
  }

  /** Fails if the {@link AtomicLongMap} contains the given entry. */
  public void doesNotContainEntry(@NullableDecl Object key, long value) {
    if (key != null) {
      long actualValue = ((AtomicLongMap<Object>) actual()).get(key);
      if (actualValue == value) {
        fail("does not contain entry", Maps.immutableEntry(key, value));
      }
    }
  }

  // TODO(kak): Consider adding containsExactly() / containsExactlyEntriesIn() like MapSubject?
}
