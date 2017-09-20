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

import javax.annotation.Nullable;

// TODO(kak): Make this final?
public class DefaultSubject extends Subject<DefaultSubject, Object> {
  // TODO(kak): Make this package-protected?
  /**
   * @deprecated Switch your {@code Subject} from accepting {@link FailureStrategy} (and exposing a
   *     {@link SubjectFactory}) to accepting a {@link FailureMetadata} (and exposing a {@link
   *     Subject.Factory}), at which point you'll call the {@code FailureMetadata} overload of this
   *     constructor instead.
   */
  @Deprecated
  public DefaultSubject(FailureStrategy failureStrategy, @Nullable Object o) {
    super(failureStrategy, o);
  }

  public DefaultSubject(FailureMetadata metadata, @Nullable Object o) {
    super(metadata, o);
  }
}
