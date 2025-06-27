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

import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Throwable} values.
 *
 * <p>Truth does not provide its own support for calling a method and automatically catching an
 * expected exception, only for asserting on the exception after it has been caught. To catch the
 * exception, we suggest {@link org.junit.Assert#assertThrows(Class,
 * org.junit.function.ThrowingRunnable) assertThrows} (JUnit), <a
 * href="https://kotlinlang.org/api/latest/kotlin.test/kotlin.test/assert-fails-with.html">{@code
 * assertFailsWith}</a> ({@code kotlin.test}), or similar functionality from your testing library of
 * choice.
 *
 * <pre>
 * InvocationTargetException expected =
 *     assertThrows(InvocationTargetException.class, () -> method.invoke(null));
 * assertThat(expected).hasCauseThat().isInstanceOf(IOException.class);
 * </pre>
 */
public class ThrowableSubject extends Subject {
  private final @Nullable Throwable actual;

  /**
   * The constructor is for use by subclasses only. If you want to create an instance of this class
   * itself, call {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected ThrowableSubject(FailureMetadata metadata, @Nullable Throwable actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /*
   * TODO(cpovirk): consider a special case for isEqualTo and isSameInstanceAs that adds |expected|
   * as a suppressed exception
   */

  /**
   * Returns a {@link StringSubject} to make assertions about the {@linkplain Throwable#getMessage
   * message} of the {@link Throwable}.
   */
  public final StringSubject hasMessageThat() {
    // We provide a more helpful error message if hasCauseThat() methods are chained too deep, as in
    // assertThat(new Exception()).hasCauseThat().hasMessageThat()....
    // This message also triggers for the simpler case of assertThat(null).hasMessageThat()....
    if (actual == null) {
      failForNullThrowable("Attempt to assert about the message of a null Throwable");
      return ignoreCheck().that("");
    }
    StandardSubjectBuilder check = check("getMessage()");
    if (actual instanceof ErrorWithFacts && ((ErrorWithFacts) actual).facts().size() > 1) {
      check =
          check.withMessage(
              "(Note from Truth: When possible, instead of asserting on the full message, assert"
                  + " about individual facts by using ExpectFailure.assertThat.)");
    }
    return check.that(actual.getMessage());
  }

  /**
   * Returns a new {@link ThrowableSubject} to make assertions on the direct {@linkplain
   * Throwable#getCause cause} of the {@link Throwable}. This method can be invoked repeatedly (e.g.
   * {@code assertThat(e).hasCauseThat().hasCauseThat()....} to assert on a particular indirect
   * cause.
   */
  // Any Throwable is fine, and we use plain Throwable to emphasize that it's not used "for real."
  @SuppressWarnings("ShouldNotSubclass")
  public final ThrowableSubject hasCauseThat() {
    // We provide a more helpful error message if hasCauseThat() methods are chained too deep, as in
    // assertThat(new Exception()).hasCauseThat().hasCauseThat()....
    // This message also triggers for the simpler case of assertThat(null).hasCauseThat()....
    if (actual == null) {
      failForNullThrowable("Attempt to assert about the cause of a null Throwable");
      return ignoreCheck()
          .that(
              new Throwable() {
                @Override
                @SuppressWarnings("UnsynchronizedOverridesSynchronized")
                public Throwable fillInStackTrace() {
                  setStackTrace(new StackTraceElement[0]); // for old versions of Android
                  return this;
                }
              });
    }
    return check("getCause()").that(actual.getCause());
  }

  static Factory<ThrowableSubject, Throwable> throwables() {
    return ThrowableSubject::new;
  }
}
