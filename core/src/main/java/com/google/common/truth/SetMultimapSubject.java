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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

import javax.annotation.Nullable;

/**
 * Type-specific extensions of {@link com.google.common.collect.Multimap} subjects for
 * {@link com.google.common.collect.SetMultimap} subjects.
 *
 * @author Daniel Ploch
 */
public class SetMultimapSubject<
        S extends SetMultimapSubject<S, K, V, M>, K, V, M extends SetMultimap<K, V>>
    extends MultimapSubject<S, K, V, M> {
  SetMultimapSubject(FailureStrategy failureStrategy, @Nullable M multimap) {
    super(failureStrategy, multimap);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <K, V, M extends SetMultimap<K, V>>
      SetMultimapSubject<? extends SetMultimapSubject<?, K, V, M>, K, V, M> create(
          FailureStrategy failureStrategy, @Nullable SetMultimap<K, V> multimap) {
    return new SetMultimapSubject(failureStrategy, multimap);
  }

  // TODO(user):  Add a valuesForKey override for SetSubject if we make SetSubject

  /**
   * @deprecated {@code #isEqualTo} A ListMultimap can never compare equal with a SetMultimap if
   *      either Multimap is non-empty, because {@link java.util.List} and {@link java.util.Set}
   *      can never compare equal.  Prefer
   *      {@link MultimapSubject#containsExactly(com.google.common.collect.Multimap)} instead.
   *      Consult {@link com.google.common.collect.Multimap#equals} for more information.
   */
  @Deprecated
  public void isEqualTo(@Nullable ListMultimap<?, ?> other) {
    super.isEqualTo(other);
  }
}
