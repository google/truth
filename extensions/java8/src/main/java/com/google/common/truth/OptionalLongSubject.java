/*
 * Copyright (c) 2016 Google, Inc.
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

import java.util.OptionalLong;
import javax.annotation.Nullable;

/**
 * Propositions for Java 8 {@link OptionalLong} subjects.
 *
 * @author Ben Douglass
 */
public final class OptionalLongSubject extends Subject<OptionalLongSubject, OptionalLong> {
  OptionalLongSubject(FailureStrategy failureStrategy, @Nullable OptionalLong subject) {
    super(failureStrategy, subject);
  }

  /** Fails if the {@link OptionalLong} is empty or the subject is null. */
  public void isPresent() {
    if (actual() == null || !actual().isPresent()) {
      failWithoutActual("is present");
    }
  }

  /** Fails if the {@link OptionalLong} is present or the subject is null. */
  public void isEmpty() {
    if (actual() == null || actual().isPresent()) {
      fail("is empty");
    }
  }

  /**
   * Fails if the {@link OptionalLong} does not have the given value or the subject is null. More
   * sophisticated comparisons can be done using {@link #hasValueThat()}.
   */
  public void hasValue(long expected) {
    if (actual() == null || !actual().isPresent()) {
      fail("has value", expected);
    } else {
      long actual = actual().getAsLong();
      if (actual != expected) {
        fail("has value", expected);
      }
    }
  }

  /**
   * Prepares for a check regarding the value contained within the {@link OptionalLong}. Fails
   * immediately if the subject is empty.
   */
  public LongSubject hasValueThat() {
    if (actual() == null || !actual().isPresent()) {
      failWithoutActual("is present");
      return ignoreCheck().that(0L);
    } else {
      return check().that(actual().getAsLong());
    }
  }

  private static final SubjectFactory<OptionalLongSubject, OptionalLong> FACTORY =
      new SubjectFactory<OptionalLongSubject, OptionalLong>() {
        @Override
        public OptionalLongSubject getSubject(FailureStrategy fs, OptionalLong target) {
          return new OptionalLongSubject(fs, target);
        }
      };

  public static SubjectFactory<OptionalLongSubject, OptionalLong> optionalLongs() {
    return FACTORY;
  }
}
