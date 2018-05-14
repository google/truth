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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;

import com.google.common.annotations.GwtIncompatible;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Propositions for string subjects.
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
// TODO(kak): Make this final
public class StringSubject extends ComparableSubject<StringSubject, String> {
  // TODO(kak): Make this package-private?
  /**
   * Constructor for use by subclasses. If you want to create an instance of this class itself, call
   * {@link Subject#check}{@code .that(actual)}.
   */
  protected StringSubject(FailureMetadata metadata, @NullableDecl String string) {
    super(metadata, string);
  }

  /** @deprecated Use {@link #isEqualTo} instead. String comparison is consistent with equality. */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(String other) {
    super.isEquivalentAccordingToCompareTo(other);
  }

  /** Fails if the string does not have the given length. */
  public void hasLength(int expectedLength) {
    checkArgument(expectedLength >= 0, "expectedLength(%s) must be >= 0", expectedLength);
    check("length()").that(actual().length()).isEqualTo(expectedLength);
  }

  /** Fails if the string is not equal to the zero-length "empty string." */
  public void isEmpty() {
    if (actual() == null) {
      failWithActual(simpleFact("expected empty string"));
    } else if (!actual().isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Fails if the string is equal to the zero-length "empty string." */
  public void isNotEmpty() {
    if (actual() == null) {
      failWithActual(simpleFact("expected nonempty string"));
    } else if (actual().isEmpty()) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Fails if the string does not contain the given sequence. */
  public void contains(CharSequence string) {
    checkNotNull(string);
    if (actual() == null) {
      failWithActual("expected a string that contains", string);
    } else if (!actual().contains(string)) {
      failWithActual("expected to contain", string);
    }
  }

  /** Fails if the string contains the given sequence. */
  public void doesNotContain(CharSequence string) {
    checkNotNull(string);
    if (actual() == null) {
      failWithActual("expected a string that does not contain", string);
    } else if (actual().contains(string)) {
      failWithActual("expected not to contain", string);
    }
  }

  /** Fails if the string does not start with the given string. */
  public void startsWith(String string) {
    checkNotNull(string);
    if (actual() == null) {
      failWithActual("expected a string that starts with", string);
    } else if (!actual().startsWith(string)) {
      failWithActual("expected to start with", string);
    }
  }

  /** Fails if the string does not end with the given string. */
  public void endsWith(String string) {
    checkNotNull(string);
    if (actual() == null) {
      failWithActual("expected a string that ends with", string);
    } else if (!actual().endsWith(string)) {
      failWithActual("expected to end with", string);
    }
  }

  // TODO(cpovirk): Probably these should check for null first like the earlier methods do.

  /** Fails if the string does not match the given regex. */
  public void matches(String regex) {
    if (!actual().matches(regex)) {
      failWithActual("expected to match", regex);
    }
  }

  /** Fails if the string does not match the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void matches(Pattern regex) {
    if (!regex.matcher(actual()).matches()) {
      failWithActual("expected to match", regex);
    }
  }

  /** Fails if the string matches the given regex. */
  public void doesNotMatch(String regex) {
    if (actual().matches(regex)) {
      failWithActual("expected not to match", regex);
    }
  }

  /** Fails if the string matches the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void doesNotMatch(Pattern regex) {
    if (regex.matcher(actual()).matches()) {
      failWithActual("expected not to match", regex);
    }
  }

  /** Fails if the string does not contain a match on the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void containsMatch(Pattern regex) {
    if (!regex.matcher(actual()).find()) {
      failWithActual("expected to contain a match for", regex);
    }
  }

  /** Fails if the string does not contain a match on the given regex. */
  public void containsMatch(String regex) {
    if (!Platform.containsMatch(actual(), regex)) {
      failWithActual("expected to contain a match for", regex);
    }
  }

  /** Fails if the string contains a match on the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void doesNotContainMatch(Pattern regex) {
    if (regex.matcher(actual()).find()) {
      failWithActual("expected not to contain a match for", regex);
    }
  }

  /** Fails if the string contains a match on the given regex. */
  public void doesNotContainMatch(String regex) {
    if (Platform.containsMatch(actual(), regex)) {
      failWithActual("expected not to contain a match for", regex);
    }
  }

  /**
   * Returns a {@link StringSubject}-like instance that will ignore the case of the characters.
   *
   * <p>Character equality ignoring case is defined as follows: Characters must be equal either
   * after calling {@link Character#toLowerCase} or after calling {@link Character#toUpperCase}.
   * Note that this is independent of any locale.
   */
  public CaseInsensitiveStringComparison ignoringCase() {
    return new CaseInsensitiveStringComparison();
  }

  /** Case insensitive propositions for string subjects. */
  public final class CaseInsensitiveStringComparison {
    private CaseInsensitiveStringComparison() {}

    /**
     * Fails if the subject is not equal to the given sequence (while ignoring case). For the
     * purposes of this comparison, two strings are equal if any of the following is true:
     *
     * <ul>
     *   <li>they are equal according to {@link String#equalsIgnoreCase}
     *   <li>they are both null
     * </ul>
     *
     * <p>Example: "abc" is equal to "ABC", but not to "abcd".
     */
    public void isEqualTo(String expected) {
      if (actual() == null) {
        if (expected != null) {
          failWithoutActual(
              fact("expected a string that is equal to", expected),
              butWas(),
              simpleFact("(case is ignored)"));
        }
      } else {
        if (expected == null) {
          failWithoutActual(
              fact("expected", "null (null reference)"), butWas(), simpleFact("(case is ignored)"));
        } else if (!actual().equalsIgnoreCase(expected)) {
          failWithoutActual(fact("expected", expected), butWas(), simpleFact("(case is ignored)"));
        }
      }
    }

    /**
     * Fails if the subject is equal to the given string (while ignoring case). The meaning of
     * equality is the same as for the {@link #isEqualTo} method.
     */
    public void isNotEqualTo(String unexpected) {
      if (actual() == null) {
        if (unexpected == null) {
          failWithoutActual(
              fact("expected a string that is not equal to", "null (null reference)"),
              simpleFact("(case is ignored)"));
        }
      } else {
        if (unexpected != null && actual().equalsIgnoreCase(unexpected)) {
          failWithoutActual(
              fact("expected not to be", unexpected), butWas(), simpleFact("(case is ignored)"));
        }
      }
    }

    /** Fails if the string does not contain the given sequence (while ignoring case). */
    public void contains(CharSequence expectedSequence) {
      checkNotNull(expectedSequence);
      String expected = expectedSequence.toString();
      if (actual() == null) {
        failWithoutActual(
            fact("expected a string that contains", expected),
            butWas(),
            simpleFact("(case is ignored)"));
      } else if (!containsIgnoreCase(expected)) {
        failWithoutActual(
            fact("expected to contain", expected), butWas(), simpleFact("(case is ignored)"));
      }
    }

    /** Fails if the string contains the given sequence (while ignoring case). */
    public void doesNotContain(CharSequence expectedSequence) {
      checkNotNull(expectedSequence);
      String expected = expectedSequence.toString();
      if (actual() == null) {
        failWithoutActual(
            fact("expected a string that does not contain", expected),
            butWas(),
            simpleFact("(case is ignored)"));
      } else if (containsIgnoreCase(expected)) {
        failWithoutActual(
            fact("expected not to contain", expected), butWas(), simpleFact("(case is ignored)"));
      }
    }

    private boolean containsIgnoreCase(String string) {
      if (string.isEmpty()) {
        // TODO(b/79459427): Fix for J2CL discrepancy when string is empty
        return true;
      }
      String subject = actual();
      for (int subjectOffset = 0;
          subjectOffset <= subject.length() - string.length();
          subjectOffset++) {
        if (subject.regionMatches(
            /* ignoreCase = */ true,
            /* toffset = */ subjectOffset,
            /* other = */ string,
            /* ooffset = */ 0,
            /* len = */ string.length())) {
          return true;
        }
      }
      return false;
    }
  }
}
