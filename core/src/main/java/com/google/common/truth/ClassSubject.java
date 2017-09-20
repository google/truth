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

import com.google.common.annotations.GwtIncompatible;
import javax.annotation.Nullable;

/**
 * Propositions for {@link Class} subjects.
 *
 * @author Kurt Alfred Kluever
 */
@GwtIncompatible("reflection")
public final class ClassSubject extends Subject<ClassSubject, Class<?>> {
  ClassSubject(FailureMetadata metadata, @Nullable Class<?> o) {
    super(metadata, o);
  }

  /**
   * Fails if this class or interface is not the same as or a subclass or subinterface of, the given
   * class or interface.
   */
  public void isAssignableTo(Class<?> clazz) {
    if (!clazz.isAssignableFrom(actual())) {
      fail("is assignable to", clazz);
    }
  }
}
