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

import static com.google.common.truth.StringUtil.messageFor;

import com.google.common.annotations.GwtIncompatible;
import org.junit.internal.AssumptionViolatedException;

/**
 * Truth - a proposition framework for tests, supporting JUnit style assertion and assumption
 * semantics in a fluent style.
 *
 * <p>TruthJUnit contains a junit-specific "failure strategy" known as an assumption. An assumption
 * is a proposition that, if the proposition is false, aborts (skips) the test. This is especially
 * useful in JUnit theories or parameterized tests, or other combinatorial tests where some subset
 * of the combinations are simply not applicable for testing.
 *
 * <p>TruthJUnit is the entry point for assumptions, via the {@link #assume()} method.
 *
 * <p>eg:
 *
 * <pre>{@code
 * import static com.google.common.truth.Truth.assertThat;
 * import static com.google.common.truth.TruthJUnit.assume;
 *
 * public void @Test testFoosAgainstBars {
 *   assume().that(foo).isNotNull();
 *   assume().that(bar).isNotNull();
 *   assertThat(foo.times(bar)).isEqualTo(blah);
 * }
 * }</pre>
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@GwtIncompatible("JUnit4")
public final class TruthJUnit {
  @GwtIncompatible("JUnit4")
  private static final FailureStrategy THROW_ASSUMPTION_ERROR =
      new AbstractFailureStrategy() {
        @Override
        public void failComparing(
            String message, CharSequence expected, CharSequence actual, Throwable cause) {
          fail(messageFor(message, expected, actual), cause);
        }

        @Override
        public void fail(String message, Throwable cause) {
          throw new ThrowableAssumptionViolatedException(message, cause);
        }
      };

  @GwtIncompatible("JUnit4")
  public static final FailureStrategy throwAssumptionError() {
    return THROW_ASSUMPTION_ERROR;
  }

  @GwtIncompatible("JUnit4")
  private static final TestVerb ASSUME = new TestVerb(THROW_ASSUMPTION_ERROR);

  @GwtIncompatible("JUnit4")
  public static final TestVerb assume() {
    return ASSUME;
  }

  // TODO(diamondm): remove this and use org.junit.AssumptionViolatedException once we're on v4.12
  @GwtIncompatible("JUnit4")
  private static class ThrowableAssumptionViolatedException extends AssumptionViolatedException {
    public ThrowableAssumptionViolatedException(String message, Throwable throwable) {
      super(message);
      if (throwable != null) initCause(throwable);
    }
  }

  private TruthJUnit() {}
}
