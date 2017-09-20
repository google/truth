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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.StringUtil.format;
import static com.google.common.truth.SubjectUtils.accumulate;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.List;
import javax.annotation.Nullable;

/**
 * An object that lets you perform checks on the value under test. For example, {@code Subject}
 * contains {@link #isEqualTo(Object)} and {@link #isInstanceOf(Class)}, and {@link StringSubject}
 * contains {@link StringSubject#startsWith startsWith(String)}.
 *
 * <p>TODO(cpovirk): Link to a doc about the full assertion chain.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>TODO(cpovirk): Link to a doc about custom subjects.
 *
 * @param <S> the self-type, allowing {@code this}-returning methods to avoid needing subclassing
 * @param <T> the type of the object being tested by this {@code Subject}
 * @author David Saff
 * @author Christian Gruber
 */
public class Subject<S extends Subject<S, T>, T> {
  /**
   * In a fluent assertion chain, the argument to the common overload of {@link
   * StandardSubjectBuilder#about(Subject.Factory) about}, the method that specifies what kind of
   * {@link Subject} to create.
   *
   * <p>TODO(cpovirk): Link to a doc about the full assertion chain.
   *
   * <h3>For people extending Truth</h3>
   *
   * <p>TODO(cpovirk): Link to a doc about custom subjects.
   */
  public interface Factory<SubjectT extends Subject<SubjectT, ActualT>, ActualT> {
    /** Creates a new {@link Subject}. */
    SubjectT createSubject(FailureMetadata metadata, ActualT actual);
  }

  private static final FailureStrategy IGNORE_STRATEGY =
      new AbstractFailureStrategy() {
        @Override
        public void fail(String message, Throwable cause) {}
      };

  /**
   * @deprecated If you are calling {@code metadata.fail*} directly, instead call {@link
   *     #failWithRawMessage}, {@link #failWithRawMessageAndCause}, or {@link #failComparing}. If
   *     you are passing {@code metadata} to a {@code Subject} constructor, instead call {@link
   *     #check()}{@code .that(...)} or {@code check().about(...).that(...)}. (You might need to
   *     create the {@link SubjectFactory} that is the argument to {@code about}.)
   */
  @Deprecated protected final FailureStrategy failureStrategy;
  // TODO(cpovirk): Make private after MultimapSubject no longer needs it.
  final FailureMetadata metadata;

  private final T actual;
  private String customName = null;

  /**
   * @deprecated Switch your {@code Subject} from accepting {@link FailureStrategy} (and exposing a
   *     {@link SubjectFactory}) to accepting a {@link FailureMetadata} (and exposing a {@link
   *     Subject.Factory}), at which point you'll call the {@code FailureMetadata} overload of this
   *     constructor instead.
   */
  @Deprecated
  public Subject(FailureStrategy failureStrategy, @Nullable T actual) {
    this(FailureMetadata.forFailureStrategy(failureStrategy), actual);
  }

  // TODO(cpovirk): Make this protected.
  public Subject(FailureMetadata metadata, @Nullable T actual) {
    this.metadata =
        actual instanceof Throwable ? metadata.offerRootCause((Throwable) actual) : metadata;
    this.failureStrategy = this.metadata.legacyStrategy();
    this.actual = actual;
  }

  /** An internal method used to obtain the value set by {@link #named(String, Object...)}. */
  protected String internalCustomName() {
    return customName;
  }

  /**
   * Adds a prefix to the subject, when it is displayed in error messages. This is especially useful
   * in the context of types that have no helpful {@code toString()} representation, e.g. boolean.
   * Writing {@code assertThat(foo).named("foo").isTrue();} then results in a more reasonable error
   * message.
   *
   * <p>{@code named()} takes a format template and argument objects which will be substituted into
   * the template, similar to {@link String#format(String, Object...)}, the chief difference being
   * that extra parameters (for which there are no template variables) will be appended to the
   * resulting string in brackets. Additionally, this only supports the {@code %s} template variable
   * type.
   */
  @SuppressWarnings("unchecked")
  @CanIgnoreReturnValue
  public S named(String format, Object... args) {
    checkNotNull(format, "Name passed to named() cannot be null.");
    this.customName = StringUtil.format(format, args);
    return (S) this;
  }

  /** Fails if the subject is not null. */
  public void isNull() {
    if (actual() != null) {
      fail("is null");
    }
  }

  /** Fails if the subject is null. */
  public void isNotNull() {
    if (actual() == null) {
      failWithoutActual("is a non-null reference");
    }
  }

