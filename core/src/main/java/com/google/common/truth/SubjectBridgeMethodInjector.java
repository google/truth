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

/**
 * Superclass of {@link Subject} that declares methods with their old signatures to preserve binary
 * compatibility.
 */
abstract class SubjectBridgeMethodInjector {
  /**
   * @deprecated When you recompile your code, you will automatically migrate to the new version of
   *     this method.
   */
  @Deprecated
  protected abstract TestVerb check();

  /**
   * @deprecated When you recompile your code, you will automatically migrate to the new version of
   *     this method.
   */
  @Deprecated
  protected abstract TestVerb ignoreCheck();
}
