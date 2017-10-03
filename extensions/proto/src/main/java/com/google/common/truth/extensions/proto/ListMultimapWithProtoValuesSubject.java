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

package com.google.common.truth.extensions.proto;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.truth.FailureMetadata;
import com.google.protobuf.Message;
import javax.annotation.Nullable;

/**
 * Type-specific extension of {@link MultimapWithProtoValuesSubject}, used to detect bad usages of
 * {@link #isEqualTo}.
 */
public class ListMultimapWithProtoValuesSubject<
        S extends ListMultimapWithProtoValuesSubject<S, K, M, C>,
        K,
        M extends Message,
        C extends ListMultimap<K, M>>
    extends MultimapWithProtoValuesSubject<S, K, M, C> {

  /** Default implementation of {@link ListMultimapWithProtoValuesSubject}. */
  public static class ListMultimapWithMessageValuesSubject<K, M extends Message>
      extends ListMultimapWithProtoValuesSubject<
          ListMultimapWithMessageValuesSubject<K, M>, K, M, ListMultimap<K, M>> {
    // See IterableOfProtosSubject.IterableOfMessagesSubject for why this class is exposed.

    ListMultimapWithMessageValuesSubject(
        FailureMetadata failureMetadata, @Nullable ListMultimap<K, M> multimap) {
      super(failureMetadata, multimap);
    }
  }

  protected ListMultimapWithProtoValuesSubject(FailureMetadata failureMetadata, C multimap) {
    super(failureMetadata, multimap);
  }

  /**
   * @deprecated {@code #isEqualTo} A SetMultimap can never compare equal with a ListMultimap if
   *     either Multimap is non-empty, because {@link java.util.Set} and {@link List} can never
   *     compare equal. Prefer {@link MultimapWithProtoValuesSubject#containsExactlyEntriesIn(
   *     com.google.common.collect.Multimap)} instead. Consult {@link
   *     com.google.common.collect.Multimap#equals} for more information.
   */
  @Deprecated
  public void isEqualTo(@Nullable SetMultimap<?, ?> other) {
    super.isEqualTo(other);
  }
}
