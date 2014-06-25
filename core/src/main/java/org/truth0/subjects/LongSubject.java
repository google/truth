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
 * Propositions for Long numeric subjects
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class LongSubject extends AnIntegerSubject<LongSubject, Long> {
  public LongSubject(FailureStrategy failureStrategy, Long l) {
    super(failureStrategy, l);
  }

  @Override
  protected Long getSubjectAsLong() {
    return getSubject();
  }

  public static final SubjectFactory<LongSubject, Long> LONG =
      new SubjectFactory<LongSubject, Long>() {
        @Override public LongSubject getSubject(FailureStrategy fs, Long target) {
          return new LongSubject(fs, target);
        }
      };
}
