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

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.LazyMessage.evaluateAll;
import static com.google.common.truth.Platform.cleanStackTrace;
import static com.google.common.truth.SubjectUtils.append;
import static com.google.common.truth.SubjectUtils.concat;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth.SimpleAssertionError;
import java.util.Set;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * An opaque, immutable object containing state from the previous calls in the fluent assertion
 * chain. It appears primarily as a parameter to {@link Subject} constructors (and {@link
 * Subject.Factory} methods), which should pass it to the superclass constructor and not otherwise
 * use or store it. In particular, users should not attempt to call {@code Subject} constructors or
 * {@code Subject.Factory} methods directly. Instead, they should use the appropriate factory
 * method:
 *
 * <ul>
 *   <li>If you're writing a test: {@link Truth#assertAbout(Subject.Factory)}{@code .that(...)}
 *   <li>If you're creating a derived subject from within another subject: {@code
 *       check().about(...).that(...)}
 *   <li>If you're testing your subject to verify that assertions fail when they should: {@link
 *       ExpectFailure}
 * </ul>
 *
 * <p>(One exception: Implementations of {@link CustomSubjectBuilder} do directly call constructors,
 * using their {@link CustomSubjectBuilder#metadata} method to get an instance to pass to the
 * constructor.)
 */
public final class FailureMetadata {
  static FailureMetadata forFailureStrategy(FailureStrategy failureStrategy) {
    return new FailureMetadata(
        failureStrategy, ImmutableList.<LazyMessage>of(), ImmutableList.<Step>of());
  }

  private final FailureStrategy strategy;

  /**
   * The data from a call to either (a) a {@link Subject} constructor or (b) {@link Subject#check}.
   */
  private static final class Step {
    static Step subjectCreation(Subject<?, ?> subject) {
      return new Step(checkNotNull(subject), null, null);
    }

    static Step checkCall(
        OldAndNewValuesAreSimilar valuesAreSimilar,
        @NullableDecl Function<String, String> descriptionUpdate) {
      return new Step(null, descriptionUpdate, valuesAreSimilar);
    }

    /*
     * We store Subject, rather than the actual value itself, so that we can call actualAsString(),
     * which lets subjects customize display through actualCustomStringRepresentation(). Why not
     * call actualAsString() immediately? First, it might be expensive, and second, the Subject
     * isn't initialized at the time we receive it. We *might* be able to make it safe to call if it
     * looks only at actual(), but it might try to look at facts initialized by a subclass, which
     * aren't ready yet.
     */
    @NullableDecl final Subject<?, ?> subject;

    @NullableDecl final Function<String, String> descriptionUpdate;

    // Present only when descriptionUpdate is.
    @NullableDecl final OldAndNewValuesAreSimilar valuesAreSimilar;

    private Step(
        @NullableDecl Subject<?, ?> subject,
        @NullableDecl Function<String, String> descriptionUpdate,
        @NullableDecl OldAndNewValuesAreSimilar valuesAreSimilar) {
      this.subject = subject;
      this.descriptionUpdate = descriptionUpdate;
      this.valuesAreSimilar = valuesAreSimilar;
    }

    boolean isCheckCall() {
      return subject == null;
    }
  }

  /*
   * TODO(cpovirk): This implementation is wasteful, especially because `steps` is used even by
   * non-chaining assertions. If it ever does matter, we could use an immutable cactus stack -- or
   * probably even avoid storing most of the chain entirely (unless we end up wanting more of the
   * chain to show "telescoping context," as in "the int value of this optional in this list in this
   * multimap").
   */

  private final ImmutableList<LazyMessage> messages;

  private final ImmutableList<Step> steps;

  FailureMetadata(
      FailureStrategy strategy, ImmutableList<LazyMessage> messages, ImmutableList<Step> steps) {
    this.strategy = checkNotNull(strategy);
    this.messages = checkNotNull(messages);
    this.steps = checkNotNull(steps);
  }

  /**
   * Returns a new instance that includes the given subject in its chain of values. Truth users do
   * not need to call this method directly; Truth automatically accumulates context, starting from
   * the initial that(...) call and continuing into any chained calls, like {@link
   * ThrowableSubject#hasMessageThat}.
   */
  FailureMetadata updateForSubject(Subject<?, ?> subject) {
    ImmutableList<Step> steps = append(this.steps, Step.subjectCreation(subject));
    return derive(messages, steps);
  }

  FailureMetadata updateForCheckCall() {
    ImmutableList<Step> steps = append(this.steps, Step.checkCall(null, null));
    return derive(messages, steps);
  }

  FailureMetadata updateForCheckCall(
      OldAndNewValuesAreSimilar valuesAreSimilar, Function<String, String> descriptionUpdate) {
    checkNotNull(descriptionUpdate);
    ImmutableList<Step> steps =
        append(this.steps, Step.checkCall(valuesAreSimilar, descriptionUpdate));
    return derive(messages, steps);
  }

  /**
   * Whether the value of the original subject and the value of the derived subject are "similar
   * enough" that we don't need to display both. For example, if we're printing a message about the
   * value of optional.get(), there's no need to print the optional itself because it adds no
   * information. Similarly, if we're printing a message about the asList() view of an array,
   * there's no need to also print the array.
   */
  enum OldAndNewValuesAreSimilar {
    SIMILAR,
    DIFFERENT;
  }

  /**
   * Returns a new instance whose failures will contain the given message. The way for Truth users
   * to set a message is {@code check().withMessage(...).that(...)} (for calls from within a {@code
   * Subject}) or {@link Truth#assertWithMessage} (for most other calls).
   */
  FailureMetadata withMessage(String format, Object[] args) {
    ImmutableList<LazyMessage> messages = append(this.messages, new LazyMessage(format, args));
    return derive(messages, steps);
  }

  void failEqualityCheck(
      ImmutableList<Fact> headFacts,
      ImmutableList<Fact> tailFacts,
      String expected,
      String actual) {
    doFail(
        ComparisonFailureWithFacts.create(
            evaluateAll(messages),
            concat(descriptionAsFacts(), headFacts),
            concat(tailFacts, rootUnlessThrowableAsFacts()),
            expected,
            actual,
            rootCause().orNull()));
  }

  void fail(ImmutableList<Fact> facts) {
    doFail(
        AssertionErrorWithFacts.create(
            evaluateAll(messages),
            concat(descriptionAsFacts(), facts, rootUnlessThrowableAsFacts()),
            rootCause().orNull()));
  }

  void fail(String message) {
    doFail(
        SimpleAssertionError.create(
            addToMessage(message), rootUnlessThrowableAsString(), rootCause().orNull()));
  }

  void fail(String message, Throwable cause) {
    doFail(
        SimpleAssertionError.create(addToMessage(message), rootUnlessThrowableAsString(), cause));
    // TODO(cpovirk): add rootCause() as a suppressed exception?
  }

  void failComparing(String message, CharSequence expected, CharSequence actual) {
    doFail(
        new JUnitComparisonFailure(
            addToMessage(message),
            expected.toString(),
            actual.toString(),
            rootUnlessThrowableAsString(),
            rootCause().orNull()));
  }

  void failComparing(String message, CharSequence expected, CharSequence actual, Throwable cause) {
    doFail(
        new JUnitComparisonFailure(
            addToMessage(message),
            expected.toString(),
            actual.toString(),
            rootUnlessThrowableAsString(),
            cause));
    // TODO(cpovirk): add rootCause() as a suppressed exception?
  }

  private void doFail(AssertionError failure) {
    cleanStackTrace(failure);
    strategy.fail(failure);
  }

  private String addToMessage(String body) {
    ImmutableList<?> messages = allPrefixMessages();
    StringBuilder result = new StringBuilder(body.length());
    Joiner.on("\n").appendTo(result, messages);
    if (!messages.isEmpty()) {
      result.append("\n");
    }
    result.append(body);
    /*
     * JUnit's ComparisonFailure will append " expected: <...> but was: <...> to the end of this.
     * Note the space at the beginning. That means that, if the body is empty, the "expected... but
     * was" text will come at the beginning of a line... indented by one space :\ The right fix for
     * this is to stop depending on JUnit's ComparisonFailure formatting. That's coming. For now,
     * the space is an annoyance.
     */
    return result.toString();
  }

  private ImmutableList<?> allPrefixMessages() {
    return concat(messages, descriptionAsStrings());
  }

  private FailureMetadata derive(ImmutableList<LazyMessage> messages, ImmutableList<Step> steps) {
    return new FailureMetadata(strategy, messages, steps);
  }

  /**
   * Returns a description of how the final actual value was derived from earlier actual values in
   * the chain, if the chain ends with at least one derivation that we have a name for.
   *
   * <p>We don't want to say: "value of string: expected [foo] but was [bar]" (OK, we might still
   * decide to say this, but for now, we don't.)
   *
   * <p>We do want to say: "value of throwable.getMessage(): expected [foo] but was [bar]"
   *
   * <p>To support that, {@code descriptionWasDerived} tracks whether we've been given context
   * through {@code check} calls <i>that include names</i>.
   *
   * <p>If we're missing a naming function halfway through, we have to reset: We don't want to claim
   * that the value is "foo.bar.baz" when it's "foo.bar.somethingelse.baz." We have to go back to
   * "object.baz." (But note that {@link #rootUnlessThrowable} will still provide the value of the
   * root foo to the user as long as we had at least one naming function: We might not know the
   * root's exact relationship to the final object, but we know it's some object "different enough"
   * to be worth displaying.)
   */
  private Optional<Fact> description() {
    String description = null;
    boolean descriptionWasDerived = false;
    for (Step step : steps) {
      if (step.isCheckCall()) {
        checkState(description != null);
        if (step.descriptionUpdate == null) {
          description = null;
          descriptionWasDerived = false;
        } else {
          description = verifyNotNull(step.descriptionUpdate.apply(description));
          descriptionWasDerived = true;
        }
        continue;
      }

      if (description == null) {
        description =
            firstNonNull(step.subject.internalCustomName(), step.subject.typeDescription());
      }
    }
    return descriptionWasDerived
        ? Optional.of(fact("value of", description))
        : Optional.<Fact>absent();
  }

  private ImmutableList<Fact> descriptionAsFacts() {
    return ImmutableList.copyOf(description().asSet());
  }

  private Set<String> descriptionAsStrings() {
    return description().transform(toStringFunction()).asSet();
  }

  /**
   * Returns the root actual value, if we know it's "different enough" from the final actual value.
   *
   * <p>We don't want to say: "expected [foo] but was [bar]. string: [bar]"
   *
   * <p>We do want to say: "expected [foo] but was [bar]. myObject: MyObject[string=bar, i=0]"
   *
   * <p>To support that, {@code seenDerivation} tracks whether we've seen multiple actual values,
   * which is equivalent to whether we've seen multiple Subject instances or, more informally,
   * whether the user is making a chained assertion.
   *
   * <p>There's one wrinkle: Sometimes chaining doesn't add information. This is often true with
   * "internal" chaining, like when StreamSubject internally creates an IterableSubject to delegate
   * to. The two subjects' string representations will be identical (or, in some cases, _almost_
   * identical), so there is no value in showing both. In such cases, implementations can call the
   * no-arg {@code checkNoNeedToDisplayBothValues()}, which sets {@code valuesAreSimilar},
   * instructing this method that that particular chain link "doesn't count." (Note also that there
   * are some edge cases that we're not sure how to handle yet, for which we might introduce
   * additional {@code check}-like methods someday.)
   */
  /*
   * TODO(cpovirk): Consider returning multiple facts in some cases. For example, for
   * assertThat(multimap).valuesForKey(key).hasSize(size), it might be useful for us to display the
   * valuesForKey collection as well as the multimap it's part of. Probably displaying only 2
   * objects -- first and last (well, last besides the one we're already displaying in the main
   * message) would be enough.
   */
  private Optional<Fact> rootUnlessThrowable() {
    Step rootSubject = null;
    boolean seenDerivation = false;
    for (Step step : steps) {
      if (step.isCheckCall()) {
        /*
         * If we don't have a description update, don't trigger display of a root object. (If we
         * did, we'd change the messages of a bunch of existing subjects, and we don't want to bite
         * that off yet.)
         *
         * If we do have a description update, then trigger display of a root object but only if the
         * old and new values are "different enough" to be worth both displaying.
         */
        seenDerivation |=
            step.descriptionUpdate != null
                && step.valuesAreSimilar == OldAndNewValuesAreSimilar.DIFFERENT;
        continue;
      }

      if (rootSubject == null) {
        if (step.subject.actual() instanceof Throwable) {
          /*
           * We'll already include the Throwable as a cause of the AssertionError (see rootCause()),
           * so we don't need to include it again in the message.
           */
          return Optional.absent();
        }
        rootSubject = step;
      }
    }
    /*
     * TODO(cpovirk): Maybe say "root foo was: ..." instead of just "foo was: ..." if there's more
     * than one foo in the chain, if the description string doesn't start with "foo," and/or if the
     * name we have is just "object?"
     */
    return seenDerivation
        ? Optional.of(
            fact(
                rootSubject.subject.typeDescription() + " was",
                rootSubject.subject.actualAsStringNoBrackets()))
        : Optional.<Fact>absent();
  }

  private ImmutableList<Fact> rootUnlessThrowableAsFacts() {
    return ImmutableList.copyOf(rootUnlessThrowable().asSet());
  }

  @NullableDecl
  private String rootUnlessThrowableAsString() {
    return rootUnlessThrowable().transform(toStringFunction()).orNull();
  }

  /**
   * Returns the first {@link Throwable} in the chain of actual values. Typically, we'll have a root
   * cause only if the assertion chain contains a {@link ThrowableSubject}.
   */
  private Optional<Throwable> rootCause() {
    for (Step step : steps) {
      if (!step.isCheckCall() && step.subject.actual() instanceof Throwable) {
        return Optional.of((Throwable) step.subject.actual());
      }
    }
    return Optional.absent();
  }
}
