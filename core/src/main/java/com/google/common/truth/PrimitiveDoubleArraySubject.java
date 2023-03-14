/*
 * Copyright (c) 2014 Google, Inc.
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
import static com.google.common.truth.Correspondence.tolerance;

import com.google.common.primitives.Doubles;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A Subject for {@code double[]}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public final class PrimitiveDoubleArraySubject extends AbstractArraySubject {
  private final double @Nullable [] actual;

  PrimitiveDoubleArraySubject(
      FailureMetadata metadata, double @Nullable [] o, @Nullable String typeDescription) {
    super(metadata, o, typeDescription);
    this.actual = o;
  }

  /**
   * A check that the actual array and {@code expected} are arrays of the same length and type,
   * containing elements such that each element in {@code expected} is equal to each element in the
   * actual array, and in the same position, with element equality defined the same way that {@link
   * Arrays#equals(double[], double[])} and {@link Double#equals(Object)} define it (which is
   * different to the way that the {@code ==} operator on primitive {@code double} defines it). This
   * method is <i>not</i> recommended when the code under test is doing any kind of arithmetic: use
   * {@link #usingTolerance} with a suitable tolerance in that case, e.g. {@code
   * assertThat(actualArray).usingTolerance(1.0e-10).containsExactly(expectedArray).inOrder()}.
   * (Remember that the exact result of floating point arithmetic is sensitive to apparently trivial
   * changes such as replacing {@code (a + b) + c} with {@code a + (b + c)}, and that unless {@code
   * strictfp} is in force even the result of {@code (a + b) + c} is sensitive to the JVM's choice
   * of precision for the intermediate result.) This method is recommended when the code under test
   * is specified as either copying values without modification from its input or returning
   * well-defined literal or constant values.
   *
   * <ul>
   *   <li>It considers {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, and
   *       {@link Double#NaN} to be equal to themselves (contrast with {@code usingTolerance(0.0)}
   *       which does not).
   *   <li>It does <i>not</i> consider {@code -0.0} to be equal to {@code 0.0} (contrast with {@code
   *       usingTolerance(0.0)} which does).
   * </ul>
   */
  // TODO(cpovirk): Move some or all of this Javadoc to the supertype, maybe deleting this override?
  @Override
  public void isEqualTo(@Nullable Object expected) {
    super.isEqualTo(expected);
  }

  /**
   * A check that the actual array and {@code expected} are not arrays of the same length and type,
   * containing elements such that each element in {@code expected} is equal to each element in the
   * actual array, and in the same position, with element equality defined the same way that {@link
   * Arrays#equals(double[], double[])} and {@link Double#equals(Object)} define it (which is
   * different to the way that the {@code ==} operator on primitive {@code double} defines it). See
   * {@link #isEqualTo(Object)} for advice on when exact equality is recommended.
   *
   * <ul>
   *   <li>It considers {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, and
   *       {@link Double#NaN} to be equal to themselves.
   *   <li>It does <i>not</i> consider {@code -0.0} to be equal to {@code 0.0}.
   * </ul>
   */
  @Override
  public void isNotEqualTo(@Nullable Object expected) {
    super.isNotEqualTo(expected);
  }

  /**
   * Starts a method chain for a check in which the actual values (i.e. the elements of the array
   * under test) are compared to expected elements using a {@link Correspondence} which considers
   * values to correspond if they are finite values within {@code tolerance} of each other. The
   * check is actually executed by continuing the method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualDoubleArray).usingTolerance(1.0e-5).contains(3.14159);
   * }</pre>
   *
   * <ul>
   *   <li>It does not consider values to correspond if either value is infinite or NaN.
   *   <li>It considers {@code -0.0} to be within any tolerance of {@code 0.0}.
   *   <li>The expected values provided later in the chain will be {@link Number} instances which
   *       will be converted to doubles, which may result in a loss of precision for some numeric
   *       types.
   *   <li>The subsequent methods in the chain may throw a {@link NullPointerException} if any
   *       expected {@link Number} instance is null.
   * </ul>
   *
   * @param tolerance an inclusive upper bound on the difference between the double values of the
   *     actual and expected numbers, which must be a non-negative finite value, i.e. not {@link
   *     Double#NaN}, {@link Double#POSITIVE_INFINITY}, or negative, including {@code -0.0}
   */
  public DoubleArrayAsIterable usingTolerance(double tolerance) {
    return new DoubleArrayAsIterable(tolerance(tolerance), iterableSubject());
  }

  private static final Correspondence<Double, Number> EXACT_EQUALITY_CORRESPONDENCE =
      Correspondence.from(
          // If we were allowed lambdas, this would be:
          // (a, e) -> Double.doubleToLongBits(a) == Double.doubleToLongBits(checkedToDouble(e)),
          new Correspondence.BinaryPredicate<Double, Number>() {
            @Override
            public boolean apply(Double actual, Number expected) {
              return Double.doubleToLongBits(actual)
                  == Double.doubleToLongBits(checkedToDouble(expected));
            }
          },
          "is exactly equal to");

  private static double checkedToDouble(Number expected) {
    checkNotNull(expected);
    checkArgument(
        expected instanceof Double
            || expected instanceof Float
            || expected instanceof Integer
            || expected instanceof Long,
        "Expected value in assertion using exact double equality was of unsupported type %s "
            + "(it may not have an exact double representation)",
        expected.getClass());
    if (expected instanceof Long) {
      checkArgument(
          Math.abs((Long) expected) <= 1L << 53,
          "Expected value %s in assertion using exact double equality was a long with an absolute "
              + "value greater than 2^52 which has no exact double representation",
          expected);
    }
    return expected.doubleValue();
  }

  /**
   * Starts a method chain for a check in which the actual values (i.e. the elements of the array
   * under test) are compared to expected elements using a {@link Correspondence} which considers
   * values to correspond if they are exactly equal, with equality defined by {@link Double#equals}.
   * This method is <i>not</i> recommended when the code under test is doing any kind of arithmetic:
   * use {@link #usingTolerance} with a suitable tolerance in that case. (Remember that the exact
   * result of floating point arithmetic is sensitive to apparently trivial changes such as
   * replacing {@code (a + b) + c} with {@code a + (b + c)}, and that unless {@code strictfp} is in
   * force even the result of {@code (a + b) + c} is sensitive to the JVM's choice of precision for
   * the intermediate result.) This method is recommended when the code under test is specified as
   * either copying a value without modification from its input or returning a well-defined literal
   * or constant value. The check is actually executed by continuing the method chain. For example:
   *
   * <pre>{@code
   * assertThat(actualDoubleArray).usingExactEquality().contains(3.14159);
   * }</pre>
   *
   * <p>For convenience, some subsequent methods accept expected values as {@link Number} instances.
   * These numbers must be either of type {@link Double}, {@link Float}, {@link Integer}, or {@link
   * Long}, and if they are {@link Long} then their absolute values must not exceed 2^53 which is
   * just over 9e15. (This restriction ensures that the expected values have exact {@link Double}
   * representations: using exact equality makes no sense if they do not.)
   *
   * <ul>
   *   <li>It considers {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, and
   *       {@link Double#NaN} to be equal to themselves (contrast with {@code usingTolerance(0.0)}
   *       which does not).
   *   <li>It does <i>not</i> consider {@code -0.0} to be equal to {@code 0.0} (contrast with {@code
   *       usingTolerance(0.0)} which does).
   *   <li>The subsequent methods in the chain may throw a {@link NullPointerException} if any
   *       expected {@link Double} instance is null.
   * </ul>
   */
  public DoubleArrayAsIterable usingExactEquality() {
    return new DoubleArrayAsIterable(EXACT_EQUALITY_CORRESPONDENCE, iterableSubject());
  }

  /**
   * A partially specified check for doing assertions on the array similar to the assertions
   * supported for {@link Iterable} subjects, in which the elements of the array under test are
   * compared to expected elements using either exact or tolerant double equality: see {@link
   * #usingExactEquality} and {@link #usingTolerance}. Call methods on this object to actually
   * execute the check.
   *
   * <p>In the exact equality case, the methods on this class which take {@link Number} arguments
   * only accept certain instances: again, see {@link #usingExactEquality} for details.
   */
  public static final class DoubleArrayAsIterable
      extends IterableSubject.UsingCorrespondence<Double, Number> {

    DoubleArrayAsIterable(
        Correspondence<? super Double, Number> correspondence, IterableSubject subject) {
      super(subject, correspondence);
    }

    /**
     * As {@link #containsAtLeast(Object, Object, Object...)} but taking a primitive double array.
     */
    @CanIgnoreReturnValue
    public Ordered containsAtLeast(double[] expected) {
      return containsAtLeastElementsIn(Doubles.asList(expected));
    }

    /** As {@link #containsAnyOf(Object, Object, Object...)} but taking a primitive double array. */
    public void containsAnyOf(double[] expected) {
      containsAnyIn(Doubles.asList(expected));
    }

    /** As {@link #containsExactly(Object...)} but taking a primitive double array. */
    @CanIgnoreReturnValue
    public Ordered containsExactly(double[] expected) {
      return containsExactlyElementsIn(Doubles.asList(expected));
    }

    /**
     * As {@link #containsNoneOf(Object, Object, Object...)} but taking a primitive double array.
     */
    public void containsNoneOf(double[] excluded) {
      containsNoneIn(Doubles.asList(excluded));
    }
  }

  private IterableSubject iterableSubject() {
    return checkNoNeedToDisplayBothValues("asList()")
        .about(iterablesWithCustomDoubleToString())
        .that(Doubles.asList(checkNotNull(actual)));
  }

  /*
   * TODO(cpovirk): Should we make Doubles.asList().toString() smarter rather than do all this?
   *
   * TODO(cpovirk): Or find a general solution for this and MultimapSubject.IterableEntries. But
   * note that here we don't use _exactly_ PrimitiveDoubleArraySubject.this.toString(), as that
   * contains "double[]." Or maybe we should stop including that in
   * PrimitiveDoubleArraySubject.this.toString(), too, someday?
   */
  private Factory<IterableSubject, Iterable<?>> iterablesWithCustomDoubleToString() {
    return new Factory<IterableSubject, Iterable<?>>() {
      @Override
      public IterableSubject createSubject(FailureMetadata metadata, @Nullable Iterable<?> actual) {
        return new IterableSubjectWithInheritedToString(metadata, actual);
      }
    };
  }

  private final class IterableSubjectWithInheritedToString extends IterableSubject {

    IterableSubjectWithInheritedToString(FailureMetadata metadata, @Nullable Iterable<?> actual) {
      super(metadata, actual);
    }

    @Override
    protected String actualCustomStringRepresentation() {
      return PrimitiveDoubleArraySubject.this
          .actualCustomStringRepresentationForPackageMembersToCall();
    }
  }
}
