/*
 * Copyright (c) 2025 Google, Inc.
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

module com.google.truth {
  requires com.google.common;

  /*
   * AutoValue's annotations aren't ready for the module system:
   * https://github.com/google/auto/issues/597. Let's hope that our usages are internal enough that
   * we don't need to worry about listing them here.
   */

  requires static com.google.errorprone.annotations;
  requires static com.google.j2objc.annotations;
  requires static junit;
  requires static org.jspecify;
  requires static org.objectweb.asm;

  exports com.google.common.truth;
}
