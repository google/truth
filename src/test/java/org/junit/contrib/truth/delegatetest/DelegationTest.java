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
package org.junit.contrib.truth.delegatetest;

import static org.junit.contrib.truth.Truth.ASSERT;

import org.junit.Test;
import org.junit.contrib.truth.AbstractVerb.DelegatedSubjectFactory;
import org.junit.contrib.truth.FailureStrategy;

/**
 * A test that's more or less intended to show how one uses an extended verb.
 * 
 */
public class DelegationTest {

  private static final Class<FooSubjectFactory> FOO = FooSubjectFactory.class;

  @Test public void customTypeCompares() {
    ASSERT._for(FOO).that(new Foo(5)).matches(new Foo(2 + 3));
  }

  @Test public void inaccessibleFactoryType() {
    try {
      ASSERT._for(InaccessibleSubjectFactory.class).that(new Foo(5)).matches(new Foo(2 + 3));
    } catch (RuntimeException e) {
      ASSERT.that(e.getMessage()).contains("Could not instantiate InaccessibleSubjectFactory");
    }
  }

  private class InaccessibleSubjectFactory extends DelegatedSubjectFactory<FooSubject, Foo> {

    private InaccessibleSubjectFactory(FailureStrategy failureStrategy) {
      super(failureStrategy);
    }

    @Override public FooSubject that(Foo that) { return null; }
    
  }
}
