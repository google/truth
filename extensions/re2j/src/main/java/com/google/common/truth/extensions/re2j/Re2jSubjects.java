/*
 * Copyright (c) 2015 Google, Inc.
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
package com.google.common.truth.extensions.re2j;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.re2j.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Truth subjects for re2j regular expressions.
 *
 * <p>Truth natively provides subjects for dealing with {@code java.util.regex} based regular
 * expressions. This class is intended to provide {@code com.google.re2j} analogues to those
 * methods.
 */
public final class Re2jSubjects {
  /**
   * Returns a subject factory for {@link String} subjects which you can use to assert things about
   * {@link com.google.re2j.Pattern} regexes.
   *
   * <p>This subject does not replace Truth's built-in {@link com.google.common.truth.StringSubject}
   * but instead provides only the methods needed to deal with regular expressions.
   *
   * @see com.google.common.truth.StringSubject
   */
  public static Subject.Factory<Re2jStringSubject, String> re2jString() {
    return Re2jStringSubject.FACTORY;
  }

  /**
   * Subject for {@link String} subjects which you can use to assert things about {@link
   * com.google.re2j.Pattern} regexes.
   *
   * @see #re2jString
   */
  public static final class Re2jStringSubject extends Subject {
    private static final Subject.Factory<Re2jStringSubject, String> FACTORY =
        new Subject.Factory<Re2jStringSubject, String>() {
          @Override
          public Re2jStringSubject createSubject(
              FailureMetadata failureMetadata, @Nullable String target) {
            return new Re2jStringSubject(failureMetadata, target);
          }
        };

    private final @Nullable String actual;

    private Re2jStringSubject(FailureMetadata failureMetadata, @Nullable String subject) {
      super(failureMetadata, subject);
      this.actual = subject;
    }

    @Override
    protected String actualCustomStringRepresentation() {
      return quote(checkNotNull(actual));
    }

    /** Fails if the string does not match the given regex. */
    public void matches(String regex) {
      if (!Pattern.matches(regex, checkNotNull(actual))) {
        failWithActual("expected to match ", regex);
      }
    }

    /** Fails if the string does not match the given regex. */
    @GwtIncompatible("com.google.re2j.Pattern")
    public void matches(Pattern regex) {
      if (!regex.matcher(checkNotNull(actual)).matches()) {
        failWithActual("expected to match ", regex);
      }
    }

    /** Fails if the string matches the given regex. */
    public void doesNotMatch(String regex) {
      if (Pattern.matches(regex, checkNotNull(actual))) {
        failWithActual("expected to fail to match", regex);
      }
    }

    /** Fails if the string matches the given regex. */
    @GwtIncompatible("com.google.re2j.Pattern")
    public void doesNotMatch(Pattern regex) {
      if (regex.matcher(checkNotNull(actual)).matches()) {
        failWithActual("expected to fail to match", regex);
      }
    }

    /** Fails if the string does not contain a match on the given regex. */
    @GwtIncompatible("com.google.re2j.Pattern")
    public void containsMatch(Pattern pattern) {
      if (!pattern.matcher(checkNotNull(actual)).find()) {
        failWithActual("expected to contain a match for", pattern);
      }
    }

    /** Fails if the string does not contain a match on the given regex. */
    public void containsMatch(String regex) {
      if (!doContainsMatch(checkNotNull(actual), regex)) {
        failWithActual("expected to contain a match for", regex);
      }
    }

    /** Fails if the string contains a match on the given regex. */
    @GwtIncompatible("com.google.re2j.Pattern")
    public void doesNotContainMatch(Pattern pattern) {
      if (pattern.matcher(checkNotNull(actual)).find()) {
        failWithActual("expected not to contain a match for", pattern);
      }
    }

    /** Fails if the string contains a match on the given regex. */
    public void doesNotContainMatch(String regex) {
      if (doContainsMatch(checkNotNull(actual), regex)) {
        failWithActual("expected not to contain a match for", regex);
      }
    }

    private static String quote(CharSequence toBeWrapped) {
      return "\"" + toBeWrapped + "\"";
    }

    private static boolean doContainsMatch(String subject, String regex) {
      return Pattern.compile(regex).matcher(subject).find();
    }
  }

  private Re2jSubjects() {}
}
