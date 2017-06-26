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
 * A generic version of {@link SubjectFactory} which defines its own verb type.
 *
 * <p>The verb defines one or more {@code that()} methods which accept an object to test and return
 * a {@link Subject} which provides testing methods. Generally, if writing an extension to Truth,
 * you should use a {@link SubjectFactory} instead, and let Truth handle the verb glue.
 *
 * <p>However, a {@link DelegatedVerbFactory} is useful when the generics on {@link SubjectFactory}
 * are too strict - for example, if your custom {@link Subject} is parameterized, and you want to
 * infer the parameter type from the test object. See {@link DelegatedVerbFactoryTest} for an
 * example implementation.
 */
public interface DelegatedVerbFactory<V extends AbstractDelegatedVerb> {
  /** Instantiates a new V with {@code failureStrategy} and itself. */
  V createVerb(FailureStrategy failureStrategy);
}
