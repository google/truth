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
import com.google.common.collect.ImmutableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link String} values.
 */
public class StringSubject extends ComparableSubject<String> {
  private final @Nullable String actual;

  /**
   * The constructor is for use by subclasses only. If you want to create an instance of this class
   * itself, call {@link Subject#check(String, Object...) check(...)}{@code .that(actual)}.
   */
  protected StringSubject(FailureMetadata metadata, @Nullable String actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /**
   * @deprecated Use {@link #isEqualTo} instead. String comparison is consistent with equality.
   */
  @Override
  @Deprecated
  public final void isEquivalentAccordingToCompareTo(@Nullable String expected) {
    super.isEquivalentAccordingToCompareTo(expected);
  }

  /** Checks that the actual value has the given length. */
  public void hasLength(int length) {
    checkArgument(length >= 0, "expected length (%s) must be >= 0", length);
    if (actual == null) {
      failWithActual("expected a string with length", length);
      return;
    }
    check("length()").that(actual.length()).isEqualTo(length);
  }

  /** Checks that the actual value is the empty string. */
  public void isEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected an empty string"));
    } else if (!actual.isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Checks that the actual value is not the empty string. */
  public void isNotEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-empty string"));
    } else if (actual.isEmpty()) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Checks that the actual value contains the given sequence. */
  public void contains(@Nullable CharSequence string) {
    checkNotNull(string);
    if (actual == null) {
      failWithActual("expected a string that contains", string);
    } else if (!actual.contains(string)) {
      failWithActual("expected to contain", string);
    }
  }

  /** Checks that the actual value contains the given sequences. */
  public void containsAllOf(@Nullable CharSequence... strings) {
    checkNotNull(strings);
    ImmutableSet<String> expected = ImmutableSet.copyOf(strings);
    checkArgument(expected.size() == strings.length, "duplicate strings in expected");
    if (actual == null) {
      failWithActual("expected a string that contains all of", expected);
      return;
    }
    List<String> missing = new ArrayList<>();
    for (String string : expected) {
      if (!actual.contains(string)) {
        missing.add(string);
      }
    }
    if (!missing.isEmpty()) {
      failWithoutActual(
          fact("expected to contain all of", expected), butWas(), fact("missing", missing));
    }
  }

  /** Checks that the actual value does not contain the given sequence. */
  public void doesNotContain(@Nullable CharSequence string) {
    checkNotNull(string);
    if (actual == null) {
      failWithActual("expected a string that does not contain", string);
    } else if (actual.contains(string)) {
      failWithActual("expected not to contain", string);
    }
  }

  /** Checks that the actual value starts with the given string. */
  public void startsWith(@Nullable String string) {
    checkNotNull(string);
    if (actual == null) {
      failWithActual("expected a string that starts with", string);
    } else if (!actual.startsWith(string)) {
      failWithActual("expected to start with", string);
    }
  }

  /** Checks that the actual value ends with the given string. */
  public void endsWith(@Nullable String string) {
    checkNotNull(string);
    if (actual == null) {
      failWithActual("expected a string that ends with", string);
    } else if (!actual.endsWith(string)) {
      failWithActual("expected to end with", string);
    }
  }

  /** Checks that the actual value matches the given regex. */
  public void matches(@Nullable String regex) {
    checkNotNull(regex);
    if (actual == null) {
      failWithActual("expected a string that matches", regex);
    } else if (!actual.matches(regex)) {
      if (regex.equals(actual)) {
        failWithoutActual(
            fact("expected to match", regex),
            butWas(),
            simpleFact("Looks like you want to use .isEqualTo() for an exact equality assertion."));
      } else if (Platform.containsMatch(actual, regex)) {
        failWithoutActual(
            fact("expected to match", regex),
            butWas(),
            simpleFact("Did you mean to call containsMatch() instead of match()?"));
      } else {
        failWithActual("expected to match", regex);
      }
    }
  }

  /** Checks that the actual value matches the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void matches(@Nullable Pattern regex) {
    checkNotNull(regex);
    if (actual == null) {
      failWithActual("expected a string that matches", regex);
    } else if (!regex.matcher(actual).matches()) {
      if (regex.toString().equals(actual)) {
        failWithoutActual(
            fact("expected to match", regex),
            butWas(),
            simpleFact(
                "If you want an exact equality assertion you can escape your regex with"
                    + " Pattern.quote()."));
      } else if (regex.matcher(actual).find()) {
        failWithoutActual(
            fact("expected to match", regex),
            butWas(),
            simpleFact("Did you mean to call containsMatch() instead of match()?"));
      } else {
        failWithActual("expected to match", regex);
      }
    }
  }

  /** Checks that the actual value does not match the given regex. */
  public void doesNotMatch(@Nullable String regex) {
    checkNotNull(regex);
    if (actual == null) {
      failWithActual("expected a string that does not match", regex);
    } else if (actual.matches(regex)) {
      failWithActual("expected not to match", regex);
    }
  }

  /** Checks that the actual value does not match the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void doesNotMatch(@Nullable Pattern regex) {
    checkNotNull(regex);
    if (actual == null) {
      failWithActual("expected a string that does not match", regex);
    } else if (regex.matcher(actual).matches()) {
      failWithActual("expected not to match", regex);
    }
  }

  /** Checks that the actual value contains a match on the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void containsMatch(@Nullable Pattern regex) {
    checkNotNull(regex);
    if (actual == null) {
      failWithActual("expected a string that contains a match for", regex);
    } else if (!regex.matcher(actual).find()) {
      failWithActual("expected to contain a match for", regex);
    }
  }

  /** Checks that the actual value contains a match on the given regex. */
  public void containsMatch(@Nullable String regex) {
    checkNotNull(regex);
    if (actual == null) {
      failWithActual("expected a string that contains a match for", regex);
    } else if (!Platform.containsMatch(actual, regex)) {
      failWithActual("expected to contain a match for", regex);
    }
  }

