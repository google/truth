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

import com.google.protobuf.MessageLite;
import com.google.errorprone.annotations.CheckReturnValue;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * A set of static methods to begin a Truth assertion chain for the lite version of protocol
 * buffers.
 *
 * <p>This class implements a subset of what {@code ProtoTruth} provides, so if you are already
 * using {@code ProtoTruth}, you should not import this class. {@code LiteProtoTruth} is only useful
 * if you cannot depend on {@code ProtoTruth} for dependency management reasons.
 *
 * <p>Note: Usage of different failure strategies such as <em>assume</em> and <em>expect</em> should
 * rely on {@linkplain com.google.common.truth.StandardSubjectBuilder#about(Subject.Factory)
 * about(liteProtos())} to begin a chain with those alternative behaviors.
 */
@CheckReturnValue
public final class LiteProtoTruth {
  public static LiteProtoSubject<?, MessageLite> assertThat(@NullableDecl MessageLite messageLite) {
    return assertAbout(liteProtos()).that(messageLite);
  }

  public static LiteProtoSubject.Factory<?, MessageLite> liteProtos() {
    return LiteProtoSubject.liteProtos();
  }

  private LiteProtoTruth() {}
}
