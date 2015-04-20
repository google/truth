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

import java.util.List;

import javax.annotation.Nullable;

/**
 * Propositions for arbitrarily typed subjects and for properties
 * of Object
 *
 * @author David Saff
 * @author Christian Gruber
 */
public class Subject<S extends Subject<S, T>, T> {
  protected final FailureStrategy failureStrategy;
  private final T subject;
  private String customName = null;

  public Subject(FailureStrategy failureStrategy, @Nullable T subject) {
    this.failureStrategy = checkNotNull(failureStrategy);
    this.subject = subject;
  }

  protected String internalCustomName() {
    return customName;
  }

  /**
   * Renames the subject so that this name appears in the error messages in place of string
   * representations of the subject.
   */
  @SuppressWarnings("unchecked")
  public S named(String name) {
    // TODO: use check().withFailureMessage... here?
    this.customName = checkNotNull(name, "Name passed to named() cannot be null.");
    return (S) this;
  }

  /**
   * Fails if the subject is not null.
   */
  public void isNull() {
    if (getSubject() != null) {
      fail("is null");
    }
  }

  /**
   * Fails if the subject is null.
   */
  public void isNotNull() {
    if (getSubject() == null) {
      failWithoutSubject("is a non-null reference");
    }
  }

  /**
   * Fails if the subject is not equal to the given object.
   */
  public void isEqualTo(@Nullable Object other) {
    if (!Objects.equal(getSubject(), other)) {
      fail("is equal to", other);
    }
  }

  /**
   * Fails if the subject is equal to the given object.
   */
  public void isNotEqualTo(@Nullable Object other) {
    if (Objects.equal(getSubject(), other)) {
      fail("is not equal to", other);
    }
  }

  /**
   * Fails if the subject is not the same instance as the given object.
   */
  public void isSameAs(@Nullable Object other) {
    if (getSubject() != other) {
      fail("is the same instance as", other);
    }
  }

  /**
   * Fails if the subject is the same instance as the given object.
   */
  public void isNotSameAs(@Nullable Object other) {
    if (getSubject() == other) {
      fail("is not the same instance as", other);
    }
  }

