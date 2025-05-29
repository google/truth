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

import static com.google.common.base.Preconditions.checkNotNull;

import org.jspecify.annotations.Nullable;

/**
 * Propositions for {@link Throwable} subjects.
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
 *
 * @author Kurt Alfred Kluever
 */
public class ThrowableSubject extends Subject {
  private final @Nullable Throwable actual;

  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected ThrowableSubject(FailureMetadata metadata, @Nullable Throwable throwable) {
    this(metadata, throwable, null);
  }

  ThrowableSubject(
      FailureMetadata metadata,
      @Nullable Throwable throwable,
      @Nullable String typeDescriptionOverride) {
    super(metadata, throwable, typeDescriptionOverride);
    this.actual = throwable;
  }

  /*
   * TODO(cpovirk): consider a special case for isEqualTo and isSameInstanceAs that adds |expected|
   * as a suppressed exception
   */

  /** Returns a {@code StringSubject} to make assertions about the throwable's message. */
  public final StringSubject hasMessageThat() {
    StandardSubjectBuilder check = check("getMessage()");
    if (actual instanceof ErrorWithFacts && ((ErrorWithFacts) actual).facts().size() > 1) {
      check =
          check.withMessage(
              "(Note from Truth: When possible, instead of asserting on the full message, assert"
                  + " about individual facts by using ExpectFailure.assertThat.)");
    }
    return check.that(checkNotNull(actual).getMessage());
  }

  /**
   * Returns a new {@code ThrowableSubject} that supports assertions on this throwable's direct
   * cause. This method can be invoked repeatedly (e.g. {@code
   * assertThat(e).hasCauseThat().hasCauseThat()....} to assert on a particular indirect cause.
   */
  // Any Throwable is fine, and we use plain Throwable to emphasize that it's not used "for real."
  @SuppressWarnings("ShouldNotSubclass")
  public final ThrowableSubject hasCauseThat() {
    // provides a more helpful error message if hasCauseThat() methods are chained too deep
    // e.g. assertThat(new Exception()).hCT().hCT()....
    // TODO(diamondm) in keeping with other subjects' behavior this should still NPE if the subject
    // *itself* is null, since there's no context to lose. See also b/37645583
    if (actual == null) {
      check("getCause()")
          .withMessage("Causal chain is not deep enough - add a .isNotNull() check?")
          .fail();
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
}
