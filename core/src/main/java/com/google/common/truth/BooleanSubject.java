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

import javax.annotation.Nullable;

/**
 * Propositions for boolean subjects.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public final class BooleanSubject extends Subject<BooleanSubject, Boolean> {
  BooleanSubject(FailureMetadata metadata, @Nullable Boolean actual) {
    super(metadata, actual);
  }

  /** Fails if the subject is false or {@code null}. */
  public void isTrue() {
    if (actual() == null) {
      failWithRawMessage("%s was expected to be true, but was null", booleanSubject());
    } else if (!actual()) {
      failWithRawMessage("%s was expected to be true, but was false", booleanSubject());
    }
  }

  /** Fails if the subject is true or {@code null}. */
  public void isFalse() {
    if (actual() == null) {
      failWithRawMessage("%s was expected to be false, but was null", booleanSubject());
    } else if (actual()) {
      failWithRawMessage("%s was expected to be false, but was true", booleanSubject());
    }
  }

  private String booleanSubject() {
    return internalCustomName() == null ? "The subject" : actualAsString();
  }
}
