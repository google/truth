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
package org.truth0;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.truth0.subjects.BooleanSubject;
import org.truth0.subjects.ClassSubject;
import org.truth0.subjects.CollectionSubject;
import org.truth0.subjects.DefaultSubject;
import org.truth0.subjects.IntegerSubject;
import org.truth0.subjects.IterableSubject;
import org.truth0.subjects.ListSubject;
import org.truth0.subjects.MapSubject;
import org.truth0.subjects.StringSubject;
import org.truth0.subjects.Subject;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
public class TestVerb extends AbstractVerb {
  public TestVerb(FailureStrategy failureStrategy) {
    super(failureStrategy);
  }

  public Subject<DefaultSubject, Object> that(Object target) {
    return new DefaultSubject(getFailureStrategy(), target);
  }

  public ClassSubject that(Class<?> target) {
    return new ClassSubject(getFailureStrategy(), target);
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

  public <T, C extends Iterable<T>> IterableSubject<? extends IterableSubject<?, T, C>, T, C> that(Iterable<T> target) {
    return IterableSubject.create(getFailureStrategy(), target);
  }

  public <T, C extends Collection<T>> CollectionSubject<? extends CollectionSubject<?, T, C>, T, C> that(Collection<T> target) {
    return CollectionSubject.create(getFailureStrategy(), target);
  }

  public <T, C extends List<T>> ListSubject<? extends ListSubject<?, T, C>, T, C> that(List<T> target) {
    return ListSubject.create(getFailureStrategy(), target);
  }

  public <T, C extends List<T>> ListSubject<? extends ListSubject<?, T, C>, T, C> that(T[] target) {
    return that(Arrays.asList(target));
  }

  public <K, V, M extends Map<K, V>> MapSubject<? extends MapSubject<?, K, V, M>, K, V, M> that(Map<K, V> target) {
    return MapSubject.create(getFailureStrategy(), target);
  }

}