  /** Checks that the actual value does not contain a match on the given regex. */
  @GwtIncompatible("java.util.regex.Pattern")
  public void doesNotContainMatch(@Nullable Pattern regex) {
    checkNotNull(regex);
    if (actual == null) {
      failWithActual("expected a string that does not contain a match for", regex);
      return;
    }
    Matcher matcher = regex.matcher(actual);
    if (matcher.find()) {
      failWithoutActual(
          fact("expected not to contain a match for", regex),
          fact("but contained", matcher.group()),
          actualValue("full string"));
    }
  }

  /** Checks that the actual value does not contain a match on the given regex. */
  public void doesNotContainMatch(@Nullable String regex) {
    checkNotNull(regex);
    if (actual == null) {
      failWithActual("expected a string that does not contain a match for", regex);
    } else if (Platform.containsMatch(actual, regex)) {
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
    return CaseInsensitiveStringComparison.create(this);
  }

  /** Offers case-insensitive checks for string values. */
  @SuppressWarnings("Casing_StringEqualsIgnoreCase") // intentional choice from API Review
  public static final class CaseInsensitiveStringComparison {
    private final StringSubject subject;
    private final @Nullable String actual;

    private CaseInsensitiveStringComparison(StringSubject subject) {
      this.subject = subject;
      this.actual = subject.actual;
    }

    /**
     * Checks that the actual value is equal to the given sequence (while ignoring case). For the
     * purposes of this comparison, two strings are equal if either of the following is true:
     *
     * <ul>
     *   <li>They are equal according to {@link String#equalsIgnoreCase}. (In Kotlin terms: They are
     *       equal according to <a
     *       href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/equals.html">{@code
     *       actual.equals(expected, ignoreCase = true)}</a>.)
     *   <li>They are both null.
     * </ul>
     *
     * <p>Example: "abc" is equal to "ABC", but not to "abcd".
     */
    public void isEqualTo(@Nullable String expected) {
      if (actual == null) {
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
        } else if (!actual.equalsIgnoreCase(expected)) {
          failWithoutActual(fact("expected", expected), butWas(), simpleFact("(case is ignored)"));
        }
      }
    }

    /**
     * Checks that the actual value is not equal to the given string (while ignoring case). The
     * meaning of equality is the same as for the {@link #isEqualTo} method.
     */
    public void isNotEqualTo(@Nullable String other) {
      if (actual == null) {
        if (other == null) {
          failWithoutActual(
              fact("expected a string that is not equal to", "null (null reference)"),
              simpleFact("(case is ignored)"));
        }
      } else {
        if (actual.equalsIgnoreCase(other)) {
          failWithoutActual(
              fact("expected not to be", other), butWas(), simpleFact("(case is ignored)"));
        }
      }
    }

    /** Checks that the actual value contains the given sequence (while ignoring case). */
    public void contains(@Nullable CharSequence string) {
      checkNotNull(string);
      String expected = string.toString();
      if (actual == null) {
        failWithoutActual(
            fact("expected a string that contains", expected),
            butWas(),
            simpleFact("(case is ignored)"));
      } else if (!containsIgnoreCase(actual, expected)) {
        failWithoutActual(
            fact("expected to contain", expected), butWas(), simpleFact("(case is ignored)"));
      }
    }

    /** Checks that the actual value contains the given sequences. */
    public void containsAllOf(@Nullable CharSequence... strings) {
      checkNotNull(strings);
      ImmutableSet<String> expected = ImmutableSet.copyOf(strings);
      checkArgument(expected.size() == strings.length, "duplicate strings in expected");
      if (actual == null) {
        failWithoutActual(
            fact("expected a string that contains all of", expected),
            butWas(),
            simpleFact("(case is ignored)"));
        return;
      }
      List<String> missing = new ArrayList<>();
      for (String string : expected) {
        if (!containsIgnoreCase(actual, string)) {
          missing.add(string);
        }
      }
      if (!missing.isEmpty()) {
        failWithoutActual(
            fact("expected to contain all of", expected),
            butWas(),
            fact("missing", missing),
            simpleFact("(case is ignored)"));
      }
    }

    /** Checks that the actual value does not contain the given sequence (while ignoring case). */
    public void doesNotContain(@Nullable CharSequence string) {
      checkNotNull(string);
      String expected = string.toString();
      if (actual == null) {
        failWithoutActual(
            fact("expected a string that does not contain", expected),
            butWas(),
            simpleFact("(case is ignored)"));
      } else if (containsIgnoreCase(actual, expected)) {
        failWithoutActual(
            fact("expected not to contain", expected), butWas(), simpleFact("(case is ignored)"));
      }
    }

    private static boolean containsIgnoreCase(String actual, String string) {
      for (int actualOffset = 0;
          actualOffset <= actual.length() - string.length();
          actualOffset++) {
        if (actual.regionMatches(
            /* ignoreCase= */ true,
            /* toffset= */ actualOffset,
            /* other= */ string,
            /* ooffset= */ 0,
            /* len= */ string.length())) {
          return true;
        }
      }
      return false;
    }

    private Fact butWas() {
      return subject.butWas();
    }

    private void failWithoutActual(Fact first, Fact... rest) {
      subject.failWithoutActual(first, rest);
    }

    static CaseInsensitiveStringComparison create(StringSubject stringSubject) {
      return new CaseInsensitiveStringComparison(stringSubject);
    }
  }

  static Factory<StringSubject, String> strings() {
    return StringSubject::new;
  }
}