  /**
   * Fails if the subject is not equal to the given object. For the purposes of this comparison, two
   * objects are equal if any of the following is true:
   *
   * <ul>
   *   <li>they are equal according to {@link Objects#equal}
   *   <li>they are arrays and are considered equal by the appropriate {@link Arrays#equals}
   *       overload
   *   <li>they are boxed integer types ({@code Byte}, {@code Short}, {@code Character}, {@code
   *       Integer}, or {@code Long}) and they are numerically equal when converted to {@code Long}.
   * </ul>
   */
  public void isEqualTo(@Nullable Object other) {
    doEqualCheck(actual(), other, true);
  }

  /**
   * Fails if the subject is equal to the given object. The meaning of equality is the same as for
   * the {@link #isEqualTo} method.
   */
  public void isNotEqualTo(@Nullable Object other) {
    doEqualCheck(actual(), other, false);
  }

  private void doEqualCheck(
      @Nullable Object rawActual, @Nullable Object rawOther, boolean expectEqual) {
    Object actual;
    Object other;
    if (isIntegralBoxedPrimitive(rawActual) && isIntegralBoxedPrimitive(rawOther)) {
      actual = integralValue(rawActual);
      other = integralValue(rawOther);
    } else {
      actual = rawActual;
      other = rawOther;
    }
    if (Objects.equal(actual, other) != expectEqual) {
      failComparingToStrings(
          expectEqual ? "is equal to" : "is not equal to", actual, other, rawOther, expectEqual);
    }
  }

  private static boolean isIntegralBoxedPrimitive(@Nullable Object o) {
    return o instanceof Byte
        || o instanceof Short
        || o instanceof Character
        || o instanceof Integer
        || o instanceof Long;
  }

  private static Long integralValue(Object o) {
    if (o instanceof Character) {
      return (long) ((Character) o).charValue();
    } else if (o instanceof Number) {
      return ((Number) o).longValue();
    } else {
      throw new AssertionError(o + " must be either a Character or a Number.");
    }
  }

  /** Fails if the subject is not the same instance as the given object. */
  public void isSameAs(@Nullable Object other) {
    if (actual() != other) {
      failComparingToStrings("is the same instance as", actual(), other, other, true);
    }
  }

  /** Fails if the subject is the same instance as the given object. */
  public void isNotSameAs(@Nullable Object other) {
    if (actual() == other) {
      fail("is not the same instance as", other);
    }
  }

