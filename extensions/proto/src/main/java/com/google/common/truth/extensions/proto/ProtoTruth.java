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
import com.google.common.truth.DelegatedVerbFactory;
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

  /**
   * Returns a {@link DelegatedVerbFactory}, akin to a {@link
   * com.google.common.truth.SubjectFactory}, which can be used to assert on multiple types of
   * Protos and collections containing them.
   */
  public static DelegatedVerbFactory<ProtoTruthDelegatedVerb> protos() {
    return ProtoTruthDelegatedVerb.factory();
  }

  public static LiteProtoSubject<?, MessageLite> assertThat(@Nullable MessageLite messageLite) {
    return assertAbout(protos()).that(messageLite);
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
    return assertAbout(protos()).that(messages);
  }

  private ProtoTruth() {}
}
