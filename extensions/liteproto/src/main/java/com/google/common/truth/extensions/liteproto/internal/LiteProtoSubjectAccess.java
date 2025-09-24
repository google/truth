/*
 * Copyright (c) 2025 Google, Inc.
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

package com.google.common.truth.extensions.liteproto.internal;

import com.google.common.truth.extensions.liteproto.LiteProtoSubject;

/**
 * Provides access to package-private methods in LiteProtoSubject for use by
 * other Truth extensions.
 *
 * <p><b>This is an internal API and should not be used by client code.</b>
 * This class may be removed or changed at any time without notice.
 */
public final class LiteProtoSubjectAccess {

  /**
   * Accessor interface for LiteProtoSubject internal methods.
   * Set by LiteProtoSubject during class initialization.
   */
  public interface Accessor {
    String getCustomStringRepresentation(LiteProtoSubject subject);
  }

  private static Accessor accessor;

  /**
   * Sets the accessor. This is called by LiteProtoSubject during class initialization.
   *
   * @param accessor the accessor implementation
   * @throws IllegalStateException if the accessor is already set
   */
  public static void setAccessor(Accessor accessor) {
    if (LiteProtoSubjectAccess.accessor != null) {
      throw new IllegalStateException("Accessor already set");
    }
    LiteProtoSubjectAccess.accessor = accessor;
  }

  /**
   * Gets the custom string representation for the given LiteProtoSubject.
   *
   * @param subject the LiteProtoSubject instance
   * @return the custom string representation
   * @throws IllegalStateException if no accessor has been set
   */
  public static String getCustomStringRepresentation(LiteProtoSubject subject) {
    if (accessor == null) {
      throw new IllegalStateException("No accessor set");
    }
    return accessor.getCustomStringRepresentation(subject);
  }

  private LiteProtoSubjectAccess() {}
}