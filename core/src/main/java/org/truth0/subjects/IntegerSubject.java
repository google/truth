/*
 * Copyright (c) 2011 Google, Inc.
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
 * Propositions for Integer numeric subjects
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class IntegerSubject extends AnIntegerSubject<IntegerSubject, Integer> {
  public IntegerSubject(FailureStrategy failureStrategy, Integer i) {
    super(failureStrategy, i);
  }

  @Override
  protected Long getSubjectAsLong() {
    return getSubject() == null ? null : getSubject().longValue();
  }

  public static final SubjectFactory<IntegerSubject, Integer> INTEGER =
      new SubjectFactory<IntegerSubject, Integer>() {
        @Override public IntegerSubject getSubject(FailureStrategy fs, Integer target) {
          return new IntegerSubject(fs, target);
        }
      };
}
