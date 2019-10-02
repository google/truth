/*
 * Copyright (c) 2019 Google, Inc.
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

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Correspondence.ExceptionStore}.
 *
 * <p>These should not be run under j2cl, because the descriptions don't include the expected stack
 * traces there.
 *
 * @author Pete Gillin
 */
@RunWith(JUnit4.class)
public final class CorrespondenceExceptionStoreTest extends BaseSubjectTestCase {

  @Test
  public void hasCompareException_empty() {
    Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
    assertThat(exceptions.hasCompareException()).isFalse();
  }

  @Test
  public void hasCompareException_hasCompareException() {
    Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
    addCompareException(exceptions);
    assertThat(exceptions.hasCompareException()).isTrue();
  }

  @Test
  public void describeAsMainCause_empty() {
    Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
    try {
      exceptions.describeAsMainCause();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void describeAsMainCause_notEmpty() {
    Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
    addCompareException(exceptions);
    assertExpectedFacts(
        exceptions.describeAsMainCause().asIterable(),
        "one or more exceptions were thrown while comparing elements");
  }

  @Test
  public void describeAsAdditionalInfo_empty() {
    Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
    assertThat(exceptions.describeAsAdditionalInfo().asIterable()).isEmpty();
  }

  @Test
  public void describeAsAdditionalInfo_notEmpty() {
    Correspondence.ExceptionStore exceptions = Correspondence.ExceptionStore.forIterable();
    addCompareException(exceptions);
    assertExpectedFacts(
        exceptions.describeAsAdditionalInfo().asIterable(),
        "additionally, one or more exceptions were thrown while comparing elements");
  }

  /** Adds a somewhat realistic exception from {@link Correspondence#compare} to the given store. */
  private static void addCompareException(Correspondence.ExceptionStore exceptions) {
    try {
      boolean unused = TestCorrespondences.WITHIN_10_OF.compare(null, 123);
    } catch (RuntimeException e) {
      exceptions.addCompareException(CorrespondenceExceptionStoreTest.class, e, null, 123);
    }
  }

  /**
   * Asserts that the given iterable has two facts, the first with the given key and no value, the
   * second with a key of {@code "first exception"} and a value describing the exception added by
   * {@link #addCompareException}.
   */
  private static void assertExpectedFacts(Iterable<Fact> facts, String expectedFirstKey) {
    assertThat(facts).hasSize(2);
    Fact first = Iterables.get(facts, 0);
    Fact second = Iterables.get(facts, 1);
    assertThat(first.key).isEqualTo(expectedFirstKey);
    assertThat(first.value).isNull();
    assertThat(second.key).isEqualTo("first exception");
    assertThat(second.value)
        .matches( // an initial statement of the method that threw and the exception type:
            "compare\\(null, 123\\) threw java.lang.NullPointerException"
                // some whitespace:
                + "\\s+"
                // the start of a stack trace, with the correct class:
                + "at com\\.google\\.common\\.truth\\.TestCorrespondences"
                // the rest of the stack trace, which we don't validate (and may contain newlines):
                + "(.|\\n)*"
                // the expected separator
                + "\\n---");
  }
}
