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
package org.junit.contrib.truth;

import org.junit.contrib.truth.subjects.BooleanSubject;
import org.junit.contrib.truth.subjects.CollectionSubject;
import org.junit.contrib.truth.subjects.DefaultSubject;
import org.junit.contrib.truth.subjects.IntegerSubject;
import org.junit.contrib.truth.subjects.ListSubject;
import org.junit.contrib.truth.subjects.StringSubject;

import java.util.Collection;
import java.util.List;

public class TestVerb extends AbstractVerb {

  public TestVerb(FailureStrategy failureStrategy) {
    super(failureStrategy);
  }

  public DefaultSubject that(Object target) {
    return new DefaultSubject(getFailureStrategy(), target);
  }

  public IntegerSubject that(Long target) {
    return new IntegerSubject(getFailureStrategy(), target);
  }

  public IntegerSubject that(Integer target) {
    return new IntegerSubject(getFailureStrategy(), target);
  }

  public BooleanSubject that(Boolean target) {
    return new BooleanSubject(getFailureStrategy(), target);
  }

  public StringSubject that(String target) {
    return new StringSubject(getFailureStrategy(), target);
  }

  public <T, C extends Collection<T>> CollectionSubject<T, C> that(C target) {
    return CollectionSubject.create(getFailureStrategy(), target);
  }

  public <T> ListSubject<T, List<T>> that(List<T> target) {
    return ListSubject.create(getFailureStrategy(), target);
  }
}
