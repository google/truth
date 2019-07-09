/*
 * License Copyright 2019 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
@file:JvmName("TruthSubjectTesting")

package com.google.common.truth.extensions.fields

import com.google.common.truth.FailureStrategy
import com.google.common.truth.StandardSubjectBuilder
import java.lang.RuntimeException

/**
 * A separate class of error, separate from AssertionError while testing Truth subject
 * implementations so as to not create incorrect test expectations.  VerificationException isn't a
 * known or thrown exception type, so it won't be confused for a false failure.
 *
 * WARNING: This should only ever be used to test Truth Subject implementations. General users of
 * Truth should not use this mechanism.
 */
class VerificationError(cause: Throwable) : RuntimeException(cause)

private val THROW_VERIFICATION_ERROR: FailureStrategy =
    FailureStrategy { failure -> throw VerificationError(failure) }

/**
 * When testing [com.google.common.truth.Subject] implementations, use [VERIFY].that() instead of
 * assertThat() to avoid mistaking an expected AssertionError to be a failure of the Subject
 * implementation, vs. a real test failure.
 */
@JvmField
internal val VERIFY: StandardSubjectBuilder =
    StandardSubjectBuilder.forCustomFailureStrategy(THROW_VERIFICATION_ERROR)
