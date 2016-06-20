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
package com.google.common.truth.codegen;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;

import javax.annotation.Nullable;

/** Subclass of Subject to be used for Testing. */
public class BarSubject extends Subject<BarSubject, String> {
  public static final SubjectFactory<BarSubject, String> BAR =
      new SubjectFactory<BarSubject, String>() {
        @Override
        public BarSubject getSubject(FailureStrategy fs, String target) {
          return new BarSubject(fs, target);
        }
      };

  public BarSubject(FailureStrategy failureStrategy, String subject) {
    super(failureStrategy, subject);
  }

  public void startsWith(@Nullable String prefix) {
    if (getSubject().startsWith(prefix)) {
      fail("matches", getSubject(), prefix);
    }
  }
}
