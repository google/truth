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

/**
 * Defines what to do when a check fails.
 *
 * <p>This type does not appear directly in a fluent assertion chain, but you choose a {@code
 * FailureStrategy} by choosing which method to call at the beginning of the chain.
 *
 * <p>Built-in strategies include:
 *
 * <ul>
 *   <li>{@linkplain Truth#assert_ assertions}
 *   <li>{@linkplain Expect expectations}
 *   <li>{@linkplain TruthJUnit#assume assumptions}
 *   <li>(and some useful only to people who implement custom subjects, described below)
 * </ul>
 *
 * <p>For more information about the fluent chain, see <a href="https://google.github.io/truth/faq#full-chain">this
 * FAQ entry</a>.
 *
 * <h3>For people extending Truth</h3>
 *
 * <p>Custom {@code FailureStrategy} implementations are unusual. If you think you need one,
 * consider these alternatives:
 *
 * <ul>
 *   <li>To test a custom subject, use {@link ExpectFailure}.
 *   <li>To create subjects for other objects related to your actual value (for chained assertions),
 *       use {@link Subject#check}, which preserves the existing {@code FailureStrategy} and other
 *       context.
 *   <li>To return a no-op subject after a previous assertion has failed (for chained assertions),
 *       use {@link Subject#ignoreCheck}
 * </ul>
 *
 * <p>When you really do need to create your own strategy, rather than expose your {@code
 * FailureStrategy} instance to users, expose a {@link StandardSubjectBuilder} instance using {@link
 * StandardSubjectBuilder#forCustomFailureStrategy
 * StandardSubjectBuilder.forCustomFailureStrategy(STRATEGY)}.
 */
public interface FailureStrategy {
  /**
   * Handles a failure. The parameter is an {@code AssertionError} or subclass thereof, and it
   * contains information about the failure, which may include:
   *
   * <ul>
   *   <li>message: {@link Throwable#getMessage getMessage()}
   *   <li>cause: {@link Throwable#getCause getCause()}
   *   <li>actual and expected values: {@link org.junit.ComparisonFailure#getActual}, {@link
   *       org.junit.ComparisonFailure#getExpected}
   *   <li>stack trace: {@link Throwable#getStackTrace}
   * </ul>
   *
   * <!-- TODO(cpovirk): suppressed exceptions someday? -->
   *
   * <p>We encourage implementations to record as much of this information as practical in the
   * exceptions they may throw or the other records they may make.
   */
  void fail(AssertionError failure);
}
