/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0.delegatetest;

import org.truth0.FailureStrategy;
import org.truth0.subjects.Subject;
import org.truth0.subjects.SubjectFactory;

/**
 * A simple example Subject to demonstrate extension.
 *
 * @author Christian Gruber (christianedwardgruber@gmail.com)
 */
public class FooSubject extends Subject<FooSubject, Foo> {

  public static final SubjectFactory<FooSubject, Foo> FOO =
      new SubjectFactory<FooSubject, Foo>() {
        @Override public FooSubject getSubject(FailureStrategy fs, Foo target) {
          return new FooSubject(fs, target);
        }
      };

  public FooSubject(FailureStrategy failureStrategy, Foo subject) {
    super(failureStrategy, subject);
  }

  public void matches(Foo object) {
    if (getSubject().value != object.value) {
      fail("matches", getSubject(), object);
    }
  }

}
