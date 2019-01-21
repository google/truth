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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.DoubleSubject.checkTolerance;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Facts.facts;
import static com.google.common.truth.Platform.getStackTraceAsString;
import static java.util.Arrays.asList;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Determines whether an instance of type {@code A} corresponds in some way to an instance of type
 * {@code E} for the purposes of a test assertion. For example, the implementation returned by the
 * {@link #tolerance(double)} factory method implements approximate equality between numeric values,
 * with values being said to correspond if the difference between them is does not exceed some fixed
 * tolerance. The instances of type {@code A} are typically actual values from a collection returned
 * by the code under test; the instances of type {@code E} are typically expected values with which
 * the actual values are compared by the test.
 *
 * <p>The correspondence is required to be consistent: for any given values {@code actual} and
 * {@code expected}, multiple invocations of {@code compare(actual, expected)} must consistently
 * return {@code true} or consistently return {@code false} (provided that neither value is
 * modified). Although {@code A} and {@code E} will often be the same types, they are <i>not</i>
 * required to be the same, and even if they are it is <i>not</i> required that the correspondence
 * should have any of the other properties of an equivalence relation (reflexivity, symmetry, or
 * transitivity).
 *
 * <p>Subclasses may optionally override {@link #formatDiff}. This results in failure messages
 * including formatted diffs between expected and actual elements, where possible.
 *
 * <p>Instances of this are typically used via {@link IterableSubject#comparingElementsUsing},
 * {@link MapSubject#comparingValuesUsing}, or {@link MultimapSubject#comparingValuesUsing}.
 *
 * @author Pete Gillin
 */
public abstract class Correspondence<A, E> {

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
   * <p>Suppose that {@code CASE_INSENSITIVE_EQUALITY} is a {@code Correspondence<String, String>}
   * whose {@code compare} method calls {@link actual.equalsIgnoreCase(expected)} and therefore
   * throws {@link NullPointerException} if the actual value is null. The assertion
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
  // TODO(b/119038894): Simplify the 'for example' by using a factory method when it's ready
  public abstract boolean compare(@NullableDecl A actual, @NullableDecl E expected);

  private static class StoredException {

    private static final Joiner ARGUMENT_JOINER = Joiner.on(", ").useForNull("null");

    private final Exception exception;
    private final String methodName;
    private final List<Object> methodArguments;

    StoredException(Exception exception, String methodName, List<Object> methodArguments) {
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
    private StoredException firstCompareException = null;
    private StoredException firstPairingException = null;
    private StoredException firstFormatDiffException = null;

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
        Class<?> callingClass, Exception exception, Object actual, Object expected) {
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
    void addActualKeyFunctionException(Class<?> callingClass, Exception exception, Object actual) {
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
        Class<?> callingClass, Exception exception, Object expected) {
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
        Class<?> callingClass, Exception exception, Object actual, Object expected) {
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
    Facts describeAsMainCause() {
      checkState(firstCompareException != null);
      // We won't do pairing or diff formatting unless a more meaningful failure was found, and if a
      // more meaningful failure was found then we shouldn't be using this method:
      checkState(firstPairingException == null);
      checkState(firstFormatDiffException == null);
      return facts(
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
    Facts describeAsAdditionalInfo() {
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
      return facts(builder.build());
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
  final boolean safeCompare(
      @NullableDecl A actual, @NullableDecl E expected, ExceptionStore exceptions) {
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
   * enable diffing, subclasses should override this method.
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
  @NullableDecl
  public String formatDiff(@NullableDecl A actual, @NullableDecl E expected) {
    return null;
  }

  /**
   * Invokes {@link #formatDiff}, catching any exceptions. If the comparison does not throw, returns
   * the result. If it does throw, adds the exception to the given {@link ExceptionStore} and
   * returns null.
   */
  @NullableDecl
  final String safeFormatDiff(
      @NullableDecl A actual, @NullableDecl E expected, ExceptionStore exceptions) {
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
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#equals(Object)} is not supported. If you meant to compare objects
   *     using this {@link Correspondence}, use {@link #compare}.
   */
  @Deprecated
  @Override
  public final boolean equals(@NullableDecl Object o) {
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
