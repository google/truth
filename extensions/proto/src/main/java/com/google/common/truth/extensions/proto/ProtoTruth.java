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

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.AbstractVerb;
import com.google.common.truth.SubjectFactory;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import javax.annotation.Nullable;

/**
 * A set of static methods to begin a Truth assertion chain for protocol buffers.
 *
 * <p>Note: Usage of different failure strategies such as <em>assume</em> and <em>expect</em> should
 * rely on {@link AbstractVerb#about(SubjectFactory)} to begin a chain with those alternative
 * behaviors.
 */
public final class ProtoTruth {
  //////////////////////////////////////////////////////////////////////////////////////////////////
  // assertThat() overloads.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  public static LiteProtoSubject<?, MessageLite> assertThat(@Nullable MessageLite messageLite) {
    return assertAbout(liteProtos()).that(messageLite);
  }

  public static ProtoSubject<?, Message> assertThat(@Nullable Message message) {
    return assertAbout(protos()).that(message);
  }

  // Note: We must specify M explicitly here. The presence of the type parameter makes this method
  // signature distinct from Truth.assertThat(Iterable<?>), and allows users to import both static
  // methods without conflict. If this method instead accepted Iterable<? extends Message>, this
  // would result in method ambiguity errors.
  // See http://stackoverflow.com/a/8467804 for a more thorough explanation.
  public static <M extends Message> IterableOfProtosSubject<?, M, Iterable<M>> assertThat(
      @Nullable Iterable<M> messages) {
    return assertAbout(IterableOfProtosSubject.<M>iterablesOfProtos()).that(messages);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // SubjectFactory factories.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns a {@link SubjectFactory} for the lite version of protocol buffers. */
  public static LiteProtoSubject.Factory<?, MessageLite> liteProtos() {
    return LiteProtoSubject.liteProtos();
  }

  /** Returns a {@link SubjectFactory} for protocol buffers. */
  public static ProtoSubject.Factory<?, Message> protos() {
    return ProtoSubject.protos();
  }

  /**
   * Returns a {@link SubjectFactory} for {@link Iterable}s of protocol buffers.
   *
   * @param messageClass an explicit type specifier for the {@link SubjectFactory}.
   */
  public static <M extends Message>
      IterableOfProtosSubject.Factory<
              IterableOfProtosSubject.IterableOfMessagesSubject<M>, M, Iterable<M>>
          iterablesOfProtos(Class<M> messageClass) {
    return IterableOfProtosSubject.<M>iterablesOfProtos();
  }

  private ProtoTruth() {}
}
