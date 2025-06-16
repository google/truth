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
import static java.util.stream.Collectors.toCollection;

import com.google.common.base.Supplier;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Stream} values.
 *
 * <p><b>Note:</b> When you perform an assertion based on the <i>contents</i> of the stream, or when
 * <i>any</i> assertion <i>fails</i>, the wrapped stream will be drained immediately into a private
 * collection to provide more readable failure messages. This consumes the stream. Take care if you
 * intend to leave the stream un-consumed or if the stream is <i>very</i> large or infinite.
 *
 * <p>If you intend to make multiple assertions on the contents of the same stream, you should
 * instead first collect the contents of the stream into a collection and then assert directly on
 * that. For example:
 *
 * <pre>{@code
 * List<Integer> list = makeStream().map(...).filter(...).collect(toImmutableList());
 * assertThat(list).contains(5);
 * assertThat(list).doesNotContain(2);
 * }</pre>
 *
 * <p>For very large or infinite streams, you may want to first {@linkplain Stream#limit limit} the
 * stream before asserting on it.
 *
 * @author Kurt Alfred Kluever
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@IgnoreJRERequirement
public final class StreamSubject extends Subject {
  private final @Nullable Stream<?> actual;
  private final Supplier<@Nullable List<?>> listSupplier;

  private StreamSubject(
      FailureMetadata metadata,
      @Nullable Stream<?> actual,
      Supplier<@Nullable List<?>> listSupplier) {
    super(metadata, actual);
    this.actual = actual;
    this.listSupplier = listSupplier;
  }

  private StreamSubject(FailureMetadata metadata, @Nullable Stream<?> actual) {
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

  /**
   * Obsolete factory instance. This factory was previously necessary for assertions like {@code
   * assertWithMessage(...).about(streams()).that(stream)....}. Now, you can perform assertions like
   * that without the {@code about(...)} call.
   *
   * @deprecated Instead of {@code about(streams()).that(...)}, use just {@code that(...)}.
   *     Similarly, instead of {@code assertAbout(streams()).that(...)}, use just {@code
   *     assertThat(...)}.
   */
  @Deprecated
  @SuppressWarnings("InlineMeSuggester") // We want users to remove the surrounding call entirely.
  public static Factory<StreamSubject, Stream<?>> streams() {
    return StreamSubject::new;
  }

  /**
   * Factory instance for creating an instance for which we already have a {@code listSupplier} from
   * an existing instance. Naturally, the resulting factory should be used to create an instance
   * only for the stream corresponding to {@code listSupplier}.
   */
  private static Factory<StreamSubject, Stream<?>> streams(
      Supplier<@Nullable List<?>> listSupplier) {
    return (metadata, actual) -> new StreamSubject(metadata, actual, listSupplier);
  }

  /** Checks that the actual stream is empty. */
  public void isEmpty() {
    checkThatContentsList().isEmpty();
  }

  /** Checks that the actual stream is not empty. */
  public void isNotEmpty() {
    checkThatContentsList().isNotEmpty();
  }

  /**
   * Checks that the actual stream has the given size.
   *
   * <p>If you'd like to check that your stream contains more than {@link Integer#MAX_VALUE}
   * elements, use {@code assertThat(stream.count()).isEqualTo(...)}.
   */
  public void hasSize(int size) {
    checkThatContentsList().hasSize(size);
  }

  /** Checks that the actual stream contains the given element. */
  public void contains(@Nullable Object element) {
    checkThatContentsList().contains(element);
  }

  /** Checks that the actual stream does not contain the given element. */
  public void doesNotContain(@Nullable Object element) {
    checkThatContentsList().doesNotContain(element);
  }

  /** Checks that the actual stream does not contain duplicate elements. */
  public void containsNoDuplicates() {
    checkThatContentsList().containsNoDuplicates();
  }

  /** Checks that the actual stream contains at least one of the given elements. */
  public void containsAnyOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    checkThatContentsList().containsAnyOf(first, second, rest);
  }

  /** Checks that the actual stream contains at least one of the given elements. */
  public void containsAnyIn(@Nullable Iterable<?> expected) {
    checkThatContentsList().containsAnyIn(expected);
  }

  /**
   * Checks that the actual stream contains all of the given elements. If an element appears more
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
   * Checks that the actual stream contains all of the given elements. If an element appears more
   * than once in the given elements, then it must appear at least that number of times in the
   * actual elements.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method. The expected elements must appear in the given order
   * within the actual elements, but they are not required to be consecutive.
   */
  @CanIgnoreReturnValue
  public Ordered containsAtLeastElementsIn(@Nullable Iterable<?> expected) {
    return checkThatContentsList().containsAtLeastElementsIn(expected);
  }

  // TODO(cpovirk): Add array overload of contains*ElementsIn methods? Also for int and long stream.

  /**
   * Checks that the actual stream contains exactly the given elements.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3 times in the
   * parameters asserts that the object must likewise be duplicated exactly 3 times in the actual
   * stream.
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
  public Ordered containsExactly(@Nullable Object @Nullable ... expected) {
    return checkThatContentsList().containsExactly(expected);
  }

  /**
   * Checks that the actual stream contains exactly the given elements.
   *
   * <p>Multiplicity is respected. For example, an object duplicated exactly 3 times in the
   * parameters asserts that the object must likewise be duplicated exactly 3 times in the actual
   * stream.
   *
   * <p>To also test that the contents appear in the given order, make a call to {@code inOrder()}
   * on the object returned by this method.
   */
  @CanIgnoreReturnValue
  public Ordered containsExactlyElementsIn(@Nullable Iterable<?> expected) {
    return checkThatContentsList().containsExactlyElementsIn(expected);
  }

  /** Checks that the actual stream does not contain any of the given elements. */
  public void containsNoneOf(
      @Nullable Object first, @Nullable Object second, @Nullable Object @Nullable ... rest) {
    checkThatContentsList().containsNoneOf(first, second, rest);
  }

  /** Checks that the actual stream does not contain any of the given elements. */
  public void containsNoneIn(@Nullable Iterable<?> excluded) {
    checkThatContentsList().containsNoneIn(excluded);
  }

  /**
   * Checks that the actual stream is strictly ordered, according to the natural ordering of its
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
   * Checks that the actual stream is strictly ordered, according to the given comparator. Strictly
   * ordered means that each element in the stream is <i>strictly</i> greater than the element that
   * preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   */
  public void isInStrictOrder(Comparator<?> comparator) {
    checkThatContentsList().isInStrictOrder(comparator);
  }

  /**
   * Checks that the actual stream is ordered, according to the natural ordering of its elements.
   * Ordered means that each element in the stream is greater than or equal to the element that
   * preceded it.
   *
   * @throws ClassCastException if any pair of elements is not mutually Comparable
   * @throws NullPointerException if any element is null
   */
  public void isInOrder() {
    checkThatContentsList().isInOrder();
  }

  /**
   * Checks that the actual stream is ordered, according to the given comparator. Ordered means that
   * each element in the stream is greater than or equal to the element that preceded it.
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
     * We do need to create a StreamSubject (rather than a plain Subject) in order to get our
     * desired string representation (unless we edit Subject itself to create and expose a
     * Supplier<List> when given a Stream...). And we have to use a special Factory to avoid
     * re-collecting the stream.
     */
    substituteCheck()
        .withMessage(
            "Warning: Stream equality is based on object identity. To compare Stream"
                + " contents, use methods like containsExactly.")
        .about(streams(listSupplier))
        .that(actual)
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
  public void isNotEqualTo(@Nullable Object other) {
    if (actual() == other) {
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
      failWithoutActual(actualValue("expected not to be"));
      return;
    }
    /*
     * But, if the objects aren't identical, we delegate to the supermethod (which checks equals())
     * just in case someone has decided to override Stream.equals in a strange way. (I haven't
     * checked whether this comes up in Google's codebase. I hope that it doesn't.)
     */
    super.isNotEqualTo(other);
  }

  // TODO(user): Do we want to support comparingElementsUsing() on StreamSubject?

  /** Be careful with using this, as documented on {@link Subject#substituteCheck}. */
  private IterableSubject checkThatContentsList() {
    return substituteCheck().that(listSupplier.get());
  }

  private static Supplier<@Nullable List<?>> listCollector(@Nullable Stream<?> actual) {
    return () -> actual == null ? null : actual.collect(toCollection(ArrayList::new));
  }
}
