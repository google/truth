/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0.subjects;


import org.truth0.FailureStrategy;

/**
 * Propositions for Byte numeric subjects
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class ByteSubject extends AnIntegerSubject<ByteSubject, Byte> {
  public ByteSubject(FailureStrategy failureStrategy, Byte b) {
    super(failureStrategy, b);
  }

  @Override
  protected Long getSubjectAsLong() {
    return getSubject() == null ? null : getSubject().longValue();
  }
  
  public static final SubjectFactory<ByteSubject, Byte> BYTE =
      new SubjectFactory<ByteSubject, Byte>() {
        @Override public ByteSubject getSubject(FailureStrategy fs, Byte target) {
          return new ByteSubject(fs, target);
        }
      };
}
