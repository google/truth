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
package com.google.common.truth;

import static java.util.Arrays.asList;

import com.google.common.collect.Iterables;

/**
 * Helper class that wraps a collection of {@link Fact} instances to make them easier to build.
 *
 * @author Pete Gillin
 */
final class Facts {

  private final Iterable<Fact> facts;

  /** Returns an instance wrapping the given facts. */
  static Facts facts(Fact... facts) {
    return new Facts(asList(facts));
  }

  /** Returns an instance wrapping the given facts. */
  static Facts facts(Iterable<Fact> facts) {
    return new Facts(facts);
  }

  private Facts(Iterable<Fact> facts) {
    this.facts = facts;
  }

  /** Returns the facts wrapped by this instance. */
  Iterable<Fact> asIterable() {
    return facts;
  }

  /**
   * Returns an instance concatenating the facts wrapped by the current instance followed by the
   * given facts.
   */
  Facts and(Facts moreFacts) {
    return new Facts(Iterables.concat(facts, moreFacts.asIterable()));
  }

  /**
   * Returns an instance concatenating the facts wrapped by the current instance followed by the
   * given facts.
   */
  Facts and(Fact... moreFacts) {
    return new Facts(Iterables.concat(facts, asList(moreFacts)));
  }
}
