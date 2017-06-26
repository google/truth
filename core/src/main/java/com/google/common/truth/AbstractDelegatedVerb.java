/*
 * Copyright (c) 2016 Google, Inc.
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

/**
 * An extendable type that allows plugin creators to define their own {@code that()} methods for the
 * Truth SPI. Intended for advanced usage only, generally you should prefer to create a {@link
 * SubjectFactory} when extending Truth.
 *
 * <p>This class has no abstract methods because it's intended to be extended only when unique
 * generics are required on the {@code that()} method. Regardless, any subclass should define at
 * least one {@code that()} method which accepts a single argument and returns a {@link Subject}, to
 * be consistent with the Truth SPI.
 *
 * <p>Every subclass of {@link AbstractDelegatedVerb} should also have a companion {@link
 * DelegatedVerbFactory} which knows how to instantiate it. The factory should pass the {@link
 * FailureStrategy} it receives, along with itself ({@code this}) into the constructor for the
 * {@link AbstractDelegatedVerb}.
 *
 * <p>For an example implementation and usage, see {@link DelegatedVerbFactoryTest}.
 */
public abstract class AbstractDelegatedVerb {
  protected AbstractDelegatedVerb() {}

  // TODO(user,cgruber): Better enforce that subclasses implement a that() method.
}
