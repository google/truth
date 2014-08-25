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
package org.truth0.subjects;

/**
 * @deprecated use {@link com.google.common.truth.SubjectFactory}.
 */
@Deprecated
public abstract
    class SubjectFactory<S extends com.google.common.truth.Subject<S,T>, T>
    extends com.google.common.truth.SubjectFactory<S, T> {

  public abstract S getSubject(org.truth0.FailureStrategy fs, T that);

  @Override public S getSubject(final com.google.common.truth.FailureStrategy fs, T that) {
    org.truth0.FailureStrategy strategy = new org.truth0.FailureStrategy() {
      @Override public void fail(String message) {
        fs.fail(message);
      }
      @Override public void fail(String message, Throwable cause) {
        fs.fail(message, cause);
      }
      @Override public void failComparing(
          String message, CharSequence expected, CharSequence actual) {
        fs.failComparing(message, expected, actual);
      }
    };
    return this.getSubject(strategy, that);
  }
}
