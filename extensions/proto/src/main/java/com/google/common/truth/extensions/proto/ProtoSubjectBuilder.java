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
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.truth.CustomSubjectBuilder;
import com.google.common.truth.FailureMetadata;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * {@link CustomSubjectBuilder} which aggregates all Proto-related {@link
 * com.google.common.truth.Subject} classes into a single place.
 *
 * <p>To obtain an instance, call {@code assertAbout(ProtoTruth.protos())}.
 */
public final class ProtoSubjectBuilder extends CustomSubjectBuilder {

  /** Factory for ProtoSubjectBuilder. */
  private static class Factory implements CustomSubjectBuilder.Factory<ProtoSubjectBuilder> {
    private static final Factory INSTANCE = new Factory();

    @Override
    public ProtoSubjectBuilder createSubjectBuilder(FailureMetadata failureMetadata) {
      return new ProtoSubjectBuilder(failureMetadata);
    }
  }

  static CustomSubjectBuilder.Factory<ProtoSubjectBuilder> factory() {
    return Factory.INSTANCE;
  }

  private ProtoSubjectBuilder(FailureMetadata failureMetadata) {
    super(failureMetadata);
  }

  public LiteProtoSubject<?, MessageLite> that(@Nullable MessageLite messageLite) {
    return new LiteProtoSubject.MessageLiteSubject(metadata(), messageLite);
  }

  public ProtoSubject<?, Message> that(@Nullable Message message) {
    return new ProtoSubject.MessageSubject(metadata(), message);
  }

  public <M extends Message> IterableOfProtosSubject<?, M, Iterable<M>> that(
      @Nullable Iterable<M> messages) {
    return new IterableOfProtosSubject.IterableOfMessagesSubject<M>(metadata(), messages);
  }

  public <K, M extends Message> MapWithProtoValuesSubject<?, K, M, Map<K, M>> that(
      @Nullable Map<K, M> map) {
    return new MapWithProtoValuesSubject.MapWithMessageValuesSubject<K, M>(metadata(), map);
  }

  public <K, M extends Message> MultimapWithProtoValuesSubject<?, K, M, Multimap<K, M>> that(
      @Nullable Multimap<K, M> map) {
    return new MultimapWithProtoValuesSubject.MultimapWithMessageValuesSubject<K, M>(
        metadata(), map);
  }

  public <K, M extends Message>
      ListMultimapWithProtoValuesSubject<?, K, M, ListMultimap<K, M>> that(
          @Nullable ListMultimap<K, M> map) {
    return new ListMultimapWithProtoValuesSubject.ListMultimapWithMessageValuesSubject<K, M>(
        metadata(), map);
  }

  public <K, M extends Message> SetMultimapWithProtoValuesSubject<?, K, M, SetMultimap<K, M>> that(
      @Nullable SetMultimap<K, M> map) {
    return new SetMultimapWithProtoValuesSubject.SetMultimapWithMessageValuesSubject<K, M>(
        metadata(), map);
  }

}
