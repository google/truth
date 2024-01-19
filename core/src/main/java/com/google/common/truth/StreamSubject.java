/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.truth.Fact.fact;
import static java.util.stream.Collectors.toCollection;

import com.google.common.base.Supplier;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Propositions for {@link Stream} subjects.
 *
 * <p><b>Note:</b> When you perform an assertion based on the <i>contents</i> of the stream, or when
 * <i>any</i> assertion <i>fails</i>, the wrapped stream will be drained immediately into a private
 * collection to provide more readable failure messages. This consumes the stream. Take care if you
 * intend to leave the stream un-consumed or if the stream is <i>very</i> large or infinite.
 *
 * <p>If you intend to make multiple assertions on the contents of the same stream, you should
 * instead first collect the contents of the stream into a collection and then assert directly on
 * that.
 *
 * <p>For very large or infinite streams you may want to first {@linkplain Stream#limit limit} the
 * stream before asserting on it.
 *
 * @author Kurt Alfred Kluever
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@SuppressWarnings("Java7ApiChecker") // used only from APIs with Java 8 in their signatures
@IgnoreJRERequirement
public final class StreamSubject extends Subject {
  // Storing the FailureMetadata instance is not usually advisable.
  private final FailureMetadata metadata;
  private final Stream<?> actual;
  private final Supplier<@Nullable List<?>> listSupplier;

  StreamSubject(
      FailureMetadata metadata,
      @Nullable Stream<?> actual,
      Supplier<@Nullable List<?>> listSupplier) {
    super(metadata, actual);
    this.metadata = metadata;
    this.actual = actual;
    this.listSupplier = listSupplier;
  }

  StreamSubject(FailureMetadata metadata, @Nullable Stream<?> actual) {
    /*
     * As discussed in the Javadoc, we're a *little* accommodating of streams that have already been
     * collected (or are outright broken, like some mocks), and we avoid collecting the contents
     * until we want them. So, if you want to perform an assertion like
     * `assertThat(previousStream).isSameInstanceAs(firstStream)`, we'll let you do that, even if
     * you've already collected the stream. This way, `assertThat(Stream)` works as well as
     * `assertThat(Object)` for streams, following the usual rules of overloading. (This would also
     * help if we someday make `assertThat(Object)` automatically delegate to `assertThat(Stream)`
     * when passed a `Stream`.)
     */
    this(metadata, actual, memoize(listCollector(actual)));
  }

  @Override
  protected String actualCustomStringRepresentation() {
    List<?> asList;
    try {
      asList = listSupplier.get();
    } catch (IllegalStateException e) {
      return "Stream that has already been operated upon or closed: " + actual();
    }
    return String.valueOf(asList);
  }

  public static Subject.Factory<StreamSubject, Stream<?>> streams() {
    return StreamSubject::new;
  }

  /** Fails if the subject is not empty. */
  public void isEmpty() {
    checkThatContentsList().isEmpty();
  }

  /** Fails if the subject is empty. */
  public void isNotEmpty() {
    checkThatContentsList().isNotEmpty();
  }

  /**
   * Fails if the subject does not have the given size.
   *
   * <p>If you'd like to check that your stream contains more than {@link Integer#MAX_VALUE}
   * elements, use {@code assertThat(stream.count()).isEqualTo(...)}.
   */
  public void hasSize(int expectedSize) {
    checkThatContentsList().hasSize(expectedSize);
  }

  /** Fails if the subject does not contain the given element. */
  public void contains(@Nullable Object element) {
    checkThatContentsList().contains(element);
  }

  /** Fails if the subject contains the given element. */
  public void doesNotContain(@Nullable Object element) {
    checkThatContentsList().doesNotContain(element);
  }

  /** Fails if the subject contains duplicate elements. */
  public void containsNoDuplicates() {
    checkThatContentsList().containsNoDuplicates();
  }

  /** Fails if the subject does not contain at least one of the given elements. */
  public void containsAnyOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    checkThatContentsList().containsAnyOf(first, second, rest);
  }

  /** Fails if the subject does not contain at least one of the given elements. */
  public void containsAnyIn(Iterable<?> expected) {
    checkThatContentsList().containsAnyIn(expected);
  }

  /**
   * Fails if the subject does not contain all of the given elements. If an element appears more
   * than once in the given elements, then it must appear at least that number of times in the
   * actual elements.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The expected elements must appear in the given order
   * within the actual elements, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  public Ordered containsAtLeast(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    return checkThatContentsList().containsAtLeast(first, second, rest);
  }

  /**
   * Fails if the subject does not contain all of the given elements. If an element appears more
   * than once in the given elements, then it must appear at least that number of times in the
   * actual elements.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The expected elements must appear in the given order
   * within the actual elements, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  public Ordered containsAtLeastElementsIn(Iterable<?> expected) {
    return checkThatContentsList().containsAtLeastElementsIn(expected);
  }

  // TODO(cpovirk): Add array overload of contains*ElementsIn methods? Also for int and long stream.

  /**
   * Fails if the subject does not contain exactly the given elements.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3 times in the
   * parameters asserts that the object must likewise be duplicated exactly 3 times in the subject.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   */
  @CanIgnoreReturnValue
  /*
   * We need to call containsExactly, not containsExactlyElementsIn, to get the handling we want for
   * containsExactly(null).
   */
  @SuppressWarnings("ContainsExactlyVariadic")
  public Ordered containsExactly(@Nullable Object @Nullable ... varargs) {
    return checkThatContentsList().containsExactly(varargs);
  }

  /**
   * Fails if the subject does not contain exactly the given elements.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3 times in the
   * parameters asserts that the object must likewise be duplicated exactly 3 times in the subject.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactlyElementsIn(Iterable<?> expected) {
    return checkThatContentsList().containsExactlyElementsIn(expected);
  }

  /**
   * Fails if the subject contains any of the given elements. (Duplicates are irrelevant to this
   * test, which fails if any of the actual elements equal any of the excluded.)
   */
  public void containsNoneOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    checkThatContentsList().containsNoneOf(first, second, rest);
  }

  /**
   * Fails if the subject contains any of the given elements. (Duplicates are irrelevant to this
   * test, which fails if any of the actual elements equal any of the excluded.)
   */
  public void containsNoneIn(Iterable<?> excluded) {
    checkThatContentsList().containsNoneIn(excluded);
  }

  /**
   * Fails if the subject is not strictly ordered, according to the natural ordering of its
   * elements. Strictly ordered means that each element in the stream is <i>strictly</i> greater
   * than the element that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public void isInStrictOrder() {
    checkThatContentsList().isInStrictOrder();
  }

  /**
   * Fails if the subject is not strictly ordered, according to the given comparator. Strictly
   * ordered means that each element in the stream is <i>strictly</i> greater than the element that
   * preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   */
  public void isInStrictOrder(Comparator<?> comparator) {
    checkThatContentsList().isInStrictOrder(comparator);
  }

  /**
   * Fails if the subject is not ordered, according to the natural ordering of its elements. Ordered
   * means that each element in the stream is greater than or equal to the element that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public void isInOrder() {
    checkThatContentsList().isInOrder();
  }

  /**
   * Fails if the subject is not ordered, according to the given comparator. Ordered means that each
   * element in the stream is greater than or equal to the element that preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   */
  public void isInOrder(Comparator<?> comparator) {
    checkThatContentsList().isInOrder(comparator);
  }

  /**
   * @deprecated {@code streamA.isEqualTo(streamB)} always fails, except when passed the exact same
   *     stream reference. If you really want to test object identity, you can eliminate this
   *     deprecation warning by using {@link #isSameInstanceAs}. If you instead want to test the
   *     contents of the stream, use {@link #containsExactly} or similar methods.
   */
  @Override
  @Deprecated
  public void isEqualTo(@Nullable Object expected) {
    /*
     * We add a warning about stream equality. Doing so is a bit of a pain. (There might be a better
     * way.)
     *
     * Calling Subject constructors directly is not generally advisable. I'm not sure if the
     * metadata munging we perform is advisable, either....
     *
     * We do need to create a StreamSubject (rather than a plain Subject) in order to get our
     * desired string representation (unless we edit Subject itself to create and expose a
     * Supplier<List> when given a Stream...). And we have to call a special constructor to avoid
     * re-collecting the stream.
     */
    new StreamSubject(
            metadata.withMessage(
                "%s",
                new Object[] {
                  "Warning: Stream equality is based on object identity. To compare Stream"
                      + " contents, use methods like containsExactly."
                }),
            actual,
            listSupplier)
        .superIsEqualTo(expected);
  }

  private void superIsEqualTo(@Nullable Object expected) {
    super.isEqualTo(expected);
  }

  /**
   * @deprecated {@code streamA.isNotEqualTo(streamB)} always passes, except when passed the exact
   *     same stream reference. If you really want to test object identity, you can eliminate this
   *     deprecation warning by using {@link #isNotSameInstanceAs}. If you instead want to test the
   *     contents of the stream, collect both streams to lists and perform assertions like {@link
   *     IterableSubject#isNotEqualTo} on them. In some cases, you may be able to use {@link
   *     StreamSubject} assertions like {@link #doesNotContain}.
   */
  @Override
  @Deprecated
  public void isNotEqualTo(@Nullable Object unexpected) {
    if (actual() == unexpected) {
      /*
       * We override the supermethod's message: That method would ask for both
       * `String.valueOf(stream)` (for `unexpected`) and `actualCustomStringRepresentation()` (for
       * `actual()`). The two strings are almost certain to differ, since `valueOf` is normally
       * based on identity and `actualCustomStringRepresentation()` is based on contents. That can
       * lead to a confusing error message.
       *
       * We could include isEqualTo's warning about Stream's identity-based equality here, too. But
       * it doesn't seem necessary: The people we really want to warn are the people whose
       * assertions *pass*. And we've already attempted to do that with deprecation.
       */
      failWithoutActual(
          fact("expected not to be", actualCustomStringRepresentationForPackageMembersToCall()));
      return;
    }
    /*
     * But, if the objects aren't identical, we delegate to the supermethod (which checks equals())
     * just in case someone has decided to override Stream.equals in a strange way. (I haven't
     * checked whether this comes up in Google's codebase. I hope that it doesn't.)
     */
    super.isNotEqualTo(unexpected);
  }

  // TODO(user): Do we want to support comparingElementsUsing() on StreamSubject?

  private IterableSubject checkThatContentsList() {
    /*
     * Calling Subject constructors directly is usually not advisable: It does not update the
     * metadata, so the resultant failure message might say (for example) "value of: foo" when it
     * should say "value of: foo.size()." However, in this specific case, that's exactly what we
     * want: We're testing the contents of the stream, so we want a "value of" line for the stream,
     * even though we happen to implement the contents check by delegating to IterableSubject.
     */
    return new IterableSubject(
        metadata, listSupplier.get(), /* typeDescriptionOverride= */ "stream");
  }

  private static Supplier<@Nullable List<?>> listCollector(@Nullable Stream<?> actual) {
    return () -> actual == null ? null : actual.collect(toCollection(ArrayList::new));
  }
}
