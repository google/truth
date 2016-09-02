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
package com.google.common.truth.delegation;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import java.util.Arrays;

/**
 * A simple example Subject to demonstrate extension.
 *
 * <p>Callers would call {@code assertAbout(foo()).that(foo).matches(bar);} or {@code
 * assertAbout(foo()).that(foo).matchesEither(bar, baz);}
 *
 * @author Christian Gruber (christianedwardgruber@gmail.com)
 */
public class FooSubject extends Subject<FooSubject, Foo> {
  private static final SubjectFactory<FooSubject, Foo> FOO =
      new SubjectFactory<FooSubject, Foo>() {
        @Override
        public FooSubject getSubject(FailureStrategy fs, Foo target) {
          return new FooSubject(fs, target);
        }
      };

  public static SubjectFactory<FooSubject, Foo> foo() {
    return FOO;
  }

  // Must be public and non-final for generated subclasses (wrappers)
  public FooSubject(FailureStrategy failureStrategy, Foo subject) {
    super(failureStrategy, subject);
  }

  public void matches(Foo expected) {
    if (actual().value != expected.value) {
      fail("matches", expected);
    }
  }

  public void matchesAny(Foo... expecteds) {
    for (Foo expected : expecteds) {
      if (actual().value == expected.value) {
        return;
      }
    }
    fail("matches", Arrays.asList(expecteds));
  }
}