  /**
   * Fails if the subject is not an instance of the given class.
   */
  public void isInstanceOf(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException("clazz");
    }
    if (!Platform.isInstanceOfType(getSubject(), clazz)) {
      if (getSubject() != null) {
        failWithBadResults("is an instance of", clazz.getName(),
            "is an instance of", getSubject().getClass().getName());
      } else {
        fail("is an instance of", clazz.getName());
      }
    }
  }

  /**
   * Fails if the subject is an instance of the given class.
   */
  public void isNotInstanceOf(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException("clazz");
    }
    if (getSubject() == null) {
      return; // null is not an instance of clazz.
    }
    if (Platform.isInstanceOfType(getSubject(), clazz)) {
      failWithRawMessage("%s expected not to be an instance of %s, but was.",
          getDisplaySubject(), clazz.getName());
    }
  }

  /**
   * Fails unless the subject is equal to any element in the given iterable.
   */
  public void isIn(Iterable<?> iterable) {
    if (!Iterables.contains(iterable, getSubject())) {
      fail("is equal to any element in", iterable);
    }
  }

  /**
   * Fails unless the subject is equal to any of the given elements.
   */
  public void isAnyOf(@Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
    List<Object> list = accumulate(first, second, rest);
    if (!list.contains(getSubject())) {
      fail("is equal to any of", list);
    }
  }

  /**
   * Fails if the subject is equal to any element in the given iterable.
   */
  public void isNotIn(Iterable<?> iterable) {
    int index = Iterables.indexOf(iterable, Predicates.<Object>equalTo(getSubject()));
    if (index != -1 ) {
      failWithRawMessage("Not true that %s is not in %s. It was found at index %s",
          getDisplaySubject(), iterable, index);
    }
  }

  /**
   * Fails if the subject is equal to any of the given elements.
   */
  public void isNoneOf(@Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
    isNotIn(accumulate(first, second, rest));
  }

  protected T getSubject() {
    return subject;
  }

  protected String getDisplaySubject() {
    if (customName != null) {
      return customName + " (<" + getSubject() + ">)";
    } else {
      return "<" + getSubject() + ">";
    }
  }

  /**
   * A convenience for implementers of {@link Subject} subclasses to use other truth
   * {@code Subject} wrappers within their own propositional logic.
   */
  protected TestVerb check() {
    return new TestVerb(failureStrategy);
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   *
   * @param verb the proposition being asserted
   */
  protected void fail(String verb) {
    failureStrategy.fail("Not true that " + getDisplaySubject() + " " + verb);
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy. Also performs
   * disambiguation if the subject and {@code part} have the same toString()'s.
   *
   * @param verb the proposition being asserted
   * @param part the value against which the subject is compared
   */
  protected void fail(String verb, Object part) {
    StringBuilder message = new StringBuilder("Not true that ")
        .append(getDisplaySubject()).append(" ");
    // If the subject and parts aren't null, and they have equal toString()'s but different
    // classes, we need to disambiguate them.
    boolean needsDisambiguation = (part != null) && (getSubject() != null)
        && getSubject().toString().equals(part.toString())
        && !getSubject().getClass().equals(part.getClass());
    if (needsDisambiguation) {
      message.append("(").append(getSubject().getClass().getName()).append(") ");
    }
    message.append(verb).append(" <").append(part).append(">");
    if (needsDisambiguation) {
      message.append(" (").append(part.getClass().getName()).append(")");
    }
    failureStrategy.fail(message.toString());
  }

  /**
   * Assembles a failure message and passes such to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param messageParts the expectations against which the subject is compared
   */
  protected void fail(String verb, Object... messageParts) {
    // For backwards binary compatibility
    if (messageParts.length == 0) {
      fail(verb);
    } else if (messageParts.length == 1) {
      fail(verb, messageParts[0]);
    } else {
      StringBuilder message = new StringBuilder("Not true that ");
      message.append(getDisplaySubject()).append(" ").append(verb);
      for (Object part : messageParts) {
        message.append(" <").append(part).append(">");
      }
      failureStrategy.fail(message.toString());
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
  protected void failWithBadResults(String verb, Object expected, String failVerb, Object actual) {
    String message = format("Not true that %s %s <%s>. It %s <%s>",
            getDisplaySubject(),
            verb,
            expected,
            failVerb,
            ((actual == null) ? "null reference" : actual));
     failureStrategy.fail(message);
  }

  /**
   * Assembles a failure message with an alternative representation of the wrapped subject
   * and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   * @param expected the expected value of the proposition
   * @param actual the custom representation of the subject to be reported in the failure.
   */
  protected void failWithCustomSubject(String verb, Object expected, Object actual) {
    String message = format("Not true that <%s> %s <%s>",
        ((actual == null) ? "null reference" : actual),
        verb,
        expected);
    failureStrategy.fail(message);
  }

  /**
   * Assembles a failure message without a given subject and passes it to the FailureStrategy
   *
   * @param verb the proposition being asserted
   */
  protected void failWithoutSubject(String verb) {
    String subject = this.customName == null ? "the subject" : "\"" + customName + "\"";
    failureStrategy.fail(format("Not true that %s %s", subject, verb));
  }

  /**
   * Passes through a failure message verbatim.  Used for {@link Subject} subclasses which
   * need to provide alternate language for more fit-to-purpose error messages.
   *
   *
   * @param message the message template to be passed to the failure.  Note, this method only
   *     guarantees to process {@code %s} tokens.  It is not guaranteed to be compatible
   *     with {@code String.format()}.  Any other formatting desired (such as floats or
   *     scientific notation) should be performed before the method call and the formatted
   *     value passed in as a string.
   * @param parameters the object parameters which will be applied to the message template.
   */
  protected void failWithRawMessage(String message, Object ... parameters) {
    failureStrategy.fail(format(message, parameters));
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#equals(Object)} is not supported on Truth subjects.
   *     If you meant to test object equality, use {@link #isEqualTo(Object)} instead.
   */
  @Deprecated
  @Override
  public boolean equals(@Nullable Object o) {
    throw new UnsupportedOperationException(
        "If you meant to test object equality, use .isEqualTo(other) instead.");
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#hashCode()} is not supported on Truth subjects.
   */
  @Deprecated
  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
  }
}
