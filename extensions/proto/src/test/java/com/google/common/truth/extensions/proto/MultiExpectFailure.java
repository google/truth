/*
 * Copyright (c) 2018 Google, Inc.
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
package com.google.common.truth.extensions.proto;

import com.google.common.base.Preconditions;
import com.google.common.truth.ExpectFailure;
import com.google.common.truth.StandardSubjectBuilder;
import java.util.ArrayList;
import java.util.List;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A collection of {@link ExpectFailure} rules, for use in a single test case.
 *
 * <p>Users should instantiate {@code MultiExpectFailure} as a {@code @Rule}, then use {@link
 * #whenTesting()} and {@link #getFailure()} just like you would with an ordinary {@link
 * ExpectFailure} rule. Each call to {@link #whenTesting()} will clobber the previous {@link
 * #getFailure()} results.
 */
class MultiExpectFailure implements TestRule {

  private final List<ExpectFailure> expectFailures;
  private int currentIndex = -1;

  MultiExpectFailure(int size) {
    expectFailures = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      expectFailures.add(new ExpectFailure());
    }
  }

  StandardSubjectBuilder whenTesting() {
    Preconditions.checkState(
        currentIndex < expectFailures.size() - 1,
        "Not enough ExpectFailures (%s)",
        expectFailures.size());
    return expectFailures.get(++currentIndex).whenTesting();
  }

  AssertionError getFailure() {
    Preconditions.checkState(currentIndex >= 0, "Must call 'whenTesting()' first.");
    return expectFailures.get(currentIndex).getFailure();
  }

  @Override
  public Statement apply(Statement base, Description description) {
    Statement statement = base;
    for (ExpectFailure expectFailure : expectFailures) {
      statement = expectFailure.apply(statement, description);
    }
    return statement;
  }
}
