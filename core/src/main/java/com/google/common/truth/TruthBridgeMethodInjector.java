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

import com.google.common.truth.AbstractVerb.DelegatedVerb;

/**
 * Superclass of {@link Truth} that declares methods with their old signatures to preserve binary
 * compatibility.
 */
abstract class TruthBridgeMethodInjector {
  /**
   * @deprecated When you recompile your code, you will automatically migrate to the new version of
   *     this method.
   */
  @Deprecated
  public static TestVerb assert_() {
    return Truth.assert_();
  }

  /**
   * @deprecated When you recompile your code, you will automatically migrate to the new version of
   *     this method.
   */
  @Deprecated
  public static TestVerb assertWithMessage(String messageToPrepend) {
    return assert_().withMessage(messageToPrepend);
  }

  /**
   * @deprecated When you recompile your code, you will automatically migrate to the new version of
   *     this method.
   */
  @Deprecated
  public static TestVerb assertWithMessage(String format, Object... args) {
    return assert_().withMessage(format, args);
  }

  /**
   * @deprecated When you recompile your code, you will automatically migrate to the new version of
   *     this method.
   */
  @Deprecated
  public static <S extends Subject<S, T>, T> DelegatedVerb<S, T> assertAbout(
      SubjectFactory<S, T> factory) {
    return assert_().about(factory);
  }
}
