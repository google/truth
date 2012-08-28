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
package org.truth0.extensiontest;

import org.truth0.FailureStrategy;
import org.truth0.TestVerb;
import org.truth0.Truth;

/**
 * An extended verb to demonstrate (and test) the subclassing mechanism for
 * extension. Note this is not the preferred approach to extension, which is
 * {@link TestVerb#for() }
 *
 * @author David Saff
 * @author Christian Gruber (christianedwardgruber@gmail.com)
 *
 */
public class ExtendedVerb extends TestVerb {
  public static final ExtendedVerb ASSERT = new ExtendedVerb(Truth.THROW_ASSERTION_ERROR);

  public ExtendedVerb(FailureStrategy failureStrategy) {
    super(failureStrategy);
  }

  public MySubject that(MyType subject) {
    return new MySubject(getFailureStrategy(), subject);
  }
}
