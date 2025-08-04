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
import static com.google.common.truth.Fact.simpleFact;
import static java.util.stream.Collectors.toCollection;

import com.google.common.base.Supplier;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link IntStream} values.
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
 * List<Integer> list = makeStream().map(...).filter(...).boxed().collect(toImmutableList());
 * assertThat(list).contains(5);
 * assertThat(list).doesNotContain(2);
 * }</pre>
 *
 * <p>For very large or infinite streams, you may want to first {@linkplain Stream#limit limit} the
 * stream before asserting on it.
 *
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@IgnoreJRERequirement
public final class IntStreamSubject extends Subject {
  private final Supplier<@Nullable List<?>> listSupplier;

  private IntStreamSubject(FailureMetadata metadata, @Nullable IntStream actual) {
    super(metadata, actual);
    // For discussion of when we collect(), see the Javadoc and also StreamSubject.
    this.listSupplier = memoize(listCollector(actual));
  }

  @Override
  protected String actualCustomStringRepresentation() {
    List<?> asList;
    try {
      asList = listSupplier.get();
    } catch (IllegalStateException e) {
      return "Stream that has already been operated upon or closed: "
          + actualForPackageMembersToCall();
    }
    return String.valueOf(asList);
  }

  /**
   * Obsolete factory instance. This factory was previously necessary for assertions like {@code
   * assertWithMessage(...).about(intStreams()).that(stream)....}. Now, you can perform assertions
   * like that without the {@code about(...)} call.
   *
   * @deprecated Instead of {@code about(intStreams()).that(...)}, use just {@code that(...)}.
   *     Similarly, instead of {@code assertAbout(intStreams()).that(...)}, use just {@code
   *     assertThat(...)}.
   */
  @Deprecated
  @SuppressWarnings("InlineMeSuggester") // We want users to remove the surrounding call entirely.
  public static Factory<IntStreamSubject, IntStream> intStreams() {
    return IntStreamSubject::new;
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
  public void contains(int element) {
    checkThatContentsList().contains(element);
  }

  /** Checks that the actual stream does not contain the given element. */
  public void doesNotContain(int element) {
    checkThatContentsList().doesNotContain(element);
  }

  /** Checks that the actual stream does not contain duplicate elements. */
  public void containsNoDuplicates() {
    checkThatContentsList().containsNoDuplicates();
  }

  /** Checks that the actual stream contains at least one of the given elements. */
  public void containsAnyOf(int first, int second, int... rest) {
    checkThatContentsList().containsAnyOf(first, second, box(rest));
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
  public Ordered containsAtLeast(int first, int second, int... rest) {
    return checkThatContentsList().containsAtLeast(first, second, box(rest));
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
  public Ordered containsExactly(int @Nullable ... expected) {
    if (expected == null) {
      failWithoutActual(
          simpleFact("could not perform containment check because expected array was null"),
          actualContents());
      return ALREADY_FAILED;
    }
    return checkThatContentsList().containsExactlyElementsIn(box(expected));
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
  public void containsNoneOf(int first, int second, int... rest) {
    checkThatContentsList().containsNoneOf(first, second, box(rest));
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
  public void isInStrictOrder(Comparator<? super Integer> comparator) {
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
  public void isInOrder(Comparator<? super Integer> comparator) {
    checkThatContentsList().isInOrder(comparator);
  }

  /** Be careful with using this, as documented on {@link Subject#substituteCheck}. */
  private IterableSubject checkThatContentsList() {
    return substituteCheck().that(listSupplier.get());
  }

  private static Supplier<@Nullable List<?>> listCollector(@Nullable IntStream actual) {
    return () -> actual == null ? null : actual.boxed().collect(toCollection(ArrayList::new));
  }

  private static Object[] box(int[] rest) {
    return IntStream.of(rest).boxed().toArray(Integer[]::new);
  }

  private Fact actualContents() {
    return actualValue("actual contents");
  }

  /** Ordered implementation that does nothing because an earlier check already caused a failure. */
  private static final Ordered ALREADY_FAILED = () -> {};

  // TODO: b/246961366 - Do we want to override + deprecate isEqualTo/isNotEqualTo?

  // TODO(user): Do we want to support comparingElementsUsing() on StreamSubject?
}
