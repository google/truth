/*
 * Copyright (c) 2014 Google, Inc.
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

import java.util.Arrays;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * A Subject for {@code Object[]} and more generically {@code T[]}.
 *
 * @author Christian Gruber
 */
public final class ObjectArraySubject<T> extends AbstractArraySubject<ObjectArraySubject<T>, T[]> {
  ObjectArraySubject(
      FailureMetadata metadata, @NullableDecl T[] o, @NullableDecl String typeDescription) {
    super(metadata, o, typeDescription);
  }

  public IterableSubject asList() {
    return checkNoNeedToDisplayBothValues("asList()").that(Arrays.asList(actual()));
  }
}