  /** Fails if the subject is not an instance of the given class. */
  public void isInstanceOf(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException("clazz");
    }
    if (!Platform.isInstanceOfType(actual(), clazz)) {
      if (actual() != null) {
        if (classMetadataUnsupported()) {
          throw new UnsupportedOperationException(
              actualAsString()
                  + ", an instance of "
                  + actual().getClass().getName()
                  + ", may or may not be an instance of "
                  + clazz.getName()
                  + ". Under -XdisableClassMetadata, we do not have enough information to tell.");
        }
        failWithBadResults(
            "is an instance of",
            clazz.getName(),
            "is an instance of",
            actual().getClass().getName());
      } else {
        fail("is an instance of", clazz.getName());
      }
    }
  }

  /** Fails if the subject is an instance of the given class. */
  public void isNotInstanceOf(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException("clazz");
    }
    if (classMetadataUnsupported()) {
      throw new UnsupportedOperationException(
          "isNotInstanceOf is not supported under -XdisableClassMetadata");
    }
    if (actual() == null) {
      return; // null is not an instance of clazz.
    }
    if (Platform.isInstanceOfType(actual(), clazz)) {
      failWithRawMessage(
          "%s expected not to be an instance of %s, but was.", actualAsString(), clazz.getName());
    }
  }

  /** Fails unless the subject is equal to any element in the given iterable. */
  public void isIn(Iterable<?> iterable) {
    if (!Iterables.contains(iterable, actual())) {
      fail("is equal to any element in", iterable);
    }
  }

  /** Fails unless the subject is equal to any of the given elements. */
  public void isAnyOf(@Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
    List<Object> list = accumulate(first, second, rest);
    if (!list.contains(actual())) {
      fail("is equal to any of", list);
    }
  }

  /** Fails if the subject is equal to any element in the given iterable. */
  public void isNotIn(Iterable<?> iterable) {
    int index = Iterables.indexOf(iterable, Predicates.<Object>equalTo(actual()));
    if (index != -1) {
      failWithRawMessage(
          "Not true that %s is not in %s. It was found at index %s",
          actualAsString(), iterable, index);
    }
  }

  /** Fails if the subject is equal to any of the given elements. */
  public void isNoneOf(@Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
    isNotIn(accumulate(first, second, rest));
  }

  /** @deprecated Prefer {@code #actual()} for direct access to the subject. */
  @Deprecated
  protected T getSubject() {
    // TODO(cgruber): move functionality to actual() and delete when no callers.
    return actual;
  }

  /** Returns the unedited, unformatted raw actual value. */
  protected final T actual() {
    return getSubject();
  }

  /** @deprecated Prefer {@code #actualAsString()} for display-formatted access to the subject. */
  @Deprecated
  protected String getDisplaySubject() {
    // TODO(cgruber) migrate people from this method once no one is subclassing it.
    String formatted = actualCustomStringRepresentation();
    if (customName != null) {
      // Covers some rare cases where a type might return "" from their custom formatter.
      // This is actually pretty terrible, as it comes from subjects overriding (formerly)
      // getDisplaySubject() in cases of .named() to make it not prefixing but replacing.
      // That goes against the stated contract of .named().  Once displayedAs() is in place,
      // we can rip this out and callers can use that instead.
      // TODO(cgruber)
      return customName + (formatted.isEmpty() ? "" : " (<" + formatted + ">)");
    } else {
      return "<" + formatted + ">";
    }
  }

  /**
   * Returns a string representation of the actual value. This will either be the toString() of the
   * value or a prefixed "name" along with the string representation.
   */
  protected final String actualAsString() {
    return getDisplaySubject();
  }

  /**
   * Supplies the direct string representation of the actual value to other methods which may prefix
   * or otherwise position it in an error message. This should only be overridden to provide an
   * improved string representation of the value under test, as it would appear in any given error
   * message, and should not be used for additional prefixing.
   *
   * <p>Subjects should override this with care.
   *
   * <p>By default, this returns {@code String.ValueOf(getActualValue())}.
   */
  protected String actualCustomStringRepresentation() {
    return String.valueOf(actual());
  }

  /**
   * A convenience for implementers of {@link Subject} subclasses to use other truth {@code Subject}
   * wrappers within their own propositional logic.
   */
  // TODO(diamondm) this should be final, can we do that safely?
  protected StandardSubjectBuilder check() {
    return new StandardSubjectBuilder(metadata);
  }

  /**
   * A convenience for implementers of {@link Subject} subclasses that use other truth {@code
   * Subject} wrappers within their own propositional logic <i>and</i> sometimes need to
   * short-circuit those subjects because the assertion chain has already failed. In such cases it
   * may still be necessary to return a {@code Subject} instance even though any subsequent
   * assertions are meaningless. Use this method to return subjects that will never report failures.
   */
  protected final StandardSubjectBuilder ignoreCheck() {
    return StandardSubjectBuilder.forCustomFailureStrategy(IGNORE_STRATEGY);
  }

  /**
   * Reports a failure constructing a message from a simple verb.
   *
   * @param proposition the proposition being asserted
   */
  protected final void fail(String proposition) {
    metadata.legacyStrategy().fail("Not true that " + actualAsString() + " " + proposition);
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy. Also performs
   * disambiguation if the subject and {@code other} have the same toString()'s.
   *
   * @param verb the proposition being asserted
   * @param other the value against which the subject is compared
   */
  protected final void fail(String verb, Object other) {
    failComparingToStrings(verb, actual(), other, other, false);
  }

  private void failComparingToStrings(
      String verb, Object actual, Object other, Object displayOther, boolean compareToStrings) {
    StringBuilder message =
        new StringBuilder("Not true that ").append(actualAsString()).append(" ");
    // If the actual and parts aren't null, and they have equal toString()'s but different
    // classes, we need to disambiguate them.
    boolean neitherNull = (other != null) && (actual != null);
    boolean sameToStrings = actualCustomStringRepresentation().equals(String.valueOf(other));
    boolean needsClassDisambiguation =
        neitherNull && sameToStrings && !actual.getClass().equals(other.getClass());
    if (needsClassDisambiguation) {
      message.append("(").append(actual.getClass().getName()).append(") ");
    }
    message.append(verb).append(" <").append(displayOther).append(">");
    if (needsClassDisambiguation) {
      message.append(" (").append(other.getClass().getName()).append(")");
    }
    if (!needsClassDisambiguation && sameToStrings && compareToStrings) {
      message.append(" (although their toString() representations are the same)");
    }
    metadata.legacyStrategy().fail(message.toString());
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param messageParts the expectations against which the subject is compared
   */
  protected final void fail(String verb, Object... messageParts) {
    // For backwards binary compatibility
    if (messageParts.length == 0) {
      fail(verb);
    } else if (messageParts.length == 1) {
      fail(verb, messageParts[0]);
    } else {
      StringBuilder message = new StringBuilder("Not true that ");
      message.append(actualAsString()).append(" ").append(verb);
      for (Object part : messageParts) {
        message.append(" <").append(part).append(">");
      }
      metadata.legacyStrategy().fail(message.toString());
    }
  }

  /**
   * Assembles a failure message and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param expected the expectations against which the subject is compared
   * @param failVerb the failure of the proposition being asserted
   * @param actual the actual value the subject was compared against
   */
  protected final void failWithBadResults(
      String verb, Object expected, String failVerb, Object actual) {
    String message =
        format(
            "Not true that %s %s <%s>. It %s <%s>",
            actualAsString(),
            verb,
            expected,
            failVerb,
            (actual == null) ? "null reference" : actual);
    metadata.legacyStrategy().fail(message);
  }

  /**
   * Assembles a failure message with an alternative representation of the wrapped subject and
   * passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param expected the expected value of the proposition
   * @param actual the custom representation of the subject to be reported in the failure.
   */
  protected final void failWithCustomSubject(String verb, Object expected, Object actual) {
    String message =
        format(
            "Not true that <%s> %s <%s>",
            (actual == null) ? "null reference" : actual, verb, expected);
    metadata.legacyStrategy().fail(message);
  }

  /** @deprecated Use {@link #failWithoutActual(String)} */
  @Deprecated
  protected final void failWithoutSubject(String proposition) {
    String strSubject = this.customName == null ? "the subject" : "\"" + customName + "\"";
    metadata.legacyStrategy().fail(format("Not true that %s %s", strSubject, proposition));
  }

  /**
   * Assembles a failure message without a given subject and passes it to the FailureStrategy
   *
   * @param proposition the proposition being asserted
   */
  protected final void failWithoutActual(String proposition) {
    failWithoutSubject(proposition);
  }

  /**
   * Passes through a failure message verbatim. Used for {@link Subject} subclasses which need to
   * provide alternate language for more fit-to-purpose error messages.
   *
   * @param message the message template to be passed to the failure. Note, this method only
   *     guarantees to process {@code %s} tokens. It is not guaranteed to be compatible with {@code
   *     String.format()}. Any other formatting desired (such as floats or scientific notation)
   *     should be performed before the method call and the formatted value passed in as a string.
   * @param parameters the object parameters which will be applied to the message template.
   */
  // TODO(cgruber) final
  protected void failWithRawMessage(String message, Object... parameters) {
    metadata.legacyStrategy().fail(format(message, parameters));
  }

  /** Passes through a failure message verbatim, along with a cause. */
  protected final void failWithRawMessageAndCause(String message, Throwable cause) {
    metadata.legacyStrategy().fail(message, cause);
  }

  /**
   * Passes through a failure message verbatim, along with the expected and actual values that the
   * {@link FailureStrategy} may use to construct a {@code ComparisonFailure}.
   */
  protected final void failComparing(String message, CharSequence expected, CharSequence actual) {
    metadata.legacyStrategy().failComparing(message, expected, actual);
  }

  /**
   * Passes through a failure message verbatim, along with a cause and the expected and actual
   * values that the {@link FailureStrategy} may use to construct a {@code ComparisonFailure}.
   */
  protected final void failComparing(
      String message, CharSequence expected, CharSequence actual, Throwable cause) {
    metadata.legacyStrategy().failComparing(message, expected, actual, cause);
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#equals(Object)} is not supported on Truth subjects. If you meant to
   *     test object equality between an expected and the actual value, use {@link
   *     #isEqualTo(Object)} instead.
   */
  @Deprecated
  @Override
  public final boolean equals(@Nullable Object o) {
    throw new UnsupportedOperationException(
        "If you meant to test object equality, use .isEqualTo(other) instead.");
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#hashCode()} is not supported on Truth subjects.
   */
  @Deprecated
  @Override
  public final int hashCode() {
    throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
  }

  private static boolean classMetadataUnsupported() {
    // https://github.com/google/truth/issues/198
    // TODO(cpovirk): Consider whether to remove instanceof tests under GWT entirely.
    // TODO(cpovirk): Run more Truth tests under GWT, and add tests for this.
    return String.class.getSuperclass() == null;
  }
}
