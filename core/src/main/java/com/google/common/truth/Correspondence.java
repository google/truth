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

import static com.google.common.base.Functions.identity;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.DoubleSubject.checkTolerance;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Platform.getStackTraceAsString;
import static java.util.Arrays.asList;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Determines whether an instance of type {@code A} corresponds in some way to an instance of type
 * {@code E} for the purposes of a test assertion. For example, the implementation returned by the
 * {@link #tolerance(double)} factory method implements approximate equality between numeric values,
 * with values being said to correspond if the difference between them does not exceed the given
 * fixed tolerance. The instances of type {@code A} are typically actual values from a collection
 * returned by the code under test; the instances of type {@code E} are typically expected values
 * with which the actual values are compared by the test.
 *
 * <p>The correspondence is required to be consistent: for any given values {@code actual} and
 * {@code expected}, multiple invocations of {@code compare(actual, expected)} must consistently
 * return {@code true} or consistently return {@code false} (provided that neither value is
 * modified). Although {@code A} and {@code E} will often be the same types, they are <i>not</i>
 * required to be the same, and even if they are it is <i>not</i> required that the correspondence
 * should have any of the other properties of an equivalence relation (reflexivity, symmetry, or
 * transitivity).
 *
 * <p>Optionally, instances of this class can also provide functionality to format the difference
 * between values which do not correspond. This results in failure messages including formatted
 * diffs between expected and actual value, where possible.
 *
 * <p>The recommended approach for creating an instance of this class is to use one of the static
 * factory methods. The most general of these is {@link #from}; the other methods are more
 * convenient in specific cases. The optional diff-formatting functionality can be added using
 * {@link #formattingDiffsUsing}. (Alternatively, you can subclass this class yourself, but that is
 * generally not recommended.)
 *
 * <p>Instances of this are typically used via {@link IterableSubject#comparingElementsUsing},
 * {@link MapSubject#comparingValuesUsing}, or {@link MultimapSubject#comparingValuesUsing}.
 *
 * @author Pete Gillin
 */
public abstract class Correspondence<A extends @Nullable Object, E extends @Nullable Object> {

  /**
   * Constructs a {@link Correspondence} that compares actual and expected elements using the given
   * binary predicate.
   *
   * <p>The correspondence does not support formatting of diffs (see {@link #formatDiff}). You can
   * add that behaviour by calling {@link Correspondence#formattingDiffsUsing}.
   *
   * <p>Note that, if the data you are asserting about contains nulls, your predicate may be invoked
   * with null arguments. If this causes it to throw a {@link NullPointerException}, then your test
   * will fail. (See {@link Correspondence#compare} for more detail on how exceptions are handled.)
   * In particular, if your predicate is an instance method reference on the actual value (as in the
   * {@code String::contains} example below), your test will fail if it sees null actual values.
   *
   * <p>Example using an instance method reference:
   *
   * <pre>{@code
   * static final Correspondence<String, String> CONTAINS_SUBSTRING =
   *     Correspondence.from(String::contains, "contains");
   * }</pre>
   *
   * <p>Example using a static method reference:
   *
   * <pre>{@code
   * class MyRecordTestHelper {
   *   static final Correspondence<MyRecord, MyRecord> EQUIVALENCE =
   *       Correspondence.from(MyRecordTestHelper::recordsEquivalent, "is equivalent to");
   *   static boolean recordsEquivalent(MyRecord actual, MyRecord expected) {
   *     // code to check whether records should be considered equivalent for testing purposes
   *   }
   * }
   * }</pre>
   *
   * <p>Example using a lambda:
   *
   * <pre>{@code
   * static final Correspondence<Object, Class<?>> INSTANCE_OF =
   *     Correspondence.from((obj, clazz) -> clazz.isInstance(obj), "is an instance of");
   * }</pre>
   *
   * @param predicate a {@link BinaryPredicate} taking an actual and expected value (in that order)
   *     and returning whether the actual value corresponds to the expected value in some way
   * @param description should fill the gap in a failure message of the form {@code "not true that
   *     <some actual element> is an element that <description> <some expected element>"}, e.g.
   *     {@code "contains"}, {@code "is an instance of"}, or {@code "is equivalent to"}
   */
  public static <A extends @Nullable Object, E extends @Nullable Object> Correspondence<A, E> from(
      BinaryPredicate<A, E> predicate, String description) {
    return new FromBinaryPredicate<>(predicate, description);
  }

  /**
   * A functional interface for a binary predicate, to be used to test whether a pair of objects of
   * types {@code A} and {@code E} satisfy some condition.
   *
   * <p>This interface will normally be implemented using a lambda or a method reference, and the
   * resulting object will normally be passed directly to {@link Correspondence#from}. As a result,
   * you should almost never see {@code BinaryPredicate} used as the type of a field or variable, or
   * a return type.
   */
  public interface BinaryPredicate<A extends @Nullable Object, E extends @Nullable Object> {

    /**
     * Returns whether or not the actual and expected values satisfy the condition defined by this
     * predicate.
     */
    boolean apply(A actual, E expected);
  }

  private static final class FromBinaryPredicate<
          A extends @Nullable Object, E extends @Nullable Object>
      extends Correspondence<A, E> {
    private final BinaryPredicate<A, E> predicate;
    private final String description;

    private FromBinaryPredicate(BinaryPredicate<A, E> correspondencePredicate, String description) {
      this.predicate = checkNotNull(correspondencePredicate);
      this.description = checkNotNull(description);
    }

    @Override
    public boolean compare(A actual, E expected) {
      return predicate.apply(actual, expected);
    }

    @Override
    public String toString() {
      return description;
    }
  }

  /**
   * Constructs a {@link Correspondence} that compares elements by transforming the actual elements
   * using the given function and testing for equality with the expected elements. If the
   * transformed actual element (i.e. the output of the given function) is null, it will correspond
   * to a null expected element.
   *
   * <p>The correspondence does not support formatting of diffs (see {@link #formatDiff}). You can
   * add that behaviour by calling {@link Correspondence#formattingDiffsUsing}.
   *
   * <p>Note that, if you the data you are asserting about contains null actual values, your
   * function may be invoked with a null argument. If this causes it to throw a {@link
   * NullPointerException}, then your test will fail. (See {@link Correspondence#compare} for more
   * detail on how exceptions are handled.) In particular, this applies if your function is an
   * instance method reference on the actual value (as in the example below). If you want a null
   * actual element to correspond to a null expected element, you must ensure that your function
   * transforms a null input to a null output.
   *
   * <p>Example:
   *
   * <pre>{@code
   * static final Correspondence<MyRecord, Integer> HAS_ID =
   *     Correspondence.transforming(MyRecord::getId, "has an ID of");
   * }</pre>
   *
   * This can be used as follows:
   *
   * <pre>{@code
   * assertThat(myRecords).comparingElementsUsing(HAS_ID).containsExactly(123, 456, 789);
   * }</pre>
   *
   * @param actualTransform a {@link Function} taking an actual value and returning a new value
   *     which will be compared with an expected value to determine whether they correspond
   * @param description should fill the gap in a failure message of the form {@code "not true that
   *     <some actual element> is an element that <description> <some expected element>"}, e.g.
   *     {@code "has an ID of"}
   */
  public static <A extends @Nullable Object, E extends @Nullable Object>
      Correspondence<A, E> transforming(
          Function<A, ? extends E> actualTransform, String description) {
    return new Transforming<>(actualTransform, identity(), description);
  }

  /**
   * Constructs a {@link Correspondence} that compares elements by transforming the actual and the
   * expected elements using the given functions and testing the transformed values for equality. If
   * an actual element is transformed to null, it will correspond to an expected element that is
   * also transformed to null.
   *
   * <p>The correspondence does not support formatting of diffs (see {@link #formatDiff}). You can
   * add that behaviour by calling {@link Correspondence#formattingDiffsUsing}.
   *
   * <p>Note that, if you the data you are asserting about contains null actual or expected values,
   * the appropriate function may be invoked with a null argument. If this causes it to throw a
   * {@link NullPointerException}, then your test will fail. (See {@link Correspondence#compare} for
   * more detail on how exceptions are handled.) In particular, this applies if your function is an
   * instance method reference on the actual or expected value (as in the example below). If you
   * want a null actual element to correspond to a null expected element, you must ensure that your
   * functions both transform a null input to a null output.
   *
   * <p>If you want to apply the same function to both the actual and expected elements, just
   * provide the same argument twice.
   *
   * <p>Example:
   *
   * <pre>{@code
   * static final Correspondence<MyRequest, MyResponse> SAME_IDS =
   *     Correspondence.transforming(MyRequest::getId, MyResponse::getId, "has the same ID as");
   * }</pre>
   *
   * This can be used as follows:
   *
   * <pre>{@code
   * assertThat(myResponses).comparingElementsUsing(SAME_IDS).containsExactlyElementsIn(myRequests);
   * }</pre>
   *
   * @param actualTransform a {@link Function} taking an actual value and returning a new value
   *     which will be compared with a transformed expected value to determine whether they
   *     correspond
   * @param expectedTransform a {@link Function} taking an expected value and returning a new value
   *     which will be compared with a transformed actual value
   * @param description should fill the gap in a failure message of the form {@code "not true that
   *     <some actual element> is an element that <description> <some expected element>"}, e.g.
   *     {@code "has the same ID as"}
   */
  public static <A extends @Nullable Object, E extends @Nullable Object>
      Correspondence<A, E> transforming(
          Function<A, ?> actualTransform, Function<E, ?> expectedTransform, String description) {
    return new Transforming<>(actualTransform, expectedTransform, description);
  }

  private static final class Transforming<A extends @Nullable Object, E extends @Nullable Object>
      extends Correspondence<A, E> {

    private final Function<? super A, ?> actualTransform;
    private final Function<? super E, ?> expectedTransform;
    private final String description;

    private Transforming(
        Function<? super A, ?> actualTransform,
        Function<? super E, ?> expectedTransform,
        String description) {
      this.actualTransform = actualTransform;
      this.expectedTransform = expectedTransform;
      this.description = description;
    }

    @Override
    public boolean compare(A actual, E expected) {
      return Objects.equal(actualTransform.apply(actual), expectedTransform.apply(expected));
    }

    @Override
    public String toString() {
      return description;
    }
  }

  /**
   * Returns a {@link Correspondence} between {@link Number} instances that considers instances to
   * correspond (i.e. {@link Correspondence#compare(Object, Object)} returns {@code true}) if the
   * double values of each instance (i.e. the result of calling {@link Number#doubleValue()} on
   * them) are finite values within {@code tolerance} of each other.
   *
   * <ul>
   *   <li>It does not consider instances to correspond if either value is infinite or NaN.
   *   <li>The conversion to double may result in a loss of precision for some numeric types.
   *   <li>The {@link Correspondence#compare(Object, Object)} method throws a {@link
   *       NullPointerException} if either {@link Number} instance is null.
   * </ul>
   *
   * @param tolerance an inclusive upper bound on the difference between the double values of the
   *     two {@link Number} instances, which must be a non-negative finite value, i.e. not {@link
   *     Double#NaN}, {@link Double#POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  public static Correspondence<Number, Number> tolerance(double tolerance) {
    return new TolerantNumericEquality(tolerance);
  }

  private static final class TolerantNumericEquality extends Correspondence<Number, Number> {

    private final double tolerance;

    private TolerantNumericEquality(double tolerance) {
      checkTolerance(tolerance);
      this.tolerance = tolerance;
    }

    @Override
    public boolean compare(Number actual, Number expected) {
      double actualDouble = checkNotNull(actual).doubleValue();
      double expectedDouble = checkNotNull(expected).doubleValue();
      return MathUtil.equalWithinTolerance(actualDouble, expectedDouble, tolerance);
    }

    @Override
    public String toString() {
      return "is a finite number within " + tolerance + " of";
    }
  }

  /**
   * Returns a correspondence which compares elements using object equality, i.e. giving the same
   * assertions as you would get without a correspondence. This exists so that we can add a
   * diff-formatting functionality to it. See e.g. {@link IterableSubject#formattingDiffsUsing}.
   */
  @SuppressWarnings("unchecked") // safe covariant cast
  static <T> Correspondence<T, T> equality() {
    return (Equality<T>) Equality.INSTANCE;
  }

  private static final class Equality<T> extends Correspondence<T, T> {

    private static final Equality<Object> INSTANCE = new Equality<>();

    @Override
    public boolean compare(T actual, T expected) {
      return Objects.equal(actual, expected);
    }

    @Override
    public String toString() {
      // This should normally not be used, since isEquality() returns true, but it should do
      // something sensible anyway:
      return "is equal to";
    }

    @Override
    boolean isEquality() {
      return true;
    }
  }

  /**
   * Constructor. Creating subclasses (anonymous or otherwise) of this class is <i>not
   * recommended</i>, but is possible via this constructor. The recommended approach is to use the
   * factory methods instead (see {@linkplain Correspondence class-level documentation}).
   *
   * @deprecated Construct an instance with the static factory methods instead. The most mechanical
   *     migration is usually to {@link #from}.
   */
  @Deprecated
  Correspondence() {}

  /**
   * Returns a new correspondence which is like this one, except that the given formatter may be
   * used to format the difference between a pair of elements that do not correspond.
   *
   * <p>Note that, if you the data you are asserting about contains null actual or expected values,
   * the formatter may be invoked with a null argument. If this causes it to throw a {@link
   * NullPointerException}, that will be taken to indicate that the values cannot be diffed. (See
   * {@link Correspondence#formatDiff} for more detail on how exceptions are handled.) If you think
   * null values are likely, it is slightly cleaner to have the formatter return null in that case
   * instead of throwing.
   *
   * <p>Example:
   *
   * <pre>{@code
   * class MyRecordTestHelper {
   *   static final Correspondence<MyRecord, MyRecord> EQUIVALENCE =
   *       Correspondence.from(MyRecordTestHelper::recordsEquivalent, "is equivalent to")
   *           .formattingDiffsUsing(MyRecordTestHelper::formatRecordDiff);
   *   static boolean recordsEquivalent(MyRecord actual, MyRecord expected) {
   *     // code to check whether records should be considered equivalent for testing purposes
   *   }
   *   static String formatRecordDiff(MyRecord actual, MyRecord expected) {
   *     // code to format the diff between the records
   *   }
   * }
   * }</pre>
   */
  public Correspondence<A, E> formattingDiffsUsing(DiffFormatter<? super A, ? super E> formatter) {
    return new FormattingDiffs<>(this, formatter);
  }

  /**
   * A functional interface to be used format the diff between a pair of objects of types {@code A}
   * and {@code E}.
   *
   * <p>This interface will normally be implemented using a lambda or a method reference, and the
   * resulting object will normally be passed directly to {@link
   * Correspondence#formattingDiffsUsing}. As a result, you should almost never see {@code
   * DiffFormatter} used as the type of a field or variable, or a return type.
   */
  public interface DiffFormatter<A extends @Nullable Object, E extends @Nullable Object> {

    /**
     * Returns a {@link String} describing the difference between the {@code actual} and {@code
     * expected} values, if possible, or {@code null} if not.
     */
    @Nullable
    String formatDiff(A actual, E expected);
  }

  private static class FormattingDiffs<A extends @Nullable Object, E extends @Nullable Object>
      extends Correspondence<A, E> {

    private final Correspondence<A, E> delegate;
    private final DiffFormatter<? super A, ? super E> formatter;

    FormattingDiffs(Correspondence<A, E> delegate, DiffFormatter<? super A, ? super E> formatter) {
      this.delegate = checkNotNull(delegate);
      this.formatter = checkNotNull(formatter);
    }

    @Override
    public boolean compare(A actual, E expected) {
      return delegate.compare(actual, expected);
    }

    @Override
    public @Nullable String formatDiff(A actual, E expected) {
      return formatter.formatDiff(actual, expected);
    }

    @Override
    public String toString() {
      return delegate.toString();
    }

    @Override
    boolean isEquality() {
      return delegate.isEquality();
    }
  }

  /**
   * Returns whether or not the {@code actual} value is said to correspond to the {@code expected}
   * value for the purposes of this test.
   *
   * <h3>Exception handling</h3>
   *
   * <p>Throwing a {@link RuntimeException} from this method indicates that this {@link
   * Correspondence} cannot compare the given values. Any assertion which encounters such an
   * exception during the course of evaluating its condition must not pass. However, an assertion is
   * not required to invoke this method for every pair of values in its input just in order to check
   * for exceptions, if it is able to evaluate its condition without doing so.
   *
   * <h4>Conventions for handling exceptions</h4>
   *
   * <p>(N.B. This section is only really of interest when implementing assertion methods that call
   * {@link Correspondence#compare}, not to users making such assertions in their tests.)
   *
   * <p>The only requirement on an assertion is that, if it encounters an exception from this
   * method, it must not pass. The simplest implementation choice is simply to allow the exception
   * to propagate. However, it is normally more helpful to catch the exception and instead fail with
   * a message which includes more information about the assertion in progress and the nature of the
   * failure.
   *
   * <p>By convention, an assertion may catch and store the exception and continue evaluating the
   * condition as if the method had returned false instead of throwing. If the assertion's condition
   * does not hold with this alternative behaviour, it may choose to fail with a message that gives
   * details about how the condition does not hold, additionally mentioning that assertions were
   * encountered and giving details about one of the stored exceptions. (See the first example
   * below.) If the assertion's condition does hold with this alternative behaviour, the requirement
   * that the assertion must not pass still applies, so it should fail with a message giving details
   * about one of the stored exceptions. (See the second and third examples below.)
   *
   * <p>This behaviour is only a convention and should only be implemented when it makes sense to do
   * so. In particular, in an assertion that has multiple stages, it may be better to only continue
   * evaluation to the end of the current stage, and fail citing a stored exception at the end of
   * the stage, rather than accumulating exceptions through the multiple stages.
   *
   * <h4>Examples of exception handling</h4>
   *
   * <p>Suppose that we have the correspondence
   *
   * <pre>{@code
   * static final Correspondence<String, String> CASE_INSENSITIVE_EQUALITY =
   *     Correspondence.from(String::equalsIgnoreCase, "equals ignoring case"}
   * }</pre>
   *
   * whose {@code compare} method throws {@link NullPointerException} if the actual value is null.
   * The assertion
   *
   * <pre>{@code
   * assertThat(asList(null, "xyz", "abc", "def"))
   *     .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
   *     .containsExactly("ABC", "DEF", "GHI", "JKL");
   * }</pre>
   *
   * may fail saying that the actual iterable contains unexpected values {@code null} and {@code
   * xyz} and is missing values corresponding to {@code GHI} and {@code JKL}, which is what it would
   * do if the {@code compare} method returned false instead of throwing, and additionally mention
   * the exception. (This is more helpful than allowing the {@link NullPointerException} to
   * propagate to the caller, or than failing with only a description of the exception.)
   *
   * <p>However, the assertions
   *
   * <pre>{@code
   * assertThat(asList(null, "xyz", "abc", "def"))
   *     .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
   *     .doesNotContain("MNO");
   * }</pre>
   *
   * and
   *
   * <pre>{@code
   * assertThat(asList(null, "xyz", "abc", "def"))
   *     .comparingElementsUsing(CASE_INSENSITIVE_EQUALITY)
   *     .doesNotContain(null);
   * }</pre>
   *
   * must both fail citing the exception, even though they would pass if the {@code compare} method
   * returned false. (Note that, in the latter case at least, it is likely that the test author's
   * intention was <i>not</i> for the test to pass with these values.)
   */
  public abstract boolean compare(A actual, E expected);

  private static class StoredException {

    private static final Joiner ARGUMENT_JOINER = Joiner.on(", ").useForNull("null");

    private final Exception exception;
    private final String methodName;
    private final List<@Nullable Object> methodArguments;

    StoredException(
        Exception exception, String methodName, List<@Nullable Object> methodArguments) {
      this.exception = checkNotNull(exception);
      this.methodName = checkNotNull(methodName);
      this.methodArguments = checkNotNull(methodArguments);
    }

    /**
     * Returns a String describing the exception stored. This includes a stack trace (except under
     * j2cl, where this is not available). It also has a separator at the end, so that when this
     * appears at the end of an {@code AssertionError} message, the stack trace of the stored
     * exception is distinguishable from the stack trace of the {@code AssertionError}.
     */
    private String describe() {
      return Strings.lenientFormat(
          "%s(%s) threw %s\n---",
          methodName, ARGUMENT_JOINER.join(methodArguments), getStackTraceAsString(exception));
    }
  }

  /**
   * Helper object to store exceptions encountered while executing a {@link Correspondence} method.
   */
  static final class ExceptionStore {

    private final String argumentLabel;
    private @Nullable StoredException firstCompareException = null;
    private @Nullable StoredException firstPairingException = null;
    private @Nullable StoredException firstFormatDiffException = null;

    static ExceptionStore forIterable() {
      return new ExceptionStore("elements");
    }

    static ExceptionStore forMapValues() {
      return new ExceptionStore("values");
    }

    private ExceptionStore(String argumentLabel) {
      this.argumentLabel = argumentLabel;
    }

    /**
     * Adds an exception that was thrown during a {@code compare} call.
     *
     * @param callingClass The class from which the {@code compare} method was called. When
     *     reporting failures, stack traces will be truncated above elements in this class.
     * @param exception The exception encountered
     * @param actual The {@code actual} argument to the {@code compare} call during which the
     *     exception was encountered
     * @param expected The {@code expected} argument to the {@code compare} call during which the
     *     exception was encountered
     */
    void addCompareException(
        Class<?> callingClass,
        Exception exception,
        @Nullable Object actual,
        @Nullable Object expected) {
      if (firstCompareException == null) {
        truncateStackTrace(exception, callingClass);
        firstCompareException = new StoredException(exception, "compare", asList(actual, expected));
      }
    }

    /**
     * Adds an exception that was thrown during an {@code apply} call on the function used to key
     * actual elements.
     *
     * @param callingClass The class from which the {@code apply} method was called. When reporting
     *     failures, stack traces will be truncated above elements in this class.
     * @param exception The exception encountered
     * @param actual The {@code actual} argument to the {@code apply} call during which the
     *     exception was encountered
     */
    void addActualKeyFunctionException(
        Class<?> callingClass, Exception exception, @Nullable Object actual) {
      if (firstPairingException == null) {
        truncateStackTrace(exception, callingClass);
        firstPairingException =
            new StoredException(exception, "actualKeyFunction.apply", asList(actual));
      }
    }

    /**
     * Adds an exception that was thrown during an {@code apply} call on the function used to key
     * expected elements.
     *
     * @param callingClass The class from which the {@code apply} method was called. When reporting
     *     failures, stack traces will be truncated above elements in this class.
     * @param exception The exception encountered
     * @param expected The {@code expected} argument to the {@code apply} call during which the
     *     exception was encountered
     */
    void addExpectedKeyFunctionException(
        Class<?> callingClass, Exception exception, @Nullable Object expected) {
      if (firstPairingException == null) {
        truncateStackTrace(exception, callingClass);
        firstPairingException =
            new StoredException(exception, "expectedKeyFunction.apply", asList(expected));
      }
    }

    /**
     * Adds an exception that was thrown during a {@code formatDiff} call.
     *
     * @param callingClass The class from which the {@code formatDiff} method was called. When
     *     reporting failures, stack traces will be truncated above elements in this class.
     * @param exception The exception encountered
     * @param actual The {@code actual} argument to the {@code formatDiff} call during which the
     *     exception was encountered
     * @param expected The {@code expected} argument to the {@code formatDiff} call during which the
     *     exception was encountered
     */
    void addFormatDiffException(
        Class<?> callingClass,
        Exception exception,
        @Nullable Object actual,
        @Nullable Object expected) {
      if (firstFormatDiffException == null) {
        truncateStackTrace(exception, callingClass);
        firstFormatDiffException =
            new StoredException(exception, "formatDiff", asList(actual, expected));
      }
    }

    /** Returns whether any exceptions thrown during {@code compare} calls were stored. */
    boolean hasCompareException() {
      return firstCompareException != null;
    }

    /**
     * Returns facts to use in a failure message when the exceptions from {@code compare} calls are
     * the main cause of the failure. At least one exception thrown during a {@code compare} call
     * must have been stored, and no exceptions from a {@code formatDiff} call. Assertions should
     * use this when exceptions were thrown while comparing elements and no more meaningful failure
     * was discovered by assuming a false return and continuing (see the javadoc for {@link
     * Correspondence#compare}). C.f. {@link #describeAsAdditionalInfo}.
     */
    ImmutableList<Fact> describeAsMainCause() {
      checkState(firstCompareException != null);
      // We won't do pairing or diff formatting unless a more meaningful failure was found, and if a
      // more meaningful failure was found then we shouldn't be using this method:
      checkState(firstPairingException == null);
      checkState(firstFormatDiffException == null);
      return ImmutableList.of(
          simpleFact("one or more exceptions were thrown while comparing " + argumentLabel),
          fact("first exception", firstCompareException.describe()));
    }

    /**
     * If any exceptions are stored, returns facts to use in a failure message when the exceptions
     * should be noted as additional info; if empty, returns an empty list. Assertions should use
     * this when exceptions were thrown while comparing elements but more meaningful failures were
     * discovered by assuming a false return and continuing (see the javadoc for {@link
     * Correspondence#compare}), or when exceptions were thrown by other methods while generating
     * the failure message. C.f. {@link #describeAsMainCause}.
     */
    ImmutableList<Fact> describeAsAdditionalInfo() {
      ImmutableList.Builder<Fact> builder = ImmutableList.builder();
      if (firstCompareException != null) {
        builder.add(
            simpleFact(
                "additionally, one or more exceptions were thrown while comparing "
                    + argumentLabel));
        builder.add(fact("first exception", firstCompareException.describe()));
      }
      if (firstPairingException != null) {
        builder.add(
            simpleFact(
                "additionally, one or more exceptions were thrown while keying "
                    + argumentLabel
                    + " for pairing"));
        builder.add(fact("first exception", firstPairingException.describe()));
      }
      if (firstFormatDiffException != null) {
        builder.add(
            simpleFact("additionally, one or more exceptions were thrown while formatting diffs"));
        builder.add(fact("first exception", firstFormatDiffException.describe()));
      }
      return builder.build();
    }

    private static void truncateStackTrace(Exception exception, Class<?> callingClass) {
      StackTraceElement[] original = exception.getStackTrace();
      int keep = 0;
      while (keep < original.length
          && !original[keep].getClassName().equals(callingClass.getName())) {
        keep++;
      }
      exception.setStackTrace(Arrays.copyOf(original, keep));
    }
  }

  /**
   * Invokes {@link #compare}, catching any exceptions. If the comparison does not throw, returns
   * the result. If it does throw, adds the exception to the given {@link ExceptionStore} and
   * returns false. This method can help with implementing the exception-handling policy described
   * above, but note that assertions using it <i>must</i> fail later if an exception was stored.
   */
  final boolean safeCompare(A actual, E expected, ExceptionStore exceptions) {
    try {
      return compare(actual, expected);
    } catch (RuntimeException e) {
      exceptions.addCompareException(Correspondence.class, e, actual, expected);
      return false;
    }
  }

  /**
   * Returns a {@link String} describing the difference between the {@code actual} and {@code
   * expected} values, if possible, or {@code null} if not.
   *
   * <p>The implementation on the {@link Correspondence} base class always returns {@code null}. To
   * enable diffing, use {@link #formattingDiffsUsing} (or override this method in a subclass, but
   * factory methods are recommended over subclassing).
   *
   * <p>Assertions should only invoke this with parameters for which {@link #compare} returns {@code
   * false}.
   *
   * <p>If this throws an exception, that implies that it is not possible to describe the diffs. An
   * assertion will normally only call this method if it has established that its condition does not
   * hold: good practice dictates that, if this method throws, the assertion should catch the
   * exception and continue to describe the original failure as if this method had returned null,
   * mentioning the failure from this method as additional information.
   */
  public @Nullable String formatDiff(A actual, E expected) {
    return null;
  }

  /**
   * Invokes {@link #formatDiff}, catching any exceptions. If the comparison does not throw, returns
   * the result. If it does throw, adds the exception to the given {@link ExceptionStore} and
   * returns null.
   */
  final @Nullable String safeFormatDiff(A actual, E expected, ExceptionStore exceptions) {
    try {
      return formatDiff(actual, expected);
    } catch (RuntimeException e) {
      exceptions.addFormatDiffException(Correspondence.class, e, actual, expected);
      return null;
    }
  }

  /**
   * Returns a description of the correspondence, suitable to fill the gap in a failure message of
   * the form {@code "<some actual element> is an element that ... <some expected element>"}. Note
   * that this is a fragment of a verb phrase which takes a singular subject.
   *
   * <p>Example 1: For a {@code Correspondence<String, Integer>} that tests whether the actual
   * string parses to the expected integer, this would return {@code "parses to"} to result in a
   * failure message of the form {@code "<some actual string> is an element that parses to <some
   * expected integer>"}.
   *
   * <p>Example 2: For the {@code Correspondence<Number, Number>} returns by {@link #tolerance} this
   * returns {@code "is a finite number within " + tolerance + " of"} to result in a failure message
   * of the form {@code "<some actual number> is an element that is a finite number within 0.0001 of
   * <some expected number>"}.
   */
  @Override
  public abstract String toString();

  /**
   * Returns whether this is an equality correspondence, i.e. one returned by {@link #equality} or
   * one whose {@link #compare} delegates to one returned by {@link #equality}.
   */
  boolean isEquality() {
    return false;
  }

  /**
   * Returns a list of {@link Fact} instance describing how this correspondence compares elements of
   * an iterable. There will be one "testing whether" fact, unless this {@link #isEquality is an
   * equality correspondence}, in which case the list will be empty.
   */
  final ImmutableList<Fact> describeForIterable() {
    if (!isEquality()) {
      return ImmutableList.of(
          fact("testing whether", "actual element " + this + " expected element"));
    } else {
      return ImmutableList.of();
    }
  }

  /**
   * Returns a list of {@link Fact} instance describing how this correspondence compares values in a
   * map (or multimap). There will be one "testing whether" fact, unless this {@link #isEquality is
   * an equality correspondence}, in which case the list will be empty.
   */
  final ImmutableList<Fact> describeForMapValues() {
    if (!isEquality()) {
      return ImmutableList.of(fact("testing whether", "actual value " + this + " expected value"));
    } else {
      return ImmutableList.of();
    }
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#equals(Object)} is not supported. If you meant to compare objects
   *     using this {@link Correspondence}, use {@link #compare}.
   */
  @Deprecated
  @Override
  public final boolean equals(@Nullable Object o) {
    throw new UnsupportedOperationException(
        "Correspondence.equals(object) is not supported. If you meant to compare objects, use"
            + " .compare(actual, expected) instead.");
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#hashCode()} is not supported.
   */
  @Deprecated
  @Override
  public final int hashCode() {
    throw new UnsupportedOperationException("Correspondence.hashCode() is not supported.");
  }
}
